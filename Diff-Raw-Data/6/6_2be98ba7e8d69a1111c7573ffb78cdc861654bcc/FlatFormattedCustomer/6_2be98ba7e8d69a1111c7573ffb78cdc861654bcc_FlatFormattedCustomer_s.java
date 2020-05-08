 /*
  * Metro allows customers from any affiliate library to join any other member library.
  *    Copyright (C) 2013  Edmonton Public Library
  *
  * This program is free software; you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation; either version 2 of the License, or
  * (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
  * MA 02110-1301, USA.
  *
  */
 package mecard.customer;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import mecard.config.CustomerFieldTypes;
 import mecard.config.FlatUserExtendedFieldTypes;
 import mecard.config.FlatUserFieldTypes;
 import mecard.util.DateComparer;
 import mecard.util.Phone;
 import mecard.util.PostalCode;
 
 /**
  *
  * @author Andrew Nisbet <anisbet@epl.ca>
  */
 public class FlatFormattedCustomer implements FormattedCustomer
 {
     private List<FlatTable> customerAccount;
     
     public FlatFormattedCustomer(Customer customer)
     {
         this.customerAccount = new ArrayList<>();
         HashMap<String, String> customerTable = new HashMap<>();
         customerTable.put(FlatUserFieldTypes.USER_ID.toString(), customer.get(CustomerFieldTypes.ID));
         customerTable.put(FlatUserFieldTypes.USER_PIN.toString(), customer.get(CustomerFieldTypes.PIN));
         customerTable.put(FlatUserFieldTypes.USER_FIRST_NAME.toString(), customer.get(CustomerFieldTypes.FIRSTNAME));
         customerTable.put(FlatUserFieldTypes.USER_LAST_NAME.toString(), customer.get(CustomerFieldTypes.LASTNAME));
         customerTable.put(FlatUserFieldTypes.USER_PREFERRED_NAME.toString(), customer.get(CustomerFieldTypes.PREFEREDNAME));
        customerTable.put(FlatUserFieldTypes.USER_BIRTH_DATE.name(), customer.get(CustomerFieldTypes.DOB));
         customerTable.put(FlatUserFieldTypes.USER_PRIV_EXPIRES.toString(), customer.get(CustomerFieldTypes.PRIVILEGE_EXPIRES));
         // set todays date as the date privilege granted.
         customerTable.put(FlatUserFieldTypes.USER_PRIV_GRANTED.toString(), DateComparer.ANSIToday());
         if (customer.isEmpty(CustomerFieldTypes.SEX) == false)
         {
             customerTable.put(FlatUserFieldTypes.USER_CATEGORY2.toString(), customer.get(CustomerFieldTypes.SEX));
         }
         this.customerAccount.add(FlatTable.getInstanceOf(FlatUserExtendedFieldTypes.USER, customerTable));
         // Address Table
         customerTable = new HashMap<>();
         customerTable.put(FlatUserFieldTypes.STREET.toString(), customer.get(CustomerFieldTypes.STREET));
         if (customer.isEmpty(CustomerFieldTypes.PHONE) == false)
         {
             customerTable.put(FlatUserFieldTypes.PHONE.toString(), Phone.formatPhone(customer.get(CustomerFieldTypes.PHONE)));
         }
         // Symphony uses CITY/STATE as a field (sigh)
         String city     = customer.get(CustomerFieldTypes.CITY);
         String province = customer.get(CustomerFieldTypes.PROVINCE);
         customerTable.put(FlatUserFieldTypes.CITY_STATE.toString(), (city + ", " + province.toUpperCase()));
         customerTable.put(FlatUserFieldTypes.POSTALCODE.toString(),
                 PostalCode.formatPostalCode(customer.get(CustomerFieldTypes.POSTALCODE)));
         customerTable.put(FlatUserFieldTypes.EMAIL.toString(), customer.get(CustomerFieldTypes.EMAIL));
         this.customerAccount.add(FlatTable.getInstanceOf(FlatUserExtendedFieldTypes.USER_ADDR1, customerTable));
     }
     
     @Override
     public List<String> getFormattedCustomer()
     {
         List<String> customerList = new ArrayList<>();
         for (FormattedTable table: this.customerAccount)
         {
             customerList.add(table.getData());
         }
         return customerList;
     }
 
     @Override
     public List<String> getFormattedHeader()
     {
         List<String> customerList = new ArrayList<>();
         for (FormattedTable table: this.customerAccount)
         {
             if (table.getName().compareTo(FlatUserExtendedFieldTypes.USER.name()) == 0)
             {
                 customerList.add(table.getHeader());
             }
         }
         return customerList;
     }
 
     @Override
     public boolean setValue(String key, String value)
     {
         for (FormattedTable table: this.customerAccount)
         {
             // check if this table contains a key as provided since BImportTable will
             // insert the value if it doesn't exist, and we don't want all the tables
             // to have this value added. 
             if (containsKey(key))
             {
                 return table.setValue(key, value);
             }
         }
         return false;
     }
 
     @Override
     public boolean containsKey(String key)
     {
         // searches all tables for entries.
         boolean result = false;
         for (FormattedTable table: this.customerAccount)
         {
             if (table.getValue(key).isEmpty() == false)
             {
                 result = true;
             }
         }
         return result;
     }
 
     @Override
     public String getValue(String key)
     {
         StringBuilder result = new StringBuilder();
         for (FormattedTable table: this.customerAccount)
         {
             String value = table.getValue(key);
             if (value.isEmpty() == false)
             {
                 result.append(value);
                 result.append(" "); // separate duplicate values with a space.
             }
         }
         return result.toString().trim();
     }
 
     @Override
     public void insertTable(FormattedTable formattedTable, int index)
     {
         if (index > this.customerAccount.size())
         {
             this.customerAccount.add((FlatTable)formattedTable);
         }
         else if (index < 0)
         {
             this.customerAccount.add(0, (FlatTable)formattedTable);
         }
         else
         {
             this.customerAccount.add(index, (FlatTable)formattedTable);
         }
     }
 
     @Override
     public boolean insertValue(String tableName, String key, String value)
     {
         for (FormattedTable table: this.customerAccount)
         {
             if (table.getName().compareTo(tableName) == 0)
             {
                 table.setValue(key, value);
                 return true;
             }
         }
         return false;
     }
 
 }
