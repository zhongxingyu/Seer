 /*
  * Copyright (C) 2009-2011 StackFrame, LLC
  * This code is licensed under GPLv2.
  */
 package com.stackframe.sarariman;
 
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Map;
 
 /**
  *
  * @author mcculley
  */
 public class Task {
 
     private final int id;
     private final String name;
     private final boolean billable;
     private final boolean active;
     private final int lineItem;
     private final Project project;
 
     public static Collection<Task> getTasks(Sarariman sarariman) throws SQLException {
         Map<Long, Project> projects = sarariman.getProjects();
         Connection connection = sarariman.openConnection();
         PreparedStatement ps = connection.prepareStatement(
                "SELECT t.id AS task_id, t.name AS task_name, t.billable, t.active, t.line_item, "
                 + "p.id AS project_id, p.name AS project_name, "
                 + "c.id AS customer_id, c.name AS customer_name "
                 + "FROM tasks AS t "
                 + "LEFT OUTER JOIN projects AS p ON t.project = p.id "
                 + "LEFT OUTER JOIN customers AS c ON c.id = p.customer");
         try {
             ResultSet resultSet = ps.executeQuery();
             try {
                 Collection<Task> list = new ArrayList<Task>();
                 while (resultSet.next()) {
                     int id = resultSet.getInt("task_id");
                     String task_name = resultSet.getString("task_name");
                     boolean billable = resultSet.getBoolean("billable");
                     int lineItem = resultSet.getInt("line_item");
                     boolean active = resultSet.getBoolean("active");
                     long project_id = resultSet.getInt("project_id");
                     Project project = null;
                     if (project_id != 0) {
                         project = projects.get(project_id);
                     }
 
                     list.add(new Task(id, task_name, billable, lineItem, active, project));
                 }
 
                 return list;
             } finally {
                 resultSet.close();
             }
         } finally {
             ps.close();
             connection.close();
         }
     }
 
     public static Collection<Task> getUnbillableTasks(Sarariman sarariman, Employee employee) throws SQLException {
         return getTasks(sarariman, employee, false);
     }
 
     public static Collection<Task> getBillableTasks(Sarariman sarariman, Employee employee) throws SQLException {
         return getTasks(sarariman, employee, true);
     }
 
     private static Collection<Task> getTasks(Sarariman sarariman, Employee employee, boolean billable) throws SQLException {
         Map<Long, Project> projects = sarariman.getProjects();
         Connection connection = sarariman.openConnection();
         PreparedStatement ps = connection.prepareStatement(
                 "SELECT t.id AS task_id, t.name AS task_name, t.active, t.line_item, "
                 + "p.id AS project_id, p.name AS project_name, "
                 + "c.id AS customer_id, c.name AS customer_name "
                 + "FROM tasks AS t "
                 + "JOIN task_assignments AS a ON a.task = t.id "
                 + "LEFT OUTER JOIN projects AS p ON t.project = p.id "
                 + "LEFT OUTER JOIN customers AS c ON c.id = p.customer "
                 + "WHERE employee=? AND billable=? AND active=TRUE");
         try {
             ps.setInt(1, employee.getNumber());
             ps.setBoolean(2, billable);
             ResultSet resultSet = ps.executeQuery();
             try {
                 Collection<Task> list = new ArrayList<Task>();
                 while (resultSet.next()) {
                     int id = resultSet.getInt("task_id");
                     String task_name = resultSet.getString("task_name");
                     boolean active = resultSet.getBoolean("active");
                     int lineItem = resultSet.getInt("line_item");
                     long project_id = resultSet.getInt("project_id");
                     Project project = null;
                     if (project_id != 0) {
                         project = projects.get(project_id);
                     }
 
                     list.add(new Task(id, task_name, billable, lineItem, active, project));
                 }
 
                 return list;
             } finally {
                 resultSet.close();
             }
         } finally {
             ps.close();
             connection.close();
         }
     }
 
     public static Collection<Task> getTasks(Sarariman sarariman, Project project) throws SQLException {
         Connection connection = sarariman.openConnection();
         PreparedStatement ps = connection.prepareStatement("SELECT t.id AS task_id, t.name AS task_name, t.billable, t.active, t.line_item "
                 + "FROM tasks AS t "
                 + "WHERE t.project = ?");
         ps.setLong(1, project.getId());
         try {
             ResultSet resultSet = ps.executeQuery();
             try {
                 Collection<Task> list = new ArrayList<Task>();
                 while (resultSet.next()) {
                     int id = resultSet.getInt("task_id");
                     String task_name = resultSet.getString("task_name");
                     boolean billable = resultSet.getBoolean("billable");
                     int lineItem = resultSet.getInt("line_item");
                     boolean active = resultSet.getBoolean("active");
                     list.add(new Task(id, task_name, billable, lineItem, active, project));
                 }
 
                 return list;
             } finally {
                 resultSet.close();
             }
         } finally {
             ps.close();
             connection.close();
         }
     }
 
     private Task(int id, String name, boolean billable, int lineItem, boolean active, Project project) {
         this.id = id;
         this.name = name;
         this.billable = billable;
         this.active = active;
         this.project = project;
         this.lineItem = lineItem;
     }
 
     public int getId() {
         return id;
     }
 
     public String getName() {
         return name;
     }
 
     public boolean isBillable() {
         return billable;
     }
 
     public boolean isActive() {
         return active;
     }
 
     public Project getProject() {
         return project;
     }
 
     public int getLineItem() {
         return lineItem;
     }
 
 }
