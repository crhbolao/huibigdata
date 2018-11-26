package spark.main;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;

import java.io.Serializable;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: bolao
 * Date: 2018/11/26 14:33
 * Version: V1.0
 * To change this template use File | Settings | File Templates.
 * Description:
 */
public class HelloWorld implements Serializable {

    public static Log LOG = LogFactory.getLog(HelloWorld.class);

    public static void main(String[] args) {
        SparkConf conf = new SparkConf();
        JavaSparkContext sc = new JavaSparkContext(conf);
        LOG.info("接收到的文件名字为：" + args[0]);
        JavaRDD<String> lines = sc.textFile("file://" + args[0]);
        List<String> collect = lines.collect();
        for (String s : collect) {
            LOG.info("读取的文件内容为：" + s);
        }
    }
}
