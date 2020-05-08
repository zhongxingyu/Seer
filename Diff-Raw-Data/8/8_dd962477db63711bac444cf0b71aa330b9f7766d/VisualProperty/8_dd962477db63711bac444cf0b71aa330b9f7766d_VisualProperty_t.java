 package edu.umich.sbolt.world;
 
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import edu.umich.sbolt.InputLinkHandler;
 
 import abolt.lcmtypes.categorized_data_t;
 import abolt.lcmtypes.category_t;
 import abolt.lcmtypes.object_data_t;
 import sml.FloatElement;
 import sml.Identifier;
 import sml.IntElement;
 import sml.StringElement;
 import sml.WMElement;
 
 /**
  * A category for each object, which contains several possible labels and their confidences
  * 
  * @author mininger
  * 
  */
 public class VisualProperty implements IInputLinkElement
 {   
 	protected static HashMap<Integer, String> categoryNames = null;
 	public static String getCategoryName(Integer categoryType){
 		if(categoryNames == null){
 			categoryNames = new HashMap<Integer, String>();
 			categoryNames.put(category_t.CAT_COLOR, "color");
 			categoryNames.put(category_t.CAT_SHAPE, "shape");
 			categoryNames.put(category_t.CAT_SIZE, "size");
 			categoryNames.put(category_t.CAT_TEXTURE, "texture");
 		}
 		return categoryNames.get(categoryType);
 	}
 	
 	public static Integer getCategoryType(String categoryName){
 		if(categoryName.equals("color")){
 			return category_t.CAT_COLOR;
 		} else if(categoryName.equals("shape")){
 			return category_t.CAT_SHAPE;
 		} else if(categoryName.equals("size")){
 			return category_t.CAT_SIZE;
 		} else if(categoryName.equals("texture")){
 			return category_t.CAT_TEXTURE;
 		} else {
 			return null;
 		}
 	}
     
     // Root identifier for the category
     protected Identifier visualID;
     
     // Name of the category
     protected Integer categoryType;
     
     // String WME for the identifier
     protected StringElement nameWME;
     
     // Labels and confidences
     protected HashMap<String, Double> labels;
     
     protected HashMap<String, FloatElement> labelWMEs;
 
     public VisualProperty(categorized_data_t category){
     	visualID = null;
     	nameWME = null;
     	categoryType = category.cat.cat;
     	labels = new HashMap<String, Double>();
     	labelWMEs = new HashMap<String, FloatElement>();
     	updateCategoryInfo(category);
     }
     
     public Integer getType(){
     	return categoryType;
     }
     
     // Accessors
     public String getName(){
         return getCategoryName(categoryType);
     }
     
     public HashMap<String, Double> getLabels(){
     	return labels;
     }
 
     // Mutators
 
     @Override
     public synchronized void updateInputLink(Identifier parentIdentifier)
     {
     	if(visualID == null){
     		visualID = parentIdentifier.CreateIdWME("visual-prop");
     		nameWME = visualID.CreateStringWME("category", getName());
     	}
     	
     	Set<String> labelsToDestroy = new HashSet<String>();
     	for(String label : labelWMEs.keySet()){
     		labelsToDestroy.add(label);
     	}
     	
     	for(Map.Entry<String, Double> label : labels.entrySet()){
     		if(labelsToDestroy.contains(label.getKey())){
     			// That WME already exists, update it
     			labelsToDestroy.remove(label.getKey());
     			FloatElement labelWME = labelWMEs.get(label.getKey());
     			if(labelWME.GetValue() != label.getValue()){
     				labelWME.Update(label.getValue());
     			}
     		} else {
     			labelWMEs.put(label.getKey(), visualID.CreateFloatWME(label.getKey(), label.getValue()));
     		}
     	}
     	
     	for(String label : labelsToDestroy){
     		labelWMEs.get(label).DestroyWME();
     		labelWMEs.remove(label);
     	}
     }
 
     @Override
     public synchronized void destroy()
     {
     	if(visualID != null){
     		for(Map.Entry<String, FloatElement> wme : labelWMEs.entrySet()){
     			wme.getValue().DestroyWME();
     		}
     		labelWMEs.clear();
     		nameWME.DestroyWME();
     		nameWME = null;
     		visualID.DestroyWME();
     		visualID = null;
     	}
     }
     
     public synchronized void updateCategoryInfo(categorized_data_t category){
     	if(category.cat.cat != categoryType){
     		return;
     	}
     	Set<String> labelsToRemove = new HashSet<String>();
     	for(String label : labels.keySet()){
     		labelsToRemove.add(label);
     	}
     	for(int i = 0; i < category.len; i++){
     		if(labelsToRemove.contains(category.label[i].toLowerCase())){
     			labelsToRemove.remove(category.label[i].toLowerCase());
     		}
 			labels.put(category.label[i].toLowerCase(), category.confidence[i]);
			// AM: only consider the first one
			break;
     	}
     	for(String label : labelsToRemove){
     		labels.remove(label);
     	}
     }
 }
