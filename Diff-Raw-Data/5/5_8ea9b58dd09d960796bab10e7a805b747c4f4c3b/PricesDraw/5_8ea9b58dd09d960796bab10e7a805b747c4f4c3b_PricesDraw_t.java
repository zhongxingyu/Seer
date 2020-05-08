 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package trader.graph;
 
 import java.awt.Color;
 import java.awt.Graphics;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.TreeMap;
 import quote.Prices;
 
 /**
  *
  * @author fiber
  */
 public class PricesDraw {
     private TreeMap<Long, Prices> graphPrices;
     private final Double graphYMarginRate = 0.2; // 10% graph margin, both top and bottom -> 20%
     private final String[] monthName = {"JAN", "FEB", "MAR", "APR", "MAY", "JUN", "JUL", "AUG", "SEP", "OCT", "NOV", "DEC"};
     public static final int GRAPH_STYLE_LINE   = 1
                            ,GRAPH_STYLE_CANDLESTICK    = 2;
     private int graphStyle;
     
     
     Graphics g;
     int marginLeft,marginTop,graphSizeX,graphSizeY;
     Double scale;
     long firstDrawPoint;
                     
                     
     
     public PricesDraw(TreeMap<Long, Prices> graphPrices){
         this.graphPrices = graphPrices;
     }
     
     public void setGraphStyle(int graphStyle){
         this.graphStyle = graphStyle;
     }
     
      public TreeMap<Long, Prices> getGraphPrices() {
         return graphPrices;
     }
     
     public void draw(Graphics g
                 , int marginLeft
                 , int marginTop
                 , int graphSizeX
                 , int graphSizeY
                 , Double scale
                 , long firstDrawPoint){
         this.g=g;
         this.marginLeft=marginLeft;
         this.marginTop=marginTop;
         this.graphSizeX=graphSizeX;
         this.graphSizeY=graphSizeY;
         this.scale=scale;
         this.firstDrawPoint=firstDrawPoint;
     
         switch(graphStyle){
             case GRAPH_STYLE_LINE:
                 _drawLine();
             break;
             case GRAPH_STYLE_CANDLESTICK:
                 _drawCandleStick();
             break;
         }
     }
     
     private void _drawLine(){
         
         if (firstDrawPoint > 0){
             long i=0;
             int x=0,y=0;
             Double[] minYAndMaxY = _getAdjCloseMinAndMax(firstDrawPoint, graphSizeX);
             Double deltaY=(minYAndMaxY[1]-minYAndMaxY[0])*(1+graphYMarginRate);
             ArrayList<GraphPoint> alGraphPoints = new ArrayList<GraphPoint>();
             
             _drawScale(minYAndMaxY);
             for(long  longTimeMillis: graphPrices.keySet()){
                 if (i++ >= firstDrawPoint) {
                     x = (int)((i-firstDrawPoint)*(scale))+marginLeft;
                     y = (int)((graphSizeY*(minYAndMaxY[1]-graphPrices.get(longTimeMillis).getAdjClose())/deltaY)+marginTop+(graphSizeY*(graphYMarginRate)/2));
                     
                     alGraphPoints.add(new GraphPoint(longTimeMillis, x, y));
                 }
                 if ((i-firstDrawPoint) >= graphSizeX ) break;
             }
             
             GraphPoint previousGraphPoint=null;
             g.setColor(Color.black);
             Calendar calendar           = Calendar.getInstance();
             Calendar previousCalendar   = Calendar.getInstance();
             
             for(GraphPoint graphPoint:alGraphPoints){
                 if(previousGraphPoint!=null){
                     calendar.setTimeInMillis(graphPoint.getLongTimeMillis());
                     if (calendar.get(Calendar.MONTH) != previousCalendar.get(Calendar.MONTH)){
                         g.setColor(Color.lightGray);
                         g.drawLine(graphPoint.getX(), marginTop, graphPoint.getX(), marginTop+graphSizeY+5);
                         g.drawString(calendar.get(Calendar.YEAR)+" "+monthName[calendar.get(Calendar.MONTH)], graphPoint.getX()+5, graphSizeY+marginTop+14);
                     }
                     g.setColor(Color.black);
                     g.drawLine(previousGraphPoint.getX()
                             , previousGraphPoint.getAdjCloseY()
                             , graphPoint.getX()
                             , graphPoint.getAdjCloseY());
                 }
                 previousGraphPoint = graphPoint;
                 previousCalendar.setTimeInMillis(previousGraphPoint.getLongTimeMillis());
             }
         }
     }
     
     private Double[] _getAdjCloseMinAndMax(long firstDrawPoint, long graphSizeX){
         Double y=0.0
             ,maxY=0.0
             ,minY=0.0;
         int i=0;
         for(long  longTimeMillis: graphPrices.keySet()){
                 if (i++ > firstDrawPoint) {
                     minY = Math.min(minY, graphPrices.get(longTimeMillis).getAdjClose());
                     maxY = Math.max(maxY, graphPrices.get(longTimeMillis).getAdjClose());
                 }
                 else if (i == firstDrawPoint) {
                     minY = graphPrices.get(longTimeMillis).getAdjClose();
                     maxY = graphPrices.get(longTimeMillis).getAdjClose();
                 }
                 if ((i-firstDrawPoint) >= (graphSizeX) ) break;
             }
         
         Double[] MinAndMaxGraphY = {minY,maxY};
         
         return MinAndMaxGraphY;
     }
     
     
     
     private void _drawCandleStick(){
         
         if (firstDrawPoint > 0){
             Double yLow=0.0, yHigh=0.0, yOpen=0.0, yClose=0.0;
             long i=0;
             int x=0;
             Double[] minYAndMaxY = _getMinAndMax(firstDrawPoint, graphSizeX);
             Double deltaY=(minYAndMaxY[1]-minYAndMaxY[0])*(1+graphYMarginRate);
             ArrayList<GraphPoint> alGraphPoints = new ArrayList<GraphPoint>();
             
             _drawScale(minYAndMaxY);
             
             for(long  longTimeMillis: graphPrices.keySet()){
                 if (i++ >= firstDrawPoint) {
                     x = (int)((i-firstDrawPoint)*(scale))+marginLeft;
                     yHigh = (graphSizeY*(minYAndMaxY[1]-graphPrices.get(longTimeMillis).getHigh())/deltaY)+marginTop+(graphSizeY*(graphYMarginRate)/2);
                     yLow = (graphSizeY*(minYAndMaxY[1]-graphPrices.get(longTimeMillis).getLow())/deltaY)+marginTop+(graphSizeY*(graphYMarginRate)/2);
                     yOpen = (graphSizeY*(minYAndMaxY[1]-graphPrices.get(longTimeMillis).getOpen())/deltaY)+marginTop+(graphSizeY*(graphYMarginRate)/2);
                     yClose = (graphSizeY*(minYAndMaxY[1]-graphPrices.get(longTimeMillis).getClose())/deltaY)+marginTop+(graphSizeY*(graphYMarginRate)/2);
                     
                     alGraphPoints.add(new GraphPoint(longTimeMillis
                                                     ,x
                                                     , yLow.intValue()
                                                     , yHigh.intValue()
                                                     , yOpen.intValue()
                                                     , yClose.intValue()));
                 }
                 if ((i-firstDrawPoint) >= graphSizeX ) break;
             }
             
             Calendar calendar           = Calendar.getInstance();
             Calendar previousCalendar   = Calendar.getInstance();
            i=0;
             for(GraphPoint graphPoint:alGraphPoints){
                 calendar.setTimeInMillis(graphPoint.getLongTimeMillis());
                if ((i++ > 0) && (calendar.get(Calendar.MONTH) != previousCalendar.get(Calendar.MONTH))){
                     g.setColor(Color.lightGray);
                     g.drawLine(graphPoint.getX(), marginTop, graphPoint.getX(), marginTop+graphSizeY+5);
                     g.drawString(calendar.get(Calendar.YEAR)+" "+monthName[calendar.get(Calendar.MONTH)], graphPoint.getX()+5, graphSizeY+marginTop+14);
                 }
 
                 g.setColor(Color.black);
                 g.drawLine(graphPoint.getX(), graphPoint.getMinY(), graphPoint.getX(), graphPoint.getMaxY());
 
                 g.setColor(((graphPoint.getCloseY()-graphPoint.getOpenY()) > 0) ? Color.red : Color.green ); // Reverse, values on screen. High is low and low is high
                 g.fillRect(graphPoint.getX()-1
                         , Math.min(graphPoint.getOpenY(),graphPoint.getCloseY())
                         , 3
                         , Math.abs(graphPoint.getCloseY()-graphPoint.getOpenY()));
                 previousCalendar.setTimeInMillis(graphPoint.getLongTimeMillis());
             }
         }
     }
     
     private void _drawScale(Double[] minYAndMaxY){
         Double deltaY=(minYAndMaxY[1]-minYAndMaxY[0])*(1+graphYMarginRate);
         int[] graphScaleValues = _getGraphScale(deltaY);
         int y1 = (int) (((minYAndMaxY[1]*(2+graphYMarginRate)) - (minYAndMaxY[0]*graphYMarginRate))/2);
         int y2 = (int) (((minYAndMaxY[0]*(2+graphYMarginRate)) - (minYAndMaxY[1]*graphYMarginRate))/2);
         int ySegment = (int) (graphScaleValues[0]*Math.pow(10, graphScaleValues[1]));
         int yGraph;
         g.setColor(Color.lightGray);
         for (int yAux=y1;yAux>y2;yAux--){
             if ((yAux % ySegment) == 0){
                 yGraph = ((int)((new Double(y1-yAux)/(y1-y2))*graphSizeY))+marginTop;
                 g.drawString(""+yAux, marginLeft+(int)(graphSizeX*scale)+3, yGraph+4);
                 g.drawLine(marginLeft, yGraph, marginLeft+(int)(graphSizeX*scale)+1, yGraph);
             }
         }
     }
     
     
     private Double[] _getMinAndMax(long firstDrawPoint, long graphSizeX){
         Double maxY=0.0
               ,minY=0.0;
         int i=0;
         for(long  longTimeMillis: graphPrices.keySet()){
                 if (i++ > firstDrawPoint) {
                     minY = Math.min(minY, graphPrices.get(longTimeMillis).getLow());
                     maxY = Math.max(maxY, graphPrices.get(longTimeMillis).getHigh());
                 }
                 else if (i == firstDrawPoint) {
                     minY = graphPrices.get(longTimeMillis).getLow();
                     maxY = graphPrices.get(longTimeMillis).getHigh();
                 }
                 if ((i-firstDrawPoint) >= (graphSizeX) ) break;
             }
         
         Double[] minAndMaxGraphY = {minY,maxY};
         
         return minAndMaxGraphY;
     }
     
     
     public int[] _getGraphScale(Double deltaY) {
         final int segments = 5;
         
         int fractionalDigits=0;
         int allDigitsdeltaYInteger=0;
         
         String deltaYString = ""+deltaY;
         String[] deltaYSplitted = deltaYString.split("\\.");
         
         if (deltaYSplitted.length>1){
             deltaYSplitted[1] = deltaYSplitted[1].substring(0, ((deltaYSplitted[1].length() > 4)? 4 : deltaYSplitted[1].length()));
             fractionalDigits=-deltaYSplitted[1].length();
             try{
             allDigitsdeltaYInteger = Integer.parseInt(deltaYSplitted[0]+deltaYSplitted[1]);
             }catch(Exception e){
                 System.out.println("deltaYSplitted[0]:"+deltaYSplitted[0]+" deltaYSplitted[1]:"+deltaYSplitted[1]);
             }
         }
         else {
             fractionalDigits=0;
             allDigitsdeltaYInteger = new Integer(deltaYSplitted[0]);
         }
         
         Double segmentAmp = new Double(allDigitsdeltaYInteger)/segments;
         Double[] scaleMap = {0.7, 1.4, 3.25, 7.0};
         boolean segmentFound=false;
         do {
             if (segmentAmp > scaleMap[scaleMap.length-1]){
                 segmentAmp /= 10;
                 fractionalDigits++;
             }
             else if (segmentAmp < scaleMap[0]) {
                 segmentAmp *= 10;
                 fractionalDigits--;
             }
             else {
                 segmentFound = true;
             }
         } while (!segmentFound);
         
         int[] scaleUnits = {1,2,5};
         int scaleUnit=0;
         for (int i=scaleMap.length-2;i>=0;i--){
             if (segmentAmp>scaleMap[i]){
                 scaleUnit = scaleUnits[i];
                 break;
             }
         }
         
         int[] result = {scaleUnit,fractionalDigits};
         
         return result;        
     }
     
 }
