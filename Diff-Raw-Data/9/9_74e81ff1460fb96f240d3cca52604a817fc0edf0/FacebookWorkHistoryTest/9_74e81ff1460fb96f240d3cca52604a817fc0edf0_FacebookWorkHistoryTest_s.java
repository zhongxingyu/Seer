 /**
  * Copyright (c) 2013 HAN University of Applied Sciences
  * Arjan Oortgiese
  * Joëll Portier
  *
  * Permission is hereby granted, free of charge, to any person
  * obtaining a copy of this software and associated documentation
  * files (the "Software"), to deal in the Software without
  * restriction, including without limitation the rights to use,
  * copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the
  * Software is furnished to do so, subject to the following
  * conditions:
  *
  * The above copyright notice and this permission notice shall be
  * included in all copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
  * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
  * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
  * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
  * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
  * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
  * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
  * OTHER DEALINGS IN THE SOFTWARE.
  */
 package com.dare2date.domein.facebook;
 
 import junit.framework.Assert;
 import org.junit.Before;
 import org.junit.Test;
 
 public class FacebookWorkHistoryTest {
     private FacebookWorkHistory work;
 
     @Before
     public void before() {
         work = new FacebookWorkHistory();
     }
 
     @Test
     public void testSetId() {
         work.setId("12345");
 
         Assert.assertEquals("12345", work.getId());
     }
 
     @Test
     public void testSetName() {
         work.setName("Test B.V.");
 
         Assert.assertEquals("Test B.V.", work.getName());
     }
 
     @Test
     public void testEquals() {
         FacebookWorkHistory item1 = new FacebookWorkHistory();
         FacebookWorkHistory item2 = new FacebookWorkHistory();
 
         item1.setId("1");
         item1.setName("Test");
 
         item2.setId("1");
         item2.setName("Not");
 
        Assert.assertFalse(item1.equals(item2));
     }
 
     @Test
     public void testNotEquals() {
         FacebookWorkHistory item1 = new FacebookWorkHistory();
         FacebookWorkHistory item2 = new FacebookWorkHistory();
 
         item1.setId("1");
         item1.setName("Test");
 
         item2.setId("2");
         item2.setName("Test");
 
         Assert.assertFalse(item1.equals(item2));
     }
 }
