 package com.thompson234.bg95.guice;
 
 import com.google.common.base.Throwables;
 import com.google.inject.AbstractModule;
 import com.google.inject.Provides;
 import com.google.inject.Singleton;
 import com.thompson234.bg95.SearchConfiguration;
 import com.thompson234.bg95.service.SearchService;
 import com.thompson234.bg95.service.impl.LuceneSearchServiceImpl;
 import org.apache.lucene.store.Directory;
 import org.apache.lucene.store.FSDirectory;
 
 import javax.inject.Named;
 import java.io.File;
 
 public class SearchModule extends AbstractModule {
 
     private final SearchConfiguration _configuration;
 
     public SearchModule(SearchConfiguration configuration) {
         _configuration = configuration;
     }
 
     @Override
     protected void configure() {
         bind(SearchService.class).to(LuceneSearchServiceImpl.class).in(Singleton.class);
     }
 
     @Provides
     public Directory directory() {
         try {
            final File indexDir = new File(indexDir());

            if (!indexDir.exists()) {
                indexDir.mkdirs();
            }

            return FSDirectory.open(indexDir);
         } catch (Exception ex) {
             throw Throwables.propagate(ex);
         }
     }
 
     @Provides
     @Named("search.indexDir")
     public String indexDir() {
         return _configuration.getIndexDir();
     }
 }
