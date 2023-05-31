# Spark SQL

## Spark SQL是什么

Spark SQL 是 Spark 中用来处理结构化数据的一个模块，它提供了一个编程抽象（DataFrame），并且可以作为分布式 SQL 的查询引擎。
在 Spark 中，Spark SQL 并不仅仅是狭隘的 SQL，而是作为 Spark 程序优化、执行的核心组件。

## Spark SQL 的架构

Spark SQL 由 Core，Catalyst，Hive 和 Hive-Thriftserver 共 4 个部分组成。

- Core：负责处理数据的输入/输出，从不同的数据源获取数据（如 RDD,HDFS,Parquet 文件和 JSON 文件等），然后将查询结果输出成 Data Frame。
- Catalyst：负责处理查询语句的整个执行过程，包括解析、绑定、优化、生成物理计划等。
- Hive：负责对 Hive 数据的处理。
- Hive-Thriftserver：提供 Client 和 JDBC/ODBC 等接口。

Spark SQL 核心：Catalyst 查询编译器
Spark SQL 的核心是一个叫做 Catalyst 的查询编译器，它将用户程序中的 SQL/DataFrame/Dataset 经过一系列的操作，最终转化为 Spark 系统中执行的 RDD。
Catalyst 查询编译器组成
Catalyst 有以下几个重要的组成部分：

### 1. Parser

将 SQL/DataFrame/Dataset 转化成一棵未经解析（Unresolved）的树，在 Spark 中称为逻辑计划（Logical Plan），它是用户程序的一种抽象。

### 1. Analyzer

利用目录（Catalog）中的信息，对 Parser 中生成的树进行解析。

Analyzer 有一系列规则（Rule）组成，每个规则负责某项检查或者转换操作，如解析 SQL 中的表名、列名，同时判断它们是否存在。

通过 Analyzer，我们可以得到解析后的逻辑计划。

### 3. Optimizer

对解析完的逻辑计划进行树结构的优化，以获得更高的执行效率。

优化过程也是通过一系列的规则来完成，常用的规则如谓词下推（Predicate Pushdown）、列裁剪（Column Pruning）、连接重排序（Join Reordering）等。

此外，Spark SQL 中还有一个基于成本的优化器（Cost-based Optimizer），是由 DLI 内部开发并贡献给开源社区的重要组件。该优化器可以基于数据分布情况，自动生成最优的计划。

### 4. Planner

将优化后的逻辑计划转化成物理执行计划（Physical Plan）。

由一系列的策略（Strategy）组成，每个策略将某个逻辑算子转化成对应的物理执行算子，并最终变成 RDD 的具体操作。

注意在转化过程中，一个逻辑算子可能对应多个物理算子的实现，如 join 可以实现成 SortMergeJoin 或者 BroadcastHashJoin，这时候需要基于成本模型（Cost Model）来选择较优的算子。

上面提到的基于成本的优化器在这个选择过程中也能起到关键的作用。

整个 Catalyst 框架拥有良好的可扩展性，开发者可以根据不同的需求，灵活地添加自己的语法、解析规则、优化规则和转换策略。

### 基本 SQL 运行流程

传统关系型数据库中 ，最基本的 SQL 查询语句由 Projection (a1, a2, a3) 、DataSource (table A) 和 Filter (condition) 三部分组成。
分别对应了 SQL 查询过程中的 Result、DataSource 和 Operation，也就是按照 Result --> DataSource --> Operation 的顺序来描述。
但是 SQL 的实际执行过程是按照 Operation --> DataSource --> Result 的顺序来执行的，这与 SQL 的语法正好相反。

具体的执行过程如下：  

Parse 对应 Operation
Bind 对应 DataSource
Result 对应 Optimize Execute

1. 词法和语法解析（Parse）：对写入的 SQL 语句进行词法和语法解析，分辨出 SQL 语句中哪些是关键词（如 select、from 和 where）、哪些是表达式、哪些是 Projection、哪些是 DataSource 等，判断 SQL 语法是否规范，并形成逻辑计划。
2. 绑定（Bind）：将 SQL 语句和数据库的数据字典（列、表、视图等）进行绑定，如果相关的 Projection 和 DataSource 等都在的话，则表示这个 SQL 语句是可以执行的，并生成可执行计划。
3. 优化（Optimize）：一般的数据库会提供几个执行计划，这些计划一般都有运行统计数据，数据库会在这些计划中选择一个最优的计划，生成最优执行计划。
4. 执行（Execute）：执行前面的步骤获取到的最优执行计划，返回实际查询得到的数据集。

