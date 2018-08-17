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

