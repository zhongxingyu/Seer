 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package com.seldonsystems.demo.javafx.employeeadmin.model.vo;
 
 import com.seldonsystems.demo.javafx.employeeadmin.model.enums.DeptEnum;
 
 /**
  *
  * @author Jhonghee Park @ Seldon Systems, Inc.
  */
 public class UserVO {
 
     private String username = "";
     private String fname = "";
     private String lname = "";
     private String email = "";
     private String password = "";
     private DeptEnum department = DeptEnum.NONE_SELECTED;
 
    public UserVO() {}

     public UserVO(String uname, String fname, String lname, String email, String password, DeptEnum department) {
        if( uname != null ) this.username = uname;
        if( fname != null ) this.fname = fname;
        if( lname != null ) this.lname = lname;
        if( email != null ) this.email = email;
        if( password != null ) this.password = password;
        if( department != null ) this.department = department;
     }
 
     public Boolean isValid() {
         return !username.equals("") && !password.equals("") && !department.equals(DeptEnum.NONE_SELECTED);
     }
 
     public String getGivenName() {
         return this.lname + ", " + this.fname;
     }
 
     public DeptEnum getDepartment() {
         return department;
     }
 
     public String getEmail() {
         return email;
     }
 
     public String getFname() {
         return fname;
     }
 
     public String getLname() {
         return lname;
     }
 
     public String getPassword() {
         return password;
     }
 
     public String getUsername() {
         return username;
     }
 
     
 }
