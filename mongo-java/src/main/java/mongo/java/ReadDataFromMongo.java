package mongo.java;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.mongodb.*;
import mongo.utils.ProperUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;

import java.util.Date;
import java.util.HashSet;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: bolao
 * Date: 2018/8/18 17:34
 * Version: V1.0
 * To change this template use File | Settings | File Templates.
 * Description:   临时测试用来读取mongo中的数据
 */
public class ReadDataFromMongo {


    public MongoTemplate mongoTemplate;

    public ReadDataFromMongo(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    /**
     * 从 mongo 中加载数据
     */
    public void LoadDataFromMongo() {
        Query query = new Query();
        query.with(new Sort(new Sort.Order(Sort.Direction.DESC, "dwCreatedAt")));
        List<DBObject> mongoDatas = mongoTemplate.find(query, DBObject.class, "currDay1");
        int i = 0;
        for (DBObject dbObject : mongoDatas) {
            JSONObject parse = (JSONObject) JSON.parse(dbObject.toString());
            JSONArray data = parse.getJSONArray("data");
            HashSet<String> set = new HashSet<String>();
            for (Object o : data) {
                JSONObject object = (JSONObject) o;
                String wifiMac = object.getString("wifiMac");
                set.add(wifiMac);
            }
            if (set.size() > 1) {
                for (Object o : data) {
                    JSONObject object = (JSONObject) o;
                    String wifiMac = object.getString("wifiMac");
                    if (StringUtils.equalsIgnoreCase(wifiMac,"c8eea6290c0b") ){
                        System.out.println(parse);
                    }
                }
            }
        }
    }

    /**
     * mongo js mapreduce 统计
     */
    public void mongoCount() {

        String now = Long.toString(new Date().getTime());
        String pleasedTime = "300";
        String wifiMac = "'c8eea6290c0b'";
        // 其中的String.format是拼接字符串，相当于将对应的参数赋值到对应的map内容中%s。
        String map = String.format(ProperUtils.getString("countCurrDayMap"), wifiMac, now, 600, pleasedTime);
        String reduce = ProperUtils.getString("countCurrDayReduce");

        String collectionName = "currDay1";
        DBCollection collection = mongoTemplate.getCollection(collectionName);
        MapReduceOutput output = collection.mapReduce(map, reduce, null, MapReduceCommand.OutputType.INLINE, new BasicDBObject());
        Iterable<DBObject> results = output.results();
        for (DBObject result : results) {
            // 其中result中“_id”为主键，“value”为mapreduce统计的结果内容。
            String id = result.get("_id").toString();
            DBObject values = (DBObject) result.get("value");
            System.out.println("主键为：" + id + "对应的内容为：" + values.toString());
        }
    }

    public static void main(String[] args) {
        ClassPathXmlApplicationContext classPathXmlApplicationContext = new ClassPathXmlApplicationContext("applicationContext.xml");
        MongoTemplate mongoTemplate = (MongoTemplate) classPathXmlApplicationContext.getBean("mongoTemplate");
        ReadDataFromMongo mongo = new ReadDataFromMongo(mongoTemplate);
        mongo.LoadDataFromMongo();
//        mongo.mongoCount();
    }
}
