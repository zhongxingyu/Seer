 /**
  * Copyright 1&1 Internet AG, https://github.com/1and1/
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package net.oneandone.shared.artifactory.model;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNotEquals;
 import org.junit.Test;
 
 /**
  *
  * @author Mirko Friedenhagen
  */
 public class MD5Test {
 
     @Test(expected = NullPointerException.class)
     public void checkMD5IsNull() {
         MD5.valueOf(null);
     }
 
     @Test(expected = IllegalArgumentException.class)
     public void checkMd5WrongLength() {
         MD5.valueOf("85be001d8d4c7114a27d27ac4c31b9dbc");
     }
 
     @Test(expected = IllegalArgumentException.class)
     public void checkMd5WrongCharacter() {
         MD5.valueOf("85be001d8d4c7114a27d27ac4c31b9dx");
     }
     
     @Test
     public void checkMd5() {
         final String givenMD5String = "85be001d8d4c7114a27d27ac4c31b9db";
         final MD5 md5 = MD5.valueOf(givenMD5String);
         assertEquals(givenMD5String, String.valueOf(md5));
     }
 
     @Test
     public void checkMD5Equals() {
         final String givenMD5String = "85be001d8d4c7114a27d27ac4c31b9db";
         final MD5 md5a = MD5.valueOf(givenMD5String);
         final MD5 md5b = MD5.valueOf(givenMD5String);
         final MD5 md5c = MD5.valueOf("85be001d8d4c7114a27d27ac4c31b9dc");
         assertEquals(md5a, md5b);
         assertNotEquals(md5a, md5c);
         assertNotEquals(md5a, "bar");
         assertNotEquals(md5a, null);
     }
 
     @Test
     public void checkHash() {
         final String givenMD5String = "85be001d8d4c7114a27d27ac4c31b9db";
         final MD5 md5 = MD5.valueOf(givenMD5String);
        assertNotEquals(1, md5.hashCode());
     }
 }
