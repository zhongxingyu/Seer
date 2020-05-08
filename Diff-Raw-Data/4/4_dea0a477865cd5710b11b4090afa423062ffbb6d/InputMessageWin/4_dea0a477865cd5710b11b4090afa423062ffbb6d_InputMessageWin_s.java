 package org.alc.web.ui.window;
 
 import java.util.Date;
 
 import org.alc.util.SecurityUtil;
 import org.alc.util.ZkDateFormat;
 import org.alc.util.ZkEventUtil;
 import org.apache.log4j.Logger;
 import org.springframework.util.StringUtils;
 import org.zkoss.util.resource.Labels;
 import org.zkoss.zk.ui.Component;
 import org.zkoss.zk.ui.SuspendNotAllowedException;
 import org.zkoss.zk.ui.event.Event;
 import org.zkoss.zk.ui.event.EventListener;
 import org.zkoss.zk.ui.event.EventQueues;
 import org.zkoss.zk.ui.event.Events;
 import org.zkoss.zk.ui.event.KeyEvent;
 import org.zkoss.zul.Button;
 import org.zkoss.zul.Separator;
 import org.zkoss.zul.Textbox;
 import org.zkoss.zul.Window;
 
 public class InputMessageWin extends Window {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 	
 	private static final Logger logger = Logger.getLogger(InputMessageWin.class);
 	
 	private final Textbox textbox;
 	private StringBuffer msg = new StringBuffer();
 	private String userName = "";
 	
 	private InputMessageWin(Component comp) {
 		super();
 		
 		textbox = new Textbox();
 		
 		this.setParent(comp);
 		userName = SecurityUtil.getUser().getUsername();
 		System.out.println("UserName:" + userName);
 		createWin();
 	}
 	
 	public static String show (Component comp) {
 		return new InputMessageWin(comp).getMsg();
 	}
 
 
 	private void createWin() {
 		setWidth("450px");
 		setHeight("160px");
 		setTitle(Labels.getLabel("app.message.info.pleaseInsertText"));
 		setId("confBox");
 		setVisible(true);
 		setClosable(true);
		this.setCtrlKeys("^s@c");
 		
 		Separator sp = new Separator();
 		sp.setParent(this);
 		
 		textbox.setWidth("98%");
 		textbox.setHeight("80px");
 		textbox.setMultiline(true);
 		textbox.setRows(5);
 		textbox.setParent(this);
 		 
 		Separator sp2 = new Separator();
 		sp2.setBar(true);
 		sp2.setParent(this);
 		
 		Button btnSend = new Button();
 		btnSend.setLabel(Labels.getLabel("app.message.info.send"));
 		btnSend.setParent(this);
 		
 		/* 
 		 * @Event Listener
 		 * default action as 'ON_CLOSE'
 		 * 'enter' key pressed when finished typing text,close window 
 		 * 
 		 */ 		
 		addEventListener(Events.ON_CANCEL, ZkEventUtil.onCloseListener());
 		addEventListener(Events.ON_CTRL_KEY, new OnSendMsgListener());
 		btnSend.addEventListener(Events.ON_CLICK, new OnSendMsgListener());
 		
 		try {
 			doModal();
 		} catch (SuspendNotAllowedException e) {
 			logger.fatal("", e);
 		} 
 		
 	}
 	
 	private final class OnSendMsgListener implements EventListener<Event> {
 
 		@Override
 		public void onEvent(Event event) throws Exception {
 			if (StringUtils.isEmpty(StringUtils.trimAllWhitespace(textbox.getText()))) {
 				onClose();
 				return;
 			}
 				
 			sendMsg(textbox.getText());
 			onClose();			
 		}
 		
 	}
 	
 	private void sendMsg(String aMsg) {
 		this.msg.append(ZkDateFormat.getDateTimeLongFormater().format(new Date()) + 
 				"/ " + Labels.getLabel("app.message.info.from") + 
 				" [" + userName + "]:\n");
 		this.msg.append(aMsg);
 		this.msg.append("\n" + "_____________________________________________________" + "\n");
 		EventQueues.lookup("quickMessageEQ", EventQueues.APPLICATION, true).publish(new Event("aMessage", null, this.msg.toString()));
 	}
 
 	public String getMsg() {
 		return msg.toString();
 	}
 
 }
