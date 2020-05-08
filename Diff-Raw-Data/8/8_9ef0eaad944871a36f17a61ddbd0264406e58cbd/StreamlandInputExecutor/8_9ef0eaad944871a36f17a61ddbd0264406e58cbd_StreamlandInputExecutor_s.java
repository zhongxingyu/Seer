 /*
  * This file is part of Streamland.
  *
  * Copyright (c) 2012, Spout LLC <http://www.spout.org/>
  *
  * Permission is hereby granted, free of charge, to any person obtaining a copy of
  * this software and associated documentation files (the "Software"), to deal in
  * the Software without restriction, including without limitation the rights to
  * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
  * of the Software, and to permit persons to whom the Software is furnished to do
  * so, subject to the following conditions:
  *
  * The above copyright notice and this permission notice shall be included in all
  * copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
  * SOFTWARE.
  */
 package org.spout.droplet.streamland.command;
 
 import org.spout.api.component.impl.CameraComponent;
 import org.spout.api.component.impl.SceneComponent;
 import org.spout.api.entity.Player;
 import org.spout.api.entity.state.PlayerInputState;
 import org.spout.api.geo.discrete.Transform;
 import org.spout.api.input.InputExecutor;
 import org.spout.api.math.QuaternionMath;
 import org.spout.api.math.Vector3;
 
 public class StreamlandInputExecutor implements InputExecutor {
 	private Player player;
 	private CameraComponent camera;
 	private float speed = 50;
 
 	public StreamlandInputExecutor(Player player) {
 		this.player = player;
 		camera = player.get(CameraComponent.class);
 	}
 
 	public void execute(float dt, Transform tranform) {
 		PlayerInputState inputState = player.input();
 		SceneComponent sc = player.getScene();
 		Transform ts = sc.getTransform(); //TODO: Maybe need getTransformLive?
 
 		Vector3 offset = Vector3.ZERO;
 		if (inputState.getForward()) {
 			offset = offset.subtract(ts.forwardVector().multiply(speed).multiply(dt));
 		}
 		if (inputState.getBackward()) {
 			offset = offset.add(ts.forwardVector().multiply(speed).multiply(dt));
 		}
 		if (inputState.getLeft()) {
 			offset = offset.subtract(ts.rightVector().multiply(speed).multiply(dt));
 		}
 		if (inputState.getRight()) {
 			offset = offset.add(ts.rightVector().multiply(speed).multiply(dt));
 		}
 		if (inputState.getJump()) {
 			offset = offset.add(ts.upVector().multiply(speed).multiply(dt));
 		}
 		if (inputState.getCrouch()) {
 			offset = offset.subtract(ts.upVector().multiply(speed).multiply(dt));
 		}
 
 		ts.translateAndSetRotation(offset, QuaternionMath.rotation(inputState.pitch(), inputState.yaw(), ts.getRotation().getRoll()));
 		sc.setTransform(ts);
 	}
 }
