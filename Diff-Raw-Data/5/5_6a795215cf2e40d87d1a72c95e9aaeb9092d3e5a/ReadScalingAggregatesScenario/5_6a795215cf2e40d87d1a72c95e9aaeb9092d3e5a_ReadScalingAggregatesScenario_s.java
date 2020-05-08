 /**
  * Bristlecone Test Tools for Databases
  * Copyright (C) 2006-2007 Continuent Inc.
  * Contact: bristlecone@lists.forge.continuent.org
  *
  * This program is free software; you can redistribute it and/or modify
  * it under the terms of version 2 of the GNU General Public License as
  * published by the Free Software Foundation.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA
  *
  * Initial developer(s): Robert Hodges and Ralph Hannus.
  * Contributor(s):
  */
 
 package com.continuent.bristlecone.benchmark.scenarios;
 
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 
 import org.apache.log4j.Logger;
 
 import com.continuent.bristlecone.benchmark.db.SqlDialect;
 import com.continuent.bristlecone.benchmark.db.Table;
 
 /**
  * Implements a scenario that computes aggregates on rows that are joined 
  * using a cross-product between two tables.  This scenario is designed
  * to maximize stress on the database by forcing CPU intensive computations. 
  * 
  * @author rhodges
  */
 public class ReadScalingAggregatesScenario extends ScenarioBase
 {
   private static final Logger logger = Logger.getLogger(ReadScalingAggregatesScenario.class);
   private static final String lineSeparator = System.getProperty("line.separator");
   
   protected int selectrows = 1;
   
   /** 
    * Defines the number of rows selected for running aggregates, which
    * affects the amount of work the DBMS engine much perform. 
    */
   public void setSelectrows(int selectrows)
   {
     this.selectrows = selectrows;
   }
 
   /** Prepare for scenario. */
   public void prepare() throws Exception
   {
     // No work to be done here. 
   }
 
   /** Execute an interation. */
   public void iterate(long iterationCount) throws Exception
   {
     // Select two random tables and generate SQL to join between them. 
     SqlDialect dialect = helper.getSqlDialect();
     Table[] tables = tableSet.getRandomTables(2); 
     String select = dialect.getSelectCrossProductCount(tables);
 
     // Generate indexes for searching.  
     int i1 = (int) (Math.random() * this.datarows);
     int i2 = i1 + this.selectrows;
 
     // Pick a table at random on which to operate.
     PreparedStatement pstmt = this.conn.prepareStatement(select);
     pstmt.setInt(1, i1);
     pstmt.setInt(2, i2);
     
     // If we are in debug mode dump the SQL and arguments. 
    if (logger.isInfoEnabled())
     {
       StringBuffer sb = new StringBuffer();
       sb.append("SQL=").append(select);
       sb.append(lineSeparator).append("i1=").append(i1);
       sb.append(lineSeparator).append("i2=").append(i2);
      logger.info(sb.toString());
     }
     
     // Do the query and force cycling through results. 
     ResultSet rs = pstmt.executeQuery();
     while (rs.next())
     {
       rs.getString(1);
     }
     pstmt.close();
   }
 
   /** Clean up resources used by scenario. */
   public void cleanup() throws Exception
   {
     // No work to be done here. 
   }
 }
