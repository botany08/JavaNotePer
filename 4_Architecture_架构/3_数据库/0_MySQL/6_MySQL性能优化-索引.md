## Mysql中的索引

### 1.索引基础介绍

- **索引的定义**
  
  1. 索引是一种特殊的文件(InnoDB数据表上的索引是表空间的一个组成部分)，包含着对数据表里所有记录的引用指针。更通俗的说，数据库索引好比是一本书前面的目录，能加快数据库的查询速度。
  2. **索引仅仅提供创建、删除和修改的功能**，索引不是关系数据库理论的产物，是为了性能和维护工作而出现的东西，主要用来**加快数据操作速度和提高数据的存储利用**。
  
- **索引的分析explain**

  1. explain显示了mysql如何使用索引来处理select语句以及连接表。帮助选择更好的索引和写出更优化的查询语句。
  2. 详细输出格式：https://segmentfault.com/a/1190000008131735

  ```sql
  -- 实际SQL，查找用户名为Jefabc的员工
  select * from emp where name = 'Jefabc';
  -- 查看SQL是否使用索引，前面加上explain即可
  explain select * from emp where name = 'Jefabc';
  ```

  **expain出来的信息有10列** 

  1. **id**:选择标识符
  2. **select_type**:表示查询的类型。
  3. **table**:输出结果集的表
  4. **partitions**:匹配的分区(额外字段)
  5. **type**:表示表的连接类型，const，system是主键索引。ref，表示最左前缀索引。range，表示索引范围查询。
  6. **possible_keys**:表示查询时，可能使用的索引
  7. **key**:表示实际使用的索引
  8. **key_len**:索引字段的长度
  9. **ref**:列与索引的比较
  10. **rows**:扫描出的行数(估算的行数)
  11. **filtered**:按表条件过滤的行百分比(额外字段)
  12. **Extra**:执行情况的描述和说明

- **索引的指定使用**

  ```sql
  -- USE INDEX,表示希望MySQl去参考的索引列,不一定会用
  SELECT * FROM mytable USE INDEX (mod_time, name) ...
  -- IGNORE INDEX,表示忽略一个或者多个索引
  SELECT * FROM mytale IGNORE INDEX (priority) ...
  -- FORCE INDEX,表示强制使用的索引列,一定会用
  SELECT * FROM mytable FORCE INDEX (mod_time) ...
  ```



### 2.索引的数据类型

- **索引支持的数据类型**
  1. 选择合适的数据类型存储数据对性能有很大的影响。
  2. 越小的数据类型通常更好
     越小的数据类型通常在磁盘、内存和CPU缓存中都需要更少的空间，处理起来更快。
  3. 简单的数据类型更好
     整型数据比起字符，处理开销更小，因为字符串的比较更复杂。在MySQL中，应该用内置的日期和时间数据类型，而不是用字符串来存储时间；以及用整型数据类型存储IP地址。
  4. 尽量避免NULL
     应该指定列为NOT NULL，除非你想存储NULL。在MySQL中，含有空值的列很难进行查询优化，因为它们使得索引、索引的统计信息以及比较运算更加复杂。你应该用0、一个特殊的值或者一个空串代替空值。

- **选择合适的标识符**
  1. 整型
     通常是作为标识符的最好选择，因为可以更快的处理，而且可以设置为AUTO_INCREMENT。
  2. 字符串
     - 尽量避免使用字符串作为标识符，它们消耗更好的空间，处理起来也较慢。
     - 通常来说，字符串都是随机的，所以在索引中的位置也是随机的，这会导致页面分裂、随机访问磁盘，聚簇索引分裂(对于使用聚簇索引的存储引擎)。

### 3.索引的类型(逻辑层面)

- 存储引擎和索引类型

  | 存储引擎 | 索引类型(逻辑)               |
  | -------- | ---------------------------- |
  | InnoDB   | 普通索引、唯一索引           |
  | MyISAM   | 普通索引、唯一索引、全文索引 |
  | MEMORY   | 普通索引、唯一索引           |

#### 3.1普通索引(INDEX)

- 定义
  
  1. 最基本的索引，没有任何限制，MyIASM中默认的BTREE类型的索引，也是大多数情况下用到的索引。
- 实例代码：
  ```sql
  -- 1.直接创建索引
  CREATE INDEX index_name ON table(column(length))
  
  -- 2.修改表结构的方式添加索引
  ALTER TABLE table_name ADD INDEX index_name ON (column(length))
  
  -- 3.创建表的时候同时创建索引
  CREATE TABLE 'table' (
  'id' int(11) NOT NULL AUTO_INCREMENT,
  'title'  char(255) CHARACTER SET utf8  COLLATE utf8_general_ci NOT NULL,
  'content' text CHARACTER SET utf8  COLLATE utf8_general_ci NOT NULL,
  'time' int(10) NULL DEFAULT NULL,
  PRIMARY KEY ('id'),
  INDEX index_name (title(length)))
  
  -- 4.删除索引
  DROP INDEX index_name ON table;
  ```

