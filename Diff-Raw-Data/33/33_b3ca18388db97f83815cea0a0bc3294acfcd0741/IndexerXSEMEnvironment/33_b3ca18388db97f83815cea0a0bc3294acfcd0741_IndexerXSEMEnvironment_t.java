 /*
   This file is part of opensearch.
   Copyright © 2009, Dansk Bibliotekscenter a/s,
   Tempovej 7-11, DK-2750 Ballerup, Denmark. CVR: 15149043
 
   opensearch is free software: you can redistribute it and/or modify
   it under the terms of the GNU General Public License as published by
   the Free Software Foundation, either version 3 of the License, or
   (at your option) any later version.
 
   opensearch is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU General Public License for more details.
 
   You should have received a copy of the GNU General Public License
   along with opensearch.  If not, see <http://www.gnu.org/licenses/>.
 */
 
 /**
  * \file
  * \brief
  */
 
 
 package dk.dbc.opensearch.plugins;
 
 
import dk.dbc.opensearch.common.fedora.IObjectRepository;
 // import dk.dbc.opensearch.common.fedora.ObjectRepositoryException;
 // import dk.dbc.opensearch.common.metadata.DBCBIB;
 // import dk.dbc.opensearch.common.metadata.DublinCore;
 // import dk.dbc.opensearch.common.metadata.IPredicate;
import dk.dbc.opensearch.common.pluginframework.IPluginEnvironment;
import dk.dbc.opensearch.common.pluginframework.PluginException;
import dk.dbc.opensearch.common.types.CargoContainer;
 // import dk.dbc.opensearch.common.types.InputPair;
 // import dk.dbc.opensearch.common.types.ObjectIdentifier;
 
 // import java.io.IOException;
 // import java.net.MalformedURLException;
 // import java.util.HashMap;
 // import java.util.List;
import java.util.Map;
 // import javax.xml.rpc.ServiceException;
 
 // import org.apache.commons.configuration.ConfigurationException;
import org.apache.log4j.Logger;
 
 
 public class IndexerXSEMEnvironment implements IPluginEnvironment
 {
 
     private static Logger log = Logger.getLogger( IndexerXSEMEnvironment.class );
 
     //    private IObjectRepository objectRepository;
 
     public IndexerXSEMEnvironment( IObjectRepository repository, Map<String, String> args ) throws PluginException
     {
 	//        this.objectRepository = repository;
     }
 
 }
