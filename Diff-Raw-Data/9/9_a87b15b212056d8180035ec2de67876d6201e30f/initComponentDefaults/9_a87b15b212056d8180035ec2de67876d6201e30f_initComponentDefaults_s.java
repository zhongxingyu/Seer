 // Tags: JDK1.3
// Uses: MyMetalLookAndFeel.java
 
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
 
 package gnu.testlet.javax.swing.plaf.metal.MetalLookAndFeel;
 
 import gnu.testlet.TestHarness;
 import gnu.testlet.Testlet;
 
 import java.awt.Color;
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
 import javax.swing.plaf.BorderUIResource.LineBorderUIResource;
 import javax.swing.plaf.basic.BasicBorders;
 import javax.swing.plaf.metal.MetalBorders;
 import javax.swing.plaf.metal.MetalIconFactory;
 import javax.swing.plaf.metal.MetalLookAndFeel;
 
 /**
  * Some checks for the initComponentDefaults() method in the 
  * {@link MetalLookAndFeel} class.  
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
     MyMetalLookAndFeel laf = new MyMetalLookAndFeel();
     UIDefaults defaults = new UIDefaults();
     laf.initComponentDefaults(defaults);
 //    Iterator iterator = defaults.keySet().iterator();
 //    while (iterator.hasNext()) {
 //        Object key = iterator.next();
 //        System.out.println(key);
 //    }
     
     // TODO: in the following code, there are many 'instanceof' checks - these
     // are typically very weak tests.  Maybe they can be strengthened...
     harness.checkPoint("AuditoryCues");
     harness.check(defaults.get("AuditoryCues.allAuditoryCues") != null);
     harness.check(defaults.get("AuditoryCues.cueList") != null);
     harness.check(defaults.get("AuditoryCues.defaultCueList") != null);
     harness.check(defaults.get("AuditoryCues.noAuditoryCues") != null);
     
     harness.checkPoint("Button");
     harness.check(defaults.get("Button.border") instanceof BorderUIResource.CompoundBorderUIResource);
     harness.check(defaults.get("Button.disabledText"), new ColorUIResource(153, 153, 153));
     harness.check(defaults.get("Button.focus"), new ColorUIResource(153, 153, 204));
     harness.check(defaults.get("Button.focusInputMap") instanceof InputMapUIResource);
     harness.check(defaults.get("Button.font"), new FontUIResource("Dialog", Font.BOLD, 12));
     harness.check(defaults.get("Button.margin"), new InsetsUIResource(2, 14, 2, 14));
     harness.check(defaults.get("Button.select"), new ColorUIResource(153, 153, 153));
     harness.check(defaults.get("Button.textIconGap"), new Integer(4));
     harness.check(defaults.get("Button.textShiftOffset"), new Integer(0));
     
     harness.checkPoint("CheckBox");
     harness.check(defaults.get("CheckBox.border") instanceof BorderUIResource.CompoundBorderUIResource);
     harness.check(defaults.get("CheckBox.disabledText"), new ColorUIResource(153, 153, 153));
     harness.check(defaults.get("CheckBox.focus"), new ColorUIResource(153, 153, 204));
     harness.check(defaults.get("CheckBox.focusInputMap") instanceof InputMapUIResource);
     harness.check(defaults.get("CheckBox.font"), new FontUIResource("Dialog", Font.BOLD, 12));
     harness.check(defaults.get("CheckBox.icon") instanceof Icon);
     harness.check(defaults.get("CheckBox.margin"), new InsetsUIResource(2, 2, 2, 2));
     harness.check(defaults.get("Checkbox.select"), new ColorUIResource(153, 153, 153));
     harness.check(defaults.get("CheckBox.textIconGap"), new Integer(4));
     harness.check(defaults.get("CheckBox.textShiftOffset"), new Integer(0));
 
     harness.checkPoint("CheckBoxMenuItem");
     harness.check(defaults.get("CheckBoxMenuItem.acceleratorFont"), new Font("Dialog", Font.PLAIN, 10));
     harness.check(defaults.get("CheckBoxMenuItem.acceleratorForeground"), new ColorUIResource(102, 102, 153));
     harness.check(defaults.get("CheckBoxMenuItem.acceleratorSelectionForeground"), new ColorUIResource(0, 0, 0));
     harness.check(defaults.get("CheckBoxMenuItem.arrowIcon") instanceof Icon);
 //    harness.check(defaults.get("CheckBoxMenuItem.border") instanceof MetalBorders.MenuItemBorder); 
     harness.check(defaults.get("CheckBoxMenuItem.borderPainted"), Boolean.TRUE);
     harness.check(defaults.get("CheckBoxMenuItem.checkIcon") instanceof Icon);
     harness.check(defaults.get("CheckBoxMenuItem.commandSound"), "sounds/MenuItemCommand.wav");
     harness.check(defaults.get("CheckBoxMenuItem.disabledForeground"), new ColorUIResource(153, 153, 153));
     harness.check(defaults.get("CheckBoxMenuItem.font"), new FontUIResource("Dialog", Font.BOLD, 12));
     harness.check(defaults.get("CheckBoxMenuItem.margin"), new InsetsUIResource(2, 2, 2, 2));
     harness.check(defaults.get("CheckBoxMenuItem.selectionBackground"), new ColorUIResource(153, 153, 204));
     harness.check(defaults.get("CheckBoxMenuItem.selectionForeground"), new ColorUIResource(0, 0, 0));
 
     harness.checkPoint("ColorChooser");
     harness.check(defaults.get("ColorChooser.font"), new FontUIResource("Dialog", Font.PLAIN, 12));
     harness.check(defaults.get("ColorChooser.rgbBlueMnemonic"), new Integer(66));
     harness.check(defaults.get("ColorChooser.rgbGreenMnemonic"), new Integer(78));
     harness.check(defaults.get("ColorChooser.rgbRedMnemonic"), new Integer(68));
     harness.check(defaults.get("ColorChooser.swatchesRecentSwatchSize"), new Dimension(10, 10));
     harness.check(defaults.get("ColorChooser.swatchesSwatchSize"), new Dimension(10, 10));
 
     harness.checkPoint("ComboBox");
     harness.check(defaults.get("ComboBox.ancestorInputMap") instanceof InputMapUIResource);
     harness.check(defaults.get("ComboBox.font"), new FontUIResource("Dialog", Font.BOLD, 12));
     harness.check(defaults.get("ComboBox.selectionBackground"), new ColorUIResource(153, 153, 204));
     harness.check(defaults.get("ComboBox.selectionForeground"), new ColorUIResource(0, 0, 0));
     
     harness.checkPoint("Desktop");
     harness.check(defaults.get("Desktop.ancestorInputMap") instanceof InputMapUIResource);
     
     harness.checkPoint("DesktopIcon");
     harness.check(defaults.get("DesktopIcon.background"), new ColorUIResource(204, 204, 204));
     harness.check(defaults.get("DesktopIcon.border") instanceof BorderUIResource.CompoundBorderUIResource); 
     harness.check(defaults.get("DesktopIcon.font"), new FontUIResource("Dialog", Font.BOLD, 12));
     harness.check(defaults.get("DesktopIcon.foreground"), new ColorUIResource(0, 0, 0));
     harness.check(defaults.get("DesktopIcon.width"), new Integer(160));
     
     harness.checkPoint("EditorPane");
     harness.check(defaults.get("EditorPane.border") instanceof BasicBorders.MarginBorder); 
     harness.check(defaults.get("EditorPane.caretBlinkRate"), new Integer(500));
     harness.check(defaults.get("EditorPane.focusInputMap") instanceof InputMapUIResource);
     harness.check(defaults.get("EditorPane.font"), new FontUIResource("Dialog", Font.PLAIN, 12));
     harness.check(defaults.get("EditorPane.margin"), new InsetsUIResource(3, 3, 3, 3));
     
     harness.checkPoint("FileChooser");
     harness.check(defaults.get("FileChooser.ancestorInputMap") instanceof InputMapUIResource);
     harness.check(defaults.get("FileChooser.cancelButtonMnemonic"), new Integer(67));
     harness.check(defaults.get("FileChooser.detailsViewIcon") instanceof Icon);
     harness.check(defaults.get("FileChooser.directoryOpenButtonMnemonic"), new Integer(79));
     harness.check(defaults.get("FileChooser.fileNameLabelMnemonic"), new Integer(78));
     harness.check(defaults.get("FileChooser.filesOfTypeLabelMnemonic"), new Integer(84));
     harness.check(defaults.get("FileChooser.helpButtonMnemonic"), new Integer(72));
     harness.check(defaults.get("FileChooser.homeFolderIcon") instanceof Icon);
     harness.check(defaults.get("FileChooser.listViewIcon") instanceof Icon);
     harness.check(defaults.get("FileChooser.lookInLabelMnemonic"), new Integer(73));
     harness.check(defaults.get("FileChooser.newFolderIcon") instanceof Icon);
     harness.check(defaults.get("FileChooser.openButtonMnemonic"), new Integer(79));
     harness.check(defaults.get("FileChooser.saveButtonMnemonic"), new Integer(83));
     harness.check(defaults.get("FileChooser.updateButtonMnemonic"), new Integer(85));
     harness.check(defaults.get("FileChooser.upFolderIcon") instanceof Icon);
     
     harness.checkPoint("FileView");
     harness.check(defaults.get("FileView.computerIcon") instanceof Icon);
     harness.check(defaults.get("FileView.directoryIcon") instanceof Icon);
     harness.check(defaults.get("FileView.fileIcon") instanceof Icon);
     harness.check(defaults.get("FileView.floppyDriveIcon") instanceof Icon);
     harness.check(defaults.get("FileView.hardDriveIcon") instanceof Icon);
     
     harness.checkPoint("FormattedTextField");
     harness.check(defaults.get("FormattedTextField.border") instanceof BorderUIResource.CompoundBorderUIResource);  
     harness.check(defaults.get("FormattedTextField.caretBlinkRate"), new Integer(500));
     harness.check(defaults.get("FormattedTextField.focusInputMap") instanceof InputMapUIResource);
     harness.check(defaults.get("FormattedTextField.font"), new FontUIResource("Dialog", Font.PLAIN, 12));
     harness.check(defaults.get("FormattedTextField.margin"), new InsetsUIResource(0, 0, 0, 0));
     
     harness.checkPoint("InternalFrame");
 //    harness.check(defaults.get("InternalFrame.border") instanceof MetalBorders.InternalFrameBorder); 
     harness.check(defaults.get("InternalFrame.closeIcon") instanceof Icon);
     harness.check(defaults.get("InternalFrame.closeSound"), "sounds/FrameClose.wav");
     harness.check(defaults.get("InternalFrame.icon") instanceof Icon);
     harness.check(defaults.get("InternalFrame.iconifyIcon") instanceof Icon);
     harness.check(defaults.get("InternalFrame.maximizeIcon") instanceof Icon);
     harness.check(defaults.get("InternalFrame.maximizeSound"), "sounds/FrameMaximize.wav");
     harness.check(defaults.get("InternalFrame.minimizeIcon") instanceof Icon);
     harness.check(defaults.get("InternalFrame.minimizeSound"), "sounds/FrameMinimize.wav");
 //    harness.check(defaults.get("InternalFrame.optionDialogBorder") instanceof MetalBorders.OptionDialogBorder);
 //    harness.check(defaults.get("InternalFrame.paletteBorder") instanceof MetalBorders.PaletteBorder);
     harness.check(defaults.get("InternalFrame.paletteCloseIcon") instanceof Icon);
     harness.check(defaults.get("InternalFrame.paletteTitleHeight"), new Integer(11));
     harness.check(defaults.get("InternalFrame.restoreDownSound"), "sounds/FrameRestoreDown.wav");
     harness.check(defaults.get("InternalFrame.restoreUpSound"), "sounds/FrameRestoreUp.wav");
     harness.check(defaults.get("InternalFrame.titleFont"), new FontUIResource("Dialog", Font.BOLD, 12));
     
     harness.checkPoint("Label");
     harness.check(defaults.get("Label.disabledForeground"), new ColorUIResource(153, 153, 153));
     harness.check(defaults.get("Label.font"), new FontUIResource("Dialog", Font.BOLD, 12));
     harness.check(defaults.get("Label.foreground"), new ColorUIResource(0, 0, 0));
     
     harness.checkPoint("List");
     harness.check(defaults.get("List.cellRenderer") instanceof ListCellRenderer);
     harness.check(defaults.get("List.focusCellHighlightBorder") instanceof BorderUIResource.LineBorderUIResource);
     harness.check(defaults.get("List.focusInputMap") instanceof InputMapUIResource);
     harness.check(defaults.get("List.focusInputMap.RightToLeft") instanceof InputMapUIResource);
     harness.check(defaults.get("List.font"), new FontUIResource("Dialog", Font.BOLD, 12));
     
     harness.checkPoint("Menu");
     harness.check(defaults.get("Menu.acceleratorFont"), new FontUIResource("Dialog", Font.PLAIN, 10));
     harness.check(defaults.get("Menu.acceleratorForeground"), new ColorUIResource(102, 102, 153));
     harness.check(defaults.get("Menu.acceleratorSelectionForeground"), new ColorUIResource(0, 0, 0));
     harness.check(defaults.get("Menu.arrowIcon") instanceof Icon); 
 //    harness.check(defaults.get("Menu.border") instanceof MetalBorders.MenuItemBorder);
     harness.check(defaults.get("Menu.borderPainted"), Boolean.TRUE);
     harness.check(defaults.get("Menu.checkIcon"), null);
     harness.check(defaults.get("Menu.crossMenuMnemonic"), Boolean.TRUE);
     harness.check(defaults.get("Menu.disabledForeground"), new ColorUIResource(153, 153, 153));
     harness.check(defaults.get("Menu.font"), new FontUIResource("Dialog", Font.BOLD, 12));
     harness.check(defaults.get("Menu.margin"), new InsetsUIResource(2, 2, 2, 2));
     harness.check(defaults.get("Menu.menuPopupOffsetX"), new Integer(0));
     harness.check(defaults.get("Menu.menuPopupOffsetY"), new Integer(0));
     harness.check(defaults.get("Menu.selectionBackground"), new ColorUIResource(153, 153, 204));
     harness.check(defaults.get("Menu.selectionForeground"), new ColorUIResource(0, 0, 0));
     int[] value = (int[]) defaults.get("Menu.shortcutKeys");
     harness.check(value != null ? value.length : 0, 1);
     harness.check(value != null ? value[0] : 0, 8);
     harness.check(defaults.get("Menu.submenuPopupOffsetX"), new Integer(-4));
     harness.check(defaults.get("Menu.submenuPopupOffsetY"), new Integer(-3));
     
     harness.checkPoint("MenuBar");
 //    harness.check(defaults.get("MenuBar.border") instanceof MetalBorders.MenuBarBorder);
     harness.check(defaults.get("MenuBar.font"), new FontUIResource("Dialog", Font.BOLD, 12));
     Object[] bindings = (Object[]) defaults.get("MenuBar.windowBindings");
     harness.check(bindings.length, 2);
     harness.check(bindings[0], "F10");
     harness.check(bindings[1], "takeFocus");
     
     harness.checkPoint("MenuItem");
     harness.check(defaults.get("MenuItem.acceleratorDelimiter"), "-");
     harness.check(defaults.get("MenuItem.acceleratorFont"), new FontUIResource("Dialog", Font.PLAIN, 10));
     harness.check(defaults.get("MenuItem.acceleratorForeground"), new ColorUIResource(102, 102, 153));
     harness.check(defaults.get("MenuItem.acceleratorSelectionForeground"), new ColorUIResource(0, 0, 0));
     harness.check(defaults.get("MenuItem.arrowIcon") instanceof Icon);
 //    harness.check(defaults.get("MenuItem.border") instanceof MetalBorders.MenuItemBorder);
     harness.check(defaults.get("MenuItem.borderPainted"), Boolean.TRUE);
     harness.check(defaults.get("MenuItem.checkIcon"), null);
     harness.check(defaults.get("MenuItem.commandSound"), "sounds/MenuItemCommand.wav");
     harness.check(defaults.get("MenuItem.disabledForeground"), new ColorUIResource(153, 153, 153));
     harness.check(defaults.get("MenuItem.font"), new FontUIResource("Dialog", Font.BOLD, 12));
     harness.check(defaults.get("MenuItem.margin"), new InsetsUIResource(2, 2, 2, 2));
     harness.check(defaults.get("MenuItem.selectionBackground"), new ColorUIResource(153, 153, 204));
     harness.check(defaults.get("MenuItem.selectionForeground"), new ColorUIResource(0, 0, 0));
     
     harness.checkPoint("OptionPane");
     harness.check(defaults.get("OptionPane.border") instanceof BorderUIResource.EmptyBorderUIResource);
     harness.check(defaults.get("OptionPane.buttonAreaBorder") instanceof BorderUIResource.EmptyBorderUIResource);
     harness.check(defaults.get("OptionPane.buttonClickThreshhold"), new Integer(500));
     harness.check(defaults.get("OptionPane.errorDialog.border.background"), new ColorUIResource(153, 51, 51));
     harness.check(defaults.get("OptionPane.errorDialog.titlePane.background"), new ColorUIResource(255, 153, 153));
     harness.check(defaults.get("OptionPane.errorDialog.titlePane.foreground"), new ColorUIResource(51, 0, 0));
     harness.check(defaults.get("OptionPane.errorDialog.titlePane.shadow"), new ColorUIResource(204, 102, 102));
     harness.check(defaults.get("OptionPane.errorIcon"), null);
     harness.check(defaults.get("OptionPane.errorSound"), "sounds/OptionPaneError.wav");
     harness.check(defaults.get("OptionPane.font"), new FontUIResource("Dialog", Font.PLAIN, 12));
     harness.check(defaults.get("OptionPane.informationIcon"), null);
     harness.check(defaults.get("OptionPane.informationSound"), "sounds/OptionPaneInformation.wav");
     harness.check(defaults.get("OptionPane.messageAreaBorder") instanceof BorderUIResource.EmptyBorderUIResource);
     harness.check(defaults.get("OptionPane.minimumSize"), new DimensionUIResource(262, 90));
     harness.check(defaults.get("OptionPane.questionDialog.border.background"), new ColorUIResource(51, 102, 51));
     harness.check(defaults.get("OptionPane.questionDialog.titlePane.background"), new ColorUIResource(153, 204, 153));
     harness.check(defaults.get("OptionPane.questionDialog.titlePane.foreground"), new ColorUIResource(0, 51, 0));
     harness.check(defaults.get("OptionPane.questionDialog.titlePane.shadow"), new ColorUIResource(102, 153, 102));
     harness.check(defaults.get("OptionPane.questionIcon"), null);
     harness.check(defaults.get("OptionPane.questionSound"), "sounds/OptionPaneQuestion.wav");
     harness.check(defaults.get("OptionPane.warningDialog.border.background"), new ColorUIResource(153, 102, 51));
     harness.check(defaults.get("OptionPane.warningDialog.titlePane.background"), new ColorUIResource(255, 204, 153));
     harness.check(defaults.get("OptionPane.warningDialog.titlePane.foreground"), new ColorUIResource(102, 51, 0));
     harness.check(defaults.get("OptionPane.warningDialog.titlePane.shadow"), new ColorUIResource(204, 153, 102));
     harness.check(defaults.get("OptionPane.warningIcon"), null);
     harness.check(defaults.get("OptionPane.warningSound"), "sounds/OptionPaneWarning.wav");
     bindings = (Object[]) defaults.get("OptionPane.windowBindings");
     harness.check(bindings.length, 2);
     harness.check(bindings[0], "ESCAPE");
     harness.check(bindings[1], "close");
     
     harness.checkPoint("Panel");
     harness.check(defaults.get("Panel.font"), new FontUIResource("Dialog", Font.PLAIN, 12));
     
     harness.checkPoint("PasswordField");
     harness.check(defaults.get("PasswordField.border") instanceof BorderUIResource.CompoundBorderUIResource);
     harness.check(defaults.get("PasswordField.caretBlinkRate"), new Integer(500));
     harness.check(defaults.get("PasswordField.focusInputMap") instanceof InputMapUIResource);
     harness.check(defaults.get("PasswordField.font"), new FontUIResource("Dialog", Font.PLAIN, 12));
     harness.check(defaults.get("PasswordField.margin"), new InsetsUIResource(0, 0, 0, 0));
     
     harness.checkPoint("PopupMenu");
     harness.check(defaults.get("PopupMenu.border") instanceof MetalBorders.PopupMenuBorder);
     harness.check(defaults.get("PopupMenu.font"), new FontUIResource("Dialog", Font.BOLD, 12));
     harness.check(defaults.get("PopupMenu.popupSound"), "sounds/PopupMenuPopup.wav");
     harness.check(defaults.get("PopupMenu.selectedWindowInputMapBindings") instanceof Object[]);
     harness.check(defaults.get("PopupMenu.selectedWindowInputMapBindings.RightToLeft") instanceof Object[]);
     
     harness.checkPoint("ProgressBar");
     LineBorderUIResource b = (LineBorderUIResource) defaults.get("ProgressBar.border");
     harness.check(b.getThickness(), 1);
     harness.check(b.getLineColor(), new Color(102, 102, 102));
     harness.check(defaults.get("ProgressBar.cellLength"), new Integer(1));
     harness.check(defaults.get("ProgressBar.cellSpacing"), new Integer(0));
     harness.check(defaults.get("ProgressBar.cycleTime"), new Integer(3000));
     harness.check(defaults.get("ProgressBar.font"), new FontUIResource("Dialog", Font.BOLD, 12));
     harness.check(defaults.get("ProgressBar.foreground"), new ColorUIResource(153, 153, 204));
     harness.check(defaults.get("ProgressBar.repaintInterval"), new Integer(50));
     harness.check(defaults.get("ProgressBar.selectionBackground"), new ColorUIResource(102, 102, 153));
     
     harness.checkPoint("RadioButton");
     harness.check(defaults.get("RadioButton.border") instanceof BorderUIResource.CompoundBorderUIResource);
     harness.check(defaults.get("RadioButton.disabledText"), new ColorUIResource(153, 153, 153));
     harness.check(defaults.get("RadioButton.focus"), new ColorUIResource(153, 153, 204));
     harness.check(defaults.get("RadioButton.focusInputMap") instanceof InputMapUIResource);
     harness.check(defaults.get("RadioButton.font"), new FontUIResource("Dialog", Font.BOLD, 12));
     harness.check(defaults.get("RadioButton.icon") instanceof Icon);
     harness.check(defaults.get("RadioButton.margin"), new InsetsUIResource(2, 2, 2, 2));
     harness.check(defaults.get("RadioButton.select"), new ColorUIResource(153, 153, 153));
     harness.check(defaults.get("RadioButton.textIconGap"), new Integer(4));
     harness.check(defaults.get("RadioButton.textShiftOffset"), new Integer(0));
     
     harness.checkPoint("RadioButtonMenuItem");
     harness.check(defaults.get("RadioButtonMenuItem.acceleratorFont"), new Font("Dialog", Font.PLAIN, 10));
     harness.check(defaults.get("RadioButtonMenuItem.acceleratorForeground"), new ColorUIResource(102, 102, 153));
     harness.check(defaults.get("RadioButtonMenuItem.acceleratorSelectionForeground"), new ColorUIResource(0, 0, 0));
     harness.check(defaults.get("RadioButtonMenuItem.arrowIcon") instanceof Icon);
 //    harness.check(defaults.get("RadioButtonMenuItem.border") instanceof MetalBorders.MenuItemBorder);
     harness.check(defaults.get("RadioButtonMenuItem.borderPainted"), Boolean.TRUE);
     harness.check(defaults.get("RadioButtonMenuItem.checkIcon") instanceof Icon);
     harness.check(defaults.get("RadioButtonMenuItem.commandSound"), "sounds/MenuItemCommand.wav");
     harness.check(defaults.get("RadioButtonMenuItem.disabledForeground"), new ColorUIResource(153, 153, 153));
     harness.check(defaults.get("RadioButtonMenuItem.font"), new FontUIResource("Dialog", Font.BOLD, 12));
     harness.check(defaults.get("RadioButtonMenuItem.margin"), new InsetsUIResource(2, 2, 2, 2));
     harness.check(defaults.get("RadioButtonMenuItem.selectionBackground"), new ColorUIResource(153, 153, 204));
     harness.check(defaults.get("RadioButtonMenuItem.selectionForeground"), new ColorUIResource(0, 0, 0));
     
     harness.checkPoint("RootPane");
     harness.check(defaults.get("RootPane.colorChooserDialogBorder") instanceof Border);
     harness.check(defaults.get("RootPane.defaultButtonWindowKeyBindings") instanceof Object[]);
     harness.check(defaults.get("RootPane.errorDialogBorder") instanceof Border);
     harness.check(defaults.get("RootPane.fileChooserDialogBorder") instanceof Border);
     harness.check(defaults.get("RootPane.frameBorder") instanceof Border);
     harness.check(defaults.get("RootPane.informationDialogBorder") instanceof Border);
     harness.check(defaults.get("RootPane.plainDialogBorder") instanceof Border);
     harness.check(defaults.get("RootPane.questionDialogBorder") instanceof Border);
     harness.check(defaults.get("RootPane.warningDialogBorder") instanceof Border);
     
     harness.checkPoint("ScrollBar");
     harness.check(defaults.get("ScrollBar.allowsAbsolutePositioning"), Boolean.TRUE);
     harness.check(defaults.get("ScrollBar.background"), new ColorUIResource(204, 204, 204));
     harness.check(defaults.get("ScrollBar.darkShadow"), new ColorUIResource(102, 102, 102));
     harness.check(defaults.get("ScrollBar.focusInputMap") instanceof InputMapUIResource);
     harness.check(defaults.get("ScrollBar.focusInputMap.RightToLeft") instanceof InputMapUIResource);
     harness.check(defaults.get("ScrollBar.highlight"), new ColorUIResource(255, 255, 255));
     harness.check(defaults.get("ScrollBar.maximumThumbSize"), new DimensionUIResource(4096, 4096));
     harness.check(defaults.get("ScrollBar.minimumThumbSize"), new DimensionUIResource(8, 8));
     harness.check(defaults.get("ScrollBar.shadow"), new ColorUIResource(153, 153, 153));
     harness.check(defaults.get("ScrollBar.thumb"), new ColorUIResource(153, 153, 204));
     harness.check(defaults.get("ScrollBar.thumbHighlight"), new ColorUIResource(204, 204, 255));
     harness.check(defaults.get("ScrollBar.thumbShadow"), new ColorUIResource(102, 102, 153));
     harness.check(defaults.get("ScrollBar.width"), new Integer(17));
     
     harness.checkPoint("ScrollPane");
     harness.check(defaults.get("ScrollPane.ancestorInputMap") instanceof InputMapUIResource);
     harness.check(defaults.get("ScrollPane.ancestorInputMap.RightToLeft") instanceof InputMapUIResource);
     harness.check(defaults.get("ScrollPane.border") instanceof MetalBorders.ScrollPaneBorder);
     harness.check(defaults.get("ScrollPane.font"), new FontUIResource("Dialog", Font.PLAIN, 12));
     
     harness.checkPoint("Separator");
     harness.check(defaults.get("Separator.background"), new ColorUIResource(255, 255, 255));
     harness.check(defaults.get("Separator.foreground"), new ColorUIResource(102, 102, 153));
     
     harness.checkPoint("Slider");
     harness.check(defaults.get("Slider.focus"), new ColorUIResource(153, 153, 204));
     
     InputMap focusInputMap = (InputMap) defaults.get("Slider.focusInputMap");
     KeyStroke[] keys = focusInputMap.keys();
 //    for (int i = 0; i < keys.length; i++) {
 //        System.out.println(keys[i] + " --> " + focusInputMap.get(keys[i]));
 //    }
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
     harness.check(keyList.contains(KeyStroke.getKeyStroke("ctrl PAGE_DOWN"))); 
     harness.check(keyList.contains(KeyStroke.getKeyStroke("ctrl PAGE_UP")));  
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
     harness.check(focusInputMap.get(KeyStroke.getKeyStroke("ctrl PAGE_DOWN")), "negativeBlockIncrement");
     harness.check(focusInputMap.get(KeyStroke.getKeyStroke("ctrl PAGE_UP")), "positiveBlockIncrement");
     
     InputMap rightToLeftMap = (InputMap) defaults.get("Slider.focusInputMap.RightToLeft");
     keys = rightToLeftMap != null ? rightToLeftMap.keys() : new KeyStroke[] {};
     keyList = Arrays.asList(keys);
 //    for (int i = 0; i < keys.length; i++) {
 //        System.out.println(keys[i] + " --> " + focusInputMap.get(keys[i]));
 //    }
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
     
     harness.check(defaults.get("Slider.focusInsets"), new InsetsUIResource(0, 0, 0, 0));
     harness.check(defaults.get("Slider.foreground"), new ColorUIResource(153, 153, 204));
     harness.check(defaults.get("Slider.horizontalThumbIcon") != null); 
     harness.check(defaults.get("Slider.majorTickLength"), new Integer(6));
     harness.check(defaults.get("Slider.trackWidth"), new Integer(7));
     harness.check(defaults.get("Slider.verticalThumbIcon") != null);
     
     harness.checkPoint("Spinner");
     harness.check(defaults.get("Spinner.ancestorInputMap") instanceof InputMapUIResource);
     harness.check(defaults.get("Spinner.arrowButtonBorder") instanceof Border);
     harness.check(defaults.get("Spinner.arrowButtonInsets"), new InsetsUIResource(0, 0, 0, 0));
     harness.check(defaults.get("Spinner.arrowButtonSize"), new Dimension(16, 5));
     harness.check(defaults.get("Spinner.border") instanceof Border);
     harness.check(defaults.get("Spinner.editorBorderPainted"), Boolean.FALSE);
     harness.check(defaults.get("Spinner.font"), new FontUIResource("Dialog", Font.BOLD, 12));
     
     harness.checkPoint("SplitPane");
     harness.check(defaults.get("SplitPane.ancestorInputMap") instanceof InputMapUIResource);
     harness.check(defaults.get("SplitPane.border") instanceof Border);
     harness.check(defaults.get("SplitPane.dividerSize"), new Integer(10));
     harness.check(defaults.get("SplitPaneDivider.border") instanceof Border);
     
     harness.checkPoint("TabbedPane");
     harness.check(defaults.get("TabbedPane.ancestorInputMap") instanceof InputMapUIResource);
     harness.check(defaults.get("TabbedPane.background"), new ColorUIResource(153, 153, 153));
     harness.check(defaults.get("TabbedPane.contentBorderInsets"), new InsetsUIResource(2, 2, 3, 3));
     harness.check(defaults.get("TabbedPane.focus"), new ColorUIResource(102, 102, 153));
     harness.check(defaults.get("TabbedPane.focusInputMap") instanceof InputMapUIResource);
     harness.check(defaults.get("TabbedPane.font"), new FontUIResource("Dialog", Font.BOLD, 12));
     harness.check(defaults.get("TabbedPane.light"), new ColorUIResource(204, 204, 204));
     harness.check(defaults.get("TabbedPane.selected"), new ColorUIResource(204, 204, 204));
     harness.check(defaults.get("TabbedPane.selectedTabPadInsets"), new InsetsUIResource(2, 2, 2, 1));
     harness.check(defaults.get("TabbedPane.selectHighlight"), new ColorUIResource(255, 255, 255));
     harness.check(defaults.get("TabbedPane.tabAreaBackground"), new ColorUIResource(204, 204, 204));
     harness.check(defaults.get("TabbedPane.tabAreaInsets"), new InsetsUIResource(4, 2, 0, 6));
     harness.check(defaults.get("TabbedPane.tabInsets"), new InsetsUIResource(0, 9, 1, 9));
     harness.check(defaults.get("TabbedPane.tabRunOverlay"), new Integer(2));
     harness.check(defaults.get("TabbedPane.textIconGap"), new Integer(4));
     
     harness.checkPoint("Table");
     harness.check(defaults.get("Table.ancestorInputMap") instanceof InputMapUIResource);
     harness.check(defaults.get("Table.ancestorInputMap.RightToLeft") instanceof InputMapUIResource);
     harness.check(defaults.get("Table.focusCellHighlightBorder") instanceof BorderUIResource.LineBorderUIResource);
     harness.check(defaults.get("Table.font"), new FontUIResource("Dialog", Font.PLAIN, 12));
     harness.check(defaults.get("Table.gridColor"), new ColorUIResource(153, 153, 153));
     harness.check(defaults.get("Table.scrollPaneBorder") instanceof MetalBorders.ScrollPaneBorder);
     
     harness.checkPoint("TableHeader");
 //    harness.check(defaults.get("TableHeader.cellBorder") instanceof MetalBorders.TableHeaderBorder);
     harness.check(defaults.get("TableHeader.font"), new FontUIResource("Dialog", Font.PLAIN, 12));
     
     harness.checkPoint("TextArea");
     harness.check(defaults.get("TextArea.border") instanceof BasicBorders.MarginBorder);
     harness.check(defaults.get("TextArea.caretBlinkRate"), new Integer(500));
     harness.check(defaults.get("TextArea.focusInputMap") instanceof InputMapUIResource);
     harness.check(defaults.get("TextArea.font"), new FontUIResource("Dialog", Font.PLAIN, 12));
     harness.check(defaults.get("TextArea.margin"), new InsetsUIResource(0, 0, 0, 0));
     
     harness.checkPoint("TextField");
     harness.check(defaults.get("TextField.border") instanceof BorderUIResource.CompoundBorderUIResource);
     harness.check(defaults.get("TextField.caretBlinkRate"), new Integer(500));
     harness.check(defaults.get("TextField.focusInputMap") instanceof InputMapUIResource);
     harness.check(defaults.get("TextField.font"), new FontUIResource("Dialog", Font.PLAIN, 12));
     harness.check(defaults.get("TextField.margin"), new InsetsUIResource(0, 0, 0, 0));
     
     harness.checkPoint("TextPane");
     harness.check(defaults.get("TextPane.border") instanceof BasicBorders.MarginBorder);
     harness.check(defaults.get("TextPane.caretBlinkRate"), new Integer(500));
     harness.check(defaults.get("TextPane.focusInputMap") instanceof InputMapUIResource);
     harness.check(defaults.get("TextPane.font"), new FontUIResource("Dialog", Font.PLAIN, 12));
     harness.check(defaults.get("TextPane.margin"), new InsetsUIResource(3, 3, 3, 3));
     
     harness.checkPoint("TitledBorder");
     harness.check(defaults.get("TitledBorder.border"), null);
     harness.check(defaults.get("TitledBorder.font"), new FontUIResource("Dialog", Font.BOLD, 12));
     harness.check(defaults.get("TitledBorder.titleColor"), new ColorUIResource(0, 0, 0));
     
     harness.checkPoint("ToggleButton");
     harness.check(defaults.get("ToggleButton.border") instanceof BorderUIResource.CompoundBorderUIResource);
     harness.check(defaults.get("ToggleButton.disabledText"), new ColorUIResource(153, 153, 153));
     harness.check(defaults.get("ToggleButton.focus"), new ColorUIResource(153, 153, 204));
     harness.check(defaults.get("ToggleButton.focusInputMap") instanceof InputMapUIResource);
     harness.check(defaults.get("ToggleButton.font"), new FontUIResource("Dialog", Font.BOLD, 12));
     harness.check(defaults.get("ToggleButton.margin"), new InsetsUIResource(2, 14, 2, 14));
     harness.check(defaults.get("ToggleButton.select"), new ColorUIResource(153, 153, 153));
     harness.check(defaults.get("ToggleButton.textIconGap"), new Integer(4));
     harness.check(defaults.get("ToggleButton.textShiftOffset"), new Integer(0));
     
     harness.checkPoint("ToolBar");
     harness.check(defaults.get("ToolBar.ancestorInputMap") instanceof InputMapUIResource);
     harness.check(defaults.get("ToolBar.background"), new ColorUIResource(204, 204, 204));
 //    harness.check(defaults.get("ToolBar.border") instanceof MetalBorders.ToolBarBorder);
     harness.check(defaults.get("ToolBar.dockingBackground"), new ColorUIResource(204, 204, 204));
     harness.check(defaults.get("ToolBar.dockingForeground"), new ColorUIResource(102, 102, 153));
     harness.check(defaults.get("ToolBar.floatingBackground"), new ColorUIResource(204, 204, 204));
     harness.check(defaults.get("ToolBar.floatingForeground"), new ColorUIResource(204, 204, 255));
     harness.check(defaults.get("ToolBar.font"), new FontUIResource("Dialog", Font.BOLD, 12));
     harness.check(defaults.get("ToolBar.foreground"), new ColorUIResource(0, 0, 0));
     harness.check(defaults.get("ToolBar.separatorSize"), new DimensionUIResource(10, 10));
     
     harness.checkPoint("ToolTip");
     LineBorderUIResource b2 = (LineBorderUIResource) defaults.get("ToolTip.border");
     harness.check(b2.getThickness(), 1);
     harness.check(b2.getLineColor(), new Color(102, 102, 153));
     b2 = (LineBorderUIResource) defaults.get("ToolTip.borderInactive");
     harness.check(b2 != null ? b2.getThickness() : 0, 1);
     harness.check(b2 != null ? b2.getLineColor() : null, new Color(102, 102, 102));
     harness.check(defaults.get("ToolTip.font"), new FontUIResource("Dialog", Font.PLAIN, 12));
     harness.check(defaults.get("ToolTip.hideAccelerator"), Boolean.FALSE);
     
     harness.checkPoint("Tree");
     harness.check(defaults.get("Tree.ancestorInputMap") instanceof InputMapUIResource);
     harness.check(defaults.get("Tree.changeSelectionWithFocus"), Boolean.TRUE);
     harness.check(defaults.get("Tree.closedIcon") instanceof MetalIconFactory.TreeFolderIcon);
     harness.check(defaults.get("Tree.collapsedIcon") instanceof MetalIconFactory.TreeControlIcon);
     harness.check(defaults.get("Tree.drawsFocusBorderAroundIcon"), Boolean.FALSE);
     harness.check(defaults.get("Tree.editorBorder") instanceof BorderUIResource.LineBorderUIResource);
     harness.check(defaults.get("Tree.expandedIcon") instanceof MetalIconFactory.TreeControlIcon);
     harness.check(defaults.get("Tree.focusInputMap") instanceof InputMapUIResource);
     harness.check(defaults.get("Tree.focusInputMap.RightToLeft") instanceof InputMapUIResource);
     harness.check(defaults.get("Tree.font"), new FontUIResource("Dialog", Font.PLAIN, 12));
     harness.check(defaults.get("Tree.hash"), new ColorUIResource(204, 204, 255));
     harness.check(defaults.get("Tree.leafIcon") instanceof MetalIconFactory.TreeLeafIcon);
     harness.check(defaults.get("Tree.leftChildIndent"), new Integer(7));
     harness.check(defaults.get("Tree.line"), new ColorUIResource(204, 204, 255));
     harness.check(defaults.get("Tree.openIcon") instanceof MetalIconFactory.TreeFolderIcon);
     harness.check(defaults.get("Tree.rightChildIndent"), new Integer(13));
     harness.check(defaults.get("Tree.rowHeight"), new Integer(0));
     harness.check(defaults.get("Tree.scrollsOnExpand"), Boolean.TRUE);
     harness.check(defaults.get("Tree.selectionBorderColor"), new ColorUIResource(153, 153, 204));
     harness.check(defaults.get("Tree.textBackground"), new ColorUIResource(255, 255, 255));
     
     harness.checkPoint("Viewport");
     harness.check(defaults.get("Viewport.font"), new FontUIResource("Dialog", Font.PLAIN, 12));
   }
 }
