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
 
 package org.ow2.chameleon.everest.command;/*
  * User: Colin
  * Date: 19/07/13
  * Time: 14:12
  * 
  */
 
 import org.apache.felix.ipojo.annotations.*;
 import org.apache.felix.service.command.CommandSession;
 import org.apache.felix.service.command.Descriptor;
 import org.ow2.chameleon.everest.client.EverestClient;
 import org.ow2.chameleon.everest.client.ListResourceContainer;
 import org.ow2.chameleon.everest.client.ResourceContainer;
 import org.ow2.chameleon.everest.query.QueryFilter;
 import org.ow2.chameleon.everest.services.*;
 
 import java.util.List;
 
 @Component(immediate = true)
 @Instantiate
 @Provides(specifications = EverestGoGoCommand.class)
 public class EverestGoGoCommand {
 
     /**
      * Defines the command scope (rondo).
      */
     @ServiceProperty(name = "osgi.command.scope", value = "everest")
     String m_scope;
 
     /**
      * Defines the functions (commands).
      */
     @ServiceProperty(name = "osgi.command.function", value = "{}")
     String[] m_function = new String[]{"create", "retrieve", "update", "delete", "assertThat", "child", "relation","filter"};
 
 
     @Requires(optional = false)
     EverestService m_everest;
 
     EverestClient m_everestClient;
 
     private final static String LISTRESOURCE = "LISTOFRESOURCE" ;
 
     @Validate
     public void start() {
 
         m_everestClient = new EverestClient(m_everest);
 
     }
 
 
     @Descriptor("create a Resource")
     public void create(@Descriptor("create") String... handleId) {
         String bufferOut = new String();
         try {
             String path;
             if (handleId.length < 1) {
 
                 bufferOut = bufferOut + " Error : Need At least 1 Arguments";
             } else {
                 path = handleId[0];
                 m_everestClient.create(path);
                 for (int i = 1; i < handleId.length; i++) {
                     if ((i % 2) == 0) {
                         m_everestClient.with(handleId[i - 1], handleId[i]);
                     }
                 }
 
                 Resource resource = m_everestClient.doIt().retrieve();
                 if (!(resource == null)) {
                     bufferOut = bufferOut + "Success : creation of " + resource.getPath() + "\n";
                     ResourceMetadata resourceMetadata = resource.getMetadata();
                     for (String currentString : resourceMetadata.keySet()) {
                         bufferOut = bufferOut + currentString + " : \"" + resourceMetadata.get(currentString) + "\"" + "\n";
                     }
                 } else {
                     bufferOut = bufferOut + "Fail creation ";
                 }
 
 
             }
         } catch (Exception e) {
             e.printStackTrace();
             bufferOut = null;
         }
 
         System.out.println(bufferOut);
     }
 
     @Descriptor("retrieve a Resource")
     public void retrieve(@Descriptor("retrieve") String... handleId) {
         String bufferOut = new String();
         try {
             String path;
             if (handleId.length == 0) {
                 bufferOut = bufferOut + "Error : Must have at least 1 argument \n";
             } else if (handleId.length == 1) {
                 Resource resource;
                 path = handleId[0];
                 resource = m_everestClient.read(path).retrieve();
                 printResource(resource);
             } else {
                 path = handleId[0];
                 Resource resource = m_everestClient.read(path).retrieve();
                 if (resource.getPath().toString().equalsIgnoreCase("/")) {
                     bufferOut = bufferOut + "Name : " + resource.getPath().toString() + "\n";
                 } else {
                     bufferOut = bufferOut + "Name : " + resource.getPath().getLast().toString() + "\n";
                 }
 
                 bufferOut = bufferOut + "METADATA\n";
                 for (String currentString : handleId) {
                     if (!(currentString.equalsIgnoreCase(handleId[0]))) {
                         bufferOut = bufferOut + currentString + " : \"" + m_everestClient.read(path).retrieve(currentString) + "\"" + "\n";
                     }
                 }
             }
         } catch (Exception e) {
             e.printStackTrace();
             bufferOut = null;
         }
 
         System.out.println(bufferOut);
 
 
     }
 
     @Descriptor("update a Resource")
     public void update(@Descriptor("update") String... handleId) {
         String bufferOut = new String();
         try {
             String path;
             if (handleId.length < 3) {
 
                 bufferOut = bufferOut + " Error : Need At least 3 Arguments";
             } else {
                 path = handleId[0];
                 m_everestClient.update(path);
                 for (int i = 1; i < handleId.length; i++) {
                     if ((i % 2) == 0) {
                         m_everestClient.with(handleId[i - 1], handleId[i]);
                     }
                 }
                 Resource resource = m_everestClient.doIt().retrieve();
                 if (resource.getPath().toString().equalsIgnoreCase("/")) {
                     bufferOut = bufferOut + "Success : Update of " + resource.getPath().toString() + "\n";
                 } else {
                     bufferOut = bufferOut + "Success : Update of " + resource.getPath().getLast().toString() + " at " + resource.getPath().toString() + "\n";
                 }
                 bufferOut = bufferOut + "METADATA : \n";
                 ResourceMetadata resourceMetadata = resource.getMetadata();
                 for (String currentString : resourceMetadata.keySet()) {
                     bufferOut = bufferOut + currentString + " : \"" + resourceMetadata.get(currentString) + "\"" + "\n";
                 }
             }
         } catch (Exception e) {
             e.printStackTrace();
             bufferOut = null;
         }
 
         System.out.println(bufferOut);
 
 
     }
 
     @Descriptor("delete a Resource")
     public void delete(@Descriptor("delete") String... handleId) throws ResourceNotFoundException, IllegalActionOnResourceException {
         String bufferOut = new String();
         try {
             String path;
             if (handleId.length == 0) {
                 bufferOut = bufferOut + "Error : Must have at least 1 argument \n";
             } else {
 
                 for (String current : handleId) {
                     path = current;
                     m_everestClient.delete(path);
                     bufferOut = bufferOut + "Success : The resource at " + path + " have been destroy\n";
                 }
             }
         } catch (Exception e) {
             System.out.println(bufferOut);
             e.printStackTrace();
             bufferOut = null;
         }
 
         System.out.println(bufferOut);
 
 
     }
 
     @Descriptor("Assert property")
     public void assertThat(@Descriptor("everestassert") String... handleId) throws ResourceNotFoundException, IllegalActionOnResourceException {
         String bufferOut = new String();
 
         try {
             String path;
             String action;
             Boolean result;
             if (handleId.length < 2) {
 
                 bufferOut = bufferOut + " Error : Need At least 2 Arguments";
             } else {
                 path = handleId[0];
                 action = handleId[1];
                 if (action.equalsIgnoreCase("exist")) {
                     result = m_everestClient.assertThat(m_everestClient.read(path).retrieve()).exist();
                     bufferOut = bufferOut + result.toString() + "\n";
                 } else if (action.equalsIgnoreCase("not_exist")) {
                     result = m_everestClient.assertThat(m_everestClient.read(path).retrieve()).exist();
                     result = !(result);
                     bufferOut = bufferOut + result.toString() + "\n";
                 } else if (action.contains("=")) {
                     String[] param;
 
                     param = action.split("=");
                     result = m_everestClient.assertThat(m_everestClient.read(path).retrieve(param[0])).isEqualTo(param[1]);
                     bufferOut = bufferOut + result.toString() + "\n";
                 } else {
                     if (!(m_everestClient.read(path).retrieve(action) == null)) {
                         bufferOut = bufferOut + "true" + "\n";
                     } else {
                         bufferOut = bufferOut + "false" + "\n";
 
                     }
                 }
 
 
             }
         } catch (Exception e) {
             System.out.println(bufferOut);
             e.printStackTrace();
             bufferOut = null;
         }
         System.out.println(bufferOut);
     }
 
     @Descriptor("Get child/children of a resource")
     public void child( @Descriptor("automatically supplied shell session") CommandSession session,
                        @Descriptor("create") String... handleId
     ) {
         String bufferOut = new String();
         try {
             String path;
             if (handleId.length == 0) {
                 bufferOut = bufferOut + "Error : Must have at least 1 argument \n";
             } else if (handleId.length == 1) {
                 path = handleId[0];
                 List<Resource> resources = m_everestClient.read(path).children().retrieve();
                 session.put(LISTRESOURCE,m_everestClient.read(path).children());
                 if (!(resources == null)) {
                     printResource(resources);
                 } else {
                     bufferOut = bufferOut + "\nNo children\n";
                 }
             } else {
                 path = handleId[0];
                 for (String currentString : handleId) {
                     if (!(currentString.equalsIgnoreCase(handleId[0]))) {
                         Resource resource = m_everestClient.read(path).child(currentString).retrieve();
                         if (!(resource == null)) {
                             printResource(resource);
                         } else {
                             bufferOut = bufferOut + "\nNo child named : \"" + currentString + "\"\n";
                         }
                     }
                 }
             }
 
 
         } catch (Exception e) {
             System.out.println(bufferOut);
             e.printStackTrace();
             bufferOut = null;
         }
         System.out.println(bufferOut);
 
 
     }
 
     @Descriptor("Get Resource target by relation/relations of a resource")
     public void relation( @Descriptor("automatically supplied shell session") CommandSession session,
                           @Descriptor("create") String... handleId
     ) {
         String bufferOut = new String();
         try {
             String path;
             if (handleId.length == 0) {
                 bufferOut = bufferOut + "Error : Must have at least 1 argument \n";
             } else if (handleId.length == 1) {
                 path = handleId[0];
                 List<Resource> resources = m_everestClient.read(path).relations().retrieve();
                 session.put(LISTRESOURCE,m_everestClient.read(path).relations());
                 printResource(resources);
             } else {
                 path = handleId[0];
                 for (String currentString : handleId) {
                     if (!(currentString.equalsIgnoreCase(handleId[0]))) {
                         Resource resource = m_everestClient.read(path).relation(currentString).retrieve();
                         printResource(resource);
                     } else {
                         bufferOut = bufferOut + "\nNo relation named :\"" + currentString + "\"\n";
                     }
                 }
             }
         } catch (Exception e) {
             System.out.println(bufferOut);
             e.printStackTrace();
             bufferOut = null;
         }
         System.out.println(bufferOut);
 
     }
 
     @Descriptor("Apply a filter on a set of Resource")
     public void filter(@Descriptor("automatically supplied shell session") CommandSession session,
                        @Descriptor("request for the filter creation") String request
     )   {
         System.out.println("Request " + request);
         ListResourceContainer listResourceContainer = (ListResourceContainer) session.get(LISTRESOURCE);
         if ( listResourceContainer == null){
             System.out.println(" NO ENTRY TO FILTER ");
             return;
         }
         try{
            QueryFilter parserQuery = new QueryFilter(request,m_everestClient.getM_everest());
             ResourceFilter resourceFilter = parserQuery.input();
             ListResourceContainer filterResourceList = listResourceContainer.filter(resourceFilter);
             printResource(filterResourceList);
             session.put(LISTRESOURCE,filterResourceList);
         }catch (Exception e){
             e.printStackTrace();
         }
     }
 
 
     private void printResource(ListResourceContainer listResourceContainer){
         if ((listResourceContainer == null) || (listResourceContainer.retrieve().isEmpty())) {
             return;
         }
 
         for (Resource resource: listResourceContainer.retrieve()){
             printResource(resource);
             System.out.println("");
         }
     }
 
     private void printResource(List<Resource> listResource){
         if ((listResource == null) || (listResource.isEmpty())) {
             return;
         }
 
         for (Resource resource: listResource){
             printResource(resource);
             System.out.println("");
         }
     }
 
     private void printResource(Resource resource){
         if (resource == null) {
             return;
         }
         List<Relation> relationList = resource.getRelations();
         ResourceMetadata resourceMetadata = resource.getMetadata();
         System.out.println("Resource : " + resource.getPath());
         System.out.println("{");
         System.out.println("\tMetadata :");
 
         if (!(resourceMetadata.isEmpty())) {
             for (String currentString : resourceMetadata.keySet()) {
                 System.out.println("\t\t" + currentString + " : \"" + resourceMetadata.get(currentString) + "\"" + " : " +resourceMetadata.get(currentString).getClass().getName());
             }
         } else {
             System.out.println( "\t\tNo metadata");
         }
         System.out.println("\t\t_relation :");
         System.out.println("\t\t\t{");
         if (!(relationList.isEmpty())) {
             for(Relation relation : relationList){
                 System.out.println("\t\t\tname :" +relation.getName());
                 System.out.println("\t\t\thref :" +relation.getHref());
                 System.out.println("\t\t\taction :" +relation.getAction());
                 System.out.println("\t\t\tdescription :" +relation.getDescription());
                 List<Parameter> listParameters = relation.getParameters();
 
                 if ((listParameters != null) || (!listParameters.isEmpty()) ){
                     System.out.println("\t\t\t_parameter");
 
                     System.out.println("\t\t\t\t{");
                     for (Parameter parameter : listParameters){
                         System.out.println("\t\t\t\tname :"+parameter.name());
                         System.out.println("\t\t\t\tclass :"+parameter.type().getName());
                         System.out.println("\t\t\t\tdescription :"+parameter.description());
                         System.out.println("\t\t\t\toptional :"+parameter.optional());
                     }
                     System.out.println("\t\t\t\t}");
                 }
                 System.out.println("\t\t\t}");
             }
         } else {
             System.out.println( "\n\t\tNo relation");
             System.out.println("\t\t\t}");
         }
         System.out.println("\t\t}");
         System.out.println("\t}");
     }
 
     private void printResource(ResourceContainer resourceContainer){
         printResource(resourceContainer.retrieve());
     }
 }
