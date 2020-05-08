 package zz.utils.ui.mvc.view.swing;
 
 import javax.swing.JLabel;
 
import zz.utils.Utils;
 import zz.utils.properties.IProperty;
 import zz.utils.properties.IPropertyListener;
 import zz.utils.ui.mvc.model.PropertyModel;
 
 public class JLabelView extends JLabel
 {
 	private PropertyModel<String> model;
 	
 	private final IPropertyListener<String> valueListener = new IPropertyListener<String>() {
 		@Override
 		public void propertyChanged(IProperty<String> aProperty, String aOldValue, String aNewValue) {
			if (! Utils.equalOrBothNull(getText(), aNewValue)) setText(aNewValue);
 		}
 	};
 	
 	public JLabelView(PropertyModel<String> model) 
 	{
 		super(model.pValue.get());
 		this.model = model;
 		model.pValue.addListener(valueListener);
 	}
 }
