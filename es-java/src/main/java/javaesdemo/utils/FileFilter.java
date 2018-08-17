package javaesdemo.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.util.Date;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: bolao
 * Date: 2018/7/25 15:11
 * Version: V1.0
 * To change this template use File | Settings | File Templates.
 * Description:    对es的数据过滤掉不必要的字段
 */
public class FileFilter {

    /**
     * 主要是进行文件过滤
     */
    public void filter() {

        String sourcePath = "C:\\Users\\sssd\\Desktop\\es.txt";
        String savaPath = "C:\\Users\\sssd\\Desktop\\newes.txt";
        Date date = new Date();
        String format = DateUtils.ES_DATE_FORMAT.get().format(date);
        try {
            List<String> lines = FileUtils.readLines(new File(sourcePath), "UTF-8");
            Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(savaPath), true), "UTF-8"));
            for (String line : lines) {
                JSONObject jsonObject = (JSONObject) JSON.parse(line);
                if (jsonObject.containsKey("userlocation")) {
                    jsonObject.remove("userlocation");
                }
                jsonObject.put("DW_UPDATED_AT", format);
                String tempStr = jsonObject.toJSONString() + "\n";
                writer.write(tempStr);
            }
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        FileFilter fileFilter = new FileFilter();
        fileFilter.filter();
    }

}
