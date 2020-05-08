 /*
  * $Id$
  * Copyright 2000,2005 wingS development team.
  *
  * This file is part of wingS (http://www.j-wings.org).
  *
  * wingS is free software; you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License
  * as published by the Free Software Foundation; either version 2.1
  * of the License, or (at your option) any later version.
  *
  * Please see COPYING for the complete licence.
  */
 package wingset;
 
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.wings.SConstants;
 import org.wings.SFrame;
 import org.wings.SIcon;
 import org.wings.SResourceIcon;
 import org.wings.SRootLayout;
 import org.wings.STabbedPane;
 import org.wings.SURLIcon;
 import org.wings.SButton;
 import org.wings.SBorderLayout;
 import org.wings.border.SEmptyBorder;
 import org.wings.header.Link;
 import org.wings.resource.DefaultURLResource;
 import org.wings.session.BrowserType;
 import org.wings.style.CSSProperty;
 
 import java.io.Serializable;
 import java.net.URL;
 import java.awt.event.ActionListener;
 import java.awt.event.ActionEvent;
 
 /**
  * The root of the WingSet demo application.
  *
  * @author <a href="mailto:haaf@mercatis.de">Armin Haaf</a>
  * @author <a href="mailto:B.Schmid@eXXcellent.de">Benjamin Schmid</a>
  * @version $Revision$
  */
 public class WingSet implements Serializable {
     /**
      * Jakarta commons logger.
      */
     private final static Log log = LogFactory.getLog(WingSet.class);
 
     // Some different icons used inside the demo applciation
     private final static SIcon JAVA_CUP_ICON = new SResourceIcon("org/wings/icons/JavaCup.gif");
     private final static SIcon SMALL_COW_ICON = new SURLIcon("../icons/cowSmall.gif");
     private final static SURLIcon STANDARD_TAB_BACKGROUND = new SURLIcon("../icons/ButtonsBackground.gif");
     private final static SURLIcon SELECTED_TAB_BACKGROUND = new SURLIcon("../icons/ButtonsBackgroundHighlighted.gif");
 
     /**
      * If true then use {@link StatisticsTimerTask} to log statistics on a regular basis to a logging file.
      * (Typically a file named wings-statisticsxxxlog placed in jakarta-tomcat/temp directory)
      */
     private static final boolean LOG_STATISTICS_TO_FILE = true;
 
     /**
      * Optional external custom CSS stylesheet to style your application according to your needs.
      */
     private Link customStyleSheetLink;
 
     static {
         if (LOG_STATISTICS_TO_FILE) {
             StatisticsTimerTask.startStatisticsLogging(60);
         }
     }
 
     /**
      * The root frame of the WingSet application.
      */
     private final SFrame frame;
 
     private final STabbedPane tab;
 
     private boolean customStyleApplied;
 
     /**
      * Constructor of the wingS application.
      * <p/>
      * <p>This class is referenced in the <code>web.xml</code> as root entry point for the wingS application.
      * For every new client an new {@link org.wings.session.Session} is created which constructs a new instance of this class.
      */
     public WingSet() {
         // Create root frame
         frame = new SFrame("WingSet Demo");
 
         // Create the tabbed pane containing all the wingset example tabs
         tab = new STabbedPane(SConstants.TOP);
         tab.setName("examples");
 
         // do some global styling of the wingSet application
         styleWingsetApp();
 
         // Assemble wingSet
         tab.add(new WingsImage(), "wingS!");
         tab.add(new LabelExample(), "Label");
         tab.add(new TextComponentExample(), "Text Component");
         tab.addTab("Tree", JAVA_CUP_ICON, new TreeExample(), "Tree Tool Tip");
         tab.add(new OptionPaneExample(), "OptionPane");
         tab.add(new TableExample(), "Table");
         tab.add(new ListExample(), "List");
         tab.add(new ButtonExample(), "Button");
         tab.add(new ToggleButtonExample(), "ToggleButton");
         tab.add(new CheckBoxExample(), "CheckBox");
         tab.add(new RadioButtonExample(), "RadioButton");
         tab.add(new Faces(), "Faces");
         tab.add(new FileChooserExample(), "FileChooser");
         tab.add(new ScrollPaneExample(), "ScrollPane");
         tab.add(new PageScrollerExample(), "PageScroller");
         tab.add(new MenuExample(), "Menu");
         tab.add(new TabbedPaneExample(), "Tabbed Pane");
         tab.addTab("Template Layout", SMALL_COW_ICON, new TemplateExample(), "Template Layout Manager");
         tab.add(new InteractiveTemplateExample(), "Interactive Template");
         tab.add(new ProgressBarExample(), "ProgressBar");
         tab.add(new MemUsageExample(), "Memory Usage");
         tab.add(new JavaScriptListenerExample(), "Script Listener");
         tab.add(new PopupExample(), "Popup Menu");
         tab.add(new KeyboardBindingsExample(), "Keyboard Bindings");
         tab.add(new DynamicLayoutExample(), "Dynamic Layouts");
         tab.add(new BackButtonExample(), "Browser Back");
         tab.add(new DesktopPaneExample(), "DesktopPane");
         tab.add(new DragAndDropExample(), "Drag and Drop");
         tab.add(new RawTextComponentExample(), "Raw Text Component");
         tab.add(new ErrorPageExample(), "Error Page");
         tab.add(new TableNestingExample(), "Limited table nesting (DEVEL)");
         tab.add(new ListBugTest(), "BUG TODO: In IE List does not appear");
 
         // Add component to content pane using a layout constraint (
         frame.getContentPane().add(tab);
 
         SButton switchStyleButton = new SButton("Toggle WingSet styling");
         switchStyleButton.setShowAsFormComponent(false);
         switchStyleButton.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 if (customStyleApplied)
                     unstyleWingsetApp();
                 else
                     styleWingsetApp();
             }
         });
         switchStyleButton.setBorder(new SEmptyBorder(5, 0, 5, 0));
         frame.getContentPane().add(switchStyleButton, SBorderLayout.SOUTH);
 
         frame.show();
     }
 
     /**
      * This method demonstrates some mehtods to influence the style of an wingS application
      */
     private void styleWingsetApp() {
         // 1) Apply custom HTML layout template as template for the root frame of the application
         try {
             URL templateURL = frame.getSession().getServletContext().getResource("/templates/ExampleFrame.thtml");
             if (templateURL != null) {
                 SRootLayout layout = new SRootLayout(templateURL);
                 frame.setLayout(layout);
             }
         } catch (java.io.IOException except) {
             log.warn("Exception", except);
         }
 
         // 2) Include an application specific CSS stylesheet to extend/overwrite the default wingS style set.
         if (frame.getSession().getUserAgent().getBrowserType() == BrowserType.IE) {
             // Yeah - some 'browsers' always require special attention
             customStyleSheetLink = new Link("stylesheet", null, "text/css", null, new DefaultURLResource("../css/wingset-ie.css"));
         } else {
             customStyleSheetLink = new Link("stylesheet", null, "text/css", null, new DefaultURLResource("../css/wingset-default.css"));
         }
         frame.addHeader(customStyleSheetLink);
 
         // 3) Programatically set/Overwrite CSS properties on specific components (here global frame).
         //    Remember: It's cleaner to do such global definitions in your external css file vs. in th java code.
        frame.setAttribute(CSSProperty.MARGIN, "8px !important");
 
         // 4) More advanced version of 3)
         //    Here we demonstrate how you can style single component by applying a CSS properties on a
         //    part of a component using component specific "css selectors" or "pseudo css selectors".
         //
         //    As we want to set styles on specific areas of the componentn and not the component itself, we use
         //    "CSS selectors" provided by the componentn to address these areas. They are dynamically resolved
         //     to the according CSS statements during the rednering process.
         //
         //  Set a background image on selected and unselected tabs (gradient grey image or orange image):
         tab.setAttribute(STabbedPane.SELECTOR_UNSELECTED_TAB, CSSProperty.BACKGROUND_IMAGE, STANDARD_TAB_BACKGROUND);
         tab.setAttribute(STabbedPane.SELECTOR_SELECTED_TAB, CSSProperty.BACKGROUND_IMAGE, SELECTED_TAB_BACKGROUND);
 
         customStyleApplied = true;
     }
 
     /**
      * Revert styling done in {@link #styleWingsetApp()}
      */
     private void unstyleWingsetApp() {
         frame.setLayout(new SRootLayout());
         frame.removeHeader(customStyleSheetLink);
         frame.setAttribute(CSSProperty.MARGIN, null);
         tab.setAttribute(STabbedPane.SELECTOR_UNSELECTED_TAB, CSSProperty.BACKGROUND_IMAGE, (SIcon) null);
         tab.setAttribute(STabbedPane.SELECTOR_SELECTED_TAB, CSSProperty.BACKGROUND_IMAGE, (SIcon) null);
 
         customStyleApplied = false;
     }
 
 
 }
 
 
