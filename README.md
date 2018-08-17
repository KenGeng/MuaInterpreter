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

```
# 阶段2
在第一段的基础上补充实现以下内容：

在第一段的基础上补充实现以下内容：

## MakeUp Programming Language

* `repeat <number> <list>`：运行list中的代码number次

## 函数定义和调用

### 定义

		make <word> [<list1> <list2>]
			word为函数名
			list1为参数列表
			list2为操作列表

### 调用

		<functionName> <arglist>
			<functionName>为make中定义的函数名，不需要双引号"
			<arglist>是参数列表，<arglist>中的值和函数定义时的<list1>中名字进行一一对应绑定

### 函数相关的操作
			
* `output <value>`：设定value为返回给调用者的值，但是不停止执行
* `stop`：停止执行

### 表达式计算

允许使用以下运算符对数字进行计算：

	+-*/%()


## 三、主要仪器设备
系统环境：macOS Sierra 10.12.6
开发环境：IntelliJ IDEA for Mac 2017.2.5

## 四、操作方法和实验步骤
概述：本部分是在MUA解释器第一段的报告上撰写的，重点描述了为了满足MUA解释器第二段的需求而进行的修改与添加，关于MUA解释器第一段的介绍可参见MUA解释器第一段的报告上，这里不再赘述。
### 4.1 整体思路
1. 调整读入输入的方式
在MUA第一段中，我是直接用nextline读入一整行，再对这一行进行处理，这样是无法支持对换行输入的读取。为了支持换行输入，我改用了next方法，每次只读入一个字符串，来根据这个字符串来决定是否继续读入。为此，还需要维护一个变量count_p用来记录还需要读入多少个参数。比如如果这个字符串是make，那么它是一个需要两个参数的operator，count_p需要+2，之后，再次调用next来读入下一个参数,如果下一个参数是一个变量名比如"a，那么这是一个普通的字符串，count_p--；如果它是一个需要两个参数的operator，那么count_p--之后，还要+2.伪代码如下：
while (count_p>0){
                input = in.next();//读入标准输入
                deal with input and add the parameters into the buffer
                ...
                count_p--;//参数个数计数器--
                count_p+= number Of parameters the input needs;//参数个数计数器增加 用于处理嵌套的情况
                ...
            }

这样之后，还需要再对list类型的输入进行特殊处理，整个list应该被视为一个参数，为此需要进行括号的匹配，将最外面的一对[]之间的输入都当作list的内容。括号的匹配比较简单，也是维护一个括号计数器， 遇到左括号+1，遇到右括号-1，为0的时候，表示list输入完毕。
2. 关于repeat 操作
repeat操作有两个参数，第一个是执行的次数n，第二个是需要执行的在list中的语句。我的实现思路比较直接，在Parse类中编写了一个对应的op_repeat方法，当这个方法被调用时，它会使用一个for循环来调用parsing方法解释执行相应代码n次。
3. 函数的定义和调用操作
因为函数其实就是一个有着两个子list的list，所以在进行函数的定义，即添加函数到全局的hashmap的时候不将函数与list做区分。
在调用函数时，才对函数进行解析。
以一个例子来进行说明：
```
make "f [
	[a ]
	[
	make "b 10
	make "a add :a :b
	output :a
	stop
	print :a
	]
]
```

    上述代码定义了一个函数f，它有一个参数list，以及操作list。定义时，它直接作为一个普通的list存入。当解释器要解释对函数的调用语句，如“ f 12 ”时，它会先判断f是否可能为一个函数名，即f是否为一个list变量，且有两个子list。如果是，那么就进入执行函数的语句，开始解析f。在解析f时，创建了一个存储函数的局部变量及传入参数的hash map。同时，为了完成解析，我参照后面的实验要求，实现了不完全的item操作，（item操作描述：item <number> <list>`：返回列表中的第number项元素。）这样，我可以用item取出参数列表和操作列表。
    之后我会解析参数列表，通过split方法来得知参数的个数与参数名。再从对最上层解释器对函数的调用语句解析出“12”这个具体的参数值，将参数和参数值加入局部的hash map中,再处理操作列表。接着，将处理过的操作语句和局部的hash map一起传入parser中进行函数的解析。关于函数的返回值我是在局部的hash map中添加了一个特殊的变量: __return_value__，它有一个特殊的初始值。当函数中有output命令时，它会用output的结果来为__return_value__赋值。最后，根据__return_value__的值来决定是否将结果传给上层调用者。
    这里还有一点要说明，在MUA第一段中，我的parser传入的参数只有处理过的操作语句，这里为了支持函数内部定义的局部变量，我在parser中增加了一个hash map参数来指示解析中遇到的变量属于哪一层。

