 package com.room.render;
 
 import java.util.HashMap;
 
 import com.room.Global;
 import com.room.R;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.opengl.GLES20;
 import android.opengl.GLUtils;
 import android.util.Log;
 
 public class RTextureLoader
 {
 	// SINGLETON!!
 	public static RTextureLoader getInstance()
 	{
 		if(instance == null)
 		{
 			instance = new RTextureLoader();	
 			//instance.init();
 		}		
 		return instance;
 	}	
 	
 	public int invalidTextureID;
 	
 	public void init()
 	{
 		invalidTextureID = loadTexture(R.drawable.invalid,false,false);
 		
 		textureID = new HashMap<String,Integer>();
 		textureID.put("bathroom_floor_tile", loadTexture(R.drawable.bathroom_floor_tile,false,false));
 		textureID.put("bathroom_wall_tile", loadTexture(R.drawable.bathroom_wall_tile,false,false));
 		textureID.put("floor_hardwood", loadTexture(R.drawable.floor_hardwood,false,false));
 		textureID.put("wall_board", loadTexture(R.drawable.wall_board,false,false));
 		textureID.put("wall_plaster",loadTexture(R.drawable.wall_plaster,false,false));
 		textureID.put("prop_urn_body", loadTexture(R.drawable.prop_urn_body,false,false));
 		textureID.put("prop_urn_ash", loadTexture(R.drawable.prop_urn_ash,false,false));
 		textureID.put("prop_urn_sticks", loadTexture(R.drawable.prop_urn_sticks,false,false));
 		textureID.put("prop_bed", loadTexture(R.drawable.prop_bed,false,false));
 		textureID.put("prop_body", loadTexture(R.drawable.prop_body,false,false));
 		textureID.put("prop_drawer", loadTexture(R.drawable.prop_drawer,false,false));
 		textureID.put("prop_pharoah", loadTexture(R.drawable.prop_pharoah,false,false));
 		textureID.put("prop_sink", loadTexture(R.drawable.prop_sink,false,false));
 		textureID.put("prop_soldier", loadTexture(R.drawable.prop_soldier,false,false));
 		textureID.put("prop_toilet", loadTexture(R.drawable.prop_toilet,false,false));
 		textureID.put("prop_tpaper", loadTexture(R.drawable.prop_tpaper,false,false));
 		textureID.put("prop_warrior", loadTexture(R.drawable.prop_warrior,false,false));
 		textureID.put("prop_phone_receiver", loadTexture(R.drawable.prop_phone_receiver,false,false));
 		textureID.put("prop_phone_base", loadTexture(R.drawable.prop_phone_base,false,false));
 		textureID.put("prop_frame1", loadTexture(R.drawable.prop_frame1,false,false));
 		textureID.put("door_bathroom", loadTexture(R.drawable.door_bathroom,false,false));
 		textureID.put("door_fake", loadTexture(R.drawable.door_fake,false,false));
 		textureID.put("joystick_knob", loadTexture(R.drawable.joystick_knob,true,true));
 		textureID.put("joystick_ring", loadTexture(R.drawable.joystick_ring,true,true));
 		textureID.put("ui_poi", loadTexture(R.drawable.ui_poi,true,true));
 		textureID.put("decal_wall_day2", loadTexture(R.drawable.decal_wall_day2,false,true));
 		textureID.put("decal_wall_day3", loadTexture(R.drawable.decal_wall_day3,false,true));
 		textureID.put("decal_wall_day4", loadTexture(R.drawable.decal_wall_day4,false,true));
 		textureID.put("decal_wall_day5", loadTexture(R.drawable.decal_wall_day5,false,true));
 		textureID.put("decal_board_day3", loadTexture(R.drawable.decal_board_day3,false,true));
 		textureID.put("decal_board_day4", loadTexture(R.drawable.decal_board_day4,false,true));
 		textureID.put("decal_board_day5", loadTexture(R.drawable.decal_board_day5,false,true));		
 		textureID.put("decal_small_crack", loadTexture(R.drawable.decal_small_crack,true,true));
 		textureID.put("decal_big_crack", loadTexture(R.drawable.decal_big_crack,true,true));
 		textureID.put("decal_number", loadTexture(R.drawable.decal_number,true,true));
 		textureID.put("portrait_girl", loadTexture(R.drawable.portrait_girl,true,true));
 	}
 	
 	public int getTextureID(String textureName)
 	{
 		if(textureName == null)
 			return invalidTextureID;
 		
 		Integer id = textureID.get(textureName);
 		
 		if(id == null)
 			return invalidTextureID;
 		
 		return id;
 	}
 	
 	private int loadTexture(int resourceID, boolean clampU, boolean clampV)
 	{
 		Bitmap img = null;
 		int texture[] = new int[1];
 		try
 		{
			img = BitmapFactory.decodeResource(Global.mainActivity.getResources(), resourceID);						
 			
 			GLES20.glGenTextures(1, texture, 0);	
 			
 			//select the texture
 			GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture[0]);
 			
 			//set its mipmap settings
 			GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
 			GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR_MIPMAP_LINEAR);
 			
 			//set its wrapping settings
 			if(clampU)
 				GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
 			else
 				GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_REPEAT);
 			
 			if(clampV)
 				GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
 			else
 				GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_REPEAT);
 
         	//determine texture format automatically
 			GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, img, 0);
 			
 			//generate mipmaps
 			GLES20.glHint(GLES20.GL_GENERATE_MIPMAP_HINT, GLES20.GL_NICEST);
 			GLES20.glGenerateMipmap(GLES20.GL_TEXTURE_2D);
 			
 			Log.d("ResourceLoader", "Loaded texture "+img.getHeight()+"x"+img.getWidth());
 			
 			//deseelct the texture
 			GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
 		}
 		catch (Exception e)
 		{
 			Log.d("ResourceLoader", e.toString()+ ":" + e.getMessage());
 		}
 		img.recycle();
 		return texture[0];		
 	}	
 	
 	private RTextureLoader()
 	{
 	
 	}
 	
 	
 	private HashMap<String,Integer> textureID;
 	private static RTextureLoader instance;
 }
