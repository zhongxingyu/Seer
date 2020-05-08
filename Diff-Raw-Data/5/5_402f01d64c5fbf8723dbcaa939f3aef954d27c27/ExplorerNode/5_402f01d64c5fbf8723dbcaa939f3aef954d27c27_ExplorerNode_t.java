 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package jp.atr.dni.bmi.desktop.explorereditor;
 
 import com.sun.corba.se.impl.orbutil.graph.NodeData;
 import java.awt.event.ActionEvent;
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import javax.swing.AbstractAction;
 import javax.swing.Action;
 import javax.swing.JFileChooser;
 import javax.swing.JMenu;
 import javax.swing.JMenuItem;
 import javax.swing.JOptionPane;
 import jp.atr.dni.bmi.desktop.model.FileType;
 import jp.atr.dni.bmi.desktop.model.GeneralFileInfo;
 import jp.atr.dni.bmi.desktop.neuroshareutils.AnalogInfo;
 import jp.atr.dni.bmi.desktop.neuroshareutils.Entity;
 import jp.atr.dni.bmi.desktop.neuroshareutils.EntityInfo;
 import jp.atr.dni.bmi.desktop.neuroshareutils.EventInfo;
 import jp.atr.dni.bmi.desktop.neuroshareutils.FileInfo;
 import jp.atr.dni.bmi.desktop.neuroshareutils.NSReader;
 import jp.atr.dni.bmi.desktop.neuroshareutils.NeuralInfo;
 import jp.atr.dni.bmi.desktop.neuroshareutils.NeuroshareFile;
 import jp.atr.dni.bmi.desktop.neuroshareutils.NsnFileModelConverter;
 import jp.atr.dni.bmi.desktop.neuroshareutils.SegmentInfo;
 import jp.atr.dni.bmi.desktop.neuroshareutils.SegmentSourceInfo;
 import org.apache.commons.io.FileUtils;
 import org.openide.ErrorManager;
 import org.openide.nodes.AbstractNode;
 import org.openide.nodes.PropertySupport;
 import org.openide.nodes.Sheet;
 import org.openide.nodes.Sheet.Set;
 import org.openide.util.Exceptions;
 import org.openide.util.actions.Presenter;
 import org.openide.util.lookup.Lookups;
 
 /**
  *
  * @author kharada
  * @version 2011/01/24
  */
 public class ExplorerNode extends AbstractNode {
 
    /**
     *
     * @param obj
     */
    public ExplorerNode(GeneralFileInfo obj) {
       super(new ExplorerChildren(obj), Lookups.singleton(obj));
       setDisplayName(obj.getFileName());
 
       String iconPath = "jp/atr/dni/bmi/desktop/explorereditor/docu_memo16.png";
       FileType nodeType = obj.getFileType();
       if (nodeType == FileType.DIRECTORY) {
          iconPath = "jp/atr/dni/bmi/desktop/explorereditor/folder_blu_n16.png";
       } else if (nodeType == FileType.NSN) {
          iconPath = "jp/atr/dni/bmi/desktop/explorereditor/docu_plain16.png";
       }
       setIconBaseWithExtension(iconPath);
 
    }
 
    /**
     *
     */
    public ExplorerNode() {
       super(new ExplorerChildren());
       setDisplayName("Root of Data");
       setIconBaseWithExtension("jp/atr/dni/bmi/desktop/explorereditor/monitor16.png");
    }
 
    /**
     *
     * @return
     */
    @Override
    public String getHtmlDisplayName() {
       GeneralFileInfo obj = getLookup().lookup(GeneralFileInfo.class);
       if (obj != null) {
          return "<font color='!textText'>" + obj.getFileName() + "</font>" + "<font color='!controlShadow'><i>" + obj.getModifiedTimeString() + "</i></font>";
       } else {
          return null;
       }
    }
 
    /**
     *
     * @param popup
     * @return
     */
    @Override
    public Action[] getActions(boolean popup) {
 
       return new Action[]{
                  new SaveAction(), // Save as -> Neuroshare
                  null, //Line.
                  new ReloadAction(), // Reload
                  null, //Line.
                  new AddAction(), // Add
                  null, //Line.
                  new DeleteAction(), // Delete
                  null, //Line.
                  new CopyToWorkspaceAction() // Copy To Workspace
               };
    }
 
    /**
     *
     * @return
     */
    @Override
    protected Sheet createSheet() {
       Sheet sheet = Sheet.createDefault();
 
       Sheet.Set propertyGrp = Sheet.createPropertiesSet();
       propertyGrp.setDisplayName("General");
       propertyGrp.setName("properties"); // Do Not Change! - why?
 
       GeneralFileInfo obj = getLookup().lookup(GeneralFileInfo.class);
 
       // Case : user selects root.
       if (obj == null) {
          return sheet;
       }
 
       //  Get fileType to switch
       FileType fileType = obj.getFileType();
 
       //fileType exists
       // Directory
       // File/nsn
       // File/txt pdf ........
 
       // node is Directory or File
       if (fileType != FileType.UNKNOWN) {
 
          // Set Default Properties.
          propertyGrp = setDefaultProperties(propertyGrp, obj);
          if (propertyGrp != null) {
             sheet.put(propertyGrp);
          }
 
          if (fileType == FileType.NSN) {
             ArrayList<Sheet.Set> neurosharePropertyGrp = setNeuroshareProperties(obj);
             if (neurosharePropertyGrp != null) {
                for (int ii = 0; ii < neurosharePropertyGrp.size(); ii++) {
                   sheet.put(neurosharePropertyGrp.get(ii));
                }
             }
          }
       }
       return sheet;
    }
 
    private Set setDefaultProperties(Set propertyGrp, GeneralFileInfo obj) {
 
       try {
 
          // Set property value. with using obj's get*** method.
          PropertySupport.Reflection nameProp = new PropertySupport.Reflection(obj, String.class, "getFileName", null);
          PropertySupport.Reflection pathProp = new PropertySupport.Reflection(obj, String.class, "getFilePath", null);
          PropertySupport.Reflection sizeProp = new PropertySupport.Reflection(obj, Long.class, "getFileSize", null);
         PropertySupport.Reflection typeProp = new PropertySupport.Reflection(obj, FileType.class, "getFileType", null);
          PropertySupport.Reflection timeProp = new PropertySupport.Reflection(obj, String.class, "getModifiedTimeString", null);
 
          // Set display name.
          nameProp.setName("Name");
          pathProp.setName("Full Path");
          sizeProp.setName("Size");
          typeProp.setName("Type");
          timeProp.setName("Last Modified");
 
          // Set description.
          nameProp.setShortDescription("file name.");
          pathProp.setShortDescription("file full path.");
          sizeProp.setShortDescription("file size [Byte].");
          typeProp.setShortDescription("file type.");
          timeProp.setShortDescription("last modified time.");
 
          // Set propties to the "General" group.
          propertyGrp.put(nameProp);
          propertyGrp.put(pathProp);
          propertyGrp.put(sizeProp);
          propertyGrp.put(typeProp);
          propertyGrp.put(timeProp);
 
       } catch (IllegalArgumentException ex) {
          Exceptions.printStackTrace(ex);
       } catch (NoSuchMethodException ex) {
          ErrorManager.getDefault();
       }
 
       return propertyGrp;
 
    }
 
    private Set setNeuroshareHeaderProperties(GeneralFileInfo obj) {
 
       Sheet.Set neuroshareGrp = Sheet.createPropertiesSet();
 
       try {
 
          // Create Sheet.
          neuroshareGrp.setDisplayName("General");
          neuroshareGrp.setName("neuroshare");
          neuroshareGrp.setValue("tabName", "Neuroshare");
 
          // Get Neuroshare Data using NSReader.
          NSReader reader = new NSReader();
          NeuroshareFile nsn = reader.readNSFileOnlyInfo(obj.getFilePath());
          obj.setNsObj(nsn);
 
          FileInfo fi = nsn.getFileInfo();
 
          // Set property value. with using obj's get*** method.
         PropertySupport.Reflection fileTypeProp = new PropertySupport.Reflection(fi, FileType.class, "fileType");
          PropertySupport.Reflection entityCountProp = new PropertySupport.Reflection(fi, Long.class, "getEntityCount", null);
          PropertySupport.Reflection timeStampResProp = new PropertySupport.Reflection(fi, double.class, "timeStampRes");
          PropertySupport.Reflection timeSpanProp = new PropertySupport.Reflection(fi, double.class, "timeSpan");
          PropertySupport.Reflection appNameProp = new PropertySupport.Reflection(fi, String.class, "appName");
          PropertySupport.Reflection yearProp = new PropertySupport.Reflection(fi, long.class, "year");
          PropertySupport.Reflection monthProp = new PropertySupport.Reflection(fi, long.class, "month");
          PropertySupport.Reflection dayProp = new PropertySupport.Reflection(fi, long.class, "dayOfMonth");
          PropertySupport.Reflection hourProp = new PropertySupport.Reflection(fi, long.class, "hourOfDay");
          PropertySupport.Reflection minProp = new PropertySupport.Reflection(fi, long.class, "minOfDay");
          PropertySupport.Reflection secProp = new PropertySupport.Reflection(fi, long.class, "secOfDay");
          PropertySupport.Reflection milliSecProp = new PropertySupport.Reflection(fi, long.class, "milliSecOfDay");
          PropertySupport.Reflection commentsProp = new PropertySupport.Reflection(fi, String.class, "comments");
 
          // Set display name.
          fileTypeProp.setName("File Type"); // need to be identified.
          entityCountProp.setName("Entity Count"); // need to be identified.
          timeStampResProp.setName("Time Stamp Resolution"); // need to be identified.
          timeSpanProp.setName("Time Span"); // need to be identified.
          appNameProp.setName("Application Name"); // need to be identified.
          yearProp.setName("Year"); // need to be identified.
          monthProp.setName("Month"); // need to be identified.
          dayProp.setName("Day"); // need to be identified.
          hourProp.setName("Hour"); // need to be identified.
          minProp.setName("Min"); // need to be identified.
          secProp.setName("Sec"); // need to be identified.
          milliSecProp.setName("MilliSec"); // need to be identified.
          commentsProp.setName("Comments"); // need to be identified.
 
          // Set description.
          fileTypeProp.setShortDescription("file type. <i><B>ns_FileInfo.szFileType.</B></i>");
          entityCountProp.setShortDescription("entity count. <i><B>ns_FileInfo.dwEntityCount.</B></i> <font color='red'><B>Unable to modify value.</B></font>");
          timeStampResProp.setShortDescription("time stamp resolution. <i><B>ns_FileInfo.dTimeStampResolution.</B></i>");
          timeSpanProp.setShortDescription("time span. <i><B>ns_FileInfo.dTimeSpan.</B></i>");
          appNameProp.setShortDescription("application name. <i><B>ns_FileInfo.szAppName.</B></i>");
          yearProp.setShortDescription("year. <i><B>ns_FileInfo.dwTime_Year.</B></i>");
          monthProp.setShortDescription("month. <i><B>ns_FileInfo.dwTime_Month.</B></i>");
          dayProp.setShortDescription("day of month. <i><B>ns_FileInfo.dwTime_Day.</B></i>");
          hourProp.setShortDescription("hour. <i><B>ns_FileInfo.dwTime_Hour.</B></i>");
          minProp.setShortDescription("minite. <i><B>ns_FileInfo.dwTime_Min.</B></i>");
          secProp.setShortDescription("seconds. <i><B>ns_FileInfo.dwTime_Sec.</B></i>");
          milliSecProp.setShortDescription("milliseconds. <i><B>ns_FileInfo.dwTime_MilliSec.</B></i>");
          commentsProp.setShortDescription("comments. <i><B>ns_FileInfo.szFileComment.</B></i>");
 
          // Set propties to the "Neuroshare" group.
          neuroshareGrp.put(fileTypeProp);
          neuroshareGrp.put(entityCountProp);
          neuroshareGrp.put(timeStampResProp);
          neuroshareGrp.put(timeSpanProp);
          neuroshareGrp.put(appNameProp);
          neuroshareGrp.put(yearProp);
          neuroshareGrp.put(monthProp);
          neuroshareGrp.put(dayProp);
          neuroshareGrp.put(hourProp);
          neuroshareGrp.put(minProp);
          neuroshareGrp.put(secProp);
          neuroshareGrp.put(milliSecProp);
          neuroshareGrp.put(commentsProp);
 
       } catch (IllegalArgumentException ex) {
          Exceptions.printStackTrace(ex);
       } catch (NoSuchMethodException ex) {
          ErrorManager.getDefault();
       }
 
       return neuroshareGrp;
    }
 
    private Set setNeuroshareEntityProperties(NodeData obj, Entity entity) {
 
       // Create Sheet.
       Sheet.Set neuroshareGrp = Sheet.createPropertiesSet();
 
       try {
 
          neuroshareGrp.setDisplayName("Entity Information");
          neuroshareGrp.setName("neuroshareEntity");
          neuroshareGrp.setValue("tabName", "Neuroshare");
 
          // Set property value. with using obj's get*** method.
          PropertySupport.Reflection entityLabelProp = new PropertySupport.Reflection(entity, String.class, "entityLabel");
          PropertySupport.Reflection entityTypeProp = new PropertySupport.Reflection(entity, Long.class, "getEntityType", null);
          PropertySupport.Reflection itemCountProp = new PropertySupport.Reflection(entity, Long.class, "getItemCount", null);
 
          // Set display name.
          entityLabelProp.setName("Label"); // need to be identified.
          entityTypeProp.setName("Type"); // need to be identified.
          itemCountProp.setName("Item Count"); // need to be identified.
 
          // Set description.
          entityLabelProp.setShortDescription("label of the entity. <i><B>ns_EntityInfo.szEntityLabel.</B></i>");
          entityTypeProp.setShortDescription("type of the entity. <i><B>ns_EntityInfo.dwEntityType.</B></i> <font color='red'><B>Unable to modify value.</B></font>");
          itemCountProp.setShortDescription("item counts of the entity. <i><B>ns_EntityInfo.dwItemCount.</B></i> <font color='red'><B>Unable to modify value.</B></font>");
 
          // Set propties to the "neuroshareEntity" group.
          neuroshareGrp.put(entityLabelProp);
          neuroshareGrp.put(entityTypeProp);
          neuroshareGrp.put(itemCountProp);
 
          switch (entity.getTag().getElemType()) {
             case ENTITY_EVENT:
                EventInfo eventInfo = (EventInfo) entity;
                PropertySupport.Reflection ei_EventTypeProp = new PropertySupport.Reflection(eventInfo, Long.class, "getEventType", null);
                PropertySupport.Reflection ei_CSVDescProp = new PropertySupport.Reflection(eventInfo, String.class, "csvDesc");
                ei_EventTypeProp.setName("Event Type"); // need to be identified.
                ei_CSVDescProp.setName("CSV Desc"); // need to be identified.
                ei_EventTypeProp.setShortDescription("type of the event. <i><B>ns_EventInfo.dwEventType.</B></i> <font color='red'><B>Unable to modify value.</B></font>");
                ei_CSVDescProp.setShortDescription("description of the event. <i><B>ns_EventInfo.szCSVDesc.</B></i>");
                neuroshareGrp.put(ei_EventTypeProp);
                neuroshareGrp.put(ei_CSVDescProp);
                break;
             case ENTITY_ANALOG:
                AnalogInfo analogInfo = (AnalogInfo) entity;
                PropertySupport.Reflection ai_SampleRateProp = new PropertySupport.Reflection(analogInfo, double.class, "sampleRate");
                PropertySupport.Reflection ai_MinValProp = new PropertySupport.Reflection(analogInfo, double.class, "minVal");
                PropertySupport.Reflection ai_MaxValProp = new PropertySupport.Reflection(analogInfo, double.class, "maxVal");
                PropertySupport.Reflection ai_UnitsProp = new PropertySupport.Reflection(analogInfo, String.class, "units");
                PropertySupport.Reflection ai_ResolutionProp = new PropertySupport.Reflection(analogInfo, double.class, "resolution");
                PropertySupport.Reflection ai_LocationXProp = new PropertySupport.Reflection(analogInfo, double.class, "locationX");
                PropertySupport.Reflection ai_LocationYProp = new PropertySupport.Reflection(analogInfo, double.class, "locationY");
                PropertySupport.Reflection ai_LocationZProp = new PropertySupport.Reflection(analogInfo, double.class, "locationZ");
                PropertySupport.Reflection ai_LocationUserProp = new PropertySupport.Reflection(analogInfo, double.class, "locationUser");
                PropertySupport.Reflection ai_HighFreqCornerProp = new PropertySupport.Reflection(analogInfo, double.class, "highFreqCorner");
                PropertySupport.Reflection ai_HighFreqOrderProp = new PropertySupport.Reflection(analogInfo, long.class, "highFreqOrder");
                PropertySupport.Reflection ai_HighFilterTypeProp = new PropertySupport.Reflection(analogInfo, String.class, "highFilterType");
                PropertySupport.Reflection ai_LowFreqCornerProp = new PropertySupport.Reflection(analogInfo, double.class, "lowFreqCorner");
                PropertySupport.Reflection ai_LowFreqOrderProp = new PropertySupport.Reflection(analogInfo, long.class, "lowFreqOrder");
                PropertySupport.Reflection ai_LowFilterTypeProp = new PropertySupport.Reflection(analogInfo, String.class, "lowFilterType");
                PropertySupport.Reflection ai_ProbeInfoProp = new PropertySupport.Reflection(analogInfo, String.class, "probeInfo");
                ai_SampleRateProp.setName("Sample Rate"); // need to be identified.
                ai_MinValProp.setName("Min Val"); // need to be identified.
                ai_MaxValProp.setName("Max Val"); // need to be identified.
                ai_UnitsProp.setName("Unit"); // need to be identified.
                ai_ResolutionProp.setName("Resolution"); // need to be identified.
                ai_LocationXProp.setName("Location X"); // need to be identified.
                ai_LocationYProp.setName("Location Y"); // need to be identified.
                ai_LocationZProp.setName("Location Z"); // need to be identified.
                ai_LocationUserProp.setName("Location User"); // need to be identified.
                ai_HighFreqCornerProp.setName("H Freq Corner"); // need to be identified.
                ai_HighFreqOrderProp.setName("H Freq Order"); // need to be identified.
                ai_HighFilterTypeProp.setName("H Filter Type"); // need to be identified.
                ai_LowFreqCornerProp.setName("L Freq Corner"); // need to be identified.
                ai_LowFreqOrderProp.setName("L Freq Order"); // need to be identified.
                ai_LowFilterTypeProp.setName("L Filter Type"); // need to be identified.
                ai_ProbeInfoProp.setName("ProbeInfo"); // need to be identified.
                ai_SampleRateProp.setShortDescription("sampling rate. [Hz] <i><B>ns_AnalogInfo.dSampleRate.</B></i>");
                ai_MinValProp.setShortDescription("minimum value. <i><B>ns_AnalogInfo.dMinVal.</B></i>");
                ai_MaxValProp.setShortDescription("maximum value. <i><B>ns_AnalogInfo.dMaxVal.</B></i>");
                ai_UnitsProp.setShortDescription("unit. <i><B>ns_AnalogInfo.szUnits.</B></i>");
                ai_ResolutionProp.setShortDescription("resolution. <i><B>ns_AnalogInfo.dResolution.</B></i>");
                ai_LocationXProp.setShortDescription("x position. <i><B>ns_AnalogInfo.dLocationX.</B></i>");
                ai_LocationYProp.setShortDescription("y position. <i><B>ns_AnalogInfo.dLocationY.</B></i>");
                ai_LocationZProp.setShortDescription("z position. <i><B>ns_AnalogInfo.dLocationZ.</B></i>");
                ai_LocationUserProp.setShortDescription("additional information about position. <i><B>ns_AnalogInfo.dLocationUser.</B></i>");
                ai_HighFreqCornerProp.setShortDescription("high frequency cutoff. [Hz] <i><B>ns_AnalogInfo.dHighFreqCorner.</B></i>");
                ai_HighFreqOrderProp.setShortDescription("order of the filter used for high frequency cutoff. <i><B>ns_AnalogInfo.dwHighFreqOrder.</B></i>");
                ai_HighFilterTypeProp.setShortDescription("type of the filter used for high frequency cutoff. <i><B>ns_AnalogInfo.szHighFilterType.</B></i>");
                ai_LowFreqCornerProp.setShortDescription("low frequency cutoff. [Hz] <i><B>ns_AnalogInfo.dLowFilterType.</B></i>");
                ai_LowFreqOrderProp.setShortDescription("order of the filter used for low frequency cutoff. <i><B>ns_AnalogInfo.dwLowFreqOrder.</B></i>");
                ai_LowFilterTypeProp.setShortDescription("type of the filter used for low frequency cutoff. <i><B>ns_AnalogInfo.szLowFilterType.</B></i>");
                ai_ProbeInfoProp.setShortDescription("probe information. <i><B>ns_AnalogInfo.szProbeInfo.</B></i>");
                neuroshareGrp.put(ai_SampleRateProp);
                neuroshareGrp.put(ai_MinValProp);
                neuroshareGrp.put(ai_MaxValProp);
                neuroshareGrp.put(ai_UnitsProp);
                neuroshareGrp.put(ai_ResolutionProp);
                neuroshareGrp.put(ai_LocationXProp);
                neuroshareGrp.put(ai_LocationYProp);
                neuroshareGrp.put(ai_LocationZProp);
                neuroshareGrp.put(ai_LocationUserProp);
                neuroshareGrp.put(ai_HighFreqCornerProp);
                neuroshareGrp.put(ai_HighFreqOrderProp);
                neuroshareGrp.put(ai_HighFilterTypeProp);
                neuroshareGrp.put(ai_LowFreqCornerProp);
                neuroshareGrp.put(ai_LowFreqOrderProp);
                neuroshareGrp.put(ai_LowFilterTypeProp);
                neuroshareGrp.put(ai_ProbeInfoProp);
                break;
             case ENTITY_SEGMENT:
                SegmentInfo segmentInfo = (SegmentInfo) entity;
                PropertySupport.Reflection si_SourceCountProp = new PropertySupport.Reflection(segmentInfo, long.class, "getSourceCount", null);
                PropertySupport.Reflection si_MinSampleCountProp = new PropertySupport.Reflection(segmentInfo, long.class, "getMinSampleCount", null);
                PropertySupport.Reflection si_MaxSampleCountProp = new PropertySupport.Reflection(segmentInfo, long.class, "getMaxSampleCount", null);
                PropertySupport.Reflection si_SampleRateProp = new PropertySupport.Reflection(segmentInfo, double.class, "sampleRate");
                PropertySupport.Reflection si_UnitsProp = new PropertySupport.Reflection(segmentInfo, String.class, "units");
                si_SourceCountProp.setName("Source Count"); // need to be identified.
                si_MinSampleCountProp.setName("Min Sample Count"); // need to be identified.
                si_MaxSampleCountProp.setName("Max Sample Count"); // need to be identified.
                si_SampleRateProp.setName("Sample Rate"); // need to be identified.
                si_UnitsProp.setName("Unit"); // need to be identified.
                si_SourceCountProp.setShortDescription("number of sources. <i><B>ns_SegmentInfo.dwSourceCount.</B></i> <font color='red'><B>Unable to modify value.</B></font>");
                si_MinSampleCountProp.setShortDescription("minimum number of samples. <i><B>ns_SegmentInfo.dwMinSampleCount.</B></i> <font color='red'><B>Unable to modify value.</B></font>");
                si_MaxSampleCountProp.setShortDescription("maximum number of samples. <i><B>ns_SegmentInfo.dwMaxSampleCount.</B></i> <font color='red'><B>Unable to modify value.</B></font>");
                si_SampleRateProp.setShortDescription("sampling rate. [Hz] <i><B>ns_SegmentInfo.dSampleRate.</B></i>");
                si_UnitsProp.setShortDescription("unit. <i><B>ns_SegmentInfo.szUnits.</B></i>");
                neuroshareGrp.put(si_SourceCountProp);
                neuroshareGrp.put(si_MinSampleCountProp);
                neuroshareGrp.put(si_MaxSampleCountProp);
                neuroshareGrp.put(si_SampleRateProp);
                neuroshareGrp.put(si_UnitsProp);
 
                ArrayList<SegmentSourceInfo> segSourceInfos = segmentInfo.getSegSourceInfos();
                if (segSourceInfos != null) {
                   for (int jj = 0; jj < segSourceInfos.size(); jj++) {
                      SegmentSourceInfo ss = segSourceInfos.get(jj);
 
                      // Create Sheet.
                      Sheet.Set sourceGrp = Sheet.createPropertiesSet();
                      sourceGrp.setDisplayName("Source No : " + (jj + 1) + " : ProbeInfo [" + ss.getProbeInfo() + "]");
                      sourceGrp.setName("neuroshareEntity" + ":" + jj); // need to be identified.
                      sourceGrp.setValue("tabName", "Neuroshare");
 
                      PropertySupport.Reflection si_MinValProp = new PropertySupport.Reflection(ss, double.class, "minVal");
                      PropertySupport.Reflection si_MaxValProp = new PropertySupport.Reflection(ss, double.class, "maxVal");
                      PropertySupport.Reflection si_ResolutionProp = new PropertySupport.Reflection(ss, double.class, "resolution");
                      PropertySupport.Reflection si_SubSampleShiftProp = new PropertySupport.Reflection(ss, double.class, "subSampleShift");
                      PropertySupport.Reflection si_LocationXProp = new PropertySupport.Reflection(ss, double.class, "locationX");
                      PropertySupport.Reflection si_LocationYProp = new PropertySupport.Reflection(ss, double.class, "locationY");
                      PropertySupport.Reflection si_LocationZProp = new PropertySupport.Reflection(ss, double.class, "locationZ");
                      PropertySupport.Reflection si_LocationUserProp = new PropertySupport.Reflection(ss, double.class, "locationUser");
                      PropertySupport.Reflection si_HighFreqCornerProp = new PropertySupport.Reflection(ss, double.class, "highFreqCorner");
                      PropertySupport.Reflection si_HighFreqOrderProp = new PropertySupport.Reflection(ss, long.class, "highFreqOrder");
                      PropertySupport.Reflection si_HighFilterTypeProp = new PropertySupport.Reflection(ss, String.class, "highFilterType");
                      PropertySupport.Reflection si_LowFreqCornerProp = new PropertySupport.Reflection(ss, double.class, "lowFreqCorner");
                      PropertySupport.Reflection si_LowFreqOrderProp = new PropertySupport.Reflection(ss, long.class, "lowFreqOrder");
                      PropertySupport.Reflection si_LowFilterTypeProp = new PropertySupport.Reflection(ss, String.class, "lowFilterType");
                      PropertySupport.Reflection si_ProbeInfoProp = new PropertySupport.Reflection(ss, String.class, "probeInfo");
                      si_MinValProp.setName("Min Val"); // need to be identified.
                      si_MaxValProp.setName("Max Val"); // need to be identified.
                      si_ResolutionProp.setName("Resolution"); // need to be identified.
                      si_SubSampleShiftProp.setName("Sub Sample Shift"); // need to be identified.
                      si_LocationXProp.setName("Location X"); // need to be identified.
                      si_LocationYProp.setName("Location Y"); // need to be identified.
                      si_LocationZProp.setName("Location Z"); // need to be identified.
                      si_LocationUserProp.setName("Location User"); // need to be identified.
                      si_HighFreqCornerProp.setName("H Freq Corner"); // need to be identified.
                      si_HighFreqOrderProp.setName("H Freq Order"); // need to be identified.
                      si_HighFilterTypeProp.setName("H Filter Type"); // need to be identified.
                      si_LowFreqCornerProp.setName("L Freq Corner"); // need to be identified.
                      si_LowFreqOrderProp.setName("L Freq Order"); // need to be identified.
                      si_LowFilterTypeProp.setName("L Filter Type"); // need to be identified.
                      si_ProbeInfoProp.setName("ProbeInfo"); // need to be identified.
                      si_MinValProp.setShortDescription("minimum value. <i><B>ns_SegSourceInfo.dMinVal.</B></i>");
                      si_MaxValProp.setShortDescription("maximum value. <i><B>ns_SegSourceInfo.dMaxVal.</B></i>");
                      si_ResolutionProp.setShortDescription("resolution. <i><B>ns_SegSourceInfo.dResolution.</B></i>");
                      si_SubSampleShiftProp.setShortDescription("sub sample shift. <i><B>ns_SegSourceInfo.dSubSampleShift.</B></i>");
                      si_LocationXProp.setShortDescription("x position. <i><B>ns_SegSourceInfo.dLocationX.</B></i>");
                      si_LocationYProp.setShortDescription("y position. <i><B>ns_SegSourceInfo.dLocationY.</B></i>");
                      si_LocationZProp.setShortDescription("z position. <i><B>ns_SegSourceInfo.dLocationZ.</B></i>");
                      si_LocationUserProp.setShortDescription("additional information about position. <i><B>ns_SegSourceInfo.dLocationUser.</B></i>");
                      si_HighFreqCornerProp.setShortDescription("high frequency cutoff. [Hz] <i><B>ns_SegSourceInfo.dHighFreqCorner.</B></i>");
                      si_HighFreqOrderProp.setShortDescription("order of the filter used for high frequency cutoff. <i><B>ns_SegSourceInfo.dwHighFreqOrder.</B></i>");
                      si_HighFilterTypeProp.setShortDescription("type of the filter used for high frequency cutoff. <i><B>ns_SegSourceInfo.szHighFilterType.</B></i>");
                      si_LowFreqCornerProp.setShortDescription("low frequency cutoff. [Hz] <i><B>ns_SegSourceInfo.dLowFilterType.</B></i>");
                      si_LowFreqOrderProp.setShortDescription("order of the filter used for low frequency cutoff. <i><B>ns_SegSourceInfo.dwLowFreqOrder.</B></i>");
                      si_LowFilterTypeProp.setShortDescription("type of the filter used for low frequency cutoff. <i><B>ns_SegSourceInfo.szLowFilterType.</B></i>");
                      si_ProbeInfoProp.setShortDescription("probe information. <i><B>ns_SegSourceInfo.szProbeInfo.</B></i>");
                      sourceGrp.put(si_MinValProp);
                      sourceGrp.put(si_MaxValProp);
                      sourceGrp.put(si_ResolutionProp);
                      sourceGrp.put(si_SubSampleShiftProp);
                      sourceGrp.put(si_LocationXProp);
                      sourceGrp.put(si_LocationYProp);
                      sourceGrp.put(si_LocationZProp);
                      sourceGrp.put(si_LocationUserProp);
                      sourceGrp.put(si_HighFreqCornerProp);
                      sourceGrp.put(si_HighFreqOrderProp);
                      sourceGrp.put(si_HighFilterTypeProp);
                      sourceGrp.put(si_LowFreqCornerProp);
                      sourceGrp.put(si_LowFreqOrderProp);
                      sourceGrp.put(si_LowFilterTypeProp);
                      sourceGrp.put(si_ProbeInfoProp);
                   }
                }
                break;
             case ENTITY_NEURAL:
                NeuralInfo neuralInfo = (NeuralInfo) entity;
                PropertySupport.Reflection ni_SourceEntityIDProp = new PropertySupport.Reflection(neuralInfo, long.class, "sourceEntityID");
                PropertySupport.Reflection ni_SourceUnitIDProp = new PropertySupport.Reflection(neuralInfo, long.class, "sourceUnitID");
                PropertySupport.Reflection ni_ProbeInfoProp = new PropertySupport.Reflection(neuralInfo, String.class, "probeInfo");
                ni_SourceEntityIDProp.setName("Source Entity ID"); // need to be identified.
                ni_SourceUnitIDProp.setName("Source Unit ID"); // need to be identified.
                ni_ProbeInfoProp.setName("ProbeInfo"); // need to be identified.
                ni_SourceEntityIDProp.setShortDescription("ID number of the source entity. <i><B>ns_NeuralInfo.dwSourceEntityID.</B></i>");
                ni_SourceUnitIDProp.setShortDescription("sorted unit ID number of the source entity. <i><B>ns_NeuralInfo.dwSourceUnitID.</B></i>");
                ni_ProbeInfoProp.setShortDescription("probe information. <i><B>ns_NeuralInfo.szProbeInfo.</B></i>");
                neuroshareGrp.put(ni_SourceEntityIDProp);
                neuroshareGrp.put(ni_SourceUnitIDProp);
                neuroshareGrp.put(ni_ProbeInfoProp);
                break;
 
             default:
                break;
          }
       } catch (IllegalArgumentException ex) {
          Exceptions.printStackTrace(ex);
       } catch (NoSuchMethodException ex) {
          ErrorManager.getDefault();
       }
 
       return neuroshareGrp;
    }
 
    private ArrayList<Sheet.Set> setNeuroshareProperties(GeneralFileInfo obj) {
 
       ArrayList<Sheet.Set> sets = new ArrayList<Sheet.Set>();
 
       try {
 
          // Create Sheet.
          Sheet.Set neuroshareGrp = Sheet.createPropertiesSet();
          neuroshareGrp.setDisplayName("General");
          neuroshareGrp.setName("neuroshare");
          neuroshareGrp.setValue("tabName", "Neuroshare");
 
          NeuroshareFile nsn = null;
          // Read data.
          NSReader reader = new NSReader();
          nsn = reader.readNSFileOnlyInfo(obj.getFilePath());
 
          obj.setNsObj(nsn);
 
          FileInfo fi = nsn.getFileInfo();
          ArrayList<Entity> entities = nsn.getEntities();
 
          // Set property value. with using obj's get*** method.
          PropertySupport.Reflection fileTypeProp = new PropertySupport.Reflection(fi, String.class, "fileType");
          PropertySupport.Reflection entityCountProp = new PropertySupport.Reflection(fi, Long.class, "getEntityCount", null);
          PropertySupport.Reflection timeStampResProp = new PropertySupport.Reflection(fi, double.class, "timeStampRes");
          PropertySupport.Reflection timeSpanProp = new PropertySupport.Reflection(fi, double.class, "timeSpan");
          PropertySupport.Reflection appNameProp = new PropertySupport.Reflection(fi, String.class, "appName");
          PropertySupport.Reflection yearProp = new PropertySupport.Reflection(fi, long.class, "year");
          PropertySupport.Reflection monthProp = new PropertySupport.Reflection(fi, long.class, "month");
          PropertySupport.Reflection dayProp = new PropertySupport.Reflection(fi, long.class, "dayOfMonth");
          PropertySupport.Reflection hourProp = new PropertySupport.Reflection(fi, long.class, "hourOfDay");
          PropertySupport.Reflection minProp = new PropertySupport.Reflection(fi, long.class, "minOfDay");
          PropertySupport.Reflection secProp = new PropertySupport.Reflection(fi, long.class, "secOfDay");
          PropertySupport.Reflection milliSecProp = new PropertySupport.Reflection(fi, long.class, "milliSecOfDay");
          PropertySupport.Reflection commentsProp = new PropertySupport.Reflection(fi, String.class, "comments");
 
          // Set display name.
          fileTypeProp.setName("File Type"); // need to be identified.
          entityCountProp.setName("Entity Count"); // need to be identified.
          timeStampResProp.setName("Time Stamp Resolution"); // need to be identified.
          timeSpanProp.setName("Time Span"); // need to be identified.
          appNameProp.setName("Application Name"); // need to be identified.
          yearProp.setName("Year"); // need to be identified.
          monthProp.setName("Month"); // need to be identified.
          dayProp.setName("Day"); // need to be identified.
          hourProp.setName("Hour"); // need to be identified.
          minProp.setName("Min"); // need to be identified.
          secProp.setName("Sec"); // need to be identified.
          milliSecProp.setName("MilliSec"); // need to be identified.
          commentsProp.setName("Comments"); // need to be identified.
 
          // Set description.
          fileTypeProp.setShortDescription("file type. <i><B>ns_FileInfo.szFileType.</B></i>");
          entityCountProp.setShortDescription("entity count. <i><B>ns_FileInfo.dwEntityCount.</B></i> <font color='red'><B>Unable to modify value.</B></font>");
          timeStampResProp.setShortDescription("time stamp resolution. <i><B>ns_FileInfo.dTimeStampResolution.</B></i>");
          timeSpanProp.setShortDescription("time span. <i><B>ns_FileInfo.dTimeSpan.</B></i>");
          appNameProp.setShortDescription("application name. <i><B>ns_FileInfo.szAppName.</B></i>");
          yearProp.setShortDescription("year. <i><B>ns_FileInfo.dwTime_Year.</B></i>");
          monthProp.setShortDescription("month. <i><B>ns_FileInfo.dwTime_Month.</B></i>");
          dayProp.setShortDescription("day of month. <i><B>ns_FileInfo.dwTime_Day.</B></i>");
          hourProp.setShortDescription("hour. <i><B>ns_FileInfo.dwTime_Hour.</B></i>");
          minProp.setShortDescription("minite. <i><B>ns_FileInfo.dwTime_Min.</B></i>");
          secProp.setShortDescription("seconds. <i><B>ns_FileInfo.dwTime_Sec.</B></i>");
          milliSecProp.setShortDescription("milliseconds. <i><B>ns_FileInfo.dwTime_MilliSec.</B></i>");
          commentsProp.setShortDescription("comments. <i><B>ns_FileInfo.szFileComment.</B></i>");
 
          // Set propties to the "Neuroshare" group.
          neuroshareGrp.put(fileTypeProp);
          neuroshareGrp.put(entityCountProp);
          neuroshareGrp.put(timeStampResProp);
          neuroshareGrp.put(timeSpanProp);
          neuroshareGrp.put(appNameProp);
          neuroshareGrp.put(yearProp);
          neuroshareGrp.put(monthProp);
          neuroshareGrp.put(dayProp);
          neuroshareGrp.put(hourProp);
          neuroshareGrp.put(minProp);
          neuroshareGrp.put(secProp);
          neuroshareGrp.put(milliSecProp);
          neuroshareGrp.put(commentsProp);
 
          sets.add(neuroshareGrp);
 
          for (int ii = 0; ii < entities.size(); ii++) {
             Entity entity = entities.get(ii);
             EntityInfo entityInfo = entity.getEntityInfo();
 
             // Create Sheet.
             Sheet.Set entityGrp = Sheet.createPropertiesSet();
             entityGrp.setDisplayName("Entity No : " + (ii + 1) + " : Type [" + entity.getTag().getElemType() + "]");
             entityGrp.setName("neuroshareEntity" + ii); // need to be identified.
             entityGrp.setValue("tabName", "Neuroshare");
 
             // Set property value. with using obj's get*** method.
             PropertySupport.Reflection entityLabelProp = new PropertySupport.Reflection(entityInfo, String.class, "entityLabel");
             PropertySupport.Reflection entityTypeProp = new PropertySupport.Reflection(entityInfo, Long.class, "getEntityType", null);
             PropertySupport.Reflection itemCountProp = new PropertySupport.Reflection(entityInfo, Long.class, "getItemCount", null);
 
             // Set display name.
             entityLabelProp.setName("Label"); // need to be identified.
             entityTypeProp.setName("Type"); // need to be identified.
             itemCountProp.setName("Item Count"); // need to be identified.
 
             // Set description.
             entityLabelProp.setShortDescription("label of the entity. <i><B>ns_EntityInfo.szEntityLabel.</B></i>");
             entityTypeProp.setShortDescription("type of the entity. <i><B>ns_EntityInfo.dwEntityType.</B></i> <font color='red'><B>Unable to modify value.</B></font>");
             itemCountProp.setShortDescription("item counts of the entity. <i><B>ns_EntityInfo.dwItemCount.</B></i> <font color='red'><B>Unable to modify value.</B></font>");
 
             // Set propties to the "neuroshareEntity" group.
             entityGrp.put(entityLabelProp);
             entityGrp.put(entityTypeProp);
             entityGrp.put(itemCountProp);
 
             switch (entity.getTag().getElemType()) {
                case ENTITY_EVENT:
                   EventInfo eventInfo = (EventInfo) entity;
                   PropertySupport.Reflection ei_EventTypeProp = new PropertySupport.Reflection(eventInfo, Long.class, "getEventType", null);
                   PropertySupport.Reflection ei_CSVDescProp = new PropertySupport.Reflection(eventInfo, String.class, "csvDesc");
                   ei_EventTypeProp.setName("Event Type"); // need to be identified.
                   ei_CSVDescProp.setName("CSV Desc"); // need to be identified.
                   ei_EventTypeProp.setShortDescription("type of the event. <i><B>ns_EventInfo.dwEventType.</B></i> <font color='red'><B>Unable to modify value.</B></font>");
                   ei_CSVDescProp.setShortDescription("description of the event. <i><B>ns_EventInfo.szCSVDesc.</B></i>");
                   entityGrp.put(ei_EventTypeProp);
                   entityGrp.put(ei_CSVDescProp);
                   sets.add(entityGrp);
                   break;
                case ENTITY_ANALOG:
                   AnalogInfo analogInfo = (AnalogInfo) entity;
                   PropertySupport.Reflection ai_SampleRateProp = new PropertySupport.Reflection(analogInfo, double.class, "sampleRate");
                   PropertySupport.Reflection ai_MinValProp = new PropertySupport.Reflection(analogInfo, double.class, "minVal");
                   PropertySupport.Reflection ai_MaxValProp = new PropertySupport.Reflection(analogInfo, double.class, "maxVal");
                   PropertySupport.Reflection ai_UnitsProp = new PropertySupport.Reflection(analogInfo, String.class, "units");
                   PropertySupport.Reflection ai_ResolutionProp = new PropertySupport.Reflection(analogInfo, double.class, "resolution");
                   PropertySupport.Reflection ai_LocationXProp = new PropertySupport.Reflection(analogInfo, double.class, "locationX");
                   PropertySupport.Reflection ai_LocationYProp = new PropertySupport.Reflection(analogInfo, double.class, "locationY");
                   PropertySupport.Reflection ai_LocationZProp = new PropertySupport.Reflection(analogInfo, double.class, "locationZ");
                   PropertySupport.Reflection ai_LocationUserProp = new PropertySupport.Reflection(analogInfo, double.class, "locationUser");
                   PropertySupport.Reflection ai_HighFreqCornerProp = new PropertySupport.Reflection(analogInfo, double.class, "highFreqCorner");
                   PropertySupport.Reflection ai_HighFreqOrderProp = new PropertySupport.Reflection(analogInfo, long.class, "highFreqOrder");
                   PropertySupport.Reflection ai_HighFilterTypeProp = new PropertySupport.Reflection(analogInfo, String.class, "highFilterType");
                   PropertySupport.Reflection ai_LowFreqCornerProp = new PropertySupport.Reflection(analogInfo, double.class, "lowFreqCorner");
                   PropertySupport.Reflection ai_LowFreqOrderProp = new PropertySupport.Reflection(analogInfo, long.class, "lowFreqOrder");
                   PropertySupport.Reflection ai_LowFilterTypeProp = new PropertySupport.Reflection(analogInfo, String.class, "lowFilterType");
                   PropertySupport.Reflection ai_ProbeInfoProp = new PropertySupport.Reflection(analogInfo, String.class, "probeInfo");
                   ai_SampleRateProp.setName("Sample Rate"); // need to be identified.
                   ai_MinValProp.setName("Min Val"); // need to be identified.
                   ai_MaxValProp.setName("Max Val"); // need to be identified.
                   ai_UnitsProp.setName("Unit"); // need to be identified.
                   ai_ResolutionProp.setName("Resolution"); // need to be identified.
                   ai_LocationXProp.setName("Location X"); // need to be identified.
                   ai_LocationYProp.setName("Location Y"); // need to be identified.
                   ai_LocationZProp.setName("Location Z"); // need to be identified.
                   ai_LocationUserProp.setName("Location User"); // need to be identified.
                   ai_HighFreqCornerProp.setName("H Freq Corner"); // need to be identified.
                   ai_HighFreqOrderProp.setName("H Freq Order"); // need to be identified.
                   ai_HighFilterTypeProp.setName("H Filter Type"); // need to be identified.
                   ai_LowFreqCornerProp.setName("L Freq Corner"); // need to be identified.
                   ai_LowFreqOrderProp.setName("L Freq Order"); // need to be identified.
                   ai_LowFilterTypeProp.setName("L Filter Type"); // need to be identified.
                   ai_ProbeInfoProp.setName("ProbeInfo"); // need to be identified.
                   ai_SampleRateProp.setShortDescription("sampling rate. [Hz] <i><B>ns_AnalogInfo.dSampleRate.</B></i>");
                   ai_MinValProp.setShortDescription("minimum value. <i><B>ns_AnalogInfo.dMinVal.</B></i>");
                   ai_MaxValProp.setShortDescription("maximum value. <i><B>ns_AnalogInfo.dMaxVal.</B></i>");
                   ai_UnitsProp.setShortDescription("unit. <i><B>ns_AnalogInfo.szUnits.</B></i>");
                   ai_ResolutionProp.setShortDescription("resolution. <i><B>ns_AnalogInfo.dResolution.</B></i>");
                   ai_LocationXProp.setShortDescription("x position. <i><B>ns_AnalogInfo.dLocationX.</B></i>");
                   ai_LocationYProp.setShortDescription("y position. <i><B>ns_AnalogInfo.dLocationY.</B></i>");
                   ai_LocationZProp.setShortDescription("z position. <i><B>ns_AnalogInfo.dLocationZ.</B></i>");
                   ai_LocationUserProp.setShortDescription("additional information about position. <i><B>ns_AnalogInfo.dLocationUser.</B></i>");
                   ai_HighFreqCornerProp.setShortDescription("high frequency cutoff. [Hz] <i><B>ns_AnalogInfo.dHighFreqCorner.</B></i>");
                   ai_HighFreqOrderProp.setShortDescription("order of the filter used for high frequency cutoff. <i><B>ns_AnalogInfo.dwHighFreqOrder.</B></i>");
                   ai_HighFilterTypeProp.setShortDescription("type of the filter used for high frequency cutoff. <i><B>ns_AnalogInfo.szHighFilterType.</B></i>");
                   ai_LowFreqCornerProp.setShortDescription("low frequency cutoff. [Hz] <i><B>ns_AnalogInfo.dLowFilterType.</B></i>");
                   ai_LowFreqOrderProp.setShortDescription("order of the filter used for low frequency cutoff. <i><B>ns_AnalogInfo.dwLowFreqOrder.</B></i>");
                   ai_LowFilterTypeProp.setShortDescription("type of the filter used for low frequency cutoff. <i><B>ns_AnalogInfo.szLowFilterType.</B></i>");
                   ai_ProbeInfoProp.setShortDescription("probe information. <i><B>ns_AnalogInfo.szProbeInfo.</B></i>");
                   entityGrp.put(ai_SampleRateProp);
                   entityGrp.put(ai_MinValProp);
                   entityGrp.put(ai_MaxValProp);
                   entityGrp.put(ai_UnitsProp);
                   entityGrp.put(ai_ResolutionProp);
                   entityGrp.put(ai_LocationXProp);
                   entityGrp.put(ai_LocationYProp);
                   entityGrp.put(ai_LocationZProp);
                   entityGrp.put(ai_LocationUserProp);
                   entityGrp.put(ai_HighFreqCornerProp);
                   entityGrp.put(ai_HighFreqOrderProp);
                   entityGrp.put(ai_HighFilterTypeProp);
                   entityGrp.put(ai_LowFreqCornerProp);
                   entityGrp.put(ai_LowFreqOrderProp);
                   entityGrp.put(ai_LowFilterTypeProp);
                   entityGrp.put(ai_ProbeInfoProp);
                   sets.add(entityGrp);
                   break;
                case ENTITY_SEGMENT:
                   SegmentInfo segmentInfo = (SegmentInfo) entity;
                   PropertySupport.Reflection si_SourceCountProp = new PropertySupport.Reflection(segmentInfo, long.class, "getSourceCount", null);
                   PropertySupport.Reflection si_MinSampleCountProp = new PropertySupport.Reflection(segmentInfo, long.class, "getMinSampleCount", null);
                   PropertySupport.Reflection si_MaxSampleCountProp = new PropertySupport.Reflection(segmentInfo, long.class, "getMaxSampleCount", null);
                   PropertySupport.Reflection si_SampleRateProp = new PropertySupport.Reflection(segmentInfo, double.class, "sampleRate");
                   PropertySupport.Reflection si_UnitsProp = new PropertySupport.Reflection(segmentInfo, String.class, "units");
                   si_SourceCountProp.setName("Source Count"); // need to be identified.
                   si_MinSampleCountProp.setName("Min Sample Count"); // need to be identified.
                   si_MaxSampleCountProp.setName("Max Sample Count"); // need to be identified.
                   si_SampleRateProp.setName("Sample Rate"); // need to be identified.
                   si_UnitsProp.setName("Unit"); // need to be identified.
                   si_SourceCountProp.setShortDescription("number of sources. <i><B>ns_SegmentInfo.dwSourceCount.</B></i> <font color='red'><B>Unable to modify value.</B></font>");
                   si_MinSampleCountProp.setShortDescription("minimum number of samples. <i><B>ns_SegmentInfo.dwMinSampleCount.</B></i> <font color='red'><B>Unable to modify value.</B></font>");
                   si_MaxSampleCountProp.setShortDescription("maximum number of samples. <i><B>ns_SegmentInfo.dwMaxSampleCount.</B></i> <font color='red'><B>Unable to modify value.</B></font>");
                   si_SampleRateProp.setShortDescription("sampling rate. [Hz] <i><B>ns_SegmentInfo.dSampleRate.</B></i>");
                   si_UnitsProp.setShortDescription("unit. <i><B>ns_SegmentInfo.szUnits.</B></i>");
                   entityGrp.put(si_SourceCountProp);
                   entityGrp.put(si_MinSampleCountProp);
                   entityGrp.put(si_MaxSampleCountProp);
                   entityGrp.put(si_SampleRateProp);
                   entityGrp.put(si_UnitsProp);
                   sets.add(entityGrp);
 
                   ArrayList<SegmentSourceInfo> segSourceInfos = segmentInfo.getSegSourceInfos();
                   if (segSourceInfos != null) {
                      for (int jj = 0; jj < segSourceInfos.size(); jj++) {
                         SegmentSourceInfo ss = segSourceInfos.get(jj);
 
                         // Create Sheet.
                         Sheet.Set sourceGrp = Sheet.createPropertiesSet();
                         sourceGrp.setDisplayName("Source No : " + (jj + 1) + " : ProbeInfo [" + ss.getProbeInfo() + "]");
                         sourceGrp.setName("neuroshareEntity" + ":" + jj); // need to be identified.
                         sourceGrp.setValue("tabName", "Neuroshare");
 
                         PropertySupport.Reflection si_MinValProp = new PropertySupport.Reflection(ss, double.class, "minVal");
                         PropertySupport.Reflection si_MaxValProp = new PropertySupport.Reflection(ss, double.class, "maxVal");
                         PropertySupport.Reflection si_ResolutionProp = new PropertySupport.Reflection(ss, double.class, "resolution");
                         PropertySupport.Reflection si_SubSampleShiftProp = new PropertySupport.Reflection(ss, double.class, "subSampleShift");
                         PropertySupport.Reflection si_LocationXProp = new PropertySupport.Reflection(ss, double.class, "locationX");
                         PropertySupport.Reflection si_LocationYProp = new PropertySupport.Reflection(ss, double.class, "locationY");
                         PropertySupport.Reflection si_LocationZProp = new PropertySupport.Reflection(ss, double.class, "locationZ");
                         PropertySupport.Reflection si_LocationUserProp = new PropertySupport.Reflection(ss, double.class, "locationUser");
                         PropertySupport.Reflection si_HighFreqCornerProp = new PropertySupport.Reflection(ss, double.class, "highFreqCorner");
                         PropertySupport.Reflection si_HighFreqOrderProp = new PropertySupport.Reflection(ss, long.class, "highFreqOrder");
                         PropertySupport.Reflection si_HighFilterTypeProp = new PropertySupport.Reflection(ss, String.class, "highFilterType");
                         PropertySupport.Reflection si_LowFreqCornerProp = new PropertySupport.Reflection(ss, double.class, "lowFreqCorner");
                         PropertySupport.Reflection si_LowFreqOrderProp = new PropertySupport.Reflection(ss, long.class, "lowFreqOrder");
                         PropertySupport.Reflection si_LowFilterTypeProp = new PropertySupport.Reflection(ss, String.class, "lowFilterType");
                         PropertySupport.Reflection si_ProbeInfoProp = new PropertySupport.Reflection(ss, String.class, "probeInfo");
                         si_MinValProp.setName("Min Val"); // need to be identified.
                         si_MaxValProp.setName("Max Val"); // need to be identified.
                         si_ResolutionProp.setName("Resolution"); // need to be identified.
                         si_SubSampleShiftProp.setName("Sub Sample Shift"); // need to be identified.
                         si_LocationXProp.setName("Location X"); // need to be identified.
                         si_LocationYProp.setName("Location Y"); // need to be identified.
                         si_LocationZProp.setName("Location Z"); // need to be identified.
                         si_LocationUserProp.setName("Location User"); // need to be identified.
                         si_HighFreqCornerProp.setName("H Freq Corner"); // need to be identified.
                         si_HighFreqOrderProp.setName("H Freq Order"); // need to be identified.
                         si_HighFilterTypeProp.setName("H Filter Type"); // need to be identified.
                         si_LowFreqCornerProp.setName("L Freq Corner"); // need to be identified.
                         si_LowFreqOrderProp.setName("L Freq Order"); // need to be identified.
                         si_LowFilterTypeProp.setName("L Filter Type"); // need to be identified.
                         si_ProbeInfoProp.setName("ProbeInfo"); // need to be identified.
                         si_MinValProp.setShortDescription("minimum value. <i><B>ns_SegSourceInfo.dMinVal.</B></i>");
                         si_MaxValProp.setShortDescription("maximum value. <i><B>ns_SegSourceInfo.dMaxVal.</B></i>");
                         si_ResolutionProp.setShortDescription("resolution. <i><B>ns_SegSourceInfo.dResolution.</B></i>");
                         si_SubSampleShiftProp.setShortDescription("sub sample shift. <i><B>ns_SegSourceInfo.dSubSampleShift.</B></i>");
                         si_LocationXProp.setShortDescription("x position. <i><B>ns_SegSourceInfo.dLocationX.</B></i>");
                         si_LocationYProp.setShortDescription("y position. <i><B>ns_SegSourceInfo.dLocationY.</B></i>");
                         si_LocationZProp.setShortDescription("z position. <i><B>ns_SegSourceInfo.dLocationZ.</B></i>");
                         si_LocationUserProp.setShortDescription("additional information about position. <i><B>ns_SegSourceInfo.dLocationUser.</B></i>");
                         si_HighFreqCornerProp.setShortDescription("high frequency cutoff. [Hz] <i><B>ns_SegSourceInfo.dHighFreqCorner.</B></i>");
                         si_HighFreqOrderProp.setShortDescription("order of the filter used for high frequency cutoff. <i><B>ns_SegSourceInfo.dwHighFreqOrder.</B></i>");
                         si_HighFilterTypeProp.setShortDescription("type of the filter used for high frequency cutoff. <i><B>ns_SegSourceInfo.szHighFilterType.</B></i>");
                         si_LowFreqCornerProp.setShortDescription("low frequency cutoff. [Hz] <i><B>ns_SegSourceInfo.dLowFilterType.</B></i>");
                         si_LowFreqOrderProp.setShortDescription("order of the filter used for low frequency cutoff. <i><B>ns_SegSourceInfo.dwLowFreqOrder.</B></i>");
                         si_LowFilterTypeProp.setShortDescription("type of the filter used for low frequency cutoff. <i><B>ns_SegSourceInfo.szLowFilterType.</B></i>");
                         si_ProbeInfoProp.setShortDescription("probe information. <i><B>ns_SegSourceInfo.szProbeInfo.</B></i>");
                         sourceGrp.put(si_MinValProp);
                         sourceGrp.put(si_MaxValProp);
                         sourceGrp.put(si_ResolutionProp);
                         sourceGrp.put(si_SubSampleShiftProp);
                         sourceGrp.put(si_LocationXProp);
                         sourceGrp.put(si_LocationYProp);
                         sourceGrp.put(si_LocationZProp);
                         sourceGrp.put(si_LocationUserProp);
                         sourceGrp.put(si_HighFreqCornerProp);
                         sourceGrp.put(si_HighFreqOrderProp);
                         sourceGrp.put(si_HighFilterTypeProp);
                         sourceGrp.put(si_LowFreqCornerProp);
                         sourceGrp.put(si_LowFreqOrderProp);
                         sourceGrp.put(si_LowFilterTypeProp);
                         sourceGrp.put(si_ProbeInfoProp);
                         sets.add(sourceGrp);
                      }
                   }
                   break;
                case ENTITY_NEURAL:
                   NeuralInfo neuralInfo = (NeuralInfo) entity;
                   PropertySupport.Reflection ni_SourceEntityIDProp = new PropertySupport.Reflection(neuralInfo, long.class, "sourceEntityID");
                   PropertySupport.Reflection ni_SourceUnitIDProp = new PropertySupport.Reflection(neuralInfo, long.class, "sourceUnitID");
                   PropertySupport.Reflection ni_ProbeInfoProp = new PropertySupport.Reflection(neuralInfo, String.class, "probeInfo");
                   ni_SourceEntityIDProp.setName("Source Entity ID"); // need to be identified.
                   ni_SourceUnitIDProp.setName("Source Unit ID"); // need to be identified.
                   ni_ProbeInfoProp.setName("ProbeInfo"); // need to be identified.
                   ni_SourceEntityIDProp.setShortDescription("ID number of the source entity. <i><B>ns_NeuralInfo.dwSourceEntityID.</B></i>");
                   ni_SourceUnitIDProp.setShortDescription("sorted unit ID number of the source entity. <i><B>ns_NeuralInfo.dwSourceUnitID.</B></i>");
                   ni_ProbeInfoProp.setShortDescription("probe information. <i><B>ns_NeuralInfo.szProbeInfo.</B></i>");
                   entityGrp.put(ni_SourceEntityIDProp);
                   entityGrp.put(ni_SourceUnitIDProp);
                   entityGrp.put(ni_ProbeInfoProp);
                   sets.add(entityGrp);
                   break;
 
                default:
                   break;
             }
          }
 
       } catch (IllegalArgumentException ex) {
          Exceptions.printStackTrace(ex);
       } catch (NoSuchMethodException ex) {
          ErrorManager.getDefault();
       }
 
       return sets;
    }
 
    // Right-clicked menu. Save
    private class SaveAction extends AbstractAction implements Presenter.Popup {
 
       public SaveAction() {
          putValue(NAME, "Neuroshare");
       }
 
       @Override
       public void actionPerformed(ActionEvent e) {
          GeneralFileInfo obj = getLookup().lookup(GeneralFileInfo.class);
 
          // Select file.
          if (obj == null || obj.getFileType().equals("Directory")) {
             JOptionPane.showMessageDialog(null, "Select a file.");
             return;
          }
 
          // Neuroshare file to Neuroshare file. : Overwrite the Neuroshare File.
          if (obj.getFileType().equals("File/nsn")) {
             if (obj.getNsObj() != null) {
                // Re-Create the Neuroshare file.
                NsnFileModelConverter.ModelConvert(obj.getNsObj(), obj.getFilePath(), obj.getFilePath());
             } else {
                JOptionPane.showMessageDialog(null, "Nothing to change.");
                return;
             }
          } // Other file format. : Nothing to do.
          else {
             JOptionPane.showMessageDialog(null, "Nothing to do.");
             return;
          }
 
          // refresh obj's value.
          GeneralFileInfo newFile = new GeneralFileInfo(obj.getFilePath());
          obj.setModifiedTime(newFile.getModifiedTime());
          obj.setFileSize(newFile.getFileSize());
 
          //setName to refresh obj's value.
          setName(obj.getFileName());
 
 
       }
 
       @Override
       public JMenuItem getPopupPresenter() {
          JMenu jmConvertto = new JMenu("Save as");
          jmConvertto.add(new JMenuItem(this));
          return jmConvertto;
       }
    }
 
    // Right-clicked menu. Reload
    private class ReloadAction extends AbstractAction implements Presenter.Popup {
 
       public ReloadAction() {
          putValue(NAME, "Reload");
       }
 
       @Override
       public void actionPerformed(ActionEvent e) {
          GeneralFileInfo obj = getLookup().lookup(GeneralFileInfo.class);
 
          if (obj == null) {
             JOptionPane.showMessageDialog(null, "Select a directory or file.");
             return;
          }
 
 
          if (obj.getFileType() == FileType.DIRECTORY) {
             // Reload Directory.
             setChildren(new ExplorerChildren(obj));
 
          } else if (obj.getFileType() != FileType.UNKNOWN) {
             // Reload File.
             GeneralFileInfo tempFile = new GeneralFileInfo(obj.getFilePath());
             obj.setModifiedTime(tempFile.getModifiedTime());
             obj.setFileSize(tempFile.getFileSize());
             setName(tempFile.getFileName());
          }
       }
 
       @Override
       public JMenuItem getPopupPresenter() {
          JMenuItem jmConvertto = new JMenuItem(this);
          return jmConvertto;
       }
    }
 
    // Right-clicked menu. Delete
    private class DeleteAction extends AbstractAction implements Presenter.Popup {
 
       public DeleteAction() {
          putValue(NAME, "Delete");
       }
 
       @Override
       public void actionPerformed(ActionEvent e) {
          GeneralFileInfo obj = getLookup().lookup(GeneralFileInfo.class);
 
          if (obj == null) {
             JOptionPane.showMessageDialog(null, "Select a directory or file.");
             return;
          }
 
          // Confirm to delete or not.
          int selected = JOptionPane.showConfirmDialog(null, "Delete it (cannot be undone)?", "Confirm", JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
          if (selected != JOptionPane.OK_OPTION) {
             return;
          }
 
          // Delete Directory.
          if (obj.getFileType().equals("Directory")) {
             try {
                FileUtils.deleteDirectory(new File(obj.getFilePath()));
             } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
             }
 
          } // Delete File.
          else if (obj.getFileType() != FileType.UNKNOWN) {
             try {
                FileUtils.forceDelete(new File(obj.getFilePath()));
             } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
             }
          }
       }
 
       @Override
       public JMenuItem getPopupPresenter() {
          JMenuItem jmConvertto = new JMenuItem(this);
          return jmConvertto;
       }
    }
 
    // Right-clicked menu. Add
    private class AddAction extends AbstractAction implements Presenter.Popup {
 
       public AddAction() {
          putValue(NAME, "Files");
          // Options (short cut and nemonic)
          //putValue(MNEMONIC_KEY, KeyEvent.VK_F);
          //putValue(DISPLAYED_MNEMONIC_INDEX_KEY, 1);
       }
 
       @Override
       public void actionPerformed(ActionEvent e) {
          GeneralFileInfo obj = getLookup().lookup(GeneralFileInfo.class);
 
          if (obj == null || obj.getFileType() != FileType.DIRECTORY && obj.getFileType() != FileType.UNKNOWN) {
             JOptionPane.showMessageDialog(null, "Select a directory.");
             return;
          }
 
          // Choose files
          JFileChooser fc = new JFileChooser();
          fc.setMultiSelectionEnabled(true);
          int selected = fc.showOpenDialog(null);
          if (selected == JFileChooser.APPROVE_OPTION) {
             File[] srcFiles = fc.getSelectedFiles();
             for (int ii = 0; ii < srcFiles.length; ii++) {
                String newFilePath = obj.getFilePath() + File.separator + srcFiles[ii].getName();
                File newFile = new File(newFilePath);
                try {
                   FileUtils.copyFile(srcFiles[ii], newFile);
                } catch (IOException ex) {
                   Exceptions.printStackTrace(ex);
                }
             }
 
             // Reset nodes.
             setChildren(new ExplorerChildren(obj));
          }
       }
 
       @Override
       public JMenuItem getPopupPresenter() {
          JMenu jmConvertto = new JMenu("Add");
          jmConvertto.add(new JMenuItem(this));
          return jmConvertto;
       }
    }
 
    // Right-clicked menu. Copy to Workspace
    private class CopyToWorkspaceAction extends AbstractAction implements Presenter.Popup {
 
       public CopyToWorkspaceAction() {
          putValue(NAME, "Copy To Workspace");
       }
 
       @Override
       public void actionPerformed(ActionEvent e) {
          GeneralFileInfo obj = getLookup().lookup(GeneralFileInfo.class);
 
          if (obj == null || obj.getFileType() == FileType.DIRECTORY || obj.getFileType() == FileType.UNKNOWN) {
             JOptionPane.showMessageDialog(null, "Select a data file.");
             return;
          }
 
          // Open ChannelSelecter Dialog.
          ChannelSelector cs = new ChannelSelector(obj);
          cs.showDialog();
 
       }
 
       @Override
       public JMenuItem getPopupPresenter() {
          JMenuItem jmConvertto = new JMenuItem(this);
          return jmConvertto;
       }
    }
 }
