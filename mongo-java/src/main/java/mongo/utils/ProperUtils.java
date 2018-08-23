package mongo.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Created with IntelliJ IDEA.
 * User: bolao
 * Date: 2018/8/23 13:50
 * Version: V1.0
 * To change this template use File | Settings | File Templates.
 * Description:  用来加载一些相关的配置文件
 */
public class ProperUtils {

    public static Properties properties = new Properties();

    static {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        if (classLoader == null) {
            classLoader = Properties.class.getClassLoader();
        }
        InputStream inputStream = classLoader.getResourceAsStream("mongoSql.xml");
        try {
            properties.loadFromXML(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 从配置文件中加载内容
     *
     * @param keyName   xml配置文件中对应的内容
     * @return
     */
    public static String getString(String keyName) {
        return properties.getProperty(keyName);
    }

    public static void main(String[] args) {
        String countCurrDayMap = ProperUtils.getString("countCurrDayMap");
        System.out.println(countCurrDayMap);
    }

}
