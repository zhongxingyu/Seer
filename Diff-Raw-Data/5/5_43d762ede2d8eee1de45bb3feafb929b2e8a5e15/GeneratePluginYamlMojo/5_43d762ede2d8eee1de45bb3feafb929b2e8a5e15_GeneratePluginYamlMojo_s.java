 /**
  * 
  */
 package de.jaschastarke.bukkit.maven;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.StringWriter;
 import java.lang.reflect.Field;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Modifier;
 import java.net.URL;
 import java.net.URLClassLoader;
 import java.text.MessageFormat;
 import java.util.ArrayList;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Properties;
 
 import org.apache.maven.plugin.MojoExecutionException;
 import org.apache.maven.plugin.MojoFailureException;
 import org.bukkit.permissions.PermissionDefault;
 import org.yaml.snakeyaml.DumperOptions;
 import org.yaml.snakeyaml.Yaml;
 
 import de.jaschastarke.bukkit.lib.commands.ICommand;
 import de.jaschastarke.maven.AbstractExecMojo;
 import de.jaschastarke.minecraft.lib.permissions.IAbstractPermission;
 import de.jaschastarke.minecraft.lib.permissions.IChildPermissionContainer;
 import de.jaschastarke.minecraft.lib.permissions.IContainer;
 import de.jaschastarke.minecraft.lib.permissions.IHasDescription;
 import de.jaschastarke.minecraft.lib.permissions.IPermission;
 import de.jaschastarke.utils.ClassDescriptorStorage;
 import de.jaschastarke.utils.ClassDescriptorStorage.ClassDescription;
 import de.jaschastarke.utils.DocComment;
 import de.jaschastarke.utils.ClassHelper;
 
 /**
  * 
  * Example Usage:
  * <pre>{@code
  *    <plugin>
  *        <groupId>org.apache.maven.plugins</groupId>
  *        <artifactId>maven-compiler-plugin</artifactId>
  *        <version>2.3.2</version>
  *        <configuration>
  *            <source>1.6</source>
  *            <target>1.6</target>
  *            <annotationProcessors>
  *              <!-- Needed to fetch DocComments from Source -->
  *              <annotationProcessor>de.jaschastarke.maven.AnnotationProcessor</annotationProcessor>
  *            </annotationProcessors>
  *        </configuration>
  *    </plugin>
  *    <plugin>
  *      <groupId>de.jaschastarke</groupId>
  *      <artifactId>plib</artifactId>
  *      <version>0.1-SNAPSHOT</version>
  *      <executions>
  *        <execution>
  *          <phase>compile</phase>
  *          <goals>
  *            <goal>pluginyaml</goal>
  *          </goals>
  *          <configuration>
  *            <!-- plugin.yml -->
  *            <mainClass>de.jaschastarke.minecraft.limitedcreative.LimitedCreative</mainClass>
  *            <softdepend>
  *              <param>WorldGuard</param>
  *              <param>WorldEdit</param>
  *              <param>MultiInv</param>
  *            </softdepend>
  *            <custom>
  *              <dev-url>http://dev.bukkit.org/server-mods/limited-creative/</dev-url>
  *            </custom>
  *            <registeredPermissions>
  *              <param>de.jaschastarke.minecraft.limitedcreative.Perms:Root</param>
  *            </registeredPermissions>
  *          </configuration>
  *        </execution>
  *      </executions>
  *    </plugin>
  * }</pre>
  * @author Jascha
  * @goal pluginyaml
  * @requiresDependencyResolution compile
  */
 public class GeneratePluginYamlMojo extends AbstractExecMojo {
     private static final int FILE_WIDTH = 80;
     
     private enum Settings {
         DEFAULT("default"),
         DESCRIPTION("description"),
         PERMISSION("permission"),
         PERMISSION_MESSAGE("permission-message"),
         USAGE("usage"),
         ALIASES("aliases");
         
         private String value;
         Settings(final String t) {
             value = t;
         }
         public String toString() {
             return value;
         }
     }
     
     /**
      * @parameter default-value="${project.version}"
      */
     private String version;
     
     /**
      * @parameter default-value="${project.name}"
      */
     private String name;
     
     /**
      * @parameter
      */
     private String mainClass;
     
     /**
      * @parameter
      */
     private String[] dependencies;
     
     /**
      * @parameter
      */
     private String[] softdepend;
     
     /**
      * @parameter
      */
     private boolean database;
     
     /**
      * @parameter
      */
     private Properties custom;
     
     /**
      * @parameter
      */
     private List<String> registeredPermissions;
     
     /**
      * @parameter
      */
     private List<String> registeredCommands;
     
     
     /**
      * @parameter default-value="${project.build.outputDirectory}/META-INF"
      * @required 
      */
     private String metaDirectory;
     
     /**
      * @parameter default-value="${project.build.outputDirectory}"
      * @required 
      */
     private String targetDirectory;
 
     private ClassDescriptorStorage cds;
     
     private URLClassLoader loader;
     
     private static final int EXPECTED_GIT_HASH_LENGTH = 40;
     private static final int TRUNCATED_GIT_HASH_LENGTH = 10; // github like
     
     /* (non-Javadoc)
      * @see org.apache.maven.plugin.Mojo#execute()
      */
     @Override
     public void execute() throws MojoExecutionException, MojoFailureException {
         try {
             cds = ClassDescriptorStorage.load(new File(this.metaDirectory + "/descriptions.jos"));
         } catch (IOException e) {
             throw new MojoExecutionException("Failed to load class doccomments", e);
         }
 
         List<URL> classpathURLs = new ArrayList<URL>();
         //this.addRelevantPluginDependenciesToClasspath(classpathURLs);
         this.addRelevantProjectDependenciesToClasspath(classpathURLs);
         loader = new URLClassLoader(classpathURLs.toArray(new URL[classpathURLs.size()]), getClass().getClassLoader());
         
         /*try {
             URL url = new File(this.classes_directory).toURI().toURL();
             Method method = URLClassLoader.class.getDeclaredMethod("addURL", new Class[]{URL.class});
             method.setAccessible(true);
             method.invoke(ClassLoader.getSystemClassLoader(), new Object[]{url});
         } catch (Exception e) {
             e.printStackTrace();
         }*/
         
         String sversion = this.version;
         if (sversion.endsWith("-SNAPSHOT")) {
             boolean success = false;
             try {
                 Process p = Runtime.getRuntime().exec("git rev-parse --verify HEAD");
                 BufferedReader b = new BufferedReader(new InputStreamReader(p.getInputStream()));
                 String hash = b.readLine();
                 b.close();
                if (hash.trim().length() == EXPECTED_GIT_HASH_LENGTH) {
                     sversion += "-" + hash.substring(0, TRUNCATED_GIT_HASH_LENGTH);
                     success = true;
                 } else {
                     getLog().info("Invalid GitHash from `git rev-parse --verify HEAD`: " + hash);
                 }
             } catch (IOException e) {
                 getLog().info("Failed to read GitHash via `git rev-parse --verify HEAD`: " + e.getMessage());
             }
             if (!success) {
                 File f = new File(".git/refs/heads/master");
                 if (f.canRead()) {
                     try {
                         BufferedReader b = new BufferedReader(new FileReader(f));
                         String hash = b.readLine();
                         b.close();
                         if (hash.trim().length() == EXPECTED_GIT_HASH_LENGTH) {
                             sversion += "-" + hash.substring(0, TRUNCATED_GIT_HASH_LENGTH);
                             success = true;
                         } else {
                             getLog().info("Invalid GitHash from .git/refs/heads/master: " + hash);
                         }
                     } catch (FileNotFoundException e) {
                         getLog().info("Failed to read GitHash from .git/refs/heads/master: " + e.getMessage());
                     } catch (IOException e) {
                         getLog().info("Failed to read GitHash from .git/refs/heads/master:" + " " + e.getMessage());
                     }
                 }
             }
         }
         
         Map<String, Object> data = new LinkedHashMap<String, Object>();
         data.put("name", this.name);
         data.put("version", sversion);
         data.put("main", this.mainClass);
         if (this.database)
             data.put("database", this.database);
         if (this.dependencies != null)
            data.put("dependencies", this.dependencies);
         if (this.softdepend != null)
             data.put("softdepend", this.softdepend);
         
         if (this.custom != null) {
             for (Entry<Object, Object> property : this.custom.entrySet()) {
                 data.put((String) property.getKey(), property.getValue());
             }
         }
         if (this.registeredCommands != null) {
             data.put("commands", this.getCommands());
         }
         if (this.registeredPermissions != null) {
             data.put("permissions", this.getPermissions());
         }
         
         DumperOptions options = new DumperOptions();
         options.setWidth(FILE_WIDTH);
         options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
         Yaml yaml = new Yaml(options);
         StringWriter writer = new StringWriter();
         yaml.dump(data, writer);
         
         //getLog().info(writer.toString());
         try {
             FileWriter fw = new FileWriter(new File(this.targetDirectory + "/plugin.yml"));
             fw.write(writer.toString());
             fw.close();
         } catch (IOException e) {
             e.printStackTrace();
         }
     }
     
     private Map<String, Object> getCommands() throws MojoFailureException {
         Map<String, Object> list = new LinkedHashMap<String, Object>();
 
         for (String cls : this.registeredCommands) {
             try {
                 Object pobj;
                 try {
                     pobj = ClassHelper.getInstance(cls, loader);
                 } catch (SecurityException e) {
                     throw new MojoFailureException("SE: registeredCommand class not found: " + cls);
                 } catch (NoSuchFieldException e) {
                     throw new MojoFailureException("NSFE: registeredCommand class not found: " + cls);
                 } catch (IllegalArgumentException e) {
                     throw new MojoFailureException("IAE: registeredCommand class could not be instanciated: " + cls);
                 } catch (InvocationTargetException e) {
                     throw new MojoFailureException("ITE: registeredCommand class could not be instanciated: " + cls);
                 }
                 
                 if (pobj instanceof ICommand) {
                     ClassDescription cd = cds.getClassFor(pobj);
                     DocComment comment = cd.getDocComment();
                     Map<String, Object> command = new LinkedHashMap<String, Object>();
                     if (((ICommand) pobj).getAliases() != null)
                         command.put(Settings.ALIASES.toString(), ((ICommand) pobj).getAliases());
                     if (comment != null) {
                         String usage = comment.getAnnotationValue(Settings.USAGE.toString());
                         String permission = comment.getAnnotationValue(Settings.PERMISSION.toString());
                         String permissionMessage = comment.getAnnotationValue("permissionMessage");
                         
                         command.put(Settings.DESCRIPTION.toString(), comment.getDescription());
                         if (usage != null)
                             command.put(Settings.USAGE.toString(), usage);
                         
                         if (permission != null)
                             command.put(Settings.PERMISSION.toString(), permission);
                         if (permissionMessage != null)
                             command.put(Settings.PERMISSION_MESSAGE.toString(), permissionMessage);
                     }
                     
                     list.put(((ICommand) pobj).getName(), command);
                 }
             } catch (ClassNotFoundException e) {
                 throw new MojoFailureException("registeredCommand class not found: " + cls);
             } catch (InstantiationException e) {
                 e.printStackTrace();
                 throw new MojoFailureException("registeredCommand class couldn't instanciated: " + cls);
             } catch (IllegalAccessException e) {
                 e.printStackTrace();
                 throw new MojoFailureException("registeredCommand class couldn't instanciated: " + cls);
             }
         }
         
         return list;
     }
     
     private Map<String, Object> getPermissions() throws MojoFailureException {
         Map<String, Object> list = new LinkedHashMap<String, Object>();
         
         for (String cls : this.registeredPermissions) {
             try {
                 Object pobj;
                 try {
                     pobj = ClassHelper.getInstance(cls, loader);
                 } catch (SecurityException e) {
                     e.printStackTrace();
                     throw new MojoFailureException("SE: registeredPermission class not found: " + cls);
                 } catch (NoSuchFieldException e) {
                     e.printStackTrace();
                     throw new MojoFailureException("NSFE: registeredPermission class not found: " + cls);
                 } catch (IllegalArgumentException e) {
                     throw new MojoFailureException("IAE: registeredPermission class could not be instanciated: " + cls);
                 } catch (InvocationTargetException e) {
                     throw new MojoFailureException("ITE: registeredPermission class could not be instanciated: " + cls);
                 }
 
                 if (pobj instanceof IPermission) {
                     debug("Registered Permission is IPermission {0} <{1}>", ((IAbstractPermission) pobj).getFullString(), pobj.getClass().getName());
                     DocComment comment = cds.getClassFor(pobj).getDocComment();
                     if (comment == null)
                         addPermissionToList(list, (IPermission) pobj);
                     else
                         addPermissionToList(list, (IPermission) pobj, comment.getDescription());
                 }
                 if (pobj instanceof IContainer) {
                     debug("Registered Permission is IContainer {0}", pobj.getClass().getName());
                     addPermissionsToList(list, (IContainer) pobj);
                 }
             } catch (ClassNotFoundException e) {
                 throw new MojoFailureException("registeredPermission class not found: " + cls);
             } catch (InstantiationException e) {
                 e.printStackTrace();
             } catch (IllegalAccessException e) {
                 e.printStackTrace();
             }
         }
         
         return list;
     }
     private void addPermissionsToList(final Map<String, Object> list, final IContainer perms) {
         for (IPermission perm : perms.getPermissions()) {
             debug("  Has PermissionChild: {0} <{1}>", perm.getFullString(), perm.getClass().getName());
             DocComment dc = getContainerPropertyDoc(perms, perm);
             if (dc != null) {
                 addPermissionToList(list, perm, dc.getDescription());
             } else {
                 addPermissionToList(list, perm);
             }
         }
     }
     private void addPermissionToList(final Map<String, Object> list, final IPermission perm) {
         if (perm instanceof Enum) {
             DocComment dc = getEnumPropertyDoc((Enum<?>) perm);
             if (dc != null) {
                 addPermissionToList(list, perm, dc.getDescription());
             } else {
                 addPermissionToList(list, perm, null);
             }
         } else {
             addPermissionToList(list, perm, null);
         }
     }
     private DocComment getContainerPropertyDoc(final IContainer set, final IPermission perm) {
         ClassDescription cd = cds.getClassFor(set);
         Class<?> cls = set.getClass();
         for (Field field : cls.getFields()) {
             try {
                 if (Modifier.isPublic(field.getModifiers())) {
                     if (Modifier.isStatic(field.getModifiers()) && field.get(null) == perm) {
                         return cd.getElDocComment(field.getName());
                     } else if (field.get(set) == perm) {
                         return cd.getElDocComment(field.getName());
                     }
                 }
             } catch (IllegalArgumentException e) {
                 e.printStackTrace();
             } catch (IllegalAccessException e) {
                 e.printStackTrace();
             }
         }
         return null;
     }
     private DocComment getEnumPropertyDoc(final Enum<?> set) {
         ClassDescription cd = cds.getClassFor(set);
         Class<?> cls = set.getClass();
         for (Field field : cls.getFields()) {
             try {
                 if (field.isEnumConstant() && field.get(null) == set) {
                     return cd.getElDocComment(field.getName());
                 }
             } catch (IllegalArgumentException e) {
                 e.printStackTrace();
             } catch (IllegalAccessException e) {
                 e.printStackTrace();
             }
         }
         return null;
     }
     private void addPermissionToList(final Map<String, Object> list, final IPermission perm, final String description) {
         debug("   Describe IPermission {0}", perm.getFullString());
         Map<String, Object> data = new LinkedHashMap<String, Object>();
         if (perm.getDefault() == PermissionDefault.TRUE) {
             data.put(Settings.DEFAULT.toString(), true);
         } else if (perm.getDefault() == PermissionDefault.FALSE) {
             data.put(Settings.DEFAULT.toString(), false);
         } else {
             data.put(Settings.DEFAULT.toString(), perm.getDefault().toString());
         }
         if (perm instanceof IHasDescription) {
             data.put(Settings.DESCRIPTION.toString(), ((IHasDescription) perm).getDescription());
         } else if (description != null) {
             data.put(Settings.DESCRIPTION.toString(), description);
         }
         if (perm instanceof IChildPermissionContainer) {
             debug("   Is IChildPermissionContainer");
             Map<String, Boolean> clist = new LinkedHashMap<String, Boolean>();
             for (Map.Entry<IPermission, Boolean> child : ((IChildPermissionContainer) perm).getChilds().entrySet()) {
                 debug("     " + child);
                 clist.put(child.getKey().getFullString(), child.getValue());
             }
             if (clist.size() > 0)
                 data.put("children", clist);
         }
         list.put(perm.getFullString(), data);
     }
     
     private void debug(final String msg, final Object... objects) {
         getLog().debug(MessageFormat.format(msg, objects));
     }
 }
