package hdfs.java.utils;

import org.apache.commons.io.IOUtils;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import hdfs.java.entity.FileInfo;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;


/**
 * Created with IntelliJ IDEA.
 * User: bolao
 * Date: 2018/7/28 16:24
 * Version: V1.0
 * To change this template use File | Settings | File Templates.
 * Description:    java hdfs 的相关操作
 */
public class HdfsUtils {

    public static FileSystem fs;

    static {
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("applicationContext.xml");
        FileSystem fileSystem = (FileSystem) context.getBean("fileSystem");
        fs = fileSystem;
    }

    /**
     * 将本地压缩文件直接解压上传到hdfs上
     *
     * @param path         本地压缩包存放的路径
     * @param savaHdfsPath hdfs save path
     * @return
     */
    public static FileInfo unZipToHdfs(String path, String savaHdfsPath) {
        FileInfo fileInfo = new FileInfo();
        Path hdfsPath = new Path(savaHdfsPath);
        InputStream inputStream = null;
        OutputStream outputStream = null;
        try {
            //获取本地的压缩文件
            ZipFile zipFile = new ZipFile(new File(path));
            String fileName = zipFile.getName();
            fileInfo.setExtendName(fileName);

            // 判断hdfs上是否存在要保存的路径
            if (!fs.exists(hdfsPath) || !fs.isDirectory(hdfsPath)) {
                if (!fs.mkdirs(hdfsPath)) {
                    StringBuffer sb = new StringBuffer();
                    sb.append("hdfs 无法创建目录！");
                    throw new IOException(sb.toString());
                }
            }

            int fileNum = 0;
            long totalSize = 0;
            Path tempPath = null;
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                if (entry.isDirectory()) {
                    // 如果是文件加的话，则fs上创建对应的文件夹
                    Path dirPath = new Path(savaHdfsPath, entry.getName());
                    fs.mkdirs(dirPath);
                    continue;
                }
                Path filePath = new Path(savaHdfsPath, entry.getName());
                inputStream = zipFile.getInputStream(entry);
                // 总的文件大小
                totalSize += inputStream.available();
                //文件总数
                if (!entry.getName().toString().endsWith(".xml") && !entry.getName().toString().endsWith(".avro")) {
                    fileNum += 1;
                }
                outputStream = fs.create(filePath, true);
                IOUtils.copy(inputStream, outputStream);
                outputStream.flush();
                fileInfo.setFileNum(fileNum);
                fileInfo.setFileSize(totalSize);
                inputStream.close();
                outputStream.close();
            }
            return fileInfo;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return fileInfo;
    }


    public static void main(String[] args) {
        String sourcePath = "C:\\Users\\sssd\\Desktop\\testzip.zip";
        String hdfsPath = "hdfs://new1data/user/test1";
        HdfsUtils.unZipToHdfs(sourcePath, hdfsPath);
    }

}
