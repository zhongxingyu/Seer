 package com.lorent.vovo.service;
 
 import org.apache.log4j.Logger;
 
 import com.lorent.common.event.JNIEvent;
 import com.lorent.common.event.JNIEventAdapter;
 import com.lorent.common.service.BaseService;
 import com.lorent.common.util.OpenfireUtil;
 import com.lorent.util.LCCUtil;
 import com.lorent.vovo.Vovo;
 import com.lorent.vovo.util.Constants;
 import com.lorent.vovo.util.DataUtil;
 import com.lorent.vovo.util.MyDataBase;
 import com.lorent.vovo.util.MyOpenfireUtil;
 import com.lorent.vovo.util.PrivateDataUtil;
 import com.lorent.vovo.util.VovoStringUtil;
 
 public class LoginService extends BaseService {
 	
 	private Logger log = Logger.getLogger(LoginService.class);
 	private MyJNIEventAdapter lccAdapter = new MyJNIEventAdapter();
 	public void doLogin(String userName,String passPsw,String serverIP, int status) throws Exception{
 		log.info("do Login " + userName);
 		
 		
 			//openfire登录
 			int serverPort = context.getConfigManager().getIntProperty(Constants.ConfigKey.OPENFIRE_PORT.toString(), Constants.CONFIG_OPENFIRE_PORT);
 			int timeout = context.getConfigManager().getIntProperty(Constants.ConfigKey.timeout.toString(), Constants.CONFIG_TIME_OUT);
 			int localCSPort = context.getConfigManager().getIntProperty(Constants.ConfigKey.localcsport.toString(), Constants.CONFIG_LOCAL_CS_PORT);
 //			OpenfireUtil.init(serverIP, serverPort);
 //			OpenfireUtil.login(userName, passPsw);
 			try {
 				   OpenfireUtil.getInstance().init(serverIP, serverPort, timeout);
 				  } catch (Exception e1) {
 				   OpenfireUtil.getInstance().disconnect();
 				   log.error("doLogin", e1);
 				   throw new Exception(VovoStringUtil
 				     .getErrorString("error.server.ip"));
 				  }
 
 			MyOpenfireUtil.addListener();
 			try {
 				   OpenfireUtil.getInstance().login(userName, passPsw,
 				     Constants.OPENFIRE_RESOURCE);
 				  } catch (Exception e2) {
 				   log.error("doLogin", e2);
 				   OpenfireUtil.getInstance().disconnect();
 				   throw new Exception(VovoStringUtil
 				     .getErrorString("error.username_password.msg"));
 				  }
 //			MyOpenfireUtil.changeMyPresence(status);
 			DataUtil.setValue(Constants.DataKey.PrivateData, PrivateDataUtil.getLogInfo());
 			MyOpenfireUtil.addIQProvider();
 			DataUtil.setValue(Constants.DataKey.userName, userName);
 		
 		
 			initLCCUtil();
 			int csPort = context.getConfigManager().getIntProperty("csPort", 5060);
 //			LCCUtil.getInstance().register("sip:" + userName + "@" + serverIP + ":" + csPort, passPsw, "sip:" + serverIP + ":" + csPort, 0);
			LCCUtil.getInstance().register(userName, serverIP, csPort+"", passPsw, localCSPort);
//			LCCUtil.getInstance().registerForP2P(userName, serverIP, csPort+"", passPsw, localCSPort);
         //init database
         MyDataBase.init(userName);
         /*
         String mcuLocalPort = Vovo.getMyContext().getConfigManager().getIntProperty("csPort", 5060)+"";
         try {
 //            Thread.sleep(1000);
             if(DataUtil.getLvmcJNIListener() !=null ){
                 instance.removeEventListener(DataUtil.getLvmcJNIListener());
                 DataUtil.removeElement(Key.LvmcJNIListener);
             }
             DataUtil.setValue(Key.LvmcJNIListener, new LvmcJNIListener());
             instance.addEventListener(DataUtil.getLvmcJNIListener());
 //            if(DataUtil.getLccRegisterFlag()==null || DataUtil.getLccRegisterFlag().booleanValue() == false){
                 log.info("lvmc  LCCUtil  register=================================================");
                 instance.register("sip:" + username + "@" + mcuIP + ":" + mcuLocalPort, password, "sip:" + mcuIP + ":" + mcuLocalPort, 0);
 //                DataUtil.setValue(Key.LccRegisterFlag, Boolean.TRUE);
 //            }
             
         } catch (Exception e) {
             //log.error("", ex);
             throw e;
         }
         ControllerFacade.execute("shareDesktopController", "startScreenShareProcess");
         log.info("登录成功");
         */
 	}
 	
 	public void initLCCUtil(){
 		LCCUtil.getInstance().removeAllListener();
 		LCCUtil.getInstance().addEventListener(lccAdapter);
 		LCCUtil.getInstance().setVideo(true, null);
 		LCCUtil.getInstance().setPreview(false);
 		LCCUtil.getInstance().doSetLccType(LCCUtil.LCC_TYPE_MIX);
 		int width = context.getConfigManager().getIntProperty("videoWidth", 352);
 		int height = context.getConfigManager().getIntProperty("videoHeight", 288);
 		int bitrate = context.getConfigManager().getIntProperty("videoBitrate", 128);
 		LCCUtil.getInstance().doSetVideoSize(width, height);
 		LCCUtil.getInstance().doSetVideoBitrate(bitrate);
 	}
 
 	
 	public int doCheckSoundCard(){
         return LCCUtil.getInstance().doCheckSoundcard();
     }
 	
 	private class MyJNIEventAdapter extends JNIEventAdapter{
 		@Override
 		public void registerOkCallBack(JNIEvent event) {
 			Vovo.sendMessage("registerOK", null);
 		}
 		
 		@Override
 		public void registerFailCallBack(JNIEvent event) {
 			Vovo.sendMessage("registerFail", null);
 		}
 		
 		@Override
 		public void incomingCallBack(JNIEvent event) {
 			Vovo.sendMessage("callIncoming", event.getParas());
 		}
 		
 		@Override
 		public void connectedCallBack(JNIEvent event) {
 			Vovo.sendMessage("callConnected", event.getParas());
 		}
 		
 		@Override
 		public void callerrorCallBack(JNIEvent event) {
 			Vovo.sendMessage("callError", event.getParas());
 		}
 		
 		@Override
 		public void hangupCallBack(JNIEvent event) {
 			Vovo.sendMessage("callHangup", event.getParas());
 		}
 	}
 }
