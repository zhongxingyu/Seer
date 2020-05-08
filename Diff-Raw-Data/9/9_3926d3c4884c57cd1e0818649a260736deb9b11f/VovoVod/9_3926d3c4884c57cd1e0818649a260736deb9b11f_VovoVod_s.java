 package com.lorent.vovo;
 
 import java.net.URL;
 import java.util.Calendar;
 import java.util.TimeZone;
 
 import javax.swing.JOptionPane;
 import javax.swing.SwingUtilities;
 import javax.swing.UIManager;
 
 import com.jtattoo.plaf.hifi.HiFiLookAndFeel;
 import com.jtattoo.plaf.mcwin.McWinLookAndFeel;
 import com.lorent.common.app.BaseApplication;
 import com.lorent.common.util.LCMUtil;
 import com.lorent.common.util.ProcessUtil;
 import com.lorent.common.util.StringUtil;
 import com.lorent.util.LCCUtil;
 import com.lorent.vovo.dto.LoginInfo;
 import com.lorent.vovo.util.Constants;
 
 public class VovoVod extends BaseApplication {
 
 	@Override
 	protected String getConfigFilePath() {
 		log.info("VovoVod config file : "+Constants.CONFIG_PATH);
 		log.info("java.library.path: "+System.getProperty("java.library.path"));
 //		LCCUtil.getInstance();
 		return Constants.CONFIG_PATH;
 //		return "c:\\vovovod.conf";
 	}
 
 	@Override
 	protected String getContextPath() {
 		return "classpath:com/lorent/vovo/config/applicationContext-*.xml";
 	}
 
 	@Override
 	protected String getFilterPath() {
 		return "/com/lorent/vovo/config/filter_conf.xml";
 	}
 
 	@Override
 	protected String getI18nPath() {
 		return "com/lorent/vovo/resource/i18n/view";
 	}
 
 	@Override
 	protected String getListenerPath() {
 		return "/com/lorent/vovo/config/event_listener.xml";
 	}
 
 	@Override
 	protected String getPermissionFilePath() {
 		return "/com/lorent/vovo/config/permission_config.xml";
 	}
 
 	@Override
 	protected void startApp() throws Exception {
 		Calendar c = Calendar.getInstance(TimeZone.getTimeZone("GMT+08:00"));
 		int year = c.get(Calendar.YEAR);
 		int month = c.get(Calendar.MONTH);
 		int day = c.get(Calendar.DATE);
		if (year >= 2013 && month >= 1) {
 			JOptionPane.showMessageDialog(null, "超过期限");
 			System.exit(0);
 		}
 		
 		new Thread(){
 
 			@Override
 			public void run() {
 				try {
 					String cmdstr = "cmd /c "+StringUtil.convertFilePath2DOSCommandStr(Constants.USER_DIR+"\\processwatch.exe");
 					ProcessUtil.getInstance().startProcess(cmdstr);
 					log.info(cmdstr);
 				} catch (Exception e) {
 					log.error("startApp", e);
 					e.printStackTrace();
 				}
 			}
 			
 		}.start();
 		
 		try {
 			UIManager.setLookAndFeel(new HiFiLookAndFeel());
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		
 		SwingUtilities.invokeLater(new Runnable() {
 			@Override
 			public void run() {
 				exeC("vod", "showVodFrame");
 			}
 		});
 	}
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		VovoVod instance = new VovoVod();
 		URL location = instance.getClass().getProtectionDomain().getCodeSource().getLocation();
 		System.out.println(location);
 		instance.setContext(new VovoVodContext());
 		instance.execute();
 	}
 
 	public static LCMUtil getLcmUtil() throws Exception{
 		String xmlRpcUrl = "http://"+VovoVod.getMyContext().getConfigManager().getProperty("serverIP","10.168.250.12")+ VovoVod.getMyContext().getConfigManager().getProperty("lcm.xmlrpc", ":6090/lcmRpc");
 		return LCMUtil.newInstance(xmlRpcUrl);
 	}
 	
 	public static VovoVodContext getMyContext(){
 		return (VovoVodContext) getContext(VovoVod.class);
 	}
 	
 	public static Object exeC(String className, String methodName, Object... paras){
 		return getMyContext().getExecuteManager().executeController(className, methodName, paras);
 	}
 }
