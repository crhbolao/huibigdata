package katta.utils;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.ivyft.katta.util.ZkConfiguration;
import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.solr.common.SolrDocument;

import java.io.*;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <pre>
 * Created with IntelliJ IDEA.
 * User: lwb
 * Date: 2016/3/14
 * Time: 13:50
 * To change this template use File | Settings | File Templates.
 * </pre>
 *
 * @author lwb
 */

/**
 * 定义一些常用的工具类
 */
public class CommonUtils {

    /***
     * 根据long time转换为java.util.Date
     * @param time
     * @return
     */
    public static Date parstLongForDate(Long time){

        if(time == null)
            return null;

        Date date = new Date();
        date.setTime(time);
        return date;
    }


    /***
     * 将 obj 转换为 clazz类型的值,目前只限于处理Integer、Long和String类型，后续类型增加再说
     * @param obj
     * @param clazz
     * @param <T>
     * @return
     * @throws Exception
     */
    public static <T> T getValByType(Object obj , Class<T> clazz) throws Exception{

        if(obj == null){
            if(clazz == Integer.class){
                return (T)Integer.valueOf(0);
            }else if(clazz == Long.class){
                return (T)Long.valueOf(0);
            }else if(clazz == String.class){
                return null;
            }
        }

        Object target = null;

        if(clazz == Integer.class){
            target =  Integer.valueOf((String)obj);
        }else if(clazz == Long.class){
            target =  Long.valueOf((String)obj);
        }else if(clazz == String.class){
            target = (String)obj;
        }

        return (T)target;
    }


    /****
     *
     * @param target     实例化对象
     * @param column     get的字段名称
     * @return           得到的字段值
     * @throws Exception
     */
    public static Object getColumnValByObject(Object target , String column) throws Exception{

        Field field = target.getClass().getDeclaredField(column);
        field.setAccessible(true);

        return field.get(target);
    }


    /****
     *
     * 用流水号当做表名
     * @return
     */
    public static synchronized String getUUId(){
        return UUID.randomUUID().toString().replaceAll("-", "");
    }

    /**
     * 转换zkConf 为 hadoop conf
     * @param zkcf
     * @return
     */
    public static Configuration zkconf2Hconf(ZkConfiguration zkcf){
        Configuration configuration = new Configuration();
        Iterator<String> iter = zkcf.getKeys();
        while(iter.hasNext()){
            String key = iter.next();
            configuration.set(key, zkcf.getString(key));
        }
        return configuration;
    }

    public static Hashtable<String,Object> zkconf2Htable(ZkConfiguration zkcf){
        Hashtable<String,Object>  confHtable = new Hashtable<String, Object>();
        Iterator<String> iter = zkcf.getKeys();
        while(iter.hasNext()){
            String key = iter.next();
            confHtable.put(key, zkcf.getString(key));
        }
        return confHtable;
    }

    /**
     * 将conf 配置文件转换成 Hashtable
     * @param conf
     * @return
     */
    public static Hashtable<String,Object> conf2Htable(Configuration conf){
        Hashtable<String,Object>  confHtable = new Hashtable<String, Object>();
        for (Map.Entry<String, String> stringStringEntry : conf) {
            if(StringUtils.isNotEmpty(conf.get(stringStringEntry.getKey()))) {
                confHtable.put(stringStringEntry.getKey(), stringStringEntry.getValue());
            }
        }
        return confHtable;
    }

    /**
     *  将htable 的 配置文件转换到 Configuration 中
     * @param htable
     * @return
     */
    public static Configuration Htable2Conf(Hashtable<String,Object> htable){
        Configuration configuration =new Configuration();
        for (String key : htable.keySet()) {
            configuration.set(key,String.valueOf(htable.get(key)));
        }
        return configuration;
    }

    /**
     *  合并两个配置文件 conf
     * @param conf1   conf1
     * @param conf2   conf2
     * @param sameOver  相同是否要进行覆盖
     * @return
     */
    public static Configuration unionConf(Configuration conf1, Configuration conf2, boolean sameOver){
        for (Map.Entry<String, String> stringStringEntry : conf2) {
            // conf1进行判断，如果不存在 或者 存在且覆盖参数sameOver为true
            if(StringUtils.isEmpty(conf1.get(stringStringEntry.getKey())) || sameOver) {
                conf1.set(stringStringEntry.getKey(), stringStringEntry.getValue());
            }
        }
        return conf1;
    }


