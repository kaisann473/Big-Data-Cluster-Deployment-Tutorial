# hadoop安装

只配置HDFS和YARN

- ## Hadoop 完全分布式

master-base

```shell
# Hadoop 完全分布式
# 解压缩
cd /opt/software
tar -zxvf hadoop-3.1.3.tar.gz -C /opt/module
# 配置环境变量
vim /etc/profile.d/hadoop.sh
```

```shell
#hadoop
export HADOOP_HOME=/opt/module/hadoop-3.1.3
export PATH=$PATH:$HADOOP_HOME/bin
```

```shell
source /etc/profile
# 修改配置文件
cd $HADOOP_HOME/etc/hadoop

# 配置Hadoop守护进程环境变量

# JAVA_HOME 在当前环境是多余的，但这是个好习惯
# namenode datanode secondarynamenode resourcemanager nodemanager 的用户

vim hadoop-env.sh
```

```shell
export JAVA_HOME=/opt/module/jdk1.8.0_171
export HDFS_NAMENODE_USER=root
export HDFS_DATANODE_USER=root
export HDFS_SECONDARYNAMENODE_USER=root
export YARN_RESOURCEMANAGER_USER=root
export YARN_NODEMANAGER_USER=root
```

```shell
# 配置HADOOP全局

# fs.defaultFS: HDFS的地址(必填)
# hadoop.tmp.dir: 临时目录(默认/tmp会自动清理，照成问题)
# hadoop.http.staticuser.user: 网页登录的静态用户(获取ui上的读写权限)

vim core-site.xml
```

```xml
<property>
    <name>fs.defaultFS</name>
    <value>hdfs://master:9000</value>
</property>
<property>
    <name>hadoop.tmp.dir</name>
    <value>/var/hadoop/tmp</value>
</property>
<property>
    <name>hadoop.http.staticuser.user</name>
    <value>root</value>
</property>
```

```shell
# 配置HDFS

# dfs.namenode.http-address: namenode的服务监听socket 
# dfs.namenode.secondary.http-address: secondarynamenode的服务监听socket

vim hdfs-site.xml
```

```xml
<property>
    <name>dfs.namenode.http-address</name>
    <value>master:9870</value>
</property>
<property>
    <name>dfs.namenode.secondary.http-address</name>
    <value>slave1:9868</value>
</property>
```

```shell
# 配置Yarn

# yarn.resourcemanager.hostname：resourcemanager的hostname
# yarn.nodemanager.vmem-check-enabled：nodemanager虚拟内存检查(开启时，其他组件on Yarn运行时，大概率失败)

vim yarn-site.xml
```

```xml
<property>
    <name>yarn.resourcemanager.hostname</name>
    <value>master</value>
</property>
<property>
    <name>yarn.nodemanager.vmem-check-enabled</name>
    <value>false</value>
</property>
```

```shell
# 配置工作节点

vim workers
```

```text
master
slave1
slave2
```

```shell
# 同步hadoop环境变量和hadoop到集群

# 格式化namenode
hdfs namenode -format

cd $HADOOP_HOME
# 启动整个hadoop集群
sbin/start-dfs.sh
sbin/start-yarn.sh
mapred --daemon start historyserver
```

- ## Hadoop HA

master-hadoop

```shell
# 安装Zookeeper
cd /opt/software
tar -zxvf /opt/software/apache-zookeeper-3.5.7-bin.tar.gz -C /opt/module

vim /etc/profile.d/zookeeper.sh
```

```shell
# zookeeper
export ZOOKEEPER_HOME=/opt/module/apache-zookeeper-3.5.7-bin
export PATH=$PATH:$ZOOKEEPER_HOME/bin
```

```shell
source /etc/profile

cd $ZOOKEEPER_HOME/conf

cp zoo_sample.cfg zoo.cfg
vim zoo.cfg
```

```conf
dataDir=/opt/module/apache-zookeeper-3.5.7-bin/data
server.1=master:2888:3888
server.2=slave1:2888:3888
server.3=slave2:2888:3888
```

```shell
cd $ZOOKEEPER_HOME
mkdir -p $ZOOKEEPER_HOME/data
cd $ZOOKEEPER_HOME/data
vim myid
```

