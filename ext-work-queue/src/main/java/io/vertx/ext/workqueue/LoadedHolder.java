package io.vertx.ext.workqueue;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;

class LoadedHolder implements MessageHolder {

	private final JsonObject body;

	LoadedHolder(JsonObject body) {
		this.body = body;
	}

	@Override
	public JsonObject getBody() {
		return body;
	}

	@Override
	public void reply(JsonObject reply, Handler<AsyncResult<Message<JsonObject>>> replyReplyHandler) {
		// Do nothing - we are loaded from storage so the sender has long gone
	}
}