 /*
  * Copyright 2011, Erik Lund
  *
  * This file is part of Voxicity.
  *
  *  Voxicity is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  *
  *  Voxicity is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with Voxicity.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package voxicity;
 
 import org.lwjgl.LWJGLException;
 import org.lwjgl.input.Keyboard;
 import org.lwjgl.input.Mouse;
 import org.lwjgl.opengl.Display;
 import org.lwjgl.opengl.DisplayMode;
 import org.lwjgl.opengl.EXTTextureArray;
 import org.lwjgl.opengl.GL11;
 import org.lwjgl.opengl.GL13;
 import org.lwjgl.opengl.GL20;
 import org.lwjgl.opengl.GLContext;
 import org.lwjgl.util.glu.GLU;
 import org.lwjgl.util.vector.Vector3f;
 import org.lwjgl.util.vector.Matrix3f;
 import org.lwjgl.util.Color;
 import org.lwjgl.Sys;
 
 import java.io.File;
 import java.io.PrintStream;
 import java.io.FileNotFoundException;
 import java.util.List;
 import java.util.ArrayList;
 
 public class Voxicity
 {
 
 	long last_fps_update = 0;
 	int fps_count = 0;
 
 	static int fps = 0;
 
 	long last_frame = 0;
 
 	long last_block_change = 0;
 
 	float rot_x;
 	float rot_y;
 	float mouse_speed = 2.0f;
 	float camera_offset = 0.75f;
 
 	public static Frustum cam_vol = new Frustum();
 
 	static Vector3f camera = new Vector3f();
 
 	Vector3f last_pos = new Vector3f();
 
 	Vector3f accel = new Vector3f( 0, 0, 0 );
 	Vector3f move_speed = new Vector3f();
 
 	Vector3f place_loc = new Vector3f( 0, 0, 0 );
 	Vector3f look_vec = new Vector3f();
 
 	boolean is_close_requested = false;
 	boolean jumping = true;
 	boolean flying = true;
 
 	Server server;
 	Client client;
 	static ConnectionGlue conn_glue;
 
 	World world;
 
 	Block floating_block;
 
 	Thread chunk_loader;
 
 	public Voxicity( Server server, Client client )
 	{
 		this.server = server;
 		this.client = client;
 	}
 
 	public void init()
 	{
 		try
 		{
 			System.out.println( "Intializing display" );
 			Display.setDisplayMode( new DisplayMode( 1200, 720 ) );
 			Display.create();
 			System.out.println( "Display created" );
 		}
 		catch ( LWJGLException e )
 		{
 			e.printStackTrace();
 			System.exit(0);
 		}
 
 		TextRenderer.init();
 
 		setup_camera();
 		Mouse.setGrabbed( true );
 		world = client.world;
 
 		System.out.println( "Setting up OpenGL states" );
 		GL11.glShadeModel( GL11.GL_SMOOTH );
 		GL11.glEnable( GL11.GL_DEPTH_TEST );
 		GL11.glEnable( GL11.GL_TEXTURE_2D );
 		GL11.glEnable( GL11.GL_CULL_FACE );
 		GL11.glEnable( GL11.GL_BLEND );
 		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
 		GL11.glClearColor( 126.0f / 255.0f, 169.0f / 255.0f, 254.0f / 255.0f, 1.0f );
 
 		System.out.println( "Checking for GL_TEXTURE_2D_ARRAY_EXT: " + GLContext.getCapabilities().GL_EXT_texture_array );
 		GL11.glEnable( EXTTextureArray.GL_TEXTURE_2D_ARRAY_EXT );
 
 		System.out.println( "Number of texture units: " + GL11.glGetInteger( GL13.GL_MAX_TEXTURE_UNITS ) );
 		System.out.println( "Number of image texture units: " + GL11.glGetInteger( GL20.GL_MAX_TEXTURE_IMAGE_UNITS ) );
 		System.out.println( "Number of vertex texture units: " + GL11.glGetInteger( GL20.GL_MAX_VERTEX_TEXTURE_IMAGE_UNITS ) );
 		System.out.println( "Number of combined vertex/image texture units: " + GL11.glGetInteger( GL20.GL_MAX_COMBINED_TEXTURE_IMAGE_UNITS ) );
 
 		load_texture_pack();
 
 		start_chunk_loader();
 
 		last_fps_update = Time.get_time_ms();
 		get_time_delta();
 
 		while ( !is_close_requested )
 		{
 			System.out.println( "Update at " + Time.get_time_µs() );
 			client.update();
 			update( get_time_delta() / 1000.0f );
 			System.out.println( "Load new chunks at " + Time.get_time_µs() );
 			server.update();
 			System.out.println( "Render at " + Time.get_time_µs() );
 			client.renderer.render( cam_vol );
 			System.out.println( "Loop done" );
 
 			is_close_requested |= Display.isCloseRequested();
 		}
 
 		shutdown();
 	}
 
 	void start_chunk_loader()
 	{
 		Thread chunk_loader = new Thread()
 		{
 			public void run()
 			{
 				while( true )
 				{
 					int view = 4;
 					for ( int x = -view ; x <= view ; x++ )
 						for ( int y = -view ; y <= view ; y++ )
 							for ( int z = -view ; z <= view ; z++ )
 							{
 								if ( is_close_requested )
 									return;
 
 								server.load_chunk(camera.x + Constants.Chunk.side_length * x, camera.y + Constants.Chunk.side_length * y, camera.z + Constants.Chunk.side_length * z ); 
 
 							}
 					try
 					{
 						Thread.currentThread().sleep( 1000 );
 					}
 					catch ( Exception e )
 					{
 						e.printStackTrace();
 					}
 				}
 			}
 		};
 
 		chunk_loader.start();
 		this.chunk_loader = chunk_loader;
 	}
 
 	int get_time_delta()
 	{
 		// Get the time in milliseconds
 		long new_time = Time.get_time_ms();
 		int delta = (int) ( new_time - last_frame );
 		last_frame = new_time;
 		return delta;
 	}
 
 	void update( float delta )
 	{
 		//Store the new last position
 		last_pos.set( camera );
 
 		if ( Keyboard.isKeyDown( Keyboard.KEY_ESCAPE ) )
 			is_close_requested = true;
 
 		if ( Keyboard.isKeyDown( Keyboard.KEY_Q ) )
 			is_close_requested = true;
 
 		while ( Keyboard.next() )
 		{
 			if ( Keyboard.getEventKey() == Keyboard.KEY_E && Keyboard.getEventKeyState() )
 				toggle_mouse_grab();
 
 			if ( Keyboard.getEventKey() == Keyboard.KEY_G && Keyboard.getEventKeyState() )
 			{
 				toggle_flying();
 			}
 		}
 
 		if ( Mouse.isGrabbed() )
 		{
 			float x_move = 0;
 			float z_move = 0;
 
 			if ( Keyboard.isKeyDown( Keyboard.KEY_A ) )
 				x_move -= 5;
 
 			if ( Keyboard.isKeyDown( Keyboard.KEY_D ) )
 				x_move += 5;
 
 			if ( Keyboard.isKeyDown( Keyboard.KEY_W ) )
 				z_move -= 5;
 
 			if ( Keyboard.isKeyDown( Keyboard.KEY_S ) )
 				z_move += 5;
 
 			if ( flying )
 			{
 				if ( Keyboard.isKeyDown( Keyboard.KEY_SPACE ) ) camera.y += 5 * delta;
 				if ( Keyboard.isKeyDown( Keyboard.KEY_C ) ) camera.y -= 5 * delta;
 			}
 			else
 			{
 				if ( Keyboard.isKeyDown( Keyboard.KEY_SPACE ) && !jumping )
 				{
 					move_speed.y = 8.0f;
 					jumping = true;
 				}
 
 				if ( jumping )
 					accel.y = -23f;
 
 			}
 
 			if ( Mouse.isButtonDown( 0 ) )
 				place_block();
 
 			if ( Mouse.isButtonDown( 1 ) )
 				remove_block();
 
 			int x_delta = Mouse.getDX();
 			int y_delta = Mouse.getDY();
 
 			rot_x += ( x_delta / 800.0f ) * 45.0f * mouse_speed;
 			rot_y += ( y_delta / 800.0f ) * 45.0f * mouse_speed;
 
 			rot_x = rot_x > 360.0f ? rot_x - 360.0f : rot_x;
 			rot_x = rot_x < -360.0 ? rot_x + 360.0f : rot_x;
 
			rot_y = Math.min( rot_y, 90.0f );
			rot_y = Math.max( rot_y, -90.0f );
 
 			float cos_rot_x = ( float ) Math.cos( Math.toRadians( rot_x ) );
 			float sin_rot_x = ( float ) Math.sin( Math.toRadians( rot_x ) );
 			float cos_rot_y = ( float ) Math.cos( Math.toRadians( rot_y ) );
 			float sin_rot_y = ( float ) Math.sin( Math.toRadians( rot_y ) );
 
 			float corr_x = ( x_move * cos_rot_x ) - ( z_move * sin_rot_x );
 			float corr_z = ( x_move * sin_rot_x ) + ( z_move * cos_rot_x );
 
 			accel.x = corr_x;
 			accel.z = corr_z;
 
 			move_speed.x = accel.x; 
 			move_speed.y += accel.y * delta;
 			move_speed.z = accel.z;
 
 			camera.x += move_speed.x * delta;
 			camera.y+= move_speed.y * delta;
 			camera.z += move_speed.z * delta;
 
 			// Set the look vector
 			look_vec.set( sin_rot_x * cos_rot_y * 4, sin_rot_y * 4, cos_rot_x * cos_rot_y * -4 );
 		}
 
 		System.out.println( "Check collisions at " + Time.get_time_µs() );
 		check_collisions();
 		System.out.println( "Done checking collisions at " + Time.get_time_µs() );
 
 		calc_place_loc();
 
 		cam_vol.set_pos( new Vector3f( camera.x, camera.y + camera_offset, camera.z ), new Vector3f( camera.x + look_vec.x, camera.y + camera_offset + look_vec.y, camera.z + look_vec.z ), new Vector3f( 0, 1, 0 ) );
 
 		update_fps();
 	}
 
 	void update_fps()
 	{
 		if ( Time.get_time_ms() - last_fps_update > 250 )
 		{
 			fps = fps_count * 4;
 			fps_count = 0;
 			last_fps_update += 250;
 			Display.setTitle( "Voxicity - FPS: " + fps );
 		}
 		fps_count++;
 	}
 
 
 	void setup_camera()
 	{
 		GL11.glMatrixMode(GL11.GL_PROJECTION);
 		GL11.glLoadIdentity();
 		GLU.gluPerspective( 45.0f, 1200 / 720.0f, 0.1f, 100f );
 		GL11.glMatrixMode(GL11.GL_MODELVIEW);
 
 		camera.x = 0;
 		camera.y = 3;
 		camera.z = 0;
 		rot_x = 0;
 		rot_y = 0;
 
 		cam_vol.set_attribs( 45.0f, 1200 / 720.0f, 0.1f, 100.0f );
 	}
 
 	void toggle_mouse_grab()
 	{
 		Mouse.setGrabbed( !Mouse.isGrabbed() );
 	}
 
 	void toggle_flying()
 	{
 		if ( flying == false )
 		{
 			flying = true;
 			accel.y = 0;
 			move_speed.y = 0;
 			System.out.println( "Flying is " + flying );
 		}
 		else
 		{
 			flying = false;
 			jumping = true;
 			System.out.println( "Flying is " + flying );
 		}
 	}
 
 	void check_collisions()
 	{
 		int slice_num = 10;
 
 		boolean collided_x = false;
 		boolean collided_y = false;
 		boolean collided_z = false;
 
 		AABB player = new AABB( 0.5f, 1.7f, 0.5f );
 
 		Vector3f new_pos = new Vector3f( camera.x, camera.y, camera.z );
 
 		Vector3f slice_distance = new Vector3f();
 		Vector3f.sub( new_pos, last_pos, slice_distance );
 		slice_distance.scale( 1.0f / ( slice_num * 1.0f ) );
 
 		Vector3f slice_pos = new Vector3f( last_pos );
 		player.pos = slice_pos;
 
 		Vector3f corrected_pos = new Vector3f( new_pos );
 
 		// Check y axis for collisions
 		for ( int i = 0 ; i < slice_num ; i++ )
 		{
 			
 			Vector3f.add( slice_pos, slice_distance, slice_pos );
 
 			AABB above = world.get_hit_box( Math.round( player.pos.x ), Math.round( player.top() ), Math.round( player.pos.z ) );
 			if ( above != null )
 			{
 				if ( player.collides( above ) )
 				{
 					collided_y = true;
 					player.pos.y += above.bottom_intersect( player );
 					move_speed.y = -move_speed.y;
 					accel.y = 0;
 				}
 			}
 
 			AABB beneath = world.get_hit_box( Math.round( player.pos.x), Math.round( player.bottom() - 0.01f ), Math.round(player.pos.z) );
 			if ( beneath != null )
 			{
 				if ( player.collides( beneath ) && move_speed.y < 0 )
 				{
 					collided_y = true;
 					player.pos.y += beneath.top_intersect( player ) + 0.0001f;
 					move_speed.y = 0;
 					accel.y = 0;
 					jumping = false;
 				}
 			}
 			else
 			{
 				jumping = true;
 			}
 
 			if ( collided_y )
 				break;
 		}
 
 		corrected_pos.y = slice_pos.y;
 		slice_pos = new Vector3f( last_pos );
 		player.pos = slice_pos;
 
 		// Check x axis for collisions
 		for ( int i = 0 ; i < slice_num ; i++ )
 		{
 
 			Vector3f.add( slice_pos, slice_distance, slice_pos );
 
 			AABB upper_neg_x = world.get_hit_box( Math.round(player.left()), Math.round(player.top()), Math.round(player.pos.z) );
 			if ( upper_neg_x != null )
 			{
 				if ( player.collides( upper_neg_x ) )
 				{
 					collided_x = true;
 					move_speed.x = 0;
 					player.pos.x += upper_neg_x.right_intersect( player ) + 0.0001f;
 				}
 			}
 
 			AABB upper_pos_x = world.get_hit_box( Math.round(player.right()), Math.round(player.top()), Math.round(player.pos.z) );
 			if ( upper_pos_x != null )
 			{
 				if ( player.collides( upper_pos_x ) )
 				{
 					collided_x = true;
 					move_speed.x = 0;
 					player.pos.x += upper_pos_x.left_intersect( player ) - 0.0001f;
 				}
 			}
 
 			AABB lower_neg_x = world.get_hit_box( Math.round(player.left()), Math.round(player.bottom()), Math.round(player.pos.z) );
 			if ( lower_neg_x != null )
 			{
 				if ( player.collides( lower_neg_x ) )
 				{
 					collided_x = true;
 					move_speed.x = 0;
 
 					if ( Math.abs( lower_neg_x.right_intersect( player ) ) < Math.abs( lower_neg_x.top_intersect( player ) ) )
 						player.pos.x += lower_neg_x.right_intersect( player ) + 0.0001f;
 				}
 			}
 
 			AABB lower_pos_x = world.get_hit_box( Math.round(player.right()), Math.round(player.bottom()), Math.round(player.pos.z) );
 			if ( lower_pos_x != null )
 			{
 				if ( player.collides( lower_pos_x ) )
 				{
 					collided_x = true;
 					move_speed.x = 0;
 					if ( Math.abs( lower_pos_x.left_intersect( player ) ) < Math.abs( lower_pos_x.top_intersect( player ) ) )
 						player.pos.x += lower_pos_x.left_intersect( player ) - 0.0001f;
 				}
 			}
 
 			if ( collided_x )
 				break;
 		}
 
 		corrected_pos.x = slice_pos.x;
 		slice_pos = new Vector3f( last_pos );
 		player.pos = slice_pos;
 
 		for ( int i = 0 ; i < slice_num ; i++ )
 		{
 			Vector3f.add( slice_pos, slice_distance, slice_pos );
 
 			AABB upper_neg_z = world.get_hit_box( Math.round(player.pos.x), Math.round(player.top()), Math.round(player.back()) );
 			if ( upper_neg_z != null )
 			{
 				if ( player.collides( upper_neg_z ) )
 				{
 					collided_z = true;
 					move_speed.z = 0;
 					player.pos.z += upper_neg_z.front_intersect( player ) + 0.0001f;
 				}
 			}
 
 			AABB upper_pos_z = world.get_hit_box( Math.round(player.pos.x), Math.round(player.top()), Math.round(player.front()) );
 			if ( upper_pos_z != null )
 			{
 				if ( player.collides( upper_pos_z ) )
 				{
 					collided_z = true;
 					move_speed.z = 0;
 					player.pos.z += upper_pos_z.back_intersect( player ) - 0.0001f;
 				}
 			}
 
 			AABB lower_neg_z = world.get_hit_box( Math.round(player.pos.x), Math.round(player.bottom()), Math.round(player.back()) );
 			if ( lower_neg_z != null )
 			{
 				if ( player.collides( lower_neg_z ) )
 				{
 					collided_z = true;
 					move_speed.z = 0;
 					if ( Math.abs( lower_neg_z.front_intersect( player ) ) < Math.abs( lower_neg_z.top_intersect( player ) ) )
 						player.pos.z += lower_neg_z.front_intersect( player ) + 0.0001f;
 				}
 			}
 
 			AABB lower_pos_z = world.get_hit_box( Math.round(player.pos.x), Math.round(player.bottom()), Math.round(player.front()) );
 			if ( lower_pos_z != null )
 			{
 				if ( player.collides( lower_pos_z ) )
 				{
 					collided_z = true;
 					move_speed.z = 0;
 					if ( Math.abs( lower_pos_z.back_intersect( player ) ) < Math.abs( lower_pos_z.top_intersect( player ) ) )
 						player.pos.z += lower_pos_z.back_intersect( player ) - 0.0001f;
 				}
 			}
 
 			if ( collided_z )
 				break;
 		}
 
 		corrected_pos.z = slice_pos.z;
 
 
 		if ( collided_x || collided_y || collided_z )
 		{
 			// Set the player's new position after collision checking/handling
 			camera.x = corrected_pos.x;
 			camera.y = corrected_pos.y;
 			camera.z = corrected_pos.z;
 		}
 	}
 
 	void calc_place_loc()
 	{
 		float nearest_distance = Float.POSITIVE_INFINITY;
 		BlockLoc nearest_block = new BlockLoc( Math.round( camera.x + look_vec.x ), Math.round( camera.y + camera_offset + look_vec.y ), Math.round( camera.z + look_vec.z ), world );
 
 		int x_incr = (int)Math.signum( look_vec.x );
 		int y_incr = (int)Math.signum( look_vec.y );
 		int z_incr = (int)Math.signum( look_vec.z );
 
 		for ( int x = Math.round( camera.x ) ; x != Math.round( camera.x + look_vec.x ) + x_incr ; x += x_incr)
 			for ( int y = Math.round( camera.y + camera_offset ) ; y != Math.round( camera.y + camera_offset + look_vec.y ) + y_incr ; y += y_incr )
 				for ( int z = Math.round( camera.z ) ; z != Math.round( camera.z + look_vec.z ) + z_incr ; z += z_incr )
 				{
 					AABB box = world.get_hit_box( x, y, z );
 
 					if ( box != null )
 					{
 						Float distance = box.collision_distance( new Vector3f( camera.x, camera.y + camera_offset, camera.z ), look_vec );
 						
 						if ( distance < nearest_distance )
 						{
 							nearest_distance = distance;
 							nearest_block.x = x;
 							nearest_block.y = y;
 							nearest_block.z = z;
 						}
 					}
 				}
 
 		place_loc.set( nearest_block.x, nearest_block.y, nearest_block.z );
 	}
 
 	boolean can_change_block()
 	{
 		return Time.get_time_ms() - last_block_change > ( 1000 / 5 );
 	}
 
 	void place_block()
 	{
 		if ( can_change_block() )
 			last_block_change = Time.get_time_ms();
 		else
 			return;
 
 		int id = world.get_block( place_loc.x, place_loc.y, place_loc.z );
 		if ( id == Constants.Blocks.air )
 		{
 			world.set_block( place_loc.x, place_loc.y, place_loc.z, Constants.Blocks.dirt );
 		}
 		else
 		{
 			switch( world.get_hit_box( Math.round(place_loc.x), Math.round(place_loc.y), Math.round(place_loc.z) ).collision_side( new Vector3f( camera.x, camera.y + camera_offset, camera.z ), look_vec ) )
 			{
 				case Up:
 					world.set_block( place_loc.x, place_loc.y + 1, place_loc.z, Constants.Blocks.dirt );
 					break;
 				case Down:
 					world.set_block( place_loc.x, place_loc.y - 1, place_loc.z, Constants.Blocks.dirt );
 					break;
 				case West:
 					world.set_block( place_loc.x - 1, place_loc.y, place_loc.z, Constants.Blocks.dirt );
 					break;
 				case East:
 					world.set_block( place_loc.x + 1, place_loc.y, place_loc.z, Constants.Blocks.dirt );
 					break;
 				case North:
 					world.set_block( place_loc.x, place_loc.y, place_loc.z + 1, Constants.Blocks.dirt );
 					break;
 				case South:
 					world.set_block( place_loc.x, place_loc.y, place_loc.z - 1, Constants.Blocks.dirt );
 					break;
 			}
 
 		}
 	}
 
 	void remove_block()
 	{
 		if ( can_change_block() )
 			last_block_change = Time.get_time_ms();
 		else
 			return;
 
 		if ( world.get_block( place_loc.x, place_loc.y, place_loc.z ) != Constants.Blocks.air )
 		{
 			world.set_block( place_loc.x, place_loc.y, place_loc.z, Constants.Blocks.air );
 		}
 	}
 
 	void get_system_info()
 	{
 		
 	}
 
 	void shutdown()
 	{
 		conn_glue.quit();
 		try
 		{
 			chunk_loader.join();
 		}
 		catch ( Exception e )
 		{
 			e.printStackTrace();
 		}
 
 		server.shutdown();
 		System.out.println( "Destroying display" );
 		Display.destroy();
 	}
 
 	void load_texture_pack()
 	{
 		TextureManager.get_texture( "textures/stone.png" );
 		TextureManager.get_texture( "textures/dirt.png" );
 		TextureManager.get_texture( "textures/grass.png" );
 	}
 
 	public static void main( String[] args )
 	{
 		try
 		{
 			File new_out = new File( "voxicity.log" );
 			System.setOut( new PrintStream( new_out ) );
 
 			Config config = new Config( "voxicity.properties" );
 
 			Connection server_conn = new Connection();
 			Connection client_conn = new Connection();
 
 			conn_glue = new ConnectionGlue( server_conn, client_conn );
 			new Thread( conn_glue ).start();
 
 			Server server = new Server( config );
 			Client client = new Client( config, client_conn );
 
 			server.new_connection( server_conn );
 
 			Voxicity voxy = new Voxicity( server, client );
 			voxy.init();
 		}
 		catch ( Exception e )
 		{
 			e.printStackTrace();
 		}
 	}
 }
