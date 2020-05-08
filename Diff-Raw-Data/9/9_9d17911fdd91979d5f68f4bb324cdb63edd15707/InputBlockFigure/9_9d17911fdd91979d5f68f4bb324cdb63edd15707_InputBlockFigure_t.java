 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package plcedit;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseEvent;
 import java.awt.geom.Point2D.Double;
 import org.jhotdraw.draw.DrawingView;
 import org.jhotdraw.draw.FigureEvent;
 import org.jhotdraw.draw.FigureListener;
 import org.jhotdraw.draw.HorizontalLayouter;
 import org.jhotdraw.draw.TextFigure;
 import org.jhotdraw.draw.Figure;
 
 import org.jhotdraw.xml.DOMInput;
 import org.jhotdraw.xml.DOMOutput;
 import java.io.IOException;
 
 /**
  *
  * @author Vortex-5
  */
 public class InputBlockFigure extends CodeBlockFigure {
     private TextChangedListener txtchange; //used for user set text box
     private ActionListener updatelistener; //used for triggering a context menu update
 
     private InputBlock SavedBlock = null;
 
     @Override
     protected void createModel() {
         if (SavedBlock == null) {
             accociatedcode = new InputBlock();
         }
         else {
             accociatedcode = SavedBlock;
             SavedBlock = null;
         }
         txtchange = new TextChangedListener(this,(InputBlock)accociatedcode);
         updatelistener = new UpdateListener(this);
     }
 
     @Override
     public InputBlock getModel()
     {
         return (InputBlock)accociatedcode;
     }
 
     @Override
     protected void initialize_component() {
         update();
     }
 
     public void update()
     {
         this.willChange();
 
         createAttributeDisplay(); //create our display area
 
         attrib.setLayouter(new HorizontalLayouter());
 
         attrib.add(spacer);
 
         TextFigure storetype = new TextFigureWithClickEvent(((InputBlock)accociatedcode).getStoreObj().type.toDisplayString(), updatelistener);
         storetype.setEditable(false);
 
         //add link information so we can edit it
         storetype.setAttribute(SpecialAttributeKeys.MODIFY_LINK_CREATESCONTEXTMENU, ((InputBlock)accociatedcode).getStoreObj().type);
         attrib.add(storetype);
 
         TextFigure space = new TextFigure(" ");
         attrib.add(space);
 
         TextFigure var = new TextFigure(getModel().getVariable());
         var.setAttribute(SpecialAttributeKeys.MODIFY_LINK_INPUT_BLOCK, getModel());
         var.addFigureListener(txtchange);
         attrib.add(var);
 
         TextFigure seperator = new TextFigure(" := ");
         attrib.add(seperator);
 
         TextFigure port = new TextFigure(getModel().getDisplayedPort());
         port.setAttribute(SpecialAttributeKeys.MODIFY_LINK_CREATESCONTEXTMENU, getModel());
         port.setEditable(false);
         attrib.add(port);
 
         attrib.add(spacer);
 
 
         update_base();
 
         this.changed();
 
     }
 
 
       //Event handler handles when text is changed in any of the boxes.
 
     protected class TextChangedListener implements FigureListener
     {
         InputBlockFigure caller;
         InputBlock accociatedcode;
 
         public TextChangedListener(InputBlockFigure fig, InputBlock code)
         {
             caller = fig;
             accociatedcode = code;
         }
 
         public void areaInvalidated(FigureEvent e) {
         }
 
         public void attributeChanged(FigureEvent e) {
         }
 
         public void figureAdded(FigureEvent e) {
         }
 
         public void figureChanged(FigureEvent e) {
             InputBlock edit = (InputBlock)e.getFigure().getAttribute(SpecialAttributeKeys.MODIFY_LINK_INPUT_BLOCK);
             edit.setVariable(((TextFigure)e.getFigure()).getText().trim());
 
             caller.update();
         }
 
         public void figureHandlesChanged(FigureEvent arg0) {
         }
 
         public void figureRemoved(FigureEvent arg0) {
         }
 
         public void figureRequestRemove(FigureEvent arg0) {
         }
     }
 
 
     protected class UpdateListener implements ActionListener
     {
         InputBlockFigure _sender;
 
         public UpdateListener(InputBlockFigure sender) {
             _sender = sender;
         }
 
         public void actionPerformed(ActionEvent arg0) {
             _sender.update();
         }
     }
 
     @Override
     public boolean handleMouseClick(Double p, MouseEvent evt, DrawingView view) {
         try
         {
             Figure target = this.findFigureInside(p);
             target.handleMouseClick(p, evt, view);  //propigate the event downwards
         }
         catch (Exception ex)
         {
         }
 
         return super.handleMouseClick(p, evt, view);
     }
 
     @Override
     public void write(DOMOutput out) throws IOException {
         writeBoundingBox(out); //save our position data
 
         writeUID(out);
 
         //out.addAttribute("data_port", getModel().getDisplayedPort());

        out.addAttribute("data_type", getModel().getStoreObj().type.getSaveString());
         out.addAttribute("data_var", getModel().getVariable());
 
         //writeAttributes(out); //export all attributes
     }
 
     @Override
     public void read(DOMInput in) throws IOException {
         readSavedBounds(in);
 
         //String data = in.getAttribute("data_port", "UNDEFINED"); //not required we only have one output port
        String type = in.getAttribute("data_type", "UNDEFINED");
         String var = in.getAttribute("data_var", "UNDEFINED");
 
        if (!var.equals("UNDEFINED") && !type.equals("UNDEFINED")) {
             SavedBlock = new InputBlock(readUID(in));
            SavedBlock.getStoreObj().type.setFromSaveString(type);
             SavedBlock.setVariable(var);
         }
 
         //readAttributes(in);
     }
 }
