 /*
  * Geotools - OpenSource mapping toolkit
  * (C) 2001, Institut de Recherche pour le Dveloppement
  * (C) 1999, Pches et Ocans Canada
  *
  *    This library is free software; you can redistribute it and/or
  *    modify it under the terms of the GNU Lesser General Public
  *    License as published by the Free Software Foundation; either
  *    version 2.1 of the License, or (at your option) any later version.
  *
  *    This library is distributed in the hope that it will be useful,
  *    but WITHOUT ANY WARRANTY; without even the implied warranty of
  *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  *    Lesser General Public License for more details.
  *
  *    You should have received a copy of the GNU Lesser General Public
  *    License along with this library; if not, write to the Free Software
  *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
  *
  *
  * Contacts:
  *     UNITED KINGDOM: James Macgill
  *             mailto:j.macgill@geog.leeds.ac.uk
  *
  *     FRANCE: Surveillance de l'Environnement Assiste par Satellite
  *             Institut de Recherche pour le Dveloppement / US-Espace
  *             mailto:seasnet@teledetection.fr
  *
  *     CANADA: Observatoire du Saint-Laurent
  *             Institut Maurice-Lamontagne
  *             mailto:osl@osl.gc.ca
  */
 package org.geotools.gui.swing;
 
 // J2SE dependencies
 import java.awt.Font;
 import java.awt.Frame;
 import java.awt.Toolkit;
 import java.awt.Component;
 import java.awt.Dimension;
 import java.awt.EventQueue;
 import java.awt.GridLayout;
 import java.awt.BorderLayout;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JDialog;
 import javax.swing.JTextArea;
 import javax.swing.JComponent;
 import javax.swing.JScrollPane;
 import javax.swing.JOptionPane;
 import javax.swing.JProgressBar;
 import javax.swing.JLayeredPane;
 import javax.swing.JDesktopPane;
 import javax.swing.BorderFactory;
 import javax.swing.JInternalFrame;
 import javax.swing.BoundedRangeModel;
 import java.lang.reflect.InvocationTargetException;
 
 // Geotools dependencies
 import org.geotools.util.ProgressListener;
 import org.geotools.resources.Utilities;
