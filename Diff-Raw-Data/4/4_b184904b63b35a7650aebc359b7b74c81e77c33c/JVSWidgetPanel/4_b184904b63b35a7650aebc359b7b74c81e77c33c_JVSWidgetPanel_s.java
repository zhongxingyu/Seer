 package org.javascool.gui;
 
 import org.javascool.Core;
 import org.javascool.core.ProgletEngine;
 import org.javascool.core.ProgletEngine.Proglet;
 import org.javascool.macros.Macros;
 import org.javascool.widgets.Console;
 import org.javascool.widgets.HtmlDisplay;
 
 /** Le panneau contenant les widgets
  * Les widgets de Java's cool sont disposés dans des onglets. Certain onglets de
  * navigation web peuvent être fermés par une croix.
  * @see org.javascool.widgets.Console
  * @see org.javascool.widgets.HtmlDisplay
  * @see org.javascool.widgets
  */
 class JVSWidgetPanel extends JVSTabs {
 
     private static final long serialVersionUID = 1L;
     private String progletTabId;
     /** Instance du JVSWidgetPanel */
     private static JVSWidgetPanel jwp;
 
     public static JVSWidgetPanel getInstance() {
         if (jwp == null) {
             jwp = new JVSWidgetPanel();
         }
         return jwp;
     }
 
     private JVSWidgetPanel() {
         super();
         this.add("Console", "", Console.getInstance());
     }
 
     /** Charge les tabs de la proglet
      * Charge le tab de la proglet (Panel) et l'HTMLDisplay avec le fichier d'aide.
      * @param name Le nom du package de la proglet
      */
     public void setProglet(String name) {
         this.removeAll();
         this.add("Console", "", org.javascool.widgets.Console.getInstance());
         Proglet proglet = ProgletEngine.getInstance().getProglet();
         if (proglet.getPane() != null) {
             this.progletTabId = this.add("Proglet " + name, "", proglet.getPane());
         }
         if (proglet.getHelp() != null) {
             this.add("Aide de la proglet", "", new HtmlDisplay().setPage(Macros.getResourceURL(proglet.getHelp())));
         }
         HtmlDisplay memo = new HtmlDisplay();
         memo.setPage(ClassLoader.getSystemResource(Core.help));
         this.add("Mémo", "", memo);
     }
 
     /** Affiche l'onglet de la Proglet si il existe */
     public void focusOnProgletPanel() {
         if (progletTabId != null) {
             this.switchToTab(progletTabId);
         }
     }
 
     /** Affiche la console */
     public void focusOnConsolePanel() {
         this.setSelectedIndex(this.indexOfTab("Console"));
     }
 
     /** Ouvre un nouvel onglet web
      * Ouvre un nouveau HTMLDisplay dans un onglet. Cet onglet peut être fermer à
      * l'aide de la croix qui se situe à droite du titre de l'onglet.
      * @param url L'url de la page à charger
      * @param tabName Le titre du tab à ouvrir
      * @see org.javascool.widgets.HtmlDisplay
      * @see String
      */
     public void openWebTab(String url, String tabName) {
         HtmlDisplay memo = new HtmlDisplay();
         memo.setPage(url);
         this.add(tabName, "", memo);
         this.setTabComponentAt(this.indexOfTab(tabName), new TabPanel(this));
         this.setSelectedComponent(memo);
     }
 }
