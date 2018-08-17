import java.io.*;
import java.math.BigDecimal;
import java.util.*;

/**
 * packagename: project2
 * author: ken
 * created in: 2017/11/30
 * QAQ
 */
public class Parse {


    //变量类型常数设置 与 MuaVariable中的相同
    static final int WORD = 0;
    static final int NUM = 1;
    static final int BOOL = 2;
    static final int LIST = 3;
    static final int OP = 4;
    //用于返回的mua的变量
    static final MuaVariable FINISHED = new MuaVariable("FINISHED");
    static final MuaVariable UNINIT = new MuaVariable("__UnInit__");
    static final MuaVariable TRUE = new MuaVariable("true");
    static final MuaVariable FALSE = new MuaVariable("false");
    static final MuaVariable STOP = new MuaVariable("STOP");
    static final MuaVariable EXPORT = new MuaVariable("EXPORT");
    static boolean debug = false;
    final int EXPRESSION = 5;
    List<MuaVariable> grammar_info;//以list形式存储需要解析的信息
    MuaError muaError_handle = new MuaError();//错误处理类

    //带参数的构造函数
    public Parse(List<MuaVariable> input) {
        grammar_info = input;
    }

    static public int getNumOfPara(String name, Map<String, MuaVariable> variables, Map<String, Integer> operation_map) {
        if (operation_map.containsKey(name)) {
            return operation_map.get(name);
        } else if (isAnFunction(name, variables)) {
            String temp_para = variables.get(name).toList().get(0).toString();
            String[] para = temp_para.substring(1, temp_para.length() - 1).trim().split(" ");
            if (temp_para.equals("[]") || temp_para.equals("[ ]")) return 0;
            return para.length;
        } else return 0;

    }

    static String reformatExpression(String exp, Map<String, MuaVariable> variables, Map<String, Integer> operation_map) {
        List<String> inin = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < exp.length(); i++) {
            if (isCharExpress(exp.charAt(i)) == 2) {
                sb.append(" ").append(exp.charAt(i)).append(" ");
            } else sb.append(exp.charAt(i));
        }
        exp = sb.toString();

