 package semGen.models.properties.ui;
 import javax.swing.JPanel;
 import javax.swing.SwingConstants;
 import javax.swing.border.LineBorder;
 
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Graphics;
 
 import javax.swing.JLabel;
 
 import java.awt.Dimension;
 
 import javax.swing.BoxLayout;
 
 import semGen.models.properties.IModelProperty;
 import semGen.models.properties.ModelPropertyListener;
 
 
 public class PropertyComponent extends JPanel {
 	private JLabel _lblEquationValue;
 	private JLabel _propertyNameComponent;
 
 	// Model property associated with this property component
 	private IModelProperty _modelProperty;
 	
 	/**
 	 * Create the panel.
 	 */
 	public PropertyComponent() {
 		
 		setOpaque(false);
 		
 		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
 		
 		PropertyNameContainerComponent panelPropertyNameContainer = new PropertyNameContainerComponent();
 		panelPropertyNameContainer.setOpaque(false);
 		add(panelPropertyNameContainer);
 		
 		_propertyNameComponent = new JLabel("?");
 		_propertyNameComponent.setVerticalAlignment(SwingConstants.CENTER);
 		panelPropertyNameContainer.add(_propertyNameComponent);
 		
 		JPanel panelEquationContainer = new JPanel();
 		panelEquationContainer.setOpaque(false);
 		add(panelEquationContainer);
 		
 		_lblEquationValue = new JLabel("?");
 		panelEquationContainer.add(_lblEquationValue);
 	}
 	
 	/**
 	 * Get the property name component
 	 * @return property name component
 	 */
 	public Component getPropertyNameComponent(){
 		return _propertyNameComponent;
 	}
 	
 	/**
 	 * Tells whether a property is set
 	 * @return True if this component has a model property. False otherwise.
 	 */
 	public boolean hasProperty(){
 		return _modelProperty != null;
 	}
 	
 	/**
 	 * Gets the stored property
 	 * @return IModelProperty is there's one set. Null otherwise.
 	 */
 	public IModelProperty getProperty(){
 		return _modelProperty;
 	}
 	
 	/*
 	 * Sets values from the property on the component
 	 */
 	public void setProperty(IModelProperty property){
 		_modelProperty = property;
 		
 		// Update the ui
 		if(_modelProperty != null){
 			_propertyNameComponent.setText(_modelProperty.getName());
 			_lblEquationValue.setText(_modelProperty.getEquation());
 		}
 		
 		setSize(getPreferredSize());
 	}
 	
 	private class PropertyNameContainerComponent extends JPanel{
 		
 		/**
 		 * Draw an oval
 		 */
 		@Override
 	    protected void paintBorder(Graphics g) {
 	         g.setColor(getForeground());
	         
	         // Before -1 added the border was cut off at the bottom
	         g.drawOval(0, 0, getWidth() - 1, getHeight() - 1);
 	    }
 	}
 }