```text
1
```

```shell
# 同步hadoop环境变量和hadoop到集群
```

slave1-hadoop

```shell
source /etc/profile

vim $ZOOKEEPER_HOME/data/myid
```

```shell
2
```

slave2-hadoop

```shell
source /etc/profile

vim $ZOOKEEPER_HOME/data/myid
```

```shell
3
```

master-hadoop

```shell
zkServer.sh start 
```

slave1-hadoop

```shell
zkServer.sh start 
```

slave2-hadoop

```shell
zkServer.sh start 

```

配置Hadoop HA  

```shell
# Hadoop HA
# 解压缩
cd /opt/software
tar -zxvf hadoop-3.1.3.tar.gz -C /opt/module
# 配置环境变量
vim /etc/profile.d/hadoop.sh
```

```shell
#hadoop
export HADOOP_HOME=/opt/module/hadoop-3.1.3
export PATH=$PATH:$HADOOP_HOME/bin
```

```shell
source /etc/profile
# 修改配置文件
cd $HADOOP_HOME/etc/hadoop

# 配置Hadoop守护进程环境变量

# JAVA_HOME 在当前环境是多余的，但这是个好习惯
# namenode datanode secondarynamenode resourcemanager nodemanager zkfc journalnode 的用户

vim hadoop-env.sh
```

```shell
export JAVA_HOME=/opt/module/jdk1.8.0_171
export HDFS_NAMENODE_USER=root
export HDFS_DATANODE_USER=root
export HDFS_SECONDARYNAMENODE_USER=root
export YARN_RESOURCEMANAGER_USER=root
export YARN_NODEMANAGER_USER=root
export HDFS_ZKFC_USER=root
export HDFS_JOURNALNODE_USER=root
```

```shell
# 配置HADOOP全局

# fs.defaultFS: HDFS的地址(必填)
# hadoop.tmp.dir: 临时目录(默认/tmp会自动清理，照成问题)
# hadoop.http.staticuser.user: 网页登录的静态用户(获取ui上的读写权限)
# ha.zookeeper.quorum: ZooKeeper 集群的地址

vim core-site.xml
```

```xml
<property>
    <name>fs.defaultFS</name>
    <value>hdfs://mycluster</value>
</property>
<property>
    <name>hadoop.tmp.dir</name>
    <value>/var/hadoop/tmp</value>
</property>
<property>
    <name>hadoop.http.staticuser.user</name>
    <value>root</value>
</property>
<property>
    <name>ha.zookeeper.quorum</name>
    <value>master:2181,slave1:2181,slave2:2181</value>
</property>
```

```shell
# 配置HDFS

# dfs.nameservices: nameservices的逻辑名称
# fs.ha.namenodes.mycluster:  NameNode 节点列表
# dfs.namenode.rpc-address.mycluster.nn?: 某个NameNode节点的RPC地址 
# dfs.namenode.http-address.mycluster.nn?：某个NameNode节点的HTTP地址
# dfs.namenode.shared.edits.dir: 多个NameNode节点之间共享的编辑日志目录
# dfs.journalnode.edits.dir: JournalNode节点存储 HDFS编辑日志的目录
# dfs.ha.automatic-failover.enabled: 自动故障转移功能
# dfs.client.failover.proxy.provider.mycluster: 客户端使用的故障转移代理的类
# dfs.ha.fencing.methods: 使用的切换机制
# dfs.namenode.secondary.http-address secondarynamenode的服务监听socket

vim hdfs-site.xml
```

