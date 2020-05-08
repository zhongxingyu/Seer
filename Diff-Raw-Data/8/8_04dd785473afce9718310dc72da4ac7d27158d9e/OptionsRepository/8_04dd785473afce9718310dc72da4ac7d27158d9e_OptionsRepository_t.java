 /*
  * Copyright 2009 Matej Koubik <mkoubik@gmail.com>
  * 
  */
 
 package backend.sqlite.repositories;
 
 import backend.IOptionsRepository;
import backend.sqlite.db.Database;
 
 /**
  * Repository for options/settings persistency into sqlite database.
  * @author matej
  */
 public class OptionsRepository implements IOptionsRepository {
 
     /**
      * JDBC Connection wrapper.
      */
     private Database db;
 
     /**
      *
      * @param db JDBC connection wrapper
      */
     public OptionsRepository(Database db) {
         this.db = db;
     }
 
     /**
      * Retrieve option from database.
      * @param option option name
      * @return option value
      */
     public String getOption(String option) {
         throw new UnsupportedOperationException("Not supported yet.");
     }
 
     /**
      * Save option to database.
      * @param option option name
      * @param value option value
      */
     public void setOption(String option, String value) {
         throw new UnsupportedOperationException("Not supported yet.");
     }
 
 }
