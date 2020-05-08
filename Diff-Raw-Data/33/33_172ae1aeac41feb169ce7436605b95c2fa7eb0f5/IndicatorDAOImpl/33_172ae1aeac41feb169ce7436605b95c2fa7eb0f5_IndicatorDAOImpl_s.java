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
 
 package org.inbio.ait.dao.impl;
 
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.List;
 import org.inbio.ait.dao.IndicatorDAO;
 import org.inbio.ait.model.AutocompleteNode;
 import org.springframework.jdbc.core.simple.ParameterizedRowMapper;
 import org.springframework.jdbc.core.simple.SimpleJdbcDaoSupport;
 
 /**
  * Implementing IndicatorDAO methods
  * @author esmata
  */
 public class IndicatorDAOImpl extends SimpleJdbcDaoSupport implements IndicatorDAO{
 
     @Override
     public List<AutocompleteNode> getChildNodesByNodeId(int nodeId) {
         List<AutocompleteNode> nodes = new ArrayList<AutocompleteNode>();
         try{
            String query = "Select indicator_id,indicator_name from ait.indicator where indicator_ancestor_id = "+nodeId+";";
 
             nodes = getSimpleJdbcTemplate().query(query,
                     new AutocompleteMapper());
         }
         catch(Exception e){return nodes;}
         return nodes;
     }
 
     private static class AutocompleteMapper implements ParameterizedRowMapper<AutocompleteNode> {
 
         @Override
         public AutocompleteNode mapRow(ResultSet rs, int rowNum) throws SQLException {
 
             AutocompleteNode acn = new AutocompleteNode();
             acn.setItemName(rs.getString("indicator_name"));
             acn.setItemId(rs.getString("indicator_id"));
             return acn;
         }
     }
 
 }
