# 大数据集群安装分享

## 包含内容

- 简单的安装教程
- 简单的开启教程
- 零星的基础知识

## 版本信息 
| 名称      | 版本    |
|----------|---------|
| Hadoop   | 3.1.3   |
| ZooKeeper | 3.5.7   |
| Hive     | 3.1.2   |
| Hudi     | 0.12.0  |
| ClickHouse| 21.9.4  |
| JDK       | 1.8     |
| Flume    | 1.9.0   |
| Kafka    | 2.4.1   |
| Spark    | 3.1.1   |
| Flink    | 1.14.0  |
| Redis    | 6.2.6   |
| HBase    | 2.2.3   |
| Azkaban  | 3.84.4  |
| MySQL    | 5.7     |
| Scala    | 2.12.10 |

## 相关的镜像

- 未安装版（用于练习）

```shell
docker pull kaisann/centos:master
docker pull kaisann/centos:slave1
docker pull kaisann/centos:slave2
```

- 完全版用于(学习组件)

```shell
docker pull kaisann/centos:master-finish
docker pull kaisann/centos:slave1-finish
docker pull kaisann/centos:slave2-finish
```

- BUG

ClickHouse 无法启动

```text
2023.06.22 14:24:23.253260 [ 18179 ] {} <Error> Application: filesystem error: in rename: Invalid cross-device link [/var/lib/clickhouse/store/0cf/0cf93d21-5a39-4a97-8cf9-3d215a392a97/202305_1_11_2] [/var/lib/clickhouse/store/0cf/0cf93d21-5a39-4a97-8cf9-3d215a392a97/delete_tmp_202305_1_11_2]
```
原因：
无效跨设备链接, `/var/lib/clickhouse/store`存在另一台机器的数据，照成冲突，情况`store`重启服务即可


[Clickhouse](https://blog.csdn.net/weixin_45912745/article/details/121982209)
