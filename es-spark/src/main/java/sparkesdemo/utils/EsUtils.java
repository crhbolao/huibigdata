package sparkesdemo.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.MapWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.FlatMapFunction;
import org.apache.spark.api.java.function.PairFlatMapFunction;
import org.apache.spark.sql.DataFrame;
import org.apache.spark.sql.SQLContext;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.hadoop.cfg.ConfigurationOptions;
import org.elasticsearch.hadoop.mr.EsInputFormat;
import org.elasticsearch.hadoop.mr.EsOutputFormat;
import org.elasticsearch.hadoop.mr.WritableArrayWritable;
import org.elasticsearch.hadoop.util.WritableUtils;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import scala.Tuple2;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.math.BigDecimal;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: bolao
 * Date: 2018/7/14 9:57
 * Version: V1.0
 * To change this template use File | Settings | File Templates.
 * Description:   spark es utils
 */
public class EsUtils implements Serializable {

    /**
     * 日志
     */
    protected static Log LOG = LogFactory.getLog(EsUtils.class);

    /**
     * es map dataType => esIndex
     */
    public static final HashMap<String, String> ES_INDEX_TYPE_MAP = new HashMap<String, String>(3);

    /**
     * 静态代码块，主要是用来初始化，esindex。
     */
    static {
        // 获取classloader
        ClassLoader classLoader = EsUtils.class.getClassLoader();
        if (classLoader == null) {
            classLoader = Thread.currentThread().getContextClassLoader();
        }
        InputStream inputStream = classLoader.getResourceAsStream("es.properties");
        // 获取properties
        Properties properties = new Properties();
        try {
            properties.load(inputStream);
            String weibo_user = properties.getProperty("weibo_user");
            String weibo_post = properties.getProperty("weibo_post");
            // 初始化相关参数
            ES_INDEX_TYPE_MAP.put("weibo_user", "hui_test_user/user_test");
            ES_INDEX_TYPE_MAP.put("weibo_post", "hui_test_post/post_test");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * build spark write es conf
     *
     * @param conf    hadoop conf
     * @param esId    es 主键
     * @param esIndex es index
     * @param esNodes es 集群节点
     * @return
     */
    public static Configuration buildWriteConf(Configuration conf, String esId, String esIndex, String esNodes) {
        // 如果索引不存在，则自动创建
        conf.set("es.index.auto.create", "true");
        // 指定es index
        conf.set("es.resource", esIndex);
        // 制定es 主键
        conf.set("es.mapping.id", esId);
        // 当前task 的 task id
        conf.set("mapred.tip.id", "task_201707121733_0003_m_000005");
        // 指定es节点
        conf.set("es.nodes", esNodes);
        return conf;
    }

    /**
     * spark write to es
     *
     * @param list 待写的内容
     * @param conf spark es write conf
     */
    public static void sparkWriteEs(List list, Configuration conf, JavaSparkContext sc) {
        // 将待写的内容换转为json string
        List<String> tempList = convertToJson(list);
        // list 转换为 javaRDD
        JavaRDD<String> javaRDD = sc.parallelize(tempList);
        javaRDD.mapPartitionsToPair(new PairFlatMapFunction<Iterator<String>, NullWritable, MapWritable>() {
            @Override
            public Iterable<Tuple2<NullWritable, MapWritable>> call(Iterator<String> iterator) throws Exception {
                LinkedList<Tuple2<NullWritable, MapWritable>> resList = new LinkedList<Tuple2<NullWritable, MapWritable>>();
                while (iterator.hasNext()) {
                    // spark write data to es 中特殊的数据格式：MapWritable
                    final MapWritable writableMap = new MapWritable();
                    try {
                        final String next = iterator.next();
                        final JSONObject jsonObject = JSONObject.parseObject(next.trim());
                        final Set<Map.Entry<String, Object>> entries = jsonObject.entrySet();
                        for (Map.Entry<String, Object> entry : entries) {
                            final Object entryValue = entry.getValue();
                            if (entryValue instanceof BigDecimal) {
                                BigDecimal a = new BigDecimal(((BigDecimal) entryValue).doubleValue());
                                writableMap.put(new Text(entry.getKey()), WritableUtils.toWritable(a.doubleValue()));
                            } else {
                                writableMap.put(new Text(entry.getKey()), WritableUtils.toWritable(entryValue));
                            }
                        }
                        resList.add(new Tuple2<NullWritable, MapWritable>(null, writableMap));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                return resList;
            }
        }).saveAsNewAPIHadoopFile("", NullWritable.class, MapWritable.class, EsOutputFormat.class, conf);
    }

    /**
     * 将代写的文件内容转换为json string
     *
     * @param list
     * @return
     */
    public static List<String> convertToJson(List list) {
        ArrayList<String> resList = new ArrayList<String>();
        final HashMap<String, Object> hashMap = new HashMap<String, Object>();
        for (Object o : list) {
            JSONObject object = (JSONObject) o;
            hashMap.put("1", object.getString("1"));
        }
        resList.add(JSON.toJSONString(hashMap));
        return resList;
    }

    /**
     * build es query conf（By time）
     *
     * @param conf      hadoop conf
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @param esIndexs  esindex
     * @param esNodes   esnode
     * @param dataType  数据类型
     * @return
     */
    public static Configuration buildQueryByTimeConf(Configuration conf, String startTime, String endTime, String esIndexs, String esNodes, String dataType) {
        Configuration config = conf;
        //指定读取的索引名称
        config.set(ConfigurationOptions.ES_RESOURCE, esIndexs);
        //正式环境指定es节点
        config.set(ConfigurationOptions.ES_NODES, esNodes);

        config.set(ConfigurationOptions.ES_READ_FIELD_EMPTY_AS_NULL, "false");

        config.set(ConfigurationOptions.ES_QUERY, buildQueryByTimeStr(dataType, startTime, endTime));

        return config;
    }


    /**
     * build es query string (by time)
     *
     * @param dataType  数据类型
     * @param startTime 起始时间
     * @param endTime   结束时间
     * @return
     */
    public static String buildQueryByTimeStr(String dataType, String startTime, String endTime) {

        ImmutableMap<String, String> tempmap = ImmutableMap.of("gt", startTime, "lte", endTime);
        HashMap<String, Object> conditionMap = new HashMap<String, Object>(1);
        if (StringUtils.equalsIgnoreCase(dataType, "post")) {
            StringBuffer sb = new StringBuffer();
            sb.append("DW_CREATED_AT:[").append(startTime).append(" TO ").append(endTime + "]");
            conditionMap.put("query", sb.toString());
        } else if (StringUtils.equalsIgnoreCase(dataType, "user")) {
            StringBuffer sb = new StringBuffer();
            sb.append("DW_UPDATED_AT:[").append(startTime).append(" TO ").append(endTime + "]");
            conditionMap.put("query", sb.toString());
        } else {
            StringBuffer sb = new StringBuffer();
            sb.append("dwCreatedAt:[").append("2018-02-08T17:33:48").append(" TO ").append("2018-03-08T17:33:48" + "]");
            conditionMap.put("query", sb.toString());
        }
        HashMap<String, Object> tempMap = new HashMap<String, Object>(1);
        tempMap.put("query", ImmutableMap.of("query_string", conditionMap));

        String condition = JSON.toJSONString(tempMap);
        return condition;
    }

    /**
     * query data from es
     *
     * @param conf             hadoop conf
     * @param javaSparkContext sparkcontext
     * @param startTime        开始时间
     * @param endTime          结束时间
     * @param esIndex          esindes
     * @param esNodes          esnodes
     * @param dataType         数据类型
     * @return
     */
    public static JavaRDD<JSONObject> queryDataByTime(Configuration conf, JavaSparkContext javaSparkContext, String startTime, String endTime, String esIndex, String esNodes, String dataType) {
        Configuration queryByTimeConf = buildQueryByTimeConf(conf, startTime, endTime, esIndex, esNodes, dataType);
        JavaRDD<JSONObject> javaRDD = javaSparkContext.newAPIHadoopRDD(queryByTimeConf, EsInputFormat.class, NullWritable.class, MapWritable.class)
                .mapPartitions(new FlatMapFunction<Iterator<Tuple2<NullWritable, MapWritable>>, JSONObject>() {
                    @Override
                    public Iterable<JSONObject> call(Iterator<Tuple2<NullWritable, MapWritable>> iterator) throws Exception {
                        LinkedList<JSONObject> resList = new LinkedList<JSONObject>();
                        while (iterator.hasNext()) {
                            final Tuple2<NullWritable, MapWritable> next = iterator.next();
                            final MapWritable mapWritable = next._2;
                            JSONObject jsonObject = new JSONObject();
                            final Set<Map.Entry<Writable, Writable>> entries = mapWritable.entrySet();
                            for (Map.Entry<Writable, Writable> entry : entries) {
                                final Writable key = entry.getKey();
                                final Writable value = entry.getValue();
                                if (value instanceof WritableArrayWritable) {
                                    WritableArrayWritable var6 = (WritableArrayWritable) value;
                                    jsonObject.put(key.toString(), var6.toStrings());
                                } else {
                                    jsonObject.put(key.toString(), value.toString());
                                }
                            }
                            resList.add(jsonObject);
                        }
                        return resList;
                    }
                });
        return javaRDD;
    }

    /**
     * build es update conf
     *
     * @param conf    hadoop conf
     * @param esId    es 主键
     * @param esIndex esIndex
     * @param esNodes esnode
     * @return
     */
    public static Configuration buildUpdateConf(Configuration conf, String esId, String esIndex, String esNodes) {
        // 允许数据自动创建es index
        conf.set("es.index.auto.create", "true");
        // 设置es index
        conf.set("es.resource", esIndex);
        // 设置es id
        conf.set("es.mapping.id", esId);
        // es的更新方式为upsert
        conf.set("es.write.operation", "upsert");
        conf.set("mapred.tip.id", "task_201807121733_0003_m_000005");
        // es nnodes
        conf.set("es.nodes", esNodes);
        return conf;
    }

    /**
     * es update postdata
     *
     * @param conf        hadoop conf
     * @param esId        es 主键
     * @param esIndex     es index
     * @param esNodes     es nodex
     * @param sc          javasparkcontext
     * @param javaPairRDD javaRDD 数据
     */
    public static void updatePostData(Configuration conf, String esId, String esIndex, String esNodes, JavaSparkContext sc, JavaPairRDD<String, JSONObject> javaPairRDD) {
        Configuration configuration = buildUpdateConf(conf, esId, esIndex, esNodes);
        javaPairRDD.mapPartitionsToPair(new PairFlatMapFunction<Iterator<Tuple2<String, JSONObject>>, NullWritable, MapWritable>() {
            @Override
            public Iterable<Tuple2<NullWritable, MapWritable>> call(Iterator<Tuple2<String, JSONObject>> iterator) throws Exception {
                LinkedList<Tuple2<NullWritable, MapWritable>> resList = new LinkedList<Tuple2<NullWritable, MapWritable>>();
                while (iterator.hasNext()) {
                    Tuple2<String, JSONObject> next = iterator.next();
                    JSONObject jsonObject = next._2;
                    Set<Map.Entry<String, Object>> entries = jsonObject.entrySet();
                    final MapWritable writableMap = new MapWritable();
                    for (Map.Entry<String, Object> entry : entries) {
                        final Object entryValue = entry.getValue();
                        if (entryValue instanceof BigDecimal) {
                            BigDecimal a = new BigDecimal(((BigDecimal) entryValue).doubleValue());
                            writableMap.put(new Text(entry.getKey()), WritableUtils.toWritable(a.doubleValue()));
                        } else {
                            writableMap.put(new Text(entry.getKey()), WritableUtils.toWritable(entryValue));
                        }
                    }
                    resList.add(new Tuple2<NullWritable, MapWritable>(null, writableMap));
                }
                return resList;
            }
        }).saveAsNewAPIHadoopFile("", NullWritable.class, MapWritable.class, EsOutputFormat.class, configuration);
    }

    /**
     * 根据某个点段从es中匹配数据
     *
     * @param conf     spark es query conf
     * @param esIndexs es index
     * @param esNodes  es nodes
     * @return
     */
    public static SparkConf buildCountDataFromEsByKey(SparkConf conf, String esIndexs, String esNodes) {
        SparkConf config = conf;
        //指定读取的索引名称
        config.set(ConfigurationOptions.ES_RESOURCE, esIndexs);
        //正式环境指定es节点
        config.set(ConfigurationOptions.ES_NODES, esNodes);
        config.set(ConfigurationOptions.ES_READ_FIELD_EMPTY_AS_NULL, "false");
        return config;
    }

    /**
     * 根据某个字段从es中查找数据，并统计es中的帖子数。
     *
     * @param conf    spark es conf
     * @param esIndex es index
     * @param esNodes es nodes
     * @param keys    es 待查询字段的内容。
     * @return
     */
    public static HashMap<String, Long> countDataFromEsByKey(SparkConf conf, String esIndex, String esNodes, List<String> keys) {
        // 构建spark es query conf
        SparkConf newConf = buildCountDataFromEsByKey(conf, esIndex, esNodes);
        JavaSparkContext javaSparkContext = new JavaSparkContext(newConf);
        // 初始化spark sql.
        SQLContext sql = new SQLContext(javaSparkContext);
        sql.sql("CREATE TEMPORARY TABLE postTab USING org.elasticsearch.spark.sql OPTIONS (resource '" + esIndex + "')");
        // 构建spark sql 查询语句
        HashMap<String, Long> resMap = new HashMap<String, Long>();
        for (String key : keys) {
            // sql查询语句
            String tempsql = "select * from postTab where USER_ID = " + key;
            // 查询获取的数据dataFrame
            DataFrame dataFrame = sql.sql(tempsql);
            // dataFrame.count 用来进行统计
            resMap.put(key, dataFrame.count());
        }
        return resMap;
    }

    /**
     * java es client 初始化
     *
     * @param esName  es集群的名字
     * @param esHosts es hosts: 192.168.1.235:9300,192.168.1.237:9300,192.168.1.238:9300 （注意其实用逗号来分割）
     * @return
     */
    public static Client getEsClient(String esName, String esHosts) {
        Client transportClient = null;
        Settings settings = Settings.builder().put("cluster.name", esName).build();
        TransportClient client = TransportClient.builder().settings(settings).build();
        try {
            String[] nodes = esHosts.split(",");
            for (String node : nodes) {
                if (node.length() > 0) {
                    String[] hostPost = node.split(":");
                    transportClient = client.addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(hostPost[0]), Integer.parseInt(hostPost[1])));
                }
            }
            LOG.info("es初始化连接完成！！！");
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        return transportClient;
    }

    /**
     * java es client query by key
     *
     * @param keys     待查询字段的内容
     * @param esIndexs esindex
     * @param client   java es client
     * @return
     */
    public static HashMap<String, Long> countDataFromEsByKey(List<String> keys, String esIndexs, Client client) {
        String[] split = esIndexs.split("/");
        String esIndex = split[0];
        String esType = split[1];
        HashMap<String, Long> resMap = new HashMap<String, Long>();
        for (String key : keys) {
            // 构建查询语句，指定查询字段
            QueryBuilder queryBuilder = QueryBuilders.commonTermsQuery("USER_ID", key);
            SearchResponse response = client.prepareSearch(esIndex)
                    .setTypes(esType)
                    .setSearchType(SearchType.DFS_QUERY_AND_FETCH)
                    .setQuery(queryBuilder)
                    .execute()
                    .actionGet();
            resMap.put(key, response.getHits().totalHits());
        }
        return resMap;
    }
}
