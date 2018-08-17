/**
 * packagename: project2
 * author: ken
 * created in: 2017/11/30
 * QAQ
 */
public class MuaError {

    public void command_not_found(String command) {
        System.out.println("MuaInterpreter Error: command not found:" + command);
        System.exit(0);
    }

    public void parameter_missing() {
        System.out.println("MuaInterpreter Error: No enough parameters!");
        System.exit(0);
    }

    public void variable_missing() {
        System.out.println("MuaInterpreter Error: No such variable!");
        System.exit(0);
    }

    public void illeagal_input() {
        System.out.println("MuaInterpreter Error: Illeagal_input!");
        System.exit(0);
    }
    public void illeagal_input(String cause) {
        System.out.println("MuaInterpreter Error: Illeagal_input about"+" "+cause+"!");
        System.exit(0);
    }

    public void algorithm_error() {
        System.out.println("MuaInterpreter Error: You may div or mod zero!");
        System.exit(0);
    }
}