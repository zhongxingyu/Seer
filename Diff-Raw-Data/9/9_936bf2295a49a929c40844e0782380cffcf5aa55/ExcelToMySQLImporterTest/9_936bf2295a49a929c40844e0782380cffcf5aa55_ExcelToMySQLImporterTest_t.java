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
 import org.junit.Before;
 import org.junit.Test;
 import org.mockito.InOrder;
 import org.mockito.Mock;
 import org.mockito.MockitoAnnotations;
 import org.springframework.mock.web.MockMultipartFile;
 import org.springframework.web.multipart.MultipartFile;
 
 import java.util.HashSet;
 
 import static org.hamcrest.Matchers.is;
 import static org.junit.Assert.assertThat;
 import static org.mockito.Mockito.*;
 
 /**
  * Unit tests for {@link ExcelToMySQLImporter}
  *
  * @author Carlos Fernandez
  * @date 6 Oct 2009
  */
 public class ExcelToMySQLImporterTest {
 
     @Mock private FamilyDAO familyDAO;
 
     @Mock private ComponentDAO componentDAO;
 
     @Before
     public void setup() {
         MockitoAnnotations.initMocks(this);
     }
 
     @Test
     public void shouldImportZeroComponentsFromEmptyFile() throws Exception {
         //given
         ExcelToMySQLImporter importer = new ExcelToMySQLImporter(familyDAO, componentDAO);
         MultipartFile file = new MockMultipartFile("emptyData.xlsx", getClass().getResourceAsStream("/emptyData.xlsx"));
 
         //when
         Long importedComponents = importer.importData(file);
 
         //then
         assertThat("The number of components imported is not 0", importedComponents, is(0L));
         verifyZeroInteractions(familyDAO);
         verifyZeroInteractions(componentDAO);
     }
 
     @Test
     public void shouldImportNComponentsAndNFamiliesFromFileWithNComponentsAndNFamiliesAllOfThemWithFamilyCodeValue() throws Exception {
         //given
         ExcelToMySQLImporter importer = new ExcelToMySQLImporter(familyDAO, componentDAO);
         MultipartFile file = new MockMultipartFile("data.xlsx", getClass().getResourceAsStream("/data.xlsx"));
 
         Family family1 = createFamily("36", "CONTADORES");
         Component component1 = createComponent("KITAZUL162212",
             "KIT AZUL BATERIA 16-2-21/2 BAHISA", 33.0, 0.0, 383.2065, family1);
         Family family2 = createFamily("33", "ACCES. FONTANERIA");
         Component component2 = createComponent("000636",
             "LATIGUILLO FLEX.RIVER MH 2 300", 40.0, 0.0, 38.274, family2);
 
         //when
         Long importedComponents = importer.importData(file);
 
         //then
         assertThat("The number of components imported is not 2", importedComponents, is(2L));
         InOrder inOrder = inOrder(familyDAO, componentDAO);
 
         inOrder.verify(familyDAO).getById(Family.class, family1.getCode());
         inOrder.verify(componentDAO).getById(Component.class, component1.getCode());
         inOrder.verify(familyDAO).save(family1);
 
         inOrder.verify(familyDAO).getById(Family.class, family2.getCode());
         inOrder.verify(componentDAO).getById(Component.class, component2.getCode());
         inOrder.verify(familyDAO).save(family2);
 
         verifyNoMoreInteractions(familyDAO, componentDAO);
     }
 
     @Test
     public void shouldImportNComponentsAndNFamiliesFromFileWithNComponentsAndNFamiliesWithFamilyCodeValueAndSomeOtherComponentsAndFamiliesWithNoFamilyCodeValue()
             throws Exception {
         //given
         ExcelToMySQLImporter importer = new ExcelToMySQLImporter(familyDAO, componentDAO);
         MultipartFile file = new MockMultipartFile("dataWithMissingFamilyCodes.xlsx",
                 getClass().getResourceAsStream("/dataWithMissingFamilyCodes.xlsx"));
 
         Family family = createFamily("36", "CONTADORES");
         Component component = createComponent("KITAZUL162212",
             "KIT AZUL BATERIA 16-2-21/2 BAHISA", 33.0, 0.0, 383.2065, family);
 
         //when
         Long importedComponents = importer.importData(file);
 
         //then
         assertThat("The number of components imported is not 1", importedComponents, is(1L));
         InOrder inOrder = inOrder(familyDAO, componentDAO);
 
         inOrder.verify(familyDAO).getById(Family.class, family.getCode());
         inOrder.verify(componentDAO).getById(Component.class, component.getCode());
         inOrder.verify(familyDAO).save(family);
 
         verifyNoMoreInteractions(familyDAO, componentDAO);
     }
 
     @Test
     public void shouldNotImportComponentsWithTheSameCodeTwice()
             throws Exception {
         //given
         ExcelToMySQLImporter importer = new ExcelToMySQLImporter(familyDAO, componentDAO);
         MultipartFile file = new MockMultipartFile("dataWithDuplicatedComponentCodes.xlsx",
                 getClass().getResourceAsStream("/dataWithDuplicatedComponentCodes.xlsx"));
 
         Family family1 = createFamily("36", "CONTADORES");
         Component component = createComponent("KITAZUL162212",
             "KIT AZUL BATERIA 16-2-21/2 BAHISA", 33.0, 0.0, 383.2065, family1);
         Family family2 = createFamily("33", "ACCES. FONTANERIA");
         family2.setComponents(new HashSet<Component>());
 
         when(componentDAO.getById((Class<Component>) anyObject(), anyString())).thenReturn(null, component);
 
         //when
         Long importedComponents = importer.importData(file);
 
         //then
         assertThat("The number of components imported is not 1", importedComponents, is(1L));
         InOrder inOrder = inOrder(familyDAO, componentDAO);
 
         inOrder.verify(familyDAO).getById(Family.class, family1.getCode());
         inOrder.verify(componentDAO).getById(Component.class, component.getCode());
         inOrder.verify(familyDAO).save(family1);
 
         inOrder.verify(familyDAO).getById(Family.class, family2.getCode());
         inOrder.verify(componentDAO).getById(Component.class, component.getCode());
         inOrder.verify(familyDAO).save(family2);
 
         verifyNoMoreInteractions(familyDAO, componentDAO);
     }
 
     @Test
     public void shouldNotImportFamiliesWithTheSameCodeTwice()
             throws Exception {
         //given
         ExcelToMySQLImporter importer = new ExcelToMySQLImporter(familyDAO, componentDAO);
         MultipartFile file = new MockMultipartFile("dataWithDuplicatedFamilyCodes.xlsx",
                 getClass().getResourceAsStream("/dataWithDuplicatedFamilyCodes.xlsx"));
 
         Family family = createFamily("36", "CONTADORES");
         Component component1 = createComponent("KITAZUL162212",
            "KIT AZUL BATERIA 16-2-21/2 BAHISA", 33.0, 0.0, 383.2065, family);
         Component component2 = createComponent("000636",
            "LATIGUILLO FLEX.RIVER MH 2 300", 40.0, 0.0, 38.274, family);
 
         when(familyDAO.getById(Family.class, family.getCode())).thenReturn(null, family);
 
         //when
         Long importedComponents = importer.importData(file);
 
         //then
         assertThat("The number of components imported is not 2", importedComponents, is(2L));
         InOrder inOrder = inOrder(familyDAO, componentDAO);
 
         inOrder.verify(familyDAO).getById(Family.class, family.getCode());
         inOrder.verify(componentDAO).getById(Component.class, component1.getCode());
         inOrder.verify(familyDAO).save(family);
 
         inOrder.verify(familyDAO).getById(Family.class, family.getCode());
         inOrder.verify(componentDAO).getById(Component.class, component2.getCode());
         inOrder.verify(componentDAO).save(component2);
 
         verifyNoMoreInteractions(familyDAO, componentDAO);
     }
 
     private Component createComponent(String code, String description, double discount1,
             double discount2, double price, Family family1) {
         Component component = new Component();
         component.setCode(code);
         component.setDescription(description);
        component.setPrice(price);
         component.setDiscount1(discount1);
         component.setDiscount2(discount2);
         component.setFamily(family1);
         return component;
     }
 
     private Family createFamily(String code, String description) {
         Family family = new Family();
         family.setCode(code);
         family.setDescription(description);
         return family;
     }
 
 }
