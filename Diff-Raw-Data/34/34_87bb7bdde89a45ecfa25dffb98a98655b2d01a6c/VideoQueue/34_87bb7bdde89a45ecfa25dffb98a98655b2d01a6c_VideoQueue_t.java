 package com.iped.ipcam.utils;
 
 import java.util.LinkedList;
 import java.util.Queue;
 
 import android.util.Log;
 
 import com.iped.ipcam.pojo.JpegImage;
 import com.iped.ipcam.pojo.MpegImage;
 
 public class VideoQueue {
 
 	private LinkedList<JpegImage> jpegImageList = null;
 	
 	private LinkedList<String> timeList = null;
 	
 	private LinkedList<MpegImage> mpegImageList = null;
 	
 	private Object lock = new Object();
 	
 	public VideoQueue() {
 		jpegImageList = new LinkedList<JpegImage>();
 		timeList = new LinkedList<String>();
 		mpegImageList = new LinkedList<MpegImage>();
 	}
 	
 	public void addJpegImage(JpegImage image) {
 		if(jpegImageList.size()>=4) {
 			JpegImage i = jpegImageList.poll();
 			if(i.bitmap != null) {
 				i.bitmap.recycle();
 				i = null;
 			}
 		}
 		jpegImageList.add(image);
 	}
 	
 	
 	public void addNewTime(String time) {
 		synchronized (lock) {
 			timeList.offer(time);
 		}
 	}
 	
 	public int getTimeListLength() {
 		return timeList.size();
 	}
 	
 	public int getImageListLength() {
 		return jpegImageList.size();
 	}
 	
 	public JpegImage removeImage() {
 		if(getImageListLength()>0) {
			synchronized (lock) {
				return jpegImageList.poll();
			}
 		}else {
 			return null;
 		}
 	}
 	
 	public String pollTime() {
 		try{
 			synchronized (lock) {
 				return timeList.poll();
 			}
 		}catch(Exception e) {
 			Log.d("VideoQueue", "### 111 " + e.getMessage());
 			return null;
 		}
 	}
 	
 	public JpegImage getFirstImage() {
 		if(getImageListLength()>0) {
 			try{
 				synchronized (lock) {
 					return jpegImageList.peek();
 				}
 			}catch(Exception e) {
 				Log.d("VideoQueue", "### 2222 " + e.getLocalizedMessage());
 				return null;
 			}
 		}else {
 			return null;
 		}
 	}
 	
 	public String getFirstTime() {
 		synchronized (lock) {
 			if(getTimeListLength()>0) {
 				try{
 					return timeList.peek();
 				}catch(Exception e) {
 					Log.d("VideoQueue", "### 3333 " + e.getLocalizedMessage());
 					return null;
 				}
 			}
 		}
 		return null;
 	}
 	
 	public void clear() {
 		synchronized (lock) {
 			jpegImageList.clear();
 		}
 	}
 	
 	public void clearTime() {
 		synchronized (lock) {
 			timeList.clear();
 		}
 	}
 	
 	/** store mpeg4 **/
 	public void addMpegImage(MpegImage mpegImage) {
 		synchronized (lock) {
 			if(mpegImageList.size()>=4) {
 				try{
 					MpegImage i = mpegImageList.poll();
 					i.rgb = null;
 				} catch(Exception e) {
 					Log.d("VideoQueue", "### 4444 " + e.getLocalizedMessage());
 				}
 			}
 			mpegImageList.offer(mpegImage);
 		}
 	}
 	
 	public MpegImage getMpegImage() {
 		synchronized (lock) {
 			if(getMpegLength()>0) {
 				try{
 					return mpegImageList.poll();//.peek();
 				}catch(Exception e) {
 					Log.d("VideoQueue", "### 5555 " + e.getLocalizedMessage());
 				}
 			}
 		}
 		return null;
 	}
 	
 	public void removeMpegImage() {
 		synchronized (lock) {
 			if(getMpegLength()>0) {
 				try{
 					mpegImageList.remove();
 				}catch(Exception e) {
 					Log.d("VideoQueue", "### 6666 " + e.getLocalizedMessage());
 				}
 			}
 		}
 	}
 	
 	public int getMpegLength() {
 		return  mpegImageList.size();
 	}
 }
