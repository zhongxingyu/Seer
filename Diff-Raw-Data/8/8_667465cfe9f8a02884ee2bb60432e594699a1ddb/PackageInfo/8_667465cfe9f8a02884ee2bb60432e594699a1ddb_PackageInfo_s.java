 package de.matrixweb.smaller.maven.plugin.node;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import org.apache.commons.io.FileUtils;
 import org.apache.commons.io.IOUtils;
 import org.apache.http.client.fluent.Request;
 import org.apache.http.client.fluent.Response;
 import org.codehaus.jackson.map.DeserializationConfig.Feature;
 import org.codehaus.jackson.map.ObjectMapper;
 import org.eclipse.jgit.api.CloneCommand;
 import org.eclipse.jgit.api.Git;
 import org.eclipse.jgit.api.errors.GitAPIException;
 
 import de.matrixweb.smaller.maven.plugin.node.Descriptor.Version;
 
 class PackageInfo {
 
   private static final ObjectMapper OM;
   static {
     OM = new ObjectMapper();
     OM.setDeserializationConfig(OM.getDeserializationConfig().without(
         Feature.FAIL_ON_UNKNOWN_PROPERTIES));
   }
 
   private final String name;
 
   private String version;
 
   private Descriptor descriptor;
 
   private final Logger log;
 
   private final NpmCache cache;
 
   private final File tempLocation;
 
   static PackageInfo createPackage(final String input, final Logger log,
       final NpmCache cache) throws IOException {
     if (input.startsWith("git:") || input.startsWith("git+")) {
       return fromGit(input, log, cache);
     } else if (input.contains("@")) {
       return fromNameVersion(input, log, cache);
     } else {
       return fromUrl(input, log, cache);
     }
   }
 
   private static PackageInfo fromGit(final String input, final Logger log,
       final NpmCache cache) throws IOException {
     try {
       String uri = input;
       if (uri.startsWith("git+")) {
         uri = uri.substring(4);
       }
       String branch = "master";
       final int idx = uri.indexOf("#");
       if (idx > -1) {
         branch = uri.substring(idx + 1);
         uri = uri.substring(0, idx);
       }
       final File temp = File.createTempFile("smaller-node", ".dir");
       try {
         temp.delete();
         temp.mkdirs();
         final CloneCommand clone = Git.cloneRepository();
         clone.setURI(uri).setDirectory(temp).setBranch(branch).setBare(false);
         clone.call().getRepository().close();
 
         log.debug("Cloned repository from " + input);
         for (final String file : temp.list()) {
           log.debug("  ... " + file);
         }
 
         return createPackageFromTemp(temp, log, cache);
       } catch (final IOException e) {
         FileUtils.deleteDirectory(temp);
         throw e;
       }
     } catch (final GitAPIException e) {
       throw new IOException("Failed to clone git repository " + input, e);
     }
   }
 
   private static PackageInfo fromNameVersion(final String input,
       final Logger log, final NpmCache cache) {
     final int idx = input.indexOf('@');
     if (idx > -1) {
       return new PackageInfo(input.substring(0, idx), input.substring(idx + 1),
           null, log, cache);
     } else {
       return new PackageInfo(input, "", null, log, cache);
     }
   }
 
   private static PackageInfo fromUrl(final String input, final Logger log,
       final NpmCache cache) throws IOException {
     final InputStream in = get(cache, log, input, "Downloading .tar.gz");
     try {
       final File temp = File.createTempFile("smaller-node", ".dir");
       try {
         temp.delete();
         temp.mkdirs();
         Extractor.uncompress(input, log, in, temp);
 
         File dir = temp;
         if (!new File(dir, "package.json").exists()) {
           for (final File sub : temp.listFiles()) {
             if (new File(sub, "package.json").exists()) {
               dir = sub;
               break;
             }
           }
         }
         if (!new File(dir, "package.json").exists()) {
           throw new IOException("Failed to find package.json in tar.gz");
         }
 
         return createPackageFromTemp(dir, log, cache);
       } catch (final IOException e) {
         FileUtils.deleteDirectory(temp);
         throw e;
       }
     } finally {
       IOUtils.closeQuietly(in);
     }
   }
 
   private static PackageInfo createPackageFromTemp(final File temp,
       final Logger log, final NpmCache cache) throws IOException {
     final PackageJson json = getPackageJson(temp);
     return new PackageInfo(json.getName(), json.getVersion(), temp, log, cache);
   }
 
   private static InputStream get(final NpmCache cache, final Logger log,
       final String url, final String message) throws IOException {
     return get(cache, false, log, url, message);
   }
 
   private static InputStream get(final NpmCache cache,
       final boolean forceUpdate, final Logger log, final String url,
       final String message) throws IOException {
     InputStream in = cache.get(url, forceUpdate);
     if (in == null) {
       log.info(message + " " + url);
       final Response response = Request.Get(url).execute();
       cache.put(url, response.returnContent().asStream());
       in = cache.get(url);
     }
     return in;
   }
 
   private static PackageJson getPackageJson(final File dir) throws IOException {
     return OM.readValue(new File(dir, "package.json"), PackageJson.class);
   }
 
   PackageInfo(final String name, final String version, final File tempLocation,
       final Logger log, final NpmCache cache) {
     this.name = name;
     this.version = version;
     this.tempLocation = tempLocation;
     this.log = log;
     this.cache = cache;
   }
 
   /**
    * @param version
    *          the version to set
    */
   public void setVersion(final String version) {
     this.version = version;
   }
 
   private Descriptor getDescriptor() throws IOException {
     if (this.descriptor == null) {
       final InputStream in = get(this.cache, this.log,
           "http://registry.npmjs.org/" + this.name, "Requesting");
       try {
         this.descriptor = OM.readValue(in, Descriptor.class);
       } finally {
         IOUtils.closeQuietly(in);
       }
     }
     return this.descriptor;
   }
 
   private Map<String, String> getDependencies() throws IOException {
     if (this.tempLocation != null) {
       return getPackageJson(this.tempLocation).getDependencies();
     }
     return getDescriptor().getVersions().get(this.version).getDependencies();
   }
 
   void install(final File root, final File installDir) throws IOException {
     if ("".equals(this.version)) {
       this.version = getDescriptor().getDistTags().getLatest();
     }
     final File pkgDir = new File(installDir, this.name);
     try {
       installSources(pkgDir);
       installDependencies(root, getDependencies(), pkgDir);
     } finally {
       if (this.tempLocation != null) {
         FileUtils.deleteDirectory(this.tempLocation);
       }
     }
   }
 
   private void installSources(final File pkgDir) throws IOException {
     if (this.tempLocation != null) {
       FileUtils.copyDirectory(this.tempLocation, pkgDir);
     } else {
       final Version versionDescriptor = getDescriptor().getVersions().get(
           this.version);
       if (versionDescriptor == null) {
         throw new IOException("Version " + this.version + " not found");
       }
       downloadAndExtract(versionDescriptor, pkgDir, true, false);
     }
   }
 
   private void downloadAndExtract(final Version versionDescriptor,
       final File pkgDir, final boolean retry, final boolean forceUpdate)
       throws IOException {
     try {
       final InputStream in = getTarball(versionDescriptor, forceUpdate);
       try {
         Extractor.uncompress(this.name, this.version, this.log, in, pkgDir);
       } finally {
         IOUtils.closeQuietly(in);
       }
     } catch (final IOException e) {
      this.log.info("Retry with redownloading and extracting");
      downloadAndExtract(versionDescriptor, pkgDir, false, true);
     }
   }
 
   private InputStream getTarball(final Version version,
       final boolean forceUpdate) throws IOException {
     final File local = new File(System.getProperty("user.home"), ".npm/"
         + this.name + "/" + this.version + "/package.tgz");
     this.log.debug("Check local .npm for " + local);
     if (local.exists()) {
       return new FileInputStream(local);
     }
     return get(this.cache, this.log, version.getDist().getTarball(),
         "Downloading");
   }
 
   private void installDependencies(final File root,
       final Map<String, String> dependencies, final File pkgDir)
       throws IOException {
     for (final Entry<String, String> dependency : dependencies.entrySet()) {
       final String pkgName = dependency.getKey();
       final String requiredVersion = dependency.getValue();
       this.log.debug("Looking for " + pkgName + '@' + requiredVersion
           + " in parent folders of " + pkgDir);
       final String foundVersion = findInstalledVersion(root, pkgDir, pkgName);
       if (foundVersion == null || !satisfies(requiredVersion, foundVersion)) {
         PackageInfo depPkg;
         try {
           depPkg = new PackageInfo(pkgName, "", null, this.log, this.cache);
           depPkg.setVersion(SemanticVersion.getBestMatch(depPkg.getDescriptor()
               .getVersions().keySet(), requiredVersion));
         } catch (final ParseException e) {
           // Try git-version or url
           depPkg = createPackage(requiredVersion, this.log, this.cache);
         }
         depPkg.install(root, new File(pkgDir, "node_modules"));
       }
     }
   }
 
   private boolean satisfies(final String requiredVersion,
       final String foundVersion) {
     final Range range = new Range(requiredVersion);
     return range.satisfies(ParsedVersion.parse(foundVersion));
   }
 
   private String findInstalledVersion(final File root, final File dir,
       final String pkgName) throws IOException {
     String version = null;
 
     final File nodeModulesDir = new File(dir, "node_modules");
     final File pkgDir = new File(nodeModulesDir, pkgName);
     if (pkgDir.exists()) {
       this.log.debug("  searching " + pkgDir + "...");
       final PackageJson json = getPackageJson(pkgDir);
       version = json.getVersion();
       this.log.debug("  found version " + version);
     } else if (!dir.getParentFile().equals(root)) {
       version = findInstalledVersion(root, dir.getParentFile(), pkgName);
     }
 
     return version;
   }
 
 }
