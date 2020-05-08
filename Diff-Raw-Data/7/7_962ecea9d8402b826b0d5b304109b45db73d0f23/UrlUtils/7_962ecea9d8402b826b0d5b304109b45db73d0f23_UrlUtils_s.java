 /*
  * Copyright (c) 2013 goodplayer
  * 
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not
  * use this file except in compliance with the License. You may obtain a copy of
  * the License at
  * 
  * http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
  * License for the specific language governing permissions and limitations under
  * the License.
  */
 package net.moetang.turismo_plus.util;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.regex.Pattern;
 
 public class UrlUtils {
 	public List<PathEntry> uriToPathEntry(String uri){
 		List<PathEntry> entrylist = new ArrayList<>();
 		try {
 			if(!uri.startsWith("/"))
 				return entrylist;
 			//sdflj/sdfs     is two
 			//     /sdfsf    is two
 			//     /sdfdsf/  is two
 			String[] paths = uri.split("/");
 			if(paths.length == 0){
 				entrylist.add(new RootEntry());
 			}else{
 				for(int i = 1; i < paths.length; i++){
 					entrylist.add(analyseEntryType(paths[i]));
 				}
 			}
 		} catch (Exception e) {
 		}
 		return entrylist;
 	}
 	public List<PathEntry> uriToRequestOriginalPathEntry(String uri){
 		List<PathEntry> entrylist = new ArrayList<>();
 		try {
 			//sdflj/sdfs     is two
 			//     /sdfsf    is two
 			//     /sdfdsf/  is two
 			String[] paths = uri.split("/");
 			if(paths.length == 0){
 				entrylist.add(new RootEntry());
 			}else{
 				for(int i = 1; i < paths.length; i++){
 					entrylist.add(new NormalEntry(paths[i]));
 				}
 			}
 		} catch (Exception e) {
 		}
 		return entrylist;
 	}
 	
 	protected static enum PathEntryType{
 		NORMAL, VAR, WILDCARD, REGEX, REGEX_VAR, ROOT, REQUEST_ENTRY
 	}
 	
 	/**
 	 * @author goodplayer
 	 */
 	public static abstract class PathEntry implements Comparable<PathEntry>{
 		protected PathEntryType pathEntryType;
 		protected String pathName;
 		protected void setPathName(String pathName) {
 			this.pathName = pathName;
 		}
 		protected void setPathEntryType(PathEntryType pathEntryType) {
 			this.pathEntryType = pathEntryType;
 		}
 		/**
 		 * test request uri if matches<br />
 		 * Note: Please use these entries to test the request uri entries<br />
 		 * 测试请求uri是否匹配<br />
 		 * 注意：请用规则entry匹配请求uri的entry<br/>
 		 * 
 		 */
 		public abstract boolean match(PathEntry requestUri);
 		/**
 		 * the order of two mapper entry when test request uri<br />
 		 * 当测试请求uri时候的两个entry的顺序<br />
 		 * <br/>
 		 * Root -> Normal -> Var -> RegEx -> Wildcard<br/>
 		 * <br />
 		 * @see java.lang.Comparable#compareTo(java.lang.Object)
 		 */
 		@Override
 		public abstract int compareTo(PathEntry o);
 		/**
 		 * test if two mapper entry are equal, used in action mapper search tree construction<br />
 		 * 在生成匹配树的时候，比较两个entry是否相等
 		 * 
 		 */
 		@Override
 		public boolean equals(Object obj){
 			if(obj == null || !(obj instanceof PathEntry))
 				return false;
 			PathEntry out = (PathEntry) obj;
 			return out.pathEntryType.equals(pathEntryType)&&out.pathName.equals(pathName);
 		}
 		//resolver of filling the var defined in pathEntry
 		public abstract EntryResolver getPathEntryResolver();
 		
 		protected static EntryResolver emptyResolver = new EntryResolver() {
 			@Override
 			public void resolve(PathEntry entryToResolve) {
 			}
 		};
 		
 	}
 	protected static final class RootEntry extends PathEntry{
 		public RootEntry() {
 			this.pathEntryType = PathEntryType.ROOT;
 			this.pathName = "/";
 		}
 		@Override
 		public int compareTo(PathEntry o) {
 			if(o == null){
 				return 1;
 			}else if(o instanceof RootEntry){
 				return 0;
 			}else{
 				return -1;
 			}
 		}
 		@Override
 		public boolean match(PathEntry obj) {
 			return (obj!=null)?"/".equals(obj.pathName):false;
 		}
 		@Override
 		public EntryResolver getPathEntryResolver() {
 			return emptyResolver;
 		}
 	}
 	protected static final class NormalEntry extends PathEntry{
 		public NormalEntry(String pathName) {
 			this.pathEntryType = PathEntryType.NORMAL;
 			this.pathName = pathName;
 		}
 		@Override
 		public boolean match(PathEntry requestUri) {
 			return (requestUri!=null)?this.pathName.equals(requestUri.pathName):false;
 		}
 		@Override
 		public int compareTo(PathEntry o) {
 			if(o == null || o instanceof RootEntry){
 				return 1;
 			}else if((o instanceof NormalEntry)){
 				return 0;
 			}else{
 				return -1;
 			}
 		}
 		@Override
 		public EntryResolver getPathEntryResolver() {
 			return emptyResolver;
 		}
 	}
 	protected static final class VarEntry extends PathEntry{
 		private String[] splits;
 		private String[] patterns;
 		public VarEntry(String pathName) {
 			this.pathEntryType = PathEntryType.VAR;
 			this.pathName = pathName;
 			this.patterns = this.splitPatternUri(pathName);
 		}
 		@Override
 		public boolean match(PathEntry requestUri) {
 			String prestr = patterns[0];
 			if(prestr.length() != 0 && (!requestUri.pathName.startsWith(prestr))){
 				return false;
 			}
 			String poststr = patterns[2];
 			if(poststr.length() != 0 && (!requestUri.pathName.endsWith(poststr))){
 				return false;
 			}
 			if(prestr.length() == requestUri.pathName.indexOf(poststr)){
 				return false;
 			}
 			return true;
 		}
 		@Override
 		public int compareTo(PathEntry o) {
 			if(o == null || o instanceof NormalEntry || o instanceof RootEntry){
 				return 1;
 			}else if((o instanceof VarEntry)){
 				return 0;
 			}else{
 				return -1;
 			}
 		}
 		private String[] splitPatternUri(String uri){
 			if(this.splits == null){
 				String[] result = new String[3];
 				int firstpos = this.pathName.indexOf("#");
 				result[0] = this.pathName.substring(0, firstpos);
 				int secpos = this.pathName.indexOf("#", firstpos+1);
 				if(secpos == -1){
 					result[1] = this.pathName.substring(firstpos+1);
 					result[2] = "";
 				}else{
 					result[1] = this.pathName.substring(firstpos+1, secpos);
 					result[2] = this.pathName.substring(secpos+1);
 				}
 				this.splits = result;
 			}
 			return this.splits;
 		}
 		@Override
 		public EntryResolver getPathEntryResolver() {
 			return new EntryResolver() {
 				@Override
 				public void resolve(PathEntry entryToResolve) {
 					String[] patterns = splitPatternUri(pathName);
 					String path = entryToResolve.pathName;
 					String prestr = patterns[0];
 					String poststr = patterns[2];
 					String key = patterns[1];
 					String value = path.substring(prestr.length(), path.lastIndexOf(poststr));
 					Env._setParam(key, value);
 				}
 			};
 		}
 	}
 	protected static final class WildCardEntry extends PathEntry{
 		private String[] parts;
 		public WildCardEntry(String pathName) {
 			this.pathEntryType = PathEntryType.WILDCARD;
 			this.pathName = pathName;
 			this.parts = this.pathName.split("\\*");
 		}
 		@Override
 		public boolean match(PathEntry requestUri) {
 			if(requestUri == null)
 				return false;
 			String uri = requestUri.pathName;
 			int i ;
 			for(String part : parts){
 				if(part.length() == 0)
 					continue;
 				if((i = uri.indexOf(part))!=-1){
 					uri = uri.substring(i+part.length());
 				}else{
 					return false;
 				}
 			}
 			return true;
 		}
 		@Override
 		public int compareTo(PathEntry o) {
 			if(o == null || o instanceof RootEntry || o instanceof NormalEntry || o instanceof VarEntry || o instanceof RegExEntry){
 				return 1;
 			}else if((o instanceof WildCardEntry)){
 				return 0;
 			}else{
 				return -1;
 			}
 		}
 		@Override
 		public EntryResolver getPathEntryResolver() {
 			return emptyResolver;
 		}
 	}
 	protected static final class RegExEntry extends PathEntry{
 		private Pattern p;
 		private String[] patterns;
 		public RegExEntry(String pathName) {
 			this.pathEntryType = PathEntryType.REGEX;
 			this.pathName = pathName;
 			this.patterns = this.splitPatternUri(pathName);
 		}
 		@Override
 		public boolean match(PathEntry requestUri) {
 			String prestr = patterns[0];
 			if(prestr.length() != 0 && (!requestUri.pathName.startsWith(prestr))){
 				return false;
 			}
 			String poststr = patterns[2];
 			if(poststr.length() != 0 && (!requestUri.pathName.endsWith(poststr))){
 				return false;
 			}
			String toTestStr = requestUri.pathName.substring(prestr.length(), requestUri.pathName.indexOf(poststr));
 			if(toTestStr.length()==0){
 				return false;
 			}
 			if(p == null){
 				String regex = patterns[1];
 				p = Pattern.compile(regex);
 			}
 			return p.matcher(toTestStr).matches();
 		}
 		@Override
 		public int compareTo(PathEntry o) {
 			if(o == null || o instanceof RootEntry || o instanceof NormalEntry || o instanceof VarEntry){
 				return 1;
 			}else if((o instanceof RegExEntry)){
 				return 0;
 			}else{
 				return -1;
 			}
 		}
 		private String[] splitPatternUri(String uri){
 			String[] result = new String[3];
 			int firstpos = this.pathName.indexOf("#");
 			result[0] = this.pathName.substring(0, firstpos);
 			int secpos = this.pathName.indexOf("#", firstpos+2);
 			if(secpos == -1){
 				result[1] = this.pathName.substring(firstpos+2);
 				result[2] = "";
 			}else{
 				result[1] = this.pathName.substring(firstpos+2, secpos);
 				result[2] = this.pathName.substring(secpos+1);
 			}
 			return result;
 		}
 		@Override
 		public EntryResolver getPathEntryResolver() {
 			return emptyResolver;
 		}
 	}
 	
 	protected PathEntry analyseEntryType(String pathName) {
 		if(pathName.contains("#")){
 			if(pathName.contains("*"))
 				throw new RuntimeException("Can not use # and * at the same time!");
 			else{//with #
 				int firstPos = pathName.indexOf("#");
 				if(firstPos != pathName.length()-1){
 					if(pathName.charAt(firstPos+1)!='#')
 						return new VarEntry(pathName);
 					else{
 						if(firstPos+1 != pathName.length()-1){
 							return new RegExEntry(pathName);
 						}
 					}
 				}
 			}
 		}else if(pathName.contains("*")){//with *
 			return new WildCardEntry(pathName);
 		}
 		return new NormalEntry(pathName);
 	}
 }
