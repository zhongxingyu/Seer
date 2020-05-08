 /*  Copyright (C) 2011  Nicholas Wright
 	
 	part of 'Aid', an imageboard downloader.
 
     This program is free software: you can redistribute it and/or modify
     it under the terms of the GNU General Public License as published by
     the Free Software Foundation, either version 3 of the License, or
     (at your option) any later version.
 
     This program is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
     GNU General Public License for more details.
 
     You should have received a copy of the GNU General Public License
     along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package config;
 
 import java.util.Properties;
 
 public class DefaultAppSettings extends Properties {
 	private static final long serialVersionUID = 1L;
 
 	public DefaultAppSettings() {
 		this.setProperty("page_threads","1");
 		this.setProperty("image_threads","1");
 		this.setProperty("write_Blocked","false");
		this.setProperty("base_url","http://boards.4chan.org/");
		this.setProperty("sub_pages","a;15,w;15,wg;15");
 	}
 }
