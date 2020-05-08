 package com.delcyon.capo.webapp.widgets;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.OutputStream;
 import java.util.List;
 import java.util.SortedSet;
 import java.util.TreeSet;
 
 import org.w3c.dom.Element;
 
 import com.delcyon.capo.CapoApplication;
 import com.delcyon.capo.datastream.StreamUtil;
 import com.delcyon.capo.datastream.stream_attribute_filter.MimeTypeFilterInputStream;
 import com.delcyon.capo.resourcemanager.ContentFormatType;
 import com.delcyon.capo.resourcemanager.ResourceDescriptor;
 import com.delcyon.capo.resourcemanager.ResourceDescriptor.Action;
 import com.delcyon.capo.resourcemanager.ResourceDescriptor.State;
 import com.delcyon.capo.resourcemanager.types.ContentMetaData;
 import com.delcyon.capo.resourcemanager.types.Versionable;
 import com.delcyon.capo.util.HexUtil;
 import com.delcyon.capo.util.diff.Diff;
 import com.delcyon.capo.util.diff.InputStreamTokenizer.TokenList;
 import com.delcyon.capo.webapp.models.DomItemModel;
 import com.delcyon.capo.webapp.models.DomItemModel.DomUse;
 import com.delcyon.capo.webapp.models.ResourceDescriptorItemModel;
 import com.delcyon.capo.webapp.models.WContentMetaDataItemModel;
 import com.delcyon.capo.webapp.servlets.resource.WResourceDescriptor;
 import com.delcyon.capo.webapp.widgets.WAceEditor.Theme;
 import com.delcyon.capo.webapp.widgets.WDiffWidget.DiffFormat;
 import com.delcyon.capo.xml.XMLDiff;
 import com.delcyon.capo.xml.XPath;
 
 import eu.webtoolkit.jwt.AlignmentFlag;
 import eu.webtoolkit.jwt.AnchorTarget;
 import eu.webtoolkit.jwt.SelectionMode;
 import eu.webtoolkit.jwt.Signal;
 import eu.webtoolkit.jwt.WAbstractItemView;
 import eu.webtoolkit.jwt.WAnchor;
 import eu.webtoolkit.jwt.WContainerWidget;
 import eu.webtoolkit.jwt.WFileUpload;
 import eu.webtoolkit.jwt.WImage;
 import eu.webtoolkit.jwt.WLength;
 import eu.webtoolkit.jwt.WLink;
 import eu.webtoolkit.jwt.WModelIndex;
 import eu.webtoolkit.jwt.WMouseEvent;
 import eu.webtoolkit.jwt.WProgressBar;
 import eu.webtoolkit.jwt.WPushButton;
 import eu.webtoolkit.jwt.WTabWidget;
 import eu.webtoolkit.jwt.WTableView;
 import eu.webtoolkit.jwt.WWidget;
 import eu.webtoolkit.jwt.servlet.UploadedFile;
 
 public class WCapoResourceEditor extends WTabWidget
 {
     private static final String XML_CONTENT_TYPE = "xml";
     private static final String HEX_CONTENT_TYPE = "hex";
     private static final String SHELL_CONTENT_TYPE = "sh";
     private static final String MIMETYPE_IMAGE_PREFIX = "image/";
     private static final String APPLICATION_X_SHELLSCRIPT = "application/x-shellscript";
     private static final String EXTENTION_DELIMITER = ".";
     private static final String EMPTY_STRING = "";
     private Object model = null;
     private WTableView attributeTableView;
     private WContainerWidget detailsContainerWidget;
     private String content = null;
     private String contentType = null;
     private ContentFormatType contentFormatType = null;
     private String mimeType = null;
     private long length = 0l;
     private WLink downloadLink;
     private WPushButton createContentPushButton;
     private WAceEditor aceEditor;
     private WAceEditor formattedContentDisplay;
     private WFileUpload upload;
     private WContainerWidget historyContainerWidget;
     private WTableView historyTableView;
     private Signal modelChanged = new Signal();
     private WDiffWidget diffWidget;
     private String currentVersionName = "";
 
     /**
      * Actually loads the data into the editor. This is the only place tabs and what not should be added since it's the main controller method for this class
      * 
      * @param content
      *            - actual data to display
      * @param contentType
      *            - ace mode to use when displaying data
      * @param contentFormatType
      *            - Capo Content format type
      * @param mimeType
      *            - standard mime type
      * @param length
      *            - length of data
      */
     public void setContent(String content, String contentType, ContentFormatType contentFormatType, String mimeType, Long length)
     {
         this.content = content;
 
         // empty out all of the existing tabs since were going to add some random number back in
         while (this.getCount() > 0)
         {
             this.removeTab(this.getWidget(0));
         }
 
         // null content is ok
         if (content != null)
         {
 
             // we can also work with empty content, we just don't want to show it if there's nothing there
             if (length != null && length > 0l)
             {
                 getFormattedContentDisplay().setText(content);
                 getFormattedContentDisplay().setMode(contentType);
                 this.addTab(getFormattedContentDisplay(), "Content");
             }
 
             // don't allow binary content to be edited
             if (contentFormatType != ContentFormatType.BINARY)
             {
                 getAceEditor().setText(content);
                 getAceEditor().setMode(contentType);
                 this.addTab(getAceEditor(), "Edit");
             }
         }// treat images a little differently, since we know what to do with them to showup
         else if (mimeType != null && mimeType.startsWith(MIMETYPE_IMAGE_PREFIX))
         {
             WResourceDescriptor wResourceDescriptor = new WResourceDescriptor((ResourceDescriptor) this.model);
             this.addTab(new WImage(wResourceDescriptor, "Content"), "Content");
         }
 
         // update the download link with the new mode data
         getDownloadLink().setResource(new WResourceDescriptor((ResourceDescriptor) this.model));
 
         // always add the details tab last
         this.addTab(getDetailsContainerWidget(), "Details");
 
         // add version history tab
         if (this.model instanceof Versionable)
         {
 
             try
             {
                 // add these tabs, but hide them if the model isn't versioned
                 this.addTab(getHistoryContainerWidget(), "History").setHidden(((Versionable) this.model).isVersioned() == false);
                 this.addTab(getDiffWidget(), "Diff").setHidden(true);
             }
             catch (Exception e)
             {
                 e.printStackTrace();
             }
 
         }
 
         // this might be able to be removed, but was put there when we we're using a background image
         this.getWidget(0).setAttributeValue("style", "background-color: rgba(255, 255, 255, 0.55);");
 
     }
 
     /**
      * this widget is here to simply add comcolor codeing to the content to make it more readable, but is ALWAYS readonly.
      * 
      * @return
      */
     private WAceEditor getFormattedContentDisplay()
     {
         if (formattedContentDisplay == null)
         {
             formattedContentDisplay = new WAceEditor();
             formattedContentDisplay.setTheme(Theme.eclipse); // TODO set via user preference
             formattedContentDisplay.setReadOnly(true);
         }
         return formattedContentDisplay;
     }
 
     /**
      * This is the widget which actually edit's the content
      * 
      * @return
      */
     private WAceEditor getAceEditor()
     {
         if (aceEditor == null)
         {
             aceEditor = new WAceEditor();
             aceEditor.setTheme(Theme.tomorrow);
             // add a save listen to the editor
             aceEditor.save().addListener(this, this::save);
         }
         return aceEditor;
     }
 
     /**
      * this will save the 'content' to the 'model', then call refresh
      */
     private void save(String content)
     {
         try
         {
 
             // update content
             WCapoResourceEditor.this.content = content;
 
             Versionable versionableModel = null;
             if (model instanceof Versionable && ((Versionable) model).isVersioned())
             {
                 versionableModel = (Versionable) model;
             }
 
             // checkout if we need too
             if (versionableModel != null)
             {
                 versionableModel.checkout();
             }
 
             // then save
             ((ResourceDescriptor) model).writeBlock(null, content.getBytes());
 
             // checkin if we need too
             if (versionableModel != null)
             {
                 versionableModel.checkin();
             }
 
             // refresh the view
             refresh();
         }
         catch (Exception exception)
         {
             exception.printStackTrace(); // TODO do something with this error
         }
 
     };
 
     /**
      * @return the text content of this detail pane
      */
     public String getContent()
     {
         return content;
     }
 
     /**
      * @param model
      *            , can be either a resource descriptor or DOM element.
      */
     public void setModel(Object model)
     {
         this.model = model;
 
         // keep all of our variable as nulls, so we don't get weird on each model reset
         String content = null;
         String contentType = null;
         ContentFormatType contentFormatType = null;
         String mimeType = null;
         getCreateContentButton().setHidden(true);
         long length = 0l;
 
         if (this.model instanceof Element)
         {
             getAttributeTableView().setModel(new DomItemModel((Element) this.model, DomUse.ATTRIBUTES));
             // set some decent defaults for xml data
             content = ((Element) this.model).getTextContent();
             contentFormatType = ContentFormatType.XML;
             contentType = XML_CONTENT_TYPE;
         }
         else if (this.model instanceof ResourceDescriptor)
         {
             // go ahead a cast here so we can have some cleaner looking code
             ResourceDescriptor resourceDescriptor = (ResourceDescriptor) this.model;
             getAttributeTableView().setModel(new ResourceDescriptorItemModel(resourceDescriptor, DomUse.ATTRIBUTES));
             try
             {
                 if (resourceDescriptor instanceof Versionable)
                 {
                     List<ContentMetaData> versionHistory = ((Versionable) resourceDescriptor).getVersionHistory();
                     for (ContentMetaData contentMetaData : versionHistory)
                     {
                         if(contentMetaData.getValue("isBaseVersion").equals("true"))
                         {
                             this.currentVersionName  = contentMetaData.getValue("versionName");
                         }
                     }
                     getHistoryTableView().setModel(new WContentMetaDataItemModel(versionHistory, "isBaseVersion,Current", "versionTimeStamp,Date", "versionName,Version", "mimeType", "MD5", "size", "contentFormatType,Type"));
                 }
             }
             catch (Exception e1)
             {
 
                 e1.printStackTrace();
             }
             try
             {
                 // used to be a container check here, but doesn't make since when backing is JCR
 
                 // start figuring out what kind of data we're dealing with
                 contentFormatType = resourceDescriptor.getResourceMetaData(null).getContentFormatType();
                 mimeType = resourceDescriptor.getResourceMetaData(null).getValue(MimeTypeFilterInputStream.MIME_TYPE_ATTRIBUTE);
 
                 // default mimetype to empty string so we don't have to litter null checks every where
                 if (mimeType == null)
                 {
                     mimeType = EMPTY_STRING;
                 }
 
                 length = resourceDescriptor.getResourceMetaData(null).getLength();
 
                 // figure out the contentType. Content type basically matches up to any available ACE mode in the ace editor
                 if (contentFormatType == ContentFormatType.TEXT || contentFormatType == ContentFormatType.NO_CONTENT)
                 {
                     if (contentFormatType == ContentFormatType.NO_CONTENT)
                     {
                         // no content? enable the create button
                         getCreateContentButton().setHidden(false);
                     }
                     else
                     {// other wise use the data we have in the model
                         resourceDescriptor.getResourceState();
                         content = new String(resourceDescriptor.readBlock(null));
                         resourceDescriptor.reset(State.OPEN);
                     }
 
                     // see if the node name has some sort of clue as to the editor mode to use
                     String localName = resourceDescriptor.getLocalName();
                     if (localName.indexOf(EXTENTION_DELIMITER) <= 0) // check for extension
                     {
                         // ok, well what about the mime type
                         if (mimeType.equalsIgnoreCase(APPLICATION_X_SHELLSCRIPT))
                         {
                             // to bad we only know one type.. :-(
                             contentType = SHELL_CONTENT_TYPE;
                         }
                     }
                     else
                     // use extension
                     {
                         // sure hope this matches up to some available ACE editor mode
                         contentType = localName.substring(localName.indexOf(EXTENTION_DELIMITER) + 1);
                     }
                 }
 
                 // ok, this is XML not text, so put the editor in XML mode
                 // TODO once we become namespace aware, we should use somesort of scheme aware xml editor, for known XML schemas
                 else if (contentFormatType == ContentFormatType.XML)
                 {
                     resourceDescriptor.getResourceState();
                     content = new String(resourceDescriptor.readBlock(null));
                     resourceDescriptor.reset(State.OPEN);
                     contentType = XML_CONTENT_TYPE;
                 }
                 // looks like we have some binary content here, and it's not an image, lets dump it to a nice hex output
                 // TODO this should probably not default to showing the binary, but present the user with the option to view binary content
                 // unless it's a registered mimetype, that the user, or admin has set in their preferences or something.
                 else if (contentFormatType == ContentFormatType.BINARY && length < 70000l && mimeType.startsWith(MIMETYPE_IMAGE_PREFIX) == false)
                 {
                     byte[] bytes = resourceDescriptor.readBlock(null);
                     resourceDescriptor.reset(State.OPEN);
                     content = HexUtil.dump(bytes);
                     contentType = HEX_CONTENT_TYPE;
 
                 }
 
             }
             catch (Exception e)
             {
                 e.printStackTrace();
             }
         }
 
         setContent(content, contentType, contentFormatType, mimeType, length);
         modelChanged.trigger();
     }
 
     /**
      * This just empty out any content from the model, then refreshes the widget
      */
     public void clearContent()
     {
         try
         {
             ((ResourceDescriptor) WCapoResourceEditor.this.model).writeBlock(null, EMPTY_STRING.getBytes());
             refresh();
         }
         catch (Exception e)
         {
             // TODO Auto-generated catch block
             e.printStackTrace();
         }
     }
 
     private WContainerWidget getDetailsContainerWidget()
     {
         if (detailsContainerWidget == null)
         {
             detailsContainerWidget = new WContainerWidget();
             WAnchor anchor = new WAnchor(getDownloadLink(), "Download"); // this is a link so "save as" will work
 
             anchor.setTarget(AnchorTarget.TargetNewWindow);
             upload = new WFileUpload();
             upload.setFileTextSize(10000); // needed to get a basic starting point on the progress apparently
             upload.setProgressBar(new WProgressBar());
 
             // fired when the user selects a file to upload, which we use to indicate that we'd like to start uploading
             upload.changed().addListener(this, upload::upload);
 
             // trigger that gets called once a file is done uploading
             upload.uploaded().addListener(this, this::fileUploaded);
 
             // TODO This upload error has to be processed. We really need an error dialog
             upload.fileTooLarge().addListener(this, () -> System.err.println("error, too large"));
 
             // clean content button.
             WPushButton clearContentPushButton = new WPushButton("Clear Content");
             clearContentPushButton.clicked().addListener(this, this::clearContent);
 
             // checkin version button
             WPushButton checkinVersionPushButton = new WPushButton("Track Versions");
             checkinVersionPushButton.clicked().addListener(this, this::makeVersionable);
 
             // //checkout version button
             // WPushButton checkoutVersionPushButton = new WPushButton("Check Out");
             // checkoutVersionPushButton.clicked().addListener(this, this::checkoutContent);
 
             detailsContainerWidget.addWidget(upload);
             detailsContainerWidget.addWidget(anchor);
             detailsContainerWidget.addWidget(clearContentPushButton);
             detailsContainerWidget.addWidget(getCreateContentButton());
             detailsContainerWidget.addWidget(checkinVersionPushButton);
             // detailsContainerWidget.addWidget(checkoutVersionPushButton);
             detailsContainerWidget.addWidget(getAttributeTableView());
 
         }
         return detailsContainerWidget;
     }
 
     private WContainerWidget getHistoryContainerWidget()
     {
         if (historyContainerWidget == null)
         {
             historyContainerWidget = new WContainerWidget();
 
             // checkin version button
             WPushButton checkinVersionPushButton = new WPushButton("Check In");
             checkinVersionPushButton.clicked().addListener(this, this::checkinContent);
 
             // checkout version button
             WPushButton checkoutVersionPushButton = new WPushButton("Check Out");
             checkoutVersionPushButton.clicked().addListener(this, this::checkoutContent);
 
             // checkout version button
             WPushButton restoreSelectedVersionPushButton = new WPushButton("Restore Selected");
             restoreSelectedVersionPushButton.disable();
             restoreSelectedVersionPushButton.clicked().addListener(this, this::restoreSelected);
             getHistoryTableView().selectionChanged().addListener(this, () -> processSelectionEvent(getHistoryTableView(), restoreSelectedVersionPushButton));
             modelChanged.addListener(this, restoreSelectedVersionPushButton::disable);
 
             // checkout version button
             WPushButton deleteSelectedVersionPushButton = new WPushButton("Delete Selected");
             deleteSelectedVersionPushButton.clicked().addListener(this, this::deleteSelected);
             getHistoryTableView().selectionChanged().addListener(this, () -> processSelectionEvent(getHistoryTableView(), deleteSelectedVersionPushButton));
             modelChanged.addListener(this, deleteSelectedVersionPushButton::disable);
 
             // diff version button
             WPushButton diffSelectedVersionPushButton = new WPushButton("Diff Selected");
             diffSelectedVersionPushButton.clicked().addListener(this, this::diffSelected);
             getHistoryTableView().selectionChanged().addListener(this, () -> processSelectionEvent(getHistoryTableView(), diffSelectedVersionPushButton));
             modelChanged.addListener(this, diffSelectedVersionPushButton::disable);
 
             historyContainerWidget.addWidget(checkinVersionPushButton);
             historyContainerWidget.addWidget(checkoutVersionPushButton);
             historyContainerWidget.addWidget(restoreSelectedVersionPushButton);
             historyContainerWidget.addWidget(deleteSelectedVersionPushButton);
             historyContainerWidget.addWidget(diffSelectedVersionPushButton);
             historyContainerWidget.addWidget(getHistoryTableView());
 
         }
         return historyContainerWidget;
     }
 
     private void processSelectionEvent(WAbstractItemView abstractItemView, WWidget widget)
     {
         if (abstractItemView.getSelectedIndexes() == null || abstractItemView.getSelectedIndexes().isEmpty())
         {
             widget.disable();
         }
         else
         {
             widget.enable();
         }
     }
 
     private void deleteSelected()
     {
         SortedSet<WModelIndex> selectedIndexes = getHistoryTableView().getSelectedIndexes();
         if (selectedIndexes != null && selectedIndexes.isEmpty() == false)
         {
             WModelIndex selectedIndex = selectedIndexes.first();
             try
             {
                ((Versionable) this.model).remove(((ContentMetaData) selectedIndex.getInternalPointer()).getResourceURI().getResourceURIString());
                 refresh();
             }
             catch (Exception e)
             {
                 // TODO Auto-generated catch block
                 e.printStackTrace();
             }
         }
     }
 
     private void restoreSelected()
     {
         SortedSet<WModelIndex> selectedIndexes = getHistoryTableView().getSelectedIndexes();
         if (selectedIndexes != null && selectedIndexes.isEmpty() == false)
         {
             WModelIndex selectedIndex = selectedIndexes.first();
             try
             {
                 ((Versionable) this.model).restore(((ContentMetaData) selectedIndex.getInternalPointer()).getResourceURI().getResourceURIString());
                 refresh();
             }
             catch (Exception e)
             {
                 // TODO Auto-generated catch block
                 e.printStackTrace();
             }
         }
     }
 
     /**
      * Generate a diff between the current version, and a selected version
      */
     private void diffSelected()
     {
         SortedSet<WModelIndex> selectedIndexes = getHistoryTableView().getSelectedIndexes();
         if (selectedIndexes != null && selectedIndexes.isEmpty() == false)
         {
             WModelIndex selectedIndex = selectedIndexes.first();
             try
             {
                 String versionURI = ((Versionable) this.model).getVersion((((ContentMetaData) selectedIndex.getInternalPointer()).getResourceURI().getResourceURIString()));
                 ResourceDescriptor versionResourceDescriptor = CapoApplication.getDataManager().getResourceDescriptor(null, versionURI);
 
                 ContentFormatType contentFormatType = ((ResourceDescriptor) this.model).getResourceMetaData(null).getContentFormatType();
                 ContentFormatType versionContentFormatType = versionResourceDescriptor.getResourceMetaData(null).getContentFormatType();
                 if (versionContentFormatType.ordinal() > contentFormatType.ordinal())
                 {
                     contentFormatType = versionContentFormatType;
                 }
 
                 if (contentFormatType == ContentFormatType.XML)
                 {
                     XMLDiff xmlDiff = new XMLDiff();
                     Element diffelement = xmlDiff.getDifferences(versionResourceDescriptor.readXML(null), ((ResourceDescriptor) this.model).readXML(null));
                     XPath.dumpNode(diffelement, System.out);
                 }
                 else if (contentFormatType == ContentFormatType.TEXT)
                 {
                     Diff diff = new Diff(versionResourceDescriptor.getInputStream(null), ((ResourceDescriptor) this.model).getInputStream(null), TokenList.NEW_LINE);
                     getDiffWidget().setHeaders(((ContentMetaData) selectedIndex.getInternalPointer()).getValue("versionName"),"Current ("+currentVersionName+")");
                     getDiffWidget().setDiff(diff.getDifferences(), DiffFormat.CAPO);
                 }
 //TODO do we really want to DISPLAY BINARY DIFFS, seems like a lot of work for not a lot of benefit.
 //                {
 //                    ByteArrayOutputStream baseByteArrayOutputStream = new ByteArrayOutputStream();
 //                    StreamUtil.readInputStreamIntoOutputStream(versionResourceDescriptor.getInputStream(null), baseByteArrayOutputStream);
 //                    ByteArrayOutputStream modByteArrayOutputStream = new ByteArrayOutputStream();
 //                    StreamUtil.readInputStreamIntoOutputStream( ((ResourceDescriptor) this.model).getInputStream(null), modByteArrayOutputStream);
 //                    Diff diff = new Diff(HexUtil.bytesToHex(baseByteArrayOutputStream.toByteArray()),HexUtil.bytesToHex(modByteArrayOutputStream.toByteArray()));
 //                    //byte[] diffs = diff.getDifferencesAsBytes();
 //                    getDiffWidget().setDiff(diff.getDifferences(), DiffFormat.CAPO);
 //                    //System.out.println(new String(diffs));
 //                }
 
                 this.setTabHidden(this.getIndexOf(getDiffWidget()), false);
                 this.setCurrentIndex(this.getIndexOf(getDiffWidget()));
             }
             catch (Exception e)
             {
                 // TODO Auto-generated catch block
                 e.printStackTrace();
             }
         }
     }
 
     private WDiffWidget getDiffWidget()
     {
         if (diffWidget == null)
         {
             diffWidget = new WDiffWidget();
         }
 
         return diffWidget;
     }
 
     /**
      * This just create the attribute table for the model
      * 
      * @return
      */
     private WTableView getHistoryTableView()
     {
         if (historyTableView == null)
         {
             historyTableView = new WTableView();
             historyTableView.addStyleClass("bg-transparent");
             historyTableView.setItemDelegateForColumn(0, new WCSSItemDelegate("font-weight: bold;"));
             historyTableView.setSortingEnabled(true);
             historyTableView.setSelectable(true);
             historyTableView.setAlternatingRowColors(true);
             historyTableView.setColumnResizeEnabled(true);
             historyTableView.setColumnAlignment(0, AlignmentFlag.AlignRight);
             historyTableView.setColumnWidth(1, new WLength(500));
             historyTableView.setSelectionMode(SelectionMode.SingleSelection);
             historyTableView.doubleClicked().addListener(this,this::historyDoubleClicked);
         }
         return historyTableView;
     }
 
     private void historyDoubleClicked(WModelIndex index,WMouseEvent event)
     {
         SortedSet<WModelIndex> indexes = new TreeSet<WModelIndex>();
         indexes.add(index);
         getHistoryTableView().setSelectedIndexes(indexes);        
         diffSelected();
     }
     
     private void makeVersionable()
     {
         try
         {
             int currentIndex = getCurrentIndex();
             ((ResourceDescriptor) model).performAction(null, Action.CHECKOUT);
             ((ResourceDescriptor) model).performAction(null, Action.CHECKIN);
             setTabHidden(getIndexOf(getHistoryContainerWidget()), false);
             refresh();
             setCurrentIndex(currentIndex);
         }
         catch (Exception exception)
         {
             exception.printStackTrace();
         }
     }
 
     private void checkinContent()
     {
 
         try
         {
             if (model != null)
             {
                 ((ResourceDescriptor) model).performAction(null, Action.CHECKIN);
                 refresh();
             }
         }
         catch (Exception e)
         {
             e.printStackTrace();
         }
 
     }
 
     private void checkoutContent()
     {
 
         try
         {
             if (model != null)
             {
                 ((ResourceDescriptor) model).performAction(null, Action.CHECKOUT);
             }
         }
         catch (Exception e)
         {
             e.printStackTrace();
         }
 
     }
 
     /**
      * Handles file upload event
      */
     private void fileUploaded()
     {
         try
         {
             Versionable versionableModel = null;
             if (model instanceof Versionable && ((Versionable) model).isVersioned())
             {
                 versionableModel = (Versionable) model;
             }
 
             // checkout if we need too
             if (versionableModel != null)
             {
                 versionableModel.checkout();
             }
 
             // we always get the first uploaded file, as we don't allow multiple files here
             List<UploadedFile> uploadedFiles = upload.getUploadedFiles();
             String tempFileName = uploadedFiles.get(0).getSpoolFileName();
             File tempFile = new File(tempFileName);
             // once we have a handle on the file, stream it into our resource descriptor
             // This is a little crazy, i'd rather pass a pointer around, but we're dealing with streams and jcr and all sorts of stuff
             OutputStream outputStream = ((ResourceDescriptor) WCapoResourceEditor.this.model).getOutputStream(null);
             StreamUtil.readInputStreamIntoOutputStream(new FileInputStream(tempFile), outputStream);
             outputStream.close();
 
             // checkin if we need too
             if (versionableModel != null)
             {
                 versionableModel.checkin();
             }
 
             refresh();
         }
         catch (Exception e)
         {
             e.printStackTrace();
         }
 
     }
 
     /**
      * Exposed download link so that we can change the backing model when needed.
      * 
      * @return
      */
     private WLink getDownloadLink()
     {
         if (downloadLink == null)
         {
             downloadLink = new WLink();
         }
         return downloadLink;
     }
 
     /**
      * just creates the create content button, privately exposed, so it can be enabled and disabled accordingly
      * 
      * @return
      */
     private WWidget getCreateContentButton()
     {
         if (createContentPushButton == null)
         {
             createContentPushButton = new WPushButton("Create Content");
             createContentPushButton.clicked().addListener(this, this::createContent);
         }
         return createContentPushButton;
     }
 
     /**
      * This will create an empty place holder for some text content on an empty node
      */
     private void createContent()
     {
         setContent(EMPTY_STRING, contentType, contentFormatType, mimeType, length);
     }
 
     /**
      * This just create the attribute table for the model
      * 
      * @return
      */
     private WTableView getAttributeTableView()
     {
         if (attributeTableView == null)
         {
             attributeTableView = new WTableView();
             attributeTableView.addStyleClass("bg-transparent");
             attributeTableView.setItemDelegateForColumn(0, new WCSSItemDelegate("font-weight: bold;"));
             attributeTableView.setSortingEnabled(true);
             attributeTableView.setSelectable(true);
             attributeTableView.setAlternatingRowColors(true);
             attributeTableView.setColumnResizeEnabled(true);
             attributeTableView.setColumnAlignment(0, AlignmentFlag.AlignRight);
             attributeTableView.setColumnWidth(1, new WLength(500));
             attributeTableView.setSelectionMode(SelectionMode.SingleSelection);
         }
         return attributeTableView;
     }
 
     /**
      * Will refresh all of the data from the model.
      */
     @Override
     public void refresh()
     {
         try
         {
             ((ResourceDescriptor) WCapoResourceEditor.this.model).getResourceMetaData(null).refresh();
             ((ResourceDescriptor) WCapoResourceEditor.this.model).advanceState(State.CLOSED, null);
             ((ResourceDescriptor) WCapoResourceEditor.this.model).reset(State.OPEN);
             ((ResourceDescriptorItemModel) getAttributeTableView().getModel()).reload();
             WCapoResourceEditor.this.setModel(WCapoResourceEditor.this.model);
         }
         catch (Exception e)
         {
             // TODO Auto-generated catch block
             e.printStackTrace();
         }
 
         super.refresh();
     }
 
 }
