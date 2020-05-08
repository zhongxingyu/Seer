 /*
  * This file is part of the aidGer project.
  *
  * Copyright (C) 2010-2011 The aidGer Team
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package de.aidger.utils;
 
 import java.beans.XMLDecoder;
 import java.beans.XMLEncoder;
 import java.io.BufferedOutputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import de.aidger.model.Runtime;
 import de.aidger.model.models.CostUnit;
 import de.unistuttgart.iste.se.adohive.controller.IAdoHiveManager;
 import de.unistuttgart.iste.se.adohive.exceptions.AdoHiveException;
 import de.unistuttgart.iste.se.adohive.model.IAdoHiveModel;
 
 /**
  * This class manages data stored in a XML file.
  * 
  * @author aidGer Team
  */
 @SuppressWarnings("unchecked")
 public class DataXMLManager implements IAdoHiveManager {
 
     /**
      * The file name where the data will be stored.
      */
     private final String filename = Runtime.getInstance().getConfigPath()
             + "data.xml";
 
     /**
      * The cost unit map as list that is loaded from file.
      */
     private List<CostUnit> costUnitMap = new ArrayList<CostUnit>();
 
     /**
      * The unedited cost unit. It is needed by update().
      */
     private CostUnit costUnitBeforeEdit;
 
     /**
      * The email suffix for the assistants.
      */
     private String emailSuffix;
 
     /**
      * Creates an example cost unit map and stores it to file.
      */
     private void createDefaultData() {
         try {
             XMLEncoder xmlEncoder = new XMLEncoder(new BufferedOutputStream(
                 new FileOutputStream(filename)));
 
             // create example cost units
             CostUnit c = new CostUnit();
             c.setCostUnit("11111111");
             c.setFunds("Fonds 1");
             c.setTokenDB("A");
 
             CostUnit c_ = new CostUnit();
             c_.setCostUnit("22222222");
             c_.setFunds("Fonds 2");
             c_.setTokenDB("B");
 
             CostUnit c__ = new CostUnit();
             c__.setCostUnit("33333333");
             c__.setFunds("Fonds 3");
             c__.setTokenDB("C");
 
             costUnitMap.add(c);
             costUnitMap.add(c_);
             costUnitMap.add(c__);
 
             emailSuffix = "studi.informatik.uni-stuttgart.de";
 
             xmlEncoder.writeObject(costUnitMap);
             xmlEncoder.writeObject(emailSuffix);
 
             xmlEncoder.close();
         } catch (IOException e) {
         }
     }
 
     /**
      * Reads the data by loading it from XML file.
      */
     private void readData() {
         try {
             XMLDecoder dec = new XMLDecoder(new FileInputStream(filename));
 
             costUnitMap = (List<CostUnit>) dec.readObject();
             emailSuffix = (String) dec.readObject();
 
             // mark all cost units as not new
             for (CostUnit c : costUnitMap) {
                 c.setNew(false);
             }
 
             dec.close();
         } catch (IOException e) {
         }
     }
 
     /**
      * Saves the data to the XML file.
      */
     private void saveData() {
         try {
             XMLEncoder xmlEncoder = new XMLEncoder(new BufferedOutputStream(
                 new FileOutputStream(filename)));
 
             xmlEncoder.writeObject(costUnitMap);
             xmlEncoder.writeObject(emailSuffix);
 
             xmlEncoder.close();
         } catch (IOException e) {
         }
     }
 
     /**
      * Creates a data manager.
      */
     public DataXMLManager() {
         File file = new File(filename);
 
         if (file.exists()) {
             readData();
         } else {
             createDefaultData();
         }
     }
 
     /**
      * Returns the cost unit map as list of cost units.
      * 
      * @return the cost unit map as list of cost units
      */
     public List<CostUnit> getCostUnitMap() {
         try {
             return getAll();
         } catch (AdoHiveException e) {
 
         }
 
         return null;
     }
 
     /**
      * Returns the email suffix of the assistants.
      * 
      * @return the email suffix of the assistants
      */
     public String getEmailSuffix() {
         return emailSuffix;
     }
 
     /**
      * Sets the unedited cost unit.
      * 
      * @param costUnitBeforeEdit
      *            the unedited cost unit
      */
     public void setCostUnitBeforeEdit(CostUnit costUnitBeforeEdit) {
         this.costUnitBeforeEdit = costUnitBeforeEdit;
     }
 
     /**
      * Returns a cost unit object from given database token.
      * 
      * @param tokenDB
      *            the database token
      * @return a cost unit object. If no object was found, null is returned.
      */
     public CostUnit fromTokenDB(String tokenDB) {
         for (CostUnit c : getCostUnitMap()) {
             if (tokenDB.equals(c.getTokenDB())) {
                 return c;
             }
         }
 
         return null;
     }
 
     /**
      * Returns an object list of all stored cost units.
      * 
      * @return an object list of all stored cost units
      */
     public Object[] getCostUnits() {
         List<String> costUnits = new ArrayList<String>();
 
         for (CostUnit c : getCostUnitMap()) {
             costUnits.add(c.getCostUnit());
         }
 
         return costUnits.toArray();
     }
 
     @Override
     public List<CostUnit> getAll() throws AdoHiveException {
        List<CostUnit> costUnits = new ArrayList<CostUnit>();

        for (CostUnit costUnit : costUnitMap) {
            costUnits.add(costUnit.clone());
        }

        return costUnits;
     }
 
     @Override
     public boolean add(IAdoHiveModel e) throws AdoHiveException {
         costUnitMap.add((CostUnit) e);
 
         saveData();
 
         return true;
     }
 
     @Override
     public boolean update(IAdoHiveModel o) throws AdoHiveException {
         costUnitMap.remove(costUnitBeforeEdit);
         costUnitMap.add((CostUnit) o);
 
         saveData();
 
         return true;
     }
 
     @Override
     public boolean remove(IAdoHiveModel e) throws AdoHiveException {
         costUnitMap.remove(e);
 
         saveData();
 
         return true;
     }
 
     // the following methods will not be used but are implemented
 
     @Override
     public void clear() throws AdoHiveException {
         costUnitMap.clear();
     }
 
     @Override
     public boolean contains(IAdoHiveModel o) throws AdoHiveException {
         return costUnitMap.contains(o);
     }
 
     @Override
     public IAdoHiveModel get(int index) throws AdoHiveException {
         return costUnitMap.get(index);
     }
 
     @Override
     public boolean isEmpty() throws AdoHiveException {
         return costUnitMap.isEmpty();
     }
 
     @Override
     public int size() throws AdoHiveException {
         return costUnitMap.size();
     }
 
     @Override
     public List<CostUnit> subList(int fromIndex, int toIndex)
             throws AdoHiveException {
         return costUnitMap.subList(fromIndex, toIndex);
     }
 
     @Override
     public IAdoHiveModel getById(int id) throws AdoHiveException {
         return get(id);
     }
 
     @Override
     public IAdoHiveModel getByKeys(Object... o) throws AdoHiveException {
         return null;
     }
 }
