 /*
  * Copyright (c) 2008, Muhd Kamil Mohd Baki. All Rights Reserved.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.qi4j.chronos.ui.wicket.bootstrap.assembler.service;
 
 import org.qi4j.bootstrap.ApplicationAssembly;
 import org.qi4j.bootstrap.AssemblyException;
 import org.qi4j.bootstrap.LayerAssembly;
 import org.qi4j.bootstrap.ModuleAssembly;
 import org.qi4j.chronos.assembler.LayerAssembler;
 import org.qi4j.chronos.service.AccountService;
 import org.qi4j.chronos.service.UserService;
 import static org.qi4j.structure.Visibility.application;
 
 /**
  * TODO: Remove this
  */
 public final class ServiceLayerAssembler
     implements LayerAssembler
 {
     public static final String LAYER_SERVICE = "service";
     public static final String MODULE_SERVICE = "service";
 
     public final LayerAssembly createLayerAssembly( ApplicationAssembly anApplicationAssembly )
         throws AssemblyException
     {
         LayerAssembly serviceLayer = anApplicationAssembly.newLayerAssembly( LAYER_SERVICE );
 
         ModuleAssembly service = serviceLayer.newModuleAssembly( MODULE_SERVICE );
         service.addServices(
             AccountService.class,
             UserService.class
         ).visibleIn( application );
 
         return serviceLayer;
     }
 }
