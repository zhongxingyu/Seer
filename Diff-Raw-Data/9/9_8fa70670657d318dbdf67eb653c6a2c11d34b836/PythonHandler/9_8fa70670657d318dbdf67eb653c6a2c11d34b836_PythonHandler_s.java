 /*
  * Copyright (C) 2012 CyborgDev <cyborg@alta189.com>
  *
  * This file is part of CyborgFactoids
  *
  * CyborgFactoids is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * CyborgFactoids is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this.  If not, see <http://www.gnu.org/licenses/>.
  */
 package com.alta189.cyborg.factoids.handlers;
 
 import com.alta189.cyborg.factoids.Factoid;
 import com.alta189.cyborg.factoids.FactoidContext;
 import com.alta189.cyborg.factoids.FactoidResult;
 import com.alta189.cyborg.factoids.LocationType;
 import com.alta189.cyborg.factoids.ReturnType;
 import com.alta189.cyborg.factoids.handlers.util.HTTPUtil;
 import java.io.UnsupportedEncodingException;
 import java.net.URLEncoder;
 
 public class PythonHandler implements Handler {
 	private static final String name = "python";
 	private static final String newLine = System.getProperty("line.separator");
 
 	@Override
 	public String getName() {
 		return name;
 	}
 
 	@Override
 	public FactoidResult handle(Factoid factoid, FactoidContext context) {
 
 		String[] args;
 		if (context.getRawArgs() == null || context.getRawArgs().isEmpty()) {
 			args = new String[0];
 		} else {
 			args = context.getRawArgs().split(" ");
 		}
 
 		StringBuilder url = new StringBuilder();
 		url.append("args=");
 
 		if (args != null && args.length > 0) {
 			url.append("['");
 			for (int i = 0; i < args.length; i++) {
 				url.append(args[i]).append("'");
 				if (i == args.length - 1) {
 					url.append("]");
 				} else {
 					url.append(",'");
 				}
 			}
 		} else {
 			url.append("['']");
 		}
 
 		url.append("; nick='").append(context.getSender().getNick()).append("'; chan='").append(context.getLocationType() == LocationType.CHANNEL_MESSAGE ? context.getChannel().getName() : "").append("'; ");
 		url.append(factoid.getContents());
 		String data = null;
 		try {
 			data = URLEncoder.encode(url.toString(), "UTF-8");
 		} catch (UnsupportedEncodingException e) {
 			e.printStackTrace();
 		}
 
 		if (data == null) {
 			return null;
 		}
 
 		url = new StringBuilder();
 		url.append("http://eval.appspot.com/eval?statement=").append(data);
 
 		String result = HTTPUtil.readURL(url.toString());
 		if (result == null || result.isEmpty()) {
 			return null;
 		}
 
 		if (result.contains(newLine)) {
 			result = result.split(newLine)[0];
 		}
 
 		if (result == null || result.isEmpty()) {
 			return null;
 		}
 		String handler = null;
 		if (result.length() > 4 && result.startsWith("<") && result.contains(">")) {
 			handler = result.substring(1, result.indexOf(">"));
 			if (handler.equalsIgnoreCase("act") || handler.equalsIgnoreCase("action") || handler.equalsIgnoreCase("notice") || handler.equalsIgnoreCase("reply")) {
 				result = result.substring(result.indexOf(">") + 1);
 				if (result.startsWith(" ") && result.length() > 1) {
 					result = result.substring(1, result.length());
 				}
 			}
 		}
 
 		if (result == null || result.isEmpty()) {
 			return null;
 		}
 
 		FactoidResult factoidResult = new FactoidResult();
 		if (handler != null && (handler.equalsIgnoreCase("act") || handler.equalsIgnoreCase("action"))) {
 			factoidResult.setReturnType(ReturnType.ACTION);
 		} else if (handler != null && handler.equalsIgnoreCase("notice")) {
 			factoidResult.setReturnType(ReturnType.NOTICE);
 		} else {
 			factoidResult.setReturnType(ReturnType.MESSAGE);
 		}
 		factoidResult.setTarget(context.getLocationType() == LocationType.CHANNEL_MESSAGE ? context.getChannel().getName() : context.getSender().getNick());
 		factoidResult.setBody(result);
 
 		return factoidResult;
 	}
 }
