package com.dlz.mail.db;

import com.dlz.mail.Test;
import com.dlz.mail.utils.Constant;
import com.dlz.mail.utils.ExcelUtil;
import com.dlz.mail.utils.Log;
import com.dlz.mail.utils.TextUtil;
import org.apache.commons.dbutils.ResultSetHandler;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class CSVResultHandler implements ResultSetHandler<String> {
    private String taskName;

    public CSVResultHandler(String taskName) {
        this.taskName = taskName;
    }

    public String handle(ResultSet rs) throws SQLException {
        //用查询的结果生成csv文件
        List<List<Object>> result = getDataList(rs);
        if (result == null || result.size() == 0){
            Log.d("查询的结果转为List集合为空");
            return "";
        }
        String path = ExcelUtil.createExcelByPOI( System.getProperty("user.dir") + Constant.FileConfig.CSV_DIR, taskName, result);//创建文件
        if (TextUtil.isEmpty(path)){
            Log.d("文件创建失败");
            return "";
        }
        return path;
    }



    /**
     * 将查询的结果转为List集合
     * @param rs
     * @return
     * @throws SQLException
     */
    protected List<List<Object>> getDataList(ResultSet rs) throws SQLException {
        //获取每列的列名
        List<Object> headers = getHeaderList(rs);
        if (headers == null) {
            System.out.println("获取每列的列名出错");
            return null;

        }
        //获取每行的值
        List<List<Object>> contentList = new ArrayList<List<Object>>();
        contentList.add(headers);//添加头部信息
        while (rs.next()) {
            //获取一行的值
            ResultSetMetaData data = rs.getMetaData();
            int clonumnCount = data.getColumnCount();
            List<Object> rowDataList = new ArrayList<Object>();
            for (int i = 1; i <= clonumnCount; i++) {
                Object valueObject = getValueByType(data.getColumnType(i), rs, i);
                if(valueObject != null){
                    rowDataList.add(valueObject);
                }
            }
            contentList.add(rowDataList);

        }
        return contentList;
    }


    /**
     * 获取数据库的列的名字的集合
     * @param rs
     * @return
     */
    public List<Object> getHeaderList(ResultSet rs){
        if (rs == null) {
            return null;
        }
        try {
            ResultSetMetaData data = rs.getMetaData();
            if(data == null){
                return null;
            }

            ArrayList<Object> clolumnList = new ArrayList<Object>();
            int columnCount = data.getColumnCount();//列的总数目
            for (int i = 1; i <= columnCount; i++) {
                String clolumnName =  data.getColumnName(i);
                clolumnList.add(clolumnName);
            }
            return clolumnList;

        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("获取数据库的列的名字出错");
        }

        return null;
    }


    /**
     * 据类型获取一行中对应的值
     * @param columnType
     * @param rs
     * @param index
     * @return
     * @throws SQLException
     */
    public Object getValueByType(int columnType, ResultSet rs, int index ) throws SQLException {
        Object value = null;
        switch(columnType){
            case Types.NUMERIC:
                value = rs.getLong(index);
                break;
            case Types.VARCHAR:
                value = rs.getString(index);
                break;
            case Types.DATE:
                value = rs.getDate(index);
                break;
            case Types.TIMESTAMP:
                value = rs.getTimestamp(index);
                break;
            case Types.TIME:
                value = rs.getTime(index);
                break;
            case Types.BOOLEAN:
                value = rs.getBoolean(index);
                break;
            case Types.ARRAY :
                value = rs.getArray(index);
                break;
            case Types.BIGINT :
                value = rs.getInt(index);
                break;
            case Types.BINARY:
                value = rs.getBinaryStream(index);
                break;
            case Types.BLOB:
                value = rs.getBlob(index );
                break;
            case Types.CHAR:
                value = rs.getString(index);
                break;
            case Types.INTEGER:
                value = rs.getInt(index);
                break;
            case Types.DOUBLE :
                value = rs.getDouble(index);
                break;
            case Types.FLOAT:
                value = rs.getFloat(index);
                break;
            case Types.SMALLINT:
                value = rs.getInt(index);
                break;
            case Types.DECIMAL:
                value = rs.getLong(index);
                break;
            default:
                value = rs.getObject(index);
                break;
        }
        return value;
    }
}
