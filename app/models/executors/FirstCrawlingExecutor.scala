package models.executors

import java.util.concurrent.Executors

object FirstCrawlingExecutor {
  val es = Executors.newCachedThreadPool
}
