 package org.primefaces.examples.moviecollector.view;
 
 import java.io.Serializable;
 import java.util.Iterator;
 
 import javax.faces.application.FacesMessage;
 import javax.faces.component.UIComponent;
 import javax.faces.component.UIViewRoot;
 import javax.faces.context.FacesContext;
 import javax.faces.event.ActionEvent;
 
 import org.hamcrest.core.Is;
 import org.primefaces.examples.moviecollector.beans.CategoryBean;
 import org.primefaces.examples.moviecollector.domain.Category;
 import org.primefaces.examples.moviecollector.service.CategoryService;
 import org.primefaces.model.DefaultTreeNode;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.context.annotation.Scope;
 import org.springframework.stereotype.Component;
 
 @Component("createCategory")
 @Scope("request")
 public class CreateCategory implements Serializable {
 
 	private Category category = new Category();
 	
 	private String test;
 
 	private DefaultTreeNode selectedNode;
 	
 	private CategoryService categoryService;
 
 	public CreateCategory() {
 	}
 
 	@Autowired
 	public CreateCategory(CategoryService categoryService) {
 		this.categoryService = categoryService;
 	}
 
 	public void save(ActionEvent actionEvent) {
 		FacesContext context = FacesContext.getCurrentInstance();
 		CategoryBean bean = (CategoryBean) context.getApplication().evaluateExpressionGet(context, "#{categoryBean}", CategoryBean.class);
 		
 		if (selectedNode != null) {
 			category.setParent((Category) selectedNode.getData());
			new DefaultTreeNode(category, selectedNode);
 			selectedNode.setExpanded(true);
 		}
 		else {
 			new DefaultTreeNode(category, bean.getRoot());	
 		}
 		
 		
 		
 		categoryService.createNew(category);
 		FacesMessage facesMessage = new FacesMessage(
 				FacesMessage.SEVERITY_INFO, "Info", "Category is saved");
 		FacesContext.getCurrentInstance().addMessage(null, facesMessage);
 		category = new Category();
 	}
 
 
 	
 	private UIComponent findComponent(UIComponent parent, String id) {
 		if (id.equals(parent.getId())) {
 			return parent;
 		}
 		Iterator<UIComponent> kids = parent.getFacetsAndChildren();
 		while (kids.hasNext()) {
 			UIComponent kid = kids.next();
 			UIComponent found = findComponent(kid, id);
 			if (found != null) {
 				return found;
 			}
 		}
 		return null;
 	}
 
 	public void setCategory(Category category) {
 		this.category = category;
 	}
 
 	public Category getCategory() {
 		return category;
 	}
 
 	public void setTest(String test) {
 		this.test = test;
 	}
 
 	public String getTest() {
 		return test;
 	}
 
 	public void setSelectedNode(DefaultTreeNode selectedNode) {
 		this.selectedNode = selectedNode;
 	}
 
 	public DefaultTreeNode getSelectedNode() {
 		return selectedNode;
 	}
 
 }
