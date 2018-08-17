# MuaInterpreter
A Java interpreter for PL MUA
# 阶段1：

# 一、实验目的和要求

 

使用Java语言编写一个MUA语言解释器

 

# 二、实验内容和原理

 

\# MakeUp Programming Language

 

**##** **基本数据类型****value**

 

数字number，单词word，列表list，布尔bool

 

\* 数字的字面量以[0~9]或'-'开头，不区分整数，浮点数

\* 单词的字面量以双引号"开头，不含空格，采用Unicode编码。在"后的任何内容，直到空格（包括空格、tab和回车）为止的字符都是这个单词的一部分，包括其中可能有的"和[]等符号

\* 列表的字面量以方括号[]包含，其中的元素以空格分隔；元素可是任意类型；元素类型可不一致

 

**##** **基本操作**

 

基本形式：操作名 参数

 

操作名是一个不含空格的词，与参数间以空格分隔。参数可以有多个，多个参数间以空格分隔。每个操作所需的参数数量是确定的，所以不需要括号或语句结束符号。有的操作有返回值，有的没有。

 

一个程序就是操作的序列。

 

**基本操作有：**

 

\* `//`：注释

\* `make <word> <value>`： 将value绑定到word上。基本操作的单词不能用做这里的word。绑定后的word称作名字，位于命名空间。

\* `thing <word>`：返回word所绑定的值

\* `:<word>`：与thing相同

\* `erase <word>`：清除word所绑定的值

\* `isname <word>`：返回word是否是一个名字，true/false

\* `print <value>`：输出value

\* `read`：返回一个从标准输入读取的数字或单词

\* `readlinst`：返回一个从标准输入读取的一行，构成一个列表，行中每个以空格分隔的部分是list的一个元素

\* 运算符operator

​       * `add`, `sub`, `mul`, `div`, `mod`：`<operator> <number> <number>`

​       * `eq`, `gt`, `lt`：`<operator> <number|word> <number|word>`

​       * `and`, `or`：`<operator> <bool> <bool>`

​       * `not`：`not <bool>`

 

 

 

# 三、主要仪器设备

系统环境：macOS Sierra 10.12.6

开发环境：IntelliJ IDEA for Mac 2017.2.5

 

# 四、操作方法和实验步骤

**4.1** **整体思路**

我编写的MuaInterpreter思路如下：

首先创建一个MuaVariable类来进行Mua语言变量的处理，之后在主类MuaInterpreter中设置一个Map类型的静态变量来存储MuaInterpreter使用make命令创建的所有Mua变量。然后在主函数中读入标准输入，使用Info类来处理标准输入，处理之后的标准输入为一个以MuaVariable为基础类型的List，List中包含所有的命令与命令所需的操作数。之后，将List作为要处理的语法信息传递给Parse类中用来进行语法分析的方法parsing(List<MuaVariable> grammar_info)函数，parsing方法会进行Mua语法的解析,根据不同的命令调用不同的方法， 并打印相应的结果或添加相应变量到Map中。在parsing方法中采用了递归的实现来处理命令的嵌套（如print eq thing "a "qwe）。最后，实现了一个错误处理类MuaError来处理运行中遇到的语法错误。一旦遇到语法错误，程序变回退出。

**4.2** **数据结构**

**4.2.1 MuaVariable****类**

我编写了MuaVariable类来存储Mua解释器生成的变量。该类包含了三个内部变量：

​       String v_name;//存储变量名；对于Word变量，是将开头的引号也一并存储的

​    String raw_value;//存储该Mua变量存储的原始字符串

​    int type;//存储Mua变量类型 Word：0  number：1 bool：2 list：3  op(命令):4 

**4.2.2 Map<String, MuaVariable> variables**

Java Map接口中键和值一一映射. 可以通过键来获取值。

即给定一个键和一个值，可以将该值存储在一个Map对象. 之后，可以通过键来访问对应的值。

我使用Java自带的HashMap类型来存储所有的MuaVariable，可以方便地查询在解释过程中定义的变量。 

 

**4.3** **各个类的说明**

 

**4.3.1 MuaVariable****类**

(a) 类的功能：

Mua变量类，用来表示Mua的变量

(b) 构造函数

分为默认构造函数与3个带参数的构造函数：

MuaVariable() ：默认构造函数，所有成员变量初始为空，设置类型为word

MuaVariable(String name, String value)：带参数的构造函数，可指定Mua变量名、变量值，并且会根据变量值自动设置变量类型

MuaVariable(String value)：带参数的构造函数，可指定Mua变量值，并且会根据变量值自动设置变量类型

 

(c) 方法的注释

boolean isStringAnOp(String in)： 返回输入的字符串是否为Mua的一个命令

 

boolean isNumber(String str)：返回输入的字符串是否为一个数字，采用正则表达式进行判断

 

void set_type()：设置变量类型

 

String toString()：返回Mua变量作为一个Word的取值

 

double toDouble()：返回Mua变量作为一个Num的取值，为了方便处理，统一转为Double

 

