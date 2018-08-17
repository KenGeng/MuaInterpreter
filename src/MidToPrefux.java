import java.math.BigDecimal;
import java.util.Map;
import java.util.Stack;
import java.util.Vector;

/**
 * packagename: project2
 * author: ken
 * created in: 2017/11/30
 * QAQ
 */
public class MidToPrefux {
    private static boolean debug = false;
    String result;
    String input;
    Map<String, MuaVariable> variables;
    Map<String, Integer> operation_map;

    MidToPrefux(String in, Map<String, MuaVariable> in_variables ,Map<String, Integer> in_operation_map ) {
        input = in;
        variables = in_variables;
        operation_map = in_operation_map;
        result = midToPrefix(in, variables,in_operation_map);

    }

    //我讨厌科学计数法…………………………
    static  String deleteE(String input){
        int pos_dot,pos_e;
        pos_dot=pos_e=-1;
        for (int i =0;i<input.length();i++){
            if (input.charAt(i)=='E') pos_e=i;
            if (input.charAt(i)=='.') pos_dot=i;
        }
        String expo_str = input.substring(pos_e+1,input.length());
        int expo = Integer.valueOf(expo_str);
        if (pos_dot==-1){
            System.out.println("绝望");
        }
        String res="";
        if (expo<0){
            res+="0.";
            for (int i=1;i<-expo;i++){
                res+="0";
            }
            for (int j=0;j<input.length();j++){
                if (input.charAt(j)=='E') break;
                if (input.charAt(j)!='.') res+=input.charAt(j);
                else continue;
            }
        }else {
            for (int j=0;j<input.length();j++){
                if (input.charAt(j)=='E') break;
                if (input.charAt(j)!='.') res+=input.charAt(j);
                else continue;
            }
            for (int i=0;i<expo-(pos_e-pos_dot);i++){
                res+="0";
            }

        }
        return res;

    }

