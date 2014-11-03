/*
 * Copyright (c) 2011-2014 The original author or authors
 * ------------------------------------------------------
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompanies this distribution.
 *
 *     The Eclipse Public License is available at
 *     http://www.eclipse.org/legal/epl-v10.html
 *
 *     The Apache License v2.0 is available at
 *     http://www.opensource.org/licenses/apache2.0.php
 *
 * You may elect to redistribute this code under either of these licenses.
 */

package io.vertx.ext.rest.test;

import io.vertx.core.Handler;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpClientResponse;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.ext.rest.RouteMatcher;
import io.vertx.test.core.HttpTestBase;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 */
public class RouteMatcherTest extends HttpTestBase {

  @Test
  public void testRouteWithPattern1GET() {
    testRouteWithPattern1(HttpMethod.GET);
  }

  @Test
  public void testRouteWithPattern2GET() {
    testRouteWithPattern2(HttpMethod.GET);
  }

  @Test
  public void testRouteWithPattern3GET() {
    testRouteWithPattern3(HttpMethod.GET);
  }

  @Test
  public void testRouteWithPattern4GET() {
    testRouteWithPattern4(HttpMethod.GET);
  }

  @Test
  public void testRouteWithPattern5GET() {
    testRouteWithPattern5(HttpMethod.GET);
  }

  @Test
  public void testRouteWithPattern6GET() {
    testRouteWithPattern6(HttpMethod.GET);
  }

  @Test
  public void testRouteWithPattern7GET() {
    testRouteWithPattern6(HttpMethod.GET);
  }

  // There's no need to repeat patterns 1-6 for all HTTP methods

  @Test
  public void testRouteWithPatternPUT() {
    testRouteWithPattern1(HttpMethod.PUT);
  }

  @Test
  public void testRouteWithPatternPOST() {
    testRouteWithPattern1(HttpMethod.POST);
  }

  @Test
  public void testRouteWithPatternDELETE() {
    testRouteWithPattern1(HttpMethod.DELETE);
  }

  @Test
  public void testRouteWithPatternHEAD() {
    testRouteWithPattern1(HttpMethod.HEAD);
  }

  @Test
  public void testRouteWithPatternOPTIONS() {
    testRouteWithPattern1(HttpMethod.OPTIONS);
  }

  @Test
  public void testRouteWithPatternTRACE() {
    testRouteWithPattern1(HttpMethod.TRACE);
  }

  @Test
  public void testRouteWithPatternCONNECT() {
    testRouteWithPattern1(HttpMethod.CONNECT);
  }

  @Test
  public void testRouteWithPatternPATCH() {
    testRouteWithPattern1(HttpMethod.PATCH);
  }

  @Test
  public void testRouteWithRegexGET() {
    testRouteWithRegex(HttpMethod.GET);
  }

  @Test
  public void testRouteWithRegexPUT() {
    testRouteWithRegex(HttpMethod.PUT);
  }

  @Test
  public void testRouteWithRegexPOST() {
    testRouteWithRegex(HttpMethod.POST);
  }

  @Test
  public void testRouteWithRegexDELETE() {
    testRouteWithRegex(HttpMethod.DELETE);
  }

  @Test
  public void testRouteWithRegexHEAD() {
    testRouteWithRegex(HttpMethod.HEAD);
  }

  @Test
  public void testRouteWithRegexOPTIONS() {
    testRouteWithRegex(HttpMethod.OPTIONS);
  }

  @Test
  public void testRouteWithRegexTRACE() {
    testRouteWithRegex(HttpMethod.TRACE);
  }

  @Test
  public void testRouteWithRegexCONNECT() {
    testRouteWithRegex(HttpMethod.CONNECT);
  }

  @Test
  public void testRouteWithRegexPATCH() {
    testRouteWithRegex(HttpMethod.PATCH);
  }

  @Test
  public void testRouteNoMatchPattern() {
    Map<String, String> params = new HashMap<>();
    testRoute(false, "foo", params, HttpMethod.GET, "bar", false, false);
  }

  @Test
  public void testRouteNoMatchRegex() {
    Map<String, String> params = new HashMap<>();
    testRoute(true, "foo", params, HttpMethod.GET, "bar", false, false);
  }

