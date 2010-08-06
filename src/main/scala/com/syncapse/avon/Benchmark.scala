package com.syncapse.avon

object Benchmark {

  def measure(desc: String)(body: => Int): Int = {
    val startTime = System.currentTimeMillis
    val result = body
    val total = System.currentTimeMillis - startTime
    System.out.printf("%s: result: %s time: %s\n", desc, body.toString, total.toString)
    result
  }
}