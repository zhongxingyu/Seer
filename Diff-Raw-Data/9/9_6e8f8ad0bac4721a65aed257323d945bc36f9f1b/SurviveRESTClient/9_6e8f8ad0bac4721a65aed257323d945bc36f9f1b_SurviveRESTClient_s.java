 /*
  * Copyright 2012 Faculty of Informatics - Masaryk University.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package com.muni.fi.pa165.survive.rest.client;
 
 import com.muni.fi.pa165.dto.AbstractDto;
 import com.muni.fi.pa165.survive.rest.client.services.CustomRestService;
 import com.muni.fi.pa165.survive.rest.client.services.impl.WeaponServiceImpl;
 import com.muni.fi.pa165.survive.rest.client.utils.CommandLineValidator;
 import com.muni.fi.pa165.survive.rest.client.utils.DtoBuilder;
 import com.muni.fi.pa165.survive.rest.client.utils.OptionsProvider;
 import org.apache.commons.cli.CommandLine;
 import org.apache.commons.cli.CommandLineParser;
 import org.apache.commons.cli.HelpFormatter;
 import org.apache.commons.cli.Options;
 import org.apache.commons.cli.ParseException;
 import org.apache.commons.cli.PosixParser;
 
 import com.muni.fi.pa165.dto.AreaDto;
 import com.muni.fi.pa165.dto.WeaponDto;
 import com.muni.fi.pa165.survive.rest.client.services.impl.AreaServiceImpl;
 import java.util.List;
 import javax.ws.rs.core.Response;
 
 /**
  * Main class of the REST client application.
  *
  * @author Michal
  */
 public class SurviveRESTClient {
 
     public static void main(String[] args) {
 
         CommandLineParser parser = new PosixParser();
         Options options = OptionsProvider.getInstance().getOptions();
 
         try {
             CommandLine line = parser.parse(options, args);
             List<String> validate = CommandLineValidator.validate(line);
             if (!validate.isEmpty()) {
                 System.out.println("The following errors occured when parsing the command:");
                 
                 for (String string : validate) {
                     System.out.println(string);
                 }
                 
                 System.out.println("");
                 printHelp(options);
                 System.exit(1);
             }
 
             if (line.hasOption("h")) {
                 printHelp(options);
                 System.exit(0);
             }
 
             CustomRestService crudService;
 
             String operation = line.getOptionValue("o");
 
             // weapon mode
             if (line.hasOption("w")) {
                 crudService = new WeaponServiceImpl();
                 switch (operation) {
                     case "C": {
                         WeaponDto dto = DtoBuilder.getWeaponDto(line);
                         Object byId = crudService.create(dto);
                         printEntity(crudService.getResponse(), "Creating a weapon", byId);
                         break;
                     }
                     case "R": {
                         Long id = Long.parseLong(line.getOptionValue("i"));
                         Object byId = crudService.getById(id);
                         printEntity(crudService.getResponse(), "Reading a weapon with id " + id, byId);
                         break;
                     }
                     case "U": {
 
                         WeaponDto dto = DtoBuilder.getWeaponDto(line);
                         Object byId = crudService.update(dto);
                         printEntity(crudService.getResponse(), "Updating a weapon with id " + line.getOptionValue("i"), byId);
 
                         break;
                     }
                     case "D": {
                         Long id = Long.parseLong(line.getOptionValue("i"));
                         Response delete = crudService.delete(id);
                         printEntity(crudService.getResponse(), "Deleting a weapon with id " + line.getOptionValue("i"), crudService.getResponse().getStatusInfo());
                         break;
                     }
                     case "A":
                         List<AbstractDto> all = crudService.getAll();
                         printEntities(crudService.getResponse(), "Reading all weapons", all);
 
                         break;
                 }
             } else if (line.hasOption("a")) {
                 crudService = new AreaServiceImpl();
                 switch (operation) {
                     case "C": {
                         AreaDto dto = DtoBuilder.getAreaDto(line);
                         Object byId = crudService.create(dto);
                         printEntity(crudService.getResponse(), "Creating an area", byId);
                         break;
                     }
                     case "R": {
                         Long id = Long.parseLong(line.getOptionValue("i"));
                         Object byId = crudService.getById(id);
                         printEntity(crudService.getResponse(), "Reading an area with id " + id, byId);
                         break;
                     }
                     case "U": {
                         AreaDto dto = DtoBuilder.getAreaDto(line);
                         Object byId = crudService.update(dto);
                         printEntity(crudService.getResponse(), "Updating an area with id " + line.getOptionValue("i"), byId);
                         break;
                     }
                     case "D": {
                         Long id = Long.parseLong(line.getOptionValue("i"));
                         Object byId = crudService.delete(id);
                         printEntity(crudService.getResponse(), "Deleting an area with id " + line.getOptionValue("i"), crudService.getResponse().getStatusInfo());
                         break;
                     }
                     case "A":
                         List<AbstractDto> all = crudService.getAll();
                         printEntities(crudService.getResponse(), "Reading all areas", all);
                         break;
                 }
             } else {
                 printHelp(options);
             }
         } catch (ParseException ex) {
             System.out.println(ex.getMessage());
             printHelp(options);
             System.exit(1);
         } catch (NumberFormatException ex) {
            System.exit(1);
         }
     }
 
     private static void printHelp(Options options) {
         new HelpFormatter().printHelp("[mode] -o [operation] [arguments]...", options);
     }
 
     private static void printEntity(Response response, String msg, Object o) {
         System.out.println(response.toString());
         System.out.println(msg + ":");
 
         if (o == null) {
             System.out.println("Entity doesn't exist!");
         } else {
             System.out.println(o.toString());
         }
 
     }
 
     private static void printEntities(Response response, String msg, List<AbstractDto> list) {
         System.out.println(response.toString());
         System.out.println(msg + ":");
 
         if (list == null) {
             System.out.println("No entity exist!");
         } else {
             for (AbstractDto wp : list) {
                 System.out.println(wp.toString());
             }
         }
 
     }
 
 }
