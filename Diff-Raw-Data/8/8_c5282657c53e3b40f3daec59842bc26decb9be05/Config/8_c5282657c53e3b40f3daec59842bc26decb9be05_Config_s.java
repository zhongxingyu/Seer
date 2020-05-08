 /*
  * Copyright 2012 Sebastian Koppehel
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package de.bastisoft.ogre.gui;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.nio.file.Path;
 import java.nio.file.Paths;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import javax.swing.UIManager;
 
 import de.bastisoft.ogre.gui.QueryInputs.Input;
 
 class Config {
 
     static enum LookAndFeelSetting {
         
         DEFAULT         ("<default>"),
         CROSS_PLATFORM  ("<cross-platform>"),
         SYSTEM          ("<system>"),
         CLASS           (null);
         
         final String keyword;
         private LookAndFeelSetting(String keyword) { this.keyword = keyword; }
         
     };
     
     private static final String FILENAME_HOMEDIR     = ".grokview.properties";
     private static final String FILENAME_CLASSPATH   = "state.properties";
     
     
     // Server settings
     
     private static final String PREFIX_SERVER        = "server";
     
     private static final String KEY_NAME             = "name";
     private static final String KEY_BASE_URL         = "base.url";
     private static final String KEY_PROXY_HOST       = "proxy.host";
     private static final String KEY_PROXY_PORT       = "proxy.port";
     private static final String KEY_LIMIT_PAGES      = "limit.pages";
     private static final String KEY_PAGE_LIMIT       = "page.limit";
     private static final String KEY_FETCH_LINES      = "fetch.lines";
     private static final String KEY_FETCH_LINES_LAST = "fetch.lines.last";
     
     private static final String KEY_SELECTED         = "selected";
     
     
     // Query inputs
     
     private static final String PREFIX_INPUT         = "input";
     private static final Map<Input, String> INPUT_KEYS;
     
     static {
         INPUT_KEYS = new HashMap<>();
         
         INPUT_KEYS.put(Input.QUERY,   "query");
         INPUT_KEYS.put(Input.DEFS,    "defs");
         INPUT_KEYS.put(Input.REFS,    "refs");
         INPUT_KEYS.put(Input.PATH,    "path");
         INPUT_KEYS.put(Input.HIST,    "hist");
         INPUT_KEYS.put(Input.PROJECT, "project");
     }
     
     private static final String KEY_PROJECT_LIST     = "project.recent";
     private static final String KEY_PROJECT_ENTRY    = "entry";
     
     
     // Frame state
     
     private static final String PREFIX_FRAME         = "frame";
     
     private static final String KEY_FRAME_X          = "x";
     private static final String KEY_FRAME_Y          = "y";
     private static final String KEY_FRAME_WIDTH      = "width";
     private static final String KEY_FRAME_HEIGHT     = "height";
     private static final String KEY_MAXIMIZED_HORIZ  = "maximized.horizontal";
     private static final String KEY_MAXIMIZED_VERT   = "maximized.vertical";
     private static final String KEY_SPLIT_POSITION   = "split.position";
     
     
     // Other settings
     
     private static final String KEY_LOOK_AND_FEEL    = "look.and.feel";
     
     
     List<Server> servers;
     Server selectedServer;
     FrameState frameState;
     LookAndFeelSetting lookAndFeelSetting;
     String lookAndFeel;
     
     Config() {
         servers = new ArrayList<>();
         servers.add(new Server(new ServerSettings("Default"), new QueryInputs()));
         frameState = null;
         
         lookAndFeelSetting = LookAndFeelSetting.DEFAULT;
         if (UIManager.getSystemLookAndFeelClassName().endsWith("windows.WindowsLookAndFeel"))
             lookAndFeelSetting = LookAndFeelSetting.SYSTEM;
         lookAndFeel = null;
     }
     
     private static Config read(PropertyMap props) {
         Config c = new Config();
         
         c.servers.clear();
         for (PropertyMap serverMap : props.submaps(PREFIX_SERVER))
             c.servers.add(new Server(readServerSettings(serverMap), readInputs(serverMap.submap(PREFIX_INPUT))));
         
         int selectedIndex = props.submap(PREFIX_SERVER).getInt(KEY_SELECTED, -1);
         if (selectedIndex >= 0 && selectedIndex < c.servers.size())
             c.selectedServer = c.servers.get(selectedIndex);
         
         PropertyMap guiProps = props.submap(PREFIX_FRAME);
         FrameState frameState = readFrameState(guiProps);
         if (frameState != null)
             c.frameState = frameState;
         
         c.lookAndFeelSetting = LookAndFeelSetting.CLASS;
         c.lookAndFeel = props.get(KEY_LOOK_AND_FEEL, LookAndFeelSetting.DEFAULT.keyword);
         
         for (LookAndFeelSetting setting : LookAndFeelSetting.values())
             if (setting.keyword != null && setting.keyword.equalsIgnoreCase(c.lookAndFeel)) {
                 c.lookAndFeelSetting = setting;
                 break;
             }
         
         return c;
     }
     
     private static ServerSettings readServerSettings(PropertyMap props) {
         String name = props.get(KEY_NAME, "");
         String baseURL = props.get(KEY_BASE_URL, "");
         String proxyHost = props.get(KEY_PROXY_HOST, "");
         int proxyPort = props.getInt(KEY_PROXY_PORT, 8080);
         boolean limitPages = props.getBool(KEY_LIMIT_PAGES, true);
         int pageLimit = props.getInt(KEY_PAGE_LIMIT, 15);
         boolean fetchLines = props.getBool(KEY_FETCH_LINES, true);
         boolean fetchLinesLast = props.getBool(KEY_FETCH_LINES_LAST, true);
         
         return new ServerSettings(name, baseURL, proxyHost, proxyPort, limitPages, pageLimit, fetchLines, fetchLinesLast);
     }
     
     private static QueryInputs readInputs(PropertyMap props) {
         QueryInputs inputs = new QueryInputs();
         
         for (Input input : Input.values()) {
             String key = INPUT_KEYS.get(input);
             if (key != null)
                 inputs.setInput(input, props.get(key));
         }
         
         List<PropertyMap> projectHistory = props.submaps(KEY_PROJECT_LIST);
         List<String> projects = new ArrayList<>();
         for (PropertyMap map : projectHistory) {
             String prj = map.get(KEY_PROJECT_ENTRY);
            System.out.printf("prj: ", prj);
             if (prj != null)
                 projects.add(prj);
         }
         
         inputs.setProjects(projects);
         
         return inputs;
     }
     
     private static FrameState readFrameState(PropertyMap props) {
         int x = props.getInt(KEY_FRAME_X, -1);
         int y = props.getInt(KEY_FRAME_Y, -1);
         int width = props.getInt(KEY_FRAME_WIDTH, 0);
         int height = props.getInt(KEY_FRAME_HEIGHT, 0);
         boolean maxHoriz = props.getBool(KEY_MAXIMIZED_HORIZ, false);
         boolean maxVert = props.getBool(KEY_MAXIMIZED_VERT, false);
         int splitPosition = props.getInt(KEY_SPLIT_POSITION, -1);
         
         if (x >= 0 && y >= 0 && width > 0 && height > 0)
             return new FrameState(x, y, width, height, maxHoriz, maxVert, splitPosition);
         
         return null;
     }
     
     private PropertyMap write() {
         PropertyMap props = new PropertyMap();
         
         for (int i = 0; i < servers.size(); i++) {
             Server server = servers.get(i);
             PropertyMap serverProps = new PropertyMap();
             
             serverProps.set(KEY_NAME, server.serverSettings.name);
             serverProps.set(KEY_BASE_URL, server.serverSettings.baseURL);
             serverProps.set(KEY_PROXY_HOST, server.serverSettings.proxyHost);
             serverProps.set(KEY_PROXY_PORT, server.serverSettings.proxyPort);
             
             serverProps.set(KEY_LIMIT_PAGES, server.serverSettings.limitPages);
             serverProps.set(KEY_PAGE_LIMIT, server.serverSettings.pageLimit);
             serverProps.set(KEY_FETCH_LINES, server.serverSettings.fetchLines);
             serverProps.set(KEY_FETCH_LINES_LAST, server.serverSettings.fetchLinesLast);
             
             PropertyMap inputProps = new PropertyMap();
             for (Input input : Input.values()) {
                 String key = INPUT_KEYS.get(input);
                 if (key != null)
                     inputProps.set(key, server.queryInputs.getInput(input));
             }
             
             int j = 0;
             for (Iterator<String> it = server.queryInputs.getProjects().iterator(); it.hasNext(); j++) {
                 PropertyMap map = new PropertyMap();
                 map.set(KEY_PROJECT_ENTRY, it.next());
                 inputProps.addIndexed(map, KEY_PROJECT_LIST, j);
             }
             
             serverProps.add(inputProps, PREFIX_INPUT);
             props.addIndexed(serverProps, PREFIX_SERVER, i);
         }
         
         if (selectedServer != null) {
             int selectedIndex = servers.indexOf(selectedServer);
             if (selectedIndex > -1) {
                 PropertyMap serverProps = new PropertyMap();
                 serverProps.set(KEY_SELECTED, selectedIndex);
                 props.add(serverProps, PREFIX_SERVER);
             }
         }
         
         PropertyMap guiProps = new PropertyMap();
         guiProps.set(KEY_FRAME_X, frameState.x);
         guiProps.set(KEY_FRAME_Y, frameState.y);
         guiProps.set(KEY_FRAME_WIDTH, frameState.width);
         guiProps.set(KEY_FRAME_HEIGHT, frameState.height);
         guiProps.set(KEY_MAXIMIZED_HORIZ, frameState.maximizedHorizontal);
         guiProps.set(KEY_MAXIMIZED_VERT, frameState.maximizedVertical);
         guiProps.set(KEY_SPLIT_POSITION, frameState.splitPosition);
         props.add(guiProps, PREFIX_FRAME);
         
         switch (lookAndFeelSetting) {
         case DEFAULT:
             props.set(KEY_LOOK_AND_FEEL, "<default>");
             break;
         case SYSTEM:
             props.set(KEY_LOOK_AND_FEEL, "<system>");
             break;
         default:
             props.set(KEY_LOOK_AND_FEEL, lookAndFeel);
         }
         
         return props;
     }
     
     static Config readConfig() {
         Config config = loadFromHomeDir();
         if (config != null)
             return config;
         
         config = loadFromClasspath();
         if (config != null)
             return config;
         
         return new Config();
     }
     
     private static Config loadFromHomeDir() {
         try {
             return read(PropertyMap.load(getConfigFile()));
         }
         catch (IOException e) {
             e.printStackTrace();
             return null;
         }
     }
     
     private static Config loadFromClasspath() {
         try (InputStream stream = Config.class.getResourceAsStream(FILENAME_CLASSPATH)) {
             if (stream != null)
                 return read(PropertyMap.load(stream));
         }
         catch (IOException e) {
             e.printStackTrace();
         }
         
         return null;
     }
     
     static void saveConfig(Config config) {
         PropertyMap props = config.write();
         try {
             props.save(getConfigFile());
         }
         catch (IOException e) {
             e.printStackTrace();
         }
     }
 
     private static Path getConfigFile() {
         Path homeDir = Paths.get(System.getProperty("user.home"));
         return homeDir.resolve(FILENAME_HOMEDIR);
     }
 
 }
