 /*
  *  Copyright (C) 2013 caryoscelus
  *
  *  This program is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  *
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package gg;
 
 import com.badlogic.gdx.Gdx;
 
 import java.util.Map;
 import java.util.HashMap;
 
 public class Story {
     private static Story _instance;
     
    StoryObject[] objects;
     
     Map<Integer, String> eventNames = new HashMap();
     Map<String, StoryEvent> events = new HashMap();
     
     public StoryScreen screen;
     
//     private Story () {
//         eventNames = new HashMap();
//         events = new HashMap();
//     }
     
     public static Story instance () {
         if (_instance == null) {
             _instance = new Story();
         }
         return _instance;
     }
     
     
     public void addEvent (int id, String name, StoryEvent event) {
         Gdx.app.log("story", "addevent "+name+" "+id);
         
         eventNames.put(id, name);
         events.put(name, event);
     }
     
     public StoryEvent getEvent (int id) {
         return getEvent(eventNames.get(id));
     }
     
     public StoryEvent getEvent (String name) {
         return events.get(name);
     }
     
     public void trigger (int id) {
         trigger(getEvent(id));
     }
     
     public void trigger (String name) {
         trigger(getEvent(name));
     }
     
     public void trigger (StoryEvent event) {
         if (event != null) {
             event.trigger();
         }
     }
     
     
     public void ui (StoryDialog dialogue) {
         if (screen != null) {
             screen.showStory(dialogue);
         }
     }
 }
