package dubbo.producer.service;

import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: sssd
 * Date: 2017/11/9 10:44
 * Version: V1.0
 * To change this template use File | Settings | File Templates.
 * Description:   dubbo service 的接口
 */
public interface DubboService {

    public String demoService(Map params) throws Exception;

}
