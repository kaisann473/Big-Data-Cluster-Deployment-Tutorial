package org.kaisann.spark.order

class HiveTransfer(_sourceDatabase: String,
                   _sourceTable: String,
                   _targetDatabase: String,
                   _targetTable: String) extends DataTransfer {
  override protected def sourceDatabase: Option[String] = Some(_sourceDatabase)
  override protected val sourceTable: String = _sourceTable
  override protected def targetDatabase: Option[String] = Some(_targetDatabase)
  override protected val targetTable: String = _targetTable
  override val getSource: String = s"${sourceDatabase.get}.$sourceTable"
  override val getTarget: String = s"${targetDatabase.get}.$targetTable"
}
