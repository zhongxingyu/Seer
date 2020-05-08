 package com.inspedio.entity;
 
 import javax.microedition.lcdui.Graphics;
 
 import com.inspedio.entity.actions.Parallel;
 import com.inspedio.entity.actions.Sequence;
 import com.inspedio.entity.primitive.InsPoint;
 import com.inspedio.entity.primitive.InsSize;
 import com.inspedio.enums.HAlignment;
 import com.inspedio.enums.VAlignment;
 import com.inspedio.system.core.InsGlobal;
 import com.inspedio.system.helper.InsKeys;
 import com.inspedio.system.helper.InsLogger;
 import com.inspedio.system.helper.InsUtil;
 import com.inspedio.system.helper.extension.InsAlignment;
 
 /**
  * InsBasic represent any game object which have position and size.<br>
  * 
  * @author Hyude
  * @version 1.0
  */
 public class InsBasic extends InsAtom{
 
 	// ATTRIBUTE
 	/**
 	 * X and Y coordinate of this object
 	 */
 	public InsPoint position;
 	/**
 	 * Width and Height coordinate of this object
 	 */
 	public InsSize size;
 	/**
 	 * Vertical and Horizontal alignment of this object
 	 */
 	public InsAlignment align;
 	/**
 	 * Action currently assigned to this object
 	 */
 	public InsAction action;
 	
 	/**
 	 * Instantiates a default <code>InsBasic</code> object.
 	 */
 	public InsBasic()
 	{
 		this(0, 0, 0, 0);
 	}
 	
 	/**
 	 * Instantiates a <code>InsBasic</code> with given size and position.
 	 * 
 	 * @param	X		The X-coordinate of the point in world space.
 	 * @param	Y		The Y-coordinate of the point in world space.
 	 * @param	Width	Desired width of object.
 	 * @param	Height	Desired height of object.
 	 */
 	public InsBasic(int X, int Y, int Width, int Height)
 	{
 		super();
 		this.position = new InsPoint(X, Y);
 		this.size = new InsSize(Width, Height);
 		this.align = new InsAlignment();
 		this.action = null;
 	}
 	
 	/**
 	 * Set Object Size
 	 */
 	public void setSize(int Width, int Height)
 	{
 		this.size.setSize(Width, Height);
 	}
 	
 	/**
 	 * Add Object Size
 	 */
 	public void addSize(int Width, int Height)
 	{
		this.size.setSize(this.size.width + Width, this.size.height + Height);
 	}
 	
 	/**
 	 * Set Object Position
 	 */
 	public void setPosition(int X, int Y)
 	{
 		this.position.setPoint(X, Y);
 	}
 	
 	/**
 	 * Add Object Position
 	 */
 	public void addPosition(int X, int Y)
 	{
		this.position.setPoint(this.position.x + X, this.position.y + Y);
 	}
 	
 	public void setAlignment(HAlignment horizontal, VAlignment vertical){
 		this.align.setAlignment(horizontal, vertical);
 	}
 	
 	
 	/**
 	 * Set this object Middle Point to given point<br>
 	 * Useful for centering object into square
 	 */
 	public void setMiddlePoint(int X, int Y)
 	{
 		this.position.x = X + ((this.align.horizontal.getValue()-1) * (this.size.width / 2));
 		this.position.y = Y + ((this.align.vertical.getValue()-1) * (this.size.height / 2));
 	}
 
 	public void preUpdate() {
 		
 	}
 
 	public void update() {
 		handleKeyState(InsGlobal.keys);
 		if(this.action != null){
 			this.action.act();
 		}
 	}
 
 	public void postUpdate() {
 		
 	}
 
 	public void draw(Graphics g) {
 		
 	}
 	
 	/**
 	 * Forcefully Set Action of this object.
 	 */
 	public boolean setAction(InsAction Action){
 		return this.setAction(Action, true);
 	}
 	
 	/**
 	 * Set Action of this object.
 	 * 
 	 * @param	forceSet	Set this to FALSE to set action only there is no current active action
 	 */
 	public boolean setAction(InsAction Action, boolean forceSet){
 		if((this.action == null) ||forceSet){
 			this.action = Action;
 			this.action.setTarget(this);
 			return true;
 		}
 		return false;
 		
 	}
 	
 	public void appendAction(InsAction Action){
 		if(this.action != null){
 			this.setAction(Sequence.create(new InsAction[] {this.action, Action}, null), true);
 		} else {
 			this.setAction(Action, true);
 		}
 	}
 	
 	public void combineAction(InsAction Action){
 		if(this.action != null){
 			this.setAction(Parallel.create(new InsAction[] {this.action, Action}, null), true);
 		} else {
 			this.setAction(Action, true);
 		}
 		
 	}
 	
 	public void unsetAction(InsAction Action){
 		if(this.action == Action){
 			this.action.setTarget(null);
 			this.action = null;
 			InsLogger.writeLog("Action Unset");
 		}
 	}
 	
 	public void clearAction(){
 		this.action = null;
 	}
 	
 	/**
 	 * Override this to implement keyHandler
 	 */
 	protected void handleKeyState(InsKeys key)
 	{
 	}
 	
 	/**
 	 * Override this to implement touchPressed behavior
 	 * 
 	 * @return	TRUE if you want touchEvent to not passed to next Object
 	 */
 	protected boolean onTouchPressed(){
 		return false;
 	}
 	
 	/**
 	 * Override this to implement touchReleased behavior
 	 * 
 	 * @return	TRUE if you want touchEvent to not passed to next Object
 	 */
 	protected boolean onTouchReleased(){
 		return false;
 	}
 
 	/**
 	 * Override this to implement touchDragged behavior
 	 * 
 	 * @return	TRUE if you want touchEvent to not passed to next Object
 	 */
 	protected boolean onTouchDragged(){
 		return false;
 	}
 	
 	/**
 	 * Override this to implement touchHold behavior
 	 * 
 	 * @return	TRUE if you want touchEvent to not passed to next Object
 	 */
 	protected boolean onTouchHold(){
 		return false;
 	}
 	
 	/**
 	 * Do not override this unless you want to specifically access coordinate touched
 	 */
 	public boolean onPointerPressed(int X, int Y) {
 		if(this.isOverlap(X, Y)){
 			return onTouchPressed();
 		}
 		return false;
 	}
 
 	public boolean onPointerReleased(int X, int Y) {
 		if(this.isOverlap(X, Y)){
 			return onTouchReleased();
 		}
 		return false;
 	}
 
 	public boolean onPointerDragged(int X, int Y) {
 		if(this.isOverlap(X, Y)){
 			return onTouchDragged();
 		}
 		return false;
 	}
 	
 	public boolean onPointerHold(int X, int Y) {
 		if(this.isOverlap(X, Y)){
 			return onTouchHold();
 		}
 		return false;
 	}
 	
 	/**
 	 * Whether X and Y pointer is inside object
 	 */
 	public boolean isOverlap(int X, int Y){
 		return ((InsUtil.Absolute(X - this.getMiddleX()) <= (this.size.width / 2)) && (InsUtil.Absolute(Y - this.getMiddleY()) <= (this.size.height / 2)));
 	}
 	
 	public boolean isOverlap(InsPoint P){
 		return ((InsUtil.Absolute(P.x - this.getMiddleX()) <= (this.size.width / 2)) && (InsUtil.Absolute(P.y - this.getMiddleY()) <= (this.size.height / 2)));
 	}
 	
 	public int getMiddleX(){
 		return this.position.x - (((this.align.horizontal.getValue()-1) * this.size.width) / 2); 
 	}
 	
 	public int getMiddleY(){
 		return this.position.y - (((this.align.vertical.getValue()-1) * this.size.height) / 2); 
 	}
 	
 	public int getLeft(){
 		return this.position.x - ((this.align.horizontal.getValue() * this.size.width) / 2); 
 	}
 	
 	public int getTop(){
 		return this.position.y - ((this.align.vertical.getValue() * this.size.height) / 2); 
 	}
 
 }
