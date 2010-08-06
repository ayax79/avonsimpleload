package com.syncapse.avon

trait TestingApp {
  lazy val threads = System.getProperty("test.threads", "50").toInt
  lazy val sleep = System.getProperty("test.sleep", "500").toInt

  def performTest[T](f: UserInfo => T): Unit = {
    System.out.println("Running test app " + getClass.getSimpleName)
    System.out.println("host: " + AvonUtil.host + " port: " + AvonUtil.port + " prefix:" + AvonUtil.prefix)

    for (i <- 0 until threads) {
      val userInfo = new UserInfo {
        val username = "avontest" + i
        val email = "avontest" + i + "@mailinator.com"
        val userType = UserTypes.randomUserType
        val accountId = (Integer.MAX_VALUE - i).toLong
      }

      new Thread(new Runnable() {
        def run = {
          while (true) {
            try {
              f(userInfo)
            } catch {
              case e: Exception =>
                System.err.println(e.getMessage)
                e.printStackTrace
            }
            Thread.sleep(sleep)
          }
        }
      }).start
    }

  }

}