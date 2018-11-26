package spark.submit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.spark.launcher.SparkAppHandle;
import org.apache.spark.launcher.SparkLauncher;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

/**
 * Created with IntelliJ IDEA.
 * User: bolao
 * Date: 2018/11/26 10:45
 * Version: V1.0
 * To change this template use File | Settings | File Templates.
 * Description:   java spark 提交任务
 */
public class SubmitMain {

    /**
     * 日志
     */
    public Log LOG = LogFactory.getLog(SubmitMain.class);

    /**
     * 加载配置文件
     */
    public static ClassPathXmlApplicationContext context;

    /**
     * 初始化加载配置文件
     */
    static {
        context = new ClassPathXmlApplicationContext("applicationContext.xml");
    }

    /**
     * 程序的执行入口
     */
    public void exture() throws Exception {
        long start = System.currentTimeMillis();
        final CountDownLatch countDownLatch = new CountDownLatch(1);
        Map params = (Map) context.getBean("params");

        HashMap<String, String> map = new HashMap<String, String>();
        map.put("HADOOP_CONF_DIR", String.valueOf(params.get("HADOOP_CONF_DIR")));
        map.put("YARN_CONF_DIR", String.valueOf(params.get("YARN_CONF_DIR")));
        map.put("SPARK_CONF_DIR", String.valueOf(params.get("SPARK_CONF_DIR")));
        map.put("SPARK_HOME", String.valueOf(params.get("SPARK_HOME")));
        map.put("JAVA_HOME", String.valueOf(params.get("JAVA_HOME")));
        String appArgs = String.valueOf(params.get("appArgs"));
        String[] split = appArgs.split(",");
        SparkAppHandle spark = new SparkLauncher(map)
                .setDeployMode(String.valueOf(params.get("deployMode")))
                .setAppResource(String.valueOf(params.get("appResource")))
                .setMainClass(String.valueOf(params.get("mainClass")))
                .setAppName(String.valueOf(params.get("appName")))
                .setMaster(String.valueOf(params.get("master")))
                .setConf(SparkLauncher.DRIVER_MEMORY, String.valueOf(params.get("spark.driver.memory")))
                .setConf(SparkLauncher.EXECUTOR_MEMORY, String.valueOf(params.get("spark.executor.memory")))
                .setConf(SparkLauncher.EXECUTOR_CORES, String.valueOf(params.get("spark.executor.cores")))
                .addAppArgs(split)
                .startApplication(new SparkAppHandle.Listener() {
                    @Override
                    public void stateChanged(SparkAppHandle handle) {
                        if (handle.getState().isFinal()) {
                            countDownLatch.countDown();
                        }
                    }

                    @Override
                    public void infoChanged(SparkAppHandle handle) {
                        LOG.info("**********  info  changed  **********");
                    }
                });
        countDownLatch.await();
        long end = System.currentTimeMillis();
        LOG.info("任务运行的时间为：" + (end - start));
    }

    public static void main(String[] args) throws Exception {
        SubmitMain submitMain = new SubmitMain();
        submitMain.exture();
    }
}
