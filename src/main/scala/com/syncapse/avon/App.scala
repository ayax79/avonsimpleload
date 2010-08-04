package com.syncapse.avon

import org.slf4j.LoggerFactory

/**
 * Hello world!
 *
 */
object App extends Application {
  lazy val threads = System.getProperty("test.threads", "50").toInt
  lazy val sleep = System.getProperty("test.sleep", "500").toInt

  System.out.println("host: " + AvonUtil.host + " port: " + AvonUtil.port + " prefix:" + AvonUtil.prefix)

  for (i <- 0 until threads) {
    new Thread(new Runnable() {
      def run = {
        while (true) {
          AvonUtil.fullLogin
          Thread.sleep(sleep)
        }
      }
    }).start
  }
}
