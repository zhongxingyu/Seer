 package com.inspedio.system.helper;
 
 import java.util.Hashtable;
 
 import com.inspedio.entity.primitive.InsImage;
 import com.inspedio.entity.primitive.InsSound;
 import com.inspedio.enums.AudioEncode;
 import com.inspedio.enums.AudioType;
 
 /**
  * This is helper class for caching assets
  * 
  * @author Hyude
  * @version 1.0
  */
 public class InsCache {
 
 	private Hashtable soundList  = new Hashtable();
 	private Hashtable imageList = new Hashtable();
 	
 	private static InsCache instance = null;
 	
 	/**
 	 * Construct InsAssets
 	 */
 	private InsCache()
 	{
 	}
 	
 	public static InsCache getInstance(){
 		if(instance == null){
 			instance = new InsCache();
 		}
 		return instance;
 	}
 	
 	
 	/**
 	 * Remove all Cache
 	 */
 	public void clearCache()
 	{
 		this.soundList.clear();
 		this.imageList.clear();
 		System.gc();
 	}
 	
 	/**
 	 * Add Image to ImageList.
 	 * If there are already Image with same path, width, and height, return that Image
 	 * if there isn't any, create new Image, add it to list and return it
 	 * 
 	 * @param	imagePath		Path to SpriteSheet Image
 	 * @param	frameWidth		The Width of each frame in this image
 	 * @param	frameHeight		The Height of each frame in this image
 	 * 
 	 * @return	The Image with given imagePath, frameWidth, and frameHeight
 	 */
 	public InsImage getImage(String imagePath, int frameWidth, int frameHeight)
 	{
 		try
 		{
 			if(this.imageList.containsKey(imagePath)){
 				return (InsImage) this.imageList.get(imagePath);
 			} else {
 				InsImage img = new InsImage(imagePath, frameWidth, frameHeight);
 				this.imageList.put(imagePath, img);
 				return img;
 			}
 		}
 		catch (Exception e)
 		{
 			e.printStackTrace();
 		}
 		
 		return null;
 	}
 	
 	/**
 	 * Add Image to ImageList.
 	 * If there are already Image with same path, return that Image
 	 * if there isn't any, create new Image, add it to list and return it
 	 * 
 	 * @param	imagePath		Path to SpriteSheet Image
 	 * 
 	 * @return	The Image with given imagePath, frameWidth, and frameHeight
 	 */
 	public InsImage getImage(String imagePath)
 	{
 		try
 		{
 			if(this.imageList.containsKey(imagePath)){
 				return (InsImage) this.imageList.get(imagePath);
 			} else {
 				InsImage img = new InsImage(imagePath);
 				this.imageList.put(imagePath, img);
 				return img;
 			}
 		}
 		catch (Exception e)
 		{
 			e.printStackTrace();
 		}
 		
 		return null;
 	}
 	
 	
 	/**
 	 * Delete Image from ImageList.
 	 * 
 	 * @param	imagePath		Path to SpriteSheet Image
 	 * 
 	 * @return	TRUE if Image is found and deleted, FALSE otherwise
 	 */
 	public boolean deleteImage(String imagePath)
 	{
 		try
 		{
 			if(this.imageList.containsKey(imagePath)){
 				this.imageList.remove(imagePath);
 				return true;
 			} else {
 				return false;
 			}
 		}
 		catch (Exception e)
 		{
 			e.printStackTrace();
 		}
 		return false;
 	}
 	
 	/**
 	 * Add Sound to SoundList.
 	 * If there are already Sound with same path, encode and type, return that Sound
 	 * if there isn't any, create new Sound, add it to list and return it
 	 * 
 	 * @param	audioPath		The path to audio file
 	 * @param	audioEncoding	Encoding Type of audio
 	 * @param	audioType		Whether it is BGM or SFX. -1 for BGM, 1 for SFX
 	 * 
 	 * @return	The Sound with given parameters
 	 */
 	public InsSound getSound(String audioPath, AudioEncode audioEncoding, AudioType audioType)
 	{
 		try
 		{
 			if(this.soundList.containsKey(audioPath)){
 				return (InsSound) this.soundList.get(audioPath);
 			} else {
 				InsSound s = new InsSound(audioPath, audioEncoding, audioType);
 				this.imageList.put(audioPath, s);
 				return s;
 			}
 		}
 		catch (Exception e)
 		{
 			e.printStackTrace();
 		}
 		return null;
 	}
 	
 	/**
 	 * Delete Sound from SoundList.
 	 * 
 	 * @param	audioPath		The path to audio file
 	 * 
 	 * @return	TRUE if Sound is found and deleted, FALSE otherwise
 	 */
 	public boolean deleteSound(String audioPath)
 	{
 		try
 		{
 			if(this.soundList.containsKey(audioPath)){
 				this.soundList.remove(audioPath);
 				return true;
 			} else {
 				return false;
 			}
 		}
 		catch (Exception e)
 		{
 			e.printStackTrace();
 		}
 		
 		return false;
 	}
 	
 }
