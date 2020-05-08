 package org.toj.dnd.irctoolkit.engine.command.map;
 
 import org.toj.dnd.irctoolkit.engine.command.IrcCommand;
 import org.toj.dnd.irctoolkit.engine.command.IrcCommand.CommandSegment;
 import org.toj.dnd.irctoolkit.engine.command.MapCommand;
 import org.toj.dnd.irctoolkit.exceptions.ToolkitCommandException;
 import org.toj.dnd.irctoolkit.map.MapModel;
 import org.toj.dnd.irctoolkit.map.Point;
 import org.toj.dnd.irctoolkit.token.Color;
 import org.toj.dnd.irctoolkit.util.AbbreviationUtil;
 import org.toj.dnd.irctoolkit.util.AxisUtil;
 
 @IrcCommand(command="place", args = {CommandSegment.NULLABLE_STRING, CommandSegment.STRING, CommandSegment.STRING})
 public class CreateMapObjectCommand extends MapCommand {
 
     private MapModel model;
     private Point dests;
 
     public CreateMapObjectCommand(Object[] args) {
         String model = (String) args[0];
         if (model != null) {
             MapModel o = context.getModelList().findModelByChOrDesc(model);
             if (o == null) {
                 o = context.getModelList().createNewModel(AbbreviationUtil.getIcon(model), model, Color.BLACK, null, 0);
             }
             this.model = o;
         }
         dests = new Point(AxisUtil.toNumber((String) args[1]),
                 AxisUtil.toNumber((String) args[2]));
     }
 
     @Override
     protected void doExecute() throws ToolkitCommandException {
         if (model == null && this.caller != null) {
            MapModel o = context.getModelList().findModelByChOrDesc(caller);
            if (o == null) {
                context.getModelList().createNewModel(AbbreviationUtil.getIcon(caller), caller, Color.BLACK, null, 0);
             }
         }
         if (model == null || dests == null) {
             return;
         }
         context.getCurrentMap().drawObjectsOntoGrid(new int[] {dests.getX()}, new int[] {dests.getY()}, model);
     }
 
     @Override
     protected boolean mapChanged() {
         return true;
     }
 
     @Override
     protected boolean modelChanged() {
         return true;
     }
 }
