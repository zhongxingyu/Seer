 package com.github.nmorel.gw2.server.controllers;
 
 import com.github.nmorel.gw2.server.beans.Item;
 import com.github.nmorel.gw2.server.repositories.ItemRepository;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.ResponseBody;
 
 import javax.inject.Inject;
 
 /** @author Nicolas Morel */
 @Controller
 public class ItemController extends AbstractController
 {
 
     @Inject
     private ItemRepository itemRepo;
 
     @RequestMapping( value = "/item", method = RequestMethod.GET, produces = MEDIA_TYPE_JSON_UTF8 )
     public @ResponseBody Iterable<Item> list()
     {
         return itemRepo.findAll();
     }
 
     @RequestMapping( value = "/item/{id}", method = RequestMethod.GET, produces = MEDIA_TYPE_JSON_UTF8 )
    public @ResponseBody Item getById( @PathVariable String id )
     {
         return itemRepo.findOne(id);
     }
 }
