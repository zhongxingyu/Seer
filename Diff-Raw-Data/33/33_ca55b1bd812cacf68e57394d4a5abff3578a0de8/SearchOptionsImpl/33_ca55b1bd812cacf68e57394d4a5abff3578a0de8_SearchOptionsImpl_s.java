 /*******************************************************************************
  * Copyleft 2013 Massimiliano Leone - massimiliano.leone@iubris.net .
  * 
  * SearchOptionsImpl.java is part of Socrates.
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
 
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import net.iubris.socrates.config.SearchOptions;
 import net.iubris.socrates.model.http.request.url.language.Language;
 import net.iubris.socrates.model.http.request.url.parameters.optional.search.values.RankBy;
 import net.iubris.socrates.model.http.response.data.search.PlaceType;
 
 public final class SearchOptionsImpl implements SearchOptions {
 	public SearchOptionsImpl() {
 		System.out.println("ConfigOptionalImpl: "+this.hashCode());
 	}
 	
 
 	@Override
 	public Set<PlaceType> getTypes() {
 		HashSet<PlaceType> hashSet = new HashSet<PlaceType>();
		hashSet.add(PlaceType.bar);
		hashSet.add(PlaceType.cafe);
 		return hashSet;
 	}
 
 	
 
 	@Override
 	public List<String> getNames() {				
 		return null;
 	}
 
 	@Override
 	public Integer getRadius() {
 		return 5000;
 		//return null;
 	}
 	
 	@Override
 	public RankBy getRankBy() {
 		//return RankBy.distance;
 		return RankBy.prominence;
 	}
 	@Override
 	public String getKeyword() {
 		return "";
 	}
 	@Override
 	public Language getLanguage() {
 		return Language.italian;
 	}
 }
