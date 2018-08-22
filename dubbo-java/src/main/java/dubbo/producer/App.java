package dubbo.producer;

import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Created with IntelliJ IDEA.
 * User: bolao
 * Date: 2018/8/21 16:22
 * Version: V1.0
 * To change this template use File | Settings | File Templates.
 * Description:    主要是用来启动dubbo的服务
 */
public class App {

    public static void main(String[] args) throws Exception{
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("dubboPrivider.xml");
        context.start();
        while(true){
            System.out.println("run....");
            Thread.sleep(3000);
        }
    }

}
