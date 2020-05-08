 package ar.edu.itba.paw.grupo1.web.pages.PropertyList;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeInstantiation;
 import org.apache.wicket.markup.html.basic.Label;
 import org.apache.wicket.markup.html.link.BookmarkablePageLink;
 import org.apache.wicket.markup.html.link.Link;
 import org.apache.wicket.markup.repeater.Item;
 import org.apache.wicket.markup.repeater.RefreshingView;
 import org.apache.wicket.model.IModel;
 import org.apache.wicket.spring.injection.annot.SpringBean;
 
 import ar.edu.itba.paw.grupo1.model.Property;
 import ar.edu.itba.paw.grupo1.model.support.EntityModel;
 import ar.edu.itba.paw.grupo1.repository.PropertyRepository;
 import ar.edu.itba.paw.grupo1.web.WicketSession;
 import ar.edu.itba.paw.grupo1.web.WicketUtils;
 import ar.edu.itba.paw.grupo1.web.pages.AddProperty.AddPropertyPage;
 import ar.edu.itba.paw.grupo1.web.pages.Base.BasePage;
 import ar.edu.itba.paw.grupo1.web.pages.EditProperty.EditPropertyPage;
 import ar.edu.itba.paw.grupo1.web.pages.PropertyDetail.PropertyDetailPage;
 
 @SuppressWarnings("serial")
 @AuthorizeInstantiation(WicketSession.USER)
 public class PropertyListPage extends BasePage {
 
 	@SpringBean
 	private PropertyRepository properties;
 
 	public PropertyListPage() {
 		
 		add(new BookmarkablePageLink<Void>("newProperty", AddPropertyPage.class));
 		RefreshingView<Property> refreshingView = new RefreshingView<Property>("properties") {
 
 			@Override
 			protected Iterator<IModel<Property>> getItemModels() {
 				List<IModel<Property>> result = new ArrayList<IModel<Property>>();
 				for (Property prop : properties.getProperties(getSignedInUser())) {
 					result.add(new EntityModel<Property>(Property.class, prop));
 				}
 				return result.iterator();
 			}
 
 			@Override
 			protected void populateItem(Item<Property> item) {
 
 				Property property = item.getModelObject();
 				
 				Link<Property> detailLink = new Link<Property>("detail", item.getModel()) {
 					
 				     public void onClick() {
 				          setResponsePage(new PropertyDetailPage(getModelObject()));
 				     }
 				};
 				detailLink.add(new Label("id", property.getId().toString()));
 				item.add(detailLink);
 				item.add(new Label("description", property.getDescription()));
 				
 				Link<Property> editLink = new Link<Property>("edit", item.getModel()) {
 					
 				     public void onClick() {
 				          setResponsePage(new EditPropertyPage(getModelObject()));
 				     }
 				};
 				item.add(editLink);
 				
 				boolean notSold = !property.isSold();
 				Link<Property> publishLink = new Link<Property>("publish", item.getModel()) {
 
 					@Override
 					public void onClick() {
 						getModelObject().publish();
 					}
 				};
 				WicketUtils.addToContainer(item, publishLink, notSold && !property.isPublished());
 				
 				Link<Property> unpublishLink = new Link<Property>("unpublish", item.getModel()) {
 
 					@Override
 					public void onClick() {
 						getModelObject().unpublish();
 					}
 				};
 				WicketUtils.addToContainer(item, unpublishLink, notSold && property.isPublished());
 				
 				Link<Property> reserveLink = new Link<Property>("reserve", item.getModel()) {
 
 					@Override
 					public void onClick() {
 						getModelObject().reserve();
 					}
 				};
 				WicketUtils.addToContainer(item, reserveLink, notSold && !property.isReserved());
 				
 				Link<Property> unreservelink = new Link<Property>("unreserve", item.getModel()) {
 
 					@Override
 					public void onClick() {
 						getModelObject().unreserve();
 					}
 				};
 				WicketUtils.addToContainer(item, unreservelink, notSold && property.isReserved());
 				
 				Link<Property> sellLink = new Link<Property>("sell", item.getModel()) {
 
 					@Override
 					public void onClick() {
 						getModelObject().sell();
 					}
 				};
 				WicketUtils.addToContainer(item, sellLink, notSold);
 			}
 		};
		addLabel("noProperties", !refreshingView.getItems().hasNext());
 		add(refreshingView);
 	}
 	
 	@SuppressWarnings("unchecked")
 	@Override
 	protected void onBeforeRender() {
 		RefreshingView<Property> refreshingView = (RefreshingView<Property>) get("properties");
		get("noProperties").setVisible(!refreshingView.getItems().hasNext());
 		super.onBeforeRender();
 	}
 }
