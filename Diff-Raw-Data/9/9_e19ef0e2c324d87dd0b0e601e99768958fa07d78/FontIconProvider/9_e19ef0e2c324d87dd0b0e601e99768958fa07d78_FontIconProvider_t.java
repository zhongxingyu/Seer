 package blacksmyth.general;
 
 import java.awt.Font;
 import java.awt.GraphicsEnvironment;
 
 import javax.swing.AbstractButton;
 import javax.swing.JTabbedPane;
 
 /**
  * A bridge/singleton pattern implementation providing convenient access to the 
  * "Font Awesome" font set. The glyphs from this font set are used as an 
  * alternative to icon images in Swing components. 
  * @author linds
  */
 public final class FontIconProvider {
   private static final String FONT_FILENAME = "fontawesome-webfont.ttf";
   private static Font BASE_ICON_FONT;
   private static Font MAIN_ICON_FONT;
   
   private static FontIconProvider instance;
 
   protected FontIconProvider() {
     
     GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
 
     BASE_ICON_FONT = ResourceBridge.getFont(FONT_FILENAME);
     ge.registerFont(BASE_ICON_FONT);
     MAIN_ICON_FONT = BASE_ICON_FONT.deriveFont(Font.PLAIN, 24);
   }
   
   /**
    * Provides the single instance of this class, configured to provide 
    * icon glyphs from a dedicated font, accessible only via this instance.
    * @return
    */
   public static FontIconProvider getInstance() {
     if (instance == null) {
       instance = new FontIconProvider();
     }
     return instance;
   }
 
   /**
    * Configures the supplied <tt>button</tt> to display the provided
    * <tt>iconAsChar</tt> as button icon by using the specialised 
    * font within this class for glyph production.  The character
    * specified should only bee one of the characters supplied via
    * this class.
    * @param button
    * @param iconAsChar
    */
   public void setGlyphAsText(AbstractButton button, char iconAsChar) {
     button.setFont(MAIN_ICON_FONT);
     button.setText(String.valueOf(iconAsChar));
   }
 
   public void setGlyphAsTitle(JTabbedPane pane, int tabIndex, char iconAsChar) {
     pane.setFont(MAIN_ICON_FONT);
     pane.setTitleAt(tabIndex, String.valueOf(iconAsChar));
   }
   
   public static final char icon_glass = '\uf000';
   public static final char icon_music = '\uf001';
   public static final char icon_search = '\uf002';
   public static final char icon_envelope = '\uf003';
   public static final char icon_heart = '\uf004';
   public static final char icon_star = '\uf005';
   public static final char icon_star_empty = '\uf006';
   public static final char icon_user = '\uf007';
   public static final char icon_film = '\uf008';
   public static final char icon_th_large = '\uf009';
   public static final char icon_th = '\uf00a';
   public static final char icon_th_list = '\uf00b';
   public static final char icon_ok = '\uf00c';
   public static final char icon_remove = '\uf00d';
   public static final char icon_zoom_in = '\uf00e';
   public static final char icon_zoom_out = '\uf010';
   public static final char icon_off = '\uf011';
   public static final char icon_signal = '\uf012';
   public static final char icon_cog = '\uf013';
   public static final char icon_trash = '\uf014';
   public static final char icon_home = '\uf015';
   public static final char icon_file = '\uf016';
   public static final char icon_time = '\uf017';
   public static final char icon_road = '\uf018';
   public static final char icon_download_alt = '\uf019';
   public static final char icon_download = '\uf01a';
   public static final char icon_upload = '\uf01b';
   public static final char icon_inbox = '\uf01c';
   public static final char icon_play_circle = '\uf01d';
   public static final char icon_repeat = '\uf01e';
   public static final char icon_refresh = '\uf021';
   public static final char icon_list_alt = '\uf022';
   public static final char icon_lock = '\uf023';
   public static final char icon_flag = '\uf024';
   public static final char icon_headphones = '\uf025';
   public static final char icon_volume_off = '\uf026';
   public static final char icon_volume_down = '\uf027';
   public static final char icon_volume_up = '\uf028';
   public static final char icon_qrcode = '\uf029';
   public static final char icon_barcode = '\uf02a';
   public static final char icon_tag = '\uf02b';
   public static final char icon_tags = '\uf02c';
   public static final char icon_book = '\uf02d';
   public static final char icon_bookmark = '\uf02e';
   public static final char icon_print = '\uf02f';
   public static final char icon_camera = '\uf030';
   public static final char icon_font = '\uf031';
   public static final char icon_bold = '\uf032';
   public static final char icon_italic = '\uf033';
   public static final char icon_text_height = '\uf034';
   public static final char icon_text_width = '\uf035';
   public static final char icon_align_left = '\uf036';
   public static final char icon_align_center = '\uf037';
   public static final char icon_align_right = '\uf038';
   public static final char icon_align_justify = '\uf039';
   public static final char icon_list = '\uf03a';
   public static final char icon_indent_left = '\uf03b';
   public static final char icon_indent_right = '\uf03c';
   public static final char icon_facetime_video = '\uf03d';
   public static final char icon_picture = '\uf03e';
   public static final char icon_pencil = '\uf040';
   public static final char icon_map_marker = '\uf041';
   public static final char icon_adjust = '\uf042';
   public static final char icon_tint = '\uf043';
   public static final char icon_edit = '\uf044';
   public static final char icon_share = '\uf045';
   public static final char icon_check = '\uf046';
   public static final char icon_move = '\uf047';
   public static final char icon_step_backward = '\uf048';
   public static final char icon_fast_backward = '\uf049';
   public static final char icon_backward = '\uf04a';
   public static final char icon_play = '\uf04b';
   public static final char icon_pause = '\uf04c';
   public static final char icon_stop = '\uf04d';
   public static final char icon_forward = '\uf04e';
   public static final char icon_fast_forward = '\uf050';
   public static final char icon_step_forward = '\uf051';
   public static final char icon_eject = '\uf052';
   public static final char icon_chevron_left = '\uf053';
   public static final char icon_chevron_right = '\uf054';
   public static final char icon_plus_sign = '\uf055';
   public static final char icon_minus_sign = '\uf056';
   public static final char icon_remove_sign = '\uf057';
   public static final char icon_ok_sign = '\uf058';
   public static final char icon_question_sign = '\uf059';
   public static final char icon_info_sign = '\uf05a';
   public static final char icon_screenshot = '\uf05b';
   public static final char icon_remove_circle = '\uf05c';
   public static final char icon_ok_circle = '\uf05d';
   public static final char icon_ban_circle = '\uf05e';
   public static final char icon_arrow_left = '\uf060';
   public static final char icon_arrow_right = '\uf061';
   public static final char icon_arrow_up = '\uf062';
   public static final char icon_arrow_down = '\uf063';
   public static final char icon_share_alt = '\uf064';
   public static final char icon_resize_full = '\uf065';
   public static final char icon_resize_small = '\uf066';
   public static final char icon_plus = '\uf067';
   public static final char icon_minus = '\uf068';
   public static final char icon_asterisk = '\uf069';
   public static final char icon_exclamation_sign = '\uf06a';
   public static final char icon_gift = '\uf06b';
   public static final char icon_leaf = '\uf06c';
   public static final char icon_fire = '\uf06d';
   public static final char icon_eye_open = '\uf06e';
   public static final char icon_eye_close = '\uf070';
   public static final char icon_warning_sign = '\uf071';
   public static final char icon_plane = '\uf072';
   public static final char icon_calendar = '\uf073';
   public static final char icon_random = '\uf074';
   public static final char icon_comment = '\uf075';
   public static final char icon_magnet = '\uf076';
   public static final char icon_chevron_up = '\uf077';
   public static final char icon_chevron_down = '\uf078';
   public static final char icon_retweet = '\uf079';
   public static final char icon_shopping_cart = '\uf07a';
   public static final char icon_folder_close = '\uf07b';
   public static final char icon_folder_open = '\uf07c';
   public static final char icon_resize_vertical = '\uf07d';
   public static final char icon_resize_horizontal = '\uf07e';
   public static final char icon_bar_chart = '\uf080';
   public static final char icon_twitter_sign = '\uf081';
   public static final char icon_facebook_sign = '\uf082';
   public static final char icon_camera_retro = '\uf083';
   public static final char icon_key = '\uf084';
   public static final char icon_cogs = '\uf085';
   public static final char icon_comments = '\uf086';
   public static final char icon_thumbs_up = '\uf087';
   public static final char icon_thumbs_down = '\uf088';
   public static final char icon_star_half = '\uf089';
   public static final char icon_heart_empty = '\uf08a';
   public static final char icon_signout = '\uf08b';
   public static final char icon_linkedin_sign = '\uf08c';
   public static final char icon_pushpin = '\uf08d';
   public static final char icon_external_link = '\uf08e';
   public static final char icon_signin = '\uf090';
   public static final char icon_trophy = '\uf091';
   public static final char icon_github_sign = '\uf092';
   public static final char icon_upload_alt = '\uf093';
   public static final char icon_lemon = '\uf094';
 }
