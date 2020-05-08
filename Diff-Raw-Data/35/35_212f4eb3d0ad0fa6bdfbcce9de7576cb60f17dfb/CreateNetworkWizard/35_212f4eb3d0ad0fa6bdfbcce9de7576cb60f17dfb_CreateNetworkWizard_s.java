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
 
 import java.net.MalformedURLException;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.amanzi.neo.core.NeoCorePlugin;
 import org.amanzi.neo.core.utils.EditPropertiesPage;
 import org.amanzi.neo.core.utils.EditPropertiesPage.PropertyWrapper;
 import org.amanzi.neo.loader.AbstractLoader;
 import org.amanzi.neo.loader.ui.utils.LoaderUiUtils;
 import org.amanzi.neo.services.DatasetService;
 import org.amanzi.neo.services.GisProperties;
 import org.amanzi.neo.services.NeoServiceFactory;
 import org.amanzi.neo.services.enums.INodeType;
 import org.amanzi.neo.services.enums.NodeTypes;
import org.amanzi.neo.services.events.UpdateDatabaseEvent;
 import org.amanzi.neo.services.events.UpdateDrillDownEvent;
 import org.amanzi.neo.services.events.UpdateViewEventType;
 import org.amanzi.neo.services.statistic.IStatistic;
 import org.amanzi.neo.services.statistic.StatisticManager;
 import org.amanzi.neo.services.ui.NeoServiceProviderUi;
 import org.amanzi.neo.services.ui.utils.ActionUtil;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.core.runtime.jobs.Job;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.wizard.IWizardPage;
 import org.eclipse.jface.wizard.Wizard;
 import org.eclipse.ui.INewWizard;
 import org.eclipse.ui.IWorkbench;
 import org.neo4j.graphdb.Node;
 
 /**
  * TODO Purpose of
  * <p>
  * </p>
  * 
  * @author tsinkel_a
  * @since 1.0.0
  */
 public class CreateNetworkWizard extends Wizard implements INewWizard {
 
     private IWorkbench workbench;
     private IStructuredSelection selection;
     private Map<INodeType, EditPropertiesPage> pages = new HashMap<INodeType, EditPropertiesPage>();
     private CreateNetworkMainPage mainPage;
 
     @Override
     public void init(IWorkbench workbench, IStructuredSelection selection) {
         this.workbench = workbench;
         this.selection = selection;
         setWindowTitle("Create network");
         setForcePreviousAndNextButtons(true);
         pages.clear();
         initPages();
     }
 
     @Override
     public IWizardPage getPreviousPage(IWizardPage page) {
          List<INodeType> struct = mainPage.getStructure();
         if (page instanceof CreateNetworkMainPage) {
             return null;
         } else if (page instanceof EditPropertiesPage) {
             INodeType type = ((EditPropertiesPage)page).getType();
             for (int i = 2; i < struct.size(); i++) {
                 if (struct.get(i).equals(type)) {
                     return pages.get(struct.get(i-1));
                 }
             }
         }
         return null;
     }
 
     @Override
     public IWizardPage getNextPage(IWizardPage page) {
         List<INodeType> struct = mainPage.getStructure();
         if (page instanceof CreateNetworkMainPage) {
             return pages.get(struct.get(1));
         } else if (page instanceof EditPropertiesPage) {
             INodeType type = ((EditPropertiesPage)page).getType();
             for (int i = 1; i < struct.size() - 1; i++) {
                 if (struct.get(i).equals(type)) {
                     return pages.get(struct.get(i+1));
                 }
             }
         }
         return null;
     }
 
     private void initPages() {
         mainPage = new CreateNetworkMainPage("mainPage");
         addPage(mainPage);
     }
 
     @Override
     public boolean canFinish() {
         if (super.canFinish()) {
             for (IWizardPage page : pages.values()) {
                 if (!page.isPageComplete()) {
                     return false;
                 }
             }
             return true;
         }
         return false;
     }
 
     @Override
     public boolean performFinish() {
         NeoServiceProviderUi.getProvider().commit();
         Job job=new Job("finish") {
             
             @Override
             protected IStatus run(IProgressMonitor monitor) {
                 DatasetService service = NeoServiceFactory.getInstance().getDatasetService();
                 String networkName=mainPage.getNetworkName();
                 List<INodeType> structure = mainPage.getStructure();
                 String projectName=LoaderUiUtils.getAweProjectName();
                 final Node root = service.getRootNode(projectName, networkName, NodeTypes.NETWORK);
                 GisProperties gis=service.getGisNode(root);
                 gis.setCrs(mainPage.getSelectedCRS());
                 service.saveGis(gis);
                 service.setStructure(root,structure);
                 IStatistic statistics = StatisticManager.getStatistic(root);
                 for (INodeType type:structure){
                     if (!(type instanceof NodeTypes)){
                         service.saveDynamicNodeType(type.getId());
                     }
                     EditPropertiesPage page = pages.get(type);
                     if (page==null){
                         continue;
                     }
                     List<PropertyWrapper> propList = page.getProperties();
                     for (PropertyWrapper property:propList){
                         statistics.registerProperty(networkName,type.getId(),property.getName(),property.getType(),property.getDefValue());  
                     }
                     
                 }
                 statistics.save();
                 try {
                     AbstractLoader.addDataToCatalog();
                 } catch (MalformedURLException e) {
                     e.printStackTrace();
                 }
                 AbstractLoader.sendUpdateEvent(UpdateViewEventType.GIS);
                 ActionUtil.getInstance().runTask(new Runnable() {
                     
                     @Override
                     public void run() {
                         NeoCorePlugin.getDefault().getUpdateViewManager().fireUpdateView(new UpdateDrillDownEvent(root, "org.amanzi.neo.wizards.CreateNetworkWizard"));
                     }
                 }, true);
                 return Status.OK_STATUS;
             }
         };
         job.schedule();
         return true;
     }
 
     /**
      * @param type
      */
     public void createPage(INodeType type) {
         if (pages.containsKey(type)) {
             return;
         }
         EditPropertiesPage page = new EditPropertiesPage(type.getId(),String.format("Configure type '%s'", type.getId()), type);
         page.initProperty();
         pages.put(type, page);
         page.setWizard(this);
     }
 
     public void removePage(INodeType type) {
         IWizardPage page = pages.remove(type);
         if (page != null) {
             page.dispose();
         }
     }
 
 }
