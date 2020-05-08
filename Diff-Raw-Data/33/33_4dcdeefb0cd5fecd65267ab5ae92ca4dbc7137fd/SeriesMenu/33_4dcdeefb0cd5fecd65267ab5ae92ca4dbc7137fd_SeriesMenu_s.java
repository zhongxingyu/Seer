 /*
  * Copyright (c) 2013 Philippe VIENNE
  *
  * This file is a part of SpeleoGraph
  *
  * SpeleoGraph is free software: you can redistribute
  * it and/or modify it under the terms of the GNU General
  * Public License as published by the Free Software
  * Foundation, either version 3 of the License, or (at your
  * option) any later version.
  *
  * SpeleoGraph is distributed in the hope that it will
  * be useful, but WITHOUT ANY WARRANTY; without even the
  * implied warranty of MERCHANTABILITY or FITNESS FOR A
  * PARTICULAR PURPOSE.  See the GNU General Public License
  * for more details.
  *
  * You should have received a copy of the GNU General Public
  * License along with SpeleoGraph.
  * If not, see <http://www.gnu.org/licenses/>.
  */
 
 package org.cds06.speleograph.graph;
 
 import org.apache.commons.lang3.Validate;
 import org.cds06.speleograph.I18nSupport;
 import org.cds06.speleograph.SpeleoGraphApp;
 import org.cds06.speleograph.data.Series;
 import org.cds06.speleograph.data.Type;
 import org.jfree.data.general.DatasetChangeEvent;
 import org.jfree.data.general.DatasetChangeListener;
 import org.slf4j.LoggerFactory;
 
 import javax.swing.*;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 import java.awt.*;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.File;
 import java.util.HashMap;
 import java.util.List;
 
 /**
  * This class help to create a menu to edit the opened series.
  * <p>It can be used in a {@link javax.swing.JMenuBar} or as a simple {@link javax.swing.JPopupMenu}</p>
  *
  * @author Philippe VIENNE
  * @since 1.0
  */
 public class SeriesMenu implements DatasetChangeListener {
 
     private SpeleoGraphApp application;
 
     private JMenu menu = new JMenu("Séries");
     private HashMap<Series, JPopupMenu> menus = new HashMap<>(20);
     private List<Series> series = Series.getInstances();
 
     public SeriesMenu(SpeleoGraphApp app) {
         Validate.notNull(app);
         this.application = app;
         Series.addListener(this);
     }
 
     /**
      * Receives notification when a Series has been edited or the Series list has changed.
      *
      * @param event information about the event.
      */
     @Override
     public void datasetChanged(DatasetChangeEvent event) {
         menu.removeAll();
         for (Series s : series.toArray(new Series[series.size()])) {
            createPopupMenuForSeries(s);
            JPopupMenu m = menus.get(s);
             JMenu jMenu = new JMenu(s.getName());
            for (Component item : m.getComponents()) {
                 if (item instanceof JMenuItem || item instanceof JSeparator) {
                     jMenu.add(item);
                 }
             }
             menu.add(jMenu);
         }
         menu.setVisible(menu.getMenuComponentCount() > 0);
     }
 
    private void createPopupMenuForSeries(final Series series) {
 
        if (series == null) return;
 
         final JPopupMenu menu = new JPopupMenu(series.getName());
 
         menu.removeAll();
         final JMenuItem renameItem = new JMenuItem("Renommer la série");
         renameItem.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
 
                 menu.setVisible(false);
                 String newName = "";
                 while (newName != null && newName.equals("")) {
                     newName = (String) JOptionPane.showInputDialog(
                             application,
                             "Entrez un nouveau nom pour la série",
                             null,
                             JOptionPane.QUESTION_MESSAGE,
                             null,
                             null,
                             series.getName()
                     );
                 }
                 series.setName(newName);
 
             }
         });
         menu.add(renameItem);
 
         if (series.getType().equals(Type.WATER)) {
             JMenuItem samplingItem = new JMenuItem("Créer une série échantillonée");
             samplingItem.addActionListener(new ActionListener() {
                 @Override
                 public void actionPerformed(ActionEvent e) {
                     try {
                         boolean hasANumber = false;
                         int duration = 60 * 60 * 24;
                         while (!hasANumber)
                             try {
                                 String result = JOptionPane.showInputDialog(
                                         application,
                                         "Quel est la longueur de l'échantillonage (en secondes) ?",
                                         60 * 60 * 24);
                                 duration = Integer.parseInt(result);
                                 hasANumber = true;
                             } catch (NumberFormatException e1) {
                                 hasANumber = false;
                             }
                         boolean hasAName = false;
                         String name = "";
                         while (!hasAName)
                             try {
                                 name = JOptionPane.showInputDialog(
                                         application,
                                         "Quel est le nom de la nouvelle série ?",
                                         series.getName());
                                 hasAName = !"".equals(name);
                             } catch (Exception e1) {
                                 hasAName = false;
                             }
                         series.generateSampledSeries(1000 * duration).setName(name);
                     } catch (Exception e1) {
                         LoggerFactory.getLogger(SeriesMenu.class).error("Erreur lors de l'échantillonage", e1);
                     }
                 }
             });
             menu.add(samplingItem);
         }
 
         {
             JMenuItem samplingItem = new JMenuItem("Supprimer la série");
             samplingItem.addActionListener(new ActionListener() {
                 @Override
                 public void actionPerformed(ActionEvent e) {
                     if (JOptionPane.showConfirmDialog(
                             application,
                             "Etes-vous sur de vouloir supprimer cette série",
                             "Confirmation",
                             JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.OK_OPTION) {
                         series.delete();
                     }
                 }
             });
             menu.add(samplingItem);
         }
 
         {
             final JMenuItem up = new JMenuItem("Remonter dans la liste"),
                     down = new JMenuItem("Descendre dans la liste");
             ActionListener listener = new AbstractAction() {
                 @Override
                 public void actionPerformed(ActionEvent e) {
                     if (e.getSource().equals(up)) {
                         series.upSeriesInList();
                     } else {
                         series.downSeriesInList();
                     }
                 }
             };
             up.addActionListener(listener);
             down.addActionListener(listener);
             menu.addSeparator();
             if (series.isFirst()) {
                 menu.add(down);
             } else if (series.isLast()) {
                 menu.add(up);
             } else {
                 menu.add(up);
                 menu.add(down);
             }
             menu.addSeparator();
         }
 
         {
             JMenuItem colorItem = new JMenuItem("Couleur de la série");
             colorItem.addActionListener(new AbstractAction() {
                 @Override
                 public void actionPerformed(ActionEvent e) {
                     series.setColor(JColorChooser.showDialog(
                             application,
                             I18nSupport.translate("actions.selectColorForSeries"),
                             series.getColor()));
                 }
             });
             menu.add(colorItem);
         }
 
         {
             JMenu plotRenderer = new JMenu("Affichage de la série");
             final ButtonGroup modes = new ButtonGroup();
             java.util.List<DrawStyle> availableStyles;
             if (series.getType().isHighLowType()) {
                 availableStyles = DrawStyles.getDrawableStylesForHighLow();
             } else {
                 availableStyles = DrawStyles.getDrawableStyles();
             }
             for (final DrawStyle s : availableStyles) {
                 final JRadioButtonMenuItem item = new JRadioButtonMenuItem(
                         DrawStyles.getHumanCheckboxText(s)
                 );
                 item.addChangeListener(new ChangeListener() {
                     @Override
                     public void stateChanged(ChangeEvent e) {
                         if (item.isSelected())
                             series.setStyle(s);
                     }
                 });
                 modes.add(item);
                 if (s.equals(series.getStyle())) {
                     modes.setSelected(item.getModel(), true);
                 }
                 plotRenderer.add(item);
             }
             menu.add(plotRenderer);
         }
         menu.addSeparator();
         menu.add(new AbstractAction() {
 
             {
                 putValue(Action.NAME, "Fermer le fichier");
             }
 
             @Override
             public void actionPerformed(ActionEvent e) {
                 if (JOptionPane.showConfirmDialog(
                         application,
                         "Etes-vous sur de vouloir fermer toutes les séries du fichier ?",
                         "Confirmation",
                         JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.OK_OPTION) {
                     final File f = series.getOrigin();
                     for (final Series s : Series.getInstances().toArray(new Series[Series.getInstances().size()])) {
                         if (s.getOrigin().equals(f))
                             s.delete();
                     }
                 }
             }
         });
 
        this.menus.put(series, menu);
     }
 
     public JMenu getMenu() {
         return menu;
     }
 
     public JPopupMenu getPopupMenu(Series s) {
         if (!menus.containsKey(s)) {
             createPopupMenuForSeries(s);
         }
         return menus.get(s);
     }
 }
