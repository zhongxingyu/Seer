 /********************************************************************************
  * <eCharacter> is a research project of the <e-UCM>
  *          research group.
  *
  *    Developed by: Alejandro Muñoz del Rey, Sergio de Luis Nieto and David González
  *    Ledesma.
  *    Under the supervision of Baltasar Fernández-Manjón and Javier Torrente
  * 
  *    Copyright 2012-2013 <e-UCM> research group.
  *  
  *     <e-UCM> is a research group of the Department of Software Engineering
  *          and Artificial Intelligence at the Complutense University of Madrid
  *          (School of Computer Science).
  *  
  *          C Profesor Jose Garcia Santesmases sn,
  *          28040 Madrid (Madrid), Spain.
  *  
  *          For more info please visit:  <http://character.e-ucm.es>, 
  *          <http://e-adventure.e-ucm.es> or <http://www.e-ucm.es>
  *  
  *  ****************************************************************************
  *      <eCharacter> is free software: you can 
  *      redistribute it and/or modify it under the terms of the GNU Lesser 
  *      General Public License as published by the Free Software Foundation, 
  *      either version 3 of the License, or (at your option) any later version.
  *  
  *      <eCharacter> is distributed in the hope that it 
  *      will be useful, but WITHOUT ANY WARRANTY; without even the implied 
  *      warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
  *      See the GNU Lesser General Public License for more details.
  *  
  *      You should have received a copy of the GNU Lesser General Public License
  *      along with <eCharacter>. If not, 
  *      see <http://www.gnu.org/licenses/>.
  ******************************************************************************/
 
 package es.eucm.echaracter.gui;
 
 import de.lessvoid.nifty.Nifty;
 import de.lessvoid.nifty.effects.Effect;
 import de.lessvoid.nifty.effects.EffectEventId;
 import de.lessvoid.nifty.elements.Element;
 import de.lessvoid.nifty.elements.render.ImageRenderer;
 import de.lessvoid.nifty.elements.render.TextRenderer;
 import es.eucm.echaracter.control.Control;
 import es.eucm.echaracter.i18n.I18N;
 import java.util.ArrayList;
 import java.util.List;
 
 public class FamilyStageBuilder {
     private static final int FAMILIES_PAGE = 3;
     private Nifty nifty;
     private I18N i18nGui; 
     private Control control;
     private String stageType, language;
     private int modelsPage, familyPage, familySelected;
     
     public FamilyStageBuilder(Nifty nifty, Control control,I18N i18nGui,String pathNoSel, String language){
         stageType = "modelScreen";
         this.nifty = nifty;
         this.control = control;
         modelsPage = 0;
         familyPage = 0;
         this.i18nGui = i18nGui;
         this.language = language;
         initModels(pathNoSel);
     }
     
     /*receives as input the lenguaje of the aplication
       and builds all the families and the pictures of models*/
     
     public void initModels(final String pathNoSel){
         nifty.getScreen(stageType).findElementByName("leftT").setVisible(false);
         nifty.getScreen(stageType).findElementByName("rightT").setVisible(false);
         nifty.getScreen(stageType).findElementByName("m").setVisible(false);
         nifty.getScreen(stageType).findElementByName("panel_screenright").setVisible(false);
         for(int i=0; i<FAMILIES_PAGE;i++){
             Element image = nifty.getScreen(stageType).findElementByName("t"+i);
             ImageRenderer imager = image.getRenderer(ImageRenderer.class);
             imager.setImage(nifty.getRenderEngine().createImage(pathNoSel, false));
         }
         familySelected = -1;
         this.showFamilyPage("0");
     }
     
     //Change the model's page hiding pictures of previous models or previous family
     
     public String changeCharacterPage(I18N i18nFamily,String steep){
         if(steep.equals("+")){
             modelsPage++;
         }
         if(steep.equals("-")){
             modelsPage--;
         }
         if(steep.equals("0")){
             modelsPage = 0;
         }
         Element image = nifty.getScreen(stageType).findElementByName("m");
         image.setVisible(true);
         ImageRenderer imager = image.getRenderer(ImageRenderer.class);
         String imagePath = control.getModelIconPath(control.getModelsLabel().get(modelsPage));
         if(imagePath!=null){
             imager.setImage(nifty.getRenderEngine().createImage(imagePath, false));
         }
         else{
             imager.setImage(nifty.getRenderEngine().createImage("assets/Interface/x.png", false));
         }
         if(modelsPage > 0){
             nifty.getScreen(stageType).findElementByName("leftT").setVisible(true);
         }
         else{
             nifty.getScreen(stageType).findElementByName("leftT").setVisible(false);
         }
         if((control.getNumModels() - modelsPage) > 1){
             nifty.getScreen(stageType).findElementByName("rightT").setVisible(true);
         }
         else{
             nifty.getScreen(stageType).findElementByName("rightT").setVisible(false);
         }
         return control.getModelFamilyPath(control.getModelsLabel().get(modelsPage));
     }
     
     public void showFamilyPage(String steep){
         ArrayList<String> families = control.getFamiliesID();
         if(steep.equals("+")){
             familyPage++;
         }
         if(steep.equals("-")){
             familyPage--;
         }
         if(steep.equals("0")){
             familyPage = 0;
         }
         for(int i=familyPage; i<families.size();i++){
             control.selectFamily(families.get(i));
             I18N i18nAux = new I18N(control.getLanguageFamilyPath(),language);
             if(i<FAMILIES_PAGE+familyPage){
                 int j= i-familyPage;
                 nifty.getScreen(stageType).findElementByName("t"+j).setVisible(true);
                 nifty.getScreen(stageType).findElementByName("description"+j).getRenderer(TextRenderer.class).setText(i18nAux.getString(control.getMetadataFamilyAbstract()));
                 nifty.getScreen(stageType).findElementByName("textPanel"+j).layoutElements();
                 nifty.getScreen(stageType).findElementByName("name"+j).getRenderer(TextRenderer.class).setText(i18nAux.getString(control.getMetadataFamilyName()));
                 nifty.getScreen(stageType).findElementByName("textPanel"+j).layoutElements();
                 
                 Element image = nifty.getScreen(stageType).findElementByName("f"+j);
                 image.setVisible(true);
                 ImageRenderer imager = image.getRenderer(ImageRenderer.class);
                 String imagePath = control.getIconPathFamily();
                 if(imagePath!=null){
                     imager.setImage(nifty.getRenderEngine().createImage(imagePath, false));
                 }
                 else{
                     imager.setImage(nifty.getRenderEngine().createImage("assets/Interface/x.png", false));
                 }
             }
         }
         for(int i=families.size();i<FAMILIES_PAGE;i++){
             nifty.getScreen(stageType).findElementByName("t"+i).setVisible(false);
         }
         if(familyPage > 0){
             nifty.getScreen(stageType).findElementByName("upT").setVisible(true);
         }
         else{
             nifty.getScreen(stageType).findElementByName("upT").setVisible(false);
         }
         if((familyPage + FAMILIES_PAGE) < families.size()){
             nifty.getScreen(stageType).findElementByName("downT").setVisible(true);
         }
         else{
             nifty.getScreen(stageType).findElementByName("downT").setVisible(false);
         }
     }
     
     public int selectFamily(String id, String pathNoSel, String pathSel){
         for(int i=0; i<FAMILIES_PAGE;i++){
             Element image = nifty.getScreen(stageType).findElementByName("t"+i);
             ImageRenderer imager = image.getRenderer(ImageRenderer.class);
             imager.setImage(nifty.getRenderEngine().createImage(pathNoSel, false));
         }
         Element image = nifty.getScreen(stageType).findElementByName("t"+id);
         ImageRenderer imager = image.getRenderer(ImageRenderer.class);
         imager.setImage(nifty.getRenderEngine().createImage(pathSel, false));
         nifty.getScreen(stageType).findElementByName("panel_screenright").setVisible(true);
         familySelected = Integer.parseInt(id)+familyPage;
         return familySelected;
     }
     
     public void showInfo(String family){
         int i = Integer.parseInt(family)+familyPage;
         ArrayList<String> families = control.getFamiliesID();
         control.selectFamily(families.get(i));
         I18N i18nAux = new I18N(control.getLanguageFamilyPath(),language);
         String description = "\\#FF0000#"+i18nGui.getString("idDescriptionFamily")+"\n"+i18nAux.getString(control.getMetadataFamilyDescription());
         String author = "\\#FF0000#"+i18nGui.getString("idAuthor")+"\n"+i18nAux.getString(control.getMetadataFamilyAuthor());
         String url;
         if(control.getMetadataFamilyURL()!=null){
             url = "\\#FF0000#"+i18nGui.getString("idUrl")+"\n"+i18nAux.getString(control.getMetadataFamilyURL());
             nifty.getScreen(stageType).findElementByName("familyUrlPopupText").getRenderer(TextRenderer.class).setText(url);
             nifty.getScreen(stageType).findElementByName("familyUrlPopupPanel").layoutElements();
             nifty.getScreen(stageType).findElementByName("familyUrlPopupPanel").setVisible(true);
         }
         else{
             nifty.getScreen(stageType).findElementByName("familyUrlPopupPanel").setVisible(false);
         }
         nifty.getScreen(stageType).findElementByName("familyDescriptionPopupText").getRenderer(TextRenderer.class).setText(description);
         nifty.getScreen(stageType).findElementByName("familyDescriptionPopupPanel").layoutElements();
         nifty.getScreen(stageType).findElementByName("familyAuthorPopupText").getRenderer(TextRenderer.class).setText(author);
         nifty.getScreen(stageType).findElementByName("familyAuthorPopupPanel").layoutElements();
         nifty.getScreen(stageType).findElementByName("familyNamePopupText").getRenderer(TextRenderer.class).setText(i18nAux.getString(control.getMetadataFamilyName()));
         nifty.getScreen(stageType).findElementByName("familyNamePopupPanel").layoutElements();
         nifty.getScreen(stageType).findElementByName("acceptText").getRenderer(TextRenderer.class).setText(i18nGui.getString("idAcept"));
         nifty.getScreen(stageType).findElementByName("accept").layoutElements();
         nifty.getScreen(stageType).getLayerElements().get(2).setVisible(true);
        if(familySelected!=-1){
            control.selectFamily(families.get(familySelected));
        }
     }
 }
 
