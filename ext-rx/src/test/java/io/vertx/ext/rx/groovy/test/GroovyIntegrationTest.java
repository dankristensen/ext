package io.vertx.ext.rx.groovy.test;

import groovy.lang.Binding;
import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyShell;
import groovy.lang.Script;
import io.vertx.test.core.VertxTestBase;
import org.junit.Test;

import java.io.File;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class GroovyIntegrationTest extends VertxTestBase {

  @Test
  public void testConsumeBodyStream() throws Exception {
    runScript("src/test/groovy/testConsumeBodyStream.groovy");
  }

  @Test
  public void testRegisterAgain() throws Exception {
    runScript("src/test/groovy/testRegisterAgain.groovy");
  }

  @Test
  public void testObservableUnsubscribeDuringObservation() throws Exception {
    runScript("src/test/groovy/testObservableUnsubscribeDuringObservation.groovy");
  }

  @Test
  public void testObservableNetSocket() throws Exception {
    runScript("src/test/groovy/testObservableNetSocket.groovy");
  }

  @Test
  public void testObservableWebSocket() throws Exception {
    runScript("src/test/groovy/testObservableWebSocket.groovy");
  }

  @Test
  public void testObservableHttpRequest() throws Exception {
    runScript("src/test/groovy/testObservableHttpRequest.groovy");
  }

  @Test
  public void testObserverToHandler() throws Exception {
    runScript("src/test/groovy/testObserverToHandler.groovy");
  }

  @Test
  public void testScheduledTimer() throws Exception {
    runScript("src/test/groovy/testScheduledTimer.groovy");
  }

  @Test
  public void testScheduledBuffer() throws Exception {
    runScript("src/test/groovy/testScheduledBuffer.groovy");
  }

  @Test
  public void testTimeMap() throws Exception {
    runScript("src/test/groovy/testTimeMap.groovy");
  }

  @Test
  public void testConcat() throws Exception {
    runScript("src/test/groovy/testConcat.groovy");
  }

  private void runScript(String script) throws Exception {
    GroovyShell gcl = new GroovyShell();
    Script s = gcl.parse(new File(script));
    Binding binding = new Binding();
    binding.setProperty("test", this);
    s.setBinding(binding);
    s.run();
  }

}
