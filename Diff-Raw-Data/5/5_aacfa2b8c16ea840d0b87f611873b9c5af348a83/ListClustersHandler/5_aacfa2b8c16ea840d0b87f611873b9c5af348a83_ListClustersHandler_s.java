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
 
 package com.elasticgrid.tools.cli;
 
 import com.elasticgrid.model.Cluster;
 import com.elasticgrid.model.ClusterNotFoundException;
 import org.rioproject.tools.cli.OptionHandler;
 import java.io.BufferedReader;
 import java.io.PrintStream;
import java.util.Collection;
 
 public class ListClustersHandler extends AbstractHandler implements OptionHandler {
 
     /**
      * Process the option.
      *
      * @param input Parameters for the option, may be null
      * @param br An optional BufferdReader, used if the option requires input.
      * if this is null, the option handler may create a BufferedReader to handle the input
      * @param out The PrintStream to use if the option prints results or
      * choices for the user. Must not be null
      *
      * @return The result of the action.
      */
     public String process(String input, BufferedReader br, PrintStream out) {
         try {
            Collection<Cluster> clusters = getClusterManager().findClusters();
             System.out.println("Found " + clusters.size() + " clusters");
             Formatter.printClusters(clusters, br, out);
             return "";
         } catch (ClusterNotFoundException e) {
             return "cluster not found!";
         } catch (Exception e) {
             e.printStackTrace(out);
             return "unexpected cluster exception";
         }
     }
 
     /**
      * Get the usage of the command
      *
      * @return Command usage
      */
     public String getUsage() {
         return("usage: list-clusters\n");
     }
 
 }