#### 3.2唯一索引(UNIQUE)

- 定义
  
1. 与普通索引类似，不同的就是索引列的值必须唯一，但允许有空值(注意和主键不同)。如果是组合索引，则列值的组合必须唯一，创建方法和普通索引类似。

- 实例代码
  ```sql
  -- 1.创建唯一索引
  CREATE UNIQUE INDEX indexName ON table(column(length));
  
  -- 2.修改表结构
  ALTER TABLE table_name ADD UNIQUE indexName ON (column(length));
  
  -- 3.创建表的时候直接指定
  CREATE TABLE `table` (
  'id' int(11) NOT NULL AUTO_INCREMENT,
  'title' char(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  'content' text CHARACTER SET utf8 COLLATE utf8_general_ci NULL,
  'time' int(10) NULL DEFAULT NULL,
  PRIMARY KEY ('id'),
  UNIQUE indexName (title(length))
  );
  ```

#### 3.3全文索引(FULLTEXT)

- 定义
  1. FULLTEXT索引仅可用于MyISAM表。可以从CHAR、VARCHAR或TEXT列中作为CREATE TABLE语句的一部分被创建，或是随后使用ALTER TABLE 或CREATE INDEX被添加。
  2. 对于较大的数据集，输入一个没有FULLTEXT索引的表中，然后创建索引，其速度比把资料输入现有FULLTEXT索引的速度更为快。
  3. 对于大容量的数据表，生成全文索引是一个非常消耗时间非常消耗硬盘空间的做法。
4. 全文索引的实现是，二元分词法和倒排索引(数据结构为B+树)

- 实例代码
  ```sql
  -- 1.创建表的适合添加全文索引
  CREATE TABLE 'table' (
  'id' int(11) NOT NULL	 AUTO_INCREMENT,
  'title' char(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  'content' text CHARACTER SET utf8	COLLATE utf8_general_ci NULL,
  'time' int(10) NULL DEFAULT NULL,
  PRIMARY KEY ('id'),
  FULLTEXT (content)
  );
  
  -- 2.修改表结构添加全文索引
  ALTER TABLEarticle ADD FULLTEXT index_content(content);

  -- 3.直接创建索引
  CREATE FULLTEXT INDEX index_content ON article(content);
  ```

#### 3.4单列索引和多列索引

- 多个单列索引与单个多列索引的查询效果不同。
  1. 因为执行查询时，MySQL只能使用一个索引，会从多个索引中选择一个限制最为严格的索引。

#### 3.5组合索引(最左前缀)

- 定义
  
1. 平时用的SQL查询语句一般都有比较多的限制条件，为了进一步榨取MySQL的效率，就要考虑建立组合索引。

- 具体实例
  1. 例如上表中针对title和time建立一个组合索引
     `ALTER TABLE article ADD INDEX index_titme_time (title(50),time(10))`
  2. 建立这样的组合索引，其实是相当于分别建立了下面两组组合索引
     - title,time
     - title

- 最左前缀原则
  1. 索引只从最左面的开始组合，所以没有单独time的索引，并不是只要包含这两列的查询都会用到该组合索引
  2. 如下面的几个SQL所示
     ```sql
     -- 使用到上面的索引
     SELECT * FROM article WHERE title='测试' AND time=1234567890;
     SELECT * FROM article WHERE title='测试';
     -- 不使用上面的索引
     SELECT * FROM article WHREE time=1234567890;
     ```



### 4.MySQL索引的优化(使用上)

#### 4.1索引的问题及分类

- **缺点**
  
  1. 虽然索引大大提高了查询速度，同时却会降低更新表的速度。
  2. 如对表进行INSERT、UPDATE和DELETE。因为更新表时，MySQL不仅要保存数据，还要保存一下索引文件。建立索引会占用磁盘空间的索引文件。
  
- **索引失效的情况**、

  1. 如果条件中有or，即使其中有条件带索引也不会使用(这也是为什么尽量少用or的原因)。
     注意：要想使用or，又想让索引生效，只能将or条件中的每个列都加上索引。
  2. 对于多列索引，不是使用的第一部分，则不会使用索引（**最左前缀原则**）。
  3. like查询是以%开头。
  4. 如果列类型是字符串，那一定要在条件中将数据使用引号引用起来,否则不使用索引。
  5. 如果mysql估计使用全表扫描要比使用索引快,则不使用索引。