    /**
     * 获得两个list的交集
     * @param a
     * @param b
     * @return
     */
    public static List getIntersect(List a,List b){
        if(a==null || b==null){
            return new ArrayList(0);
        }
        List tlist = new ArrayList(b);
        b.removeAll(a);
        tlist.removeAll(b);
        return tlist;
    }

    /**
     * 获得任意个list的间的笛卡尔积
     * @param lists
     * @return
     */
    public static Set<String> getCrossJoin(List<String>... lists){
        Set<String> result = new HashSet<String>(0);
        if(lists.length==1 || lists[1].size()==0){
            result.addAll(lists[0]);
            return result;
        }
        if(lists.length>=2) {
            for (String entity1 : lists[0]) {
                for (String entity2 : lists[1]) {
                    Set<String> tempSet =new HashSet<String>();
                    tempSet.addAll(Arrays.asList(entity1.split(",")));
                    tempSet.add(entity2);
                    result.add(StringUtils.join(tempSet, ","));
                }
            }
            if(lists.length==2) {
                return result;
            }else{
                LinkedList linkedList = new LinkedList(Arrays.asList(lists));
                linkedList.remove(0);
                lists = (List[])linkedList.toArray(new List[lists.length - 1]);
                lists[0] = new ArrayList<String>(result);
                return getCrossJoin(lists);
            }
        }
        return result;
    }

