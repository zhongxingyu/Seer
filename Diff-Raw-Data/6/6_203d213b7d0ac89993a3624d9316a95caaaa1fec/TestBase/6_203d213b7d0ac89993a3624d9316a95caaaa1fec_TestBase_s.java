 package org.cagrid.systest;
 
 import static org.ops4j.pax.exam.CoreOptions.junitBundles;
 import static org.ops4j.pax.exam.CoreOptions.maven;
 import static org.ops4j.pax.exam.CoreOptions.options;
 import static org.ops4j.pax.exam.CoreOptions.vmOption;
 import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.karafDistributionConfiguration;
 import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.keepRuntimeFolder;
 import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.replaceConfigurationFile;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.inject.Inject;
 
 import org.junit.runner.RunWith;
 import org.ops4j.pax.exam.Configuration;
 import org.ops4j.pax.exam.MavenUtils;
 import org.ops4j.pax.exam.Option;
 import org.ops4j.pax.exam.ProbeBuilder;
 import org.ops4j.pax.exam.TestProbeBuilder;
 import org.ops4j.pax.exam.junit.PaxExam;
 import org.ops4j.pax.exam.spi.intern.TestProbeBuilderImpl;
 import org.osgi.framework.Bundle;
 import org.osgi.framework.BundleContext;
 
 @RunWith(PaxExam.class)
 public abstract class TestBase {
 
 	/**
 	 * Debug system property, set to any value to enable.
 	 */
 	public final static String DEBUG_PROPERTY = "systest.debug";
 
 	/**
 	 * Debug suspend system property, set to 'y' or 'n' (or leave unset).
 	 */
 	public final static String DEBUG_SUSPEND_PROPERTY = "systest.suspend";
 
 	private final Map<Integer, String> _bundleStates = new HashMap<Integer, String>();
 	protected final Map<Integer, String> bundleStates = Collections
 			.unmodifiableMap(_bundleStates);
 
 	@Inject
 	protected BundleContext bundleContext;
 
 	protected TestBase() {
 		_bundleStates.put(Bundle.UNINSTALLED, "UNINSTALLED");
 		_bundleStates.put(Bundle.INSTALLED, "INSTALLED");
 		_bundleStates.put(Bundle.RESOLVED, "RESOLVED");
 		_bundleStates.put(Bundle.STARTING, "STARTING");
 		_bundleStates.put(Bundle.STOPPING, "STOPPING");
 		_bundleStates.put(Bundle.ACTIVE, "ACTIVE");
 	}
 
 	@ProbeBuilder
 	public TestProbeBuilder addTestBase(TestProbeBuilder probeBuilder) {
 		if (probeBuilder instanceof TestProbeBuilderImpl) {
 			TestProbeBuilderImpl probeBuilderImpl = (TestProbeBuilderImpl) probeBuilder;
 			probeBuilderImpl.addAnchor(TestBase.class);
 			probeBuilderImpl.addAnchor(ContextLoader.class);
 			@SuppressWarnings("rawtypes")
 			List<Class> additionalClasses = getAdditionalClasses();
 			if (additionalClasses != null) {
 				for (Class<?> additionalClass : additionalClasses) {
 					probeBuilderImpl.addAnchor(additionalClass);
 				}
 			}
 		}
 		return probeBuilder;
 	}
 	
 	@Configuration
 	public Option[] config() throws Exception {
 		prePAX();
 
 		List<Option> options = new ArrayList<Option>();
 
 		if (isDebug()) {
 			options.add(vmOption("-agentlib:jdwp=transport=dt_socket,server=y,address=5005,suspend="
 					+ System.getProperty(DEBUG_PROPERTY, "n")));
 			options.add(vmOption("-Dcom.sun.management.jmxremote.ssl=false"));
 			options.add(vmOption("-Dcom.sun.management.jmxremote.authenticate=false"));
 			options.add(vmOption("-Dcom.sun.management.jmxremote.port=5006"));
 		}
 
 		String localRepository = System.getProperty("maven.repo.local");
 		System.out.println("!!! localRepository = " + localRepository);
 		if (localRepository != null) {
 			options.add(vmOption("-Dorg.ops4j.pax.url.mvn.localRepository=" + localRepository));
 		}
 
 		File certificateDirectory = new File(ContextLoader.getKarafEtc(), "certificates");
 		options.add(vmOption("-DX509_CERT_DIR=" + certificateDirectory.getAbsolutePath()));
 		
 		List<Option> containerOptions = getContainerOptions();
 		if ((containerOptions == null) || containerOptions.isEmpty()) {
 			throw new RuntimeException("No container specified!");
 		}
 		options.addAll(containerOptions);
 
 		List<Option> fileOptions = getFileOptions();
 		if (fileOptions != null)
 			options.addAll(fileOptions);
 
 		options.add(junitBundles());
 
 		options.addAll(getTestBundles());
 
 		return options(options.toArray(new Option[options.size()]));
 	}
 
 	@SuppressWarnings("rawtypes")
 	protected List<Class> getAdditionalClasses() {
 		return null;
 	}
 
 	/**
 	 * Override to run pre-pax setup.
 	 */
 	protected void prePAX() throws Exception {
 	}
 
 	/**
 	 * Create file replacement options for everything in ${karaf.base}/etc.
 	 */
 	protected List<Option> getFileOptions() {
 		List<Option> options = new ArrayList<Option>();
 		File karafBase = new File(
 				System.getProperty(ContextLoader.KARAF_BASE_KEY));
 		File karafEtc = new File(karafBase, "etc");
 		addFileOptions("etc", karafEtc, options);
 		return options;
 	}
 
 	private void addFileOptions(String path, File dir, List<Option> options) {
 		for (File file : dir.listFiles()) {
 			String filePath = path + "/" + file.getName();
 			if (file.isDirectory()) {
 				addFileOptions(filePath, file, options);
 			} else {
 				options.add(replaceConfigurationFile(filePath, file));
 			}
 		}
 	}
 
 	/**
 	 * Override to set debug mode.
 	 */
 	protected boolean isDebug() {
 		return false;
 	}
 
 	/**
 	 * Override to use a different container.
 	 */
 	protected List<Option> getContainerOptions() {
 		File karafBase = ContextLoader.getKarafBase();
 
 		List<Option> options = new ArrayList<Option>();
 		String karafVersion = MavenUtils.getArtifactVersion("org.apache.karaf",
 				"apache-karaf");
 		options.add(karafDistributionConfiguration()
 				.frameworkUrl(
 						maven("org.apache.servicemix", "apache-servicemix")
 								.versionAsInProject().type("tar.gz"))
 				.name("Apache ServiceMix").karafVersion(karafVersion)
 				.unpackDirectory(karafBase));
 		options.add(keepRuntimeFolder());
 		return options;
 	}
 
 	/**
 	 * Must implement to add test bundles.
 	 */
 	protected abstract List<? extends Option> getTestBundles();
 }
