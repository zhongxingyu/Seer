 package com.dogonyalki;
 
 import com.dogonyalki.elements.ElementOther;
 import com.dogonyalki.elements.ElsePlayer;
 
 public class Client {
 
 	// ===========================================================
 	// Constants
 	// ===========================================================
 
 	// ===========================================================
 	// Fields
 	// ===========================================================
 
 	public int id = 0;
 	ElementOther[] elements = new ElementOther[10];
 
 	// ===========================================================
 	// Constructors
 	// ===========================================================
 
 	// ===========================================================
 	// Virtual methods
 	// ===========================================================
 
 	// ===========================================================
 	// Methods
 	// ===========================================================
 
 	public void getMessage() {
 
 		float[] message = new float[3 * GameActivity.elseplayers.length];
 
 		final float sin = (float) Math.sin(-message[3 * id + 2] - Math.PI / 2);
 		final float cos = (float) Math.cos(-message[3 * id + 2] - Math.PI / 2);
		
 		for (int i = GameActivity.elseplayers.length - 1; i > 0; --i) {
 			final ElsePlayer player = GameActivity.elseplayers[i];
 			final float x = message[3 * i] - message[3 * id];
 			final float y = message[3 * i + 1] - message[3 * id + 1];
 			player.setCenterPosition(x * cos - y * sin, x * sin + y * cos);
 
 			player.setCurrentTileIndex((int) (message[3 * i + 2] - (float) Math.atan2(player.getCenterY(),
 					player.getCenterX())));
 		}
 
 		for (int i = elements.length - 1; i > 0; --i) {
			final float x = elements[i].x - message[3 * id];
			final float y = elements[i].y - message[3 * id + 1];
			elements[i].setCenterPosition(x * cos - y * sin, x * sin + y * cos);
 
 			elements[i].setCurrentTileIndex((int) (elements[i].w - (float) Math.atan2(elements[i].getCenterY(),
 					elements[i].getCenterX())));
 		}
 	}
 }
