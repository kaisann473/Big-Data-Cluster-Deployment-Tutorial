package org.kaisann.flink.etl

import org.apache.flink.configuration.Configuration
import org.apache.flink.streaming.api.functions.sink.{RichSinkFunction, SinkFunction}
import org.apache.hadoop.hbase.{HBaseConfiguration, HConstants, TableName}
import org.apache.hadoop.hbase.client.{Connection, ConnectionFactory, Put, Table}
import org.apache.hadoop.hbase.util.Bytes

class HBaseSink extends RichSinkFunction[(String, Int)] {
  private var conn: Connection = _
  private var table: Table = _

  override def open(parameters: Configuration): Unit = {
    println("新的")
    val config: org.apache.hadoop.conf.Configuration = HBaseConfiguration.create
    config.set(HConstants.ZOOKEEPER_QUORUM, "172.18.0.10,172.18.0.11,172.18.0.12")
    config.set(HConstants.ZOOKEEPER_CLIENT_PORT, "2181")
    config.setInt(HConstants.HBASE_CLIENT_OPERATION_TIMEOUT, 30000)
    config.setInt(HConstants.HBASE_CLIENT_SCANNER_TIMEOUT_PERIOD, 30000)
    conn = ConnectionFactory.createConnection(config)

    val tableName: TableName = TableName.valueOf("Word")
    table = conn.getTable(tableName)
//    Thread.sleep(5 * 1000)
  }

  override def invoke(value: (String, Int), context: SinkFunction.Context): Unit = {
    val put: Put = new Put(Bytes.toBytes(value._1))
    put.addColumn(Bytes.toBytes("info"), Bytes.toBytes("count"), Bytes.toBytes(value._2.toString))
    println(s"写入${value._1}，${value._2}")
    table.put(put)
  }

  override def close(): Unit = {
    table.close()
    conn.close()
  }
}
