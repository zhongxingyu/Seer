 package de.aidger.view.models;
 
import static de.aidger.utils.Translation._;

 import java.text.SimpleDateFormat;
 
 import de.aidger.model.models.Activity;
 import de.unistuttgart.iste.se.adohive.model.IActivity;
 
 /**
  * The UI activity is used for prettier rendering of the model.
  * 
  * @author aidGer Team
  */
 public class UIActivity extends Activity {
 
     /**
      * Initializes the Activity class.
      */
     public UIActivity() {
     }
 
     /**
      * Initializes the Activity class with the given activity model.
      * 
      * @param a
      *            the activity model
      */
     public UIActivity(IActivity a) {
         super(a);
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see de.aidger.model.AbstractModel#toString()
      */
     @Override
     public String toString() {
        return getType() + " (" + getSender() + " " + _("at") + " "
                 + (new SimpleDateFormat("dd.MM.yyyy")).format(getDate()) + ")";
     }
 }
