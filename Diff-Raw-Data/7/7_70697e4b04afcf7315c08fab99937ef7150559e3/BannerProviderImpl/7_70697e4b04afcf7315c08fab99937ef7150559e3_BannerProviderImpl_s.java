 /**
  * Elastic Grid
  * Copyright (C) 2008-2009 Elastic Grid, LLC.
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Affero General Public License as
  * published by the Free Software Foundation, either version 3 of the
  * License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Affero General Public License for more details.
  *
  * You should have received a copy of the GNU Affero General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package com.elasticgrid.boot;
 
 import org.rioproject.util.BannerProvider;
 
 /**
  * Banner provider.
  *
  * @author Jerome Bernard
  */
 public class BannerProviderImpl implements BannerProvider {
     public String getBanner(String service) {
         StringBuffer banner = new StringBuffer();
         banner.append("\n");
         banner.append("____ _    ____ ____ ___ _ ____    ____ ____ _ ___\n");
        banner.append("|___ |    |__| [__   |  | |       | __ |__/ | |  \\  "+ service + "\n");
        banner.append("|___ |___ |  | ___]  |  | |___    |__] |  \\ | |__/  Version: ${pom.version}\n");                                                            
         banner.append("\n");
        banner.append("Elastic Grid Home: " + System.getProperty("EG_HOME"));
         return banner.toString();
     }
 }
