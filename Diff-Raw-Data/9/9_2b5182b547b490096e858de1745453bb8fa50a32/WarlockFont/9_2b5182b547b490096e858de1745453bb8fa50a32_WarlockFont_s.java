 /**
  * Warlock, the open-source cross-platform game client
  *  
  * Copyright 2008, Warlock LLC, and individual contributors as indicated
  * by the @authors tag. 
  *
  * This is free software; you can redistribute it and/or modify it
  * under the terms of the GNU Lesser General Public License as
  * published by the Free Software Foundation; either version 2.1 of
  * the License, or (at your option) any later version.
  *
  * This software is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this software; if not, write to the Free
  * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
  * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
  */
 package cc.warlock.core.client;
 
 public class WarlockFont {
 
 	public static final WarlockFont DEFAULT_FONT = new WarlockFont();
 	static {
 		DEFAULT_FONT.setFamilyName("default");
 		DEFAULT_FONT.setSize(-1);
 	}
 	
 	protected String familyName;
 	protected int size;
 	
 	public WarlockFont () { }
 	public WarlockFont (WarlockFont other)
 	{
 		this.familyName = new String(other.familyName);
 		this.size = other.size;
 	}
 	
 	public String getFamilyName() {
 		return familyName;
 	}
 	public void setFamilyName(String familyName) {
 		this.familyName = familyName;
 	}
 	public int getSize() {
 		return size;
 	}
 	public void setSize(int size) {
 		this.size = size;
 	}
 	
 	@Override
 	public boolean equals(Object obj) {
 		if (obj instanceof WarlockFont) {
 			WarlockFont other = (WarlockFont)obj;
			
 			return (familyName.equals(other.familyName) && size == other.size);
 		}
 		return super.equals(obj);
 	}
 	
 	public boolean isDefaultFont()
 	{
 		return this.equals(DEFAULT_FONT);
 	}
 }
