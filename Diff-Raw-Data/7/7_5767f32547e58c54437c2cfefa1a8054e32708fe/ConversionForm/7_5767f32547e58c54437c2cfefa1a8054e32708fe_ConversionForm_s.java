 /**
  * Copyright 2011 Alexandre Dutra
  *
  *    Licensed under the Apache License, Version 2.0 (the "License");
  *    you may not use this file except in compliance with the License.
  *    You may obtain a copy of the License at
  *
  *        http://www.apache.org/licenses/LICENSE-2.0
  *
  *    Unless required by applicable law or agreed to in writing, software
  *    distributed under the License is distributed on an "AS IS" BASIS,
  *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *    See the License for the specific language governing permissions and
  *    limitations under the License.
  */
 package fr.xebia.confluence2wordpress.form;
 
 import java.io.Serializable;
 import java.util.List;
 
 import org.apache.commons.lang.text.StrTokenizer;
 
 /**
  * @author Alexandre Dutra
  *
  */
 public class ConversionForm implements Serializable {
 
     private static final long serialVersionUID = 1L;
 
     private Long pageId;
 
     private String pageTitle;
 
     private String ignoreConfluenceMacros;
 
     private String resourcesBaseUrl;
 
     private Boolean optimizeForRDP;
 
     private Integer postId;
 
     private String postSlug;
 
     private Integer wordpressUserId;
 
    private List<String> wordpressCategoryNames;
 
    private List<String> wordpressTagNames;
 
     public Long getPageId() {
         return pageId;
     }
 
     public void setPageId(Long pageId) {
         this.pageId = pageId;
     }
 
     public String getPageTitle() {
         return pageTitle;
     }
 
     public void setPageTitle(String pageTitle) {
         this.pageTitle = pageTitle;
     }
 
     public String getIgnoreConfluenceMacros() {
         return ignoreConfluenceMacros;
     }
 
     @SuppressWarnings("unchecked")
     public List<String> getIgnoreConfluenceMacrosAsList() {
         return new StrTokenizer(this.ignoreConfluenceMacros).getTokenList();
     }
 
     public void setIgnoreConfluenceMacros(String ignoreConfluenceMacros) {
         this.ignoreConfluenceMacros = ignoreConfluenceMacros;
     }
 
     public String getResourcesBaseUrl() {
         return resourcesBaseUrl;
     }
 
     public void setResourcesBaseUrl(String resourcesBaseUrl) {
         this.resourcesBaseUrl = resourcesBaseUrl;
     }
 
     public Boolean getOptimizeForRDP() {
         return optimizeForRDP;
     }
 
     public void setOptimizeForRDP(Boolean optimizeForRDP) {
         this.optimizeForRDP = optimizeForRDP;
     }
 
     public Integer getPostId() {
         return postId;
     }
 
     public void setPostId(Integer postId) {
         this.postId = postId;
     }
 
     public String getPostSlug() {
         return postSlug;
     }
 
     public void setPostSlug(String postSlug) {
         this.postSlug = postSlug;
     }
 
     public Integer getWordpressUserId() {
         return wordpressUserId;
     }
 
     public void setWordpressUserId(Integer wordpressUserId) {
         this.wordpressUserId = wordpressUserId;
     }
 
     public List<String> getWordpressCategoryNames() {
         return wordpressCategoryNames;
     }
 
     public void setWordpressCategoryNames(List<String> wordpressCategoryIds) {
         this.wordpressCategoryNames = wordpressCategoryIds;
     }
 
     public List<String> getWordpressTagNames() {
         return wordpressTagNames;
     }
 
     public void setWordpressTagNames(List<String> wordpressTagNames) {
         this.wordpressTagNames = wordpressTagNames;
     }
 
 }
