import io.vertx.groovy.core.Vertx
import rx.Observer;

Vertx vertx = Vertx.vertx();
Observer<Long> observer = new Observer<Long>() {
  @Override
  void onCompleted() {
    test.fail();
  }

  @Override
  void onError(Throwable e) {
    test.fail(e.message);
  }

  @Override
  void onNext(Long l) {
    test.testComplete();
  }
}
vertx.setTimer(1, observer.toHandler());
test.await();