import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/**
 * packagename: project2
 * author: ken
 * created in: 2017/11/30
 * QAQ
 */
public class Info {
    int debug = 0;

    String raw_input;
    List<MuaVariable> result;

    Info() {
        raw_input = "";
        result = new ArrayList<MuaVariable>();
    }


    public void get_info(String input, Map<String, MuaVariable> variables) {


        raw_input = input;
        //System.out.print("Debug info+"+raw_input);
        int begin = 0, end = 0, cursor = 0;

        for (int i = 0; i < raw_input.length(); ) {
            if (raw_input.charAt(i) == ' ') {
                if (debug == 1) {
                    System.out.print(raw_input.charAt(i));
                }
                i++;

                continue;
            } else {
                begin = i;
            }
            int j = 0;
            int flag = 0;//用来判断是否需要补上]
            //多个[]的匹配问题应该用堆栈
            if (raw_input.charAt(begin) == '[') {

                int count_left = 1;
                int count = begin+1;
                while (count_left!=0&&count<raw_input.length()){
                    if (raw_input.charAt(count)==']') count_left--;
                    if (raw_input.charAt(count)=='[') count_left++;
                    count++;
                }

                j= end = count;
                flag = 1;

            } else if (raw_input.charAt(begin) == '(') {
                int count_left_small = 1;
                int count = begin+1;
                while (count_left_small!=0&&count<raw_input.length()){
                    if (raw_input.charAt(count)==')') count_left_small--;
                    if (raw_input.charAt(count)=='(') count_left_small++;
                    count++;
                }

                j= end = count;
                flag = 2;
            } else {
                for (j = begin; j < raw_input.length(); j++) {
                    if (raw_input.charAt(j) == ' ') {
                        end = j;
                        break;
                    }
                }
            }
            if (j == raw_input.length()) end = j;
            if (end < begin) end = raw_input.length() - 1;
            String temp;

            temp = raw_input.substring(begin, end);

            result.add(new MuaVariable(temp));


            i = end + 1;

        }
    }


    public void addParameters(String in) {
        result.add(new MuaVariable(in));
    }


}