import org.geotools.resources.SwingUtilities;
 import org.geotools.resources.gui.Resources;
 import org.geotools.resources.gui.ResourceKeys;
 
 
 /**
  * Informe l'utilisateur des progrs d'une opration  l'aide de messages dans une fentre.
  * Cette classe peut aussi crire des avertissements, ce qui est utile entre autre lors de la
  * lecture d'un fichier de donnes durant laquelle on veut signaler les erreurs mais sans arrter
  * la lecture pour autant.
  *
  * <p>&nbsp;</p>
  * <p align="center"><img src="doc-files/ProgressWindow.png"></p>
  * <p>&nbsp;</p>
  *
  * <p>Cette classe est conue pour fonctionner correctement mme si ses mthodes sont appelles
  * dans un autre thread que celui de <i>Swing</i>. Il est donc possible de faire la longue
  * opration en arrire plan et d'appeller les mthodes de cette classe sans se soucier des
  * problmes de synchronisation. En gnral, faire l'opration en arrire plan est recommand
  * afin de permettre le rafraichissement de l'cran par <i>Swing</i>.</p>
  *
 * @version $Id: ProgressWindow.java,v 1.2 2003/03/28 14:28:12 desruisseaux Exp $
  * @author Martin Desruisseaux
  */
 public class ProgressWindow implements ProgressListener {
     /**
      * Largeur initiale de la fentre des progrs, en pixels.
      */
     private static final int WIDTH = 360;
 
     /**
      * Hauteur initiale de la fentre des progrs, en pixels.
      */
     private static final int HEIGHT = 120;
 
     /**
      * Hauteur de la zone de texte qui contiendra des messages d'avertissements.
      */
     private static final int WARNING_HEIGHT = 120;
 
     /**
      * Largeur de la marge horizontale, en pixels.
      */
     private static final int HMARGIN = 12;
 
     /**
      * Largeur de la marge verticale, en pixels.
      */
     private static final int VMARGIN = 9;
 
     /**
      * Nombre d'espaces  placer dans la marge de
      * la fentre contenant les messages d'erreurs.
      */
     private static final int WARNING_MARGIN = 8;
 
     /**
      * Fentre affichant les progrs de la longue opration.
      * Il peut s'agir notamment d'un objet {@link JDialog} ou
      * d'un objet {@link JInternalFrame}, dpendamment de la
      * composante parente.
      */
     private final Component window;
 
     /**
      * Conteneur dans lequel insrer les lments tels que
      * la barre des progrs. Ca peut tre le mme objet que
      * {@link #window}, mais pas ncessairement.
      */
     private final JComponent content;
 
     /**
      * Barre des progrs. La plage de cette barre doit
      * obligatoirement aller au moins de 0  100.
      */
     private final JProgressBar progressBar;
 
     /**
      * Description de l'opration en cours. Des exemples de descriptions
      * seraient "Lecture de l'en-tte" ou "Lecture des donnes".
      */
     private final JLabel description;
 
     /**
      * Rgion dans laquelle afficher les messages d'avertissements.
      * Cet objet doit tre de la classe {@link JTextArea}. il ne sera
      * toutefois construit que si des erreurs surviennent effectivement.
      */
     private JComponent warningArea;
 
     /**
      * Source du dernier message d'avertissement. Cette information est
      * conserve afin d'viter de rpter la source lors d'ventuels
      * autres messages d'avertissements.
      */
     private String lastSource;
     
     /**
      * Construit une fentre qui informera des progrs d'une opration.
      * La fentre n'apparatra pas imdiatement. Elle n'apparatra que
      * lorsque la mthode {@link #started} sera appele.
      *
      * @param parent Composante parente. La fentre des progrs sera
      *        construite dans le mme cadre que cette composante. Ce
      *        paramtre peut tre nul s'il n'y a pas de parent.
      */
     public ProgressWindow(final Component parent) {
         /*
          * Cration de la fentre qui contiendra
          * les composantes affichant le progrs.
          */
         Dimension       parentSize;
         final Resources  resources = Resources.getResources(parent!=null ? parent.getLocale() : null);
         final String         title = resources.getString(ResourceKeys.PROGRESSION);
         final JDesktopPane desktop = JOptionPane.getDesktopPaneForComponent(parent);
         if (desktop != null) {
             final JInternalFrame frame = new JInternalFrame(title);
             window                     = frame;
             content                    = new JPanel();
             parentSize                 = desktop.getSize();
             frame.setContentPane(content); // Pour avoir un fond opaque
             frame.setDefaultCloseOperation(JInternalFrame.HIDE_ON_CLOSE);
             desktop.add(frame, JLayeredPane.PALETTE_LAYER);
         } else {
             final Frame frame    = JOptionPane.getFrameForComponent(parent);
             final JDialog dialog = new JDialog(frame, title);
             window               = dialog;
             content              = (JComponent) dialog.getContentPane();
             parentSize           = frame.getSize();
             if (parentSize.width==0 || parentSize.height==0) {
                 parentSize=Toolkit.getDefaultToolkit().getScreenSize();
             }
             dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
             dialog.setResizable(false);
         }
         window.setBounds((parentSize.width-WIDTH)/2, (parentSize.height-HEIGHT)/2, WIDTH, HEIGHT);
         /*
          * Cration de l'tiquette qui dcrira l'opration
          * en cours. Au dpart, aucun texte ne sera plac
          * dans cette tiquette.
          */
         description = new JLabel();
         description.setHorizontalAlignment(JLabel.CENTER);
         /*
          * Procde  la cration de la barre des progrs.
          * Le modle de cette barre sera retenu pour tre
          */
         progressBar = new JProgressBar();
         progressBar.setIndeterminate(true);
         progressBar.setBorder(BorderFactory.createCompoundBorder(
                               BorderFactory.createEmptyBorder(6,9,6,9),
                               progressBar.getBorder()));
         /*
          * Dispose les lments  l'intrieur de la fentre.
          * On leur donnera une bordure vide pour laisser un
          * peu d'espace entre eux et les bords de la fentre.
          */
         content.setLayout(new GridLayout(2,1));
         content.setBorder(BorderFactory.createCompoundBorder(
                           BorderFactory.createEmptyBorder(VMARGIN,HMARGIN,VMARGIN,HMARGIN),
                           BorderFactory.createEtchedBorder()));
         content.add(description);
         content.add(progressBar);
     }
 
     /**
      * Returns a localized string for the specified key.
      */
     private String getString(final int key) {
         return Resources.getResources(window.getLocale()).getString(key);
     }
 
     /**
      * Retourne le titre de la fentre. Il s'agira en gnral
      * du titre de la bote de dialogue. Par dfaut, ce titre
      * sera "Progression" dans la langue de l'utilisateur.
      */
     public String getTitle() {
         return get(Caller.TITLE);
     }
 
     /**
      * Dfinit le titre de la fentre des progrs. Un argument
      * nul rtablira le titre par dfaut de la fentre.
      */
     public void setTitle(final String name) {
         set(Caller.TITLE, (name!=null) ? name : getString(ResourceKeys.PROGRESSION));
     }
 
     /**
      * Retourne le message d'crivant l'opration
      * en cours. Si aucun message n'a t dfinie,
      * retourne <code>null</code>.
      */
     public String getDescription() {
         return get(Caller.LABEL);
     }
 
     /**
      * Spcifie un message qui dcrit l'opration en cours.
      * Ce message est typiquement spcifie avant le dbut
      * de l'opration. Toutefois, cette mthode peut aussi
      * tre appele  tout moment pendant l'opration sans
      * que cela affecte le pourcentage accompli. La valeur
      * <code>null</code> signifie qu'on ne souhaite plus
      * afficher de description.
      */
     public void setDescription(final String description) {
         set(Caller.LABEL, description);
     }
 
     /**
      * Indique que l'opration a commence. L'appel de cette
      * mthode provoque l'apparition de la fentre si elle
      * n'tait pas dj visible.
      */
     public void started() {
         call(Caller.STARTED);
     }
 
     /**
      * Indique l'tat d'avancement de l'opration. Le progrs est reprsent par un
      * pourcentage variant de 0  100 inclusivement. Si la valeur spcifie est en
      * dehors de ces limites, elle sera automatiquement ramene entre 0 et 100.
      */
     public void progress(final float percent) {
         int p=(int) percent; // round toward 0
         if (p<  0) p=  0;
         if (p>100) p=100;
         set(Caller.PROGRESS, p);
     }
 
     /**
      * Indique que l'opration est termine. L'indicateur visuel informant des
      * progrs disparatra, sauf si des messages d'erreurs ou d'avertissements
      * ont t affichs.
      */
     public void complete() {
         call(Caller.COMPLETE);
     }
 
     /**
      * Libre les ressources utilises par l'tat d'avancement. Si l'tat
      * d'avancement tait affiche dans une fentre, cette fentre peut tre
      * dtruite.
      */
     public void dispose() {
         call(Caller.DISPOSE);
     }
 
     /**
      * crit un message d'avertissement. Les messages apparatront dans
      * une zone de texte sous la barre des progrs. Cette zone de texte
      * ne deviendra visible qu'aprs l'criture d'au moins un message.
      *
      * @param source Chane de caractre dcrivant la source de l'avertissement.
      *        Il s'agira par exemple du nom du fichier dans lequel une anomalie
      *        a t dtecte. Peut tre nul si la source n'est pas connue.
      * @param margin Texte  placer dans la marge de l'avertissement <code>warning</code>,
      *        ou <code>null</code> s'il n'y en a pas. Il s'agira le plus souvent du numro
      *        de ligne o s'est produite l'erreur dans le fichier <code>source</code>. Ce
      *        texte sera automatiquement plac entre parenthses.
      * @param warning Message d'avertissement  crire.
      */
     public synchronized void warningOccurred(final String source, String margin,
                                              final String warning)
     {
        final StringBuffer buffer = new StringBuffer(warning.length()+16);
         if (source != lastSource) {
             lastSource = source;
             if (warningArea != null) {
                 buffer.append('\n');
             }
             buffer.append(source!=null ? source : getString(ResourceKeys.UNTITLED));
             buffer.append('\n');
         }
         int wm = WARNING_MARGIN;
         if (margin != null) {
             margin = trim(margin);
             if (margin.length() != 0) {
                 wm -= (margin.length()+3);
                 buffer.append(Utilities.spaces(wm));
                 buffer.append('(');
                 buffer.append(margin);
                 buffer.append(')');
                 wm = 1;
             }
         }
         buffer.append(Utilities.spaces(wm));
         buffer.append(warning);
         if (buffer.charAt(buffer.length()-1) != '\n') {
             buffer.append('\n');
         }
         set(Caller.WARNING, buffer.toString());
     }
 
     /**
      * Indique qu'une exception est survenue pendant le traitement de l'opration.
      * L'implmentation par dfaut fait apparatre le message de l'exception dans
      * une fentre spare.
      */
     public void exceptionOccurred(final Throwable exception) {
         ExceptionMonitor.show(window, exception);
     }
 
     /**
      * Retourne la chane <code>margin</code> sans les
      * ventuelles parenthses qu'elle pourrait avoir
      * de part et d'autre.
      */
     private static String trim(String margin) {
         margin = margin.trim();
         int lower = 0;
         int upper = margin.length();
         while (lower<upper && margin.charAt(lower+0)=='(') lower++;
         while (lower<upper && margin.charAt(upper-1)==')') upper--;
         return margin.substring(lower, upper);
     }
 
     /**
      * Interroge une des composantes de la bote des progrs.
      * L'interrogation sera faite dans le thread de <i>Swing</i>.
      *
      * @param  task Information dsire. Ce code doit tre une
      *         des constantes telles que {@link Caller#TITLE}
      *         ou {@link Caller#LABEL}.
      * @return L'information demande.
      */
     private String get(final int task) {
         final Caller caller = new Caller(-task);
        SwingUtilities.invokeAndWait(caller);
         return caller.text;
     }
 
     /**
      * Modifie l'tat d'une des composantes de la bote des progrs.
      * La modification sera faite dans le thread de <i>Swing</i>.
      *
      * @param  task Information  modifier. Ce code doit tre une
      *         des constantes telles que {@link Caller#TITLE}
      *         ou {@link Caller#LABEL}.
      * @param  text Le nouveau texte.
      */
     private void set(final int task, final String text) {
         final Caller caller = new Caller(task);
         caller.text = text;
         EventQueue.invokeLater(caller);
     }
 
     /**
      * Modifie l'tat d'une des composantes de la bote des progrs.
      * La modification sera faite dans le thread de <i>Swing</i>.
      *
      * @param  task Information  modifier. Ce code doit tre une
      *         des constantes telles que {@link Caller#PROGRESS}.
      * @param  value Nouvelle valeur  affecter  la composante.
      */
     private void set(final int task, final int value) {
         final Caller caller = new Caller(task);
         caller.value = value;
         EventQueue.invokeLater(caller);
     }
 
     /**
      * Appelle une mthode <i>Swing</i> sans argument.
      * @param  task Mthode  appeler. Ce code doit tre une
      *         des constantes telles que {@link Caller#STARTED}
      *         ou {@link Caller#DISPOSE}.
      */
     private void call(final int task) {
         EventQueue.invokeLater(new Caller(task));
     }
 
     /**
      * Tche  excuter dans le thread de <i>Swing</i> pour interroger
      * ou modifier l'tat d'une composante. Cette tache est destine  tre appele par
      * les mthodes {@link EventQueue#invokeLater} et {@link EventQueue#invokeAndWait}.
      * Les tches possibles sont dsignes par des constantes telles que {@link #TITLE}
      * et {@link #LABEL}. Une valeur positive signifie que l'on modifie l'tat de cette
      * composante (dans ce cas, il faut d'abord avoir affect une valeur  {@link #text}),
      * tandis qu'une valeur ngative signifie que l'on interroge l'tat de la comosante
      * (dans ce cas, il faudra extrait l'tat du champ {@link #text}).
      *
     * @version $Id: ProgressWindow.java,v 1.2 2003/03/28 14:28:12 desruisseaux Exp $
      * @author Martin Desruisseaux
      */
     private class Caller implements Runnable {
         /**
          * Constante indiquant que l'on souhaite interroger
          * ou modifier le titre de la bote des progrs.
          */
         public static final int TITLE=1;
 
         /**
          * Constante indiquant que l'on souhaite interroger
          * ou modifier la description des progrs.
          */
         public static final int LABEL=2;
 
         /**
          * Constante indiquant que l'on souhaite modifier
          * la valeur de la barre des progrs.
          */
         public static final int PROGRESS=3;
 
         /**
          * Constante indiquant que l'on souhaite
          * faire apparatre un avertissement.
          */
         public static final int WARNING=4;
 
         /**
          * Constante indiquant que l'on souhaite
          * faire apparatre la bote des progrs.
          */
         public static final int STARTED=5;
 
         /**
          * Constante indiquant que l'on souhaite
          * faire disparatre la bote des progrs.
          */
         public static final int COMPLETE=6;
 
         /**
          * Constante indiquant que l'on souhaite
          * faire disparatre la bote des progrs.
          */
         public static final int DISPOSE=7;
 
         /**
          * Constante indiquant la tche que l'on souhaite effectuer. Il doit s'agir
          * d'une valeur telle que {@link #TITLE} et {@link #LABEL}, ainsi que leurs
          * valeurs ngatives.
          */
         private final int task;
 
         /**
          * Valeur  affecter ou valeur retourne. Pour des valeurs positives de {@link #task},
          * il s'agit de la valeur  affecter  une composante. Pour des valeurs ngatives de
          * {@link #task}, il s'agit de la valeur retourne par une composante.
          */
         public String text;
 
         /**
          * Valeur  affecter  la barre des progrs.
          */
         public int value;
 
         /**
          * Construit un objet qui effectura la tche identifie par la constante <code>task</code>.
          * Cette constantes doit tre une valeur telle que {@link #TITLE} et {@link #LABEL}, ou une
          * de leurs valeurs ngatives.
          */
         public Caller(final int task) {
             this.task = task;
         }
 
         /**
          * Excute la tche identifie par la constante {@link #task}.
          */
         public void run() {
             final BoundedRangeModel model = progressBar.getModel();
             switch (task) {
                 case   +LABEL: description.setText(text);  return;
                 case   -LABEL: text=description.getText(); return;
                 case PROGRESS: model.setValue(value); progressBar.setIndeterminate(false); return;
                 case  STARTED: model.setRangeProperties(  0,1,0,100,false); window.setVisible(true); break;
                 case COMPLETE: model.setRangeProperties(100,1,0,100,false); window.setVisible(warningArea!=null); break;
             }
             synchronized (ProgressWindow.this) {
                 if (window instanceof JDialog) {
                     final JDialog window = (JDialog) ProgressWindow.this.window;
                     switch (task) {
                         case   +TITLE: window.setTitle(text);  return;
                         case   -TITLE: text=window.getTitle(); return;
                         case  STARTED: window.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE); return;
                         case COMPLETE: window.setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);       return;
                         case  DISPOSE: window.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
                                        if (warningArea==null || !window.isVisible()) window.dispose();
                                        return;
                     }
                 } else {
                     final JInternalFrame window = (JInternalFrame) ProgressWindow.this.window;
                     switch (task) {
                         case   +TITLE: window.setTitle(text);     return;
                         case   -TITLE: text=window.getTitle();    return;
                         case  STARTED: window.setClosable(false); return;
                         case COMPLETE: window.setClosable(true);  return;
                         case  DISPOSE: window.setDefaultCloseOperation(JInternalFrame.DISPOSE_ON_CLOSE);
                                        if (warningArea==null || !window.isVisible()) window.dispose();
                                        return;
                     }
                 }
                 /*
                  * Si la tche spcifie n'est aucune des tches numres ci-haut,
                  * on supposera que l'on voulait afficher un message d'avertissement.
                  */
                 if (warningArea == null) {
                     final JTextArea     warningArea = new JTextArea();
                     final JScrollPane        scroll = new JScrollPane(warningArea);
                     final JPanel              panel = new JPanel(new BorderLayout());
                     final JPanel              title = new JPanel(new BorderLayout());
                     ProgressWindow.this.warningArea = warningArea;
                     warningArea.setFont(Font.getFont("Monospaced"));
                     warningArea.setEditable(false);
                     title.setBorder(BorderFactory.createEmptyBorder(0,HMARGIN,VMARGIN,HMARGIN));
                     panel.add(content,                                     BorderLayout.NORTH);
                     title.add(new JLabel(getString(ResourceKeys.WARNING)), BorderLayout.NORTH );
                     title.add(scroll,                                      BorderLayout.CENTER);
                     panel.add(title,                                       BorderLayout.CENTER);
                     if (window instanceof JDialog) {
                         final JDialog window = (JDialog) ProgressWindow.this.window;
                         window.setContentPane(panel);
                         window.setResizable(true);
                     } else {
                         final JInternalFrame window = (JInternalFrame) ProgressWindow.this.window;
                         window.setContentPane(panel);
                         window.setResizable(true);
                     }
                     window.setSize(WIDTH, HEIGHT+WARNING_HEIGHT);
                    window.setVisible(true); // Seems required in order to force relayout.
                 }
                 final JTextArea warningArea=(JTextArea) ProgressWindow.this.warningArea;
                 warningArea.append(text);
             }
         }
     }
 }
