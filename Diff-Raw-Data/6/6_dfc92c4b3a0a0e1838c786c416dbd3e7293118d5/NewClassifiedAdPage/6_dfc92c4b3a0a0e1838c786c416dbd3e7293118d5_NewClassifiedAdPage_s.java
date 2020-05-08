 /*
  * NewClassifiedAd.java
  * Copyright (c) 2009, Monte Alto Research Center, All Rights Reserved.
  *
  * This software is the confidential and proprietary information of
  * Monte Alto Research Center ("Confidential Information"). You shall not
  * disclose such Confidential Information and shall use it only in
  * accordance with the terms of the license agreement you entered into
  * with Monte Alto Research Center
  */
 package com.marc.lastweek.web.pages.classifiedad;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import org.apache.wicket.PageParameters;
 import org.apache.wicket.markup.html.basic.Label;
 import org.apache.wicket.markup.html.form.Form;
 import org.apache.wicket.markup.html.form.RequiredTextField;
 import org.apache.wicket.markup.html.form.SubmitLink;
 import org.apache.wicket.markup.html.form.TextArea;
 import org.apache.wicket.markup.html.form.TextField;
 import org.apache.wicket.markup.html.link.Link;
 import org.apache.wicket.markup.html.list.ListItem;
 import org.apache.wicket.markup.html.list.ListView;
 import org.apache.wicket.markup.html.panel.FeedbackPanel;
 import org.apache.wicket.markup.html.panel.Panel;
 import org.apache.wicket.model.LoadableDetachableModel;
 import org.apache.wicket.model.Model;
 import org.apache.wicket.validation.validator.EmailAddressValidator;
 
 import com.marc.lastweek.business.entities.category.Category;
 import com.marc.lastweek.business.entities.category.Subcategory;
 import com.marc.lastweek.business.views.commons.NewClassifiedAdAndUserDataTO;
 import com.marc.lastweek.web.application.LastweekApplication;
 import com.marc.lastweek.web.pages.BasePage;
 import com.marc.lastweek.web.util.ResourceUtils;
 
 public class NewClassifiedAdPage extends BasePage {
 
 
 	private static final long serialVersionUID = -7976014359463033532L;
 
 
 	protected NewClassifiedAdAndUserDataTO newClassifiedAdTO = new NewClassifiedAdAndUserDataTO();
 	protected CategoryPanel categoryPanel;
 	protected SubcategoryPanel subcategoryPanel;
 	protected DescriptionPanel descriptionPanel;
 	protected UserDataPanel userDataPanel;
 
 	private class CategoryPanel extends Panel {
 
 		private static final long serialVersionUID = -1266334687818119105L;
 
 		public CategoryPanel(String id) {
 			super(id);
 			add(new ListView("category", new LoadableDetachableModel(){
 
 				private static final long serialVersionUID = 23432234242344232L;
 				@Override
 				protected Object load() {
 					return LastweekApplication.get().getGeneralService().findAll(Category.class);
 				}
 
 			}){
 
				private static final long serialVersionUID = 1L;
 
 				@Override
 				protected void populateItem(ListItem item) {
 					Category  category = (Category)item.getModelObject();
 					final Long categoryId = category.getId();
 					final String categoryName = category.getName();
 					Link categoryLink = new Link("categoryLink") {
 
 						private static final long serialVersionUID = -1462588672710233585L;
 
 
 						@Override
 						public void onClick() {
 							NewClassifiedAdPage.this.newClassifiedAdTO.setCategoryId(categoryId);
 							CategoryPanel.this.setVisible(false);
 							NewClassifiedAdPage.this.subcategoryPanel.setCategoryId(categoryId);
 							NewClassifiedAdPage.this.subcategoryPanel.setVisible(true);
 						}
 
 					};
 
 					categoryLink.add(new Label("categoryLabel",categoryName));
 					item.add(categoryLink);
 				}
 
 			});
 		}
 
 	}
 
 
 	private class SubcategoryPanel extends Panel {
 
 		private static final long serialVersionUID = 7478601607373577524L;
 		protected Long categoryId = null;
 
 		public  SubcategoryPanel(final String id) {
 			super(id);
 			SubcategoryPanel.this.setVisible(false);
 			add(new ListView("subcategory",new LoadableDetachableModel() {
 
 				private static final long serialVersionUID = 5737843643543228915L;
 
 				@Override
 				protected Object load() {
 						Map<String,Object> parameters = new HashMap<String,Object>();
 						parameters.put("categoryId",SubcategoryPanel.this.categoryId);
 						return LastweekApplication.get().getGeneralService().queryForList(Subcategory.class, "findSubcategoriesByCategoryId",parameters);					
 				}
 			}){
 
				private static final long serialVersionUID = 1L;
 
 				@Override
 				protected void populateItem(final ListItem item) {
 					Subcategory subcategory = (Subcategory)item.getModelObject();
 					final Long subcategoryId = subcategory.getId();
 					final String subcategoryName = subcategory.getName();
 					Link categoryLink = new Link("subcategoryLink") {
 
 						private static final long serialVersionUID = -1462588672710233585L;
 
 						@Override
 						public void onClick() {
 							NewClassifiedAdPage.this.newClassifiedAdTO.setSubcategoryId(subcategoryId);
 							SubcategoryPanel.this.setVisible(false);
 							NewClassifiedAdPage.this.descriptionPanel.setVisible(true);
 
 						}
 
 					};				
 					categoryLink.add(new Label("subcategoryLabel",subcategoryName));
 					item.add(categoryLink);
 
 				}
 
 			});
 
 		}
 		public void setCategoryId(Long categoryId){
 			this.categoryId = categoryId;
 		}
 	}
 	
 	
 	
 	private class DescriptionPanel extends Panel {
 
 		private static final long serialVersionUID = 3616303310835436664L;
 
 		public  DescriptionPanel(final String id) {
 			super(id);
 			DescriptionPanel.this.setVisible(false);							
  			add(new DescriptionForm("descriptionForm"));
 		}
 	
 	}
 	
 	
 	private class UserDataPanel extends Panel {
 
 		private static final long serialVersionUID = -7322178890866291550L;
 
 		public  UserDataPanel(final String id) {
 			super(id);
 			UserDataPanel.this.setVisible(false);							
  			add(new UserDataForm("userDataForm"));
 		}
 	
 	}
 
 	private class DescriptionForm extends Form {
 		private static final long serialVersionUID = 9053897905303403343L;
 		protected final TextField price;
 		protected final RequiredTextField title;
 		protected final TextArea description;
 	    
 
 	    public DescriptionForm(String id) {
 	        super(id);
 	        this.price = new TextField("price", new Model(NewClassifiedAdPage.this.newClassifiedAdTO.getPrice()), Double.class);
 	        this.price.setLabel(ResourceUtils.createResourceModel("newclassifiedad.form.price", NewClassifiedAdPage.this)); 
 
 	        this.title = new RequiredTextField("title", new Model(NewClassifiedAdPage.this.newClassifiedAdTO.getTitle()));
 	        this.description = new TextArea("description", new Model(NewClassifiedAdPage.this.newClassifiedAdTO.getDescription()));
 	        this.description.setRequired(true);
 	        add(this.price);
 	        add(this.title);
 	        add(this.description);
 	        add(new SubmitLink("submitLink"){
 
 				private static final long serialVersionUID = 8056648125766325902L;
 
 				@Override
 				public void onSubmit() {
 					NewClassifiedAdPage.this.newClassifiedAdTO.setPrice(new Double(DescriptionForm.this.price.getModelObjectAsString()));
 			    	NewClassifiedAdPage.this.newClassifiedAdTO.setTitle(DescriptionForm.this.title.getModelObjectAsString());
 			    	NewClassifiedAdPage.this.newClassifiedAdTO.setDescription(DescriptionForm.this.description.getModelObjectAsString());
 			    	NewClassifiedAdPage.this.descriptionPanel.setVisible(false);
 					NewClassifiedAdPage.this.userDataPanel.setVisible(true);
 				}
 	        	
 	        });
 	    }
 	}
 	
 	private class UserDataForm extends Form {
 		private static final long serialVersionUID = 9053897905303403343L;
 		protected final RequiredTextField email;
 		protected final TextField phone;
 	    
 
 	    public UserDataForm(String id) {
 	        super(id);
 	        this.phone = new TextField("phone", new Model(NewClassifiedAdPage.this.newClassifiedAdTO.getPhone()), String.class);
 
 	        this.email = new RequiredTextField("email", new Model(NewClassifiedAdPage.this.newClassifiedAdTO.getEmail()));
 	        this.email.add(EmailAddressValidator.getInstance());
 	        this.email.setRequired(true);
 	        
 	        add(this.phone);
 	        add(this.email);
 	        add(new SubmitLink("submitUserDataLink"){
 
 				private static final long serialVersionUID = 8056648125766325902L;
 
 				@Override
 				public void onSubmit() {
 					NewClassifiedAdPage.this.newClassifiedAdTO.setPhone(UserDataForm.this.phone.getModelObjectAsString());
 			    	NewClassifiedAdPage.this.newClassifiedAdTO.setEmail(UserDataForm.this.email.getModelObjectAsString());
 			    	NewClassifiedAdPage.this.userDataPanel.setVisible(false);
 				}
 	        	
 	        });
 	    }
 	}
 
 
 	public NewClassifiedAdPage() {
 		super();	
 		this.categoryPanel = new CategoryPanel("categoryPanel");
 		add(this.categoryPanel);
 		this.subcategoryPanel = new SubcategoryPanel("subcategoryPanel");
 		add(this.subcategoryPanel);
 		this.descriptionPanel = new DescriptionPanel("descriptionPanel");
 		add(this.descriptionPanel);
 		this.userDataPanel = new UserDataPanel("userDataPanel");
 		add(this.userDataPanel);
 		
 		
 		
 		final FeedbackPanel feedback = new FeedbackPanel("feedbackPanel");
 		feedback.setOutputMarkupId(true);
 		add(feedback);
 	}
 
 
 
 
 	public NewClassifiedAdPage(PageParameters pageParameters) {
 		super(pageParameters);
 		// TODO Auto-generated constructor stub
 	}
 
 
 
 
 }
