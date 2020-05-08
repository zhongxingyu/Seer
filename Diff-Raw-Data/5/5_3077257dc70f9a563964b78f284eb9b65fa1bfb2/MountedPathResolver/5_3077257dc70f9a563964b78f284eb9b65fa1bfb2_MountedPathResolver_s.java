 /***************************************************************************
 *                                                                          *
 *  Organization: Earth System Grid Federation                              *
 *                                                                          *
 ****************************************************************************
 *                                                                          *
 *   Copyright (c) 2009, Lawrence Livermore National Security, LLC.         *
 *   Produced at the Lawrence Livermore National Laboratory                 *
 *   Written by: Gavin M. Bell (gavin@llnl.gov)                             *
 *   LLNL-CODE-420962                                                       *
 *                                                                          *
 *   All rights reserved. This file is part of the:                         *
 *   Earth System Grid Federation (ESGF) Data Node Software Stack           *
 *                                                                          *
 *   For details, see http://esgf.org/                                      *
 *   Please also read this link                                             *
 *    http://esgf.org/LICENSE                                               *
 *                                                                          *
 *   * Redistribution and use in source and binary forms, with or           *
 *   without modification, are permitted provided that the following        *
 *   conditions are met:                                                    *
 *                                                                          *
 *   * Redistributions of source code must retain the above copyright       *
 *   notice, this list of conditions and the disclaimer below.              *
 *                                                                          *
 *   * Redistributions in binary form must reproduce the above copyright    *
 *   notice, this list of conditions and the disclaimer (as noted below)    *
 *   in the documentation and/or other materials provided with the          *
 *   distribution.                                                          *
 *                                                                          *
 *   Neither the name of the LLNS/LLNL nor the names of its contributors    *
 *   may be used to endorse or promote products derived from this           *
 *   software without specific prior written permission.                    *
 *                                                                          *
 *   THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS    *
 *   "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT      *
 *   LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS      *
 *   FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL LAWRENCE    *
 *   LIVERMORE NATIONAL SECURITY, LLC, THE U.S. DEPARTMENT OF ENERGY OR     *
 *   CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,           *
 *   SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT       *
 *   LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF       *
 *   USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND    *
 *   ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,     *
 *   OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT     *
 *   OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF     *
 *   SUCH DAMAGE.                                                           *
 *                                                                          *
 ***************************************************************************/
 
 /**
    Description:
 
 **/
 package esg.node.filters;
 
 import java.util.regex.Pattern;
 import java.util.regex.Matcher;
 import java.util.List;
 import java.util.ArrayList;
 import java.util.Map;
 import java.util.TreeSet;
 import java.util.Comparator;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.apache.commons.logging.impl.*;
 
 public class MountedPathResolver implements esg.common.Resolver {
     
     
     private List<MountPoint> mountPoints = null;
 
     public MountedPathResolver() {
         this.mountPoints = new ArrayList<MountPoint>(5);
     }
 
     public MountedPathResolver(Map<String,String> readMountpoints) {
         this();
         addMountPoints(readMountpoints);
     }
 
     //Up-front loading, getting data into the resolver
     //adding an unordered mountpoint map
     public void addMountPoints(Map<String,String> readMountpoints) {
         this.mountPoints.clear();
 
         Comparator<? super String> stringLengthComparator = new Comparator<String>() {
             public int compare(String o1, String o2) {
                if(o1.length() > o2.length())      { return -1; }
                else if(o1.length() < o2.length()) { return  1;}
                else { return  0; }
             }
         };
                 
         //Just sort the keys in length order.
         TreeSet<String> rmpSorted = new TreeSet<String>(stringLengthComparator);
         rmpSorted.addAll(readMountpoints.keySet());
         
         for(String key : rmpSorted) {
             addMountPoint(key,readMountpoints.get(key));
         }
     }
     
     private synchronized void addMountPoint(String mountpoint, String localpath) {
         System.out.println("Adding mountpoint: "+mountpoint+" --> "+localpath);
         this.mountPoints.add(new MountPoint(Pattern.compile("/"+mountpoint+"/(.*$)").matcher(""),localpath));
     }
     
     public String resolve(String input) {
         String out = null;
         System.out.println("Resolving "+input);
         System.out.println("Scanning over ["+mountPoints.size()+"] mounts");
         for(MountPoint mp : mountPoints) {
             mp.mountmatcher.reset(input);
             if(mp.mountmatcher.find()) {
                 System.out.print("+");
                 out = mp.localpath+java.io.File.separator+mp.mountmatcher.group(1);
                 break;
             }
             System.out.print("-");
         }
         System.out.println("Resolved to local path: ["+out+"]");
         return out;
     }
 
     private class MountPoint {
         Matcher mountmatcher = null;
         String localpath = null;
         MountPoint(Matcher mountmatcher, String localpath) {
             this.mountmatcher = mountmatcher;
             this.localpath = localpath;
         }
     }
 
     public String toString() { return mountPoints.toString(); }
 }
