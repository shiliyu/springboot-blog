package hello.entity;

public class Result {

    String status;
    String msg;
    // NB: boolean type variable naming convention!
    // 这样命名好吗？
    boolean isLogin;
    Object data;

    // 用static factory method 替代 new！
    public static Result failsAsIn(String message) {
        return new Result("failed", message, false);
    }

    public static Result succeedsWith(String message) {
        return new Result("ok", message, true);
    }

    // 构造器变为 private，保证所有的实例都是通过工厂方法创建的！
    // 这样，创建出来的实例状态都是符合预期的!
    public Result(String status, String msg, boolean isLogin) {
        this(status, msg, isLogin, null);
    }

    public Result(String status, String msg, boolean isLogin, Object data) {
        this.status = status;
        this.msg = msg;
        this.isLogin = isLogin;
        this.data = data;
    }

    public String getStatus() {
        return status;
    }

    public String getMsg() {
        return msg;
    }

    public boolean isLogin() {
        return isLogin;
    }

    public Object getData() {
        return data;
    }
}
