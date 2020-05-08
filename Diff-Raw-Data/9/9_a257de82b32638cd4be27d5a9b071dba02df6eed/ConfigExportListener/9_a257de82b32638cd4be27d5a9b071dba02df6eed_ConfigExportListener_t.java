 package org.hazelwire.gui;
 
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.MouseEvent;
 import org.eclipse.swt.events.MouseListener;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.FileDialog;
 import org.hazelwire.xml.ExportModuleConfiguration;
 
 /**
  * This class is responsible for converting the current 
  * generation settings to an XML file. It is a subclass of
  * {@link MouseListener} and it is bound to the 'export to
  * XML' button.
  */
 public class ConfigExportListener implements MouseListener
 {	
 	@Override
 	public void mouseDoubleClick(MouseEvent arg0)
 	{}
 
 	@Override
 	public void mouseDown(MouseEvent arg0)
 	{}
 
 	@Override
 	/**
 	 * When the 'export to XML' button is clicked, this method is called.
 	 * It creates a new {@link FileDialog}, in which the user can find the
 	 * location to save the XML file to and enter its name, followed by the
 	 * .xml extension. If the user completes these steps, the method will
 	 * export the current configuration to an XML file.
 	 */
 	public void mouseUp(MouseEvent m)
 	{
 		FileDialog fd = new FileDialog(((Button)m.getSource()).getShell(), SWT.SAVE);
         fd.setText("Export configuration");
         String[] ext = new String[1];
        ext[0] = "*.xml";
         fd.setFilterExtensions(ext);
 
         String selected = fd.open();
         if(selected!=null){
         	try
 			{
         		GUIBridge.synchronizeModulesFrontToBack();
 				new ExportModuleConfiguration().exportModuleConfiguration(selected);
 			}
 			catch (Exception e)
 			{
 				// TODO proper visual errors
 				e.printStackTrace();
 			}
         }
 	}
 
 }
