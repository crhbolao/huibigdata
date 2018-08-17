package katta.utils;

import com.alibaba.fastjson.JSON;
import katta.enity.FlowData;
import katta.enity.proto.FlowDataProto;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.document.*;
import org.apache.solr.common.SolrDocument;

import java.io.Serializable;
import java.util.*;

/**
 * <pre>
 * User:        liufang
 * Date:        2016/3/29  9:18
 * Email:       liufang@nowledgedata.com.cn
 * Version      V1.0
 * Company:     陕西识代运筹信息科技有限公司
 * Discription:
 *
 * Modify:      2016/3/29  9:18
 * Author:
 * </pre>
 */
public class DocConvert {
    private static Log LOG = LogFactory.getLog(DocConvert.class);

    public static Document flowData2LDoc(FlowData flowData) {
        Document doc = new Document();
        try {

            Map<String, Serializable> metaField = flowData.putField();
            for (String key : metaField.keySet()) {
                if (key.length() > 4 && "COL_".equals(key.substring(0, 4))) {
                    doc.add(new StringField("META", key.substring(4) + "_" + String.valueOf(metaField.get(key)), Field.Store.YES));
                }
            }
            doc.add(new TextField("DIMENSION", StringUtils.trimToEmpty(flowData.getDimension()), Field.Store.YES));
            doc.add(new TextField("TITLE", StringUtils.trimToEmpty(flowData.getTitle()), Field.Store.YES));
            doc.add(new TextField("CONTENT", StringUtils.trimToEmpty(flowData.getContent()), Field.Store.YES));
            doc.add(new TextField("SCREEN_NAME", StringUtils.trimToEmpty(flowData.getNickName()), Field.Store.YES));

            doc.add(new IntField("INDEX", flowData.getIndex(), Field.Store.YES));
            doc.add(new IntField("CNT_FAVOURITES", flowData.getCntFavourites(), Field.Store.YES));
            doc.add(new IntField("CNT_REPLIES", flowData.getCntReplies(), Field.Store.YES));
            doc.add(new IntField("CNT_READ", flowData.getCntRead(), Field.Store.YES));
            doc.add(new IntField("CNT_LIKES", flowData.getCntRead(), Field.Store.YES));
            doc.add(new IntField("CNT_REPLIED", flowData.getCntReplied(), Field.Store.YES));
            doc.add(new IntField("CNT_FORWARDED", flowData.getCntForwarded(), Field.Store.YES));
            doc.add(new IntField("CNT_FAVORITED", flowData.getCntFavorited(), Field.Store.YES));
            doc.add(new IntField("POST_TYPE", flowData.getPostType(), Field.Store.YES));
            doc.add(new IntField("ACCOUNT_TYPE", flowData.getAccountType(), Field.Store.YES));
            doc.add(new IntField("CNT_FOLLOWERS", flowData.getCntFollowers(), Field.Store.YES));
            doc.add(new IntField("CNT_FOLLOWING", flowData.getCntFollowing(), Field.Store.YES));
            doc.add(new IntField("CNT_POSTS", flowData.getCntPosts(), Field.Store.YES));
            doc.add(new IntField("CNT_FRIENDS", flowData.getCntFriends(), Field.Store.YES));
            doc.add(new IntField("LOC_CITY_LEVEL", flowData.getLocCityLevel(), Field.Store.YES));
            doc.add(new IntField("LEVEL_CODE", flowData.getLevelCode(), Field.Store.YES));

            doc.add(new LongField("CREATED_AT", flowData.getCreatedAt() == null ? System.currentTimeMillis() : flowData.getCreatedAt().getTime(), Field.Store.YES));
            doc.add(new LongField("BIRTHDAY", flowData.getBirthday() == null ? System.currentTimeMillis() : flowData.getBirthday().getTime(), Field.Store.YES));
            doc.add(new LongField("DW_CREATED_AT", flowData.getDwCreatedAt() == null ? System.currentTimeMillis() : flowData.getDwCreatedAt().getTime(), Field.Store.YES));
            doc.add(new LongField("LOC_COUNTRY", flowData.getLocCountry(), Field.Store.YES));
            doc.add(new LongField("LOC_PROVINCE", flowData.getLocProvince(), Field.Store.YES));
            doc.add(new LongField("LOC_CITY", flowData.getLocCity(), Field.Store.YES));
            doc.add(new LongField("ENTITY_SECTION_URN", flowData.getEntitySectionUrn(), Field.Store.YES));
            doc.add(new LongField("SERVICE_TYPE_URN", flowData.getServiceTypeUrn(), Field.Store.YES));

            doc.add(new StringField("POST_URN", StringUtils.trimToEmpty(flowData.getPostUrn()), Field.Store.YES));
            doc.add(new StringField("POST_ID", StringUtils.trimToEmpty(flowData.getPostId()), Field.Store.YES));
            doc.add(new StringField("USER_ID", StringUtils.trimToEmpty(flowData.getUserId()), Field.Store.YES));
            doc.add(new StringField("USER_URN", StringUtils.trimToEmpty(flowData.getUserUrn()), Field.Store.YES));
            doc.add(new StringField("ROOT_POST_URL", StringUtils.trimToEmpty(flowData.getRootPostUrl()), Field.Store.YES));
            doc.add(new StringField("ENTITY_SECTION_NAME", StringUtils.trimToEmpty(flowData.getEntitySectionName()), Field.Store.YES));
            doc.add(new StringField("SERVICE_TYPE_NAME", StringUtils.trimToEmpty(flowData.getServiceTypeName()), Field.Store.YES));
            doc.add(new StringField("IMAGES", StringUtils.trimToEmpty(flowData.getImages()), Field.Store.YES));
            doc.add(new StringField("SOURCE_URN", StringUtils.trimToEmpty(flowData.getSourceUrn()), Field.Store.YES));
            doc.add(new StringField("GENDER", StringUtils.trimToEmpty(flowData.getGender()), Field.Store.YES));
            doc.add(new StringField("AGE_ZONE", StringUtils.trimToEmpty(flowData.getAgeZone()), Field.Store.YES));
            doc.add(new StringField("TXT_DESCRIPTION", StringUtils.trimToEmpty(flowData.getTxtDescription()), Field.Store.YES));
            doc.add(new StringField("VERIFIED_STR", StringUtils.trimToEmpty(flowData.getVerifiedStr()), Field.Store.YES));
            doc.add(new StringField("PRO_FILE_URL", StringUtils.trimToEmpty(flowData.getProfileUrl()), Field.Store.YES));
            doc.add(new StringField("ROOT_POST_URN", StringUtils.trimToEmpty(flowData.getRootPostUrn()), Field.Store.YES));

            for (String userTag : flowData.getUserTags()) {
                doc.add(new StringField("USER_TAGS", StringUtils.trimToEmpty(userTag), Field.Store.YES));
            }

            for (String item : flowData.putItems()) {
                doc.add(new StringField("ITEMS", StringUtils.trimToEmpty(item), Field.Store.YES));
            }

            for (String tag : flowData.getTags()) {
                doc.add(new StringField("TAGS", StringUtils.trimToEmpty(tag), Field.Store.YES));
            }

            for (String edus : flowData.getEducations()) {
                doc.add(new StringField("EDUCATIONS", StringUtils.trimToEmpty(edus), Field.Store.YES));
            }

            for (String jobs : flowData.getJobs()) {
                doc.add(new StringField("JOBS", StringUtils.trimToEmpty(jobs), Field.Store.YES));
            }

            if (flowData.getTopics() != null && flowData.getTopics().size() > 0) {
                for (String topic : flowData.getTopics()) {
                    doc.add(new StringField("TOPICS", StringUtils.trimToEmpty(topic), Field.Store.YES));
                }
            } else {
                doc.add(new StringField("TOPICS", StringUtils.trimToEmpty("0"), Field.Store.YES));
            }

            String post_url = StringUtils.trimToEmpty(flowData.getPostUrl());
            if (StringUtils.isNotBlank(post_url)) {
                try {
                    post_url = JSON.parseObject(post_url).getString("url");
                } catch (Exception e) {
                    // LOG.info("errorUrl>>> "+post_url);
                    // LOG.info(ExceptionUtils.getFullStackTrace(e));
                }
            }
            doc.add(new StringField("POST_URL", post_url, Field.Store.YES));

        } catch (Exception e) {
            LOG.info(ExceptionUtils.getFullStackTrace(e));
        }
        return doc;
    }


