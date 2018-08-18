package mongo;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.mongodb.DBObject;
import org.apache.commons.lang.StringUtils;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;

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

    public void LoadDataFromMongo() {
        Query query = new Query();
        query.with(new Sort(new Sort.Order(Sort.Direction.DESC, "dwCreatedAt")));
        List<DBObject> mongoDatas = mongoTemplate.find(query, DBObject.class, "currDay2");
        int i = 0;
        for (DBObject dbObject : mongoDatas) {
            JSONObject parse = (JSONObject) JSON.parse(dbObject.toString());
            JSONArray data = parse.getJSONArray("data");
            for (Object datum : data) {
                JSONObject datum1 = (JSONObject) datum;
                if (StringUtils.equalsIgnoreCase(datum1.getString("wifiMac"), "c8eea6290c0b")) {
                    System.out.println(parse);
                    i++;
                    break;
                }
            }
        }
        System.out.println("总共有" + i + "条数据！");
    }

    public static void main(String[] args) {
        ClassPathXmlApplicationContext classPathXmlApplicationContext = new ClassPathXmlApplicationContext("applicationContext.xml");
        MongoTemplate mongoTemplate = (MongoTemplate) classPathXmlApplicationContext.getBean("mongoTemplate");
        ReadDataFromMongo mongo = new ReadDataFromMongo(mongoTemplate);
        mongo.LoadDataFromMongo();
    }
}
