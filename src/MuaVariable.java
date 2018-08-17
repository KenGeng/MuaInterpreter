import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;


/**
 * packagename: project2
 * author: ken
 * created in: 2017/11/30
 * QAQ
 */
public class MuaVariable {
    //各种变量类型
    final int WORD = 0;
    final int NUM = 1;
    final int BOOL = 2;
    final int LIST = 3;
    final int OP = 4;
    final int EXPRESSION = 5;
    String v_name;//变量名
    String raw_value;//变量原始存储的字符串
    int type;//类型
    private boolean debug = false;

    MuaVariable() {
        v_name = "";
        raw_value = "";
        type = 0;
        set_type();
    }

    MuaVariable(String name, String value) {
        v_name = name;
        raw_value = value;
        set_type();
    }

    MuaVariable(String value) {
        v_name = "";
        raw_value = value;
        set_type();
    }

    MuaVariable(int value) {
        v_name = "";
        raw_value = String.valueOf(value);
        set_type();
    }

    public boolean isNumber(String str) {//使用正则表达式来判断是否为数字

       // BigDecimal bg=new BigDecimal(str);
        String reg = "^[-+]?(([0-9]+)([.]([0-9]+))?|([.]([0-9]+))|([A-Z]+)([0-9]+))$";
        //String temp =  String.valueOf(bg.doubleValue());
        return str.matches(reg);
    }

    public void set_type() {//设置变量类型
        if (isNumber(raw_value)) {
            //System.out.println("raw"+raw_value+" type:num");
            type = NUM;
        } else if (raw_value.equals("true") || raw_value.equals("false")) {
            type = BOOL;
        } else if (raw_value.charAt(0) == '[') {
            //System.out.println("raw"+raw_value+" type:list");
            type = LIST;
        } else if (isStringAnOp(raw_value)) {

            type = OP;
        } else if (raw_value.charAt(0) == '(') {
            type = EXPRESSION;
        } else type = WORD;
        //System.out.println("raw"+raw_value+" type:"+type);
    }

    public boolean isStringAnOp(String in) {//判断是否为Mua的操作符
        char[] for_colon = in.toCharArray();
        if (for_colon[0] == '/' && for_colon[1] == '/') {
            return true;
        }
        if (for_colon[0] == ':') {
            return true;
        }
        if (in.equals("add") || in.equals("sub") || in.equals("mul") || in.equals("div") || in.equals("mod") || in.equals("eq") || in.equals("gt") || in.equals("lt") || in.equals("and") || in.equals("or") || in.equals("not") || in.equals("make") || in.equals("thing") || in.equals(":") || in.equals("erase") || in.equals("isname") || in.equals("print") || in.equals("read") || in.equals("readlinst") || in.equals("repeat") || in.equals("item") || in.equals("output") || in.equals("stop")|| in.equals("isnumber")|| in.equals("isword")|| in.equals("isbool")|| in.equals("islist")|| in.equals("isempty"))
            return true;
            //if (this.toList().size()==2) return true;
        else return false;

    }


    //返回不同类型的变量值
    public String toString() {
        return raw_value;
    }
    public String toWord() {
        return raw_value.substring(1,raw_value.length());
    }//忽略第一个引号

    public double toDouble() {

        if (debug)System.out.println("debug double:"+this.raw_value+"bbb+");

        //if (raw_value.charAt(raw_value.length()-1)=='E') raw_value+="-4";
        BigDecimal bd = new BigDecimal(raw_value);

        double d = Double.valueOf(raw_value);


        //if (temp.length()>10) d = Double.valueOf(temp.substring(0,a2.toPlainString().length()-1));

        return d;
    }

    public boolean toBoolean() {
        return Boolean.valueOf(raw_value);
    }

    public List<MuaVariable> toList() {
        if (debug)System.out.println("debug tolist:"+raw_value);
        //String[] arr = raw_value.trim().split(" ");//要用trim去除掉前后多余的空格
        List<MuaVariable> list = new ArrayList<MuaVariable>();
        String undispose = raw_value.substring(1, raw_value.length() - 1).trim();//去掉最外层的[]
        for (int i = 0; i < undispose.length(); ) {
            int end = 0;
            if (undispose.charAt(i) == ' ') {
                i++;
                continue;
            }
            if (undispose.charAt(i) == '[') {

                int count = 1;
                for (int j = i + 1; j < undispose.length(); j++) {
                    if (undispose.charAt(j) == '[') count++;
                    if (undispose.charAt(j) == ']') {
                        count--;
                        if (count == 0) {
                            end = j;
                            break;
                        }
                    }
                }
                //make "a [ [ a ] [ make sad] ]
                list.add(new MuaVariable(undispose.substring(i, end) + "]"));//end+1因为需要把]也存进去
            } else {
                int flag = 0;
                for (int j = i + 1; j < undispose.length(); j++) {
                    if (undispose.charAt(j) == ' ') {
                        end = j;
                        flag = 1;
                        break;
                    }
                }
                if (flag == 0) end = undispose.length();
                list.add(new MuaVariable(undispose.substring(i, end)));
            }
            i = end + 1;

        }


        return list;
    }


}
