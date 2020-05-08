 /**
  * 
  */
 package javax.swing.origamist;
 
 import java.awt.Component;
 import java.awt.Container;
 import java.awt.Dialog;
 import java.awt.FlowLayout;
 import java.awt.Frame;
 import java.awt.Window;
 import java.awt.event.ActionEvent;
 import java.awt.event.FocusAdapter;
 import java.awt.event.FocusEvent;
 import java.awt.event.KeyAdapter;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.util.HashMap;
 import java.util.LinkedHashMap;
 import java.util.Map.Entry;
 import java.util.ResourceBundle;
 
 import javax.swing.AbstractAction;
 import javax.swing.AbstractButton;
 import javax.swing.Action;
 import javax.swing.BorderFactory;
 import javax.swing.ButtonGroup;
 import javax.swing.JButton;
 import javax.swing.JDialog;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JRadioButton;
 import javax.swing.JTextField;
 import javax.swing.UIManager;
 import javax.swing.event.DocumentEvent;
 
 import com.jgoodies.forms.layout.CellConstraints;
 import com.jgoodies.forms.layout.FormLayout;
 
 import cz.cuni.mff.peckam.java.origamist.math.AngleUnit;
 import cz.cuni.mff.peckam.java.origamist.math.MathHelper;
 import cz.cuni.mff.peckam.java.origamist.services.ServiceLocator;
 import cz.cuni.mff.peckam.java.origamist.services.interfaces.ConfigurationManager;
 import cz.cuni.mff.peckam.java.origamist.utils.UniversalDocumentListener;
 
 /**
  * A dialog for selecting angles.
  * 
  * @author Martin Pecka
  */
 public class AngleSelectionDialog extends JDialog
 {
 
     /** */
     private static final long               serialVersionUID = 3740375500491682514L;
 
     /** The currently selected unit. */
     public static final String              UNIT_PROPERTY    = "unit";
 
     /** The message to display. */
     protected Object                        message          = "";
 
     /** The selected angle. */
     protected Double                        angle            = null;
 
     /** The value (in radians) of the input field, if any. */
     protected Double                        customValue      = null;
 
     /** The selected unit. */
     protected AngleUnit                     unit             = null;
 
     /** The default unit. */
     protected AngleUnit                     defaultUnit;
 
     /** The angles that will be suggested as radio buttons (in radians). */
     protected final Double[]                suggestedAngles  = new Double[] { 0d, Math.PI / 2d, Math.PI };
 
     /** The default value. */
     protected Double                        defaultValue;
 
     /** The text field for inputting custom angles. */
     protected JTextField                    inputField;
 
     /** The label that shows custom angle units after the input field. */
     protected JLabel                        afterInputLabel;
 
     /** Group for angle unit radio buttons. */
     protected ButtonGroup                   angleUnitGroup;
 
     /** Angle unit radio button. */
     protected JRadioButton                  degreesBtn, radsBtn, gradsBtn;
 
     /** Group for angle values. */
     protected ButtonGroup                   valuesGroup;
 
     /** The radio buttons for suggested values. Keys are the suggested angles in radians. */
     protected HashMap<Double, JRadioButton> suggestedValues  = new LinkedHashMap<Double, JRadioButton>(
                                                                      suggestedAngles.length);
 
     /** The radio button for custom angle selection. */
     protected JRadioButton                  customAngle;
 
     /** Buttons for submitting the dialog. */
     protected JButton                       okButton, cancelButton;
 
     /** If the dialog has been closed with the OK button, this is true. */
     protected boolean                       closedByOKButton = false;
 
     /** The resource bundle this class can use. */
     protected ResourceBundle                messages         = ResourceBundle.getBundle(getClass().getName(),
                                                                      ServiceLocator.get(ConfigurationManager.class)
                                                                              .get().getLocale());
 
     /**
      * Create a dialog with the defaults set to 90 degrees.
      * 
      * @param owner The owner of this dialog.
      * @param message The message to display.
      * @param title Title of the dialog window.
      */
     public AngleSelectionDialog(Window owner, Object message, String title)
     {
         this(owner, message, title, AngleUnit.DEGREE, 90d);
     }
 
     /**
      * @param owner The owner of this dialog.
      * @param message The message to display.
      * @param title Title of the dialog window.
      * @param defaultUnit The default unit. If <code>null</code>, degrees will be selected.
      * @param defaultValue The default value. If <code>null</code>, the custom field will be focused and empty.
      */
     public AngleSelectionDialog(Window owner, Object message, String title, AngleUnit defaultUnit, Double defaultValue)
     {
         super(owner, title, Dialog.ModalityType.APPLICATION_MODAL);
         this.message = message;
         this.defaultValue = defaultValue;
         this.defaultUnit = defaultUnit;
         init();
     }
 
     /**
      * Create a dialog with the defaults set to 90 degrees.
      * 
      * @param owner The owner of this dialog.
      * @param message The message to display.
      * @param title Title of the dialog window.
      */
     public AngleSelectionDialog(Frame owner, Object message, String title)
     {
         this(owner, message, title, AngleUnit.DEGREE, 90d);
     }
 
     /**
      * @param owner The owner of this dialog.
      * @param message The message to display.
      * @param title Title of the dialog window.
      * @param defaultUnit The default unit. If <code>null</code>, degrees will be selected.
      * @param defaultValue The default value. If <code>null</code>, the custom field will be focused and empty.
      */
     public AngleSelectionDialog(Frame owner, Object message, String title, AngleUnit defaultUnit, Double defaultValue)
     {
         super(owner, title, Dialog.ModalityType.APPLICATION_MODAL);
         this.message = message;
         this.defaultValue = defaultValue;
         this.defaultUnit = defaultUnit;
         init();
     }
 
     /**
      * Create a dialog with the defaults set to 90 degrees.
      * 
      * @param owner The owner of this dialog.
      * @param message The message to display.
      * @param title Title of the dialog window.
      */
     public AngleSelectionDialog(Dialog owner, Object message, String title)
     {
         this(owner, message, title, AngleUnit.DEGREE, 90d);
     }
 
     /**
      * @param owner The owner of this dialog.
      * @param message The message to display.
      * @param title Title of the dialog window.
      * @param defaultUnit The default unit. If <code>null</code>, degrees will be selected.
      * @param defaultValue The default value. If <code>null</code>, the custom field will be focused and empty.
      */
     public AngleSelectionDialog(Dialog owner, Object message, String title, AngleUnit defaultUnit, Double defaultValue)
     {
         super(owner, title, Dialog.ModalityType.APPLICATION_MODAL);
         this.message = message;
         this.defaultValue = defaultValue;
         this.defaultUnit = defaultUnit;
         init();
     }
 
     /**
      * Create a dialog with the defaults set to 90 degrees.
      * 
      * @param message The message to display.
      * @param title Title of the dialog window.
      */
     public AngleSelectionDialog(Object message, String title)
     {
         this(message, title, AngleUnit.DEGREE, 90d);
     }
 
     /**
      * @param message The message to display.
      * @param title Title of the dialog window.
      * @param defaultUnit The default unit. If <code>null</code>, degrees will be selected.
      * @param defaultValue The default value. If <code>null</code>, the custom field will be focused and empty.
      */
     public AngleSelectionDialog(Object message, String title, AngleUnit defaultUnit, Double defaultValue)
     {
         super((Frame) null, title, true);
         this.message = message;
         this.defaultValue = defaultValue;
         this.defaultUnit = defaultUnit != null ? defaultUnit : AngleUnit.DEGREE;
         init();
     }
 
     /**
      * Initialize the dialog.
      */
     protected void init()
     {
         createComponents();
         buildLayout();
 
         setResizable(false);
 
         if (angleUnitGroup.getSelection() != null)
             angleUnitGroup.setSelected(angleUnitGroup.getSelection(), false);
 
         AngleUnit preferredUnit = ServiceLocator.get(ConfigurationManager.class).get().getPreferredAngleUnit();
         if (preferredUnit == null || preferredUnit == AngleUnit.DEGREE)
             degreesBtn.doClick();
         else if (preferredUnit == AngleUnit.GRAD)
             gradsBtn.doClick();
         else
             radsBtn.doClick();
 
         if (defaultValue == null) {
             customAngle.setSelected(true);
         } else {
             Double value = defaultUnit.convertTo(defaultValue, AngleUnit.RAD);
             if (suggestedValues.get(value) == null) {
                 customAngle.setSelected(true);
                 inputField.setText(unit.getNiceValue(defaultValue));
             } else {
                 suggestedValues.get(value).doClick();
             }
             inputField.requestFocusInWindow();
         }
 
         addWindowListener(new WindowAdapter() {
             @Override
             public void windowClosing(WindowEvent e)
             {
                 if (!closedByOKButton)
                     new CancelAction().actionPerformed(new ActionEvent(AngleSelectionDialog.this, 0, "cancel"));
             }
         });
 
         pack();
         setLocationRelativeTo(null);
     }
 
     /**
      * Initialize Swing components.
      */
     protected void createComponents()
     {
 
         KeyListener keyListener = new KeyAdapter() {
             @Override
             public void keyPressed(KeyEvent e)
             {
                 if (e.getKeyCode() == KeyEvent.VK_ENTER)
                     okButton.doClick();
                 else if (e.getKeyCode() == KeyEvent.VK_ESCAPE)
                     cancelButton.doClick();
             }
         };
 
         angleUnitGroup = new ButtonGroup();
 
         degreesBtn = new JRadioButton(new AngleUnitAction(AngleUnit.DEGREE));
         angleUnitGroup.add(degreesBtn);
         degreesBtn.addKeyListener(new FocusingKeyListener(degreesBtn));
 
         radsBtn = new JRadioButton(new AngleUnitAction(AngleUnit.RAD));
         angleUnitGroup.add(radsBtn);
         radsBtn.addKeyListener(new FocusingKeyListener(radsBtn));
 
         gradsBtn = new JRadioButton(new AngleUnitAction(AngleUnit.GRAD));
         angleUnitGroup.add(gradsBtn);
         gradsBtn.addKeyListener(new FocusingKeyListener(gradsBtn));
 
         valuesGroup = new ButtonGroup();
         for (Double angle : suggestedAngles) {
             final JRadioButton btn = new JRadioButton(new SuggestedAngleAction(angle));
             btn.addKeyListener(keyListener);
             btn.addKeyListener(new FocusingKeyListener(btn));
             suggestedValues.put(angle, btn);
             valuesGroup.add(btn);
         }
 
         customAngle = new JRadioButton(new CustomAngleAction());
         valuesGroup.add(customAngle);
         customAngle.setFocusable(false);
 
         inputField = new JTextField(10);
         inputField.getDocument().addDocumentListener(new UniversalDocumentListener() {
             @Override
             protected void update(DocumentEvent e)
             {
                 String text = inputField.getText();
                 try {
                     customValue = unit.convertTo(unit.parseValue(text), AngleUnit.RAD);
                 } catch (NumberFormatException ex) {
                     customValue = null;
                 }
             }
         });
         inputField.addFocusListener(new FocusAdapter() {
             @Override
             public void focusGained(FocusEvent e)
             {
                 if (inputField.getText().length() == 0 && valuesGroup.getSelection() != null
                         && valuesGroup.getSelection() != customAngle.getModel() && angle != null) {
                     inputField.setText(unit.getNiceValue(AngleUnit.RAD.convertTo(angle, unit)));
                     pack();
                 }
 
                 if (valuesGroup.getSelection() != customAngle.getModel()) {
                     customAngle.doClick();
                     inputField.requestFocusInWindow();
                 }
             }
         });
         inputField.addKeyListener(keyListener);
         addPropertyChangeListener(UNIT_PROPERTY, new PropertyChangeListener() {
             @Override
             public void propertyChange(PropertyChangeEvent evt)
             {
                 if (customValue != null) {
                     double newVal = AngleUnit.RAD.convertTo(customValue, unit);
                     if (Math.abs(Math.rint(newVal) - newVal) < MathHelper.EPSILON)
                         newVal = Math.rint(newVal);
                     inputField.setText(unit.getNiceValue(newVal));
                     pack();
                 }
             }
         });
 
         afterInputLabel = new JLabel();
         addPropertyChangeListener(UNIT_PROPERTY, new PropertyChangeListener() {
             @Override
             public void propertyChange(PropertyChangeEvent evt)
             {
                 afterInputLabel.setText(unit.getUnit());
                 pack();
             }
         });
 
         okButton = new JButton(new OKAction());
 
         cancelButton = new JButton(new CancelAction());
     }
 
     /**
      * Add the Swing components to a layout.
      */
     protected void buildLayout()
     {
         Container content = getContentPane();
         content.setLayout(new FormLayout("pref", "pref,$lgap,pref,$lgap,pref"));
 
         CellConstraints cc = new CellConstraints();
 
         JPanel angleUnits = new JPanel();
         content.add(angleUnits, cc.xy(1, 1));
         angleUnits.setBorder(BorderFactory.createTitledBorder(messages.getString("units.title")));
 
         angleUnits.add(degreesBtn);
         angleUnits.add(radsBtn);
         angleUnits.add(gradsBtn);
 
         JPanel angleValues = new JPanel(new FormLayout("pref", "pref,$lgap,pref"));
         content.add(angleValues, cc.xy(1, 3));
         angleValues.setBorder(BorderFactory.createTitledBorder(messages.getString("values.title")));
 
         JPanel suggestedValues = new JPanel();
         angleValues.add(suggestedValues, cc.xy(1, 1));
 
         for (Entry<Double, JRadioButton> e : this.suggestedValues.entrySet())
             suggestedValues.add(e.getValue());
 
         JPanel customValue = new JPanel();
         angleValues.add(customValue, cc.xy(1, 3));
 
         customValue.add(customAngle);
         customValue.add(inputField);
         customValue.add(afterInputLabel);
 
         JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
         content.add(buttonsPanel, cc.xy(1, 5));
         buttonsPanel.add(okButton);
         buttonsPanel.add(cancelButton);
     }
 
     /**
      * Show the dialog, wait for a user action, and then return the seleted angle.
      * 
      * @return The selected angle, or <code>null</code> if no angle was specified or the dialog was cancelled.
      */
     public Double getAngle()
     {
         closedByOKButton = false;
         setVisible(true);
         dispose();
 
         return angle;
     }
 
     /**
      * Action to change the current angle units.
      * 
      * @author Martin Pecka
      */
     protected class AngleUnitAction extends AbstractAction
     {
         /** */
         private static final long serialVersionUID = 6938098480887384631L;
 
         /** The unit this action represents. */
         protected final AngleUnit unit;
 
         /**
          * @param unit The unit this action represents.
          */
         public AngleUnitAction(AngleUnit unit)
         {
             super(unit.toString());
             this.unit = unit;
         }
 
         @Override
         public void actionPerformed(ActionEvent e)
         {
             AngleUnit oldUnit = AngleSelectionDialog.this.unit;
             AngleSelectionDialog.this.unit = unit;
             AngleSelectionDialog.this.firePropertyChange(UNIT_PROPERTY, oldUnit, unit);
         }
     }
 
     /**
      * Action for selecting a suggested angle value.
      * 
      * @author Martin Pecka
      */
     protected class SuggestedAngleAction extends AbstractAction
     {
         /** */
         private static final long serialVersionUID = -8793340281827504735L;
 
         /** The value of this action. */
         protected final double    value;
 
         public SuggestedAngleAction(double value)
         {
             super(unit != null ? unit.formatValue(value, AngleUnit.RAD) : Double.toString(value));
             this.value = value;
             AngleSelectionDialog.this.addPropertyChangeListener(UNIT_PROPERTY, new PropertyChangeListener() {
                 @Override
                 public void propertyChange(PropertyChangeEvent evt)
                 {
                     putValue(Action.NAME, unit.formatValue(SuggestedAngleAction.this.value, AngleUnit.RAD));
                     pack();
                 }
             });
         }
 
         @Override
         public void actionPerformed(ActionEvent e)
         {
             angle = value;
         }
 
     }
 
     /**
      * Action for selecting that a custom angle will be put in.
      * 
      * @author Martin Pecka
      */
     protected class CustomAngleAction extends AbstractAction
     {
         /** */
         private static final long serialVersionUID = 7732332151890591919L;
 
         public CustomAngleAction()
         {
             super(messages.getString("customAngle.label"));
         }
 
         @Override
         public void actionPerformed(ActionEvent e)
         {
             inputField.requestFocusInWindow();
             angle = customValue;
         }
 
     }
 
     /**
      * Confirm the dialog.
      * 
      * @author Martin Pecka
      */
     protected class OKAction extends AbstractAction
     {
         /** */
         private static final long serialVersionUID = 8308947712033535447L;
 
         public OKAction()
         {
             super(UIManager.getString("OptionPane.okButtonText", ServiceLocator.get(ConfigurationManager.class).get()
                     .getLocale()));
         }
 
         @Override
         public void actionPerformed(ActionEvent e)
         {
             if (!(valuesGroup.getSelection() == customAngle.getModel() && customValue == null)) {
                 closedByOKButton = true;
                 ServiceLocator.get(ConfigurationManager.class).get().setPreferredAngleUnit(unit);
                 setVisible(false);
             } else {
                 JOptionPane.showMessageDialog(null, messages.getString("badnumber.message"),
                         messages.getString("badnumber.title"), JOptionPane.ERROR_MESSAGE);
             }
         }
     }
 
     /**
      * Cancel the dialog.
      * 
      * @author Martin Pecka
      */
     protected class CancelAction extends AbstractAction
     {
         /** */
         private static final long serialVersionUID = 3484909152830981442L;
 
         public CancelAction()
         {
             super(UIManager.getString("OptionPane.cancelButtonText", ServiceLocator.get(ConfigurationManager.class)
                     .get().getLocale()));
         }
 
         @Override
         public void actionPerformed(ActionEvent e)
         {
             angle = null;
             setVisible(false);
         }
 
     }
 
     /**
      * A key listener that traverses focus between radio buttons under the same parent using arrow keys.
      * 
      * @author Martin Pecka
      */
     protected class FocusingKeyListener extends KeyAdapter
     {
         /** The button that is the source of this event. */
         protected Component button;
 
         /**
          * @param button The button that is the source of this event.
          */
         public FocusingKeyListener(Component button)
         {
             this.button = button;
         }
 
         @Override
         public void keyPressed(KeyEvent e)
         {
             if (e.getKeyCode() == KeyEvent.VK_LEFT || e.getKeyCode() == KeyEvent.VK_DOWN) {
                 Component comp = button.getFocusCycleRootAncestor().getFocusTraversalPolicy()
                         .getComponentBefore(button.getParent(), button);
                 if (comp instanceof AbstractButton && comp.getParent() == button.getParent()) {
                     ((AbstractButton) comp).doClick();
                     comp.requestFocusInWindow();
                 }
             } else if (e.getKeyCode() == KeyEvent.VK_RIGHT || e.getKeyCode() == KeyEvent.VK_UP) {
                 Component comp = button.getFocusCycleRootAncestor().getFocusTraversalPolicy()
                         .getComponentAfter(button.getParent(), button);
                 if (comp instanceof AbstractButton && comp.getParent() == button.getParent()) {
                     ((AbstractButton) comp).doClick();
                     comp.requestFocusInWindow();
                 }
             }
         }
     }
 }
