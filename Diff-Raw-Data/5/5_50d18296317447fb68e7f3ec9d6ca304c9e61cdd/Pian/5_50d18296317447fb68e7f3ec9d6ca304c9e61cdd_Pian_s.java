 package pian.client;
 
 import java.util.Iterator;
 
 import com.google.gwt.core.client.EntryPoint;
 import com.google.gwt.dom.client.Style.Unit;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.event.logical.shared.AttachEvent;
 import com.google.gwt.event.logical.shared.AttachEvent.Handler;
 import com.google.gwt.user.client.ui.Button;
 import com.google.gwt.user.client.ui.CheckBox;
 import com.google.gwt.user.client.ui.DockPanel;
 import com.google.gwt.user.client.ui.HTML;
 import com.google.gwt.user.client.ui.HorizontalPanel;
 import com.google.gwt.user.client.ui.Hyperlink;
 import com.google.gwt.user.client.ui.Image;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.RootPanel;
 import com.google.gwt.user.client.ui.SuggestBox;
 import com.google.gwt.user.client.ui.TabPanel;
 import com.google.gwt.user.client.ui.VerticalPanel;
 
 public class Pian implements EntryPoint {
 	private VerticalPanel pageContainer = new VerticalPanel();
 	
 	private SuggestBox searchBox = new SuggestBox();
 	private Button searchButton = new Button("Tìm nhạc");
 	private CheckBox allOptionCheckBox = new CheckBox("Tất cả");
 	private CheckBox titleCheckBox = new CheckBox("Bài hát");
 	private CheckBox artistCheckBox = new CheckBox("Ca sĩ");
 	private CheckBox albumCheckBox = new CheckBox("Album");
 	private Image logoImage = new Image();
 	private DockPanel searchPanel = new DockPanel();
 	private HorizontalPanel controlPanel = new HorizontalPanel();
 	private DockPanel optionPanel = new DockPanel();
 	
 	private DockPanel resultPanel = new DockPanel();
 	private TabPanel songPanel = new TabPanel();
 	private VerticalPanel allResultPanel = new VerticalPanel();
 	private VerticalPanel songResultPanel = new VerticalPanel();
 	private VerticalPanel artistResultPanel = new VerticalPanel();
 	private VerticalPanel albumResultPanel = new VerticalPanel();
 	private VerticalPanel playerPanel = new VerticalPanel();
 	private VerticalPanel playlistPanel = new VerticalPanel();
 	private Label playlistLabel = new Label("Playlist của bạn");
 	
 	
 	@Override
 	public void onModuleLoad() {
 		/* render full search box. */
 		logoImage.setUrl("images/logo.jpg");
 		logoImage.setSize("115px", "110px");
 		
 		searchBox.setWidth("300px");
 		
 		searchButton.getElement().getStyle().setPaddingLeft(10, Unit.PX);
 		
 		// add searchBox and searchButton to its panel.
 		controlPanel.setBorderWidth(0);
 		controlPanel.setWidth("400px");
 		controlPanel.add(searchBox);
 		controlPanel.add(searchButton);
 		
 		// add options to its panel.
 		optionPanel.add(controlPanel, DockPanel.NORTH);
 		optionPanel.add(allOptionCheckBox, DockPanel.WEST);
 		optionPanel.add(titleCheckBox, DockPanel.WEST);
 		optionPanel.add(artistCheckBox, DockPanel.WEST);
 		optionPanel.add(albumCheckBox, DockPanel.WEST);
 		
 		// Associate the logoImage, controlPanel, optionPanel with the searchPanel.
 		searchPanel.setVerticalAlignment(VerticalPanel.ALIGN_MIDDLE);
 		searchPanel.add(logoImage, DockPanel.WEST);
 		searchPanel.add(optionPanel, DockPanel.EAST);
 		/* render full search box. */
 		
 		/* Test result panel */
 		allResultPanel.setSpacing(5);
 		allResultPanel.add(renderCellPanel("http://stream3.mp3.zdn.vn/ES3stAYFQh/452b03c7ccc92e8bc7b50838207651e2/4eccceb0/2011/11/22/5/2/52e7b9a0ee8627037ca3a2a8235012fe.mp3", "Làm Ơn", "Trần Trung Đức", "ab001", "Đang cập nhật"));
 		allResultPanel.add(renderCellPanel("http://stream.mp3.zdn.vn/fsfsdfdsfdserwrwq3/4bf90c3cf20967f828e3d33e2e774d74/4eccd9c0/d/cc/dcc132fbbc40e0d1c12c28523249bb91.mp3", "Vài Lần Đón Đưa", "Lê Hiếu", "ab001", "Đang cập nhật"));
 		/* Test result panel */
 		
 		/* render result box. */
 		songPanel.setWidth("700px");
 		songPanel.add(allResultPanel, "Tất cả");
 		songPanel.add(songResultPanel, "Tựa đề");
 		songPanel.add(artistResultPanel, "Ca sĩ");
 		songPanel.add(albumResultPanel, "Album");
 		songPanel.selectTab(0);
 		
 		
 		
 		playerPanel.setPixelSize(280, 30);
 		playerPanel.add(new HTML("<div id=\"mainPlayer\">Loading the player ...</div>"));
 		playerPanel.addAttachHandler(new Handler() {
 			@Override
 			public void onAttachOrDetach(AttachEvent event) {
 				if (event.isAttached())
 					Pian.renderPlayer();
 				
 			}
 		});
 		/*playerPanel.add(new HTML("<embed "+
 				"flashvars=\"file=http://media.vinahoo.com/secdl/1752f958c3cc8fdd1798dbc19758a530/4eccb263/bbmp3/music/2010/10/11/GBC1Q9.mp3\" " +
 				"allowfullscreen=\"false\" " +
 				"allowscripaccess=\"always\" " +
 				"id=\"player1\" " +
 				"name=\"player1\" " +
 				"src=\"player/player.swf\" " +
 				"width=\"270\" " +
 				"height=\"24\" " +
 				"/>"));*/
 		
 		playlistPanel.setWidth("280px");
 		playlistPanel.add(playlistLabel);
 		
 		// Associate the songPanel, playerPanel, playlistPanel with the searchPanel.
 		resultPanel.setWidth("980px");
 		resultPanel.setHeight("100%");
 		resultPanel.setSpacing(5);
 		resultPanel.add(songPanel, DockPanel.WEST);
 		resultPanel.add(playerPanel, DockPanel.NORTH);
 		resultPanel.add(playlistPanel, DockPanel.NORTH);
 		
 		// Associate the searchPanel, resultPanel with the pageContainer.
 		pageContainer.setBorderWidth(0);
 		pageContainer.setWidth("100%");
 		pageContainer.setHorizontalAlignment(HorizontalPanel.ALIGN_CENTER);
 		pageContainer.setSpacing(10);
 		pageContainer.add(searchPanel);
 		pageContainer.add(resultPanel);
 		
 		// Associate the pageContainer with the RootPanel to display it.
 		RootPanel.get().add(pageContainer);
 		
 		// move cursor focus to the input box.
 		searchBox.setFocus(true);
 	}
 
 	private DockPanel renderCellPanel(final String id, String songTitle, String artist, String albumId, String album) {
 		Hyperlink titleLink = new Hyperlink(songTitle, id);
 		titleLink.addClickHandler(new ClickHandler() {
 			@Override
 			public void onClick(ClickEvent event) {
 				Pian.playSong(id);
 			}
 		});
 		Hyperlink albumNameLabel = new Hyperlink(album, albumId);
 		
 		VerticalPanel songInfoPanel = new VerticalPanel();
 		songInfoPanel.setWidth("500px");
 		songInfoPanel.add(titleLink);
 		songInfoPanel.add(albumNameLabel);
 		
 		Label addToPlaylistLabel = new Label("Thêm vào Playlist");
 		Label downloadLabel = new Label("Tải về");
 		
 		VerticalPanel songOperationPanel = new VerticalPanel();
 		songOperationPanel.setHorizontalAlignment(HorizontalPanel.ALIGN_RIGHT);
 		songOperationPanel.setWidth("150px");
 		songOperationPanel.add(addToPlaylistLabel);
 		songOperationPanel.add(downloadLabel);
 		
 		DockPanel cellPanel = new DockPanel();
 		cellPanel.setWidth("100%");
 		cellPanel.add(songInfoPanel, DockPanel.WEST);
 		cellPanel.add(songOperationPanel, DockPanel.EAST);
 		
 		return cellPanel;
 	}
 	
 	public static native void renderPlayer() /*-{
 		$wnd.jwplayer("mainPlayer").setup({
 			flashplayer: "/player/player.swf",
			file: "http://stream3.mp3.zdn.vn/ES3stAYFQh/452b03c7ccc92e8bc7b50838207651e2/4eccceb0/2011/11/22/5/2/52e7b9a0ee8627037ca3a2a8235012fe.mp3",
 			controlbar: "bottom",
 			width: 270,
 			height: 24,
 		});
 	}-*/;
 	
 	public static native void playSong(String url) /*-{
		//$wnd.jwplayer("mainPlayer").load(url);
 		$wnd.jwplayer("mainPlayer").play();
 		//alert(url);
 	}-*/;
 }
