package com.dlz.mail.utils; 

import org.junit.Test; 
import org.junit.Before; 
import org.junit.After;

import java.util.ArrayList;
import java.util.List;

/** 
* ExcelUtil Tester. 
* 
* @author <Authors name> 
* @since <pre>04/17/2018</pre> 
* @version 1.0 
*/ 
public class ExcelUtilTest { 

@Before
public void before() throws Exception { 
} 

@After
public void after() throws Exception { 
} 

/** 
* 
* Method: createExcel(String outPutPath, String fileName, List<List<Object>> data) 
* 
*/ 
@Test
public void testCreateExcel() throws Exception { 
//TODO: Test goes here... 
} 

/** 
* 
* Method: createExcelByPOI(String outPutPath, String fileName, List<List<Object>> data) 
* 
*/ 
@Test
public void testCreateExcelByPOI() throws Exception { 
//TODO: Test goes here...
    List<List<Object>> data = new ArrayList<>();
    for (int i = 0; i < 66666; i++){
        List<Object> item = new ArrayList<>();
        item.add(i);
        item.add(i + 1);
        data.add(item);
    }

    ExcelUtil.createExcelByPOI(System.getProperty("user.dir") + Constant.FileConfig.CSV_DIR, "测试", data);
} 


} 
