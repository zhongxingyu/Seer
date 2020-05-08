 /**
  * Copyright (c) 2010-2011 Martin M Reed
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public License
  * along with this program. If not, see <http://www.gnu.org/licenses/>.
  */
 package org.hardisonbrewing.maven.cxx.xcode;
 
 import java.io.File;
 
 public class TargetDirectoryService extends org.hardisonbrewing.maven.core.TargetDirectoryService {
 
     protected TargetDirectoryService() {
 
         // do nothing
     }
 
     public static final String getConfigBuildDirPath( String target ) {
 
         StringBuffer stringBuffer = new StringBuffer();
         stringBuffer.append( getTargetBuildDirPath( target ) );
         stringBuffer.append( File.separator );
         stringBuffer.append( XCodeService.getConfiguration( target ) );
         return stringBuffer.toString();
     }
 
     public static final String getTargetBuildDirPath( String target ) {
 
         String scheme = XCodeService.getScheme();
 
         StringBuffer stringBuffer = new StringBuffer();
         stringBuffer.append( getTargetDirectoryPath() );
         if (scheme != null) {
             stringBuffer.append( File.separator );
             stringBuffer.append( scheme );
         }
         else {
             stringBuffer.append( File.separator );
             stringBuffer.append( target );
         }
         return stringBuffer.toString();
     }
 
     public static final String getTempPackagePath( String target ) {
 
         StringBuffer stringBuffer = new StringBuffer();
         stringBuffer.append( getTempPackagePath() );
         stringBuffer.append( File.separator );
         stringBuffer.append( target );
         return stringBuffer.toString();
     }
 }
