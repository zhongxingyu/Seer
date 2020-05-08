 package com.redhat.topicindex.extras.client.local;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.annotation.PostConstruct;
 import org.jboss.errai.bus.client.api.ErrorCallback;
 import org.jboss.errai.bus.client.api.Message;
 import org.jboss.errai.bus.client.api.RemoteCallback;
 import org.jboss.errai.enterprise.client.jaxrs.api.RestClient;
 import org.jboss.errai.ioc.client.api.EntryPoint;
 import org.vectomatic.file.File;
 import org.vectomatic.file.FileReader;
 import org.vectomatic.file.events.LoadEndEvent;
 import org.vectomatic.file.events.LoadEndHandler;
 
 import com.google.gwt.dom.client.Style.Unit;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.regexp.shared.MatchResult;
 import com.google.gwt.regexp.shared.RegExp;
 import com.google.gwt.user.client.Window;
 import com.google.gwt.user.client.ui.Button;
 import com.google.gwt.user.client.ui.Grid;
 import com.google.gwt.user.client.ui.HorizontalPanel;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.RootPanel;
 import com.google.gwt.user.client.ui.TextArea;
 import com.google.gwt.user.client.ui.TextBox;
 import com.google.gwt.user.client.ui.VerticalPanel;
 import com.redhat.topicindex.rest.entities.interfaces.RESTImageV1;
 import com.smartgwt.client.widgets.Progressbar;
 
 @EntryPoint
 public class App
 {
 	private static final String RESTIMAGEV1_FILENAME_RE = "\"filename\":\"(.*?)\"";
 	private static final RegExp RESTIMAGEV1_FILENAME_EXP = RegExp.compile(RESTIMAGEV1_FILENAME_RE);
 	private static final String RESTIMAGEV1_ID_RE = "\"id\":(\\d+)";
 	private static final RegExp RESTIMAGEV1_ID_EXP = RegExp.compile(RESTIMAGEV1_ID_RE);
 	private static final String REST_SERVER = "http://localhost:8080/TopicIndex/seam/resource/rest";
 
 	// private static final String REST_SERVER = "http://skynet-dev.usersys.redhat.com:8080/TopicIndex/seam/resource/rest";
 
 	private final List<ImageUploadData> imageUploadBlocks = new ArrayList<ImageUploadData>();
 	private final TextBox prefix = new TextBox();
 	private final TextArea finalResults = new TextArea(); 
 	final Button upload = new Button("Upload");
 	final Button newLangButton = new Button("New Language");
 	final Progressbar progress = new Progressbar();
 
 	@PostConstruct
 	public void init()
 	{
 		/* Init the REST service */
 		RestClient.setApplicationRoot(REST_SERVER);
 		RestClient.setJacksonMarshallingActive(true);
 		
 		progress.setVisible(false);
 		finalResults.setReadOnly(true);
 		finalResults.setWidth("500px");
 		finalResults.setHeight("300px");
 		finalResults.setVisible(false);
 
 		final Grid layoutGrid = new Grid(4, 1);
 
 		final HorizontalPanel prefixLayout = new HorizontalPanel();
 		layoutGrid.setWidget(0, 0, prefixLayout);
 
 		final Label prefixLabel = new Label("Enter an optional image name prefix");
 		prefixLabel.getElement().getStyle().setPaddingRight(10, Unit.PX);
 		prefixLayout.add(prefixLabel);
 		prefixLayout.add(prefix);
 
 		final VerticalPanel verticalPanel = new VerticalPanel();
 		layoutGrid.setWidget(1, 0, verticalPanel);
 
 		final ImageUploadData initialBlock = new ImageUploadData();
 		imageUploadBlocks.add(initialBlock);
 		verticalPanel.add(initialBlock.getGrid());
 		
 		layoutGrid.setWidget(2, 0, finalResults);
 
 		final HorizontalPanel horizontalLayout = new HorizontalPanel();
 		layoutGrid.setWidget(3, 0, horizontalLayout);
 		
 		newLangButton.addClickHandler(new ClickHandler()
 		{
 			@Override
 			public void onClick(final ClickEvent event)
 			{
 				final ImageUploadData newBlock = new ImageUploadData();
 				imageUploadBlocks.add(newBlock);
 				verticalPanel.add(newBlock.getGrid());
 			}
 		});
 		horizontalLayout.add(newLangButton);
 		
 		horizontalLayout.add(upload);
 
 		upload.addClickHandler(new ClickHandler()
 		{
 			@Override
 			public void onClick(ClickEvent event)
 			{
 
 				setEnabled(false);	
 				
 				finalResults.setText("");
 				finalResults.setVisible(false);
 				
 				progress.setVisible(true);
 				
 				final StringBuilder results = new StringBuilder();
 				processImage(0, 0, results);
 			}
 		});
 		
 		horizontalLayout.add(progress);
 
 		RootPanel.get().add(layoutGrid);
 	}
 	
 	private void uploadDone(final StringBuilder results)
 	{
 		finalResults.setText(results.toString());
 		finalResults.setVisible(true);
 		progress.setVisible(false);
 		setEnabled(true);
 	}
 
 	private void processImage(final int blockIndex, final int fileIndex, final StringBuilder results)
 	{
 		/* there are no more blocks to process, so enable the UI and return */
 		if (blockIndex >= this.imageUploadBlocks.size())
 		{					
 			uploadDone(results);			
 			return;
 		}
 
 		/* see if there are any more files to process */
 		final ImageUploadData data = imageUploadBlocks.get(blockIndex);
 
 		/* There are no more files to process, so move to the next block */
 		if (fileIndex >= data.getUpload().getFiles().getLength())
 		{
 			processImage(blockIndex + 1, 0, results);
 		}
 		/* There are files to process, process them and then move to the next file */
 		else
 		{
			final float blockLevelProgress = ((float)blockIndex + 1) / this.imageUploadBlocks.size();
			final float fileLevelProgress = ((float)fileIndex + 1) / data.getUpload().getFiles().getLength() / this.imageUploadBlocks.size();
 			
 			progress.setPercentDone((int) ((blockLevelProgress + fileLevelProgress)  * 100));
 			
 			final File file = data.getUpload().getFiles().getItem(fileIndex);
 
 			final RemoteCallback<String> genericSuccessCallback = new RemoteCallback<String>()
 			{
 				@Override
 				public void callback(final String retValue)
 				{
 					/* temp workaround for bug at https://community.jboss.org/thread/200710?tstart=0 */
 					final MatchResult filenameMatcher = RESTIMAGEV1_FILENAME_EXP.exec(retValue);
 					final boolean filenameMatchFound = RESTIMAGEV1_FILENAME_EXP.test(retValue);
 
 					final MatchResult idMatcher = RESTIMAGEV1_ID_EXP.exec(retValue);
 					final boolean idMatchFound = RESTIMAGEV1_ID_EXP.test(retValue);
 
 					if (filenameMatchFound && idMatchFound)
 					{
 						final String filename = filenameMatcher.getGroup(1);
 						final String id = idMatcher.getGroup(1);
 
 						results.append(id + ": " + filename + "\n");
 					}
 
 					processImage(blockIndex, fileIndex + 1, results);
 				}
 			};
 
 			final RemoteCallback<RESTImageV1> successCallback = new RemoteCallback<RESTImageV1>()
 			{
 				@Override
 				public void callback(final RESTImageV1 retValue)
 				{
 					results.append(retValue.getFilename() + ": " + retValue.getId().toString() + "\n");
 					
 					processImage(blockIndex, fileIndex + 1, results);
 				}
 			};
 
 			final ErrorCallback errorCallback = new ErrorCallback()
 			{
 				@Override
 				public boolean error(Message message, Throwable throwable)
 				{
 					results.append(file.getName() + ": ERROR!");
 					processImage(blockIndex, fileIndex + 1, results);					
 					return true;
 				}
 			};
 
 			final RESTInterfaceV1 restMethod = RestClient.create(RESTInterfaceV1.class, genericSuccessCallback, errorCallback);
 
 			final FileReader reader = new FileReader();
 			reader.addLoadEndHandler(new LoadEndHandler()
 			{
 				@Override
 				public void onLoadEnd(LoadEndEvent event)
 				{
 
 					final String result = reader.getStringResult();
 					final byte[] buffer = getByteArray(result, 1);
 
 					final RESTImageV1 image = new RESTImageV1();
 					image.explicitSetImageData(buffer);
 					image.explicitSetFilename(prefix.getText()+ file.getName());
 
 					try
 					{
 						restMethod.createJSONImage("", image);
 					}
 					catch (final Exception ex)
 					{
 						ex.printStackTrace();
 						Window.alert(ex.toString());
 					}
 				}
 			});
 
 			reader.readAsBinaryString(file);
 		}
 	}
 	
 	private void setEnabled(final boolean enabled)
 	{
 		for (final ImageUploadData block : imageUploadBlocks)
 		{
 			block.setEnabled(enabled);
 		}
 		
 		this.finalResults.setEnabled(enabled);
 		this.prefix.setEnabled(enabled);
 		this.upload.setEnabled(enabled);
 		this.newLangButton.setEnabled(enabled);
 	}
 
 	public static byte[] getByteArray(final String string, final int bytesPerChar)
 	{
 		char[] chars = string.toCharArray();
 		byte[] toReturn = new byte[chars.length * bytesPerChar];
 		for (int i = 0; i < chars.length; i++)
 		{
 			for (int j = 0; j < bytesPerChar; j++)
 				toReturn[i * bytesPerChar + j] = (byte) (chars[i] >>> (8 * (bytesPerChar - 1 - j)));
 		}
 		return toReturn;
 	}
 }
