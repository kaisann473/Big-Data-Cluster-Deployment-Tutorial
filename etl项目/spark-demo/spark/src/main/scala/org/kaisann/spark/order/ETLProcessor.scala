package org.kaisann.spark.order

import org.apache.spark.sql.{DataFrame, SparkSession}

trait ETLProcessor {
  def sparkSession: SparkSession
  def dataSource: String
  def target: String
  def extract: DataFrame
  def transform(df: DataFrame): DataFrame =  df
  def load(df :DataFrame): Unit
  final def process(): Unit = {
    val df: DataFrame = transform(extract)
    df.show
    load(df)
  }


  final protected def extractFromHive(dataSource: String): DataFrame = {
    sparkSession.table(dataSource)
  }

  final protected def extractFromMySql(dbTable: String): DataFrame = {
    sparkSession.read
      .format("jdbc")
      .option("user", "root")
      .option("password", "123456")
      .option("driver", "com.mysql.jdbc.Driver")
      .option("url", "jdbc:mysql://172.18.0.12:3306/shtd_store?useUnicode=true&characterEncoding=utf-8")
      .option("dbtable", dbTable)
      .load()

  }
  /**
   * 预制的加载函数，
   * 将数据帧加载到Hive
   * @param df            数据帧
   * @param partitionExpr 分区表达式
   * @param target        目标表
   * @param keyword       抽取关键字
   */
  final protected def loadIntoHive(df: DataFrame,
                         keyword: String,
                         target: String,
                         partitionExpr: String): Unit = {
    df.createOrReplaceTempView("temp")
    sparkSession.sql(
      s"""
         |insert $keyword table $target
         |partition ($partitionExpr)
         |select * from temp
         |""".stripMargin)
  }
}
