 package ru.cut2drox.ui;
 
 import java.awt.Toolkit;
 import java.awt.datatransfer.StringSelection;
 import java.io.ByteArrayInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.net.URISyntaxException;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Event;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Listener;
 import org.eclipse.swt.widgets.Monitor;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.graphics.ImageData;
 import org.eclipse.swt.graphics.ImageLoader;
 import com.dropbox.client2.exception.DropboxException;
 
 import ru.cut2drox.brain.DBWrapper;
 import ru.cut2drox.brain.TrayIcon;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.wb.swt.SWTResourceManager;
 
 public class SendForm {
 
 	public static final String DATE_FORMAT_NOW = "yyyyMMddHHmmss";
 	protected static Shell shell;
 	Shell shellForm;
 	static Display display;
 	static TrayIcon ti;
 	
 	public static void main(String[] args) {
 		display = new Display();
 	    shell = new Shell(display);
 	    
 	    ti = new TrayIcon(display,shell);
 
 	    while (!shell.isDisposed()) {
 	        if (!display.readAndDispatch())
 	          display.sleep();
 	      }
 	    display.dispose();
 	    
 	}
 	
 	/**
 	 * @param tip 
 	 * @wbp.parser.entryPoint
 	 */
 	public void open(Image image) {
 		display = Display.getDefault();
 		
 		createContents(image);
 		shellForm.open();
 		shellForm.layout();
 		while (!shellForm.isDisposed()) {
 			if (!display.readAndDispatch()) {
 				display.sleep();
 			}
 		}
 	}
 
 	protected void createContents(final Image image) {
 		shellForm = new Shell();
 		shellForm.setSize(378, 352);
 		shellForm.setText("SWT Application");
 		shellForm.setLayout(new GridLayout(2, false));
 		
 		Monitor[] list = display.getMonitors();
 		org.eclipse.swt.graphics.Rectangle client = shellForm.getBounds();
 		org.eclipse.swt.graphics.Rectangle screen = list[0].getBounds();
		client.width=image.getBounds().width+85; //20-на левый отступ + 25 на кнопку + около 20 на скролл 
		client.height=image.getBounds().height+110;
 		client.x = screen.width/2 -client.width/2;
 		client.y = screen.height/2 - client.height/2;
 		shellForm.setBounds(client);
 		
 		final ResizableCanvas canvas = new ResizableCanvas(shellForm,  SWT.NO_BACKGROUND | SWT.NO_REDRAW_RESIZE | SWT.V_SCROLL  | SWT.H_SCROLL,display);
 		
 		canvas.setImage(image);
 		GridData gd_canvas = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
 		gd_canvas.heightHint = 1;
 		gd_canvas.widthHint = 1;
 		canvas.setLayoutData(gd_canvas);
 
 		canvas.addScrolling();
 
 		shellForm.addListener (SWT.Resize,  new Listener () {
 		    public void handleEvent (Event e) {
		    	int i=shellForm.getClientArea().width-53;
		    	int j=shellForm.getClientArea().height-55;
 		    if(i>image.getBounds().width+15)
 		    {
 		    	i=image.getBounds().width+17;
 		    }
 		    if(j>image.getBounds().height+15)
 		    {
 		    	j=image.getBounds().height+17;
 		    }
		    	canvas.setRBounds(20, 20, i, j);
 		    } 
 		});
 		
 		
 		
 		Composite composite = new Composite(shellForm, SWT.NONE);
 		GridData gd_composite = new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1);
 		gd_composite.heightHint = 101;
 		composite.setLayoutData(gd_composite);
 		
 		Button btnNewButton_1 = new Button(composite, SWT.NONE);
 		btnNewButton_1.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent arg0) {
 				TextLabel tl = new TextLabel(shellForm,SWT.DIALOG_TRIM);
 				try {
 					tl.open(canvas,image); 
 					} catch (FileNotFoundException e) {e.printStackTrace();}
 			}
 		});
 		btnNewButton_1.setImage(SWTResourceManager.getImage("D:\\T6.png"));
		btnNewButton_1.setBounds(0, 14, 25, 25);
 		btnNewButton_1.setText("");
 		
 		Button btnNewButton_2 = new Button(composite, SWT.NONE);
		btnNewButton_2.setBounds(0, 45, 25, 25);
 		btnNewButton_2.setText("");
 		
 		Button button = new Button(composite, SWT.NONE);
 		button.setText("");
		button.setBounds(0, 76, 25, 25);
         //-----------------------------------  
 		
 		Button btnNewButton = new Button(shellForm, SWT.NONE);
 		btnNewButton.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 2, 1));
 		btnNewButton.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent arg0) {
 				shellForm.close();
 				try {
 					Calendar cal = Calendar.getInstance();
 					SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_NOW);
 					ImageLoader loader = new ImageLoader();
 					loader.data = new ImageData[] {image.getImageData()};
 					String partName = sdf.format(cal.getTime());
 					String nameImage = partName +".jpg";
 					String fullPath = ClassLoader.getSystemResource(".").getPath()+"images/"+nameImage;
 					//Проверять на папку и если нету создавать!!!!
 					loader.save(fullPath, SWT.IMAGE_JPEG);
 					DBWrapper connection = new DBWrapper();
 					String imageShortURL = connection.sendImage(fullPath, nameImage);
 					
 					//--------govnokod------------------
 					String content = "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" " +
 							"\"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">" +
 							"<html xmlns=\"http://www.w3.org/1999/xhtml\"><head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=windows-1251\" />" +
 							"<title>cut2drox</title></head> " +
 							"<body><div align=\"center\">" +
 							"<img src=\""+imageShortURL +"\"/></div> " +
 							"</body></html>";
 					String nameCover = partName + ".xhtml";
 					ByteArrayInputStream inputStream = new ByteArrayInputStream(content.getBytes());
 					String finalURL = connection.sendFile(inputStream, content.length(), nameCover);
 					
 					StringSelection ss = new StringSelection(finalURL);
 					Toolkit.getDefaultToolkit().getSystemClipboard().setContents(ss, null);
 					
 					ti.showBalloon(shell);
 					//----------------------------------	
 		  
 				} catch (MalformedURLException e) {
 					e.printStackTrace();
 				} catch (DropboxException e) {
 					e.printStackTrace();
 				} catch (IOException e) {
 					e.printStackTrace();
 				} catch (URISyntaxException e) {
 					e.printStackTrace();
 				}
 			}
 		});
 		btnNewButton.setText("\u041E\u0442\u043F\u0440\u0430\u0432\u0438\u0442\u044C");
 
 	}
 }
