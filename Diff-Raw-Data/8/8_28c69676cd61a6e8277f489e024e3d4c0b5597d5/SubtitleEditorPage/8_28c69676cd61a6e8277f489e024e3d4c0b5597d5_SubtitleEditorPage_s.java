 package libreSubs.libreSubsSite.editPage;
 
 
 
 import libreSubs.libreSubsSite.ErrorPage;
 import libreSubs.libreSubsSite.SubParameters;
 import libreSubs.libreSubsSite.WicketApplication;
 import libreSubs.libreSubsSite.menuPanel.MenuPanel;
 
 import org.apache.wicket.PageParameters;
 import org.apache.wicket.RestartResponseException;
 import org.apache.wicket.markup.html.WebPage;
 import org.apache.wicket.markup.html.basic.Label;
 import org.apache.wicket.markup.html.form.Form;
 import org.apache.wicket.markup.html.form.TextArea;
 import org.apache.wicket.markup.html.panel.FeedbackPanel;
 import org.apache.wicket.model.CompoundPropertyModel;
 import org.libreSubsCommons.SubtitleResourceResolver;
 import org.wicketstuff.annotation.mount.MountPath;
 
 @MountPath(path = "editSrtPage")
 public class SubtitleEditorPage extends WebPage {
 	
 	
 	String subtitle;
 	SubParameters subParameters;
 
 	@SuppressWarnings("serial")
 	public SubtitleEditorPage(final PageParameters parameters) {
 		final CharSequence idParam = parameters.getCharSequence(SubtitleResourceResolver.idParameter);
 		if(idParam == null){
 			throw new RestartResponseException(new ErrorPage() {				
 				@Override
 				protected String errorMessage() {
 					return "Parametro " + SubtitleResourceResolver.idParameter
							+ " no foi passado.";
 				}
 			}); 
 		}
 		
 		final CharSequence langParam = parameters.getCharSequence(SubtitleResourceResolver.langParameter); 
 		if(langParam == null){
 			throw new RestartResponseException(new ErrorPage() {				
 				@Override
 				protected String errorMessage() {
 					return "Parametro "
 							+ SubtitleResourceResolver.langParameter
							+ " no foi passado.";
 				}
 			});
 		}
 		
 		final String id = idParam.toString();
 		final String lang = langParam.toString();
 		
 		if(!WicketApplication.subtitleExists(id, lang)){
 			throw new RestartResponseException(new ErrorPage() {				
 				@Override
 				protected String errorMessage() {
					return "Legenda no existe.";
 				}
 			});
 		}
 		
 		add(new MenuPanel("menu"));
 		add(new Label("id", id));
 		add(new Label("lang", lang));
 		add(new FeedbackPanel("saveFeedback"));
 		
 		subtitle = WicketApplication.getSubtitleOrNull(id, lang);
 		
 		subParameters = new SubParameters();
 		final Form<String> editForm = new Form<String>("editForm",new CompoundPropertyModel<String>(
 				subParameters)){
 			@Override
 			protected void onSubmit() {
 				WicketApplication.changeContentsForSubtitle(id,lang,subParameters.content);
 				info("Legenda alterada");
 			}
 		};
 		
 		add(editForm);
 		subParameters.content = subtitle;
 		final TextArea<String> editSubtitleTextArea = new TextArea<String>("content");
 		editForm.add(editSubtitleTextArea);
 	}	
 
 }
