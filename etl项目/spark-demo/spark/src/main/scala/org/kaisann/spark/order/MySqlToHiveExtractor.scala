package org.kaisann.spark.order

import org.apache.spark.sql.{DataFrame, SparkSession}

class MySqlToHiveExtractor (_dataTransfer: DataTransfer,
                           _extractor: ExtractMode,
                           _partition: Partition,
                           _modify: DataFrame => DataFrame = df => df)
  extends ToHiveExtractor {
  protected override def dataTransfer: DataTransfer = _dataTransfer
  protected override def extractMode: ExtractMode = _extractor
  protected override def partition: Partition = _partition
  protected override def modify: DataFrame => DataFrame = _modify
  /**
   * 父类读取方法的实现，用读取mysql
   * @param sparkSession 用匿名方式获取SparkSession
   *  @return DataFrame
   */
  override protected def reader(implicit sparkSession: SparkSession): DataFrame = {
    sparkSession.read
      .format("jdbc")
      .option("user", "root")
      .option("password", "123456")
      .option("driver", "com.mysql.jdbc.Driver")
      .option("url", "jdbc:mysql://172.18.0.12:3306/shtd_store?useUnicode=true&characterEncoding=utf-8")
      .option("dbtable", dataTransfer.getSource)
      .load()
  }
}
