
import java.util.*;

//import static project2.Parse.reformatExpression;

public class MuaInterpreter {

    //hash map用来存储各个变量
    static Map<String, MuaVariable> variables = new HashMap<>();
    //hash map用来存储所有op 及其参数个数
    static Map<String, Integer> operation_map = new HashMap<>();

    public static boolean isAnFunction(String name, Map<String, MuaVariable> variables) {
        String in = name;
        if (variables.containsKey(name) && variables.get(name).type == variables.get(name).LIST && variables.get(name).toList().size() == 2) {
            return true;
        }
        if (operation_map.containsKey(in)) {
            return true;
        } else {
            return false;
        }
    }
    //用于debug
    private static boolean debug = false;
    public static void main(String[] args) {
        init_operation_map();
        initialExistingName();
        Scanner in = new Scanner(System.in);
        Scanner in2 = new Scanner(System.in);


        System.out.println("*****Test Info*****");
        String input = "begin";

        while (!input.equals("EOF")) {
            System.out.print(">>>> ");
            Info test = new Info();
            input = in.next();//读入第一个标准输入

            if (input.length() >= 2 && input.substring(0, 2).equals("//")) {//处理注释的情况
                String temp_comment = in.nextLine();//直接忽略这一行后面的输入 同时因为是回车作为终结符的缘故，如果input为"//qwe"这种，这里的in.nextLine()也不会影响后面的读入
                continue;
            }


            int flag_for_add = 0;//用于处理输入中含有+-*/%()的情况

            MuaVariable input_obj;
            if (variables.containsKey(input)) {
                input_obj = variables.get(input);//处理第一个输入
            } else input_obj = new MuaVariable(input);//处理第一个输入
            test.addParameters(input);
            int count_p = Parse.getNumOfPara(input,variables,operation_map);//参数个数计数器

            String for_list_in = "";//用于拼接list输入
            int count_left = 0;//左中括号数目

            while (count_p > 0) {
                for_list_in="";
                input = in.next();//读入标准输入
                //System.out.println("debug keyboard in "+input);
                if (input.length() >= 2 && input.substring(0, 2).equals("//")) {//处理含有注释的情况
                    String temp_comment = in.nextLine();
                    continue;
                }

                if (input.charAt(0) == '(') {
                    input = getExpression(input, in, variables);
                }
                if (input.charAt(0) == '[') {//如果是list

                    count_left++;//左中括号数目++

                    if (input.length() >= 3 && input.substring(1, 3).equals("//")) {
                        String temp_comment = in.nextLine();
                        //System.out.println("??");
                    } else {
                        for_list_in += input.substring(0, input.length());//处理[后面紧跟字符的情况 拼入for_list_in
                        for_list_in += " ";//尾巴加个空格 规整化 方便处理
                        if (input.charAt(input.length() - 1) == ']') {//如果是形如 [3123] 这样的list
                            count_left--;//左中括号数目++
                        }
                    }


                }

                while (count_left > 0) {//读入list 为了允许换行
                    String input_2 ="";
                    input_2=in.next();//读入标准输入
                    if (debug) System.out.println("debug inp2 "+input_2);

                    if (input_2.length() >= 2 && input_2.substring(0, 2).equals("//")) {
                        String temp_comment = in.nextLine();
                        continue;
                    }
                    for_list_in += input_2;//拼入 for_list_in
                    for_list_in += ' ';
                    for (int i = 0; i < input_2.length(); i++) {
                        if (input_2.charAt(i) == '[') count_left++;

                        if (input_2.charAt(i) == ']') {
                            count_left--;
                        }
                    }

                    if (count_left == 0) {//list读入完毕

                        input = for_list_in;//更新input
                        for_list_in="";
                        break;
                    }
                }



                input = input.trim();
                MuaVariable temp_obj;//初始化要添加的MuaVariable参数
                if (variables.containsKey(input)) {
                    temp_obj = variables.get(input);//处理第一个输入
                } else temp_obj = new MuaVariable(input);//处理第一个输入

                test.addParameters(input);//加入到test中
                //System.out.println("input"+count_p+" "+input);
                count_p--;//参数个数计数器--
                count_p += Parse.getNumOfPara(input,variables,operation_map);//参数个数计数器增加 用于处理嵌套的情况




            }

            while (!test.result.isEmpty()) {
                Parse parser = new Parse(test.result);//用处理过的输入 初始化语法解释器
                MuaVariable res;
                res = parser.parsing(test.result, variables,operation_map);//获得解释的结果
                if (res.raw_value.charAt(0)=='['&&res.raw_value.charAt(res.raw_value.length()-1)==']') res.raw_value = res.raw_value.substring(1,res.raw_value.length()-1).trim();//用于处理直接thing一个list的情况，删去读出的list结果前后的括号
                if (debug)System.out.println("nua res"+res);
                Info test2 = new Info();
                test2.get_info(res.toString(), variables);//重新初始化Info信息


                while (!res.raw_value.equals("FINISHED")||!test2.result.isEmpty()) {//用于处理直接thing一个list的情况，生成一个新的info来处理thing出来的结果 并传给解释器 后面的非空判断是是处理当thing的list有多条操作的情况
                    res = parser.parsing(test2.result, variables,operation_map);//重新解释

                    if (test2.result.isEmpty()) break;

                }
                if (res.toString().equals("EOF")) {//d当输入EOF时，退出程序
                    System.out.println("project2.MuaInterpreter has exited.");
                    System.exit(0);
                }

                if (!Parse.isFunctionOrOp(res.toString(),variables,operation_map)  && !res.toString().equals("FINISHED"))
                    parser.muaError_handle.command_not_found("main"+res.toString());//遇到不符合标准的返回值 进行错误处理

            }

        }

    }
    //中缀转前缀 处理输入中含有+-*/%()的情况

