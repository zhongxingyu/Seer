 package org.bh.gui.swing;
 
 import java.awt.Component;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.util.Vector;
 
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 
 import org.bh.data.DTOScenario;
 import org.bh.platform.Services;
 import org.bh.platform.i18n.ITranslator;
 import org.bh.validation.VRIsDouble;
 import org.bh.validation.VRIsInteger;
 import org.bh.validation.VRIsNotEqual;
 import org.bh.validation.VRIsPositive;
 import org.bh.validation.VRMandatory;
 import org.bh.validation.ValidationRule;
 
 import com.jgoodies.forms.layout.CellConstraints;
 import com.jgoodies.forms.layout.FormLayout;
 
 /**
  * This class contains the form for head-data
  * 
  * @author Anton Kharitonov
  * @author Patrick Heinz
  * @version 0.3, 01.01.2010
  * 
  */
 @SuppressWarnings("serial")
 public class BHScenarioHeadForm extends JPanel {
 	private BHDescriptionLabel lscenname;
 	private BHDescriptionLabel lscendescript;
 	private BHDescriptionLabel lequityyield;
 	private BHDescriptionLabel ldeptyield;
 	private BHDescriptionLabel ltradetax;
 	private BHDescriptionLabel lcorporatetax;
 	private BHDescriptionLabel lbaseyear;
 
 	private BHTextField tfscenname;
 	private BHTextField tfscendescript;
         //fields show percentage as %
 	private BHTextField tfequityyield;
 	private BHTextField tfdeptyield;
 	private BHTextField tftradetax;
 	private BHTextField tfcorporatetax;
         
 	private BHTextField tfBase;
 	private JLabel lpercentequity;
 	private JLabel lpercentdept;
 	private JLabel lpercenttrade;
 	private JLabel lpercentcorporate;
 	
 	private BHDescriptionLabel lPeriodType;
 	private BHComboBox cmbPeriodType;
 
 	private BHFocusTraversalPolicy focusPolicy;
 
 	ITranslator translator = Services.getTranslator();
 
 	/**
 	 * Constructor.
 	 */
 	public BHScenarioHeadForm() {
 		this.initialize();
 	}
 
 	/**
 	 * Initialize method.
 	 */
 	private void initialize() {
 
 		String rowDef = "4px,p,4px,p,4px,p,4px,p,20px,p,4px,p,4px,p,20px,p,4px";
 		String colDef = "4px,4px,right:pref,4px,pref,max(80px;pref),4px,left:pref,24px:grow,pref,4px,pref,4px,right:pref,4px,pref,pref,4px,pref,4px,pref,4px";
 
 		FormLayout layout = new FormLayout(colDef, rowDef);
 		this.setLayout(layout);
 
 		layout.setColumnGroups(new int[][] { { 6, 17 } });
 
 		CellConstraints cons = new CellConstraints();
 
 		this.add(this.getlscenName(), cons.xywh(3, 4, 1, 1));
 		this.add(this.gettfscenName(), cons.xywh(6, 4, 4, 1));
 		this.add(this.getlBase(), cons.xywh(14, 4, 1, 1));
 		this.add(this.gettfBase(), cons.xywh(17, 4, 1, 1));
 		this.add(this.getlscenDescript(), cons.xywh(3, 6, 1, 1));
 		this.add(this.gettfscenDescript(), cons.xywh(6, 6, 12, 1));
 
 		this.add(this.getlequityYield(), cons.xywh(3, 12, 1, 1));
 		this.add(this.getldeptYield(), cons.xywh(3, 14, 1, 1));
 		this.add(this.getltradeTax(), cons.xywh(14, 12, 1, 1));
 		this.add(this.getlcorporateTax(), cons.xywh(14, 14, 1, 1));
 		this.add(this.gettfequityYield(), cons.xywh(6, 12, 1, 1));
 		this.add(this.gettfdeptYield(), cons.xywh(6, 14, 1, 1));
 		this.add(this.gettftradeTax(), cons.xywh(17, 12, 1, 1));
 		this.add(this.gettfcorporateTax(), cons.xywh(17, 14, 1, 1));
 		this.add(this.getlpercentEquity(), cons.xywh(8, 12, 1, 1));
 		this.add(this.getlpercentDept(), cons.xywh(8, 14, 1, 1));
 		this.add(this.getlpercentTrade(), cons.xywh(19, 12, 1, 1));
 		this.add(this.getlpercentCorporate(), cons.xywh(19, 14, 1, 1));
 		
 		this.add(this.getlPeriodType(), cons.xywh(3, 16, 1, 1));
 		this.add(this.getCmbPeriodType(), cons.xywh(6, 16, 12, 1, "left, default"));
 
 		Vector<Component> order = new Vector<Component>();
 		order.add(this.gettfscenName());
 		order.add(this.gettfBase());
 		order.add(this.gettfscenDescript());
 		order.add(this.gettfequityYield());
 		order.add(this.gettfdeptYield());
 		order.add(this.gettftradeTax());
 		order.add(this.gettfcorporateTax());
 		order.add(this.getCmbPeriodType());
 
 		this.setFocusPolicy(order);
 
 		this.setFocusTraversalPolicy(this.getFocusPolicy());
 		this.setFocusTraversalPolicyProvider(true);
 	}
 
 	// TODO add missing label keys etc. and translations, change hard coded
 	// values to keys
 
 	public void setFocusPolicy(Vector<Component> order) {
 		this.focusPolicy = new BHFocusTraversalPolicy(order);
 	}
 
 	public BHFocusTraversalPolicy getFocusPolicy() {
 		if (this.focusPolicy == null) {
 			this.focusPolicy = new BHFocusTraversalPolicy(null);
 		}
 
 		return this.focusPolicy;
 	}
 
 	/**
 	 * Getter method for component lscenName.
 	 * 
 	 * @return the initialized component
 	 */
 	public BHDescriptionLabel getlscenName() {
 
 		if (this.lscenname == null) {
 			this.lscenname = new BHDescriptionLabel(DTOScenario.Key.NAME);
 		}
 
 		return this.lscenname;
 	}
 
 	/**
 	 * Getter method for component lscenDescript.
 	 * 
 	 * @return the initialized component
 	 */
 	public BHDescriptionLabel getlscenDescript() {
 
 		if (this.lscendescript == null) {
 			this.lscendescript = new BHDescriptionLabel(DTOScenario.Key.COMMENT);
 		}
 
 		return this.lscendescript;
 	}
 
 	/**
 	 * Getter method for component lequityYield.
 	 * 
 	 * @return the initialized component
 	 */
 	public BHDescriptionLabel getlequityYield() {
 
 		if (this.lequityyield == null) {
 			this.lequityyield = new BHDescriptionLabel(DTOScenario.Key.REK);
 		}
 
 		return this.lequityyield;
 	}
 
 	/**
 	 * Getter method for component ldeptYield.
 	 * 
 	 * @return the initialized component
 	 */
 	public BHDescriptionLabel getldeptYield() {
 
 		if (this.ldeptyield == null) {
 			this.ldeptyield = new BHDescriptionLabel(DTOScenario.Key.RFK);
 		}
 
 		return this.ldeptyield;
 	}
 
 	/**
 	 * Getter method for component ltradeTax.
 	 * 
 	 * @return the initialized component
 	 */
 	public BHDescriptionLabel getltradeTax() {
 
 		if (this.ltradetax == null) {
 			this.ltradetax = new BHDescriptionLabel(DTOScenario.Key.BTAX);
 		}
 
 		return this.ltradetax;
 	}
 
 	/**
 	 * Getter method for component lcorporateTax.
 	 * 
 	 * @return the initialized component
 	 */
 	public BHDescriptionLabel getlcorporateTax() {
 
 		if (this.lcorporatetax == null) {
 			this.lcorporatetax = new BHDescriptionLabel(DTOScenario.Key.CTAX);
 		}
 
 		return this.lcorporatetax;
 	}
 
 	/**
 	 * Getter method for component lbaseYear.
 	 * 
 	 * @return the initialized component
 	 */
 	public BHDescriptionLabel getlBase() {
 
 		if (this.lbaseyear == null) {
 			this.lbaseyear = new BHDescriptionLabel(DTOScenario.Key.IDENTIFIER);
 		}
 
 		return this.lbaseyear;
 	}
 
 	// Here do the getters for the textfields begin
 
 	/**
 	 * Getter method for component tfscenName.
 	 * 
 	 * @return the initialized component
 	 */
 	public BHTextField gettfscenName() {
 
 		if (this.tfscenname == null) {
 			this.tfscenname = new BHTextField(DTOScenario.Key.NAME, false);
 			ValidationRule[] rules = { VRMandatory.INSTANCE };
 			tfscenname.setValidationRules(rules);
 		}
 		return this.tfscenname;
 	}
 
 	/**
 	 * Getter method for component tfscenDescript.
 	 * 
 	 * @return the initialized component
 	 */
 	public BHTextField gettfscenDescript() {
 
 		if (this.tfscendescript == null) {
 			this.tfscendescript = new BHTextField(DTOScenario.Key.COMMENT,
 					false);
 		}
 		return this.tfscendescript;
 	}
 
 	/**
 	 * Getter method for component tfequityYeild.
 	 * 
 	 * @return the initialized component
 	 */
 	public BHTextField gettfequityYield() {
 
 		if (this.tfequityyield == null) {
 			this.tfequityyield = new BHPercentTextField(DTOScenario.Key.REK);
 			ValidationRule[] rules = { VRMandatory.INSTANCE,
 					VRIsDouble.INSTANCE, VRIsPositive.INSTANCE,
 					VRIsNotEqual.ISNOTZERO };
 			tfequityyield.setValidationRules(rules);
 		}
 		return this.tfequityyield;
 	}
 
 	/**
 	 * Getter method for component tfdeptYield.
 	 * 
 	 * @return the initialized component
 	 */
 	public BHTextField gettfdeptYield() {
 
 		if (this.tfdeptyield == null) {
 			this.tfdeptyield = new BHPercentTextField(DTOScenario.Key.RFK);
 			ValidationRule[] rules = { VRMandatory.INSTANCE,
 					VRIsDouble.INSTANCE, VRIsPositive.INSTANCE,
 					VRIsNotEqual.ISNOTZERO };
 			tfdeptyield.setValidationRules(rules);
 		}
 		return this.tfdeptyield;
 	}
 
 	/**
 	 * Getter method for component tftradeTax.
 	 * 
 	 * @return the initialized component
 	 */
 	public BHTextField gettftradeTax() {
 
 		if (this.tftradetax == null) {
 			this.tftradetax = new BHPercentTextField(DTOScenario.Key.BTAX);
 			ValidationRule[] rules = { VRMandatory.INSTANCE,
 					VRIsDouble.INSTANCE, VRIsPositive.INSTANCE };
 			tftradetax.setValidationRules(rules);
 		}
 		return this.tftradetax;
 	}
 
 	/**
 	 * Getter method for component tfcorporateTax.
 	 * 
 	 * @return the initialized component
 	 */
 	public BHTextField gettfcorporateTax() {
 
 		if (this.tfcorporatetax == null) {
 			this.tfcorporatetax = new BHPercentTextField(DTOScenario.Key.CTAX);
 			ValidationRule[] rules = { VRMandatory.INSTANCE,
 					VRIsDouble.INSTANCE, VRIsPositive.INSTANCE };
 			tfcorporatetax.setValidationRules(rules);
 		}
 		return this.tfcorporatetax;
 	}
 
 	/**
 	 * Getter method for component tfbaseYear.
 	 * 
 	 * @return the initialized component
 	 */
 	public BHTextField gettfBase() {
 
 		if (this.tfBase == null) {
			this.tfBase = new BHTextField(DTOScenario.Key.IDENTIFIER, false);
 			ValidationRule[] rules = { VRMandatory.INSTANCE,
 					VRIsInteger.INSTANCE };
 			tfBase.setValidationRules(rules);
 		}
 		return this.tfBase;
 	}
 
 	/**
 	 * Getter method for component lpercentEquity.
 	 * 
 	 * @return the initialized component
 	 */
 	public JLabel getlpercentEquity() {
 
 		if (this.lpercentequity == null) {
 			this.lpercentequity = new JLabel("%");
 		}
 		return this.lpercentequity;
 	}
 
 	/**
 	 * Getter method for component lpercentDept.
 	 * 
 	 * @return the initialized component
 	 */
 	public JLabel getlpercentDept() {
 
 		if (this.lpercentdept == null) {
 			this.lpercentdept = new JLabel("%");
 		}
 		return this.lpercentdept;
 	}
 
 	/**
 	 * Getter method for component lpercentTrade.
 	 * 
 	 * @return the initialized component
 	 */
 	public JLabel getlpercentTrade() {
 
 		if (this.lpercenttrade == null) {
 			this.lpercenttrade = new JLabel("%");
 		}
 		return this.lpercenttrade;
 	}
 
 	/**
 	 * Getter method for component lpercentCorporate.
 	 * 
 	 * @return the initialized component
 	 */
 	public JLabel getlpercentCorporate() {
 
 		if (this.lpercentcorporate == null) {
 			this.lpercentcorporate = new JLabel("%");
 		}
 		return this.lpercentcorporate;
 	}
 
 	public BHDescriptionLabel getlPeriodType() {
 		if (lPeriodType == null) {
 			lPeriodType = new BHDescriptionLabel(DTOScenario.Key.PERIOD_TYPE);
 		}
 		return lPeriodType;
 	}
 
 	public BHComboBox getCmbPeriodType() {
 		if (cmbPeriodType == null) {
 			cmbPeriodType = new BHComboBox(DTOScenario.Key.PERIOD_TYPE);
 		}
 		return cmbPeriodType;
 	}
 
 	// TODO remove main later
 	/**
 	 * Test main method.
 	 */
 	@SuppressWarnings("deprecation")
 	public static void main(String args[]) {
 
 		JFrame test = new JFrame("Test for ViewHeadData1");
 		test.setContentPane(new BHScenarioHeadForm());
 		test.addWindowListener(new WindowAdapter() {
 			@Override
 			public void windowClosing(WindowEvent e) {
 				System.exit(0);
 			}
 		});
 		test.pack();
 		test.show();
 	}
 }
