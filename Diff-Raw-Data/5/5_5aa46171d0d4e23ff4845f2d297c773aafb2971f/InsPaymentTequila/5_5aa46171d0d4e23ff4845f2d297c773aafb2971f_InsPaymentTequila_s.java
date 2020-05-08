 package com.inspedio.system.helper.payment;
 
 import javax.microedition.lcdui.Graphics;
 
 import com.inspedio.entity.primitive.InsCallback;
 import com.inspedio.enums.KeyCode;
 import com.inspedio.system.core.InsGlobal;
 import com.inspedio.system.helper.InsKeys;
 import com.inspedio.system.helper.InsPointer;
 import com.tqm.tqp.EventListener;
 import com.tqm.tqp.PaymentListener;
 import com.tqm.tqp.TequilaPlanetApplication;
 import com.tqm.tqp.VirtualGoodDetails;
 
 public class InsPaymentTequila implements PaymentListener, EventListener{
 
 	private static TequilaPlanetApplication teqInstance = null;
 	public static boolean doneInit = false;
 	
 	private static InsPaymentTequila instance = null;
 	
 	private static InsCallback paymentSuccessCallback = null;
 	
 	private static InsCallback paymentFailedCallback = null;
 	
 	private InsPaymentTequila(){
 	}
 	
 	public static InsPaymentTequila getInstance(){
 		if(instance == null){
 			instance = new InsPaymentTequila();
 		}
 		return instance;
 	}
 	
 	public static void init(){
 		if(!doneInit){
 			teqInstance = TequilaPlanetApplication.getInstance(InsGlobal.midlet, TequilaPlanetApplication.LANG_ENGLISH);
 			teqInstance.addPaymentListener(getInstance());
 			teqInstance.setEventListener(getInstance());
 			if(teqInstance.getTQPNick() == null){
 	            teqInstance.setTQPNick("Default");
 	        }
 			doneInit = true;
 		}
 		showAdvertisement();
 	}
 	
 	public static void showAdvertisement(){
 		getTequilaInstance().showAdvertisementDialog();
 	}
 	
 	public static TequilaPlanetApplication getTequilaInstance(){
 		if(teqInstance == null){
 			init();
 		}
 		return teqInstance;
 	}
 	
 	public static VirtualGoodDetails[] getVirtualGoods(){
 		return getTequilaInstance().getVirtualGoodDetails();
 	}
 	
 	public static void requestPayment(VirtualGoodDetails vg, InsCallback successCallback, InsCallback failedCallback){
 		paymentSuccessCallback = successCallback;
 		paymentFailedCallback = failedCallback;
 		getTequilaInstance().showVirtualGoodPaymentDialog(vg);
 	}
 	
 	public void notifyPaymentStatus(int arg) {
 		switch (arg) {
 			case PAYMENT_STATUS_ACCEPTED:
 				if(paymentSuccessCallback != null){
 					paymentSuccessCallback.call();
 				}
 				break;
 			case PAYMENT_STATUS_REJECTED:
 				if(paymentFailedCallback != null){
 					paymentFailedCallback.call();
 				}
 				break;
 		}
 	}
 	
 	public void update(){
 		this.handleKeyEvent(InsGlobal.keys);
 		this.handlePointerEvent(InsGlobal.pointer);
 	}
 	
 	public void handleKeyEvent(InsKeys key){
 		for(int i = 0; i < 7; i++){
 			if(key.justPressed(KeyCode.getKey(i))){
 				getTequilaInstance().keyActionPressed(translateKeyCode(i));
 			}
 			
 			if(key.justReleased(KeyCode.getKey(i))){
 				getTequilaInstance().keyActionReleased(translateKeyCode(i));
 			}
 		}
 	}
 		
 	public int translateKeyCode(int Code){
 		switch (Code) {
 			case 0:
 				return TequilaPlanetApplication.KEY_ACTION_ARROW_LEFT;
 			case 1:
 				return TequilaPlanetApplication.KEY_ACTION_ARROW_RIGHT;
 			case 2:
 				return TequilaPlanetApplication.KEY_ACTION_ARROW_UP;
 			case 3:
 				return TequilaPlanetApplication.KEY_ACTION_ARROW_DOWN;
 			case 4:
 				return TequilaPlanetApplication.KEY_ACTION_SOFTKEY_MIDDLE;
 			case 5:
 				return TequilaPlanetApplication.KEY_ACTION_SOFTKEY_LEFT;
 			case 6:
 				return TequilaPlanetApplication.KEY_ACTION_SOFTKEY_RIGHT;
 		}
 		return 0;
 	}
 	
 	public void handlePointerEvent(InsPointer pointer){
 		for(int i = 0; i < pointer.pressed.length; i++){
 			getTequilaInstance().pointerPressed(pointer.pressed[i].x, pointer.pressed[i].y);
 		}
 		
 		for(int i = 0; i < pointer.released.length; i++){
			getTequilaInstance().pointerReleased(pointer.pressed[i].x, pointer.pressed[i].y);
 		}
 		
 		for(int i = 0; i < pointer.dragged.length; i++){
			getTequilaInstance().pointerDragged(pointer.pressed[i].x, pointer.pressed[i].y);
 		}
 	}
 	
 	public void draw(Graphics g){
 		getTequilaInstance().paint(g);
 	}
 	
 	public void dailyReward(int arg0) {
 	}
 
 	public void gainFocus() {
 		InsGlobal.onFocusPayment = false;
 		InsGlobal.resumeGame();
 	}
 
 	public boolean isAcceptingDailyReward() {
 		return false;
 	}
 
 	public boolean isAcceptingImages() {
 		return false;
 	}
 
 	public void loseFocus() {
 		InsGlobal.onFocusPayment = true;
 		InsGlobal.pauseGame();
 	}
 
 	public void requestQuit() {
 		InsGlobal.save.save();
 		InsGlobal.exitGame();
 	}
 
 	
 }
