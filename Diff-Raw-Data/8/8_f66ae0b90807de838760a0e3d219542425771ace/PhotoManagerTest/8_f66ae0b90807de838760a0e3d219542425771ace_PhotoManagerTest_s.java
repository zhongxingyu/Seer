 package com.cs301w01.meatload.test;
 
import com.cs301w01.meatload.controllers.PhotoManager;
 
 import android.test.AndroidTestCase;
 
 public class PhotoManagerTest extends AndroidTestCase {
	PhotoManager mClassToTest;
 	int mArg1;
 	int mArg2;
  
 	protected void setUp() throws Exception {
		mClassToTest=new PhotoManager(5); //5 is photoID
 		mArg1=6;
 		mArg2=3;
 		super.setUp();
 	}
  
 	protected void tearDown() throws Exception {
 		super.tearDown();
 	}
  
 	public void testAdd(){
 		assertEquals(9,9);
 		assertEquals(5,4);
 		//assertEquals(9,mClassToTest.add(mArg1,mArg2));
 	}
 }
