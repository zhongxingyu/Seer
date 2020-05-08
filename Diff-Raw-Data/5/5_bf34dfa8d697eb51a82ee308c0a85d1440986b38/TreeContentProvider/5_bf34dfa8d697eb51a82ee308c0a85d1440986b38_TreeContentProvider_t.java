 package de.hswt.hrm.scheme.ui.tree;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.eclipse.jface.viewers.ITreeContentProvider;
 import org.eclipse.jface.viewers.Viewer;
 
 import com.google.common.collect.Lists;
 
 import de.hswt.hrm.scheme.model.Category;
 import de.hswt.hrm.scheme.model.RenderedComponent;
 
 public class TreeContentProvider implements ITreeContentProvider{
     
     private List<RenderedComponent> comps;
 
     @Override
     public void dispose() {
         comps = null;
     }
 
     @SuppressWarnings("unchecked")
     @Override
     public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
         comps = (List<RenderedComponent>) newInput;
     }
 
     @Override
     public Object[] getElements(Object inputElement) {
     	Category[] cats = getCategorys();
     	SchemeTreeItem[] items = new CategoryTreeItem[cats.length];
     	for(int i = 0; i < items.length; i++){
     		items[i] = new CategoryTreeItem(cats[i], getRenderedComponents(cats[i]));
     	}
         return items;
     }
 
     @Override
     public Object[] getChildren(final Object parentElement) {
     	return ((SchemeTreeItem) parentElement).getChildren();
     }
 
     @Override
     public Object getParent(Object element) {
     	return ((SchemeTreeItem) element).getParent();
     }
 
     @Override
     public boolean hasChildren(Object element) {
         return ((SchemeTreeItem) element).hasChildren();
     }
     
     private List<RenderedComponent> getRenderedComponents(Category cat){
     	List<RenderedComponent> result = new ArrayList<RenderedComponent>();
     	for(RenderedComponent c : comps){
     		if(c.getComponent().getCategory().equals(cat)){
     			result.add(c);
     		}
     	}
     	return result;
     }
     
     private Category[] getCategorys(){
         List<Category> cats = Lists.newArrayList();
         for(RenderedComponent c : comps){
        	final Category category = c.getComponent().getCategory();
        	if(!cats.contains(category)){
        		cats.add(category);
        	}
         }
         Category[] r = new Category[cats.size()];
         cats.toArray(r);
         return r;
     }
 
 }
