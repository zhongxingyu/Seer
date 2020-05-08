 package cubetech.CGame;
 
 import cubetech.Block;
 import cubetech.Game.SpawnEntity;
 import cubetech.collision.BlockModel;
 
 import cubetech.common.CVar;
 import cubetech.common.CVarFlags;
 import cubetech.common.Helper;
 import cubetech.gfx.CubeMaterial;
 
 import cubetech.gfx.ResourceManager;
 import cubetech.gfx.Sprite;
 import cubetech.gfx.SpriteManager;
 import cubetech.gfx.TextManager.Align;
 import cubetech.input.Input;
 import cubetech.input.Key;
 import cubetech.input.KeyEvent;
 import cubetech.input.KeyEventListener;
 import cubetech.input.MouseEvent;
 import cubetech.input.MouseEventListener;
 import cubetech.misc.Ref;
 import cubetech.spatial.SpatialQuery;
 import cubetech.ui.ButtonEvent;
 import cubetech.ui.CButton;
 import cubetech.ui.CCheckbox;
 import cubetech.ui.CComponent;
 import cubetech.ui.CContainer;
 import cubetech.ui.CContainer.Direction;
 import cubetech.ui.CImage;
 import cubetech.ui.CLabel;
 import cubetech.ui.CScrollPane;
 import cubetech.ui.CSpinner;
 import cubetech.ui.FlowLayout;
 import cubetech.ui.StashItemUI;
 import java.util.ArrayList;
 import java.util.EnumSet;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import org.lwjgl.input.Keyboard;
 import org.lwjgl.util.Color;
 import org.lwjgl.util.vector.Vector2f;
 import org.lwjgl.util.vector.Vector4f;
 
 /**
  *
  * @author mads
  */
 public class MapEditor implements KeyEventListener, MouseEventListener {
     public static final int EDITOR_LAYER = -10;
     CContainer sidepanel;
     CContainer menupanel;
 
     // Stash
     CContainer stashpanel;
     CContainer stashScrollCont;
     ButtonEvent stashButtonEvent = new ButtonEvent() {
         public void buttonPressed(CComponent button, MouseEvent evt) {
             if(evt.Button > 0)
                 removeFromStash((StashItemUI)button);
             else
                 copyBlockToCenter(((StashItemUI)button).getBlock());
         }
     };
 
     CVar edit_nearlayer;
     CVar edit_farlayer;
 
     CImage currentTexture;
     CLabel toolText;
     CLabel cursorText;
     CLabel centerLabel;
     CLabel extentLabel;
     CCheckbox gridBox;
     Block selectedBlock = null;
     ArrayList<Block> selectedBlocks = new ArrayList<Block>();
     Block selectedBlockCopy = null;
     Tool tool = Tool.SELECT;
     Tool revertTool = null;
     int gridSpacing = 16;
 
     CContainer toolContainer = null;
 
     // select tool
     Vector2f select_point = null;
     ArrayList<Block> select_queue = new ArrayList<Block>();
     int select_index = 0;
 
     // move tool
     boolean snapToGrid = false;
     boolean move_dragging = false;
     Vector2f dragStart = null;
     Vector2f dragEnd = null;
 
     // Scale tool
     boolean scale_dragging = false;
     boolean scale_top = false;
     //Corner scale_corner = Corner.TOPLEFT;
 
     // Rotate tool
     boolean rotate_dragging = false;
     float rotate_startAngle = 0f;
     float rotate_endAngle = 0f;
     CLabel rotate_label = null;
 
     // entity selector
     CContainer entityCont = null;
 
     CContainer popupContainer = null;
 
     // material selector
     CContainer matSelect = null;
     CContainer texContainer = null;
     CScrollPane scrollPane = null;
     boolean mat_show = false;
 
     // copy tool
     boolean copy_dragging = false;
 
     private BlockModel selectedModel = null;
     private boolean select_model = false;
 
     ArrayList<SpawnEntity> entities = null;
     boolean showEntities = false;
 
     enum Tool {
         SELECT,
         RESIZE,
         MOVE,
         ROTATE,
         COPY,
         TEXMOVE,
         TEXSCALE,
         NEWBLOCK,
         NEWENTITY
     }
     
     public MapEditor() {
         initUI();
         Ref.Input.SetKeyCatcher(Ref.Input.GetKeyCatcher() | Input.KEYCATCH_CGAME);
         selectTool(Tool.SELECT);
         edit_nearlayer = Ref.cvars.Get("edit_nearlayer", "1", EnumSet.of(CVarFlags.NONE));
         edit_farlayer = Ref.cvars.Get("edit_farlayer", "100", EnumSet.of(CVarFlags.NONE));
 
         entities =  Ref.game.spawnEntities.getList();
     }
 
     public void setGridSpacing(int units) {
         if(units < 1)
             units = 1;
         gridSpacing = units;
         gridBox.setText("Grid: " + units + "u");
     }
 
     private boolean isBlockInVisibleLayer(Block b) {
         int layer = b.getLayer();
 
         if(layer > -edit_nearlayer.iValue || layer < -edit_farlayer.iValue)
             return false;
         return true;
     }
 
     public void Render() {
         sidepanel.Render(new Vector2f());
         menupanel.Render(new Vector2f());
 
         stashpanel.Render(new Vector2f());
 
         if(mat_show)
             matSelect.Render(new Vector2f());
 
         if(popupContainer != null)
             popupContainer.Render(new Vector2f());
 
         if(edit_nearlayer.modified) {
             int val = edit_nearlayer.iValue+2;
 //            if(val < EDITOR_LAYER+1)
 //                val = 10;
             edit_nearlayer.modified = false;
 //            EDITOR_LAYER = -val;
         }
 
         if(showEntities) {
             for (SpawnEntity spawnEntity : entities) {
                 Block b = spawnEntity.getBlock();
                 b.Render();
             }
         }
 
         selection_temp_mins.set(Integer.MAX_VALUE, Integer.MAX_VALUE);
         selection_temp_maxs.set(Integer.MIN_VALUE, Integer.MIN_VALUE);
         renderSelectedModel();
         renderSelectedBlock();
 
         // Multiselect
         if(isMultiSelecting())
             renderAllSelection();
 
         if(gridBox.isSelected())
             renderGrid();
     }
 
     private boolean isMultiSelecting() {
         return ((selectedModel != null && selectedBlock != null) || selectedBlocks.size() > 1);
     }
 
     private int numMultiSelect() {
         int num = selectedBlocks.size();
         if(num == 0 && selectedBlock != null)
             num++;
         if(selectedModel != null)
             num++;
         return num;
     }
 
     private void updateUIBlock(Block b) {
         if(b != null) {
             currentTexture.setMaterial(b.Material);
 //            currentTexture.setTex(b.Texture);
 //            currentTexture.setTexoffset(b.TexOffset);
 //            currentTexture.setTexsize(b.TexSize);
             Vector2f center = b.GetCenter();
             centerLabel.setText("Center:     \nx: "+center.x+"\ny: " + center.y + "\nz: " + b.getLayer());
             extentLabel.setText("Extent:     \nx: " + b.GetExtents().x + "\ny: " + b.GetExtents().y + "\nH: " + b.Handle);
         } else {
 //            currentTexture.setTex(Ref.ResMan.getWhiteTexture());
             centerLabel.setText("Center:     \nx: N/A\ny: N/A\nz: N/A");
             extentLabel.setText("Extent:     \nx: N/A\ny: N/A");
         }
     }
 
     private void deselectBlock(Block b) {
         if(Ref.Input.IsKeyPressed(Keyboard.KEY_LSHIFT)) {
             if(!selectedBlocks.isEmpty()) {
                 selectedBlocks.remove(b);
 //                for (Block block : selectedBlocks) {
 //                    if(b == block) {
 //                        selectedBlocks.remove(b);
 //                        break;
 //                    }
 //                }
             }
             if(selectedBlock == b) {
                 if(selectedBlocks.size() < 1)
                 {
                     selectBlock(null);
                     return;
                 }
                 selectedBlock = selectedBlocks.get(selectedBlocks.size()-1);
 
                 if(selectedBlocks.size() == 1)
                     selectedBlocks.clear();
             }
         }
     }
 
     
 
     private void selectModel(BlockModel model) {
         selectedModel = model;
         
         if(model == null) {
             // remove from ui
             select_model = false;
             return;
         }
         
         select_model = true;
         // If not multiselecting, deselect block
         if(!Ref.Input.IsKeyPressed(Keyboard.KEY_LSHIFT))
             selectBlock(null);
         
         if(((selectedBlock != null) || selectedBlocks.size() > 1))
             setMiscCont(true, false, null, false, false);
         else
             setMiscCont(false, false, null, true, true);
     }
 
     private void selectBlock(Block b) {
         boolean doMultiselect = Ref.Input.IsKeyPressed(Keyboard.KEY_LSHIFT);
         // Clear model if b is not null
         if(select_model && b != null && !doMultiselect) {
             selectModel(null);
             select_model = false;
         }
         if(doMultiselect && b != null && selectedBlock != null) {
             if(selectedBlocks.isEmpty())
                 selectedBlocks.add(selectedBlock);
             selectedBlocks.add(b);
         }
         else {
             selectedBlocks.clear();
         }
 
         if(doMultiselect && ((selectedBlock == null && select_model && b != null) || selectedBlocks.size() > 1))
             setMiscCont(true, true, b, false, false);
         else
             setMiscCont(false, true, b, false, true);
         selectedBlock = b;
         updateUIBlock(b);
 
     }
 
     private void stashBlock(Block b) {
         if(b == null)
             return;
         for (int i= 0; i < stashScrollCont.getComponentCount(); i++) {
             // TODO: Check for equal values, not equal instance
             if(((StashItemUI)stashScrollCont.getComponent(i)).getBlock().equals(b, true))
                 return;
         }
         stashScrollCont.addComponent(new StashItemUI(b, stashButtonEvent));
         stashScrollCont.doLayout();
         stashpanel.doLayout();
     }
 
     private void removeFromStash(StashItemUI item) {
         stashScrollCont.removeComponent(item);
         stashScrollCont.doLayout();
     }
 
     private void selectTool(Tool tool) {
         if(tool == null)
             return;
         if(tool == revertTool)
             revertTool = null;
         this.tool = tool;
         toolText.setText("Tool: " + tool.toString());
 
         
 
         switch(tool) {
             case MOVE:
                 if(selectedBlock == null && !select_model) {
                     selectTool(Tool.SELECT); // Nothing selected
                     return;
                 }
 
                 // Clear move
                 dragStart = null;
                 move_dragging = false;
 
                 addToolContainer();
                 
                 if(!select_model) {
                     CContainer moveCont = new CContainer(new FlowLayout(true, false, true));
                     moveCont.addComponent(new CLabel("Adjust layer:"));
                     moveCont.addComponent(new CSpinner(-selectedBlock.getLayer(), new ButtonEvent() {
                         public void buttonPressed(CComponent button, MouseEvent evt) {
                             selectedBlock.setLayer(-((CSpinner)button).getValue());
                         }
                     }));
                     toolContainer.addComponent(moveCont);
                     moveCont.doLayout();
                 }
                 
                 CCheckbox xbox = new CCheckbox("Snap to grid", new ButtonEvent() {
                     public void buttonPressed(CComponent button, MouseEvent evt) {
                         snapToGrid = ((CCheckbox)button).isSelected();
                     }
                 });
                 xbox.setSelected(snapToGrid);
                     
                 toolContainer.addComponent(xbox);
                 toolContainer.doLayout();
                 toolContainer.doLayout();
                 break;
             case COPY:
                 if(selectedBlock == null) {
                     selectTool(Tool.SELECT); // Nothing selected
                     return;
                 }
 
                 // Clear move
                 dragStart = null;
                 move_dragging = false;
 
                 addToolContainer();
                 xbox = new CCheckbox("Snap to grid", new ButtonEvent() {
                     public void buttonPressed(CComponent button, MouseEvent evt) {
                         snapToGrid = ((CCheckbox)button).isSelected();
                     }
                 });
                 xbox.setSelected(snapToGrid);
 
                 toolContainer.addComponent(xbox);
                 toolContainer.doLayout();
                 toolContainer.doLayout();
                 break;
             case RESIZE:
                 if(selectedBlock == null) {
                     selectTool(Tool.SELECT); // Nothing selected
                     return;
                 }
 
                 // Clear state
                 dragStart = dragEnd = null;
                 scale_dragging = false;
                 
                 addToolContainer();
                 xbox = new CCheckbox("Snap to grid", new ButtonEvent() {
                     public void buttonPressed(CComponent button, MouseEvent evt) {
                         snapToGrid = ((CCheckbox)button).isSelected();
                     }
                 });
                 xbox.setSelected(snapToGrid);
 
                 toolContainer.addComponent(xbox);
                 toolContainer.doLayout();
                 toolContainer.doLayout();
 
                 break;
             case ROTATE:
                 if(selectedBlock == null) {
                     selectTool(Tool.SELECT); // Nothing selected
                     return;
                 }
                 addToolContainer();
                 if(rotate_label == null)
                     rotate_label = new CLabel("Angle: " + (int)(selectedBlock.getAngle() * (180/Math.PI)));
                 else
                     rotate_label.setText("Angle: " + (int)(selectedBlock.getAngle() * (180/Math.PI)));
                 toolContainer.addComponent(rotate_label);
                 toolContainer.doLayout();
                 toolContainer.doLayout();
                 //clearToolContainer();
                 break;
             case SELECT:
                 select_point = null;
                 clearToolContainer();
                 break;
             default:
                 clearToolContainer();
                 break;
         }
     }
 
     private void MoveBlock(Block b, Vector2f newposition) {
         b.SetCentered(newposition, b.GetExtents());
     }
 
     private Block copyBlock(Block b) {
         Block newb = Ref.cm.cm.AddBlock();
         newb.SetCentered(b.GetCenter(), b.GetExtents());
         newb.Material = b.Material;
         newb.Collidable = b.Collidable;
         newb.SetAngle(b.getAngle());
         newb.setLayer(b.getLayer()-1);
         return newb;
     }
 
     private Block copyBlockToCenter(Block b) {
         Block newb = Ref.cm.cm.AddBlock();
         
         newb.SetCentered(Ref.cgame.cg.refdef.Origin, b.GetExtents());
         newb.Material = b.Material;
         newb.Collidable = b.Collidable;
         newb.SetAngle(b.getAngle());
         newb.setLayer(b.getLayer());
         selectBlock(newb);
         return newb;
     }
 
 
     private void selectMaterial(Block b, CubeMaterial mat) {
         if(b != null) {
             b.setMaterial(mat);
 //            b.Texture = mat.getTexture();
 //            b.TexOffset = mat.getTextureOffset(0);
 //            b.TexSize = mat.getTextureSize();
         }
         updateUIBlock(selectedBlock);
     }
 
     private void deleteModel(BlockModel model) {
         if(model == null)
             return;
 
         if(isMultiSelecting())
             return;
 
         selectModel(null);
 
 
         Block[] tmpBlock = model.blocks.toArray(new Block[model.blocks.size()]);
 
         for (Block block : tmpBlock) {
             block.Remove();
         }
         if(model.entityNum > 0)
             System.out.println("deleteModel warning: The model was used by an entity.");
     }
 
     private void deleteBlock(Block b) {
         if(b == null)
             return;
 
         if(isMultiSelecting() && numMultiSelect() > 2)
         {
             // be careful not to deselect everything
             selectedBlocks.remove(b);
             if(selectedBlock == b) {
                 b = selectedBlocks.get(selectedBlocks.size()-1);
                 selectedBlock = b;
                 updateUIBlock(b);
             }
         } else {
             if(selectedBlock == b)
                 selectBlock(null);
             b.Remove();
         }
     }
 
     private void rotateBlock(Block b, float angle) {
         // Convert to degrees, cut decimal
         int deg_angle = (int)(angle * (180/Math.PI))%360;
         // Conert back to radians
         angle = (float) (deg_angle * (Math.PI / 180));
         b.SetAngle(angle);
     }
 
     private void scaleBlock(Block b, Vector2f newExtent) {
         b.setHalfHeight(newExtent.y);
         b.setHalfWidth(newExtent.x);
         
     }
 
     private void clearToolContainer() {
         if(toolContainer != null) {
             sidepanel.removeComponent(toolContainer);
             toolContainer = null;
         }
     }
 
     private void addToolContainer() {
         clearToolContainer();
         toolContainer = new CContainer(new FlowLayout(false, true, false));
         sidepanel.addComponent(toolContainer);
     }
 
     private void toolMouseEvent(MouseEvent evt) {
         Vector2f gameCoords = OrgCoordsToGameCoords(evt.Position);
         cursorText.setText("Cursor: [" + (int)gameCoords.x + ", " + (int)gameCoords.y + "]");
         //Tool tempTool = tool;
         
         switch(tool) {
             case SELECT:
                 selectMouseEvent(evt);
                 break;
             case MOVE:
                 moveMouseEvent(evt);
                 break;
             case RESIZE:
                 resizeMouseEvent(evt);
                 break;
             case ROTATE:
                 rotateMouseEvent(evt);
                 break;
             case COPY:
                 copyMouseEvent(evt);
                 break;
             default:
                 System.out.println("Unimplemented tool: " + tool);
                 break;
         }
     }
 
     private void rotateMouseEvent(MouseEvent evt) {
         Vector2f gameCoords = OrgCoordsToGameCoords(evt.Position);
         if(!rotate_dragging) {
             if(evt.Button == 0 && evt.Pressed) {
                 // Initiate
                 selectedBlockCopy = selectedBlock.clone();
                 selectedBlockCopy.setLayer(selectedBlockCopy.getLayer()+1);
                 dragStart = gameCoords;
                 Vector2f center = selectedBlock.GetCenter();
                 rotate_dragging = true;
                 rotate_startAngle = (float) (Math.atan2(dragStart.y - center.y, dragStart.x - center.x) + Math.PI / 2f) - selectedBlock.getAngle();
                 rotate_endAngle = selectedBlock.getAngle();
                 //rotate_endAngle = rotate_startAngle;
             }
         } else {
             dragEnd = gameCoords;
             Vector2f center = selectedBlock.GetCenter();
             rotate_endAngle = (float) (Math.atan2(dragEnd.y - center.y, dragEnd.x - center.x) + Math.PI / 2f);
             rotate_endAngle -= rotate_startAngle;
             if(rotate_label != null)
                 rotate_label.setText("Angle: " + (int)(rotate_endAngle * (180/Math.PI))%360);
             selectedBlockCopy.SetAngle(rotate_endAngle);
             updateUIBlock(selectedBlockCopy);
             if(evt.Button == 0 && !evt.Pressed) {
                 // Apply
                 rotate_dragging = false;
                 selectedBlockCopy = null;
                 rotateBlock(selectedBlock, rotate_endAngle);
                 rotate_label.setText("Angle: " + (int)(selectedBlock.getAngle() * (180/Math.PI))%360);
                 selectBlock(selectedBlock);
                 selectTool(revertTool);
             }
         }
     }
 
     // Returns true if source vector is inside the sphere defined by the target
     // and radius
     private boolean nearVector(Vector2f source, Vector2f target, float radius) {
         if(source.x >= target.x - radius && source.x <= target.x + radius
                 && source.y >= target.y - radius && source.y <= target.y + radius)
             return true;
         return false;
     }
 
     private void resizeMouseEvent(MouseEvent evt) {
         Vector2f gameCoords = OrgCoordsToGameCoords(evt.Position);
         if(!scale_dragging) {
             if(evt.Button == 0 && evt.Pressed) {
                 // Figure out corner
                 Vector2f extent = selectedBlock.GetExtents();
                 Vector2f axis[] = selectedBlock.GetAxis();
                 Vector2f bCenter = selectedBlock.GetCenter();
                 Vector2f rightCenter = new Vector2f(bCenter.x + axis[0].x * extent.x, bCenter.y + axis[0].y * extent.x);
                 Vector2f topCenter = new Vector2f(bCenter.x + axis[1].x * extent.y, bCenter.y + axis[1].y * extent.y);
 
                 if(nearVector(gameCoords, rightCenter, 1f)) {
                     scale_top = false;
                     scale_dragging = true;
                     
                 } else if(nearVector(gameCoords, topCenter, 1f)) {
                     scale_top = true;
                     scale_dragging = true;
                 }
 
                 if(scale_dragging) {
                     dragStart = gameCoords;
                     dragEnd = gameCoords;
                     selectedBlockCopy = selectedBlock.clone();
                     selectedBlockCopy.setLayer(selectedBlockCopy.getLayer()+1);
                 }
 
                 
             }
         } else if(scale_dragging) {
             dragEnd = gameCoords;
             if(snapToGrid) {
                 applyGrid(dragEnd);
             }
             Vector2f bCenter = selectedBlock.GetCenter();
             Vector2f delta = new Vector2f(dragEnd.x - bCenter.x, dragEnd.y - bCenter.y);
             float len = delta.length();
             if(scale_top)
                 selectedBlockCopy.setHalfHeight(len);
             else
                 selectedBlockCopy.setHalfWidth(len);
 
             updateUIBlock(selectedBlockCopy);
 
             if(evt.Button == 0 && !evt.Pressed) {
                 scale_dragging = false;
                 Vector2f finalExtent = selectedBlockCopy.GetExtents();
                 scaleBlock(selectedBlock, finalExtent);
                 selectedBlockCopy = null;
                 selectBlock(selectedBlock);
                 // TODO
                 selectTool(revertTool);
             }
         }
     }
 
     private void moveMouseEvent(MouseEvent evt) {
         Vector2f gameCoords = OrgCoordsToGameCoords(evt.Position);
         if(!move_dragging) {
             if(evt.Button == 0 && evt.Pressed) {
                 if(select_model && selectedModel != null) {
                     if(selectedModel.intersects(gameCoords)) {
                         // initate model move
                         move_dragging = true;
                         dragStart = new Vector2f(gameCoords.x - selectedModel.center.x, gameCoords.y - selectedModel.center.y);
                     }
                 } else {
                     if(selectedBlock != null && selectedBlock.Intersects(gameCoords)) {
                         // Intiate block move
                         selectedBlockCopy = selectedBlock.clone();
                         selectedBlockCopy.setLayer(selectedBlockCopy.getLayer()+1);
                         move_dragging = true;
                         dragStart = new Vector2f(gameCoords.x - selectedBlock.GetCenter().x, gameCoords.y - selectedBlock.GetCenter().y);
                     }
                 }
             }
         } else if(move_dragging) {
             
             if(snapToGrid) {
                 dragEnd = gameCoords;
                 applyGrid(dragEnd);
             } else if(dragStart != null) {
                 if(dragEnd == null)
                     dragEnd = new Vector2f();
                 dragEnd.x = gameCoords.x - dragStart.x;
                 dragEnd.y = gameCoords.y - dragStart.y;
             }
             if(select_model && selectedModel != null)
                 selectedModel.moveTo(dragEnd);
             else if(selectedBlockCopy != null) {
                 selectedBlockCopy.SetCentered(dragEnd, selectedBlockCopy.GetExtents());    
                 updateUIBlock(selectedBlockCopy);
             }
             
             if(evt.Button == 0 && !evt.Pressed) {
                 // Finalize move
                 move_dragging = false;
                 selectedBlockCopy = null;
                 if(!select_model) {
                     MoveBlock(selectedBlock, dragEnd);
                     selectBlock(selectedBlock);
                 }
                 selectTool(revertTool);
             }
         }
     }
 
     private void copyMouseEvent(MouseEvent evt) {
         Vector2f gameCoords = OrgCoordsToGameCoords(evt.Position);
         if(!copy_dragging) {
             if(evt.Button == 0 && evt.Pressed) {
                 if(selectedBlock.Intersects(gameCoords)) {
                     // Intiate
                     selectedBlockCopy = selectedBlock.clone();
                     selectedBlockCopy.setLayer(selectedBlockCopy.getLayer()+1);
                     copy_dragging = true;
                     dragStart = new Vector2f(gameCoords.x - selectedBlock.GetCenter().x, gameCoords.y - selectedBlock.GetCenter().y);
                 }
             }
         } else if(copy_dragging) {
 
             if(snapToGrid) {
                 dragEnd = gameCoords;
                 applyGrid(dragEnd);
             } else {
                 if(dragEnd == null)
                     dragEnd = new Vector2f();
                 dragEnd.x = gameCoords.x - dragStart.x;
                 dragEnd.y = gameCoords.y - dragStart.y;
             }
             selectedBlockCopy.SetCentered(dragEnd, selectedBlockCopy.GetExtents());
 
             updateUIBlock(selectedBlockCopy);
             if(evt.Button == 0 && !evt.Pressed) {
                 // Finalize move
                 copy_dragging = false;
                 
                 selectBlock(copyBlock(selectedBlockCopy));
                 selectedBlockCopy = null;
                 //MoveBlock(selectedBlock, dragEnd);
                 //selectBlock(selectedBlock);
                 selectTool(revertTool);
             }
         }
     }
 
     private SpawnEntity hitEntity(Vector2f pos) {
         for (SpawnEntity spawnEntity : entities) {
             if(spawnEntity.getBlock().Intersects(pos))
                 return spawnEntity;
         }
         return null;
     }
 
     // Select tool got a mouse event
     
     private void selectMouseEvent(MouseEvent evt) {
         Vector2f gameCoords = OrgCoordsToGameCoords(evt.Position);
 //        toolText.setText("Tool: Select");
 
         if(evt.Button != 0 || !evt.Pressed)
             return;
 
         if(select_point != null && select_queue.size() > 1 && select_point.x >= gameCoords.x - 1 && select_point.x <= gameCoords.x + 1
                 && select_point.y >= gameCoords.y - 1 && select_point.y <= gameCoords.y + 1)
         {
             // Keep results
             select_index++;
             if(Ref.Input.IsKeyPressed(Keyboard.KEY_LSHIFT))
                 deselectBlock(select_queue.get(select_index-1));
             if(select_index >= select_queue.size())
                 select_index = 0;
             
 //            select_index--;
             Block block = select_queue.get(select_index);
             if(block.isModel())
                 selectModel(block.getModel());
             else
                 selectBlock(block);
             return;
         }
 
         select_point = gameCoords;
         select_index = 0;
         select_queue.clear();
 
         SpawnEntity spEnt = hitEntity(gameCoords);
         if(spEnt != null)
             select_queue.add(spEnt.getBlock());
 
         Block b = null;
         
         SpatialQuery q = Ref.spatial.Query(gameCoords.x, gameCoords.y, gameCoords.x, gameCoords.y);
         while((b=(Block)q.ReadNext()) != null) {
             if(b.Intersects(gameCoords)) {
                 if(isBlockInVisibleLayer(b))
                     select_queue.add(b);
             }
         }
         
 
         if(select_queue.size() > 0) {
 
             if(Ref.Input.IsKeyPressed(Keyboard.KEY_LSHIFT)) {
                 boolean select = true;
                 if(isBlockSelected(select_queue.get(0)))
                     select = false;
                 for (Block block : select_queue) {
                     if(select) {
                         if(block.isModel())
                             selectModel(block.getModel());
                         else
                             selectBlock(block);
                     } else
                         deselectBlock(block);
                 }
                 select_queue.clear();
                 //deselectBlock(select_queue.get(select_index));
             }
             else {
                 b = select_queue.get(select_index);
                 if(b.isModel())
                     selectModel(b.getModel());
                 else
                     selectBlock(b);
             }
         } else if(!Ref.Input.IsKeyPressed(Keyboard.KEY_LSHIFT)) {
             selectBlock(null);
             select_point = null;
         }
     }
 
     private boolean isBlockSelected(Block b) {
         if(b == selectedBlock)
             return true;
         return (selectedBlocks.contains(b));
     }
 
     private void renderSelectedModel() {
         if(!select_model || selectedModel == null)
             return; // no model selected
 
         // Grow multiselect area
         Helper.AddPointToBounds(selectedModel.mins, selection_temp_mins, selection_temp_maxs);
         Helper.AddPointToBounds(selectedModel.maxs, selection_temp_mins, selection_temp_maxs);
 
         for (Block b: selectedModel.blocks) {
             renderHighlightBlock(b.GetCenter(), b.getAbsExtent(), EDITOR_LAYER, null);
         }
 
        // Vector2f.sub(maxs, mins, tempAbsSize);
         Sprite spr = Ref.SpriteMan.GetSprite(SpriteManager.Type.GAME);
         spr.Set(selectedModel.mins, selectedModel.size, Ref.ResMan.getWhiteTexture(), null,null);
         spr.SetColor(122,255,35,80);
         spr.SetDepth(EDITOR_LAYER);
 
 
         
         switch(tool) {
             case MOVE:
                 spr = Ref.SpriteMan.GetSprite(SpriteManager.Type.GAME);
                 spr.Set(selectedModel.center.x, selectedModel.center.y, 6f, Ref.ResMan.LoadTexture("data/particle.png"));
                 spr.SetDepth(EDITOR_LAYER-1);
                 spr.SetColor(255, 255, 255, 100);
                 spr = Ref.SpriteMan.GetSprite(SpriteManager.Type.GAME);
                 spr.Set(selectedModel.center.x, selectedModel.center.y, 4f, Ref.ResMan.LoadTexture("data/edit_move.png"));
                 spr.SetDepth(EDITOR_LAYER);
                 break;
         }
     }
 
     // render full selection area
     private void renderAllSelection() {
         Vector2f selectionSize = new Vector2f();
         Vector2f.sub(selection_temp_maxs, selection_temp_mins, selectionSize);
         Sprite spr = Ref.SpriteMan.GetSprite(SpriteManager.Type.GAME);
         spr.Set(selection_temp_mins, selectionSize, Ref.ResMan.getWhiteTexture(), null,null);
         spr.SetColor(28,50,210,100);
         spr.SetDepth(EDITOR_LAYER);
     }
 
 
 
     static Color defaultColor = new Color(210,210,210,255);
     public static void renderHighlightBlock(Vector2f center, Vector2f absExtent, float layer, Color color) {
         if(color == null)
             color = defaultColor;
 
         float lineWidth = 0.5f;
         float halfLineWidth = lineWidth * 0.5f;
         
         Sprite spr = Ref.SpriteMan.GetSprite(SpriteManager.Type.GAME);
         spr.Set(new Vector2f(center.x - absExtent.x - halfLineWidth, center.y - absExtent.y - halfLineWidth),
                 new Vector2f(absExtent.x * 2f + lineWidth, lineWidth), Ref.ResMan.getWhiteTexture(), null,null);
         spr.SetDepth(layer);
         spr.SetColor(color);
         
         spr = Ref.SpriteMan.GetSprite(SpriteManager.Type.GAME);
         spr.Set(new Vector2f(center.x - absExtent.x - halfLineWidth, center.y + absExtent.y - halfLineWidth),
                 new Vector2f(absExtent.x * 2f + lineWidth, lineWidth), Ref.ResMan.getWhiteTexture(), null,null);
         spr.SetDepth(layer);
         spr.SetColor(color);
 
         spr = Ref.SpriteMan.GetSprite(SpriteManager.Type.GAME);
         spr.Set(new Vector2f(center.x - absExtent.x - halfLineWidth, center.y - absExtent.y - halfLineWidth),
                 new Vector2f(lineWidth, absExtent.y * 2f + lineWidth), Ref.ResMan.getWhiteTexture(), null,null);
         spr.SetDepth(layer);
         spr.SetColor(color);
 
         spr = Ref.SpriteMan.GetSprite(SpriteManager.Type.GAME);
         spr.Set(new Vector2f(center.x + absExtent.x - halfLineWidth, center.y - absExtent.y - halfLineWidth),
                 new Vector2f(lineWidth, absExtent.y * 2f + lineWidth), Ref.ResMan.getWhiteTexture(), null,null);
         spr.SetDepth(layer);
         spr.SetColor(color);
     }
 
     
 
     private Vector2f selection_temp_mins = new Vector2f();
     private Vector2f selection_temp_maxs = new Vector2f();
 
     private void renderSelectedBlock() {
         if(selectedBlock == null)
             return;
 
         Sprite spr = null;
         
         // Draw white border
         Vector2f bCenter = new Vector2f();
         if(!selectedBlocks.isEmpty()) {
             Vector2f point = new Vector2f();
             for (Block block : selectedBlocks) {
                 Vector2f blockCenter = block.GetCenter();
                 Vector2f blockAbsExtent = block.getAbsExtent();
 
                 // Grow selection area
                 Vector2f.add(blockCenter, blockAbsExtent, point);
                 Helper.AddPointToBounds(point, selection_temp_mins, selection_temp_maxs);
                 Vector2f.sub(blockCenter, blockAbsExtent, point);
                 Helper.AddPointToBounds(point, selection_temp_mins, selection_temp_maxs);
 
                 // render block selection
                 renderHighlightBlock(blockCenter, blockAbsExtent, EDITOR_LAYER, null);
 
                 
             }
             Vector2f selectionSize = new Vector2f();
             Vector2f.sub(selection_temp_maxs, selection_temp_mins, selectionSize);
             selectionSize.scale(0.5f);
             Vector2f.add(selection_temp_mins, selectionSize, selectionSize);
             bCenter.set(selectionSize);
         } else {
             spr = Ref.SpriteMan.GetSprite(SpriteManager.Type.GAME);
             spr.Set(selectedBlock.GetCenter().x, selectedBlock.GetCenter().y, 1f, Ref.ResMan.LoadTexture("data/particle.png"));
             spr.SetColor(200, 200, 200, 200);
             spr.SetDepth(EDITOR_LAYER);
 
             Vector2f point = new Vector2f();
             Vector2f.add(selectedBlock.GetCenter(), selectedBlock.getAbsExtent(), point);
             Helper.AddPointToBounds(point, selection_temp_mins, selection_temp_maxs);
             Vector2f.sub(selectedBlock.GetCenter(), selectedBlock.getAbsExtent(), point);
             Helper.AddPointToBounds(point, selection_temp_mins, selection_temp_maxs);
 
             renderHighlightBlock(selectedBlock.GetCenter(), selectedBlock.getAbsExtent(), EDITOR_LAYER, null);
 
             bCenter.set(selectedBlock.GetCenter());
         }
         
         //spr.Set(selectedBlock.getPosition(), absSize, Ref.ResMan.getWhiteTexture(), selectedBlock.TexOffset, selectedBlock.TexSize);
         //spr.SetAngle(selectedBlock.getAngle());
 
         // Tool specific rendering
         switch(tool) {
             case ROTATE:
                 if(selectedBlockCopy != null && rotate_dragging) {
 //                    selectedBlockCopy.SetCentered(dragEnd, selectedBlockCopy.GetExtents());
                     
                     selectedBlockCopy.Render();
                     //bCenter = dragEnd;
                 }
                 spr = Ref.SpriteMan.GetSprite(SpriteManager.Type.GAME);
                 spr.Set(bCenter.x, bCenter.y, 6f, Ref.ResMan.LoadTexture("data/particle.png"));
                 spr.SetDepth(EDITOR_LAYER-1);
                 spr.SetColor(255, 255, 255, 100);
                 spr = Ref.SpriteMan.GetSprite(SpriteManager.Type.GAME);
                 spr.Set(bCenter.x, bCenter.y, 4f, Ref.ResMan.LoadTexture("data/edit_rotate.png"));
                 spr.SetDepth(EDITOR_LAYER);
                 break;
             case RESIZE:
                 Vector2f extent = selectedBlock.GetExtents();
                 Vector2f axis[] = selectedBlock.GetAxis();
 
                 if(selectedBlockCopy != null && scale_dragging) {
                     
                     selectedBlockCopy.Render();
 
                     extent = selectedBlockCopy.GetExtents();
                     axis = selectedBlockCopy.GetAxis();
                 }
                 
 
                 Vector2f rightCenter = new Vector2f(bCenter.x + axis[0].x * extent.x, bCenter.y + axis[0].y * extent.x);
                 spr = Ref.SpriteMan.GetSprite(SpriteManager.Type.GAME);
                 spr.setLine(bCenter, rightCenter);
                 spr.SetDepth(EDITOR_LAYER);
                 spr.SetColor(255, 0, 0, 255);
 
 
                 spr = Ref.SpriteMan.GetSprite(SpriteManager.Type.GAME);
                 spr.Set(rightCenter.x, rightCenter.y, 1f, Ref.ResMan.LoadTexture("data/corner.png"));
                 spr.SetAngle(selectedBlock.getAngle());
                 spr.SetDepth(EDITOR_LAYER);
 
                 Vector2f topCenter = new Vector2f(bCenter.x + axis[1].x * extent.y, bCenter.y + axis[1].y * extent.y);
                 spr = Ref.SpriteMan.GetSprite(SpriteManager.Type.GAME);
                 spr.setLine(bCenter, topCenter);
                 spr.SetDepth(EDITOR_LAYER);
                 spr.SetColor(0, 255, 0, 255);
                 spr = Ref.SpriteMan.GetSprite(SpriteManager.Type.GAME);
                 spr.Set(topCenter.x, topCenter.y, 1f, Ref.ResMan.LoadTexture("data/corner.png"));
                 spr.SetAngle(selectedBlock.getAngle());
                 spr.SetDepth(EDITOR_LAYER);
 //                spr = Ref.SpriteMan.GetSprite(SpriteManager.Type.GAME);
 //                spr.Set(bCenter.x + absSize.x, bCenter.y - absSize.y, 1f, Ref.ResMan.LoadTexture("data/corner.png"));
 //                spr.SetDepth(EDITOR_LAYER);
 //                spr = Ref.SpriteMan.GetSprite(SpriteManager.Type.GAME);
 //                spr.Set(bCenter.x + absSize.x, bCenter.y + absSize.y, 1f, Ref.ResMan.LoadTexture("data/corner.png"));
 //                spr.SetDepth(EDITOR_LAYER);
                 break;
             case MOVE:
                 if(selectedBlockCopy != null && move_dragging && dragEnd != null) {
                     selectedBlockCopy.Render();
                     bCenter = dragEnd;
                 }
                 spr = Ref.SpriteMan.GetSprite(SpriteManager.Type.GAME);
                 spr.Set(bCenter.x, bCenter.y, 6f, Ref.ResMan.LoadTexture("data/particle.png"));
                 spr.SetDepth(EDITOR_LAYER-1);
                 spr.SetColor(255, 255, 255, 100);
                 spr = Ref.SpriteMan.GetSprite(SpriteManager.Type.GAME);
                 spr.Set(bCenter.x, bCenter.y, 4f, Ref.ResMan.LoadTexture("data/edit_move.png"));
                 spr.SetDepth(EDITOR_LAYER);
                 break;
             case COPY:
                 if(selectedBlockCopy != null && copy_dragging && dragEnd != null) {
                     selectedBlockCopy.Render();
                     bCenter = dragEnd;
                 }
                 spr = Ref.SpriteMan.GetSprite(SpriteManager.Type.GAME);
                 spr.Set(bCenter.x, bCenter.y, 6f, Ref.ResMan.LoadTexture("data/particle.png"));
                 spr.SetDepth(EDITOR_LAYER-1);
                 spr.SetColor(255, 255, 255, 100);
                 spr = Ref.SpriteMan.GetSprite(SpriteManager.Type.GAME);
                 spr.Set(bCenter.x, bCenter.y, 4f, Ref.ResMan.LoadTexture("data/edit_copy.png"));
                 spr.SetDepth(EDITOR_LAYER);
                 break;
         }
     }
 
 
 
     private void initUI() {
         stashpanel = new CContainer(new FlowLayout(false, false, true));
         stashpanel.setPosition(new Vector2f(0, 96));
         stashpanel.setSize(new Vector2f(120, Ref.glRef.GetResolution().y - 128));
         stashpanel.setBackground(Ref.ResMan.LoadTexture("data/rightmenubar.png"));
         stashpanel.setResizeToChildren(Direction.NONE);
         CContainer stashTop = new CContainer(new FlowLayout(false, false, true));
         stashTop.addComponent(new CLabel("Stash:", Align.CENTER, 0.8f));
         stashpanel.addComponent(stashTop);
         CScrollPane stashScroll = new CScrollPane(Direction.VERTICAL);
         stashScrollCont = new CContainer(new FlowLayout(false, false, true));
 //        for (int i= 0; i < 30; i++) {
 //            stashScrollCont.addComponent(new StashItemUI(Ref.cm.cm.GetBlock(10)));
 //        }
         stashScroll.addComponent(stashScrollCont);
         stashpanel.addComponent(stashScroll);
         
         stashpanel.doLayout();
         stashScroll.setSize2(new Vector2f(stashpanel.getInternalSize().x, stashpanel.getInternalSize().y - stashTop.getLayoutSize().y));
         stashScroll.setResizeToChildren(Direction.NONE);
         stashScroll.doLayout();
 
         entityCont = new CContainer(new FlowLayout(false, false, true));
         entityCont.setPosition(new Vector2f(100, 100));
         entityCont.setSize(new Vector2f(220, 120));
         //entityCont.setInternalMargin(new Vector4f(5,5,5,5));
         entityCont.setBackground(Ref.ResMan.LoadTexture("data/rightmenubar.png"));
         entityCont.setResizeToChildren(Direction.NONE);
         CScrollPane entityScrollCont = new CScrollPane(Direction.VERTICAL);
         CContainer entityList = new CContainer(new FlowLayout(false, false, true));
         entityList.addComponent(new CButton("item_boots", null, Align.LEFT, 0.5f, new ButtonEvent() {
             public void buttonPressed(CComponent button, MouseEvent evt) {
                 SpawnEntity ent = new SpawnEntity("item_boots", Ref.cgame.cg.refdef.Origin);
                 Ref.game.spawnEntities.AddEntity(ent);
                 entities = Ref.game.spawnEntities.getList();
                 selectBlock(null);
             }
         }));
 //        for (int i = 0; i < 10; i++) {
 //
 //        }
         entityList.doLayout();
         entityScrollCont.addComponent(entityList);
         entityScrollCont.setSize2(entityCont.getInternalSize());
         entityScrollCont.setResizeToChildren(Direction.NONE);
         entityCont.addComponent(entityScrollCont);
         entityCont.doLayout();
 
 //        popupContainer = entityCont;
 
         sidepanel = new CContainer(new FlowLayout(false, false, true));
         sidepanel.setPosition(new Vector2f(Ref.glRef.GetResolution().x - 260, 0));
         sidepanel.setSize(new Vector2f(260, Ref.glRef.GetResolution().y));
         sidepanel.setBackground(Ref.ResMan.LoadTexture("data/rightmenubar.png"));
         menupanel = new CContainer(new FlowLayout(true, true, false));
         menupanel.setBackground(Ref.ResMan.LoadTexture("data/topmenubar.png"));
         menupanel.setSize(new Vector2f(512, 64));
 
         // Menu
         menupanel.addComponent(new CButton(Ref.ResMan.LoadTexture("data/edit_select.png"), new Vector2f(32,32), new ButtonEvent() {
             public void buttonPressed(CComponent button, MouseEvent evt) {
                 selectTool(Tool.SELECT);
             }
         }));
         menupanel.addComponent(new CButton(Ref.ResMan.LoadTexture("data/edit_move.png"), new Vector2f(32,32), new ButtonEvent() {
 
             public void buttonPressed(CComponent button, MouseEvent evt) {
                 selectTool(Tool.MOVE);
             }
         }));
         menupanel.addComponent(new CButton(Ref.ResMan.LoadTexture("data/edit_resize.png"), new Vector2f(32,32), new ButtonEvent() {
 
             public void buttonPressed(CComponent button, MouseEvent evt) {
                 selectTool(Tool.RESIZE);
             }
         }));
         menupanel.addComponent(new CButton(Ref.ResMan.LoadTexture("data/edit_copy.png"), new Vector2f(32,32), new ButtonEvent() {
 
             public void buttonPressed(CComponent button, MouseEvent evt) {
                 selectTool(Tool.COPY);
             }
         }));
         menupanel.addComponent(new CButton(Ref.ResMan.LoadTexture("data/edit_rotate.png"), new Vector2f(32,32), new ButtonEvent() {
             public void buttonPressed(CComponent button, MouseEvent evt) {
                 selectTool(Tool.ROTATE);
             }
         }));
         menupanel.addComponent(new CButton(Ref.ResMan.LoadTexture("data/edit_texedit.png"), new Vector2f(32,32),new ButtonEvent() {
             public void buttonPressed(CComponent button, MouseEvent evt) {
                 selectTool(Tool.TEXMOVE);
             }
         }));
         menupanel.addComponent(new CButton(Ref.ResMan.LoadTexture("data/edit_new.png"), new Vector2f(32,32), new ButtonEvent() {
             public void buttonPressed(CComponent button, MouseEvent evt) {
                 selectTool(Tool.NEWBLOCK);
             }
         }));
         menupanel.doLayout();
 
         // Side Panel
         CContainer cont = new CContainer(new FlowLayout(false, true, false));
         CButton texBut = new CButton(Ref.ResMan.getWhiteTexture(), new Vector2f(150, 150), new ButtonEvent() {
             public void buttonPressed(CComponent button, MouseEvent evt) {
                 mat_show = !mat_show;
             }
         });
 
         currentTexture = (CImage)texBut.getComponent(0);
         toolText = new CLabel(".", Align.CENTER, 0.9f);
         cursorText = new CLabel(".", Align.CENTER, 0.75f);
         cont.addComponent(cursorText);
         cont.addComponent(toolText);
         cont.addComponent(texBut);
         cont.doLayout();
         sidepanel.addComponent(cont);
 
         CContainer blockCont = new CContainer(new FlowLayout(false, true, false));
         blockCont.setResizeToChildren(CContainer.Direction.VERTICAL); // Allow vertical resize
         blockCont.setMargin(0, 0, 0, 0);
 
         CContainer butCont = new CContainer(new FlowLayout(true, true, false));
 
 
         butCont.addComponent(new CButton("Remove",null, Align.LEFT, 0.8f, new ButtonEvent() {
             public void buttonPressed(CComponent button, MouseEvent evt) {
                 if(select_model)
                     deleteModel(selectedModel);
                 else if(selectedBlock != null)
                     deleteBlock(selectedBlock);
                 if(selectedBlocks.size() > 0)
                     for (Block block : selectedBlocks) {
                         deleteBlock(block);
                     }
             }
         }));
         butCont.addComponent(new CButton("Stash",null, Align.LEFT, 0.8f, new ButtonEvent() {
             public void buttonPressed(CComponent button, MouseEvent evt) {
                 stashBlock(selectedBlock);
             }
         }));
         butCont.doLayout();
 
         blockCont.addComponent(butCont);
 
         CContainer posCont = new CContainer(new FlowLayout(true, false, false));
         centerLabel = new CLabel("Center:     \nx: N/A\ny: N/A\nz: N/A", Align.LEFT, 0.7f);
         posCont.addComponent(centerLabel);
 //        posCont.addComponent(new CLabel("Angle:\n0deg", Align.LEFT, 0.6f));
         extentLabel = new CLabel("Extent:     \nx: N/A\ny: N/A", Align.LEFT, 0.7f);
         extentLabel.setMargin(10, 0, 0, 0);
         posCont.addComponent(extentLabel);
         posCont.doLayout();
         blockCont.addComponent(posCont);
         miscCont = new CContainer(new FlowLayout(true, true, false));
         
         miscCont.doLayout();
         blockCont.addComponent(miscCont);
         CContainer gridCont = new CContainer(new FlowLayout(true, true, false));
         gridBox = new CCheckbox("Grid: 16u  ");
         gridBox.setMargin(0, 0, 5, 0);
         gridCont.addComponent(gridBox);
         gridCont.addComponent(new CButton(Ref.ResMan.LoadTexture("data/magnifier_zoom_in.png"), new Vector2f(16, 16), new ButtonEvent() {
             public void buttonPressed(CComponent button, MouseEvent evt) {
                 setGridSpacing(gridSpacing*2);
             }
         }));
 
         gridCont.addComponent(new CButton(Ref.ResMan.LoadTexture("data/magnifier_zoom_out.png"), new Vector2f(16, 16), new ButtonEvent() {
 
             public void buttonPressed(CComponent button, MouseEvent evt) {
                 setGridSpacing(gridSpacing/2);
             }
         }));
         
         gridCont.doLayout();
         blockCont.addComponent(gridCont);
         blockCont.addComponent(new CCheckbox("Show Collidable", new ButtonEvent() {
             public void buttonPressed(CComponent button, MouseEvent evt) {
                 boolean value = ((CCheckbox)button).isSelected();
                 Ref.cvars.Set2("cg_drawsolid", ""+(value?2:0), true);
             }
         }));
         CCheckbox showEnts = new CCheckbox("Show Entities", new ButtonEvent() {
             public void buttonPressed(CComponent button, MouseEvent evt) {
                 boolean value = ((CCheckbox)button).isSelected();
                 Ref.cvars.Set2("cg_drawentities", ""+(value?1:0), true);
                 showEntities = value;
             }
         });
         showEnts.setSelected(Ref.cgame.cg_drawentities.iValue==1?true:false);
         blockCont.addComponent(showEnts);
 
         // Top layer
         CContainer layerCont = new CContainer(new FlowLayout(true, true, false));
         layerCont.addComponent(new CLabel("Near Layer:", Align.LEFT, 0.8f));
         layerCont.addComponent(new CSpinner(Ref.cvars.Find("cg_depthnear").iValue, new ButtonEvent() {
             public void buttonPressed(CComponent button, MouseEvent evt) {
                 Ref.cvars.Set2("edit_nearlayer", ""+((CSpinner)button).getValue(), true);
             }
         }));
         layerCont.doLayout();
         blockCont.addComponent(layerCont);
 
         // Bottom layer
         CContainer layerCont2 = new CContainer(new FlowLayout(true, true, false));
         layerCont2.addComponent(new CLabel("Far Layer:", Align.LEFT, 0.8f));
         layerCont2.addComponent(new CSpinner(Ref.cvars.Find("cg_depthfar").iValue, new ButtonEvent() {
             public void buttonPressed(CComponent button, MouseEvent evt) {
                 Ref.cvars.Set2("edit_farlayer", ""+((CSpinner)button).getValue(), true);
             }
         }));
         layerCont2.doLayout();
         blockCont.addComponent(layerCont2);
 
         toolContainer = new CContainer(new FlowLayout(false, true, false));
         
         blockCont.doLayout();
         sidepanel.addComponent(blockCont);
         sidepanel.addComponent(toolContainer);
 //        blockCont.setSize2(new Vector2f(250, blockCont.getSize().y));
         sidepanel.doLayout();
 
         // Init Material Selection UI
         matSelect = new CContainer(new FlowLayout(false, false, true));
         matSelect.setBackground(Ref.ResMan.LoadTexture("data/rightmenubar.png"));
         Vector2f size = new Vector2f(Ref.glRef.GetResolution());
         size.scale(0.5f);
         matSelect.setSize(size);
         matSelect.setResizeToChildren(Direction.NONE);
 //        matSelect.setPosition(new Vector2f(50, 50));
         CContainer searchContainer = new CContainer(new FlowLayout(true, true, false));
         searchContainer.addComponent(new CLabel("Find Material:"));
         searchContainer.addComponent(new CButton("Close", null, Align.RIGHT, 0.75f, new ButtonEvent() {
             public void buttonPressed(CComponent button, MouseEvent evt) {
                 mat_show = false;
             }
         }));
         matSelect.addComponent(searchContainer);
         scrollPane = new CScrollPane(CContainer.Direction.VERTICAL);
         texContainer = new CContainer(new FlowLayout(false, false, true));
         
 
         updateMaterialList();
 
         
         
         scrollPane.addComponent(texContainer);
         matSelect.addComponent(scrollPane);
         matSelect.doLayout();
         matSelect.doLayout();
         scrollPane.setSize2(new Vector2f(scrollPane.getSize().x, matSelect.getInternalSize().y - searchContainer.getLayoutSize().y));
         matSelect.setPosition(new Vector2f(size.x - matSelect.getLayoutSize().x/2f,size.y - matSelect.getLayoutSize().y/2f));
     }
 
     CContainer miscCont = null;
 
     private void setMiscCont(boolean group, boolean collidable, Block b, boolean ungroup, boolean toEntity) {
         miscCont.removeComponents();
         miscCont.setSize2(new Vector2f(10,10));
 
         if(group)
             miscCont.addComponent(new CButton("Group", null, Align.CENTER, 0.8f, new ButtonEvent() {
                 public void buttonPressed(CComponent button, MouseEvent evt) {
                     groupSelected();
                 }
             }));
         else if(ungroup) {
             miscCont.addComponent(new CButton("Ungroup", null, Align.CENTER, 0.8f, new ButtonEvent() {
                 public void buttonPressed(CComponent button, MouseEvent evt) {
                     ungroupSelected();
                 }
             }));
 
             
         }
         if(toEntity || true) {
             miscCont.addComponent(new CButton("Entity", null, Align.CENTER, 0.8f, new ButtonEvent() {
                 public void buttonPressed(CComponent button, MouseEvent evt) {
                     openEntityPopupMenu(OrgCoordsToPixelCoords(Ref.Input.playerInput.MousePos));
                 }
             }));
         }
         if(collidable && b != null) {
             CCheckbox collide = new CCheckbox("Collidable", new ButtonEvent() {
                 public void buttonPressed(CComponent button, MouseEvent evt) {
                     if(isMultiSelecting()) {
                         for (Block block : selectedBlocks) {
                             block.Collidable = ((CCheckbox)button).isSelected();
                         }
                     }
                     else if(selectedBlock != null)
                         selectedBlock.Collidable = ((CCheckbox)button).isSelected();
                 }
             });
             collide.setSelectedDontFire(b.Collidable);
             miscCont.addComponent(collide);
         }
         miscCont.doLayout();
         sidepanel.doLayout();
     }
 
     private void ungroupSelected() {
         if(!select_model || selectedModel == null)
             return;
 
         Block[] data = selectedModel.blocks.toArray(new Block[selectedModel.blocks.size()]);
         for (Block block : data) {
             selectedModel.removeBlock(block);
         }
         selectModel(null);
         setMiscCont(false, false, null, false, false);
     }
 
     private void openEntityPopupMenu(Vector2f position) {
         popupContainer = entityCont;
         Vector2f popupSize = popupContainer.getLayoutSize();
         Vector2f res = Ref.glRef.GetResolution();
 
         Vector2f finalpos = new Vector2f(position);
         if(position.x + popupSize.x > res.x) {
             finalpos.x = res.x - popupSize.x;
         }
         if (position.y + popupSize.y > res.y) {
             finalpos.y = res.y - popupSize.y;
         }
 //        Vector2f position = new Vector2f(sidepanel.getInternalPosition());
 //        Vector2f.add(position, miscCont.getInternalPosition(), position);
 
 //        finalpos.y = 200;//res.y - finalpos.y + popupSize.y;
         entityCont.setPosition(finalpos);
     }
 
     private void groupSelected() {
         // If a model is selected, latch the other block onto it
         if(selectedModel != null && select_model) {
             for (Block block : selectedBlocks) {
                 selectedModel.addBlock(block);
             }
             if(selectedBlocks.isEmpty() && selectedBlock != null)
                 selectedModel.addBlock(selectedBlock);
             selectModel(selectedModel);
         } else {
             // No model selected, create one from the blocks
             BlockModel mdl = null;
             for (int i= 0; i < selectedBlocks.size(); i++) {
                 Block b = selectedBlocks.get(i);
                 if(i == 0) {
                     mdl = Ref.cm.cm.getModel(Ref.cm.cm.ToSubModel(b));
                 } else {
                     mdl.addBlock(b);
                 }
             }
             selectModel(mdl);
         }   
     }
 
     private void updateMaterialList() {
         
         Vector2f elementSize = new Vector2f(64,64);
         float margin = 10;
         Vector2f elemSizePlusMargin = new Vector2f(elementSize.x+margin*2,elementSize.y+margin*2);
         float maxWidth = matSelect.getInternalSize().x;
         String[] list = ResourceManager.getMaterialList();
         
         int elemPerRow = (int) (maxWidth / elemSizePlusMargin.x);
         int elemCount = 0;
         if(list != null && list.length > 0) {
             elemCount = list.length;
         }
         int i = 0;
         while(i < elemCount) {
             CContainer texRow = new CContainer(new FlowLayout(true, true, false));
             texRow.setResizeToChildren(Direction.VERTICAL);
 
             for (int j= 0; j < elemPerRow && i < elemCount; j++) {
                 CubeMaterial mat = null;
                 CButton elem = null;
                 try {
                     mat = CubeMaterial.Load(list[i], true);
                     elem = new CButton(mat, elementSize, new ButtonEvent() {
                         public void buttonPressed(CComponent button, MouseEvent evt) {
                             CubeMaterial mat = ((CImage)((CButton)button).getComponent(0)).getMaterial();
                             if(mat != null) {
                                 selectMaterial(selectedBlock, mat);
                                 mat_show = false;
                             }
                         }
                     });
                 } catch (Exception ex) {
                     Logger.getLogger(MapEditor.class.getName()).log(Level.SEVERE, null, ex);
                     elem = new CButton(Ref.ResMan.LoadTexture("data/tile.png"));
                 }
                 
                 //CImage elem = new CImage("data/tile.png");
                 elem.setSize(elementSize);
 
                 CContainer elemCont = new CContainer(new FlowLayout(false, false, true));
                 elemCont.addComponent(elem);
                 
 
                 elemCont.addComponent(new CLabel(CubeMaterial.stripPath(list[i]),Align.CENTER,0.5f));
                 elemCont.setMargin(2, 2, 2, 2);
                 elemCont.doLayout();
                 texRow.addComponent(elemCont);
                 i++;
             }
             texContainer.addComponent(texRow);
         }
         scrollPane.doLayout();
     }
 
     
 
     private void renderGrid() {
         Vector2f lineSize = new Vector2f(Ref.cgame.cg.refdef.FovX / Ref.glRef.GetResolution().x,
                                         Ref.cgame.cg.refdef.FovY / Ref.glRef.GetResolution().y);
         
         Vector2f startPos = new Vector2f(Ref.cgame.cg.refdef.Origin);
        int width = Ref.cgame.cg.refdef.FovX + gridSpacing * 2;
         int height = Ref.cgame.cg.refdef.FovY + gridSpacing * 2;
         startPos.x -= Ref.cgame.cg.refdef.FovX / 2f + gridSpacing;
         startPos.y -= Ref.cgame.cg.refdef.FovY / 2f + gridSpacing;
         
         applyGrid(startPos);
         
         // Draw horizontal lines
         int nLines = height / gridSpacing;
         Vector2f size = new Vector2f(width, lineSize.y);
         for (int i= 0; i < nLines; i++) {
             Sprite spr = Ref.SpriteMan.GetSprite(SpriteManager.Type.GAME);
             Vector2f position = new Vector2f(startPos);
             position.y += gridSpacing * i - lineSize.y * 0.5f;
             spr.Set(position, size, Ref.ResMan.getWhiteTexture(), null, null);
             spr.SetColor(200, 200, 200, 150);
             spr.SetDepth(EDITOR_LAYER);
         }
         nLines = width / gridSpacing;
         size = new Vector2f(lineSize.x, height);
         for (int i= 0; i < nLines; i++) {
             Sprite spr = Ref.SpriteMan.GetSprite(SpriteManager.Type.GAME);
             Vector2f position = new Vector2f(startPos);
             position.x += gridSpacing * i - lineSize.x * 0.5f;
             spr.Set(position, size, Ref.ResMan.getWhiteTexture(), null, null);
             spr.SetColor(200, 200, 200, 150);
             spr.SetDepth(EDITOR_LAYER);
         }
         
     }
 
     private void applyGrid(Vector2f v) {
         float l = (int)(v.x / gridSpacing);
         v.x -= l * gridSpacing;
         //v.x = v.x / gridSpacing;
         if(v.x >= gridSpacing * 0.5f)
             l++;
         else if(v.x <= gridSpacing * -0.5f)
             l--;
         v.x = l * gridSpacing;
         
         l = (int)(v.y / gridSpacing);
         v.y -= l * gridSpacing;
         //v.y = v.y / gridSpacing;
 
         if(v.y >= gridSpacing * 0.5f)
             l++;
         else if(v.y <= gridSpacing * -0.5f)
             l--;
         
         v.y = l * gridSpacing;
     }
 
 
     public void KeyPressed(KeyEvent evt) {
         // TODO: Allow tool to get input
 
         Key k = (Key) evt.getSource();
         if(k.Char >= '0' && k.Char <= '9') {
             int index = k.Char - '0' - 1;
             if(k.Pressed && menupanel.getComponentCount() > index) {
                 CComponent comp = menupanel.getComponent(index);
                 if(comp instanceof CButton) {
                     CButton but = (CButton)comp;
                     but.pressButton();
                 }
             }
         } else if(k.key == Keyboard.KEY_DELETE && k.Pressed) {
             if(select_model)
                 deleteModel(selectedModel);
             else if(selectedBlock != null)
                 deleteBlock(selectedBlock);
             if(selectedBlocks.size() > 0)
                 for (Block block : selectedBlocks) {
                     deleteBlock(block);
                 }
         } else if(k.key == Keyboard.KEY_LSHIFT && k.Pressed) {
 //            if(tool == Tool.SELECT && k.Pressed) {
 //                revertTool = tool;
 //                selectTool(Tool.MOVE);
 //            } else if(!k.Pressed && revertTool != null && !Ref.Input.playerInput.Mouse1) {
 //                selectTool(revertTool);
 //            }
         } else if(k.key == Keyboard.KEY_LCONTROL) {
             if(tool == Tool.SELECT && k.Pressed) {
                 revertTool = tool;
                 selectTool(Tool.RESIZE);
             } else if(!k.Pressed && revertTool != null && !Ref.Input.playerInput.Mouse1) {
                 selectTool(revertTool);
             }
         } else if(k.key == Keyboard.KEY_LMENU) {
             if(tool == Tool.SELECT && k.Pressed) {
                 revertTool = tool;
                 selectTool(Tool.ROTATE);
             } else if(!k.Pressed && revertTool != null && !Ref.Input.playerInput.Mouse1) {
                 selectTool(revertTool);
             }
         } else if (k.key == Keyboard.KEY_SPACE) {
             if(tool == Tool.SELECT && k.Pressed) {
                 revertTool = tool;
                 selectTool(Tool.COPY);
             } else if(!k.Pressed && revertTool != null && !Ref.Input.playerInput.Mouse1) {
                 selectTool(revertTool);
             }
         } else {
             Key key = (Key)evt.getSource();
             Ref.Input.binds.ParseBinding(key.key, key.Pressed, key.Time);
         }
     }
 
     public void GotMouseEvent(MouseEvent evt) {
         // From 0-1 to 0-resolution space
         Vector2f absCoords = new Vector2f(Ref.glRef.GetResolution());
         Vector2f orgCoords = new Vector2f(evt.Position);
         absCoords.x *= evt.Position.x;
         absCoords.y *= 1-evt.Position.y;
 
         evt.Position = absCoords;
 
         Vector2f position = null, size = null;
         if(popupContainer != null) {
             position = popupContainer.getPosition();
             size = popupContainer.getLayoutSize();
         }
         
             if(popupContainer != null && absCoords.x >= position.x && absCoords.x < position.x + size.x &&
                 absCoords.y >= position.y && absCoords.y < position.y + size.y) {
                 popupContainer.MouseEvent(evt);
             } else if(popupContainer != null && evt.Button == 0 && evt.Pressed) {
                 // remove popupcont
                 popupContainer = null;
             } else {
                 position = sidepanel.getPosition();
                 size = sidepanel.getLayoutSize();
                 if(absCoords.x >= position.x && absCoords.x < position.x + size.x &&
                         absCoords.y >= position.y && absCoords.y < position.y + size.y) {
                     sidepanel.MouseEvent(evt);
                 } else {
                     position = menupanel.getPosition();
                     size = menupanel.getLayoutSize();
                     if(absCoords.x >= position.x && absCoords.x < position.x + size.x &&
                         absCoords.y >= position.y && absCoords.y < position.y + size.y) {
                         menupanel.MouseEvent(evt);
                     }  else {
                         position = stashpanel.getPosition();
                         size = stashpanel.getLayoutSize();
                         if(absCoords.x >= position.x && absCoords.x < position.x + size.x &&
                             absCoords.y >= position.y && absCoords.y < position.y + size.y) {
                             stashpanel.MouseEvent(evt);
                         } else if(mat_show) {
                             matSelect.MouseEvent(evt);
                         } else {
                             evt.Position = orgCoords;
                             toolMouseEvent(evt);
                         }
                     }
 
 
                 }
             }
        // }
 
         
     }
 
     private Vector2f OrgCoordsToPixelCoords(Vector2f pos) {
         Vector2f result = new Vector2f(Ref.glRef.GetResolution());
         result.x = result.x * (pos.x);
         result.y = result.y * (1f-pos.y);
         return result;
     }
 
     private Vector2f OrgCoordsToGameCoords(Vector2f pos) {
         Vector2f result = new Vector2f(Ref.cgame.cg.refdef.Origin);
         result.x += Ref.cgame.cg.refdef.FovX * (pos.x - 0.5f);
         result.y += Ref.cgame.cg.refdef.FovY * (pos.y - 0.5f);
         return result;
     }
 }
