## Mysql的主从复制

###1.MySQL主从复制的介绍（提高数据库的容灾能力）

- **基本介绍**
  1. MySQL内建的复制功能是构建大型，高性能应用程序的基础。
  2. 将MySQL的数据分布到到多个系统上去，这种分步的机制，是通过将MySQL的某一台主机的数据复制到其它主机(Slave)上，并重新执行一遍来实现的。
  3. 复制过程中一个服务器充当服务器，而一个或多个其它服务器充当从服务器。
  4. 主服务器将更新写入二进制日志，并维护文件的一个索引以跟踪日志循环。这些日志可以记录发送到从服务器的更新。
  5. 当一个从服务器连接主服务器时，它通知主服务器从服务器在日志中读取的最后一次成功更新的位置，从服务器接收从那时起发生的任何更新，然后封锁等等主服务器通知新的更新。
  6. 请注意当你进行复制时，所有对复制中的表的更新必须在主服务器上进行。不然会发生，用户对主服务器上的表进行的更新，与对从服务器上的表所进行的更新之间的冲突。

- **MySQL支持的复制**
  1. 基于语句的复制。
     在主服务器上执行的SQL语句，在从服务器上执行同样的语句，配置为 `binlog_format='STATEMENT'`
  2. 基于行的复制。
     把改变的内容复制过去，而不是把命令在从服务器上执行一遍，从MySQL5.0开始支持，配置为`binlog_format='ROW'`
  3. 混合类型的复制。
     默认采用基于语句的复制，一旦发现基于语句的无法精确的复制时，就会采用基于行的复制,配置为`binlog_format='MIXED'`

- **MySQL复制解决的问题**
  1. 数据分布
  2. 负载平衡
  3. 备份
  4. 高可用性和容错行



### 2.主从复制的工作原理

