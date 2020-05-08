 package linewars.gamestate.shapes;
 
 import linewars.configfilehandler.ConfigData;
 import linewars.configfilehandler.ConfigData.NoSuchKeyException;
 import linewars.configfilehandler.ParserKeys;
 import linewars.gamestate.Position;
 import linewars.gamestate.Transformation;
 
 public strictfp class Rectangle extends Shape {
 	
 	static {
 		Shape.addClassForInitialization("rectangle", Rectangle.class);
 	}
 	
 	//TODO document
 	private double width, height;
 	private Transformation position;
 	
 	public Rectangle(ConfigData config){
 		width = config.getNumber(ParserKeys.width);
 		height = config.getNumber(ParserKeys.height);
 		double rotation = 0;
 		try{
 			rotation = Math.PI * config.getNumber(ParserKeys.rotation);
 		}catch(NoSuchKeyException e){
 			//Just means rotation wasn't set, so it defaults to 0
 		}
 		position = new Transformation(new Position(config.getNumber(ParserKeys.x), config.getNumber(ParserKeys.y)), rotation);
 	}
 	
 	//TODO document
 	public Rectangle(Transformation position, double width, double height){
 		this.position = position;
 		this.width = width;
 		this.height = height;
 	}
 
 	@Override
 	public Shape stretch(Transformation change) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public Shape transform(Transformation change) {
 		Transformation newTransform = new Transformation(position.getPosition().add(change.getPosition()), position.getRotation() + change.getRotation());
 		return new Rectangle(newTransform, width, height);
 	}
 
 	@Override
 	public Transformation position() {
 		return position;
 	}
 
 	@Override
 	public Circle boundingCircle() {
 		// compute diagonal
 		double diagonal = Math.sqrt(width * width + height * height);
 		// construct and return a Circle
 		return new Circle(position, diagonal / 2);
 	}
 
 	@Override
 	public Rectangle boundingRectangle() {
 		return this;
 	}
 	
 	/**
 	 * 
 	 * @return The width of the rectangle, ignoring any rotation.
 	 */
 	public double getWidth(){
 		return width;
 	}
 	
 	/**
 	 * 
 	 * @return The height of the rectangle, ignoring any rotation.
 	 */
 	public double getHeight(){
 		return height;
 	}
 	
 	public Position[] getVertexPositions(){
 		Position[] ret = new Position[4];
 		Position halfWidth = new Position(Math.cos(position.getRotation()) * width, Math.sin(position.getRotation()) * width).scale(.5);
 		Position halfHeight = new Position(Math.sin(position.getRotation()) * height, Math.cos(position.getRotation()) * height).scale(.5);
 		ret[0] = position.getPosition().add(halfHeight).add(halfWidth);
 		ret[1] = position.getPosition().add(halfHeight).subtract(halfWidth);
 		ret[2] = position.getPosition().subtract(halfHeight).subtract(halfWidth);
 		ret[3] = position.getPosition().subtract(halfHeight).add(halfWidth);
 		return ret;
 	}
 	
 	//TODO document
 	//counter-clockwise edge vectors
 	public Position[] getEdgeVectors(){
 		Position[] ret = new Position[4];
 		Position w = new Position(Math.cos(position.getRotation()) * width, Math.sin(position.getRotation()) * width);
 		Position h = new Position(Math.cos(position.getRotation()) * height, Math.sin(position.getRotation()) * height);
 		ret[0] = w;
 		ret[1] = h;
 		ret[2] = w.scale(-1);
 		ret[3] = h.scale(-1);
 		return ret;
 	}
 
 	@Override
 	public boolean positionIsInShape(Position toTest) {
 		//TODO test
 		//ray-casting algorithm, look it up.  Implementation might be wrong :(
 		int numCrossings = 0;
 		Position[] vertices = getVertexPositions();
 		
 		for(int i = 0, j = vertices.length - 1; i < vertices.length; j = i, i++){
 			if(vertices[i].getX() <= toTest.getX() && vertices[j].getX() <= toTest.getX()
 				&& ((vertices[i].getY() < toTest.getY() && vertices[j].getY() >= toTest.getY())
 				|| (vertices[j].getY() < toTest.getY() && vertices[i].getY() >= toTest.getY()))){
 					numCrossings++;
 				}
 		}
 		
 		return numCrossings % 2 == 1;
 	}
 	
 	//Very strict; these Rectangles will only be considered equal if they are bit-identical.
 	@Override
 	public boolean equals(Object other){
 		if(other == null) return false;
 		if(!(other instanceof Rectangle)) return false;
 		Rectangle otherRect = (Rectangle) other;
 		if(width != otherRect.width) return false;
 		if(height != otherRect.height) return false;
 		if(!position.equals(otherRect.position)) return false;
 		return true;
 	}
 
 	@Override
 	public ConfigData getData() {
 		ConfigData cd = new ConfigData();
		cd.set(ParserKeys.shapetype, "rectangle");
 		cd.set(ParserKeys.width, width);
 		cd.set(ParserKeys.height, height);
 		cd.set(ParserKeys.rotation, position.getRotation());
 		cd.set(ParserKeys.x, position.getPosition().getX());
 		cd.set(ParserKeys.y, position.getPosition().getY());
 		return cd;
 	}
 }
