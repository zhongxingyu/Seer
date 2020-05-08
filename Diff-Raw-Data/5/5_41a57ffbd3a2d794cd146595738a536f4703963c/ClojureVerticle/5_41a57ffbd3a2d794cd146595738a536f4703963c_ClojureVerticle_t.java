 /*
  * Copyright 2011-2012 the original author or authors.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package org.vertx.java.platform.impl;
 
 import org.vertx.java.core.logging.Logger;
 import org.vertx.java.core.logging.impl.LoggerFactory;
 import org.vertx.java.platform.Verticle;
 
 import clojure.lang.RT;
 
 public class ClojureVerticle
         extends Verticle
 {
     private static final Logger log = LoggerFactory.getLogger(ClojureVerticle.class);
 
     private final ClassLoader cl;
     private final String scriptName;
 
     ClojureVerticle(String scriptName, ClassLoader cl) {
         this.cl = cl;
         this.scriptName = scriptName;
     }
 
 	public void start() throws Exception {
 		log.info("Starting clojure verticle: " + scriptName);
 
 		//tell RT to use cl as classloader, otherwise it will use Thread cl
        // call RT.load() to avoid NPE during on Clojure 1.5.x
        // see  http://dev.clojure.org/jira/browse/CLJ-1172
        //http://stackoverflow.com/questions/15207596/npe-in-clojure-lang-compiler-when-trying-to-load-a-resource

        RT.load("clojure/core");
 		
 		clojure.lang.Var.pushThreadBindings(clojure.lang.RT.map( clojure.lang.Compiler.LOADER, cl) );
 
 		RT.var("vertx.core", "vertx", getVertx());
 		RT.var("vertx.core", "container", getContainer());
 		RT.loadResourceScript(scriptName);
 		
 		log.info("Started clojure verticle: " + scriptName);
 	}
 
     public void stop() throws Exception {
 		log.info("Stop verticle: " + scriptName);
     }
 }
