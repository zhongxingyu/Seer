 /*  MessageBodyParser.java - Parses the body of a message
  *  Copyright (C) 1999 Fredrik Ehnbom
  *
  *  This program is free software; you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation; either version 2 of the License, or
  *  (at your option) any later version.
  *
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with this program; if not, write to the Free Software
  *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
  */
 
 package org.gjt.fredde.yamm.mail;
 
 import java.text.*;
 import java.io.*;
 import java.util.*;
 
 /**
  * Parses the body of a message
  */
 public class MessageBodyParser {
 
 	public static final int END = 0;
 	public static final int ERROR = 1;
 	public static final int ATTACHMENT = 2;
 
 	protected boolean html = false;
 
 	public static String unMime(String m, boolean html) {
 		char[] check = "0123456789ABCDEF".toCharArray();
 		StringTokenizer tok = new StringTokenizer(m);
 		String mime2 = "";
 
 		while (tok.hasMoreTokens()) {
 			String mime = tok.nextToken();
 
 			if (mime.indexOf("=") != -1) {
 
 				if (mime.toLowerCase().indexOf("://") != -1) {
 					mime2 += mime + " ";
 					continue;
 				}
 
 				for (int index = mime.indexOf("=") + 1;;
 					index = mime.indexOf("=", index) + 1) {
 
 					if (index == 0) {
 						break;
 					} else if (index + 2 > mime.length()) {
 						break;
 					}
 					char[] hex = mime.substring(index,
 						index + 2).toCharArray();
 
 					boolean char1 = false;
 					boolean char2 = false;
 
 					for (int i = 0; i < 16; i++) {
 						if (hex[0] == check[i]) {
 							char1 = true;
 						}
 						if (hex[1] == check[i]) {
 							char2 = true;
 						}
 					}
 
 					if (char1 && char2) {
 						int asciiChar =
 							Integer.parseInt(
 							new String(hex), 16);
 						String begin = mime.substring(0,
 								index - 1);
 						String end = mime.substring(
 								index + 2,
 								mime.length());
 						if (html) {
 							mime = begin + "&#" +
 								asciiChar +
 								";" + end;
 						} else {
 							mime = begin +
 								(char) asciiChar
 								+ end;
 						}
 					}
 				}
 				mime2 += mime + " ";
 			} else {
 				mime2 += mime + " ";
 			}
 		}
 
 		return mime2;
 	}
 
 	protected String makeLink(String link) {
 		StringTokenizer tok = new StringTokenizer(link);
 		link = "";
 
 		while (tok.hasMoreTokens()) {
 			String temp = tok.nextToken();
 
 			if (temp.indexOf("://") != -1) {
 				String tmp[] = MessageParser.parseLink(temp);
 
 				temp = "<a href=\"" + tmp[1] + "\">" + tmp[1] +
 						"</a>";
 				link += tmp[0] + temp + tmp[2] + " ";
 			} else {
 				link += temp + " ";
 			}
 		}
 
 		return link;
 	}
 
 	public static String makeEmailLink(String mail) {
 		StringTokenizer tok = new StringTokenizer(mail);
 
 		mail = "";
 
 		while (tok.hasMoreTokens()) {
 			String temp = tok.nextToken();
 
 			if (temp.indexOf("@") != -1) {
 				String tmp[] = MessageParser.parseLink(temp);
 
 				temp = "<a href=mailto:" + tmp[1] + ">" +
 								tmp[1] + "</a>";
 
 				mail += tmp[0] + temp + tmp[2] + " ";
 			} else {
 				mail += temp + " ";
 			}
 		}
 
 		return mail;
 	}
 
 	public MessageBodyParser(boolean html) {
 		this.html = html;
 	}
 
 	public int parse(BufferedReader in, PrintWriter out,
 				String attachment) throws IOException,
 							MessageParseException {
 		for (;;) {
 			String temp = in.readLine();
 
 			if (temp == null) {
 				throw new MessageParseException("Unexpected" +
 							"end of message.");
 			}
 
 			if (temp.equals(".")) {
 				return END;
 			}
 
 			if (temp.indexOf("=") != -1) {
 				temp = unMime(temp, html);
 			}
 
 			if (temp.indexOf("://") != -1) {
 				if (temp.toLowerCase().indexOf("href=") == -1) {
 					temp = makeLink(temp);
 				}
 			}
 
 			if (temp.indexOf("@") != -1) {
 				if (temp.toLowerCase().indexOf("href=") == -1) {
 					temp = makeEmailLink(temp);
 				}
 			}
 
 			if (temp.startsWith("--" + attachment)) {
				if (temp.endsWith("-- ")) {
					return END;
				} else {
					return ATTACHMENT;
				}
 			}
 
 			if (temp.endsWith("= ") && !temp.endsWith("== ")) {
 				temp = temp.substring(0, temp.length() - 2);
 				out.print(temp);
 			} else {
 				out.println(temp);
 			}
 		}
 //		return ERROR;
 	}
 }
