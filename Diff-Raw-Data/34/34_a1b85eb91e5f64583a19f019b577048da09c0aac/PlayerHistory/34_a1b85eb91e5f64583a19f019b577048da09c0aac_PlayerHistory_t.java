 /*
 * Copyright (C) 2012-2014 Gregory S. Meiste  <http://gregmeiste.com>
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package com.meiste.greg.ptw;
 
 import java.util.List;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.Editor;
 
 import com.google.gson.Gson;
 import com.meiste.greg.ptw.tab.Questions;
 
 public final class PlayerHistory {
     private List<Integer> ids;
     private List<RaceQuestions> questions;
     private List<RaceAnswers> answers;
 
     public static PlayerHistory fromJson(final String json) {
         return new Gson().fromJson(json, PlayerHistory.class);
     }
 
     public void commit(final Context context) {
         if ((ids == null) || (questions == null) || (answers == null))
             return;
         if ((ids.size() != questions.size()) && (ids.size() != answers.size()))
             return;
         if (ids.size() <= 0)
             return;
 
         Util.log("PlayerHistory: Restoring " + ids.size() + " races for player.");
 
         final SharedPreferences qcache = context.getSharedPreferences(Questions.QCACHE, Activity.MODE_PRIVATE);
         final SharedPreferences acache = context.getSharedPreferences(Questions.ACACHE, Activity.MODE_PRIVATE);
 
         final Editor qeditor = qcache.edit();
         final Editor aeditor = acache.edit();
 
         for (int i = 0; i < ids.size(); i++) {
             qeditor.putString(Questions.CACHE_PREFIX + ids.get(i), questions.get(i).toJson());
             aeditor.putString(Questions.CACHE_PREFIX + ids.get(i), answers.get(i).toJson());
         }
 
         qeditor.apply();
         aeditor.apply();
 
        setTime(context, System.currentTimeMillis());
    }

    public static void setTime(final Context context, final long time) {
        Util.getState(context).edit().putLong("history", time).apply();
     }
 
     public static long getTime(final Context context) {
         return Util.getState(context).getLong("history", 0);
     }
 }
