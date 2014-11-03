package io.vertx.ext.rx.groovy;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.ext.rx.java.*;
import io.vertx.groovy.core.Vertx;
import io.vertx.groovy.core.streams.ReadStream;
import rx.Observable;
import rx.Observer;
import rx.Scheduler;

/**
 * A set of Groovy extensions for Rxifying the Groovy API.
 *
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class RxGroovyMethods {

  public static <T> Observable<T> toObservable(ReadStream<T> stream) {
    return Observable.create(new HandlerAdapter<>(stream));
  }

  public static <T>Handler<AsyncResult<T>> toHandler(Observer<T> observer) {
    return io.vertx.ext.rx.java.RxHelper.toHandler(observer);
  }

  public static Scheduler scheduler(Vertx vertx) {
    return RxHelper.scheduler((io.vertx.core.Vertx) vertx.getDelegate());
  }
}
