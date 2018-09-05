package hbase.demo;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.*;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.util.Bytes;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: bolao
 * Date: 2018/9/5 18:06
 * Version: V1.0
 * To change this template use File | Settings | File Templates.
 * Description:     hbase相关操作
 */
public class HbaseDemo {

    /**
     * 日志
     */
    private Configuration conf;

    @Before
    public void init() {
        conf = HBaseConfiguration.create();
        conf.set("hbase.zookeeper.quorum", "nowledgedata-n238:2181,nowledgedata-n239:2181,nowledgedata-n240:2181");
    }

    @Test
    public void testPut() throws Exception {
        HTable table = new HTable(conf, "hquser");
        Put put = new Put(Bytes.toBytes("195861-1000085491"));
        put.add(Bytes.toBytes("info"), Bytes.toBytes("accountType"), Bytes.toBytes("3"));
        put.add(Bytes.toBytes("info"), Bytes.toBytes("cntFollowers"), Bytes.toBytes("3"));
        put.add(Bytes.toBytes("info"), Bytes.toBytes("cntFriends"), Bytes.toBytes("5"));
        table.put(put);
        table.close();
    }

    @Test
    public void testPutAll() throws Exception {
        HTable table = new HTable(conf, "hquser");
        List<Put> puts = new ArrayList<Put>(10000);
        for(int i=0 ; i<1000000; i++){
            Put put = new Put(Bytes.toBytes("rk"+i));
            put.add(Bytes.toBytes("info"), Bytes.toBytes("sal"), Bytes.toBytes(""+i));
            puts.add(put);
            //这里防止数据量过大，内存溢出，所以10000条就插入一次，分批插入
            if(i % 10000 == 0){
                table.put(puts);
                puts = new ArrayList<Put>(10000);
            }
        }
        table.put(puts);
        table.close();
    }

    @Test
    public void testGet() throws Exception{
        HTable table = new HTable(conf, "hquser");
        Get get = new Get(Bytes.toBytes("195861-1000085491"));
        //get.addColumn(Bytes.toBytes("info"), Bytes.toBytes("name"));
        get.setMaxVersions(5);
        Result result = table.get(get);
        //result.getValue(family, qualifier)
        for(KeyValue kv: result.list()){
            String family = new String(kv.getFamily());
            String qualifier = new String(kv.getQualifier());
            String value = new String(kv.getValue());
            System.out.println(family + " " + qualifier + " : "+ value);
        }
        table.close();
    }

    @Test
    public void testScan()throws Exception{
        HTablePool pool = new HTablePool(conf,1000);
        HTableInterface table = pool.getTable("user");
        Scan scan = new Scan(Bytes.toBytes("rk0001"),Bytes.toBytes("rk0002"));
        scan.addFamily(Bytes.toBytes("info"));
        ResultScanner scanner = table.getScanner(scan);
        for(Result r : scanner){
            byte[] name = r.getValue(Bytes.toBytes("info"), Bytes.toBytes("name"));
            byte[] age = r.getValue(Bytes.toBytes("info"), Bytes.toBytes("age"));
            byte[] sex = r.getValue(Bytes.toBytes("info"), Bytes.toBytes("sex"));
            System.out.println(new String(name) + " : " + new String(age) + " : " + new String(sex));
        }
        pool.close();
    }

    @Test
    public void testDel() throws Exception{
        HTable table = new HTable(conf,"tab_dog");
        Delete del = new Delete(Bytes.toBytes("rk0001"));
        del.deleteColumn(Bytes.toBytes("info"), Bytes.toBytes("name"));
        table.delete(del);
        table.close();
    }

    @Test
    public void testDrop() throws Exception{
        HBaseAdmin admin = new HBaseAdmin(conf);
        admin.disableTable("tab_dog");
        admin.deleteTable("tab_dog");
        admin.close();
    }

    @Test
    public void testCreate() throws Exception{
        HBaseAdmin admin = new HBaseAdmin(conf);
        //创建表信息（表名）
        HTableDescriptor desc = new HTableDescriptor(TableName.valueOf("tab_dog"));
        //创建列族
        HColumnDescriptor cd = new HColumnDescriptor("info");
        cd.setMaxVersions(10);
        //添加列族
        desc.addFamily(cd);
        //创建表
        admin.createTable(desc);
        admin.close();
    }

}
