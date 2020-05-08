 /**
  *<p>Title: </p>
  *<p>Description:  </p>
  *<p>Copyright:TODO</p>
  *@author Vishvesh Mulay
  *@version 1.0
  */
 package edu.common.dynamicextensions.entitymanager;
 
 
 
 public interface EntityManagerExceptionConstantsInterface
 {
 
     //Exception constant in case of hibernate system exception
      String DYEXTN_S_001 = "DYEXTN_S_001";
 
     //Exception constant in case of JDBC system exception
      String DYEXTN_S_002 = "DYEXTN_S_002";
 
     //Exception constant in case of user not authenticated
      String DYEXTN_A_001 = "DYEXTN_A_001";
 
     //Exception constant in case of fatal system exception
      String DYEXTN_S_000 = "DYEXTN_A_000";
 
     //Exception constant in case of data type factory not initialised
      String DYEXTN_A_002 = "DYEXTN_A_002";
 
     // Exception constant in case of entity name is invalid for saving
      String DYEXTN_A_003 = "DYEXTN_A_003";
      // Exception constant in case of entity description exceeds maximum length.
      String DYEXTN_A_004 = "DYEXTN_A_004";
      //Exception constant in case of association's cardinalities are invalid
      String DYEXTN_A_005 = "DYEXTN_A_005";
      //Exception constant in case of attributes share the same name
      String DYEXTN_A_006 = "DYEXTN_A_006";
 //   Exception constant in case of name exceeds maximum limit.
      String DYEXTN_A_007 = "DYEXTN_A_007";
 
 
      //Object not found error
      String DYEXTN_A_008 = "DYEXTN_A_008";
      //Exception constant in case when user can not change the data type of an attribute.
      String DYEXTN_A_009 = "DYEXTN_A_009";
      //In case when parent can not be changed for the entity.
      String DYEXTN_A_010 = "DYEXTN_A_010";
 //   In case when parent is not saved for the entity.
      String DYEXTN_A_011 = "DYEXTN_A_011";
 //   Exception constant in case of discriminatorValue or discriminatorColumn not specified for TABLE_PER_HEIRARCHY
 
      String DYEXTN_A_012 = "DYEXTN_A_012";
 //   In case when attribute is deleted having data present for it.
      String DYEXTN_A_013 = "DYEXTN_A_013";
 
 //   In case when of record deletion which is refered by some other record.
      String DYEXTN_A_014 = "DYEXTN_A_014";
 
      // In case when entity group name is duplicate
      String DYEXTN_A_015 = "DYEXTN_A_015";
 
      // In case when association is falsely populated
      String DYEXTN_A_016 = "DYEXTN_A_016";
     
//   In case when form name is duplicate in entitygroup
     String DYEXTN_A_019 = "DYEXTN_A_019";

     
 }