MuaVariable raw_args = op_item(new MuaVariable("0"),variables.get(choice),variables);//获得参数列表
MuaVariable raw_func = op_item(new MuaVariable("1"),variables.get(choice),variables);//获得操作列表
Map<String, MuaVariable> func_variables =new HashMap<>();//存储函数的局部变量的hash map
List<MuaVariable> args_input_value = new ArrayList<MuaVariable>();//在函数调用时，输入的具体的参数
for (int i =0 ;i< number of parametres ;i++){
args_input_value.add(parsing(grammar_info,variables));//从grammar_info中解析出进行函数调用时，输入的参数的具体的值
func_variables.put(arg_array[i], args_input_value.get(i));//将变量与变量的值插入局部变量的hash map中

            }


Info func_stmt = new Info();
func_stmt.get_info(raw_func);//处理要运行的操作语句，便于后面的解析
func_variables.put("__return_value__",UNINIT);//初始化返回值
解释执行函数的操作...
if (func_variables.get("__return_value__").toString().equals("__UnInit__")){//函数没有返回值
return FINISHED;
}else return func_variables.get("__return_value__");//返回相应的值


4.表达式计算
当表达式不含函数或前面定义的操作（“：”除外）：
    当一个表达式含有+-*/%()时，我的MUA解释器会将其视为一个中缀表达式，而在MUA第一段中，我们实现了add,sub,mul,div,mod等操作，这些操作的格式先有操作符，再有操作数，符合前缀表达式的形式。因此，为了支持含有+-*/%()的表达式，我一个很直接的思路就是将含有+-*/%()的中缀表达式转化为前缀表达式，再调用第一段中实现的add,sub,mul,div,mod等操作完成对表达式的计算。
比如对于形如:a+((:b+:c)*:d)-:e这样的表达式，我会将其转化为对应的前缀表达式 -+a*+bcde ，再转化为sub add a mul add b c d e,这样我就完全可以使用之前的方法来完成表达式的计算。
1. 负号与减号：我的解决方法是先识别出负号，负号相比减号，它的特征是在负号的前面要么没有字符，要么是左括号，识别出负号之后，我会在负号前面添加一个0，这样(-3*5)就变为(0-3*5)，负号转变成了减号。
以下是中缀转前缀的伪代码：

public static String midToPrefix(String raw_cal){
        Stack<algo_operator> operator_stack = new Stack<>();//操作符栈

        pre-treat raw_cal string//预处理输入 如果是负号 添加0 转化为减号 简化处理

        char[] cal_arr = pre.toString().toCharArray();
        for (int i =cal_arr.length-1;i>=0;){//从最右边开始遍历

            if (cal_arr[i] is +-*/ or right bracket) {

                algo_operator new_op = new algo_operator(cal_arr[i]);
                if (operator_stack.isEmpty()) {//栈空
                    operator_stack.push(new_op);//直接入栈
                    if (new_op.op==')') operator_stack.peek().setPriority(-1);//右括号入栈后，优先级调整为最低
                }
                else if (new_op.priority >= operator_stack.peek().priority) {
                    operator_stack.push(new_op);//当前操作符优先级>=栈顶运算符，入栈
                    if (new_op.op==')') operator_stack.peek().setPriority(-1); //右括号入栈后，优先级调整为最低
                }
                else {
                    while (!operator_stack.isEmpty()&&operator_stack.peek().priority > new_op.priority){// 当前操作符优先级<栈顶运算符且栈不空
                        operator_stack.pop();//pop 
                        concat the operator to the result string
                    }
                    operator_stack.push(new_op);//push 当前操作符
                }
                i--;
                continue;
            }else if (cal_arr[i]=='('){//遇到左括号
                while (operator_stack.peek().op!=')'){
                    operator_stack.pop();//将栈里的右括号上面的操作符全部pop
                    concat the operator to the result string
                }
                operator_stack.pop();//pop 最后的右括号
                i--;
                continue;
            }else{
                concat the operand to the result string
                i change;
                continue;
            }
        }
        while (!operator_stack.isEmpty()){//如果栈不空
           operator_stack.pop();//pop
        }
        reverse result string //逆序
        return res;

    }


