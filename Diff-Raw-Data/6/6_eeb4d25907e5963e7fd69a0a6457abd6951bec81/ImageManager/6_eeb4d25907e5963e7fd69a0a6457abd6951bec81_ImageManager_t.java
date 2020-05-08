 package org.romaframework.aspect.view.html.image;
 
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 import java.util.WeakHashMap;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.romaframework.aspect.view.ViewAspect;
 import org.romaframework.core.Roma;
 import org.romaframework.core.Utility;
 import org.romaframework.core.config.ApplicationConfiguration;
 import org.romaframework.core.config.RomaApplicationContext;
 
 public class ImageManager {
 
 	private final static Log									LOG							= LogFactory.getLog(ImageManager.class);
 
 	public static final String								IMAGE_PACKAGE		= ".image";
 
 	private static final Map<String, byte[]>	images					= new WeakHashMap<String, byte[]>();
 	private List<String>											imagePaths			= new ArrayList<String>();
 	public List<String>												additionalPaths	= new ArrayList<String>();								;
 
 	private ApplicationConfiguration					config;
 
 	public ApplicationConfiguration getConfig() {
 		return config;
 	}
 
 	public void setConfig(ApplicationConfiguration romaConfig) {
 		config = romaConfig;
 		initImagePaths();
 	}
 
 	protected void initImagePaths() {
		final String viewAspectPath = Roma.component(ApplicationConfiguration.class).getApplicationPackage() + Utility.PACKAGE_SEPARATOR + ViewAspect.ASPECT_NAME;
 		imagePaths.clear();
 		addImagePath(Utility.CLASSPATH_PREFIX + viewAspectPath + IMAGE_PACKAGE, imagePaths);
		addImagePath("static" + File.separator + "themes" + File.separator + "default" + File.separator + "image", imagePaths);
 
 	}
 
 	public void setAdditionalImagePaths(final List<String> paths) {
 		for (final String path : paths) {
 			addImagePath(path, additionalPaths);
 		}
 
 	}
 
 	protected void addImagePath(String iPath, final List<String> destination) {
 		String path = iPath;
 		if (path.startsWith("classpath:")) {
 			path = path.substring("classpath:".length());
 			path = "WEB-INF" + File.separator + "classes" + File.separator + path;
 		}
 		if (path.charAt(0) != File.separatorChar) {
 			path = File.separator + path;
 		}
 		destination.add(RomaApplicationContext.getApplicationPath() + Utility.getAbsoluteResourcePath(path));
 	}
 
 	/**
 	 * returns the absolute path of an image on the file system
 	 * 
 	 * @param imageName
 	 *          the image name
 	 * @return the absolute path of an image on the file system
 	 */
 	public String getImagePath(final String imageName) {
 		for (final String imagePath : imagePaths) {
 			final String absolutePath = imagePath + File.separator + imageName;
 			final File file = newFile(absolutePath);
 			if (file.exists()) {
 				return file.getAbsolutePath();
 			}
 		}
 
 		for (final String imagePath : additionalPaths) {
 			final String absolutePath = imagePath + File.separator + imageName;
 			final File file = newFile(absolutePath);
 			if (file.exists()) {
 				return file.getAbsolutePath();
 			}
 		}
 
 		return null;
 	}
 
 	private File newFile(final String absolutePath) {
 		return new File(absolutePath);
 	}
 
 	/**
 	 * returns an image as a byte array
 	 * 
 	 * @param imageName
 	 *          the image name
 	 * @return an image as a byte array
 	 */
 	public synchronized byte[] getImage(final String imageName) {
 		byte[] result = images.get(imageName);
 		try {
 			if (result == null) {
 				final String imagePath = getImagePath(imageName);
 				if (imagePath == null) {
 					return null;
 				}
 
 				final File file = newFile(imagePath);
 
 				if (!file.exists()) {
 					return null;
 				}
 
 				final InputStream inputStream = new FileInputStream(file);
 				final ByteArrayOutputStream out = new ByteArrayOutputStream();
 				int nextChar = inputStream.read();
 				while (nextChar != -1) {
 					out.write(nextChar);
 					nextChar = inputStream.read();
 				}
 
 				result = out.toByteArray();
 				images.put(imageName, result);
 			}
 		} catch (final Exception e) {
 			LOG.error(e.getMessage(), e);// TODO
 		}
 		return result;
 	}
 
 	public List<String> getImagePaths() {
 		return imagePaths;
 	}
 
 	public List<String> getAdditionalPaths() {
 		return additionalPaths;
 	}
 
 	public void setImagePaths(List<String> imagePaths) {
 		this.imagePaths = imagePaths;
 	}
 
 	public void setAdditionalPaths(List<String> additionalPaths) {
 		this.additionalPaths = additionalPaths;
 	}
 }
