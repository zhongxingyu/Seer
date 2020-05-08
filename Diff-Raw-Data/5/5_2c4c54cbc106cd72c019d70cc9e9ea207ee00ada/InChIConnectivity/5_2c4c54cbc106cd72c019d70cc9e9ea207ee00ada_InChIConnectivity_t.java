 /**
  * InChIConnectivity.java
  *
  * 2011.12.29
  *
  * This file is part of the CheMet library
  * 
  * The CheMet library is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * CheMet is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU Lesser General Public License
  * along with CheMet.  If not, see <http://www.gnu.org/licenses/>.
  */
 package uk.ac.ebi.metabolomes.util.inchi;
 
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 import org.apache.log4j.Logger;
 
 /**
  * @name    InChIConnectivity
  * @date    2011.12.29
  * @version $Rev$ : Last Changed $Date$
  * @author  pmoreno
  * @author  $Author$ (this version)
  * @brief   ...class description...
  *
  */
 public class InChIConnectivity {
     
     private static final Logger LOGGER = Logger.getLogger(InChIConnectivity.class);
     
     public static String getInChIConnectivity(String InChI) {
         String tranformedInChI = getInChIUptoFirstConnectivityLayer(InChI);
         return getRidOfHydrogenInMolFormulaOfInChIFirstLayer(tranformedInChI);
     }
     
     public static String getInChIUptoFirstConnectivityLayer(String inchi) {
         if(inchi==null)
             return null;
         String[] parts = inchi.split("/"); 
         if( parts.length > 3 || (parts.length == 3 && inchi.lastIndexOf("/")+1==inchi.length())) {
             return parts[0]+"/"+parts[1]+"/"+parts[2]+"/"; // so we include the last /
         } else if(parts.length == 3) {
             return parts[0]+"/"+parts[1]+"/"+parts[2];
         }
         else 
             return inchi;
     }
     
     private static final Pattern hydrogenInInChIFirstLayer = Pattern.compile("H\\d*");;
    private static final Pattern CHxOnlyPattern = Pattern.compile("/([A-Z][a-z]{0,1}){0,1}H\\d*/h");
     /**
      * Takes a first layer (mol formula) and connectivity and
      * replaced the H<number> for %, so that mysql will use it 
      * as a wildcard to search for the same molecule formula (no hydrogens)
      * and connectivity table.
      * 
      * @param inchiFirstLayer
      * @return converted inchi.
      */
     public static String getRidOfHydrogenInMolFormulaOfInChIFirstLayer(String inchiFirstLayer) {
        // We need to avoid the case of InChI=1S/C%/h1H4/ or 1S/F%/h1H/ or 1S/Cl%/h1H/ or 1S/%/h1H/
         Matcher CHxOnlyMatcher = CHxOnlyPattern.matcher(inchiFirstLayer);
         if(CHxOnlyMatcher.find())
             return inchiFirstLayer;
         Matcher hydrogenInLayerMatcher = hydrogenInInChIFirstLayer.matcher(inchiFirstLayer);
         return hydrogenInLayerMatcher.replaceFirst("%");
     }
 }
