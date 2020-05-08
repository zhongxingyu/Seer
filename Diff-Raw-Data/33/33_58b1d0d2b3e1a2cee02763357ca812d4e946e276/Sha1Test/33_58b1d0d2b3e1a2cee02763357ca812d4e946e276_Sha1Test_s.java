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
 
import net.oneandone.shared.artifactory.model.Sha1;
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNotEquals;
 import org.junit.Test;
 
 /**
  *
  * @author Mirko Friedenhagen
  */
 public class Sha1Test {
 
     @Test(expected = NullPointerException.class)
     public void checkSha1IsNull() {
         Sha1.valueOf(null);
     }
 
     @Test(expected = IllegalArgumentException.class)
     public void checkSha1WrongLength() {
         Sha1.valueOf("d70e4ec32cf9ee8124ceec983147efc36115318");
     }
 
     @Test(expected = IllegalArgumentException.class)
     public void checkSha1WrongCharacter() {
         Sha1.valueOf("g70e4ec32cf9ee8124ceec983147efc361153180");
     }
     
     @Test
     public void checkSha1() {
         final String givenSha1String = "d70e4ec32cf9ee8124ceec983147efc361153180";
         final Sha1 sha1 = Sha1.valueOf(givenSha1String);
         assertEquals(givenSha1String, String.valueOf(sha1));
     }
 
     @Test
     public void checkSha1Equals() {
         final String givenSha1String = "d70e4ec32cf9ee8124ceec983147efc361153180";
         final Sha1 sha1a = Sha1.valueOf(givenSha1String);
         final Sha1 sha1b = Sha1.valueOf(givenSha1String);
         final Sha1 sha1c = Sha1.valueOf("d70e4ec32cf9ee8124ceec983147efc361153181");
         assertEquals(sha1a, sha1b);
         assertNotEquals(sha1a, sha1c);
         assertNotEquals(sha1a, "bar");
         assertNotEquals(sha1a, null);
     }
 
     @Test
     public void checkHash() {
         final String givenSha1String = "d70e4ec32cf9ee8124ceec983147efc361153180";
         final Sha1 sha1 = Sha1.valueOf(givenSha1String);
        sha1.hashCode();
     }
 }
