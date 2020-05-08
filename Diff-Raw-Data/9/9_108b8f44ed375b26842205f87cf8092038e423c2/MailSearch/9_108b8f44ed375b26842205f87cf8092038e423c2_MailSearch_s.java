 /*  
  * MailSearch.java  
  *   
  * Copyright (C) 2009 LWsystems GmbH & Co. KG  
  * This program is free software; you can redistribute it and/or 
  * modify it under the terms of the GNU General Public License as 
  * published by the Free Software Foundation; either version 2 of 
  * the License, or (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,  
  * but WITHOUT ANY WARRANTY; without even the implied warranty of  
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the  
  * GNU General Public License for more details.  
  *   
  * You should have received a copy of the GNU General Public License  
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.  
  */
 package de.lwsystems.mailarchive.web;
 
 import de.lwsystems.mailarchive.repository.MessageID;
 import de.lwsystems.mailarchive.web.extendedsearch.ExtendedSearchFrame;
 import de.lwsystems.mailarchive.web.mailsendhandler.MailSendFailureException;
 import de.lwsystems.mailarchive.web.mailsendhandler.MailSendHandler;
 import de.lwsystems.mailarchive.web.util.FieldExistsFilter;
 import java.awt.Color;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.ItemEvent;
 import java.awt.event.ItemListener;
 import java.io.BufferedReader;
 import java.io.ByteArrayInputStream;
 import java.io.IOException;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import org.apache.lucene.queryParser.ParseException;
 import org.wings.SFrame;
 import org.wings.SPanel;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileReader;
 import java.io.InputStream;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.Collection;
 import java.util.Date;
 import java.util.LinkedList;
 import java.util.Properties;
 import java.util.Vector;
 import javax.security.auth.login.LoginContext;
 import javax.servlet.http.HttpServletRequest;
 import javax.swing.DefaultComboBoxModel;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 import org.apache.lucene.analysis.standard.StandardAnalyzer;
 import org.apache.lucene.queryParser.QueryParser;
 import org.apache.lucene.search.Filter;
 import org.apache.lucene.search.QueryWrapperFilter;
 import org.apache.lucene.util.Version;
 import org.springframework.beans.factory.config.PropertyOverrideConfigurer;
 import org.springframework.beans.factory.xml.XmlBeanFactory;
 import org.springframework.core.io.FileSystemResource;
 import org.springframework.security.GrantedAuthority;
 import org.springframework.security.context.SecurityContextHolder;
 import org.springframework.web.context.support.ServletContextResource;
 import org.wings.Resource;
 import org.wings.SBorderLayout;
 import org.wings.SButton;
 import org.wings.SComboBox;
 import org.wings.SConstants;
 import org.wings.SDialog;
 import org.wings.SDimension;
 import org.wings.SGridLayout;
 import org.wings.SIcon;
 import org.wings.SLabel;
 import org.wings.SMenu;
 import org.wings.SMenuBar;
 import org.wings.SMenuItem;
 import org.wings.SOptionPane;
 import org.wings.STable;
 import org.wings.STextField;
 import org.wings.SURLIcon;
 import org.wings.StaticResource;
 import org.wings.border.SLineBorder;
 import org.wings.event.SRenderEvent;
 import org.wings.event.SRenderListener;
 import org.wings.header.FaviconHeader;
 import org.wings.header.Link;
 import org.wings.resource.DefaultURLResource;
 import org.wings.resource.ResourceNotFoundException;
 import org.wings.script.JavaScriptEvent;
 import org.wings.script.JavaScriptListener;
 import org.wings.session.SessionManager;
 import org.wings.style.CSSProperty;
 import org.wings.table.SDefaultTableColumnModel;
 import org.wings.table.STableColumn;
 import org.wings.table.STableColumnModel;
 import org.wingx.XScrollPane;
 import org.wingx.XTable;
 import org.wingx.table.XTableClickListener;
 
 /**
  * 
  * @author rene
  */
 public class MailSearch {
 
     Properties props;
     SearchController searchcontroller;
     private String[] columnnames = {"Von", "An", "", "Betreff", "Datum", "ID"};
     LoginContext lc;
     SFrame rootFrame;
     MailSendHandler mailHandler;
     Collection<Filter> spamFilters = null;
     Properties tableprops = new Properties();
 
     /** This is the entry point of you application as denoted in your web.xml */
     public MailSearch() {
 
 
         SessionManager.getSession().addRequestListener(searchcontroller);
         readConfig();
         // the root application frame
         rootFrame = new SFrame();
         rootFrame.setPreferredSize(SDimension.FULLAREA);
         //rootFrame.setAttribute(CSSProperty.WIDTH, "100%");
         rootFrame.addHeader(new Link("stylesheet",
                 null, "text/css", null,
                 new DefaultURLResource("../css/mailarchive.css")));
         rootFrame.addHeader(new FaviconHeader("../images/favicon.ico"));
         rootFrame.setTitle("Benno Mailarchiv");
         Resource r = new StaticResource("xml", "application/opensearchdescription+xml") {
 
             @Override
             protected InputStream getResourceStream() throws ResourceNotFoundException {
 
                 String realurl=rootFrame.getSession().getServletRequest().getRequestURL().toString();
                 //cut-off request for resource to get the parent
                 String [] splittedurl=realurl.split(("/"));
                 String resulturl="";
                 for (int i=0;i<splittedurl.length-1;i++) {
                     resulturl+=splittedurl[i]+"/";
                 }
                 String result = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?> \n"
                         + "<OpenSearchDescription xmlns=\"http://a9.com/-/spec/opensearch/1.1/\"> \n"
                         + "<ShortName>Benno</ShortName> \n"
                         + "<Description>Benno Mailarchiv</Description>\n"
                         + "<InputEncoding>UTF-8</InputEncoding>\n"
                         + "<Url type=\"text/html\" template=\""
                         + resulturl
                         +"?query={searchTerms}\"/> \n"
                         +"<Image height=\"16\" width=\"16\" type=\"image/x-icon\">"
                         + resulturl
                         +"../images/favicon.ico"
                         + "</Image>\n"
                         + "</OpenSearchDescription>\n";
                 return new ByteArrayInputStream(result.getBytes());
 
             }
         };
         Link l = new ExtendedLink("search", "Benno", "application/opensearchdescription+xml", null, r, "Benno MailArchiv");
 
         rootFrame.addHeader(l);
         SPanel panel = buildSearchPanel();
         panel.setPreferredSize(SDimension.FULLAREA);
         rootFrame.getContentPane().add(panel);
 
         // "show" applicationLog
         rootFrame.setVisible(true);
     }
 
     private void readConfig() {
         XmlBeanFactory factory = new XmlBeanFactory(new ServletContextResource(SessionManager.getSession().getServletContext(), "WEB-INF/applicationContext-index.xml"));
         PropertyOverrideConfigurer configurer = new PropertyOverrideConfigurer();
         if (new File("/etc/benno/archive.properties").exists()) {
             configurer.setLocation(new FileSystemResource("/etc/benno/archive.properties"));
             configurer.postProcessBeanFactory(factory);
         }
 
 
         //the Handler for sending mails (e.g.. to forward to the recipient)
         if (factory.containsBean("mailsendhandler")) {
             Logger.getLogger(MailSearch.class.getName()).log(Level.SEVERE, null, "Found a mailsendhandler bean in archive.properties");
             mailHandler = (MailSendHandler) factory.getBean("mailsendhandler");
             Logger.getLogger(MailSearch.class.getName()).log(Level.SEVERE, null, mailHandler.toString());
         } else {
             Logger.getLogger(MailSearch.class.getName()).log(Level.SEVERE, null, "No MailHandler in Factory. Edit /etc/benno/archive.properties, if you want to send mails");
             Logger.getLogger(MailSearch.class.getName()).log(Level.SEVERE, null, factory.toString());
             mailHandler = null;
         }
 
 
         try {
             tableprops.load(new FileInputStream("/etc/benno/searchtable.properties"));
         } catch (IOException ex) {
             Logger.getLogger(MailSearch.class.getName()).log(Level.SEVERE, null, ex);
         }
 
         File spamQueriesFile = new File("/etc/benno/exclude-queries");
         if (spamQueriesFile.exists()) {
 
             spamFilters = new LinkedList<Filter>();
             try {
                 BufferedReader in = new BufferedReader(new FileReader("/etc/benno/exclude-queries"));
 
                 String line;
                 QueryParser qp = new QueryParser(Version.LUCENE_24, "", new StandardAnalyzer(Version.LUCENE_24));
                 while ((line = in.readLine()) != null) {
                     if (line.startsWith("#")) {
                         continue;
                     }
                     try {
                         Filter q;
                         if (line.startsWith("not empty ")) {
 
                             //q=new PrefixQuery(new Term(line.substring(10)));
                             //q=new RangeQuery(new Term(line.substring(10),"*"),new Term(line.substring(10),"*"), true);
                             q = new FieldExistsFilter();
                             ((FieldExistsFilter) q).addField(line.substring(10));
                         } else {
                             q = new QueryWrapperFilter(qp.parse(line));
                         }
                         spamFilters.add(q);
                     } catch (ParseException ex) {
                         Logger.getLogger(MailSearch.class.getName()).log(Level.SEVERE, null, ex);
                     }
                 }
 
                 in.close();
             } catch (IOException ex) {
                 Logger.getLogger(MailSearch.class.getName()).log(Level.SEVERE, null, "I/O exception while reading /etc/benno/exclude-queries:" + ex);
             }
 
 
         }
     }
 
     class DateRange {
 
         final String desc; //
         final Date from;
         final Date to;
 
         public DateRange(String desc, Date from, Date to) {
             this.desc = desc;
             this.from = from;
             this.to = to;
         }
 
         @Override
         public String toString() {
             return desc;
         }
     }
 
     private DateRange[] buildDateModel() {
         //DateRange[] ranges = null;
         Vector<DateRange> ranges = new Vector<DateRange>();
 
         try {
            final long millisPerDay = 24 * 60 * 60;
             DateFormat yearDateFormat = new SimpleDateFormat("yyyy");
             ranges.add(new DateRange("alle", null, null));
             ranges.add(new DateRange("letzte 30 Tage", new Date(System.currentTimeMillis() - 30 * millisPerDay), null));
             ranges.add(new DateRange("letzte 90 Tage", new Date(System.currentTimeMillis() - 90 * millisPerDay), null));
             ranges.add(new DateRange("letztes Jahr", new Date(System.currentTimeMillis() - 365 * millisPerDay), null));
             Calendar cal = Calendar.getInstance();
             cal.setTime(new Date());
             for (String y : searchcontroller.getYears()) {
                 Date thisYear = yearDateFormat.parse(y);
                 Integer nexty = (new Integer(y)) + 1;
                 if (cal.getTime().after(thisYear)) {
                     ranges.add(new DateRange(y, thisYear, yearDateFormat.parse(nexty.toString())));
                 }
             }
 
         } catch (java.text.ParseException ex) {
             Logger.getLogger(MailSearch.class.getName()).log(Level.SEVERE, null, ex);
         }
 
 
 
 
 
         return ranges.toArray(new DateRange[0]);
 
 
 
 
     }
 
     private SPanel buildSearchPanel() {
         SGridLayout titlePanelLayout = new SGridLayout(4, 1);
         SPanel titlePanel = new SPanel(titlePanelLayout);
         titlePanelLayout.setVgap(0);
 
         SGridLayout panellay = new SGridLayout(2, 1);
 
 
         panellay.setPreferredSize(SDimension.FULLAREA);
         SPanel panel = new SPanel(panellay);
         //panel.setAttribute(CSSProperty.WIDTH, "1000p");
         //TITLE
         SURLIcon brandlogo = new SURLIcon("../images/benno-logo-50px.png");
 
         brandlogo.setIconHeight(60);
 
         SLabel title = new SLabel(brandlogo);
 
 
         title.setStyle("title");
         //title.setFont(new SFont("sans-serif", SFont.BOLD, 20));
         SPanel headerPanel = new SPanel(new SBorderLayout());
         headerPanel.setStyle("titlebar");
         headerPanel.setPreferredSize(new SDimension(0, 66));
         //SURLIcon logo = new SURLIcon("../images/logo_lws.png");
         //logo.setIconHeight(50);
         //SLabel logoLabel = new SLabel(logo);
         //logoLabel.setVerticalAlignment(SConstants.RIGHT_ALIGN);
 
 
 
         //headerPanel.add(logoLabel, SBorderLayout.WEST);
         headerPanel.setAttribute(CSSProperty.WIDTH, "100%");
         titlePanel.add(headerPanel);
 
 
         title.setAttribute(CSSProperty.POSITION, "absolute");
         title.setAttribute(CSSProperty.TOP, "0");
         title.setAttribute(CSSProperty.LEFT, "0");
         titlePanel.add(title);
 
         //SEARCH
         final STextField textField = new STextField();
         textField.setColumns(40);
         final SButton doSearch = new SButton("Suche");
 
         textField.addRenderListener(new SRenderListener() {
 
             public void startRendering(SRenderEvent sre) {
                 HttpServletRequest sr = sre.getSourceComponent().getSession().getServletRequest();
                 String query = sr.getParameter("query");
                 if (query != null) {
                     textField.setText(query);
                     textField.fireFinalEvents();
                     searchcontroller.executeQuery();
                 }
             }
 
             public void doneRendering(SRenderEvent sre) {
             }
         });
 
 
 
 
 
         SIcon searchIcon = new SURLIcon("../images/tango/system-search.png");
         searchIcon.setIconHeight(18);
         doSearch.setIcon(searchIcon);
 
         SIcon extendedSearchIcon = new SURLIcon("../images/tango/document-properties.png");
         extendedSearchIcon.setIconHeight(18);
         final SButton doExtendedSearch = new SButton("Erweiterte Suche");
         doExtendedSearch.setIcon(extendedSearchIcon);
         final SLabel searchresults = new SLabel("");
         final XTable results = new XTable();
 
         results.setAutoCreateColumnsFromModel(false);
         results.setPreferredSize(SDimension.FULLAREA);
         STableColumnModel colmodel = new SDefaultTableColumnModel();
         colmodel.addColumn(new STableColumn(0, tableprops.getProperty("column0", "220")));
         colmodel.addColumn(new STableColumn(1, tableprops.getProperty("column1", "220")));
         colmodel.addColumn(new STableColumn(2, tableprops.getProperty("column2", "22")));
         colmodel.addColumn(new STableColumn(3, tableprops.getProperty("column3", "*")));
         colmodel.addColumn(new STableColumn(4, tableprops.getProperty("column4", "130")));
         results.setColumnModel(colmodel);
 
         results.setAttribute(CSSProperty.WHITE_SPACE, "normal");
         results.setAttribute(CSSProperty.WIDTH, "100%");
         results.setAttribute("table-layout", "fixed");
         results.setAttribute(CSSProperty.OVERFLOW, "hidden");
         searchcontroller = new SearchController(results, searchresults, spamFilters);
 
         STableColumnModel cm = results.getColumnModel();
 
 
 
 
         for (int i = 0; i
                 < cm.getColumnCount(); i++) {
             STableColumn col = cm.getColumn(i);
             col.setHeaderValue(columnnames[i]);
 
 
 
 
         }
 
         results.setSelectionMode(STable.MULTIPLE_SELECTION);
 
 
         results.setDefaultRenderer(new SearchResultTableCellRenderer());
         results.addClickListener(3, new XTableClickListener() {
 
             public void clickOccured(int row, int col) {
                 SDialog mailview = new SDialog(results.getParentFrame(), "Mail anzeigen", true);
                 mailview.setX(10);
                 mailview.setY(10);
                 mailview.setModal(true);
                 mailview.add(new MailViewFrame(searchcontroller.getTableModel().getRepository(), (String) searchcontroller.getTableModel().getValueAt(row, 5), searchcontroller, mailHandler));
                 mailview.setVisible(true);
 
 
 
 
             }
         });
 
 
         textField.addActionListener(new ActionListener() {
 
             public void actionPerformed(ActionEvent e) {
                 doSearch.doClick();
 
 
 
 
             }
         });
 
         SGridLayout searchFieldLayout = new SGridLayout(1, 4);
 
 
 
 
 
 
         searchFieldLayout.setHgap(10);
         searchFieldLayout.setVgap(10);
         SPanel searchField = new SPanel(searchFieldLayout);
         searchField.setHorizontalAlignment(SConstants.LEFT);
         searchField.add(textField);
 
         // search options
         SPanel searchOptionsPanel = new SPanel(new SGridLayout(1, 3));
         searchOptionsPanel.setHorizontalAlignment(SConstants.LEFT);
         SComboBox dateRangeChooser = new SComboBox();
         dateRangeChooser.setModel(new DefaultComboBoxModel(buildDateModel()));
         dateRangeChooser.addItemListener(new ItemListener() {
 
             public void itemStateChanged(ItemEvent itemevent) {
                 DateRange dr = (DateRange) itemevent.getItem();
                 searchcontroller.setDateRange(dr.from, dr.to);
                 doSearch.doClick();
 
 
 
 
             }
         });
 
         searchOptionsPanel.add(dateRangeChooser);
 
         searchField.add(searchOptionsPanel);
         searchField.add(doSearch);
         searchField.add(doExtendedSearch);
         titlePanel.add(searchField);
 
 
 
 
 
         doSearch.addActionListener(searchcontroller);
         doSearch.doClick();
         //final SDialog extendedSearch = new SDialog(rootFrame);
         final SPanel extendedSearch = new SPanel();
         titlePanel.add(extendedSearch);
         //extendedSearch.setClosable(true);
         //extendedSearch.setModal(false);
         //extendedSearch.setX(540);
         //extendedSearch.setY(116);
 
         //needed to prevent error for empty dialog
         extendedSearch.setVisible(false);
 
         doExtendedSearch.addActionListener(new ActionListener() {
 
             public void actionPerformed(ActionEvent arg0) {
                 if (extendedSearch.isVisible()) {
                     extendedSearch.removeAll();
                     extendedSearch.setVisible(false);
 
 
 
 
                 } else {
                     extendedSearch.add(new ExtendedSearchFrame(searchcontroller, textField));
                     extendedSearch.setVisible(true);
 
 
 
 
                 }
             }
         });
 
         textField.addActionListener(
                 new ActionListener() {
 
                     public void actionPerformed(ActionEvent ev) {
                         searchcontroller.setQuery(((STextField) ev.getSource()).getText());
 
 
 
 
                     }
                 });
         titlePanel.add(searchresults);
         XScrollPane resultspane = new XScrollPane(results);
         resultspane.setVerticalExtent(25);
         Integer[] extents = {25, 30, 40, 50, 100, 1000};
         //Integer[] extents = {};
         resultspane.setExtents(extents);
 
 
 
 
 
         if (mailHandler != null && mailHandler.isReady()) {
 
 
             final SMenuBar menuBar = new SMenuBar();
             menuBar.setAttribute(CSSProperty.WIDTH, "100px");
             menuBar.setAttribute(CSSProperty.FLOAT, "left");
 
             menuBar.setBorder(new SLineBorder(Color.LIGHT_GRAY, 2));
 
 
 
             final SMenu mailSendMenu = new SMenu("Aktion");
             mailSendMenu.setEnabled(false);
             results.addSelectionListener(new ListSelectionListener() {
 
                 public void valueChanged(ListSelectionEvent arg0) {
                     if (results.getSelectedRowCount() == 0) {
                         mailSendMenu.setEnabled(false);
 
 
 
 
                     } else {
                         mailSendMenu.setEnabled(true);
 
 
 
 
 
                     }
                 }
             });
             menuBar.setBackground(new Color(210, 210, 210));
 
 
 
             SMenu ownAddressMenu = new SMenu("Sende an eigene Adresse");
 
 
 
 
             for (GrantedAuthority ga : SecurityContextHolder.getContext().getAuthentication().getAuthorities()) {
                 if (ga.getAuthority().startsWith("ROLE_MAIL_")) {
                     final String mailAddress = ga.getAuthority().substring(10);
                     final SMenuItem menuItem = new SMenuItem(mailAddress);
                     menuItem.addActionListener(new ActionListener() {
 
                         public void actionPerformed(ActionEvent arg0) {
                             String[] toAddresses = {mailAddress};
                             try {
                                 for (int mailnum : results.getSelectedRows()) {
                                     InputStream is = searchcontroller.getTableModel().getRepository().getDocument(new MessageID((String) results.getModel().getValueAt(mailnum, 5)));
                                     mailHandler.sendMail(null, toAddresses, new String[0], new String[0], is);
                                 }
                             } catch (MailSendFailureException ex) {
                                 SOptionPane.showMessageDialog(menuItem, "Fehler: " + ex.getLocalizedMessage(), "Information", SOptionPane.ERROR_MESSAGE);
                                 Logger.getLogger(MailSearch.class.getName()).log(Level.SEVERE, null, ex);
                             }
                         }
                     });
                     ownAddressMenu.add(menuItem);
                 }
             }
 
 
             mailSendMenu.add(ownAddressMenu);
 
 
 
 
             if (searchcontroller.isAdmin()) {
                 SMenuItem sendToItem = new SMenuItem("Sende an ...");
                 sendToItem.addActionListener(new ActionListener() {
 
                     public void actionPerformed(ActionEvent arg0) {
                         final SDialog dialog = new SDialog();
                         dialog.add(new SLabel("Zieladresse:"));
                         final STextField toAddress = new STextField();
                         dialog.add(toAddress);
                         SPanel buttonPanel = new SPanel(new SGridLayout(1, 2, 5, 5));
                         SButton okButton = new SButton("OK");
                         buttonPanel.add(okButton);
                         okButton.addActionListener(new ActionListener() {
 
                             public void actionPerformed(ActionEvent e) {
                                 if (toAddress != null && !toAddress.getText().equals("")) {
                                     String[] toAddresses = {toAddress.getText()};
 
 
 
 
                                     try {
                                         for (int mailnum : results.getSelectedRows()) {
                                             InputStream is = searchcontroller.getTableModel().getRepository().getDocument(new MessageID((String) results.getModel().getValueAt(mailnum, 5)));
                                             mailHandler.sendMail(null, toAddresses, new String[0], new String[0], is);
 
 
 
 
                                         }
                                     } catch (MailSendFailureException ex) {
                                         SOptionPane.showMessageDialog(dialog, "Fehler: " + ex.getLocalizedMessage(), "Information", SOptionPane.ERROR_MESSAGE);
                                         Logger.getLogger(MailSearch.class.getName()).log(Level.SEVERE, null, ex);
                                     }
                                     dialog.setVisible(false);
 
 
 
 
                                 }
 
                             }
                         });
 
                         SButton cancelButton = new SButton("Abbrechen");
 
                         buttonPanel.add(cancelButton);
 
                         cancelButton.addActionListener(new ActionListener() {
 
                             public void actionPerformed(ActionEvent e) {
                                 dialog.setVisible(false);
 
 
 
 
                             }
                         });
 
                         dialog.add(buttonPanel);
 
                         dialog.setVisible(true);
 
 
 
 
                     }
                 });
                 mailSendMenu.add(sendToItem);
 
 
 
 
             }
             final SMenuItem originalItem = new SMenuItem("Sende an Originalempfänger");
             originalItem.addActionListener(new ActionListener() {
 
                 public void actionPerformed(ActionEvent arg0) {
 
                     try {
                         for (int mailnum : results.getSelectedRows()) {
                             InputStream is = searchcontroller.getTableModel().getRepository().getDocument(new MessageID((String) results.getModel().getValueAt(mailnum, 5)));
                             mailHandler.sendMail(null, null, null, null, is);
 
 
 
 
                         }
                     } catch (MailSendFailureException ex) {
                         SOptionPane.showMessageDialog(searchresults, "Fehler: " + ex.getLocalizedMessage(), "Information", SOptionPane.ERROR_MESSAGE);
                         Logger.getLogger(MailSearch.class.getName()).log(Level.SEVERE, null, ex);
 
 
 
 
 
 
                         return;
                     }
                     SOptionPane.showMessageDialog(searchresults, "Erfolgreich versandte Emails: " + results.getSelectedRowCount(), "Information", SOptionPane.PLAIN_MESSAGE);
 
 
 
 
                 }
             });
             mailSendMenu.add(originalItem);
 
 
             menuBar.add(mailSendMenu);
 
             panel.add(menuBar);
 
 
 
 
         }
         panel.add(resultspane);
 //        
 //        SButton sendMail=new SButton("Mails weiterleiten");
 //        panel.setHorizontalAlignment(SConstants.LEFT_ALIGN);
 //        panel.add(sendMail);
         //panel.add(results);
         titlePanel.add(panel);
         SPanel menu = createMenu(rootFrame);
         menu.setHorizontalAlignment(SConstants.RIGHT_ALIGN);
         menu.setVerticalAlignment(SConstants.TOP_ALIGN);
         menu.setAttribute(CSSProperty.MARGIN, "5");
         headerPanel.add(menu, SBorderLayout.NORTH);
 
 
 
 
         return titlePanel;
 
 
 
 
     }
 
     private SPanel createMenu(final SFrame frame) {
         SGridLayout MenuLayout = new SGridLayout(1, 3);
         MenuLayout.setHgap(10);
         SPanel Menu = new SPanel(MenuLayout);
 
         SButton logoutbutton = new SButton("Logout");
         SIcon logoutIcon = new SURLIcon("../images/tango/process-stop.png");
         logoutIcon.setIconHeight(18);
         logoutbutton.setIcon(logoutIcon);
 
         logoutbutton.addScriptListener(new JavaScriptListener(JavaScriptEvent.ON_CLICK, "window.location.href='../j_spring_security_logout'"));
         String emails = "";
 //        for (GrantedAuthority ga : SecurityContextHolder.getContext().getAuthentication().getAuthorities()) {
 //            if (ga.getAuthority().startsWith("ROLE_MAIL_")) {
 //                emails = emails + " " + ga.getAuthority().substring(10);
 //            }
 //
 //        }
 //        username.setText(emails);   //for debugging
 //        Menu.add(username);
 
 
 
 
         if (searchcontroller.isAdmin()) {
             SIcon adminIcon = new SURLIcon("../images/tango/system-users.png");
             adminIcon.setIconHeight(18);
             SButton admin = new SButton("Benutzerverwaltung");
             admin.setIcon(adminIcon);
             admin.addActionListener(new ActionListener() {
 
                 public void actionPerformed(ActionEvent e) {
                     SDialog adminview = new SDialog(frame, "Benutzerverwaltung", true);
                     adminview.setX(10);
                     adminview.setY(10);
                     adminview.setModal(true);
                     adminview.add(new AdminFrame(searchcontroller));
                     adminview.setVisible(true);
 
 
 
 
                 }
             });
             Menu.add(admin);
 
 
 
 
         }
 
         Menu.add(logoutbutton);
         Menu.setHorizontalAlignment(SConstants.LEFT);
 
 
 
 
 
         return Menu;
 
 
     }
 }
