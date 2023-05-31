# Flume



- source的类型
  1. netcat
    netcat用于监听一个指定的端口的tcp请求数据
  2. avro
    avro source接收的是avro序列化后的数据，然后反序列化后继续传输， 可以实现以上三种复杂流动
  3. exec
    接收的linux命令的结果
  4. sequence generator
    主要用于测试，产生不断自增的序列数字， 从0起步
  5. spooling directory source
    spooling directory source比较常用，它可以监视某个目录下的新文件，并写入到channel，写完后的文件会重命名添加complete标签到文件名
  6. HTTP
    可以接受http post和get请求。get请求不稳定只能用在测试，通常使用POST请求。http请求通过一个可插拔的'handler'(需实现HTTPSourceHandler接口)可以转换为event数组
    1. JSONHandler可以处理json格式的event，支持UTF-8、UTF-16和UTF-32字符集，它将接受一个event数组，将其转换为flume event，转换的编码基于request请求中指定的字符集，如果没指定就默认是UTF-8
    2. BlobHandler一般用于上传的请求，如上传PDF或JPG文件，它可以将其转换为包含请求参数和BLOB（binary large object）的event。

- souce selector  
    选择器selector是souce的子组件
    1. replicating模式，即复制模式，source获取到数据后会复制一份，然后发送到两个节点。
    2. multiplexing，即路由模式，并在配置文件中指定区分的基准

- souce interceptor  
    1. timestamp interceptor
      时间戳拦截器，event消息头里将包含毫秒为单位的时间戳
    2. host interceptor
      在event的headers里展示运行agent的主机ip或名字
    3. static interceptor
      自定添加一个key-value到消息头
    4. UUID interceptor
      让消息头里添加一个全局唯一标识的id
    5. search and replace interceptor
      需要替换和搜索某些字符串，可以使用这个拦截器，需要配合正则表达式
    6. regex filter interceptor
      解析事件体，根据正则表达式来筛选

- channel的类型
  1. memory
    速度快，但是会丢数据
  2. file
    速度会慢，但是可靠性有保证，数据不易丢失
  3. jdbc
    采用derby为缓存
  4. SPILLABLEMEMORY
    类似shuffle中的环形缓冲区
- slink的类型
  1. logger 
    打印成日志到控制台
  2. file roll
    每隔一段时间将采集的日志写入到文件，可以用于文件合并
  3. hdfs
    将数据写入到hdfs，实际较为常用
  4. avro
    实现复杂数据流的基础，上一个agent发送avro序列化数据后，下一个agent通过source再接收avro序列化数据，再反序列化后继续传输




flume-ng agent --conf conf --conf-file $FLUME_HOME/example.conf --name a1 -Dflume.root.logger=INFO,console

docker run -itd --name uploader --net mynetwork --ip 172.18.0.13 --privileged=true centos:7 /sbin/init

kafka-console-consumer.sh --bootstrap-server master:9092,slave1:9092,slave2:9092 --topic test

flume-ng agent --conf conf --conf-file $FLUME_HOME/example.conf --name a1 -Dflume.root.logger=INFO,console

[Flume使用入门](https://www.cnblogs.com/youngchaolin/p/12218605.html#_label4)