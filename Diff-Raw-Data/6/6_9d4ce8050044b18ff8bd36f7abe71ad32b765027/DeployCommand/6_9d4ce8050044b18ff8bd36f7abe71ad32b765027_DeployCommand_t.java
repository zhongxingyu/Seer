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
 package org.oobium.build.console.commands.remote;
 
 import static org.oobium.build.console.commands.RemoteCommand.getInstallations;
 import static org.oobium.utils.FileUtils.getLastModified;
 import static org.oobium.utils.coercion.TypeCoercer.coerce;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Set;
 
 import org.oobium.build.exceptions.OobiumException;
 import org.oobium.build.util.SSH;
 import org.oobium.build.util.SSHEvent;
 import org.oobium.build.util.SSHListener;
 import org.oobium.build.workspace.Application;
 import org.oobium.build.workspace.Workspace;
 import org.oobium.console.ConsolePrintStream;
 import org.oobium.utils.Config.Mode;
 import org.oobium.utils.FileUtils;
 import org.oobium.utils.StringUtils;
 
 public class DeployCommand extends RemoteCommand {
 
 	private static final int KEEP = 3;
 	
 	@Override
 	public void configure() {
 		applicationRequired = true;
 	}
 
 	private void deploy(Workspace ws, Application app, final File exportDir) throws OobiumException, IOException {
 		RemoteConfig config = getRemoteConfig(app);
 		if(config == null) {
 			return;
 		}
 		
 		final SSH ssh = new SSH(config.host, config.username, config.password);
 		try {
 //			ssh.setOut(new ConsolePrintStream(console.out));
 //			ssh.setErr(new ConsolePrintStream(console.err));
 			
 			String name = app.name;
 			String version = app.version.resolve(getLastModified(new File(exportDir, "bundles"))).toString();
 	
 			String remote = name;
 			if(config.dir != null) {
 				remote = config.dir + "/" + remote;
 			}
 	
 			String current = remote + "_" + version;
 			
 			console.out.println("Searching for previous installations...");
 			String[] previous = getInstallations(ssh, remote);
 			
 			if(flag('f')) {
 				console.out.println("  force install requested - will perform full upload.");
 				ssh.copy(exportDir, current);
 			} else {
 				if(previous == null) {
 					console.out.println("  no previous installation found - will perform full upload.");
 					console.out.println("Copying files to " + current + "...");
 					SSHListener listener = new SSHListener() {
 						@Override
 						public void handleEvent(SSHEvent event) {
 							switch(event.type) {
 							case ScpStart:
 								File file = (File) ((Object[]) event.data)[0];
 								String name = file.getAbsolutePath().substring(exportDir.getAbsolutePath().length());
 								console.out.print("  copying " + name + "...");
 								break;
 							case ScpEnd:
 								console.out.println(" done.");
 								break;
 							}
 						}
 					};
 					ssh.addListener(listener);
 					ssh.copy(exportDir, current);
 					ssh.removeListener(listener);
 				} else if(previous[0].equals(current)) {
 					console.out.println("  remote installation is already up to date - exiting.");
 					return;
 				} else {
 					console.out.println("  previous installation found (" + previous[0] + ") - will perform update.");
 					update(ssh, exportDir, previous[0], current);
 				}
 			}
 			
 			ssh.exec("chmod +x " + current + "/*.sh");
 			
 			ssh.setSudo(config.sudo);
 			if(previous != null) {
 				console.out.print("Stopping...");
 				ssh.exec("./stop.sh", current);
 				console.out.println(" done.");
 			}
 			console.out.print("Starting...");
 			ssh.exec("nohup ./start.sh", current);
 			console.out.println(" done.");
 			ssh.setSudo(false);
 	
 			if(flag('v')) {
 				ssh.exec("ps aux | grep felix");
 				
 				ssh.setSudo(config.sudo);
 				ssh.exec("cat nohup.out", current);
				ssh.setSudo(false);
 			}
 			
			ssh.setSudo(config.sudo);
 			finish(ssh, previous);
 		} finally {
 			ssh.disconnect();
 		}
 	}
 	
 	protected void finish(SSH ssh, String[] previous) throws IOException, OobiumException {
 		if(previous != null) {
 			if("all".equals(param("keep"))) {
 				console.out.println("Keeping all previous installations.");
 			} else {
 				int i = coerce(param("keep"), KEEP);
 				if(i >= previous.length) {
 					console.out.println("Keeping " + i + " previous installations; there are no older installations to remove.");
 				} else {
					console.out.println("Keeping " + i + " previous installations, removing older installations...");
 					for( ; i < previous.length; i++) {
 						console.out.print("  removing " + previous[i] + "...");
 						ssh.exec("rm -r " + previous[i]);
 						console.out.println(" done.");
 					}
 				}
 			}
 		}
 		console.out.println("Deployment Complete.");
 	}
 
 	@Override
 	public void run() {
 		Workspace ws = getWorkspace();
 		Application app = getApplication();
 
 		Mode mode = hasParam("mode") ? Mode.parse(param("mode")) : Mode.PROD;
 
 		if(flag('v')) {
 			console.out.println("deploying in " + mode + " mode");
 			console.capture();
 		}
 		
 		try {
 			long start = System.currentTimeMillis();
 
 			ws.cleanExport(app);
 			File exportDir = ws.export(app, mode);
 			
 			String msg = "exported <a href=\"open file " + exportDir + "\">" + app.name() + "</a>";
 			if(flag('v')) {
 				console.out.println(msg + " in " + (System.currentTimeMillis() - start) + "ms");
 			} else {
 				console.out.println(msg);
 			}
 
 			deploy(ws, app, exportDir);
 		} catch(Exception e) {
 			console.err.print(e);
 		} finally {
 			if(flag('v')) {
 				console.release();
 			}
 		}
 	}
 	
 	private void update(SSH ssh, File exportDir, String previous, String current) throws IOException, OobiumException {
 		Set<String> created = new HashSet<String>();
 		ssh.exec("mkdir -p " + current);
 		created.add(current);
 
 		// copy over felix bin folder and file
 		ssh.exec("cp -rp " + previous + "/bin " + current);
 
 		// find all bundles in previous installation
 		Set<String> inPrevious = new HashSet<String>();
 		String[] sa = ssh.exec("ls -d " + previous + "/bundles/*").split("\\s+");
 		for(String s : sa) {
 			inPrevious.add(s.substring(s.lastIndexOf('/') + 1));
 		}
 
 		List<String> localCopy = new ArrayList<String>();
 		Map<File, String> remoteCopy = new LinkedHashMap<File, String>();
 		
 		int len = exportDir.getAbsolutePath().length();
 		File[] files = FileUtils.findAll(exportDir);
 		for(File file : files) {
 			String name = file.getName();
 			if(!name.equals("felix.jar")) {
 				// create directory if necessary
 				String dpath = (current + file.getParent().substring(len)).replace('\\', '/');
 				if(!created.contains(dpath)) {
 					ssh.exec("mkdir -p " + dpath);
 					created.add(dpath);
 				}
 				// copy file, either from local export directory or previous installation
 				if(inPrevious.contains(name)) {
 					localCopy.add(previous + "/bundles/" + name);
 //					ssh.exec("cp -p " + previous + "/bundles/" + name + " " + current + "/bundles/");
 				} else {
 					String fpath = (current + file.getPath().substring(len)).replace('\\', '/');
 					remoteCopy.put(file, fpath);
 //					ssh.copy(file, fpath);
 				}
 			}
 		}
 		
 		console.out.print("Copying remote files to " + current + "...");
 		ssh.exec("cp -p " + StringUtils.join(localCopy, ' ') + " " + current + "/bundles/");
 		console.out.println(" done.");
 		
 		console.out.println("Copying local files to " + current + "...");
 		for(Entry<File, String> entry : remoteCopy.entrySet()) {
 			File file = entry.getKey();
 			String name = file.getAbsolutePath().substring(exportDir.getAbsolutePath().length());
 			console.out.print("  copying " + name + "...");
 			ssh.copy(entry.getKey(), entry.getValue());
 			console.out.println(" done.");
 		}
 	}
 	
 }
