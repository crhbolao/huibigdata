package hdfs.java.utils;

import org.apache.commons.io.IOUtils;
import org.apache.hadoop.fs.*;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import hdfs.java.entity.FileInfo;

import java.io.*;
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
                inputStream.close();
                outputStream.close();
            }
            fileInfo.setFileNum(fileNum);
            fileInfo.setFileSize(totalSize);
            return fileInfo;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return fileInfo;
    }

    /**
     * 从hdfs上下载数据到本地
     *
     * @param hdfsPath hdfs上的文件
     * @param savapath 本地的文件
     */
    public static void fileReadFromHdfs(String hdfsPath, String savapath) {
        try {
            FileStatus[] fileStatuses = fs.listStatus(new Path(hdfsPath));
            for (FileStatus status : fileStatuses) {
                if (status.isDirectory()) {
                    String tempPath = status.getPath().toString();
                    String dirName = status.getPath().getName();
                    File file = new File(savapath, dirName);
                    if (!file.mkdir()) {
                        System.out.println("本地创建文件目录失败！");
                        break;
                    }
                    fileReadFromHdfs(tempPath, file.getPath());
                } else if (status.isFile()) {
                    FSDataInputStream inputStream = fs.open(status.getPath());
                    File file = new File(savapath, status.getPath().getName());
                    FileOutputStream outputStream = new FileOutputStream(file);
                    byte[] ioBuffer = new byte[1024];
                    int readNum = inputStream.read(ioBuffer);
                    while (readNum != -1) {
                        outputStream.write(ioBuffer, 0, readNum);
                        readNum = inputStream.read(ioBuffer);
                    }
                    inputStream.close();
                    outputStream.close();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 上传本地文件到hdfs
     *
     * @param localpath 本地待保存文件
     * @param hdfspath  hdfs上的文件保存路径
     */
    public static void pushFile2Hdfs(String localpath, String hdfspath) {
        File file = new File(localpath);
        File[] files = file.listFiles();
        try {
            for (File tempFile : files) {
                if (tempFile.isDirectory()) {
                    String dirName = tempFile.getName();
                    Path path = new Path(hdfspath, dirName);
                    if (!fs.exists(path) && !fs.mkdirs(path)) {
                        System.out.println("该目录不存在且不能创建对应的文件目录！");
                    }
                    pushFile2Hdfs(tempFile.getPath(), path.toString());
                } else if (tempFile.isFile()) {
                    FileInputStream inputStream = new FileInputStream(tempFile);
                    Path path = new Path(hdfspath, tempFile.getName());
                    FSDataOutputStream outputStream = fs.create(path, true);
                    IOUtils.copy(inputStream, outputStream);
                    outputStream.flush();
                    outputStream.close();
                    inputStream.close();
                } else {
                    System.out.println("未知的文件类型！");
                    continue;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static void main(String[] args) {
/*        String sourcePath = "C:\\Users\\sssd\\Desktop\\testzip.zip";
        String hdfsPath = "hdfs://new1data/user/test1";
        HdfsUtils.unZipToHdfs(sourcePath, hdfsPath);*/


/*        String hdfspath = "hdfs://new1data/user/test1/testzip";
        String savapath = "C:\\Users\\sssd\\Desktop\\testzip";
        HdfsUtils.fileReadFromHdfs(hdfspath, savapath);*/

        String localPath = "C:\\Users\\sssd\\Desktop\\testzip";
        String hdfsPath = "hdfs://new1data/user/sssd";
        HdfsUtils.pushFile2Hdfs(localPath, hdfsPath);
    }
}
