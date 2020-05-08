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
 
 package org.inbio.ait.web.ajax.controller;
 
 import java.util.List;
 import javax.servlet.ServletOutputStream;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import org.inbio.ait.manager.QueryManager;
 import org.inbio.ait.manager.SpeciesManager;
 import org.inbio.ait.util.SpeciesNode;
 import org.inbio.ait.web.utils.TaxonInfoIndexColums;
 import org.springframework.web.servlet.ModelAndView;
 import org.springframework.web.servlet.mvc.Controller;
 
 /**
  *
  * @author esmata
  */
 public class TableController implements Controller{
 
     private SpeciesManager speciesManager;
     private QueryManager queryManager;
 
     @Override
     public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
 		String paramLayer = request.getParameter("l");
         String paramTaxon = request.getParameter("tx");
         String paramIndi = request.getParameter("i");
         String type = request.getParameter("t");
 		String errorMsj = "Error con los parámetros: "+
                 paramLayer+" "+paramTaxon+" "+paramIndi+" "+type;
 
 		try {
             //Arrays that contains the parameters data
             String[] layerArray = paramLayer.split("\\|");
             String[] taxonArray = paramTaxon.split("\\|");
             String[] indiArray = paramIndi.split("\\|");
 
             //Obtain the species list by all criteria
             List<SpeciesNode> species = speciesManager.speciesNodesByCriteria
                     (layerArray, taxonArray, indiArray);
 
             //Matrix to store the results
             Long[][] matrix;
 
              //Building the matrix content
             int x = species.size(), y = 0;
             if(type.equals("p")){ //If polygons, show species * indicators
                 y = indiArray.length;
                 matrix = new Long[x][y];
                 //Loop over rows
                 for(int i = 0;i<x;i++){
                     SpeciesNode node = species.get(i);
                     //Loop over columns
                     for(int j = 0;j<y;j++){
                         matrix[i][j] = queryManager.countByIndicator(node.getId(),
                                 indiArray[j],
                                TaxonInfoIndexColums.SPECIMENS.getName(),
                                layerArray[0]); //species,indicator,colunm,polygon
                     }
                 }
             }
             else{ //if indicators, show species * polygons
                 y = layerArray.length;
                 matrix = new Long[x][y];
                 //Loop over rows
                 for(int i = 0;i<x;i++){
                     SpeciesNode node = species.get(i);
                     //Loop over columns
                     for(int j = 0;j<y;j++){
                         matrix[i][j] = queryManager.countByPolygon(node.getId(),
                                 layerArray[j],
                                TaxonInfoIndexColums.SPECIMENS.getName(),
                                indiArray[0]); //species,polygon,colunm,indicator
                     }
                 }
             }
 
             //Return results via xml
             return writeReponse(request,response,matrix,species);
 
 		} catch (IllegalArgumentException iae) {
 			throw new Exception(errorMsj + " "+ iae.getMessage());
 		}
     }
 
     /**
      * Return the XML with the results
      * @param request
      * @param response
      * @param species
      * @param matchesByPolygon
      * @return
      * @throws java.lang.Exception
      */
 	private ModelAndView writeReponse(HttpServletRequest request,
 			HttpServletResponse response,Long[][] matrix,
             List<SpeciesNode> species) throws Exception {
 
 		response.setCharacterEncoding("ISO-8859-1");
 		response.setContentType("text/xml");
 		ServletOutputStream out = response.getOutputStream();
         
         StringBuilder result = new StringBuilder();
         result.append("<?xml version='1.0' encoding='ISO-8859-1'?><response>");
         result.append("<speciesList>");
         for(SpeciesNode sp : species){
             result.append("<species>"+sp.getName()+"</species>");
         }
         result.append("</speciesList>");
         if(matrix.length>0){
             int rows = matrix.length;
             int columns = matrix[0].length;
             for (int i = 0; i < rows; i++) {
                 result.append("<row>");
                 for (int j = 0; j < columns; j++) {
                     result.append("<column>" + matrix[i][j] + "</column>");
                 }
                 result.append("</row>");
             }
         }
         result.append("</response>");
 
         out.println(result.toString());
 		out.flush();
 		out.close();
 
 		return null;
 	}
 
     /**
      * @return the speciesManager
      */
     public SpeciesManager getSpeciesManager() {
         return speciesManager;
     }
 
     /**
      * @param speciesManager the speciesManager to set
      */
     public void setSpeciesManager(SpeciesManager speciesManager) {
         this.speciesManager = speciesManager;
     }
 
     /**
      * @return the queryManager
      */
     public QueryManager getQueryManager() {
         return queryManager;
     }
 
     /**
      * @param queryManager the queryManager to set
      */
     public void setQueryManager(QueryManager queryManager) {
         this.queryManager = queryManager;
     }
 
 }
