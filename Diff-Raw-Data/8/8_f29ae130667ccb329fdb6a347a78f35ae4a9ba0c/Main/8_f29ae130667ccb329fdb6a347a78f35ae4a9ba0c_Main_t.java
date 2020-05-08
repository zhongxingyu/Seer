 package net.larry1123.elecentertainment.phantasm.core;
 
 import com.badlogic.gdx.ApplicationListener;
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.graphics.GL10;
 import com.badlogic.gdx.graphics.Texture;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 
 public class Main implements ApplicationListener {

    Texture t1;
    Texture t2;
     Texture texture;
     SpriteBatch batch;
     float elapsed;
 
     @Override
     public void create() {
        t1 = new Texture(Gdx.files.internal("libgdx-logo.png"));
        t2 = new Texture(Gdx.files.internal("ElecEntertainment.jpeg"));
         texture = t1;
         batch = new SpriteBatch();
     }
 
     @Override
     public void resize(int width, int height) {
         if (texture == t1) {
             texture = t2;
         } else {
             texture = t1;
         }
         this.render();
     }
 
     @Override
     public void render() {
         elapsed += Gdx.graphics.getDeltaTime();
         Gdx.gl.glClearColor(0, 0, 0, 0);
         Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
         batch.begin();
         batch.draw(texture, 100 + 100 * (float) Math.cos(elapsed), 100 + 25 * (float) Math.sin(elapsed));
         batch.end();
     }
 
     @Override
     public void pause() {
     }
 
     @Override
     public void resume() {
     }
 
     @Override
     public void dispose() {
     }
 }
