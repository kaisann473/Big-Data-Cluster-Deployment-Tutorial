package org.kaisann.spark.order

import org.apache.spark.sql.{DataFrame, SparkSession}

trait Extractor {
  protected def reader(implicit sparkSession: SparkSession): DataFrame

  protected def modify: DataFrame => DataFrame

  def extract(implicit sparkSession: SparkSession): Unit
}
