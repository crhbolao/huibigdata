package sparkesdemo.utils;

import com.alibaba.fastjson.JSONObject;
import org.apache.hadoop.conf.Configuration;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;
import org.joda.time.DateTime;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.ArrayList;
import java.util.Date;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: bolao
 * Date: 2018/8/24 13:46
 * Version: V1.0
 * To change this template use File | Settings | File Templates.
 * Description:   spark 操作 es
 */
public class SparkEs {

    public String esNodes;

    public Configuration conf;

    public SparkEs(Map parmas) {
        this.esNodes = (String) parmas.get("esNodes");
        this.conf = (Configuration) parmas.get("conf");
    }

    /**
     * 将本地文件通过spark写入到es中。
     *
     * @param path
     */
    public void writeToEs(String path) {
        SparkConf sparkConf = new SparkConf();
        sparkConf.setMaster("local[*]").setAppName("sparkWriteToEs");
        JavaSparkContext sparkContext = new JavaSparkContext(sparkConf);
        String dataTypeId = "weibo_user";
        final String esIndexs = EsUtils.ES_INDEX_TYPE_MAP.get(dataTypeId);
        conf = EsUtils.buildWriteConf(this.conf, "USER_URN", esIndexs, esNodes);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("test", "lailai");
        ArrayList<String> list = new ArrayList<String>();
        EsUtils.sparkWriteEs(list, conf, sparkContext);
    }

    /**
     *   从es中查询数据
     */
    public void queryFromEs() {

        Date currentDate = new DateTime().toDate();
        String startTime = DateUtils.ES_BEGIN_DATE_FORMAT.get().format(currentDate);
        String endTime = DateUtils.ES_DATE_FORMAT.get().format(currentDate);
        SparkConf sparkConf = new SparkConf();
        sparkConf.setAppName("queryFromEs").setMaster("local[*]");
        JavaSparkContext sparkContext = new JavaSparkContext(sparkConf);

    }

    public static void main(String[] args) {
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("applicationContext.xml");
        Map params = (Map) context.getBean("commonParams");

    }
}