    public static List<Document> flowData2LDoc(List<FlowData> flowDataList) {
        List<Document> documentList = new ArrayList<Document>();
        if (flowDataList != null) {
            for (FlowData flowData : flowDataList) {
                Document doc = flowData2LDoc(flowData);
                documentList.add(doc);
            }
        }
        return documentList;
    }

    public static FlowData kattaSolrDoc2FlowData(SolrDocument solrDocument) {
        FlowData flowData = new FlowData();
        if (solrDocument.get("META") != null) {
            if (solrDocument.get("META") instanceof List) {
                for (String str : (List<String>) solrDocument.get("META")) {
                    String[] keyVal = str.split("_");
                    flowData.addField("COL_" + keyVal[0], keyVal[1]);
                }
            } else {
                String[] keyVal = String.valueOf(solrDocument.get("META")).split("_");
                flowData.addField("COL_" + keyVal[0], keyVal[1]);
            }
        }
        if (solrDocument.get("ITEMS") != null) {
            if (solrDocument.get("ITEMS") instanceof List) {
                for (String item : (List<String>) solrDocument.get("ITEMS")) {
                    flowData.addItems(item);
                }
            } else {
                flowData.addItems(String.valueOf(solrDocument.get("ITEMS")));
            }
        }
        if (solrDocument.get("TOPICS") != null) {
            if (solrDocument.get("TOPICS") instanceof List) {
                for (String topic : (List<String>) solrDocument.get("TOPICS")) {
                    flowData.addTopics(topic);
                }
            } else {
                flowData.addTopics(String.valueOf(solrDocument.get("TOPICS")));
            }
        }

        if (solrDocument.get("USER_TAGS") != null) {
            if (solrDocument.get("USER_TAGS") instanceof List) {
                for (String userTag : (List<String>) solrDocument.get("USER_TAGS")) {
                    flowData.getUserTags().add(userTag);
                }
            } else {
                flowData.getUserTags().add(String.valueOf(solrDocument.get("USER_TAGS")));
            }
        }

        if (solrDocument.get("TAGS") != null) {
            if (solrDocument.get("TAGS") instanceof List) {
                for (String tag : (List<String>) solrDocument.get("TAGS")) {
                    flowData.getTags().add(tag);
                }
            } else {
                flowData.getTags().add(String.valueOf(solrDocument.get("TAGS")));
            }
        }

        if (solrDocument.get("EDUCATIONS") != null) {
            if (solrDocument.get("EDUCATIONS") instanceof List) {
                for (String edus : (List<String>) solrDocument.get("EDUCATIONS")) {
                    flowData.getEducations().add(edus);
                }
            } else {
                flowData.getEducations().add(String.valueOf(solrDocument.get("EDUCATIONS")));
            }
        }

        if (solrDocument.get("JOBS") != null) {
            if (solrDocument.get("JOBS") instanceof List) {
                for (String jobs : (List<String>) solrDocument.get("JOBS")) {
                    flowData.getJobs().add(jobs);
                }
            } else {
                flowData.getJobs().add(String.valueOf(solrDocument.get("JOBS")));
            }
        }


        try {
            flowData.setDimension(CommonUtils.getValByType(solrDocument.get("DIMENSION"), String.class));
            flowData.setIndex(CommonUtils.getValByType(solrDocument.get("INDEX"), Integer.class));
            flowData.setPostUrn(CommonUtils.getValByType(solrDocument.get("POST_URN"), String.class));
            flowData.setPostId(CommonUtils.getValByType(solrDocument.get("POST_ID"), String.class));
            flowData.setCreatedAt(new Date(CommonUtils.getValByType(solrDocument.get("CREATED_AT"), Long.class)));
            flowData.setTitle(CommonUtils.getValByType(solrDocument.get("TITLE"), String.class));
            flowData.setContent(CommonUtils.getValByType(solrDocument.get("CONTENT"), String.class));
            flowData.setUserId(CommonUtils.getValByType(solrDocument.get("USER_ID"), String.class));
            flowData.setUserUrn(CommonUtils.getValByType(solrDocument.get("USER_URN"), String.class));
            flowData.setNickName(CommonUtils.getValByType(solrDocument.get("SCREEN_NAME"), String.class));
            flowData.setCntRead(CommonUtils.getValByType(solrDocument.get("CNT_READ"), Integer.class));
            flowData.setCntLikes(CommonUtils.getValByType(solrDocument.get("CNT_LIKES"), Integer.class));
            flowData.setCntReplied(CommonUtils.getValByType(solrDocument.get("CNT_REPLIED"), Integer.class));
            flowData.setPostUrl(CommonUtils.getValByType(solrDocument.get("POST_URL"), String.class));
            flowData.setRootPostUrl(CommonUtils.getValByType(solrDocument.get("ROOT_POST_URL"), String.class));
            flowData.setEntitySectionUrn(CommonUtils.getValByType(solrDocument.get("ENTITY_SECTION_URN"), Long.class));
            flowData.setEntitySectionName(CommonUtils.getValByType(solrDocument.get("ENTITY_SECTION_NAME"), String.class));
            flowData.setServiceTypeUrn(CommonUtils.getValByType(solrDocument.get("SERVICE_TYPE_URN"), Long.class));
            flowData.setServiceTypeName(CommonUtils.getValByType(solrDocument.get("SERVICE_TYPE_NAME"), String.class));
            flowData.setCntForwarded(CommonUtils.getValByType(solrDocument.get("CNT_FORWARDED"), Integer.class));
            flowData.setCntFavorited(CommonUtils.getValByType(solrDocument.get("CNT_FAVORITED"), Integer.class));
            flowData.setImages(CommonUtils.getValByType(solrDocument.get("IMAGES"), String.class));
            flowData.setPostType(CommonUtils.getValByType(solrDocument.get("POST_TYPE"), Integer.class));
            flowData.setSourceUrn(CommonUtils.getValByType(solrDocument.get("SOURCE_URN"), String.class));
            flowData.setAccountType(CommonUtils.getValByType(solrDocument.get("ACCOUNT_TYPE"), Integer.class));
            flowData.setGender(CommonUtils.getValByType(solrDocument.get("GENDER"), String.class));
            flowData.setCntFollowers(CommonUtils.getValByType(solrDocument.get("CNT_FOLLOWERS"), Integer.class));
            flowData.setCntFollowing(CommonUtils.getValByType(solrDocument.get("CNT_FOLLOWING"), Integer.class));
            flowData.setCntPosts(CommonUtils.getValByType(solrDocument.get("CNT_POSTS"), Integer.class));
            flowData.setCntFriends(CommonUtils.getValByType(solrDocument.get("CNT_FRIENDS"), Integer.class));
            flowData.setLocCountry(CommonUtils.getValByType(solrDocument.get("LOC_COUNTRY"), Long.class));
            flowData.setLocProvince(CommonUtils.getValByType(solrDocument.get("LOC_PROVINCE"), Long.class));
            flowData.setLocCity(CommonUtils.getValByType(solrDocument.get("LOC_CITY"), Long.class));
            flowData.setLocCityLevel(CommonUtils.getValByType(solrDocument.get("LOC_CITY_LEVEL"), Integer.class));
            flowData.setBirthday(new Date(CommonUtils.getValByType(solrDocument.get("BIRTHDAY"), Long.class)));
            flowData.setLevelCode(CommonUtils.getValByType(solrDocument.get("LEVEL_CODE"), Integer.class));
            flowData.setAgeZone(CommonUtils.getValByType(solrDocument.get("AGE_ZONE"), String.class));
            flowData.setCntReplies(CommonUtils.getValByType(solrDocument.get("CNT_REPLIES"), Integer.class));
            flowData.setCntFavourites(CommonUtils.getValByType(solrDocument.get("CNT_FAVOURITES"), Integer.class));
            flowData.setProfileUrl(CommonUtils.getValByType(solrDocument.get("PRO_FILE_URL"), String.class));
            flowData.setTxtDescription(CommonUtils.getValByType(solrDocument.get("TXT_DESCRIPTION"), String.class));
            flowData.setVerifiedStr(CommonUtils.getValByType(solrDocument.get("VERIFIED_STR"), String.class));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return flowData;
    }


    public static List<FlowData> kattaSolrDoc2FlowData(List<SolrDocument> solrDocuments) {
        List<FlowData> flowDatas = new ArrayList<FlowData>();
        if (solrDocuments != null) {
            for (SolrDocument entries : solrDocuments) {
                FlowData flowData = kattaSolrDoc2FlowData(entries);
                flowDatas.add(flowData);
            }
        }
        return flowDatas;
    }


    public static FlowData solrDoc2FlowData(SolrDocument sd) {
        FlowData flowData = new FlowData();
        int timeDiff = TimeZone.getDefault().getRawOffset() - TimeZone.getTimeZone("GMT").getRawOffset();
        try {
            flowData.setPostUrn(CommonUtils.getValByType(sd.get("POST_URN"), String.class));
            flowData.setPostId(CommonUtils.getValByType(sd.get("POST_ID"), String.class));
            Date createdAt = CommonUtils.parstLongForDate(CommonUtils.getValByType(sd.get("CREATED_AT"), Long.class));
            if (createdAt != null) {
                createdAt.setTime(createdAt.getTime() - timeDiff);
                flowData.setCreatedAt(createdAt);
            }
            Date dwCreatedAt = CommonUtils.parstLongForDate(CommonUtils.getValByType(sd.get("DW_CREATED_AT"), Long.class));
            if (dwCreatedAt != null) {
                dwCreatedAt.setTime(dwCreatedAt.getTime() - timeDiff);
                flowData.setDwCreatedAt(dwCreatedAt);
            }
            flowData.setDimension(CommonUtils.getValByType(sd.get("DIMENSION"), String.class));
            flowData.setTitle(CommonUtils.getValByType(sd.get("TITLE"), String.class));
            flowData.setContent(CommonUtils.getValByType(sd.get("CONTENT"), String.class));
            flowData.setUserId(CommonUtils.getValByType(sd.get("USER_ID"), String.class));
            flowData.setUserUrn(CommonUtils.getValByType(sd.get("USER_URN"), String.class));
            flowData.setNickName(CommonUtils.getValByType(sd.get("SCREEN_NAME"), String.class));
            flowData.setCntRead(CommonUtils.getValByType(sd.get("CNT_READ"), Integer.class));
            flowData.setCntLikes(CommonUtils.getValByType(sd.get("CNT_LIKES"), Integer.class));
            flowData.setCntReplied(CommonUtils.getValByType(sd.get("CNT_REPLIED"), Integer.class));
            flowData.setPostUrl(CommonUtils.getValByType(sd.get("EXTENSION"), String.class));
            flowData.setRootPostUrl(CommonUtils.getValByType(sd.get("ROOT_POST_URL"), String.class));
            flowData.setRootPostUrn(CommonUtils.getValByType(sd.get("ROOT_POST_URN"), String.class));
            flowData.setEntitySectionUrn(CommonUtils.getValByType(sd.get("ENTITY_SECTION_URN"), Long.class));
            flowData.setEntitySectionName(CommonUtils.getValByType(sd.get("ENTITY_SECTION_NAME"), String.class));
            flowData.setServiceTypeUrn(CommonUtils.getValByType(sd.get("SERVICE_TYPE_URN"), Long.class));
            flowData.setServiceTypeName(CommonUtils.getValByType(sd.get("SERVICE_TYPE_NAME"), String.class));
            flowData.setCntForwarded(CommonUtils.getValByType(sd.get("CNT_FORWARDED"), Integer.class));
            flowData.setCntFavorited(CommonUtils.getValByType(sd.get("CNT_FAVORITED"), Integer.class));
            flowData.setImages(CommonUtils.getValByType(sd.get("IMAGES"), String.class));
            flowData.setPostType(CommonUtils.getValByType(sd.get("POST_TYPE"), Integer.class));
            flowData.setSourceUrn(CommonUtils.getValByType(sd.get("SOURCE_URN"), String.class));
            flowData.setAccountType(CommonUtils.getValByType(sd.get("ACCOUNT_TYPE"), Integer.class));
            flowData.setGender(CommonUtils.getValByType(sd.get("GENDER"), String.class));
            flowData.setTitle(CommonUtils.getValByType(sd.get("TITLE"), String.class));
            if (sd.get("ITEMS") != null) {
                if (sd.get("ITEMS") instanceof List) {
                    for (String item : (List<String>) sd.get("ITEMS")) {
                        if (StringUtils.isNotBlank(item)) {
                            flowData.addItems(item);
                        }
                    }
                } else {
                    flowData.addItems(String.valueOf(sd.get("ITEMS")));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return flowData;
    }





    public static FlowData proto2FlowData(FlowDataProto.FlowDataMsg flowDataMsg) {
        FlowData flowData = new FlowData();
        flowData.setDimension(flowDataMsg.getDimension());
        flowData.setPostUrn(flowDataMsg.getPostUrn());
        flowData.setPostId(flowDataMsg.getPostId());
        flowData.setCreatedAt(CommonUtils.parstLongForDate(flowDataMsg.getCreatedAt()));
        flowData.setDwCreatedAt(CommonUtils.parstLongForDate(flowDataMsg.getDwCreatedAt()));
        flowData.setTitle(flowDataMsg.getTitle());
        flowData.setContent(flowDataMsg.getContent());
        flowData.setNickName(flowDataMsg.getNickName());
        flowData.setCntRead(flowDataMsg.getCntRead());
        flowData.setCntLikes(flowDataMsg.getCntLikes());
        flowData.setCntReplied(flowDataMsg.getCntReplied());
        flowData.setPostUrl(flowDataMsg.getPostUrl());
        flowData.setRootPostUrl(flowDataMsg.getRootPostUrl());
        flowData.setRootPostUrn(flowDataMsg.getRootPostUrn());
        flowData.setEntitySectionUrn(flowDataMsg.getEntitySectionUrn());
        flowData.setEntitySectionName(flowDataMsg.getEntitySectionName());
        flowData.setServiceTypeUrn(flowDataMsg.getServiceTypeUrn());
        flowData.setServiceTypeName(flowDataMsg.getServiceTypeName());
        flowData.setCntForwarded(flowDataMsg.getCntForwarded());
        flowData.setCntFavorited(flowDataMsg.getCntFavorited());
        flowData.setImages(flowDataMsg.getImages());
        flowData.setPostType(flowDataMsg.getPostType());
        flowData.setSourceUrn(flowDataMsg.getSourceUrn());
        if (flowDataMsg.getItemsList() != null) {
            flowData.setItems(new HashSet(flowDataMsg.getItemsList()));
        }
        flowData.setUserUrn(flowDataMsg.getUserUrn());
        flowData.setUserId(flowDataMsg.getUserId());
        flowData.setAccountType(flowDataMsg.getAccountType());
        flowData.setGender(flowDataMsg.getGender());
        flowData.setLevelCode(flowDataMsg.getLevelCode());
        flowData.setCntFollowers(flowDataMsg.getCntFollowers());
        flowData.setCntFollowing(flowDataMsg.getCntFollowing());
        flowData.setCntPosts(flowDataMsg.getCntPosts());
        flowData.setCntFriends(flowDataMsg.getCntFriends());
        flowData.setLocCountry(flowDataMsg.getLocCountry());
        flowData.setLocProvince(flowDataMsg.getLocProvince());
        flowData.setLocCity(flowDataMsg.getLocCity());
        flowData.setLocCityLevel(flowDataMsg.getLocCityLevel());
        flowData.setBirthday(CommonUtils.parstLongForDate(flowDataMsg.getBirthday()));
        flowData.setCntReplies(flowDataMsg.getCntReplies());
        if (flowDataMsg.getUserTagsList() != null) {
            flowData.setUserTags(new HashSet(flowDataMsg.getUserTagsList()));
        }
        if (flowDataMsg.getTagsList() != null) {
            flowData.setTags(new HashSet<String>(flowDataMsg.getTagsList()));
        }
        if (flowDataMsg.getEducationsList() != null) {
            flowData.setEducations(new HashSet<String>(flowDataMsg.getEducationsList()));
        }
        if (flowDataMsg.getJobsList() != null) {
            flowData.setJobs(new HashSet<String>(flowDataMsg.getJobsList()));
        }
        flowData.setTxtDescription(flowDataMsg.getTxtDescription());
        flowData.setVerifiedStr(flowDataMsg.getVerifiedStr());
        flowData.setCntFavourites(flowDataMsg.getCntFavourites());
        flowData.setProfileUrl(flowDataMsg.getProfileUrl());
        return flowData;
    }

    public static FlowDataProto.FlowDataMsg flowData2Proto(FlowData flowData) {
        FlowDataProto.FlowDataMsg.Builder builder = FlowDataProto.FlowDataMsg.newBuilder();
        if (StringUtils.isNotEmpty(flowData.getDimension())){
            builder.setDimension(flowData.getDimension());
        }

        if (StringUtils.isNotEmpty(flowData.getPostUrn())) {
            builder.setPostUrn(flowData.getPostUrn());
        }
        if (StringUtils.isNotEmpty(flowData.getPostId())) {
            builder.setPostId(flowData.getPostId());
        }

        if (flowData.getCreatedAt() != null) {
            builder.setCreatedAt(flowData.getCreatedAt().getTime());
        }
        if (flowData.getDwCreatedAt() != null) {
            builder.setDwCreatedAt(flowData.getDwCreatedAt().getTime());
        }
        if (StringUtils.isNotEmpty(flowData.getTitle())) {
            builder.setTitle(flowData.getTitle());
        }
        if (StringUtils.isNotEmpty(flowData.getContent())) {
            builder.setContent(flowData.getContent());
        }
        if (StringUtils.isNotEmpty(flowData.getNickName())) {
            builder.setNickName(flowData.getNickName());
        }
        builder.setCntRead(flowData.getCntRead());
        builder.setCntLikes(flowData.getCntLikes());
        builder.setCntReplied(flowData.getCntReplied());
        builder.setLocCityLevel(flowData.getLocCityLevel());
        if (StringUtils.isNotEmpty(flowData.getPostUrl())) {
            builder.setPostUrl(flowData.getPostUrl());
        }
        if (StringUtils.isNotEmpty(flowData.getRootPostUrl())) {
            builder.setRootPostUrl(flowData.getRootPostUrl());
        }
        if (StringUtils.isNotEmpty(flowData.getRootPostUrn())) {
            builder.setRootPostUrn(flowData.getRootPostUrn());
        }
        builder.setEntitySectionUrn(flowData.getEntitySectionUrn());
        if (StringUtils.isNotEmpty(flowData.getEntitySectionName())) {
            builder.setEntitySectionName(flowData.getEntitySectionName());
        }
        builder.setServiceTypeUrn(flowData.getServiceTypeUrn());
        if (StringUtils.isNotEmpty(flowData.getServiceTypeName())) {
            builder.setServiceTypeName(flowData.getServiceTypeName());
        }
        builder.setCntForwarded(flowData.getCntForwarded());
        builder.setCntFavorited(flowData.getCntFavorited());
        if (StringUtils.isNotEmpty(flowData.getImages())) {
            builder.setImages(flowData.getImages());
        }
        builder.setPostType(flowData.getPostType());
        if (StringUtils.isNotEmpty(flowData.getSourceUrn())) {
            builder.setSourceUrn(flowData.getSourceUrn());
        }
        if (flowData.getItems() != null && flowData.getItems().size() != 0) {
            builder.addAllItems(flowData.getItems());
        }
        if (StringUtils.isNotEmpty(flowData.getUserUrn())) {
            builder.setUserUrn(flowData.getUserUrn());
        }
        if (StringUtils.isNotEmpty(flowData.getUserId())) {
            builder.setUserId(flowData.getUserId());
        }
        builder.setAccountType(flowData.getAccountType());
        if (StringUtils.isNotEmpty(flowData.getGender())) {
            builder.setGender(flowData.getGender());
        }
        builder.setLevelCode(flowData.getLevelCode());
        builder.setCntFollowers(flowData.getCntFollowers());
        builder.setCntFollowing(flowData.getCntFollowing());
        builder.setCntPosts(flowData.getCntPosts());
        builder.setCntFriends(flowData.getCntFriends());
        builder.setLocCountry(flowData.getLocCountry());
        builder.setLocProvince(flowData.getLocProvince());
        builder.setLocCity(flowData.getLocCity());
        if (flowData.getBirthday() != null) {
            builder.setBirthday(flowData.getBirthday().getTime());
        }
        builder.setCntReplies(flowData.getCntReplies());
        if (flowData.getUserTags() != null && flowData.getUserTags().size() != 0) {
            builder.addAllUserTags(flowData.getUserTags());
        }

        if (flowData.getTags() != null && flowData.getTags().size() != 0) {
            builder.addAllTags(flowData.getTags());
        }
        if (flowData.getEducations() != null && flowData.getEducations().size() != 0) {
            builder.addAllEducations(flowData.getEducations());
        }
        if (flowData.getJobs() != null && flowData.getJobs().size() != 0) {
            builder.addAllJobs(flowData.getJobs());
        }

        builder.setCntFavourites(flowData.getCntFavourites());
        if (flowData.getProfileUrl() != null) {
            builder.setProfileUrl(flowData.getProfileUrl());
        }

        if (flowData.getTxtDescription() != null) {
            builder.setTxtDescription(flowData.getTxtDescription());
        }

        if (flowData.getVerifiedStr() != null) {
            builder.setVerifiedStr(flowData.getVerifiedStr());
        }

        return builder.build();
    }

}