- **分类**
  1. 索引分为聚集索引和非聚集索引两种。
  2. 聚集索引，将数据存储与索引放到了一块，索引结构的叶子节点保存了行数据。
  3. 非聚集索引，将数据与索引分开存储，索引结构的叶子节点指向了数据对应的位置。
  4. 聚集索引能提高多行检索的速度，而非聚集索引对于单行的检索很快。
  
- **聚集索引和非聚集索引的应用场景**

  ![](https://javanote.oss-cn-shenzhen.aliyuncs.com/8_聚簇索引和非聚簇索引.png)

#### 4.2索引的一些注意事项

- **索引不会包含有NULL值的列**
  1. 只要列中包含有NULL值都将不会被包含在索引中，复合索引中只要有一列含有NULL值，那么这一列对于此复合索引就是无效的。
  2. 所以在数据库设计时不要让字段的默认值为NULL。

- **使用短索引**
  1. 对串列进行索引，如果可能应该指定一个前缀长度。
     例如，如果有一个CHAR(255)的列，如果在前10个或20个字符内，多数值是惟一的，那么就不要对整个列进行索引。
  2. 短索引不仅可以提高查询速度而且可以节省磁盘空间和I/O操作。
     `ALERT table_name ADD INDEX index_name ON  id(id(4));`

- **索引列排序**
  1. MySQL查询只使用一个索引，如果where子句中已经使用了索引的话，那么order by中的列是不会使用索引的。
  2. 数据库默认排序可以符合要求的情况下不要使用排序操作。
  3. 尽量不要包含多个列的排序，如果需要最好给这些列创建复合索引。

- **like语句操作（不会使用索引）**
  1. 一般情况下不鼓励使用like操作，如果非使用不可，如何使用也是一个问题。
  2. like “%aaa%” 不会使用索引，而like “aaa%”可以使用索引。

- **不要在列上进行运算**
  1. 在列上进行运算会导致数据库不使用索引，而进行全盘扫描。

- **单下操作符**
  1. MySQL只对单下操作符才使用索引：`<,<=,=,>,>=,between,in,`以及某些时候的like(不以通配符%或_开头的情形)
  2. 理论上每张表里面最多可创建16个索引.



### 5.底层索引的实现

#### 5.1引擎和索引实现

- 索引是在存储引擎中实现的，而不是在服务器层中实现的。每种存储引擎的索引都不一定完全相同，并不是所有的存储引擎都支持所有的索引类型。

  | 存储引擎 | 索引实现                 |
  | -------- | ------------------------ |
  | InnoDB   | B+树                     |
  | MyISAM   | B+树、空间索引、全文索引 |
  | MEMORY   | Hash索引、B+树           |

  

#### 5.2B树原理

- **出现背景**

  1.  当有100个数值需要储存时，在二叉树及平衡二叉树中，会产生100个树节点。如果有办法让一个节点存储多个元素(数值)，那么，是不是可以减少树节点，从而减少树整体层级高度呢，要知道，减少树层级高度的过程，就是优化查询效率的过程。 
  2. B树与平衡二叉树相似，不同的是B树属于多叉树，又名**平衡多路查找树**。每个节点上可以存储多个数值元素。 
  3. B树所有的叶子节点都处于同一层级，不存在层级高度差的问题。

- **B树原理图**

  ![](https://javanote.oss-cn-shenzhen.aliyuncs.com/10_B树原理图.png)

  B树需满足以下几个规则
  1. 非叶子节点的子节点数大于1，且小于或等于M，并且M大于1。(M阶代表一个树节点最多有多少个查找路径，M=M路,当M=2则是2叉树,M=3则是3叉)
  2. 枝节点(中间节点)的关键字数大于或等于(M-1)/2且小于等于M-1。取整数，除不尽的时候向上取整。如6路查找树，枝节点关键字数大于或等于3且小于或等于5。
  3. **所有子节点都是处于同一层级**。
  4. 非叶子节点里，记录着关键字及关键字的磁盘记录指针，并且还有指向其子节点指针。
  5. 叶子节点里，记录着关键字及关键字的磁盘记录指针，子节点指针为null。

#### 5.3B+树原理

- **索引为什么不存在内存**

  1. 利用二叉树结构存储(N0=N2+1，N=N0+N1+N2)
     - 假设单个索引节点12B，1000w个数据行，unique索引，则叶子节点共占约100MB，整棵树最多200MB。
     - 假设一行数据占用200B，则数据共占约2G。
     - **索引:数据的占用比约为1/10**
  2.  **在基于索引的存储架构中，`索引:数据的占用比`过高，因此，索引无法全部装入内存**。 

- **索引优化的核心问题**

  1. 索引存储在磁盘，提高索引速度的核心是，**如何减少磁盘读写次数**。

     **因为每次通过索引查找，要先将索引内容读取到内存进行运算，获取下一条索引位置。重复此步骤，直到找到目标值。**

  2. 各个数据结构的读写次数，树的读写次数相当于树高。

     - 线性结构：读/写平均O(n)次
     - 二叉搜索树(BST)：读/写平均O(log2(n))次；如果树不平衡，则最差读/写O(n)次
     - 自平衡二叉搜索树(AVL)：在BST的基础上加入了自平衡算法，读/写最大O(log2(n))次
     - 红黑树(RBT)：另一种自平衡的查找树，读/写最大O(log2(n))次

     

- **B树解决的问题**

  1. 不要纠结于时间复杂度，与单纯的算法不同，磁盘IO次数才是更大的影响因素。读者可以推导看看，B树与AVL的时间复杂度是相同的，但由于B树的层数少，磁盘IO次数少，实践中B树的性能要优于AVL等二叉树。 
  2. B树通过维持节点大小，和磁盘块大小相同，减少了磁盘的IO次数。

- **B树遗留的问题**

  1. **未定位数据行**

     B树节点存储的是主键内容，并没有表中一行的数据。

  2. **无法处理范围查询**

     B树只能定位到某一行的主键，并不能获取到范围数据。

- **B+树的原理**

  ![](https://javanote.oss-cn-shenzhen.aliyuncs.com/11_B%2B%E6%A0%91%E5%8E%9F%E7%90%86.png)

  1. 修改key与子树的组织逻辑，将索引访问都落到叶子节点，行数据直接存储在叶子节点。
  2. 按顺序通过链表，将叶子节点串起来(方便范围查询)

#### 5.4B*树

- **B+和B*之间的区别**
  1. 首先是关键字个数限制问题，B+树初始化的关键字初始化个数是`cei(m/2)`，b*树的初始化个数为(cei(2/3*m))。
  2. B+树节点满时就会分裂，而B*树节点满时会检查兄弟节点是否满(因为每个节点都有指向兄弟的指针)。如果兄弟节点未满，则向兄弟节点转移关键字。如果兄弟节点已满，则从当前节点和兄弟节点各拿出1/3的数据创建一个新的节点出来。

- **特点**
  1. B*树在B+树的基础上，因其初始化的容量变大，使得节点空间使用率更高。
  2. B*树又存有兄弟节点的指针，可以向兄弟节点转移关键字的特性使得B*树额分解次数变得更少。

#### 5.5B+Tree索引

- **Innodb引擎使用的是B+树索引**

- **创建一张表**
  
  ```sql
  CREATE TABLE People (
     last_name varchar(50) not null,
     first_name varchar(50) not null,
     dob date not null,
     gender enum('m', 'f') not null,
     key(last_name, first_name, dob)
  );
  
  ```

- **B树索引结构**
  ![](https://javanote.oss-cn-shenzhen.aliyuncs.com/9_B树索引结构.png)

- **索引的使用**
  
  1. 索引存储的值按索引列中的顺序排列。
  2. 可以利用B-Tree索引进行全关键字、关键字范围和关键字前缀查询。
  3. 如果想使用索引，必须保证按索引的最左边前缀(leftmost prefix of the index)来进行查询。-- 叶子节点通过链表连接
  
- **索引的匹配规则**
  
  1. **匹配全值(Match the full value)**：对索引中的所有列都指定具体的值。例如，上图中索引可以帮助查找出生于1960-01-01的Cuba Allen。
  2. **匹配最左前缀(Match a leftmost prefix)**：可以利用索引查找last name为Allen的人，仅仅使用索引中的第1列。
  3. **匹配列前缀(Match a column prefix)**：例如，可以利用索引查找last name以J开始的人，这仅仅使用索引中的第1列。
  4. **匹配值的范围查询(Match a range of values)**：可以利用索引查找last name在Allen和Barrymore之间的人，仅仅使用索引中第1列。
  5. **匹配部分精确而其它部分进行范围匹配(Match one part exactly and match a range on another part)**：可以利用索引查找last name为Allen，而first name以字母K开始的人。
  6. **仅对索引进行查询(Index-only queries)**：如果查询的列都位于索引中，则不需要读取元组的值。
  7. 由于B-树中的节点都是顺序存储的，所以可以利用索引进行查找(找某些值)，也可以对查询结果进行ORDER BY。
  
- **B-tree索引的限制**
  
  1. **查询必须从索引的最左边的列开始**。例如不能利用索引查找在某一天出生的人。
  2. **不能跳过某一索引列**。例如，不能利用索引查找last name为Smith且出生于某一天的人。
  3. **存储引擎不能使用索引中范围条件右边的列**。例如，如果的查询语句为WHERE last_name="Smith" AND first_name LIKE 'J%' AND dob='1976-12-23'，则该查询只会使用索引中的前两列，因为LIKE是范围查询。

#### 5.6Hash索引

- **索引应用**
  
  1. MySQL中，只有Memory存储引擎显示支持hash索引，是Memory表的默认索引类型。Memory表也可以使用B-Tree索引。
  2. Memory存储引擎支持非唯一hash索引，这在数据库领域是罕见的，如果多个值有相同的hash code，索引把它们的行指针用链表保存到同一个hash表项中。
  
- **创建一张表**
  
  ```sql
  CREATE TABLE testhash (
     fname VARCHAR(50)	NOT NULL,
     lname VARCHAR(50) NOT NULL,
     KEY USING HASH(fname)
  ) ENGINE=MEMORY;
  ```


- **Hash索引的生成**
  
  1. 通过Hash函数，计算列fname的Hashcode。
  2. 记录Hashcode和数据行的对应关系，生成索引表。
  3. 同一个HashCode中，会存储多个值，这就是hash碰撞。
  
- **Hash的执行顺序**
  `mysql> SELECT lname FROM testhash WHERE fname='Peter';`
  1. MySQL会计算'Peter'的hash值，然后通过它来查询索引的行指针。因为f('Peter') = 8784，MySQL会在索引中查找8784，得到指向记录3的指针。
  2. 先计算hash值，查询索引的行指针，查询到结果。
  3. 因为索引自己仅仅存储很短的值，所以，索引非常紧凑。Hash值不取决于列的数据类型，一个TINYINT列的索引与一个长字符串列的索引一样大。
  
- **Hash索引的一些限制**
  
  1. 由于索引仅包含hash code和记录指针，所以MySQL不能通过使用索引避免读取记录。但是访问内存中的记录是非常迅速的，不会对性能造成太大的影响。
  2. 不能使用Hash索引排序。由于 Hash 索引中存放的是经过 Hash 计算之后的 Hash值，而且Hash值的大小关系并不一定和 Hash运算前的键值完全一样，所以数据库无法利用索引的数据来避免任何排序运算；
  3. Hash索引不支持键的部分匹配，因为是通过整个索引值来计算hash值的。
  4. Hash索引只支持等值比较，例如使用=，IN( )和<=>。对于WHERE price>100并不能加速查询。
  5. Hash索引遇到大量Hash值相等的情况后性能并不一定就会比B-Tree索引高。

#### 5.7空间(R-Tree)索引

- MyISAM支持空间索引，主要用于地理空间数据类型，例如GEOMETRY。

#### 5.8全文(Full-text)索引

- 全文索引是MyISAM的一个特殊索引类型，主要用于全文检索。
- 全文索引的实现是，**二元分词法和倒排索引(数据结构为B+树)**

### 6.高性能的索引策略

#### 6.1聚簇索引

- **定义**

  1. 聚簇索引将数据存储与索引放到了一块，索引结构的**叶子节点保存了行数据**。
  2. 只有solidDB和InnoDB支持建立聚簇索引。
  3. 聚簇索引具有唯一性，由于**聚簇索引是将数据跟索引结构放到一块**，因此**一个表仅有一个聚簇索引**。
  4. **表中行的物理顺序和索引中行的物理顺序是相同的**，在创建任何非聚簇索引之前创建聚簇索引，这是因为聚簇索引改变了表中行的物理顺序，数据行按照一定的顺序排列，并且自动维护这个顺序。
  5. 由于聚簇索引，会大量移动表数据的位置和索引一致，所以重建一次索引的代价很大。

- **InnoDB中的聚簇索引**

  ![](https://javanote.oss-cn-shenzhen.aliyuncs.com/12_InnoDB中的聚簇索引.png)

  1. 在innodb中，在聚簇索引之上创建的索引称之为辅助索引，非聚簇索引都是辅助索引，像复合索引、前缀索引、唯一索引。

  2. **辅助索引叶子节点存储的不再是行的物理位置，而是主键值，辅助索引访问数据总是需要二次查找**。 

  3. 聚簇索引**默认是主键**，如果表中没有定义主键，InnoDB会选择一个唯一且非空的索引代替。

     如果没有这样的索引，InnoDB 会隐式定义一个主键(类似oracle中的RowId)来作为聚簇索引。

     如果已经设置了主键为聚簇索引又希望再单独设置聚簇索引，必须先删除主键，然后添加想要的聚簇索引，最后恢复设置主键即可。

- **具体实例分析**

  假设id为聚簇索引，name为非聚簇索引

  1. noDB使用的是聚簇索引，将主键组织到一棵B+树中，而行数据就储存在叶子节点上，若使用"where id = 14"这样的条件查找主键，则按照B+树的检索算法即可查找到对应的叶节点，之后获得行数据。
  2. 若对Name列进行条件搜索，则需要两个步骤：第一步在辅助索引B+树中检索Name，到达其叶子节点获取对应的主键。第二步使用主键在主索引B+树种再执行一次B+树检索操作，最终到达叶子节点即可获取整行数据。(重点在于通过其他键需要建立辅助索引)

#### 6.2非聚簇索引

- **定义**

  1. 非聚簇索引将数据与索引分开存储，索引结构的**叶子节点指向了数据对应的位置**。
  2. MyISAM一般使用非聚簇索引。

- MyISAM中的非聚簇索引

  ![](https://javanote.oss-cn-shenzhen.aliyuncs.com/13_MyISAM的非聚簇索引.png)

  1. MyISAM使用的是非聚簇索引，非聚簇索引的两棵B+树看上去没什么不同，节点的结构完全一致只是存储的内容不同而已，
  2. 主键索引B+树的节点存储了主键，辅助键索引B+树存储了辅助键。表数据存储在独立的地方，这两颗B+树的叶子节点都使用一个地址指向真正的表数据。
  3. 对于表数据来说，这两个键没有任何差别。由于索引树是独立的，通过辅助键检索无需访问主键的索引树。

#### 6.3聚簇索引和非聚簇索引的区别联系

- **两种索引的查找流程**
  1. **聚簇索引的查找**。当使用辅助索引时，要经过两次B+树的查找。一次是辅助索引的B+树，一次是聚簇索引的B+树。最后再聚簇索引的树节点中，获取到行数据。
  2. **非聚簇索引的查找**。首先查找索引的B+树，在树节点中获取到指向表数据的指针。再通过指针，去寻找具体的行数据。
- **聚簇索引的优势**
  1. **当访问的数据都在同页中，只需要进行一次IO访问**。由于行数据和聚簇索引的叶子节点存储在一起。同一页中会有多条行数据，**访问同一数据页不同行记录时**，已经把页加载到了Buffer中(缓存器)。再次访问时，**会在内存中完成访问，不必访问磁盘**。这样主键和行数据是一起被载入内存的，找到叶子节点就可以立刻将行数据返回了，如果按照主键Id来组织数据，获得数据更快。
  2. 辅助索引的叶子节点，存储主键值，而不是数据的存放地址。 **当表数据发生变化时，只需要重新维护一遍聚簇索引，不用维护辅助索引**。因为辅助索引存放的是主键值，减少了辅助索引占用的存储空间大小。
  3. 因为MyISAM的主索引并非聚簇索引，那么具体数据的物理地址必然是凌乱的。**通过索引拿到访问数据的物理地址，按照合适的算法进行I/O读取，需要进行多次IO访问**。聚簇索引则只需一次I/O。
  4. 如果涉及到大数据量的排序、全表扫描、count之类的操作的话，还是MyISAM占优势些，因为索引所占空间小，这些操作是需要在内存中完成的。 
- **聚簇索引的字段数据类型**
  1. 当使用主键为聚簇索引时，主键最好不要使用uuid。因为uuid的值太过离散，不适合排序且可能出线新增加记录的uuid，会插入在索引树中间的位置，导致索引树调整复杂度变大，消耗更多的时间和资源。
  2. 建议使用int类型的自增，方便排序并且默认会在索引树的末尾增加主键值，对索引树的结构影响最小。而且，主键值占用的存储空间越大，辅助索引中保存的主键值也会跟着变大，占用存储空间，也会影响到IO操作读取到的数据量。
  3. 主键建议使用自增ID。聚簇索引的数据的物理存放顺序与索引顺序是一致的，只要索引是相邻的，那么对应的数据一定也是相邻地存放在磁盘上的。



#### 6.4覆盖索引

- **定义**

  1. mysql的innodb引擎通过搜索树方式实现索引，索引类型分为主键索引和二级索引(非主键索引)。主键索引树中，叶子结点保存着主键即对应行的全部数据。而二级索引树中，叶子结点保存着索引值和主键值，当使用二级索引进行查询时，需要进行回表操作。
  2. 覆盖索引是指，当sql语句的所求查询字段(select列)和查询条件字段(where子句)全都包含在一个索引中，可以直接使用索引查询而不需要回表。
  3. 通过使用覆盖索引，可以减少搜索树的次数，就是减少回表，这是常用的性能优化手段。

- **具体实例**

  ```sql
  -- 建表
  CREATE TABLE `user_table` (
    `id` int(11) unsigned NOT NULL AUTO_INCREMENT,
    `username` varchar(255) NOT NULL,
    `password` varchar(255) DEFAULT NULL,
    `age` int(11) unsigned Not NULL,
    PRIMARY KEY (`id`),
    key (`username`)
  ) ENGINE=InnoDB  DEFAULT CHARSET=utf8
  ```
  1. 覆盖索引
     执行 `select id from user_table where username = 'lzs'`时，因为username索引树的叶子结点上保存有username和id的值，所以通过username索引树查找到id后，我们就已经得到所需的数据了，这时候就不需要再去主键索引上继续查找了。

  2. 不使用覆盖索引
     执行语句 `select password from user_table where username = 'lzs'` 时。首先，username索引树上找到username=lzs 对应的主键id。然后，通过回表在主键索引树上找到满足条件的数据。

- **建立索引实现覆盖索引**

  1. 当两个字段A,B是高频的查询字段时，可以建立一个主键聚簇索引，另一个建立辅助索引。查询聚簇索引的值，条件为辅助索引，这样就可以实现覆盖索引。

#### 6.5联合索引-最左前缀

- **定义**

  1. 假设字段username设置索引，则辅助索引B+树节点，存储的是字段username的信息。假设字段username和password设置索引，则辅助索引B+树节点，存储的是两个字段的信息。这就是联合索引。

- **特点**

  1. 联合索引的多个字段中，只有当查询条件为联合索引的第一个字段时，查询才能使用该索引。

     ```sql
     -- 假设联合索引(username,password)
     -- 1.使用到索引的两种情况
     select * from user_table where username = 'lzs';
     select * from user_table where username = 'lzs' and password = '123456';
     -- 2.没有使用到联合索引,因为password不是联合索引的第一个字段
     select * from user_table where  password = '123456';
     ```

  
  2. 索引可以用于查询条件字段为索引字段，根据字段值最左若干个字符进行的模糊查询。
  
     ```sql
      -- 假设联合索引(username,password)
     -- 使用到索引
     查出用户名的第一个字是“张”开头的人的密码。即查询条件子句为"where username like '张%'"
     -- 没有使用到索引
     查处用户名中含有“张”字的人的密码。即查询条件子句为"where username like '%张%'"
     查出用户名以“张”字结尾的人的密码。即查询条件子句为"where username like '%张'"
     ```
  
  
  3. 当查询的字段，都存在联合索引中的时候，不用进行回表，相当于是覆盖索引。
  
     ```sql
     -- 假设联合索引(username,password)
     -- 不用进行回表,因为字段password存在联合索引中
     select password from user_table where username = 'lzs';
     ```
  

#### 6.6索引下推

- **定义**

  1. 使用联合索引且需要回表查询全行数据的时候，会筛选出最少的索引数目，再进行回表查询。这样读取的全行数据比较少，减少IO压力，称为索引下推。

- **具体实例**

  ```sql
  -- 联合索引为(username,age)
  -- 执行语句
  select * from user_table where username like '张%' and age < 10;
  ```

  1. 第一步，会先在辅助索引的B+树进行查找。先查找出`username like '张%'`的索引，再从中筛选出`age < 10`的索引，以达到最少的索引数目。
  2. 第二步，拿筛选出的索引回表查询，获取主键索引B+树的数据，从中读取全行数据。

- **索引下推的启用**

  1. mysql默认启用索引下推，也可以通过修改系统变量来控制启用。

     ```sql
     SET optimizer_switch = 'index_condition_pushdown=off';
     ```

- **索引下推的使用**

  1. Innodb引擎的表，索引下推只能用于二级索引。因为Innodb的主键索引树叶子结点上保存的是全行数据，所以这个时候索引下推并不会起到减少查询全行数据的效果。

  2. 索引下推一般可用于所求查询字段（select列）不是/不全是联合索引的字段，查询条件为多条件查询且查询条件子句（where/order by）字段全是联合索引。

     ```sql
     -- 假设表t有联合索引（a,b）,下面语句可以使用索引下推提高效率
     select * from t where a > 2 and b > 10;
     ```

#### 6.7利用索引排序

- **生成顺序结果集**

  1. `MySQL`中有两种方式生成有序结果集，一是使用filesort，二是按索引顺序扫描。

- **按索引顺序扫描**

  1. 利用索引进行排序操作是非常快的，而且可以利用同一索引同时进行查找和排序操作。
  2. 当索引的顺序与`ORDER BY`中的列顺序相同且所有的列是同一方向(全部升序或者全部降序)时，可以使用索引来排序。
  3. 如果查询是连接多个表，仅当`ORDER BY`中的所有列都是第一个表的列时才会使用索引。
  4. 其它情况都会使用filesort。

- **索引排序的实例**

  ```sql
  -- 创建表
  create table actor(
    actor_id int unsigned NOT NULL AUTO_INCREMENT,
    name varchar(16) 	NOT NULL DEFAULT '',
    password varchar(16) 	NOT NULL DEFAULT '',
    PRIMARY KEY(actor_id),
    KEY     (name)
  ) ENGINE=InnoDB
  insert into actor(name,password) values('cat01','1234567');
  insert into actor(name,password) values('cat02','1234567');
  insert into actor(name,password) values('ddddd','1234567');
  insert into actor(name,password) values('aaaaa','1234567');
  
  -- 利用索引排序
  mysql> explain select actor_id from actor order by actor_id;
  Extra: Using index
  mysql> explain select actor_id from actor order by password;
  Extra: Using filesort
  mysql> explain select actor_id from actor order by name;
  Extra: Using index
  ```
  
- **Filesort排序**
  
  1. 当MySQL不能使用索引进行排序时，就会利用自己的排序算法(快速排序算法)在内存(sort buffer)中对数据进行排序。
  2. 如果内存装载不下，会将磁盘上的数据进行分块，再对各个数据块进行排序，然后将各个块合并成有序的结果集(实际上就是外排序)。
  3. Filesort排序有两种算法，两遍扫描算法和一次扫描算法。
  
- **两遍扫描算法(Two passes)**

  1. 先将须要排序的字段和可以直接定位到相关行数据的指针信息取出。
  2. 然后在设定的内存(通过参数sort_buffer_size设定)中进行排序。
  3. 完成排序之后再次通过行指针信息取出所需的Columns。
  4. 该算法是4.1之前采用的算法，它需要两次访问数据，尤其是第二次读取操作会导致大量的随机I/O操作。另一方面，内存开销较小。

- **一次扫描算法(single pass)**

  1. 该算法一次性将所需的Columns全部取出，在内存中排序后直接将结果输出。
  2. 从MySQL4.1版本开始使用该算法。它减少了I/O的次数，效率较高，但是内存开销也较大。如果将并不需要的Columns也取出来，就会极大地浪费排序过程所需要的内存。
  3. 在MySQL4.1之后的版本中，可以通过设置max_length_for_sort_data参数来控制MySQL选择第一种排序算法还是第二种。
  4. 当取出的所有大字段总大小大于max_length_for_sort_data的设置时，MySQL就会选择使用第一种排序算法，反之，则会选择第二种。
  5. 为了尽可能地提高排序性能，倾向于使用第二种排序算法，所以在Query中仅仅取出需要的Columns是非常有必要的。

- **连接表的排序**

  1. 当对连接操作进行排序时，如果ORDER BY仅仅引用第一个表的列，MySQL对该表进行filesort操作，然后进行连接处理，此时，EXPLAIN输出"Using filesort"；
  2. 否则，MySQL必须将查询的结果集生成一个临时表，在连接完成之后进行filesort操作，此时，EXPLAIN输出"Using temporary;Using filesort"。

  

### 7.索引与加锁

- **InnoDB的索引对锁的影响**

  1. 引对于InnoDB非常重要，因为可以让查询锁更少的元组。因为MySQL 5.0中，InnoDB直到事务提交时才会解锁。
  2. 即使InnoDB行级锁的开销非常高效，内存开销也较小，但还是存在开销。
  3. 对不需要的元组的加锁，会增加锁的开销，降低并发性。
  4. InnoDB仅对需要访问的元组加锁，而索引能够减少InnoDB访问的元组数。但只有在存储引擎层过滤掉那些不需要的数据，才能达到这种目的。
     一旦索引达不到过滤的目的，MySQL服务器只能对InnoDB返回的数据进行WHERE操作。此时，已经无法避免对那些元组加锁了。InnoDB已经锁住那些元组，服务器无法解锁了。

- **具体实例**

  ```sql
  -- InnoDB只对SELECT语句中WHERE子句actor_id<4使用索引，没有对actor_id<>1使用索引(运算符<>不能使用索引)。
  -- 所以MySQL服务器会锁住1,2,3三个元组。使用索引就是为了让MySQL服务器锁住更少的元组，更高的锁住元组。
  SET AUTOCOMMIT=0;
  BEGIN;
  SELECT actor_id FROM actor WHERE actor_id < 4
  AND actor_id <> 1 FOR UPDATE;
  ```

  