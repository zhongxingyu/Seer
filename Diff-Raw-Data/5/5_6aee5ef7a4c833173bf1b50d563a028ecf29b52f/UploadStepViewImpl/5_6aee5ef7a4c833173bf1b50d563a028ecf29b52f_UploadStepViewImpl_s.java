 package org.cotrix.web.importwizard.client.step.upload;
 
 import org.cotrix.web.importwizard.client.resources.ImportConstants;
 import org.cotrix.web.importwizard.client.util.AlertDialog;
 import org.cotrix.web.importwizard.client.util.TextUtil;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.event.dom.client.ChangeEvent;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.uibinder.client.UiBinder;
 import com.google.gwt.uibinder.client.UiField;
 import com.google.gwt.uibinder.client.UiHandler;
 import com.google.gwt.uibinder.client.UiTemplate;
 import com.google.gwt.user.client.ui.Button;
 import com.google.gwt.user.client.ui.Composite;
 import com.google.gwt.user.client.ui.FormPanel;
 import com.google.gwt.user.client.ui.FormPanel.SubmitCompleteEvent;
 import com.google.gwt.user.client.ui.FlowPanel;
 import com.google.gwt.user.client.ui.HorizontalPanel;
 import com.google.gwt.user.client.ui.InlineLabel;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.Widget;
 
 /**
  * @author "Federico De Faveri federico.defaveri@fao.org"
  *
  */
 public class UploadStepViewImpl extends Composite implements UploadStepView {
 
 	private static UploadStepUiBinder uiBinder = GWT.create(UploadStepUiBinder.class);
 	
 	@UiTemplate("UploadStep.ui.xml")
 	interface UploadStepUiBinder extends UiBinder<Widget, UploadStepViewImpl> {}
 	
 	@UiField Button browseButton;
 	@UiField FileUploadExt fileUploadButton;
 	
 	@UiField InlineLabel fileNameLabel;
 	@UiField InlineLabel fileSizeLabel;
 	
 	@UiField Label retryButton;
 	
 	@UiField FlowPanel uploadPanel;
 	@UiField ProgressBar progressBar;
 	@UiField HorizontalPanel uploadFailPanel;
 	
 	@UiField FormPanel form;
 
 	private Presenter presenter;
 	private AlertDialog alertDialog;
 
 	public UploadStepViewImpl() {
 		initWidget(uiBinder.createAndBindUi(this));
 		
 		form.setAction(GWT.getModuleBaseURL()+"fileupload");
 		
 		form.addSubmitCompleteHandler(new FormPanel.SubmitCompleteHandler() {
 			public void onSubmitComplete(SubmitCompleteEvent event) {
 				presenter.onSubmitComplete(event);
 			}
 		});
 	}
 	
 	public void setPresenter(Presenter presenter) {
 		this.presenter = presenter;
 	}
 	
 	public void setupUpload(String filename, long filesize)
 	{
 		uploadPanel.setVisible(true);
 		uploadFailPanel.setVisible(false);
		progressBar.setVisible(true);
 		progressBar.setProgress(0);
 		
 		String ellipsedName = TextUtil.ellipsize(filename, ImportConstants.INSTANCE.fileNameMaxSize());
 		fileNameLabel.setText(ellipsedName);
 		fileNameLabel.setTitle(filename);
 		fileNameLabel.setVisible(true);
 		
 		fileSizeLabel.setText("("+TextUtil.toKiloBytes(filesize)+"K)");
 		fileSizeLabel.setVisible(true);
 	}
 	
 	public void setUploadProgress(int progress)
 	{
 		progressBar.setProgress(progress);
 	}
 	
 	public void setUploadFailed()
 	{
 		progressBar.setVisible(false);
 		uploadFailPanel.setVisible(true);
 	}
 	
 	public void setUploadComplete(String codeListType)
 	{
 		
 	}
 
 	public void resetFileUpload() {
 		fileNameLabel.setVisible(false);
 		fileSizeLabel.setVisible(false);
 	}
 	
 	public void alert(String message) {
 		if(alertDialog == null){
 			alertDialog = new AlertDialog(true);
 		}
 		alertDialog.setMessage(message);
 		alertDialog.showRelative(browseButton);
 	}
 
 	@UiHandler("retryButton")
 	public void onRetryButtonClicked(ClickEvent event) {
 		presenter.onRetryButtonClicked();
 	}
 
 	@UiHandler("browseButton")
 	public void onBrowseButtonClicked(ClickEvent event) {
 		this.fileUploadButton.click();
 	}
 
 	@UiHandler("fileUploadButton")
 	public void onUploadFileChange(ChangeEvent event) {
 		presenter.onUploadFileChanged(fileUploadButton.getFiles(),fileUploadButton.getFilename());
 	}
 
 	public void reset() {
 		resetFileUpload();
 		this.form.reset();
 	}
 
 	public void submitForm() {
 		this.form.submit();
 	}
 
 }
