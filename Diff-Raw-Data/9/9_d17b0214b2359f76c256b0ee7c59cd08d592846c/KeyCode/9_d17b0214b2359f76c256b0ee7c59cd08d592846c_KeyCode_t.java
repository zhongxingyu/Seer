 package com.inspedio.enums;
 
 import com.inspedio.system.core.InsGlobal;
 
 public class KeyCode extends InsEnum{
 
 	public static final KeyCode LEFT = new KeyCode("LEFT", 0);
 	public static final KeyCode RIGHT = new KeyCode("RIGHT", 1);
 	public static final KeyCode UP = new KeyCode("UP", 2);
 	public static final KeyCode DOWN = new KeyCode("DOWN", 3);
 	public static final KeyCode FIRE = new KeyCode("FIRE", 4);
 	public static final KeyCode SOFTKEY_LEFT = new KeyCode("SOFTKEY_LEFT", 5);
 	public static final KeyCode SOFTKEY_RIGHT = new KeyCode("SOFTKEY_RIGHT", 6);
 	public static final KeyCode GAME_A = new KeyCode("GAME_A", 7);
 	public static final KeyCode GAME_B = new KeyCode("GAME_B", 8);
 	public static final KeyCode GAME_C = new KeyCode("GAME_C", 9);
 	public static final KeyCode GAME_D = new KeyCode("GAME_D", 10);
 	
 	protected KeyCode(String Name, int Value) {
 		super(Name, Value);
 	}
 	
 	/**
 	 * Return keycode considering rotated screen
 	 */
 	public static KeyCode GetDynamicCode(KeyCode K){
		if(InsGlobal.isScreenRotated && !InsGlobal.onFocusPayment){
 			if(K == UP){
 				return LEFT;
 			} else if(K == RIGHT){
 				return UP;
 			} else if(K == DOWN){
 				return RIGHT;
 			} else if(K == LEFT){
 				return DOWN;
 			}
 		}
 		return K;
 	}
 	
 	public static KeyCode getKey(int Code){
 		switch (Code) {
 			case 0:
 				return LEFT;
 			case 1:
 				return RIGHT;
 			case 2:
 				return UP;
 			case 3:
 				return DOWN;
 			case 4:
 				return FIRE;
 			case 5:
 				return SOFTKEY_LEFT;
 			case 6:
 				return SOFTKEY_RIGHT;	
 			case 7:
 				return GAME_A;
 			case 8:
 				return GAME_B;
 			case 9:
 				return GAME_C;
 			case 10:
 				return GAME_D;
 		}
 		
 		return null;
 	}
 	
 
 }