```xml
<property>
    <name>dfs.nameservices</name>
    <value>mycluster</value>
</property>
<property>
    <name>dfs.ha.namenodes.mycluster</name>
    <value>nn1,nn2</value>
</property>
<property>
    <name>dfs.namenode.rpc-address.mycluster.nn1</name>
    <value>master:9000</value>
    </property>
<property>
    <name>dfs.namenode.http-address.mycluster.nn1</name>
    <value>master:9870</value>
</property>
<property>
    <name>dfs.namenode.rpc-address.mycluster.nn2</name>
    <value>slave1:9000</value>
    </property>
<property>
    <name>dfs.namenode.http-address.mycluster.nn2</name>
    <value>slave1:9870</value>
</property>
<property>
    <name>dfs.namenode.shared.edits.dir</name>
    <value>qjournal://master:8485;slave1:8485;slave2:8485/mycluster</value>
</property>
<property>
    <name>dfs.journalnode.edits.dir</name>
    <value>/var/hadoop/dfs/journalnode/</value>
</property>
<property>
    <name>dfs.ha.automatic-failover.enabled</name>
    <value>true</value>
</property>
<property>
    <name>dfs.client.failover.proxy.provider.mycluster</name>
    <value>org.apache.hadoop.hdfs.server.namenode.ha.ConfiguredFailoverProxyProvider</value>
</property>
<property>
    <name>dfs.ha.fencing.methods</name>
    <value>shell(/bin/true)</value>
</property>
<property>
    <name>dfs.namenode.secondary.http-address</name>
    <value>slave2:9868</value>
</property>
```

```shell
# 配置Yarn

# yarn.resourcemanager.ha.enabled: 启用或禁用ResourceManager的HA
# yarn.resourcemanager.cluster-id: YARN集群指定唯一标识符
# yarn.resourcemanager.ha.rm-ids: RM的ID列表
# yarn.resourcemanager.hostname.rm?: 某个RM的主机名
# yarn.resourcemanager.zk-address: ZooKeeper群集地址
# yarn.log-aggregation-enable: 启用或禁用日志聚合
# yarn.log-aggregation.retain-seconds: 保留聚合日志的时间量
# yarn.resourcemanager.recovery.enabled: 启用或禁用ResourceManager恢复
# yarn.resourcemanager.store.class: 提供ResourceManager状态存储的类
# yarn.nodemanager.vmem-check-enabled：nodemanager虚拟内存检查(开启时，其他组件on Yarn运行时，大概率失败)

vim yarn-site.xml
```

```xml
<property>
    <name>yarn.resourcemanager.ha.enabled</name>
    <value>true</value>
</property>
<property>
    <name>yarn.resourcemanager.cluster-id</name>
    <value>yrc</value>
</property>
<property>
    <name>yarn.resourcemanager.ha.rm-ids</name>
    <value>rm1,rm2</value>
</property>
<property>
    <name>yarn.resourcemanager.hostname.rm1</name>
    <value>slave1</value>
</property>
<property>
    <name>yarn.resourcemanager.hostname.rm2</name>
    <value>slave2</value>
</property>
<property>
    <name>yarn.resourcemanager.zk-address</name>
    <value>master:2181,slave1:2181,slave2:2181</value>
</property>
<property>
    <name>yarn.log-aggregation-enable</name>
    <value>true</value>
    </property>
<property>
    <name>yarn.log-aggregation.retain-seconds</name>
    <value>86400</value>
</property>
<property>
    <name>yarn.resourcemanager.recovery.enabled</name>
    <value>true</value>
</property>
<property>
    <name>yarn.resourcemanager.store.class</name>
    <value>org.apache.hadoop.yarn.server.resourcemanager.recovery.ZKRMStateStore</value>
</property>
<property>
    <name>yarn.nodemanager.vmem-check-enabled</name>
    <value>false</value>
</property>
```

```shell
# 配置工作节点

vim workers
```

```text
master
slave1
slave2
```

```shell
# 同步hadoop环境变量和hadoop到集群

# 格式化namenode、zkfc

# 在所有虚拟机上启动journalnode
```

master-hadoop

```shell
hdfs --daemon start journalnode
```

slave1-hadoop

```shell
hdfs --daemon start journalnode
```

slave2-hadoop

```shell
hdfs --daemon start journalnode
```

master-hadoop

```shell
# 格式化其中一个namenode
hadoop namenode -format

# 单独启动namenode
hdfs namenode
```

slave1-hadoop

```shell
# 同步格式化好的namenode
hdfs namenode -bootstrapStandby
```

master-hadoop

```shell
# 格式化zkfc
hdfs zkfc -formatZK
```

```shell
cd $HADOOP_HOME
# 启动hadoop集群
sbin/start-dfs.sh
sbin/start-yarn.sh
```

