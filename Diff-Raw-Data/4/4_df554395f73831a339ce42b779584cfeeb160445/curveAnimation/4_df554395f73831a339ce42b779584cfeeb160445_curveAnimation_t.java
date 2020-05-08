 import processing.core.*; 
 import processing.data.*; 
 import processing.event.*; 
 import processing.opengl.*; 
 
 import java.util.HashMap; 
 import java.util.ArrayList; 
 import java.io.File; 
 import java.io.BufferedReader; 
 import java.io.PrintWriter; 
 import java.io.InputStream; 
 import java.io.OutputStream; 
 import java.io.IOException; 
 
 public class curveAnimation extends PApplet {
 
 /**
  AnimationApp.pde
  Author: Guilherme Herzog
  Created on: May 13
  **/
 
 PFont font; // it's a font
 float curveT;
 
 // Curve
 CurveCat curve;
 CurveCat decimedCurve;
 
 // Selection Box
 PVector mouseInit;
 PVector mouseFinal;
 
 // State Context
 StateContext stateContext;
 
 // Context
 Context context;
 
 // Colours
 int mainColor = 0xff0066C8;
 int secondaryColor = 0xffFF9700;
 int thirdColor = 0xff3990E3;
 
 // Images
 PImage img;
 
 public void setup() 
 {
   size(800, 600);
   smooth();
 
   font = createFont("", 14);
   curveT = 0;
   img = loadImage("play.png");
 
   // PVectors used to create the selection box
   mouseInit = new PVector(0,0);
   mouseFinal = new PVector(0,0);
 
   curveTightness(curveT);
 
   context = new Context();
   update();
   context.setSelectionBox(mouseInit, mouseFinal);
 
   stateContext = new StateContext(context);
   stateContext.setContext(context);
 }
 
 // TODO Mudar isso para um interface s\u00f3 usando o mouse
 public void keyPressed() 
 { 
   update();
   stateContext.keyPressed();
 }
 
 // Mouse press callback
 public void mousePressed() 
 {
   mouseInit.set(mouseX, mouseY);
   mouseFinal.set(mouseX, mouseY);
   update();
   stateContext.mousePressed();
 }
     
 public void mouseReleased()
 {
 
   update();
   stateContext.mouseReleased();
 
   // Resets dragged rectangle
   mouseInit.set(0,0);
   mouseFinal.set(0,0);
   update();
 }
 
 // Mouse drag callback
 public void mouseDragged () 
 {
   update();
   mouseFinal.set(mouseX, mouseY);
   stateContext.mouseDragged();
 }
 
 
 public void draw() 
 {
   update();
   stateContext.draw();
   stateContext.drawInterface();
 }
 
 public void update(){
   context.updateContext(
     new PVector(mouseX, mouseY),
     new PVector(pmouseX, pmouseY), 
     mouseButton,
     keyCode, 
     key,
     mouseInit,
     mouseFinal);
 
     try{
       context.setMouseCount(mouseEvent.getClickCount());
     }catch(NullPointerException e){
       context.setMouseCount(0);
     }
 }
 
 
 class Context{
 	PVector mouse;
 	PVector pMouse;
 	int mouseButton;
 	int keyCode;
 	char key;
 	CurveCat curve;
 	PVector mouseInit;
 	PVector mouseFinal;
 	int[] selectedSegments;
 	int mouseCount;
 	SmoothPositionInterpolator pos;
 	boolean play;
 
 	Context(){
 		selectedSegments = new int[0];
 		this.curve = new CurveCat();
 		this.curve.setTolerance(7);
 
 		pos = new SmoothPositionInterpolator();
 		play = false;
 	}
 
 	public void updateContext(PVector mouse, PVector pmouse, int _mouseButton, int keyCode, char key,
 		PVector _mouseInit, PVector _mouseFinal){
 		this.mouse = mouse;
 		this.pMouse = pmouse;
 		this.keyCode = keyCode;
 		this.key = key;
 		this.mouseButton = _mouseButton;
 		this.mouseInit = _mouseInit;
 		this.mouseFinal = _mouseFinal;
 	}
 
 	public void setMouseCount(int _mouseCount){
 		this.mouseCount = _mouseCount;
 	}
 
 	public void setSelectionBox(PVector ini, PVector _final){
 		this.mouseInit = ini;
 		this.mouseFinal = _final;
 	}
 
 	public void print(){
 		println("Context[");
 		println("this.mouse: "+this.mouse+",");
 		println("this.pMouse: "+this.pMouse+",");
 		println("this.keyCode: "+this.keyCode+",");
 		println("this.key: "+this.key+",");
 		Utils.print_r(selectedSegments);
 	}
 
 	public void diselect(){
 		this.selectedSegments = new int[0];
 	}
 
 	public boolean isPlayed(){
 		return play;
 	}
 
 	public void play(){
 		frameCount = 0;
 		pos.clear();
 
		if(curve.getNumberControlPoints() == 0){
			return;
		}

 
 		for (int i = 0; i<curve.getNumberControlPoints() - 1; i++){
 			PVector p = curve.getControlPoint(i);
 
 			pos.set(p.z, p);
 		}
 
 		play = true;
 	}
 
 	public void refreshInterpolator(){
 
 		if(!this.isPlayed()){
 			return;
 		}
 
 		pos.clear();
 
 		float length = curve.curveLength();
 
 		for (int i = 0; i<curve.getNumberControlPoints() - 1; i++){
 			PVector p = curve.getControlPoint(i);
 
 			pos.set(p.z, p);
 		}
 
 		play = true;
 	}
 
 	public void stop(){
 		play = false;
 	}
 
 }
 //
 // Classe que representa uma curva Catmull-Rom
 //
 class CurveCat
 {
   // Control points
   ArrayList<PVector> controlPoints;
 
   // History of the curve
   ArrayList<ArrayList<PVector>> history;
   int historyIndex = -1;
 
   // If it can be decimed
   boolean decimable;
   float tolerance;
 
   // Number of points that the curve can be show
   int numberDivisions = 1000; 
 
   // Min Ditance wich can be in the curve
   float minDistance = 5;
   int strokeColor = color(0);
 
   CurveCat() 
   {
     controlPoints = new ArrayList<PVector>();
     decimable = true;
     tolerance = 7;
 
     history = new ArrayList<ArrayList<PVector>>();
   }
 
   public void clear()
   {
     saveCurve();
     decimable = true;
     controlPoints = new ArrayList<PVector>();
   }
 
   public void removeElement(int index){
     saveCurve();
     if (controlPoints.size()>1)
       controlPoints.remove(index);
   }
 
   public Segment getSegment(ArrayList<PVector> pAux, int i)
   { 
          PVector a = i >= 1 ? pAux.get(i-1) : pAux.get(0);
          PVector b = pAux.get(i);
          PVector c = pAux.get(i+1);
          PVector d = i+2 < pAux.size() ? pAux.get(i+2) : pAux.get(i+1);
          return new Segment(a,b,c,d);
   }
 
   public Segment getSegment(int i)
   { 
          return getSegment(controlPoints,i);
   }
 
   // M\u00e9todo que retorna os principais controlPoints que s\u00e3o essenciais para a curva
   public ArrayList<PVector> DouglasPeuckerReducing(ArrayList<PVector> cpoints, float epsilon){
     float maxDistance = 0, distance = 0;
     int index = 0;
     int end = cpoints.size();
     ArrayList<PVector> result;
 
     for (int i = 2; i < end - 1; ++i) {
       distance = shortestDistanceToSegment(cpoints.get(i), cpoints.get(1), cpoints.get(end - 1));
       if( distance > maxDistance){
         maxDistance = distance;
         index = i;
       }
     }
 
     if(maxDistance > epsilon){
       ArrayList<PVector> results1, results2;
 
       // Fiz isso aqui porque n\u00e3o posso modificar o cpoints
       ArrayList<PVector> tmp = new ArrayList<PVector>();
       for (int i = index; i < end - 1; ++i) {
           tmp.add(cpoints.get(i));
       }
       results1 = DouglasPeuckerReducing(tmp, epsilon);
 
       // Fiz isso aqui porque n\u00e3o posso modificar o cpoints
       tmp = new ArrayList<PVector>();
       for (int i = 1; i < index; ++i) {
           tmp.add(cpoints.get(i));
       }
       results2 = DouglasPeuckerReducing(tmp, epsilon);
 
       // Concatenando dois arrays, por que tinha que ser t\u00e3o dif\u00edcil ? Custava retornar o array novo ?
       results1.addAll(results2);
       result = (ArrayList<PVector>) results1.clone();
     }else{
       result = cpoints;
     }
 
     return result;
   }
 
   // M\u00e9todo para percorrer um segmento de reta que come\u00e7a em segBegin e terminar em segEnd vendo qual menor distancia para o vetor cpoint
   public float shortestDistanceToSegment(PVector cpoint, PVector segBegin, PVector segEnd){
     PVector tmp = (PVector) segEnd.get();
     tmp.sub(segBegin);
 
     int numberDivisions = 1000;
     float delta = tmp.mag()/numberDivisions;
 
     float distance = 99999;
 
     for (int i = 0; i < numberDivisions; ++i) {
         tmp = segEnd.get();
         tmp.mult(i*delta);
         tmp = PVector.add(segBegin, tmp);
         if(tmp.dist(cpoint) < distance){
           distance = tmp.dist(cpoint);
         }
     }
 
     return distance;
   }
 
   // Remove pontos de controle de uma curva criada pela lista p que possuam distancia menor que a tolerancia em rela\u00e7\u00e3o aos pontos da nova curva.
   public void decimeCurve(float tolerance)
   {
       PVector a = new PVector();
       PVector b = new PVector();
       PVector c = new PVector();
       PVector d = new PVector();
       
       PVector a2 = new PVector();
       PVector b2 = new PVector();
       PVector c2 = new PVector();
       PVector d2 = new PVector();
       
       boolean remove;
      
       int size = controlPoints.size() - 1;
       
       Segment segAux;
       Segment segP;
       ArrayList<PVector> pAux;
 
       boolean wasDecimed = false;
 
       // Pego os vetores essenciais para a curva
       ArrayList<PVector> essentials = DouglasPeuckerReducing(controlPoints, 0.5f);
 
       // Array que vai conter os vetores a serem testados
       ArrayList<PVector> testableControlPoints = (ArrayList<PVector>) controlPoints.clone();
 
       // Removendo os pontos essenciais dos test\u00e1veis
       for (int i = 0; i < essentials.size(); ++i) {
         testableControlPoints.remove(essentials.get(i));
       }
 
       // Percorre os test\u00e1veis removendo e verificando com a toler\u00e2ncia.
       for(int i = 1; i < testableControlPoints.size() - 1; i++){
 
          pAux = new ArrayList<PVector>(controlPoints.size());
          pAux = (ArrayList<PVector>) controlPoints.clone();
 
          // Pega o vetor e procura qual o indice dele nos controlPoints
          int index = controlPoints.indexOf( testableControlPoints.get(i) );
          pAux.remove(index);
          segAux = getSegment(pAux,index-1);
          remove = true;
          
          for (int j=0; j<=numberDivisions; j++) 
          {
             float t = (float)(j) / (float)(numberDivisions);
             float tAux;
             if (t < 0.5f)
             {
                  segP = getSegment(controlPoints,index-1);
                  tAux = t*2;     
             } 
             else 
             {
                 segP = getSegment(controlPoints,index);
                 tAux = t*2 - 1;
             }
             
             float x = curvePoint(segAux.a.x, segAux.b.x, segAux.c.x, segAux.d.x, t);
             float y = curvePoint(segAux.a.y, segAux.b.y, segAux.c.y, segAux.d.y, t);
             PVector v1 = new PVector(x,y);
 
             float x2 = curvePoint(segP.a.x, segP.b.x, segP.c.x, segP.d.x, tAux);
             float y2 = curvePoint(segP.a.y, segP.b.y, segP.c.y, segP.d.y, tAux);
             PVector v2 = new PVector(x2,y2);
 
             float dist = v1.dist(v2);
             if(dist >= tolerance){
                remove = false;
             }
          }
          
          if(remove){
            this.controlPoints.remove(index);
            wasDecimed = true;
          }
          
       }
 
       this.decimable = wasDecimed;
   }
 
   public void decimeAll(){
     saveCurve();
     while(this.canBeDecimed()){
       this.decimeCurve(this.tolerance);
     }  
   }
 
   public void setTolerance(float t){
     this.tolerance = t;
   }
   
 
   public boolean canBeDecimed(){
     return this.decimable;
   }
 
   public int getNumberControlPoints () { 
       return controlPoints.size();
   } 
 
   // Insere o ponto q entre index-1 e index
   public void insertPoint(PVector q, int index){
     saveCurve();
     controlPoints.add(index,q);
     this.decimable = true;
   }
 
   public void insertPoint(PVector q){
     saveCurve();
     controlPoints.add(q);
     this.decimable = true;
   }
 
   // Altera o valor do elemento index da lista p para q
   public void setPoint(PVector q, int index)
   {
     try {
       controlPoints.set(index,q);    
     } catch (Exception e) {
         //print("Erro ao setar ponto de controle");
     }
   }
 
   // Retorna as coordenadas (X,Y) para de uma lista de PVectors p dado o index.
   public PVector getControlPoint(int index)
   {
     if (controlPoints.size() > index && index >-1)
       return controlPoints.get(index);
     else
       return new PVector(0,0);
   }
   
   // Retorna o indice do ponto de controle mais pr\u00f3ximo de q. Caso
   // este n\u00e3o esteja a uma distancia minima especificada por minDistance,
   // retorna -1
   public int findControlPoint(PVector q)
   {
     int op=-1;
     float bestDist = 100000;
     for (int i = 0; i < getNumberControlPoints(); i++) 
     {
       float d = controlPoints.get(i).dist(q);
       if (d < minDistance && d < bestDist) 
       { 
         bestDist = d;
         op = i;
       }
     }
     return op;
   }
 
   // Outra interface para findControlPoint, passando as coordenadas
   // do ponto na lista de argumentos
   public int findControlPoint (float x, float y) {
     return findControlPoint (new PVector (x, y));
   }
 
   // Outra interface para findControlPoint, passando as coordenadas do mouse
   public int findControlPoint () {
     return findControlPoint (mouseX, mouseY);
   }
 
   //
   // Retorna o indice do segmento da curva onde o ponto mais proximo de q foi 
   // encontrado. As coordenadas do ponto mais proximo s\u00e3o guardadas em r
   // 
   public int findClosestPoint (ArrayList<PVector> cps, PVector q, PVector r) {
 
     int bestSegment = -1;
     float bestDistance = 10000000;
     float bestSegmentDistance = 100000;
     
     for (int i = 0; i < cps.size()-1; i++) {
       Segment seg = getSegment(i);
 
       PVector result = new PVector();
       for (int j=0; j<=numberDivisions/2; j++) 
       {
         float t = (float)(j) / (float)(numberDivisions);
         float x = curvePoint(seg.a.x, seg.b.x, seg.c.x, seg.d.x, t);
         float y = curvePoint(seg.a.y, seg.b.y, seg.c.y, seg.d.y, t);
         float dist = dist (x, y, q.x, q.y);
 
         if (j == 0 || dist < bestSegmentDistance) {
           bestSegmentDistance = dist;
           result.set(x, y, 0);
         }
       }
       if (bestSegmentDistance < bestDistance) {
         r.set (result.x, result.y, 0);
         bestSegment = i;
         bestDistance = bestSegmentDistance;
       }
     }
     return bestSegment;
   }
 
   public int[] getControlPointsBetween(PVector init, PVector pFinal){
     PVector aux;
 
     ArrayList<Integer> result = new ArrayList<Integer>();
     for (int i = 0; i<controlPoints.size() ; i++){
       PVector controlPoint = controlPoints.get(i);
 
       float dist1 = controlPoint.dist(init);
       float dist2 = controlPoint.dist(pFinal);
 
       if(pow(dist1,2) + pow(dist2,2) <= pow(init.dist(pFinal),2)){
         result.add(i);
       }
     }
 
     int[] r = new int[result.size()];
 
     for (int i = 0; i<result.size(); i++){
         r[i] = result.get(i);
     }
 
     return r;
   }
 
   /** FIM DOS M\u00c9TODOS DE EDI\u00c7\u00c3O E CRIA\u00c7\u00c3O **/
 
   /** M\u00c9TODOS PARA PARAMETRIZA\u00c7\u00c3O DE UMA CURVA **/
 
   // Retorna o tamanho de uma curva dados uma lista de pontos de controle
   public float curveLength()
   {
     float curveLength = 0;
     for (int i = 0; i < getNumberControlPoints()-1; i++) {
       Segment seg = getSegment(i);
 
       for (int j=0; j<=numberDivisions; j++) 
       {
         float t = (float)(j) / (float)(numberDivisions);
         float x = curvePoint(seg.a.x, seg.b.x, seg.c.x, seg.d.x, t);
         float y = curvePoint(seg.a.y, seg.b.y, seg.c.y, seg.d.y, t);
         t = (float)(j+1) / (float)(numberDivisions);
         float x2 = curvePoint(seg.a.x, seg.b.x, seg.c.x, seg.d.x, t);
         float y2 = curvePoint(seg.a.y, seg.b.y, seg.c.y, seg.d.y, t);
         float dist = dist (x, y, x2, y2);
         curveLength += dist;
       }
     }
     return (float)curveLength;
   }
 
   public float curveLengthBetweenControlPoints(int pBegin, int pEnd)
   {
     float curveLength = 0;
     for (int i = pBegin; i < pEnd; i++) {
       Segment seg = getSegment(i);
 
       for (int j=0; j<=numberDivisions; j++) 
       {
         float t = (float)(j) / (float)(numberDivisions);
         float x = curvePoint(seg.a.x, seg.b.x, seg.c.x, seg.d.x, t);
         float y = curvePoint(seg.a.y, seg.b.y, seg.c.y, seg.d.y, t);
         t = (float)(j+1) / (float)(numberDivisions);
         float x2 = curvePoint(seg.a.x, seg.b.x, seg.c.x, seg.d.x, t);
         float y2 = curvePoint(seg.a.y, seg.b.y, seg.c.y, seg.d.y, t);
         float dist = dist (x, y, x2, y2);
         curveLength += dist;
       }
     }
     return (float)curveLength;
   }
 
   public void reAmostragem()
   {
     CurveCat aux = new CurveCat();
     int index = 0;
     for (int i = 0; i < getNumberControlPoints()-1; i++) {
       Segment seg = getSegment(i);
 
       for (int j=0; j<=numberDivisions; j++) 
       {
         float t = (float)(j) / (float)(numberDivisions);
         float x = curvePoint(seg.a.x, seg.b.x, seg.c.x, seg.d.x, t);
         float y = curvePoint(seg.a.y, seg.b.y, seg.c.y, seg.d.y, t);
 
         aux.insertPoint(new PVector(x,y), index);
         index++;
       }
     }
 
     this.controlPoints = aux.controlPoints;
 
     this.decimable = true;
   }
 
   public void decimeCurve(){
     this.decimeCurve(this.tolerance);
   }
 
   public void saveCurve(){
     if(history.size() > 0){
       if(history.get(history.size() - 1).equals(controlPoints))
         return;
     }
     ArrayList<PVector> branch = (ArrayList<PVector>) controlPoints.clone();
     history.add(branch);
     historyIndex++;
   }
 
   public void undo(){
     if(historyIndex == history.size() - 1){
       saveCurve();
     }
     historyIndex--;
     update();
   }
 
   public void redo(){
     if(historyIndex + 1 < history.size() && historyIndex != -1){
       historyIndex++;
       update();
     }
   }
 
   public void update(){
     if(historyIndex != -1 && historyIndex < history.size()){
       controlPoints = history.get(historyIndex);
     }
   }
 
   /**
    M\u00c9TODOS DE DESENHAR
    **/
   // Desenha uma curva de acordo com a lista p de pontos de controle.
   public void draw()
   { 
     stroke(this.strokeColor);
     strokeWeight(1.5f);
     strokeCap(ROUND);
     for (int i = 0; i < getNumberControlPoints() - 1; i++) {
       Segment seg = getSegment(i);
 
       beginShape();
       curveVertex(seg.a.x, seg.a.y);
       curveVertex(seg.b.x, seg.b.y);
       curveVertex(seg.c.x, seg.c.y);
       curveVertex(seg.d.x, seg.d.y);
       endShape();
       // curve (seg.a.x, seg.a.y, seg.b.x, seg.b.y, seg.c.x, seg.c.y, seg.d.x, seg.d.y);
 
       // for (int j=0; j<=1000; j++) 
       // {
       //   float t = (float)(j) / (float)(numberDivisions);
       //   float x = curvePoint(seg.a.x, seg.b.x, seg.c.x, seg.d.x, t);
       //   float y = curvePoint(seg.a.y, seg.b.y, seg.c.y, seg.d.y, t);
       //   t = (float)(j+1) / (float)(numberDivisions);
       //   float x2 = curvePoint(seg.a.x, seg.b.x, seg.c.x, seg.d.x, t);
       //   float y2 = curvePoint(seg.a.y, seg.b.y, seg.c.y, seg.d.y, t);
 
       //   PVector r0 = new PVector(x, y), r1 = new PVector(x2,y2);
       //   r1.sub(r0);
 
       //   pushMatrix();
       //   PVector orthogonal = new PVector((-1)*r1.y, r1.x);
       //   translate( x + (x - x2)/2, y + (y - y2)/2);
 
       //   int escalar = 50;
       //   line(0, 0, orthogonal.x*escalar, orthogonal.y*escalar);
       //   popMatrix();
       // }
     }
   }
 
   // Desenha elipses de acordo com os elementos do tipo PVector da lista p
   public void drawControlPoints()
   {
     fill(secondaryColor);
     stroke(secondaryColor);
     for (int i = 0; i < getNumberControlPoints(); i++) 
     {
       ellipse (controlPoints.get(i).x, controlPoints.get(i).y, 7, 7);
       text("t: "+controlPoints.get(i).z, controlPoints.get(i).x + 10, controlPoints.get(i).y - 10);
     } 
     fill(255);
   }
   public void drawControlPoint(int i)
   {
     fill(mainColor);
     stroke(mainColor);
     if (controlPoints.size() > i && i>-1)
       ellipse(controlPoints.get(i).x, controlPoints.get(i).y, 10, 10);
   }
 
   public CurveCat clone(){
     CurveCat aux = new CurveCat();
     aux.controlPoints = (ArrayList<PVector>) controlPoints.clone();
     return aux;
   }
 
   public String toString(){
     String curve = "Curve: { ControlPoints: [";
     for (int i = 0; i<this.getNumberControlPoints(); i++){
       PVector aux = this.getControlPoint(i);
       curve += "("+aux.x+", "+aux.y+"),";
     }
     curve += "]";
     curve += "}";
     return curve;
   }
 }
 
 class DrawningState extends State {
 
     float distanceToSelect = 5;
     private boolean canSketch;
     float t, ms;
 
     DrawningState(Context _context){
       super(_context);
 
       context.curve.decimeAll();
     }
 
     public void mousePressed() 
     {
       t = 0;
       ms = frameCount;
       // Ent\u00e3o seleciona o mais pr\u00f3ximo
       int selectedSegment = context.curve.findControlPoint(context.mouse);
       // Verifica se o local clicado \u00e9 proximo do final da curva;
       if (selectedSegment == context.curve.getNumberControlPoints()-1){ canSketch = true; }
       else { canSketch = false; }
         
       if (canSketch){
         this.context.curve.insertPoint(this.context.mouse);
       }
     }
     
     public void mouseReleased(PVector mouse) 
     {
         super.mouseReleased();
     	  // Retorna o estado de poder desenhar para FALSE
         canSketch = false;
     }
     public void mouseDragged()
     {	
       float elapsed = 0;
       if(frameCount != ms){
         elapsed = frameCount - ms;
       }
       ms = frameCount;
       t = t + elapsed;
 
       if (canSketch){
         context.mouse.add(new PVector(0,0,t));
   		  context.curve.insertPoint(context.mouse, context.curve.getNumberControlPoints());
       }
     }
 
     public void keyPressed(){
       context.curve.clear(); 
       context.selectedSegments = new int[0];
     }
 
     public void draw()
     {
     	
   	}
 
     public void drawInterface()
     {
       int posX = width-80;
 	    int posY = height-20;
       fill(mainColor);
       stroke(mainColor);
       rect(posX-10,posY-20,80,30);
       fill(255);
       text("Creating", posX, posY);
     }
  
 }
 class EditingState extends State {
 
     int cpsMovimenteds = 5;
 
     PVector originalPositionDragged;
     
     EditingState(Context context){
       super(context);
     }
 
     public void mousePressed() 
     {
         if(context.mouseButton == RIGHT){
 
             // Verfica se tem nenhum element selecionado
             if(context.selectedSegments.length == 0)
             {
               // Ent\u00e3o seleciona o mais pr\u00f3ximo
               PVector closestPoint = new PVector();
               PVector q = new PVector(context.mouse.x, context.mouse.y);
               int selectedSegment = context.curve.findClosestPoint(context.curve.controlPoints, q, closestPoint);
               float distance = q.dist(closestPoint);
               if (distance < distanceToSelect)
               {
                context.selectedSegments = new int[1];
                context.selectedSegments[0] = selectedSegment;
               }
             }
             // Remove todos os segmentos selecionados
             for (int i = context.selectedSegments.length - 1; i>=0; i--){
               context.curve.removeElement(context.selectedSegments[i]);
             }
 
             // Remove a sele\u00e7\u00e3o
             context.diselect();
       }
       else
       {
         // Seleciona o segmento em quest\u00e3o se for o mouse LEFT
         PVector closestPoint = new PVector();
         PVector q = new PVector(context.mouse.x, context.mouse.y);
         int selectedSegment = context.curve.findClosestPoint (context.curve.controlPoints, q, closestPoint);
         //int closestControlPointIndex  = context.curve.findControlPoint(new PVector(context.mouse.x, context.mouse.y));
         PVector closestControlPoint = context.curve.getControlPoint(selectedSegment);
 
         float distanceControlPoint = q.dist(closestControlPoint);
         float distance = q.dist(closestPoint);
 
         // Verifica se a distancia \u00e9 maior do que o limite para selecionar
         if(distance > distanceToSelect)
         {
               context.diselect();
         }
         else
         {
           boolean selected = false;
           
           for (int i = 0; i<context.selectedSegments.length; i++){
             if(selectedSegment == context.selectedSegments[i]){
               selected = true;
               selectedSegment = i;
               break;
             }
           }
 
           if(!selected){
             context.selectedSegments = new int[1];
             context.selectedSegments[0] = selectedSegment;
             selectedSegment = 0;
           }
 
           if(distanceControlPoint > 10){
               context.curve.insertPoint(q, context.selectedSegments[selectedSegment] + 1);
               context.selectedSegments[selectedSegment]++;
           }
         }  
       }
     }
 
     public void mouseReleased() 
     {
         if(context.selectedSegments.length == 0)
         {
             context.selectedSegments = context.curve.getControlPointsBetween(context.mouseInit, context.mouseFinal);
         }
 
         context.refreshInterpolator();
     }
 
     public void mouseDragged()
     {
         context.stop();
 
         if (context.mouseButton == LEFT)
         {
           // Se tiver selecionado v\u00e1rios mant\u00e9m a mesma movimenta\u00e7\u00e3o
           if (context.selectedSegments.length > 1)
           {
             // Pega a varia\u00e7\u00e3o de x e de y
             float dx = context.mouse.x - context.pMouse.x;
             float dy = context.mouse.y - context.pMouse.y;
 
             // Soma aos elementos selecionados
             for (int i = 0; i<context.selectedSegments.length; i++){
               PVector controlPoint = context.curve.getControlPoint(context.selectedSegments[i]);
               context.curve.setPoint(new PVector(controlPoint.x + dx, controlPoint.y + dy, controlPoint.z), context.selectedSegments[i]);
             }
           }else if(context.selectedSegments.length != 0){
 
             // Pega a varia\u00e7\u00e3o de x e de y
             float dx = context.mouse.x - context.pMouse.x;
             float dy = context.mouse.y - context.pMouse.y;
 
             // Soma aos elementos selecionados
             for (int i = -this.cpsMovimenteds; i< this.cpsMovimenteds; i++){
               float tdx;
               float tdy;
               if( i != 0){
                 tdx = dx/(5*abs(i));
                 tdy = dy/(5*abs(i));
               }else{
                 tdx = dx;
                 tdy = dy;
               }
 
               PVector controlPoint = context.curve.getControlPoint(context.selectedSegments[0] + i);
               context.curve.setPoint(new PVector(controlPoint.x + tdx, controlPoint.y + tdy, controlPoint.z), context.selectedSegments[0] + i);
             }
 
           }
         }
 
         context.refreshInterpolator();
     }
 
     public void keyPressed(){
       if(context.selectedSegments.length != 0){
         for (int i = context.selectedSegments.length - 1; i>=0; i--){
           context.curve.removeElement(context.selectedSegments[i]);
         }
 
         context.diselect();  
       }
     }
 
     public void draw()
     {
         context.curve.drawControlPoints();
         if(context.selectedSegments.length == 0)
         {
             // Desenha caixa de sele\u00e7\u00e3o com Alpha 50
             fill(mainColor, 50);
             stroke(mainColor, 50);
             rect(context.mouseInit.x, 
             context.mouseInit.y, 
             context.mouseFinal.x - context.mouseInit.x, 
             context.mouseFinal.y - context.mouseInit.y);
         }
 
         // Draw control points;
         if(context.selectedSegments.length > 0)
         {
             for (int i = 0; i<context.selectedSegments.length; i++)
             {
                 context.curve.drawControlPoint(context.selectedSegments[i]);
             }
         }
     }
 
     public void drawInterface()
     {
         int posX = width-80;
         int posY = height-20;
 
         fill(secondaryColor);
         stroke(secondaryColor);
         rect(posX-10,posY-20,80,30);
         fill(255);
         text("Editing", posX, posY);
     }
  
 }
 // Linearly interpolates properties for a specific
 // time, given values of these properties at 
 // known times (keyframes)
 class Interpolator {
   ArrayList<Float> time;
   ArrayList<Property> prop;
  
   // Constructor
   Interpolator() {
     time = new ArrayList<Float>();
     prop = new ArrayList<Property>();
   }
   
   // Returns the number of keyframes
   public int nKeys () {
     return time.size();
   }
 
   // Return the time for keyframe i
   public float keyTime(int i) {
     return time.get(i);
   }
 
   // Return the property for keyframe i
   public Property keyProp (int i) {
     return prop.get(i);
   }
 
   // Returns the greatest index of time which contains a 
   // value smaller or equal to t. If no such value exists, 
   // returns -1.
   public int locateTime(float t) {
     int i = -1;
     while (i+1 < time.size() && time.get(i+1) <= t) i++;
     return i;
   }
 
   // Sets the property p for time t
   public void set (float t, Property p) {
     int i = locateTime(t);
     if (i >=0 && time.get(i) == t) {
       prop.set(i,p);
     }
     else {
       time.add(i+1,t);
       prop.add(i+1,p);
     }
   }
 
   // Gets the (linearly) interpolated property for time t 
   public Property get(float t) {
     int i = locateTime(t);
     if (i >=0) {
       if (time.get(i) == t) {
         return prop.get(i);
       }
       else if (i+1 < time.size()) {
         float s = norm (t, time.get(i), time.get(i+1));
         Property p = new Property(), a = prop.get(i), b = prop.get(i+1);
         int n = max (a.size(), b.size());
         for (int k = 0; k < n; k++) {
           p.set(k, lerp(a.get(k), b.get(k), s));
         }
         return p;
       }
       else return prop.get(i);
     }
     else {
       assert (time.size() > 0);
       return prop.get(0);
     }
   }
 
   public void clear(){
     time = new ArrayList<Float>();
     prop = new ArrayList<Property>();    
   }
 };
 
 class OverSketchState extends State {
 
     CurveCat aux;
 
     OverSketchState(Context context){
       super(context);
       this.aux = new CurveCat();
     }
 
     public void mousePressed() 
     {
         if(this.context.mouseButton == LEFT){
             // Seleciona o segmento em quest\u00e3o se for o mouse LEFT
             int selectedSegment = context.curve.findControlPoint(new PVector(context.mouse.x, context.mouse.y));
 
             PVector closestPoint = new PVector();
             PVector q = new PVector(context.mouse.x, context.mouse.y);
             selectedSegment = context.curve.findClosestPoint (context.curve.controlPoints, q, closestPoint);
             float distance = q.dist(closestPoint);
 
             if(distance > distanceToSelect){
                   context.diselect();
             }
 
             context.curve.insertPoint(q, selectedSegment + 1);
             selectedSegment++;
 
             context.selectedSegments = new int[1];
             context.selectedSegments[0] = selectedSegment;
 
             this.aux = new CurveCat();
             this.aux.strokeColor = color(0,0,0,50);
 
             for (int i = 0; i<selectedSegment; i++){
                 q = context.curve.getControlPoint(i);
                 this.aux.insertPoint(q, i);
             }
 
             mouseInit.set(0, 0);
             mouseFinal.set(0, 0);
         }
     }
 
     public void mouseReleased() 
     {
         if(this.aux.getNumberControlPoints() == 0){
             return;
         }
         int selectedSegment = context.curve.findControlPoint(new PVector(context.mouse.x, context.mouse.y));
 
         PVector closestPoint = new PVector();
         PVector q = new PVector(context.mouse.x, context.mouse.y);
         selectedSegment = context.curve.findClosestPoint (context.curve.controlPoints, q, closestPoint);
         float distance = q.dist(closestPoint);
 
         if(distance > distanceToSelect){
               context.diselect();
         }
 
         context.selectedSegments = new int[1];
         context.selectedSegments[0] = selectedSegment;
 
         context.curve.insertPoint(q, selectedSegment + 1);
         selectedSegment++;
 
         for (int i = selectedSegment; i<context.curve.getNumberControlPoints(); i++){
             q = context.curve.getControlPoint(i);
             this.aux.insertPoint(q, this.aux.getNumberControlPoints());
         }
 
         context.curve = null;
         context.curve = aux;
 
         context.curve.strokeColor = color(0);
 
         super.mouseReleased();
     }
 
     public void mouseDragged()
     {
         if(context.mouseButton == LEFT){
             this.aux.insertPoint(context.mouse, this.aux.getNumberControlPoints());
         }
     }
 
     public void keyPressed(){
 
     }
 
     public void draw()
     {
         if (this.aux.getNumberControlPoints() >=4) 
             this.aux.draw();
 
         //context.curve.drawControlPoints();
         if(context.selectedSegments.length == 0)
         {
             // Desenha caixa de sele\u00e7\u00e3o com Alpha 50
             fill(mainColor, 50);
             stroke(mainColor, 50);
             rect(context.mouseInit.x, 
               context.mouseInit.y, 
               context.mouseFinal.x - context.mouseInit.x, 
               context.mouseFinal.y - context.mouseInit.y);
         }
 
         // Draw control points;
         if(context.selectedSegments.length > 0)
         {
             for (int i = 0; i<context.selectedSegments.length; i++)
             {
                 //context.curve.drawControlPoint(context.selectedSegments[i]);
             }
         }
     }
     @Override
     public void drawInterface()
     {
         int posX = width-80;
         int posY = height-20;
 
         fill(secondaryColor);
         stroke(secondaryColor);
         rect(posX-10,posY-20,80,30);
         fill(255);
         text("OverSketch", posX, posY);
     }
  
 }
 // A property is an array of floats representing a
 // multidimensional point
 class Property extends ArrayList<Float> {
   
   // An empty property
   Property() {
     super();
   }
 
   // A one-float property
   Property (float a) {
     super();
     set (0,a);
   }
 
   // A two-float property
   Property (float a, float b) {
     super();
     set (0,a);
     set (1,b);
   }
 
   // A three-float property
   Property (float a, float b, float c) {
     super();
     set (0,a);
     set (1,b);
     set (2,c);
   }
 
   // Sets the i'th dimension of the property
   // to value v
   public void set(int i, float v) {
      assert(i>=0);
      while (i >= size()) add(0.0f);
      super.set(i,v);
   }
 
   // Returns the i'th dimension of the property.
   // Returns 0.0 if that dimension was never set
   public Float get(int i) {
     assert (i>=0);
     if (i >= size()) return 0.0f;
     return super.get(i);
   }
 };
 class Segment{
    PVector a,b,c,d;
   
    Segment(PVector _a, PVector _b, PVector _c, PVector _d){
       a = _a;
       b = _b;
       c = _c;
       d = _d;
    } 
    
    Segment(){
    
    }
   
 }
 // Smooth (Cubic) interpolation of properties
 class SmoothInterpolator extends Interpolator {
 
   // Gets the Catmull-Rom interpolated property for time t 
   public Property get(float t) {
     int i = locateTime(t);
     if (i >= 0) {
       if (time.get(i) == t) return prop.get(i);
       if (i+1 < time.size()) {
         // Compute the 4 points that will be used
         // to interpolate the property 
         Property a,b,c,d;
         a = b = prop.get(i); 
         c = d = prop.get(i+1); 
         if (i > 0) a = prop.get(i-1); 
         if (i+2 < time.size()) d = prop.get(i+2);
         // Interpolate the parameter
         float s = norm (t, time.get(i), time.get(i+1)); 
         // Now interpolate the property dimensions
         Property p = new Property(); 
         int n = max (a.size(), b.size());
         for (int k = 0; k < n; k++) {
           p.set(k, curvePoint(a.get(k), b.get(k), c.get(k), d.get(k), s));
         }
         return p;
       }
       else return prop.get(i);
     }
     else {
       assert (time.size() > 0);
       return prop.get(0);
     }
   }
 
 };
 
 // Wraps a interpolator class so that 
 // methods return PVectors representing positions rather 
 // than generic properties
 class SmoothPositionInterpolator {
   
   // The interpolator being wrapped
   SmoothInterpolator interp;
   
   // Constructor
   SmoothPositionInterpolator () {
     this.interp = new SmoothInterpolator();
   }
 
   // Converts a property to a PVector
   public PVector toPVector (Property p) {
     return new PVector (p.get(0), p.get(1), p.get(2));
   }
   
   // Returns the number of keyframes
   public int nKeys () {
     return interp.time.size();
   }
 
   // Return the time for keyframe i
   public float keyTime(int i) {
     return interp.time.get(i);
   }
 
   // Return the property for keyframe i
   public PVector keyPos (int i) {
     return toPVector(interp.prop.get(i));
   }
 
   // Sets the position for time t
   public void set (float t, PVector p) { 
     interp.set(t, new Property (p.x, p.y, p.z));
   }
   
   // Gets the position at time t
   public PVector get (float t) {
     return toPVector (interp.get(t));
   }
   
   // Returns the estimated tangent (a unit vector) at point t
   public PVector getTangent (float t) {
     PVector tan = (t < 0.01f) ?
                    PVector.sub(get(t+0.01f),get(t)) :
                    PVector.sub(get(t),get(t-0.01f));
     tan.normalize();
     return tan;
   }
   
   // Draws key frames as circles and the curve 
   // as n segments equally spaced in time
   public void draw(int n) {
     pushStyle();
     noFill();
     float tickSize = 5;
     float tMax = keyTime(nKeys()-1);
     PVector p0 = get(0);
     for (int i = 0; i < n; i++) {
       float t = (float) i * tMax / (n-1);
       PVector p = get(t);
       PVector tan = getTangent(t);
       tan.mult(tickSize);
       line(p0.x,p0.y,p.x,p.y);
       line(p.x-tan.y, p.y+tan.x,p.x+tan.y, p.y-tan.x);
       p0 = p;
     }
     popStyle();
     for (int i = 0; i < interp.nKeys(); i++) {
       Property p = interp.keyProp(i);
       ellipse(p.get(0), p.get(1), 10, 10);
     }
   }
 
   public void clear(){
     interp.clear();
   }
 }
 
 class State
 {
 	Context context;
     // Constants
     final float distanceToSelect = 30;
 
      // Variaveis de Curvas
     int selectedSegment;
     PVector closestPoint;
     PVector q;
 
 	State(Context _context){
 		context = _context;
 	}
 
 	State(){
 		context = new Context();
   	}
 
 	public void mousePressed(){
 
 	};
 	public void mouseDragged(){
 
 	};
 	public void mouseReleased(){
 		context.curve.decimeAll();
 	};
 
 	public void keyPressed(){
 
 	};
 	public void draw(){};
 	public void drawInterface(){};
 }
 	
 public class StateContext {
 
     private State myState;
     private Context context;
     private boolean debug;
 
         /**
          * Standard constructor
          */
     StateContext(Context _context) 
     {
         debug = false;
         setState(new DrawningState(_context));
         
     }
 
     public void setContext(Context _context){
         this.context = _context;
     }
 
     public void debug(){
         debug = !debug;
     }
  
     /**
      * Setter method for the state.
      * Normally only called by classes implementing the State interface.
      * 
      * Devemos criar um m\u00e9todo setState pra cada Estado
      * @param NEW_STATE
      */
     public void setState(final State NEW_STATE) {
         myState = NEW_STATE;
     }
  
     /**
      * Mouse Actions Methods
      * @param  PVector mouse
      */
     public void mousePressed()
     {
         // Verifica se clicou no bot\u00e3o "Clear";
         if(Utils.mouseOverRect(new PVector(mouseX, mouseY),width/2 + 60,height-40, 110, 30)){
             context.curve.clear();
             context.pos.clear();
             context.stop();
             this.setState(new DrawningState(context));
             context.selectedSegments = new int[0];
             return;
         }
 
         if(Utils.mouseOverRect(new PVector(mouseX, mouseY),width-80-130, height-20-20, 110, 30)){
 
             if(this.myState instanceof OverSketchState){
                 this.setState(new EditingState(context));
                 return;
             }
 
             this.setState(new OverSketchState(context));
             context.selectedSegments = new int[0];
             return;
         }
 
         if(Utils.mouseOverRect(new PVector(mouseX, mouseY),20, height-50, 50, 50)){
             if(context.isPlayed())
                 context.stop();
             else
                 context.play(); 
 
             return;
         }
 
         // Seleciona o segmento em quest\u00e3o se for o mouse LEFT
         PVector closestPoint = new PVector();
         PVector q = new PVector(context.mouse.x, context.mouse.y);
         int selectedSegment = context.curve.findClosestPoint (context.curve.controlPoints, q, closestPoint);
         //int closestControlPointIndex  = context.curve.findControlPoint(new PVector(context.mouse.x, context.mouse.y));
         PVector closestControlPoint = context.curve.getControlPoint(selectedSegment);
 
         float distance = q.dist(closestPoint);
 
         if(distance < 10 && !(myState instanceof OverSketchState) && !(myState instanceof EditingState)){
           myState = new EditingState(this.context);
         }
 
         if(selectedSegment == context.curve.getNumberControlPoints() - 2 && distance < 10){
             myState = new DrawningState(this.context);
         }
 
         myState.mousePressed();
     }
     public void mouseDragged()
     {
         myState.mouseDragged();
     }
     public void mouseReleased()
     {
         myState.mouseReleased();
     }
 
     public void keyPressed(){
         switch (context.key){
             case '1' :
               this.setState(new DrawningState(this.context));
             break;  
 
             case '2' :
                 this.setState(new EditingState(this.context));
             break;  
 
             case 'd' :
               this.debug();
             break;  
 
             case 's' :
                 this.context.curve.decimeCurve();
             break;   
 
             case 'p' :
                 if(context.isPlayed())
                     context.stop();
                 else
                     context.play();
             break;
 
             case 'z' :
                 this.context.curve.undo();
             break;         
 
             case 'r' :
                 this.context.curve.redo();
             break;    
 
             // Essa tecla \u00e9 espec\u00edfica para cada estado, entao devemos implement\u00e1-la nas classes de State
             case DELETE :
               myState.keyPressed();
             break;
         }
     }
     public void draw()
     {
         background (255);
         noFill();
         if (context.curve.getNumberControlPoints() >=4) 
             context.curve.draw();
         
         myState.draw();
 
         if(context.isPlayed()){
             float lastTime = context.pos.keyTime(context.pos.nKeys()-1);
             float t = frameCount%PApplet.parseInt(lastTime);
 
             // Essa parte faria parar no final da anima\u00e7\u00e3o
             // if(t == 0)
             //     context.stop();
             
             PVector p = context.pos.get(t);
 
             PVector tan = context.pos.getTangent(t);
             stroke(100,100,100);
             context.pos.draw (100);
             float ang = atan2(tan.y,tan.x);
 
             pushMatrix();
             translate (p.x,p.y);
             rotate (ang);
             noStroke();
             fill(mainColor);
             ellipse(0,0, 20, 20);
             popMatrix();
         }
 
 
     }
     public void drawInterface()
     {
         int posX = width-80;
         int posY = height-20;
         stroke(thirdColor);
         fill(thirdColor);
         rect(width-80-130, height-20-20, 110, 30);
 
         stroke(255);
         fill(255);
         text("OverSkecthing", posX-125, posY);
 
         stroke(thirdColor);
         fill(thirdColor);
         rect(width/2 + 60, height-40, 110, 30);
 
         stroke(255);
         fill(255);
         text("Clear", width/2 + 70, height-20);
 
         myState.drawInterface();
 
         if(debug){
           fill(255,0,0);
           stroke(255,0,0);
           text("Curve Length:"+context.curve.curveLength()+" px", 10, height-20);
           text("Curve Tightness:"+curveT, 10, 20);
           text("Tolerance:"+context.curve.tolerance, 10, 40);
         }
 
         pushMatrix();
         translate(20, height-50);
         image(img, 0, 0);
         popMatrix();
     }
 }
 static class Utils{
   
   public static void printArrayPVector(PVector[] p)
   {
     for (int i=0;i<p.length-1;i++)
       println(i+" "+p[i]);
   }
 
   public static boolean mouseOverRect(PVector mouse, int x, int y, int w, int h) {
   	return (mouse.x >= x && mouse.x <= x+w && mouse.y >= y && mouse.y <= y+h);
   }
 
   public static void pvectorArrayCopy(PVector[] src, PVector[] dest){
   	for (int i = 0; i<src.length; i++){
   		dest[i] = src[i];
   	}
   }
 
   public static void print_r(int[] array){
     for (int i = 0; i<array.length; i++){
       println(array[i]);
     }
   }
 }
   static public void main(String[] passedArgs) {
     String[] appletArgs = new String[] { "curveAnimation" };
     if (passedArgs != null) {
       PApplet.main(concat(appletArgs, passedArgs));
     } else {
       PApplet.main(appletArgs);
     }
   }
 }
