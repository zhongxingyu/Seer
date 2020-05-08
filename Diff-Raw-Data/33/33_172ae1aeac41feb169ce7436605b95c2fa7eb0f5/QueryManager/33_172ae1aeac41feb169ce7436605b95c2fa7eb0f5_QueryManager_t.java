 /* AIT - Analysis of taxonomic indicators
  *
  * Copyright (C) 2010  INBio (Instituto Nacional de Biodiversidad)
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package org.inbio.ait.manager;
 
 import java.util.List;
 import org.inbio.ait.model.TaxonInfoIndex;
 
 /**
  * @author esmata
  */
 public interface QueryManager {
 
     public List<TaxonInfoIndex> getallTaxonInfo();
 
     /**
      * Count all dwc registers from taxonInfoIndex table that match
      * with the specified query criteria
      * @return
      */
     public Long countByCriteria
             (String[] layerList,String[] taxonList,String[] indicList,String colum);
 
     /**
      * Count all dwc registers from taxonInfoIndex table that match
      * with the specified query criteria
      * @param x represents the first search criteria corresponding to the data
      * from x axis on the chart
      * @param y tha same as x but with the y axis
      * @param xType it coul be "geo","indi" or "taxo" see ChartCriteria.java enum
      * @param yType it coul be "geo","indi" or "taxo" see ChartCriteria.java enum
      * @return
      */
     public Long countByCriteria(String x,String y,int xType,int yType);
 
    public Long countByIndicator(String species,String indicator,String colunm,String polygon);
 
    public Long countByPolygon(String species,String polygon,String colunm,String indicator);
 
 }
