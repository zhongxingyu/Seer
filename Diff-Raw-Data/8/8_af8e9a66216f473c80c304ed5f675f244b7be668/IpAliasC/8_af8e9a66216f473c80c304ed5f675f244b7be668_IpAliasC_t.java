 /**
  * This file is part of Orion source code
  * 
  * Copyright (C) 2012 [Gore]Clan - http://www.goreclan.net
  *
  * Orion is free software; you can redistribute it and/or
  * modify it under the terms of the GNU General Lesser Public
  * License as published by the Free Software Foundation; either
  * version 2 of the License, or (at your option) any later version.
  *
  * This software is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with Orion. If not, write to the Free Software Foundation,
  * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA
  * 
  * @author      Daniele Pantaleone
  * @version     1.0
  * @copyright   Daniele Pantaleone, 30 January, 2013
  * @package     com.orion.control
  **/
 
 package com.orion.control;
 
 import java.net.UnknownHostException;
 import java.sql.SQLException;
 import java.util.List;
 
 import org.apache.commons.logging.Log;
 import org.joda.time.DateTime;
 import org.joda.time.DateTimeZone;
 
 import com.orion.bot.Orion;
 import com.orion.dao.IpAliasDao;
 import com.orion.dao.MySqlIpAliasDao;
 import com.orion.domain.Client;
 import com.orion.domain.IpAlias;
 
 public class IpAliasC {
         
     private final Log log;
     private final DateTimeZone timezone;
     private final IpAliasDao dao;
     
     
     /**
      * Object constructor.
      * 
      * @author Daniele Pantaleone
      * @param  orion <tt>Orion</tt> object reference
      **/
     public IpAliasC(Orion orion) {
         this.log = orion.log;
         this.timezone = orion.timezone;
         this.dao = new MySqlIpAliasDao(orion);
     }
     
     
     /**
      * Return a collection of <tt>IpAlias</tt> objects matching the given <tt>Client</tt>
      * 
      * @author Daniele Pantaleone
      * @param  client The <tt>Client</tt> object on which to perform the search
      * @throws ClassNotFoundException If the JDBC driver fails in being loaded
      * @throws SQLException If the load query fails somehow
      * @throws UnknownHostException If we can't generate an <tt>InetAddress</tt> object using a <tt>Client</tt> IP address
      * @return A collection of </tt>IpAlias</tt> objects matching the given <tt>Client</tt>
      **/
     public List<IpAlias> getByClient(Client client) throws ClassNotFoundException, SQLException, UnknownHostException {
         return this.dao.loadByClient(client);
     }
     
     
     /**
      * Return an <tt>IpAlias</tt> object matching the given <tt>Client</tt> id and IP address
      * 
      * @author Daniele Pantaleone
      * @param  client The <tt>Client</tt> object on which to perform the search
      * @throws ClassNotFoundException If the JDBC driver fails in being loaded
      * @throws SQLException If the load query fails somehow
      * @throws UnknownHostException If we can't generate an <tt>InetAddress</tt> object using the <tt>Client</tt> IP address
      * @return An <tt>IpAlias</tt> object matching the given <tt>Client</tt> id and IP address or <tt>null</tt> if we have no match
      **/
     public IpAlias getByClientIp(Client client) throws ClassNotFoundException, SQLException, UnknownHostException {
         return this.dao.loadByClientIp(client);
     }
     
     
     /**
      * Create a new entry in the database for the current object
      * 
      * @author Daniele Pantaleone
      * @param  ipalias The <tt>IpAlias</tt> object whose informations needs to be stored
      * @throws ClassNotFoundException If the JDBC driver fails in being loaded
      * @throws SQLException If the insert query fails somehow
      **/
     public void insert(IpAlias ipalias) throws ClassNotFoundException, SQLException {
         
        if (!ipalias.getClient().isBot()) {
             DateTime date = new DateTime(this.timezone);
             ipalias.time_add  = date;
             ipalias.time_edit = date;
             this.log.trace("[SQL] INSERT `ipaliases`: " + ipalias.toString());
             this.dao.insert(ipalias);
         }
         
     }
     
     
     /**
      * Update domain object in the database
      * 
      * @author Daniele Pantaleone
      * @param  ipalias The <tt>IpAlias</tt> object whose informations needs to be updated
      * @throws ClassNotFoundException If the JDBC driver fails in being loaded
      * @throws SQLException If the update query fails somehow
      **/
     public void update(IpAlias ipalias) throws ClassNotFoundException, SQLException {
         
        if (!ipalias.getClient().isBot()) {
             ipalias.time_edit = new DateTime(this.timezone);
             this.log.trace("[SQL] UPDATE `ipaliases`: " + ipalias.toString());
             this.dao.update(ipalias);
         }
         
     }
     
     
     /**
      * Delete domain object from the database.
      * 
      * @author Daniele Pantaleone
      * @param  ipalias The <tt>IpAlias</tt> object whose informations needs to be deleted
      * @throws ClassNotFoundException If the JDBC driver fails in being loaded
      * @throws SQLException If the delete query fails somehow
      **/
     public void delete(IpAlias ipalias) throws ClassNotFoundException, SQLException {
         
        if (!ipalias.getClient().isBot()) {
             this.log.trace("[SQL] DELETE `ipaliases`: " + ipalias.toString());
             this.dao.delete(ipalias);
         }
         
     }
     
     
     /**
      * Save the <tt>IpAlias</tt> object in the database
      *
      * @author Daniele Pantaleone
      * @param  alias The <tt>IpAlias</tt> object to be saved in the database
      * @throws ClassNotFoundException If the JDBC driver fails in being loaded
      * @throws SQLException If the load query fails somehow
      **/
     public void save(IpAlias ipalias) throws ClassNotFoundException, SQLException { 
         if (ipalias.id > 0) { this.update(ipalias); } 
         else { this.insert(ipalias); }
     }
     
 }
