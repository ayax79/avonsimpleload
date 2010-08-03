package com.syncapse.avon

import org.slf4j.LoggerFactory

/**
 * Hello world!
 *
 */
object App extends Application {
  for (i <- 0 until 50) {
    new Thread(new Runnable() {
      def run = {
        while(true) {
          AvonUtil.fullLogin
          Thread.sleep(500)
        }
      }
    }).start
  }
}
