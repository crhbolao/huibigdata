package dubbo.producer.service.impl;

import dubbo.entity.Student;
import dubbo.producer.service.DubboService;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: sssd
 * Date: 2017/11/9 10:45
 * Version: V1.0
 * To change this template use File | Settings | File Templates.
 * Description:  dubbo service 的接口实现类
 */

@Service("dubboService")
public class DubboServiceImpl implements DubboService {
    @Override
    public String demoService(Map params) throws Exception {
        Object obj = params.get("temp");
        if (obj instanceof Student){
            Student student = (Student) obj;
            System.out.println(student.getName() + "---" + student.getCity());
        }
      /*  Class<?> aClass = obj.getClass();
        System.out.println("@@@" + aClass);
        HashMap<String, Object> map = new HashMap<String, Object>();
        for (Field field : aClass.getDeclaredFields()) {
            field.setAccessible(true);
            String fieldName = field.getName();
            Object value = field.get(obj);
            map.put(fieldName, value);
        }*/
        return null;
    }
}
