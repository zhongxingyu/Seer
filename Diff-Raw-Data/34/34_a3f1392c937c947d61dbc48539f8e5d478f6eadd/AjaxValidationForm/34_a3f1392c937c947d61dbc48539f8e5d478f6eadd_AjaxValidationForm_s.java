 package com.madalla.wicket.form;
 
 import org.apache.wicket.Component;
 import org.apache.wicket.ajax.AjaxRequestTarget;
 import org.apache.wicket.behavior.Behavior;
 import org.apache.wicket.extensions.ajax.markup.html.IndicatingAjaxButton;
 import org.apache.wicket.feedback.ContainerFeedbackMessageFilter;
 import org.apache.wicket.feedback.IFeedbackMessageFilter;
 import org.apache.wicket.markup.html.form.Form;
 import org.apache.wicket.markup.html.form.FormComponent;
 import org.apache.wicket.markup.html.panel.FeedbackPanel;
 import org.apache.wicket.model.IModel;
 import org.apache.wicket.util.visit.IVisit;
 import org.apache.wicket.util.visit.IVisitor;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * Extend this to add Ajax submit behaviour to your form.
  * <p>
  * NOTE : You will need to add a <em>formSubmit</em> and a <em>formFeedback</em> to
  * your markup for the form.
  * </p>
  * <p>
  * The Form will give you an Ajax submit button as well as enabling inputs and feedbacks
  * to work better with Ajax.
  * </p>
  *
  * @author Eugene Malan
  *
  * @param <T>
  */
 public abstract class AjaxValidationForm<T> extends Form<T>  {
 
 
 	private static final long serialVersionUID = 1L;
 
 	private Logger log = LoggerFactory.getLogger(this.getClass());
 
 	public AjaxValidationForm(String id) {
 		super(id);
 		init();
 	}
 
 	public AjaxValidationForm(String id, IModel<T> model) {
 		super(id, model);
 		init();
 	}
 
 	protected FeedbackPanel addFeedbackPanel(){
 		IFeedbackMessageFilter filter = new ContainerFeedbackMessageFilter(this);
 		final FeedbackPanel formFeedback = new FeedbackPanel("formFeedback", filter);
 		 formFeedback.setMaxMessages(1);
 		add(formFeedback);
 		return formFeedback;
 	}
 
 	private void init(){
 
 		final FeedbackPanel formFeedback = addFeedbackPanel();
 
 		add(new IndicatingAjaxButton("formSubmit", this){
 			private static final long serialVersionUID = 1L;
 
 			@Override
 			protected void onSubmit(final AjaxRequestTarget target, Form<?> form) {
 
 				//visit all form components and re-render by adding to target
 				form.visitFormComponents(new IVisitor<FormComponent<?>, Void>(){
 
 					public void component(FormComponent<?> formComponent, IVisit<Void> visit) {
 						log.debug("onSubmit.formVisiter - " + formComponent + formComponent.getModelObject());
 
 						if (formComponent.isValid() && formComponent.getOutputMarkupId()){
 							target.add(formComponent);
 						}
 						
 					}
 
 				});
 
 				target.add(formFeedback);
 				AjaxValidationForm.this.onSubmit(target);
 
 			}
 
 			@Override
 			protected void onError(final AjaxRequestTarget target, Form<?> form) {
 				log.debug("Ajax onError called");
 
 				form.visitFormComponents(new IVisitor<FormComponent<?>, Void>() {
 
 					public void component(FormComponent<?> formComponent, IVisit<Void> visit) {
 						log.debug("onError.formVisiter - " + formComponent);
 
 						if (!formComponent.isValid() && formComponent.getOutputMarkupId()) {
 							target.add(formComponent);
 						}
 						
 					}
 
 				});
 				target.add(formFeedback);
 			}
 		});
 	}
 
 	public void reset(final AjaxRequestTarget target){
 
 		this.visitChildren(new IVisitor<Component, Void>(){
 
 			public void component(Component component, IVisit<Void> visit) {
 
 				if (component instanceof FormComponent && component.getOutputMarkupId()) {
 					log.debug("reset.formVisiter - " + component);
 					FormComponent<?> formComponent = (FormComponent<?>) component;
 					for (Behavior item : formComponent.getBehaviors()){
 						if (item instanceof AjaxValidationBehaviour){
 							((AjaxValidationBehaviour) item).reset(target);
 						}
 					}
                 } else if (component instanceof FeedbackPanel && component.getOutputMarkupId()){
                 	log.debug("reset.formVisiter - " + component);
 					target.add(component);
                 }
 				
 			}
 
 		});
 
 		//target.prependJavascript("var f Wicket.$(" + getMarkupId() + "); if (f != null){ f.reset();}");
 	}
 
 	@Override
 	protected void onBeforeRender() {
 
 		this.visitChildren(new IVisitor<Component, Void>(){
 
 			public void component(Component component, IVisit<Void> visit) {
 				log.debug("form visitChildren - "+ component);
 				if (component instanceof IAjaxValidationComponent && component instanceof FormComponent<?>) {
 					log.debug("adding validation style behaviour. " + component);
 					component.add(new ValidationStyleBehaviour());
 					component.setOutputMarkupId(true);
                 } else if (component instanceof FeedbackPanel){
                     log.debug("keep reference to this feedbackPanel." + component);
                     component.setOutputMarkupId(true);
                 }
 				
 			}
 
 		});
 
 		super.onBeforeRender();
 	}

 	protected abstract void onSubmit(final AjaxRequestTarget target) ;
 
 
 
 
 }
