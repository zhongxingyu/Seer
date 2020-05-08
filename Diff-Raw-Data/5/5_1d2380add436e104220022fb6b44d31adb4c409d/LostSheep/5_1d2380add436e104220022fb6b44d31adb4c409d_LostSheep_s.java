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
 
 package lostsheep;
 
 import chlorophytum.Scripting;
 import chlorophytum.UiManager;
 import chlorophytum.Loader;
 import chlorophytum.credits.CreditsScreen;
 import chlorophytum.util.Invokable;
 
 import com.badlogic.gdx.Game;
 import com.badlogic.gdx.Gdx;
 
 /**
  * Main class, not much interesting actually..
  */
 public class LostSheep extends Game {
     CreditsScreen creditsScreen = new CreditsScreen();
     MapScreen mapScreen = new MapScreen();
     
     @Override
     public void create () {
         Loader.instance().setMapPath("data/maps", ".tmx");
         Scripting.init();
         UiManager.instance().loadDefaultSkin();
         
        LostSheepGame.instance().init();
        
         setScreen(mapScreen);
     }
     
     @Override
     public void render () {
         getScreen().render(Gdx.graphics.getDeltaTime());
     }
     
     @Override
     public void resize (int width, int height) {
         getScreen().resize(width, height);
     }
     
     @Override
     public void pause () {
         getScreen().pause();
     }
     
     @Override
     public void resume () {
         getScreen().resume();
     }
     
     @Override
     public void dispose () {
         getScreen().dispose();
     }
 }
