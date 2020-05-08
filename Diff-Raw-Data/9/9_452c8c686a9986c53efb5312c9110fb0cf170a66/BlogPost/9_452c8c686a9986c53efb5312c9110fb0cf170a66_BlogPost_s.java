 package org.denevell.rocklobster.blogposts;
 import java.util.HashMap;
 
 public class BlogPost {
 
 	
 		private HashMap<String, String> metadata = new HashMap<String, String>();
 		private String post = "";
 		private String filename;
 
 		public BlogPost() {
 		}
 
 		public void addMetadata(String key, String value) {
 			metadata.put(key, value);
 		}
 		
 		public HashMap<String, String> getMetadata() {
 			return metadata;
 		}
 		
 		public void setPost(String post) {
 			this.post = post;
 		}
 		
 		@Override
 		public String toString() {
 			return "metadata: " + metadata + "\ncontent: " + post;
 		}
 
 		public String getPost() {
 			return this.post;
 		}
 
 		public void setFilename(String name) {
 			this.filename = name;
 		}
 		
 		public String getFilename() {
 			return filename;
 		}
 		
 		/**
 		 * If we have hello.txt, it will give us hello.
 		 * @return
 		 */
 		public String getBasefilename() {
 			String[] strs = new StringBuffer(getFilename()).reverse().toString().split("\\.", 2);
 			return new StringBuffer(strs[1]).reverse().toString();
 		}
 
 }
