 /*	Guardian Lite - an Android reader for the Guardian newspaper.
  *	Copyright (C) 2011  Eel Pie Consulting Limited
  *
  *	This program is free software: you can redistribute it and/or modify
  * 	it under the terms of the GNU General Public License as published by
  * 	the Free Software Foundation, either version 3 of the License, or
  * 	(at your option) any later version.
  *
  *	This program is distributed in the hope that it will be useful,
  * 	but WITHOUT ANY WARRANTY; without even the implied warranty of
  * 	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * 	GNU General Public License for more details.
  *
  *	You should have received a copy of the GNU General Public License
  *	along with this program.  If not, see <http://www.gnu.org/licenses/>.	*/
 
 package nz.gen.wellington.guardian.android.activities;
 
 import nz.gen.wellington.guardian.android.factories.SingletonFactory;
 import nz.gen.wellington.guardian.android.model.ArticleSet;
import nz.gen.wellington.guardian.android.model.SectionColourMap;
 import nz.gen.wellington.guardian.android.model.TagArticleSet;
 import nz.gen.wellington.guardian.android.usersettings.FavouriteSectionsAndTagsDAO;
 import nz.gen.wellington.guardian.model.Tag;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.widget.Toast;
 
 public class tag extends ArticleListActivity {
 
 	private static final String TAG = "tag";
 
 	private MenuItem favouriteMenuItem;
 	private FavouriteSectionsAndTagsDAO favouriteSectionsAndTagsDAO;
 	private ArticleSet articleSet;
 
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);	
 		this.favouriteSectionsAndTagsDAO = SingletonFactory.getFavouriteSectionsAndTagsDAO(this.getApplicationContext());
 		articleSet = (ArticleSet) this.getIntent().getExtras().get("articleset");
 		
 		if (articleSet.getSection() != null) {
			setHeadingColour(SectionColourMap.getColourForSection(articleSet.getSection().getId()));
		}
 		setHeading(articleSet.getName());
 	}
 	
 	protected String getRefinementDescription(String refinementType) {
 		if (refinementType.equals("keyword")) {
 			return "Articles tagged with " + getArticleSet().getName() + " have also been tagged with:";
 		}
 		if (refinementType.equals("date")) {
 			return "Articles have been tagged with " + getArticleSet().getName() + " on these dates:";
 		}
 		return null;
 	}
 	
 	protected ArticleSet getArticleSet() {
 		return this.articleSet;
 	}
 	
 	public boolean onCreateOptionsMenu(Menu menu) {
 		menu.add(0, MenuedActivity.HOME, 0, "Home");	
 		MenuItem refreshOption = menu.add(0, MenuedActivity.REFRESH, 0, "Refresh");
 		enableMenuItemIfConnectionIsAvailable(refreshOption);
 		
 		if (articleSet instanceof TagArticleSet) {
 			Tag tag = ((TagArticleSet) articleSet).getTag();
 			if (favouriteSectionsAndTagsDAO.isFavourite(tag)) {
 				favouriteMenuItem = menu.add(0, MenuedActivity.ADD_REMOVE_FAVOURITE, 0, "Remove Favourite");
 			} else {
 				favouriteMenuItem = menu.add(0, MenuedActivity.ADD_REMOVE_FAVOURITE, 0, "Add to Favourites");
 			}
 		}
 			
 	    return true;
 	}
 	
 	
 	public boolean onOptionsItemSelected(MenuItem item) {
 		if (!super.onOptionsItemSelected(item)) {
 			switch (item.getItemId()) {
 			case MenuedActivity.ADD_REMOVE_FAVOURITE:
 				addToFavourites();
 				return true;		
 			}
 		}
 		return false;
 	}
 
 	
 	private void addToFavourites() {
 		if (articleSet instanceof TagArticleSet) {
 			Tag tag = ((TagArticleSet) articleSet).getTag();
 			if (!favouriteSectionsAndTagsDAO.isFavourite(tag)) {
 				Log.i(TAG, "Adding current tag to favourites: " + tag.getName());
 				if (favouriteSectionsAndTagsDAO.addTag(tag)) {
 					favouriteMenuItem.setTitle("Remove Favourite");
 				} else {
 					Toast.makeText(this, "Favourites list is full", Toast.LENGTH_LONG).show();
 				}
 
 			} else {
 				Log.i(TAG, "Removing current tag from favourites: " + tag.getName());
 				favouriteSectionsAndTagsDAO.removeTag(tag);
 				favouriteMenuItem.setTitle("Add to Favourites");
 			}
 		}
 	}
 	
 }
