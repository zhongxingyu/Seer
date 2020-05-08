 /**
  * Copyright 2012 Alex Jones
  *
  * Licensed to the Apache Software Foundation (ASF) under one
  * or more contributor license agreements.  See the NOTICE file
  * distributed with this work for additional information
  * regarding copyright ownership.  The ASF licenses this file
  * to you under the Apache License, Version 2.0 (the
  * "License"); you may not use this file except in compliance
  * with the License.  You may obtain a copy of the License at
  *
  *   http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an
  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  * KIND, either express or implied.  See the License for the
  * specific language governing permissions and limitations
  * under the License.    
  *
  * @author unclealex72
  *
  */
 
 package uk.co.unclealex.music.devices;
 
import java.nio.file.Paths;
 
 import javax.inject.Inject;
 
 import uk.co.unclealex.music.configuration.Directories;
 import uk.co.unclealex.music.configuration.User;
 import uk.co.unclealex.music.files.FileLocation;
 
 /**
  * The default implementation of {@link DeviceService}.
  * 
  * @author alex
  * 
  */
 public class DeviceServiceImpl implements DeviceService {
 
   /**
    * The {@link Directories} object used to find where the base device directory
    * is.
    */
   private final Directories directories;
 
   /**
    * Instantiates a new device service impl.
   * 
   * @param directories
   *          the directories
    */
   @Inject
   public DeviceServiceImpl(Directories directories) {
     super();
     this.directories = directories;
   }
 
   /**
    * {@inheritDoc}
    */
   @Override
   public FileLocation getLinkLocation(User owner, FileLocation encodedLocation) {
    return new FileLocation(getDirectories().getDevicesPath(), Paths.get(owner.getName()).resolve(
        encodedLocation.getRelativePath()), true);
   }
 
   /**
   * Gets the {@link Directories} object used to find where the base device
   * directory is.
   * 
   * @return the {@link Directories} object used to find where the base device
   *         directory is
    */
   public Directories getDirectories() {
     return directories;
   }
 }
