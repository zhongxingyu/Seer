 package eu.comexis.napoleon.client.utils;
 
 import static com.google.gwt.query.client.GQuery.$;
 import java.util.List;
 
 import com.google.gwt.user.client.ui.ListBox;
 import com.google.gwt.user.client.ui.Widget;
 
 import eu.comexis.napoleon.client.resources.Literals;
 import eu.comexis.napoleon.client.resources.Resources;
 import eu.comexis.napoleon.client.widget.InformationDialog;
 import eu.comexis.napoleon.shared.validation.ValidationMessage;
 
 public class UiHelper {
 
   // static class
   private UiHelper() {
   }
 
   public static ListBox createListBoxForEnum(Class<? extends Enum<?>> enumClass, String prefix,
       boolean mutiple) {
     ListBox box = new ListBox(mutiple);
 
     for (Enum<?> e : enumClass.getEnumConstants()) {
       box.addItem(translateEnum(prefix, e), e.name());
     }
 
     return box;
   }
 
   public static String translateEnum(String prefix, Enum<?> e) {
     if (e == null) {
       return "";
     }
     return translateEnum(prefix, e, "");
   }
 
   public static String translateEnum(String prefix, Enum<?> e, String suffix) {
     String literalKey = new StringBuilder(prefix).append(e.name()).append(suffix).toString();
     return Literals.INSTANCE.getString(literalKey);
   }
 
   public static void selectTextItemBoxByValue(ListBox tb, String value) {
     for (int i = 0; i < tb.getItemCount(); i++) {
       if (tb.getValue(i).equals(value)) {
         tb.setSelectedIndex(i);
         break;
       }
     }
   }
   
   public static void selectTextItemBoxByValue(ListBox tb, Enum<?> value) {
     if (value != null){
       selectTextItemBoxByValue(tb, value.name());
     }
   }
   
   public static void displayValidationMessage(List<ValidationMessage> validationMessages, Widget w){
     resetForm(w);
     
     InformationDialog.get().showValidationMessages(validationMessages);
     
     for (ValidationMessage m : validationMessages){
       String selector = "[name='"+m.getComponentId()+"' ]";
       $(selector, w).addClass(Resources.INSTANCE.css().fieldInError());
     }
   }
   
   
   public static void resetForm(Widget w){
     $("input, select", w).removeClass(Resources.INSTANCE.css().fieldInError());
   }
 
 }
