var test = require("test");
Rx = require("rx.vertx");
Rx = require("rx.time");
var initContext = vertx.context();
var eventCount = 0;
Rx.Observable.timer(10, 10).bufferWithTime(100).take(10).subscribe(
  function(event) {
    eventCount++;
    test.assertEquals(initContext._jdel(), vertx.context()._jdel());
  },
  function(err) {
    test.fail(err.message);
  }, function() {
    test.assertEquals(10, eventCount);
    test.testComplete();
  }
);
