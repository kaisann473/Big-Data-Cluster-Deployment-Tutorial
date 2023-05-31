package org.kaisann.spark.order

import org.apache.log4j.{Level, Logger}
import org.apache.spark.sql.expressions.Window
import org.apache.spark.sql.{DataFrame, SaveMode, SparkSession, functions}
import org.apache.spark.sql.functions.{add_months, avg, col, column, concat, concat_ws, count, date_add, date_format, desc, lag, last_day, lead, lit, month, months_between, sum, to_date, when, year}
import org.kaisann.spark.order

import java.text.SimpleDateFormat
import java.util.{Date, Properties}

/**
 * 这是整个应用程序的主类
 */
object Main {
  private def time: String = new SimpleDateFormat("yyyyMMdd").format(new Date)
  private def timeStamp: String = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date)
  def main(args: Array[String]): Unit = {
    Logger.getLogger("org").setLevel(Level.OFF)

    implicit val sparkSession: SparkSession = SparkSession.builder()
      .enableHiveSupport()
      .config("hive.exec.dynamic.partition", "true")
      .config("hive.exec.dynamic.partition.mode", "nonstrict")
      .getOrCreate()
//    sparkSession.sql("truncate table ods.nation")
//    sparkSession.sql("truncate table ods.region")
//    sparkSession.sql("truncate table ods.cust")
//    sparkSession.sql("truncate table ods.`order`")
//    sparkSession.sql("truncate table ods.payment")
//    sparkSession.sql("truncate table dwd.dim_nation")
//    sparkSession.sql("truncate table dwd.dim_region")
//    sparkSession.sql("truncate table dwd.dim_cust")
//    sparkSession.sql("truncate table dwd.`fact_order`")
//    sparkSession.sql("truncate table dwd.fact_payment")

    try {
//      sparkSession.read
//        .format("jdbc")
//        .option("user", "root")
//        .option("password", "123456")
//        .option("driver", "com.mysql.jdbc.Driver")
//        .option("url", "jdbc:mysql://172.18.0.12:3306/shtd_store?useUnicode=true&characterEncoding=utf-8")
//        .option("dbtable", "nation")
//        .load().createOrReplaceTempView("temp")
//      sparkSession.sql(
//        s"""
//           |insert overwrite table ods.nation
//           |partition(etldate = $time)
//           |select * from temp
//           |""".stripMargin)
//      println("nation")
//      sparkSession.sql("select * from ods.nation").show()
//
//      sparkSession.read
//        .format("jdbc")
//        .option("user", "root")
//        .option("password", "123456")
//        .option("driver", "com.mysql.jdbc.Driver")
//        .option("url", "jdbc:mysql://172.18.0.12:3306/shtd_store?useUnicode=true&characterEncoding=utf-8")
//        .option("dbtable", "region")
//        .load().createOrReplaceTempView("temp")
//      sparkSession.sql(
//        s"""
//           |insert overwrite table ods.region
//           |partition(etldate = $time)
//           |select * from temp
//           |""".stripMargin)
//      println("region")
//      sparkSession.sql("select * from ods.region").show()
//
//      sparkSession.read
//        .format("jdbc")
//        .option("user", "root")
//        .option("password", "123456")
//        .option("driver", "com.mysql.jdbc.Driver")
//        .option("url", "jdbc:mysql://172.18.0.12:3306/shtd_store?useUnicode=true&characterEncoding=utf-8")
//        .option("dbtable", "cust")
//        .load().createOrReplaceTempView("temp")
//      sparkSession.sql(
//        s"""
//           |insert overwrite table ods.cust
//           |partition(etldate = $time)
//           |select * from temp
//           |""".stripMargin)
//      println("cust")
//      sparkSession.sql("select * from ods.cust").show()
//
//      var incrementalValue = sparkSession.table("ods.`order`")
//        .agg(functions.max("orderkey"))
//        .collect()(0)(0)
//        .asInstanceOf[Int]
//      sparkSession.read
//        .format("jdbc")
//        .option("user", "root")
//        .option("password", "123456")
//        .option("driver", "com.mysql.jdbc.Driver")
//        .option("url", "jdbc:mysql://172.18.0.12:3306/shtd_store?useUnicode=true&characterEncoding=utf-8")
//        .option("dbtable", "`order`")
//        .load()
//        .filter(s"orderkey > $incrementalValue")
//        .withColumn("dealdate", date_format(col("orderdate"), "yyyyMMdd"))
//        .createOrReplaceTempView("temp")
//      sparkSession.sql(
//        s"""
//           |insert into table ods.`order`
//           |partition(dealdate)
//           |select * from temp
//           |""".stripMargin)
//      println("order")
//      sparkSession.sql("select * from ods.`order`").show()
//
//
//      incrementalValue = sparkSession.table("ods.payment")
//        .agg(functions.max("paymentkey"))
//        .collect()(0)(0)
//        .asInstanceOf[Int]
//      sparkSession.read
//        .format("jdbc")
//        .option("user", "root")
//        .option("password", "123456")
//        .option("driver", "com.mysql.jdbc.Driver")
//        .option("url", "jdbc:mysql://172.18.0.12:3306/shtd_store?useUnicode=true&characterEncoding=utf-8")
//        .option("dbtable", "payment")
//        .load()
//        .filter(s"paymentkey > $incrementalValue")
//        .createOrReplaceTempView("temp")
//      sparkSession.sql(
//        s"""
//           |insert into table ods.payment
//           |partition(etldate = $time)
//           |select * from temp
//           |""".stripMargin)
//      println("payment")
//      sparkSession.sql("select * from ods.payment").show()
//
//      sparkSession.table("ods.nation")
//        .withColumn("dwd_insert_user", lit("user1"))
//        .withColumn("dwd_insert_time", lit(timeStamp))
//        .withColumn("dwd_modify_user", lit("user1"))
//        .withColumn("dwd_modify_time", lit(timeStamp))
//        .createOrReplaceTempView("temp")
//      sparkSession.sql(
//        s"""
//           |insert overwrite table dwd.dim_nation
//           |partition(etldate)
//           |select * from temp
//           |""".stripMargin)
//      println("dim_nation")
//      sparkSession.sql("select * from dwd.dim_nation").show()
//
//      sparkSession.table("ods.region")
//        .withColumn("dwd_insert_user", lit("user1"))
//        .withColumn("dwd_insert_time", lit(timeStamp))
//        .withColumn("dwd_modify_user", lit("user1"))
//        .withColumn("dwd_modify_time", lit(timeStamp))
//        .createOrReplaceTempView("temp")
//      sparkSession.sql(
//        s"""
//           |insert overwrite table dwd.dim_region
//           |partition(etldate)
//           |select * from temp
//           |""".stripMargin)
//      println("dim_region")
//      sparkSession.sql("select * from dwd.dim_region").show()
//
//      sparkSession.table("ods.cust")
//        .withColumn("dwd_insert_user", lit("user1"))
//        .withColumn("dwd_insert_time", lit(timeStamp))
//        .withColumn("dwd_modify_user", lit("user1"))
//        .withColumn("dwd_modify_time", lit(timeStamp))
//        .createOrReplaceTempView("temp")
//      sparkSession.sql(
//        s"""
//           |insert overwrite table dwd.dim_cust
//           |partition(etldate)
//           |select * from temp
//           |""".stripMargin)
//      println("dim_cust")
//      sparkSession.sql("select * from dwd.dim_cust").show()
//
//      sparkSession.table("ods.`order`")
//        .withColumn("dwd_insert_user", lit("user1"))
//        .withColumn("dwd_insert_time", lit(timeStamp))
//        .withColumn("dwd_modify_user", lit("user1"))
//        .withColumn("dwd_modify_time", lit(timeStamp))
//        .createOrReplaceTempView("temp")
//      sparkSession.sql(
//        s"""
//           |insert overwrite table dwd.`fact_order`
//           |partition(dealdate)
//           |select * from temp
//           |""".stripMargin)
//      println("fact_order")
//      sparkSession.sql("select * from dwd.`fact_order`").show()
//
////      val thirdPartition: String = sparkSession.table("ods.order")
////        .select(col("dealdate"))
////        .distinct()
////        .orderBy(desc("dealdate"))
////        .limit(3)
////        .collect()(2)(0).asInstanceOf[String]
////
////      println(s"""
////           |use this in Hive ctl :
////           |alter table ods.`order` drop partition (dealdate < '$thirdPartition')
////           |""".stripMargin)
//
//      sparkSession.table("ods.payment")
//        .withColumn("dwd_insert_user", lit("user1"))
//        .withColumn("dwd_insert_time", lit(timeStamp))
//        .withColumn("dwd_modify_user", lit("user1"))
//        .withColumn("dwd_modify_time", lit(timeStamp))
//        .dropDuplicates("custname", "custkey")
//        .orderBy("paymentkey")
//        .createOrReplaceTempView("temp")
//      sparkSession.sql(
//        s"""
//           |insert overwrite table dwd.fact_payment
//           |partition(etldate)
//           |select * from temp
//           |""".stripMargin)
//      println("fact_payment")
//      sparkSession.sql("select * from dwd.fact_payment").show()


//      /**
//       * 编写Scala工程代码，根据dwd层表统计每个地区、每个国家、每个月下单的数
//       * 量和下单的总金额，存入MySQL数据库shtd_store的nationeverymonth表中，然
//       * 后在Linux的MySQL命令行中给出SQL语句以实现：根据订单总数、消费总额、国家表主
//       * 键三列均逆序排序的方式，查询出前5条，将SQL语句与执行结果截图粘贴至对应报
//       * 告中;
//       */
//      val df: DataFrame = sparkSession.table("dwd.`fact_order`")
//        .join(sparkSession.table("dwd.dim_cust"), "custkey")
//        .join(sparkSession.table("dwd.dim_nation"), "nationname")
//        .join(sparkSession.table("dwd.dim_region"), "regionkey")
//        .withColumn("year", year(col("orderdate")))
//        .withColumn("month", month(col("orderdate")))
//        .groupBy("nationkey", "nationname", "regionkey", "regionname", "year", "month")
//        .agg("nationname" -> "count",
//          "orderprice" -> "sum")
//        .withColumnRenamed("count(nationname)", "totalorder")
//        .withColumnRenamed("sum(orderprice)", "totalconsumption")
//        .select("nationkey", "nationname", "regionkey", "regionname", "totalconsumption", "totalorder", "year", "month")
//      df.show()

//      /**
//       * 请根据dwd层表计算出某年每个国家的平均消费额和所有国家平均消费额相比较
//       * 结果（“高/低/相同”）, 然后在Linux的MySQL命令行中根据订单总数、消费
//       * 总额、国家表主键三列均逆序排序的方式，查询出前5条，将SQL语句与执行结果
//       * 截图粘贴至对应报告中;
//       */
//      val df: DataFrame = sparkSession.table("dwd.`fact_order`")
//        .join(sparkSession.table("dwd.dim_cust"), "custkey")
//        .join(sparkSession.table("dwd.dim_nation"), "nationname")
//        .groupBy("nationkey", "nationname")
//        .avg("orderprice")
//        .withColumnRenamed("avg(orderprice)", "nationavgconsumption")
//        .withColumn("allnationavgconsumption", avg("nationavgconsumption").over())
//        .withColumn("comparison",
//          when(col("nationavgconsumption") > col("allnationavgconsumption"), "high")
//            .otherwise(when(col("nationavgconsumption") < col("allnationavgconsumption"), "low")
//            .otherwise("same")))
//        df.show

      /**
       * 编写Scala工程代码，根据dwd层表统计连续两个月下单并且下单金额保持增长
       * 的用户，订单发生时间限制为大于等于某年，存入MySQL数据库shtd_store的
       * usercontinueorder表中。然后在Linux的MySQL命令行中根据订单总数、消
       * 费总额、客户主键三列均逆序排序的方式，查询出前5条，将SQL语句与执行结果
       * 截图粘贴至对应报告中。
       */
      val df: DataFrame = sparkSession.table("dwd.`fact_order`")
        .join(sparkSession.table("dwd.dim_cust"), "custkey")
        .withColumn("current_month", date_format(col("orderdate"), "yyyyMM"))
        .withColumn("next_month", date_format(add_months(col("orderdate"), 1), "yyyyMM"))
        .withColumn("next_row", lead("current_month", 1).over(Window.partitionBy("custkey").orderBy("current_month")))
        .withColumn("month", when(col("next_month") === col("next_row"), concat_ws("_", col("current_month"), col("next_month"))))
        .filter(col("month") =!= "null")
        .withColumn("totalconsumption", sum("orderprice").over())
        .withColumn("totalorder", count("month").over())
        .select("custkey", "custname", "month", "totalconsumption", "totalorder")
      df.show()
//      val props = new Properties()
//      props.setProperty("user", "root")
//      props.setProperty("password", "123456")
//      df.write
//        .mode(SaveMode.Overwrite)
//        .jdbc("jdbc:mysql://172.18.0.12:3306/shtd_store?useUnicode=true&characterEncoding=utf-8",
//          "nationeverymonth", props)
    } catch {
      case e: Exception =>
        println(e.getMessage)
        e.printStackTrace()
    } finally {
      sparkSession.stop()
    }
  }
}