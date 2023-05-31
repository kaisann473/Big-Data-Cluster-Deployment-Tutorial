# hbase安装

## 共同部分

```shell
# 解压
cd /opt/software
tar -zxvf hbase-2.2.3-bin.tar.gz -C /opt/module

# 配置环境变量
vim /etc/profile.d/hbase.sh
```

```shell
#hbase
export HBASE_HOME=/opt/module/hbase-2.2.3
export PATH=$PATH:$HBASE_HOME/bin
```

```shell
# 应用环境变量
source /etc/profile

# 编辑配置文件

# 配置hbase守护进程环境变量

# JAVA_HOME 在当前环境是多余的，但这是个好习惯
# HBASE_MANAGES_ZK 开启或关闭自带zookeeper
# HBASE_PID_DIR hbase的进程号存放地址

cd $HBASE_HOME/conf
vim hbase-env.sh
```

```shell
export JAVA_HOME=/opt/module/jdk1.8.0_171
export HBASE_MANAGES_ZK=false 
export HBASE_PID_DIR=/var/hadoop/hbase
```

```shell
vim regionservers
```

```shell
master
slave1
slave2
```

## 分歧部分

```shell
# 配置hbase

# hbase.rootdir: hbase的根目录
# hbase.cluster.distributed: 是否开启集群模式
# hbase.zookeeper.property.dataDir: zookeeper的根目录
# hbase.zookeeper.quorum：zookeeper的通讯地址
# hbase.unsafe.stream.capability.enforce: 是否限制非安全的流式操作

vim hbase-site.xml
```

- ### hadoop完全分布式

```xml
<property>
    <name>hbase.rootdir</name>
    <value>hdfs://master:9000/hbase</value>
</property>
<property> 
 <name>hbase.cluster.distributed</name> 
 <value>true</value> 
 </property>  
<property> 
    <name>hbase.zookeeper.property.dataDir</name> 
    <value>/opt/module/apache-zookeeper-3.5.7-bin/data</value> 
</property> 
<property>
    <name>hbase.zookeeper.quorum</name>
    <value>master,slave1,slave2</value>
</property>
<property>
    <name>hbase.unsafe.stream.capability.enforce</name>
    <value>false</value>
</property>
```

- ### hadoop ha

```xml
<property>
    <name>hbase.rootdir</name>
    <value>hdfs://mycluster/hbase</value>
</property>
<property> 
 <name>hbase.cluster.distributed</name> 
 <value>true</value> 
 </property>  
<property> 
    <name>hbase.zookeeper.property.dataDir</name> 
    <value>/opt/module/apache-zookeeper-3.5.7-bin/data</value> 
</property> 
<property>
    <name>hbase.zookeeper.quorum</name>
    <value>master,slave1,slave2</value>
</property>
<property>
    <name>hbase.unsafe.stream.capability.enforce</name>
    <value>false</value>
</property>
```

```shell
# 分布到集群
```

## 资料来源

----
[Centos7安装hadoop + zookeeper + hbase HA集群](https://www.cnblogs.com/xiaoleimagic/p/15897556.html)  
[hbase2.2.3安装记录](https://blog.csdn.net/qq_44870331/article/details/116733463)  
[HBase关闭没有master.pid的问题](https://blog.csdn.net/qq_44958979/article/details/118423061)  
[解决基于Hadoop3.1.3下 HMaster启动不起来（Hadoop 3.1.3 + HBase 2.2.4 ）](https://blog.csdn.net/h1101723183/article/details/107136301)  
[解决Hbase报错java.lang.IllegalStateException: The procedure WAL relies on the ability to hsync for....](https://blog.csdn.net/weixin_35757704/article/details/119058666)