当表达式包含函数或前面定义的操作（“：”除外）：
根据老师最新的要求，在表达式两端都会有个括号。同时结合前面的内容，我已经可以处理不包含op与函数的表达式。因此，我采用了递归的解决方案，先处理输入为能正确解析的格式，再将字表达式中每个函数与op解析出来，得到数值的结果后，替换函数与op,再调用前面的方法来计算出结果。这里有一个小trick，因为所有的表达式与函数都是前缀，所以在处理表达式中的函数的时候，我是从右向左进行解析，因为对于合法的表达式来说，最右边的函数所有的参数必定是给出的具体数字。这样从右向左必然可以完成对所有函数的替换。

### 4.2 数据结构
4.2.1 MuaVariable类
我编写了MuaVariable类来存储Mua解释器生成的变量。该类包含了三个内部变量：
	String v_name;//存储变量名；对于Word变量，是将开头的引号也一并存储的
    String raw_value;//存储该Mua变量存储的原始字符串
    int type;//存储Mua变量类型 Word：0  number：1 bool：2 list：3  op(命令):4 
4.2.2 Map<String, MuaVariable> variables
Java Map接口中键和值一一映射. 可以通过键来获取值。
即给定一个键和一个值，可以将该值存储在一个Map对象. 之后，可以通过键来访问对应的值。
我使用Java自带的HashMap类型来存储所有的MuaVariable，可以方便地查询在解释过程中定义的变量。 

### 4.3 各个类的说明

4.3.1 MuaVariable类
(a) 类的功能：
Mua变量类，用来表示Mua的变量
(b) 新的方法的注释
添加了新的变量类型：final int EXPRESSION = 5; 
public int numOfPara(String in) 返回字符串in需要的参数的个数

4.3.3 Parse类 
(a) 类（接口）的功能
进行语法分析与解释执行
(b) 新的方法的注释
static String reformatExpression(String exp,Map<String, MuaVariable> variables)：重新调整输入的表达式字符串exp。主要是在运算符左右添加空格，便于解析。
MuaVariable op_expression(MuaVariable temp,Map<String, MuaVariable> variables)：处理表达式的方法。

4.3.4 MuaInterpreter 类 
main函数入口 ；
设置一个Map类型的静态变量来存储MuaInterpreter使用make命令创建的所有Mua变量

4.3.5 MuaError 类 
(a) 类（接口）的功能
处理parse过程中的错误，一旦错误会打印错误信息，并直接退出整个程序。

# 阶段3

## MakeUp Programming Language

在第二阶段的基础上，补充实现以下操作：

## 类型判断

* `isnumber <value>`：返回value是否是数字 
* `isword <value>`：返回value是否是字
* `islist <value>`：返回value是否是表 
* `isbool <value>`：返回value是否是布尔量 
* `isempty <word|list>`: 返回word或list是否是空

## 数值计算

* `random <number>`：返回[0,number)的一个随机数
* `sqrt <number>`：返回number的平方根
* `int <number>`: floor the int

## 字表处理

