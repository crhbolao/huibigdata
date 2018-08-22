package katta.java;

import com.ivyft.katta.client.LuceneClient;
import com.ivyft.katta.util.ZkConfiguration;
import katta.enity.FlowData;
import katta.utils.DocConvert;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.common.SolrDocument;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: bolao
 * Date: 2018/8/20 11:47
 * Version: V1.0
 * To change this template use File | Settings | File Templates.
 * Description:
 */
public class ReadDataBylucene {

    private final LuceneClient luceneClient = new LuceneClient(new ZkConfiguration());

    public void LoadDataBylucene() throws Exception {
        String batchId = "61149fa959794a2eb2175002e4e32d88";
        SolrQuery sq = new SolrQuery("*:*");
        List<SolrDocument> documentList = luceneClient.query(sq, new String[]{batchId}).getDocs();
        for (SolrDocument solrDocument : documentList) {
            FlowData flowData = DocConvert.kattaSolrDoc2FlowData(solrDocument);
            System.out.println(flowData.toString());
        }
    }

    public static void main(String[] args) throws Exception{
        ReadDataBylucene readDataBylucene = new ReadDataBylucene();
        readDataBylucene.LoadDataBylucene();
    }


}
