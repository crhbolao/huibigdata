package solr.bean;

import org.apache.solr.client.solrj.beans.Field;

import java.io.Serializable;

/**
 * Created with IntelliJ IDEA.
 * User: bolao
 * Date: 2018/8/31 15:36
 * Version: V1.0
 * To change this template use File | Settings | File Templates.
 * Description:   和solrcore中对应的实体类
 */
public class Student implements Serializable {

    private static final long serialVersionUID = 1L;

    @Field("country")
    private String country;

    @Field("name")
    private String name;

    @Field("age")
    private Integer age;

    @Field("sex")
    private String sex;

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }
}
