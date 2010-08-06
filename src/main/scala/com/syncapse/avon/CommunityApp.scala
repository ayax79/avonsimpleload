package com.syncapse.avon

import org.apache.http.impl.client.DefaultHttpClient

object CommunityApp extends Application with TestingApp {
  performTest {
    ui: UserInfo =>
      val saml = AvonUtil.samlResponseString(ui)
      val client = AvonUtil.httpClient
      AvonUtil.makeLoginRequest(client, (ui, saml))
      AvonUtil.makeGetCommunitiesRequest(client)
  }

}