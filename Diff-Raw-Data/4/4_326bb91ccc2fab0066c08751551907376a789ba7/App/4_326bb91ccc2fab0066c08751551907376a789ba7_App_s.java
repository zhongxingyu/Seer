 package com.redhat.topicindex.extras.client.local;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.annotation.PostConstruct;
 import org.jboss.errai.bus.client.api.ErrorCallback;
 import org.jboss.errai.bus.client.api.Message;
 import org.jboss.errai.bus.client.api.RemoteCallback;
 import org.jboss.errai.enterprise.client.jaxrs.MarshallingWrapper;
 import org.jboss.errai.enterprise.client.jaxrs.api.RestClient;
 import org.jboss.errai.ioc.client.api.EntryPoint;
 import org.vectomatic.file.File;
 import org.vectomatic.file.FileReader;
 import org.vectomatic.file.events.ErrorHandler;
 import org.vectomatic.file.events.LoadEndEvent;
 import org.vectomatic.file.events.LoadEndHandler;
 
 import com.google.gwt.dom.client.Style.Unit;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.regexp.shared.MatchResult;
 import com.google.gwt.regexp.shared.RegExp;
 import com.google.gwt.user.client.ui.Button;
 import com.google.gwt.user.client.ui.Grid;
 import com.google.gwt.user.client.ui.HorizontalPanel;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.RootPanel;
 import com.google.gwt.user.client.ui.TextArea;
 import com.google.gwt.user.client.ui.TextBox;
 import com.google.gwt.user.client.ui.VerticalPanel;
 import com.redhat.topicindex.rest.collections.BaseRestCollectionV1;
 import com.redhat.topicindex.rest.collections.RESTImageCollectionV1;
 import com.redhat.topicindex.rest.collections.RESTLanguageImageCollectionV1;
 import com.redhat.topicindex.rest.entities.interfaces.RESTImageV1;
 import com.redhat.topicindex.rest.entities.interfaces.RESTLanguageImageV1;
 import com.smartgwt.client.widgets.Progressbar;
 
 @EntryPoint
 public class App
 {
 	private static final String IMAGE_COLLECTION_EXPAND = "{\"branches\": [{\"branches\": [{\"trunk\": {\"name\": \"languageimages\"}}], \"trunk\": {\"name\": \"images\"}}]}";
	//private static final String REST_SERVER = "http://localhost:8080/TopicIndex/seam/resource/rest";
	private static final String REST_SERVER = "http://skynet-dev.usersys.redhat.com:8080/TopicIndex/seam/resource/rest";
 
 	private final List<ImageUploadData> imageUploadBlocks = new ArrayList<ImageUploadData>();
 	private final TextBox prefix = new TextBox();
 	private final TextArea finalResults = new TextArea();
 	final Button upload = new Button("Upload");
 	final Button newLangButton = new Button("New Language");
 	final Progressbar progress = new Progressbar();
 
 	/**
 	 * The GWT entry point
 	 */
 	@PostConstruct
 	public void init()
 	{
 		/* Init the REST service */
 		RestClient.setApplicationRoot(REST_SERVER);
 		RestClient.setJacksonMarshallingActive(true);
 
 		/* Build the user interface */
 		buildUI();
 	}
 
 	private void buildUI()
 	{
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
 				final List<String> fileNames = getUniqueFileNames();
 				if (fileNames.size() != 0)
 				{
 					setEnabled(false);
 
 					finalResults.setText("");
 					finalResults.setVisible(false);
 
 					progress.setVisible(true);
 
 					final StringBuilder results = new StringBuilder();
 					final List<RESTImageV1> images = new ArrayList<RESTImageV1>();
 					processFile(0, fileNames, results, images);
 				}
 			}
 		});
 		
 		horizontalLayout.add(progress);
 		
 		RootPanel.get().add(layoutGrid);
 	}
 
 	private List<String> getUniqueFileNames()
 	{
 		final List<String> retValue = new ArrayList<String>();
 
 		for (final ImageUploadData data : this.imageUploadBlocks)
 		{
 			for (final File file : data.getUpload().getFiles())
 			{
 				if (!retValue.contains(data))
 				{
 					if (!retValue.contains(file.getName()))
 						retValue.add(file.getName());
 				}
 			}
 		}
 
 		return retValue;
 	}
 
 	/**
 	 * Set the UI after the uploads are done
 	 * 
 	 * @param results
 	 *            The results to be displayed to the user.
 	 */
 	private void uploadDone(final StringBuilder results, final List<RESTImageV1> images)
 	{
 		RemoteCallback<RESTImageCollectionV1> successCallback = new RemoteCallback<RESTImageCollectionV1>()
 		{
 			@Override
 			public void callback(final RESTImageCollectionV1 retValue)
 			{
 				System.out.println("Progress [UPLOAD DONE]");
 				
 				/* output a mapping of file names to image ids */
 				if (retValue.getItems() != null)
 				{
 					for (final RESTImageV1 image : retValue.getItems())
 					{
 						if (image.getLanguageImages_OTM() != null && image.getLanguageImages_OTM().getItems() != null)
 						{
 							for (final RESTLanguageImageV1 langImage : image.getLanguageImages_OTM().getItems())
 							{
 								results.append(image.getId() + ": " + langImage.getFilename() + "\n");
 							}
 						}
 					}
 				}
 
 				reEnabledUI(results);
 			}
 		};
 
 		final ErrorCallback errorCallback = new ErrorCallback()
 		{
 			@Override
 			public boolean error(Message message, Throwable throwable)
 			{
 				results.append("Upload was a failure!");
 				reEnabledUI(results);
 				return true;
 			}
 		};
 
 		final RESTImageCollectionV1 restImages = new RESTImageCollectionV1();
 		for (final RESTImageV1 image : images)
 		{
 			restImages.addItem(image);
 		}
 
 		final RESTInterfaceV1 restMethod = RestClient.create(RESTInterfaceV1.class, successCallback, errorCallback);
 
 		try
 		{
 			System.out.println("Progress [READING]: 100%");
 			System.out.println("Progress [UPLOADING]");
 			restMethod.createJSONImages(IMAGE_COLLECTION_EXPAND, restImages);
 		}
 		catch (final Exception ex)
 		{
 			results.append("Upload was a failure!\n");
 			results.append(ex.toString());
 		}
 	}
 
 	private void reEnabledUI(final StringBuilder results)
 	{
 		finalResults.setText(results.toString());
 		finalResults.setVisible(true);
 		progress.setVisible(false);
 		setEnabled(true);
 	}
 
 	private void processLanguageImage(final int blockIndex, final int fileIndex, final List<String> fileNames, final StringBuilder results, final RESTImageV1 image, final File file, final RESTLanguageImageV1 langImg, final List<RESTImageV1> images)
 	{
 		final FileReader reader = new FileReader();
 
 		reader.addErrorHandler(new ErrorHandler()
 		{
 			@Override
 			public void onError(org.vectomatic.file.events.ErrorEvent event)
 			{
 				processBlock(blockIndex + 1, fileIndex, fileNames, results, image, images);
 			}
 		});
 
 		reader.addLoadEndHandler(new LoadEndHandler()
 		{
 			@Override
 			public void onLoadEnd(LoadEndEvent event)
 			{
 				final ImageUploadData block = imageUploadBlocks.get(blockIndex);
 
 				final String result = reader.getStringResult();
 				final byte[] buffer = getByteArray(result, 1);
 
 				langImg.explicitSetImageData(buffer);
 				langImg.explicitSetLocale(block.getLanguage().getValue(block.getLanguage().getSelectedIndex()));
 				langImg.explicitSetFilename(prefix.getText() + file.getName());
 
 				processBlock(blockIndex + 1, fileIndex, fileNames, results, image, images);
 			}
 		});
 
 		reader.readAsBinaryString(file);
 	}
 
 	private void processBlock(final int blockIndex, final int fileIndex, final List<String> fileNames, final StringBuilder results, final RESTImageV1 image, final List<RESTImageV1> images)
 	{
 		if (blockIndex >= this.imageUploadBlocks.size())
 		{
 			processFile(fileIndex + 1, fileNames, results, images);
 		}
 		else
 		{
 			final String filename = fileNames.get(fileIndex);
 			final ImageUploadData block = this.imageUploadBlocks.get(blockIndex);
 
 			for (final File file : block.getUpload().getFiles())
 			{
 				if (file.getName().equals(filename))
 				{
 					final RESTLanguageImageV1 langImg = new RESTLanguageImageV1();
 					langImg.setAddItem(true);
 					image.getLanguageImages_OTM().addItem(langImg);
 					processLanguageImage(blockIndex, fileIndex, fileNames, results, image, file, langImg, images);
 
 					break;
 				}
 			}
 		}
 	}
 
 	private void processFile(final int fileIndex, final List<String> fileNames, final StringBuilder results, final List<RESTImageV1> images)
 	{
 		if (fileIndex >= fileNames.size())
 		{
 			uploadDone(results, images);
 		}
 		else
 		{
 			final int progressValue = (int) ((float) fileIndex / fileNames.size() * 100);
 			progress.setPercentDone(progressValue);
 
 			System.out.println("Progress [READING]: " + progressValue + "%");
 
 			final RESTImageV1 image = new RESTImageV1();
 			image.setAddItem(true);
 			image.explicitSetLanguageImages_OTM(new RESTLanguageImageCollectionV1());
 			images.add(image);
 			processBlock(0, fileIndex, fileNames, results, image, images);
 		}
 	}
 
 	/**
 	 * Set the state of the UI elements
 	 * 
 	 * @param enabled
 	 *            true if the elements are to be enabled, false otherwise
 	 */
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
 
 	/**
 	 * Replacement for String.toByteArray()
 	 * 
 	 * @param string
 	 *            The string to convert
 	 * @param bytesPerChar
 	 *            The number of bytes per character
 	 * @return the same as the standard Java String.toByteArray() method
 	 */
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
