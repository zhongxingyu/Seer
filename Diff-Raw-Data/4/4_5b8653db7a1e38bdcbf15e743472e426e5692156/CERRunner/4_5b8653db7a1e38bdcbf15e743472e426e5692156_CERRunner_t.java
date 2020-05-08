 /*
  	org.manalith.ircbot.plugin.cer2/CERRunner.java
  	ManalithBot - An open source IRC bot based on the PircBot Framework.
  	Copyright (C) 2011  Seong-ho, Cho <darkcircle.0426@gmail.com>
 
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
 package org.manalith.ircbot.plugin.cer2;
 
 import java.io.File;
 
 
import org.apache.commons.lang.ArrayUtils;
 import org.manalith.ircbot.common.PropertyManager;
 
 public class CERRunner {
 
 	private String[] args;
 	private String dataPath;
 	private String usernick;
 
 	public CERRunner() {
 		this.setArgs(null);
 		this.setDataPath("");
 	}
 
 	public CERRunner(String newUserNick, String[] newArgs) {
 		this.setUserNick(newUserNick);
 		this.setArgs(newArgs);
 		this.setDataPath("");
 	}
 
 	public CERRunner(String newUserNick, String newDataPath, String[] newArgs) {
 		this.setUserNick(newUserNick);
 		this.setDataPath(newDataPath);
 		File path = new File(this.getDataPath());
 
 		if (!path.exists())
 			path.mkdirs();
 
 		this.setArgs(newArgs);
 	}
 
 	public void setArgs(String[] newArgs) {
 		this.args = newArgs;
 	}
 
 	private String[] getArgs() {
 		return this.args;
 	}
 
 	public void setDataPath(String newDataPath) {
 		this.dataPath = newDataPath;
 	}
 
 	private String getDataPath() {
 		return this.dataPath;
 	}
 
 	private void setUserNick(String newUserNick) {
 		this.usernick = newUserNick;
 	}
 
 	private String getUserNick() {
 		return this.usernick;
 	}
 
 	public String run() throws Exception {
 		String result = "";
 
 		CERTableUpdater updater = new CERTableUpdater(this.getDataPath());
 		updater.update();
 
 		String [] cmd = null;
 		CERInfoProvider info = null;
 
		if (ArrayUtils.isEmpty(this.getArgs())) {
 			String[] default_currency = null;
 
 			PropertyManager prop = new PropertyManager(this.getDataPath(),
 					"customsetlist.prop");
 			prop.loadProperties();
 
 
 			String[] userlist = prop.getKeyList();
 			if (userlist == null) {
 				default_currency = new String[4];
 				default_currency[0] = "USD";
 				default_currency[1] = "EUR";
 				default_currency[2] = "JPY";
 				default_currency[3] = "CNY";
 			} else {
 				int existidx = this.indexOfContained(userlist,
 						this.getUserNick());
 				if (existidx != -1) {
 					default_currency = prop.getValue(userlist[existidx]).split(
 							"\\,");
 				} else {
 					default_currency = new String[4];
 					default_currency[0] = "USD";
 					default_currency[1] = "EUR";
 					default_currency[2] = "JPY";
 					default_currency[3] = "CNY";
 				}
 			}
 
 			for (int i = 0; i < default_currency.length; i++) {
 				
 				String [] args = new String[1];
 				args[0] = default_currency[i];
 				cmd = CERMessageTokenAnalyzer
 						.convertToCLICommandString(args);
 				info = new CERInfoProvider(this.getDataPath(), cmd);
 
 				if (i != 0)
 					result += ", " + info.commandInterpreter();
 				else
 					result += info.commandInterpreter();
 			}
 		} else {
 			cmd = CERMessageTokenAnalyzer.convertToCLICommandString(this
 					.getArgs());
 			info = new CERInfoProvider(this.getDataPath(), cmd);
 
 			result = info.commandInterpreter();
 		}
 
 		return result;
 	}
 
 	private int indexOfContained(String[] strarray, String value) {
 		int result = -1;
 		int length = strarray.length;
 		
 		for (int i = 0; i < length; i++) {
 			if (strarray[i].equals(value)) {
 				result = i;
 				break;
 			}
 		}
 
 		return result;
 	}
 }
