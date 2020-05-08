 /*
  * Copyright 2013 OW2 Chameleon
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *  http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package org.ow2.chameleon.everest.client;/*
  * User: Colin
  * Date: 17/07/13
  * Time: 15:14
  * 
  */
 
 import org.ow2.chameleon.everest.services.*;
 
 import java.util.ArrayList;
 import java.util.List;
 
 public class ListResourceContainer {
 
     /**
      * Lis of resourceContainer which contains
      */
     List<ResourceContainer> m_resourcesContainer = new ArrayList<ResourceContainer>();
 
     public ListResourceContainer(ResourceContainer resourceContainer) {
         m_resourcesContainer.add(resourceContainer);
     }
 
     public ListResourceContainer(List<ResourceContainer> listresource) {
         this.m_resourcesContainer = listresource;
     }
 
     /**
      * Get the list of parent of the m_resourcesContainer. If 2 resources have the same parent, he appears just one time in
      * the return.
      *
      * @return
      * @throws ResourceNotFoundException
      */
     public synchronized ListResourceContainer parent() throws ResourceNotFoundException {
 
         if (m_resourcesContainer == null) {
 
             return this;
         }
 
         List<Path> listPath = new ArrayList<Path>();
         List<ResourceContainer> returnResources = new ArrayList<ResourceContainer>();
 
         for (ResourceContainer current : m_resourcesContainer) {
             if (!(current.parent() == null)) {
                 //check if the parent is already save in the return list
                 if (!(listPath.contains(current.parent().m_resource.getPath()))) {
                     returnResources.add(current.parent());
                     listPath.add(current.parent().m_resource.getPath());
                 }
             }
         }
 
         if (!(returnResources.isEmpty())) {
             m_resourcesContainer = returnResources;
             return this;
         }
 
 
         this.m_resourcesContainer = null;
         return this;
     }
 
     /**
      * Get the children of each member of   m_resourcesContainer
      *
      * @return
      */
     public synchronized ListResourceContainer children() {
 
         if (m_resourcesContainer == null) {
 
             return this;
         }
         List<ResourceContainer> returnResources = new ArrayList<ResourceContainer>();
 
         for (ResourceContainer currentResourceContainer : m_resourcesContainer) {
             if (!(currentResourceContainer.children().m_resourcesContainer == null)) {
                 returnResources.addAll(currentResourceContainer.children().m_resourcesContainer);
             }
         }
         if (!(returnResources.isEmpty())) {
             m_resourcesContainer = returnResources;
             return this;
         }
 
         this.m_resourcesContainer = null;
         return this;
     }
 
     /**
      * Get the children of each member of   m_resourcesContainer
      *
      * @return
      */
     public synchronized ListResourceContainer children(ResourceFilter filter) {
 
         if ((m_resourcesContainer == null)  ) {
             return this;
         }
         if (filter == null ){
             return children();
         }
         List<ResourceContainer> returnResources = new ArrayList<ResourceContainer>();
 
         for (ResourceContainer currentResourceContainer : m_resourcesContainer) {
             if (!(currentResourceContainer.children(filter).m_resourcesContainer == null)) {
                 returnResources.addAll(currentResourceContainer.children().m_resourcesContainer);
             }
         }
         if (!(returnResources.isEmpty())) {
             m_resourcesContainer = returnResources;
             return this;
         }
 
         this.m_resourcesContainer = null;
         return this;
     }
 
     /**
      * Get the child identified by name of each member of   m_resourcesContainer
      *
      * @param name
      * @return
      */
     public synchronized ListResourceContainer child(String name) {
 
         if (m_resourcesContainer == null) {
             return this;
         }
         List<ResourceContainer> returnResources = new ArrayList<ResourceContainer>();
 
         for (ResourceContainer currentResourceContainer : m_resourcesContainer) {
             ResourceContainer temp = currentResourceContainer.child(name);
             if ( temp.m_resource != null) {
                 ResourceContainer temp1;
                 temp1 = new ResourceContainer(temp.m_resource);
                returnResources.add(temp);
             }
         }
         if (!(returnResources.isEmpty())) {
            m_resourcesContainer = returnResources;
             return this;
         }
 
         this.m_resourcesContainer = null;
         return this;
 
 
     }
 
     /**
      * Get the all resource which are in relations with each member of m_resourcesContainer
      *
      * @return
      * @throws ResourceNotFoundException
      */
     public synchronized ListResourceContainer relations() throws ResourceNotFoundException {
         if (m_resourcesContainer == null) {
             return this;
         }
         List<Path> listPath = new ArrayList<Path>();
         List<ResourceContainer> returnResources = new ArrayList<ResourceContainer>();
         List<ResourceContainer> temp;
 
         for (ResourceContainer current : m_resourcesContainer) {
             if (!(current.relations().m_resourcesContainer == null)) {
                 temp = current.relations().m_resourcesContainer;
                 for (ResourceContainer currentResource : temp) {
                     // check if the resource is already save
                     if (!(listPath.contains(currentResource.m_resource.getPath()))) {
                         listPath.add(current.m_resource.getPath());
                         returnResources.add(currentResource);
                     }
                 }
             }
 
         }
         if (!(returnResources.isEmpty())) {
             m_resourcesContainer = returnResources;
             return this;
         }
 
         this.m_resourcesContainer = null;
         return this;
     }
 
     /**
      * Get the all resource which are in relations with each member of m_resourcesContainer which match with the filter
      *
      * @return
      * @throws ResourceNotFoundException
      */
     public synchronized ListResourceContainer relations(RelationFilter filter) throws ResourceNotFoundException {
         if (m_resourcesContainer == null) {
             return this;
         }
         if (filter == null){
             return relations();
         }
 
         List<Path> listPath = new ArrayList<Path>();
         List<ResourceContainer> returnResources = new ArrayList<ResourceContainer>();
         List<ResourceContainer> temp;
 
         for (ResourceContainer current : m_resourcesContainer) {
             if (!(current.relations(filter).m_resourcesContainer == null)) {
                 temp = current.relations(filter).m_resourcesContainer;
                 for (ResourceContainer currentResource : temp) {
                     // check if the resource is already save
                     if (!(listPath.contains(currentResource.m_resource.getPath()))) {
                         listPath.add(current.m_resource.getPath());
                         returnResources.add(currentResource);
                     }
                 }
             }
 
         }
         if (!(returnResources.isEmpty())) {
             m_resourcesContainer = returnResources;
             return this;
         }
 
         this.m_resourcesContainer = null;
         return this;
     }
 
     /**
      * Get the all resource which are in relation identified by name with each member of m_resourcesContainer
      *
      * @param relationName : name of the relation
      * @return
      * @throws ResourceNotFoundException
      */
     public synchronized ListResourceContainer relation(String relationName) throws ResourceNotFoundException {
         if (m_resourcesContainer == null) {
             return this;
         }
         List<Path> listPath = new ArrayList<Path>();
         List<ResourceContainer> returnResources = new ArrayList<ResourceContainer>();
 
         for (ResourceContainer current : m_resourcesContainer) {
             if (!(current.relation(relationName).m_resource == null)) {
                 if (!(listPath.contains(current.relation(relationName).m_resource.getPath()))) {
                     listPath.add(current.relation(relationName).m_resource.getPath());
                     returnResources.add(current.relation(relationName));
                 }
             }
         }
         if (!(returnResources.isEmpty())) {
             m_resourcesContainer = returnResources;
             return this;
         }
 
         this.m_resourcesContainer = null;
         return this;
     }
 
     /**
      * Get the metadata identified by his ID at the string format of each member of  m_resourcesContainer
      *
      * @param metadataId : Id of the Metadata
      * @return
      */
     public synchronized List<String> retrieve(String metadataId) {
 
         if (m_resourcesContainer == null) {
             return null;
         }
 
         List<String> returnList = new ArrayList<String>();
 
         for (ResourceContainer current : m_resourcesContainer) {
            if (current.retrieve(metadataId) != null) {
                System.out.println(current.retrieve().getMetadata());
                returnList.add(current.retrieve(metadataId));
             }
         }
 
         if (returnList.isEmpty()) {
             return null;
         }
 
 
         return returnList;
 
     }
 
     /**
      * Get all the resources present in m_resourcesContainer
      *
      * @return
      */
     public synchronized List<Resource> retrieve() {
 
         if (m_resourcesContainer == null) {
             return null;
         }
 
         List<Resource> returnList = new ArrayList<Resource>();
 
         for (ResourceContainer current : m_resourcesContainer) {
             returnList.add(current.retrieve());
         }
 
         if (returnList.isEmpty()) {
             return null;
         }
 
         return returnList;
 
     }
 
     /**
      * Get a metadata identified by his ID at the T format
      *
      * @param metadataId : Id of the Metadata
      * @param clazz      : Class of the value of the metadata
      * @param <T>
      * @return
      */
     public synchronized <T> List<T> retrieve(String metadataId, Class<T> clazz) {
         if (m_resourcesContainer == null) {
             return null;
         }
 
         List<T> returnList = new ArrayList<T>();
 
         for (ResourceContainer current : m_resourcesContainer) {
             if (current.retrieve(metadataId) != null) {
                 returnList.add(current.retrieve(metadataId, clazz));
             }
         }
 
         if (returnList.isEmpty()) {
             return null;
         }
 
         return returnList;
 
     }
 
     /**
      * Define the next action to UPDATE send by the doIt method.Erase the parameters.
      *
      * @return
      */
     public synchronized ListResourceContainer update() {
         if (m_resourcesContainer == null) {
             return null;
         }
 
         for (ResourceContainer current : m_resourcesContainer) {
             current.update();
         }
 
         return this;
     }
 
     /**
      * Define the next action to CREATE send by the doIt method.Erase the parameters.
      *
      * @return
      */
     public synchronized ListResourceContainer create() {
 
         if (m_resourcesContainer == null) {
             return null;
         }
         for (ResourceContainer current : m_resourcesContainer) {
             current.create();
         }
         return this;
     }
 
     /**
      * Define the next action to DELETE send by the doIt method.Erase the parameters.
      *
      * @return
      */
     public synchronized ListResourceContainer delete() {
         if (m_resourcesContainer == null) {
             return null;
         }
 
         for (ResourceContainer current : m_resourcesContainer) {
             current.delete();
         }
         return this;
     }
 
     /**
      * Define the parameters send by the doIt method. each called to with() put a new <key,value> in m_currentParams.
      *
      * @return
      */
     public synchronized ListResourceContainer with(String key, Object value) {
         if (m_resourcesContainer == null) {
             return null;
         }
 
         for (ResourceContainer current : m_resourcesContainer) {
             current.with(key, value);
         }
         return this;
     }
 
     /**
      * This method is called after called an action method (create , delete or update) and have set parameters with the
      * method with(). This method throw a request on m_resource with the action define by the last call of an action method and with a set of parameters.
      *
      * @return : The resource container which contains the resource create , update , delete.
      * @throws ResourceNotFoundException
      * @throws IllegalActionOnResourceException
      *
      */
     public synchronized ListResourceContainer doIt() throws ResourceNotFoundException, IllegalActionOnResourceException {
         if (m_resourcesContainer == null) {
             return null;
         }
 
         List<ResourceContainer> returnResources = new ArrayList<ResourceContainer>();
 
         for (ResourceContainer current : m_resourcesContainer) {
             returnResources.add(current.doIt());
         }
 
         m_resourcesContainer = returnResources;
         return this;
     }
 
 
     public synchronized ListResourceContainer filter(ResourceFilter filter) {
         if (m_resourcesContainer == null) {
             return null;
         }
 
         List<ResourceContainer> returnResources = new ArrayList<ResourceContainer>();
 
         for (ResourceContainer current : m_resourcesContainer) {
             ResourceContainer temp = current.filter(filter);
            if (!(temp.m_resource == null)) {
                ResourceContainer temp1;
                temp1 = new ResourceContainer(temp.m_resource);
                returnResources.add(temp1);
            }
         }
 
         if (!(returnResources.isEmpty())) {
             m_resourcesContainer = returnResources;
             return this;
         }
 
         this.m_resourcesContainer = null;
         return this;
     }
 
 
 }
