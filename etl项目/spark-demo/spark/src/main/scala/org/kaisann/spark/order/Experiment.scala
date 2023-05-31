package org.kaisann.spark.order
import org.apache.spark.sql.functions.{col, desc, lit}
import org.apache.spark.sql.{DataFrame, SparkSession, functions}

import java.text.SimpleDateFormat
import java.util.Date
object Experiment {
  private def sparkSession: SparkSession = SparkSession.builder()
    .getOrCreate()
  private def time: String = new SimpleDateFormat("yyyyMMdd").format(new Date)
  private def timeStamp: String = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date)
  private def addDwdInfo(df: DataFrame): DataFrame = {
    df.withColumn("dwd_insert_user", lit("user1"))
      .withColumn("dwd_insert_time", lit(timeStamp))
      .withColumn("dwd_modify_user", lit("user1"))
      .withColumn("dwd_modify_time", lit(timeStamp))
  }

  private def getAppend(target: String, incrementalKey: String)(df: DataFrame): DataFrame = {
    val incrementalValue = sparkSession.table(target)
      .agg(functions.max(incrementalKey))
      .collect()(0)(0)
      .asInstanceOf[Int]
    df.filter(s"$incrementalKey > $incrementalValue")
  }

  def main(args: Array[String]): Unit = {

    try {
      //数据抽取
      for (table <- List("nation", "region", "cust")) {
        new ETLProcessor {
          override def sparkSession: SparkSession = Experiment.sparkSession

          override def dataSource: String = table

          override def target: String = s"ods.$table"

          override def extract: DataFrame = extractFromMySql(dataSource)

          override def load(df: DataFrame): Unit =
            loadIntoHive(df, "overwrite", target, s"etldate = $time")
        }
      } // tables => ods.tables
      new ETLProcessor {
        override def sparkSession: SparkSession = Experiment.sparkSession

        override def dataSource: String = "`order`"

        override def target: String = "ods.`order`"

        override def extract: DataFrame = extractFromMySql(dataSource)

        override def transform(df: DataFrame): DataFrame = getAppend(target, "`orderkey`")(df)

        override def load(df: DataFrame): Unit =
          loadIntoHive(df ,"into", target, s"dealdate")
      }.process() //order => ods.order
      new ETLProcessor {
        override def sparkSession: SparkSession = Experiment.sparkSession

        override def dataSource: String = "payment"

        override def target: String = "ods.payment"

        override def extract: DataFrame = extractFromMySql(dataSource)

        override def transform(df: DataFrame): DataFrame = getAppend(target, "paymentkey")(df)

        override def load(df: DataFrame): Unit =
          loadIntoHive(df, "into", target, s"etldate = $time")
      }.process() //payment => ods.payment

      //数据处理
      for (table <- List("nation", "region", "cust")) {
        new ETLProcessor {

          override def sparkSession: SparkSession = Experiment.sparkSession

          override def dataSource: String = s"ods.$table"

          override def target: String = s"dwd.dim_$table"

          override def extract: DataFrame = extractFromHive(dataSource)

          override def transform(df: DataFrame): DataFrame = addDwdInfo(df)

          override def load(df: DataFrame): Unit =
            loadIntoHive(df, "overwrite", target, "etldate")

        }.process()
      } //ods.tables => dwd.dim_tables
      new ETLProcessor {
        override def sparkSession: SparkSession = Experiment.sparkSession

        override def dataSource: String = s"ods.`order`"

        override def target: String = s"dwd.`fact_order`"

        override def extract: DataFrame = extractFromHive(dataSource)

        override def transform(df: DataFrame): DataFrame = addDwdInfo(df)

        override def load(df: DataFrame): Unit =
          loadIntoHive(df, "overwrite", target, "dealdate")
      }.process() //ods.order => dwd.fact_order
      new ETLProcessor {
        override def sparkSession: SparkSession = Experiment.sparkSession


        override def dataSource: String = s"ods.payment"

        override def target: String = s"dwd.fact_payment"

        override def extract: DataFrame = extractFromHive(dataSource)

        override def transform(df: DataFrame): DataFrame =
          addDwdInfo(df)
            .dropDuplicates("custname", "custkey")
            .orderBy("paymentkey")

        override def load(df: DataFrame): Unit =
          loadIntoHive(df, "overwrite", target, "etldate")
      }.process() //ods.payment => dwd.fact_payment

      val thirdPartition: String = sparkSession.table("ods.order")
        .select(col("dealdate"))
        .distinct()
        .orderBy(desc("dealdate"))
        .limit(3)
        .collect()(2)(0).asInstanceOf[String]
      println(
        s"""
           |use this in Hive ctl :
           |alter table ods.`order` drop partition (dealdate < '$thirdPartition')
           |""".stripMargin)

      //指标计算



    } catch {
      case e: Exception =>
        e.getMessage
        e.printStackTrace()
    } finally {
      sparkSession.stop()
    }
  }
}
