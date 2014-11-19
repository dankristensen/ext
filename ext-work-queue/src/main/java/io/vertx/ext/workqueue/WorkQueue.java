/*
 * Copyright 2011-2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.vertx.ext.workqueue;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.Message;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.impl.LoggerFactory;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.TimeUnit;

/**
 * Work Queue Bus Module
 * <p>
 * Please see the busmods manual for a full description
 * <p>
 *
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
public class WorkQueue extends AbstractVerticle {
	private static final Logger logger = LoggerFactory.getLogger(WorkQueue.class);
	// LHS is typed as ArrayList to ensure high perf offset based index operations
	private final Queue<String> processors = new LinkedList<>();
	private final Queue<MessageHolder> messages = new LinkedList<>();

	private long processTimeout;
	private String persistorAddress;
	private String collection;
	private MessageConsumer<JsonObject> registerConsumer;
	private MessageConsumer<JsonObject> unregisterConsumer;
	private MessageConsumer<JsonObject> sendConsumer;

	@Override
	public void start() {
		JsonObject config = config();
		String address = config.getString("address");
		if (address == null) {
			throw new IllegalArgumentException("Missing address");
		}
		processTimeout = config.getLong("process_timeout", TimeUnit.MINUTES.toMillis(5));
		persistorAddress = config.getString("persistor_address", null);
		collection = config.getString("collection", null);

		if (persistorAddress != null) {
			loadMessages();
		}

		Handler<Message<JsonObject>> registerHandler = new Handler<Message<JsonObject>>() {
			@Override
			public void handle(Message<JsonObject> message) {
				doRegister(message);
			}
		};
		registerConsumer = vertx.eventBus().consumer(address + ".register", registerHandler);
		Handler<Message<JsonObject>> unregisterHandler = new Handler<Message<JsonObject>>() {
			@Override
			public void handle(Message<JsonObject> message) {
				doUnregister(message);
			}
		};
		unregisterConsumer = vertx.eventBus().consumer(address + ".unregister", unregisterHandler);
		sendConsumer = vertx.eventBus().consumer(address, new Handler<Message<JsonObject>>() {
			@Override
			public void handle(Message<JsonObject> message) {
				doSend(message);
			}
		});
	}

	@Override
	public void stop() throws Exception {
		registerConsumer.unregister();
		unregisterConsumer.unregister();
		sendConsumer.unregister();
	}

	private void checkWork() {
		if (!messages.isEmpty() && !processors.isEmpty()) {
			final MessageHolder message = messages.poll();
			final String address = processors.poll();
			final long timeoutID = vertx.setTimer(processTimeout, new Handler<Long>() {
				@Override
				public void handle(Long id) {
					// Processor timed out - put message back on queue
					logger.warn("Processor timed out, message will be put back on queue");
					messages.add(message);
				}
			});
			vertx.eventBus().send(address, message.getBody(), new Handler<AsyncResult<Message<JsonObject>>>() {
				@Override
				public void handle(AsyncResult<Message<JsonObject>> reply) {
					messageReplied(message, reply, address, timeoutID);
				}
			});
		}
	}

	// A reply has been received from the processor
	private void messageReplied(final MessageHolder message, final AsyncResult<Message<JsonObject>> reply,
			final String processorAddress,
			final long timeoutID) {
		if (reply.result().replyAddress() != null) {
			// The reply itself has a reply specified so we don't consider the message processed just yet
			message.reply(reply.result().body(), new Handler<AsyncResult<Message<JsonObject>>>() {
				@Override
				public void handle(final AsyncResult<Message<JsonObject>> replyReply) {
					reply.result().reply(replyReply.result().body(), new Handler<AsyncResult<Message<JsonObject>>>() {
						@Override
						public void handle(AsyncResult<Message<JsonObject>> replyReplyReply) {
							messageReplied(new NonLoadedHolder(replyReply.result()), replyReplyReply, processorAddress, timeoutID);
						}
					});
				}
			});
		} else {
			if (persistorAddress != null) {
				JsonObject msg = new JsonObject().put("action", "delete").put("collection", collection)
						.put("matcher", message.getBody());
				vertx.eventBus().send(persistorAddress, msg, new Handler<AsyncResult<Message<JsonObject>>>() {
					@Override
					public void handle(AsyncResult<Message<JsonObject>> replyReply) {
						if (!replyReply.result().body().getString("status").equals("ok")) {
							logger.error("Failed to delete document from queue: " + replyReply.result().body().getString("message"));
						}
						messageProcessed(timeoutID, processorAddress, message, reply.result());
					}
				});
			} else {
				messageProcessed(timeoutID, processorAddress, message, reply.result());
			}
		}
	}

	// The conversation between the sender and the processor has ended, so we can add the processor back on the queue
	private void messageProcessed(long timeoutID, String processorAddress, MessageHolder message,
			Message<JsonObject> reply) {
		// The processor
		// can go back on the queue
		vertx.cancelTimer(timeoutID);
		processors.add(processorAddress);
		message.reply(reply.body(), null);
		checkWork();
	}

	private void doRegister(Message<JsonObject> message) {
		String processor = getMandatoryString("processor", message);
		if (processor == null) {
			return;
		}
		processors.add(processor);
		checkWork();
		sendOK(message);
	}

	private void doUnregister(Message<JsonObject> message) {
		String processor = getMandatoryString("processor", message);
		if (processor == null) {
			return;
		}
		processors.remove(processor);
		sendOK(message);
	}

	private void doSend(final Message<JsonObject> message) {
		if (persistorAddress != null) {
			JsonObject msg = new JsonObject().put("action", "save").put("collection", collection)
					.put("document", message.body());
			vertx.eventBus().send(persistorAddress, msg, new Handler<AsyncResult<Message<JsonObject>>>() {
				@Override
				public void handle(AsyncResult<Message<JsonObject>> reply) {
					if (reply.result().body().getString("status").equals("ok")) {
						actualSend(message);
					} else {
						sendAcceptedReply(message.body(), "error", reply.result().body().getString("message"));
						sendError(message, reply.result().body().getString("message"));
					}
				}
			});
		} else {
			actualSend(message);
		}
	}

	private void sendAcceptedReply(JsonObject body, String status, String message) {
		String acceptedReply = body.getString("accepted-reply");
		if (acceptedReply != null) {
			JsonObject repl = new JsonObject().put("status", status);
			if (message != null) {
				repl.put("message", message);
			}
			vertx.eventBus().send(acceptedReply, repl);
		}
	}

	private void actualSend(Message<JsonObject> message) {
		messages.add(new NonLoadedHolder(message));
		// Been added to the queue so reply if appropriate
		sendAcceptedReply(message.body(), "accepted", null);
		checkWork();
	}

	private String getMandatoryString(String field, Message<JsonObject> message) {
		String val = message.body().getString(field);
		if (val == null) {
			sendError(message, field + " must be specified");
		}
		return val;
	}

	private void sendOK(Message<JsonObject> message) {
		sendOK(message, null);
	}

	private void sendOK(Message<JsonObject> message, JsonObject json) {
		sendStatus("ok", message, json);
	}

	private void sendStatus(String status, Message<JsonObject> message, JsonObject json) {
		if (json == null) {
			json = new JsonObject();
		}
		json.put("status", status);
		message.reply(json);
	}

	private void sendError(Message<JsonObject> message, String error) {
		sendError(message, error, null);
	}

	private void sendError(Message<JsonObject> message, String error, Exception e) {
		logger.error(error, e);
		JsonObject json = new JsonObject().put("status", "error").put("message", error);
		message.reply(json);
	}

	// Load all the message into memory
	// TODO - we could limit the amount we load at startup
	private void loadMessages() {
		JsonObject msg = new JsonObject().put("action", "find").put("collection", collection)
				.put("matcher", new JsonObject());
		vertx.eventBus().send(persistorAddress, msg, createLoadReplyHandler());
	}

	private void processLoadBatch(JsonArray toLoad) {
		for (Object obj : toLoad) {
			if (obj instanceof JsonObject) {
				messages.add(new LoadedHolder((JsonObject) obj));
			}
		}
		checkWork();
	}

	private Handler<AsyncResult<Message<JsonObject>>> createLoadReplyHandler() {
		return new Handler<AsyncResult<Message<JsonObject>>>() {
			@Override
			public void handle(AsyncResult<Message<JsonObject>> reply) {
				processLoadBatch(reply.result().body().getJsonArray("results"));
				if (reply.result().body().getString("status").equals("more-exist")) {
					// Get next batch
					reply.result().reply((JsonObject) null, createLoadReplyHandler());
				}
			}
		};
	}

}
