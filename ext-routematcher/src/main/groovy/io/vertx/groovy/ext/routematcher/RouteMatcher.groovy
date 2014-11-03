/*
 * Copyright 2014 Red Hat, Inc.
 *
 * Red Hat licenses this file to you under the Apache License, version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License.  You may obtain a copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package io.vertx.groovy.ext.routematcher;
import groovy.transform.CompileStatic
import io.vertx.groovy.core.http.HttpServerRequest
import io.vertx.core.http.HttpMethod
import io.vertx.core.Handler
/**
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
@CompileStatic
public class RouteMatcher {
  final def io.vertx.ext.rest.RouteMatcher delegate;
  public RouteMatcher(io.vertx.ext.rest.RouteMatcher delegate) {
    this.delegate = delegate;
  }
  public Object getDelegate() {
    return delegate;
  }
  public static RouteMatcher routeMatcher() {
    def ret= RouteMatcher.FACTORY.apply(io.vertx.ext.rest.RouteMatcher.routeMatcher());
    return ret;
  }
  public RouteMatcher accept(HttpServerRequest request) {
    this.delegate.accept((io.vertx.core.http.HttpServerRequest)request.getDelegate());
    return this;
  }
  /**
   * Specify a handler that will be called for a matching request
   * @param method - the HTTP method
   * @param pattern The simple pattern
   * @param handler The handler to call
   */
  public RouteMatcher matchMethod(HttpMethod method, String pattern, Handler<HttpServerRequest> handler) {
    this.delegate.matchMethod(method, pattern, new Handler<io.vertx.core.http.HttpServerRequest>() {
      public void handle(io.vertx.core.http.HttpServerRequest event) {
        handler.handle(HttpServerRequest.FACTORY.apply(event));
      }
    });
    return this;
  }
  /**
   * Specify a handler that will be called for all HTTP methods
   * @param pattern The simple pattern
   * @param handler The handler to call
   */
  public RouteMatcher all(String pattern, Handler<HttpServerRequest> handler) {
    this.delegate.all(pattern, new Handler<io.vertx.core.http.HttpServerRequest>() {
      public void handle(io.vertx.core.http.HttpServerRequest event) {
        handler.handle(HttpServerRequest.FACTORY.apply(event));
      }
    });
    return this;
  }
  /**
   * Specify a handler that will be called for a matching request
   * @param method - the HTTP method
   * @param pattern The simple pattern
   * @param handler The handler to call
   */
  public RouteMatcher matchMethodWithRegEx(HttpMethod method, String pattern, Handler<HttpServerRequest> handler) {
    this.delegate.matchMethodWithRegEx(method, pattern, new Handler<io.vertx.core.http.HttpServerRequest>() {
      public void handle(io.vertx.core.http.HttpServerRequest event) {
        handler.handle(HttpServerRequest.FACTORY.apply(event));
      }
    });
    return this;
  }
  /**
   * Specify a handler that will be called for all HTTP methods
   * @param regex A regular expression
   * @param handler The handler to call
   */
  public RouteMatcher allWithRegEx(String regex, Handler<HttpServerRequest> handler) {
    this.delegate.allWithRegEx(regex, new Handler<io.vertx.core.http.HttpServerRequest>() {
      public void handle(io.vertx.core.http.HttpServerRequest event) {
        handler.handle(HttpServerRequest.FACTORY.apply(event));
      }
    });
    return this;
  }
  /**
   * Specify a handler that will be called when no other handlers match.
   * If this handler is not specified default behaviour is to return a 404
   */
  public RouteMatcher noMatch(Handler<HttpServerRequest> handler) {
    this.delegate.noMatch(new Handler<io.vertx.core.http.HttpServerRequest>() {
      public void handle(io.vertx.core.http.HttpServerRequest event) {
        handler.handle(HttpServerRequest.FACTORY.apply(event));
      }
    });
    return this;
  }

  static final java.util.function.Function<io.vertx.ext.rest.RouteMatcher, RouteMatcher> FACTORY = io.vertx.lang.groovy.Factories.createFactory() {
    io.vertx.ext.rest.RouteMatcher arg -> new RouteMatcher(arg);
  };
}
