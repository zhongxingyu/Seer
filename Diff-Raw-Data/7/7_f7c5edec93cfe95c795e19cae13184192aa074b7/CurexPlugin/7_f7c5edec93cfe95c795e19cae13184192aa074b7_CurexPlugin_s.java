 package org.manalith.ircbot.plugin.curex;
 
 import org.apache.commons.lang3.ArrayUtils;
 import org.manalith.ircbot.plugin.SimplePlugin;
 import org.manalith.ircbot.resources.MessageEvent;
 import org.springframework.stereotype.Component;
 
 @Component("curexPlugin")
 public class CurexPlugin extends SimplePlugin {
 
 	private String appid;
 
 	public void setAppid(String app_id) {
 		appid = app_id;
 	}
 
 	@Override
 	public String getName() {
 		return "환율계산기";
 	}
 
 	@Override
 	public String getCommands() {
 		return "!환율";
 	}
 
 	@Override
 	public String getHelp() {
 		return "설  명: 기본적으로 달러, 유로, 옌, 위안, 비트코인의 환율을 살펴볼 수 있습니다."
 				+ " 필요한 경우 다른 화폐의 환율을 볼 수 있으며, 지정 금액에 대한 환율 계산도 가능합니다."
 				+ " (자세한 도움말: !환율 help)";
 	}
 
 	@Override
 	public void onMessage(MessageEvent event) {
 		parseEvent(event);
 	}
 
 	@Override
 	public void onPrivateMessage(MessageEvent event) {
 		parseEvent(event);
 	}
 
 	protected void parseEvent(MessageEvent event) {
 		String[] irccmd = event.getMessageSegments();
 		String[] options = null;
 		String result = null;
 		String path = getResourcePath();
 
 		// list
 		if (irccmd[0].equals("!환율")) {
 			options = ArrayUtils.subarray(irccmd, 1, irccmd.length);
 			try {
 				CurexRunner runner = new CurexRunner(path, appid, options);
 				result = runner.run();
 			} catch (Exception e) {
 				result = e.getMessage();
				return;
 			}
		}
 
		event.respond(result);
 	}
 }
