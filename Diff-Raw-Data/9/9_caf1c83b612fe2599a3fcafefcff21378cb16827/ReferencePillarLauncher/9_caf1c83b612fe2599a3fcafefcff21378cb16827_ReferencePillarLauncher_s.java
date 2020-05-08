 /*
  * #%L
  * Bitmagasin integrationstest
  * 
  * $Id: ReferencePillarLauncher.java 685 2012-01-06 16:35:17Z jolf $
  * $HeadURL: https://sbforge.org/svn/bitrepository/bitrepository-reference/trunk/bitrepository-reference-pillar/src/main/java/org/bitrepository/pillar/ReferencePillarLauncher.java $
  * %%
  * Copyright (C) 2010 The State and University Library, The Royal Library and The State Archives, Denmark
  * %%
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License as 
  * published by the Free Software Foundation, either version 2.1 of the 
  * License, or (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Lesser Public License for more details.
  * 
  * You should have received a copy of the GNU General Lesser Public 
  * License along with this program.  If not, see
  * <http://www.gnu.org/licenses/lgpl-2.1.html>.
  * #L%
  */
 package org.bitrepository.pillar.referencepillar;
 
 import org.bitrepository.common.settings.XMLFileSettingsLoader;
 import org.bitrepository.pillar.Pillar;
 import org.bitrepository.pillar.PillarComponentFactory;
 import org.bitrepository.pillar.PillarRunner;
 
 /**
  * Method for launching the ReferencePillar. 
  * It just loads the configurations and uses them to create the PillarSettings needed for starting the ReferencePillar.
  */
 public final class ReferencePillarLauncher {
     /**
      * Private constructor. To prevent instantiation of this utility class.
      */
     private ReferencePillarLauncher() { }
     
     /**
      * @param args <ol>
      * <li> The path to the directory containing the settings. See {@link XMLFileSettingsLoader} for details.</li>
      * <li> The path to the private key file with the certificates for communication.</li>
      * <li> The pillar's componentID.</li>
      * </ol>
      */
     public static void main(String[] args) {
         String pathToSettings = args[0];
        String pathToKeyFile = args[1];
         String pillarID =  args.length == 3 ? pillarID = args[2]: null;
         Pillar fullPillar =
                 PillarComponentFactory.getInstance().createReferencePillar(pathToSettings, pathToKeyFile, pillarID);
         PillarRunner.launchPillar(fullPillar);
     }
 }
