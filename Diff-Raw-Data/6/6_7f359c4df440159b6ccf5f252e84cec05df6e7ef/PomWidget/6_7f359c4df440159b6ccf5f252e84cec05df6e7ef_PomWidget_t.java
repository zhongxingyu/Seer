 /**
  * Copyright to the original author or authors.
  *
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in
  * compliance with the License. You may obtain a copy of the License at:
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software distributed under the License is
  * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and limitations under the License.
  */
 package fitnesse.maven.widget;
 
 import fitnesse.html.HtmlUtil;
 import fitnesse.maven.util.FileUtil;
 import fitnesse.maven.util.MavenDependencyResolver;
 import fitnesse.maven.util.MavenException;
 import fitnesse.wiki.PageData;
 import fitnesse.wikitext.WidgetBuilder;
 import fitnesse.wikitext.widgets.ClasspathWidget;
 import fitnesse.wikitext.widgets.ParentWidget;
 
 import java.io.File;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 
 public class PomWidget extends ClasspathWidget {
     protected static MavenDependencyResolver MAVEN_DEPENDENCY_RESOLVER = new MavenDependencyResolver();
     protected static ClasspathWidgetFactory CLASSPATH_WIDGET_FACTORY = new ClasspathWidgetFactory();
     protected static FileUtil FILE_UTIL = new FileUtil();
 
 
     static {
         PageData.classpathWidgetBuilder = new WidgetBuilder(new Class[]{ClasspathWidget.class, PomWidget.class});
     }
 
     public static final String REGEXP = "^!pom [^\r\n]*";
     private static final Pattern pattern = Pattern.compile("^!pom (.*)", Pattern.CASE_INSENSITIVE);
     private File pomFile;
     private String errorMessage;
 
     public PomWidget(ParentWidget parentWidget, String inputText) throws Exception {
         super(parentWidget, inputText);
 
         pomFile = new File(parsePomFile(inputText));
         if (FILE_UTIL.exists(pomFile)) {
             String pomParent = parsePomParentDir(inputText);
             CLASSPATH_WIDGET_FACTORY.build(parentWidget, pomParent + "classes");
             CLASSPATH_WIDGET_FACTORY.build(parentWidget, pomParent + "test-classes");
             try {
                 CLASSPATH_WIDGET_FACTORY.build(parentWidget, MAVEN_DEPENDENCY_RESOLVER.resolve(pomFile));
             } catch (MavenException err) {
                 errorMessage = err.getMessage();
             }
         }
     }
 
     private String parsePomFile(String input) {
         Matcher matcher = pattern.matcher(input);
         if (matcher.find()) {
             return matcher.group(1);
         }
         return null;
     }
 
     private String parsePomParentDir(String input) {
         String pomFile = parsePomFile(input);
         return pomFile.substring(0, pomFile.indexOf("pom.xml"));
     }
 
     @Override
     public String render() throws Exception {
        if (errorMessage != null) {
             return HtmlUtil.metaText("Maven POM: " + pomFile) + HtmlUtil.BRtag + "<pre>" + errorMessage + "</pre>";
         }
         if (FILE_UTIL.exists(pomFile))
             return HtmlUtil.metaText("Maven POM: " + pomFile) + HtmlUtil.BRtag;
         else
             return HtmlUtil.metaText("Maven POM could NOT be found: " + pomFile);
     }
 }
