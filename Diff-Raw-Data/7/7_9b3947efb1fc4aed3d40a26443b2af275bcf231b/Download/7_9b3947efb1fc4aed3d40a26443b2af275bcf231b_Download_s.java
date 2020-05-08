 /*******************************************************************************
  * Copyright (c) 2013 Juuso Vilmunen.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the GNU Public License v3.0
  * which accompanies this distribution, and is available at
  * http://www.gnu.org/licenses/gpl.html
  * 
  * Contributors:
  *     Juuso Vilmunen - initial API and implementation
  ******************************************************************************/
 package waazdoh.cp2p.impl;
 
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 
 import waazdoh.client.Binary;
 import waazdoh.client.MBinaryID;
 import waazdoh.client.MStringID;
 import waazdoh.client.WaazdohInfo;
 import waazdoh.cutils.MLogger;
 import waazdoh.cutils.MTimedFlag;
 import waazdoh.cutils.xml.JBean;
 
 public final class Download implements Runnable, MessageResponseListener,
 		SourceListener {
 	private Binary bin;
 	private MNodeConnection source;
 	private MLogger log;
 	private MTimedFlag flag;
 	private MTimedFlag giveupflag = new MTimedFlag(1000 * 60 * 4);
 	private long endtime;
 	private long starttime;
 	private Map<Integer, NeededStart> sentstarts = new HashMap<Integer, Download.NeededStart>();
 	private boolean hasBeenReady;
 	private String speedinfo;
 	private int nullcount;
 	private int countbytes;
 	private int overwritebytes;
 
 	Download(Binary b, MNodeConnection source) {
 		this.bin = b;
 		this.source = source;
 		//
 		source.addSourceListener(this);
 		//
 		Thread t = new Thread(this, "Download");
 		t.start();
 
 		log = MLogger.getLogger(this);
 		log.info("created");
 	}
 
 	public String getMemoryUsageInfo() {
 		String info = "";
 		info += "sentstarts:[" + sentstarts.size() + "]";
 		return info;
 	}
 
 	public void stop() {
 		log.info("stopping download " + bin);
 		giveupflag.trigger();
 	}
 
 	@Override
 	public void run() {
 		this.starttime = System.currentTimeMillis();
 		flag = new MTimedFlag(10000);
 		while (!isReady() && source.isRunning() && !giveupflag.isTriggered()) {
 			flag.reset();
 			resetSentStarts();
 			sendWhoHasMessage();
 			//
 			flag.waitTimer();
 		}
 		//
 		source.removeDownload(getID());
 		updateSpeedInfo();
 		log.info("Download DONE! " + speedinfo + " source:"
 				+ source.isRunning() + " ready:" + isReady() + " giveup: "
 				+ giveupflag);
 		//
 		this.source.reportDownload(getID(), isReady());
 	}
 
 	@Override
 	public void nodeAdded(Node n) {
 		sendWhoHasMessage(n);
 	}
 
 	@Override
 	public boolean isDone() {
 		return giveupflag.isTriggered() || isReady();
 	}
 
 	public void updateSpeedInfo() {
 		this.endtime = System.currentTimeMillis();
 		this.speedinfo = "Has downloaded " + countbytes + " bytes in "
 				+ (endtime - starttime) + " msecs" + "("
 				+ (1000.0f * bin.length() / (endtime - starttime)) + " f/sec)";
 	}
 
 	public synchronized boolean isReady() {
 		if (!hasBeenReady) {
 			hasBeenReady = bin.isReady();
 		}
 		return hasBeenReady;
 	}
 
 	private void resetSentStarts() {
 		sentstarts = new HashMap<Integer, Download.NeededStart>();
 	}
 
 	private void sendWhoHasMessage(Node n) {
 		if (!isReady()) {
 			if (n != null) {
 				MMessage whoHasMessage = getWhoHasMessage();
 				log.info("whohas to node[" + n + "] " + whoHasMessage);
 				n.addMessage(whoHasMessage, this);
 			} else {
 				sendWhoHasMessage();
 			}
 		} else {
 			flag.trigger();
 		}
 	}
 
 	@Override
 	public void messageReceived(Node n, MMessage b) {
 		handleResponse(n, b);
 	}
 
 	private void sendWhoHasMessage() {
 		if (!isReady()) {
 			MMessage m = getWhoHasMessage();
 			log.info("broadcasting whohas " + m);
 			if (m != null) {
 				source.broadcastMessage(m, this);
 			} else if (!isReady() && sentstarts.size() == 0) {
 				bin.resetCRC();
 				log.info("resetCRC " + isReady());
 				// reset();
 			}
 		} else {
 			log.info("already ok " + bin);
 			flag.trigger();
 		}
 	}
 
 	private synchronized void reset() {
 		bin.clear();
 	}
 
 	public MMessage getWhoHasMessage() {
 		synchronized (sentstarts) {
 			MMessage m = source.getMessage("whohas");
 			m.addAttribute("streamid", "" + getID());
 			JBean needed = m.add("needed");
 			if (addNeededPieces(needed) && !isReady()) {
 				return m;
 			} else {
 				return null;
 			}
 		}
 	}
 
 	private void handleResponse(Node n, MMessage b) {
 		if (b.getName().equals("stream") && !isReady()) {
 			MStringID sid = b.getIDAttribute("streamid");
 			if (sid != null && getID().equals(sid)) {
 				log.info("response " + b);
 
 				flag.reset();
 				giveupflag.reset();
 				//
 				byte responsebytes[] = b.getAttachment("bytes");
 				if (responsebytes != null) {
 					log.info("Download got floats " + responsebytes.length);
 					this.countbytes += responsebytes.length;
 					int start = b.getAttributeInt("start");
 					// int end = b.getAttributeInt("end");
 					//
 					overwritebytes += bin.addAt(start, responsebytes);
 					//
 					synchronized (sentstarts) {
 						for (Integer i : new HashSet<Integer>(
 								sentstarts.keySet())) {
 							NeededStart s = sentstarts.get(i);
 							if (start >= s.start && start <= s.end) {
 								sentstarts.remove(i);
 							}
 						}
 					}
 					updateSpeedInfo();
 				} else {
 					log.info("response bytes null");
 				}
 				//
				MNodeID throughtid = new MNodeID(b.getAttribute("through"));
				if (throughtid != null) {
					sendWhoHasMessage(source.getNode(throughtid));
 				} else if (n != null) {
 					sendWhoHasMessage(n);
 				} else {
 					log.info("not sending back a message... broadcasting later");
 				}
 				//
 				log.debug("stream response handled");
 			} else {
 				log.error("StreamID " + sid + " while waiting " + bin);
 			}
 		} else {
 			log.info("unknown message " + b);
 		}
 	}
 
 	private String toHexString(byte[] responsebytes) {
 		StringBuffer sb = new StringBuffer();
 		for (byte b : responsebytes) {
 			sb.append(Integer.toHexString((int) b));
 		}
 		return sb.toString();
 	}
 
 	private MBinaryID getID() {
 		return bin.getID();
 	}
 
 	private synchronized boolean addNeededPieces(JBean needed) {
 		while (bin.get(this.nullcount) != null) {
 			this.nullcount++;
 		}
 		int start = nullcount;
 		if (start >= bin.length()) {
 			// done
 			return false;
 		} else {
 			int getlength = bin.length() - start;
 			if (getlength > WaazdohInfo.DOWNLOADER_MAX_REQUESTED_PIECELENGTH) {
 				getlength = WaazdohInfo.DOWNLOADER_MAX_REQUESTED_PIECELENGTH;
 			}
 			//
 			Byte[] bytes = bin.getByteBuffer();
 			int bytesLength = bin.getBytesLength();
 			//
 			while (bytes[start] != null || getStartNeedSent(start) != null) {
 				NeededStart s = getStartNeedSent(start);
 				if (s != null) {
 					start = s.end;
 				}
 				start++;
 				if (start >= bytesLength) {
 					break;
 				}
 			}
 			//
 			int end = start;
 			while (end < (bin.length() - 1)
 					&& (end >= bytesLength || bytes[end] == null)) {
 				end++;
 				if (end > bytesLength) {
 					end = bin.length() - 1;
 				}
 			}
 			log.info("start:" + start + " end:" + end + " with " + sentstarts
 					+ " ow:" + (100.0f * overwritebytes / countbytes));
 			if (end > start || (end < bytesLength && bytes[end] == null)) {
 				NeededStart s = new NeededStart(start, end);
 				sentstarts.put(start, s);
 				//
 				JBean p = needed.add("piece");
 				p.addValue("start", start);
 				p.addValue("end", end);
 				log.info("piece needed " + p);
 				return true;
 			} else {
 				return false;
 			}
 		}
 	}
 
 	private NeededStart getStartNeedSent(int start) {
 		NeededStart needed = sentstarts.get(start);
 		return needed;
 	}
 
 	@Override
 	public String toString() {
 		return "Download[" + bin + "][" + speedinfo + "]";
 	}
 
 	private class NeededStart {
 		private int start;
 		private int end;
 
 		public NeededStart(int start, int end) {
 			this.start = start;
 			this.end = end;
 		}
 
 		@Override
 		public String toString() {
 			return "needed[" + start + " -> " + end + "]";
 		}
 	}
 
 }
