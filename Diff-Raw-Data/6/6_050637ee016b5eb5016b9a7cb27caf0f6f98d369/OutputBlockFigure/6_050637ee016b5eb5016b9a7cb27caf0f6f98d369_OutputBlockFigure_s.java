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
 public class OutputBlockFigure extends CodeBlockFigure {
     private TextChangedListener txtchange; //used for user set text box
     private ActionListener updatelistener; //used for triggering a context menu update
 
     private OutputBlock SavedBlock = null;
 
     @Override
     protected void createModel() {
         if (SavedBlock == null) {
             accociatedcode = new OutputBlock();
         }
         else {
             accociatedcode = SavedBlock;
             SavedBlock = null;
         }
         txtchange = new TextChangedListener(this,(OutputBlock)accociatedcode);
         updatelistener = new UpdateListener(this);
     }
     
     @Override
     public OutputBlock getModel()
     {
         return (OutputBlock)accociatedcode;
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
 
         TextFigure port = new TextFigure(getModel().getDisplayedPort());
         port.setAttribute(SpecialAttributeKeys.MODIFY_LINK_CREATESCONTEXTMENU, getModel());
         port.setEditable(false);
         attrib.add(port);
 
         TextFigure seperator = new TextFigure(" := ");
         attrib.add(seperator);
 
         TextFigure value = new TextFigure(getModel().getValue());
         value.setAttribute(SpecialAttributeKeys.MODIFY_LINK_OUTPUT_BLOCK, getModel());
         value.addFigureListener(txtchange);
         attrib.add(value);
 
 
         update_base();
 
         this.changed();
 
     }
 
 
     //Event handler handles when text is changed in any of the boxes.
 
     protected class TextChangedListener implements FigureListener
     {
         OutputBlockFigure caller;
         OutputBlock accociatedcode;
 
         public TextChangedListener(OutputBlockFigure fig, OutputBlock code)
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
             OutputBlock edit = (OutputBlock)e.getFigure().getAttribute(SpecialAttributeKeys.MODIFY_LINK_OUTPUT_BLOCK);
             edit.setValue(((TextFigure)e.getFigure()).getText().trim());
 
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
         OutputBlockFigure _sender;
 
         public UpdateListener(OutputBlockFigure sender) {
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
         out.addAttribute("data_val", getModel().getValue());
 
         //writeAttributes(out); //export all attributes
     }
 
     @Override
     public void read(DOMInput in) throws IOException {
         readSavedBounds(in);
 
        String data = in.getAttribute("data_port", "UNDEFINED");
         String val = in.getAttribute("data_val", "UNDEFINED");
        if (!data.equals("UNDEFINED") && !val.equals("UNDEFINED")) {
             SavedBlock = new OutputBlock(readUID(in));
             SavedBlock.setValue(val);
         }
         
         //readAttributes(in);
     }
 
 }
