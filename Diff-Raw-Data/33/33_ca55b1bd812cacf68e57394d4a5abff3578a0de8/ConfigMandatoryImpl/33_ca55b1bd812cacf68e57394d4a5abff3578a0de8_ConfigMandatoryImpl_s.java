 /*******************************************************************************
  * Copyleft 2013 Massimiliano Leone - massimiliano.leone@iubris.net .
  * 
  * ConfigMandatoryImpl.java is part of Socrates.
  * 
  * 'Socrates' is free software; you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation; either version 3 of the License, or
  * (at your option) any later version.
  * 
  * 'Socrates' is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Lesser General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with 'Socrates' ; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301 USA
  ******************************************************************************/
 package net.iubris.socrates_sample.config;
 
 import net.iubris.socrates.config.ConfigMandatory;
 import net.iubris.socrates.model.http.request.url.output.HttpParserOutputType;
 
 /*
 @Provides
 Class<PlacesList> providePlaceListClass() {
 	return PlacesList.class;
 }*/
 public final class ConfigMandatoryImpl implements ConfigMandatory{
 	@Override
 	public boolean isUseSensor() {
 		return true;
 	}
 	
 
 	@Override
 	public HttpParserOutputType getOutput() {				
 		return HttpParserOutputType.json;
 	}
 	@Override
 	public String getKey() {				
		return "AIzaSyAfOlNmRr5G-4BPDd1faYsn9kvkV5ebBRk";
 	}
 
 	@Override
 	public String getApplicationName() {				
 		return "SocratesTest";
 	}
 }
