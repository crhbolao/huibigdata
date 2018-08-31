package solr.java;

import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import solr.bean.Student;

import java.io.IOException;
import java.util.LinkedList;

/**
 * Created with IntelliJ IDEA.
 * User: bolao
 * Date: 2018/8/31 15:27
 * Version: V1.0
 * To change this template use File | Settings | File Templates.
 * Description:  使用solr提供的注解机制,说明: @Field无参数时,匹配当前字段,也可以自定义,字段必须在schema.xml中的Filed中存在.
 */
public class SolrUtils {

    /**
     * solrServer
     */
    public SolrServer solrServer;

    /**
     * 构造器初始化
     *
     * @param solrServer
     */
    public SolrUtils(SolrServer solrServer) {
        this.solrServer = solrServer;
    }

    /**
     * 用来向 solr 中加载数据
     */
    public void loadDataToSolr() {
        LinkedList<Student> students = new LinkedList<Student>();
        Student student = new Student();
        student.setAge(12);
        student.setCountry("巴西");
        student.setName("zhang4");
        student.setSex("女");
        students.add(student);
        try {
            this.solrServer.addBeans(students);
            UpdateResponse commit = this.solrServer.commit();
            System.out.println("提交返回的结果为：" + commit);
        } catch (SolrServerException e) {
            e.printStackTrace();
            System.out.println("solr添加内容失败！");
        } catch (IOException e) {
            System.out.println("solr提交失败！");
            try {
                this.solrServer.rollback();
            } catch (SolrServerException e1) {
                e1.printStackTrace();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }

    public SolrServer getSolrServer() {
        return solrServer;
    }

    public void setSolrServer(SolrServer solrServer) {
        this.solrServer = solrServer;
    }

    public static void main(String[] args) {
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("applicationContext.xml");
        SolrServer solrServer = (SolrServer) context.getBean("solrServer");
        SolrUtils solrUtils = new SolrUtils(solrServer);
        solrUtils.loadDataToSolr();
    }

}
