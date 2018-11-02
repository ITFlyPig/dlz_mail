package com.dlz.mail.utils;


import com.dlz.mail.task.ExecuteSQL;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.*;
import java.nio.file.*;
import java.sql.Timestamp;
import java.util.List;

/**
 * Created by wangyuelin on 2017/12/19.
 * 解析文件的工具类
 */
public class FileUtil {
    private static final String TAG = "FileUtil";

    private static int ZIP_SIZE = 2 * 1024 * 1024;//文件大小超过2M就进行压缩

    private static final Logger logger = LogManager.getLogger(FileUtil.class);




    /**
     * 读取txt文件
     * @param path
     * @return
     */
    public static String readTxtFile(String path){
        if (TextUtil.isEmpty(path)){
            return "";
        }
        File file = new File(path);
        StringBuilder result = new StringBuilder();
        try{
            BufferedReader br = new BufferedReader(new FileReader(file));//构造一个BufferedReader类来读取文件
            String s = null;
            while((s = br.readLine())!=null){//使用readLine方法，一次读一行
                result.append(System.lineSeparator()+s);
            }
            br.close();
        }catch(Exception e){
            e.printStackTrace();
        }
        return result.toString();
    }


    /**
     * 判断文件是否应该进行压缩
     * @param filePath
     * @return
     */
    public static boolean shouldZIP(String filePath){
        if (TextUtil.isEmpty(filePath) ){
            return false;
        }
        File file = new File(filePath);
        if (file.exists() && file.isFile() && (file.length() > ZIP_SIZE) ){
            return true;

        }
        return false;
    }



    /**
     * 得到文件的名称
     * @param path 可以使绝对路径，也可以是只有文件名
     * @return
     */
    public static String getFileName(String path){
        String fileName ="";
        if (TextUtil.isEmpty(path)){
            fileName = System.currentTimeMillis() + "";
            return fileName;
        }

        if (path.contains(File.separator)){//表示是路径
            fileName  = path.substring(path.lastIndexOf(File.separator) + 1);

        }else {
            fileName = path;
        }
        if (!TextUtil.isEmpty(fileName)){
           fileName = fileName.substring(0, fileName.lastIndexOf("."));
        }
        return fileName;
    }

    /**
     * 得到文件的名称，包含文件的后缀名
     * @param path 可以使绝对路径，也可以是只有文件名
     * @return
     */
    public static String getFileNameWithType(String path){
        String fileName ="";
        if (TextUtil.isEmpty(path)){
            fileName = System.currentTimeMillis() + "";
            return fileName;
        }

        if (path.contains(File.separator)){//表示是路径
            fileName  = path.substring(path.lastIndexOf(File.separator) + 1);

        }else {
            fileName = path;
        }
        return fileName;
    }

    /**
     * 文件名称里面添加时间戳
     * @param fileName
     * @return
     */
    public static String addTime(String fileName){
        if (TextUtil.isEmpty(fileName)){
            return "";
        }
        if (fileName.contains(".")){//有后缀的
            int index = fileName.lastIndexOf(".");
            String nameWithTime = fileName.substring(0, index) + TimeUtil.getCurTime() ;
            if (index < fileName.length()){
                nameWithTime =  nameWithTime + fileName.substring(index);
            }
            return nameWithTime;
        }
        return fileName + TimeUtil.getCurTime();//无后缀的

    }

    /**
     * 获取此时文件的后缀，因为可能存在对大问价的压缩，两个线程同时在压缩一个文件（即往一个文件里写数据，最终的数据是不正确的）
     * @return
     */
    public static String getNowFileSuffix() {
        return "_suffix_" +  System.currentTimeMillis();
    }


    /**
     * 重命名多线程创建的文件,文件名称末尾是时间戳的才修改,例如   /Downloads/wiznote-macos-2018-10-30_suffix_123456789.dmg
     * @param filePath
     * @return
     */
    public static String renameFile(String filePath) {
        if (TextUtil.isEmpty(filePath)) {
            return null;
        }
        File oldFile = new File(filePath);
        if (!oldFile.exists()) {
            return null;
        }

        String wantedPath = getWantedPath(filePath);
        if (TextUtil.isEmpty(wantedPath)) {
            return null;
        }
        File newFile = new File(wantedPath);
        oldFile.renameTo(newFile);
        return newFile.getAbsolutePath();


    }

    /**
     * 想要的路径，因为为了多线程，在文件生成的时候会在文件后面添加当时的时间戳，真正使用的时候要除去时间戳
     * @param filePath
     * @return
     */
    public static String getWantedPath(String filePath) {
        if (TextUtil.isEmpty(filePath)) {
            return null;
        }
        int start = filePath.lastIndexOf("_suffix_");
        int end = filePath.lastIndexOf(".");
        if (start >= end || start < 0) {
            return filePath;
        }

        return filePath.substring(0, start) + filePath.substring(end, filePath.length());

    }


    /**
     * 压缩文件
     *
     * @param filePath
     * @return
     */
    public static String handleZIP(String filePath) {
        if (TextUtil.isEmpty(filePath)) {
            logger.debug("handleZIP 压缩文件，传入的文件路径为空，直接返回");
            return null;
        }

        if (FileUtil.shouldZIP(filePath)) {
            File srcFile = new File(filePath);
            File[] files = new File[]{srcFile};

            String zipName = FileUtil.getFileName(filePath) + ".zip";
            String zipPath = srcFile.getParent() + File.separator + zipName;

            //开始压缩前判断之前是否存在相同的压缩文件，存在就删除
            File oldZip = new File(zipPath);
            oldZip.deleteOnExit();

            ZipFileUtil.compressFiles2Zip(files, zipPath);

            return zipPath;
        }
        return filePath;

    }
}
