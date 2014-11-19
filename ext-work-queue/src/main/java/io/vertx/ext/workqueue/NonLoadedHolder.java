package io.vertx.ext.workqueue;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;

class NonLoadedHolder implements MessageHolder {

	private final Message<JsonObject> message;

	NonLoadedHolder(Message<JsonObject> message) {
		this.message = message;
	}

	@Override
	public JsonObject getBody() {
		return message.body();
	}

	@Override
	public void reply(JsonObject reply, Handler<AsyncResult<Message<JsonObject>>> replyReplyHandler) {
		message.reply(reply, replyReplyHandler);
	}
}