boolean toBoolean()：返回Mua变量作为一个bool的取值

 

List<String> toList()：返回Mua变量作为一个list的取值

(d) 全局变量的注释

//各种变量类型

​    final int WORD = 0;

​    final int NUM  = 1;

​    final int BOOL = 2;

​    final int LIST = 3;

​    final int OP = 4;

(e) 字段/属性的注释

​    String v_name;//存储变量名

​    String raw_value;//存储该Mua变量存储的原始字符串

​    int type;//存储Mua变量类型 Word：0  number：1 bool：2 list：3  op(命令):4 

 

**4.3.2 Info****类** 

(a) 类（接口）的功能

处理标准输入， 取出op与operand

(b) 构造函数的注释

Info() :初始化成员变量

(c) 方法的注释

void get_info(String input)：将标准输入读入的字符串按照空格split，分为操作数与操作符，并特殊处理输入的变量类型为list的情况，将结果存入成员变量中的List<MuaVariable> result;

(d) 字段/属性的注释

String raw_input; 原始的 标准输入

List<MuaVariable> result; 处理之后的结果list

**4.3.3 Parse****类** 

(a) 类（接口）的功能

进行语法分析与解释执行

(b) 构造函数的注释

//带参数的构造函数

Parse(List<MuaVariable> input) ：将成员变量List<MuaVariable> grammar_info初始化为input的值

(c) 方法的注释

public MuaVariable parsing(List<MuaVariable> grammar_info) : 会进行Mua语法的解析,根据不同的命令调用不同的方法， 并打印相应的结果或添加相应变量到Map中。在parsing方法中采用了递归的实现来处理命令的嵌套（如print eq thing "a "qwe）。

 

public MuaVariable op_make(MuaVariable operand0, MuaVariable operand1) ：根据传入的参数创建Mua变量，其中operand0为变量名 operand1为变量值 返回执行结果，成功则返回FINISHED常量

 

public MuaVariable op_thing(MuaVariable operand0) ：根据传入的参数取出Mua变量的值，其中operand0为变量名 返回变量的值

 

public MuaVariable op_colon(MuaVariable operand0) ：根据传入的参数取出Mua变量的值，其中operand0为变量名， 返回变量的值

 

public MuaVariable op_erase(MuaVariable o_v_name) ：根据传入的参数在抹除Map中的Mua变量，其中operand0为变量名 成功则返回FINISHED常量

 

public MuaVariable op_isname(MuaVariable o_v_name) ：根据传入的参数判断是否我Mua变量名，o_v_name为变量名

 

public MuaVariable op_print(MuaVariable operand0) ：打印变量值，operand0为变量值 注意如果不是打印变量，只是单纯的打印字符串的话，要在字符串加引号 如print "hello,world

 

public MuaVariable op_read() ：从标准输入读入变量值，返回一个用读入的值初始化的Mua变量

 

public MuaVariable op_readlinst() ：从标准输入读入一行，返回一个用读入的值初始化的Mua变量

 

public MuaVariable op_arithmetic(String op, List<MuaVariable> result) ：根据传入的参数调用相应的算术方法或者逻辑方法

 

public MuaVariable op_add(MuaVariable a, MuaVariable b) ：只能对number类型操作，返回运算的结果

其余算术操作类似，不再赘述

 

public MuaVariable op_eq(MuaVariable a, MuaVariable b) ：只能比较Word和Word或者number和number 返回一个TRUE或者FALSE常量，其余比较操作类似，不再赘述

 

public MuaVariable op_and(MuaVariable a, MuaVariable b) ：只能对bool类型变量操作，返回TRUE或者FALSE常量，其余逻辑操作类似，不再赘述

 

(d) 全局变量的注释

```

```

//变量类型常数设置 与 MuaVariable中的相同

​    static final int WORD = 0;

​    static final int NUM  = 1;

​    static final int BOOL = 2;

​    static final int LIST = 3;

​    static final int OP = 4;

​    static boolean debug=false;

 

​    //用于返回的mua的变量

​    static final MuaVariable FINISHED = new MuaVariable("FINISHED");

​    static final MuaVariable TRUE = new MuaVariable("true");

​    static final MuaVariable FALSE = new MuaVariable("false");

 ```

 (e) 字段/属性的注释

​    List<MuaVariable> grammar_info;//以list形式存储需要解析的信息

​    MuaError muaError_handle = new MuaError();//错误处理类

 

 

**4.3.4 MuaInterpreter** **类** 

main函数入口 ；

设置一个Map类型的静态变量来存储MuaInterpreter使用make命令创建的所有Mua变量

 

**4.3.5 MuaError** **类** 

(a) 类（接口）的功能

处理parse过程中的错误，一旦错误会打印错误信息，并直接退出整个程序。

(b) 方法的注释

public void command_not_found(java.lang.String command)：处理输入的命令找不到的错误。

 

public void parameter_missing()：处理参数不够的错误

 

public void variable_missing()：处理找不到相应变量的错误

 

public void illeagal_input()：处理输入不合法的错误
