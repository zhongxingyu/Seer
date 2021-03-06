 /*
  * Licensed to "Neo Technology," Network Engine for Objects in Lund AB
  * (http://neotechnology.com) under one or more contributor license agreements.
  * See the NOTICE file distributed with this work for additional information
  * regarding copyright ownership. Neo Technology licenses this file to you under
  * the Apache License, Version 2.0 (the "License"); you may not use this file
  * except in compliance with the License. You may obtain a copy of the License
  * at (http://www.apache.org/licenses/LICENSE-2.0). Unless required by
  * applicable law or agreed to in writing, software distributed under the
  * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS
  * OF ANY KIND, either express or implied. See the License for the specific
  * language governing permissions and limitations under the License.
  */
 package org.neo4j.neoclipse;
 
 import org.eclipse.jface.resource.ImageDescriptor;
 import org.eclipse.jface.resource.ImageRegistry;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.ui.ISharedImages;
 import org.eclipse.ui.PlatformUI;
 
 /**
  * This class manages neo icons.
  * @author Peter H&auml;nsgen
  */
 public class NeoIcons
 {
     /*
      * Some constants representing icons.
      */
     public static final String HOME = "home";
     public static final String REFRESH = "refresh";
     public static final String ZOOM = "zoom";
     public static final String PLUS_ENABLED = "plus_enabled";
     public static final String PLUS_DISABLED = "plus_disabled";
     public static final String MINUS_ENABLED = "minus_enabled";
     public static final String MINUS_DISABLED = "minus_disabled";
     public static final String GRID = "grid";
     public static final String RADIAL = "radial";
     public static final String SPRING = "spring";
     public static final String TREE = "tree";
     public static final String NEO = "small";
     public static final String NEO_ROOT = "root";
     public static final String HELP = "help";
     /**
      * The image registry.
      */
     protected static ImageRegistry reg;
     /**
      * Shared Eclipse UI icons.
      */
     protected static ISharedImages sharedImages;
 
     /**
      * Initializes the neo images.
      */
     public static void init( Activator activator )
     {
         reg = activator.getImageRegistry();
         sharedImages = PlatformUI.getWorkbench().getSharedImages();
        // TODO use neo icons
         reg.put( NeoIcons.NEO, Activator.imageDescriptorFromPlugin(
             Activator.PLUGIN_ID, "icons/obj16/neo-16.png" ) );
//        System.out.println( "loaded one icon." );
         reg.put( NeoIcons.NEO_ROOT, Activator.imageDescriptorFromPlugin(
             Activator.PLUGIN_ID, "icons/obj16/neo-red-16.png" ) );
         // help icon
         reg.put( NeoIcons.HELP, sharedImages
             .getImage( ISharedImages.IMG_LCL_LINKTO_HELP ) );
         // misc
         reg.put( NeoIcons.HOME, Activator.imageDescriptorFromPlugin(
             Activator.PLUGIN_ID, "icons/enabled/home.gif" ) );
         reg.put( NeoIcons.REFRESH, Activator.imageDescriptorFromPlugin(
             Activator.PLUGIN_ID, "icons/enabled/refresh.gif" ) );
         reg.put( NeoIcons.ZOOM, Activator.imageDescriptorFromPlugin(
             Activator.PLUGIN_ID, "icons/enabled/zoom.gif" ) );
         // traversal depth
         reg.put( NeoIcons.PLUS_ENABLED, Activator.imageDescriptorFromPlugin(
             Activator.PLUGIN_ID, "icons/enabled/plus.gif" ) );
         reg.put( NeoIcons.PLUS_DISABLED, Activator.imageDescriptorFromPlugin(
             Activator.PLUGIN_ID, "icons/disabled/plus.gif" ) );
         reg.put( NeoIcons.MINUS_ENABLED, Activator.imageDescriptorFromPlugin(
             Activator.PLUGIN_ID, "icons/enabled/minus.gif" ) );
         reg.put( NeoIcons.MINUS_DISABLED, Activator.imageDescriptorFromPlugin(
             Activator.PLUGIN_ID, "icons/disabled/minus.gif" ) );
         // layouts
         reg.put( NeoIcons.GRID, Activator.imageDescriptorFromPlugin(
             Activator.PLUGIN_ID, "icons/enabled/grid.gif" ) );
         reg.put( NeoIcons.RADIAL, Activator.imageDescriptorFromPlugin(
             Activator.PLUGIN_ID, "icons/enabled/radial.gif" ) );
         reg.put( NeoIcons.SPRING, Activator.imageDescriptorFromPlugin(
             Activator.PLUGIN_ID, "icons/enabled/spring.gif" ) );
         reg.put( NeoIcons.TREE, Activator.imageDescriptorFromPlugin(
             Activator.PLUGIN_ID, "icons/enabled/tree.gif" ) );
     }
 
     /**
      * Looks up the image for the given name.
      */
     public static Image getImage( String name )
     {
         if ( reg == null )
         {
            init( Activator.PLUGIN );
         }
         return reg.get( name );
     }
 
     /**
      * Looks up the image descriptor for the given name.
      */
     public static ImageDescriptor getDescriptor( String name )
     {
         if ( reg == null )
         {
            init( Activator.PLUGIN );
         }
         return reg.getDescriptor( name );
     }
 }
