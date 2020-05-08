 /*
  * Project: xdccBee
  * Copyright (C) 2009 snert@snert-lab.de,
  *
  *  This program is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  *
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package de.snertlab.xdccBee.irc;
 
 import java.io.File;
 
 import org.jibble.pircbot.DccFileTransfer;
 
 import de.snertlab.xdccBee.ui.TableItemDownload;
 
 
 /**
  * @author holgi
  *
  */
 
 public class DccDownload {
 
 	private DccPacket dccPacket;
 	private DccFileTransfer dccFileTransfer;
 	private File destinationFile;
 	private TableItemDownload tableItemDownload;
 	private MyTableItemDownloadThread downloadThread;
 	
 	public DccDownload(DccPacket dccPacket, File destination){
 		this.dccPacket = dccPacket;
 		this.destinationFile = destination;
 	}
 
 	public String getKey() {
 		return dccPacket.toString();
 	}
 	
 	public void setDccFileTransfer(DccFileTransfer dccFileTransfer){
 		this.dccFileTransfer = dccFileTransfer;
 	}
 	
 	public DccFileTransfer getDccFileTransfer(){
 		return dccFileTransfer;
 	}
 	
 	public File getDestinationFile(){
 		return destinationFile;
 	}
 
 	public boolean matchDccFileTransfer(DccFileTransfer dccFileTransfer) {
 		//FIXME: Filename kann unterschiedlich Packet Name sein => einfach nick und download Status??
 		if( dccPacket.getSender().equals(dccFileTransfer.getNick()) ){
 			return true;
 		}
 		return false;
 	}
 
 	public DccPacket getDccPacket() {
 		return dccPacket;
 	}
 
 	public void setTableItemDownload(TableItemDownload tableItemDownload) {
 		this.tableItemDownload = tableItemDownload;
 	}
 	
 	public void start(){
 		downloadThread = new MyTableItemDownloadThread();
 		downloadThread.start();
 	}
 
 	/**
 	 * 
 	 */
 	public void stop() {
 		if(downloadThread==null){
 			tableItemDownload.setState(TableItemDownload.STATE_DOWNLOAD_ABORT);
 		}else{
 			downloadThread.stopMe();
 		}
 	}
 	
 	private class MyTableItemDownloadThread extends Thread {
 		
 		private boolean stop;
 		private String state;
 		
 		@Override
 		public void run() {
			while((int)dccFileTransfer.getProgress()<dccFileTransfer.getSize() && ! stop){
 				tableItemDownload.getDisplay().asyncExec( new Runnable() {
 					public void run() {
						if(stop) return;
 						tableItemDownload.updateFileTransferDisplay(dccFileTransfer);
 						tableItemDownload.setState(TableItemDownload.STATE_DOWNLOAD_DOWNLOAD);
 					}
 				});						
 				try {
 					Thread.sleep(1000);
 				} catch (InterruptedException e) {
 					e.printStackTrace();
 				}
 			}
 			state = TableItemDownload.STATE_DOWNLOAD_FINISHED;
 			if(stop){
 				state = TableItemDownload.STATE_DOWNLOAD_ABORT;
 			}
 			tableItemDownload.getDisplay().asyncExec( new Runnable() {
 				@Override
 				public void run() {
 					tableItemDownload.setState(state);
 					dccFileTransfer.close();
 				}
 			});
 		}
 		
 		public void stopMe(){
 			stop = true;
 		}
 	}
 
 }