    public static String midToPrefix(String raw_cal, Map<String, MuaVariable> variables,Map<String, Integer> in_operation_map) {
        Stack<algo_operator> operator_stack = new Stack<>();
        if (debug)System.out.println("debug mid raw call"+raw_cal);
        StringBuilder res_no = new StringBuilder();

        StringBuilder pre = new StringBuilder(raw_cal);
        String inin = "";

        int pos_e= -1;
        int begin_e=0;int end_e = pre.length();
        Vector<Integer> begin_ee=new Vector<>();
        Vector<Integer> end_ee=new Vector<>();
        Vector<Integer> pos_ee = new Vector<>();
        Vector<String> remove_e = new Vector<>();
        for (int i=0;i<pre.length();i++){
            if (pre.charAt(i)=='E'){
                pos_ee.add(i);
            }
        }
        int count =0;
        while (count<pos_ee.size()){
            for (int i=pos_ee.get(count);i>=0;i--){
                if (pre.charAt(i) == '+' || pre.charAt(i) == '-' ||pre.charAt(i)== '*' ||pre.charAt(i) == '/' || pre.charAt(i)== '%' || pre.charAt(i) == ')'|| pre.charAt(i) == '('){
                    begin_ee.add(i+1);break;
                }
            }
            if (pre.charAt(pos_ee.get(count)+1)=='-'){
                for (int i=pos_ee.get(count)+2;i<pre.length();i++){

                    if (pre.charAt(i) == '+' || pre.charAt(i) == '-' ||pre.charAt(i)== '*' ||pre.charAt(i) == '/' || pre.charAt(i)== '%' || pre.charAt(i) == ')'){
                        end_ee.add(i);break;
                    }
                }
            }else {
                for (int i=pos_ee.get(count);i<pre.length();i++){

                    if (pre.charAt(i) == '+' || pre.charAt(i) == '-' ||pre.charAt(i)== '*' ||pre.charAt(i) == '/' || pre.charAt(i)== '%' || pre.charAt(i) == ')'){
                        end_ee.add(i);break;
                    }
                }
            }

//            BigDecimal bd = new BigDecimal(pre.substring(begin_e+1,end_e));
//            System.out.println("debug jiaoao"+bd.toPlainString());
//            inin = pre.substring(0,begin_e+1)+bd.toPlainString()+pre.substring(end_e,pre.length());
            remove_e.add(deleteE(pre.substring(begin_ee.get(count),end_ee.get(count))));
            count++;

        }
        for (int i=0;i<remove_e.size();i++){
            if (i==0) {
                inin+=pre.substring(0,begin_ee.get(i))+remove_e.get(i);
                if (debug)System.out.println("debug mid bubutgg"+inin);
            }

           else inin+=pre.substring(end_ee.get(i-1),begin_ee.get(i))+remove_e.get(i);
        }



        if (count==0){
            inin = pre.toString();
        }else  inin+=pre.substring(end_ee.get(count-1),pre.length());

        if (debug) System.out.println("debug mid0 "+pre.toString());
        char[] cal_arr = inin.toCharArray();
        StringBuilder fuc = new StringBuilder(inin);
        for (int i = fuc.length() - 1; i >= 0; i--) {
            if (fuc.charAt(i) == '-') {
                if (i == 0 || fuc.charAt(i - 1) == '(') {//预处理输入 如果是负号 添加0 转化为减号 简化处理
                    fuc.insert(i, '0');
                }
            }
        }
        inin=fuc.toString();
        if (debug) System.out.println("debug mid last"+inin);
        for (int i = cal_arr.length - 1; i >= 0; ) {
            if (cal_arr[i] == '+' || cal_arr[i] == '-' || cal_arr[i] == '*' || cal_arr[i] == '/' || cal_arr[i] == '%' || cal_arr[i] == ')') {

                algo_operator new_op = new algo_operator(String.valueOf(cal_arr[i]), variables,in_operation_map);
                if (operator_stack.isEmpty()) {
                    operator_stack.push(new_op);
                    if (new_op.op.equals(")")) operator_stack.peek().setPriority(-1);
                } else if (new_op.priority >= operator_stack.peek().priority) {
                    operator_stack.push(new_op);
                    if (new_op.op.equals(")")) operator_stack.peek().setPriority(-1);
                } else {
                    while (!operator_stack.isEmpty() && operator_stack.peek().priority > new_op.priority) {
                        res_no.append(operator_stack.pop().mua_op).append(" ");
                    }
                    operator_stack.push(new_op);
                }
                i--;
                continue;
            } else if (cal_arr[i] == '(') {
                while (!operator_stack.peek().op.equals(")")) {
                    res_no.append(operator_stack.pop().mua_op).append(" ");
                }
                operator_stack.pop();//pop 最后的)
                i--;
                continue;
            } else {
                int end, begin = 0;
                end = i;
                res_no.append(cal_arr[i]);
                if (i == 0) res_no.append(" ");
                for (int j = i - 1; j >= 0; j--) {
                    if (cal_arr[j] == '+' || cal_arr[j] == '-' || cal_arr[j] == '*' || cal_arr[j] == '/' || cal_arr[j] == '%' || cal_arr[j] == ')' || cal_arr[j] == '(') {
                        res_no.append(" ");
                        begin = j + 1;
                        break;
                    }

                    res_no.append(cal_arr[j]);
                    if (j == 0) res_no.append(" ");
                }
                //res_no.append(cal_arr.toString().substring(begin, end + 1));
                i = begin - 1;
                continue;
            }

        }
        while (!operator_stack.isEmpty()) {
            res_no.append(operator_stack.pop().mua_op).append(" ");
        }
        String res = "";
        StringBuffer sb = new StringBuffer();
        for (int i = res_no.length() - 1; i >= 0; i--) {
            sb.append(res_no.charAt(i));
        }
        res = sb.toString();
        return res;

    }

    String getResult() {
        return result;
    }

    static class algo_operator {
        String op;
        String mua_op;
        int priority;//+-:0 */:1
        int num_p;

        algo_operator() {
            op = "+";
            mua_op = "add";
            priority = 0;
            num_p = 2;
        }

        algo_operator(String in, Map<String, MuaVariable> variables, Map<String, Integer> operation_map ) {
            op = in;

            if (variables.containsKey(in) && variables.get(in).v_name.isEmpty() && variables.get(in).type == variables.get(in).LIST && variables.get(in).toList().size() == 2) {
                num_p = Parse.getNumOfPara(in,variables,operation_map);
                priority = 1;
                StringBuffer sb = new StringBuffer();
                for (int i = in.length() - 1; i >= 0; i--) {
                    sb.append(in.charAt(i));
                }
                mua_op = sb.toString();

            } else {
                switch (in) {
                    case "+":
                        priority = 0;
                        mua_op = "dda";
                        num_p = 2;
                        break;
                    case "-":
                        priority = 0;
                        mua_op = "bus";
                        num_p = 2;
                        break;
                    case "*":
                        priority = 1;
                        mua_op = "lum";
                        num_p = 2;
                        break;
                    case "/":
                        priority = 1;
                        mua_op = "vid";
                        num_p = 2;
                        break;
                    case "%":
                        priority = 1;
                        mua_op = "dom";
                        num_p = 2;
                        break;
                    case ")":
                        priority = 2;
                        mua_op = "no";
                        num_p = 0;
                        break;
                }

            }

        }

        void setPriority(int in) {
            priority = in;
        }

    }
}
