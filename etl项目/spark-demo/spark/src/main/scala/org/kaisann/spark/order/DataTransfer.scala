package org.kaisann.spark.order

trait DataTransfer {
  protected def sourceDatabase: Option[String]
  protected val sourceTable: String
  protected def targetDatabase: Option[String]
  protected val targetTable: String
  val getSource: String
  val getTarget: String
  lazy val TransferInfo: String = s"$getSource ==> $getTarget"
}
