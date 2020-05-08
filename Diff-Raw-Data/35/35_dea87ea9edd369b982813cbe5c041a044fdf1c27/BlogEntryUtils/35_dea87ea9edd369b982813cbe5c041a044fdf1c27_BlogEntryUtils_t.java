 package com.tihonchik.lenonhonor360.util;
 
 import java.util.List;
 
 import com.tihonchik.lenonhonor360.app.LenonHonor360App;
 import com.tihonchik.lenonhonor360.db.BlogDatabase;
 import com.tihonchik.lenonhonor360.models.BlogEntry;
 
 public class BlogEntryUtils {
 	public static int getNewestBlogId() {
 		BlogDatabase db = new BlogDatabase(LenonHonor360App.getInstance()
 				.getApplicationContext());
 		return db.getNewestBlogId();
 	}
 
 	public static void deleteBlogEntry(int id) {
 		BlogDatabase db = new BlogDatabase(LenonHonor360App.getInstance()
 				.getApplicationContext());
 		db.deleteBlogEntry(id);
 		db.deleteBlogEntryImages(id);
 	}
 
 	public static void deleteBlogEntryImages(int id) {
 		BlogDatabase db = new BlogDatabase(LenonHonor360App.getInstance()
 				.getApplicationContext());
 		db.deleteBlogEntryImages(id);
 	}
 
 	public static void insertBlogEntries(List<BlogEntry> entries) {
 		BlogDatabase db = new BlogDatabase(LenonHonor360App.getInstance()
 				.getApplicationContext());
 		db.insertNewBlogEntries(entries);
 	}
 
 	public static List<BlogEntry> getAllBlogEntries() {
 		BlogDatabase db = new BlogDatabase(LenonHonor360App.getInstance()
 				.getApplicationContext());
 		List<BlogEntry> entries = db.findAllBlogEntries();
 		for (BlogEntry entry : entries) {
 			entry.setImages(db.getImagesById(entry.getId()));
 		}
 		return entries;
 	}
 
 	public static List<Integer> getAllBlogIds() {
 		BlogDatabase db = new BlogDatabase(LenonHonor360App.getInstance()
 				.getApplicationContext());
 		return db.findAllBlogIds();
 	}
 
 	public static void insertImage(int id, String image) {
 		BlogDatabase db = new BlogDatabase(LenonHonor360App.getInstance()
 				.getApplicationContext());
 		db.insertNewImage(id, image);
 	}
 
 	public static BlogEntry getNewestBlog() {
 		BlogDatabase db = new BlogDatabase(LenonHonor360App.getInstance()
 				.getApplicationContext());
 		return db.getNewestBlog();
 	}
 
 	public static BlogEntry getBLogById(int id) {
 		BlogDatabase db = new BlogDatabase(LenonHonor360App.getInstance()
 				.getApplicationContext());
 		BlogEntry blog = db.getBlogById(id);
 		blog.setImages(db.getImagesById(id));
 		return blog;
 	}
 
 	public static void insertBlogEntry(BlogEntry entry) {
 		BlogDatabase db = new BlogDatabase(LenonHonor360App.getInstance()
 				.getApplicationContext());
 		db.insertNewBlogEntry(entry);
 		for (String image : entry.getImages()) {
 			db.insertNewImage(entry.getId(), image);
 		}
 	}

	public static String replaceHTMLTags(String text) {
		return text.replaceAll("\\<[^>]*>", "");
	}
 }
