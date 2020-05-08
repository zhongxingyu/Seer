 /** 
  * Copyright [2013] Antonio J. Iniesta
  *  Licensed under the Apache License, Version 2.0 (the "License");
  *  you may not use this file except in compliance with the License.
  *  You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  *  Unless required by applicable law or agreed to in writing, software
  *  distributed under the License is distributed on an "AS IS" BASIS,
  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *  See the License for the specific language governing permissions and
  *  limitations under the License.
  * 
  * File created: 03/02/2013 at 12:05:31 by antonio
  */
 package com.iniesta.editorfx.editor.files;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.InputStreamReader;
 import java.nio.charset.Charset;
 
 import javafx.concurrent.Service;
 import javafx.concurrent.Task;
 
 /**
  * @author antonio
  * This service load a file and convert it into a String to bind it to a viewer file
  */
 public class ServiceFileText extends Service<String> {
 
 	private File file;
 
 	public ServiceFileText(File file){
 		this.file = file;
 	}
 	
 	/* (non-Javadoc)
 	 * @see javafx.concurrent.Service#createTask()
 	 */
 	@Override
 	protected Task<String> createTask() {		
 		return new Task<String>() {
 			@Override
 			protected String call() throws Exception {
 				String fileText = "";
 				FileInputStream fis = null;
 				BufferedReader br = null;
 				try{
 					fis = new FileInputStream(file);
 					br = new BufferedReader(new InputStreamReader(fis, Charset.forName("UTF-8")));				
 					if(fis!=null && br!=null){
 						String line = null;
 						while((line = br.readLine())!=null){
							fileText += line;
 						}
 					}
 				} catch(Exception ex){
 					fileText = null;
 				} finally{
 					if(fis!=null){fis.close();}
 					if(br!=null){br.close();}
 				}
 				return fileText;
 			}
 		};
 	}
 
 }
