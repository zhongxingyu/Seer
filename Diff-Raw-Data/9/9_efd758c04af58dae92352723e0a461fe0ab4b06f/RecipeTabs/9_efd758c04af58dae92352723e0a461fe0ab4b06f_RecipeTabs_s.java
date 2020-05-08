 package org.wrowclif.recipebox.ui;
 
 import org.wrowclif.recipebox.R;
 
 import org.wrowclif.recipebox.AppData;
 import org.wrowclif.recipebox.Recipe;
 import org.wrowclif.recipebox.impl.UtilityImpl;
 
 import android.content.Intent;
 import android.os.Bundle;
 
 import android.app.TabActivity;
 import android.graphics.Color;
 import android.view.Gravity;
 import android.view.View;
 import android.view.WindowManager;
 import android.widget.RelativeLayout;
 import android.widget.TabHost;
 import android.widget.TabHost.TabSpec;
 import android.widget.TabHost.OnTabChangeListener;
 import android.widget.TextView;
 
 public class RecipeTabs extends TabActivity {
 
 	protected Recipe curRecipe;
 	protected boolean editing;
 
 	private int active_text_color;
 	private int inactive_text_color;
 
 	public void onCreate(Bundle savedInstance) {
 		super.onCreate(savedInstance);
 		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
 		setContentView(R.layout.recipe_tabs);
 
 		TabHost host = getTabHost();
 		TabSpec spec = null;
 		Intent intent = getIntent();
 		long id = intent.getLongExtra("id", -1);
 		boolean edit = intent.getBooleanExtra("edit", false);
 		int tab = intent.getIntExtra("tab", 0);
 
 		this.curRecipe = UtilityImpl.singleton.getRecipeById(id);
 		this.editing = edit;
 
 		setTitle(curRecipe.getName());
 
 		curRecipe.updateLastViewTime();
 
 		intent = new Intent(this, RecipeDisplay.class);
 
 		spec = host.newTabSpec("info").setIndicator("Info").setContent(intent);
 
 		host.addTab(spec);
 
 		intent = new Intent(this, IngredientsDisplay.class);
 
 		spec = host.newTabSpec("ingredients").setIndicator("Ingredients").setContent(intent);
 
 		host.addTab(spec);
 
 		intent = new Intent(this, InstructionsDisplay.class);
 
 		spec = host.newTabSpec("instructions").setIndicator("Instructions").setContent(intent);
 
 		host.addTab(spec);
 
 
 		host.setCurrentTab(tab);
 
 		active_text_color = getResources().getColor(R.color.text_color);
 		inactive_text_color = getResources().getColor(R.color.background_light);
 
 		View tabView = null;
 
 		for(int i = 0; i < 3; i++) {
 			tabView = host.getTabWidget().getChildAt(i);
 			TextView tabText = (TextView) tabView.findViewById(android.R.id.title);
 			AppData.getSingleton().useHeadingFont(tabText);
 			tabText.setTextSize(18);
 			tabText.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.FILL_PARENT,
 																	RelativeLayout.LayoutParams.FILL_PARENT));
 			tabText.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL);
 			if(tab == i) {
 				tabView.setBackgroundResource(R.color.tab_active);
 				tabText.setTextColor(active_text_color);
 			} else {
 				tabView.setBackgroundResource(R.drawable.tab_inactive);
 				tabText.setTextColor(inactive_text_color);
 
 			}
 		}
 
 		host.setOnTabChangedListener(new OnTabChangeListener() {
 			public void onTabChanged(String tabId) {
 				int tab = getTabHost().getCurrentTab();
 				for(int i = 0; i < 3; i++) {
 					View tabView = getTabHost().getTabWidget().getChildAt(i);
 					if(tab == i) {
 						tabView.setBackgroundResource(R.color.tab_active);
 						((TextView) tabView.findViewById(android.R.id.title)).setTextColor(active_text_color);
 					} else {
 						tabView.setBackgroundResource(R.drawable.tab_inactive);
 						((TextView) tabView.findViewById(android.R.id.title)).setTextColor(inactive_text_color);
 
 					}
 				}
 			}
 		});
 	}
 
 }
