 package com.github.krassekoder.mm13client.gui;
 
import com.github.krassekoder.mm13client.gui.AboutDialog;
import com.github.krassekoder.mm13client.gui.LoginDialog;
 import com.trolltech.qt.gui.QAction;
import com.trolltech.qt.gui.QApplication;
 import com.trolltech.qt.gui.QMainWindow;
 import com.trolltech.qt.gui.QMenu;
 import com.trolltech.qt.gui.QMenuBar;
 import com.trolltech.qt.gui.QStatusBar;
 import com.trolltech.qt.gui.QTabWidget;
 
 public final class MainWindow extends QMainWindow {
 
     private QMenuBar menu;
     private QMenu toolsMenu, helpMenu;
     private QAction loginAction;
     private QAction aboutAction;
     private QStatusBar status;
     private QTabWidget tabs;
     private AboutDialog about;
     private LoginDialog login;
 
     public MainWindow() {
         super();
         setupUi();
     }
 
     private void setupUi() {
         setWindowTitle("MensaManager 2013");
 
         setupMenus();
         setStatusBar(status = new QStatusBar(this));
 
         setCentralWidget(tabs = new QTabWidget(this));
 
     }
 
     private void openAbout() {
         if (about == null) {
             about = new AboutDialog(this);
         }
         about.show();
     }
 
     private void openLoginDialog() {
         if (login == null) {
             login = new LoginDialog(this);
         }
         login.exec();
     }
 
     private void setupMenus() {
         setMenuBar(menu = new QMenuBar(this));
 
         menu.addMenu(toolsMenu = new QMenu(tr("&Tools"), menu));
         toolsMenu.addAction(loginAction = new QAction(tr("&Login..."), toolsMenu));
         loginAction.triggered.connect(this, "openLoginDialog()");
 
         menu.addMenu(helpMenu = new QMenu(tr("&Help"), menu));
         helpMenu.addAction(aboutAction = new QAction(tr("&About..."), helpMenu));
         aboutAction.triggered.connect(this, "openAbout()");
     }
 }
