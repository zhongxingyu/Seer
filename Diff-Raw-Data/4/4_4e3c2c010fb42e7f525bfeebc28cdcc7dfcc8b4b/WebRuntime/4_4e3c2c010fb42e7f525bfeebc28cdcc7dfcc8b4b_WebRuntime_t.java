 // Copyright 2008 Google Inc.
 //
 // Licensed under the Apache License, Version 2.0 (the "License");
 // you may not use this file except in compliance with the License.
 // You may obtain a copy of the License at
 //
 // http://www.apache.org/licenses/LICENSE-2.0
 //
 // Unless required by applicable law or agreed to in writing, software
 // distributed under the License is distributed on an "AS IS" BASIS,
 // WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 // See the License for the specific language governing permissions and
 // limitations under the License.
 
 package com.ooksi;
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.DataInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStreamWriter;
 import java.util.Enumeration;
 import java.util.Hashtable;
 import java.util.Vector;
 
 import javax.microedition.lcdui.Command;
 import javax.microedition.lcdui.CommandListener;
 import javax.microedition.lcdui.Display;
 import javax.microedition.lcdui.Displayable;
 import javax.microedition.lcdui.Form;
 import javax.microedition.lcdui.Image;
 import javax.microedition.lcdui.ImageItem;
 import javax.microedition.lcdui.Item;
 import javax.microedition.lcdui.ItemCommandListener;
 import javax.microedition.lcdui.List;
 import javax.microedition.midlet.MIDlet;
 import javax.microedition.midlet.MIDletStateChangeException;
 
 import org.kobjects.utils4me.Registry;
 
 import com.google.minijoe.compiler.CompilerException;
 import com.google.minijoe.compiler.Eval;
 import com.google.minijoe.sys.JsArray;
 import com.google.minijoe.sys.JsFunction;
 import com.ooksi.apps.AppData;
 import com.ooksi.apps.AppList;
 import com.ooksi.net.HTTPResultListener;
 import com.ooksi.net.Http;
 import com.sun.midp.publickeystore.WebPublicKeyStore;
 import com.sun.perseus.model.Set;
 
 /**
  * Client to execute js code (src or compiled) downloaded from a webservice.
  * Expected api of webservice:
  * 
 * .../list - list all avilable apps in csv format of ID, Name, TypeFlag (c or
 * s) .../apps/ID/code - the app code in either src or compiled form
  * 
  * @author Maksim Lin
  */
 public class WebRuntime extends OoksiMIDlet 
 	implements CommandListener, ItemCommandListener {
 
 	static final Command CMD_EXIT = new Command("Exit", Command.EXIT, 1);
 	static String kalaInitApp;
 	static final AppList appStore = new AppList();
 	static final Hashtable installedApps = new Hashtable();
 
 	public WebRuntime() {
 
 	}
 
 	protected void destroyApp(boolean unconditional)
 			throws MIDletStateChangeException {
 	}
 
 	protected void pauseApp() {
 	}
 
 	public void startApp() throws MIDletStateChangeException {
 
 		Form f = new Form("Ooksi Apps");
 		Image img = null;
 		try {
 			img = Image.createImage("/orca.png");
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		//Enumeration apps = appList.keys();
 		while(apps.hasMoreElements()) {
 			//AppData = apps.nextElement()
 		
 		
 			ImageItem item = new ImageItem("App One", 
 					img, 
 					Item.LAYOUT_LEFT,
 					"app button",
 					Item.BUTTON);
 			
 			item.setDefaultCommand(new Command("run", Command.ITEM, 1));
 			item.setItemCommandListener(this);
 			
 			f.append(item);
 		
 		}
 		Display.getDisplay(this).setCurrent(f);
 
 		
 //		try {
 //			String initAppName = Registry.get("init", "missing");
 //			System.out.println("Init is:" + initAppName);
 //			execJs(initAppName);
 //		} catch (Exception e) {
 //			// TODO Auto-generated catch block
 //			e.printStackTrace();
 //		}
 	}
 
 	public void commandAction(Command cmd, Displayable d) {
 		if (cmd == CMD_EXIT) {
 			notifyDestroyed();
 		}
 	}
 
 	private void execJs(String appname) {
 		MIDPEnvironment env = new MIDPEnvironment(this);
 
 		Display.getDisplay(this).setCurrent(env.screen);
 
 		try {
 			System.out.println("running " + appname + ":...");
 			if (appname.endsWith(".mjc")) {
 				JsFunction.exec(new DataInputStream(getClass()
 						.getResourceAsStream(appname)), env);
 			} else {
 				String jsSrcText = MIDPUtils.readFileAsText(appname);
 				Eval.eval(jsSrcText, env);
 			}
 			new Thread(env).start();
 		} catch (IOException e) {
 			System.out.println("ERR:"+e.toString());
 			throw new RuntimeException(e.toString());
 		} catch (CompilerException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 	}
 
 	public Object initStartDisplay() {
 		execJs(kalaInitApp);
 		return null;
 	}
 
 	public void commandAction(Command c, Item item) {
 		//execJs((String)appList.get(item.getLabel()));		
 	}
 }
