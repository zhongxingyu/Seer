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
 
 import com.badlogic.gdx.*;
 import com.badlogic.gdx.graphics.*;
 import com.badlogic.gdx.graphics.g2d.*;
 import com.badlogic.gdx.scenes.scene2d.*;
 import com.badlogic.gdx.scenes.scene2d.ui.*;
 import com.badlogic.gdx.maps.tiled.*;
 import com.badlogic.gdx.maps.tiled.renderers.*;
 import com.badlogic.gdx.math.*;
 import com.badlogic.gdx.utils.*;
 import com.badlogic.gdx.Input.*;
 import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
 
 import java.lang.Math;
 import java.util.Map;
 import java.util.HashMap;
 import java.io.IOException;
 
 import clojure.lang.RT;
 import clojure.lang.Var;
 import clojure.lang.Compiler;
 
 public class MapScreen implements Screen, StoryScreen {
     // renderers
     OrthogonalTiledMapRenderer renderer;
     OrthographicCamera camera;
     
     final float TILES_NX = 25;
     final float TILES_NY = TILES_NX*3/4f;
     
     final float TILE_SIZE = 16;
     
     // data
     TiledMap map;
     
     Texture personSprite;
     
     // gameplay
     Person person;
     
     
     boolean inited = false;
     
     // input
     Map<Integer,Boolean> pressed = new HashMap();
     
     
     // UI
     Skin skin;
     
     Stage storyStage;
     boolean showStory = false;
     
     public void init () {
         map = new TmxMapLoader().load("data/maps/map.tmx");
         
         renderer = new OrthogonalTiledMapRenderer(map, 1 / TILE_SIZE);
         
         camera = new OrthographicCamera();
         camera.setToOrtho(false, TILES_NX, TILES_NY);
         camera.update();
         
         // move to gameplay management
         
         person = new Person();
         person.moveTo(map, new Vector2(50, 50));
         
         
 //         personSprite = new Texture("data/maps/char-1.png");
         
         // UI
         initUiSkins();
         
         Story.instance().screen = this;
         storyStage = new Stage();
         
         initStory();
     }
     
     // move to story
     void initStory () {
         try {
             // if this removed, crash occurs..; could be replaced by access to
             // any static member of RT though
             RT.init();
             
             // libs
             Compiler.loadFile("data/scripts/base.clj");
             
             Compiler.loadFile("data/scripts/story.clj");
         } catch (IOException e) {
             Gdx.app.error("clojure", "can't find file", e);
         }
         
         // show help
         Story.instance().trigger("help");
     }
     
     void initUiSkins () {
         // A skin can be loaded via JSON or defined programmatically, either is fine. Using a skin is optional but strongly
         // recommended solely for the convenience of getting a texture, region, etc as a drawable, tinted drawable, etc.
         skin = new Skin();
 
         // Generate a 1x1 white texture and store it in the skin named "white".
         Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
         pixmap.setColor(Color.WHITE);
         pixmap.fill();
         skin.add("white", new Texture(pixmap));
 
         // Store the default libgdx font under the name "default".
         skin.add("default", new BitmapFont());
 
         // Configure a TextButtonStyle and name it "default". Skin resources are stored by type, so this doesn't overwrite the font.
         TextButton.TextButtonStyle textButtonStyle = new TextButton.TextButtonStyle();
         textButtonStyle.up = skin.newDrawable("white", Color.DARK_GRAY);
         textButtonStyle.down = skin.newDrawable("white", Color.DARK_GRAY);
         textButtonStyle.checked = skin.newDrawable("white", Color.BLUE);
         textButtonStyle.over = skin.newDrawable("white", Color.LIGHT_GRAY);
         textButtonStyle.font = skin.getFont("default");
         skin.add("default", textButtonStyle);
         
         Label.LabelStyle labelStyle = new Label.LabelStyle();
         labelStyle.font = skin.getFont("default");
         labelStyle.fontColor = Color.DARK_GRAY;
        labelStyle.background = skin.newDrawable("white", Color.LIGHT_GRAY);
         skin.add("default", labelStyle);
         
         Window.WindowStyle windowStyle = new Window.WindowStyle();
         windowStyle.titleFont = skin.getFont("default");
         windowStyle.background = skin.newDrawable("white", Color.LIGHT_GRAY);
         skin.add("default", windowStyle);
     }
     
     @Override
     public void show () {
         if (!inited) {
             init();
         }
         
         updateStoryStage();
     }
     
     @Override
     public void hide () {
         
     }
     
     public void update(float dt) {
         // check movement
         float mdx = 0, mdy = 0;
         if (Gdx.input.isKeyPressed(Keys.LEFT)) {
             mdx += -1*dt;
         }
         if (Gdx.input.isKeyPressed(Keys.RIGHT)) {
             mdx += 1*dt;
         }
         if (Gdx.input.isKeyPressed(Keys.UP)) {
             mdy += 1*dt;
         }
         if (Gdx.input.isKeyPressed(Keys.DOWN)) {
             mdy += -1*dt;
         }
         
         person.move.x = mdx;
         person.move.y = mdy;
         person.update(dt);
         
         
         // check feature
         if (Gdx.input.isKeyPressed(Keys.SPACE)) {
             if (!pressed.get(Keys.SPACE)) {
                 pressed.put(Keys.SPACE, true);
                 person.clicked();
             }
         } else {
             pressed.put(Keys.SPACE, false);
         }
         
         
         //check help
         if (Gdx.input.isKeyPressed(Keys.F1)) {
             Story.instance().trigger("help");
         }
     }
     
     @Override
     public void render (float dt) {
         update(dt);
         
         Gdx.gl.glClearColor(0.7f, 0.7f, 0.7f, 1);
         Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
         
         camera.position.x = person.position.x + 1;
         camera.position.y = person.position.y - 1;
         camera.update();
         
         renderer.setView(camera);
         renderer.render();
         
         person.render(renderer.getSpriteBatch());
         
         if (showStory) {
             storyStage.act(dt);
             storyStage.draw();
         }
     }
     
     @Override
     public void resize (int width, int height) {
     }
     
     @Override
     public void pause () {
     }
     
     @Override
     public void resume () {
     }
     
     @Override
     public void dispose () {
         map.dispose();
     }
     
     
     // UI
     void updateStoryStage () {
         if (showStory) {
             Gdx.input.setInputProcessor(storyStage);
         }
     }
     
     void setupStoryStageUi (StoryDialog dialogue) {
//         Stack stack = new Stack();
//         stack.setFillParent(true);
        
        Table table = new Table();
         table.setFillParent(true);
         
        Window window = new Window("text", skin);
        window.setFillParent(true);
         
         final Label label = new Label(dialogue.text, skin);
        window.add(label).space(2);
         
         // dialogue options
         for (Map.Entry<String, StoryEvent> entry : dialogue.options.entrySet()) {
             final String text = entry.getKey();
             final StoryEvent event = entry.getValue();
             
             final TextButton button = new TextButton(text, skin);
             button.addListener(new ChangeListener() {
                 public void changed (ChangeEvent cevent, Actor actor) {
                     boolean result = event.trigger();
                     if (!result) {
                         hideStory();
                     }
                 }
             });
            window.row();
            window.add(button).width(256).space(2);
         }
         
        table.add(window).expand();
         
         storyStage.addActor(table);
     }
     
     @Override
     public void showStory (StoryDialog dialogue) {
         showStory = true;
         
         setupStoryStageUi(dialogue);
         
         updateStoryStage();
     }
     
     @Override
     public void hideStory () {
         showStory = false;
         updateStoryStage();
     }
 }
