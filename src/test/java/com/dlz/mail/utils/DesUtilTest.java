package com.dlz.mail.utils; 

import org.junit.Test; 
import org.junit.Before; 
import org.junit.After; 

/** 
* DesUtil Tester. 
* 
* @author <Authors name> 
* @since <pre>04/10/2018</pre> 
* @version 1.0 
*/ 
public class DesUtilTest { 

@Before
public void before() throws Exception { 
} 

@After
public void after() throws Exception { 
} 

/** 
* 
* Method: getInstance() 
* 
*/ 
@Test
public void testGetInstance() throws Exception { 
//TODO: Test goes here... 
} 

/** 
* 
* Method: encrypt(String str) 
* 
*/ 
@Test
public void testEncrypt() throws Exception { 
//TODO: Test goes here...
    String pas = DesUtil.getInstance().encrypt("BI@Sc0o#ma1l");
    System.out.println(pas);
} 

/** 
* 
* Method: decrypt(String str) 
* 
*/ 
@Test
public void testDecrypt() throws Exception { 
//TODO: Test goes here... 
} 


} 
