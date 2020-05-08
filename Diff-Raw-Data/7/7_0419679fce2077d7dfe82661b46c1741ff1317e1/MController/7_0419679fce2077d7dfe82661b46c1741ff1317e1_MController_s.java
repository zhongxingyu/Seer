 package com.mcode.mcontroller.core;
 
 import java.io.IOException;
 
 import network.multicast.Publisher;
 
 import com.badlogic.gdx.Game;
 import com.badlogic.gdx.Gdx;
import com.mcode.mcontroller.core.configs.MulticastConfig;
 import com.mcode.mcontroller.core.screens.MainControlScreen;
 
 public class MController extends Game {
 	private static final String TAG = "MController";
 	private Publisher pub;
 
 	@Override
 	public void create() {
 		try {
 			pub = new Publisher();
 		} catch (IOException e) {
 			Gdx.app.error(TAG, "Error instantiating publisher", e);
 		}
 		setScreen(new MainControlScreen(this));
 	}
 	
 	public void sendKeyBytes(byte[] byteArray) {
 		try {
 			pub.publish(byteArray);
 		} catch (IOException e) {
 			Gdx.app.error(TAG, "Error sending bytes", e);
 		}
 		
 	}
 
 }
