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
 package org.oobium.build.console.commands.migrate;
 
 import org.oobium.build.console.commands.MigrateCommand;
 
 public class PurgeCommand extends MigrateCommand {
 
 	@Override
 	public void configure() {
 		applicationRequired = true;
 	}
 	
 	@Override
 	protected String getPath() {
 		return "/migrate/purge";
 	}
 
 	@Override
 	public void run() {
 		String r = flag('f') ? "Y" : ask("All data will be lost!!! Continue anyway? [Y/N] ");
 		if(!"Y".equalsIgnoreCase(r)) {
 			console.out.println("operation cancelled");
		} else {
			super.run();
 		}
 	}
 	
 }
