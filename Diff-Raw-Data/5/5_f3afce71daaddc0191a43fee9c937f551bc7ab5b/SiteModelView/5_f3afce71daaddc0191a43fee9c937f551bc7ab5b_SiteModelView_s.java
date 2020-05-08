 package newgui.gui.modelViews;
 
 import gui.document.ACGDocument;
 import gui.inputPanels.Configurator.InputConfigException;
 import gui.inputPanels.DoubleParamView;
 import gui.inputPanels.SiteModelElement;
 import gui.inputPanels.SiteModelElement.MutModelType;
 import gui.inputPanels.SiteModelElement.RateModelType;
 import gui.widgets.Style;
 import gui.widgets.Stylist;
 
 import java.awt.BorderLayout;
 import java.awt.CardLayout;
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Dimension;
 import java.awt.FlowLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.ItemEvent;
 import java.awt.event.ItemListener;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.swing.BorderFactory;
 import javax.swing.Box;
 import javax.swing.BoxLayout;
 import javax.swing.JCheckBox;
 import javax.swing.JComboBox;
 import javax.swing.JComponent;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JSeparator;
 import javax.swing.JSpinner;
 import javax.swing.JTextField;
 import javax.swing.SpinnerNumberModel;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 
 import net.miginfocom.swing.MigLayout;
 
 import org.w3c.dom.Element;
 
 /**
  * The 'view' portion for site / rate models, allowing user to see and configure some options
  * @author brendano
  *
  */
 public class SiteModelView extends JPanel {
 
 	//The 'model' portion that stores the data
 	private SiteModelElement siteModel;
 		
 	private final String[] rateTypes = new String[]{"One rate", "Gamma rates" /* "Custom rates" */};
 	
 	private Stylist stylist = new Stylist();
 
 	public SiteModelView(SiteModelElement siteModel) {
 		this.siteModel = siteModel;
 		oneRatePanel = new JPanel();
 		rateConfigPanel = new JPanel();
 		gammaPanel = new JPanel();
 		customPanel = new JPanel();
 		initComponents();
 		updateView();
 	}
 	
 	
 	/**
 	 * Sets the model associated with this view
 	 * @param siteModel
 	 */
 //	public void setSiteModel(SiteModelElement siteModel) {
 //		this.siteModel = siteModel;
 //		updateView();
 //	}
 	
 	/**
 	 * Get the site model that holds the model data 
 	 * @return
 	 */
 	public SiteModelElement getSiteModel() {
 		return siteModel;
 	}
 	
 	/**
 	 * Refresh the values of the UI elements based on the state of siteModel. If new settings are loaded from a 
 	 * file, for instance, call this method to make the right values appear in the widgets
 	 */
 	public void updateView() {
 		if (siteModel.getModelType().equals( MutModelType.F84)) 
 			mutBox.setSelectedIndex(0);
 		
 		if (siteModel.getModelType().equals( MutModelType.TN93)) {
 			mutBox.setSelectedIndex(1);
 		}
 		
 		if (siteModel.getRateModelType().equals( RateModelType.Constant)) {
 			rateBox.setSelectedIndex(0);
 		}
 		if (siteModel.getRateModelType().equals( RateModelType.Gamma)) {
 			rateBox.setSelectedIndex(1);
 			Integer categs = siteModel.getRatCatgeoryCount();
 			categsSpinner.setValue( categs );
 		}
 		if (siteModel.getRateModelType().equals( RateModelType.Custom)) {
 			rateBox.setSelectedIndex(2);
 		}
 		
 		alphaView.updateView();
 		kappaView.updateView();
 		kappaRView.updateView();
 		kappaYView.updateView();
 		repaint();
 	}
 
 
 	public void readNodesFromDocument(ACGDocument doc)
 			throws InputConfigException {
 		siteModel.readElements(doc);
 		updateView();
 	}
 	
 	
 	private void initComponents() {
 		stylist.addStyle(new Style() {
 			public void apply(JComponent comp) {
 				comp.setOpaque(false);
 				comp.setAlignmentX(Component.LEFT_ALIGNMENT);
 				comp.setAlignmentY(Component.TOP_ALIGNMENT);
 			}
 		});
 		
 		stylist.applyStyle(this);
 		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
 		
 		mutPanel = new JPanel();
 		//mutPanel.setLayout(new BoxLayout(mutPanel, BoxLayout.Y_AXIS));
 		mutPanel.setLayout(new MigLayout());
 		//mutPanel.setPreferredSize(new Dimension(300, 400));
 		stylist.applyStyle(mutPanel);
 		JPanel mutTop = new JPanel();
 		stylist.applyStyle(mutTop);
 		
 		
 		mutTop.add(new JLabel("Mutation model: "));
 		//mutTop.setBorder(BorderFactory.createLineBorder(Color.BLUE));
 		stylist.applyStyle(mutTop);
 		mutBox = new JComboBox(new Object[]{/*"JC69", "K2P", */ "F84", "TN93"}); 
 		mutBox.addItemListener(new ItemListener() {
 			public void itemStateChanged(ItemEvent evt) {
 				updateMutModelBox();
 				
 			}
 		});
 		mutTop.add(mutBox);
 		
 		stylist.applyStyle(mutPanel);
 		mutParamsPanel = new JPanel();
 		//mutParamsPanel.setBorder(BorderFactory.createLineBorder(Color.red));
 		mutParamsPanel.setLayout(new BoxLayout(mutParamsPanel, BoxLayout.Y_AXIS));
 		//mutParamsPanel.setPreferredSize(new Dimension(300, 200));
 		stylist.applyStyle(mutParamsPanel);
 		
 		mutPanel.add(mutTop, "wrap");
 		mutPanel.add(mutParamsPanel, "wrap");
 		//mutPanel.add(Box.createVerticalGlue());
 		
 		kappaView = new DoubleParamView("Kappa", siteModel.getTtRatioElement());
 		stylist.applyStyle(kappaView);
 		kappaView.setPreferredSize(new Dimension(200, 40));
 		kappaRView = new DoubleParamView("Kappa R", siteModel.getKappaRElement());
 		kappaYView = new DoubleParamView("Kappa Y", siteModel.getKappaYElement());
 		stylist.applyStyle(kappaRView);
 		stylist.applyStyle(kappaYView);
 		mutParamsPanel.add(kappaView);
 		
 		this.add(mutPanel);
 		
 		JSeparator sep = new JSeparator(JSeparator.HORIZONTAL);
 		this.add(sep);
 		
 		ratePanel = new JPanel();
 		ratePanel.setLayout(new MigLayout());
 		this.add(ratePanel);
 		stylist.applyStyle(ratePanel);
 		ratePanel.setPreferredSize(new Dimension(300, 200));
 		JPanel rateTop = new JPanel();
 		stylist.applyStyle(rateTop);
 		rateTop.add(new JLabel("Rate model: "));
 		rateBox = new JComboBox(rateTypes);
 		rateBox.addItemListener(new ItemListener() {
 			public void itemStateChanged(ItemEvent evt) {
 				CardLayout cl = (CardLayout)(rateConfigPanel.getLayout());
 			    cl.show(rateConfigPanel, (String)evt.getItem());
 			    if (rateBox.getSelectedIndex()==0)
 			    	siteModel.setRateModelType( SiteModelElement.RateModelType.Constant);
 			    if (rateBox.getSelectedIndex()==1)
 			    	siteModel.setRateModelType( SiteModelElement.RateModelType.Gamma);
 			  //  if (rateBox.getSelectedIndex()==2)
 			   // 	siteModel.setRateModelType( SiteModelElement.RateModelType.Custom);
 
 			    rateConfigPanel.repaint();
 			}
 		});
 		rateTop.add(rateBox);
 		ratePanel.add(rateTop, "wrap");
 		
 
 		rateConfigPanel.setLayout(new CardLayout());
 		rateConfigPanel.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 0));
 		stylist.applyStyle(rateConfigPanel);
 		ratePanel.add(rateConfigPanel, "wrap");
 		
 		oneRatePanel.setLayout(new MigLayout());
 		JLabel rateLabel = new JLabel("Rate (subs. / site):"); 
 		oneRatePanel.add(rateLabel);
 		oneRatePanel.setToolTipText("Pick the rate at which sites evolve in expected substitutions / time");
 		rateTextField = new JTextField("1.0");
 		rateTextField.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				siteModel.setConstantRate( Double.parseDouble(rateTextField.getText()));
 				System.out.println("Setting constant rate to L : " + Double.parseDouble(rateTextField.getText()));
 			}
 		});
 		rateTextField.setPreferredSize(new Dimension(60, 24));
 		rateTextField.setHorizontalAlignment(JTextField.RIGHT);
 		oneRatePanel.add(rateTextField);
 		oneRatePanel.setOpaque(false);
 		rateConfigPanel.add(oneRatePanel, rateTypes[0]);
 
 		gammaPanel.setLayout(new MigLayout());
 		categsSpinner = new JSpinner(new SpinnerNumberModel(4, 1, 100, 1));
 		categsSpinner.addChangeListener(new ChangeListener() {
 			public void stateChanged(ChangeEvent evt) {
 				siteModel.setRateCategories( (Integer)categsSpinner.getValue());
 			}
 		});
 		categsSpinner.setToolTipText("Number of categories in discrete gamma rates model");
 		stylist.applyStyle(categsSpinner);
 		JLabel categLabel = new JLabel("Rate categories:");
 		categLabel.setMaximumSize(new Dimension(120, 100));
 		gammaPanel.add(categLabel);
 		gammaPanel.add(categsSpinner, "wrap");
 		
 		
 		alphaView = new DoubleParamView("Gamma shape (alpha):", siteModel.getAlphaParamElement());
 		gammaPanel.add(alphaView, "span 2");
 		
 		gammaPanel.setOpaque(false);
 		rateConfigPanel.add(gammaPanel, rateTypes[1]);
 		
 		//customPanel.add(new JTextField("Custom rate stuff"));
 		//customPanel.setOpaque(false);
 		//rateConfigPanel.add(customPanel,rateTypes[2]);
 		
 		this.add(Box.createVerticalGlue());
 	}
 
 	protected void updateMutModelBox() {
 		if (mutBox.getSelectedIndex()==0) {
 	    	siteModel.setMutModelType( SiteModelElement.MutModelType.F84);
 	    	mutParamsPanel.remove(kappaRView);
 	    	mutParamsPanel.remove(kappaYView);
 	    	mutParamsPanel.add(kappaView);
 	    	mutParamsPanel.revalidate();
 	    	mutParamsPanel.repaint();
 		}
 		
 		if (mutBox.getSelectedIndex()==1) {
 	    	siteModel.setMutModelType( SiteModelElement.MutModelType.TN93);
 	    	mutParamsPanel.remove(kappaView);
 	    	mutParamsPanel.add(kappaRView);
 	    	mutParamsPanel.add(kappaYView);
 	    	mutParamsPanel.revalidate();
 	    	mutParamsPanel.repaint();
 		}
 		
 	    
 	    mutPanel.repaint();
 	}
 	
 	/**
 	 * Push changes from UI elements in this view to the underlying model
 	 */
 	public void updateModel() {
 		//I *think* that all components update the model right away, so we don't have to do 
 		//anything here. 
 	}
 
 	private final JPanel rateConfigPanel;
 	private JPanel mutPanel;
 	private JPanel ratePanel;
 	private JPanel mutParamsPanel;
 	
 	private final JPanel gammaPanel;
 	private final JPanel customPanel;
 	private final JPanel oneRatePanel;
 
 	private JTextField rateTextField;
 	private JSpinner categsSpinner; 
 	
 	private JComboBox mutBox;
 	private JComboBox rateBox;
 	private DoubleParamView alphaView;
 	private DoubleParamView kappaView;
 	private DoubleParamView kappaRView;
 	private DoubleParamView kappaYView;
 	
 }
