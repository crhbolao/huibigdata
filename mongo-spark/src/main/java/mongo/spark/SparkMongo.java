package mongo.spark;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.mongodb.hadoop.MongoInputFormat;
import com.mongodb.hadoop.util.MongoConfigUtil;
import org.apache.hadoop.conf.Configuration;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.PairFlatMapFunction;
import org.bson.BSONObject;
import org.joda.time.DateTime;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import scala.Tuple2;

import java.io.Serializable;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: bolao
 * Date: 2018/8/23 9:21
 * Version: V1.0
 * To change this template use File | Settings | File Templates.
 * Description:    spark mongo 读取数据
 */
public class SparkMongo implements Serializable {

    /**
     * hadoop conf
     */
    public transient Configuration conf;

    /**
     * mongo 数据表
     */
    protected List<String> mongoDbList = null;

    /**
     * mongo server
     */
    protected String mongoServer;

    /**
     * 初始化 mongo 的相关参数
     */
    public void init(){
        String[] strings = conf.getStrings("mongo.output.uri");
        mongoDbList = Arrays.asList(strings);
        mongoServer = conf.get("mongo.server.uri");
    }

    /**
     *  用来读取mongo的数据
     */
    public void readDataFromMongo(){
        // 从mongolists 中 获取 mongo 数据表
        final String DBIndex = this.mongoDbList.get((DateTime.now().getDayOfYear() - 1) % this.mongoDbList.size());
        // 用来拼接mongo 访问的 url
        final String serverUrl = this.mongoServer.endsWith("/") ? this.mongoServer + "hqu_ivs." + DBIndex : this.mongoServer + "/hqu_ivs." + DBIndex;

        conf.setInt(MongoConfigUtil.OUTPUT_BATCH_SIZE, 5000);
        conf.set(MongoConfigUtil.INPUT_URI, serverUrl);
        conf.set(MongoConfigUtil.CREATE_INPUT_SPLITS, "false");

        SparkConf sparkConf = new SparkConf();
        sparkConf.setAppName("Spark-Mongo").setMaster("local[*]");
        JavaSparkContext javaSparkContext = new JavaSparkContext(sparkConf);
        Map<String, String> collect = javaSparkContext.newAPIHadoopRDD(conf, MongoInputFormat.class, Object.class, BSONObject.class)
                .mapPartitionsToPair(new PairFlatMapFunction<Iterator<Tuple2<Object, BSONObject>>, String, String>() {
                    @Override
                    public Iterable<Tuple2<String, String>> call(Iterator<Tuple2<Object, BSONObject>> iterator) throws Exception {
                        LinkedList<Tuple2<String, String>> list = new LinkedList<Tuple2<String, String>>();
                        while (iterator.hasNext()) {
                            Tuple2<Object, BSONObject> next = iterator.next();
                            BSONObject bsonObject = next._2();
                            JSONObject jsonObject = (JSONObject) JSON.toJSON(bsonObject.toMap());
                            String clientMac = jsonObject.getString("_id");
                            list.add(new Tuple2<String, String>(clientMac, jsonObject.toJSONString()));
                        }
                        return list;
                    }
                }).collectAsMap();

        for (Map.Entry<String, String> entry : collect.entrySet()) {
            System.out.println(entry.getValue());
        }

    }

    public Configuration getConf() {
        return conf;
    }

    public void setConf(Configuration conf) {
        this.conf = conf;
    }


    public static void main(String[] args) {
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("applicationContext.xml");
        SparkMongo sparkMongo = (SparkMongo) context.getBean("sparkMongo");
        sparkMongo.init();
        sparkMongo.readDataFromMongo();
    }
}
