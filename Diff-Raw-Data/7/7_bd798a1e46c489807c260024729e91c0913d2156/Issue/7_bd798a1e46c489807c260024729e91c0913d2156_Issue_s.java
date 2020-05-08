 package org.daisy.validation.epubcheck;
 
 import com.google.common.base.Preconditions;
 import com.google.common.base.Strings;
 
 public final class Issue {
 
 	public static enum Type {
 		WARNING, ERROR, VERSION, INTERNAL_ERROR;
 
 		public static Type safeValueOf(String name) {
 			Preconditions.checkNotNull(name);
 			try {
 				return valueOf(name);
 			} catch (IllegalArgumentException e) {
 				//TODO log
 				return INTERNAL_ERROR;
 			}
 		}
 	}
 
 	public final Type type;
 	public final String file;
 	public final int lineNo;
 	public final int colNo;
 	public final String txt;
 
 	public Issue(Type type, String file, int lineNo, int colNo, String txt) {
 		Preconditions.checkNotNull(type);
 		Preconditions.checkArgument(!Strings.isNullOrEmpty(txt),
 				"Issue message must not be empty.");
 		this.type = type;
 		this.file = file;
 		this.lineNo = lineNo;
 		this.colNo = colNo;
 		this.txt = txt;
 	}
 
 	public Issue(Type type, String file, String txt) {
 		this(type, file, -1, -1, txt);
 	}
 
 	public Issue(Type type, String txt) {
 		this(type, null, -1, -1, txt);
 	}
 
 	public String toString() {
 		StringBuilder sb = new StringBuilder();
 		sb.append('[').append(type).append(']');
 		sb.append(txt);
 		if (!Strings.isNullOrEmpty(file)) {
 			sb.append(" - ").append(file);
 			if (lineNo > -1) {
 				sb.append(" (").append(lineNo);
 				if (colNo > -1) {
 					sb.append(':').append(colNo);
 				}
 				sb.append(')');
 			}
 		}
 		return sb.toString();
 	}
 }
