 package cz.cvut.fel.jee.labEshop.web.brand;
 
 import java.io.Serializable;
 import java.util.List;
 
 import javax.ejb.Stateful;
 import javax.faces.bean.ViewScoped;
 import javax.faces.component.UIInput;
 import javax.faces.component.UIViewRoot;
 import javax.faces.context.FacesContext;
 import javax.inject.Inject;
 import javax.inject.Named;
 
 import org.jboss.seam.international.status.Messages;
 import org.slf4j.Logger;
 
 import cz.cvut.fel.jee.labEshop.manager.BrandManager;
 import cz.cvut.fel.jee.labEshop.model.Brand;
 
 /**
  * Brands JSF controller.
  * 
  * @author Kamil Prochazka (<a href="mailto:prochka6@fel.cvut.cz">prochka6</a>)
  */
 @Named("brandsBean")
 @ViewScoped
 @Stateful
 public class BrandsBean implements Serializable {
 
 	private static final long serialVersionUID = 1L;
 
 	@Inject
 	private Logger log;
 
 	@Inject
 	private Messages messages;
 
 	@Inject
 	private BrandManager brandManager;
 
 	private transient List<Brand> brands;
 
 	private Brand selected;
 
 	public String submit() {
 		if (selected == null) {
 			throw new IllegalStateException();
 		}
 
 		if (isNew()) {
 			brandManager.createBrand(selected);
 			log.info("Brand {} created.", selected);
			messages.info("Brand created.");
 		} else {
 			selected = brandManager.updateBrand(selected);
			log.info("Brand [id={}] {} updated.", selected.getId(), selected);
			messages.info("Brand updated.");
 		}
 
 		resetForm();
 		selected = null;
 		brands = null;
 		return null;
 	}
 
 	public String newBrand() {
 		resetForm();
 		selected = new Brand();
 
 		return null;
 	}
 
 	public String selectBrand(Brand brand) {
 		resetForm();
 		selected = brand;
 
 		return null;
 	}
 
 	private void resetForm() {
 		UIViewRoot root = FacesContext.getCurrentInstance().getViewRoot();
 		((UIInput) root.findComponent(":detailForm:brandName")).resetValue();
 		((UIInput) root.findComponent(":detailForm:brandComment")).resetValue();
 	}
 
 	public Brand getSelected() {
 		return selected;
 	}
 
 	public void setSelected(Brand selected) {
 		this.selected = selected;
 	}
 
 	public List<Brand> getBrands() {
 		if (brands == null) {
 			brands = brandManager.findAllBrands();
 		}
 
 		return brands;
 	}
 
 	public boolean isNew() {
 		return selected != null && selected.getId() == null ? true : false;
 	}
 
 	public boolean isUpdate() {
 		return selected != null && selected.getId() != null ? true : false;
 	}
 
 	public boolean isSelection() {
 		return selected != null ? true : false;
 	}
 
 }
