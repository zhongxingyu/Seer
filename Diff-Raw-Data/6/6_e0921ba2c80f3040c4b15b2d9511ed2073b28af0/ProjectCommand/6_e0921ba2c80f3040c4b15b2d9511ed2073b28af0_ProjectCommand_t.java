 /*******************************************************************************
  * Copyright (c) 2010 Oobium, Inc.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Jeremy Dowdall <jeremy@oobium.com> - initial API and implementation
  ******************************************************************************/
 package org.oobium.build.console.commands.export;
 
 import static org.oobium.utils.FileUtils.*;
 
 import java.io.File;
 
 import org.oobium.build.workspace.Application;
 import org.oobium.build.workspace.BlazeApp;
 import org.oobium.build.workspace.Project;
 import org.oobium.build.workspace.Workspace;
 import org.oobium.utils.FileUtils;
 
 public class ProjectCommand extends ApplicationCommand {
 
 	@Override
 	public void run() {
 		Project project = getProject();
 		if(project instanceof Application) {
 			super.run();
 		}
 		else if(project instanceof BlazeApp) {
 			if(!hasParam("turnkey")) {
 				console.err.println("location of the blaze turnkey zip not specified (usage: export turnkey:<path/to/zip>)");
 				return;
 			}
 			File turnkeyZip = new File(param("turnkey"));
 			if(!turnkeyZip.isFile()) {
 				console.err.println("blaze turnkey zip cannot be found");
 				return;
 			}
 
 			Workspace ws = getWorkspace();
 			BlazeApp blaze = (BlazeApp) project;
 			
 			try {
 				long start = System.currentTimeMillis();
 
 				ws.cleanExport();
 
 				File wd = new File(ws.getExportDir(), "blaze-server");
 	
 				FileUtils.extract(turnkeyZip, wd);
 				
 				File tomcat = new File(wd, "tomcat");
 				File installDir = new File(tomcat, "webapps");
 
 //				writeFile(tomcat, "conf/Catalina/localhost/" + blaze.name + ".xml",
 //					"<Context privileged='true' antiResourceLocking='false' antiJARLocking='false' reloadable='true'></Context>");
 
 				File appDir = new File(installDir, "ROOT");
 				deleteContents(appDir);
 
 				File metaInf = createFolder(appDir, "META-INF");
 				copy(blaze.manifest,  new File(metaInf, "MANIFEST.MF"));
 
 				File webInf = createFolder(appDir, "WEB-INF");
 				copy(blaze.bin, webInf);
 				copy(blaze.flex, webInf);
 				copy(blaze.lib, webInf);
 				copyContents(blaze.oobium, new File(webInf, "lib"));
 				copy(blaze.webXml, webInf);
 				
 				File client = createFolder(appDir, "client");
 				writeFile(client, "index.html", "<html><head><title>Hello World!</title></head><body><h1>Hello World!</h1></body></html>");
 				
				
				for(File file : findAll(wd)) {
					file.setExecutable(true);
				}
				
				
 				String msg = "exported <a href=\"open file " + wd + "\">" + blaze.name + "</a>";
 				if(flag('v')) {
 					console.out.println(msg + " in " + (System.currentTimeMillis() - start) + "ms");
 				} else {
 					console.out.println(msg);
 				}
 			} catch(Exception e) {
 				console.err.print(e);
 			} finally {
 				if(flag('v')) {
 					console.release();
 				}
 			}
 		}
 	}
 	
 }
