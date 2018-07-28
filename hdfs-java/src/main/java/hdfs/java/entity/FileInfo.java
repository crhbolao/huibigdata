package hdfs.java.entity;

/**
 * Created with IntelliJ IDEA.
 * User: bolao
 * Date: 2018/7/28 16:26
 * Version: V1.0
 * To change this template use File | Settings | File Templates.
 * Description:    关于文件信息的实体类
 */
public class FileInfo {

    public int fileNum = 0;
    public long fileSize = 0;
    public String extendName = "zip";

    public FileInfo() {

    }

    public FileInfo(int fileNum, long fileSize, String extendName) {
        this.fileNum = fileNum;
        this.fileSize = fileSize;
        this.extendName = extendName;
    }

    public int getFileNum() {
        return fileNum;
    }

    public void setFileNum(int fileNum) {
        this.fileNum = fileNum;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public String getExtendName() {
        return extendName;
    }

    public void setExtendName(String extendName) {
        this.extendName = extendName;
    }
}
