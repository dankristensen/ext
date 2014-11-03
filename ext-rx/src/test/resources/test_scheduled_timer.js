var test = require("test");
Rx = require("rx.vertx");
Rx = require("rx.time");
var initContext = vertx.context();
Rx.Observable.timer(100, 100).take(10).subscribe(
  function(event) {
    test.assertEquals(initContext._jdel(), vertx.context()._jdel());
  },
  function(err) {
    test.fail(err.message);
  }, function() {
    test.testComplete();
  }
);
