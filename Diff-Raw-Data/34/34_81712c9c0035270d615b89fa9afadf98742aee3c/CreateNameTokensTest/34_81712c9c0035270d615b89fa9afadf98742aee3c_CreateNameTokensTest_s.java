 /**
  * Copyright 2013 ArcBees Inc.
  *
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not
  * use this file except in compliance with the License. You may obtain a copy of
  * the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
  * License for the specific language governing permissions and limitations under
  * the License.
  */
 
 package com.arcbees.plugin.template.place;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.junit.Assert;
 import org.junit.Test;
 
import com.arcbees.plugin.template.create.place.CreatedNameTokens;
import com.arcbees.plugin.template.create.place.NameToken;
import com.arcbees.plugin.template.create.place.NameTokenOptions;
import com.arcbees.plugin.template.domain.place.CreateNameTokens;
 
 public class CreateNameTokensTest {
     @Test
     public void testNameTokens() {
         NameToken token = new NameToken();
         token.setCrawlable(true);
         token.setToken("MyToken");
 
         List<NameToken> nameTokens = new ArrayList<NameToken>();
         nameTokens.add(token);
 
         NameTokenOptions nameTokenOptions = new NameTokenOptions();
         nameTokenOptions.setNameTokens(nameTokens);
 
         CreatedNameTokens createdNameToken = CreateNameTokens.run(nameTokenOptions, false);
         
         Assert.assertNotNull(createdNameToken.getNameTokens().getContents());
         Assert.assertNotNull(createdNameToken.getFields());
         Assert.assertNotNull(createdNameToken.getMethods());
     }
 }
