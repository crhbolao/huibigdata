package hbase.java.demo;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.util.Bytes;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Created with IntelliJ IDEA.
 * User: bolao
 * Date: 2018/9/5 10:06
 * Version: V1.0
 * To change this template use File | Settings | File Templates.
 * Description:   hbase 多线程读取数据
 */
public class HbaseDataReader {

    /**
     * 初始化线程池
     */
    private static ExecutorService pool = Executors.newFixedThreadPool(10);

    private Configuration conf;


    public HbaseDataReader() {
        conf = HBaseConfiguration.create();
        conf.set("hbase.master", "192.168.1.239:60000");
        conf.set("hbase.zookeeper.quorum", "nowledgedata-n238:2181,nowledgedata-n239:2181,nowledgedata-n240:2181");
    }

    /**
     * 从hbase中读取数据
     *
     * @param rowKeys          hbase rowkeys
     * @param filterColumn     hbase列簇下面对应的列名
     * @param tableName        hbase数据表
     * @param columnFamilyName hbase列簇
     * @return
     */
    public ArrayList<Result> readDataFromHbase(List<String> rowKeys, List<String> filterColumn, String tableName, String columnFamilyName) {
        if (rowKeys == null || rowKeys.size() <= 0) {
            return null;
        }
        final int maxRowKeySize = 1000;
        // 如果可以被整除，说明是1000的倍数，其对应的loopsize便是1000的倍数，如果不能被整除，需要在其的倍数加1.
        int loopSize = rowKeys.size() % maxRowKeySize == 0 ? rowKeys.size() / maxRowKeySize : rowKeys.size() / maxRowKeySize + 1;
        ArrayList<Future<List<Result>>> results = new ArrayList<Future<List<Result>>>();
        for (int loop = 0; loop < loopSize; loop++) {
            // 用来获取 最末端的 rowkey
            int rowKeyEnd = (loop + 1) * maxRowKeySize > rowKeys.size() ? rowKeys.size() : (loop + 1) * maxRowKeySize;
            int rowkeyStart = loop * maxRowKeySize;
            List<String> partRowKeys = rowKeys.subList(rowkeyStart, rowKeyEnd);
            HbaseDataGetter hbaseDataGetter = new HbaseDataGetter(partRowKeys, filterColumn, tableName, columnFamilyName, conf);
            synchronized (pool) {
                Future<List<Result>> submit = pool.submit(hbaseDataGetter);
                results.add(submit);
            }
        }
        ArrayList<Result> finalyRes = new ArrayList<Result>();
        try {
            for (Future<List<Result>> result : results) {
                List<Result> tempRes = result.get();
                finalyRes.addAll(tempRes);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return finalyRes;
    }

    public static void main(String[] args) {

        ArrayList<String> rowKeys = new ArrayList<String>();
        rowKeys.add("195861-1000085491");
        rowKeys.add("195861-1000334725");
        rowKeys.add("195861-1000510265");
        rowKeys.add("195861-1000910997");
        rowKeys.add("195861-1001687113");
        rowKeys.add("195861-1002084154");
        rowKeys.add("195861-1002984302");

        ArrayList<String> filterColumn = new ArrayList<String>();
        filterColumn.add("accountType");
        filterColumn.add("cntFollowers");
        filterColumn.add("cntFriends");

        String tableName = "hquser";
        String columnFamilyName = "info";
        HbaseDataReader hbaseDataReader = new HbaseDataReader();
        ArrayList<Result> list = hbaseDataReader.readDataFromHbase(rowKeys, filterColumn, tableName, columnFamilyName);
        for (Result result : list) {
            // 获取的是主键
            String s = Bytes.toString(result.getRow());
            byte[] accountTypes = result.getValue(Bytes.toBytes(columnFamilyName), Bytes.toBytes("accountType"));
            System.out.println("hbase主键为：" + s + "对应字段为：" + Bytes.toInt(accountTypes));
        }
        System.out.println("读取的数据大小为：" + list.size());
    }

}
