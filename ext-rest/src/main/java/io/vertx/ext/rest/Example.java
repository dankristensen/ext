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

import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;

/**
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
public class Example {

//  public static void main(String[] args) {
//    new Example().run();
//  }
//
//  private void run() {
//    try {
//     // subRoutes();
//
//
//    } catch (Exception e) {
//      e.printStackTrace();
//    }
//  }

  private void routes() {

    Router router = Router.router();

    // This handler would be called for ALL requests
    router.route().handler(ctx -> {

      System.out.println("received request: " + ctx.request().path());

      // Call the next one in the chain
      ctx.next();
    });

    // This handler would be called for ALL requests to the path "/foo/bar"
    router.route().setPath("foo/bar").handler(ctx -> {

      ctx.request().response().end("foo!");

      // We don't call next() so no more handlers will be called

    });

    // Like the above but with embedded params
    router.route().setPath("foo/bar/:id").handler(ctx -> {

      ctx.request().response().end("foo " + ctx.request().params().get("id"));

      // We don't call next() so no more handlers will be called

    });

    // Like the above but with regex
    router.route().setPathWithRegex("foo//bar/[abc]?").handler(ctx -> {

      ctx.request().response().end("foo " + ctx.request().params().get("id"));

      // We don't call next() so no more handlers will be called

    });

    // This handler would be called for ALL GET requests to the path "/foo/bar"
    router.route().setPath("foo/bar").setMethod(HttpMethod.GET).handler(ctx -> {

      ctx.request().response().end("foo!");

      // We don't call next() so no more handlers will be called

    });

    // This handler would be called for ALL GET or POST requests to the path "/foo/bar"
    router.route().setPath("foo/bar").addMethod(HttpMethod.GET).addMethod(HttpMethod.POST).handler(ctx -> {

      ctx.request().response().end("foo!");

      // We don't call next() so no more handlers will be called

    });

    // This handler would be called for any POSTs to the uri with content-type "application/json"
    router.route().setPath("foo/bar/order").setMethod(HttpMethod.POST).addConsumes("application/json").handler(ctx -> {

      JsonObject body = ctx.bodyJson();



    });

    // This handler would be called for any POSTs to the uri that have an Accept header that contains "application/json"
    router.route().setPath("foo/bar/order").setMethod(HttpMethod.POST).addProduces("application/json").handler(ctx -> {


    });

    // Handlers can have an order in the chain. Default order is the order they are added, but this can be explicit, e.g.

    // Handler at the head of the chain
    router.route().setOrder(0).handler(ctx -> {

    });

    // Handler at end of the chain
    router.route().setLast().handler(ctx -> {

    });

    // Routes can also be temporarily disabled and enabled
    Route route = router.route().setPath("foo/bar").handler(ctx -> {

    });
    route.disable();
    route.enable();

    // And they can be removed altogether
    route.remove();

    // Set a 404 handler - this will be called if not handled elsewhere

    // 404 handler
    router.route().setLast().handler(ctx -> {
      ctx.request().response().setStatusCode(404).end();
    });

    // exception handler
    router.route().exceptionHandler(ctx -> {
      Throwable t = ctx.failure();
      ctx.request().response().setStatusCode(500).write("OOOps something went wrong! " + t.getMessage()).end();
    });

    // Sub routes!

    // Top level router:

    router = Router.router();

    // And a sub router:

    Router subRouter = Router.router();

    router.route().setPath("/subapp").handler(subRouter);

    subRouter.route().setPath("/sub/app/blah").handler(ctx -> { });

    // User defined data
    // You can add user defined data to the context so it's available to subsequenct handlers in the chain:

    router.route().setPath("/foo/bar").handler(ctx -> {
      ctx.contextData().put("wibble", 123);
      ctx.next();
    });

    router.route().setPath("/foo/bar").handler(ctx -> {
      ctx.request().response().end("wibble is " + ctx.contextData().get("wibble"));
    });


  }


}