* `word <word> <word|number|bool>`：将两个word合并为一个word，第二个值可以是word、number或bool
* `if <bool> <list1> <list2>`：如果bool为真，则执行list1，否则执行list2。list均可以为空表
* `sentence <value1> <value2>`：将value1和value2合并成一个表，两个值的元素并列，value1的在value2的前面
* `list <value1> <value2>`：将两个值合并为一个表，如果值为表，则不打开这个表
* `join <list> <value>`：将value作为list的最后一个元素加入到list中（如果value是表，则整个value成为表的最后一个元素）
* `first <word|list>`：返回word的第一个字符，或list的第一个元素
* `last <word|list>`：返回word的最后一个字符，list的最后一个元素
* `butfirst <word|list>`：返回除第一个元素外剩下的表，或除第一个字符外剩下的字
* `butlast <word|list>`：返回除最后一个元素外剩下的表，或除最后一个字符外剩下的字

## 其他操作

* `wait <number>`：等待number个ms
* `save <word>`：保存当前命名空间在word文件中
* `load <word>`：从word文件中装载内容，加入当前命名空间
* `erall`：清除当前命名空间的全部内容
* `poall`：列出当前命名空间的全部名字

## 既有名字

系统提供了一些常用的量，或可以由其他操作实现但是常用的操作，作为固有的名字。这些名字是可以被删除（erase）的。

* `pi`：3.14159
* `run <list>`：运行list中的代码

另外，对于函数操作，补充export操作：
* `export`：将本地make的值输出到全局


## 三、主要仪器设备
系统环境：macOS Sierra 10.12.6
开发环境：IntelliJ IDEA for Mac 2017.2.5

## 四、操作方法和实验步骤
### 4.1 类型判断部分既有名字
这部分主要是判断value的类型，在我之前的实现中，每个MUA变量都包含了自己的类型信息，所以进行这部分的编程就比较方便，直接读取每个MUA变量的类型信息进行判断即可。
以isword为例：
private MuaVariable op_isword(MuaVariable operand_is0) {
    //check type
    if (operand_is0.type == WORD) {//是否是word
        return TRUE;
    } else {
        return FALSE;
    }
}

判断是否为空也比较容易，如果word只有一个单引号标记，说明它为空；如果list中元素个数为0，说明它为空。根据以上两点，进行判断即可。代码如下：
private MuaVariable op_isempty(MuaVariable operand_is0, Map<String, MuaVariable> variables) {
    //check type
    String name = operand_is0.raw_value;
    int type = operand_is0.type;
    if (type != WORD && type != LIST) {//是否合法输入
              muaError_handle.illeagal_input();
        return FINISHED;
    } else {
        if (type == WORD) {
            if (operand_is0.toString().length() == 1) return TRUE;//只有一个标识字面量的引号
            else return FALSE;
        } else {
            if (operand_is0.toList().size() == 0) return TRUE;
            else return FALSE;
        }
    }
}

