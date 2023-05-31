package org.kaisann.spark.order

/**
 * 表示一个数据集的分区，一个类家族的特质
 */
sealed trait Partition {
  def partitionKey: String

  /**
   * 提供公共的分区表达式获取方式
   * @return 分区表达式
   */
  def getExpr: String
}

/**
 *  partition家族的伴生对象，提供创建 partition 实例的工厂方法
 */
object Partition {
  /**
   * 静态分区，分区特质的实现
   *
   * @param _partitionKey  分区键
   * @param partitionValue 分区值
   */
  private case class StaticPartition(_partitionKey: String,
                        partitionValue: String) extends Partition {
    override def partitionKey: String = _partitionKey
    override def getExpr: String = {
      s"$partitionKey = $partitionValue"
    }
  }

  /**
   * 动态分区， 分区特质的实现
   *
   * @param _partitionKey 分区键
   */
  private case class DynamicPartition(_partitionKey: String) extends Partition {
    override def partitionKey: String = _partitionKey
    override def getExpr: String = {
      partitionKey
    }
  }
  /**
   * 工厂模式创建 partition的实例
   * @param partitionKey 分区键
   * @param partitionValue 静态分区要选择的分区键值
   * @return partition实例
   */
  def column(partitionKey: String,
               partitionValue: String = null): Partition = partitionValue match {
    case null => DynamicPartition(partitionKey)
    case _ => StaticPartition(partitionKey, partitionValue)
  }
}




