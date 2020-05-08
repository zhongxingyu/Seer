 package org.bloodtorrent.resources;
 
 import org.bloodtorrent.IllegalDataException;
 import org.bloodtorrent.repository.SuccessStoryRepository;
 
 /**
  * Created with IntelliJ IDEA.
  * User: sds
  * Date: 13. 3. 22
  * Time: 오전 11:40
  * To change this template use File | Settings | File Templates.
  */
 public class SuccessStoryResource {
     private final SuccessStoryRepository repository;
 
     public SuccessStoryResource(SuccessStoryRepository repository) {
         this.repository = repository;
     }
 
     public Integer numberOfStories() throws IllegalDataException {
         int size = repository.list().size();
        if(size > 3)
             throw new IllegalDataException("At most 3 Success Stories should be shown.");
         return size;
     }
 }
