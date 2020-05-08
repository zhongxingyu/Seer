 /*******************************************************************************
  * Copyright (C) 2007 The University of Manchester   
  * 
  *  Modifications to the initial code base are copyright of their
  *  respective authors, or their employers as appropriate.
  * 
  *  This program is free software; you can redistribute it and/or
  *  modify it under the terms of the GNU Lesser General Public License
  *  as published by the Free Software Foundation; either version 2.1 of
  *  the License, or (at your option) any later version.
  *    
  *  This program is distributed in the hope that it will be useful, but
  *  WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  *  Lesser General Public License for more details.
  *    
  *  You should have received a copy of the GNU Lesser General Public
  *  License along with this program; if not, write to the Free Software
  *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
  ******************************************************************************/
 package net.sf.taverna.t2.provenance.lineageservice.derby;
 
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import net.sf.taverna.t2.provenance.lineageservice.ProvenanceQuery;
 import net.sf.taverna.t2.provenance.lineageservice.utils.Port;
 
 /**
  * Uses Apache Derby to carry out provenance queries
  * 
  * @author Ian Dunlop
  * @author Stuart Owen
  * 
  */
 public class DerbyProvenanceQuery extends ProvenanceQuery {
 
     public DerbyProvenanceQuery() {
     }
    
	private String escapeValue(final String s) {
		return s.replaceAll("\\'", "\\'\\'");
	}

 
     /*
      * Overrides super class because Derby has issues with non-numerical values
      * requiring quotes
      *
      * @see
      * net.sf.taverna.t2.provenance.lineageservice.mysql.ProvenanceQuery#addWhereCaluseToQuery
      */
     @Override
     @SuppressWarnings("unused")
     protected String addWhereClauseToQuery(String q0,
             Map<String, String> queryConstraints, boolean terminate) {
         //FIXME terminate not required (I think!)
 
         // complete query according to constraints
         StringBuffer q = new StringBuffer(q0);
 
         boolean first = true;
         if (queryConstraints != null && queryConstraints.size() > 0) {
             q.append(" where ");
 
             for (Entry<String, String> entry : queryConstraints.entrySet()) {
                 if (!first) {
                     q.append(" and ");
                 }
                 // FIXME there may be more numerical inputs than isInputPort,
                 // needs checking - 21.5.09 there are - this kind of custom where clause is very dangerous and
                 //should be refactored at some point
                 if (entry.getKey().equals("V.isInputPort") || entry.getKey().equals("VB.positionInColl") || entry.getKey().equals("isInputPort")) {
                     q.append(" " + entry.getKey() + " = " + entry.getValue());
                 } else {
                    q.append(" " + entry.getKey() + " = \'" + escapeValue(entry.getValue()) + "\' ");
                 }
                 first = false;
             }
         }
 
         return q.toString();
     }
 }
