 /* Copyright (c) 2013 OpenPlans. All rights reserved.
  * This code is licensed under the BSD New License, available at the root
  * application directory.
  */
 package org.geogit.storage.neo4j;
 
 import org.geogit.storage.GraphDatabase;
 
 import com.google.inject.AbstractModule;
 
 public class Neo4JModule extends AbstractModule {

     @Override
     protected void configure() {
        bind(GraphDatabase.class).to(Neo4JGraphDatabase.class);
     }

 }
