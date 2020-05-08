 package org.apache.maven.surefire.osgi;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.PrintStream;
 import java.net.URL;
 import java.util.Collections;
 import java.util.Dictionary;
import java.util.LinkedHashSet;
 import java.util.List;
 import java.util.Set;
 import java.util.jar.Attributes;
 import java.util.jar.JarFile;
 import java.util.jar.Manifest;
 
 import org.apache.maven.surefire.booter.BooterDeserializer;
 import org.apache.maven.surefire.booter.ClasspathConfiguration;
 import org.apache.maven.surefire.booter.ProviderConfiguration;
 import org.apache.maven.surefire.booter.ProviderFactory;
 import org.apache.maven.surefire.booter.StartupConfiguration;
 import org.apache.maven.surefire.booter.SurefireReflector;
 import org.apache.maven.surefire.booter.SystemPropertyManager;
 import org.apache.maven.surefire.booter.TypeEncodedValue;
 import org.apache.maven.surefire.report.ReporterConfiguration;
 import org.osgi.framework.Bundle;
 import org.osgi.framework.BundleContext;
 import org.osgi.framework.Constants;
 import org.osgi.framework.FrameworkUtil;
 import org.osgi.framework.Version;
 import org.osgi.framework.wiring.BundleWire;
 import org.osgi.framework.wiring.BundleWiring;
 
 public class Booter
 {
 
     private static class PrintStreamImpl extends PrintStream
     {
 
         private ThreadLocal<byte[]> buffers;
 
         public PrintStreamImpl(PrintStream out)
         {
             super(out, true);
             this.buffers = new ThreadLocal<byte[]>();
         }
 
         @Override
         public synchronized void write(int b)
         {
             byte[] buffer = this.buffers.get();
             if (buffer == null)
             {
                 buffer = new byte[] {(byte) b};
                 this.buffers.set(buffer);
                 try
                 {
                     super.write(b);
                 }
                 finally
                 {
                     this.buffers.remove();
                 }
             }
             else
             {
                 super.write(buffer, 0, buffer.length);
             }
         }
 
         @Override
         public synchronized void write(byte[] bytes) throws IOException
         {
             byte[] buffer = this.buffers.get();
             if (buffer == null)
             {
                 this.buffers.set(bytes);
                 try
                 {
                     super.write(bytes);
                 }
                 finally
                 {
                     this.buffers.remove();
                 }
             }
             else
             {
                 super.write(buffer);
             }
         }
 
         @Override
         public synchronized void write(byte[] bytes, int offset, int length)
         {
             byte[] buffer = this.buffers.get();
             if (buffer == null)
             {
                 buffer = bytes;
                 if (offset != 0 || length != bytes.length)
                 {
                     buffer = new byte[length];
                     System.arraycopy(bytes, offset, buffer, 0, length);
                 }
                 this.buffers.set(buffer);
                 try
                 {
                     super.write(bytes, offset, length);
                 }
                 finally
                 {
                     this.buffers.remove();
                 }
             }
             else
             {
                 super.write(buffer, 0, buffer.length);
             }
         }
 
     }
 
     public Booter()
     {
         super();
     }
 
     public static synchronized void _main(String[] args) throws Throwable
     {
         System.out.print(Booter.class.getPackage().getName());
         System.out.print(':');
         System.out.print(Booter.class.getSimpleName());
         for (int i = 1; i < args.length; i++)
         {
             System.out.print(' ');
             System.out.print('\'');
             System.out.print(args[i].replace("'", "'\\''"));
             System.out.print('\'');
         }
         System.out.println();
         try
         {
             if (args.length > 2)
             {
                 SystemPropertyManager.setSystemProperties(new File(args[2]));
             }
             File surefirePropertiesFile = new File(args[1]);
             InputStream stream =
                 surefirePropertiesFile.exists() ? new FileInputStream(surefirePropertiesFile)
                     : null;
             BooterDeserializer booterDeserializer = new BooterDeserializer(stream);
             ProviderConfiguration providerConfiguration = booterDeserializer.deserialize();
             StartupConfiguration startupConfiguration =
                 booterDeserializer.getProviderConfiguration();
             ClasspathConfiguration classpathConfiguration =
                 startupConfiguration.getClasspathConfiguration();
             ClassLoader testClassLoader = classpathConfiguration.createTestClassLoader();
             Bundle bundle = FrameworkUtil.getBundle(Booter.class);
             Set<BundleWiring> bundleWirings = Collections.emptySet();
             {
                 String symbolicName;
                 Version version;
                 URL resource = testClassLoader.getResource(JarFile.MANIFEST_NAME);
                 InputStream inputStream = resource.openStream();
                 try
                 {
                     Manifest manifest = new Manifest(inputStream);
                     Attributes mainAttributes = manifest.getMainAttributes();
                     symbolicName = mainAttributes.getValue(Constants.BUNDLE_SYMBOLICNAME);
                     String bundleVersion = mainAttributes.getValue(Constants.BUNDLE_VERSION);
                     version = new Version(bundleVersion);
                 }
                 finally
                 {
                     inputStream.close();
                 }
                 BundleContext bundleContext = bundle.getBundleContext();
                 Bundle[] candidateBundles = bundleContext.getBundles();
                 for (Bundle candidateBundle : candidateBundles)
                 {
                     String bundleSymbolicName = candidateBundle.getSymbolicName();
                     Version bundleVersion = candidateBundle.getVersion();
                     if (symbolicName.equals(bundleSymbolicName) && version.equals(bundleVersion))
                     {
                         Dictionary<String, String> headers = candidateBundle.getHeaders();
                         String fragmentHost = headers.get(Constants.FRAGMENT_HOST);
                         if (fragmentHost == null)
                         {
                             BundleWiring bundleWiring = candidateBundle.adapt(BundleWiring.class);
                             bundleWirings = Collections.singleton(bundleWiring);
                         }
                         else
                         {
                             BundleWiring candidateBundleWiring =
                                 candidateBundle.adapt(BundleWiring.class);
                             if (candidateBundleWiring != null)
                             {
                                 List<BundleWire> candidateBundleWires =
                                     candidateBundleWiring.getRequiredWires(null);
                                 if (candidateBundleWires != null)
                                 {
                                    bundleWirings = new LinkedHashSet<BundleWiring>(candidateBundleWires.size());
                                     for (BundleWire candidateBundleWire : candidateBundleWires)
                                     {
                                         BundleWiring bundleWiring =
                                             candidateBundleWire.getProviderWiring();
                                         bundleWirings.add(bundleWiring);
                                     }
                                 }
                             }
                         }
                         break;
                     }
                 }
             }
             ReporterConfiguration reporterConfiguration =
                 providerConfiguration.getReporterConfiguration();
             Boolean trimStackTrace = reporterConfiguration.isTrimStackTrace();
             PrintStream originalSystemOut = reporterConfiguration.getOriginalSystemOut();
             PrintStream outputStream = new PrintStreamImpl(originalSystemOut);
             for (BundleWiring bundleWiring : bundleWirings)
             {
                 ClassLoader classLoader = bundleWiring.getClassLoader();
                 TypeEncodedValue forkedTestSet = providerConfiguration.getTestForFork();
                 Object testSet =
                     forkedTestSet != null ? forkedTestSet.getDecodedValue(classLoader) : null;
                 ClassLoader surefireClassLoader =
                     classpathConfiguration.createSurefireClassLoader(classLoader);
                 SurefireReflector surefireReflector = new SurefireReflector(surefireClassLoader);
                 Object factory =
                     surefireReflector.createForkingReporterFactory(trimStackTrace, outputStream);
                 PrintStream out = System.out;
                 PrintStream err = System.err;
                 try
                 {
                     ProviderFactory.invokeProvider(testSet, classLoader, surefireClassLoader,
                         factory, providerConfiguration, true, startupConfiguration, false);
                 }
                 finally
                 {
                     System.setOut(out);
                     System.setErr(err);
                 }
             }
         }
         catch (Throwable t)
         {
             t.printStackTrace(System.err);
             System.err.flush();
         }
         System.out.println("Z,0,");
         System.out.flush();
     }
 
}
