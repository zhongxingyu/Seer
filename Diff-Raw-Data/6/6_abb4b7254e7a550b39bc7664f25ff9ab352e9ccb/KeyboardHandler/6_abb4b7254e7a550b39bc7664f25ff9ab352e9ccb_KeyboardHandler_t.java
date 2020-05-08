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
  * 
  *  Additional permission under GNU GPL version 3 section 7:
  *  If you modify this Program, or any covered work, by linking or combining
  *  it with Clojure (or a modified version of that library), containing parts
  *  covered by the terms of EPL 1.0, the licensors of this Program grant you
  *  additional permission to convey the resulting work. {Corresponding Source
  *  for a non-source form of such a combination shall include the source code
  *  for the parts of Clojure used as well as that of the covered work.}
  */
 
 package chlorophytum;
 
 import com.badlogic.gdx.*;
 
 import java.util.Map;
 import java.util.HashMap;
 
 public class KeyboardHandler {
     protected Map<Integer,Boolean> pressed = new HashMap();
     protected Map<Integer,Object> keyActions = new HashMap();
     
    public void init () {
        Scripting.run("data/scripts/base-input.clj");
        Scripting.run("data/scripts/input.clj");
        Scripting.call("input", "setup-input");
    }
    
     /**
      * Logic update
      * @param dt float delta time in seconds
      */
     public void update (float dt) {
         for (Map.Entry<Integer, Object> entry : keyActions.entrySet()) {
             Integer key = entry.getKey();
             Object action = entry.getValue();
             if (Gdx.input.isKeyPressed(key)) {
                 if (!pressed.get(key)) {
                     pressed.put(key, true);
                     Scripting.call(action);
                 }
             } else {
                 pressed.put(key, false);
             }
         }
     }
     
     public void registerKeyAction (Integer key, Object action) {
         keyActions.put(key, action);
     }
 }
