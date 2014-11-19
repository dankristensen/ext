var Vertx = require("vertx-js/vertx");
var test = require("test");
Rx = require("rx.vertx");
Rx = require("rx.time");
var initContext = Vertx.currentContext();
Rx.Observable.timer(100, 100).take(10).subscribe(
  function(event) {
    test.assertEquals(initContext._jdel(), Vertx.currentContext()._jdel());
  },
  function(err) {
    test.fail(err.message);
  }, function() {
    test.testComplete();
  }
);
