package com.syncapse.avon

import java.lang.String
import org.apache.http.impl.client.DefaultHttpClient

/**
 * Hello world!
 *
 */
object LoginApp extends Application with TestingApp {
  performTest {
    ui: UserInfo =>
      val client = AvonUtil.httpClient
      val saml: String = AvonUtil.samlResponseString(ui)
      AvonUtil.makeLoginRequest(client, (ui, saml))
  }
}
