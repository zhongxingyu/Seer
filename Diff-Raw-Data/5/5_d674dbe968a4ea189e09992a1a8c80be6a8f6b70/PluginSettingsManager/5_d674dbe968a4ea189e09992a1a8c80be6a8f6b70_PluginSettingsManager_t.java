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
 package fr.xebia.confluence2wordpress.core.settings;
 
 import java.util.List;
 
 import fr.xebia.confluence2wordpress.core.converter.SyntaxHighlighterPlugin;
 import fr.xebia.confluence2wordpress.wp.WordpressConnectionProperties;
 
 public interface PluginSettingsManager extends WordpressConnectionProperties {
 
     void setWordpressRootUrl(String wordpressRootUrl);
 
     String getWordpressRootUrl();
     
     void setWordpressXmlRpcRelativePath(String wordpressXmlRpcRelativePath);
 
     String getWordpressXmlRpcRelativePath();
 
     void setProxyHost(String proxyHost);
 
     String getProxyHost();
 
     void setProxyPort(String proxyPort);
 
     String getProxyPort();
 
    void setWordpressEditPostRelativePath(String wordpressEditPostRelativePath);
 
     String getWordpressEditPostRelativePath();
    
     void setWordpressUserName(String wordpressUserName);
 
     String getWordpressUserName();
 
     void setWordpressPassword(String wordpressPassword);
 
     String getWordpressPassword();
 
     void setWordpressBlogId(String wordpressBlogId);
 
     String getWordpressBlogId();
 
     void setDefaultIgnoredConfluenceMacros(String ignoredConfluenceMacros);
 
     String getDefaultIgnoredConfluenceMacros();
 
     List<String> getDefaultIgnoredConfluenceMacrosAsList();
 
     void setAllowedConfluenceGroups(String allowedConfluenceGroups);
 
     String getAllowedConfluenceGroups();
 
     List<String> getAllowedConfluenceGroupsAsList();
 
     void setAllowedConfluenceSpaceKeys(String allowedConfluenceSpaceKeys);
 
     String getAllowedConfluenceSpaceKeys();
 
     List<String> getAllowedConfluenceSpaceKeysAsList();
 
     void setWordpressSyntaxHighlighterPlugin(String wordpressSyntaxHighlighterPlugin);
 
     String getWordpressSyntaxHighlighterPlugin();
 
     SyntaxHighlighterPlugin getWordpressSyntaxHighlighterPluginAsEnum();
 
 }