### 4.2数值计算部分
随机数调用Java的Random包的接口；sqrt和int调用Java的Math包的sqrt和floor接口即可，不再赘述。
### 4.3字表处理 
这部分可以细分为三小节，分别是if语句的实现、字表拼接、字表元素提取。
先谈谈最重要的if语句。
if操作有三个操作，第一个是bool型，后两个为list型，如果bool为真，则执行list1，否则执行list2。list均可以为空表。
if的实现分为两步：
1)	判断bool量真假，决定要执行的语句
2)	判断要执行的语句是否为空，为空直接返回，没有操作；不为空，将list中的语句传给解释器去执行。
有一点需要注意的就是，因为在我的设计中传入的list的值包含最外面的中括号，因此在将list中的语句传给解释器时，需要去掉两端的括号，从而使解释器正常执行。具体代码如下：
private MuaVariable op_if(MuaVariable operand_if0, MuaVariable operand_if1, MuaVariable operand_if2, Map<String, MuaVariable> variables, Map<String, Integer> operation_map) {
    if (operand_if0.type != BOOL) {
        muaError_handle.illeagal_input();
        return FINISHED;
    }
    String case1 = operand_if1.raw_value.substring(1, operand_if1.raw_value.length() - 1);
    String case2 = operand_if2.raw_value.substring(1, operand_if2.raw_value.length() - 1);
    List<MuaVariable> if_buf = new ArrayList<>();

    if (operand_if0.toBoolean()) {
        if (case1.isEmpty()) return FINISHED;
        else {
            if_buf.add(new MuaVariable(case1));
            return parsing(if_buf, variables, operation_map);
        }
    } else {
        if (case2.isEmpty()) return FINISHED;
        else {
            if_buf.add(new MuaVariable(case2));
            return parsing(if_buf, variables, operation_map);
        }
    }
}
接着是字表拼接。
word操作类似字符串加法，具体实现上也是将两个字符串相加，不过要注意一些细节的处理，比如第二个参量为word型的话，在我的设计中，直接对它取值的话，会有一个单引号标记，需要去掉之后再拼接。
关于sentence和list则稍有些复杂：
Sentence && List
print sentence "a  "b ==> [a b]
print sentence  [a]  [b] ==> [a b]
print sentence  "a  [b] ==>[a b]
print sentence  [a]  [ a [b] ] ==> [ a a [b] ]
print list "a  "b ==> [ a b ]
print list  [a]  [b] ==> [ [a ] [b] ]
print list  "a  [b] ==> [a [b ] ]
print list  [a]  [ a [b] ] ==>[ [a] [a [b] ] ]
参考以上老师给的例子，以及说明，可见sentence和list虽然都会获得一个新表，但当用于拼接的元素包含表时，sentence会将其展开，而list不会，直观地理解就是sentence会去掉作为拼接的元素的表的括号，而list不会。在我的实现中，每个表的值在存储时，是包括两边的括号的，因此要做list操作就很简单，直接拼接两个元素的原始值即可。而在做sentence操作时，如果有一个元素是表，那么需要遍历这个表才能获得它的各个元素，再作为元素拼入新表。以下为list和sentence的代码：
private MuaVariable op_list(MuaVariable operand_l0, MuaVariable operand_l1) {
    String l0 = "", l1 = "";
    if (operand_l0.type == WORD) {
        l0 = operand_l0.toWord();
    } else if (operand_l0.type == LIST) {
        l0 = operand_l0.raw_value;
    } else l0 = operand_l0.raw_value;
    if (operand_l1.type == WORD) {
        l1 = operand_l1.toWord();
    } else if (operand_l1.type == LIST) {
        l1 = operand_l1.raw_value;

    } else l1 = operand_l1.raw_value;
    String res = "[ " + l0 + " " + l1 + " ]";
    return new MuaVariable(res);
}
private MuaVariable op_sentence(MuaVariable operand_s0, MuaVariable operand_s1) {
    String s0 = "", s1 = "";
    if (operand_s0.type == WORD) {
        s0 = operand_s0.toWord();
    } else if (operand_s0.type == LIST) {
        List<MuaVariable> s0_list = operand_s0.toList();
        for (int i = 0; i < s0_list.size() + 0; i++) {
            s0 += s0_list.get(i) + " ";
        }
    } else s0 = operand_s0.raw_value;
      if (operand_s1.type == WORD) {
        s1 = operand_s1.toWord();
    } else if (operand_s1.type == LIST) {
        List<MuaVariable> s1_list = operand_s1.toList();
        for (int i = 0; i < s1_list.size(); i++) {
            s1 += s1_list.get(i) + " ";
        }


    } else s1 = operand_s1.raw_value;
    String res = "[ " + s0 + " " + s1 + " ]";
    return new MuaVariable(res);

}
在我的实现中，join操作和sentence的元素中含有表时没有本质区别，都会将表展开，然后拼接元素，获得新表。代码如下
private MuaVariable op_join(MuaVariable operand_j0, MuaVariable operand_j1) {
    if (operand_j0.type != LIST) {

        muaError_handle.illeagal_input("join");
        return FINISHED;
    }
    String list_str = "";
    //System.out.println("debug join:" + operand_j0.type + " " + operand_j0.raw_value);

    List<MuaVariable> j0_list = operand_j0.toList();
    for (int i = 0; i < j0_list.size(); i++) {
        list_str += j0_list.get(i) + " ";
    }
    if (operand_j1.type == WORD) list_str += operand_j1.toWord();
    else list_str += operand_j1.raw_value;

    String res = "[ " + list_str + " ]";
    return new MuaVariable(res);
}
最后就是字表元素的提取。这四个提取元素的操作没有太大区别，以first为例进行说明：
如果参数是word，那么直接用charAt()获得第一个字符即可；如果是表，那么将表打开获得第一个元素即可，不过为了统一，有些格式上的细节要处理。代码如下：
private MuaVariable op_first(MuaVariable operand_f0) {
    if (operand_f0.type == WORD) {
        System.out.println("debug first:" + operand_f0.raw_value + " ");
        return new MuaVariable("\"" + operand_f0.toWord().substring(0, 1));//记得添加引号 让Word的返回值统一
    } else {//list
        if (operand_f0.toList().get(0).type == WORD) return new MuaVariable("\"" + operand_f0.toList().get(0));//记得添加引号 让Word的返回值统一
        else if (operand_f0.toList().get(0).type == LIST) return new MuaVariable("[" + operand_f0.toList().get(0) + "]");//记得添加括号 让list的返回值统一
        else return operand_f0.toList().get(0);
    }
}

