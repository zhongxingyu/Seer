 package ddybb.d3calc.src;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Combo;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.List;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.swt.widgets.Text;
 
 public class ProfileImporter {
 	
 	private Shell shell;
 	private Composite comp;
 	private Text inputBT;
 	private String battletag;
 	private String region;
 	private Map<String, Integer> heroIDs = new HashMap<String, Integer>();
 	
 	public ProfileImporter(Display display) {
 		
 		shell = new Shell(display);
         shell.setText("Import Character");
         shell.setSize(300, 200);
         shell.setLocation(300, 300);
         
         initUI();
         
         shell.open();
 
         while (!shell.isDisposed()) {
         	if (!display.readAndDispatch()) {
             display.sleep();
         	}
         }
 	}
 	
 	private void initUI() {
 		
 		GridLayout layout = new GridLayout(2, false);
 		comp = new Composite(shell, SWT.NONE);
 		comp.setLayout(layout);
 		
 		Label label = new Label(comp, SWT.NONE);
 		label.setText("Battletag:");
 		label.setToolTipText("Player-1234 or Player#1234");
 		
 		final Combo combo = new Combo(comp, SWT.DROP_DOWN | SWT.READ_ONLY);
 		combo.add("Region");
 		combo.add("us");
         combo.add("eu");
         combo.select(0);
 		
         GridData data = new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1);
         data.horizontalSpan = 2;
         inputBT = new Text(comp, SWT.BORDER);
 		inputBT.setText("");
 		inputBT.setLayoutData(data);
 		
 		data = new GridData();
         data.widthHint = 80;
         data.heightHint = 30;
         Button okButton = new Button(comp, SWT.PUSH);
         okButton.setText("Get Heroes");
         okButton.setLayoutData(data);
         Button cancelButton = new Button(comp, SWT.PUSH);
         cancelButton.setText("Cancel");
         cancelButton.setLayoutData(data);
         
         comp.pack();
         shell.pack();
         
         okButton.addSelectionListener(new SelectionAdapter() {
 
             @Override
             public void widgetSelected(SelectionEvent e) {
             	region = combo.getText();
             	if (inputBT.getText() != null && region != "Region") {
             		getHeroes(inputBT.getText(), region);
             	}
             	
             };
         });
         
         cancelButton.addSelectionListener(new SelectionAdapter() {
 
             @Override
             public void widgetSelected(SelectionEvent e) {
                 shell.dispose();
             };
         });
 		
 	}
 	
 	private void getHeroes(String btag, String region) {
 		
 		//format and get profile
 		battletag = btag.replace("#", "-");
 		D3API.getProfile(battletag, region);
 		Map<String, Object> profile = D3API.getProfile(battletag, region);
 		
 		//lazy fail check
 		if (profile == null) { return; }
 		
 		//create hero list
 		ArrayList<Object> heroes = (ArrayList)profile.get("heroes");
 		for (Object o : heroes) {
 			Map<String, Object> temp = (Map)o;
			heroIDs.put((String)temp.get("name"), (int)temp.get("id"));
 		}
 		
 		drawHeroSelect();
 		
 	}
 	
 	private void drawHeroSelect() {
 		
 		GridLayout layout = new GridLayout(2, false);
 		comp.dispose();
 		comp = new Composite(shell, SWT.NONE);
 		comp.setLayout(layout);
 		
 		final Label label = new Label(comp, SWT.NONE);
 		label.setText("Select Hero:");
 		
 		GridData data = new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1);
 		final List list = new List(comp, SWT.BORDER);
 		list.setLayoutData(data);
		for (String name : heroIDs.keySet()) {
			list.add(name);
 		}
 		
 		data = new GridData();
         data.widthHint = 80;
         data.heightHint = 30;
         Button okButton = new Button(comp, SWT.PUSH);
         okButton.setText("Import Hero");
         okButton.setLayoutData(data);
         Button cancelButton = new Button(comp, SWT.PUSH);
         cancelButton.setText("Cancel");
         cancelButton.setLayoutData(data);
         
         comp.pack();
         shell.pack();
         
         okButton.addSelectionListener(new SelectionAdapter() {
 
             @Override
             public void widgetSelected(SelectionEvent e) {
             	if (list.getSelectionIndex() != -1 ) {
             		int heroIndex = heroIDs.get(list.getSelection()[0]);
                 	HeroImporter.importHero(D3API.getHero(battletag, region, heroIndex), region);
                 	shell.dispose();
             	}
             };
         });
         
         cancelButton.addSelectionListener(new SelectionAdapter() {
 
             @Override
             public void widgetSelected(SelectionEvent e) {
                 shell.dispose();
             };
         });
 		
 	}
 
 }
