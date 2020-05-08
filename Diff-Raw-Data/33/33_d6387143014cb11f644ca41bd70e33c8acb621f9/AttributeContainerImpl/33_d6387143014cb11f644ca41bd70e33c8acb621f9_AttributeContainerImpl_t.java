 /**
  * Copyright (C) 2000, 2001 Maynard Demmon, maynard@organic.com
  * All rights reserved.
  * 
  * Redistribution and use in source and binary forms, with or 
  * without modification, are permitted provided that the 
  * following conditions are met:
  * 
  *  - Redistributions of source code must retain the above copyright 
  *    notice, this list of conditions and the following disclaimer. 
  * 
  *  - Redistributions in binary form must reproduce the above 
  *    copyright notice, this list of conditions and the following 
  *    disclaimer in the documentation and/or other materials provided 
  *    with the distribution. 
  * 
  *  - Neither the names "Java Outline Editor", "JOE" nor the names of its 
  *    contributors may be used to endorse or promote products derived 
  *    from this software without specific prior written permission. 
  * 
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS 
  * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT 
  * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS 
  * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE 
  * COPYRIGHT HOLDERS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, 
  * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, 
  * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; 
  * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER 
  * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT 
  * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN 
  * ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE 
  * POSSIBILITY OF SUCH DAMAGE.
  */
 
 package com.organic.maynard.outliner;
 
 import java.util.*;
 
 /**
  * @author  $Author$
  * @version $Revision$, $Date$
  */
  
 public class AttributeContainerImpl implements AttributeContainer {
 	
 	// Instance Variables
 	private HashMap attributes = null;
 	private HashMap isReadOnly = null;
 	
 	
 	// The Constructors
 	public AttributeContainerImpl() {}
 	
 	
 	public void setAttribute(String key, Object value) {
 		setAttribute(key, value, false);
 	}
 	
 	public void setAttribute(String key, Object value, boolean isReadOnly) {
		// Map null keys to empty keys to prevent NPEs for code that wants to work
		// with the keys.
		if (key == null) {
			key = "";
			System.out.println("Warning: key was null in AttibuteContainerImpl setAttribute(String key, Object value, boolean isReadOnly)");
		}
		
 		if (attributes == null) {
 			this.attributes = new HashMap();
 			this.isReadOnly = new HashMap();
 		}
 		this.attributes.put(key, value);
 		this.isReadOnly.put(key, new Boolean(isReadOnly));
 	}
 	
 	public void removeAttribute(String key) {
		if (attributes != null) {
			if (key == null) {
				key = "";
			}
			
 			this.attributes.remove(key);
 			this.isReadOnly.remove(key);
 		}
 	}
 	
 	public void clearAttributes() {
 		if (attributes != null) {
 			this.attributes.clear();
 			this.isReadOnly.clear();
 		}
 	}
 	
 	public Object getAttribute(String key) {
		if (attributes != null) {
			if (key == null) {
				key = "";
			}
			
 			return attributes.get(key);
 		}
 		return null;
 	}
 	
 	public boolean isReadOnly(String key) {
		if (isReadOnly != null) {
			if (key == null) {
				key = "";
			}
			
 			Boolean b = (Boolean) isReadOnly.get(key);
 			if (b != null) {
 				return b.booleanValue();
 			}
 		}
 		return false;
 	}
 	
 	public void setReadOnly(String key, boolean isReadOnly) {
		if (key == null) {
			key = "";
		}
		
 		if (attributes == null || !(this.isReadOnly.containsKey(key))) {
 			return;
 		}
 		this.isReadOnly.put(key, new Boolean(isReadOnly));
 	}
 	
 	public int getAttributeCount() {
 		if (attributes != null) {
 			return attributes.size();
 		}
 		return 0;
 	}
 	
 	public Iterator getAttributeKeys() {
 		if (attributes != null) {
 			return attributes.keySet().iterator();
 		}
 		return null;
 	}
 }