  @Test
  public void testRouteNoMatchHandlerPattern() {
    Map<String, String> params = new HashMap<>();
    testRoute(false, "foo", params, HttpMethod.GET, "bar", false, true);
  }

  @Test
  public void testRouteNoMatchHandlerRegex() {
    Map<String, String> params = new HashMap<>();
    testRoute(true, "foo", params, HttpMethod.GET, "bar", false, true);
  }

  //----------- Private non test method ----------------------------

  private void testRouteWithPattern1(HttpMethod method) {
    Map<String, String> params = new HashMap<>();
    params.put("name", "foo");
    params.put("version", "v0.1");
    testRoute(false, "/:name/:version", params, method, "/foo/v0.1");
  }

  private void testRouteWithPattern2(HttpMethod method) {
    Map<String, String> params = new HashMap<>();
    params.put("name", "foo");
    params.put("version", "v0.1");
    testRoute(false, "modules/:name/:version", params, method, "modules/foo/v0.1");
  }

  private void testRouteWithPattern3(HttpMethod method) {
    Map<String, String> params = new HashMap<>();
    params.put("name", "foo");
    params.put("version", "v0.1");
    testRoute(false, "modules/:name/:version/", params, method, "modules/foo/v0.1/");
  }

  private void testRouteWithPattern4(HttpMethod method) {
    Map<String, String> params = new HashMap<>();
    params.put("name", "foo");
    params.put("version", "v0.1");
    testRoute(false, "modules/:name/:version/whatever", params, method, "modules/foo/v0.1/whatever");
  }

  private void testRouteWithPattern5(HttpMethod method) {
    Map<String, String> params = new HashMap<>();
    params.put("name", "foo");
    params.put("version", "v0.1");
    testRoute(false, "modules/:name/blah/:version/whatever", params, method, "modules/foo/blah/v0.1/whatever");
  }

  private void testRouteWithPattern6(HttpMethod method) {
    Map<String, String> params = new HashMap<>();
    params.put("name", "foo");
    testRoute(false, "/:name/", params, method, "/foo/");
  }

  private void testRouteWithRegex(HttpMethod method) {
    Map<String, String> params = new HashMap<>();
    params.put("param0", "foo");
    params.put("param1", "v0.1");
    String regex = "\\/([^\\/]+)\\/([^\\/]+)";
    testRoute(true, regex, params, method, "/foo/v0.1");
  }


  private void testRoute(final boolean regex, final String pattern, final Map<String, String> params,
                         final HttpMethod method, final String uri) {
    testRoute(regex, pattern, params, method, uri, true, false);
  }

  private void testRoute(final boolean regex, final String pattern, final Map<String, String> params,
                         final HttpMethod method, final String uri, final boolean shouldPass, final boolean noMatchHandler) {
    server = vertx.createHttpServer(new HttpServerOptions().setPort(DEFAULT_HTTP_PORT));
    client = vertx.createHttpClient(new HttpClientOptions());

    RouteMatcher matcher = RouteMatcher.routeMatcher();

    Handler<HttpServerRequest> handler = req -> {
      assertEquals(params.size(), req.params().size());
      for (Map.Entry<String, String> entry : params.entrySet()) {
        assertEquals(entry.getValue(), req.params().get(entry.getKey()));
      }
      req.response().end();
    };

    if (regex) {
      matcher.matchMethodWithRegEx(method, pattern, handler);
    } else {
      matcher.matchMethod(method, pattern, handler);
    }

    final String noMatchResponseBody = "oranges";

    if (noMatchHandler) {
      matcher.noMatch(req -> req.response().end(noMatchResponseBody));
    }

    server.requestHandler(matcher::accept).listen(onSuccess(s -> {
      Handler<HttpClientResponse> respHandler = resp -> {
        if (shouldPass) {
          assertEquals(200, resp.statusCode());
          testComplete();
        } else if (noMatchHandler) {
          assertEquals(200, resp.statusCode());
          resp.bodyHandler(body -> {
            assertEquals(noMatchResponseBody, body.toString());
            testComplete();
          });
        } else {
          assertEquals(404, resp.statusCode());
          testComplete();
        }
      };

      client.request(method, DEFAULT_HTTP_PORT, DEFAULT_HTTP_HOST, uri, respHandler).end();
    }));

    await();
  }

}
