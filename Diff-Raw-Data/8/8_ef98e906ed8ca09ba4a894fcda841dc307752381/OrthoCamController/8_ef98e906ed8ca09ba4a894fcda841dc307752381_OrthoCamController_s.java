 /*******************************************************************************
  * Copyright 2011 See AUTHORS file.
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *   http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  ******************************************************************************/
 
 package de.redlion.rts;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.InputAdapter;
 import com.badlogic.gdx.graphics.OrthographicCamera;
 import com.badlogic.gdx.math.Vector2;
 import com.badlogic.gdx.math.Vector3;
 
 public class OrthoCamController extends InputAdapter {
 	final OrthographicCamera camera;
 	final Vector3 curr = new Vector3();
 	final Vector2 last = new Vector2(0, 0);
 	final Vector2 delta = new Vector2();
 
 	public OrthoCamController (OrthographicCamera camera) {
 		this.camera = camera;
 	}
 
 	@Override
 	public boolean touchDragged (int x, int y, int pointer) {
 		delta.set(x, y).sub(last);
 		delta.mul(0.01f);
		camera.position.add(delta.x, delta.y, 0);
 		camera.update();
 		last.set(x, y);
 		return true;
 	}
 
 	@Override
 	public boolean touchUp (int x, int y, int pointer, int button) {
 		last.set(0, 0);
 		return true;
 	}
 	
 	@Override
 	public boolean touchDown (int x, int y, int pointer, int button) {
 		last.set(x, y);
 		return true;
 	}
 	
 	@Override
 	public boolean scrolled (int amount) {
 		camera.zoom -= -amount * Gdx.graphics.getDeltaTime() / 100;
 		camera.update();
 		return true;
 	}
 }
