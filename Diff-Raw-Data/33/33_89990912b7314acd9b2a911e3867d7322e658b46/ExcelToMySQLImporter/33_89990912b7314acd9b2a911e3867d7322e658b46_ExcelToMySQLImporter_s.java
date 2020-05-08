 /**
  * Fonti is a web application for billing and budgeting
  * Copyright (C) 2009  Carlos Fernandez
  *
  * This file is part of Fonti.
  *
  * Fonti is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * Fonti is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package com.carlos.projects.billing;
 
 import com.carlos.projects.billing.dao.ComponentDAO;
 import com.carlos.projects.billing.dao.FamilyDAO;
 import com.carlos.projects.billing.domain.Component;
 import com.carlos.projects.billing.domain.Family;
 import org.apache.commons.lang.StringUtils;
 import org.apache.poi.ss.usermodel.Row;
 import org.apache.poi.xssf.usermodel.XSSFWorkbook;
 import org.springframework.web.multipart.MultipartFile;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Properties;
 import java.util.Set;
 
 /**
  * Implementation of the Importer interface that imports from an Excel file
  * to a MySQL database
  *
  * @author Carlos Fernandez
  * @date 6 Oct 2009
  */
 public class ExcelToMySQLImporter implements Importer {
 
     private FamilyDAO familyDAO;
 
     private ComponentDAO componentDAO;
 
     private Properties messages;
 
     public ExcelToMySQLImporter(FamilyDAO familyDAO, ComponentDAO componentDAO) {
         this.familyDAO = familyDAO;
         this.componentDAO = componentDAO;
     }
 
     public Long importData(MultipartFile excelFile) {
         XSSFWorkbook workbook;
         try {
             File componentsFile = new File("components.xlsx");
             excelFile.transferTo(componentsFile);
             workbook = new XSSFWorkbook(componentsFile.getAbsolutePath());
         } catch (IOException e) {
             throw new RuntimeException(messages.getProperty("import.error"), e);
         }
         workbook.setMissingCellPolicy(Row.CREATE_NULL_AS_BLANK);
         Iterator<Row> rowIterator = workbook.getSheetAt(workbook.getActiveSheetIndex()).iterator();
         Long numberOfImportedItems = 0L;
         while (rowIterator.hasNext()) {
             Row row = rowIterator.next();
             String familyCode = row.getCell(4).getStringCellValue();
             //The first row of the excel file is the one with the titles
             if (row.getRowNum() != 0 && StringUtils.isNotBlank(familyCode)) {
                 Component component = null;
                 String componentCode = row.getCell(2).getStringCellValue();
                 if (componentDAO.getById(Component.class, componentCode) == null) {
                     component = createComponentFromRow(row);
                     componentDAO.save(component);
                     numberOfImportedItems += 1L;
                 }
                 if (familyDAO.getById(Family.class, familyCode) == null) {
                     Family family = createFamilyFromRowAndComponent(row, component);
                     familyDAO.save(family);
                 }
             }
         }
         return numberOfImportedItems;
     }
 
     private Family createFamilyFromRowAndComponent(Row row, Component component) {
         Family family = new Family();
         family.setCode(StringUtils.trim(row.getCell(4).getStringCellValue()));
         family.setDescription(StringUtils.trim(row.getCell(9).getStringCellValue()));
         Set<Component> components = new HashSet<Component>();
         components.add(component);
         family.setComponents(components);
         return family;
     }
 
     private Component createComponentFromRow(Row row) {
         Component component = new Component();
         component.setCode(StringUtils.trim(row.getCell(2).getStringCellValue()));
         component.setDescription(StringUtils.trim(row.getCell(3).getStringCellValue()));
         component.setDiscount1(row.getCell(6).getNumericCellValue());
         component.setDiscount2(row.getCell(7).getNumericCellValue());
         component.setPrice(row.getCell(8).getNumericCellValue());
         return component;
     }
 
 }
