 package nz.gen.wellington.guardian.android.activities;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import nz.gen.wellington.guardian.android.R;
 import nz.gen.wellington.guardian.android.activities.ui.TagListPopulatingService;
 import nz.gen.wellington.guardian.android.api.ImageDAO;
 import nz.gen.wellington.guardian.android.factories.ArticleSetFactory;
 import nz.gen.wellington.guardian.android.factories.SingletonFactory;
 import nz.gen.wellington.guardian.android.model.Article;
 import nz.gen.wellington.guardian.android.network.NetworkStatusService;
 import nz.gen.wellington.guardian.android.usersettings.FavouriteSectionsAndTagsDAO;
 import nz.gen.wellington.guardian.android.usersettings.PreferencesDAO;
 import android.graphics.Bitmap;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.util.TypedValue;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.Window;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class article extends MenuedActivity implements FontResizingActivity {
 		
 	private static final String REMOVE_SAVED_ARTICLE = "Remove saved article";
 	private static final String SAVE_ARTICLE = "Save article";
 	
 	private NetworkStatusService networkStatusService;
     private ImageDAO imageDAO;
     private PreferencesDAO preferencesDAO;
     private ArticleSetFactory articleSetFactory;
     private FavouriteSectionsAndTagsDAO favouriteSectionsAndTagsDAO;
     private Article article;
        
 	private MainImageUpdateHandler mainImageUpdateHandler;
     private MainImageLoader mainImageLoader;
 
     private Map<String, Bitmap> images;
 	private MenuItem favouriteMenuItem;
     
     
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		
 		imageDAO = SingletonFactory.getImageDao(this.getApplicationContext());
 		preferencesDAO = SingletonFactory.getPreferencesDAO(this.getApplicationContext());
 		articleSetFactory = SingletonFactory.getArticleSetFactory(this.getApplicationContext());
 		networkStatusService = new NetworkStatusService(this.getApplicationContext());
 		favouriteSectionsAndTagsDAO = SingletonFactory.getFavouriteSectionsAndTagsDAO(this.getApplicationContext());
 		
 		images = new HashMap<String, Bitmap>();
     	mainImageUpdateHandler = new MainImageUpdateHandler();
     	
 		requestWindowFeature(Window.FEATURE_NO_TITLE);
 		setContentView(R.layout.article);
 		
 		Article article = (Article) this.getIntent().getExtras().get("article");
 		this.article = article;
 		
 		if (article != null) {			
 			populateArticle(article);
 			
 		} else {
         	Toast.makeText(this, "Could not load article", Toast.LENGTH_SHORT).show();
 		}	
 	}
 	
 	@Override
 	protected void onResume() {
 		super.onResume();
 		final int baseSize = preferencesDAO.getBaseFontSize();
 		setFontSize(baseSize);
 	}
 	
 	@Override
 	protected void onPause() {
 		super.onPause();
 		images.clear();
 	}
 
 	
 	private void populateArticle(Article article) {		
 		if (article.getSection() != null) {
 			setHeading(article.getSection().getName());
 			setHeadingColour(article.getSection().getColour());
 		}
 		
         TextView headline = (TextView) findViewById(R.id.Headline);
         TextView pubDate = (TextView) findViewById(R.id.PubDate);
         TextView byline = (TextView) findViewById(R.id.Byline);
         TextView standfirst = (TextView) findViewById(R.id.Standfirst);
         TextView description = (TextView) findViewById(R.id.Description);
         
 		final int baseSize = preferencesDAO.getBaseFontSize();
         setFontSize(baseSize);
         
         headline.setText(article.getTitle());
         if (article.getPubDate() != null) {
         	pubDate.setText(article.getPubDateString());
         }
         byline.setText(article.getByline());
         standfirst.setText(article.getStandfirst());
         description.setText(article.getDescription());
         
             	
     	final boolean connectionAvailable = networkStatusService.isConnectionAvailable();
     	
     	final String mainImageUrl = article.getMainImageUrl();
 		if (mainImageUrl != null) {			
 			TextView caption = (TextView) findViewById(R.id.Caption);
 			caption.setText(article.getCaption());
 			
 			final boolean isWifiConnectionAvailable = networkStatusService.isConnectionAvailable() && networkStatusService.isWifiConnection();
 			final boolean downloadMainImage = isWifiConnectionAvailable || (networkStatusService.isConnectionAvailable() && preferencesDAO.getLargePicturesPreference().equals("ALWAYS"));
 			
 			mainImageLoader = new MainImageLoader(imageDAO, article.getMainImageUrl(), downloadMainImage);
 			Thread loader = new Thread(mainImageLoader);
 			loader.start();			
 		}
 		
 		final boolean isTagged = !article.getAuthors().isEmpty() || !article.getKeywords().isEmpty();
 		if (isTagged) {
 			populateTags(article, connectionAvailable);
 		}
 	}
 
 
 	public void setFontSize(int baseSize) {
 		TextView headline = (TextView) findViewById(R.id.Headline);
 		TextView caption = (TextView) findViewById(R.id.Caption);
 		TextView pubDate = (TextView) findViewById(R.id.PubDate);
 		TextView byline = (TextView) findViewById(R.id.Byline);
 		TextView standfirst = (TextView) findViewById(R.id.Standfirst);
 		TextView description = (TextView) findViewById(R.id.Description);
 		
 		headline.setTextSize(TypedValue.COMPLEX_UNIT_PT, baseSize + 1);
 		pubDate.setTextSize(TypedValue.COMPLEX_UNIT_PT, baseSize - 2);
 		caption.setTextSize(TypedValue.COMPLEX_UNIT_PT, baseSize -2);
 		byline.setTextSize(TypedValue.COMPLEX_UNIT_PT, baseSize);
 		pubDate.setTextSize(TypedValue.COMPLEX_UNIT_PT, baseSize - 2);
         standfirst.setTextSize(TypedValue.COMPLEX_UNIT_PT, baseSize);
         description.setTextSize(TypedValue.COMPLEX_UNIT_PT, baseSize);
 	}
 	
 	private void populateTags(Article article, final boolean connectionAvailable) {
 		LayoutInflater inflater = LayoutInflater.from(this);
 		findViewById(R.id.TagLabel).setVisibility(View.VISIBLE);
 		TagListPopulatingService.populateTags(inflater, connectionAvailable, (LinearLayout) findViewById(R.id.AuthorList), articleSetFactory.getArticleSetsForTags(article.getAuthors()), this.getApplicationContext());
 		TagListPopulatingService.populateTags(inflater, connectionAvailable, (LinearLayout) findViewById(R.id.TagList), articleSetFactory.getArticleSetsForTags(article.getKeywords()), this.getApplicationContext());
 	}
 
 	private void populateMainImage(String mainImageUrl) {
 		if (article != null && article.getMainImageUrl() != null && article.getMainImageUrl().equals(mainImageUrl)) {		
 			if (images.containsKey(mainImageUrl)) {		
 				Bitmap bitmap = images.get(mainImageUrl);
 				if (bitmap != null) {
 					ImageView imageView = (ImageView) findViewById(R.id.ArticleImage);
 					TextView caption = (TextView) findViewById(R.id.Caption);				
 					imageView.setImageBitmap(bitmap);			
 					imageView.setVisibility(View.VISIBLE);
 					caption.setVisibility(View.VISIBLE);
 				}
 			}
 		}
 	}
 	
 	
 	public boolean onCreateOptionsMenu(Menu menu) {
 		menu.add(0, 1, 0, "Home");
 		menu.add(0, 2, 0, "Favourites");
 	    menu.add(0, 3, 0, "Sections");	    
 	    if (favouriteSectionsAndTagsDAO.isSavedArticle(article)) {
 	    	favouriteMenuItem = menu.add(0, 4, 0, REMOVE_SAVED_ARTICLE);
 		} else {
 			favouriteMenuItem = menu.add(0, 4, 0, SAVE_ARTICLE);
 		}	    
 	    return true;
 	}
 	
 	
 	public boolean onOptionsItemSelected(MenuItem item) {
 	    switch (item.getItemId()) {
 	    case 1:
 	    	switchToMain();
 	    	return true;
 	    case 2: 	    	
 	    	switchToFavourites();
 	    	return true;	 
 	    case 3:
 	    	switchToSections();
 	    	return true;	    
 		case 4:
 			processSavedArticle(article);			
 	    	return true;
 	    }
 	    return false;
 	}
 	
 
 	private void processSavedArticle(Article article) {
 		if (!favouriteSectionsAndTagsDAO.isSavedArticle(article)) {
 			if (favouriteSectionsAndTagsDAO.addSavedArticle(article)) {
				favouriteMenuItem.setTitle(REMOVE_SAVED_ARTICLE);
 			} else {
 				Toast.makeText(this, "Saved articles list is full", Toast.LENGTH_LONG).show();
 			}			
 		} else {
 			if (favouriteSectionsAndTagsDAO.removeSavedArticle(article)) {
				favouriteMenuItem.setTitle(SAVE_ARTICLE);
 			} else {
 				Toast.makeText(this, "Saved articles list is full", Toast.LENGTH_LONG).show();
 			}
 		}
 	}
 
 		
 	class MainImageLoader implements Runnable {		
 
 		private ImageDAO imageDAO;
 		private String mainImageUrl;
 		private boolean shouldDownloadMainImage;
 		
 		public MainImageLoader(ImageDAO imageDAO, String mainImageUrl, boolean isWifiConnectionAvailable) {
 			this.imageDAO = imageDAO;
 			this.mainImageUrl = mainImageUrl;
 			this.shouldDownloadMainImage = isWifiConnectionAvailable;
 		}
 
 		@Override
 		public void run() {
 			Bitmap image = null;
 			if (imageDAO.isAvailableLocally(mainImageUrl) || shouldDownloadMainImage) {
 				image = imageDAO.getImage(mainImageUrl);
 			}
 			
 			if (image != null) {
 				images.put(mainImageUrl, image);
 				sendMainImageAvailableMessage(mainImageUrl);
 			}			
 			return;
 		}
 
 		private void sendMainImageAvailableMessage(String mainImageUrl) {
 			Message msg = new Message();
 			msg.what = MainImageUpdateHandler.MAIN_IMAGE_AVAILABLE;
 			msg.getData().putString("mainImageUrl", mainImageUrl);
 			mainImageUpdateHandler.sendMessage(msg);
 		}
 		
 	}
 		
 	
 	class MainImageUpdateHandler extends Handler {
 		
 		private static final int MAIN_IMAGE_AVAILABLE = 1;
 
 		public void handleMessage(Message msg) {
 			super.handleMessage(msg);
 			
 			switch (msg.what) {	   
 			    case MAIN_IMAGE_AVAILABLE:
 			    final String mainImageUrl = msg.getData().getString("mainImageUrl");
 			    populateMainImage(mainImageUrl);
 			}
 		}
 	}
 			
 }
