 /*
 Copyright (c) 2005 Lucas Holt
 All rights reserved.
 
 Redistribution and use in source and binary forms, with or without modification, are
 permitted provided that the following conditions are met:
 
   Redistributions of source code must retain the above copyright notice, this list of
   conditions and the following disclaimer.
 
   Redistributions in binary form must reproduce the above copyright notice, this
   list of conditions and the following disclaimer in the documentation and/or other
   materials provided with the distribution.
 
   Neither the name of the Just Journal nor the names of its contributors
   may be used to endorse or promote products derived from this software without
   specific prior written permission.
 
 THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND
 CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE
 GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR
 BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY
 OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR
 TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 POSSIBILITY OF SUCH DAMAGE.
 */
 
 package com.justjournal.ctl.api;
 
 import com.justjournal.db.MoodDao;
 import com.justjournal.db.MoodTo;
 import org.springframework.cache.annotation.Cacheable;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.ResponseBody;
 
 import java.util.Collection;
 
 /**
  * List moods used for journal entries.
  *
  * @author Lucas Holt
  * @since 1.0
  */
 @Controller
 @RequestMapping("/api/mood")
public class MoodController {
 
     @Cacheable(value = "mood", key = "id")
    @RequestMapping(value = "{id}", method = RequestMethod.GET, produces = "application/json")
     @ResponseBody
    public MoodTo getById(@PathVariable("id") Integer id) {
         return MoodDao.get(id);
     }
 
     /**
      * All moods usable by blogs
      *
      * @return mood list
      */
     @Cacheable("mood")
     @RequestMapping(method = RequestMethod.GET, headers = "Accept=*/*", produces = "application/json")
     public
     @ResponseBody
     Collection<MoodTo> getMoodList() {
         return MoodDao.viewByRelationship();
     }
 
 }