----
hadoop完全分布式  
[hadoop3.x集群部署-配置HDFS、yarn、MapReduce](https://www.malaoshi.top/show_1IX1tjXoC8D2.html)  
[Hadoop3.1.1全分布式安装部署](https://www.youyoustudio.com/2019/05/09/171.html)  
[Hadoop 3 default ports](https://www.stefaanlippens.net/hadoop-3-default-ports.html)  
[Hadoop解决 java.net.BindException: Port in use: hadoop103:8088](https://blog.csdn.net/weixin_43950862/article/details/114481146)  
[Hadoop hdfs 访问权限问题](https://www.cnblogs.com/kpwong/p/13869301.html)  
hadoop ha  
[七、Hadoop3.3.1 HA 高可用集群QJM （基于Zookeeper，NameNode高可用+Yarn高可用）](https://www.cnblogs.com/lehoso/p/15591387.html#%E4%BF%AE%E6%94%B9-vim-core-sitexml)  
[Hadoop3.x入门-搭建3节点Hadoop HA集群](https://blog.csdn.net/u013189824/article/details/123947841)  
[Hadoop集群大数据解决方案之搭建Hadoop3.X+HA模式（二）](https://blog.csdn.net/LXWalaz1s1s/article/details/90734197)  
[hadoop2.0 HA的主备自动切换](https://blog.51cto.com/sstudent/1388865)
[HDFS中的HA原理解析](https://www.jianshu.com/p/eb077c9d0f1e)  
[Hadoop3.x配置3-5个NameNode-HA](https://www.modb.pro/db/132200)  
[YARN HA 配置文件设置](https://www.cnblogs.com/benfly/p/8118525.html)  
[Hadoop3 HA高可用集群搭建](http://windcf.com/archives/83)  
[Hadoop3 Yarn ha搭建及测试排错](https://www.cnblogs.com/shine-rainbow/p/17154161.html)  
[Yarn Active ResourceManager 重启作业不中断配置](https://hwilu.github.io/2019/10/09/Yarn-RM-recovery/)  
[在linux的shell中/bin/true是什么意思？](https://blog.csdn.net/u010154760/article/details/44940371)  
[YARN 聚合日志配置](https://blog.csdn.net/duyenson/article/details/118994693)  
[格式化namenode时报连接错误](https://blog.csdn.net/ASN_forever/article/details/82460045)  
[解决启动zookeeper时Could not find or Load main class org.apache.zookeeper.server.quorum.QuorumPeerMain的报错](https://blog.csdn.net/Melo_FengZhi/article/details/109121715)  
[HDFS ha 格式化报错：a shared edits dir must not be specified if HA is not enabled](https://www.cnblogs.com/woofwoof/p/10261542.html)  
[hadoop HA 集群启动发现现datanode没有启动，namenode clusterID与datanode clusterID不兼容，不匹配。](https://blog.csdn.net/chengzi_home/article/details/79569662)  
[hadoop 三节点集群搭建后只有一个 datanode？](https://juejin.cn/post/7128471298933522469)  

----
弃用部分

MapReduce的配置，集群直接使用Spark和Flink作为计算框架，完全不需要MapReduce，只需要配置HDFS和YARN

- hadoop 完全分布式

```shell
# 配置Mapreduce(并不会用到Mapreduce，请忽略)

# mapreduce.framework.name: mapreduce工作模式

vim mapred-site.xml
```

```xml
<property>
    <name>mapreduce.framework.name</name>
    <value>yarn</value>
</property>
```

- hadoop ha

```shell
```shell
# 配置Mapreduce

# mapreduce.framework.name: mapreduce工作模式
# mapreduce.jobhistory.address: JobHistory服务器的socket
# mapreduce.jobhistory.webapp.address: JobHistory服务器Web应用程序的socket
vim mapred-site.xml
```

```xml
<property>
    <name>mapreduce.framework.name</name>
    <value>yarn</value>
</property>
<property>
    <name>mapreduce.jobhistory.address</name>
    <value>master:10020</value>
</property>
<property>
    <name>mapreduce.jobhistory.webapp.address</name>
    <value>master:19888</value>
</property>
```

----
[FLINK HADOOP_CLASSPATH设置，集成hadoop](https://blog.csdn.net/qq_33358554/article/details/110918177)  