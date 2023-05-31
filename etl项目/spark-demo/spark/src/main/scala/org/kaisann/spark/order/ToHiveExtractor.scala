package org.kaisann.spark.order

import org.apache.spark.sql.{DataFrame, SparkSession, functions}

trait ToHiveExtractor extends Extractor {
  protected def dataTransfer: DataTransfer
  protected def extractMode: ExtractMode
  protected def partition: Partition
  /**
   * 完整实现数据抽取的具体方法
   * @param sparkSession 用匿名方式获取SparkSession
   */
  final def extract(implicit sparkSession: SparkSession): Unit = {
    val dataFrame: DataFrame = reader
    extractMode.incrementalKey match {
      case Some(str) =>
        val incrementalValue = sparkSession.table(dataTransfer.getTarget)
          .agg(functions.max(str))
          .collect()(0)(0)
          .asInstanceOf[Int]
        dataFrame.filter(s"$str > $incrementalValue")
      case None =>
    }
    modify(dataFrame).createOrReplaceTempView("temp")

    println(dataTransfer.TransferInfo)
    sparkSession.sql("select * from temp").show

    sparkSession.sql(
      s"""
         |insert ${extractMode.mode} table ${dataTransfer.getTarget}
         |partition(${partition.getExpr})
         |select * from temp
         |""".stripMargin)
  }
}


