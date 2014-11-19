package io.vertx.ext.workqueue;

import static org.hamcrest.CoreMatchers.is;
import io.vertx.core.AsyncResult;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import io.vertx.test.core.VertxTestBase;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

public class WorkQueueUnitTest extends VertxTestBase {

	private static final String TEST_WORK_QUEUE_NAME = "test-work";

	@Override
	public void setUp() throws Exception {
		super.setUp();
		DeploymentOptions options = new DeploymentOptions();
		options.setConfig(getConfig());
		CountDownLatch deployLatch = new CountDownLatch(1);

		vertx.deployVerticle("java:" + WorkQueue.class.getName(), options, ar -> {
			if (ar.succeeded()) {
				deployLatch.countDown();
			}
		});
		deployLatch.await(1, TimeUnit.MINUTES);
	}

	@Test
	public void shouldRegisterCorrectly() {
		JsonObject jsonObject = new JsonObject();
		jsonObject.put("processor", "processor1");
		vertx.eventBus().send(TEST_WORK_QUEUE_NAME + ".register", jsonObject, new Handler<AsyncResult<Message<JsonObject>>>() {
			@Override
			public void handle(AsyncResult<Message<JsonObject>> event) {
				assertThat(event.result().body().getString("status"), is("ok"));
				testComplete();
			}
		});
		await();
	}

	@Test
	public void shouldFailRegisteringIfNoProcessorIsSupplied() {
		JsonObject jsonObject = new JsonObject();
		vertx.eventBus().send(TEST_WORK_QUEUE_NAME + ".register", jsonObject, new Handler<AsyncResult<Message<JsonObject>>>() {
			@Override
			public void handle(AsyncResult<Message<JsonObject>> event) {
				assertThat(event.result().body().getString("status"), is("error"));
				testComplete();
			}
		});
		await();
	}

	@Test
	public void shouldUnregisterCorrectly() {
		JsonObject jsonObject = new JsonObject();
		jsonObject.put("processor", "processor1");
		vertx.eventBus().send(TEST_WORK_QUEUE_NAME + ".unregister", jsonObject, new Handler<AsyncResult<Message<JsonObject>>>() {
			@Override
			public void handle(AsyncResult<Message<JsonObject>> event) {
				assertThat(event.result().body().getString("status"), is("ok"));
				testComplete();
			}
		});
		await();
	}

	@Test
	public void shouldFailUnregisteringIfNoProcessorIsSupplied() {
		JsonObject jsonObject = new JsonObject();
		vertx.eventBus().send(TEST_WORK_QUEUE_NAME + ".unregister", jsonObject, new Handler<AsyncResult<Message<JsonObject>>>() {
			@Override
			public void handle(AsyncResult<Message<JsonObject>> event) {
				assertThat(event.result().body().getString("status"), is("error"));
				testComplete();
			}
		});
		await();
	}

	@Test
	public void shouldSendAndProccessWorkCorrectly() {
		final JsonObject sendObject = new JsonObject();
		sendObject.put("jobName", "handleThis");
		startWorkProcessor(sendObject);
		JsonObject jsonObject = new JsonObject();
		jsonObject.put("processor", "processor1");
		vertx.eventBus().send(TEST_WORK_QUEUE_NAME + ".register", jsonObject, new Handler<AsyncResult<Message<JsonObject>>>() {
			@Override
			public void handle(AsyncResult<Message<JsonObject>> event) {
				assertThat(event.result().body().getString("status"), is("ok"));
				vertx.eventBus().<JsonObject> send(TEST_WORK_QUEUE_NAME, sendObject, ar -> {
					testComplete();
				});
			}
		});
		await();
	}

	private JsonObject getConfig() {
		JsonObject config = new JsonObject();
		config.put("address", TEST_WORK_QUEUE_NAME);
		return config;
	}

	private void startWorkProcessor(JsonObject expectedInput) {
		vertx.eventBus().<JsonObject> consumer("processor1", ar -> {
			JsonObject body = ar.body();
			assertThat(body, is(expectedInput));
			ar.reply(new JsonObject());
		});
	}
}