    ////
    private static String getExpression(String in, Scanner temp_scan, Map<String, MuaVariable> variables) {

        String exp = in;
        exp += " ";
        int count_small_left = 0;

        for (int i = 0; i < in.length(); i++) {
            if (in.charAt(i) == '(') {//(kkk)
                count_small_left++;
            }
            if (in.charAt(i) == ')') {
                count_small_left--;
            }
        }
//读入新的输入来允许空格
        while (count_small_left > 0) {
            String input_2 = temp_scan.next();
            exp += input_2;
            exp += " ";
            for (int i = 0; i < input_2.length(); i++) {
                if (input_2.charAt(i) == '(') count_small_left++;

                if (input_2.charAt(i) == ')') {
                    count_small_left--;
                }
            }

        }

        exp = Parse.reformatExpression(exp, variables,operation_map);


        //System.out.println("lll:"+exp);
        return exp.trim();
    }

    static void init_operation_map(){
        operation_map.put("add",2);
        operation_map.put("sub",2);
        operation_map.put("mul",2);
        operation_map.put("div",2);
        operation_map.put("mod",2);
        operation_map.put("eq",2) ;
        operation_map.put("gt",2) ;
        operation_map.put("lt",2) ;
        operation_map.put("and",2);
        operation_map.put("or",2) ;
        operation_map.put("not",2);
        operation_map.put("make",2);
        operation_map.put("thing",1);
        operation_map.put(":",0) ;
        operation_map.put("//",0) ;
        operation_map.put("erase",1);
        operation_map.put("isname",1);
        operation_map.put("print",1);
        operation_map.put("read",0);
        operation_map.put("readlinst",0);
        operation_map.put("repeat",2);
        operation_map.put("item",2);
        operation_map.put("output",1);
        operation_map.put("stop",0);
        operation_map.put("export",0);
        operation_map.put("isnumber",1);
        operation_map.put("isword",1);
        operation_map.put("isbool",1);
        operation_map.put("islist",1);
        operation_map.put("isempty",1);
//        random <number>`：返回[0,number)的一个随机数
        operation_map.put("random",1);
//        sqrt <number>`：返回number的平方根
        operation_map.put("sqrt",1);
//        int <number>`: floor the int
        operation_map.put("int",1);
//word <word> <word|number|bool>`：将两个word合并为一个word，第二个值可以是word、number或bool
        operation_map.put("word",2);//问一下 value的引号
        //if <bool> <list1> <list2>`：如果bool为真，则执行list1，否则执行list2。list均可以为空表
        operation_map.put("if",3);
//        * `sentence <value1> <value2>`：将value1和value2合并成一个表，两个值的元素并列，value1的在value2的前面
        operation_map.put("sentence",2);
//        * `list <value1> <value2>`：将两个值合并为一个表，如果值为表，则不打开这个表
        operation_map.put("list",2);
//        * `join <list> <value>`：将value作为list的最后一个元素加入到list中（如果value是表，则整个value成为表的最后一个元素）
        operation_map.put("join",2);
//        * `first <word|list>`：返回word的第一个字符，或list的第一个元素
        operation_map.put("first",1);
//        * `last <word|list>`：返回word的最后一个字符，list的最后一个元素
        operation_map.put("last",1);
//        * `butfirst <word|list>`：返回除第一个元素外剩下的表，或除第一个字符外剩下的字
        operation_map.put("butfirst",1);
//        * `butlast <word|list>`：返回除最后一个元素外剩下的表，或除最后一个字符外剩下的字operation_map.put("if",3);
        operation_map.put("butlast",1);
//        * `wait <number>`：等待number个ms
        operation_map.put("wait",1);
//        * `save <word>`：保存当前命名空间在word文件中
        operation_map.put("save",1);
//        * `load <word>`：从word文件中装载内容，加入当前命名空间
        operation_map.put("load",1);
//        * `erall`：清除当前命名空间的全部内容
        operation_map.put("erall",0);
//        * `poall`：列出当前命名空间的全部名字
        operation_map.put("poall",0);

        operation_map.put("output",1);
        operation_map.put("abs",1);
        //operation_map.put("run",1);

    }
    //声明既有名字
    static void initialExistingName(){
        variables.put("pi",new MuaVariable(String.valueOf(3.1415926)));//添加既有名字
        variables.put("run",new MuaVariable("[ [ run_v ]  [ output :run_v ]]"));//添加常用操作
}
}