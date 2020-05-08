 package ch.bbv.wikiHow.dal;
 
 import static ch.bbv.wikiHow.WikiHowAppActivity.TAG;
 
 import java.io.IOException;
 import java.util.List;
 
 import android.content.Context;
 import android.util.Log;
 import ch.bbv.wikiHow.model.Article;
 
 public class ArticleCache {
 
     private static final String PRELOADED_DATABASE_NAME = "cached.db";
     private final ArticleDbAdapter dbAdapter;
 
     public ArticleCache(final Context context) throws IOException {
         final SimpleDatabaseHelper databaseHelper = new SimpleDatabaseHelper(context, PRELOADED_DATABASE_NAME);
         this.dbAdapter = new ArticleDbAdapter(databaseHelper);
     }
 
     public void open() {
         try {
             this.dbAdapter.open();
         }
         catch(final IOException e) {
             Log.e(TAG, "could not open DatabaseAdapter", e);
             throw new RuntimeException(e);
         }
     }
 
     public void close() {
         this.dbAdapter.close();
     }
 
     public Article getCachedArticle(final String identifier) {
         return this.dbAdapter.fetchArticle(identifier);
     }
 
     public void hydrateArticle(final Article article) {
         final String html = this.dbAdapter.fetchArticleHtml(article.getIdentifier());
 
         if(html != null) {
             article.setHtml(html);
         }
     }
 
     public boolean cacheArticle(final Article article) {
         return this.dbAdapter.insertArticle(article) > 0;
     }
 
     // title kann auch * oder % enthalten -> DB ruft like auf...
     public List<Article> getArticlesByTitle(final String title) {
        return this.dbAdapter.queryArticle(ArticleDbAdapter.KEY_CATEGORY + " like " + title);
     }
 }
