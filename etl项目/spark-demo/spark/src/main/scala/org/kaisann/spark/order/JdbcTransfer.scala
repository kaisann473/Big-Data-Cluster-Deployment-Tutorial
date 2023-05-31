package org.kaisann.spark.order

class JdbcTransfer(_sourceTable: String,
                   _targetDatabase: String,
                   _targetTable: String) extends DataTransfer {
  def this(_sourceTable: String, _targetDatabase: String) = {
    this(_sourceTable, _targetDatabase, _sourceTable)
  }

  override protected val sourceDatabase: Option[String] = None
  override protected val sourceTable: String = s"`${_sourceTable}`"
  override protected val targetDatabase: Option[String] = Some(_targetDatabase)
  override protected val targetTable: String = s"`${_targetTable}`"
  override val getSource: String = sourceTable
  override val getTarget: String = s"${targetDatabase.get}.$targetTable"
}
