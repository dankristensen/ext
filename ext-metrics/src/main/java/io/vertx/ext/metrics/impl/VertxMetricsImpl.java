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

package io.vertx.ext.metrics.impl;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.Meter;
import com.codahale.metrics.Metered;
import com.codahale.metrics.Metric;
import com.codahale.metrics.Snapshot;
import com.codahale.metrics.Timer;
import io.vertx.core.Handler;
import io.vertx.core.Verticle;
import io.vertx.core.VertxOptions;
import io.vertx.core.datagram.DatagramSocket;
import io.vertx.core.datagram.DatagramSocketOptions;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.core.metrics.spi.DatagramSocketMetrics;
import io.vertx.core.metrics.spi.EventBusMetrics;
import io.vertx.core.metrics.spi.HttpClientMetrics;
import io.vertx.core.metrics.spi.HttpServerMetrics;
import io.vertx.core.metrics.spi.NetMetrics;
import io.vertx.core.metrics.spi.VertxMetrics;
import io.vertx.core.net.NetClient;
import io.vertx.core.net.NetClientOptions;
import io.vertx.core.net.NetServer;
import io.vertx.core.net.NetServerOptions;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.codahale.metrics.MetricRegistry.*;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 */
class VertxMetricsImpl extends AbstractMetrics implements VertxMetrics {

  static final String BASE_NAME = "vertx";

  private Counter timers;
  private Counter verticles;
  private Handler<Void> doneHandler;

  VertxMetricsImpl(VertxOptions vertxOptions) {
    super(new Registry(), BASE_NAME);
    initialize(vertxOptions);
  }

  public void initialize(VertxOptions options) {
    timers = counter("timers");

    gauge(options::getEventLoopPoolSize, "event-loop-size");
    gauge(options::getWorkerPoolSize, "worker-pool-size");
    if (options.isClustered()) {
      gauge(options::getClusterHost, "cluster-host");
      gauge(options::getClusterPort, "cluster-port");
    }

    verticles = counter("verticles");
  }

  @Override
  public void verticleDeployed(Verticle verticle) {
    verticles.inc();
    counter("verticles", verticleName(verticle)).inc();
  }

  @Override
  public void verticleUndeployed(Verticle verticle) {
    verticles.dec();
    counter("verticles", verticleName(verticle)).dec();
  }

  @Override
  public void timerCreated(long id) {
    timers.inc();
  }

  @Override
  public void timerEnded(long id, boolean cancelled) {
    timers.dec();
  }

  @Override
  public EventBusMetrics createMetrics(EventBus eventBus) {
    return new EventBusMetricsImpl(this, name(baseName(), "eventbus"));
  }

  @Override
  public HttpServerMetrics createMetrics(HttpServer server, HttpServerOptions options) {
    return new HttpServerMetricsImpl(this, name(baseName(), "http.servers"));
  }

  @Override
  public HttpClientMetrics createMetrics(HttpClient client, HttpClientOptions options) {
    return new HttpClientMetricsImpl(this, instanceName(name(baseName(), "http.clients"), client), options);
  }

  @Override
  public NetMetrics createMetrics(NetServer server, NetServerOptions options) {
    return new NetMetricsImpl(this, name(baseName(), "net.servers"), false);
  }

  @Override
  public NetMetrics createMetrics(NetClient client, NetClientOptions options) {
    return new NetMetricsImpl(this, instanceName(name(baseName(), "net.clients"), client), true);
  }

  @Override
  public DatagramSocketMetrics createMetrics(DatagramSocket socket, DatagramSocketOptions options) {
    return new DatagramSocketMetricsImpl(this, name(baseName(), "datagram"));
  }

  @Override
  public void close() {
    registry().shutdown();
    if (doneHandler != null) {
      doneHandler.handle(null);
    }
  }

  @Override
  public String metricBaseName() {
    return baseName();
  }

  @Override
  public Map<String, JsonObject> metrics() {
    Map<String, JsonObject> metrics = new HashMap<>();
    registry().getMetrics().forEach((name, metric) -> {
      JsonObject data = convertMetric(metric, TimeUnit.SECONDS, TimeUnit.MILLISECONDS);
      metrics.put(name, data);
    });

    return metrics;
  }

  void setDoneHandler(Handler<Void> handler) {
    this.doneHandler = handler;
  }

  private static String verticleName(Verticle verticle) {
    return verticle.getClass().getName();
  }

  //----------------------- Convert metrics to JsonObject

  private JsonObject convertMetric(Metric metric, TimeUnit rateUnit, TimeUnit durationUnit) {
    if (metric instanceof Gauge) {
      return toJson((Gauge) metric);
    } else if (metric instanceof Counter) {
      return toJson((Counter) metric);
    } else if (metric instanceof Histogram) {
      return toJson((Histogram) metric);
    } else if (metric instanceof Meter) {
      return toJson((Meter) metric, rateUnit);
    } else if (metric instanceof Timer) {
      return toJson((Timer) metric, rateUnit, durationUnit);
    } else {
      throw new IllegalArgumentException("Unknown metric " + metric);
    }
  }

  private static JsonObject toJson(Gauge gauge) {
    return new JsonObject().put("value", gauge.getValue());
  }

  private static JsonObject toJson(Counter counter) {
    return new JsonObject().put("count", counter.getCount());
  }

  private static JsonObject toJson(Histogram histogram) {
    Snapshot snapshot = histogram.getSnapshot();
    JsonObject json = new JsonObject();
    json.put("count", histogram.getCount());

    // Snapshot
    populateSnapshot(json, snapshot, 1);

    return json;
  }

  private JsonObject toJson(Meter meter, TimeUnit rateUnit) {
    JsonObject json = new JsonObject();

    // Meter
    populateMetered(json, meter, rateUnit);

    return json;
  }

  private JsonObject toJson(Timer timer, TimeUnit rateUnit, TimeUnit durationUnit) {
    Snapshot snapshot = timer.getSnapshot();
    JsonObject json = new JsonObject();

    // Meter
    populateMetered(json, timer, rateUnit);

    // Snapshot
    double factor = 1.0 / durationUnit.toNanos(1);
    populateSnapshot(json, snapshot, factor);

    // Duration rate
    String duration = durationUnit.toString().toLowerCase();
    json.put("durationRate", duration);

    return json;
  }

  private static void populateMetered(JsonObject json, Metered meter, TimeUnit rateUnit) {
    double factor = rateUnit.toSeconds(1);
    json.put("count", meter.getCount());
    json.put("meanRate", meter.getMeanRate() * factor);
    json.put("oneMinuteRate", meter.getOneMinuteRate() * factor);
    json.put("fiveMinuteRate", meter.getFiveMinuteRate() * factor);
    json.put("fifteenMinuteRate", meter.getFifteenMinuteRate() * factor);
    String rate = "events/" + rateUnit.toString().toLowerCase();
    json.put("rate", rate);
  }

  private static void populateSnapshot(JsonObject json, Snapshot snapshot, double factor) {
    json.put("min", snapshot.getMin() * factor);
    json.put("max", snapshot.getMax() * factor);
    json.put("mean", snapshot.getMean() * factor);
    json.put("stddev", snapshot.getStdDev() * factor);
    json.put("median", snapshot.getMedian() * factor);
    json.put("75%", snapshot.get75thPercentile() * factor);
    json.put("95%", snapshot.get95thPercentile() * factor);
    json.put("98%", snapshot.get98thPercentile() * factor);
    json.put("99%", snapshot.get99thPercentile() * factor);
    json.put("99.9%", snapshot.get999thPercentile() * factor);
  }
}
