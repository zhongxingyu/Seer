 package net.comes.care.ui.views;
 
 import javax.annotation.PostConstruct;
 import javax.inject.Inject;
 
 import net.comes.care.ui.viewer.SensorTableViewer;
 
 import org.eclipse.e4.ui.di.UIEventTopic;
 import org.eclipse.jface.dialogs.MessageDialog;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Text;
 
 import de.lmu.ifi.dbs.medmon.sensor.core.ISensor;
 import de.lmu.ifi.dbs.medmon.sensor.core.ISensorDirectoryService;
 
 public class SensorView {
 
 	public static final String ID = "net.comes.care.ui.view.sensor";
 
 	@Inject
 	private ISensorDirectoryService sensorDirectory;
 
 	private Text txtSearch;
 	private SensorTableViewer sensorTableViewer;
 
 	@PostConstruct
 	protected void createContent(Composite parent) {
 		parent.setLayout(new GridLayout(1, false));
 
 		txtSearch = new Text(parent, SWT.BORDER);
 		txtSearch.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
 
 		sensorTableViewer = new SensorTableViewer(parent, SWT.BORDER | SWT.FULL_SELECTION);
 		sensorTableViewer.getTable().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
 		
 		sensorTableViewer.setInput(sensorDirectory.getSensors());	
 	}
 
 	
 	@Inject
	protected void sensorChanged(final @UIEventTopic(ISensorDirectoryService.SENSOR_TOPIC) ISensor sensor) {
 		if(sensorTableViewer == null || sensorTableViewer.getControl().isDisposed())
 			return;
 		
 		//Even it's a UIEventTopic it isn't always synced.
 		sensorTableViewer.getControl().getDisplay().asyncExec(new Runnable() {
 			@Override
 			public void run() {
 				sensorTableViewer.setInput(sensorDirectory.getSensors());
 				MessageDialog.openConfirm(txtSearch.getShell(), "Sensor changed", "Sensor " + sensor.getName());				
 			}
 		});
 
 	}
 	
 }
