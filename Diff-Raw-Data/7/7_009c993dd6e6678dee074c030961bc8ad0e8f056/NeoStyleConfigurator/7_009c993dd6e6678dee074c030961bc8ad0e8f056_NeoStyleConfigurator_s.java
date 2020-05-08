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
 package org.amanzi.awe.neostyle;
 
 import java.awt.Color;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.List;
 
 import net.refractions.udig.project.internal.Layer;
 import net.refractions.udig.project.ui.internal.dialogs.ColorEditor;
 import net.refractions.udig.style.IStyleConfigurator;
 
 import org.amanzi.awe.catalog.neo.GeoNeo;
 import org.amanzi.awe.catalog.neo.NeoGeoResource;
 import org.amanzi.neo.services.enums.GisTypes;
 import org.amanzi.neo.services.enums.NetworkTypes;
 import org.amanzi.neo.services.statistic.PropertyHeader;
 import org.amanzi.neo.services.ui.NeoServiceProviderUi;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.custom.ScrolledComposite;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.SelectionListener;
 import org.eclipse.swt.graphics.RGB;
 import org.eclipse.swt.layout.FormAttachment;
 import org.eclipse.swt.layout.FormData;
 import org.eclipse.swt.layout.FormLayout;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Combo;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Group;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Spinner;
 import org.neo4j.graphdb.GraphDatabaseService;
 
 /**
  * Style editor for org.amanzi.awe.render.network
  * 
  * @author Cinkel_A
  * @since 1.0.0
  */
 public class NeoStyleConfigurator extends IStyleConfigurator {
 
     /** NeoStyleConfigurator ID field */
     public static final String ID = "org.amanzi.awe.neostyle.style"; //$NON-NLS-1$
 
     private static final String[] FONT_SIZE_ARRAY = new String[] {"8", "9", "10", "11", "12", "14", "16", "18", "20", "24"}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$ //$NON-NLS-9$ //$NON-NLS-10$
     //private static final String[] ICON_SIZES = new String[] {"6", "8", "12", "16", "32", "48", "64"}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$
 
     public NeoStyleConfigurator() {
         super();
     }
 
     @Override
     public boolean canStyle(Layer aLayer) {
         return aLayer.getStyleBlackboard().get(ID) != null;
     }
 
     private Label labelFill;
     private Label labelLabel;
     private Label labelLine;
     private NeoStyle curStyle;
     private ColorEditor cEdFill;
     private ColorEditor cEdLine;
     private ColorEditor cEdLabel;
     private Label lSmallSymb;
     private Spinner tSmallSymb;
     private Label lSmallestSymb;
     private Spinner tSmallestSymb;
     private Label lLabeling;
     private Spinner tLabeling;
     private Button rButton1;
     private Button rButton2;
     private Label lSymbolSize;
     private Spinner tSymbolSize;
     private Label lTransparency;
     private Spinner tTransparency;
     private Label lFillSite;
     private ColorEditor cEdFillSite;
     private Label lMaxSymSize;
     private Spinner sMaxSymSize;
     private Label lDefBeamwidth;
     private Spinner sDefBeamwidth;
     private Label lIconOffset;
     private Spinner sIconOffset;
 
     private Group grSiteSymb;
 
     private Group grScale;
 
     private boolean isNetwork;
     private boolean isProbe;
 
     private Combo cFontSize;
 
     private Combo cMainProperty;
 
     private Combo cSecondaryProperty;
 
     private Combo cSecondaryFontSize;
 
     private Label lFontSize;
 
     private Label lMainProperty;
 
     private Group labelsGroup;
 
     private Label lSecondaryFontSize;
 
     private Label lSecondaryProperty;
 
     private Button bTransp;
 
     private Button bCorrelation;
 
     @Override
     public void createControl(Composite parent) {
         //Lagutko, 15.03.2010, adding a Scroll
         GridLayout mainLayout = new GridLayout(1, false);
         parent.setLayout(mainLayout);
         
         ScrolledComposite scroll = new ScrolledComposite(parent, SWT.V_SCROLL | SWT.H_SCROLL);
         scroll.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
         scroll.setExpandVertical(true);
         scroll.setExpandHorizontal(true);
         
         parent = new Composite(scroll, SWT.NONE);
         parent.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
         parent.setLayout(new GridLayout(1, true));
         
         FormLayout layout = new FormLayout();
         layout.marginHeight = 0;
         layout.marginWidth = 0;
         layout.marginWidth = 0;
         layout.spacing = 0;
         parent.setLayout(layout);
         // color block
         Group xGroup = new Group(parent, SWT.NONE);
         xGroup.setText("Colors"); //$NON-NLS-1$
         FormData formData = new FormData();
         formData.top = new FormAttachment(0, 5);
         formData.left = new FormAttachment(0, 5);
         formData.right = new FormAttachment(70, -10);
         xGroup.setLayout(new FormLayout());
         xGroup.setLayoutData(formData);
 
         labelFill = new Label(xGroup, SWT.NONE);
         labelFill.setText(Messages.Color_Fill_Sector);
         cEdFill = new ColorEditor(xGroup);
         formData = new FormData();
         formData.top = new FormAttachment(0, 5);
         formData.left = new FormAttachment(2);
         labelFill.setLayoutData(formData);
         formData = new FormData();
         formData.left = new FormAttachment(labelFill, 130);
         cEdFill.getButton().setLayoutData(formData);
 
         lFillSite = new Label(xGroup, SWT.NONE);
         lFillSite.setText(Messages.Color_Fill_Site);
         cEdFillSite = new ColorEditor(xGroup);
         formData = new FormData();
         formData.top = new FormAttachment(labelFill, 15);
         formData.left = new FormAttachment(2);
         lFillSite.setLayoutData(formData);
         formData = new FormData();
         formData.left = new FormAttachment(labelFill, 130);
         formData.top = new FormAttachment(labelFill, 10);
         cEdFillSite.getButton().setLayoutData(formData);
 
         labelLine = new Label(xGroup, SWT.NONE);
         labelLine.setText(Messages.Color_Line);
         cEdLine = new ColorEditor(xGroup);
         formData = new FormData();
         formData.top = new FormAttachment(lFillSite, 15);
         formData.left = new FormAttachment(2);
         labelLine.setLayoutData(formData);
         formData = new FormData();
         formData.left = new FormAttachment(labelFill, 130);
         formData.top = new FormAttachment(lFillSite, 10);
         cEdLine.getButton().setLayoutData(formData);
 
         labelLabel = new Label(xGroup, SWT.NONE);
         labelLabel.setText(Messages.Color_Label);
         cEdLabel = new ColorEditor(xGroup);
         formData = new FormData();
         formData.top = new FormAttachment(labelLine, 15);
         formData.left = new FormAttachment(2);
         labelLabel.setLayoutData(formData);
         formData = new FormData();
         formData.left = new FormAttachment(labelFill, 130);
         formData.top = new FormAttachment(labelLine, 10);
         cEdLabel.getButton().setLayoutData(formData);
 
         labelsGroup = new Group(parent, SWT.NONE);
         labelsGroup.setText("Labels"); //$NON-NLS-1$
         formData = new FormData();
         formData.top = new FormAttachment(xGroup, 5);
         formData.left = new FormAttachment(0, 5);
         formData.right = new FormAttachment(70, -10);
         labelsGroup.setLayoutData(formData);
         labelsGroup.setLayout(new GridLayout(2, false));
 
         lMainProperty = new Label(labelsGroup, SWT.NONE);
         lMainProperty.setText(Messages.Site_Property);
         lMainProperty.setLayoutData(new GridData(SWT.LEFT));
         cMainProperty = new Combo(labelsGroup, SWT.DROP_DOWN | SWT.READ_ONLY);
         cMainProperty.setLayoutData(new GridData(SWT.FILL | GridData.FILL_HORIZONTAL));
         lFontSize = new Label(labelsGroup, SWT.NONE);
         lFontSize.setText(Messages.Font_Size_Site);
         lFontSize.setLayoutData(new GridData(SWT.LEFT));
         cFontSize = new Combo(labelsGroup, SWT.DROP_DOWN | SWT.RIGHT);
         cFontSize.setItems(getDefaultFontItem());
         cFontSize.setLayoutData(new GridData(SWT.FILL | GridData.FILL_HORIZONTAL));
 
         lSecondaryProperty = new Label(labelsGroup, SWT.NONE);
         lSecondaryProperty.setText(Messages.Sector_Property);
         lSecondaryProperty.setLayoutData(new GridData(SWT.LEFT));
         cSecondaryProperty = new Combo(labelsGroup, SWT.DROP_DOWN | SWT.READ_ONLY);
         cSecondaryProperty.setLayoutData(new GridData(SWT.FILL | GridData.FILL_HORIZONTAL));
         lSecondaryFontSize = new Label(labelsGroup, SWT.NONE);
         lSecondaryFontSize.setText(Messages.Font_Size_Sector);
         lSecondaryFontSize.setLayoutData(new GridData(SWT.LEFT));
         cSecondaryFontSize = new Combo(labelsGroup, SWT.DROP_DOWN | SWT.RIGHT);
         cSecondaryFontSize.setItems(getDefaultFontItem());
         cSecondaryFontSize.setLayoutData(new GridData(SWT.FILL | GridData.FILL_HORIZONTAL));
 
         // formData = new FormData();
         // formData.top = new FormAttachment(sFontSize, 5, SWT.CENTER);
         // formData.left = new FormAttachment(2);
         // lFontSize.setLayoutData(formData);
         // formData = new FormData();
         // formData.left = new FormAttachment(labelFill, 130);
         // formData.top = new FormAttachment(labelLabel, 10);
         // sFontSize.setLayoutData(formData);
 
         grSiteSymb = new Group(parent, SWT.NONE);
         grSiteSymb.setText(Messages.Density_Thresholds);
         formData = new FormData();
         formData.top = new FormAttachment(labelsGroup, 15);
         formData.left = new FormAttachment(0, 5);
         formData.right = new FormAttachment(70, -10);
         grSiteSymb.setLayout(new FormLayout());
         grSiteSymb.setLayoutData(formData);
 
         lSmallestSymb = new Label(grSiteSymb, SWT.NONE);
         tSmallestSymb = new Spinner(grSiteSymb, SWT.BORDER);
         formData = new FormData();
         formData.top = new FormAttachment(tSmallestSymb, 5, SWT.CENTER);
         formData.left = new FormAttachment(2);
         lSmallestSymb.setLayoutData(formData);
 
         formData = new FormData();
         formData.left = new FormAttachment(70, 10);
         formData.right = new FormAttachment(100, -5);
         formData.top = new FormAttachment(0, 5);
         tSmallestSymb.setLayoutData(formData);
 
         lSmallSymb = new Label(grSiteSymb, SWT.NONE);
         tSmallSymb = new Spinner(grSiteSymb, SWT.BORDER);
         formData = new FormData();
         formData.top = new FormAttachment(tSmallSymb, 5, SWT.CENTER);
         formData.left = new FormAttachment(2);
         lSmallSymb.setLayoutData(formData);
 
         formData = new FormData();
         formData.left = new FormAttachment(70, 10);
         formData.right = new FormAttachment(100, -5);
         formData.top = new FormAttachment(tSmallestSymb, 5);
         tSmallSymb.setLayoutData(formData);
 
         lLabeling = new Label(grSiteSymb, SWT.NONE);
         tLabeling = new Spinner(grSiteSymb, SWT.BORDER);
 
         formData = new FormData();
         formData.top = new FormAttachment(tLabeling, 5, SWT.CENTER);
         formData.left = new FormAttachment(2);
         lLabeling.setLayoutData(formData);
 
         formData = new FormData();
         formData.left = new FormAttachment(70, 10);
         formData.right = new FormAttachment(100, -5);
         formData.top = new FormAttachment(tSmallSymb, 5);
         tLabeling.setLayoutData(formData);
 
         lSmallestSymb.setText(Messages.Smallest_Symbols);
         lSmallSymb.setText(Messages.Small_Symbols);
         lLabeling.setText(Messages.Labels);
 
         grScale = new Group(parent, SWT.NONE);
         grScale.setText(Messages.Symbol_Sizes);
         formData = new FormData();
         formData.top = new FormAttachment(grSiteSymb, 15);
         formData.left = new FormAttachment(0, 5);
         formData.right = new FormAttachment(70, -10);
         grScale.setLayout(new FormLayout());
         grScale.setLayoutData(formData);
         rButton1 = new Button(grScale, SWT.RADIO);
         formData = new FormData();
         formData.left = new FormAttachment(2);
         formData.top = new FormAttachment(0, 5);
         rButton1.setLayoutData(formData);
         rButton2 = new Button(grScale, SWT.RADIO);
         formData = new FormData();
         formData.top = new FormAttachment(0, 5);
         formData.left = new FormAttachment(rButton1, 10);
         rButton2.setLayoutData(formData);
 
         rButton1.setText(Messages.Symbol_Scale_With_Zoom);
         rButton2.setText(Messages.Symbol_Use_Fixed_Size);
 
         lSymbolSize = new Label(grScale, SWT.NONE);
         lSymbolSize.setText(Messages.Symbol_Size);
         tSymbolSize = new Spinner(grScale, SWT.BORDER);
         formData = new FormData();
         formData.top = new FormAttachment(tSymbolSize, 5, SWT.CENTER);
         formData.left = new FormAttachment(2);
         lSymbolSize.setLayoutData(formData);
 
         formData = new FormData();
         formData.left = new FormAttachment(70, 10);
         formData.top = new FormAttachment(rButton1, 5);
         formData.right = new FormAttachment(100, -5);
         tSymbolSize.setLayoutData(formData);
 
         lTransparency = new Label(grScale, SWT.NONE);
         lTransparency.setText(Messages.Symbol_Transparency);
         tTransparency = new Spinner(grScale, SWT.BORDER);
         formData = new FormData();
         formData.top = new FormAttachment(tTransparency, 5, SWT.CENTER);
         formData.left = new FormAttachment(2);
         lTransparency.setLayoutData(formData);
 
         formData = new FormData();
         formData.left = new FormAttachment(70, 10);
         formData.top = new FormAttachment(tSymbolSize, 5);
         formData.right = new FormAttachment(100, -5);
         tTransparency.setLayoutData(formData);
 
         bTransp = new Button(grScale, SWT.CHECK);
         bTransp.setText(Messages.Ignore_transsparency);
         formData = new FormData();
         formData.left = new FormAttachment(2);
         formData.top = new FormAttachment(tTransparency, 5);
         bTransp.setLayoutData(formData);
 
         bCorrelation = new Button(grScale, SWT.CHECK);
         bCorrelation.setText(Messages.Draw_correlation);
         formData = new FormData();
         formData.left = new FormAttachment(2);
         formData.top = new FormAttachment(bTransp, 5);
         bCorrelation.setLayoutData(formData);
 
         lMaxSymSize = new Label(grScale, SWT.NONE);
         sMaxSymSize = new Spinner(grScale, SWT.BORDER);
         lMaxSymSize.setText(Messages.Symbol_Max_Size);
 
         formData = new FormData();
         formData.top = new FormAttachment(sMaxSymSize, 5, SWT.CENTER);
         formData.left = new FormAttachment(2);
         lMaxSymSize.setLayoutData(formData);
 
         formData = new FormData();
         formData.left = new FormAttachment(70, 10);
         formData.top = new FormAttachment(bCorrelation, 5);
         formData.right = new FormAttachment(100, -5);
         sMaxSymSize.setLayoutData(formData);
         
         lDefBeamwidth = new Label(grScale, SWT.NONE);
         sDefBeamwidth = new Spinner(grScale, SWT.BORDER);
         lDefBeamwidth.setText(Messages.Symbol_Def_Beam);
 
         formData = new FormData();
         formData.top = new FormAttachment(sDefBeamwidth, 5, SWT.CENTER);
         formData.left = new FormAttachment(2);
         lDefBeamwidth.setLayoutData(formData);
 
         formData = new FormData();
         formData.left = new FormAttachment(70, 10);
         formData.top = new FormAttachment(sMaxSymSize, 5);
         formData.right = new FormAttachment(100, -5);
         sDefBeamwidth.setLayoutData(formData);
 
         lIconOffset = new Label(grScale, SWT.NONE);
         sIconOffset = new Spinner(grScale, SWT.BORDER);
         lIconOffset.setText(Messages.Icon_Offset);
 
         formData = new FormData();
         formData.top = new FormAttachment(sIconOffset, 5, SWT.CENTER);
         formData.left = new FormAttachment(2);
         lIconOffset.setLayoutData(formData);
 
         formData = new FormData();
         formData.left = new FormAttachment(70, 10);
         formData.top = new FormAttachment(sDefBeamwidth, 5);
         formData.right = new FormAttachment(100, -5);
         sIconOffset.setLayoutData(formData);
 
         // sets spinners range
         tLabeling.setMinimum(1);
         tLabeling.setMaximum(10000);
         tTransparency.setMinimum(1);
         tTransparency.setMaximum(100);
         tSmallestSymb.setMinimum(1);
         tSmallestSymb.setMaximum(10000);
         tSmallSymb.setMinimum(1);
         tSmallSymb.setMaximum(10000);
         tSymbolSize.setMinimum(1);
         tSymbolSize.setMaximum(10000);
         tTransparency.setMaximum(100);
         sDefBeamwidth.setMinimum(10);
         sDefBeamwidth.setMaximum(360);
         rButton1.addSelectionListener(new SelectionListener() {
             
             @Override
             public void widgetSelected(SelectionEvent e) {
                 lMaxSymSize.setVisible(rButton1.getSelection());
                 sMaxSymSize.setVisible(rButton1.getSelection());
             }
             
             @Override
             public void widgetDefaultSelected(SelectionEvent e) {
                 widgetSelected(e);
             }
         });
         
         scroll.setContent(parent);
         scroll.setMinSize(parent.computeSize(SWT.DEFAULT, SWT.DEFAULT));
     }
 
     /**
      * @return
      */
     private String[] getDefaultFontItem() {
 
         return FONT_SIZE_ARRAY;
     }
 
     @Override
     protected void refresh() {
         try {
             curStyle = (NeoStyle)getStyleBlackboard().get(ID);
             cEdFill.setColorValue(rgbFromColor(curStyle.getFill()));
             cEdLabel.setColorValue(rgbFromColor(curStyle.getLabel()));
             cEdLine.setColorValue(rgbFromColor(curStyle.getLine()));
             cEdFillSite.setColorValue(rgbFromColor(curStyle.getSiteFill()));
             tSmallestSymb.setSelection(curStyle.getSmallestSymb());
             tSmallSymb.setSelection(curStyle.getSmallSymb());
             tLabeling.setSelection(curStyle.getLabeling());
             if (curStyle.isFixSymbolSize()) {
                 rButton2.setSelection(true);
             } else {
                 rButton1.setSelection(true);
             }
 
             tSymbolSize.setSelection(curStyle.getSymbolSize());
             tTransparency.setSelection(curStyle.getSymbolTransparency());
             bTransp.setSelection(curStyle.isIgnoreTransparency());
             bCorrelation.setSelection(curStyle.isDrawCorrelations());
             sMaxSymSize.setSelection(curStyle.getMaximumSymbolSize());
             sDefBeamwidth.setSelection(curStyle.getDefaultBeamwidth());
             cFontSize.setText(String.valueOf(curStyle.getFontSize()));
             cSecondaryFontSize.setText(String.valueOf(curStyle.getSecondaryFontSize()));
             cSecondaryProperty.setItems(getSecondaryPropertyChoices());
             cMainProperty.setItems(getMainPropertyChoices());
             cSecondaryProperty.setText(curStyle.getSecondaryProperty());
             cMainProperty.setText(curStyle.getMainProperty());
             sIconOffset.setSelection(curStyle.getIconOffset());
 
             if(isNetwork){
                 lIconOffset.setVisible(false);
                 sIconOffset.setVisible(false);
                 if (isProbe){
                     changeToProbeNetworkStyle();
                 }
             } else {
                 changeToDriveStyle();
             }
         } finally {
         }
     }
 
     /**
      *
      */
     private void changeToProbeNetworkStyle() {
         lMainProperty.setText("Probe property"); //$NON-NLS-1$
         lFontSize.setText("Probe font size"); //$NON-NLS-1$
         lFillSite.setText("Probe fill"); //$NON-NLS-1$
         
     }
 
     /**
      * get array of possible site names
      * 
      * @return String[]
      */
     private String[] getMainPropertyChoices() {
         return new String[] {NeoStyleContent.DEF_NONE, NeoStyleContent.DEF_MAIN_PROPERTY, "lat", "lon"}; //$NON-NLS-1$ //$NON-NLS-2$
     }
 
     /**
      *get array of sector names
      * 
      * @return array
      */
     private String[] getSecondaryPropertyChoices() {
         List<String> result = new ArrayList<String>();
         result.add(NeoStyleContent.DEF_NONE);
         try {
             GeoNeo resource = getLayer().findGeoResource(GeoNeo.class).resolve(GeoNeo.class, null);
            String[] allFields = PropertyHeader.getPropertyStatistic(resource.getMainGisNode()).getAllFields("-main-type-");
             if (allFields != null) {
                 result.addAll(Arrays.asList(allFields));
             }
             Collections.sort(result);
             return result.toArray(new String[0]);
         } catch (IOException e) {
             // TODO Handle IOException
             e.printStackTrace();
             Collections.sort(result);
             return result.toArray(new String[0]);
         }
     }
 
     @Override
     public void focus(Layer layer1) {
         try {
             GeoNeo geoNeo = layer1.findGeoResource(NeoGeoResource.class).resolve(GeoNeo.class, null);
             isNetwork = geoNeo.getGisType() == GisTypes.NETWORK;
             if (isNetwork) {
                 GraphDatabaseService service = NeoServiceProviderUi.getProvider().getService();
                 //TODO now we store network type in gis node.
 //                Node mainNode = NeoUtils.getMainNodeFromGis(geoNeo.getMainGisNode(), service);
                 isProbe = NetworkTypes.PROBE.checkType(geoNeo.getMainGisNode(), service);
             } else {
                 isProbe = false;
             }
         } catch (IOException e) {
             // TODO Handle IOException
             e.printStackTrace();
             isNetwork = true;
             isProbe = false;
         }
         super.focus(layer1);
     }
     /**
      *
      */
     private void changeToDriveStyle() {
         //Hide site fill color
         lFillSite.setVisible(false);
         cEdFillSite.getButton().setVisible(false);
 
         //Change sector fill to 'fill' and reset layout
         labelFill.setText(Messages.Color_Fill_Drive);
         FormData formData = new FormData();
         formData.left = new FormAttachment(labelFill, 130);
         formData.top = new FormAttachment(labelFill, 10);
         cEdLine.getButton().setLayoutData(formData);
         formData = new FormData();
         formData.top = new FormAttachment(labelFill, 15);
         formData.left = new FormAttachment(2);
         labelLine.setLayoutData(formData);
 
         lSecondaryFontSize.setVisible(false);
         cSecondaryFontSize.setVisible(false);
 
         lFontSize.setText(Messages.Font_Size);
         lMainProperty.setText(Messages.Point_Property);
         lSecondaryProperty.setText(Messages.Measurement_Property);
     }
 
     @Override
     public void preApply() {
         super.preApply();
         curStyle.setFill(colorFromRGB(cEdFill.getColorValue()));
         curStyle.setSiteFill(colorFromRGB(cEdFillSite.getColorValue()));
         curStyle.setLabel(colorFromRGB(cEdLabel.getColorValue()));
         curStyle.setLine(colorFromRGB(cEdLine.getColorValue()));
         curStyle.setSmallestSymb(getSmallestSymb());
         curStyle.setSmallSymb(getSmallSymb());
         curStyle.setLabeling(getLabeling());
         curStyle.setFixSymbolSize(rButton2.getSelection());
         curStyle.setSymbolSize(getSymbolSize());
         curStyle.setSymbolTransparency(getSectorTransparency());
         curStyle.setFontSize(getLabelFontSize());
         curStyle.setSectorFontSize(getSectorFontSize());
         curStyle.setMaximumSymbolSize(sMaxSymSize.getSelection());
         curStyle.setDefaultBeamwidth(sDefBeamwidth.getSelection());
         curStyle.setIconOffset(sIconOffset.getSelection());
         curStyle.setMainProperty(cMainProperty.getText());
         curStyle.setSecondaryProperty(cSecondaryProperty.getText());
         curStyle.setIgnoreTransparency(bTransp.getSelection());
         curStyle.setDrawCorrelations(bCorrelation.getSelection());
         getStyleBlackboard().put(ID, curStyle);
     }
 
     /**
      * gets sector font size
      * 
      * @return font size
      */
     private Integer getSectorFontSize() {
         try {
             return Integer.parseInt(cSecondaryFontSize.getText());
         } catch (NumberFormatException e) {
             return NeoStyleContent.DEF_FONT_SIZE_SECTOR;
         }
     }
 
     /**
      * gets label (site for network) font size
      * 
      * @return font size
      */
     private Integer getLabelFontSize() {
         try {
             return Integer.parseInt(cFontSize.getText());
         } catch (NumberFormatException e) {
             return NeoStyleContent.DEF_FONT_SIZE;
         }
     }
 
     /**
      * @return
      */
     private Integer getSectorTransparency() {
         return tTransparency.getSelection();
     }
 
     /**
      * gets integer value of textfield tSymbolSize
      * 
      * @return
      */
     private Integer getSymbolSize() {
         return tSymbolSize.getSelection();
 
     }
 
     /**
      * gets integer value of textfield tLabeling
      * 
      * @return
      */
     private Integer getLabeling() {
         return tLabeling.getSelection();
 
     }
 
     /**
      * gets integer value of textfield tSmallSymb
      * 
      * @return
      */
     private Integer getSmallSymb() {
         return tSmallSymb.getSelection();
 
     }
 
     /**
      * gets integer value of textfield tSmallestSymb
      * 
      * @return
      */
     private Integer getSmallestSymb() {
         return tSmallestSymb.getSelection();
 
     }
 
     /**
      * gets color from RGB
      * 
      * @param colorValue - RGB value
      * @return
      */
     private Color colorFromRGB(RGB colorValue) {
         return new Color(colorValue.red, colorValue.green, colorValue.blue);
     }
 
     /**
      * gets RGB from color
      * 
      * @param color color value
      * @return
      */
     private RGB rgbFromColor(Color color) {
         return new RGB(color.getRed(), color.getGreen(), color.getBlue());
     }
 }
