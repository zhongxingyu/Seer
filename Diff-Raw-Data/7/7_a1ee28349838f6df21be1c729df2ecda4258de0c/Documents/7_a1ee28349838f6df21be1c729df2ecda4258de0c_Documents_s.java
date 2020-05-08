 /*
  * Copyright (C) 2011 Openismus GmbH
  *
  * This file is part of GWT-Glom.
  *
  * GWT-Glom is free software: you can redistribute it and/or modify it
  * under the terms of the GNU Lesser General Public License as published by the
  * Free Software Foundation, either version 3 of the License, or (at your
  * option) any later version.
  *
  * GWT-Glom is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
  * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
  * for more details.
  *
  * You should have received a copy of the GNU Lesser General Public License
  * along with GWT-Glom.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package org.glom.web.shared;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 
 /**
  * TODO: Can't we have a list of pairs instead, avoiding the custom class?
  */
 @SuppressWarnings("serial")
 public class Documents implements Serializable {
	private final ArrayList<String> documentIDs = new ArrayList<String>();
	private final ArrayList<String> titles = new ArrayList<String>();
 
 	public Documents() {
 	}
 
 	public void addDocument(final String documentID, final String title) {
 		documentIDs.add(documentID);
 		titles.add(title);
 	}
 
 	public String getDocumentID(final int index) {
 		return documentIDs.get(index);
 	}
 
 	public String getTitle(final int index) {
 		return titles.get(index);
 	}
 
 	public int getCount() {
 		return documentIDs.size();
 	}
 }
