 /*
  * This file is part of the aidGer project.
  *
  * Copyright (C) 2010-2013 The aidGer Team
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
 
 package de.aidger.model.models;
 
 import static de.aidger.utils.Translation._;
 
 import java.sql.Date;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.List;
 
 import siena.Table;
 import siena.Column;
 
 import de.aidger.model.AbstractModel;
 
 /**
  * Represents a single entry in the employment column of the database. Contains
  * functions to retrieve and change the data in the database.
  * 
  * @author aidGer Team
  */
 @Table("Beschaeftigung")
 public class Employment extends AbstractModel<Employment> {
 
     /**
      * References the corresponding assistant.
      */
     @Column("Hilfskraft_ID")
     private Long assistantId;
 
     /**
      * References the corresponding contract.
      */
     @Column("Vertrag_ID")
     private Long contractId;
 
     /**
      * References the corresponding course.
      */
     @Column("Veranstaltung_ID")
     private Long courseId;
 
     /**
      * The funds of the employment.
      */
     @Column("Fonds")
     private String funds;
 
     /**
      * The month in which the employment took place.
      */
     @Column("Monat")
     private Byte month;
 
     /**
      * The year in which the employment took place.
      */
     @Column("Jahr")
     private Short year;
 
     /**
      * The hours worked during the employment.
      */
     @Column("AnzahlStunden")
     private Double hourCount;
 
     /**
      * The cost unit given by the university.
      */
     @Column("Kostenstelle")
     private Integer costUnit;
 
     /**
      * The qualification of the employment.
      */
     @Column("Qualifikation")
     private String qualification;
 
     /**
      * Remarks regarding the employment.
      */
     @Column("Bemerkung")
     private String remark;
 
     /**
      * Initializes the Employment class.
      */
     public Employment() {
         if (getValidators().isEmpty()) {
             validatePresenceOf(new String[] { "funds", "qualification", "hourCount" },
                 new String[] { _("Funds"), _("Qualification"), _("Hour count") });
             validateInclusionOf(new String[] { "qualification" },
                 new String[] { _("Qualification") }, new String[] { "g", "u", "b" });
             validateExistenceOf(new String[]{"assistantId"},
                     new String[]{_("Assistant")}, new Assistant());
             validateExistenceOf(new String[]{"contractId"},
                     new String[]{_("Contract")}, new Contract());
             validateExistenceOf(new String[]{"courseId"},
                     new String[]{_("Course")}, new Course());
         }
     }
 
     /**
      * Initializes the Employment class with the given employment model.
      * 
      * @param e
      *            the employment model
      */
     public Employment(Employment e) {
         this();
         setId(e.getId());
         setAssistantId(e.getAssistantId());
         setContractId(e.getContractId());
         setCostUnit(e.getCostUnit());
         setCourseId(e.getCourseId());
         setFunds(e.getFunds());
         setHourCount(e.getHourCount());
         setMonth(e.getMonth());
         setQualification(e.getQualification());
         setRemark(e.getRemark());
         setYear(e.getYear());
     }
 
     /**
      * Clone the current employment.
      */
     @Override
     public Employment clone() {
         Employment e = new Employment();
         e.setId(id);
         e.setAssistantId(assistantId);
         e.setContractId(contractId);
         e.setCourseId(courseId);
         e.setCostUnit(costUnit);
         e.setFunds(funds);
         e.setHourCount(hourCount);
         e.setMonth(month);
         e.setQualification(qualification);
         e.setRemark(remark);
         e.setYear(year);
         return e;
     }
 
     /**
      * Custom validation function
      * 
      * @return True if everything is correct
      */
     public boolean validate() {
         boolean ret = true;
         if (costUnit <= 0) {
             addError("costUnit", _("Cost unit"), _("has to be bigger than 0"));
             ret = false;
         }
         if (hourCount < 0) {
             addError("hourCount", _("Hour count"), _("has to be positive"));
             ret = false;
         }
         if (year <= 1000 || year >= 10000) {
             addError("year", _("Year"), _("is an incorrect year"));
             ret = false;
         }
         if (month <= 0 || month >= 13) {
             addError("month", _("Month"), _("is an incorrect month"));
             ret = false;
         }
         if (funds.length() > 10) {
             addError("funds", _("Funds"),
                 _("can't be longer than 10 characters"));
             ret = false;
         }
         return ret;
     }
 
     /**
      * Get all employments during the given time.
      * 
      * @param start
      *            The start date
      * @param end
      *            The end date
      * @return The employments during the given time
      */
     public List<Employment> getEmployments(Date start, Date end) {
         Calendar cal = Calendar.getInstance();
         cal.setTime(start);
         short startYear = (short) cal.get(Calendar.YEAR);
         byte startMonth = (byte) cal.get(Calendar.MONTH);
 
         cal.setTime(end);
         short endYear = (short) cal.get(Calendar.YEAR);
         byte endMonth = (byte) cal.get(Calendar.MONTH);
         return getEmployments(startYear, startMonth, endYear, endMonth);
     }
 
     /**
      * Get all employments during the given time.
      * 
      * @param startYear
      *            The start year
      * @param startMonth
      *            The start month
      * @param endYear
      *            The end year
      * @param endMonth
      *            The end month
      * @return The employments during the given time
      */
     public List<Employment> getEmployments(short startYear, byte startMonth,
             short endYear, byte endMonth) {
         List<Employment> employments = all().filter("year >=", startYear)
                 .filter("year <=", endYear).fetch();
         for (int i = 0; i < employments.size(); i++) {
             if ((employments.get(i).getMonth() < startMonth && employments.get(
                     i).getYear() <= startYear)
                     || (employments.get(i).getMonth() > endMonth && employments
                             .get(i).getYear() >= endYear)) {
                employments.remove(employments.get(i--));
             }
         }
         return employments;
     }
 
     /**
      * Get all employments with the given contract.
      * 
      * @param contract
      *            The given contract
      * @return The employments with the given contract
      */
     public List<Employment> getEmployments(Contract contract) {
         return all().filter("contractId", contract.getId()).fetch();
     }
 
     /**
      * Get all employments of the given assistant.
      * 
      * @param assistant
      *            The given assistant
      * @return The employments of the given assistant
      */
     public List<Employment> getEmployments(Assistant assistant) {
         return all().filter("assistantId", assistant.getId()).fetch();
     }
 
     /**
      * Get all employments for the given course.
      * 
      * @param course
      *            The given course
      * @return The employments for the given course
      */
     public List<Employment> getEmployments(Course course) {
         return all().filter("courseId", course.getId()).fetch();
     }
 
     /**
      * Get all employments in the given semester
      * 
      * @param semester
      *            The given semester
      * @return The employments in the given semester
      */
     public List<Employment> getEmployments(String semester) {
         List<Employment> employments = new Employment().all().fetch();
         for (int i = 0; i < employments.size(); i++) {
             Course course = new Course().getById(employments.get(i).getCourseId());
             if (!course.getSemester().equals(semester)) {
                 employments.remove(employments.get(i));
             }
         }
         return employments;
     }
 
     /**
      * Get a list of distinct cost units.
      * 
      * @return A list of distinct cost units
      */
     public List<Integer> getDistinctCostUnits() {
     	List<Employment> employments = all().order("costUnit").fetch();
     	List<Integer> costUnits = new ArrayList<Integer>();
     	for(Employment employment : employments)
     		if(!costUnits.contains(employment.getCostUnit()))
     			costUnits.add(employment.getCostUnit());
     	return costUnits;
     }
 
     /**
      * Get the id referencing the assistant.
      * 
      * @return The id of the assistant
      */
     public Long getAssistantId() {
         return assistantId;
     }
 
     /**
      * Get the id referencing the contract.
      * 
      * @return The id of the contract
      */
     public Long getContractId() {
         return contractId;
     }
 
     /**
      * Get the cost unit of the contract.
      * 
      * @return The cost unit of the contract
      */
     public Integer getCostUnit() {
         return costUnit;
     }
 
     /**
      * Get the id referencing the course.
      * 
      * @return The id of the course
      */
     public Long getCourseId() {
         return courseId;
     }
 
     /**
      * Get the funds of the employment.
      * 
      * @return The funds of the employment
      */
     public String getFunds() {
         return funds;
     }
 
     /**
      * Get the hours worked during the employment.
      * 
      * @return The hours worked during the employment
      */
     public Double getHourCount() {
         return hourCount;
     }
 
     /**
      * Get the month of the employment.
      * 
      * @return The month of the employment
      */
     public Byte getMonth() {
         return month;
     }
 
     /**
      * Get the qualification of the employment.
      * 
      * @return The qualification of the employment
      */
     public String getQualification() {
         return qualification;
     }
 
     /**
      * Get the remarks regarding the employment.
      * 
      * @return The remarks regarding the employment
      */
     public String getRemark() {
         return remark;
     }
 
     /**
      * Get the year of the employment.
      * 
      * @return The year of the employment
      */
     public Short getYear() {
         return year;
     }
 
     /**
      * Set the id referencing the assistant.
      * 
      * @param id
      *            The id of the assistant
      */
     public void setAssistantId(Long id) {
         assistantId = id;
     }
 
     /**
      * Set the id referencing the contract.
      * 
      * @param id
      *            The id of the contract
      */
     public void setContractId(Long id) {
         contractId = id;
     }
 
     /**
      * Set the cost unit of the contract.
      * 
      * @param cost
      *            The cost unit of the contract
      */
     public void setCostUnit(Integer cost) {
         costUnit = cost;
     }
 
     /**
      * Set the id referencing the course.
      * 
      * @param id
      *            The id of the course
      */
     public void setCourseId(Long id) {
         courseId = id;
     }
 
     /**
      * Set the funds of the employment.
      * 
      * @param funds
      *            The funds of the employment
      */
     public void setFunds(String funds) {
         this.funds = funds;
     }
 
     /**
      * Set the hours worked during the employment.
      * 
      * @param hours
      *            The hours worked during the employment
      */
     public void setHourCount(Double hours) {
         hourCount = hours;
     }
 
     /**
      * Set the month of the employment.
      * 
      * @param month
      *            The month of the employment
      */
     public void setMonth(Byte month) {
         this.month = month;
 
     }
 
     /**
      * Set the qualification of the employment.
      * 
      * @param quali
      *            The qualification of the employment
      */
     public void setQualification(String quali) {
         qualification = quali;
     }
 
     /**
      * Set the remarks regarding the employment.
      * 
      * @param rem
      *            The remarks regarding the employment
      */
     public void setRemark(String rem) {
         remark = rem;
     }
 
     /**
      * Set the year of the employment.
      * 
      * @param year
      *            The year of the employment
      */
     public void setYear(Short year) {
         this.year = year;
     }
 
 }
