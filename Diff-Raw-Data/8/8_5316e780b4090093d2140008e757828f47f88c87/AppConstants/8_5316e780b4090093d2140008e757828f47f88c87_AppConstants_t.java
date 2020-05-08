 /*
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License. 
  */
 package net.rptools.maptool.client;
 
 import java.io.File;
 import java.io.FilenameFilter;
 
 import net.rptools.lib.swing.ImageBorder;
 import net.rptools.maptool.model.Token;
 import net.tsc.servicediscovery.ServiceGroup;
 
 public class AppConstants {
 
 	public static final String APP_NAME = "MapTool";
 
 	public static final File UNZIP_DIR = AppUtil.getAppHome("resource");
 
 	public static final ServiceGroup SERVICE_GROUP = new ServiceGroup("maptool");
 
 	public static final ImageBorder GRAY_BORDER = new ImageBorder("net/rptools/maptool/client/image/border/gray");
 	public static final ImageBorder SHADOW_BORDER = new ImageBorder("net/rptools/maptool/client/image/border/shadow");
 	public static final ImageBorder HIGHLIGHT_BORDER = new ImageBorder("net/rptools/maptool/client/image/border/highlight");
 
 	public static final int NOTE_PORTRAIT_SIZE = 200;
 
 	public static final FilenameFilter IMAGE_FILE_FILTER = new FilenameFilter() {
 		public boolean accept(File dir, String name) {
 			name = name.toLowerCase();
 			return name.endsWith(".bmp") || 
 					name.endsWith(".png") || 
 					name.endsWith(".jpg") || 
 					name.endsWith(".jpeg") || 
 					name.endsWith(".gif") ||
 
 					// RPTools Token format
 					name.endsWith(Token.FILE_EXTENSION);
 		}
 	};
 
 	public static final String CAMPAIGN_FILE_EXTENSION = ".cmpgn";
	public static final String CAMPAIGN_PROPERTIES_FILE_EXTENSION = ".mtprops";
 	public static final String MAP_FILE_EXTENSION = ".rpmap";
 	public static final String MACRO_FILE_EXTENSION = ".mtmacro";
 	public static final String MACROSET_FILE_EXTENSION = ".mtmacset";
 }
