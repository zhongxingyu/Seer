 package no.ntnu.capgeminitest.binding.android;
 
import java.util.HashMap;
 import java.util.Map;
 
 import no.ntnu.capgeminitest.binding.Property;
 import no.ntnu.capgeminitest.binding.android.propertyprovider.PropertyProviderFactory;
 import android.app.Activity;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 
 public abstract class BindingActivity extends Activity {
     
    private Map<String, Property<?>> bindings = new HashMap<String, Property<?>>();
     
     public class BindingLayoutInflater {
         private LayoutInflater inflater;
         private BindingFactory bindingFactory;
         
         public BindingLayoutInflater(LayoutInflater inflater, BindingFactory bindingFactory) {
             this.inflater = inflater;
             this.bindingFactory = bindingFactory;
         }
         
         public View inflate(int resource, ViewGroup root) {
             return inflater.inflate(resource, root);
         }
         
         public Map<String, Property<?>> getBoundProperties() {
             return bindingFactory.getBoundProperties();
         }
     }
     
     private PropertyProviderFactory propertyProviderFactory =
             PropertyProviderFactory.getDefaultFactory();
     
     /**
      * Set content view from a layout resource.
      * 
      * This is the
      */
     public View createBoundView(int id, Map<String, Property<?>> bindings) {
         BindingLayoutInflater inflater = getBindingLayoutInflater();
         View v = inflater.inflate(id, null);
         bindings.putAll(inflater.getBoundProperties());
         return v;
     }
     
     public void setBoundContentView(int id, Map<String, Property<?>> bindings) {
         setContentView(createBoundView(id, bindings));
     }
     
     private BindingLayoutInflater getBindingLayoutInflater() {
         LayoutInflater inflater = getLayoutInflater().cloneInContext(this);
         BindingFactory factory = new BindingFactory(propertyProviderFactory, inflater);
         inflater.setFactory(factory);
         
         return new BindingLayoutInflater(inflater, factory);
     }
 }
