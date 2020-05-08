 package com.mcode.mcontroller.desktop;
 
 import java.awt.AWTException;
 import java.awt.Robot;
 import java.awt.event.KeyEvent;
 
 import network.multicast.MessageListener;
 import network.multicast.Subscriber;
 
import com.mcode.mcontroller.core.configs.MulticastConfig;
 import com.mcode.mjl.util.BitFlags;
 
 public class MControllerHost implements MessageListener {
 	private static final String TAG = "MControllerHost";
 	
 	private BitFlags lastConfig = new BitFlags(1);
 	private Robot r;
 	private MControllerHost() throws AWTException {
 		r = new Robot();
 	}
 	public static void main(String[] args) throws Exception {
 		Subscriber sub = new Subscriber();
 		sub.subscribe(new MControllerHost());
 	}
 	
 	private int getKey(int num) {
 		switch(num) {
 		case 0:
 			return KeyEvent.VK_1;
 		case 1:
 			return KeyEvent.VK_2;
 		case 2:
 			return KeyEvent.VK_3;
 		case 3:
 			return KeyEvent.VK_Q;
 		case 4:
 			return KeyEvent.VK_W;
 		case 5:
 			return KeyEvent.VK_E;
 		}
 		return KeyEvent.VK_0;
 	}
 	
 	@Override
 	public void messageReceived(byte[] data) {
 		System.out.println("recv");
 		BitFlags config = new BitFlags(data);
 		
 		for(int i = 0; i < 6; i++) {
 			if(config.get(i) != lastConfig.get(i)) {
 				if(config.get(i)) {
 					r.keyPress(getKey(i));
 					System.out.println("press");
 				} else {
 					r.keyRelease(getKey(i));
 					System.out.println("release");
 				}
 			}
 		}
 		lastConfig = config;
 	}
 }
