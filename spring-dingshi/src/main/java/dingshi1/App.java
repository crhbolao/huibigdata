package dingshi1;

import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Created with IntelliJ IDEA.
 * User: bolao
 * Date: 2018/9/3 18:44
 * Version: V1.0
 * To change this template use File | Settings | File Templates.
 * Description:
 */
public class App {

    public static void main(String[] args) {
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("applicationContext.xml");
        context.start();
    }

}
