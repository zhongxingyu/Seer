 package com.soebes.aether.interpolator.internal;
 
 /*******************************************************************************
  * Copyright (c) 2010-2011 Sonatype, Inc.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * and Apache License v2.0 which accompanies this distribution.
  * The Eclipse Public License is available at
  *   http://www.eclipse.org/legal/epl-v10.html
  * The Apache License v2.0 is available at
  *   http://www.apache.org/licenses/LICENSE-2.0.html
  * You may elect to redistribute this code under either of these licenses.
  *******************************************************************************/
 
 import org.apache.maven.repository.internal.MavenRepositorySystemSession;
 import org.sonatype.aether.RepositorySystem;
 import org.sonatype.aether.RepositorySystemSession;
 import org.sonatype.aether.repository.LocalRepository;
 
 /**
  * A helper to boot the repository system and a repository system session.
  */
 public class Booter
 {
 //FIXME: Think about using Guice instead of hard coded factories!
     public static RepositorySystem newRepositorySystem()
     {
         return ManualRepositorySystemFactory.newRepositorySystem();
     }
 
     public static RepositorySystemSession newRepositorySystemSession( RepositorySystem system )
     {
         MavenRepositorySystemSession session = new MavenRepositorySystemSession();
 
         //FIXME: The local repository configuration must defined by reading the settings.xml file instead of hard coding.
//        LocalRepository localRepo = new LocalRepository(org.apache.maven.repository.RepositorySystem.defaultUserLocalRepository);
        LocalRepository localRepo = new LocalRepository("target/loca-repo");
         session.setLocalRepositoryManager( system.newLocalRepositoryManager( localRepo ) );
         session.setTransferListener( new ConsoleTransferListener() );
         session.setRepositoryListener( new ConsoleRepositoryListener() );
 
         return session;
     }
 
 }