- **一主一从**

  ![](https://javanote.oss-cn-shenzhen.aliyuncs.com/15_一主一从.png)

  1. 主服务器上面的任何修改都会保存在二进制日志Binary log里面。
  2. 从服务器上面启动一个I/O线程(实际上就是一个主服务器的客户端进程)，连接到主服务器上面请求读取二进制日志，然后把读取到的二进制日志写到本地的一个Realy log里面。
  3. 从服务器上面开启一个SQL线程定时检查Realy log，如果发现有更改立即把更改的内容在本机上面执行一遍。

- **一主多从**

  ![](https://javanote.oss-cn-shenzhen.aliyuncs.com/16_一主多从.png)

  1. 如果一主多从的话，这时主库既要负责写又要负责为几个从库提供二进制日志。
  2. 可以将二进制日志只给某一从，这一从再开启二进制日志并将自己的二进制日志再发给其它从。
  3. 或者是这个从不记录只负责将二进制日志转发给其它从，这样性能比较好，而且数据之间的延时应该也稍微要好一些。

### 3.主从复制的详细过程

- 一般分为**同步复制和异步复制，实际复制架构中大部分为异步复制。** 
  1. Slave上面的IO进程连接上Master，并请求从指定日志文件的指定位置(或者从最开始的日志)之后的日志内容。
  2. Master接收到来自Slave的IO进程的请求后，负责复制的IO进程会根据请求信息读取日志指定位置之后的日志信息，返回给Slave的IO进程。
  3. 返回信息中除了日志所包含的信息之外，还包括本次返回的信息已经到Master端的bin-log文件的名称以及bin-log的位置。
  4. Slave的IO进程接收到信息后，将接收到的日志内容依次添加到Slave端的relay-log文件的最末端。
  5. 并将读取到的Master端的 bin-log的文件名和位置记录到master-info文件中，以便在下一次读取的时候能够清楚的告诉Master"我需要从某个bin-log的哪个位置开始往后的日志内容，请发给我"。
  6. Slave的Sql进程检测到relay-log中新增加了内容后，会马上解析relay-log的内容成为在Master端真实执行时候的那些可执行的内容，并在自身执行。



### 4.主从复制的具体配置

#### 4.1基本步骤

- **异步性**
  1. 事物首先在主节点上提交，然后复制给从节点并在从节点上应用，这样意味着在同一个时间点主从上的数据可能不一致。
  2. 异步复制的好处在于它比同步复制要快，如果对数据的一致性要求很高，还是采用同步复制较好。
- **基本步骤**
  1. 建立一个主节点，开启binlog，设置服务器id。
  2. 建立一个从节点，设置服务器id。
  3. 将从节点连接到主节点上。

####4.2主服务器
- **开启binlog日志**
  
  1. Master上面开启binlog日志，并且设置一个唯一的服务器id，在局域网内这个id必须唯一。
  2. 二进制的binlog日志记录master上的所有数据库改变，这个日志会被复制到从节点上，并且在从节点上回放。
  3. 修改my.cnf文件，在mysqld模块下修改如下内容：
     ```properties
     [mysqld]
     server-id = 1
     log_bin = /var/log/mysql/mysql-bin.log
     # 基于语句的复制,默认
     binlog_format = 'STATEMENT'
     # 基于行的复制
     binlog_format='ROW'
     # 混合复制
     binlog_format='MIXED'
     ```
  4. log_bin设置二进制日志所产生文件的基本名称，二进制日志由一系列文件组成。log_bin的值是可选项，如果没有为log_bin设置值，则默认值是：主机名-bin。如果随便修改主机名，则binlog日志的名称也会被改变的。
  5. server-id是用来唯一标识一个服务器的，每个服务器的server-id都不一样。
6. 这样slave连接到master后，会请求master将所有的binlog传递给它，然后将这些binlog在slave上回放。为了防止权限混乱，一般都是建立一个单独用于复制的账户。
  
- **基于语句的复制**
  
  1. binlog是复制过程的关键，记录了数据库的所有改变。通常即将执行完毕的语句会在binlog日志的末尾写入一条记录。
  2. **binlog只记录改变数据库的语句，对于不改变数据库的语句则不进行记录**。叫做基于语句的复制，还有一种情况是基于行的复制，两种模式各有各的优缺点。

####4.3从服务器

- 配置文件
  ```properties
  [mysqld]
  server-id = 2
  ```

- 执行命令连接主服务器
  ```shell
  change master to master_host='10.1.6.159',master_port=3306,master_user='rep',
  master_password='123456';
  start slave;
  ```

- 从服务器的日志文件
  1. 从服务器在修改完my.cnf配置重启数据库后，就开始记录binlog了。
  2. 从服务器在/var/log/mysql目录下看到一个mysql-bin.000001文件，而且还有一个mysql-bin.index文件。
  3. 这个文件保存了所有的binlog文件列表，可以通过log_bin_index进行设置，如果没有设置改值，则默认值和log_bin一样。
  4. 主服务器执行`show binlog events`命令，可以看到第一个binlog文件的内容。
     ```sql
     mysql> show binlog events\G
     *************************** 1. row ***************************
        -- Log_name是二进制日志文件的名称，一个事件不能横跨两个文件
        Log_name: mysql-bin.000001
        -- Pos 这是该事件在文件中的开始位置
             Pos: 4
      -- Event_type 事件的类型，事件类型是给slave传递信息的基本方法，
      -- 每个新的binlog都已Format_desc类型开始，以Rotate类型结束
      Event_type: Format_desc
       -- Server_id 创建该事件的服务器id
       Server_id: 1
     -- End_log_pos 该事件的结束位置，也是下一个事件的开始位置，因此事件范围为Pos~End_log_pos-1
     End_log_pos: 107
            -- Info 事件信息的可读文本，不同的事件有不同的信息
            Info: Server ver: 5.5.28-0ubuntu0.12.10.2-log, Binlog ver: 4
     *************************** 2. row ***************************
        Log_name: mysql-bin.000001
             Pos: 107
      Event_type: Query
       Server_id: 1
     End_log_pos: 181
            Info: create user rep
     *************************** 3. row ***************************
        Log_name: mysql-bin.000001
             Pos: 181
      Event_type: Query
       Server_id: 1
     End_log_pos: 316
            Info: grant replication slave on *.* to rep identified by '123456'
     3 rows in set (0.00 sec)
   ```
  
- 具体实例
  ```sql
  -- 在主服务器上执行,从服务器会自动同步该操作
  create table rep(name var);
  insert into rep values ("guol");

  -- flush logs命令强制轮转日志，生成一个新的二进制日志
  -- 通过show binlog events in 'xxx'来查看该二进制日志
  -- 通过show master status查看当前正在写入的binlog文件
  flush logs;
  ```