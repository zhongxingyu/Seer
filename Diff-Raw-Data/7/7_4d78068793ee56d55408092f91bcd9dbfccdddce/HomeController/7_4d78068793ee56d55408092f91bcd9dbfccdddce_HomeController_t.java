 /*
  * Licensed to the Apache Software Foundation (ASF) under one or more contributor license agreements. See the NOTICE
  * file distributed with this work for additional information regarding copyright ownership. The ASF licenses this file
  * to you under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the
  * License. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0 Unless required by
  * applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language
  * governing permissions and limitations under the License.
  */
 package com.sokeeper.web.controller;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.util.Assert;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.servlet.ModelAndView;
 import com.sokeeper.persist.service.ChangesService;
 import com.sokeeper.web.dto.QueryDto;
 import com.sokeeper.web.dto.MovieDto;
 
 
 /**
  * @author James Fu (fuyinhai@gmail.com)
  */
 @Controller
 public class HomeController {
 
     @Autowired
     private ChangesService changesService;
     
     /**
      * This method serve as the initial page for the web application e.g.:
      * http://localhost/context
      * it also serve for
      * http://localhost/context/home/index.htm
      * @param out
      * @return
      */
     @RequestMapping
     public ModelAndView index( QueryDto query , Map<String, Object> out) {
         Assert.notNull(changesService, "changesService can not be null.");
         out.put("query", query);
         
         List<MovieDto> movies = new ArrayList<MovieDto>();
         for (int i=0; i<10; i++) {
         	MovieDto movie = new MovieDto();
        	movie.setName("电影" + i );
        	movie.setDescription("还不错，情节细腻，故事完美");
         	movie.setImageUrl("http://img3.douban.com/spic/s11364841.jpg");
         	movie.setPrice(((Double)(Math.random() * 100)).intValue()) ;
         	movie.setOprice(movie.getPrice() + 10);
        	movie.setSharedBy("付银海");
         	movies.add(movie);
         }
         
         out.put("movies",movies);
         
         return new ModelAndView("home/index");
     } 
 }
