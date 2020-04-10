## MySql基本使用

### 1.操作数据库

#### 1.1MySQL语句的规范

1. 关键字与函数名称全部大写。
2. 数据库名称、表名称、字段名称全部小写。
3. SQL语句必须以分号结尾。

#### 1.2操作数据库(清空命令行：cls)

1. CREATE创建
  `CREATE {DATABASE | SCHEMA} [IF NOT EXISTS](可有可无) db_name [DEFAULT] CHARACTER SET [=] charset_name`
2. SHOW查询
  `SHOW DATABASES(查看全部数据库)`
  `SHOW WARNINGS(查看警告)`
  `SHOW CREATE DATABASE db_name(查看数据库创建时的信息)`
3. ALTER修改
  `ALTER  {DATABASE | SCHEMA} [IF NOT EXISTS] db_name [DEFAULT] CHARACTER SET [=] charset_name(用来修改数据库的编码方式)`
4. DROP删除
  `DROP {DATABASE | SCHEMA} [IF NOT EXISTS] db_name`

### 2.操作数据表



### 3.约束

### 4.操作数据表中的记录

### 5.子查询和连接

### 6.运算符和函数

### 7.自定义函数

### 8.存储过程(比较少用)

###9.