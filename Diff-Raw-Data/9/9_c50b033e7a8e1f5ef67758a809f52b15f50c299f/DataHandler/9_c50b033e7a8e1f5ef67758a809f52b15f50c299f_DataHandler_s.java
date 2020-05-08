 /**
  * This file is part of Flash Cart.
  *
  * Copyright (C) 2011 Javier Estévez
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published
  * by the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  *
  */
 package com.valdaris.shoppinglist.dao.impl;
 
 import java.sql.SQLException;
 import java.util.List;
 
 import com.j256.ormlite.dao.Dao;
 import com.j256.ormlite.stmt.QueryBuilder;
 import com.j256.ormlite.stmt.Where;
 import com.valdaris.shoppinglist.dao.DatabaseHelper;
 import com.valdaris.shoppinglist.dao.IDataHandler;
 import com.valdaris.shoppinglist.model.ListItem;
 import com.valdaris.shoppinglist.model.Product;
 import com.valdaris.shoppinglist.model.ShoppingList;
 
 /**
  * @author Javier Estévez
  * 
  */
 public class DataHandler implements IDataHandler {
 
     DatabaseHelper helper;
 
     public DataHandler(DatabaseHelper helper) {
         this.helper = helper;
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see com.valdaris.shoppinglist.data.IDataHandler#getLists()
      */
     @Override
     public List<ShoppingList> getLists() {
 
         Dao<ShoppingList, Integer> dao;
         try {
             dao = helper.getShoppingListDao();
             QueryBuilder<ShoppingList, Integer> builder = dao.queryBuilder();
             builder.orderBy(ShoppingList.DATA_CREATION_FIELD_NAME, false);
             List<ShoppingList> list = dao.query(builder.prepare());
             return list;
         } catch (SQLException e) {
             throw new RuntimeException(e);
         }
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see
      * com.valdaris.shoppinglist.data.IDataHandler#save(com.valdaris.shoppinglist
      * .data.ShoppingList)
      */
     @Override
     public void create(ShoppingList list) {
         try {
             Dao<ShoppingList, Integer> dao = helper.getShoppingListDao();
             dao.create(list);
         } catch (SQLException e) {
             throw new RuntimeException(e);
         }
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see
      * com.valdaris.shoppinglist.data.IDataHandler#getListProducts(com.valdaris
      * .shoppinglist.data.ShoppingList)
      */
     @Override
     public List<ListItem> getListProducts(ShoppingList list) {
         try {
             Dao<ListItem, Integer> dao = helper.getListProductDao();
             QueryBuilder<ListItem, Integer> builder = dao.queryBuilder();
             Where<ListItem, Integer> where = builder.where();
             where.eq(ListItem.LIST_ID_FIELD, list);
             builder.setWhere(where);
             List<ListItem> products = builder.query();
             return products;
         } catch (SQLException e) {
             throw new RuntimeException(e);
         }
     }
 
     @Override
     public void setListProducts(ShoppingList list, List<ListItem> products) {
         try {
             Dao<ListItem, Integer> dao = helper.getListProductDao();
             for (ListItem p : products) {
                 boolean update = p.getList() != null
                         && p.getList().getId() == list.getId();
                 p.setList(list);
                 if (update) {
                     dao.update(p);
                 } else {
                     dao.create(p);
                 }
             }
         } catch (SQLException e) {
             throw new RuntimeException(e);
         }
     }
 
    @Override
     public List<Product> findProductsByName(String name) {
         try {
             Dao<Product, Integer> dao = helper.getProductDao();
             QueryBuilder<Product, Integer> builder = dao.queryBuilder();
             Where<Product, Integer> where = builder.where();
             where.like(Product.NAME_FIELD_NAME, name);
             builder.setWhere(where);
             List<Product> products = builder.query();
             return products;
         } catch (SQLException e) {
             throw new RuntimeException(e);
         }
     }
 
 }
