 package com.github.nmorel.gw2.server.repositories;
 
 import com.github.nmorel.gw2.server.beans.Item;
 import org.springframework.data.repository.CrudRepository;
 
 /** @author Nicolas Morel */
public interface ItemRepository extends CrudRepository<Item, Integer>
 {
 }
