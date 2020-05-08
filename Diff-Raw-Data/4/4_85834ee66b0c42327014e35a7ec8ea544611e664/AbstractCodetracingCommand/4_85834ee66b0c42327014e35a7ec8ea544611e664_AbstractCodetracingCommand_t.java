 /*******************************************************************************
  * Copyright (c) Feb 19, 2012 Zend Technologies Ltd. 
  * All rights reserved. This program and the accompanying materials 
  * are made available under the terms of the Eclipse Public License v1.0 
  * which accompanies this distribution, and is available at 
  * http://www.eclipse.org/legal/epl-v10.html  
  *******************************************************************************/
 package org.zend.sdkcli.internal.commands;
 
 import java.text.SimpleDateFormat;
import java.util.Date;
 
 import org.zend.sdkcli.internal.options.Option;
 import org.zend.sdklib.application.ZendCodeTracing;
 import org.zend.sdklib.internal.target.UserBasedTargetLoader;
 
 public abstract class AbstractCodetracingCommand extends AbstractCommand {
 
 	private static final String TARGET = "t";
 
 	@Option(opt = TARGET, required = true, description = "Target ID", argName = "target id")
 	public String getTarget() {
 		return getValue(TARGET);
 	}
 
 	protected ZendCodeTracing getCodeTracing() {
 		String targetId = getTarget();
 		return new ZendCodeTracing(targetId, new UserBasedTargetLoader());
 	}
 
 	protected String getDate(long date) {
 		SimpleDateFormat formatter = new SimpleDateFormat(
 				"dd MMM yyyy HH:mm:ss Z");
		return formatter.format(new Date(date * 1000L));
 	}
 
 }
