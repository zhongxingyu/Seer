 package cz.cuni.mff.odcleanstore.webfrontend.pages.transformers.dn;
 
 import java.util.List;
 
 import org.apache.log4j.Logger;
 import org.apache.wicket.AttributeModifier;
 import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeInstantiation;
 import org.apache.wicket.markup.html.form.ChoiceRenderer;
 import org.apache.wicket.markup.html.form.DropDownChoice;
 import org.apache.wicket.markup.html.form.Form;
 import org.apache.wicket.markup.html.form.TextArea;
 import org.apache.wicket.model.CompoundPropertyModel;
 import org.apache.wicket.model.IModel;
 import org.apache.wicket.model.LoadableDetachableModel;
 
 import cz.cuni.mff.odcleanstore.configuration.ConfigLoader;
 import cz.cuni.mff.odcleanstore.webfrontend.bo.Role;
 import cz.cuni.mff.odcleanstore.webfrontend.bo.dn.DNRuleComponent;
 import cz.cuni.mff.odcleanstore.webfrontend.bo.dn.DNRuleComponentType;
 import cz.cuni.mff.odcleanstore.webfrontend.core.components.RedirectWithParamButton;
 import cz.cuni.mff.odcleanstore.webfrontend.dao.dn.DNRuleComponentDao;
 import cz.cuni.mff.odcleanstore.webfrontend.dao.dn.DNRuleComponentTypeDao;
 import cz.cuni.mff.odcleanstore.webfrontend.dao.dn.DNRuleDao;
 import cz.cuni.mff.odcleanstore.webfrontend.dao.exceptions.DaoException;
 import cz.cuni.mff.odcleanstore.webfrontend.pages.LimitedEditingPage;
 import cz.cuni.mff.odcleanstore.webfrontend.validators.DNComponentValidator;
 
 /**
  * Add-new-component-to-a-dn-rule page component.
  * 
  * @author Dušan Rychnovský (dusan.rychnovsky@gmail.com)
  *
  */
 @AuthorizeInstantiation({ Role.PIC })
 public class NewDNRuleComponentPage extends LimitedEditingPage
 {
 	private static final long serialVersionUID = 1L;
 	private static Logger logger = Logger.getLogger(NewDNRuleComponentPage.class);
 
 	private DNRuleComponentDao dnRuleComponentDao;
 	private DNRuleComponentTypeDao dnRuleComponentTypeDao;
 	
	private static String insertHint = "{?s ?p ?o} WHERE {GRAPH $$graph$$ {SELECT ?s ?p 'Y' AS ?o WHERE {?s ?p 1}}}";
	private static String deleteHint = "{?s ?p 1} WHERE {GRAPH $$graph$$ {?s ?p 1}}";
	private static String modifyHint = "DELETE {?s ?p 1} INSERT {?s ?p 'Y'} WHERE {?s ?p 1}";
 	
 	/**
 	 * 
 	 * @param ruleId
 	 */
 	public NewDNRuleComponentPage(Integer ruleId) 
 	{
 		super(
 			"Home > Backend > Data Normalization > Groups > Rules > Components > New", 
 			"Add a new Data Normalization rule component",
 			DNRuleDao.class,
 			ruleId
 		);
 		
 		checkUnathorizedInstantiation();
 
 		// prepare DAO objects
 		//
 		dnRuleComponentDao = daoLookupFactory.getDao(DNRuleComponentDao.class, isEditable());
 		dnRuleComponentTypeDao = daoLookupFactory.getDao(DNRuleComponentTypeDao.class);
 		
 		// register page components
 		//
 		addHelpWindow(new DNRuleComponentHelpPanel("content"));
 		
 		add(
 			new RedirectWithParamButton(
 				DNRuleDetailPage.class,
 				ruleId, 
 				"showDNRuleDetailPage"
 			)
 		);
 		
 		addNewComponentForm(ruleId);
 	}
 	
 	/**
 	 * 
 	 * @param ruleId
 	 */
 	private void addNewComponentForm(final Integer ruleId)
 	{
 		IModel<DNRuleComponent> formModel = new CompoundPropertyModel<DNRuleComponent>(new DNRuleComponent());
 		
 		Form<DNRuleComponent> form = new Form<DNRuleComponent>("newDNRuleComponentForm", formModel)
 		{
 			private static final long serialVersionUID = 1L;
 
 			@Override
 			protected void onSubmit()
 			{
 				DNRuleComponent dnRuleComponent = this.getModelObject();
 				dnRuleComponent.setRuleId(ruleId);
 				
 				try {
 					dnRuleComponentDao.save(dnRuleComponent);
 				}
 				catch (DaoException ex)
 				{	
 					logger.error(ex.getMessage(), ex);
 					getSession().error(ex.getMessage());
 					return;
 				}
 				catch (Exception ex)
 				{
 					logger.error(ex.getMessage(), ex);
 					getSession().error(
 						"The component could not be registered due to an unexpected error."
 					);
 					
 					return;
 				}
 				
 				getSession().info("The component was successfuly registered.");
 				setResponsePage(new DNRuleDetailPage(ruleId));
 			}
 		};
 		
 		final TextArea<String> modification = new TextArea<String>("modification");
 		
 		ChoiceRenderer<DNRuleComponentType> renderer = new ChoiceRenderer<DNRuleComponentType>("label", "id");
 		LoadableDetachableModel<List<DNRuleComponentType>> choices = new LoadableDetachableModel<List<DNRuleComponentType>>() {
 			private static final long serialVersionUID = 1L;
 
 			@Override
 			protected List<DNRuleComponentType> load() {
 				return dnRuleComponentTypeDao.loadAll();
 			}
 		};
 		DropDownChoice<DNRuleComponentType> type = new DropDownChoice<DNRuleComponentType>("type", choices, renderer) {
 			private static final long serialVersionUID = 1L;
 			
 			@Override
 			public void onSelectionChanged(DNRuleComponentType newType) {
 				String hint;
 
 				if (newType.getLabel().equals("INSERT")) {
 					
 					hint = insertHint;
 					
 				} else if (newType.getLabel().equals("DELETE")) {
 					
 					hint = deleteHint;
 					
 				} else {
 					
 					hint = modifyHint;
 					
 				}
 				
 				modification.add(new AttributeModifier("placeholder", hint));
 			}
 			
 			@Override
 			protected boolean wantOnSelectionChangedNotifications() {
 				return true;
 			}
 
 			@Override
 			protected CharSequence getDefaultChoice(String selectedValue)
 			{
 				return !isNullValid() && !getChoices().isEmpty() ? "" : super.getDefaultChoice(selectedValue);
 			}
 		};
 		type.setNullValid(false);
 		type.setRequired(true);
 		form.add(type);
 
 		modification.setRequired(true);
 		modification.add(new DNComponentValidator(ConfigLoader.getConfig().getBackendGroup().getDirtyDBJDBCConnectionCredentials(), type));
 		modification.add(new AttributeModifier("placeholder", insertHint));
 		form.add(modification);
 
 		
 		form.add(createTextarea("description", false));
 		
 		add(form);
 	}
 }
