 package br.com.zamlutti.comente.controllers;
 
 import br.com.caelum.vraptor.Get;
import br.com.caelum.vraptor.Path;
 import br.com.caelum.vraptor.Post;
 import br.com.caelum.vraptor.Resource;
 import br.com.caelum.vraptor.Result;
 import br.com.zamlutti.comente.entities.Entry;
 import br.com.zamlutti.comente.repositories.EntryRepository;
 import br.com.zamlutti.comente.utils.Urlizer;
 
 @Resource
 public class IndexController {
 
     private Result result;
     private Urlizer urlizer;
     private EntryRepository entryRepository;
 
     public IndexController(Result result, Urlizer urlizer,
             EntryRepository entryRepository) {
         this.result = result;
         this.urlizer = urlizer;
         this.entryRepository = entryRepository;
     }
 
    @Path("/")
     public void index() {
     }
 
     @Post
     public void add(Entry entry) {
         String title = entry.getTitle();
         String url = this.urlizer.urlize(title, '-');
         entry.setUrl(url);
         this.entryRepository.save(entry);
         this.result.redirectTo(CommentsController.class).add(url);
     }
 }
