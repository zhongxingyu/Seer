 package com.akjava.gwt.threebox2d.client;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.jbox2d.collision.AABB;
 import org.jbox2d.collision.shapes.CircleShape;
 import org.jbox2d.collision.shapes.PolygonShape;
 import org.jbox2d.collision.shapes.Shape;
 import org.jbox2d.collision.shapes.ShapeType;
 import org.jbox2d.common.Vec2;
 import org.jbox2d.dynamics.Body;
 import org.jbox2d.dynamics.Fixture;
 import org.jbox2d.dynamics.World;
 import org.jbox2d.dynamics.joints.DistanceJoint;
 import org.jbox2d.dynamics.joints.Joint;
 
 import com.akjava.gwt.lib.client.CanvasUtils;
 import com.akjava.gwt.lib.client.LogUtils;
 import com.akjava.gwt.stats.client.Stats;
 import com.akjava.gwt.three.client.THREE;
 import com.akjava.gwt.three.client.cameras.Camera;
 import com.akjava.gwt.three.client.core.Object3D;
 import com.akjava.gwt.three.client.experiments.CSS3DObject;
 import com.akjava.gwt.three.client.experiments.CSS3DRenderer;
 import com.akjava.gwt.three.client.lights.Light;
 import com.akjava.gwt.three.client.materials.MeshBasicMaterialBuilder;
 import com.akjava.gwt.three.client.renderers.WebGLRenderer;
 import com.akjava.gwt.three.client.scenes.Scene;
 import com.akjava.gwt.three.client.textures.Texture;
 import com.akjava.gwt.threebox2d.client.demo.EdgeDemo;
 import com.akjava.gwt.threebox2d.client.demo.simple.SimpleDemo;
 import com.akjava.gwt.threebox2d.client.demo.spring.SpringDemo;
 import com.google.gwt.canvas.client.Canvas;
 import com.google.gwt.canvas.dom.client.Context2d;
 import com.google.gwt.core.client.EntryPoint;
 import com.google.gwt.dom.client.Style.Position;
 import com.google.gwt.dom.client.Style.Unit;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.event.dom.client.KeyDownEvent;
 import com.google.gwt.event.dom.client.KeyDownHandler;
 import com.google.gwt.event.dom.client.KeyUpEvent;
 import com.google.gwt.event.dom.client.KeyUpHandler;
 import com.google.gwt.event.logical.shared.ValueChangeEvent;
 import com.google.gwt.event.logical.shared.ValueChangeHandler;
 import com.google.gwt.text.shared.Renderer;
 import com.google.gwt.user.client.Timer;
 import com.google.gwt.user.client.ui.Button;
 import com.google.gwt.user.client.ui.FocusPanel;
 import com.google.gwt.user.client.ui.HTMLPanel;
 import com.google.gwt.user.client.ui.HorizontalPanel;
 import com.google.gwt.user.client.ui.Image;
 import com.google.gwt.user.client.ui.RootLayoutPanel;
 import com.google.gwt.user.client.ui.ValueListBox;
 import com.google.gwt.user.client.ui.VerticalPanel;
 
 public class Main implements EntryPoint {
 
 
 
 	//private Canvas canvas;
 	
 	
 
 	
 
 	private Camera camera;
 
 	private WebGLRenderer renderer;
 
 	private Scene scene;
 	int width=600;
 	int height=350;
 	Object3D objRoot;
 	Map<Body,Object3D> threeObjects=new HashMap<Body,Object3D>();
 	Map<Joint,Object3D> threeJoints=new HashMap<Joint,Object3D>();
 	List<Box2dDemo> demos=new ArrayList<Box2dDemo>();
 	
 	
 	//private String currentRendererType="css3d";
 	private void switchRenderer(String type){
 		init();
 		if(renderer.gwtGetType().equals(type)){
 			return;
 		}
 		focusPanel.clear();
 		HTMLPanel div=new HTMLPanel("");
 		if(type.equals("css3d")){
 			renderer = THREE.CSS3DRenderer();
 		}else if(type.equals("webgl")){
 			renderer=THREE.WebGLRenderer();
 		}else{//canvas
 			renderer=THREE.CanvasRenderer();
 		}
 		renderer.setSize(width, height);
 		div.getElement().appendChild(renderer.getDomElement());
 		renderer.gwtSetType(type);
 		focusPanel.add(div);
 	}
 	public int scale=10;
 	@Override
 	public void onModuleLoad() {
 		
 		
 		
 		main = new MainUi();
 		RootLayoutPanel.get().add(main);
 		//RootPanel.get().add(main);
 		demos.add(new EdgeDemo());
 		demos.add(new SpringDemo());
 		
 		demos.add(new SimpleDemo());
 		
 		
 		//demos.add(new CarDemo());
 		//demos.add(new ImageWaveDemo());
 		
 		//APEngine.container(new ArrayListDisplayObjectContainer());
 		
 		//apeDemo = new CarDemo();
 		//apeDemo=new ImageWaveDemo();
 		
 		//apeDemo.initialize();
 		
 		//APEngine.step();
 		
 		
 		scene = THREE.Scene();
 		objRoot=THREE.Object3D();
 		objRoot.setPosition(-width/2, 0, 0);
 		scene.add(objRoot);
 		
 		camera = THREE.PerspectiveCamera(35,(double)width/height,.1,10000);
 		scene.add(camera);
 		//camera.getPosition().setZ(700);
 		camera.getPosition().setZ(1000);
 		
 		camera.getPosition().setX(0);
 		camera.getPosition().setY(-200);
 		
 		
 		light = THREE.PointLight(0xffffff);
 		light.setPosition(10, 0, 10);
 		scene.add(light);
 		
 		
 		//camera.getRotation().setZ(Math.toRadians(180)); //fliphorizontaled
 		renderer = THREE.CSS3DRenderer();
 		renderer.gwtSetType("css3d");
 		renderer.setSize(width, height);
 		
 		
 		HTMLPanel div=new HTMLPanel("");
 		div.getElement().appendChild(renderer.getDomElement());
 		focusPanel = new FocusPanel();
 		focusPanel.add(div);
 		main.getCenter().add(focusPanel);
 		focusPanel.addKeyDownHandler(new KeyDownHandler() {
 			
 			@Override
 			public void onKeyDown(KeyDownEvent event) {
 				apeDemo.keyDown(event);
 			}
 		});
 		focusPanel.addKeyUpHandler(new KeyUpHandler() {
 			
 			@Override
 			public void onKeyUp(KeyUpEvent event) {
 				apeDemo.KeyUp(event);
 			}
 		});
 		focusPanel.setFocus(true);
 		
 		main.getWebglButton().addClickHandler(new ClickHandler() {
 			@Override
 			public void onClick(ClickEvent event) {
 				switchRenderer("webgl");
 			}
 		});
 		main.getCanvasButton().addClickHandler(new ClickHandler() {
 			@Override
 			public void onClick(ClickEvent event) {
 				switchRenderer("canvas");
 			}
 		});
 		main.getCss3dButton().addClickHandler(new ClickHandler() {
 			@Override
 			public void onClick(ClickEvent event) {
 				switchRenderer("css3d");
 			}
 		});
 		
 		/*
 		canvas = Canvas.createIfSupported();
 		canvas.setSize("600px", "600px");
 		canvas.setCoordinateSpaceWidth(600);
 		canvas.setCoordinateSpaceHeight(600);
 		
 		root.add(canvas);
 		*/
 		final Stats stats=Stats.insertStatsToRootPanel();
 		stats.domElement().getStyle().setWidth(90.0, Unit.PX);
 		//stats.setPosition(8, 0);
 		
 		Timer timer=new Timer(){
 
 			@Override
 			public void run() {//wait?
 				long t=System.currentTimeMillis();
 				//LogUtils.log(""+(t-last));
 				last=t;
 				stats.begin();
 				if(doInit){
 					//LogUtils.log("init");
 					init();
 					doInit=false;
 				}
 				
 				updateCanvas();
 				stats.end();
 			}
 
 			
 			
 		};
 		timer.scheduleRepeating(1000/60);
 		
 		
 		updateCanvas();
 		
 
 	HorizontalPanel buttons=new HorizontalPanel();	
 	buttons.setVerticalAlignment(HorizontalPanel.ALIGN_MIDDLE);
 	
 	ValueListBox<Box2dDemo> demosList=new ValueListBox<Box2dDemo>(new Renderer<Box2dDemo>() {
 
 		@Override
 		public String render(Box2dDemo object) {
 			if(object==null){
 				return null;
 			}
 			return object.getName();
 		}
 
 		@Override
 		public void render(Box2dDemo object, Appendable appendable)
 				throws IOException {
 			
 		}
 	});
 	demosList.addValueChangeHandler(new ValueChangeHandler<Box2dDemo>() {
 		
 		@Override
 		public void onValueChange(ValueChangeEvent<Box2dDemo> event) {
 			
 			
 			apeDemo=event.getValue();
 			init();//remove
 		}
 	});
 	demosList.setValue(demos.get(0));
 	demosList.setAcceptableValues(demos);
 	
 	buttons.add(demosList);
 	
 	apeDemo=demos.get(0);
 	
 	
 	
 Button init=new Button("Restart",new ClickHandler() {
 			
 			@Override
 			public void onClick(ClickEvent event) {
 				doInit=true;
 				focusPanel.setFocus(true);
 			}
 		});
 		main.getButtons().add(buttons);
 		buttons.add(init);
 		
 		
 		init();
 	}
 	
 	long last;
 	
 	boolean doInit;
 	public static Map<Object,Integer> colorMap=new HashMap<Object, Integer>();
 	
 	private void init(){
 		
 		main.getControler().clear();
 		
 		threeObjects.clear();
 		
 		
 		if(renderer.gwtGetType().equals("css3d")){
 		((CSS3DRenderer)renderer).gwtClear();
 		//renderer.getDomElement().getStyle().setPosition(Position.ABSOLUTE);
 		//renderer.getDomElement().getStyle().setTop(0,Unit.PX);
 		}
 		
 		scene = THREE.Scene();
 		scene.add(camera);
 		
 		objRoot=THREE.Object3D();
 		objRoot.setPosition(-width/2, 0, 0);
 		scene.add(objRoot);
 		
 		scene.add(light);	
 		
 		
 		if(apeDemo!=null){
 			apeDemo.initialize();
 			if(apeDemo.createControler()!=null){
 				main.getControler().add(apeDemo.createControler());
 			}
 			}
 		
 	}
 	
 	private void updateCanvas() {
 		if(apeDemo==null){
 			return;//useless
 		}
 		
 		
 		World world=apeDemo.getWorld();
 		for(Body b=world.getBodyList();b!=null;b=b.getNext()){
 			drawBody(b);
 		}
 		for(Joint joint=world.getJointList();joint!=null;joint=joint.getNext()){
 			drawJoint(joint);
 		}
 		
 		
 		renderer.render(scene, camera);
 		
 		
 		apeDemo.step();
 		
 	}
 	
 	Vec2 jointA=new Vec2();
 	Vec2 jointB=new Vec2();
 	Vec2 jointCenter=new Vec2();
 	private void drawJoint(Joint joint) {
 		joint.getAnchorA(jointA);
 		joint.getAnchorB(jointB);
 		
 		float radian=Box2DUtils.calculateRadian(jointA, jointB);
 		Box2DUtils.getCenter(jointA, jointB,jointCenter);
 		
 		Object3D obj=threeJoints.get(joint);
 		if(obj==null){
			Canvas dotCanvas=CanvasUtils.createCanvas(1, 2);
 			dotCanvas.getContext2d().setFillStyle("#008");
 			dotCanvas.getContext2d().fillRect(0, 0, 1, 2);
 			obj=createCanvasObject(dotCanvas,dotCanvas.getCoordinateSpaceWidth(),dotCanvas.getCoordinateSpaceHeight());
 			threeJoints.put(joint, obj);
 			objRoot.add(obj);
 		}
 		
 		if(joint instanceof DistanceJoint){
 			float length=((DistanceJoint)joint).getLength();
			obj.setScale(length*scale, 1, 1);
 		}
 		
 		
 		obj.setPosition(jointCenter.x*scale, -jointCenter.y*scale, 0);
 		obj.getRotation().setZ(-radian);
 		
 	}
 	private void drawBody(Body body) {
 		Object3D obj=threeObjects.get(body);
 		if(obj==null){
 		Canvas bodyCanvas=createBodyCanvas(body,"#800",true);
 		obj=createCanvasObject(bodyCanvas,bodyCanvas.getCoordinateSpaceWidth(),bodyCanvas.getCoordinateSpaceHeight());
 		threeObjects.put(body, obj);
 		//LogUtils.log("create object:"+bodyCanvas.getCoordinateSpaceWidth()+","+bodyCanvas.getCoordinateSpaceHeight());
 		objRoot.add(obj);
 		//obj.setScale(scale, scale, scale);
 		}
 		Vec2 pos=body.getPosition();
 		obj.setPosition(pos.x*scale, -pos.y*scale, 0);
 		obj.getRotation().setZ(-body.getAngle());
 		
 	}
 	private Object3D createColorCircleObject(int r,int g,int b,double alpha,int radius,boolean stroke){
 		Object3D object;
 		Canvas canvas=CanvasUtils.createCircleImageCanvas(r, g, b, alpha, (int)(radius), 3,stroke);
 		Texture texture=THREE.Texture(canvas.getCanvasElement());
 		texture.setNeedsUpdate(true);
 		if(!renderer.gwtGetType().equals("css3d")){
 			MeshBasicMaterialBuilder basicMaterial=MeshBasicMaterialBuilder.create().map(texture);
 			object=THREE.Mesh(THREE.PlaneGeometry(radius*2, radius*2), 
 					basicMaterial.build());
 		}else{
 			Image img=new Image(canvas.toDataUrl());
 			object=CSS3DObject.createObject(img.getElement());
 		}
 		return object;
 	}
 	
 	private Object3D createCanvasObject(Canvas canvas,int w,int h){
 		Object3D object;
 		Texture texture=THREE.Texture(canvas.getCanvasElement());
 		texture.setNeedsUpdate(true);
 		if(!renderer.gwtGetType().equals("css3d")){
 			MeshBasicMaterialBuilder basicMaterial=MeshBasicMaterialBuilder.create().map(texture);
 			object=THREE.Mesh(THREE.PlaneGeometry(w, h), 
 					basicMaterial.build());
 		}else{
 			VerticalPanel v=new VerticalPanel();
 			v.setSize(w+"px", h+"px");
 			Image img=new Image(canvas.toDataUrl());
 			v.add(img);
 			//LogUtils.log("img:"+img.getWidth()+":"+img.getHeight());
 			object=CSS3DObject.createObject(v.getElement());
 		}
 		return object;
 	}
 	
 	private Object3D createColorRectObject(int r,int g,int b,double alpha,int width,int height){
 		Object3D object;
 		if(!renderer.gwtGetType().equals("css3d")){
 			MeshBasicMaterialBuilder basicMaterial=MeshBasicMaterialBuilder.create().color(r,g,b).opacity(alpha)
 					.transparent(true);
 			object=THREE.Mesh(THREE.PlaneGeometry(width, height), 
 					basicMaterial.build());
 		}else{
 			Image img=new Image(CanvasUtils.createColorRectImageDataUrl(r, g, b, 1, (int)width, (int)height));
 			object=CSS3DObject.createObject(img.getElement());
 		}
 		return object;
 	}
 	
 	
 
 	
 
 
 	private Box2dDemo apeDemo;
 
 	private FocusPanel focusPanel;
 
 	public static MainUi main;
 
 	private Light light;
 	
 
 	
 
 
 
 	
 
 	
 	
 	
 	
 	
 
 	public void drawShape(Body body){
 		Canvas bodyCanvas=createBodyCanvas(body,"#800",true);
 		Vec2 pos=body.getPosition();
 		//canvas.getContext2d().drawImage(bodyCanvas.getCanvasElement(), pos.x-(float)bodyCanvas.getCoordinateSpaceWidth()/2, pos.y-(float)bodyCanvas.getCoordinateSpaceHeight()/2);
 	}
 	
 	public Canvas createBodyCanvas(Body body,String style,boolean stroke){
 		List<Shape> shapes=new ArrayList<Shape>();
 		for(Fixture fixture=body.getFixtureList();fixture!=null;fixture=fixture.getNext()){
 			ShapeType type=fixture.getType();
 		if(type==ShapeType.POLYGON){
 			
 			shapes.add(fixture.getShape());
 			}
 		else if(type==ShapeType.CIRCLE){
 			
 			CircleShape circle=(CircleShape) fixture.getShape();
 			float radius=circle.m_radius;
 			//TODO support circle
 			PolygonShape p=new PolygonShape();
 			p.setAsBox(radius, radius);
 			shapes.add(fixture.getShape());
 			
 		}
 		}
 		//TODO swap y-cordinate
 		AABB aabb=calculateBox(shapes);
 		int w=(int) Math.max(Math.abs(aabb.upperBound.x),Math.abs(aabb.lowerBound.x))*2*scale;
 		int h=(int) Math.max(Math.abs(aabb.upperBound.y),Math.abs(aabb.lowerBound.y))*2*scale;
 		if(w<=0){
 			w=1;
 		}
 		if(h<=0){
 			h=1;
 		}
 		//should totally change w+h;
 		float offx=w/2;
 		float offy=h/2;
 		Canvas canvas=CanvasUtils.createCanvas(w, h);
 		
 		
 		//LogUtils.log("canvas created:"+canvas.getCoordinateSpaceWidth()+","+canvas.getCoordinateSpaceHeight());
 		
 		Context2d context=canvas.getContext2d();
 		context.setLineWidth(2);
 		context.setFillStyle("#eee");
 		//context.fillRect(0, 0, canvas.getCoordinateSpaceWidth(), canvas.getCoordinateSpaceHeight());
 		for(Fixture fixture=body.getFixtureList();fixture!=null;fixture=fixture.getNext()){
 			ShapeType type=fixture.getType();
 			if(type==ShapeType.POLYGON){
 				PolygonShape poly=(PolygonShape) fixture.getShape();
 				int size=poly.m_vertexCount;
 				context.beginPath();
 				float px=(poly.m_vertices[0].x)*scale+offx;
 				float py=(poly.m_vertices[0].y*1)*scale+offy;
 				//LogUtils.log("moveTo:"+px+","+py);
 				context.moveTo(px, py);
 				for(int i=1;i<size;i++){
 					px=(poly.m_vertices[i].x)*scale+offx;
 					py=(poly.m_vertices[i].y*1)*scale+offy;
 					//LogUtils.log("lineTo:"+px+","+py);
 					context.lineTo(px,py );
 				}
 				px=(poly.m_vertices[0].x)*scale+offx;
 				py=(poly.m_vertices[0].y*1)*scale+offy;
 				//LogUtils.log("lineTo:"+px+","+py);
 				context.lineTo(px,py);
 				
 				context.closePath();
 				if(stroke){
 					context.setStrokeStyle(style);
 					context.stroke();
 				}else{
 					context.setFillStyle(style);
 					context.fill();
 				}
 			}else if(type==ShapeType.CIRCLE){
 				//LogUtils.log("draw circle");
 				CircleShape circle=(CircleShape) fixture.getShape();
 				float radius=circle.m_radius;
 				context.beginPath();
 				context.arc((float)w/2, (float)h/2, radius*scale, 0, 360);
 				context.closePath();
 				if(stroke){
 					context.setStrokeStyle(style);
 					context.stroke();
 				}else{
 					context.setFillStyle(style);
 					context.fill();
 				}
 			}
 			
 			}
 		
 		
 		return canvas;
 	}
 	
 	//calculate multiple
 	public AABB calculateBox(List<Shape> polygons){
 		List<Vec2> points=new ArrayList<Vec2>();
 		for(Shape shape:polygons){
 			if(shape.getType()==ShapeType.POLYGON){
 				PolygonShape polygon=(PolygonShape) shape;
 			int vertexCount=polygon.m_vertexCount;
 			for (int i = 0; i < vertexCount; ++i) {
 				points.add(polygon.m_vertices[i]);
 			}
 			}else if(shape.getType()==ShapeType.CIRCLE){
 				CircleShape circle=(CircleShape)shape;
 				float radius=circle.m_radius;
 				points.add(new Vec2(-radius, -radius));
 				points.add(new Vec2(radius, -radius));
 				points.add(new Vec2(-radius, radius));
 				points.add(new Vec2(radius, radius));
 			}
 		}
 		if(points.size()==0){
 			return new AABB();
 		}
 		
 		float minX=points.get(0).x;
 		float minY=points.get(0).y;
 		float maxX=points.get(0).x;
 		float maxY=points.get(0).y;
 		for (int i = 1; i < points.size(); ++i) {
 			Vec2 v=points.get(i);
 				if(v.x<minX){
 					minX=v.x;
 				}
 				if(v.y<minY){
 					minY=v.y;
 				}
 				if(v.x>maxX){
 					maxX=v.x;
 				}
 				if(v.y>maxY){
 					maxY=v.y;
 				}
 		}
 		
 		AABB aabb=new AABB();
 		aabb.lowerBound.x=minX;
 		aabb.lowerBound.y=minY;
 		aabb.upperBound.x=maxX;
 		aabb.upperBound.y=maxY;
 		return aabb;
 	}
 	
 	
 
 }
