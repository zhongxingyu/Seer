 package com.abstracttech.ichiban.views.graphs;
 
 import android.content.Context;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.util.AttributeSet;
 import android.widget.ImageView;
 
import com.abstracttech.ichiban.R;
 import com.abstracttech.ichiban.data.Data;
 import com.abstracttech.ichiban.data.StatisticData;
 
 /**
  * this renders all the graphs
  * graph type is selected from Data.graphs
  * real time morphing to another graph is also possible
  */
 public class Graph extends ImageView {
 	Object[] currentData;
 	protected float top,bottom;
 	protected StatisticData datasource;
 	protected boolean updateBorders=false;
 	private int typeID;
 	private GraphType currentType;
 
 	public Graph(Context context, AttributeSet attrs) {
 		super(context, attrs);
 		Data.subscribe(this);
 		
 		//find out who i am
 		int myid=getId();
 		if(Data.graphID[0]==myid)
 			typeID=0;
 		if(Data.graphID[1]==myid)
 			typeID=1;
 		if(Data.graphID[2]==myid)
 			typeID=2;
 		if(Data.graphID[3]==myid)
 			typeID=3;
 	}
 
 	/**
 	 * morphs the graph to selected type
 	 * via setting borders and datasource
 	 * @param type to morph into
 	 */
 	private void morph(GraphType type)
 	{
 		switch(type)
 		{
 		case PATH:
 			top=0;
 			bottom=0;
 			datasource=Data.pathData;
 			this.updateBorders=true;
			this.setBackgroundResource(R.drawable.graf3);
 			break;
 		case SPEED:
 			bottom=-20;
 			top=160;
 			datasource=Data.speedData;
 			this.updateBorders=false;
			this.setBackgroundResource(R.drawable.graf2);
 			break;
 		case ACCELERATION:
 			bottom=-80;
 			top=80;
 			datasource=Data.accData;
 			this.updateBorders=false;
			this.setBackgroundResource(R.drawable.graf1);
 			break;
 		case TOTAL_ACC:
 			bottom=0;
 			top=0.5f;
 			datasource=Data.totalAccData;
 			this.updateBorders=false;
			this.setBackgroundResource(R.drawable.graf4);
 			break;
 		}
 		currentType=type;
 	}
 
 	protected float GetData()
 	{
 		return datasource.get();
 	}
 
 	protected boolean isThereData() {
 		return (datasource!=null && datasource.data!=null && Data.accData.data.size()>0);
 	}
 
 	protected Object[] getArray() {
 		return datasource.array;
 	}
 
 	private float getPoint(int i){
 		return 1 - ((Float)currentData[i] - bottom)/ (top-bottom);
 	}
 	@Override
 	protected void onDraw(Canvas canvas)
 	{
 		//morph if needed
 		if(currentType!=Data.graphs[typeID])
 			morph(Data.graphs[typeID]);
 
 		//draw bg
 		super.onDraw(canvas);
 		
 		//for path graph
 		if(updateBorders)
 		{
 			top=Data.pathData.last;
 			bottom=Data.pathData.first;
 		}
 
 		Paint p = new Paint();
 		p.setStrokeWidth(1);
 
 		p.setColor(Color.argb(255, 100, 100, 100));
 		
 		int W = this.getWidth();
 		int H = this.getHeight();
 
 		for(int i = 0; i < 4; i++)		//side data: lines, percentage,...   300   400
 		{
 			canvas.drawText("" + (int)((i + 1) * (top - bottom) / 5), W * 0.06f , H - (i + 1) * H / 4 - 2, p);
 			canvas.drawText("" + (2 - (i + 1) * 0.5f) + "s", (i + 1) * W / 4, H * (1 - 0.15f), p);
 			canvas.drawLine(0.03f * W, (i + 1) * H / 4, W * (1 - 0.03f), (i + 1) * H / 4, p);
 			canvas.drawLine((i + 1) * W / 4, 0.0225f * H, (i + 1) * W / 4, H * (1 - 0.12f), p);
 		}
 
 
 		if(isThereData())
 		{					
 			currentData= getArray();			
 			float[] points = new float[currentData.length * 4];			//array of points of graph
 			float[] pointsShadow = new float[currentData.length * 4];		//shadow array
 
 
 			for (int i = 0; i < currentData.length - 1; i++)		//Set points to draw lines
 			{
 				points[i * 4] = W * i / (float)currentData.length + 0.03f * W;
 				points[i * 4 + 1] = H * getPoint(i) * 0.88f + 0.0225f * H;
 				points[i * 4 + 2] = W * (i + 1) / (float)currentData.length + 0.03f * W;
 				points[i * 4 + 3] = H * getPoint(i+1) * 0.88f + 0.0225f * H;
 
 				pointsShadow[i * 4] = W * i / (float)currentData.length + 5 + 0.03f * W;
 				pointsShadow[i * 4 + 1] = H * getPoint(i) * 0.88f + 0.04f * H;
 				pointsShadow[i * 4 + 2] = W * (i + 1) / (float)currentData.length + 5 + 0.03f * W;
 				pointsShadow[i * 4 + 3] = H * getPoint(i+1) * 0.88f + 0.04f * H;
 			}
 
 			canvas.drawLines(pointsShadow, p);
 
 			p.setColor(Color.RED);							//setting paint (Color, stroke...) and drawing the lines
 			p.setStrokeWidth(4);
 			canvas.drawLines(points, p);
 		}	
 	}
 }