### 4.4其他操作部分
wait操作调用Java的线程睡眠接口即可；
save 与load操作调用Java读写文件的接口即可，需要注意的是，在我的实现中，命名空间是用hash map存储的，因此要注意一下hash map的遍历，以及我存储命名空间的格式为“变量名--变量值”中间用两个短横杠分开。示例如下：
A--123
v--21333
pi--3.1415926
run--[ [ m ]  [ output :m ]]
erall清空hash map即可 
poall遍历hash map打印值即可。
这里记录一下遍历hash map的操作：
Iterator iter = variables.entrySet().iterator();
while (iter.hasNext()) {

    Map.Entry entry = (Map.Entry) iter.next();
    Object key = entry.getKey();
    fileWriter.write(key + "--" + variables.get(key).raw_value + "\n");//使用-- 分割变量名和值
}
### 4.5 既有名字部分
常量直接构造在初始化解释器的时候，直接添加到存储变量的hash map中即可。
常用操作的实现要复杂一点。
首先，在我的MUA实现中，原生的op比如make, add等是放在了一个Map<String, Integer> operation_map中，map中的key为op名，值为该op需要的参数个数。
而自定义的函数则本质上就是有两个子表的表，也是变量的一种，所以是放在存储变量的hash map中。当然，在解释执行时，原生op与函数格式一致，都是名称+相应个数的参数。
像run这样的常用操作在我的实现中是自定义的函数，只不过是在解释器初始化时，直接添加好的，不需要使用者再设置。因此，在实现时，只需要添加一个函数即可。之后执行函数就可以使用之前实现过的过程了。
variables.put("run",new MuaVariable("[ [ run_v ]  [ output : run_v ]]"));//添加常用操作
这里还有一些细节，run的操作参数是一个list，这个list其实可以在函数的命名空间中运行，也可以在主解释器中运行。在我的理解，这里的run应该在主解释器中运行，这样它和我们之前做过的直接thing一个list就没有什么区别了。不过，在MUA3.0中，为了支持高阶函数，我调整了thing操作和：操作的返回值，原来它们的返回值不包括最外面的中括号，现在为了之后处理的一致性，我重新添加了这两个中括号。因此，实现run或者thing一个list的步骤时，相比以前，我对返回值在传给解释器之前多加了一个判断：如果返回给解释器的结果是一个list，那么去掉两端的中括号，之后由解释器去解释执行。
### 4.6 函数的export操作
这个操作将函数本地make的值传给全局，实现中，我只需要在遇到export操作时，将函数的局部的变量hash map直接遍历添加到主解释器的hash map 即可。实现的过程与之前的stop比较相似。

