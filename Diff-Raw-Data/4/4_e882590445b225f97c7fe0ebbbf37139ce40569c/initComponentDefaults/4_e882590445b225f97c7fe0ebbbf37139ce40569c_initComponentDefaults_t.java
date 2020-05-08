 // Tags: JDK1.3
 // Uses: MyBasicLookAndFeel
 
 // Copyright (C) 2005 David Gilbert <david.gilbert@object-refinery.com>
 
 // This file is part of Mauve.
 
 // Mauve is free software; you can redistribute it and/or modify
 // it under the terms of the GNU General Public License as published by
 // the Free Software Foundation; either version 2, or (at your option)
 // any later version.
 
 // Mauve is distributed in the hope that it will be useful,
 // but WITHOUT ANY WARRANTY; without even the implied warranty of
 // MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 // GNU General Public License for more details.
 
 // You should have received a copy of the GNU General Public License
 // along with Mauve; see the file COPYING.  If not, write to
 // the Free Software Foundation, 59 Temple Place - Suite 330,
 // Boston, MA 02111-1307, USA.  */
 
 package gnu.testlet.javax.swing.plaf.basic.BasicLookAndFeel;
 
 import gnu.testlet.TestHarness;
 import gnu.testlet.Testlet;
 
 import java.awt.Dimension;
 import java.awt.Font;
 import java.util.Arrays;
 import java.util.List;
 
 import javax.swing.Icon;
 import javax.swing.InputMap;
 import javax.swing.KeyStroke;
 import javax.swing.ListCellRenderer;
 import javax.swing.UIDefaults;
 import javax.swing.border.Border;
 import javax.swing.plaf.BorderUIResource;
 import javax.swing.plaf.ColorUIResource;
 import javax.swing.plaf.DimensionUIResource;
 import javax.swing.plaf.FontUIResource;
 import javax.swing.plaf.InputMapUIResource;
 import javax.swing.plaf.InsetsUIResource;
 import javax.swing.plaf.BorderUIResource.CompoundBorderUIResource;
 import javax.swing.plaf.basic.BasicBorders;
 import javax.swing.plaf.basic.BasicLookAndFeel;
 import javax.swing.plaf.basic.BasicBorders.ButtonBorder;
 import javax.swing.plaf.basic.BasicBorders.MarginBorder;
 
 /**
  * Some checks for the initComponentDefaults() method in the 
  * {@link BasicLookAndFeel} class.  
  */
 public class initComponentDefaults implements Testlet
 {
 
   /**
    * Runs the test using the specified harness.  
    * 
    * @param harness  the test harness (<code>null</code> not allowed).
    */
   public void test(TestHarness harness) 
   {
     // TODO: there are a lot of 'instanceof' checks in here.  Those are weak
     // tests, try to strengthen them.
 
     MyBasicLookAndFeel laf = new MyBasicLookAndFeel();
     UIDefaults defaults = new UIDefaults();
     laf.initComponentDefaults(defaults);
         
     harness.checkPoint("AuditoryCues");
     harness.check(defaults.get("AuditoryCues.allAuditoryCues") != null);
     harness.check(defaults.get("AuditoryCues.cueList") != null);
     harness.check(defaults.get("AuditoryCues.noAuditoryCues") != null);
     
     harness.checkPoint("Button");
     CompoundBorderUIResource b1 = (CompoundBorderUIResource) defaults.get("Button.border");
     harness.check(b1.getInsideBorder() instanceof MarginBorder);
     harness.check(b1.getOutsideBorder() instanceof ButtonBorder);
     harness.check(defaults.get("Button.focusInputMap") instanceof InputMapUIResource);
     harness.check(defaults.get("Button.font"), new FontUIResource("Dialog", Font.PLAIN, 12));
     harness.check(defaults.get("Button.margin"), new InsetsUIResource(2, 14, 2, 14));
     harness.check(defaults.get("Button.textIconGap"), new Integer(4));
     harness.check(defaults.get("Button.textShiftOffset"), new Integer(0));
     
     harness.checkPoint("CheckBox");
     CompoundBorderUIResource b2 = (CompoundBorderUIResource) defaults.get("CheckBox.border");
     harness.check(b2.getInsideBorder() instanceof MarginBorder);
     harness.check(b2.getOutsideBorder() instanceof ButtonBorder);
     harness.check(defaults.get("CheckBox.focusInputMap") instanceof InputMapUIResource);
     harness.check(defaults.get("CheckBox.font"), new FontUIResource("Dialog", Font.PLAIN, 12));
     harness.check(defaults.get("CheckBox.icon") instanceof Icon);
     harness.check(defaults.get("CheckBox.margin"), new InsetsUIResource(2, 2, 2, 2));
     harness.check(defaults.get("CheckBox.textIconGap"), new Integer(4));
     harness.check(defaults.get("CheckBox.textShiftOffset"), new Integer(0));
     
     harness.checkPoint("CheckBoxMenuItem");
     harness.check(defaults.get("CheckBoxMenuItem.acceleratorFont"), new FontUIResource("Dialog", Font.PLAIN, 12));
     harness.check(defaults.get("CheckBoxMenuItem.arrowIcon") instanceof Icon);
     harness.check(defaults.get("CheckBoxMenuItem.border") instanceof BasicBorders.MarginBorder);
     harness.check(defaults.get("CheckBoxMenuItem.borderPainted"), Boolean.FALSE);
     harness.check(defaults.get("CheckBoxMenuItem.checkIcon") instanceof Icon);
     harness.check(defaults.get("CheckBoxMenuItem.font"), new FontUIResource("Dialog", Font.PLAIN, 12));
     harness.check(defaults.get("CheckBoxMenuItem.margin"), new InsetsUIResource(2, 2, 2, 2));
     
     harness.checkPoint("ColorChooser");
     harness.check(defaults.get("ColorChooser.font"), new FontUIResource("Dialog", Font.PLAIN, 12));
     harness.check(defaults.get("ColorChooser.rgbBlueMnemonic"), new Integer(66));
     harness.check(defaults.get("ColorChooser.rgbGreenMnemonic"), new Integer(78));
     harness.check(defaults.get("ColorChooser.rgbRedMnemonic"), new Integer(68));
     harness.check(defaults.get("ColorChooser.swatchesRecentSwatchSize"), new Dimension(10, 10));
     harness.check(defaults.get("ColorChooser.swatchesSwatchSize"), new Dimension(10, 10));
     
     harness.checkPoint("ComboBox");
     harness.check(defaults.get("ComboBox.ancestorInputMap") instanceof InputMapUIResource);
     harness.check(defaults.get("ComboBox.font"), new FontUIResource("SansSerif", Font.PLAIN, 12));
     
     harness.checkPoint("Desktop");
     harness.check(defaults.get("Desktop.ancestorInputMap") instanceof InputMapUIResource);
     
     harness.checkPoint("DesktopIcon");
     harness.check(defaults.get("DesktopIcon.border") instanceof BorderUIResource.CompoundBorderUIResource);
         
     harness.checkPoint("EditorPane");
     harness.check(defaults.get("EditorPane.background"), new ColorUIResource(255, 255, 255));
     harness.check(defaults.get("EditorPane.border") instanceof BasicBorders.MarginBorder);
     harness.check(defaults.get("EditorPane.caretBlinkRate"), new Integer(500));
     harness.check(defaults.get("EditorPane.font"), new FontUIResource("Serif", Font.PLAIN, 12));
     harness.check(defaults.get("EditorPane.margin"), new InsetsUIResource(3, 3, 3, 3));
     
     harness.checkPoint("FileChooser");
     harness.check(defaults.get("FileChooser.ancestorInputMap") instanceof InputMapUIResource);
     harness.check(defaults.get("FileChooser.cancelButtonMnemonic"), new Integer(67));
     // FIXME: the following check is commented out - the JDK returns null because 
     // MyBasicLookAndFeel doesn't look in the right place for the gif
     //harness.check(defaults.get("FileChooser.detailsViewIcon"), null);
     harness.check(defaults.get("FileChooser.directoryOpenButtonMnemonic"), new Integer(79));
     harness.check(defaults.get("FileChooser.helpButtonMnemonic"), new Integer(72));
     // FIXME: the following 3 checks are commented out - the JDK returns null because 
     // MyBasicLookAndFeel doesn't look in the right place for the gif
     //harness.check(defaults.get("FileChooser.detailsViewIcon"), null);
     //harness.check(defaults.get("FileChooser.homeFolderIcon"), null);
     //harness.check(defaults.get("FileChooser.listViewIcon"), null);
     //harness.check(defaults.get("FileChooser.newFolderIcon"), null);
     harness.check(defaults.get("FileChooser.openButtonMnemonic"), new Integer(79));
     harness.check(defaults.get("FileChooser.saveButtonMnemonic"), new Integer(83));
     harness.check(defaults.get("FileChooser.updateButtonMnemonic"), new Integer(85));
     // FIXME: the following check is commented out - the JDK returns null because 
     // MyBasicLookAndFeel doesn't look in the right place for the gif
     //harness.check(defaults.get("FileChooser.upFolderIcon"), null);
 
     harness.checkPoint("FileView");
     // FIXME: the following 5 checks are commented out - the JDK returns null because 
     // MyBasicLookAndFeel doesn't look in the right place for the gif
     //harness.check(defaults.get("FileView.computerIcon"), null);
     //harness.check(defaults.get("FileView.directoryIcon"), null);
     //harness.check(defaults.get("FileView.fileIcon"), null);
     //harness.check(defaults.get("FileView.floppyDriveIcon"), null);
     //harness.check(defaults.get("FileView.hardDriveIcon"), null);
 
     harness.checkPoint("FormattedTextField");
     harness.check(defaults.get("FormattedTextField.border") instanceof BasicBorders.FieldBorder);
     harness.check(defaults.get("FormattedTextField.caretBlinkRate"), new Integer(500));
     harness.check(defaults.get("FormattedTextField.focusInputMap") instanceof InputMapUIResource);
     harness.check(defaults.get("FormattedTextField.font"), new FontUIResource("SansSerif", Font.PLAIN, 12));
     harness.check(defaults.get("FormattedTextField.margin"), new InsetsUIResource(0, 0, 0, 0));
     
     harness.checkPoint("InternalFrame");
     harness.check(defaults.get("InternalFrame.border") instanceof BorderUIResource.CompoundBorderUIResource);
     harness.check(defaults.get("InternalFrame.closeIcon") instanceof Icon);
     // FIXME: the following check is commented out - the JDK returns null because 
     // MyBasicLookAndFeel doesn't look in the right place for the gif
     //harness.check(defaults.get("InternalFrame.icon"), null);
     harness.check(defaults.get("InternalFrame.iconifyIcon") instanceof Icon);
     harness.check(defaults.get("InternalFrame.maximizeIcon") instanceof Icon);
     harness.check(defaults.get("InternalFrame.minimizeIcon") instanceof Icon);
     harness.check(defaults.get("InternalFrame.titleFont"), new FontUIResource("Dialog", Font.BOLD, 12));
     harness.check(defaults.get("InternalFrame.windowBindings") instanceof Object[]);
 
     harness.checkPoint("Label");
     harness.check(defaults.get("Label.disabledForeground"), new ColorUIResource(255, 255, 255));
     harness.check(defaults.get("Label.font"), new FontUIResource("Dialog", Font.PLAIN, 12));
  
     harness.checkPoint("List");
     harness.check(defaults.get("List.cellRenderer") instanceof ListCellRenderer);
     harness.check(defaults.get("List.focusCellHighlightBorder") instanceof BorderUIResource.LineBorderUIResource);
     harness.check(defaults.get("List.focusInputMap") instanceof InputMapUIResource);
     harness.check(defaults.get("List.focusInputMap.RightToLeft") instanceof InputMapUIResource);
     harness.check(defaults.get("List.font"), new FontUIResource("Dialog", Font.PLAIN, 12));
     
     harness.checkPoint("Menu");
     harness.check(defaults.get("Menu.acceleratorFont"), new FontUIResource("Dialog", Font.PLAIN, 12));
     harness.check(defaults.get("Menu.arrowIcon") instanceof Icon);
     harness.check(defaults.get("Menu.border") instanceof BasicBorders.MarginBorder);
     harness.check(defaults.get("Menu.borderPainted"), Boolean.FALSE);
     harness.check(defaults.get("Menu.checkIcon") instanceof Icon);
     harness.check(defaults.get("Menu.crossMenuMnemonic"), Boolean.TRUE);
     harness.check(defaults.get("Menu.font"), new FontUIResource("Dialog", Font.PLAIN, 12));
     harness.check(defaults.get("Menu.margin"), new InsetsUIResource(2, 2, 2, 2));
     harness.check(defaults.get("Menu.menuPopupOffsetX"), new Integer(0));
     harness.check(defaults.get("Menu.menuPopupOffsetY"), new Integer(0));
     int[] shortcuts = (int[]) defaults.get("Menu.shortcutKeys");
     if (shortcuts == null)
       shortcuts = new int[] { 999 };  // to prevent NullPointerException
     harness.check(shortcuts.length, 1);
     harness.check(shortcuts[0], 8);
     harness.check(defaults.get("Menu.submenuPopupOffsetX"), new Integer(0));
     harness.check(defaults.get("Menu.submenuPopupOffsetY"), new Integer(0));
     
     harness.checkPoint("MenuBar");
     harness.check(defaults.get("MenuBar.border") instanceof BasicBorders.MenuBarBorder);
     harness.check(defaults.get("MenuBar.font"), new FontUIResource("Dialog", Font.PLAIN, 12));
     harness.check(defaults.get("MenuBar.windowBindings") instanceof Object[]);
     
     harness.checkPoint("MenuItem");
     harness.check(defaults.get("MenuItem.acceleratorDelimiter"), "+");
     harness.check(defaults.get("MenuItem.acceleratorFont"), new FontUIResource("Dialog", Font.PLAIN, 12));
     harness.check(defaults.get("MenuItem.arrowIcon") instanceof Icon);
     harness.check(defaults.get("MenuItem.border") instanceof BasicBorders.MarginBorder);
     harness.check(defaults.get("MenuItem.borderPainted"), Boolean.FALSE);
     harness.check(defaults.get("MenuItem.checkIcon") instanceof Icon);
     harness.check(defaults.get("MenuItem.font"), new FontUIResource("Dialog", Font.PLAIN, 12));
     harness.check(defaults.get("MenuItem.margin"), new InsetsUIResource(2, 2, 2, 2));
     
     harness.checkPoint("OptionPane");
     harness.check(defaults.get("OptionPane.border") instanceof BorderUIResource.EmptyBorderUIResource);
     harness.check(defaults.get("OptionPane.buttonAreaBorder") instanceof BorderUIResource.EmptyBorderUIResource);
     harness.check(defaults.get("OptionPane.buttonClickThreshhold"), new Integer(500));
     // FIXME: the following check is commented out - the JDK returns null because 
     // MyBasicLookAndFeel doesn't look in the right place for the gif
     //harness.check(defaults.get("OptionPane.errorIcon"), null);
     harness.check(defaults.get("OptionPane.font"), new FontUIResource("Dialog", Font.PLAIN, 12));
     // FIXME: the following check is commented out - the JDK returns null because 
     // MyBasicLookAndFeel doesn't look in the right place for the gif
     //harness.check(defaults.get("OptionPane.informationIcon"), null);
     harness.check(defaults.get("OptionPane.messageAreaBorder") instanceof BorderUIResource.EmptyBorderUIResource);
     harness.check(defaults.get("OptionPane.minimumSize"), new DimensionUIResource(262, 90));
     // FIXME: the following 2 checks are commented out - the JDK returns null because 
     // MyBasicLookAndFeel doesn't look in the right place for the gif
     //harness.check(defaults.get("OptionPane.questionIcon"), null);
     //harness.check(defaults.get("OptionPane.warningIcon"), null);
     harness.check(defaults.get("OptionPane.windowBindings") instanceof Object[]);
 
     harness.checkPoint("Panel");
     harness.check(defaults.get("Panel.font"), new FontUIResource("Dialog", Font.PLAIN, 12));
 
     harness.checkPoint("PasswordField");
     harness.check(defaults.get("PasswordField.border") instanceof BasicBorders.FieldBorder);
     harness.check(defaults.get("PasswordField.caretBlinkRate"), new Integer(500));
     harness.check(defaults.get("PasswordField.font"), new FontUIResource("MonoSpaced", Font.PLAIN, 12));
     harness.check(defaults.get("PasswordField.margin"), new InsetsUIResource(0, 0, 0, 0));
     
     harness.checkPoint("PopupMenu");
     harness.check(defaults.get("PopupMenu.border") instanceof BorderUIResource.CompoundBorderUIResource);
     harness.check(defaults.get("PopupMenu.font"), new FontUIResource("Dialog", Font.PLAIN, 12));
     harness.check(defaults.get("PopupMenu.selectedWindowInputMapBindings") instanceof Object[]);
     harness.check(defaults.get("PopupMenu.selectedWindowInputMapBindings.RightToLeft") instanceof Object[]);
     
     harness.checkPoint("ProgressBar");
     harness.check(defaults.get("ProgressBar.border") instanceof BorderUIResource.LineBorderUIResource);
     harness.check(defaults.get("ProgressBar.cellLength"), new Integer(1));
     harness.check(defaults.get("ProgressBar.cellSpacing"), new Integer(0));
     harness.check(defaults.get("ProgressBar.cycleTime"), new Integer(3000));
     harness.check(defaults.get("ProgressBar.font"), new FontUIResource("Dialog", Font.PLAIN, 12));
     harness.check(defaults.get("ProgressBar.repaintInterval"), new Integer(50));
     
     harness.checkPoint("RadioButton");
     harness.check(defaults.get("RadioButton.border") instanceof BorderUIResource.CompoundBorderUIResource);
     harness.check(defaults.get("RadioButton.focusInputMap") instanceof InputMapUIResource);
     harness.check(defaults.get("RadioButton.font"), new FontUIResource("Dialog", Font.PLAIN, 12));
     harness.check(defaults.get("RadioButton.icon") instanceof Icon);
     harness.check(defaults.get("RadioButton.margin"), new InsetsUIResource(2, 2, 2, 2));
     harness.check(defaults.get("RadioButton.textIconGap"), new Integer(4));
     harness.check(defaults.get("RadioButton.textShiftOffset"), new Integer(0));
     
     harness.checkPoint("RadioButtonMenuItem");
     harness.check(defaults.get("RadioButtonMenuItem.acceleratorFont"), new FontUIResource("Dialog", Font.PLAIN, 12));
     harness.check(defaults.get("RadioButtonMenuItem.arrowIcon") instanceof Icon);
     harness.check(defaults.get("RadioButtonMenuItem.border") instanceof BasicBorders.MarginBorder);
     harness.check(defaults.get("RadioButtonMenuItem.borderPainted"), Boolean.FALSE);
     harness.check(defaults.get("RadioButtonMenuItem.checkIcon") instanceof Icon);
     harness.check(defaults.get("RadioButtonMenuItem.font"), new FontUIResource("Dialog", Font.PLAIN, 12));
     harness.check(defaults.get("RadioButtonMenuItem.margin"), new InsetsUIResource(2, 2, 2, 2));
     harness.check(defaults.get("RootPane.defaultButtonWindowKeyBindings") instanceof Object[]);
     
     harness.checkPoint("ScrollBar");
     harness.check(defaults.get("ScrollBar.background"), new ColorUIResource(224, 224, 224));
     harness.check(defaults.get("ScrollBar.focusInputMap") instanceof InputMap);
     harness.check(defaults.get("ScrollBar.focusInputMap.RightToLeft") instanceof InputMap);
     harness.check(defaults.get("ScrollBar.maximumThumbSize"), new DimensionUIResource(4096, 4096));
     harness.check(defaults.get("ScrollBar.minimumThumbSize"), new DimensionUIResource(8, 8));
     harness.check(defaults.get("ScrollBar.width"), new Integer(16));
     
     harness.check(defaults.get("ScrollPane.ancestorInputMap") instanceof InputMapUIResource);
     harness.check(defaults.get("ScrollPane.ancestorInputMap.RightToLeft") instanceof InputMapUIResource);
     harness.check(defaults.get("ScrollPane.border") instanceof BasicBorders.FieldBorder);
     harness.check(defaults.get("ScrollPane.font"), new FontUIResource("Dialog", Font.PLAIN, 12));
     
     harness.checkPoint("Slider");
     InputMap map = (InputMap) defaults.get("Slider.focusInputMap");
     KeyStroke[] keys = map.keys();
     InputMap focusInputMap = (InputMap) defaults.get("Slider.focusInputMap");
     List keyList = Arrays.asList(keys);
     harness.check(keyList.contains(KeyStroke.getKeyStroke("LEFT")));
     harness.check(keyList.contains(KeyStroke.getKeyStroke("RIGHT")));
     harness.check(keyList.contains(KeyStroke.getKeyStroke("UP")));
     harness.check(keyList.contains(KeyStroke.getKeyStroke("DOWN")));  
     harness.check(keyList.contains(KeyStroke.getKeyStroke("KP_LEFT")));
     harness.check(keyList.contains(KeyStroke.getKeyStroke("KP_RIGHT")));
     harness.check(keyList.contains(KeyStroke.getKeyStroke("KP_UP")));  
     harness.check(keyList.contains(KeyStroke.getKeyStroke("KP_DOWN")));  
     harness.check(keyList.contains(KeyStroke.getKeyStroke("HOME")));
     harness.check(keyList.contains(KeyStroke.getKeyStroke("END")));
     harness.check(keyList.contains(KeyStroke.getKeyStroke("PAGE_UP")));
     harness.check(keyList.contains(KeyStroke.getKeyStroke("PAGE_DOWN"))); 
     harness.check(focusInputMap.get(KeyStroke.getKeyStroke("LEFT")), "negativeUnitIncrement");
     harness.check(focusInputMap.get(KeyStroke.getKeyStroke("RIGHT")), "positiveUnitIncrement");
     harness.check(focusInputMap.get(KeyStroke.getKeyStroke("UP")), "positiveUnitIncrement");
     harness.check(focusInputMap.get(KeyStroke.getKeyStroke("DOWN")), "negativeUnitIncrement");
     harness.check(focusInputMap.get(KeyStroke.getKeyStroke("KP_LEFT")), "negativeUnitIncrement");
     harness.check(focusInputMap.get(KeyStroke.getKeyStroke("KP_RIGHT")), "positiveUnitIncrement");
     harness.check(focusInputMap.get(KeyStroke.getKeyStroke("KP_UP")), "positiveUnitIncrement");
     harness.check(focusInputMap.get(KeyStroke.getKeyStroke("KP_DOWN")), "negativeUnitIncrement");
     harness.check(focusInputMap.get(KeyStroke.getKeyStroke("HOME")), "minScroll");
     harness.check(focusInputMap.get(KeyStroke.getKeyStroke("END")), "maxScroll");
     harness.check(focusInputMap.get(KeyStroke.getKeyStroke("PAGE_UP")), "positiveBlockIncrement");
     harness.check(focusInputMap.get(KeyStroke.getKeyStroke("PAGE_DOWN")), "negativeBlockIncrement");
     
     InputMap rightToLeftMap = (InputMap) defaults.get("Slider.focusInputMap.RightToLeft");
     keys = rightToLeftMap != null ? rightToLeftMap.keys() : new KeyStroke[] {};
     keyList = Arrays.asList(keys);
     harness.check(keyList.contains(KeyStroke.getKeyStroke("RIGHT")));
     harness.check(keyList.contains(KeyStroke.getKeyStroke("KP_RIGHT")));
     harness.check(keyList.contains(KeyStroke.getKeyStroke("LEFT")));
     harness.check(keyList.contains(KeyStroke.getKeyStroke("KP_LEFT")));
     if (rightToLeftMap == null) 
     {
       rightToLeftMap = new InputMap();  // to prevent NullPointerException    
     }
     harness.check(rightToLeftMap.get(KeyStroke.getKeyStroke("RIGHT")), "negativeUnitIncrement");
     harness.check(rightToLeftMap.get(KeyStroke.getKeyStroke("KP_RIGHT")), "negativeUnitIncrement");
     harness.check(rightToLeftMap.get(KeyStroke.getKeyStroke("LEFT")), "positiveUnitIncrement");
     harness.check(rightToLeftMap.get(KeyStroke.getKeyStroke("KP_LEFT")), "positiveUnitIncrement");
     
     harness.check(defaults.get("Slider.focusInsets"), new InsetsUIResource(2, 2, 2, 2));
     
     harness.checkPoint("Spinner");
     harness.check(defaults.get("Spinner.ancestorInputMap") instanceof InputMapUIResource);
     harness.check(defaults.get("Spinner.arrowButtonSize"), new DimensionUIResource(16, 5));
     harness.check(defaults.get("Spinner.border") instanceof BasicBorders.FieldBorder);
     harness.check(defaults.get("Spinner.editorBorderPainted"), Boolean.FALSE);
     harness.check(defaults.get("Spinner.font"), new FontUIResource("MonoSpaced", Font.PLAIN, 12));
     
     harness.checkPoint("SplitPane");
     harness.check(defaults.get("SplitPane.ancestorInputMap") instanceof InputMapUIResource);
     harness.check(defaults.get("SplitPane.border") instanceof BasicBorders.SplitPaneBorder);
     harness.check(defaults.get("SplitPane.dividerSize"), new Integer(7));
     
     harness.checkPoint("SplitPaneDivider");
     harness.check(defaults.get("SplitPaneDivider.border") instanceof Border);
     
     harness.checkPoint("TabbedPane");
     harness.check(defaults.get("TabbedPane.ancestorInputMap") instanceof InputMapUIResource);
     harness.check(defaults.get("TabbedPane.contentBorderInsets"), new InsetsUIResource(2, 2, 3, 3));
     harness.check(defaults.get("TabbedPane.focusInputMap") instanceof InputMapUIResource);
     harness.check(defaults.get("TabbedPane.font"), new FontUIResource("Dialog", Font.PLAIN, 12));
     harness.check(defaults.get("TabbedPane.selectedTabPadInsets"), new InsetsUIResource(2, 2, 2, 1));
     harness.check(defaults.get("TabbedPane.tabAreaInsets"), new InsetsUIResource(3, 2, 0, 2));
     harness.check(defaults.get("TabbedPane.tabInsets"), new InsetsUIResource(0, 4, 1, 4));
     harness.check(defaults.get("TabbedPane.tabRunOverlay"), new Integer(2));
     harness.check(defaults.get("TabbedPane.textIconGap"), new Integer(4));
     
     harness.checkPoint("Table");
     harness.check(defaults.get("Table.ancestorInputMap") instanceof InputMapUIResource);
     harness.check(defaults.get("Table.ancestorInputMap.RightToLeft") instanceof InputMapUIResource);
     harness.check(defaults.get("Table.focusCellHighlightBorder") instanceof BorderUIResource.LineBorderUIResource);
     harness.check(defaults.get("Table.font"), new FontUIResource("Dialog", Font.PLAIN, 12));
     harness.check(defaults.get("Table.gridColor"), new ColorUIResource(128, 128, 128));
     harness.check(defaults.get("Table.scrollPaneBorder") instanceof BorderUIResource.BevelBorderUIResource);
     
     harness.checkPoint("TableHeader");
     harness.check(defaults.get("TableHeader.cellBorder"), null);
     harness.check(defaults.get("TableHeader.font"), new FontUIResource("Dialog", Font.PLAIN, 12));
     
     harness.checkPoint("TextArea");
     harness.check(defaults.get("TextArea.border") instanceof BasicBorders.MarginBorder);
     harness.check(defaults.get("TextArea.caretBlinkRate"), new Integer(500));
     harness.check(defaults.get("TextArea.font"), new FontUIResource("MonoSpaced", Font.PLAIN, 12));
     harness.check(defaults.get("TextArea.margin"), new InsetsUIResource(0, 0, 0, 0));
     
     harness.checkPoint("TextField");
     harness.check(defaults.get("TextField.border") instanceof BasicBorders.FieldBorder);
     harness.check(defaults.get("TextField.caretBlinkRate"), new Integer(500));
     harness.check(defaults.get("TextField.font"), new FontUIResource("SansSerif", Font.PLAIN, 12));
     harness.check(defaults.get("TextField.margin"), new InsetsUIResource(0, 0, 0, 0));
     
     harness.checkPoint("TextPane");
     harness.check(defaults.get("TextPane.background"), new ColorUIResource(255, 255, 255));
     harness.check(defaults.get("TextPane.border") instanceof BasicBorders.MarginBorder);
     harness.check(defaults.get("TextPane.caretBlinkRate"), new Integer(500));
     harness.check(defaults.get("TextPane.font"), new FontUIResource("Serif", Font.PLAIN, 12));
     harness.check(defaults.get("TextPane.margin"), new InsetsUIResource(3, 3, 3, 3));
     
     harness.checkPoint("TitledBorder");
     harness.check(defaults.get("TitledBorder.border") instanceof BorderUIResource.EtchedBorderUIResource);
     harness.check(defaults.get("TitledBorder.font"), new FontUIResource("Dialog", Font.PLAIN, 12));
     
     harness.checkPoint("ToggleButton");
     harness.check(defaults.get("ToggleButton.border") instanceof BorderUIResource.CompoundBorderUIResource);
     harness.check(defaults.get("ToggleButton.focusInputMap") instanceof InputMapUIResource);
     harness.check(defaults.get("ToggleButton.font"), new FontUIResource("Dialog", Font.PLAIN, 12));
     harness.check(defaults.get("ToggleButton.margin"), new InsetsUIResource(2, 14, 2, 14));
     harness.check(defaults.get("ToggleButton.textIconGap"), new Integer(4));
     harness.check(defaults.get("ToggleButton.textShiftOffset"), new Integer(0));
     
     harness.checkPoint("ToolBar");
     harness.check(defaults.get("ToolBar.ancestorInputMap") instanceof InputMapUIResource);
     harness.check(defaults.get("ToolBar.border") instanceof BorderUIResource.EtchedBorderUIResource);
     harness.check(defaults.get("ToolBar.dockingForeground"), new ColorUIResource(255, 0, 0));
     harness.check(defaults.get("ToolBar.floatingForeground"), new ColorUIResource(64, 64, 64));
     harness.check(defaults.get("ToolBar.font"), new FontUIResource("Dialog", Font.PLAIN, 12));
     harness.check(defaults.get("ToolBar.separatorSize"), new DimensionUIResource(10, 10));
     
     harness.checkPoint("ToolTip");
     harness.check(defaults.get("ToolTip.border") instanceof BorderUIResource.LineBorderUIResource);
     harness.check(defaults.get("ToolTip.font"), new FontUIResource("SansSerif", Font.PLAIN, 12));
     
     harness.checkPoint("Tree");
     harness.check(defaults.get("Tree.ancestorInputMap") instanceof InputMapUIResource);
     harness.check(defaults.get("Tree.changeSelectionWithFocus"), Boolean.TRUE);
     // FIXME: the following check is commented out - the JDK returns null because 
     // MyBasicLookAndFeel doesn't look in the right place for the gif
     //harness.check(defaults.get("Tree.closedIcon"), null);
     harness.check(defaults.get("Tree.drawsFocusBorderAroundIcon"), Boolean.FALSE);
     harness.check(defaults.get("Tree.editorBorder") instanceof BorderUIResource.LineBorderUIResource);
     harness.check(defaults.get("Tree.focusInputMap") instanceof InputMapUIResource);
     harness.check(defaults.get("Tree.focusInputMap.RightToLeft") instanceof InputMapUIResource);
     harness.check(defaults.get("Tree.font"), new FontUIResource("Dialog", Font.PLAIN, 12));
     harness.check(defaults.get("Tree.hash"), new ColorUIResource(128, 128, 128));
     // FIXME: the following check is commented out - the JDK returns null because 
     // MyBasicLookAndFeel doesn't look in the right place for the gif
     //harness.check(defaults.get("Tree.leafIcon"), null);
     harness.check(defaults.get("Tree.leftChildIndent"), new Integer(7));
     // FIXME: the following check is commented out - the JDK returns null because 
     // MyBasicLookAndFeel doesn't look in the right place for the gif
     //harness.check(defaults.get("Tree.openIcon"), null);
     harness.check(defaults.get("Tree.rightChildIndent"), new Integer(13));
     harness.check(defaults.get("Tree.rowHeight"), new Integer(16));
     harness.check(defaults.get("Tree.scrollsOnExpand"), Boolean.TRUE);
     harness.check(defaults.get("Tree.selectionBorderColor"), new ColorUIResource(0, 0, 0));
    harness.check(defaults.get("Tree.textForeground"), new ColorUIResource(Color.black));
    harness.check(defaults.get("Tree.textBackground"), new ColorUIResource(new Color(192, 192,192)));

     harness.checkPoint("Viewport");
     harness.check(defaults.get("Viewport.font"), new FontUIResource("Dialog", Font.PLAIN, 12));
   }
 }
