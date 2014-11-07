/*
 * Copyright 2014 Red Hat, Inc.
 *
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  and Apache License v2.0 which accompanies this distribution.
 *
 *  The Eclipse Public License is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  The Apache License v2.0 is available at
 *  http://www.opensource.org/licenses/apache2.0.php
 *
 *  You may elect to redistribute this code under either of these licenses.
 */

package io.vertx.ext.rest;

import io.vertx.codegen.annotations.Fluent;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerRequest;

import java.util.List;

/**
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
@VertxGen
public interface Router extends Handler<RouteContext> {

  static Router router() {
    return null;
  }

  @Fluent
  Router accept(HttpServerRequest request);

  Route route();

  // Convenience methods where the method/path is already specified
  Route route(HttpMethod method, String path);

  // Convenience methods where the method/regex is already specified
  Route routeWithRegex(HttpMethod method, String regex);

  // Add extra convenience methods e.g. for consumes/produces

  List<Route> getRoutes();

  Router clear();

}
