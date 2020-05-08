 package linewars.gamestate.shapes;
 
 import linewars.configfilehandler.ConfigData;
 import linewars.configfilehandler.ConfigData.NoSuchKeyException;
 import linewars.configfilehandler.ParserKeys;
 import linewars.gamestate.Position;
 import linewars.gamestate.Transformation;
 
 public strictfp class Circle extends Shape {
 	
 	static {
 		Shape.addClassForInitialization("circle", Circle.class);
 	}
 	
 	//TODO document
 	private final Transformation position;
 	private final double radius;
 	
 	//TODO document
 	public Circle(Transformation pos, double radius){
 		this.radius = radius;
 		position = pos;
 	}
 	
 	public Circle(ConfigData config){
 		radius = config.getNumber(ParserKeys.radius);
 		double rotation = 0;
 		try{
 			rotation = Math.PI * config.getNumber(ParserKeys.rotation);
 		}catch(NoSuchKeyException e){
 			//just means rotation wasn't set, so it is 0 by default
 		}
 		position = new Transformation(new Position(config.getNumber(ParserKeys.x), config.getNumber(ParserKeys.y)), rotation);
 	}
 
 	@Override
 	public Shape stretch(Transformation change) {
 		double length = radius * 2 + Math.sqrt(change.getPosition().distanceSquared(new Position(0, 0)));
 		double width = radius * 2;
 		double rotation = Math.atan(change.getPosition().getY() / change.getPosition().getX());
 		return new Rectangle(new Transformation(position.getPosition().add(change.getPosition().scale(.5)), rotation), length, width);
 	}
 
 	@Override
 	public Shape transform(Transformation change) {
 		return new Circle(new Transformation(position.getPosition().add(change.getPosition()), position.getRotation() + change.getRotation()), radius);
 	}
 
 	@Override
 	public Transformation position() {
 		return position;
 	}
 
 	@Override
 	public Circle boundingCircle() {
 		return this;
 	}
 
 	@Override
 	public Rectangle boundingRectangle() {
 		return new Rectangle(position, radius * 2, radius * 2);
 	}
 
 	/**
 	 * Returns the radius of the circle.
 	 */
 	public double getRadius(){
 		return radius;
 	}
 
 	@Override
 	public boolean positionIsInShape(Position toTest) {
 		return toTest.distanceSquared(position.getPosition()) < radius * radius;
 	}
 	
 	@Override
 	public String toString(){
 		return "Center = " + position + " Radius = " + radius;
 	}
 	
 	//Yes, I'm checking doubles for equality.  That's on purpose.
 	@Override
 	public boolean equals(Object other){
 		if(other == null) return false;
 		if(!(other instanceof Circle)) return false;
 		Circle otherCircle = (Circle) other;
 		if(!otherCircle.position.equals(position)) return false;
 		if(!(otherCircle.radius == radius)) return false;
 		return true;
 	}
 
 	@Override
 	public ConfigData getData() {
 		ConfigData cd = new ConfigData();
		cd.set(ParserKeys.shapetype, "circle");
 		cd.set(ParserKeys.radius, radius);
 		cd.set(ParserKeys.rotation, position.getRotation());
 		cd.set(ParserKeys.x, position.getPosition().getX());
 		cd.set(ParserKeys.y, position.getPosition().getY());
 		return cd;
 	}
 }
