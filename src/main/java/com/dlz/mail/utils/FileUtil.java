package com.dlz.mail.utils;


import java.io.*;
import java.nio.file.*;
import java.util.List;

/**
 * Created by wangyuelin on 2017/12/19.
 * 解析文件的工具类
 */
public class FileUtil {
    private static final String TAG = "FileUtil";

//    private static int ZIP_SIZE = 2 * 1024 * 1024;//文件大小超过2M就进行压缩

    private static int ZIP_SIZE = 1;//文件大小超过2M就进行压缩



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


}
