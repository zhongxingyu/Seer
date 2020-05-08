 /*
  	org.manalith.ircbot/ManalithBot.java
  	ManalithBot - An open source IRC bot based on the PircBot Framework.
  	Copyright (C) 2005, 2011  Ki-Beom, Kim
  	Copyright (C) 2011, 2012  Seong-ho, Cho <darkcircle.0426@gmail.com>
 
     This program is free software: you can redistribute it and/or modify
     it under the terms of the GNU General Public License as published by
     the Free Software Foundation, either version 3 of the License, or
     (at your option) any later version.
 
     This program is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
     GNU General Public License for more details.
 
     You should have received a copy of the GNU General Public License
     along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package org.manalith.ircbot;
 
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.util.StringTokenizer;
 
 import javax.annotation.PostConstruct;
 
 import org.apache.log4j.Logger;
 import org.manalith.ircbot.plugin.EventDispatcher;
 import org.manalith.ircbot.plugin.EventLogger;
 import org.manalith.ircbot.plugin.relay.RelayPlugin;
 import org.manalith.ircbot.resources.MessageEvent;
 import org.manalith.ircbot.util.AppContextUtils;
 import org.pircbotx.PircBotX;
 import org.pircbotx.User;
 import org.pircbotx.exception.IrcException;
 import org.pircbotx.exception.NickAlreadyInUseException;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Component;
 
 @Component
 public class ManalithBot extends PircBotX {
 	private Logger logger = Logger.getLogger(getClass());
 
 	@Autowired
 	private Configuration configuration;
 
 	@Autowired
 	private EventLogger eventLogger;
 
 	@Autowired
 	private EventDispatcher eventDispatcher;
 
 	@PostConstruct
 	public void onPostConstruct() {
 		getListenerManager().addListener(eventLogger);
 		getListenerManager().addListener(eventDispatcher);
 	}
 
 	public static ManalithBot getInstance() {
 		return AppContextUtils.getApplicationContext().getBean(
 				ManalithBot.class);
 	}
 
 	/**
 	 * Configuration으로부터 설정을 읽어들여 봇을 시작한다.
 	 * 
 	 * @throws Exception
 	 *             봇을 설정하고 구동할 때 발생하는 예외
 	 */
 	public void start() throws IOException, IrcException {
 		setLogin(configuration.getBotLogin());
 		setName(configuration.getBotName());
 		setVerbose(configuration.isVerbose());
 
 		try {
 			setEncoding(configuration.getServerEncoding());
 		} catch (UnsupportedEncodingException e) {
 			logger.error("지원되지 않는 인코딩입니다.");
 			return;
 		}
 
 		try {
 			connect(configuration.getServer(), configuration.getServerPort());
 		} catch (NickAlreadyInUseException e) {
 			logger.error("닉네임이 이미 사용중입니다.");
 			return;
 		} catch (IOException | IrcException e) {
 			throw e;
 		}
 
 		StringTokenizer st = new StringTokenizer(
 				configuration.getDefaultChannels(), ",");
 		while (st.hasMoreTokens())
 			joinChannel(st.nextToken());
 	}
 
 	public void invokeMessageEvent(MessageEvent event) {
 		eventDispatcher.dispatchMessageEvent(event);
 	}
 
 	public void sendRawLineSplit(String prefix, String message, String suffix) {
 		// Make sure suffix is valid
 		if (suffix == null)
 			suffix = "";
 
 		// Make a server side prefix string
 		User b = this.getUserBot();
 		String serverSidePrefix = ":" + b.getNick() + "!" + b.getLogin() + "@"
 				+ b.getHostmask() + " ";
 
 		byte[] serverSidePrefixByteArray = serverSidePrefix.getBytes();
 		byte[] prefixByteArray = prefix.getBytes();
 		byte[] messageByteArray = message.getBytes();
 		byte[] suffixByteArray = suffix.getBytes();
 		byte[] messageByteArrayCopy;
 
 		// Find if final line is going to be shorter than the max line length
 		String finalMessage = prefix + message + suffix;
 
 		int realMaxLineLength = getMaxLineLength() - 2; // except CR+LF
 
 		if (!autoSplitMessage
 				|| serverSidePrefixByteArray.length + prefixByteArray.length
 						+ messageByteArray.length + suffixByteArray.length < realMaxLineLength) {
 			// Length is good (or auto split message is false),
 			// just go ahead and send it
 			sendRawLine(finalMessage);
 			return;
 		}
 
 		// Too long, split it up ( with considering server side prefix length )
 		int maxMessageLength = realMaxLineLength
 				- (serverSidePrefixByteArray.length + prefixByteArray.length + suffixByteArray.length);
 
 		int startPoint = 0;
 		int endPoint = -1;
 		byte val;
 
 		while (true) {
 			endPoint = ((startPoint + maxMessageLength < messageByteArray.length) ? (startPoint + maxMessageLength)
 					: messageByteArray.length) - 1;
 
 			// some characters ( such as cjk character ) need to separate
 			// correctly.
 			while (true) {
 
 				if (endPoint == messageByteArray.length - 1)
 					break;
 
 				val = (byte) (messageByteArray[endPoint] & 0xC0);
 
 				if (val == 0x80) {
 					// upper ASCII area
 					endPoint--;
 				} else if (val == 0x40 || val == 0x0) {
 					// within ASCII area
 					break;
 				} else {
 					// highest byte of UTF-8 code
 					endPoint--;
 					break;
 				}
 			}
 
 			// extract some part of byte array
 			messageByteArrayCopy = new byte[endPoint - startPoint + 1];
 			System.arraycopy(messageByteArray, startPoint,
 					messageByteArrayCopy, 0, endPoint - startPoint + 1);
 
 			// convert from byte array to string and concatenate!
 			sendRawLine(prefix + new String(messageByteArrayCopy) + suffix);
 
 			startPoint = endPoint + 1;
 
 			// if endPoint reached to last index of messageByteArray,
 			// stop to separate raw line.
 			if (startPoint == messageByteArray.length)
 				break;
 		}
 	}
 
 	@Override
 	public void sendMessage(String target, String message) {
 		sendMessage(target, message, true);
 	}
 
 	public void sendMessage(String target, String message, boolean needRelay) {
 		// //너무 긴 문자는 자른다.
 		// if(m.getMessage().length() > 180)
 		// m.setMessage(m.getMessage().substring(0, 179));
 		// sendMessage(m.getChannel(), m.getSender() + ", " + m.getMessage());
 
 		super.sendMessage(target, message);
 
 		if (logger.isInfoEnabled())
 			logger.info(String.format("MESSAGE(LOCAL) : %s / %s", target,
 					message));
 
 		if (needRelay && RelayPlugin.RELAY_BOT != null)
 			RelayPlugin.RELAY_BOT.sendMessage(target, message);
 	}
 }
