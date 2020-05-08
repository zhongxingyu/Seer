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
 import static org.oobium.persist.migrate.MigratorService.SYS_PROP_ACTION;
 import static org.oobium.utils.literal.Map;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Set;
 
 import org.oobium.build.console.commands.remote.migrate.PurgeCommand;
 import org.oobium.build.console.commands.remote.migrate.RedoCommand;
 import org.oobium.build.console.commands.remote.migrate.RollbackCommand;
 import org.oobium.build.console.commands.remote.migrate.ToCommand;
 import org.oobium.build.exceptions.OobiumException;
 import org.oobium.build.util.SSH;
 import org.oobium.build.workspace.Application;
 import org.oobium.build.workspace.Exporter;
 import org.oobium.build.workspace.Workspace;
 import org.oobium.console.ConsolePrintStream;
 import org.oobium.utils.Config.Mode;
 import org.oobium.utils.FileUtils;
 import org.oobium.utils.StringUtils;
 
 public class MigrateCommand extends RemoteCommand {
 
 	@Override
 	public void configure() {
 		applicationRequired = true;
 		maxParams = 2;
 		
 		add(new PurgeCommand());
 		add(new RedoCommand());
 		add(new RollbackCommand());
 		add(new ToCommand());
 	}
 
 	@Override
 	protected boolean canExecute() {
 		return hasApplication() && (
 				(paramCount() == 0) ||
 				((paramCount() == 2) && ("up".equals(param(1)) || "down".equals(param(1))))
 			);
 	}
 	
 	protected String getAction() {
 		if(paramCount() == 2) {
 			return "migrate/" + param(0) + "/" + param(1);
 		}
 		return "migrate";
 	}
 
 	private void deploy(Workspace ws, Application app, File exportDir) throws OobiumException, IOException {
 		Mode mode = hasParam("mode") ? Mode.parse(param("mode")) : Mode.PROD;
 		RemoteConfig config = getRemoteConfig(app, mode);
 		if(config == null) {
 			return;
 		}
 		
 		final SSH ssh = new SSH(config.host, config.username, config.password);
 		ssh.setOut(new ConsolePrintStream(console.out));
 		ssh.setErr(new ConsolePrintStream(console.err));
 		
 		String remote = app.name;
 		String data = "data";
 		if(config.dir != null) {
 			remote = config.dir + "/" + remote;
 			data = config.dir + "/" + data;
 		}
 
 		String migrator = remote + "_migrator";
 		String[] previous = getInstallations(ssh, remote);
 
 		String current = (previous == null || previous.length == 0) ? null : previous[0];
 		
 		// deploy migrator
 		if(flag('f')) {
 			console.out.println("force install requested - performing full upload");
 			ssh.copy(exportDir, migrator);
 		} else {
 			if(current == null) {
 				console.out.println("no app installation found - performing full upload");
 				ssh.copy(exportDir, migrator);
 			} else {
 				console.out.println("app installation found (" + current + ") - performing update");
 				update(ssh, exportDir, current, migrator);
 			}
 		}
 		
 		// stop application
 		if(current != null) {
 			ssh.setSudo(config.sudo);
 			ssh.exec("./stop.sh", current);
 			ssh.setSudo(false);
 		}
 		
 		// run migrator
 		ssh.exec("java -jar bin/felix.jar", migrator);
 		
 		// start application
 		if(current != null) {
 			ssh.setSudo(config.sudo);
 			ssh.exec("nohup ./start.sh", current);
 			ssh.setSudo(false);
 		}
 		
 		// output info
 		ssh.exec("ps aux | grep felix");
 		ssh.setSudo(config.sudo);
 		ssh.exec("cat nohup.out", migrator);
 		ssh.setSudo(false);
 
 		// remove migrator and migrator database
 		ssh.exec("rm -r " + migrator);
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
 
			Exporter exporter = new Exporter(ws, app, true);
 			exporter.setMode(mode);
 			exporter.setClean(true);
 			exporter.setProperties(Map(SYS_PROP_ACTION, getAction()));
 			File exportDir = exporter.export();
 			
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
 		
 		ssh.exec("cp -p " + StringUtils.join(localCopy, ' ') + " " + current + "/bundles/");
 		
 		for(Entry<File, String> entry : remoteCopy.entrySet()) {
 			ssh.copy(entry.getKey(), entry.getValue());
 		}
 	}
 	
 }
