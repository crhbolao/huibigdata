package katta.spark;

import com.ivyft.katta.hadoop.KattaInputFormat;
import com.ivyft.katta.util.ZkConfiguration;
import com.sdyc.ndmp.protobuf.serializer.JdkSerializer;
import com.sdyc.ndmp.protobuf.serializer.StringSerializer;
import katta.enity.FlowData;
import katta.utils.CommonUtils;
import katta.utils.DocConvert;
import org.apache.hadoop.conf.Configuration;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.common.SolrDocument;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.FlatMapFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import scala.Tuple2;

import java.io.Serializable;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: bolao
 * Date: 2018/8/10 11:49
 * Version: V1.0
 * To change this template use File | Settings | File Templates.
 * Description:   从 katta 中读取对应的数据，用来临时查看katta中的数据情况
 */
public class ReadDataFromKatta implements Serializable {

    /**
     * 日志
     */
    public Logger LOG = LoggerFactory.getLogger(ReadDataFromKatta.class);

    /**
     * hadoop conf
     */
    public transient Configuration conf;

    private Hashtable<String, Object> zkconf = new Hashtable<String, Object>(0);


    public ReadDataFromKatta(Configuration conf) {
        this.conf = conf;
        this.zkconf = CommonUtils.zkconf2Htable(new ZkConfiguration());
        this.conf = CommonUtils.unionConf(this.conf, CommonUtils.Htable2Conf(zkconf), false);
    }

    public Object startSparkJob() {

        SparkConf sparkConf = new SparkConf();
        sparkConf.setMaster("local[*]").setAppName("readDataFromKatta");
        JavaSparkContext sparkContext = new JavaSparkContext(sparkConf);
        String batchId = "2135e967a1e74f7ab5b8e748a43733ff";
        String nodeId = "34480";

        //构建 katta 查询的数据表和对应的查询语句
        Hashtable<String, SolrQuery> solrQuerytable = new Hashtable<String, SolrQuery>();
        SolrQuery solrQuery = new SolrQuery();
//        solrQuery.addFilterQuery("META:" + nodeId + "_Y");
        solrQuery.addFilterQuery("*:*");
        solrQuerytable.put(batchId, solrQuery);

            JdkSerializer jdkSerializer = new JdkSerializer();
        JavaPairRDD<Object, SolrDocument> jpr = null;
        for (Map.Entry<String, SolrQuery> entry : solrQuerytable.entrySet()) {
            String tempBatchId = entry.getKey();
            SolrQuery tempSolrQuery = entry.getValue();
            conf.set("katta.input.query", StringSerializer.toString(jdkSerializer.serialize(tempSolrQuery)));
            conf.set("katta.hadoop.indexes", tempBatchId);
            JavaPairRDD<Object, SolrDocument> jprTemp = sparkContext.newAPIHadoopRDD(conf, KattaInputFormat.class, Object.class, SolrDocument.class);
            if (jpr != null) {
                jpr = jpr.union(jprTemp);
            } else {
                jpr = jprTemp;
            }
        }

        List<String> collect = jpr.mapPartitions(new FlatMapFunction<Iterator<Tuple2<Object, SolrDocument>>, String>() {
            @Override
            public Iterable<String> call(Iterator<Tuple2<Object, SolrDocument>> iterator) throws Exception {
                LinkedList<String> resList = new LinkedList<String>();
                while (iterator.hasNext()) {
                    Tuple2<Object, SolrDocument> next = iterator.next();
                    SolrDocument solrDocument = next._2();
                    FlowData flowData = DocConvert.kattaSolrDoc2FlowData(solrDocument);
                    long locProvince = flowData.getLocProvince();
                    long locCity = flowData.getLocCity();
                    long locCountry = flowData.getLocCountry();
                    resList.add(flowData.toString());
                }
                return resList;
            }
        }).collect();

        for (String s : collect) {
            System.out.println(s);
        }

        return new Object();
    }

    public static void main(String[] args) {
        ClassPathXmlApplicationContext classPathXmlApplicationContext = new ClassPathXmlApplicationContext("hadoopConfig.xml");
        Configuration conf = (Configuration) classPathXmlApplicationContext.getBean("hadoopConfiguration");
        ReadDataFromKatta readDataFromKatta = new ReadDataFromKatta(conf);
        readDataFromKatta.startSparkJob();
    }

}
