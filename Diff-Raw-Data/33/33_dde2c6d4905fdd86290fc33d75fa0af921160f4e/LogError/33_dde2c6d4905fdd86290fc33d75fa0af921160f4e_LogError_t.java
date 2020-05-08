 /**
 Copyright 2008, 2009 Mark Hooijkaas
 
 This file is part of the RelayConnector framework.
 
 The RelayConnector framework is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.
 
 The RelayConnector framework is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.
 
 You should have received a copy of the GNU General Public License
 along with the RelayConnector framework.  If not, see <http://www.gnu.org/licenses/>.
 */
 
 package org.kisst.gft.action;
 
 import java.util.HashMap;
 
 import org.kisst.cfg4j.Props;
 import org.kisst.gft.GftContainer;
 import org.kisst.gft.filetransfer.FileTransferData;
 import org.kisst.gft.task.Task;
 
 
 public class LogError  extends LogCompleted {
 	public LogError(GftContainer gft, Props props) {
 		super(gft, props);
 	}
 	
 	@Override protected void fillContext(HashMap<String,Object> context, Task task, FileTransferData ftdata) {
 		context.put("details", "Fout bij GFT filetransfer, kanaal: "+ftdata.channel.name+", van: "+ftdata.channel.src+"/"+ftdata.srcpath+" naar: "+ftdata.channel.dest+"/"+ftdata.destpath+" fout:"+task.getLastError().getMessage());
 		context.put("niveau", "error");
 		context.put("event", "error");
		context.put("tech", task.getLastAction());
 	}
 }
