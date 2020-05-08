 /*******************************************************************************
  * This file is part of MPAF.
  * 
  * MPAF is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Affero General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * MPAF is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Affero General Public License for more details.
  * 
  * You should have received a copy of the GNU Affero General Public License
  * along with MPAF.  If not, see <http://www.gnu.org/licenses/>.
  ******************************************************************************/
 package mpaf.games;
 
 public enum HandlerType {
 	
 	UNKNOWN("UNKN"),
 	/**
 	 * Battlefield 3
 	 */
	BATTLEFIELD3("Battlefield3");
 	
 	private final String code;
 
 	private HandlerType(String code) {
 		this.code = code;
 	}
 
 	public String getCode() {
 		return this.code;
 	}
 	
 	public static HandlerType getByString(String s) {
 		for(HandlerType t : HandlerType.values())
 			if(t.getCode().equalsIgnoreCase(s))
 				return t;
 		return UNKNOWN;
 	}
 }
