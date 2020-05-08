 package com.ecnmelog.app;
 
 
 import com.ecnmelog.model.AbstractStockage;
 import com.ecnmelog.model.AbstractAttente;
 import com.ecnmelog.model.Stockage;
 import com.ecnmelog.model.Attente;
 import com.ecnmelog.model.DbConn;
 import com.ecnmelog.model.Container;
 import com.ecnmelog.model.ContainerException;
 import com.ecnmelog.model.EmplacementException;
 import com.ecnmelog.model.DbConn;
 
 
 import com.ecnmelog.view.Interface;
 import com.ecnmelog.controller.StockageController;
 import java.sql.*;
 
 /**
  * Classe principale de l'application
  */
 public class App {
     /**
      * Méthode principale de l'application.
      * @param args Les arguments passés en CLI
      */
     public static void main(String[] args) {
        AbstractStockage stock = new Stockage(100);
         AbstractAttente att = new Attente();
         try {
             att.addContainer(new Container(1, 0));
             stock.storeContainer(1, 1);
         } catch (ContainerException e) {
             
         } catch (EmplacementException e) {
             
         }
         
         
         StockageController controller = new StockageController(stock, att);
         Interface fenetre = new Interface(controller);
     }
 }
