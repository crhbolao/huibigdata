package hbase.java.demo;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Result;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * Created with IntelliJ IDEA.
 * User: bolao
 * Date: 2018/9/5 11:46
 * Version: V1.0
 * To change this template use File | Settings | File Templates.
 * Description:   hbase 获取数据的线程
 */
public class HbaseDataGetter implements Callable<List<Result>> {

    /**
     * hbase rowKeys
     */
    private List<String> rowKeys;

    /**
     * hbase column family
     */
    private List<String> filterColumn;

    /**
     * hbase table
     */
    private String tableName;

    /**
     * hbase 中的列簇
     */
    private String columnFamilyName;

    /**
     * 配置文件
     */
    private Configuration conf;

    /**
     * 构造器初始化
     *
     * @param rowKeys          hbase 主键
     * @param filterColumn     hbase 列族
     * @param tableName        hbase 数据表 tablename
     * @param columnFamilyName hbase 列簇
     */
    public HbaseDataGetter(List<String> rowKeys, List<String> filterColumn, String tableName, String columnFamilyName, Configuration conf) {
        this.rowKeys = rowKeys;
        this.filterColumn = filterColumn;
        this.tableName = tableName;
        this.columnFamilyName = columnFamilyName;
        this.conf = conf;
    }

    @Override
    public List<Result> call() throws Exception {
        Result[] results = getDatasFromHbase(rowKeys, filterColumn);
        ArrayList<Result> list = new ArrayList<Result>();
        // tocode将结果result封装到list中。
        for (Result result : results) {
            list.add(result);
        }
        return list;
    }

    /**
     * 从hbase中获取数据
     *
     * @param rowKeys      hbase 主键
     * @param filterColumn hbase 列簇下面对应的列名
     */
    public Result[] getDatasFromHbase(List<String> rowKeys, List<String> filterColumn) {
        HTable table = createTable(tableName);
        ArrayList<Get> gets = new ArrayList<Get>();
        for (String rowKey : rowKeys) {
            Get get = new Get(rowKey.getBytes());
            if (filterColumn != null && filterColumn.size() > 0) {
                for (String column : filterColumn) {
                    get.addColumn(columnFamilyName.getBytes(), column.getBytes());
                    gets.add(get);
                }
            }
        }
        Result[] results = null;
        try {
            results = table.get(gets);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            gets.clear();
            try {
                table.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return results;
    }

    /**
     * java连接hbase数据表
     *
     * @param tableName hbase数据表
     * @return
     */
    public HTable createTable(String tableName) {
        HTable table = null;
        try {
            table = new HTable(this.conf, tableName);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return table;
    }

}
