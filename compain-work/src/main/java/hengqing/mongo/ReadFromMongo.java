package hengqing.mongo;

import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;

import java.util.Arrays;

/**
 * Created with IntelliJ IDEA.
 * User: bolao
 * Date: 2018/8/28 17:17
 * Version: V1.0
 * To change this template use File | Settings | File Templates.
 * Description:    读取henqin mongo 中的数据
 */
public class ReadFromMongo {

    /**
     * mongo 连接的初始化
     */
    public void mongInit(){
        MongoCredential credential = MongoCredential.createCredential("lyd", "wifimac", "Z2x9y%F@".toCharArray());
        MongoClient mongoClient = new MongoClient(new ServerAddress("172.18.223.72", 27017), Arrays.asList(credential));
        MongoDatabase db = mongoClient.getDatabase( "wifimac" );
        MongoCollection collection = db.getCollection("currDay1");
        FindIterable fi = collection.find();
        MongoCursor cursor = fi.iterator();
        while (cursor.hasNext()){
            String next = (String) cursor.next();
            System.out.println(next);
        }
    }


    public static void main(String[] args) {
        ReadFromMongo readFromMongo = new ReadFromMongo();
        readFromMongo.mongInit();
    }

}
