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
 
import net.oneandone.shared.artifactory.model.BomEntry;
 import static org.junit.Assert.*;
 import org.junit.Test;
 
 /**
  *
  * @author Mirko Friedenhagen
  */
 public class BomEntryTest {
 
     @Test
     public void checkFirstSut() {
         final BomEntry sut = BomEntry.valueOf("d70e4ec32cf9ee8124ceec983147efc361153180" + "  " + "bill-of-materials-maven-plugin-2.0-javadoc.jar");
         assertEquals("d70e4ec32cf9ee8124ceec983147efc361153180", sut.getSha1().toString());
         assertEquals("bill-of-materials-maven-plugin-2.0-javadoc.jar", sut.getFileName());
     }
 
     @Test
     public void checkSecondSut() {
         BomEntry sut = BomEntry.valueOf("8067be47fffc2648048fee3baed8a071f1979db4  bill-of-materials-maven-plugin-2.0-sources.jar");
         assertEquals("8067be47fffc2648048fee3baed8a071f1979db4", sut.getSha1().toString());
         assertEquals("bill-of-materials-maven-plugin-2.0-sources.jar", sut.getFileName());
     }
 
     @Test(expected = IllegalArgumentException.class)
     public void checkWrongFormatWithASingleSpace() {
         BomEntry.valueOf("8067be47fffc2648048fee3baed8a071f1979db4 bill-of-materials-maven-plugin-2.0-sources.jar");
     }
 }
