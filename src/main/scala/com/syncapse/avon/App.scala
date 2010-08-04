package com.syncapse.avon

import java.lang.String
import org.apache.http.impl.client.DefaultHttpClient

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
        val userInfo = new UserInfo {
          val username = "avontest" + i
          val email = "avontest" + i + "@mailinator.com"
          val userType = UserTypes.randomUserType
          val accountId = (Integer.MAX_VALUE - i).toLong
        }

        while (true) {
          val saml: String = AvonUtil.samlResponseString(userInfo)
          AvonUtil.makeLoginRequest(new DefaultHttpClient, AvonUtil.httpHost, (userInfo, saml))
          Thread.sleep(sleep)
        }
      }
    }).start
  }
}
