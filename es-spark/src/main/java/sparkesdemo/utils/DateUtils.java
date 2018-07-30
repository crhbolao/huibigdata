package sparkesdemo.utils;

import org.joda.time.DateTime;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created with IntelliJ IDEA.
 * User: bolao
 * Date: 2018/7/30 9:23
 * Version: V1.0
 * To change this template use File | Settings | File Templates.
 * Description:   spark 按时间段查询数据的时候需要指定es的时间格式
 */
public class DateUtils {

    // es 当天的开始世间 00：00
    private static final String ES_BEGIN_DATE_FORMAT_PATTERN = "yyyy-MM-dd'T'00:00:00";

    // es 当前时间
    private static final String ES_DATE_FORMAT_PATTERN = "yyyy-MM-dd'T'HH:mm:ss";

    // es 当天的结束时间
    private static final String ES_END_DATE_FORMAT_PATTERN = "yyyy-MM-dd'T'23:59:59";

    /**
     * es 当天开始时间
     */
    public static final ThreadLocal<DateFormat> ES_BEGIN_DATE_FORMAT = new ThreadLocal<DateFormat>() {
        @Override
        protected DateFormat initialValue() {
            return new SimpleDateFormat(ES_BEGIN_DATE_FORMAT_PATTERN);
        }
    };

    /**
     * es 当前时间
     */
    public static final ThreadLocal<DateFormat> ES_DATE_FORMAT = new ThreadLocal<DateFormat>() {
        @Override
        protected DateFormat initialValue() {
            return new SimpleDateFormat(ES_DATE_FORMAT_PATTERN);
        }
    };

    /**
     * es 当天结束时间
     */
    public static final ThreadLocal<DateFormat> ES_END_DATE_FORMAT = new ThreadLocal<DateFormat>() {
        @Override
        protected DateFormat initialValue() {
            return new SimpleDateFormat(ES_END_DATE_FORMAT_PATTERN);
        }
    };


    public static void main(String[] args) {
        DateTime now = DateTime.now();
        Date yday = now.minusDays(1).toDate();    // 提前一天。
        // 当天开始时间
        String startTime = DateUtils.ES_BEGIN_DATE_FORMAT.get().format(yday);
        // 当天结束时间
        String endTime = DateUtils.ES_END_DATE_FORMAT.get().format(yday);
        // 获取的是当前时间
        DateUtils.ES_DATE_FORMAT.get().format(new DateTime().toDate());
    }

}
