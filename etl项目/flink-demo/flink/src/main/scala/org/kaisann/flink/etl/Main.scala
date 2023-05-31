package org.kaisann.flink.etl

import org.apache.flink.api.common.eventtime.WatermarkStrategy
import org.apache.flink.api.common.restartstrategy.RestartStrategies
import org.apache.flink.api.common.serialization.SimpleStringSchema
import org.apache.flink.api.scala.createTypeInformation
import org.apache.flink.connector.kafka.source.KafkaSource
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer
import org.apache.flink.streaming.api.functions.ProcessFunction
import org.apache.flink.streaming.api.scala.{DataStream, OutputTag, StreamExecutionEnvironment}
import org.apache.flink.streaming.connectors.redis.RedisSink
import org.apache.flink.streaming.connectors.redis.common.config.FlinkJedisClusterConfig
import org.apache.flink.streaming.connectors.redis.common.mapper.{RedisCommand, RedisCommandDescription, RedisMapper}
import org.apache.flink.util.Collector

import java.net.InetSocketAddress
import java.util

object Main {
  private lazy val REDIS_CLUSTER_NODES: util.Set[InetSocketAddress] = new util.HashSet[InetSocketAddress](
    util.Arrays.asList(
      new InetSocketAddress("172.18.0.10", 7000),
      new InetSocketAddress("172.18.0.10", 7001),
      new InetSocketAddress("172.18.0.11", 7000),
      new InetSocketAddress("172.18.0.11", 7001),
      new InetSocketAddress("172.18.0.12", 7000),
      new InetSocketAddress("172.18.0.12", 7001)
    ))

  private lazy val source: KafkaSource[String] = KafkaSource.builder[String]()
    .setBootstrapServers("172.18.0.10:9092,172.18.0.11:9092,172.18.0.12:9092")
    .setTopics("test")
    .setGroupId("test")
    .setStartingOffsets(OffsetsInitializer.earliest)
    .setValueOnlyDeserializer(new SimpleStringSchema)
    .build()

  private lazy val hbaseOutputTag = new OutputTag[(String, Int)]("hbase-side-output")

  private lazy val mysqlOutputTag = new OutputTag[(String, Int)]("mysql-side-output")

  private lazy val redisConf: FlinkJedisClusterConfig = new FlinkJedisClusterConfig.Builder()
    .setNodes(REDIS_CLUSTER_NODES)
    .setPassword("123456")
    .setTimeout(30000)
    .build()

  private lazy val MyRedisMapper = new RedisMapper[(String, Int)] {
    override def getCommandDescription: RedisCommandDescription =
      new RedisCommandDescription(RedisCommand.HSET, "words")

    override def getKeyFromData(t: (String, Int)): String = t._1

    override def getValueFromData(t: (String, Int)): String = t._2.toString

  }

  def main(args: Array[String]): Unit = {
    val env = StreamExecutionEnvironment.getExecutionEnvironment
    env.setRestartStrategy(RestartStrategies.noRestart())

    val stream: DataStream[String] =
      env.fromSource(source,
      WatermarkStrategy.noWatermarks[String],
      "Kafka Source")

    val result: DataStream[(String, Int)] =
      stream.flatMap(_.split(" "))
        .map((_, 1))
        .keyBy(_._1)
        .sum(1)
    result.print()

    val branchPoint = result.process(new ProcessFunction[(String, Int), (String,Int)] {
      override def processElement(
                                   value: (String, Int),
                                   ctx: ProcessFunction[(String, Int), (String, Int)]#Context,
                                   out: Collector[(String, Int)]): Unit = {
        out.collect(value)
        ctx.output(hbaseOutputTag, value)
        ctx.output(mysqlOutputTag, value)
      }
    })
    val redisOutputStream = branchPoint
    val mysqlOutputStream = branchPoint.getSideOutput(mysqlOutputTag)
    val hbaseOutputStream = branchPoint.getSideOutput(hbaseOutputTag)

    redisOutputStream.addSink(new RedisSink[(String, Int)](redisConf, MyRedisMapper)).name("Redis")
    mysqlOutputStream.addSink(new MysqlSink).name("MySQL")
    hbaseOutputStream.addSink(new HBaseSink).name("HBase")

    env.execute()
  }
}
