 package es.udc.cartolab.gvsig.fonsagua.alternativas.ui;
 
 import java.awt.Dimension;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.ItemEvent;
 import java.awt.event.ItemListener;
 import java.util.List;
 import java.util.Map;
 import java.util.Vector;
 
 import javax.swing.DefaultComboBoxModel;
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JTextField;
 
 import net.miginfocom.swing.MigLayout;
 
 import org.apache.log4j.Logger;
 
 import com.iver.andami.PluginServices;
 import com.iver.andami.ui.mdiManager.IWindow;
 import com.iver.andami.ui.mdiManager.WindowInfo;
 import com.iver.cit.gvsig.fmap.layers.FLyrVect;
 
 import es.udc.cartolab.gvsig.fonsagua.OpenAlternativeExtension;
 import es.udc.cartolab.gvsig.fonsagua.forms.alternativas.AlternativasForm;
 import es.udc.cartolab.gvsig.fonsagua.utils.FonsaguaConstants;
 import es.udc.cartolab.gvsig.navtable.ToggleEditing;
 import es.udc.cartolab.gvsig.users.utils.DBSession;
 
 @SuppressWarnings("serial")
 public class NewAlternativeDialog extends JPanel implements IWindow, ActionListener {
 
     private static Logger logger = Logger.getLogger("AlternativesExtension");
 
     /*
      * The next properties are the ones related to the interface itself, so they
      * don't need a real explanation
      */
 
     private WindowInfo windowInfo = null;
     private JButton cancelButton = null;
     private JButton okButton = null;
     private FLyrVect altLayer;
     // Here we store the relationships departamento -> municipio -> cantn
     private Map<String, Map<String, List<String>>> divsCodes;
     // Translation from cod_departamento to nombre_departamento
     private Map<String, String> departNames;
     // Translation from cod_municipio to nombre_municipio
     private Map<String, String> municNames;
     // Translation from cod_canton to nombre_canton
     private Map<String, String> cantonNames;
 
     private JTextField codAltField = null;
     private JComboBox departCombo = null;
     private JComboBox municCombo = null;
     private JComboBox cantonCombo = null;
 
     public WindowInfo getWindowInfo() {
 	if (windowInfo == null) {
 	    windowInfo = new WindowInfo(WindowInfo.MODALDIALOG
 		    | WindowInfo.PALETTE | WindowInfo.NOTCLOSABLE);
 	    windowInfo.setTitle(PluginServices.getText(this,
 		    "new_alternative_title"));
 	    Dimension dim = getPreferredSize();
 	    int width, height = 0;
 	    if (dim.getHeight() > 550) {
 		height = 550;
 	    } else {
 		height = 110;
 	    }
 	    if (dim.getWidth() > 550) {
 		width = 550;
 	    } else {
 		width = new Double(dim.getWidth()).intValue() + 5;
 	    }
 	    windowInfo.setWidth(width);
 	    windowInfo.setHeight(height);
 	}
 	return windowInfo;
     }
 
     public NewAlternativeDialog(FLyrVect altLayer,
 	    Map<String, Map<String, List<String>>> divsCodes,
 	    Map<String, String> departNames, Map<String, String> municNames,
 	    Map<String, String> cantonNames) {
 	super();
 	this.altLayer = altLayer;
 	this.divsCodes = divsCodes;
 	this.departNames = departNames;
 	this.municNames = municNames;
 	this.cantonNames = cantonNames;
 	try {
 	    initialize();
 	} catch (Exception e) {
 	    logger.error(e.getMessage(), e);
 	}
     }
 
     private void initialize() throws Exception {
 
 	this.setLayout(new MigLayout());
 
 	JLabel label = new JLabel(PluginServices.getText(this,
 		"Cod_alternativa"));
 	this.add(label);
 
 	codAltField = new JTextField();
 	this.add(codAltField, "w 220!, wrap");
 
 	label = new JLabel(PluginServices.getText(this, "Cod_depart"));
 	this.add(label);
 
 	Vector<Item> items = new Vector<Item>();
 	for (String departCod : departNames.keySet()) {
 	    items.add(new Item(departCod, departNames.get(departCod)));
 	}
 
 	departCombo = new JComboBox(items);
 	departCombo.addItemListener(new ItemListener() {
 	    public void itemStateChanged(ItemEvent event) {
 		if (event.getStateChange() == ItemEvent.SELECTED) {
 		    updateMunicCombo();
 		    updateCantonCombo();
 		}
 	    }
 	});
 	this.add(departCombo, "w 220!, wrap");
 
 	label = new JLabel(PluginServices.getText(this, "Cod_munic"));
 	this.add(label);
 
 	municCombo = new JComboBox();
 	municCombo.addItemListener(new ItemListener() {
 	    public void itemStateChanged(ItemEvent event) {
 		if (event.getStateChange() == ItemEvent.SELECTED) {
 		    updateCantonCombo();
 		}
 	    }
 	});
 	this.add(municCombo, "w 220!, wrap");
 	updateMunicCombo();
 
 	label = new JLabel(PluginServices.getText(this, "Cod_canton"));
 	this.add(label);
 
 	cantonCombo = new JComboBox();
 	this.add(cantonCombo, "w 220!, wrap");
 	updateCantonCombo();
 
 	okButton = new JButton();
 	okButton.setText(PluginServices.getText(this, "Ok"));
 	okButton.addActionListener(this);
 
 	cancelButton = new JButton();
 	cancelButton.setText(PluginServices.getText(this, "Cancel"));
 	cancelButton.addActionListener(this);
 
 	this.add(okButton, "right, bottom, cell 1 5");
 	this.add(cancelButton, "right, bottom, cell 1 5");
 
     }
 
     private void updateMunicCombo() {
 	Vector<Item> items = new Vector<Item>();
 	String selDepart = ((Item) departCombo.getSelectedItem()).id;
 	if (divsCodes.containsKey(selDepart)) {
 	    for (String municCod : divsCodes.get(selDepart).keySet()) {
 		items.add(new Item(municCod, municNames.get(municCod)));
 	    }
 	}
 	municCombo.setModel(new DefaultComboBoxModel(items));
 	municCombo.setSelectedIndex(0);
     }
 
     private void updateCantonCombo() {
 	Vector<Item> items = new Vector<Item>();
 	String selDepart = ((Item) departCombo.getSelectedItem()).id;
 	String selMunic = ((Item) municCombo.getSelectedItem()).id;
 	if ((divsCodes.containsKey(selDepart))
 		&& (divsCodes.get(selDepart).containsKey(selMunic))) {
 	    for (String cantonCod : divsCodes.get(selDepart).get(selMunic)) {
 		items.add(new Item(cantonCod, cantonNames.get(cantonCod)));
 	    }
 	}
 	cantonCombo.setModel(new DefaultComboBoxModel(items));
 	cantonCombo.setSelectedIndex(0);
     }
 
     public void actionPerformed(ActionEvent e) {
 	try {
 	    if (e.getSource() == okButton) {
 
 		String[] values = new String[4];
 		values[0] = codAltField.getText();
		values[1] = ((Item) departCombo.getSelectedItem()).id;
		values[2] = ((Item) municCombo.getSelectedItem()).id;
		values[3] = ((Item) cantonCombo.getSelectedItem()).id;
 
 		for (String value : values) {
 		    if ((value == null) || (value.length() < 1)) {
 			JOptionPane.showMessageDialog(this, PluginServices
 				.getText(this,
 					"alternative_fields_mandatory_msg"),
 				PluginServices.getText(this,
 					"alternative_fields_mandatory_title"),
 				JOptionPane.ERROR_MESSAGE);
 			return;
 		    }
 		}
 		String[] keys = DBSession
 			.getCurrentSession()
 			.getDistinctValues(
 			AlternativasForm.NAME, FonsaguaConstants.dataSchema,
 				AlternativasForm.PKFIELD);
 		for (String key : keys) {
 		    if (key.equals(values[0])) {
 			JOptionPane.showMessageDialog(this, PluginServices
 				.getText(this,
 					"alternative_code_duplicated_msg"),
 				PluginServices.getText(this,
 					"alternative_code_duplicated_title"),
 				JOptionPane.ERROR_MESSAGE);
 			return;
 		    }
 		}
 		ToggleEditing edition = new ToggleEditing();
 
 		int[] indexes = new int[4];
 		indexes[0] = altLayer.getRecordset().getFieldIndexByName(
 			AlternativasForm.PKFIELD);
 		indexes[1] = altLayer.getRecordset().getFieldIndexByName(
 			AlternativasForm.DEPARTFK);
 		indexes[2] = altLayer.getRecordset().getFieldIndexByName(
 			AlternativasForm.MUNICFK);
 		indexes[3] = altLayer.getRecordset().getFieldIndexByName(
 			AlternativasForm.CANTONFK);
 		edition.modifyValues(altLayer, altLayer.getSource()
 			.getShapeCount() - 1, indexes, values);
 
 		edition.stopEditing(altLayer, false);
 		OpenAlternativeExtension.openAlternative(values[0]);
 	    } else if (e.getSource() == cancelButton) {
 		new ToggleEditing().stopEditing(altLayer, true);
 	    }
 	} catch (Exception e1) {
 	    e1.printStackTrace();
 	}
 	PluginServices.getMDIManager().closeWindow(this);
 
     }
 
     public Object getWindowProfile() {
 	return null;
     }
 
     private class Item {
 	private String id;
 	private String description;
 
 	public Item(String id, String description) {
 	    this.id = id;
 	    this.description = description;
 	}
 
 	public String toString() {
 	    return description;
 	}
     }
 }
