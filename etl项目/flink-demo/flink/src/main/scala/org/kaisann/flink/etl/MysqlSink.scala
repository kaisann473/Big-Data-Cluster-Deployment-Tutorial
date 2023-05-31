package org.kaisann.flink.etl

import org.apache.flink.configuration.Configuration
import org.apache.flink.streaming.api.functions.sink.{RichSinkFunction, SinkFunction}

import java.sql.{Connection, DriverManager, PreparedStatement}

class MysqlSink extends RichSinkFunction[(String, Int)] {
  private lazy val DB_URL = "jdbc:mysql://172.18.0.12:3306/shtd_store"
  private lazy val USER: String = "root"
  private lazy val PASSWORD: String = "123456"

  private var conn: Connection = _
  private var insertStmt: PreparedStatement  = _
  private var updateStmt: PreparedStatement  = _

  override def open(parameters: Configuration): Unit = {
    conn = DriverManager.getConnection(DB_URL, USER, PASSWORD)
    insertStmt = conn.prepareStatement("insert into wordcount (word,num1) values (?,?)")
    updateStmt = conn.prepareStatement("update wordcount set num1 = ? where word = ?")
  }
  override def invoke(value: (String, Int), context: SinkFunction.Context): Unit = {
    println("----执行了更新操作---")
    updateStmt.setString(2, value._1)
    updateStmt.setInt(1, value._2)
    updateStmt.execute()
    if (updateStmt.getUpdateCount == 0) {
      println("----执行了插入操作----")
      insertStmt.setString(1, value._1)
      insertStmt.setInt(2, value._2)
      insertStmt.execute()
    }
  }

  override def close(): Unit = {
    if (insertStmt != null) insertStmt.close()
    if (insertStmt != null) updateStmt.close()
    if (insertStmt != null) conn.close()
  }
}
