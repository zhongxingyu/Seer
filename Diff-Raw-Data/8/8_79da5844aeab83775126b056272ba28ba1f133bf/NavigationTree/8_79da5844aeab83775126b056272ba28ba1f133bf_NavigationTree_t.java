 package edu.mayo.phenoportal.client.navigation;
 
import com.smartgwt.client.data.Record;
 import com.smartgwt.client.types.SelectionStyle;
 import com.smartgwt.client.widgets.grid.CellFormatter;
 import com.smartgwt.client.widgets.grid.ListGridRecord;
 import com.smartgwt.client.widgets.tree.TreeGrid;
 import com.smartgwt.client.widgets.tree.TreeGridField;
 import com.smartgwt.client.widgets.tree.events.FolderOpenedEvent;
 import com.smartgwt.client.widgets.tree.events.FolderOpenedHandler;
 
 import edu.mayo.phenoportal.client.datasource.PhenotypeXmlDS;
 
 /**
  * Navigation tree to display phenotype categories and algorithms.
  */
 public class NavigationTree extends TreeGrid {
 
     PhenotypeXmlDS i_dataSource;
 
     public NavigationTree(PhenotypeXmlDS ds) {
         super();
         i_dataSource = ds;
 
         init();
     }
 
     private void init() {
 
         setCanEdit(false);
         // setLoadDataOnDemand(true);
 
         setWidth100();
         setHeight100();
         setDataSource(i_dataSource);
 
         // Set attribute to not show this warning
         setAttribute("reportCollisions", false, false);
 
         // Hide the column header "Name" for the
         setShowHeader(false);
         setLeaveScrollbarGap(false);
         setCanAcceptDroppedRecords(false);
         setCanReparentNodes(false);
         setSelectionType(SelectionStyle.SINGLE);
 
         // set tree animation
         setAnimateFolders(true);
         // setAnimateFolderSpeed(200);
 
         // set the CSS style
         setBaseStyle("htpTree");
 
         // Set the folder icons for opened and closed. They are in the
         // war/images directory.
         setShowOpenIcons(true);
         setFolderIcon("folder.png");
         setOpenIconSuffix("opened");
         setClosedIconSuffix("closed");
 
         // Set the message to display while the tree is loading.
         setLoadingDataMessage("Loading Phenotype Categories...");
         setEmptyMessage("");
 
         setAutoFetchData(true);
 
         // set the initial criteria
         // Criteria initialCriteria = new Criteria("parentId", "0");
         // setInitialCriteria(initialCriteria);
 
         TreeGridField phenoNameField = new TreeGridField("Name");
         phenoNameField.setCellFormatter(new CellFormatter() {
 
             @Override
             /**
              * Format the string if it is a folder with algorithms below it or
              * if this is a leaf node (an algorithm).
              */
             public String format(Object value, ListGridRecord record, int rowNum, int colNum) {
 
                 String retValue = "";
 
                 if (value != null) {
 
                     // If the "Count" attribute exists and is > 0, then
                     // decorate this tree node
 
                     int count = convertToInt(record.getAttribute("Count"));
 
                     if (count > 0) {
                         retValue = "<font color=\"#000000\"> <b>" + value + "  (" + count
                                 + ") </b> </font>";
                     } else {
                         retValue = value.toString();
                     }
 
                     // get the level attribute. If the level is 4, then this is
                     // a leaf node - an algorithm. Decorate it.
                     int level = convertToInt(record.getAttribute("Level"));
                     if (level == 4) {
                         retValue = "<i>" + value + "</i>";
 
                         // set the icon differently
                         setCustomNodeIcon(record, "algorithm.png");
                     }
                 }
 
                 return retValue;
             }
         });
 
         setFields(phenoNameField);
 
         addFolderOpenedHandler(new FolderOpenedHandler() {
 
             @Override
             public void onFolderOpened(FolderOpenedEvent event) {
 
                 String categoryId = event.getNode().getAttribute("CategoryId");
                 i_dataSource.setCategoryId(categoryId);
 
                 // Criteria criteria = new Criteria("parentId", categoryId);
                 // fetch the data passing this criteria
                 // fetchData(criteria);
 
             }
         });
     }
 
     /**
      * Remove the data from the current tree and fetch the children of the root.
      */
     public void refreshTree() {
 
         // ListGridRecord[] records = getRecords();
         //
         // // remove the current records
         // for (int i = 0; i < records.length; i++) {
         // removeData(records[i]);
         // }
         //
         // for (Record record : i_dataSource.getCacheData()) {
         // i_dataSource.removeData(record);
         // }
         //
         // invalidateCache();
         // invalidateRecordComponents();
 
         // NOTE: This call outputs the following warning: A node with this ID is
         // already present in this Tree - that node will be replaced. Note that
         // this warning may be disabled by setting the reportCollisions
         // attribute to false.
         // This doesn't seem to affect the tree.
 
        // clear out the current records
        // setData(new ListGridRecord[0]);
        i_dataSource.setCacheData(new Record[0]);

         i_dataSource.setCategoryId("0");
         i_dataSource.executeFetch(null);
     }
 
     /**
      * Convert a String to an int. If the conversion fails, then return 0.
      * 
      * @param value
      * @return
      */
     public int convertToInt(String value) {
         int intVal = 0;
         try {
             intVal = Integer.parseInt(value);
         } catch (NumberFormatException nfe) {
             // nothing to do
         }
 
         return intVal;
 
     }
 
 }
