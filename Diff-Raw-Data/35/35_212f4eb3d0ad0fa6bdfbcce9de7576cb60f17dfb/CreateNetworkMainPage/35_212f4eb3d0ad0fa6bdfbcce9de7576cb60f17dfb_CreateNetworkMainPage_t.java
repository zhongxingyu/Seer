 /* AWE - Amanzi Wireless Explorer
  * http://awe.amanzi.org
  * (C) 2008-2009, AmanziTel AB
  *
  * This library is provided under the terms of the Eclipse Public License
  * as described at http://www.eclipse.org/legal/epl-v10.html. Any use,
  * reproduction or distribution of the library constitutes recipient's
  * acceptance of this agreement.
  *
  * This library is distributed WITHOUT ANY WARRANTY; without even the
  * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
  */
 
 package org.amanzi.neo.wizards;
 
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.Iterator;
 
 import org.amanzi.neo.loader.ui.preferences.CommonCRSPreferencePage;
 import org.amanzi.neo.loader.ui.utils.LoaderUiUtils;
 import org.amanzi.neo.services.DatasetService;
 import org.amanzi.neo.services.NeoServiceFactory;
 import org.amanzi.neo.services.enums.INodeType;
 import org.amanzi.neo.services.enums.NodeTypes;
 import org.amanzi.neo.services.ui.utils.ActionUtil;
 import org.amanzi.neo.services.utils.RunnableWithResult;
 import org.apache.commons.lang.StringUtils;
 import org.eclipse.jface.dialogs.DialogPage;
 import org.eclipse.jface.preference.IPreferenceNode;
 import org.eclipse.jface.preference.PreferenceDialog;
 import org.eclipse.jface.preference.PreferenceManager;
 import org.eclipse.jface.preference.PreferenceNode;
 import org.eclipse.jface.wizard.WizardPage;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.ModifyEvent;
 import org.eclipse.swt.events.ModifyListener;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.SelectionListener;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Group;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.List;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.swt.widgets.Text;
 import org.eclipse.ui.PlatformUI;
 import org.geotools.referencing.CRS;
 import org.neo4j.graphdb.Node;
 import org.neo4j.graphdb.traversal.Traverser;
 import org.opengis.referencing.NoSuchAuthorityCodeException;
 import org.opengis.referencing.crs.CoordinateReferenceSystem;
 
 /**
  * <p>
  * Main page for custom network creation
  * </p>
  * .
  * 
  * @author tsinkel_a
  * @since 1.0.0
  */
 public class CreateNetworkMainPage extends WizardPage {
     
     /** The service. */
     private final DatasetService service = NeoServiceFactory.getInstance().getDatasetService();
     /** String CREATE_NETWORK_STRUCTURE field. */
     private static final String CREATE_NETWORK_STRUCTURE = "Create network structure";
 
     /** The select crs. */
     private Button selectCRS;
 
     /** The selected crs. */
     private CoordinateReferenceSystem selectedCRS;
 
     /** The add. */
     private Button add;
 
     /** The up. */
     private Button up;
 
     /** The down. */
     private Button down;
 
     /** The remove. */
     private Button remove;
 
     /** The structure. */
     private final ArrayList<INodeType> structure=new ArrayList<INodeType>();
 
     /** The structure list. */
     private List structureList;
 
     /** The restricted names. */
     private HashSet<String> restrictedNames;
 
     /** The network. */
     private Text network;
 
     /** The network name. */
     private String networkName;
 
     /** The main. */
     private Group main;
    private Button addOnMapBtn;
    protected boolean addOnMap;
 
     /**
      * Instantiates a new creates the network main page.
      * 
      * @param pageName the page name
      */
     public CreateNetworkMainPage(String pageName) {
         super(pageName);
         setTitle(CREATE_NETWORK_STRUCTURE);
     }
 
     /**
     * Checks if is adds the on map.
     *
     * @return true, if is adds the on map
     */
    public boolean isAddOnMap() {
        return addOnMap;
    }

    /**
      * Gets the structure.
      * 
      * @return the structure
      */
     public java.util.List<INodeType> getStructure() {
         return structure;
     }
 
     /**
      * Creates the control.
      * 
      * @param parent the parent
      */
     @Override
     public void createControl(Composite parent) {
         main = new Group(parent, SWT.FILL);
         main.setLayout(new GridLayout(3, false));
         Label networklb = new Label(main, SWT.LEFT);
         networklb.setText("Network:");
         network = new Text(main, SWT.FILL | SWT.BORDER);
         network.addModifyListener(new ModifyListener() {
 
             @Override
             public void modifyText(ModifyEvent e) {
                 updateNetworkName(e);
             }
         });
         GridData networkLayoutdata = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
         // networkLayoutdata.minimumWidth=200;
         network.setLayoutData(networkLayoutdata);
         selectCRS = new Button(main, SWT.FILL | SWT.PUSH);
         selectCRS.setAlignment(SWT.LEFT);
         selectCRS.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
         selectCRS.addSelectionListener(new SelectionListener() {
 
             @Override
             public void widgetSelected(SelectionEvent e) {
                 selectCRS();
             }
 
             @Override
             public void widgetDefaultSelected(SelectionEvent e) {
                 widgetSelected(e);
             }
         });
         Group gr = new Group(main, SWT.FILL);
         gr.setText("Network structure:");
         GridData layoutData = new GridData(SWT.FILL, SWT.FILL, true, true, 3, 1);
         gr.setLayoutData(layoutData);
         gr.setLayout(new GridLayout(2, false));
         structureList = new List(gr, SWT.FILL | SWT.BORDER | SWT.V_SCROLL);
         structureList.addSelectionListener(new SelectionListener() {
 
             @Override
             public void widgetSelected(SelectionEvent e) {
                 changeListSelection();
             }
 
             @Override
             public void widgetDefaultSelected(SelectionEvent e) {
                 widgetSelected(e);
             }
         });
         layoutData = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 4);
         structureList.setLayoutData(layoutData);
         add = new Button(gr, SWT.PUSH);
         add.addSelectionListener(new SelectionListener() {
             
             @Override
             public void widgetSelected(SelectionEvent e) {
                 addNewType();
             }
             
             @Override
             public void widgetDefaultSelected(SelectionEvent e) {
                 widgetSelected(e);
             }
         });
         add.setText("add");
         add.setLayoutData(new GridData(SWT.FILL, SWT.UP, false, false, 1, 1));
         up = new Button(gr, SWT.PUSH);
         up.setText("up");
         up.setLayoutData(new GridData(SWT.FILL, SWT.UP, false, false, 1, 1));
         up.setEnabled(false);
         up.addSelectionListener(new SelectionListener() {
             
             @Override
             public void widgetSelected(SelectionEvent e) {
                 up();
             }
             
             @Override
             public void widgetDefaultSelected(SelectionEvent e) {
                 widgetSelected(e);
             }
         });
         down = new Button(gr, SWT.PUSH);
         down.setText("down");
         down.setLayoutData(new GridData(SWT.FILL, SWT.UP, false, false, 1, 1));
         down.setEnabled(false);
         down.addSelectionListener(new SelectionListener() {
             
             @Override
             public void widgetSelected(SelectionEvent e) {
                 down();
             }
             
             @Override
             public void widgetDefaultSelected(SelectionEvent e) {
                 widgetSelected(e);
             }
         });
         remove = new Button(gr, SWT.PUSH);
         remove.setText("remove");
         remove.setLayoutData(new GridData(SWT.FILL, SWT.UP, false, false, 1, 1));
         remove.setEnabled(false);
         remove.addSelectionListener(new SelectionListener() {
             
             @Override
             public void widgetSelected(SelectionEvent e) {
                 remove();
             }
             
             @Override
             public void widgetDefaultSelected(SelectionEvent e) {
                 widgetSelected(e);
             }
         });
/*        addOnMapBtn=new Button(main,SWT.CHECK);
        addOnMapBtn.setText("add on active map");
        addOnMapBtn.addSelectionListener(new SelectionListener() {
            
            @Override
            public void widgetSelected(SelectionEvent e) {
                addOnMap=addOnMapBtn.getSelection();
            }
            
            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                widgetSelected(e);
            }
        });*/
         updateButtonLabel();
         init();
         setControl(main);
         validate();
     }
 
 
 
     /**
      * Removes the element from list.
      */
     protected void remove() {
         int id= structureList.getSelectionIndex();
         if (id<0){
             return;
         }
         INodeType elem = structure.remove(id);
         ((CreateNetworkWizard)getWizard()).removePage(elem);
         updateList();
         structureList.select(id);
         changeListSelection();
         validate();
     }
 
     /**
      * Down the element in list.
      */
     protected void down() {
         int id= structureList.getSelectionIndex();
         if (id>structure.size()-2){
             return;
         }
         INodeType elem = structure.remove(id);
         structure.add(id+1, elem);
         updateList();
         structureList.select(id+1);
         changeListSelection();
         validate();
     }
 
     /**
      * Up the element in list.
      */
     protected void up() {
         int id= structureList.getSelectionIndex();
         if (id<1){
             return;
         }
         INodeType elem = structure.remove(id);
         structure.add(id-1, elem);
         updateList();
         structureList.select(id-1);
         changeListSelection();
         validate();
     }
 
 
     /**
      * Adds the new type.
      */
     protected void addNewType() {
        SelectType dialog = new SelectType(main.getShell(), "Select type to add", getPossibleTypes());
        String newType = dialog.open();
        if (StringUtils.isNotEmpty(newType)){
            INodeType type=service.getNodeType(newType,true);
            addNewType(type);
        }
     }
 
 
     /**
      * Adds the new type.
      *
      * @param type the type
      */
     private void addNewType(INodeType type) {
         if (structure.contains(type)){
             return;
         }
         int id = structureList.getSelectionIndex();
         id=id<0?0:id>structure.size()-3?structure.size()-3:id;
         structure.add(id+1, type);
         addPage(type);
         updateList();
         structureList.select(id);
         changeListSelection();
         validate();
         
     }
 
     /**
      * Gets the possible types.
      *
      * @return the possible types
      */
     private java.util.List<INodeType> getPossibleTypes() {
         java.util.List<INodeType>result=getAllPossibletypes();
         Iterator<INodeType> it = result.iterator();
         while (it.hasNext()) {
              INodeType iNodeType = it.next();
              if (structure.contains(iNodeType)){
                  it.remove();
              }
             
         }
         return result;
     }
 
     /**
      * Gets the all possibletypes.
      *
      * @return the all possibletypes
      */
     private java.util.List<INodeType> getAllPossibletypes() {
         java.util.List<INodeType> result=new ArrayList<INodeType>();
         result.add(NodeTypes.CITY);
         result.add(NodeTypes.BSC);
         result.addAll(service.getUserDefinedNodeTypes());
         return result;
     }
 
     /**
      * Change list selection.
      *
      */
     protected void changeListSelection() {
         int selId = structureList.getSelectionIndex();
         if (selId < 2 || selId >= structure.size() - 2) {
             up.setEnabled(false);
         } else {
             up.setEnabled(true);
         }
         if (selId < 1 || selId >= structure.size() - 3) {
             down.setEnabled(false);
         } else {
             down.setEnabled(true);
         }
         if (selId >0&& selId < structure.size() - 2) {
             remove.setEnabled(true);
         } else {
             remove.setEnabled(false);
         }
     }
 
     /**
      * Select crs.
      */
     protected void selectCRS() {
         CoordinateReferenceSystem result = ActionUtil.getInstance().runTaskWithResult(new RunnableWithResult<CoordinateReferenceSystem>() {
 
             private CoordinateReferenceSystem result;
 
             @Override
             public CoordinateReferenceSystem getValue() {
                 return result;
             }
 
             @Override
             public void run() {
                 result = null;
                 CommonCRSPreferencePage page = new CommonCRSPreferencePage();
                 page.setSelectedCRS(getSelectedCRS());
                 page.setTitle("Select Coordinate Reference System");
                 page.setSubTitle("Select the coordinate reference system from the list of commonly used CRS's, or add a new one with the Add button");
                 page.init(PlatformUI.getWorkbench());
                 PreferenceManager mgr = new PreferenceManager();
                 IPreferenceNode node = new PreferenceNode("1", page); //$NON-NLS-1$
                 mgr.addToRoot(node);
                 Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
                 PreferenceDialog pdialog = new PreferenceDialog(shell, mgr);;
                 if (pdialog.open() == PreferenceDialog.OK) {
                     page.performOk();
                     result = page.getCRS();
                 }
 
             }
 
         });
 
         setSelectedCRS(result);
     }
 
     /**
      * Sets the selected crs.
      * 
      * @param result the new selected crs
      */
     private void setSelectedCRS(CoordinateReferenceSystem result) {
         if (result == null) {
             return;
         }
         selectedCRS = result;
         updateButtonLabel();
     }
 
     /**
      * Update button label.
      */
     private void updateButtonLabel() {
         CoordinateReferenceSystem crs = getSelectedCRS();
         selectCRS.setText(String.format("CRS: %s", crs.getName().toString()));
     }
 
     /**
      * Gets the selected crs.
      * 
      * @return the selected crs
      */
     public CoordinateReferenceSystem getSelectedCRS() {
         return selectedCRS == null ? getDefaultCRS() : selectedCRS;
     }
 
     /**
      * Gets the default crs.
      * 
      * @return the default crs
      */
     private CoordinateReferenceSystem getDefaultCRS() {
         try {
             return CRS.decode("EPSG:4326");
         } catch (NoSuchAuthorityCodeException e) {
             // TODO Handle NoSuchAuthorityCodeException
             throw (RuntimeException)new RuntimeException().initCause(e);
         }
     }
 
     /**
      * Update network name.
      * 
      * @param e the e
      */
     protected void updateNetworkName(ModifyEvent e) {
         networkName = network.getText().trim();
         validate();
     }
 
     /**
      * Validate.
      */
     private void validate() {
         if (StringUtils.isEmpty(networkName)) {
             setMessage("Please enter name of new network.", DialogPage.ERROR);
             setPageComplete(false);
             return;
         }else if (restrictedNames.contains(networkName)){
             setMessage("Network/dataset with same name already exist in project.", DialogPage.ERROR);
             setPageComplete(false);
             return;            
         }
         setMessage("");
         setPageComplete(true);
     }
 
     /**
      * Inits the.
      */
     private void init() {
         structure.clear();
         structure.add(NodeTypes.NETWORK);
         structure.add(NodeTypes.SITE);
         structure.add(NodeTypes.SECTOR);
         addPage(NodeTypes.SITE);
         addPage(NodeTypes.SECTOR);
         updateList();
         setPageComplete(false);
         restrictedNames = new HashSet<String>();
         DatasetService datasetService = NeoServiceFactory.getInstance().getDatasetService();
         Traverser restrictedNamesTr = datasetService.getRoots(getProjectName());
         for (Node root : restrictedNamesTr.nodes()) {
             restrictedNames.add(datasetService.getNodeName(root));
         }
 
     }
 
     /**
      * Adds the page.
      * 
      * @param type the type
      */
     private void addPage(INodeType type) {
         ((CreateNetworkWizard)getWizard()).createPage(type);
         if (getContainer().getCurrentPage() != null) {
             getContainer().updateButtons();
         }
     }
 
     /**
      * Removes the page.
      * 
      * @param type the type
      */
     private void removePage(INodeType type) {
         ((CreateNetworkWizard)getWizard()).removePage(type);
         getContainer().updateButtons();
     }
 
     /**
      * Update list.
      */
     private void updateList() {
         String[] structurStr = new String[structure.size()];
         for (int i = 0; i < structurStr.length; i++) {
             structurStr[i] = structure.get(i).getId();
         }
         structureList.setItems(structurStr);
     }
 
     /**
      * Gets the project name.
      * 
      * @return the project name
      */
     private String getProjectName() {
         return LoaderUiUtils.getAweProjectName();
     }
 
 
     /**
      * Gets the network name.
      *
      * @return the network name
      */
     public String getNetworkName() {
         return networkName;
     }
 
 }
