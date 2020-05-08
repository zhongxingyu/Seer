 package com.abbajoa.thumbnailmirror;
 
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashSet;
 import java.util.Set;
 
 import javax.imageio.ImageReader;
 import javax.imageio.metadata.IIOMetadata;
 
 public class Mirror {
 	
 	public static void mirror(final File sourceDir, final File destDir, final int maxBoxSize, final int quality, final EventListener<File> createEventListener) throws IOException, FileNotFoundException {
 		if(!sourceDir.isDirectory())
 			throw new IOException(sourceDir + " is not a directory");
 		final Set<File> destFiles = new HashSet<File>();
 		final Set<File> destDirectories = new HashSet<File>();
 		FileUtil.visitAllFiles(sourceDir, new Visitor<File, IOException>() {
 			public void visit(File file) throws IOException {
 				if(!isConvertableImageFile(file))
 					return;
 				File destFile = new File(destDir.getAbsolutePath() + "/" + FileUtil.getRelativePath(sourceDir, file) + "." + file.lastModified() + "." + maxBoxSize + "." + quality + ".jpg");
 				destFiles.add(destFile);
 				if(!destFile.exists()) {
 					File dir = destFile.getParentFile();
 					destDirectories.add(dir);
 					FileUtil.ensureDirectoryExist(dir);
 					File tempFile = new File(destFile.getAbsolutePath() + ".tmp");
 					createEventListener.onEvent(file);
 					createJpegThumbnail(file, tempFile, maxBoxSize, quality);
 					tempFile.renameTo(destFile);
 				}
 			}
 		});			
 		deleteUnnecessaryFiles(destDir, destFiles);
 		deleteUnnecessaryDirectories(destDir, destDirectories);
 	}
 
 
 	private static boolean isConvertableImageFile(File file) {
 		return file.getAbsolutePath().toLowerCase().endsWith(".jpg");
 	}
 
 	private static void createJpegThumbnail(File src, File dest, int maxBoxSize, int quality) throws FileNotFoundException, IOException {
		ImageReader reader = ImageUtil.createJpegReader(src);
 		try {
			IIOMetadata exif = ImageUtil.readJpegExifTags(reader);
			BufferedImage original = ImageUtil.readImage(reader);
			BufferedImage rescaled = ImageUtil.getRescaled(original, maxBoxSize);
			ImageUtil.writeJpeg(rescaled, exif, quality / 100f, dest);
 		} finally {
			reader.dispose();
 		}
 	}
 
 	private static void deleteUnnecessaryFiles(final File destDir, final Set<File> necessaryFiles) throws IOException, FileNotFoundException {
 		FileUtil.visitAllFiles(destDir, new Visitor<File, IOException>() {
 			public void visit(File data) throws IOException {
 				if(!necessaryFiles.contains(data))
 					data.delete();
 			}
 		});
 	}
 
 	private static void deleteUnnecessaryDirectories(final File destDir, final Set<File> necessaryDirectories) throws IOException, FileNotFoundException {
 		final ArrayList<File> toremove = new ArrayList<File>();
 		FileUtil.visitAllSubDirectories(destDir, new Visitor<File, IOException>() {
 			public void visit(File data) throws IOException {
 				if(!necessaryDirectories.contains(data))
 					toremove.add(data);
 			}
 		});
 		Collections.sort(toremove, new Comparator<File>() {
 			public int compare(File o1, File o2) {
 				Integer l1 = o1.getAbsolutePath().length();
 				Integer l2 = o2.getAbsolutePath().length();
 				return - l1.compareTo(l2);
 			}
 		});
 		for(File f : toremove)
 			f.delete();
 	}
 
 }
