package hbase.java.read;

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

    /**
     * hbase配置文件
     */
    private Configuration conf;

    /**
     * 初始化构造器
     */
    public HbaseDataReader() {
        conf = HBaseConfiguration.create();
        // 配置hbase主节点
        conf.set("hbase.master", "192.168.1.239:60000");
        // 配置hbase zookeeper 集群的节点
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
        // 如果hbase的rowkey为空，则直接返回null
        if (rowKeys == null || rowKeys.size() <= 0) {
            return null;
        }
        // 初始化每批次查询rowkey的大小
        final int maxRowKeySize = 1000;
        // 如果可以被整除，说明是1000的倍数，其对应的loopsize便是1000的倍数，如果不能被整除，需要在其的倍数加1.
        int loopSize = rowKeys.size() % maxRowKeySize == 0 ? rowKeys.size() / maxRowKeySize : rowKeys.size() / maxRowKeySize + 1;
        // 初始化从hbase查询出的结果
        ArrayList<Future<List<Result>>> results = new ArrayList<Future<List<Result>>>();
        // 批次循环
        for (int loop = 0; loop < loopSize; loop++) {
            // 用来获取最开始的rowkey
            int rowkeyStart = loop * maxRowKeySize;
            // 用来获取最末端的rowkey
            int rowKeyEnd = (loop + 1) * maxRowKeySize > rowKeys.size() ? rowKeys.size() : (loop + 1) * maxRowKeySize;
            // 截取本批次需要查找的rowkey
            List<String> partRowKeys = rowKeys.subList(rowkeyStart, rowKeyEnd);
            // 初始化hbase查询数据的线程。
            HbaseDataGetter hbaseDataGetter = new HbaseDataGetter(partRowKeys, filterColumn, tableName, columnFamilyName, conf);
            synchronized (pool) {
                Future<List<Result>> submit = pool.submit(hbaseDataGetter);
                results.add(submit);
            }
        }
        // 重新整合查询出的数据
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
