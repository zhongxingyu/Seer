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
 
 package de.aidger.model.models;
 
 import static de.aidger.utils.Translation._;
 
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.List;
 
 import de.aidger.model.AbstractModel;
 import de.unistuttgart.iste.se.adohive.controller.IFinancialCategoryManager;
 import de.unistuttgart.iste.se.adohive.exceptions.AdoHiveException;
 import de.unistuttgart.iste.se.adohive.model.IFinancialCategory;
 
 /**
  * Represents a single entry in the financial category column of the database.
  * Contains functions to retrieve and change the data in the database.
  * 
  * @author aidGer Team
  */
 public class FinancialCategory extends AbstractModel<IFinancialCategory>
         implements IFinancialCategory {
 
     /**
      * The name of the category.
      */
     private String name;
 
     /**
      * The budget costs of the category.
      */
     private Integer[] budgetCosts;
 
     /**
      * The cost units of the category.
      */
     private Integer[] costUnits;
 
     /**
      * The year the category is valid.
      */
     private Short year;
 
     /**
      * Initialize the FinancialCategory class.
      */
     public FinancialCategory() {
         updatePKs = true;
 
         if (getValidators().isEmpty()) {
             validatePresenceOf(new String[] { "name", "year", "costUnits",
                     "budgetCosts" }, new String[] { _("Name"), _("Year"),
                     _("Cost unit"), _("Budget Costs") });
         }
     }
 
     /**
      * Initialize the FinancialCategory class with the given financial category
      * model.
      * 
      * @param f
      *            the financial category model
      */
     public FinancialCategory(IFinancialCategory f) {
         this();
         setId(f.getId());
         setBudgetCosts(f.getBudgetCosts());
         setCostUnits(f.getCostUnits());
         setName(f.getName());
         setYear(f.getYear());
         setNew(false);
     }
 
     /**
      * Clone the current category.
      */
     @Override
     public FinancialCategory clone() {
         FinancialCategory f = new FinancialCategory();
         f.setBudgetCosts(budgetCosts);
         f.setCostUnits(costUnits);
         f.setName(name);
         f.setYear(year);
         f.doClone(this);
         return f;
     }
 
     /**
      * Check if two objects are equal.
      * 
      * @param o
      *            The other object
      * @return True if both are equal
      */
     @Override
     public boolean equals(Object o) {
         if (o instanceof FinancialCategory) {
             FinancialCategory f = (FinancialCategory) o;
             return (id == null ? f.id == null : id.equals(f.id))
                     && Arrays.equals(f.budgetCosts, budgetCosts)
                     && Arrays.equals(f.costUnits, costUnits)
                     && (year == null ? f.year == null : year.equals(f.year))
                     && (name == null ? f.name == null : f.name.equals(name));
         } else {
             return false;
         }
     }
 
     /**
      * Generate a unique hashcode for this instance.
      * 
      * @return The hashcode
      */
     @Override
     public int hashCode() {
         int hash = 3;
         hash = 37 * hash + (name != null ? name.hashCode() : 0);
         hash = 37 * hash + Arrays.hashCode(budgetCosts);
         hash = 37 * hash + Arrays.hashCode(costUnits);
         hash = 37 * hash + (year != null ? year.hashCode() : 0);
         return hash;
     }
 
     /**
      * Custom validation function.
      * 
      * @return True if the validation is successful
      */
     public boolean validate() {
         boolean ret = true;
         
         if (year != null) {
 	        if (year <= 1000 || year >= 10000) {
 	            addError("year", _("Year"), _("is an incorrect year"));
 	            ret = false;
 	        }
         }
 
         if (budgetCosts != null) {
 	        for (int b : budgetCosts) {
 	            if (b < 0) {
	                addError("budgetCosts", _("Budget Costs"), _("can't be less than zero"));
 	                ret = false;
 	                break;
 	            }
 	        }
         }
         
         if (costUnits != null) {
 	        List<Integer> costUnits_ = Arrays.asList(costUnits);
 	        for (Integer c : costUnits) {
 	        	if (Collections.frequency(costUnits_, c) > 1) {
	        		addError("costUnits", _("Cost Units"), _("can't be the same"));
 	        		
 	        		ret = false;
 	        		break;
 	        	}
 	        }
         }
 
         return ret;
     }
 
     /**
      * Custom validation function for remove().
      * 
      * @return True if everything is correct
      * @throws AdoHiveException
      */
     public boolean validateOnRemove() throws AdoHiveException {
         boolean ret = true;
 
         List courses = (new Course()).getCourses(this);
 
         if (courses.size() > 0) {
             addError(_("Financial Category is still linked to a Course."));
             ret = false;
         }
 
         return ret;
     }
 
     /**
      * Get a list of distinct funds.
      * 
      * @return List of distinct cost units
      * @throws AdoHiveException
      */
     public List<Integer> getDistinctCostUnits() throws AdoHiveException {
         IFinancialCategoryManager mgr = (IFinancialCategoryManager) getManager();
         return mgr.getDistinctCostUnits();
     }
 
     /**
      * Get the budget costs of the category.
      * 
      * @return The budget costs of the category
      */
     @Override
     public Integer[] getBudgetCosts() {
         return budgetCosts;
     }
 
     /**
      * Get the funds of the category.
      * 
      * @return The funds of the category
      */
     @Override
     public Integer[] getCostUnits() {
         return costUnits;
     }
 
     /**
      * Get the name of the category.
      * 
      * @return The name of the category
      */
     @Override
     public String getName() {
         return name;
     }
 
     /**
      * Get the year the category is valid.
      * 
      * @return The year the category is valid
      */
     @Override
     public Short getYear() {
         return year;
     }
 
     /**
      * Set the budget costs of the category.
      * 
      * @param costs
      *            The budget costs of the category
      */
     @Override
     public void setBudgetCosts(Integer[] costs) {
         budgetCosts = costs;
     }
 
     /**
      * Set the funds of the category.
      * 
      * @param costUnits
      *            The cost units of the category
      */
     @Override
     public void setCostUnits(Integer[] costUnits) {
         this.costUnits = costUnits;
     }
 
     /**
      * Set the name of the category.
      * 
      * @param name
      *            The name of the category
      */
     @Override
     public void setName(String name) {
         this.name = name;
     }
 
     /**
      * Set the year the category is valid.
      * 
      * @param year
      *            The year the category is valid.
      */
     @Override
     public void setYear(Short year) {
         this.year = year;
     }
 
 }
