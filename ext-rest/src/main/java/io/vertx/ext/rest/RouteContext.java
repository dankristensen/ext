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

import io.vertx.codegen.annotations.CacheReturn;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;

import java.util.Map;

/**
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
@VertxGen
public interface RouteContext {

  @CacheReturn
  HttpServerRequest request();

  @CacheReturn
  HttpServerResponse response();

  @CacheReturn
  Map<String, String> contextData();

  // TODO
  // 1. Instead of a map, add all the put/get methods like in JSONObject

  String getString(String key);

  Throwable failure();

  // If in an exception handler, then next will call the next exception handler if one
  // if not the default exception handler will be called.
  void next();
}
