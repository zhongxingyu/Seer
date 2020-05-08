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
 import com.alta189.cyborg.factoids.ReturnType;
 
 public class NoticeHandler implements Handler {
 	
	private static final String name = "reply";
 
 	@Override
 	public String getName() {
 		return name;
 	}
 
 	@Override
 	public FactoidResult handle(Factoid factoid, FactoidContext context) {
 		FactoidResult result = new FactoidResult();
 		
 		result.setBody(factoid.getContents());
 		result.setReturnType(ReturnType.NOTICE);
 		result.setTarget(context.getSender().getNick());
 
 		return result;
 	}
 }
