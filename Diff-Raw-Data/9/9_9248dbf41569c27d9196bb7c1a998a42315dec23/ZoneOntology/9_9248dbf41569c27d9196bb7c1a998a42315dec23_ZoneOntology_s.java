 package org.zoneproject.extractor.utils;
 
 /*
  * #%L
  * ZONE-utils
  * %%
  * Copyright (C) 2012 - 2013 ZONE-project
  * %%
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Affero General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU Affero General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  * #L%
  */
 
 /**
  *
  * @author Desclaux Christophe <christophe@zouig.org>
  */
 public class ZoneOntology {
     public static String PREFIX = "http://zone-project.org/";
     public static String SOURCES_PREFIX = PREFIX+"model/sources";
     public static String PLUGIN_PREFIX  = PREFIX+"model/plugins";
     
     
     public static String ANNOTATION = PREFIX+"model/annotation";
     
     public static String PLUGIN_EXTRACT_ARTICLES_CONTENT =  PLUGIN_PREFIX+"/ExtractArticlesContent";
     public static String PLUGIN_EXTRACT_ARTICLES_CONTENT_RES = PLUGIN_EXTRACT_ARTICLES_CONTENT+"#result";
     
     public static String PLUGIN_INSEEGEO = PLUGIN_PREFIX+"/INSEEGeo";
     public static String PLUGIN_OPENCALAIS = PLUGIN_PREFIX+"/OpenCalais";
     public static String PLUGIN_WIKIMETA = PLUGIN_PREFIX+"/WikiMeta";
     
     public static String PLUGIN_SVM = PLUGIN_PREFIX+"/Categorization_SVM";
     public static String PLUGIN_SVM_RES = PLUGIN_SVM+"#result";
     
     public static String PLUGIN_TWITTER_HASHTAG = PLUGIN_PREFIX+"/twitter#hashtag";
     public static String PLUGIN_TWITTER_AUTHOR = PLUGIN_PREFIX+"/twitter#author";
     public static String PLUGIN_TWITTER_MENTIONED = PLUGIN_PREFIX+"/twitter#mentioned";
     
     public static String SOURCES_TYPE = SOURCES_PREFIX+"#Source";
     public static String SOURCES_LANG = SOURCES_PREFIX+"#lang";
     public static String SOURCES_THUMB = SOURCES_PREFIX+"#thumb";
     public static String SOURCES_THEME = SOURCES_PREFIX+"#theme";
     public static String SOURCES_OWNER = SOURCES_PREFIX+"#owner";
     public static String SOURCES_OFFLINE = SOURCES_PREFIX+"#offline";
     
     public static String SOURCES_TYPE_TWITTER = SOURCES_PREFIX+"#twitter";
 
     public static String SOURCES_TYPE_TWITTER_TIMELINE = SOURCES_TYPE_TWITTER+"/timeline";
     public static String SOURCES_TWITTER_TOKEN = SOURCES_PREFIX+"#twitterToken";
     public static String SOURCES_TWITTER_TOKEN_SECRET = SOURCES_PREFIX+"#twitterSecretToken";
 
     public static String SOURCES_TYPE_TWITTER_HASHTAG= SOURCES_TYPE_TWITTER+"/hashTag";
     
     public static String GRAPH_NEWS = "http://zone-project.org/datas/items";
     public static String GRAPH_SOURCES = "http://zone-project.org/datas/sources";
     
     
 }
