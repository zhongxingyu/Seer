 package com.gumtree.api.service;
 
import com.gumtree.api.entity.Advert;

 /**
  * Created by IntelliJ IDEA.
  * User: markkelly
  * Date: 31/08/2011
  * Time: 16:01
  * To change this template use File | Settings | File Templates.
  */
 public interface ApiAdvertsReader {
 
    Advert getMostRecentAdverts() throws Exception;
 }
