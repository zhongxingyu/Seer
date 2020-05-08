 package com.memeworks.cardnight;
 
 import android.graphics.Canvas;
 import android.graphics.Paint;
 import android.graphics.Rect;
 import android.view.MotionEvent;
 
 /**
  * The Overlay Panel provides a transition effect between states as well as notifications to the user.
  * @author kolibabas
  *
  */
 public final class OverlayPanel {
 	
 	//UI State Enum
 	/** Current UI state of the panel */
 	private static int current_ui_state;
 	public static final int UI_PLAYER_COUNT	= 0;
 	public static final int UI_WAIT_PLAYERS	= 1;
 	public static final int UI_TURN_NOTIFY  = 2;
 	
 	//Panel State Enum
 	/** Current animation state of the panel */
 	private static int current_state;
 	
 	/** The panel is animating inbound */
 	public static final int STATE_INBOUND	= 0;
 	
 	/** The panel is done animating and is shown */
 	public static final int STATE_SHOWN 	= 1;
 	
 	/** The panel is animating outbound */
 	public static final int STATE_OUTBOUND  = 2;
 	
 	/** The panel is hidden */
 	public static final int STATE_HIDDEN    = 3;
 
 	//Private Variables
 	/** Whether the panel is visible */
 	public static boolean Visible;
 	
 	/** Screen position of left side panel */
 	private static float left_panel_pos_x;
 	
 	/** Screen position of right side panel */
 	private static float right_panel_pos_x;
 	
 	/** Current animation time in seconds */
 	private static float animation_timer;
 	
 	/** Maximum time for panel animation in seconds */
 	private static final float max_animation_time_secs = 0.35f;
 	
 	//Main Text
 	/** Paint to control Control opacity */
 	private static Paint control_paint = new Paint();
 	
 	/** Main text size */
 	private static float overlay_text_size = 50.0f;
 	
 	/** Main text builder */
 	private static StringBuilder overlay_text = new StringBuilder();
 	
 	/** Main text holder */
 	private static final char overlay_chars[] = new char[100];
 	
 	//TODO Outline text
 	//TODO Player Count Text
 	
 	//Play Button
 	private static Rect play_button_rect = new Rect();
 	private static boolean play_button_pressed = false;
 	//Pass Button
 	private static Rect pass_button_rect = new Rect();
 	private static boolean pass_button_pressed = false;
 	//Go Button
 	private static Rect go_button_rect = new Rect();
 	private static boolean go_button_pressed = false;
 	
 	/**
 	 * Shows the panel with the given UI State
 	 * @param ui_state Enumerated UI State to draw (e.g. UI_DEAL_COUNT)
 	 */
 	public static void Show(int ui_state)
 	{
 		current_ui_state = ui_state;
 		current_state = STATE_INBOUND;
 		animation_timer = 0;
 		Visible = true; 
 		
 		left_panel_pos_x = 0 - Util.BMP_OVERLAY_PANEL_LEFT.getWidth();
 		right_panel_pos_x = Util.SCREEN_WIDTH;
 		
 		control_paint.setARGB(0, 255, 255, 255);
 		//overlay_paint.setTypeface(); //TODO Find/set game text font
 		control_paint.setTextSize(overlay_text_size);
 		control_paint.setTextAlign(Paint.Align.CENTER);
 		control_paint.setAntiAlias(true);
 		overlay_text.setLength(0);
 				
 		switch(ui_state)
 		{
 		case UI_PLAYER_COUNT:
			overlay_text.append(R.string.player_count_request); //TODO Needs a Context.getString()
 			go_button_rect.set(Util.SCREEN_CENTER_X - (Util.BMP_OVERLAY_PANEL_GO_BUTTON.getWidth() / 2),
 							   Util.SCREEN_HEIGHT - (2 * Util.BMP_OVERLAY_PANEL_GO_BUTTON.getHeight()), 
 							   Util.SCREEN_CENTER_X + (Util.BMP_OVERLAY_PANEL_GO_BUTTON.getWidth() / 2),
 							   Util.SCREEN_HEIGHT - (1 * Util.BMP_OVERLAY_PANEL_GO_BUTTON.getHeight()));
 			break;
 		case UI_WAIT_PLAYERS:
			overlay_text.append(R.string.waiting_for_players);
 			break;
 		case UI_TURN_NOTIFY:
			overlay_text.append(R.string.turn_notify);
 			play_button_rect.set(Util.SCREEN_CENTER_X - (Util.BMP_OVERLAY_PANEL_PLAY_BUTTON.getWidth()),
 							     Util.SCREEN_CENTER_Y, 
 							     Util.SCREEN_CENTER_X,
 							     Util.SCREEN_CENTER_Y + Util.BMP_OVERLAY_PANEL_PLAY_BUTTON.getHeight());
 			
 			pass_button_rect.set(Util.SCREEN_CENTER_X,
 							     Util.SCREEN_CENTER_Y, 
 							     Util.SCREEN_CENTER_X +  Util.BMP_OVERLAY_PANEL_PASS_BUTTON.getWidth(),
 							     Util.SCREEN_CENTER_Y + Util.BMP_OVERLAY_PANEL_PASS_BUTTON.getHeight());
 			break;
 		default:
 			break;
 		}
 	}
 	
 	public static Canvas Draw(Canvas c) {
 		if (Visible)
 		{
 			//Draw background panels
 			c.drawBitmap(Util.BMP_OVERLAY_PANEL_LEFT, left_panel_pos_x, 0, null);
 			c.drawBitmap(Util.BMP_OVERLAY_PANEL_RIGHT, right_panel_pos_x, 0, null);
 			
 			//Draw Text
 			overlay_text.getChars(0, overlay_text.length(), overlay_chars, 0);
 			c.drawText(overlay_chars, 0, overlay_text.length(), Util.SCREEN_CENTER_X, Util.SCREEN_HEIGHT * 0.25f, control_paint);
 			
 			//Draw Controls
 			switch (current_ui_state)
 			{
 			case UI_TURN_NOTIFY:
 				if (play_button_pressed)
 				{
 					c.drawBitmap(Util.BMP_OVERLAY_PANEL_PLAY_BUTTON_PRESSED, play_button_rect.left, play_button_rect.top, control_paint);
 				}
 				else
 				{
 					c.drawBitmap(Util.BMP_OVERLAY_PANEL_PLAY_BUTTON, play_button_rect.left, play_button_rect.top, control_paint);
 				}
 				
 				if (pass_button_pressed)
 				{
 					c.drawBitmap(Util.BMP_OVERLAY_PANEL_PASS_BUTTON_PRESSED, pass_button_rect.left, pass_button_rect.top, control_paint);
 				}
 				else
 				{
 					c.drawBitmap(Util.BMP_OVERLAY_PANEL_PASS_BUTTON, pass_button_rect.left, pass_button_rect.top, control_paint);
 				}
 				break;
 			case UI_PLAYER_COUNT:
 				if (go_button_pressed)
 				{
 					c.drawBitmap(Util.BMP_OVERLAY_PANEL_GO_BUTTON_PRESSED, go_button_rect.left, go_button_rect.top, control_paint);
 				}
 				else
 				{
 					c.drawBitmap(Util.BMP_OVERLAY_PANEL_GO_BUTTON, go_button_rect.left, go_button_rect.top, control_paint);
 				}
 			}
 		}
 		return c;
 	}
 
 	public static void Frame_Started(float frame_time) {
 		if (Visible)
 		{
 			if (current_state != STATE_SHOWN)
 			{
 				animation_timer += frame_time;
 				
 				if (animation_timer >= max_animation_time_secs)
 				{
 					if (current_state == STATE_INBOUND)
 					{
 						left_panel_pos_x = 0;
 						right_panel_pos_x = Util.SCREEN_CENTER_X;
 						control_paint.setARGB(255, 255, 255, 255);
 						current_state = STATE_SHOWN;
 					}
 					else if (current_state == STATE_OUTBOUND)
 					{
 						Visible = false;
 					}
 					
 					animation_timer = 0;
 				}
 				else if (current_state == STATE_INBOUND)
 				{
 					left_panel_pos_x = -1 * Util.BMP_OVERLAY_PANEL_LEFT.getWidth() * (1.0f - (animation_timer / max_animation_time_secs));
 					right_panel_pos_x = Util.SCREEN_CENTER_X + (Util.BMP_OVERLAY_PANEL_RIGHT.getWidth() * (1.0f - animation_timer / max_animation_time_secs));
 					control_paint.setARGB((int) (255 * (animation_timer / max_animation_time_secs)), 255, 255, 255);
 				}
 				else if (current_state == STATE_OUTBOUND)
 				{
 					left_panel_pos_x = -1 * Util.BMP_OVERLAY_PANEL_LEFT.getWidth() * (animation_timer / max_animation_time_secs);
 					right_panel_pos_x = Util.SCREEN_CENTER_X + (Util.BMP_OVERLAY_PANEL_RIGHT.getWidth() * (animation_timer / max_animation_time_secs));
 					control_paint.setARGB((int) (255 * (1.0f - (animation_timer / max_animation_time_secs))), 255, 255, 255);
 				}
 			}
 		}
 	}	
 
 	public static boolean Touch_Event(MotionEvent evt) {
 		if (Visible)
 		{
 			play_button_pressed = false;
 			pass_button_pressed = false;
 			go_button_pressed = false;
 			
 			switch(evt.getAction())
 			{
 			case MotionEvent.ACTION_DOWN:
 			case MotionEvent.ACTION_MOVE:
 				if (current_ui_state == UI_TURN_NOTIFY && play_button_rect.contains((int)evt.getX(), (int)evt.getY()))
 				{
 					play_button_pressed = true;
 				}
 				else if (current_ui_state == UI_TURN_NOTIFY && pass_button_rect.contains((int)evt.getX(), (int)evt.getY()))
 				{
 					pass_button_pressed = true;
 				}
 				else if (current_ui_state == UI_PLAYER_COUNT && go_button_rect.contains((int)evt.getX(), (int)evt.getY()))
 				{
 					go_button_pressed = true;
 				}				
 				break;
 			case MotionEvent.ACTION_UP:
 				if (current_state == STATE_SHOWN)
 				{
 					if (current_ui_state == UI_TURN_NOTIFY && play_button_rect.contains((int)evt.getX(), (int)evt.getY()))
 					{
 						current_state = STATE_OUTBOUND;
 					}
 					else if (current_ui_state == UI_TURN_NOTIFY && pass_button_rect.contains((int)evt.getX(), (int)evt.getY()))
 					{
 						current_state = STATE_OUTBOUND;
 						//TODO Pass current turn
 					}
 					else if (current_ui_state == UI_PLAYER_COUNT && go_button_rect.contains((int)evt.getX(), (int)evt.getY()))
 					{
 						current_state = STATE_OUTBOUND;
 						//TODO Begin round
 					}
 				}
 				break;
 			default:
 				break;
 			}
 			
 		}
 		return false;
 	}
 }
