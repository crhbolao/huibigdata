package javaesdemo.utils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

/**
 * Created with IntelliJ IDEA.
 * User: bolao
 * Date: 2018/7/14 13:46
 * Version: V1.0
 * To change this template use File | Settings | File Templates.
 * Description:   关于时间的工具类
 */
public class DateUtils {

    private static final String ES_BEGIN_DATE_FORMAT_PATTERN = "yyyy-MM-dd'T'00:00:00";

    private static final String ES_DATE_FORMAT_PATTERN = "yyyy-MM-dd'T'HH:mm:ss";


    public static final ThreadLocal<DateFormat> ES_BEGIN_DATE_FORMAT = new ThreadLocal<DateFormat>() {
        @Override
        protected DateFormat initialValue() {
            return new SimpleDateFormat(ES_BEGIN_DATE_FORMAT_PATTERN);
        }
    };

    public static final ThreadLocal<DateFormat> ES_DATE_FORMAT = new ThreadLocal<DateFormat>() {
        @Override
        protected DateFormat initialValue() {
            return new SimpleDateFormat(ES_DATE_FORMAT_PATTERN);
        }
    };


}
