 /* 
  * Copyright (C) 2013 Dr Daniel R. Naylor
  * 
  * This file is part of mcMMO Party Admin.
  *
  * mcMMO Party Admin is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * mcMMO Party Admin is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with mcMMO Party Admin.  If not, see <http://www.gnu.org/licenses/>.
  * 
  **/
 package uk.co.drnaylor.mcmmopartyadmin;
 
 import com.gmail.nossr50.api.PartyAPI;
 import com.gmail.nossr50.datatypes.party.Party;
 import java.util.ArrayList;
 import java.util.List;
 
 public class Util {
     
     /**
      * Get party object from it's name.
      * @param party Party to get
      * @return Party if it exists, null otherwise.
      */
     public static Party getPartyFromList(String party) {
         List<Party> parties = PartyAPI.getParties();
         for (Party p : parties) {
             if (p.getName().equals(party)) {
                 return p;
             }
         }
         return null;
     }
     
     /**
      * Returns a string list of available parties.
      * 
      * @return Collection of party names
      */
     public static ArrayList<String> getPartyCollection() {
         ArrayList<String> a = new ArrayList<String>();
         List<Party> parties = PartyAPI.getParties();
         for (Party p : parties) {
             a.add(p.getName());
         }
         java.util.Collections.sort(a);
         return a;
     }
 }
