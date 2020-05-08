 /*
  * This file is part of AMEE.
  *
  * Copyright (c) 2007, 2008, 2009 AMEE UK LIMITED (help@amee.com).
  *
  * AMEE is free software; you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation; either version 3 of the License, or
  * (at your option) any later version.
  *
  * AMEE is free software and is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  *
  * Created by http://www.dgen.net.
  * Website http://www.amee.cc
  */
 package com.amee.service.definition;
 
 import com.amee.domain.Pager;
 import com.amee.domain.ValueDefinition;
 import com.amee.domain.algorithm.AbstractAlgorithm;
 import com.amee.domain.algorithm.Algorithm;
 import com.amee.domain.algorithm.AlgorithmContext;
 import com.amee.domain.data.ItemDefinition;
 import com.amee.domain.data.ItemValueDefinition;
 import com.amee.domain.data.ReturnValueDefinition;
 import com.amee.domain.environment.Environment;
 import com.amee.service.BaseService;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Service;
 
 import java.util.List;
 
 @Service
 public class DefinitionService extends BaseService {
 
     @Autowired
     private DefinitionServiceDAO dao;
 
     // Algorithms
 
     public Algorithm getAlgorithmByUid(String uid) {
         return dao.getAlgorithmByUid(uid);
     }
 
     public List<AlgorithmContext> getAlgorithmContexts(Environment environment) {
         return dao.getAlgorithmContexts(environment);
     }
 
     public AlgorithmContext getAlgorithmContextByUid(Environment environment, String uid) {
         AlgorithmContext algorithmContext = dao.getAlgorithmContextByUid(uid);
         checkEnvironmentObject(environment, algorithmContext);
         return algorithmContext;
     }
 
     public void save(AbstractAlgorithm algorithm) {
         dao.save(algorithm);
     }
 
     public void remove(AbstractAlgorithm algorithm) {
         dao.remove(algorithm);
     }
 
     // ItemDefinition
 
     /**
      * Returns the ItemDefinition for the Environnment and ItemDefinition UID specified. Returns null
      * if the ItemDefinition could not be found. Throws a RuntimeException if the specified Environment
      * does not match the ItemDefinition Environment.
      *
      * @param environment within which the ItemDefinition must belong
      * @param uid         of the ItemDefinition to fetch
      * @return the ItemDefinition matching the Environment and ItemDefinition UID specified
      */
     public ItemDefinition getItemDefinitionByUid(Environment environment, String uid) {
         ItemDefinition itemDefinition = dao.getItemDefinitionByUid(uid);
         checkEnvironmentObject(environment, itemDefinition);
         return itemDefinition;
     }
 
     public List<ItemDefinition> getItemDefinitions(Environment environment) {
         return dao.getItemDefinitions(environment);
     }
 
     public List<ItemDefinition> getItemDefinitions(Environment environment, Pager pager) {
         return dao.getItemDefinitions(environment, pager);
     }
 
     public void save(ItemDefinition itemDefinition) {
         dao.save(itemDefinition);
     }
 
     public void remove(ItemDefinition itemDefinition) {
         dao.remove(itemDefinition);
     }
 
     // ItemValueDefinitions
 
     public ItemValueDefinition getItemValueDefinitionByUid(ItemDefinition itemDefinition, String uid) {
         ItemValueDefinition itemValueDefinition = getItemValueDefinitionByUid(uid);
         if (itemValueDefinition.getItemDefinition().equals(itemDefinition)) {
             return itemValueDefinition;
         } else {
             return null;
         }
     }
 
     public ItemValueDefinition getItemValueDefinitionByUid(String uid) {
         return dao.getItemValueDefinitionByUid(uid);
     }
 
     public void save(ItemValueDefinition itemValueDefinition) {
         dao.save(itemValueDefinition);
     }
 
     public void remove(ItemValueDefinition itemValueDefinition) {
         dao.remove(itemValueDefinition);
     }
 
     // ReturnValueDefinitions

    public ReturnValueDefinition getReturnValueDefinitionByUid(ItemDefinition itemDefinition, String uid) {
         ReturnValueDefinition returnValueDefinition = getReturnValueDefinitionByUid(uid);
        if ((returnValueDefinition != null) && returnValueDefinition.getItemDefinition().equals(itemDefinition)) {
             return returnValueDefinition;
         } else {
             return null;
         }
     }
 
     public ReturnValueDefinition getReturnValueDefinitionByUid(String uid) {
         return dao.getReturnValueDefinitionByUid(uid);
     }
 
     public void save(ReturnValueDefinition returnValueDefinition) {
         dao.save(returnValueDefinition);
     }
 
     public void remove(ReturnValueDefinition returnValueDefinition) {
         dao.remove(returnValueDefinition);
     }
 
     // ValueDefinitions
 
     public List<ValueDefinition> getValueDefinitions(Environment environment) {
         return dao.getValueDefinitions(environment);
     }
 
     public List<ValueDefinition> getValueDefinitions(Environment environment, Pager pager) {
         return dao.getValueDefinitions(environment, pager);
     }
 
     public ValueDefinition getValueDefinition(Environment environment, String uid) {
         ValueDefinition valueDefinition = dao.getValueDefinitionByUid(uid);
         checkEnvironmentObject(environment, valueDefinition);
         return valueDefinition;
     }
 
     public void save(ValueDefinition valueDefinition) {
         dao.save(valueDefinition);
     }
 
     public void remove(ValueDefinition valueDefinition) {
         dao.remove(valueDefinition);
     }
 }
