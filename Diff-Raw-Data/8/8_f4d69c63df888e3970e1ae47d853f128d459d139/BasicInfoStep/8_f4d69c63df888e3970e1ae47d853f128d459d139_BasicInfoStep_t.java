 package com.ecom.web.upload;
 
 import org.apache.wicket.AttributeModifier;
 import org.apache.wicket.Page;
 import org.apache.wicket.ajax.AjaxRequestTarget;
 import org.apache.wicket.ajax.markup.html.AjaxLink;
 import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
 import org.apache.wicket.extensions.markup.html.form.DateTextField;
 import org.apache.wicket.injection.Injector;
 import org.apache.wicket.markup.html.WebMarkupContainer;
 import org.apache.wicket.markup.html.form.CheckBox;
 import org.apache.wicket.markup.html.form.Form;
 import org.apache.wicket.markup.html.form.TextArea;
 import org.apache.wicket.markup.html.form.TextField;
 import org.apache.wicket.markup.html.image.ContextImage;
 import org.apache.wicket.model.AbstractReadOnlyModel;
 import org.apache.wicket.model.CompoundPropertyModel;
 import org.apache.wicket.model.IModel;
 import org.apache.wicket.model.Model;
 import org.apache.wicket.request.mapper.parameter.PageParameters;
 import org.apache.wicket.request.resource.ResourceReference;
 import org.apache.wicket.spring.injection.annot.SpringBean;
 import org.apache.wicket.validation.validator.StringValidator;
 import org.bson.types.ObjectId;
 
 import com.ecom.domain.RealState;
 import com.ecom.repository.RealStateRepository;
 import com.ecom.web.components.image.EcomImageResouceReference;
 import com.ecom.web.components.image.StaticImage;
 import com.ecom.web.components.wizard.WizardStep;
 import com.ecom.web.data.DetachableRealStateModel;
 
 public class BasicInfoStep extends WizardStep {
 
 	private static final long serialVersionUID = 769908228950127137L;
 
 	private WebMarkupContainer titleImageContainer;
 
 	@SpringBean
 	private RealStateRepository realStateRepository;
 
 	public BasicInfoStep(IModel<String> wizard_title, IModel<String> summary, final IModel<ObjectId> realStateIdModel) {
 		super(wizard_title, summary);
 		Injector.get().inject(this);
 		
 		titleImageContainer = new WebMarkupContainer("titleImageContainer");
 		titleImageContainer.setOutputMarkupId(true);
 
 		final RealState realState = new RealState();
 		realState.setId(realStateIdModel.getObject());
 		
 		CompoundPropertyModel<RealState> realStateModel = new CompoundPropertyModel<RealState>(realState);
 		
 		final Form<RealState> realStateUploadInfoForm = new Form<RealState>("realStateAdvertForm", realStateModel) {
 
 			private static final long serialVersionUID = 1L;
 
 			@Override
 			public final void onSubmit() {
 
 			    RealState realStateFromDB = realStateRepository.findOne(this.getModelObject().getId());
 			    RealState realStateFromGUI = this.getModelObject();
 			    
 			    if (realStateFromGUI != null && realStateFromDB != null && !realStateFromDB.getTitleImages().isEmpty()) {
 			        realStateFromGUI.addTitleImages(realStateFromDB.getTitleImages());
			        realStateRepository.delete(this.getModelObject().getId());
 			        realStateRepository.save(realStateFromGUI);
 			    }
 			}
 		};
 		realStateUploadInfoForm.setOutputMarkupId(true);
 		
 		titleImageContainer.add(new ContextImage("title_image", new Model<String>("images/no_photo_icon.gif")));
 		realStateUploadInfoForm.add(titleImageContainer);
 		
 		final ModalWindow modalWindow;
 		modalWindow = new ModalWindow("titleUploadFileModalWindow");
 		modalWindow.setCssClassName(ModalWindow.CSS_CLASS_BLUE);
 		modalWindow.setCookieName("titleUploadFileModalWindow");
 		modalWindow.setInitialHeight(130);
 		modalWindow.setInitialWidth(190);
 		modalWindow.setPageCreator(new ModalWindow.PageCreator() {
 
 			private static final long serialVersionUID = 1L;
 
 			@Override
 			public Page createPage() {
 				return new TitleImageUploadPage(modalWindow, realStateIdModel);
 			}
 		});
 
 		modalWindow.setCloseButtonCallback(new ModalWindow.CloseButtonCallback() {
 
 			private static final long serialVersionUID = 1L;
 
 			@Override
 			public boolean onCloseButtonClicked(AjaxRequestTarget target) {
 				RealState realStateDetachModel = new DetachableRealStateModel(realStateIdModel.getObject()).getObject();
 
 				if (realStateDetachModel != null) {
 					ResourceReference imagesResourceReference = new EcomImageResouceReference();
 					PageParameters imageParameters = new PageParameters();
 					String imageId = realStateDetachModel.getTitleThumbNailImage();
 					imageParameters.set("id", imageId);
 		
 					// generates nice looking url (the mounted one) to the current image
 					CharSequence urlForImage = getRequestCycle().urlFor(imagesResourceReference, imageParameters);
 										
 					titleImageContainer.addOrReplace(new StaticImage("title_image", new Model<String>(urlForImage.toString())));
 					target.add(titleImageContainer);
 				}
 
 				return true;
 			}
 		});
 
 		realStateUploadInfoForm.add(new AjaxLink<Void>("uploadTitleImage") {
 
 			private static final long serialVersionUID = 1L;
 
 			@Override
 			public void onClick(AjaxRequestTarget target) {
 				modalWindow.show(target);
 			}
 		});
 
 		realStateUploadInfoForm.add(modalWindow);
 		final TextArea<String> title = new TextArea<String>("title");
 		title.setRequired(true);
 		title.add(StringValidator.maximumLength(150));
 		title.setOutputMarkupId(true);
 		
 //        final FeedbackLabel nameFeedbackLabel = new FeedbackLabel("title.error", title);
 //        nameFeedbackLabel.setOutputMarkupId(true);
 //        realStateUploadInfoForm.add(nameFeedbackLabel);
 
 		title.add(new AttributeModifier("class", new AbstractReadOnlyModel<String>() {
 
 			private static final long serialVersionUID = 1L;
 
 				@Override
             public String getObject() {
                 if (!title.isValid()) {
                     return "formcomponent invalid";
                 } else {
                     return "formcomponent valid";
                 }
                 
             }
         }));
 		
 		TextArea<String> description = new TextArea<String>("description");
 		description.setRequired(true);
 		description.add(StringValidator.maximumLength(600));
 		
 		TextArea<String> areaDescription = new TextArea<String>("areaDescription");
 		
 		TextArea<String> fittings = new TextArea<String>("fittings");
 		
 		TextField<String> city = new TextField<String>("city");
 		city.setRequired(true);
 		
 		TextField<String> areaCode = new TextField<String>("areaCode");
 		areaCode.setRequired(true);
 		
 		TextField<String> street = new TextField<String>("street");
 		TextField<String> houseNo = new TextField<String>("houseNo");
 		TextField<Double> size = new TextField<Double>("size");
 		TextField<Double> cost = new TextField<Double>("cost");
 		TextField<Double> floor = new TextField<Double>("floor");
 		TextField<Double> totalFloors = new TextField<Double>("totalFloors");
 		TextField<Double> totalRooms = new TextField<Double>("totalRooms");
 		TextField<Integer> bedRooms = new TextField<Integer>("bedRooms");
 		TextField<Integer> bathRooms = new TextField<Integer>("bathRooms");
 		CheckBox toiletWithBathRoom = new CheckBox("toiletWithBathRoom");
 		CheckBox cellarAvailable = new CheckBox("cellarAvailable");
 		CheckBox balconyAvailable = new CheckBox("balconyAvailable");
 		CheckBox liftAvailable = new CheckBox("liftAvailable");
 		CheckBox gardenAvailable = new CheckBox("gardenAvailable");
 		CheckBox heatingCostIncluded = new CheckBox("heatingCostIncluded");
 		CheckBox energyPassAvailable = new CheckBox("energyPassAvailable");
 		CheckBox kitchenAvailable = new CheckBox("kitchenAvailable");
 		CheckBox furnished = new CheckBox("furnished");
 		CheckBox animalsAllowed = new CheckBox("animalsAllowed");
 		CheckBox garageAvailable = new CheckBox("garageAvailable");
 		TextField<Double> additionalCost = new TextField<Double>("additionalCost");
 		TextField<Double> depositPeriod = new TextField<Double>("depositPeriod");
 		DateTextField availableFrom = new DateTextField("availableFrom");
 		TextField<Double> garageCost = new TextField<Double>("garageCost");
 		TextField<Integer> builtYear = new TextField<Integer>("builtYear");
 		TextField<String> provisionCondition = new TextField<String>("provisionCondition");
 		TextField<String> lastRenovatedYear = new TextField<String>("lastRenovatedYear");
 		CheckBox provisionFree = new CheckBox("provisionFree");
 		CheckBox barrierFree = new CheckBox("barrierFree");
 		CheckBox seniorAppartment = new CheckBox("seniorAppartment");
 
 		realStateUploadInfoForm.add(title);
 		realStateUploadInfoForm.add(description);
 		realStateUploadInfoForm.add(areaDescription);
 		realStateUploadInfoForm.add(fittings);
 		realStateUploadInfoForm.add(city);
 		realStateUploadInfoForm.add(areaCode);
 		realStateUploadInfoForm.add(street);
 		realStateUploadInfoForm.add(houseNo);
 		realStateUploadInfoForm.add(size);
 		realStateUploadInfoForm.add(cost);
 		realStateUploadInfoForm.add(floor);
 		realStateUploadInfoForm.add(totalFloors);
 		realStateUploadInfoForm.add(totalRooms);
 		realStateUploadInfoForm.add(bedRooms);
 		realStateUploadInfoForm.add(bathRooms);
 		realStateUploadInfoForm.add(toiletWithBathRoom);
 		realStateUploadInfoForm.add(cellarAvailable);
 		realStateUploadInfoForm.add(balconyAvailable);
 		realStateUploadInfoForm.add(liftAvailable);
 		realStateUploadInfoForm.add(gardenAvailable);
 		realStateUploadInfoForm.add(heatingCostIncluded);
 		realStateUploadInfoForm.add(energyPassAvailable);
 		realStateUploadInfoForm.add(provisionCondition);
 		realStateUploadInfoForm.add(kitchenAvailable);
 		realStateUploadInfoForm.add(furnished);
 		realStateUploadInfoForm.add(animalsAllowed);
 		realStateUploadInfoForm.add(garageAvailable);
 
 		// private int typeId;
 		//
 		// private int condition;
 		//
 		// private int categoryId;
 		//
 		// private int heatingTypeId;
 
 		realStateUploadInfoForm.add(additionalCost);
 		realStateUploadInfoForm.add(depositPeriod);
 		realStateUploadInfoForm.add(availableFrom);
 		realStateUploadInfoForm.add(garageCost);
 		realStateUploadInfoForm.add(builtYear);
 		realStateUploadInfoForm.add(lastRenovatedYear);
 		realStateUploadInfoForm.add(provisionFree);
 		realStateUploadInfoForm.add(barrierFree);
 		realStateUploadInfoForm.add(seniorAppartment);
 		add(realStateUploadInfoForm);
 	}
 
 
 }
