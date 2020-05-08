 /**
  * Copyright 2011 Jason Ferguson.
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
 package org.jason.mapmaker.server.service;
 
 import org.jason.mapmaker.server.repository.MtfccRepository;
 import org.jason.mapmaker.shared.model.MTFCC;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Service;
 
 import java.util.List;
 import java.util.Map;
 
 /**
  * Implementation of MtfccService interface
  *
 * @since 0.1
  * @author Jason Ferguson
  */
 @Service("mtfccService")
 public class MtfccServiceImpl implements MtfccService {
 
     private MtfccRepository mtfccRepository;
 
     @Autowired
     public void setMtfccRepository(MtfccRepository mtfccRepository) {
         this.mtfccRepository = mtfccRepository;
     }
 
     @Override
     public MTFCC get(String code) {
 
         MTFCC example = new MTFCC();
         example.setMtfccCode(code);
 
         List<MTFCC> resultList = mtfccRepository.queryByExample(example);
 
         if (resultList.size() == 0) {
             return null;
         }
 
         return resultList.get(0);
     }
 
     @Override
     public Map<String, String> getMtfccTypes() {
         return mtfccRepository.getMtfccTypes();
     }
 
     public Map<MTFCC, Long> getMtfccFeatureCount() {
         return mtfccRepository.getMtfccFeatureCount();
     }
 }
