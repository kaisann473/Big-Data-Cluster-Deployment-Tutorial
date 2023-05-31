package org.kaisann.spark.order

/**
 * 表示一个数据集的抽取模式，一个类家族的特质
 */
sealed trait ExtractMode {
  def mode: String
  def incrementalKey: Option[String]
}
/**
 *  extractor家族的伴生对象，提供创建 extractor 实例的工厂方法
 */
object ExtractMode {
  val OVERWRITE: String = "overwrite"
  val APPEND: String = "append"

  /**
   * 全量抽取模式，并且只有Extractor伴生对象能给访问它
   * @param _mode 传入抽取模式
   */
  private case class FullExtractor(_mode: String) extends ExtractMode  {
    override def mode: String = _mode
    override def incrementalKey: Option[String] = None
  }
  /**
   * 增量抽取，抽取类的实现
   * @param _mode 分区模式
   * @param _incrementalKey 增量键
   */
  private case class IncrementalExtractor(_mode: String,
                                     _incrementalKey: String) extends ExtractMode {
    override def mode: String = _mode
    def incrementalKey: Option[String] = Some(_incrementalKey)
  }

    /**
   * 工厂模式创建extractor的实例
   * @param mode 抽取模式
   * @param incrementalKey 根据此列筛选出增量部分
   * @return extractor的实例
   */
  def select(mode: String, incrementalKey: String = null): ExtractMode = mode match {
    case OVERWRITE => FullExtractor("overwrite")
    case APPEND =>
      if (incrementalKey == null) {
      throw new IllegalArgumentException("IncrementalKey can not be empty in Append Mode")
    }
      IncrementalExtractor("into", incrementalKey)
    case _ => throw new IllegalArgumentException("Mode not found")
  }
}