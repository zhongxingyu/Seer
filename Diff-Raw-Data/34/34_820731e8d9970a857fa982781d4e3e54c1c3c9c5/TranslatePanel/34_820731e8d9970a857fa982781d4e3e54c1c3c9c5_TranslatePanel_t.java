 package com.madalla.webapp.cms.editor;
 
 import java.util.List;
 import java.util.Locale;
 import java.util.Map;
 
 import org.apache.commons.lang.StringEscapeUtils;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.apache.wicket.ajax.AjaxRequestTarget;
 import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
 import org.apache.wicket.markup.html.JavascriptPackageResource;
 import org.apache.wicket.markup.html.basic.Label;
 import org.apache.wicket.markup.html.form.ChoiceRenderer;
 import org.apache.wicket.markup.html.form.DropDownChoice;
 import org.apache.wicket.model.Model;
 import org.apache.wicket.util.template.TextTemplateHeaderContributor;
 
 import tiny_mce.TinyMce;
 
 import com.madalla.bo.SiteLanguage;
 import com.madalla.bo.page.ContentData;
 import com.madalla.bo.page.ContentEntryData;
 import com.madalla.bo.page.PageData;
 import com.madalla.webapp.CmsSession;
 import com.madalla.webapp.panel.CmsPanel;
 
 /**
  * Translate Panel - View translate from text and edit the translate text. Also translate functionality.
  * 
  * <p>
  * Panel uses the TinyMCE WYSIWYG Editor. Translate functionality is supplied
  * using Googles translate service.
  * </p>
  * 
  * @author Eugene Malan
  * 
  */
 public class TranslatePanel extends CmsPanel {
 
 	private static final long serialVersionUID = 1L;
 
 	private Log log = LogFactory.getLog(this.getClass());
 
 	
 	public TranslatePanel(String name, final String nodeName, final String contentId) {
 		super(name);
 		
 		add(JavascriptPackageResource.getHeaderContribution(TinyMce.class, "tiny_mce.js"));
 				
 		//setup Javascript template
 		Map<String, Object> vars = EditorSetup.setupTemplateVariables((CmsSession) getSession());
 		add(TextTemplateHeaderContributor.forJavaScript(EditorSetup.class,"EditorSetup.js", Model.ofMap(vars)));
 		
         PageData page = getRepositoryService().getPage(nodeName);
         final ContentData content = getRepositoryService().getContent(page, contentId);
         log.debug("init - content" + content);
         
         //Base Language display
         String baseContent = getRepositoryService().getContentText(content, SiteLanguage.BASE_LOCALE);
         Label baseContentLabel = new Label("baseContent", new Model<String>(baseContent));
         baseContentLabel.setOutputMarkupId(true);
         baseContentLabel.setEscapeModelStrings(false);
         add(baseContentLabel);
                 
 		// Supported Languages
 		List<SiteLanguage> locales = SiteLanguage.getLanguages();
 
         //Dynamic destination Language editor
 		final Locale selectedLang = getDefaultLocale(locales);
		log.debug("start lang :" + selectedLang);
        final ContentEntryData contentEntry = getRepositoryService().getContentEntry(content, selectedLang.getDisplayName(), "");
        log.debug("start content entry. " + contentEntry);
         final ContentFormPanel destPanel = new ContentFormPanel("contentEditor", contentEntry );
         destPanel.setOutputMarkupId(true);
 		add(destPanel);
 		
 		//add values to javascript
 		vars.put("sourceDiv", baseContentLabel.getMarkupId());
 		vars.put("destLang", selectedLang);
 		add(TextTemplateHeaderContributor.forJavaScript(this.getClass(),"TranslatePanel.js", Model.ofMap(vars)));
 
 		//Language Selector
 		SiteLanguage selectedLanguage = SiteLanguage.getLanguage(selectedLang.getLanguage());
		
		//set dest Panel to startup language
		ContentEntryData selectedContentEntry = getRepositoryService().getContentEntry(content, selectedLanguage.locale.getDisplayName(), "");
		
		
 		final DropDownChoice<SiteLanguage> select = new DropDownChoice<SiteLanguage>("langSelect", 
 				new Model<SiteLanguage>(selectedLanguage), locales, new ChoiceRenderer<SiteLanguage>("locale.displayLanguage"));
 		select.setNullValid(false);
 		select.add(new AjaxFormComponentUpdatingBehavior("onchange"){
 			private static final long serialVersionUID = 1L;
 
 			@Override
 			protected void onUpdate(AjaxRequestTarget target) {
 				SiteLanguage language = select.getModelObject();
 				log.debug("language select changed - " + language);
 				ContentEntryData newContentEntry = getRepositoryService().getContentEntry(content, language.locale.getDisplayName(), "");
 				log.debug("new Content Entry." + newContentEntry);
 				destPanel.changeContentEntry(newContentEntry);
 				
 				target.appendJavascript("changeLanguage('"+ language.getLanguageCode()+"', '"+
 						StringEscapeUtils.escapeJavaScript(newContentEntry.getText()) + "');");
 			}
 			
 		});
 		add(select);
 
 	}
 	
	
	
	@Override
	protected void onBeforeRender() {
		
		super.onBeforeRender();
	}



	@Override
	protected void onAfterRender() {
		// TODO Auto-generated method stub
		super.onAfterRender();
	}



 	private Locale getDefaultLocale(List<SiteLanguage> locales){
 		List<SiteLanguage> configuredLangs = getRepositoryService().getSiteData().getLocaleList();
 		if (configuredLangs.isEmpty()){
 			return locales.get(0).locale;
 		} else {
 			return configuredLangs.get(0).locale;
 		}
 	}
 
 
 }
 
