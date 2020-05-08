 /*
 Mjdj MIDI Morph - an extensible MIDI processor and translator.
 Copyright (C) 2010 Confusionists, LLC (www.confusionists.com)
 
 This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version. This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 
 See the GNU General Public License for more details. You should have received a copy of the GNU General Public License along with this program. If not, see <http://www.gnu.org/licenses/>. 
 
 You may contact the author at mjdj_midi_morph [at] confusionists.com
 */
 package com.confusionists.mjdj.ui;
 
 import javax.sound.midi.ShortMessage;
 
 import org.codehaus.groovy.runtime.StackTraceUtils;
 
 import com.confusionists.mjdj.Universe;
 import com.confusionists.mjdj.midi.TransmitterWrapperImpl;
 import com.confusionists.mjdjApi.midi.MessageWrapper;
 import com.confusionists.mjdjApi.midi.ShortMessageWrapper;
 import com.confusionists.mjdjApi.midiDevice.DeviceWrapper;
 import com.confusionists.mjdjApi.util.Util;
 
 public class Logger {
 
 	public static void debugLog(String text, boolean linefeed) {
 		if (Universe.instance.main.debugToggle.isSelected())
 			log(text, linefeed);
 	}
 
 	public static void debugLog(String string, Throwable e) {
 		if (Universe.instance.isDebug())
 			log(string, e);
 
 	}
 
 	public static void debugLog(String text) {
 		if (Universe.instance.isDebug())
 			log(text);
 	}
 
 	public static void log(String text, boolean linefeed) {
 		try {
 			Universe.instance.main.logInner(text, linefeed);
 		} catch (Exception e) {
 			System.err.println(text);
 		}
 	}
 
 	public static void log(String string, Throwable e) {
 		e = StackTraceUtils.deepSanitize(e);
 		log("Error " + string + " (" + e.getMessage() + ")");
 		e.printStackTrace();
 	}
 
 	public static void log(String text) {
 		log(text, true);
 	}
 
 	public static void log(MessageWrapper messageIn, DeviceWrapper device) {
 		if (!Universe.instance.isDebug())
 			return;
 		StringBuffer logThis = new StringBuffer();
 		ShortMessageWrapper message = messageIn.getAsShortMessageWrapper();
 		if (message != null) {
 			ShortMessage shortMessage = message.getShortMessage();
 			String command = String.valueOf(shortMessage.getCommand());
 			if (shortMessage.getCommand() == ShortMessage.CONTROL_CHANGE) {
 				command = "Control Change";
 			} else if (shortMessage.getCommand() == ShortMessage.NOTE_OFF) {
 				command = "Note Off";
 			} else if (shortMessage.getCommand() == ShortMessage.NOTE_ON) {
 				command = "Note On";
 			}
			logThis.append("ShortMessage: command=" + command + ", channel=" + shortMessage.getChannel() + ", data1=" + shortMessage.getData1()
 					+ ", data2=" + shortMessage.getData2());
 		} else {
 			logThis.append("Message: " + Util.spitByteArrayAsHex(messageIn.getMessage().getMessage()));
 		}
 
 		if (device instanceof TransmitterWrapperImpl)
 			logThis.append(" IN from " + device.getName());
 		else
 			logThis.append(" OUT to " + device.getName());
 
 		log(logThis.toString());
 
 	}
 
 }
