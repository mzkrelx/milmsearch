package models

/**
 * Helper for pagination
 */
case class Page[A](
    items: Seq[A],
    totalResultCount: Long,
    startIndex: Long = 0,
    itemsPerPage: Int = 10) {

  lazy val range = 10
  lazy val prevRange = range / 2
  lazy val nextRange = prevRange - 1

  lazy val prevIndex  = Option(startIndex - itemsPerPage).filter(_ >= 0)
  lazy val nextIndex  = Option(startIndex + itemsPerPage).filter(_ < totalResultCount)
  lazy val totalPageNum = (totalResultCount / itemsPerPage) +
                          ((totalResultCount % itemsPerPage) min 1)

  /** Start position in this page */
  lazy val start = startIndex + 1

  /** End position in this page */
  lazy val end = startIndex + items.length

  lazy val currentPageNum: Long = startIndex / itemsPerPage + 1

  /** Number of items on this page */
  lazy val currentItemCount: Long = (totalResultCount - startIndex) min itemsPerPage

  /** First page number */
  lazy val firstPageNum: Long = 1

  /** Last page number */
  lazy val lastPageNum: Long = totalPageNum

  lazy val nextPageNum: Option[Long] =
    if (currentPageNum == lastPageNum) None else Some(currentPageNum + 1)

  lazy val previousPageNum: Option[Long] =
    if (currentPageNum == firstPageNum) None else Some(currentPageNum - 1)

  def pagesInRange: List[Long] = {
    val first = 1L max (currentPageNum - prevRange)
    val last = lastPageNum min (currentPageNum + nextRange)
    val trueFirst = (last - (range - 1)) match {
      case x if x > firstPageNum => x
      case _ => first
    }
    val trueLast = (trueFirst + (range - 1)) match {
      case x if x < lastPageNum => x
      case _ => last
    }

    trueFirst to trueLast toList
  }

  lazy val firstPageInRange: Long = pagesInRange.head

  lazy val lastPageInRange: Long = pagesInRange.last

}
