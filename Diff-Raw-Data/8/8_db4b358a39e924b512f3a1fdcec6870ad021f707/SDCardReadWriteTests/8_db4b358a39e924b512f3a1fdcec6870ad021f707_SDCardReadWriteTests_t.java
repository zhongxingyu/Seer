 /**
  * Copyright (C) 2011 Adriano Monteiro Marques
  *
  * Author:  Zubair Nabi <zn.zubairnabi@gmail.com>
  *
  * This program is free software; you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation; either version 2 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
  * USA
  */
 
 package org.umit.icm.mobile.test.utils;
 
 
 import org.umit.icm.mobile.utils.Constants;
 import org.umit.icm.mobile.utils.SDCardReadWrite;
 
 import twitter4j.auth.AccessToken;
 
 import junit.framework.Assert;
 import android.test.AndroidTestCase;
 
 
 public class SDCardReadWriteTests extends AndroidTestCase {
 
     public void testReadWrite() throws Throwable {
     	SDCardReadWrite.writeString("sdtest.txt", "/test" , "This is a test string");
         String readString = SDCardReadWrite.readString("sdtest.txt", "/test");
         Assert.assertEquals("This is a test string", readString);
     }
     
    public void testAccessTokenReadWrite() throws Throwable {
    	AccessToken accessToken = new AccessToken("99999-token", "tokenSecret");
     	SDCardReadWrite.writeAccessToken(Constants.KEYS_DIR, accessToken);
         AccessToken accessToken2 
         = SDCardReadWrite.readAccessToken(Constants.KEYS_DIR);
         Assert.assertTrue(accessToken.equals(accessToken2));
    }   
 
 }
