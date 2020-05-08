 package org.fnppl.opensdx.common;
 
 import java.util.Vector;
 /*
  * Copyright (C) 2010-2013 
  * 							fine people e.V. <opensdx@fnppl.org> 
  * 							Henning Thieß <ht@fnppl.org>
  * 
  * 							http://fnppl.org
 */
 
 /*
  * Software license
  *
  * As far as this file or parts of this file is/are software, rather than documentation, this software-license applies / shall be applied.
  *  
  * This file is part of openSDX
  * openSDX is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * openSDX is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU Lesser General Public License
  * and GNU General Public License along with openSDX.
  * If not, see <http://www.gnu.org/licenses/>.
  *      
  */
 
 /*
  * Documentation license
  * 
  * As far as this file or parts of this file is/are documentation, rather than software, this documentation-license applies / shall be applied.
  * 
  * This file is part of openSDX.
  * Permission is granted to copy, distribute and/or modify this document 
  * under the terms of the GNU Free Documentation License, Version 1.3 
  * or any later version published by the Free Software Foundation; 
  * with no Invariant Sections, no Front-Cover Texts, and no Back-Cover Texts. 
  * A copy of the license is included in the section entitled "GNU 
  * Free Documentation License" resp. in the file called "FDL.txt".
  * 
  */
 
 /**
  * 
  * @author Bertram Boedeker <bboedeker@gmx.de>
  * 
  */
 public class Contributor extends BusinessObject {
 
 	public static String KEY_NAME = "contributor";
 
 	public static String TYPE_LABEL = "label";
 	public static String TYPE_PERFORMER = "performer";
 	public static String TYPE_TEXTER = "texter";
 	public static String TYPE_EDITOR = "editor";
 	public static String TYPE_CONDUCTOR = "conductor";
 	public static String TYPE_ORCHESTRA = "orchestra";
 	public static String TYPE_DISPLAY_ARTIST = "display_artist";
 	public static String TYPE_SINGER = "singer";
 	public static String TYPE_COMPOSER = "composer";
 	public static String TYPE_MIXER = "mixer";
 	public static String TYPE_REMIXER = "remixer";
 	public static String TYPE_PRODUCER = "producer";
 	public static String TYPE_AUTHOR = "author";
 	public static String TYPE_ARRANGER = "arranger";
 	public static String TYPE_FEATURING = "featuring";
 	public static String TYPE_WITH = "with";
 	public static String TYPE_DJ = "DJ";
 	public static String TYPE_VERSUS = "versus";
 	public static String TYPE_MEETS = "meets";
 	public static String TYPE_PRESENTS = "presents";
 	public static String TYPE_COMPILATOR = "compilator";
 	public static String TYPE_COPYRIGHT = "copyright";
 	public static String TYPE_PRODUCTION = "production";
 	public static String TYPE_PUBLISHER = "publisher";
 	public static String TYPE_CLEARINGHOUSE = "clearinghouse";
 	
 	public static String TYPE_NARRATOR = "narrator";
 	public static String TYPE_ENSEMBLE = "ensemble";
 	public static String TYPE_CHOIR = "choir";
 	public static String TYPE_ACCOMPANIST = "accompanist";
 	public static String TYPE_SOLOIST = "soloist";
 	
 	public static final Vector<String> TYPES = new Vector<String>();
 	
 	static {
 		TYPES.add(TYPE_LABEL);
 		TYPES.add(TYPE_PERFORMER);
 		TYPES.add(TYPE_TEXTER);
 		TYPES.add(TYPE_EDITOR);
 		TYPES.add(TYPE_CONDUCTOR);
 		TYPES.add(TYPE_ORCHESTRA);
 		TYPES.add(TYPE_DISPLAY_ARTIST);
 		TYPES.add(TYPE_SINGER);
 		TYPES.add(TYPE_COMPOSER);
 		TYPES.add(TYPE_MIXER);
 		TYPES.add(TYPE_REMIXER);
 		TYPES.add(TYPE_PRODUCER);
 		TYPES.add(TYPE_AUTHOR);
 		TYPES.add(TYPE_ARRANGER);
 		TYPES.add(TYPE_FEATURING);
 		TYPES.add(TYPE_WITH);
 		TYPES.add(TYPE_DJ);
 		TYPES.add(TYPE_VERSUS);
 		TYPES.add(TYPE_MEETS);
 		TYPES.add(TYPE_PRESENTS);
 		TYPES.add(TYPE_COMPILATOR);
 		TYPES.add(TYPE_COPYRIGHT);
 		TYPES.add(TYPE_PRODUCTION);
 		TYPES.add(TYPE_PUBLISHER);
 		TYPES.add(TYPE_CLEARINGHOUSE);
 		TYPES.add(TYPE_NARRATOR);
 	}
 	
 	private BusinessStringItem name;	//MUST
 	private BusinessStringItem type;	//MUST
 	private BusinessStringItem year;	//SHOULD: for copyright & production	
 	private IDs ids;					//MUST
 	private InfoWWW www;				//SHOULD
 	private boolean on_sublevel_only = false;
 	
 
 	public static Contributor make(String name, String type, IDs ids) {
 		Contributor contributor = new Contributor();
 		contributor.name = new BusinessStringItem("name", name);
 		contributor.type = new BusinessStringItem("type", type);
 		contributor.year = null;		
 		contributor.ids = ids;
 		contributor.www = null;
 		return contributor;
 	}
 	
 	public boolean getOnSubLevelOnly() {
 		return on_sublevel_only;
 	}
 
 	public Contributor on_sublevel_only(boolean value) {
 		on_sublevel_only = value;
 		return this;
 	}
 
 	public static Contributor fromBusinessObject(BusinessObject bo) {
 		if (bo==null) return null;
 		if (!bo.getKeyname().equals(KEY_NAME)) {
 			bo = bo.handleBusinessObject(KEY_NAME);
 		}
 		if (bo==null) return null;
 		
 		Contributor contributor = new Contributor();
 		contributor.initFromBusinessObject(bo);
 		
 		contributor.name = BusinessStringItem.fromBusinessObject(bo, "name");
 		contributor.type = BusinessStringItem.fromBusinessObject(bo, "type");
 		contributor.year = BusinessStringItem.fromBusinessObject(bo, "year");
 		contributor.ids = IDs.fromBusinessObject(bo);
 		contributor.www = InfoWWW.fromBusinessObject(bo);
 		
 		return contributor;
 	}
 
 	public String toString() {
 		return getName()+" ("+getType()+")";
 	}
 
 	public Contributor name(String name) {
 		this.name = new BusinessStringItem("name", name);
 		return this;
 	}
 
 	public Contributor type(String type) {
 		this.type = new BusinessStringItem("type", type);
 		return this;
 	}
 	
 	public Contributor year(String year) {
 		if (year == null) this.year = null;
 		else this.year = new BusinessStringItem("year", year);
 		return this;
 	}	
 
 	public Contributor ids(IDs ids) {
 		this.ids = ids;
 		return this;
 	}
 
 	public Contributor www(InfoWWW www) {
 		this.www = www;
 		return this;
 	}
 	
 	public String getName() {
 		if (name==null) return null;
 		return name.getString();
 	}
 
 	public String getType() {
 		if (type==null) return null;
 		return type.getString();
 	}
 
 	public IDs getIDs() {
 		return ids;
 	}
 
 	public InfoWWW getWww() {
 		return www;
 	}
 	
 	public String getYear() {
 		if (year==null) return null;
 		return year.getString();
 	}	
 	
 	public String getKeyname() {
 		return KEY_NAME;
 	}
 }