        String[] temp = exp.split(" ");
        for (String ret : temp) {
            if (!ret.equals("")) {
                inin.add(ret);
            }
        }
        exp = "";
        for (int i = 0; i < inin.size(); i++) {
            if (inin.get(i).equals("-")) {

                if (inin.get(i - 1).equals(")")) {//是减号
                    exp += "- ";

                } else if (inin.get(i - 1).equals("(")) {
                    exp += "0 - ";
                } else if (isNumber(inin.get(i - 1))) {
                    int cnt = 0, j = 0;
                    for (j = i - 1; j >= 0; j--) {
                        if (isAnFunctionOrOp(inin.get(j), variables, operation_map)) {
                            cnt = Parse.getNumOfPara(inin.get(j), variables, operation_map);
                            break;
                        }
                    }
                    if (j + cnt >= i) {//是负号
                        exp += "-" + inin.get(i + 1) + " ";
                        i++;
                    } else {
                        exp += "- ";
                    }
                } else {//是负号
                    exp += "-" + inin.get(i + 1) + " ";
                    i++;
                }
            } else {
                exp += inin.get(i);
                exp += " ";
            }

        }
        return exp;
    }

    private static boolean isAnFunctionOrOp(String name, Map<String, MuaVariable> variables, Map<String, Integer> operation_map) {

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

    static int isCharExpress(char s) {
        if (s >= '0' && s <= '9') {
            return 1;
        }
        if (s == '+' || s == '-' || s == '*' || s == '/' || s == '%' || s == '(' || s == ')') {
            return 2;
        } else return 0;
    }

    static public boolean isNumber(String str) {//使用正则表达式来判断是否为数字
        String reg = "^[-+]?(([0-9]+)([.]([0-9]+))?|([.]([0-9]+))?)$";
        return str.matches(reg);
    }

    public MuaVariable parsing(List<MuaVariable> grammar_info, Map<String, MuaVariable> variables, Map<String, Integer> operation_map) {
        if (grammar_info.isEmpty()) muaError_handle.parameter_missing();
        MuaVariable temp = grammar_info.get(0);
        String content = temp.raw_value;
        String choice = content;
        if (content.charAt(0) == ':') {
            choice = ":";//针对冒号进行特殊处理
        }
        if (content.length() >= 2 && content.substring(0, 2).equals("//")) {
            choice = "//";
        }
        if (temp.type == EXPRESSION) {
            choice = "expression";
        }
        if (!isFunctionOrOp(choice, variables, operation_map) && !choice.equals("expression")) {
            MuaVariable res = grammar_info.get(0);
            if (!grammar_info.isEmpty()) grammar_info.remove(0);//在grammar_info list中移除已读入的operand
            return res;
        }
        if (!grammar_info.isEmpty()) grammar_info.remove(0);//在grammar_info list中移除已读入的operator

        switch (choice) {//执行相应命令
            case "//":
                return FINISHED;
            case "make": {

                MuaVariable operand_m0 = parsing(grammar_info, variables, operation_map);
                MuaVariable operand_m1 = parsing(grammar_info, variables, operation_map);

                if (operand_m0.raw_value.charAt(0) != '"') {
                    muaError_handle.illeagal_input();

                }
                //System.out.println("???"+operand_m1.raw_value);
                return op_make(operand_m0, operand_m1, variables); //success mua while judge success
            }

            case "thing": {
                MuaVariable operand_t0 = parsing(grammar_info, variables, operation_map);
                if (operand_t0.raw_value.charAt(0) != '"') {
                    if (grammar_info.isEmpty()) muaError_handle.illeagal_input();
                }
                //if (grammar_info.isEmpty()) muaError_handle.parameter_missing();
                return op_thing(operand_t0, variables);

            }

            case "print": {
                if (debug) System.out.println("for print: " + grammar_info.toString());
                if (grammar_info.isEmpty()) muaError_handle.parameter_missing();
                MuaVariable operand_p0 = parsing(grammar_info, variables, operation_map);
                if (debug) System.out.println("debug print ::#" + operand_p0.raw_value);
                return op_print(operand_p0);
            }


            case ":": {
                if (debug) System.out.println("debug 12::" + content);

                String temp2 = content.substring(1, content.length());//为了特殊处理:
                MuaVariable operand_c0 = new MuaVariable(temp2);
                if (debug) {
                    System.out.println(": " + operand_c0.toString());
                }
                return op_colon(operand_c0, variables);
            }

            case "erase": {

                MuaVariable operand_e0 = parsing(grammar_info, variables, operation_map);

                return op_erase(operand_e0, variables);
            }

            case "isname": {

                MuaVariable operand_i0 = parsing(grammar_info, variables, operation_map);
                return op_isname(operand_i0, variables);
            }

            case "read":
                return op_read();

            case "readlinst":
                return op_readlinst();

            case "add":

                return op_arithmetic("add", grammar_info, variables, operation_map);

            case "sub":
                return op_arithmetic("sub", grammar_info, variables, operation_map);
            case "mul":
                return op_arithmetic("mul", grammar_info, variables, operation_map);
            case "div":
                return op_arithmetic("div", grammar_info, variables, operation_map);
            case "mod":
                return op_arithmetic("mod", grammar_info, variables, operation_map);
            case "eq":
                return op_arithmetic("eq", grammar_info, variables, operation_map);
            case "gt":
                return op_arithmetic("gt", grammar_info, variables, operation_map);
            case "lt":
                return op_arithmetic("lt", grammar_info, variables, operation_map);
            case "and":
                return op_arithmetic("and", grammar_info, variables, operation_map);
            case "or":
                return op_arithmetic("or", grammar_info, variables, operation_map);
            case "not": {
                MuaVariable operand_n0 = parsing(grammar_info, variables, operation_map);

                return op_not(operand_n0);
            }
            case "isnumber": {
                MuaVariable operand_is0 = parsing(grammar_info, variables, operation_map);

                return op_isnumber(operand_is0);
            }
            case "isword": {
                MuaVariable operand_is0 = parsing(grammar_info, variables, operation_map);
                return op_isword(operand_is0);
            }
            case "islist": {
                MuaVariable operand_is0 = parsing(grammar_info, variables, operation_map);

                return op_islist(operand_is0);
            }
            case "isbool": {
                MuaVariable operand_is0 = parsing(grammar_info, variables, operation_map);

                return op_isbool(operand_is0);
            }
            case "isempty": {

                MuaVariable operand_is0 = parsing(grammar_info, variables, operation_map);

                return op_isempty(operand_is0, variables);
            }

            case "repeat": {
                MuaVariable operand_r0 = parsing(grammar_info, variables, operation_map);
                MuaVariable operand_r1 = parsing(grammar_info, variables, operation_map);
                return op_repeat(operand_r0, operand_r1, variables, operation_map); //success mua while judge success
            }
            case "item": {
                MuaVariable operand_it0 = parsing(grammar_info, variables, operation_map);
                MuaVariable operand_it1 = parsing(grammar_info, variables, operation_map);
                return op_item(operand_it0, operand_it1, variables); //success mua while judge success
            }
            case "abs": {
                MuaVariable operand_ab0 = parsing(grammar_info, variables, operation_map);

                return op_abs(operand_ab0);
            }
            case "random": {
                MuaVariable operand_ra = parsing(grammar_info, variables, operation_map);
                return op_random(operand_ra);
            }
            case "sqrt": {
                MuaVariable operand_sq = parsing(grammar_info, variables, operation_map);
                return op_sqrt(operand_sq);
            }
            case "int": {
                MuaVariable operand_int = parsing(grammar_info, variables, operation_map);
                return op_int(operand_int);
            }
            case "if": {

                MuaVariable operand_if0 = parsing(grammar_info, variables, operation_map);
                MuaVariable operand_if1 = parsing(grammar_info, variables, operation_map);
                MuaVariable operand_if2 = parsing(grammar_info, variables, operation_map);
                return op_if(operand_if0, operand_if1, operand_if2, variables, operation_map);
            }
            case "word": {
                MuaVariable operand_w0 = parsing(grammar_info, variables, operation_map);
                MuaVariable operand_w1 = parsing(grammar_info, variables, operation_map);
                return op_word(operand_w0, operand_w1);
            }
            case "sentence": {
                MuaVariable operand_s0 = parsing(grammar_info, variables, operation_map);
                MuaVariable operand_s1 = parsing(grammar_info, variables, operation_map);
                return op_sentence(operand_s0, operand_s1);
            }
            case "list": {
                MuaVariable operand_l0 = parsing(grammar_info, variables, operation_map);
                MuaVariable operand_l1 = parsing(grammar_info, variables, operation_map);
                return op_list(operand_l0, operand_l1);
            }
            case "join": {
                MuaVariable operand_j0 = parsing(grammar_info, variables, operation_map);
                MuaVariable operand_j1 = parsing(grammar_info, variables, operation_map);
                return op_join(operand_j0, operand_j1);
            }
            case "first": {
                MuaVariable operand_f0 = parsing(grammar_info, variables, operation_map);
                return op_first(operand_f0);
            }
            case "last": {
                MuaVariable operand_l0 = parsing(grammar_info, variables, operation_map);
                return op_last(operand_l0);
            }
            case "butfirst": {
                MuaVariable operand_b0 = parsing(grammar_info, variables, operation_map);
                return op_butfirst(operand_b0);
            }
            case "butlast": {
                MuaVariable operand_l0 = parsing(grammar_info, variables, operation_map);
                return op_butlast(operand_l0);
            }
            case "wait": {
                MuaVariable operand_w0 = parsing(grammar_info, variables, operation_map);
                return op_wait(operand_w0);
            }
            case "save": {
                MuaVariable operand_s0 = parsing(grammar_info, variables, operation_map);
                return op_save(operand_s0, variables);
            }
            case "load": {
                MuaVariable operand_l0 = parsing(grammar_info, variables, operation_map);
                return op_load(operand_l0, variables);
            }
            case "erall": {
                return op_erall(variables);
            }
            case "poall": {
                return op_poall(variables);
            }
//            case "run": {
//                MuaVariable operand_r0 = new MuaVariable("1");
//                MuaVariable operand_r1 = parsing(grammar_info, variables, operation_map);
//                return op_repeat(operand_r0, operand_r1, variables, operation_map); //success mua while judge success
//            }
            case "output": {
                MuaVariable operand_it0 = parsing(grammar_info, variables, operation_map);
                return op_output(operand_it0, variables, operation_map); //success mua while judge success
            }
            case "stop": {
                //project2.MuaVariable operand_it0 = parsing(grammar_info,variables);
                return STOP; //success mua while judge success
            }
            case "export": {

                return EXPORT;
            }

            case "expression": {
                //MuaVariable res = grammar_info.get(0);
                return op_expression(temp, variables, operation_map);
            }
            default: {//function
                MuaVariable raw_args = op_item(new MuaVariable("0"), variables.get(choice), variables);//获得参数列表
                MuaVariable raw_func = op_item(new MuaVariable("1"), variables.get(choice), variables);//获得操作列表

                Map<String, MuaVariable> func_variables = new HashMap<>();//存储函数的局部变量的hash map
                String args_s = raw_args.toString().substring(1, raw_args.toString().length() - 1);
                //System.out.println("???:"+args_s);
                String[] arg_array = args_s.trim().split(" ");
                //if (grammar_info.isEmpty()) System.out.println("wic");

                //func_variables.put(choice,variables.get(choice));
                List<MuaVariable> args_input_value = new ArrayList<MuaVariable>();
                if (args_s.isEmpty()) {//如果没有参数 不进行对func_variables的操作

                } else {
                    for (int i = 0; i < arg_array.length; i++) {
                        args_input_value.add(parsing(grammar_info, variables, operation_map));//从grammar_info中解析出进行函数调用时，输入的参数的具体的值
                        func_variables.put(arg_array[i], args_input_value.get(i));//将变量与变量的值插入局部变量的hash map中
                        //递归的话，应该要想将函数本身加入自己的func_variables中
                    }
                }


                Iterator iter = variables.entrySet().iterator();
                while (iter.hasNext()) {
                    //加入全局函数 包括自己
                    Map.Entry entry = (Map.Entry) iter.next();
                    Object key = entry.getKey();
                    if (variables.get(key).type == LIST && variables.get(key).toList().size() == 2) {
                        func_variables.put((String) key, variables.get(key));
                    }
                }
                func_variables.put("__return_value__", UNINIT);
                Info func_stmt = new Info();
                func_stmt.get_info(raw_func.toString().substring(1, raw_func.toString().length() - 1), func_variables);//处理要运行的操作语句，便于后面的解析


                //System.out.println("??"+raw_func.toString().substring(1,raw_func.toString().length()-1));
               TAG: while (!func_stmt.result.isEmpty()) {
                   if (debug)System.out.println("debug function:" + func_stmt.result);
                    Parse parser = new Parse(func_stmt.result);//用处理过的输入 初始化语法解释器
                    MuaVariable res;
                    res = parser.parsing(func_stmt.result, func_variables, operation_map);//获得解释的结果

                    Info func_stmt_2 = new Info();
                    func_stmt_2.get_info(res.raw_value, func_variables);//处理要运行的操作语句，便于后面的解析
                    while (!res.raw_value.equals("FINISHED")||!func_stmt_2.result.isEmpty()) {
                        if (debug) System.out.println("debug function in while:" + func_stmt_2.result);
                        res = parser.parsing(func_stmt_2.result, func_variables, operation_map);//获得解释的结果
                        if (res.toString().equals("EXPORT")) {
                            //export the variables in function to the main interpreter
                            Iterator iter2 = func_variables.entrySet().iterator();
                            while (iter2.hasNext()) {
                                //加入全局函数 包括自己
                                Map.Entry entry2 = (Map.Entry) iter2.next();
                                Object key = entry2.getKey();
                                variables.put((String) key,func_variables.get(key));
                            }
                            res.raw_value = FINISHED.raw_value;
                        }
                        if (res.toString().equals("STOP")) {
                            res.raw_value = FINISHED.raw_value;
                            break TAG;//不再执行后续的函数
                        }


                    }


                    if (res.toString().equals("STOP")) {
                        //System.out.println("??");
                        break;
                    }

                   if (debug)System.out.println("debug function++" + res);
                    if (!isAnOp(res.toString(), operation_map) && !isAnFunction(res.toString(), variables) && !res.toString().equals("FINISHED"))
                        parser.muaError_handle.command_not_found("paese" + res.toString());//遇到不符合标准的返回值 进行错误处理
                }
                if (debug)System.out.println("debug function returen " + func_variables.get("__return_value__"));
                if (func_variables.get("__return_value__").toString().equals("__UnInit__")) {//函数没有返回值
                    return FINISHED;
                } else return func_variables.get("__return_value__");//返回相应的值


            }//make "a add :a 1

        }
    }

    private MuaVariable op_erall(Map<String, MuaVariable> variables) {
        variables.clear();
        return FINISHED;
    }

    private MuaVariable op_poall(Map<String, MuaVariable> variables) {
        Iterator iter = variables.entrySet().iterator();
        while (iter.hasNext()) {

            Map.Entry entry = (Map.Entry) iter.next();
            Object key = entry.getKey();
            System.out.println(key);

        }
        return FINISHED;
    }

    private MuaVariable op_load(MuaVariable operand_l0, Map<String, MuaVariable> variables) {
        String filename = operand_l0.raw_value;
        File myFile = new File(filename);
        if (!myFile.exists()) {
            System.err.println("Can't Find " + filename);
        }
        //打开文件
        try {
            BufferedReader in = new BufferedReader(new FileReader(myFile));
            String str;
            //读取一行
            int cut = 0;
            while ((str = in.readLine()) != null) {
                for (int i =0;i<str.length()-1;i++){
                    if (str.charAt(i)=='-'&&str.charAt(i+1)=='-'){
                        cut =i;break;
                    }
                }
                String v_name = str.substring(0,cut);
                String v_raw_value = str.substring(cut+2,str.length());
                variables.put(v_name,new MuaVariable(v_raw_value));
            }
            in.close();
        } catch (IOException e) {
            e.getStackTrace();
        }
        return FINISHED;
    }

    private MuaVariable op_save(MuaVariable operand_s0, Map<String, MuaVariable> variables) {
        try {
            // 创建文件对象
            String filename = operand_s0.raw_value;
            File fileText = new File(filename);
            FileWriter fileWriter = new FileWriter(fileText);

            // 遍历哈希表写文件
            Iterator iter = variables.entrySet().iterator();
            while (iter.hasNext()) {

                Map.Entry entry = (Map.Entry) iter.next();
                Object key = entry.getKey();
                fileWriter.write(key + "--" + variables.get(key).raw_value + "\n");//使用-- 分割变量名和值

            }

            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return FINISHED;

    }

    private MuaVariable op_wait(MuaVariable operand_w0) {
        try {
            Thread.sleep((long) operand_w0.toDouble());                 //1000 毫秒，也就是1秒.
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
        return FINISHED;
    }

    private MuaVariable op_butlast(MuaVariable operand_l0) {
        if (operand_l0.type == WORD) {
            return new MuaVariable("\"" + operand_l0.toWord().substring(0, operand_l0.toWord().length() - 1));//记得添加引号 让Word的返回值统一
        } else {//list
            String list_str = "";
            List<MuaVariable> l0_list = operand_l0.toList();
            for (int i = 0; i < l0_list.size() - 1; i++) {
                list_str += l0_list.get(i) + " ";
            }
            String res = "[ " + list_str + " ]";
            return new MuaVariable(res);
        }
    }

    private MuaVariable op_butfirst(MuaVariable operand_b0) {
        if (operand_b0.type == WORD) {
            return new MuaVariable("\"" + operand_b0.toWord().substring(1, operand_b0.toWord().length()));//记得添加引号 让Word的返回值统一
        } else {//list
            String list_str = "";
            List<MuaVariable> b0_list = operand_b0.toList();
            for (int i = 1; i < b0_list.size(); i++) {
                list_str += b0_list.get(i) + " ";
            }
            String res = "[ " + list_str + " ]";
            return new MuaVariable(res);
        }
    }

    private MuaVariable op_first(MuaVariable operand_f0) {
        if (operand_f0.type == WORD) {
            if (debug) System.out.println("debug first:" + operand_f0.raw_value + " ");
            return new MuaVariable("\"" + operand_f0.toWord().substring(0, 1));//记得添加引号 让Word的返回值统一
        } else {//list
            if (operand_f0.toList().get(0).type == WORD) return new MuaVariable("\"" + operand_f0.toList().get(0));//记得添加引号 让Word的返回值统一
            else if (operand_f0.toList().get(0).type == LIST) return new MuaVariable("[" + operand_f0.toList().get(0) + "]");//记得添加括号 让list的返回值统一
            else return operand_f0.toList().get(0);
        }
    }

    private MuaVariable op_last(MuaVariable operand_l0) {
        if (operand_l0.type == WORD) {

            return new MuaVariable("\"" + operand_l0.toWord().substring(operand_l0.toWord().length() - 1));//记得添加引号 让Word的返回值统一
        } else {//list
            List<MuaVariable> to_list = operand_l0.toList();
            if (to_list.get(to_list.size() - 1).type == WORD) return new MuaVariable("\"" + operand_l0.toList().get(to_list.size() - 1));//记得添加引号 让Word的返回值统一
            else if (operand_l0.toList().get(0).type == LIST) return new MuaVariable("[" + operand_l0.toList().get(to_list.size() - 1) + "]");//记得添加括号 让list的返回值统一
            else return to_list.get(to_list.size() - 1);
        }
    }

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

    private MuaVariable op_list(MuaVariable operand_l0, MuaVariable operand_l1) {
        String l0 = "", l1 = "";
        if (operand_l0.type == WORD) {
            l0 = operand_l0.toWord();
        } else if (operand_l0.type == LIST) {
            l0 = operand_l0.raw_value;
        } else l0 = operand_l0.raw_value;
        //System.out.println("debug list: " + operand_l0.raw_value + " " + operand_l1.raw_value);
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
        if (debug)System.out.println("debug sentence: " + operand_s0.raw_value + " " + operand_s1.raw_value);
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
    private MuaVariable op_abs(MuaVariable operand_ab0) {
        double res = operand_ab0.toDouble();
        if (res < 0) return new MuaVariable(String.valueOf(res * -1));
        else return operand_ab0;
    }


    private MuaVariable op_if(MuaVariable operand_if0, MuaVariable operand_if1, MuaVariable operand_if2, Map<String, MuaVariable> variables, Map<String, Integer> operation_map) {
        if (operand_if0.type != BOOL) {
            muaError_handle.illeagal_input();
            return FINISHED;
        }
        String case1 = operand_if1.raw_value.substring(1, operand_if1.raw_value.length() - 1);
        //System.out.println("debug case1:" + case1);
        String case2 = operand_if2.raw_value.substring(1, operand_if2.raw_value.length() - 1);
        //System.out.println("debug case2:" + case2);
        List<MuaVariable> if_buf = new ArrayList<>();

        if (operand_if0.toBoolean()) {
            if (case1.isEmpty()) return FINISHED;
            else {
                Info if_stmt_1 = new Info();
                if_stmt_1.get_info(case1, variables);

                //System.out.println("debug if_buf:" + if_buf);
                return parsing(if_stmt_1.result, variables, operation_map);
            }
        } else {
            if (case2.isEmpty()) return FINISHED;
            else {
                Info if_stmt_2 = new Info();
                if_stmt_2.get_info(case2, variables);

                return parsing(if_stmt_2.result, variables, operation_map);
            }
        }
    }

    private MuaVariable op_word(MuaVariable operand_w0, MuaVariable operand_w1) {
        if (operand_w0.type != WORD) {
            muaError_handle.illeagal_input();
            return FINISHED;
        } else {

            String res;
            if (operand_w1.type != WORD) res = operand_w0.raw_value + operand_w1.raw_value;
            else res = operand_w0.raw_value + operand_w1.raw_value.substring(1, operand_w1.raw_value.length());//toString没写好的锅
            return new MuaVariable(res);
        }
    }



    private MuaVariable op_int(MuaVariable operand_int) {
        double res = Math.floor(operand_int.toDouble());
        return new MuaVariable(String.valueOf(res));
    }

    private MuaVariable op_random(MuaVariable operand_ra) {
        Random r = new Random();

        double res = r.nextDouble() * operand_ra.toDouble();
        return new MuaVariable(String.valueOf(res));
    }

    private MuaVariable op_sqrt(MuaVariable operand_sq) {
        double res = Math.sqrt(operand_sq.toDouble());
        return new MuaVariable(String.valueOf(res));
    }

    private MuaVariable op_isempty(MuaVariable operand_is0, Map<String, MuaVariable> variables) {
        //check type
        String name = operand_is0.raw_value;
        int type = operand_is0.type;
        if (type != WORD && type != LIST) {//是否合法输入
            //System.out.println("debug isempty?:#"+operand_is0.raw_value+" "+operand_is0.type);
            muaError_handle.illeagal_input();
            return FINISHED;
        } else {
            //System.out.println("debug isempty:#"+operand_is0.raw_value+" "+operand_is0.type);
            if (type == WORD) {
                if (operand_is0.toString().length() == 1) return TRUE;//只有一个标识字面量的引号
                else return FALSE;
            } else {
                if (operand_is0.toList().size() == 0) return TRUE;
                else return FALSE;
            }
        }
    }

    private MuaVariable op_isbool(MuaVariable operand_is0) {
        //check type
        if (operand_is0.type == BOOL) {//是否是bool
            return TRUE;
        } else {
            return FALSE;
        }
    }

    private MuaVariable op_islist(MuaVariable operand_is0) {
        //check type
        if (debug)System.out.println("debuglist: d" + operand_is0.raw_value);
        if (operand_is0.type == LIST) {//是否是列表
            return TRUE;
        } else {
            return FALSE;
        }
    }

    private MuaVariable op_isword(MuaVariable operand_is0) {
        //check type
        if (operand_is0.type == WORD) {//是否是word
            return TRUE;
        } else {
            return FALSE;
        }
    }

    private MuaVariable op_isnumber(MuaVariable operand_n0) {
        //check type
        if (operand_n0.type == NUM) {//是否是数字
            return TRUE;
        } else {
            return FALSE;
        }
    }

    private MuaVariable op_output(MuaVariable operand_it0, Map<String, MuaVariable> variables, Map<String, Integer> operation_map) {
        if (debug) System.out.println("debug output: " + operand_it0.raw_value);
        List<MuaVariable> output_buf = new ArrayList<>();
        output_buf.add(operand_it0);
        if (debug)System.out.println("debug outpu2: " + output_buf.toString());
        variables.put("__return_value__", operand_it0);
        return FINISHED;
    }


    private MuaVariable op_item(MuaVariable operand_it0, MuaVariable operand_it1, Map<String, MuaVariable> variables) {
        if (operand_it0.type != NUM) {
            System.out.println("The first parameter must be a number!");
            muaError_handle.illeagal_input();
        }
        if (operand_it1.type != LIST && operand_it1.type != WORD) {
            System.out.println("The second parameter must be a list or word!");
            muaError_handle.illeagal_input();
        }
        //System.out.println(operand_it1.raw_value.trim());
        if (operand_it0.toDouble() < 0 || operand_it0.toDouble() > operand_it1.toList().size()) {
            muaError_handle.algorithm_error();
        }
        MuaVariable res = operand_it1.toList().get((int) operand_it0.toDouble());
        //System.out.println(res.toString());


        return res;
    }

    private MuaVariable op_repeat(MuaVariable operand_r0, MuaVariable operand_r1, Map<String, MuaVariable> variables, Map<String, Integer> operation_map) {
        if (operand_r0.type != NUM) {
            System.out.println("The first parameter must be a number!");
            muaError_handle.illeagal_input();
        }
        if (operand_r1.type != LIST) {
            System.out.println("The second parameter must be a list!");
            muaError_handle.illeagal_input();
        }
        //System.out.println(operand_r1.raw_value);

        for (double i = 0; i < operand_r0.toDouble(); i++) {
            //System.out.println(operand_r1.toList().toString());
            parsing(operand_r1.toList(), variables, operation_map);
        }
        return FINISHED;
    }

    public MuaVariable op_make(MuaVariable operand0, MuaVariable operand1, Map<String, MuaVariable> variables) {
        String temp = operand0.toString();
        //System.out.println("debug make:"+temp);
        String v_name;
        if (temp.charAt(0) != '"') {
            System.out.println("There must be a \" before var name");
            return FINISHED;
        } else {
            v_name = temp.substring(1, temp.length());//从1开始，忽略"
        }
        String raw_value = operand1.toString();


        String v_value;
        if (raw_value.charAt(0) == '[') {
            if (raw_value.charAt(raw_value.length() - 1) != ']') {
                muaError_handle.illeagal_input();
            }
            v_value = raw_value;//针对list 忽略开头结尾的[]
        } else {
            v_value = raw_value;
        }

        MuaVariable temp_value = new MuaVariable(v_name, v_value);
        if (temp_value.type == WORD && operand1.toString().charAt(0) != '"') {
            System.out.println("There must be a \" before word value!");
            muaError_handle.illeagal_input();
        }
        variables.put(v_name, temp_value);
        if (debug) System.out.println("haha,v name is " + v_name + " hiehie v_value is " + variables.get(v_name));
        return FINISHED;
    }

    public MuaVariable op_thing(MuaVariable operand0, Map<String, MuaVariable> variables) {
        String o_v_name = operand0.toString();

        String v_name;
        if (o_v_name.charAt(0) == '"') {
            v_name = o_v_name.substring(1, o_v_name.length());
        } else {
            System.out.println("Wrong format. You may lose the \'\"\'");
            return FINISHED;
        }
        boolean flag = variables.containsKey(v_name);
        if (!flag) {
            muaError_handle.variable_missing();
        }
        //不用[]来索引列表.....
//            int index=-1;
//            int begin = thing_container.toString().indexOf("["),end = thing_container.toString().indexOf("]");
//            if (begin!=-1){
//                index = Integer.valueOf(thing_container.toString().substring(begin+1,end));
//            }
//            if (variables.get(v_name).type==LIST&&index!=-1&&index<variables.get(v_name).toList().size()){
//                v_value = (variables.get(v_name).toList()).get(index);
//            }
        if (variables.get(v_name).type == LIST) {//处理thing一个list 直接返回给解释器的情况
            //System.out.println("deb:"+variables.get(v_name).raw_value);
            //return variables.get(v_name);
            return new MuaVariable(variables.get(v_name).raw_value);
        } else return variables.get(v_name);

    }

    public MuaVariable op_colon(MuaVariable operand0, Map<String, MuaVariable> variables) {
        String v_name = operand0.toString();
        if (debug)System.out.println("debug ::" + v_name);
        boolean flag = variables.containsKey(v_name);//判断是否存在该变量
        if (!flag) {
            muaError_handle.variable_missing();
            return FINISHED;
        }
        if (variables.get(v_name).type == LIST) {
            return new MuaVariable(variables.get(v_name).raw_value);
        } else return variables.get(v_name);
    }

    public MuaVariable op_erase(MuaVariable o_v_name, Map<String, MuaVariable> variables) {


        String v_name;
        if (o_v_name.toString().charAt(0) == '"') {
            v_name = o_v_name.toString().substring(1, o_v_name.toString().length());
        } else v_name = o_v_name.toString();
        if (variables.containsKey(v_name)) {//判断是否存在该变量
            variables.remove(v_name);
            System.out.println(v_name + " has been erased");
        } else muaError_handle.variable_missing();
        return FINISHED;
    }

    public MuaVariable op_isname(MuaVariable o_v_name, Map<String, MuaVariable> variables) {
        if (debug)System.out.println("debug isnmae");
        String v_name;
        if (o_v_name.toString().charAt(0) == '"') {
            v_name = o_v_name.toString().substring(1, o_v_name.toString().length());
        } else v_name = o_v_name.toString();
        if (variables.containsKey(v_name)) {
            //System.out.println("true");
            return TRUE;
        }
        //System.out.println("false");
        return FALSE;
    }

    public MuaVariable op_print(MuaVariable operand0) {

        if (debug)System.out.println("debug print + " + operand0.raw_value);
        String res = operand0.toString();

        String v_name = operand0.toString().substring(1, operand0.toString().length());//qwe
        if (operand0.type == WORD) {
            System.out.println(res.substring(1, res.length()));//忽略第一个用于字面量标识的引号
            return FINISHED;
        }
        if (operand0.type == LIST) {
            System.out.println(res.substring(1, res.length() - 1).trim());//忽略list的中括号 和前后的空格
            return FINISHED;
        } else {
            //如果一个变量的值不是另一个变量的名字：make "b "a
            System.out.println(res);
        }
        return FINISHED;


    }

    public MuaVariable op_read() {
        Scanner thisin = new Scanner(System.in);
        String input = thisin.next();
        return new MuaVariable(input);
    }

    public MuaVariable op_readlinst() {
        Scanner thisin = new Scanner(System.in);
        return new MuaVariable(thisin.nextLine());
    }

    public MuaVariable op_arithmetic(String op, List<MuaVariable> result, Map<String, MuaVariable> variables, Map<String, Integer> operation_map) {
        if (result.isEmpty()) muaError_handle.parameter_missing();
        if (result.isEmpty()) muaError_handle.parameter_missing();
        MuaVariable operand_ar0 = parsing(result, variables, operation_map);
        if (result.isEmpty()) muaError_handle.parameter_missing();
        MuaVariable operand_ar1 = parsing(result, variables, operation_map);

        switch (op) {
            case "add":
                return op_add(operand_ar0, operand_ar1);
            case "sub":
                return op_sub(operand_ar0, operand_ar1);
            case "mul":
                return op_mul(operand_ar0, operand_ar1);
            case "div":
                return op_div(operand_ar0, operand_ar1);
            case "mod":
                return op_mod(operand_ar0, operand_ar1);
            case "eq":
                return op_eq(operand_ar0, operand_ar1);
            case "gt":
                return op_gt(operand_ar0, operand_ar1);
            case "lt":
                return op_lt(operand_ar0, operand_ar1);
            case "and":
                return op_and(operand_ar0, operand_ar1);
            case "or":
                return op_or(operand_ar0, operand_ar1);
        }

        return operand_ar0;
    }

    public MuaVariable op_add(MuaVariable a, MuaVariable b) {
//        if (a.type != NUM || b.type != NUM) {
//            muaError_handle.illeagal_input();
//            return FINISHED;
//        }

        double sum = a.toDouble() + b.toDouble();
        MuaVariable res = new MuaVariable(String.valueOf(sum));
        return res;
    }

    public MuaVariable op_sub(MuaVariable a, MuaVariable b) {
//        if (a.type != NUM || b.type != NUM) {
//            muaError_handle.illeagal_input();
//            return FINISHED;
//        }
        if (debug)System.out.println("Debug sub :" + a.raw_value + "  " + b.raw_value);

        double minus = a.toDouble() - b.toDouble();
        MuaVariable res = new MuaVariable(String.valueOf(minus));
        return res;
    }

    public MuaVariable op_mul(MuaVariable a, MuaVariable b) {
//        if (a.type != NUM || b.type != NUM) {
//            muaError_handle.illeagal_input();
//            return FINISHED;
//        }
        double num1 = a.toDouble();
        double num2 = b.toDouble();
        double product = num1 * num2;
        MuaVariable res = new MuaVariable(String.valueOf(product));
        return res;
    }

    public MuaVariable op_div(MuaVariable a, MuaVariable b) {
//        if (a.type != NUM || b.type != NUM) {
//            muaError_handle.illeagal_input();
//            return FINISHED;
//        }
        double num1 = a.toDouble();
        double num2 = b.toDouble();
        if (num2 == 0) muaError_handle.algorithm_error();
        double quo = num1 / num2;
        MuaVariable res = new MuaVariable(String.valueOf(quo));
        return res;
    }

    public MuaVariable op_mod(MuaVariable a, MuaVariable b) {
        if (a.type != NUM || b.type != NUM) {
            muaError_handle.illeagal_input();
            return FINISHED;
        }
        double num1 = a.toDouble();
        double num2 = b.toDouble();
        if (num2 == 0) muaError_handle.algorithm_error();
        double rem = num1 % num2;
        MuaVariable res = new MuaVariable(String.valueOf(rem));
        return res;
    }

    public MuaVariable op_eq(MuaVariable a, MuaVariable b) {
        if (a.type == NUM && b.type == NUM) {
            double aa = a.toDouble();
            double bb = b.toDouble();
            if (aa == bb) return new MuaVariable("true");
            else return new MuaVariable("false");
        } else {
//            System.out.println("sd"+a.toString());
            if (a.toString().equals(b.toString())) return TRUE;
            else return FALSE;
        }
        //mua对类型的要求？
//        if (a.type==WORD&&b.type==WORD){
//            if (a.toString().equals(b.toString())) return TRUE;
//            else return FALSE;
//        }
//
//        System.out.println("The variable type doesn't match!");
//        muaError_handle.illeagal_input();
        //return FINISHED;
    }

    public MuaVariable op_gt(MuaVariable a, MuaVariable b) {
        //check type
        if ((a.type == NUM && b.type == NUM)) {//如果都是数字 按照数字大小比较
            double aa = a.toDouble();
            double bb = b.toDouble();
            if (aa > bb) return new MuaVariable("true");
            else return new MuaVariable("false");
        } else {//否则按照字符串比较
            if (a.toString().compareTo(b.toString()) > 0) return TRUE;
            else return FALSE;
        }

    }

    public MuaVariable op_lt(MuaVariable a, MuaVariable b) {
        if ((a.type == NUM && b.type == NUM)) {
            double aa = a.toDouble();
            double bb = b.toDouble();
            if (aa < bb) return new MuaVariable("true");
            else return new MuaVariable("false");
        } else {
            if (a.toString().compareTo(b.toString()) < 0) return TRUE;
            else return FALSE;
        }

    }

    public MuaVariable op_and(MuaVariable a, MuaVariable b) {
        if (a.type != BOOL || b.type != BOOL) {
            muaError_handle.illeagal_input();
            return FINISHED;
        }

        if (a.toBoolean() && b.toBoolean()) return TRUE;
        else return FALSE;

    }

    public MuaVariable op_or(MuaVariable a, MuaVariable b) {
        if (a.type != BOOL || b.type != BOOL) {
            muaError_handle.illeagal_input();
            return FINISHED;
        }
        if (!a.toBoolean() && !b.toBoolean()) return FALSE;
        else return TRUE;

    }

    public MuaVariable op_not(MuaVariable a) {
        if (a.type != BOOL) {
            muaError_handle.illeagal_input();
            return FINISHED;
        }
        if (a.toBoolean()) return FALSE;
        else return TRUE;

    }

    private MuaVariable op_expression(MuaVariable temp, Map<String, MuaVariable> variables, Map<String, Integer> operation_map) {
        List<MuaVariable> temp_input = new ArrayList<MuaVariable>();
        String exp = reformatExpression(temp.raw_value, variables, operation_map);
//        int pos_e = -1;
//        for (int i=0;i<exp.length();i++){
//            if (exp.charAt(i)=='E'){
//                pos_e = i;
//            }
//        }
//        pos_e=-1;
//        if (pos_e>0) {
//            exp = exp.substring(0,pos_e+1)+exp.substring(pos_e+2,exp.length());
//            System.out.println("debug pos e"+exp);
//            for (int i=0;i<exp.length();i++){
//                if (exp.charAt(i)=='E'){
//                    pos_e = i;
//                }
//            }
//            System.out.println("debug pos 1.5"+exp);
//            int begin=0,end=exp.length();
//            for (int i=pos_e;i>=0;i--){
//                if (exp.charAt(i)==' ') begin =i;
//            }
//            for (int i=pos_e-2;i<exp.length();i++){
//                if (exp.charAt(i)==' ') {
//                    //System.out.println("debug pos bab"+exp);
//                    end =i;
//                    break;
//                }
//            }
//            String science_e = exp.substring(begin+1,end);
//            System.out.println("debug pos 2"+science_e);
//            BigDecimal bd = new BigDecimal(science_e);
//            System.out.println("debug 2.5 "+bd.toPlainString());
//            exp = exp.substring(0,begin+1)+bd.toPlainString().substring(0,bd.toPlainString().length()-5)+exp.substring(end,exp.length());
//
//
//        }
        if (debug)System.out.println("debug raw" + exp);
        //System.out.println("exp raw:"+temp.raw_value);
        String new_exp = "";
        String[] exp_arr = exp.split(" ");
        if (!consistsFunc(exp, variables, operation_map)) {
            temp_input = new ArrayList();
            MidToPrefux mid_to_pre = new MidToPrefux(exp.replaceAll("\\s*", ""), variables, operation_map);

            String temp_res = mid_to_pre.getResult();
            //System.out.println("??:"+temp.raw_value.replaceAll("\\s*",""));
            String[] temp_in = temp_res.trim().split(" ");

            for (String ret : temp_in) {
                temp_input.add(new MuaVariable(ret));//加入到test中
            }

            return parsing(temp_input, variables, operation_map);
        } else {
            for (int i = exp_arr.length - 1; i >= 0; i--) {
                if (isFunctionOrOp(exp_arr[i], variables, operation_map)) {
                    int args_num = getNumOfPara(exp_arr[i], variables, operation_map);//may bug
                    String next_input = "";

                    next_input += exp_arr[i] + " ";
                    int cnt = 0, flag = 0;
                    String betwbrack = "";// ( 1 + 6 )//f 1  ( 2 + 3 )
                    for (int j = i + 1, num = 0; j < exp_arr.length && num < args_num; j++) {
                        if (exp_arr[j].equals("(") && flag == 0) {
                            //betwbrack+=exp_arr[j]+" ";
                            cnt++;
                        }
                        if (exp_arr[j].equals(")")) {
                            betwbrack += exp_arr[j];
                            cnt--;
                            if (cnt == 0) {
                                flag = 1;
                            }
                        }
                        if (cnt == 0 && flag == 0) {
                            next_input += exp_arr[j] + " ";
                            num++;
                        } else if (cnt == 0 && flag == 1) {
                            next_input += betwbrack + " ";
                            betwbrack = "";
                            flag = 0;
                            num++;
                        } else {
                            betwbrack += exp_arr[j] + " ";
                        }
                        ;
                        exp_arr[j] = "";
                    }
                    Info test = new Info();
                    //System.out.println("???:"+next_input);
                    test.get_info(next_input, variables);

                    MuaVariable res = parsing(test.result, variables, operation_map);
                    exp_arr[i] = res.toString();
                    for (String ans : exp_arr) {
                        new_exp += ans + " ";
                    }
                    break;
                }

            }
            return op_expression(new MuaVariable(new_exp), variables, operation_map);
        }
    }

    boolean consistsFunc(String exp, Map<String, MuaVariable> variables, Map<String, Integer> operation_map) {
        String[] exp_arr = exp.split(" ");
        for (int i = 0; i < exp_arr.length; i++) {
            if (isFunctionOrOp(exp_arr[i], variables, operation_map)) {
                return true;
            }
        }
        return false;
    }

    static boolean isFunctionOrOp(String name, Map<String, MuaVariable> variables, Map<String, Integer> operation_map) {

        if (isAnFunction(name, variables)) {
            return true;
        }
        if (isAnOp(name, operation_map)) {
            return true;
        } else {
            return false;
        }
    }

    static public boolean isAnFunction(String name, Map<String, MuaVariable> variables) {
        if (variables.containsKey(name) && variables.get(name).raw_value.length() > 2 && variables.get(name).type == LIST && variables.get(name).toList().size() == 2) {
            return true;
        } else return false;
    }

    static public boolean isAnOp(String name, Map<String, Integer> operation_map) {
        if (operation_map.containsKey(name)) {
            return true;
        } else {
            return false;
        }
    }


}
