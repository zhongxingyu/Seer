 /*===========================================================================
   Copyright (C) 2008-2009 by the Okapi Framework contributors
 -----------------------------------------------------------------------------
   This library is free software; you can redistribute it and/or modify it 
   under the terms of the GNU Lesser General Public License as published by 
   the Free Software Foundation; either version 2.1 of the License, or (at 
   your option) any later version.
 
   This library is distributed in the hope that it will be useful, but 
   WITHOUT ANY WARRANTY; without even the implied warranty of 
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser 
   General Public License for more details.
 
   You should have received a copy of the GNU Lesser General Public License 
   along with this library; if not, write to the Free Software Foundation, 
   Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 
   See also the full LGPL text here: http://www.gnu.org/copyleft/lesser.html
 ===========================================================================*/
 
 package net.sf.okapi.ui.filters.plaintext.tests;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.net.URL;
 
 import net.sf.okapi.common.BaseContext;
 import net.sf.okapi.common.IContext;
 import net.sf.okapi.common.IParameters;
 import net.sf.okapi.common.IParametersEditor;
 import net.sf.okapi.common.Util;
 import net.sf.okapi.common.ui.Dialogs;
import net.sf.okapi.common.ui.abstracteditor.IDialogPage;
import net.sf.okapi.common.ui.abstracteditor.InputQueryDialog;
 
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.MouseAdapter;
 import org.eclipse.swt.events.MouseEvent;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.graphics.Rectangle;
 import org.eclipse.swt.layout.FormAttachment;
 import org.eclipse.swt.layout.FormData;
 import org.eclipse.swt.layout.FormLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Group;
 import org.eclipse.swt.widgets.List;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.swt.widgets.Text;
 
 /**
  * Okapi GUI tester application
  * 
  * @version 0.1, 20.06.2009
  */
 
 public class Okapi_GUI_Tester {
 
 	String[] GUI_CLASSES = new String[] {
 
 			// swt_test.Editor.class.getName(),
 			// net.sf.okapi.steps.ui.xsltransform.ParametersEditor.class.getName(),
 /* temporarily commented out during conversion to maven
 			net.sf.okapi.ui.steps.tokenization.mapping.Mapper.class.getName(),
 			net.sf.okapi.ui.steps.tokenization.mapping.MappingItemPage.class.getName(),
 			net.sf.okapi.ui.steps.wordcount.ParametersEditor.class.getName(),
 			net.sf.okapi.ui.steps.tokenization.tokens.TokensTab.class.getName(),
 			net.sf.okapi.ui.steps.tokenization.tokens.AddModifyTokenPage.class.getName(),
 			net.sf.okapi.ui.steps.tokenization.locale.LanguageListTab.class.getName(),
 			net.sf.okapi.ui.filters.table.Editor.class.getName(),
 			net.sf.okapi.ui.filters.plaintext.Editor.class.getName(),
 			net.sf.okapi.ui.filters.plaintext.common.InputQueryPageInt.class.getName(),
 			net.sf.okapi.ui.filters.plaintext.common.InputQueryPageString.class.getName(),
 			net.sf.okapi.ui.filters.table.AddModifyColumnDefPage.class.getName(),					
 			net.sf.okapi.filters.ui.openoffice.Editor.class.getName(),
 			net.sf.okapi.filters.ui.po.Editor.class.getName(),
 			net.sf.okapi.filters.ui.properties.Editor.class.getName(),
 //			net.sf.okapi.ui.filters.html.Editor.class.getName(),
 			net.sf.okapi.filters.ui.ts.Editor.class.getName(),			
 			net.sf.okapi.ui.filters.openxml.Editor.class.getName(),
 			net.sf.okapi.filters.ui.regex.Editor.class.getName(),
 			net.sf.okapi.steps.ui.bomconversion.ParametersEditor.class.getName(),
 			net.sf.okapi.steps.ui.charlisting.ParametersEditor.class.getName(),
 			net.sf.okapi.steps.ui.encodingconversion.ParametersEditor.class.getName(),
 			net.sf.okapi.steps.ui.linebreakconversion.ParametersEditor.class.getName(),
 			net.sf.okapi.steps.ui.searchandreplace.ParametersEditor.class.getName(),
 			net.sf.okapi.steps.ui.segmentation.ParametersEditor.class.getName(),
 			net.sf.okapi.steps.ui.textmodification.ParametersEditor.class.getName(),
 			net.sf.okapi.steps.ui.translationcomparison.ParametersEditor.class.getName(),
 			net.sf.okapi.steps.ui.uriconversion.ParametersEditor.class.getName(),
 			net.sf.okapi.steps.ui.fullwidthconversion.ParametersEditor.class.getName()
 */
 		};
 
 	private Group grpParameters;
 	private FormData formData_1;
 	private Text text;
 	private Button button_1;
 	private FormData formData_3;
 	private FormData formData_5;
 	private Group grpParameterEditors;
 	private List list;
 	private Text text_1;
 	private Button button_2;
 	private IParameters params = null;
 	private Button btnClear;
 	private Button button;
 	private FormData formData_4;
 	
 	protected Shell shell;
 
 	/**
 	 * Launch the application.
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		try {
 			Okapi_GUI_Tester window = new Okapi_GUI_Tester();
 			window.open();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * Open the window.
 	 */
 	public void open() {
 		Display display = Display.getDefault();
 		createContents();
 		shell.open();
 		shell.layout();
 		while (!shell.isDisposed()) {
 			if (!display.readAndDispatch()) {
 				display.sleep();
 			}
 		}
 	}
 
 	/**
 	 * Create contents of the window.
 	 */
 	protected void createContents() {
 		shell = new Shell();
 		shell.setLayout(new FormLayout());
 		shell.setSize(576, 534);
 		shell.setText("Okapi GUI Tester");
 		
 		URL url = Okapi_GUI_Tester.class.getResource("Rainbow.png");
 		if (url == null) return;
 		
 		String root = Util.getDirectoryName(url.getPath());		
 		shell.setImage(new Image(Display.getCurrent(), root + "/Rainbow.png"));
 		
 		{
 			grpParameters = new Group(shell, SWT.NONE);
 			grpParameters.setLayout(new FormLayout());
 			grpParameters.setText("Parameters");
 			{
 				formData_1 = new FormData();
 				formData_1.bottom = new FormAttachment(100);
 				formData_1.right = new FormAttachment(100, -17);
 				formData_1.left = new FormAttachment(0, 14);
 				formData_1.height = 110;
 				formData_1.width = 449;
 				grpParameters.setLayoutData(formData_1);
 			}
 			{
 				text = new Text(grpParameters, SWT.BORDER);
 				{
 					formData_3 = new FormData();
 					formData_3.left = new FormAttachment(0, 10);
 					formData_3.right = new FormAttachment(100, -4);
 					formData_3.top = new FormAttachment(0, 4);
 					text.setLayoutData(formData_3);
 				}
 				text.setEditable(false);
 			}
 			{
 				button_1 = new Button(grpParameters, SWT.NONE);
 				button_1.addSelectionListener(new SelectionAdapter() {
 					public void widgetSelected(SelectionEvent e) {
 						
 						String[] selected = Dialogs.browseFilenames(shell, "Open", false, null, 
 								"Filter Parameters (*.fprm)\tAll Files (*.*)",
 								"*.fprm\t*.*"); 
 						
 				        if (selected != null && selected.length > 0 && !Util.isEmpty(selected[0])) {
 				        	text.setText(selected[0]);
 				        	text_1.setText(fileAsString(selected[0]));
 				        	text_1.setFocus();
 				        }
 					}
 				});
 				{
 					formData_5 = new FormData();
 					formData_5.right = new FormAttachment(text, 0, SWT.RIGHT);
 					formData_5.width = 84;
 					button_1.setLayoutData(formData_5);
 				}
 				button_1.setText("Open...");
 			}
 			{
 				button = new Button(grpParameters, SWT.NONE);
 				button.addSelectionListener(new SelectionAdapter() {
 					public void widgetSelected(SelectionEvent e) {
 						
 						String selected = Dialogs.browseFilenamesForSave(shell, "Save As", text.getText(), 
 								"Filter Parameters (*.fprm)\tAll Files (*.*)",
 								"*.fprm\t*.*");
 						
 				        if (!Util.isEmpty(selected)) {
 				        	
 				        	text.setText(selected);
 				        	
 				        	if (params == null) {
 				        		if (!Util.isEmpty(text_1.getText()))
 				        			writeToFile(selected, text_1.getText());
 				        	}
 				        	else
 				        		params.save(selected);
 				        }
 				        	
 					}
 				});
 				{
 					formData_4 = new FormData();
 					formData_4.top = new FormAttachment(button_1, 4);
 					formData_4.right = new FormAttachment(text, 0, SWT.RIGHT);
 					formData_4.width = 84;
 					button.setLayoutData(formData_4);
 				}
 				button.setText("Save As...");
 			}
 		}
 		
 		grpParameterEditors = new Group(shell, SWT.NONE);
 		formData_1.top = new FormAttachment(grpParameterEditors, 4);
 		
 		text_1 = new Text(grpParameters, SWT.BORDER | SWT.WRAP | SWT.V_SCROLL | SWT.MULTI);
 		FormData formData_7 = new FormData();
 		formData_7.left = new FormAttachment(0, 10);
 		formData_7.right = new FormAttachment(button_1, -4);
 		formData_7.top = new FormAttachment(text, 4);
 		formData_7.bottom = new FormAttachment(100, -4);
 		text_1.setLayoutData(formData_7);
 		grpParameterEditors.setLayout(new FormLayout());
 		grpParameterEditors.setText("Registered Classes");
 		FormData formData = new FormData();
 		formData.right = new FormAttachment(100, -17);
 		formData.left = new FormAttachment(0, 14);
 		
 		btnClear = new Button(grpParameters, SWT.NONE);
 		formData_5.top = new FormAttachment(btnClear, 4);
 		btnClear.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 //				SWTUtils.inputQuery(shell, "caption", "Input a value prompt:", 1, null);
 				text.setText("");
 				text_1.setText("");
 			}
 		});
 		FormData formData_2 = new FormData();
 		formData_2.top = new FormAttachment(text, 4);
 		formData_2.right = new FormAttachment(text, 0, SWT.RIGHT);
 		formData_2.width = 84;
 		btnClear.setLayoutData(formData_2);
 		btnClear.setText("Clear");
 		formData.top = new FormAttachment(0, 4);
 		formData.height = 207;
 		formData.width = 531;
 		grpParameterEditors.setLayoutData(formData);
 		
 		list = new List(grpParameterEditors, SWT.BORDER | SWT.V_SCROLL);
 		list.addMouseListener(new MouseAdapter() {
 			public void mouseDoubleClick(MouseEvent e) {
 				if (list.getSelectionCount() > 0)
 						displayEditor(list.getSelection()[0]);
 			}
 		});
 		FormData formData_6 = new FormData();
 		formData_6.bottom = new FormAttachment(100, -4);
 		formData_6.top = new FormAttachment(0);
 		formData_6.right = new FormAttachment(100, -91);
 		formData_6.left = new FormAttachment(0, 4);
 		list.setLayoutData(formData_6);
 		
 		list.setItems(GUI_CLASSES);
 		list.select(0);
 		
 		button_2 = new Button(grpParameterEditors, SWT.NONE);
 		button_2.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				if (list.getSelectionCount() > 0)
 					displayEditor(list.getSelection()[0]);
 			}
 		});
 		FormData formData_8 = new FormData();
 		formData_8.left = new FormAttachment(list, 4);
 		formData_8.right = new FormAttachment(100, -4);
 		formData_8.top = new FormAttachment(0, 4);
 		button_2.setLayoutData(formData_8);
 		button_2.setText("Show...");
 
 		shell.pack();
 		Rectangle Rect = shell.getBounds();
 		if (Rect.height < 600) Rect.height = 600; 
 		shell.setMinimumSize(Rect.width, Rect.height);
 		Dialogs.centerWindow(shell, null);
 	}
 
 	@SuppressWarnings("unchecked")
 	private void displayEditor(String editorClass) {
 		
 		if (Util.isEmpty(editorClass)) return;
 		
 		Class c = null;
 		try {
 			c = Class.forName(editorClass);
 		} catch (ClassNotFoundException e) {
 			e.printStackTrace();
 		}
 				
 		if (IParametersEditor.class.isAssignableFrom(c)) {
 			
 			IParametersEditor editor = null;
 			try {
 				editor = (IParametersEditor) c.newInstance();
 				
 			} catch (InstantiationException e) {
 				
 				e.printStackTrace();
 				
 			} catch (IllegalAccessException e) {
 				
 				e.printStackTrace();
 			}		
 			IContext context = new BaseContext();
 			context.setObject("shell", shell);
 			
 			params = editor.createParameters();
 			if (params == null)
 				
 				Dialogs.showWarning(shell, "Parameters were not created.", null);
 			else {
 			
 				if (!Util.isEmpty(text_1.getText()))
 					params.fromString(text_1.getText());
 				
 				else if (!Util.isEmpty(text.getText()))
 					params.load(Util.toURI(text.getText()), true);
 				
 				else Dialogs.showWarning(shell, "No parameters loaded, defaults used.", null);
 			}
 			
 			if (editor.edit(params, false, context)) {
 				
 				if (params != null)
 					text_1.setText(params.toString());
 			}
 			else
 				params = null;
 		}
 		else if (IDialogPage.class.isAssignableFrom(c)) {
 			
 			InputQueryDialog dlg = new InputQueryDialog();
 			dlg.run(shell, c, shell.getText(), "Input a value:", null, null);
 		}
 		
 	}
 	
 	private String fileAsString(String fileName) {
 		BufferedReader reader = null;
 		try {
 			reader = new BufferedReader(new FileReader(fileName));
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		}
 
 		StringBuilder tmp = new StringBuilder();
 		char[] buf = new char[2048];
 		int count = 0;
 		try {
 			while (( count = reader.read(buf)) != -1 ) {
 				tmp.append(buf, 0, count);
 			}
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		
         return tmp.toString();
     }
 	
 	private boolean writeToFile(String fileName, String st) {
 		
 		try {
 	        BufferedWriter out = new BufferedWriter(new FileWriter(fileName));
 	        out.write(st);
 	        out.close();
 	        
 	    } catch (IOException e) {
 	    	
 	    	return false;
 	    }
 
 		return true;		
 	}
 
 }
 
