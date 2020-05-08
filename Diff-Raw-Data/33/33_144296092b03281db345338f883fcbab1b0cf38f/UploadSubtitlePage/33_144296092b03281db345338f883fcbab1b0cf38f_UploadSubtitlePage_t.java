 package libreSubs.libreSubsSite.uploadPage;
 
 import libreSubs.libreSubsSite.SubParameters;
 import libreSubs.libreSubsSite.menuPanel.MenuPanel;
 
 import org.apache.wicket.markup.html.WebPage;
 import org.apache.wicket.markup.html.panel.FeedbackPanel;
 import org.wicketstuff.annotation.mount.MountPath;
 
 @MountPath(path = "uploadSrtPage")
 public class UploadSubtitlePage extends WebPage {
 	
 	public UploadSubtitlePage() {
 		setStatelessHint(true);
 		add(new MenuPanel("menu"));
 
 		final SubtitleUploadForm simpleUploadForm = new SubtitleUploadForm(
 				"simpleUpload", new SubParameters());
 
 		// Create feedback panels
 		final FeedbackPanel uploadFeedback = new FeedbackPanel("uploadFeedback");
 
         // Add uploadFeedback to the page itself
 		add(uploadFeedback);
 		add(simpleUploadForm);
 	}
 }
