 package nextmethod.web.mvc;
 
 import com.google.common.base.Joiner;
 import com.google.common.base.Strings;
 import com.google.common.collect.ImmutableMultimap;
 import com.google.common.collect.ImmutableSet;
 import nextmethod.web.InvalidOperationException;
 
 import javax.annotation.Nonnull;
 import javax.annotation.Nullable;
 import javax.inject.Inject;
 import javax.inject.Provider;
 import javax.servlet.ServletContext;
 import java.io.File;
 import java.io.FileFilter;
 import java.io.FilenameFilter;
 import java.net.URL;
 
import static nextmethod.SystemHelpers.FileSeparator;

 /**
  *
  */
 final class VirtualPathUtility {
 
 	@Inject
 	private Provider<ServletContext> contextProvider;
 
 	private static final Object lockObject = new Object();
 
 	public String getLocalContextPath() {
 		if (WebInfFile != null)
 			return WebInfFile.getParent();
 
 		String contextPath = contextProvider.get().getContextPath();
 		contextPath = Strings.isNullOrEmpty(contextPath) ? "/" : contextPath;
 		String realPath = contextProvider.get().getRealPath(contextPath);
 		if (Strings.isNullOrEmpty(realPath)) {
 			final File webInfFolder = getWebInfFolder(null);
 			realPath = webInfFolder.getParent();
 		}
 
 		return realPath;
 	}
 
 	public String getWebInfFolder() {
 		File webInfFile = WebInfFile;
 		if (webInfFile == null) {
 			webInfFile = getWebInfFolder(null);
 		}
 
 		return webInfFile.getAbsolutePath();
 	}
 
 	public ImmutableMultimap<ClassPathType, String> getClassPath() {
 		final File webInf = new File(getWebInfFolder());
 		final File classes = new File(webInf, "classes");
 
 		final ImmutableMultimap.Builder<ClassPathType, String> builder = ImmutableMultimap.builder();
 		if (classes.exists() && classes.isDirectory()) {
 			final ImmutableSet<String> clss = loadPathEntries(ClassPathType.Path, classes);
			final String s = String.format("%s%s", classes.getAbsolutePath(), FileSeparator());
 			for (String cls : clss) {
 				builder.put(ClassPathType.Path, cls.replace(s, ""));
 			}
 		}
 
 		final File lib = new File(webInf, "lib");
 		if (lib.exists() && classes.isDirectory()) {
 			builder.putAll(ClassPathType.Jar, loadPathEntries(ClassPathType.Jar, lib));
 		}
 
 		return builder.build();
 	}
 
 	private ImmutableSet<String> loadPathEntries(final ClassPathType type, final File parent) {
 		if (!parent.exists())
 			return ImmutableSet.of();
 
 		final ImmutableSet.Builder<String> builder = ImmutableSet.builder();
 		for (File o : parent.listFiles(createFileNameFilter(type.suffix()))) {
 			builder.add(o.getAbsolutePath());
 		}
 
 		for (File dir : parent.listFiles(createDirectoryFilter())) {
 			builder.addAll(loadPathEntries(type, dir));
 		}
 
 		return builder.build();
 	}
 
 	private FileFilter createDirectoryFilter() {
 		return new FileFilter() {
 			@Override
 			public boolean accept(final File pathname) {
 				return pathname.exists() && pathname.isDirectory();
 			}
 		};
 	}
 
 	public FilenameFilter createFileNameFilter(final String suffix) {
 		final int suffixLen = suffix.length();
 		return new FilenameFilter() {
 			@Override
 			public boolean accept(final File dir, final String name) {
 				if (Strings.isNullOrEmpty(name))
 					return false;
 
 				final int length = name.length();
 				if (length < suffixLen)
 					return false;
 
 				final int offset = length - suffixLen;
 				return suffix.equalsIgnoreCase(name.substring(offset));
 			}
 		};
 	}
 
 
 	private static final String WebInfFolderName = "WEB-INF";
 	private static File WebInfFile = null;
 
 	private static File getWebInfFolder(@Nullable File baseFile) {
 		if (WebInfFile != null)
 			return WebInfFile;
 
 		if (baseFile == null) {
 			baseFile = getMvcResourcesProperties();
 		}
 		final String name = baseFile.getName();
 		if (WebInfFolderName.equalsIgnoreCase(name)) {
 			if (WebInfFile == null) {
 				synchronized (lockObject) {
 					if (WebInfFile == null) {
 						WebInfFile = baseFile;
 					}
 				}
 			}
 			return WebInfFile;
 		}
 		final File parentFile = baseFile.getParentFile();
 		if (parentFile == null)
 			return null;
 
 		return getWebInfFolder(parentFile);
 	}
 
 	private static
 	@Nonnull
 	File getMvcResourcesProperties() {
 //		final URL resource = Resources.getResource("MvcResources.properties");
 		final URL resource = VirtualPathUtility.class.getResource("MvcResources.properties");
 		if (resource == null)
 			throw new InvalidOperationException("Failed to load MvcResources.properties");
 
 		String file = normalizeFilePath(resource.toExternalForm());
 
 		return new File(file);
 	}
 
 	private static final String ClassSuffix = ".class";
 	private static final Joiner CpJoiner = Joiner.on('.');
 
 	public static String normalizeClassEntry(final String entry) {
 		int idx = entry.lastIndexOf(ClassSuffix);
 		if (idx > 0) {
 			return normalizeClassEntry(entry.substring(0, idx));
 		}
 
 		idx = entry.lastIndexOf('$');
 		if (idx > 0) {
 			return normalizeClassEntry(entry.substring(0, idx));
 		}
 		idx = entry.indexOf('\\');
 		if (idx > 0) {
 			return normalizeClassEntry(entry.replace('\\', '/'));
 		}
 
 		String[] split = entry.split("/");
 		if (split.length > 1) {
 			return normalizeClassEntry(CpJoiner.join(split));
 		}
 
 
 		return entry;
 	}
 
 	public static String normalizeFilePath(final String path) {
 		String p = "jar:";
 		if (p.equalsIgnoreCase(path.substring(0, p.length()))) {
 			return normalizeFilePath(path.substring(p.length()));
 		}
 
 		p = "file:";
 		if (p.equalsIgnoreCase(path.substring(0, p.length()))) {
 			return normalizeFilePath(path.substring(p.length()));
 		}
 
 		final String[] split = path.split("!");
 		if (split.length > 1) {
 			return normalizeFilePath(split[0]);
 		}
 
 		return path;
 	}
 
 }
