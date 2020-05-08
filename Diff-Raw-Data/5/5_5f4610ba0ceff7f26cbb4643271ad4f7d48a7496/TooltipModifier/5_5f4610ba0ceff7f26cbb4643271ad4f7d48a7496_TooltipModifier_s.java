 /*
  * Jabox Open Source Version
  * Copyright (C) 2009-2010 Dimitris Kapanidis                                                                                                                          
  * 
  * This file is part of Jabox
  * 
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see http://www.gnu.org/licenses/.
  */
 package org.jabox.webapp.modifiers;
 
 import org.apache.wicket.AttributeModifier;
 import org.apache.wicket.model.Model;
 
 public class TooltipModifier extends AttributeModifier {
 
 	private static final String TITLE = "title";
 
 	public TooltipModifier(final String tooltip) {
		super(TITLE, true, new Model<String>(tooltip));
 	}
 
 	public TooltipModifier(final Model<String> tooltipModel) {
		super(TITLE, true, tooltipModel);
 	}
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = -2577746456955106108L;
 
 }