Spark SQL 运行流程

将用户编写的 SQL 语句（或 DataFrame/Dataset），转换为 Spark 内部 RDD 的具体操作逻辑，最终提交到集群执行计算

1. 使用 SessionCatalog 保存元数据
2. 在解析 SQL 语句前需要初始化 SQLContext，它定义 Spark SQL 上下文，在输入 SQL 语句前会加载 SessionCatalog。

初始化 SQLContext 时会把元数据保存在 SessionCatalog 中，包括数据库名、表名、字段名、字段类型等。这些数据将在解析未绑定的逻辑计划上使用。

2. 使用 Antlr 生成未绑定的逻辑计划

Spark2.0 起使用 Antlr 进行词法和语法解析，Antlr 会构建一个按照关键字生成的语法树，也就是未绑定的逻辑执行计划（Unresolved Logical Plan），包含 Unresolved Relation、Unresolved Function 和 Unresolved Attribute。
![解析 SQL，生成抽象语法树（未绑定的逻辑执行计划](https://ask.qcloudimg.com/http-save/yehe-9360138/2445d8a72544988cc2255c1619af33f5.png?imageView2/2/w/2560/h/7000)
解释： 解析 SQL，生成抽象语法树（未绑定的逻辑执行计划）

3. 使用 Analyzer 绑定逻辑计划

在这个阶段 Analyzer 使用 Analysis Rules，结合 SessionCatalog 元数据，对未绑定的逻辑计划进行解析，生成已绑定的逻辑计划（Analyzed Logical Plan）。

具体流程是：

实例化一个 Simple Analyzer，然后遍历预定义好的 Batch，通过父类 Rule Executor 的执行方法运行 Batch 里的 Rules，每个 Rule 会对未绑定的逻辑计划进行处理。

有些可以通过一次解析处理，有些需要多次迭代，迭代直到达到 FixedPoint 次数或前后两次的树结构没有变化才停止操作。

！[在语法树中加入元数据信息，生成绑定的逻辑计划](https://ask.qcloudimg.com/http-save/yehe-9360138/1747ae6f527476b67abd25c75526d7df.png?imageView2/2/w/2560/h/7000)
解释：在语法树中加入元数据信息，生成绑定的逻辑计划

4. 使用 Optimizer 优化逻辑计划

Optimizer 的实现和处理方式跟 Analyzer 类似，在该类中定义一系列 Optimization Rules，利用这些 Rules 将绑定的逻辑计划进行迭代处理，完成合并、列裁剪和谓词下推等优化工作后生成优化的逻辑计划（Optimized Logical Plan）。

![Predicate Pushdown（谓词下推），Filter 下推到 Scan 的位置，将符合条件的数据筛选出来后再进行 join 操作，减少操作的数据量](https://ask.qcloudimg.com/http-save/yehe-9360138/27bccd60902bedbd2e554e92a45dae68.png?imageView2/2/w/2560/h/7000)
解释：Predicate Pushdown（谓词下推），Filter 下推到 Scan 的位置，将符合条件的数据筛选出来后再进行 join 操作，减少操作的数据量

![Column Pruning（列裁剪），只保留查询用到的列，其它列裁剪掉，减少处理的数据量, 提升速度](https://ask.qcloudimg.com/http-save/yehe-9360138/9bde28ca2909039222a2b37551b7444b.png?imageView2/2/w/2560/h/7000)
解释：Column Pruning（列裁剪），只保留查询用到的列，其它列裁剪掉，减少处理的数据量, 提升速度

5. 使用 SparkPlanner 生成可执行计划的物理计划

SparkPlanner 使用 Planning Strategies，对优化的逻辑计划进行转换，生成可以执行的物理计划（Physical Plan）。

根据过去的性能统计数据，选择最佳的物理执行计划 Cost Model，最后生成可以执行的物理执行计划树，得到 SparkPlan。

6. 使用 execute 执行物理计划

在最终真正执行物理执行计划之前，还要进行 Preparations 规则处理，最后调用 SparkPlan 的 `execute()`，执行物理计划计算 RDD。

## Spark SQL 数据抽象

Spark SQL最基本的数据抽象是RDD（Resilient Distributed Dataset）, AKA弹性分布式数据集，下面有详细的介绍

- DataFrame
  - DataFrame 基于RDD，同样是弹性分布式数据集，是一个由具名列组成的数据集
  - DataFrame 内部的有明确 Scheme 结构，即列名、列字段类型都是已知的，这带来的好处是可以减少数据读取以及更好地优化执行计划，从而保证查询效率
  - 和RDD的区别
            ataFrame 和 RDDs 最主要的区别在于一个面向的是结构化数据，一个面向的是非结构化数据，它们内部的数据结构如下：  
            ![DataFrame和RDD的内部结构](https://camo.githubusercontent.com/fd5e0bdcc317cc5f7326add814cc9e47a563c16f9d28e0f24a8f0a1b8104239d/68747470733a2f2f67697465652e636f6d2f68656962616979696e672f426967446174612d4e6f7465732f7261772f6d61737465722f70696374757265732f737061726b2d646174614672616d652b524444732e706e67)
- DataSet
  - 集成了 RDD 和 DataFrame 的优点，具备强类型的特点
  - 支持 Lambda 函数，但只能在 Scala 和 Java 语言中使用
- DataFrame和DataSet
        Spark 将 DataFrame 和 Dataset 的 API 融合到一起，提供了结构化的 API(Structured API)
        ![Spark 将 DataFrame 和 Dataset 的 API 融合到一起，提供了结构化的 API(Structured API)](https://camo.githubusercontent.com/9491f8ec3aeeac301f6d3019e1717670e795adff3a768075d7ed31173b5e298c/68747470733a2f2f67697465652e636f6d2f68656962616979696e672f426967446174612d4e6f7465732f7261772f6d61737465722f70696374757265732f737061726b2d756e696665642e706e67)  
        静态类型与运行时类型安全  
        ![类型安全图谱](https://camo.githubusercontent.com/5a4b91c4c07c24afa3a74c7942e813fe6309e5e67c6a5435fa95633d9195978c/68747470733a2f2f67697465652e636f6d2f68656962616979696e672f426967446174612d4e6f7465732f7261772f6d61737465722f70696374757265732f737061726b2de8bf90e8a18ce5ae89e585a82e706e67)  
        Untyped & Typed  
        DataFrame API 被标记为 `Untyped API`，而 DataSet API 被标记为 `Typed API`  
        DataFrame 的 Untyped 是相对于语言或 API 层面而言，它确实有明确的 Scheme 结构，即列名，列类型都是确定的，但这些信息完全由 Spark 来维护，Spark 只会在运行时检查这些类型和指定类型是否一致。这也就是为什么在 Spark 2.0 之后，官方推荐把 DataFrame 看做是 DatSet[Row]，Row 是 Spark 中定义的一个 trait，其子类中封装了列字段的信息。  
        相对而言，DataSet 是 Typed 的，即强类型。如下面代码，DataSet 的类型由 Case Class(Scala) 或者 Java Bean(Java) 来明确指定的，在这里即每一行数据代表一个 Person，这些信息由 JVM 来保证正确性，所以字段名错误和类型错误在编译的时候就会被 IDE 所发现。

        ```
        case class Person(name: String, age: Long)
        val dataSet: Dataset[Person] = spark.read.json("people.json").as[Person]
        ```

    三个数据抽象的区别  
  - RDDs 适合非结构化数据的处理，而 DataFrame & DataSet 更适合结构化数据和半结构化的处理
  - DataFrame & DataSet 可以通过统一的 Structured API 进行访问，而 RDDs 则更适合函数式编程的场景
  - 相比于 DataFrame 而言，DataSet 是强类型的 (Typed)，有着更为严格的静态类型检查
  - DataSets、DataFrames、SQL 的底层都依赖了 RDDs API，并对外提供结构化的访问接口
    ![区别](https://camo.githubusercontent.com/a6b74c74abae230af70feda1495e3ac1065328b7c34c2f9fb2918a17000fd28b/68747470733a2f2f67697465652e636f6d2f68656962616979696e672f426967446174612d4e6f7465732f7261772f6d61737465722f70696374757265732f737061726b2d7374727563747572652d6170692e706e67)
SparkSession  
    Spark 2.0 中引入了 SparkSession，其为用户提供了一个统一的切入点来学习和使用 Spark 的各项功能，并且允许用户通过它调用 DataFrame 和 DataSet 的相关 API 来编写 Spark 程序。  
    最重要的是，它减少了用户需要了解的一些概念，使得我们可以很容易地与 Spark 进行交互

----
术语补充

- Spark Schema

    在Spark SQL中，Schema描述了一个DataFrame或Dataset的结构信息，包括每个列的名称、数据类型和是否可为空等属性。

    .printSchema()方法可以用于显示DataFrame或Dataset的Schema，输出结果通常会包含以下几个部分：

  - root：表示整个Schema的根节点。

  - 列名和数据类型：使用|--符号来表示每个列，后跟列名和数据类型。例如，|-- name: string (nullable = true)表示一个名为name的字符串类型的列，其中(nullable = true)表示该列是否可为空。

  - 结构体类型：如果一个列是结构体类型，则会在其下方显示子列的信息。结构体类型的Schema以嵌套的形式展示，例如

    ```
     |-- address: struct (nullable = true)
     |    |-- street: string (nullable = true)
     |    |-- city: string (nullable = true)
     |    |-- state: string (nullable = true)
    ```

  - 数组类型：如果一个列是数组类型，则会在其下方显示元素类型的信息。数组类型的Schema以重复的形式展示，例如

    ```
    |-- array_col: array (nullable = true)
    |    |-- element: integer (containsNull = true)
    ```

    Map类型：如果一个列是Map类型，则会在其下方显示键和值的数据类型信息。例如：

    ```
     |-- emails: map (nullable = true)
     |    |-- key: string
     |    |-- value: string (valueContainsNull = true)
    ```

    举例1：

    ```shell
    root
     |-- id: integer (nullable = true)
     |-- name: string (nullable = true)
     |-- address: struct (nullable = true)
     |    |-- street: string (nullable = true)
     |    |-- city: string (nullable = true)
     |    |-- state: string (nullable = true)
     |-- array_col: array (nullable = true)
     |    |-- element: integer (containsNull = true)
     |-- emails: map (nullable = true)
     |    |-- key: string
     |    |-- value: string (valueContainsNull = true)
    ```

    举例2：

    ![printSchema的例子](https://img-blog.csdn.net/20170307093304528?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvUml2ZXJDb2Rl/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/Center)

- Spark RDD
  - 什么是RDD
        RDD（Resilient Distributed Dataset）叫做弹性分布式数据集  
        RDD是Spark中最基本的数据抽象  
        RDD 是 Spark 提供的最重要的抽象概念，它是一种有容错机制的特殊数据集合，可以分布在集群的结点上，以函数式操作集合的方式进行各种并行操作。  

        分区(Partition)是RDD的基本组成单位，RDD的计算是以分片为单位的
        分区函数(Partitioner)是对RDD进行分区的函数

        通俗点来讲，可以将 RDD 理解为一个分布式对象集合，本质上是一个只读的分区记录集合。每个 RDD 可以分成多个分区，每个分区就是一个数据集片段。一个 RDD 的不同分区可以保存到集群中的不同结点上，从而可以在集群中的不同结点上进行并行计算。

        ![RDD 的分区及分区与工作结点（Worker Node）的分布关系](https://pic1.zhimg.com/v2-d605d70641e2a2b24c4f27a528a32e3c_r.jpg)  
        RDD 的分区及分区与工作结点（Worker Node）的分布关系

  - RDD的属性
        RDD 具有容错机制，并且只读不能修改，可以执行确定的转换操作创建新的 RDD。具体来讲，RDD 具有以下几个属性。  
    - 只读：不能修改，只能通过转换操作生成新的 RDD。
    - 分布式：可以分布在多台机器上进行并行处理。
    - 弹性：计算过程中内存不够时它会和磁盘进行数据交换。
    - 基于内存：可以全部或部分缓存在内存中，在多次计算间重用。

        ![RDD的计算过程](https://images2018.cnblogs.com/blog/1228818/201804/1228818-20180421133911520-1150689001.png)
        RDD的计算过程  
        ![hello.txt的细节](https://images2018.cnblogs.com/blog/1228818/201804/1228818-20180421134031551-1670646166.png)  
        hello.txt的细节  

        在内部，每个RDD都由以下五个主要组成部分构成:
    - 一个分区列表
    - 一个计算每个分区的函数
    - 一个针对其他RDD的依赖列表
    - 可选项：一个用于键值对RDD的分区器（例如，指定RDD采用哈希分区）
    - 可选项：一个首选位置列表指定分区计算时优先考虑的物理位置（例如，HDFS 数据块）

        ```text
        Internally, each RDD is characterized by five main properties:
        
         · A list of partitions
         · A function for computing each split
         · A list of dependencies on other RDDs
         · Optionally, a Partitioner for key-value RDDs (e.g. to say that the RDD is hash-partitioned)
         · Optionally, a list of preferred locations to compute each split on (e.g. block locations for
         an HDFS file)
         ```

  - 算子
        在Spark中，算子（Operator）是指用于处理RDD（弹性分布式数据集）的函数，它可以将一个或多个RDD作为输入，执行特定的操作并生成新的RDD作为输出。算子可分为转换算子（Transformation）和动作算子（Action）。

    - 转换算子
      - RDD 的转换操作是返回新的 RDD 的操作。转换出来的 RDD 是惰性求值的，只有在行动操作中用到这些 RDD 时才会被计算
      -
    - 动作算子
      - 用于执行计算并按指定的方式输出结果
  - RDD 血缘关系（Lineage）
        描述了一个 RDD 是如何从父 RDD 计算得来的。如果某个 RDD 丢失了，则可以根据血缘关系，从父 RDD 计算得来。  
        ![RDD血缘关系](https://pic4.zhimg.com/v2-d04957b034c7321965af49c6c3f8f707_b.webp)
        给出了一个 RDD 执行过程的实例。系统从输入中逻辑上生成了 A 和 C 两个 RDD， 经过一系列转换操作，逻辑上生成了 F 这个 RDD。  
        Spark 记录了 RDD 之间的生成和依赖关系。当 F 进行行动操作时，Spark 才会根据 RDD 的依赖关系生成 DAG，并从起点开始真正的计算。  
        上述一系列处理称为一个血缘关系（Lineage），即 DAG 拓扑排序的结果。在血缘关系中，下一代的 RDD 依赖于上一代的 RDD。例如，在图中，B 依赖于 A，D 依赖于 C，而 E 依赖于 B 和 D。
  - RDD依赖类型  
         ![RDD窄依赖和宽依赖](https://pic1.zhimg.com/v2-55f33c6f16c3c50d9067ba8615ccb75c_b.webp)
        1. 窄依赖
            1. 子 RDD 的每个分区依赖于常数个父分区（即与数据规模无关）。
            2. 输入输出一对一的算子，且结果 RDD 的分区结构不变，如 map、flatMap。
            3. 输入输出一对一的算子，但结果 RDD 的分区结构发生了变化，如 union。
            4. 从输入中选择部分元素的算子，如 filter、distinct、subtract、sample。
        2. 宽依赖
            1. 子 RDD 的每个分区依赖于所有父 RDD 分区。
            2. 对单个 RDD 基于 Key 进行重组和 reduce，如 groupByKey、reduceByKey。
            3. 对两个 RDD 基于 Key 进行 join 和重组，如 join。

        Spark 的这种依赖关系设计，使其具有了天生的容错性

        相对而言，窄依赖的失败恢复更为高效，它只需要根据父 RDD 分区重新计算丢失的分区即可，而不需要重新计算父 RDD 的所有分区。而对于宽依赖来讲，单个结点失效，即使只是 RDD 的一个分区失效，也需要重新计算父 RDD 的所有分区，开销较大。  
        宽依赖操作就像是将父 RDD 中所有分区的记录进行了“洗牌”，数据被打散，然后在子 RDD 中进行重组。
  - 阶段划分
        用户提交的计算任务是一个由 RDD 构成的 DAG，如果 RDD 的转换是宽依赖，那么这个宽依赖转换就将这个 DAG 分为了不同的阶段（Stage）。由于宽依赖会带来“洗牌”，所以不同的 Stage 是不能并行计算的，后面 Stage 的 RDD 的计算需要等待前面 Stage 的 RDD 的所有分区全部计算完毕以后才能进行。  
        ![DAG阶级划分](https://pic1.zhimg.com/v2-f1be22c05986e2d17370333bdf9c173c_b.jpg)  
  - RDD缓存
        Spark RDD 是惰性求值的，而有时候希望能多次使用同一个 RDD。如果简单地对 RDD 调用行动操作，Spark 每次都会重算 RDD 及它的依赖，这样就会带来太大的消耗。为了避免多次计算同一个 RDD，可以让 Spark 对数据进行持久化。  

        Spark 可以使用 persist 和 cache 方法将任意 RDD 缓存到内存、磁盘文件系统中。缓存是容错的，如果一个 RDD 分片丢失，则可以通过构建它的转换来自动重构。被缓存的 RDD 被使用时，存取速度会被大大加速。一般情况下，Executor 内存的 60% 会分配给 cache，剩下的 40％ 用来执行任务。

        通过以下步骤来选择合适的持久化级别

          1. 如果 RDD 可以很好地与默认的存储级别（MEMORY_ONLY）契合，就不需要做任何修改了。这已经是 CPU 使用效率最高的选项，它使得 RDD 的操作尽可能快
          2. 如果 RDD 不能与默认的存储级别很好契合，则尝试使用 MEMORY_ONLY_SER，并且选择一个快速序列化的库使得对象在有比较高的空间使用率的情况下，依然可以较快被访问
          3. 尽可能不要将数据存储到硬盘上，除非计算数据集函数的计算量特别大，或者它们过滤了大量的数据。否则，重新计算一个分区的速度与从硬盘中读取的速度基本差不多
          4. 如果想有快速故障恢复能力，则使用复制存储级别。所有的存储级别都有通过重新计算丢失数据恢复错误的容错机制，但是复制存储级别可以让任务在 RDD 上持续运行，而不需要等待丢失的分区被重新计算
          5. 在不使用 cached RDD 的时候，及时使用 unpersist 方法来释放它

        RDD的运行过程  
        RDD在Spark架构中的运行过程  
        1. 创建RDD对象
        2. SparkContext负责计算RDD之间的依赖关系，构建DAG
        3. DAGScheduler负责把DAG图分解成多个Stage，每个Stage中包含了多个Task，每个Task会被TaskScheduler分发给各个WorkerNode上的Executor去执行
        ![RDD运行过程](https://s2.51cto.com/images/blog/202103/07/339017b621b1acbd8a9b8cf243ef47e4.png?x-oss-process=image/watermark,size_16,text_QDUxQ1RP5Y2a5a6i,color_FFFFFF,t_30,g_se,x_10,y_10,shadow_20,type_ZmFuZ3poZW5naGVpdGk=/format,webp)

- 拓扑排序  
    拓扑排序（Topological Sorting）是一个有向无环图（DAG, Directed Acyclic Graph）的所有顶点的线性序列  
    该序列必须满足下面两个条件：  
    1. 每个顶点出现且只出现一次。  
    2. 若存在一条从顶点 A 到顶点 B 的路径，那么在序列中顶点 A 出现在顶点 B 的前面。
    如何写出DAG的拓扑排序结果，常用的方法：
       1. 从 DAG 图中选择一个 没有前驱（即入度为0）的顶点并输出。
       2. 从图中删除该顶点和所有以它为起点的有向边。
       3. 重复 1 和 2 直到当前的 DAG 图为空或当前图中不存在无前驱的顶点为止。后一种情况说明有向图中必然存在环。
        ![DAG图](https://upload-images.jianshu.io/upload_images/8468731-9638ca653dd3aca9.png?imageMogr2/auto-orient/strip|imageView2/2/format/webp)  
        DAG图  
        ![过程](https://upload-images.jianshu.io/upload_images/8468731-da38fa971e5d52b5.png?imageMogr2/auto-orient/strip|imageView2/2/format/webp)  
    拓扑排序后的结果是 { 1, 2, 4, 3, 5 }。

----
资料来源：  
[初识 Spark SQL | 20张图详解 Spark SQL 运行原理及数据抽象](https://cloud.tencent.com/developer/article/1965056)  
[SQL的书写顺序和执行顺序](https://zhuanlan.zhihu.com/p/77847158)
[传统数据库的运行过程](https://blog.csdn.net/u012050154/article/details/50825376)  
[Spark中DataFrame的schema讲解](https://blog.csdn.net/RiverCode/article/details/60604327)  
[Spark学习之路 （三）Spark之RDD](https://www.cnblogs.com/qingyunzong/p/8899715.html)
[Spark RDD是什么？](https://zhuanlan.zhihu.com/p/139261855)  
[什么是拓扑排序（Topological Sorting）](https://www.jianshu.com/p/b59db381561a)  
[spark RDD概念及组成详解](https://blog.csdn.net/u010711495/article/details/109742384)  
[大数据Spark RDD运行设计底层原理](https://blog.51cto.com/u_15060533/2650648)  
[DataFrame和Dataset简介](https://github.com/heibaiying/BigData-Notes/blob/master/notes/SparkSQL_Dataset%E5%92%8CDataFrame%E7%AE%80%E4%BB%8B.md#%E4%B8%89DataFrame--DataSet---RDDs-%E6%80%BB%E7%BB%93)
