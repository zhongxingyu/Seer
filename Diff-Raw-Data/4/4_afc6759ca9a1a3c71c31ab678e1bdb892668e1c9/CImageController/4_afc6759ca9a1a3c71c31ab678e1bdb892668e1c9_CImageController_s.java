 package calico.controllers;
 
 import it.unimi.dsi.fastutil.longs.Long2ReferenceArrayMap;
 
 import java.awt.Image;
 import java.awt.image.ImageObserver;
 import java.io.BufferedInputStream;
 import java.io.BufferedOutputStream;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.FilenameFilter;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.net.InetAddress;
 import java.net.MalformedURLException;
 import java.net.URL;
 
 import org.apache.commons.vfs.*;
 import org.apache.log4j.Logger;
 
 import calico.COptions;
 import calico.components.CGroup;
 import calico.uuid.UUIDAllocator;
 
 public class CImageController
 {
 	public static Long2ReferenceArrayMap<CGroup> groups = new Long2ReferenceArrayMap<CGroup>();
 
 	private static Logger logger = Logger.getLogger(CImageController.class.getName());
 
 	
 	public static void setup()
 	{
 	
 	}
 	
 	public static String download_image(long uuid, String url) throws IOException
 	{
 //		if (imageExists(uuid))
 //		{
 //			try
 //			{
 //				(new File(getImagePath(uuid))).delete();
 //			}
 //			catch (Exception ioe)
 //			{
 //				
 //			}
 //		}
 		
 		if (!(new File(COptions.server.images.download_folder + "/")).exists())
 			(new File(COptions.server.images.download_folder)).mkdir();
 		
 		
 		URL urlobj = new URL(url);
 		String fileExt = getFileExtension(url);
 		
 	    // TODO: NEED TO DOWNLOAD THE IMAGE CONTENT AND WRITE TO A FILE
 	    String filePath = COptions.server.images.download_folder + Long.toString(uuid) + "." + fileExt;
 	    File imageFile = new File(filePath);
 	    
 	    if (!imageFile.exists())
 	    {
 			FileObject backupFile = COptions.fs.resolveFile(filePath);
 			backupFile.createFile();
 //	    	imageFile.createNewFile();
 	    }
 	    
 		InputStream is = urlobj.openStream();
 		OutputStream os = new FileOutputStream(imageFile);
 
 		byte[] b = new byte[2048];
 		int length;
 
 		while ((length = is.read(b)) != -1) {
 			os.write(b, 0, length);
 		}
 
 		is.close();
 		os.close();
 	   
 		return Long.toString(uuid)+"."+fileExt;
 	}
 	
 	public static void save_to_disk(long uuid, String name, byte[] image) throws IOException {
 		
 		if (imageExists(uuid))
 		{
 			try
 			{
 				(new File(getImagePath(uuid))).delete();
 			}
 			catch (Exception ioe)
 			{
 				
 			}
 		}
 		
 		if (!(new File(COptions.server.images.download_folder + "/")).exists())
 			(new File(COptions.server.images.download_folder)).mkdir();
 		
 		String fileExt = getFileExtension(name);
 		
 		String filePath = COptions.server.images.download_folder + Long.toString(uuid) + "." + fileExt;
 		
 		FileObject backupFile = COptions.fs.resolveFile(filePath);
 		if (!backupFile.exists())
 			backupFile.createFile();
 		backupFile.close();
 	    
 	    File imageFile = new File(filePath);
 	    
 	    OutputStream os = new FileOutputStream(imageFile);
 	    
 	    os.write(image);
 	    
 	    os.close();
 	    
 	}
 	
 	public static String getImagePath(final long imageUUID)
 	{
 		File[] files = (new File(COptions.server.images.download_folder + "/")).listFiles(new FilenameFilter() {
 	           public boolean accept(File dir, String name) {
 	                return name.toLowerCase().startsWith(Long.toString(imageUUID) + ".");
 	                }
 	           }
 	        );
 		
 //	    String filePath = CalicoOptions.images.download_folder + Long.toString(imageUUID) + "." + ext;
 //		File imageFile = new File(filePath);
 		if (files != null && files.length > 0)
 			return files[0].getAbsolutePath();
 		else
 			return null;
 	    
 //	    return imageFile.exists();
 	}
 	
 	public static boolean imageExists(long imageUUID)
 	{
 		return getImagePath(imageUUID) != null;
 	}
 
 	public static String getFileExtension(String url) {
 		int mid= url.lastIndexOf(".");
 	    String fileExt=url.substring(mid+1,url.length());
 		return fileExt;
 	}
 	
 	/**
 	 * Just an alias to download_image, but catches the exception
 	 * @param url
 	 * @return
 	 */
 	public static String download_image_no_exception(long uuid, String url)
 	{
 		try
 		{
 			return download_image(uuid, url);
 		}
 		catch(Exception e)
 		{
 			e.printStackTrace();
 			return null;
 		}
 	}
 	
 	public static ImageInitializer getImageInitializer(long uuid, long cuid, String imageURL, int x, int y)
 	{
 		return (new CImageController()).new ImageInitializer(uuid, cuid, imageURL, x, y);
 	}
 	
 	public static String getImageURL(final long uuid)
 	{
 		File[] files = (new File(COptions.server.images.download_folder + "/")).listFiles(new FilenameFilter() {
 	           public boolean accept(File dir, String name) {
 	                return name.toLowerCase().startsWith(Long.toString(uuid) + ".");
 	                }
 	           }
 	        );
 		
 		String localPath = files[0].getPath();
 		String ipaddress = "0.0.0.0";
 		try
 		{
 			ipaddress = InetAddress.getLocalHost().getCanonicalHostName();
 		}
 		catch (Exception e)
 		{
 			e.printStackTrace();
 		}
 		
 //		String localPath = getImagePath(uuid);
 		return "http://" + ipaddress + ":" + COptions.admin.serversocket.getLocalPort() + "/" + localPath;
 	}
 	
 	class ImageInitializer implements ImageObserver
 	{
 		long uuid;
 		long cuid;
 		String imageURL;
 		int x, y;
 		
 		public ImageInitializer(long uuid, long cuid, String imageURL, int x, int y) 
 		{
 			this.uuid = uuid;
 			this.cuid = cuid;
 			this.imageURL = imageURL;
 			this.x = x;
 			this.y = y;
 		}
 		//I'm just going to assume that when this instance gets called, the image is loaded and ready to roll
 		@Override
 		public boolean imageUpdate(Image img, int infoflags, int x,
 				int y, int width, int height) {
 			CGroupController.createImageGroup(uuid, cuid, 0L, imageURL, this.x, this.y, width, height);		
 			return false;
 		}
 		
 	}
 	
 }
