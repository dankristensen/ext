package io.vertx.ext.workqueue;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;

interface MessageHolder {
	JsonObject getBody();

	void reply(JsonObject reply, Handler<AsyncResult<Message<JsonObject>>> replyReplyHandler);
}