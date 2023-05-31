package org.kaisann.spark.order
import org.apache.spark.sql.{DataFrame, SparkSession}

class HiveToHiveExtractor (_dataTransfer: DataTransfer,
                           _extractor: ExtractMode,
                           _partition: Partition,
                           _modify: DataFrame => DataFrame = df => df) extends ToHiveExtractor {
  override protected def dataTransfer: DataTransfer = _dataTransfer
  override protected def extractMode: ExtractMode = _extractor
  override protected def partition: Partition = _partition
  override protected def modify: DataFrame => DataFrame = _modify

  override protected def reader (implicit sparkSession: SparkSession): DataFrame = {
    sparkSession.table(dataTransfer.getSource)
  }
}
