 package com.funnyplayer.net.api.geci.bean;
 
 import java.util.List;
 
import android.text.TextUtils;

 public class LrcBean {
 	int count;
 	int code;
 	List<LrcUrl> urls;
 
 	public int getCount() {
 		return count;
 	}
 
 	public void setCount(int count) {
 		this.count = count;
 	}
 
 	public int getCode() {
 		return code;
 	}
 
 	public void setCode(int code) {
 		this.code = code;
 	}
 
 	public List<LrcUrl> getResult() {
 		return urls;
 	}
 
 	public void setResult(List<LrcUrl> urls) {
 		this.urls = urls;
 	}
 
 	public static class LrcUrl {
 		private String lrc;
 		private String song;
 		private String artist;
 		private int sid;
 		private int aid;
 
 		public int getAid() {
 			return aid;
 		}
 
 		public void setAid(int aid) {
 			this.aid = aid;
 		}
 
 		public String getLrc() {
 			return lrc;
 		}
 
 		public void setLrc(String lrc) {
 			this.lrc = lrc;
 		}
 
 		public String getSong() {
 			return song;
 		}
 
 		public void setSong(String song) {
 			this.song = song;
 		}
 
 		public String getArtist() {
 			return artist;
 		}
 
 		public void setArtist(String artist) {
 			this.artist = artist;
 		}
 
 		public int getSid() {
 			return sid;
 		}
 
 		public void setSid(int sid) {
 			this.sid = sid;
 		}
		
		public String toString() {
			if (!TextUtils.isEmpty(artist) && ! TextUtils.isEmpty(song)) {
				return artist.toLowerCase() + "_" + song.toLowerCase();
			} 
			return song.toLowerCase();
		}
 	}
 
 }
