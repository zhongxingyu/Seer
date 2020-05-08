 /*
  * Copyright (C) 2009-2011 StackFrame, LLC
  * This code is licensed under GPLv2.
  */
 package com.stackframe.sarariman;
 
 import java.math.BigDecimal;
 import java.sql.Connection;
 import java.sql.Date;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.Collection;
 import java.util.LinkedHashMap;
 import java.util.Map;
 import java.util.TreeSet;
 
 /**
  *
  * @author mcculley
  */
 public class Project {
 
     private final long id;
     private final String name;
     private final String contract;
     private final String subcontract;
     private final long customer;
     private final BigDecimal funded;
     private final BigDecimal previouslyBilled;
     private final long terms;
     private final BigDecimal odc_fee;
     private final PeriodOfPerformance pop;
     private final boolean active;
     private final Sarariman sarariman;
 
     public static Map<Long, Project> getProjects(Sarariman sarariman) throws SQLException {
         Connection connection = sarariman.openConnection();
         PreparedStatement ps = connection.prepareStatement("SELECT * FROM projects ORDER BY name");
         try {
             ResultSet resultSet = ps.executeQuery();
             try {
                 Map<Long, Project> map = new LinkedHashMap<Long, Project>();
                 while (resultSet.next()) {
                     long id = resultSet.getLong("id");
                     String name = resultSet.getString("name");
                     long customer = resultSet.getLong("customer");
                     String contract = resultSet.getString("contract_number");
                     String subcontract = resultSet.getString("subcontract_number");
                     BigDecimal funded = resultSet.getBigDecimal("funded");
                     BigDecimal previouslyBilled = resultSet.getBigDecimal("previously_billed");
                     long terms = resultSet.getLong("terms");
                     PeriodOfPerformance pop = new PeriodOfPerformance(resultSet.getDate("pop_start"), resultSet.getDate("pop_end"));
                     BigDecimal odc_fee = resultSet.getBigDecimal("odc_fee");
                     boolean active = resultSet.getBoolean("active");
                     map.put(id, new Project(sarariman, id, name, customer, contract, subcontract, funded, previouslyBilled, terms, pop, odc_fee, active));
                 }
                 return map;
             } finally {
                 resultSet.close();
             }
         } finally {
             ps.close();
             connection.close();
         }
     }
 
     Project(Sarariman sarariman, long id, String name, long customer, String contract, String subcontract, BigDecimal funded,
             BigDecimal previouslyBilled, long terms, PeriodOfPerformance pop, BigDecimal odc_fee, boolean active) {
         this.sarariman = sarariman;
         this.id = id;
         this.name = name;
         this.customer = customer;
         this.contract = contract;
         this.subcontract = subcontract;
         this.funded = funded;
         this.previouslyBilled = previouslyBilled;
         this.terms = terms;
         this.odc_fee = odc_fee;
         this.pop = pop;
         this.active = active;
     }
 
     public long getId() {
         return id;
     }
 
     public String getName() {
         return name;
     }
 
     public String getContract() {
         return contract;
     }
 
     public String getSubcontract() {
         return subcontract;
     }
 
     public long getCustomer() {
         return customer;
     }
 
     public BigDecimal getFunded() {
         return funded;
     }
 
     public BigDecimal getPreviouslyBilled() {
         return previouslyBilled;
     }
 
     public Collection<Task> getTasks() throws SQLException {
         return Task.getTasks(sarariman, this);
     }
 
     public long getTerms() {
         return terms;
     }
 
     public PeriodOfPerformance getPop() {
         return pop;
     }
 
     public BigDecimal getODCFee() {
         return odc_fee;
     }
 
     public boolean isActive() {
         return active;
     }
 
     public Collection<LineItem> getLineItems() throws SQLException {
         return LineItem.getLineItems(sarariman, id);
     }
 
     public Collection<Date> getDaysBilled() throws SQLException {
         Connection connection = sarariman.openConnection();
         PreparedStatement ps = connection.prepareStatement("SELECT DISTINCT(date) AS date "
                 + "FROM hours AS h "
                 + "JOIN tasks AS t on h.task = t.id "
                 + "JOIN projects AS p on p.id = t.project "
                 + "JOIN labor_category_assignments AS a ON (a.employee = h.employee AND h.date >= a.pop_start AND h.date <= a.pop_end) "
                 + "JOIN labor_categories AS c ON (c.id = a.labor_category AND h.date >= c.pop_start AND h.date <= c.pop_end AND c.project = p.id) "
                 + "WHERE p.id = ? AND t.billable = TRUE;");
         try {
             ps.setLong(1, id);
             ResultSet rs = ps.executeQuery();
             try {
                 Collection<Date> days = new TreeSet<Date>();
                 while (rs.next()) {
                     days.add(rs.getDate("date"));
                 }
 
                 return days;
             } finally {
                 rs.close();
             }
         } finally {
             ps.close();
             connection.close();
         }
     }
 
     public boolean isManager(Employee employee) throws SQLException {
         Connection connection = sarariman.openConnection();
         try {
             PreparedStatement ps = connection.prepareStatement("SELECT * FROM project_managers WHERE employee=? AND project=?");
             try {
                 ps.setInt(1, employee.getNumber());
                 ps.setLong(2, id);
                 ResultSet rs = ps.executeQuery();
                 return rs.first();
             } finally {
                 ps.close();
             }
         } finally {
             connection.close();
         }
     }
 
     public static boolean isManager(Employee employee, Project project) throws SQLException {
         return project.isManager(employee);
     }
 
     public static Project create(Sarariman sarariman, String name, Long customer, Date pop_start, Date pop_end, String contract,
             String subcontract, BigDecimal funded, BigDecimal previouslyBilled, long terms, BigDecimal odc_fee, boolean active) throws SQLException {
         Connection connection = sarariman.openConnection();
        PreparedStatement ps = connection.prepareStatement("INSERT INTO projects (name, customer, pop_start, pop_end, contract_number, subcontract_number, funded, previously_billed, terms, odc_fee, active) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
         try {
             ps.setString(1, name);
             ps.setLong(2, customer);
             ps.setDate(3, pop_start);
             ps.setDate(4, pop_end);
             ps.setString(5, contract);
             ps.setString(6, subcontract);
             ps.setBigDecimal(7, funded);
             ps.setBigDecimal(8, previouslyBilled);
             ps.setLong(9, terms);
            ps.setBigDecimal(10, odc_fee);
             ps.setBoolean(11, active);
             ps.executeUpdate();
             ResultSet rs = ps.getGeneratedKeys();
             try {
                 rs.next();
                 PeriodOfPerformance pop = new PeriodOfPerformance(pop_start, pop_end);
                 return new Project(sarariman, rs.getLong(1), name, customer, contract, subcontract, funded, previouslyBilled, terms, pop, odc_fee, active);
             } finally {
                 rs.close();
             }
         } finally {
             ps.close();
             connection.close();
         }
     }
 
     public void update(String name, Long customer, Date pop_start, Date pop_end, String contract, String subcontract,
             BigDecimal funded, BigDecimal previouslyBilled, long terms, BigDecimal odc_fee, boolean active) throws SQLException {
         Connection connection = sarariman.openConnection();
         PreparedStatement ps = connection.prepareStatement("UPDATE projects SET name=?, customer=?, pop_start=?, pop_end=?, contract_number=?, subcontract_number=?, funded=?, previously_billed=?, terms=?, odc_fee=?, active=? WHERE id=?");
         try {
             ps.setString(1, name);
             ps.setLong(2, customer);
             ps.setDate(3, pop_start);
             ps.setDate(4, pop_end);
             ps.setString(5, contract);
             ps.setString(6, subcontract);
             ps.setBigDecimal(7, funded);
             ps.setBigDecimal(8, previouslyBilled);
             ps.setLong(9, terms);
             ps.setBigDecimal(10, odc_fee);
             ps.setBoolean(11, active);
             ps.setLong(12, id);
             ps.executeUpdate();
         } finally {
             ps.close();
             connection.close();
         }
     }
 
     public void delete() throws SQLException {
         Connection connection = sarariman.openConnection();
         PreparedStatement ps = connection.prepareStatement("DELETE FROM projects WHERE id=?");
         try {
             ps.setLong(1, id);
             ps.executeUpdate();
         } finally {
             ps.close();
             connection.close();
         }
     }
 
     @Override
     public String toString() {
         return "{id=" + id + ",name=" + name + ",customer=" + customer + ",contract=" + contract + ",subcontract=" + subcontract
                 + ",funded=" + funded + ",previouslyBilled=" + previouslyBilled + ",terms=" + terms + "}";
     }
 
 }