    /**
     * 简单类型数组类型转换
     * @param source
     * @param clazz
     * @param <T>
     * @return
     */
    public static <T> T[] convertArrays(Object[] source,Class<T> clazz){
        Object[] result = (Object[])Array.newInstance(clazz,source.length);
        int i=0;
        try {
            if(clazz.newInstance() instanceof String){
                for(Object o:source){
                    result[i++] = String.valueOf(o);
                }
            }
            if(clazz.newInstance() instanceof Integer){
                for(Object o:source){
                    result[i++] = Integer.valueOf(String.valueOf(o));
                }
            }
            if(clazz.newInstance() instanceof Long){
                for(Object o:source){
                    result[i++] = Long.valueOf(String.valueOf(o));
                }
            }
            if(clazz.newInstance() instanceof Double){
                for(Object o:source){
                    result[i++] = Double.valueOf(String.valueOf(o));
                }
            }
            if(clazz.newInstance() instanceof Float){
                for(Object o:source){
                    result[i++] = Float.valueOf(String.valueOf(o));
                }
            }
            if(clazz.newInstance() instanceof Boolean){
                for(Object o:source){
                    result[i++] = Boolean.valueOf(String.valueOf(o));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return (T[])result;
    }


    public static String serializer2String(Serializable object){
        byte[] bytes ;
        if(object == null) {
            bytes= new byte[0];
        } else {
            try {
                ByteArrayOutputStream ex = new ByteArrayOutputStream();
                ObjectOutputStream outputStream = new ObjectOutputStream(ex);
                outputStream.writeObject(object);
                outputStream.flush();
                outputStream.close();
                bytes= ex.toByteArray();
            } catch (Exception var4) {
                throw new IllegalArgumentException("Cannot serialize", var4);
            }
        }
        StringBuffer strBuf = new StringBuffer();
        for(int i = 0; i < bytes.length; ++i) {
            strBuf.append((char)((bytes[i] >> 4 & 15) + 97));
            strBuf.append((char)((bytes[i] & 15) + 97));
        }
        return strBuf.toString();
    }


    public static <T> T serializerString2Object(String str) {
        Serializable o = null;
        try {
            byte[] bytes = new byte[str.length() / 2];
            for(int i = 0; i < str.length(); i += 2) {
                char c = str.charAt(i);
                bytes[i / 2] = (byte)(c - 97 << 4);
                c = str.charAt(i + 1);
                bytes[i / 2] = (byte)(bytes[i / 2] + (c - 97));
            }
            ObjectInputStream ex = new ObjectInputStream(new ByteArrayInputStream(bytes));
            o = (Serializable)ex.readObject();
            ex.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return (T)o;
    }


    public static String serializer2String4JSON(ArrayList<SolrDocument> object){
        return JSON.toJSONString(object);
    }
   /* long startT =System.currentTimeMillis();
    List<SolrDocument> cacheSolrDocList= CommonUtils.serializerString2Object4JSON(data);
    System.out.println(">>>>>>> " + (System.currentTimeMillis() - startT));*/

    public static ArrayList<SolrDocument> serializerString2Object4JSON(String str) {
        ArrayList<SolrDocument> result= new ArrayList<SolrDocument>();
        long startT =System.currentTimeMillis();
        JSONArray jsonArray = (JSONArray)JSON.parse(str);
        System.out.println(">>>>>>> " + (System.currentTimeMillis() - startT));
        startT =System.currentTimeMillis();
        for (Object o : jsonArray) {
            JSONObject jo =(JSONObject)o;
            SolrDocument solrDocument =new SolrDocument();
            for(String key:jo.keySet()){
                solrDocument.addField(key,jo.get(key));
            }

            result.add(solrDocument);
        }

        System.out.println(">>>>>>>>>>>>>> " + (System.currentTimeMillis() - startT));
        return result;
    }


    public static void main(String[] agrs){


        ArrayList<SolrDocument> test= new ArrayList<SolrDocument>();
        SolrDocument solrDocument =new SolrDocument();
        solrDocument.addField("Test", "Text");
        test.add(solrDocument);
        String x =serializer2String4JSON(test);
        ArrayList<SolrDocument> testx= serializerString2Object4JSON(x);
        System.out.println(testx.get(0));
    }


    /****
     * 抽取solr字段实体类
     */
    public static class FlowData implements Serializable,Cloneable {

        private final static long serialVersionUID = 1L;




        /**
         * 》》》》》》》》Post字段》》》》》》》》》》》》
         */


        /**
         * 帖子在数据仓库的URN。主键
         */
        protected String postUrn;

        /**
         * 帖子ID。主键
         */
        protected String postId;


        /**
         * 发帖时间
         */
        protected Date createdAt;

        protected String createAtYear;

        protected String createAtWeek;

        protected String createAtMonth;

        protected String createAtMonthDate;

        protected String createAtDay;

        protected String createAtDayDate;

        protected String createAtHour;

        protected String createAtHourDate;



        /**
         * 入库时间
         */
        protected Date dwCreatedAt;


        /**
         * 帖子标题
         */
        protected String title;

        protected Set<String> hotTitles =new HashSet<String>();

        protected String hotTitle;


        /**
         * 帖子内容
         */
        protected String content;


        /**
         * 发帖用户昵称
         */
        protected String nickName;

        /**
         * 当前阅读数，查看数
         */
        protected int cntRead;

        /**
         * 点赞数
         */
        protected int cntLikes;

        /**
         * 回复数
         */
        protected int cntReplied;

        /**
         * 当前帖子的Url,版面地址
         */
        protected String postUrl;

        /**
         * 主贴，原微博，POST的URL
         */
        protected String rootPostUrl;

        /**
         * 主贴，原微博，POST的URN
         */
        protected String rootPostUrn;

        /**
         * 媒体全局ID
         */
        protected long entitySectionUrn;


        /**
         * 媒体名称
         */
        protected String entitySectionName;


        /**
         * 媒体类型全局ID
         */
        protected long serviceTypeUrn;


        /**
         * 媒体类型名称
         */
        protected String serviceTypeName;


        /**
         * 转发数
         */
        protected int cntForwarded;

        /**
         * 收藏数
         */
        protected int cntFavorited;


        /**
         * 帖子中携带的图片连接
         */
        protected String images;



        /**帖子类型**/
        protected int postType;


        /**
         * 设备客户端名称
         */
        protected String sourceUrn;



        /**
         * 项目Id
         */
        protected Set<String> items =new HashSet<String>();


        /**
         * 公司Topic
         */
        protected Set<String> topics =new HashSet<String>();

        protected String topic;




        /**
         * 》》》》》》》》User字段》》》》》》》》》》》》
         */


        /**用户全局id**/
        protected String userUrn;

        /**用户在媒体上的id**/
        protected String userId;



        /**用户类型**/
        protected int accountType;

        /**
         * 用户性别
         */
        protected String gender = "U";


        /**
         * 用户等级
         */
        protected int levelCode;





        /**
         *用户粉丝数
         */
        protected int cntFollowers;

        /**
         * 用户粉丝区间
         */
        protected String cntFollowersZone;

        /**
         *用户关注数
         */
        protected int cntFollowing;

        protected String cntFollowingZone;


        /**
         * 用户发帖数
         */
        protected int cntPosts;

        /**
         * 用户好友数
         */
        protected int cntFriends;


        /**
         * 所在国家
         */
        protected  long locCountry;
        /**
         * 所在省
         */
        protected  long locProvince;
        /**
         * 所在城市,地市级单位
         */
        protected  long locCity;

        /**
         * 城市等级
         */
        protected int locCityLevel;

        /**
         * 生日
         */
        protected Date birthday;

        /**
         * 年龄段
         */
        protected String ageZone="";


        /**
         * 用户回复数
         */
        protected  int cntReplies;


        /**
         * 用户标签
         */
        protected  Set<String> userTags =new HashSet<String>();

        protected String userTag;

        /**
         * 用户组标签
         */

        protected Set<String> tags =new HashSet<String>();

        protected String tag;


        /**
         * 教育信息
         */
        protected Set<String> educations =new HashSet<String>();

        /**
         * 工作信息
         */

        protected Set<String> jobs =new HashSet<String>();


        /**
         * 用户描述
         */

        protected String txtDescription;

        /**
         * 认证原因
         */

        protected String verifiedStr;

        /**
         * 用户收藏数
         */
        protected  int cntFavourites;


        /**
         * 用户主页
         */

        protected String profileUrl;



        /**
         * 》》》》》》》》动态追加的列》》》》》》》》》》》》
         */
        protected Map<String, Serializable> meta = new HashMap<String, Serializable>(5);


        protected Set<String> keyWords =new HashSet<String>();

        protected String keyWord;

        /**
         * index
         */

        protected int index;

        public boolean isClone =false;

        public FlowData() {
        }

        @Override
        public FlowData clone() throws CloneNotSupportedException {
            return (FlowData)super.clone();
        }


        public String getPostId() {
            return postId;
        }

        public void setPostId(String postId) {
            this.postId = postId;
        }

        public void setPostType(int postType) {
            this.postType = postType;
        }

        public void setGender(String gender) {
            if (gender!=null) {
                this.gender = gender.toUpperCase();
            }
        }

        public int getLevelCode() {
            return levelCode;
        }

        public void setLevelCode(int levelCode) {
            this.levelCode = levelCode;
        }

        public void setAccountType(int accountType) {
            this.accountType = accountType;
        }

        public int getPostType() {
            return postType;
        }

        public int getAccountType() {
            return accountType;
        }

        public String getGender() {
            return gender;
        }

        public void setPostUrn(String postUrn) {
            this.postUrn = postUrn;
        }

        public void setCreatedAt(Date createdAt)
        {
            this.createdAt = createdAt;
        }

        public String getCreateAtYear() {
            return createAtYear;
        }

        public void setCreateAtYear(String createAtYear) {
            this.createAtYear = createAtYear;
        }

        public String getCreateAtWeek() {
            return createAtWeek;
        }

        public void setCreateAtWeek(String createAtWeek) {
            this.createAtWeek = createAtWeek;
        }

        public String getCreateAtMonth() {
            return createAtMonth;
        }

        public void setCreateAtMonth(String createAtMonth) {
            this.createAtMonth = createAtMonth;
        }

        public String getCreateAtMonthDate() {
            return createAtMonthDate;
        }

        public void setCreateAtMonthDate(String createAtMonthDate) {
            this.createAtMonthDate = createAtMonthDate;
        }

        public String getCreateAtDay() {
            return createAtDay;
        }

        public void setCreateAtDay(String createAtDay) {
            this.createAtDay = createAtDay;
        }

        public String getCreateAtDayDate() {
            return createAtDayDate;
        }

        public void setCreateAtDayDate(String createAtDayDate) {
            this.createAtDayDate = createAtDayDate;
        }

        public String getCreateAtHour() {
            return createAtHour;
        }

        public void setCreateAtHour(String createAtHour) {
            this.createAtHour = createAtHour;
        }

        public String getCreateAtHourDate() {
            return createAtHourDate;
        }

        public void setCreateAtHourDate(String createAtHourDate) {
            this.createAtHourDate = createAtHourDate;
        }

        public void setTitle(String title) {
            if(StringUtils.isNotEmpty(title)) {
                this.title = title;
                /**
                 * 截取 帖子标题中井号括起来的 #热点#
                 */
    //            Matcher m = Pattern.compile("#([^#]+)#").matcher(title);
    //            while(m.find()){
    //                this.hotTitles.add(m.group(1));
    //            }
            }else{
                this.title ="";
    //            this.hotTitles.add("");
            }
        }



        public void setContent(String content) {
            if(StringUtils.isNotEmpty(content)){
                 this.content = content;
                /**
                 * 截取 帖子标题中井号括起来的 #热点#
                 */
                Matcher m = Pattern.compile("#([^#]+)#").matcher(content);
                while(m.find()){
                    this.hotTitles.add(m.group(1));
                }
            }else{
                 this.content ="";
                this.hotTitles.add("");
            }
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }

        public void setUserUrn(String userUrn) {
            this.userUrn = userUrn;
        }

        public void setNickName(String nickName) {
            if(StringUtils.isNotEmpty(nickName)){
                this.nickName = nickName;
            }else{
                this.nickName ="";
            }
        }

        public void setEntitySectionUrn(long entitySectionUrn) {
            this.entitySectionUrn = entitySectionUrn;
        }

        public void setEntitySectionName(String entitySectionName) {
            this.entitySectionName = entitySectionName;
        }

        public String getPostUrn() {
            return postUrn==null?"":postUrn;
        }

        public Date getCreatedAt() {
            return createdAt;
        }

        public String getTitle() {
            return title==null?"":title;
        }

        public String getContent() {
            return content==null?"":content;
        }

        public String getUserId() {
            return userId==null?"":userId;
        }

        public String getUserUrn() {
            return userUrn==null?"":userUrn;
        }

        public String getNickName() {
            return nickName==null?"":nickName;
        }

        public long getEntitySectionUrn() {
            return entitySectionUrn;
        }

        public String getEntitySectionName() {
            return entitySectionName==null?"":entitySectionName;
        }

        public Date getDwCreatedAt() {
            return dwCreatedAt;
        }

        public void setDwCreatedAt(Date dwCreatedAt) {
            this.dwCreatedAt = dwCreatedAt;
        }

        public int getCntRead() {
            return cntRead;
        }

        public void setCntRead(int cntRead) {
            this.cntRead = cntRead;
        }

        public int getCntLikes() {
            return cntLikes;
        }

        public void setCntLikes(int cntLikes) {
            this.cntLikes = cntLikes;
        }

        public int getCntReplied() {
            return cntReplied;
        }

        public void setCntReplied(int cntReplied) {
            this.cntReplied = cntReplied;
        }

        public String getPostUrl() {
            return postUrl==null?"":postUrl;
        }

        public void setPostUrl(String postUrl) {
            this.postUrl = postUrl;
        }

        public long getServiceTypeUrn() {
            return serviceTypeUrn;
        }

        public void setServiceTypeUrn(long serviceTypeUrn) {
            this.serviceTypeUrn = serviceTypeUrn;
        }

        public String getRootPostUrl() {
            return rootPostUrl==null?"":rootPostUrl;
        }

        public void setRootPostUrl(String rootPostUrl) {
            this.rootPostUrl = rootPostUrl;
        }

        public String getRootPostUrn() {
            return rootPostUrn==null?"":rootPostUrn;
        }

        public void setRootPostUrn(String rootPostUrn) {
            this.rootPostUrn = rootPostUrn;
        }

        public String getServiceTypeName() {
            return serviceTypeName==null?"":serviceTypeName;
        }

        public void setServiceTypeName(String serviceTypeName) {
            this.serviceTypeName = serviceTypeName;
        }

        public int getCntForwarded() {
            return cntForwarded;
        }

        public void setCntForwarded(int cntForwarded) {
            this.cntForwarded = cntForwarded;
        }

        public int getCntFavorited() {
            return cntFavorited;
        }

        public void setCntFavorited(int cntFavorited) {
            this.cntFavorited = cntFavorited;
        }

        public String getImages() {
            return images==null?"":images;
        }

        public void setImages(String images) {
            this.images = images;
        }

        public String getSourceUrn() {
            return sourceUrn;
        }

        public void setSourceUrn(String sourceUrn) {
            this.sourceUrn = sourceUrn;
        }

        public void addItems(String item){
            items.add(item);
        }

        public Set<String> putItems(){
            return  items;
        }

        public void addTopics(String topic){
            topics.add(topic);
        }


        public void addField(String key, Object v) {
            meta.put(key, (Serializable) v);
        }
        public Map<String, Serializable> putField(){
            return meta;
        }



        public int getCntFollowers() {
            return cntFollowers;
        }

        public void setCntFollowers(int cntFollowers) {
            this.cntFollowers = cntFollowers;
        }


        public int getCntFollowing() {
            return cntFollowing;
        }

        public void setCntFollowing(int cntFollowing) {
            this.cntFollowing = cntFollowing;
        }


        public String getCntFollowersZone() {
            return cntFollowersZone==null?"":cntFollowersZone;
        }

        public void setCntFollowersZone(String cntFollowersZone) {
            this.cntFollowersZone = cntFollowersZone;
        }

        public String getCntFollowingZone() {
            return cntFollowingZone==null?"":cntFollowingZone;
        }

        public void setCntFollowingZone(String cntFollowingZone) {
            this.cntFollowingZone = cntFollowingZone;
        }

        public int getCntPosts() {
            return cntPosts;
        }

        public void setCntPosts(int cntPosts) {
            this.cntPosts = cntPosts;
        }

        public int getCntFriends() {
            return cntFriends;
        }

        public void setCntFriends(int cntFriends) {
            this.cntFriends = cntFriends;
        }

        public long getLocCountry() {
            return locCountry;
        }

        public void setLocCountry(long locCountry) {
            this.locCountry = locCountry;
        }

        public long getLocProvince() {
            return locProvince;
        }

        public void setLocProvince(long locProvince) {
            this.locProvince = locProvince;
        }

        public long getLocCity() {
            return locCity;
        }

        public void setLocCity(long locCity) {
            this.locCity = locCity;
        }

        public int getLocCityLevel() {
            return locCityLevel;
        }

        public void setLocCityLevel(int locCityLevel) {
            this.locCityLevel = locCityLevel;
        }

        public Date getBirthday() {
            return birthday;
        }


        public void setBirthday(Date birthday) {
            this.birthday = birthday;

            /**
             * 根据出生日期算出年龄段
             */
            try {
                if(birthday!=null && StringUtils.isEmpty(ageZone)){
                    Calendar birthdayC = Calendar.getInstance();
                    birthdayC.setTime(birthday);
                    String year =String.valueOf(birthdayC.get(Calendar.YEAR));
                    if(year.length()==4) {
                        if (Integer.valueOf(year)>=1965) {
                            ageZone = year.substring(2);
                            if(ageZone.length()!=2){
                            ageZone = "";
                                return;
                            }
                        }else if(Integer.valueOf(year)<1965){
                            ageZone = "<65";
                            return;
                        }

                        if (StringUtils.isNotEmpty(ageZone) && !"00".equalsIgnoreCase(ageZone)) {
                            String bYearStr = ageZone;
                            if (bYearStr.substring(1).length() > 0) {
                                ageZone = bYearStr.substring(0, 1);
                                if (Integer.valueOf(bYearStr.substring(1)) >= 5) {
                                    ageZone += "5";
                                } else {
                                    ageZone += "0";
                                }
                            }
                        }
                    }
                }
            } catch (Exception e) {
            }

        }

        public String getAgeZone() {
            return ageZone==null?"":ageZone;
        }

        public void setAgeZone(String ageZone) {
            this.ageZone = ageZone;
        }

        public int getCntReplies() {
            return cntReplies;
        }

        public void setCntReplies(int cntReplies) {
            this.cntReplies = cntReplies;
        }

        public Set<String> getUserTags() {
            return userTags;
        }

        public void setUserTags(Set<String> userTags) {
            this.userTags = userTags;
        }

        public String getUserTag() {
            return userTag==null?"":userTag;
        }

        public void setUserTag(String userTag) {
            this.userTag = userTag;
        }

        public int getCntFavourites() {
            return cntFavourites;
        }

        public void setCntFavourites(int cntFavourites) {
            this.cntFavourites = cntFavourites;
        }


        public int getIndex() {
            return index;
        }

        public void setIndex(int index) {
            this.index = index;
        }

        public Set<String> getItems() {
            return items;
        }

        public void setItems(Set<String> items) {
            if (items!=null) {
                this.items = items;
            }
        }

        public Set<String> getTopics() {
            return topics;
        }

        public void setTopics(Set<String> topics) {
            if(topics!=null) {
                this.topics = topics;
            }
        }


        public String getTopic() {
            return topic==null?"":topic;
        }

        public void setTopic(String topic) {
            this.topic = topic;
        }

        public Map<String, Serializable> getMeta() {
            return meta;
        }

        public void setMeta(Map<String, Serializable> meta) {
            if(meta!=null) {
                this.meta = meta;
            }
        }


        public String getProfileUrl() {
            return profileUrl;
        }

        public void setProfileUrl(String profileUrl) {
            this.profileUrl = profileUrl;
        }


        public Set<String> getTags() {
            return tags;
        }

        public void setTags(Set<String> tags) {
            this.tags = tags;
        }

        public Set<String> getEducations() {
            return educations;
        }

        public void setEducations(Set<String> educations) {
            this.educations = educations;
        }

        public Set<String> getJobs() {
            return jobs;
        }

        public void setJobs(Set<String> jobs) {
            this.jobs = jobs;
        }

        public String getTxtDescription() {
            return txtDescription;
        }

        public void setTxtDescription(String txtDescription) {
            this.txtDescription = txtDescription;
        }

        public String getVerifiedStr() {
            return verifiedStr;
        }

        public void setVerifiedStr(String verifiedStr) {
            this.verifiedStr = verifiedStr;
        }

        public String getTag() {
            return tag;
        }

        public void setTag(String tag) {
            this.tag = tag;
        }

        public Set<String> getHotTitles() {
            return hotTitles;
        }

        public void setHotTitles(Set<String> hotTitles) {
            this.hotTitles = hotTitles;
        }

        public String getHotTitle() {
            return hotTitle;
        }

        public void setHotTitle(String hotTitle) {
            this.hotTitle = hotTitle;
        }

        public Set<String> getKeyWords() {
            return keyWords;
        }

        public void setKeyWords(Set<String> keyWords) {
            this.keyWords = keyWords;
        }

        public String getKeyWord() {
            return keyWord;
        }

        public void setKeyWord(String keyWord) {
            this.keyWord = keyWord;
        }

        @Override
        public String toString() {
            return JSON.toJSONString(this);
        }

        public static FlowData tobean(String JsonStr){
            FlowData flowData  = JSON.parseObject(JsonStr,FlowData.class);
            return flowData;
        }

        /**
         * 初始化根据现有数据计算的统计字段
         */
        public void initAccountField(Map<String,String> cityLevelTables,String[] cntFollowersZones,String[] cntFollowingZones){
            if (createdAt!=null ) {
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(createdAt);
                this.createAtYear = String.valueOf(calendar.get(Calendar.YEAR));
                this.createAtMonth = String.valueOf(calendar.get(Calendar.MONTH)+1);
                int weekDay=(calendar.get(Calendar.DAY_OF_WEEK)+6)%7;
                this.createAtWeek = String.valueOf(weekDay==0?7:weekDay);
                this.createAtDay = String.valueOf(calendar.get(Calendar.DAY_OF_MONTH));
                this.createAtHour = String.valueOf(calendar.get(Calendar.HOUR_OF_DAY));
                this.createAtMonthDate = this.createAtYear+"-"+this.createAtMonth;
                this.createAtDayDate = this.createAtMonthDate+"-"+this.createAtDay;
                this.createAtHourDate = this.createAtDayDate + " " +this.createAtHour;
            }


            if(cityLevelTables!=null && cityLevelTables.size()!=0){
                String cityLevel = cityLevelTables.get(String.valueOf(locCity));
                if(StringUtils.isNotEmpty(cityLevel)){
                    locCityLevel = Integer.valueOf(cityLevel);
                }
            }

            if(cntFollowersZones!=null && cntFollowersZones.length>0 && StringUtils.isNotEmpty(cntFollowersZones[0])){
                for(int i=0;i<cntFollowersZones.length;i++){
                   String[] cntFollowersZoneArray = StringUtils.split(cntFollowersZones[i], "-");
                    if(cntFollowersZoneArray.length==2){
                        int proNum = Integer.valueOf(cntFollowersZoneArray[0]);
                        int aftNum = Integer.valueOf(cntFollowersZoneArray[1]);
                        if((cntFollowers>proNum && cntFollowers<aftNum) || cntFollowers==proNum || cntFollowers==aftNum){
                            cntFollowersZone = cntFollowersZones[i];
                            break;
                        }
                    }else if(cntFollowersZoneArray.length==1){
                        int proNum = Integer.valueOf(cntFollowersZoneArray[0]);
                        if(cntFollowers>proNum){
                            cntFollowersZone = cntFollowersZones[i];
                            break;
                        }
                    }
                }
            }
            if(cntFollowingZones!=null && cntFollowingZones.length>0 && StringUtils.isNotEmpty(cntFollowingZones[0])){
                for(int i=0;i<cntFollowingZones.length;i++){
                    String[] cntFollowingZoneArray =StringUtils.split(cntFollowingZones[i], "-");
                    if(cntFollowingZoneArray.length==2){
                        int proNum = Integer.valueOf(cntFollowingZoneArray[0]);
                        int aftNum = Integer.valueOf(cntFollowingZoneArray[1]);
                        if((cntFollowing>proNum && cntFollowing<aftNum) || cntFollowing==proNum || cntFollowing==aftNum){
                            cntFollowingZone = cntFollowingZones[i];
                            break;
                        }
                    }else if(cntFollowingZoneArray.length==1){
                        int proNum = Integer.valueOf(cntFollowingZoneArray[0]);
                        if(cntFollowing>proNum){
                            cntFollowingZone = cntFollowingZones[i];
                            break;
                        }
                    }
                }
            }

        }


        /**
         * 多值列扁平化时，归零除第一个以外的所有克隆
         */
        public void initAccNumTypeField(){
            this.cntFavorited =0;
            this.cntFavourites =0;
            this.cntFollowers =0;
            this.cntFollowing =0;
            this.cntForwarded =0;
            this.cntFriends =0;
            this.cntLikes =0;
            this.cntPosts =0;
            this.cntRead =0;
            this.cntReplied =0;
            this.cntReplies =0;
        }

        /**
         * 构造器，初始化UserData
         * @param flowData
         */
        public void initUserData(FlowData flowData){
            this.setLevelCode(flowData.getLevelCode());
            this.setCntFollowers(flowData.getCntFollowers());
            this.setCntFollowing(flowData.getCntFollowing());
            this.setCntPosts(flowData.getCntPosts());
            this.setCntFriends(flowData.getCntFriends());
            this.setLocCountry(flowData.getLocCountry());
            this.setLocProvince(flowData.getLocProvince());
            this.setLocCity(flowData.getLocCity());
            this.setGender(flowData.getGender());
            this.setBirthday(flowData.getBirthday());
            this.setCntReplies(flowData.getCntReplies());
            this.setUserTags(flowData.getUserTags());
            this.setCntFavourites(flowData.getCntFavourites());
        }


    }
}