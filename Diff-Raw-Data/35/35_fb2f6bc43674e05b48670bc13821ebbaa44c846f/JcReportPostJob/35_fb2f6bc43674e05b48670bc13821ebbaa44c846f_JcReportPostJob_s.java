 /*
  * $Id: AbstractReportReader.java 1011 2008-06-16 17:57:36Z amandel $
  *
  * Copyright 2006, The jCoderZ.org Project. All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are
  * met:
  *
  *    * Redistributions of source code must retain the above copyright
  *      notice, this list of conditions and the following disclaimer.
  *    * Redistributions in binary form must reproduce the above
  *      copyright notice, this list of conditions and the following
  *      disclaimer in the documentation and/or other materials
  *      provided with the distribution.
  *    * Neither the name of the jCoderZ.org Project nor the names of
  *      its contributors may be used to endorse or promote products
  *      derived from this software without specific prior written
  *      permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS "AS IS" AND
  * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
  * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
  * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS
  * BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
  * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
  * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR
  * BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
  * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
  * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
  * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 package org.jcoderz.phoenix.sonar;
 
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.sonar.api.Properties;
 import org.sonar.api.Property;
 import org.sonar.api.batch.PostJob;
 import org.sonar.api.batch.SensorContext;
 import org.sonar.api.config.Settings;
import org.sonar.api.resources.Language;
import org.sonar.api.resources.Languages;
 import org.sonar.api.resources.Project;
 
 
 /**
  * @author Andreas Mandel
  */
 @Properties(
     @Property(key="my.property", name="My property", defaultValue="20")
 )
 public class JcReportPostJob
     implements PostJob
 {
     private static final String FILTER_ALL_LANGUAGES = "*";
     public static final Logger LOGGER = LoggerFactory.getLogger(JcReportPostJob.class.getName());
 
     
     public JcReportPostJob(Settings settings)
     {
         
     }
     
     public void executeOn (Project prj, SensorContext ctx)
     {
         // 1) Read Sonar findings & generate jcReport.xml
         
         prj.getFileSystem().getSonarWorkingDirectory();
         
         prj.getFileSystem().mainFiles(FILTER_ALL_LANGUAGES);
         
         
         
         // 2) Merge historic jc report if available
         // 3) Create HTML Report
         
         // TODO Auto-generated method stub
     }
 }
