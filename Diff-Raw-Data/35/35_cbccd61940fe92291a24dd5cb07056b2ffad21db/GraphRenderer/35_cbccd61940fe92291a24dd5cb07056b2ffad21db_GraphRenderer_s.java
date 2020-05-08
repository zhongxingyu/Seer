 package lorian.graph.android.opengl;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.microedition.khronos.egl.EGLConfig;
 import javax.microedition.khronos.opengles.GL10;
 
 import lorian.graph.WindowSettings;
 import lorian.graph.android.FunctionDataArray;
 import lorian.graph.android.GraphActivity;
 import lorian.graph.android.R;
 import lorian.graph.android.Util;
 import lorian.graph.android.WindowSettingsActivity;
 import lorian.graph.function.Calculate;
 import lorian.graph.function.Function;
 
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.graphics.Point;
 import android.opengl.GLSurfaceView;
 import android.preference.PreferenceManager;
 import android.util.Log;
 
 public class GraphRenderer implements GLSurfaceView.Renderer {
 
 	private static final String TAG = GraphActivity.TAG;
 	
 	public static List<Function> functions = new ArrayList<Function>();
 	public static List<FunctionDataArray> fdata = new ArrayList<FunctionDataArray>();
 	//private WindowSettings WindowSettingsActivity.windowsettings;
 	public static boolean auto_calc_ymin_ymax = true;
 	private boolean windowerror = false;
 	private int YaxisX, XaxisY;
 	private static int width;
 
 	private static int height; 
 	private static Context context; 
 	
 	public boolean render_functions = false;
 	public boolean render_axes = false;
 	private static boolean functions_need_recalculation = false;
 	private static boolean windowsettings_need_recalculation = false;
 	public GraphRenderer(Context context)
 	{
 		super();	
		this.context = context;
 		fdata.clear();
 		functions.clear();
 		
 	}
 	/*
 	private void drawPart(GL10 gl, double x1, double y1, double x2, double y2)
 	{
 		
 		if(y1 < 10 || y2 < 10)
 		{
 			Log.d(TAG, String.format("%f, %f, %f, %f", x1, y1, x2, y2));		}
 		
 		float buf[] = new float[] {(float)x1, (float)y1, (float)x2, (float) y2};
 		gl.glVertexPointer(2, GL10.GL_FLOAT, 0,  Util.toFloatBuffer(buf)); 
 		gl.glDrawArrays(GL10.GL_LINES, 0, 4);
 	}
 
 	private void drawPoint(GL10 gl, double x, double y)
 	{
 		
 		float buf[] = new float[] {(float) x, (float) y};
 		gl.glVertexPointer(2, GL10.GL_FLOAT, 0, Util.toFloatBuffer(buf)); 
 		gl.glDrawArrays(GL10.GL_POINTS, 0, 1);
 		if(gl.glGetError() == GL10.GL_INVALID_VALUE)
 			Log.d(TAG, "drawPoint called: x=" + x + ",y=" + y);
 		
 	}
 */
 	private float[] savePart(float[] olddata, int index, double x1, double y1, double x2, double y2)
 	{
 	   float[] tmpdata =  new float[] {(float)x1, (float)y1, (float)x2, (float) y2};
 	   float[] otherdata = olddata;
 	   otherdata[index++] = tmpdata[0];
 	   otherdata[index++] = tmpdata[1];
 	   otherdata[index++] = tmpdata[2];
 	   otherdata[index++] = tmpdata[3];
 	   return otherdata;
 	}
 	
 	private void drawAllFunctions(GL10 gl)
 	{
 		if(fdata == null)
 		{
 			Log.e(TAG, "fdata == null!"); 
 			return;
 		}
 		for(FunctionDataArray f: fdata)
 		{
 			gl.glColor4f(f.color.getR(), f.color.getG(), f.color.getB(), 1f);
 			
 			gl.glVertexPointer(2, GL10.GL_FLOAT, 0,  Util.toFloatBuffer(f.data)); 
 			gl.glDrawArrays(GL10.GL_LINES, 0, f.data.length / 2);
 		}
 	}
 
 	public void calculateFunction(Function f, boolean fill) 
 	{
 		if(f.isEmpty()) return;
 		if(WindowSettingsActivity.windowsettings == null) 
 		{
 			Log.e(TAG, "settings == null!"); 
 			return;
 		}
 		FunctionDataArray data = new FunctionDataArray();
 		//gl.glColor4f(c.getR(), c.getG(), c.getB(), 1);
 		data.color =  f.getColor();
 		data.data = new float[width * height];
 		int xpix, ypix;
 		boolean inNaN = false;
 		double x,y;
 		double step = ((double) (WindowSettingsActivity.windowsettings.getXmax() - WindowSettingsActivity.windowsettings.getXmin())) / width;
 		
 		Point previous = new Point();
 		boolean WaitForRealNumber = false;
 		//float[] buf; 
 		
 		int i=0;
 		
 		for(xpix = -1, x = WindowSettingsActivity.windowsettings.getXmin(); xpix < (int) width; xpix++, x+=step)
 		{
 			y = f.Calc(x);
 			if(Double.isNaN(y)) 
 			{ 
 				if(inNaN) continue;
 				inNaN = true;
 				double tmpX = Calculate.FindLastXBeforeNaN(f, x - step);
 				if(!Double.isNaN(tmpX))
 				{
 					double tmpY =  f.Calc(tmpX);
 					ypix = (int) ((WindowSettingsActivity.windowsettings.getYmax() - tmpY) * (height / (WindowSettingsActivity.windowsettings.getYmax() - WindowSettingsActivity.windowsettings.getYmin())));
 					//drawPart(gl, previous.x, previous.y, xpix, ypix);
 					//float[] tmpdata = savePart(previous.x, previous.y, xpix, ypix);
 					/*
 					data.data[i++] = tmpdata[0];
 					data.data[i++]= tmpdata[1];
 					data.data[i++] = tmpdata[2];
 					data.data[i++]= tmpdata[3];
 					*/
 					data.data = savePart(data.data, i, previous.x, previous.y, xpix, ypix);
 					i+=4;
 				}
 				
 				previous = null;
 				if(!WaitForRealNumber)
 				{
 					WaitForRealNumber = true;
 				}
 				continue;
 			}
 			else {
 				if(inNaN)
 				{
 					double tmpX = Calculate.FindFirstXAfterNaN(f, x - step);
 					if(!Double.isNaN(tmpX))
 					{
 						double tmpY =  f.Calc(tmpX);
 						ypix = (int) ((WindowSettingsActivity.windowsettings.getYmax() - tmpY) * (height / (WindowSettingsActivity.windowsettings.getYmax() - WindowSettingsActivity.windowsettings.getYmin())));
 						
 						//drawPoint(gl, xpix, ypix);
 						
 						if(previous == null) previous = new Point();
 						previous.set(xpix, ypix);
 					}
 					
 					
 					inNaN = false;
 					continue;
 					
 				}
 				
 				if(WaitForRealNumber) WaitForRealNumber = false;
 				
 			}
 			
 			ypix = (int) ((WindowSettingsActivity.windowsettings.getYmax() - y) * (height / (WindowSettingsActivity.windowsettings.getYmax() - WindowSettingsActivity.windowsettings.getYmin())));
 			if(xpix > -1)
 			{
 				if(previous == null)
 				{
 					//drawPoint(gl, xpix, ypix);
 				}
 				else if(Math.abs(xpix - previous.x) < width && Math.abs(ypix - previous.y) < height)
 				{
 					//drawPart(gl, previous.x, previous.y, xpix, ypix);
 					data.data = savePart(data.data, i, previous.x, previous.y, xpix, ypix);
 					i+=4;
 				}
 				/*
 				if(fill && x >= FillLowX - 0.01 && x <= FillUpX + 0.01)
 				{
 					g.setColor(Util.lighter(f.getColor(), true) );
 					((Graphics2D) g).setStroke(new BasicStroke(1.0f, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_BEVEL));
 					if(y > 0)
 					{
 						if(ypix < 0)
 							g.drawLine(xpix, 0, xpix, this.XaxisY);
 						else
 							g.drawLine(xpix, ypix-1, xpix, this.XaxisY);
 					}
 					else if(y < 0)
 					{
 						if(ypix > size.getHeight())
 							//g.drawLine(xpix, (int) size.getHeight(), xpix, this.XaxisY);
 							drawPart(g, xpix, (int) size.getHeight(), xpix, this.XaxisY);
 						else
 							//g.drawLine(xpix, ypix+1, xpix, this.XaxisY);
 							drawPart(g, xpix, ypix+1, xpix, this.XaxisY);
 					}
 					((Graphics2D) g).setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_BEVEL));
 					g.setColor(f.getColor());
 				}*/
 			}
 			if(previous == null) previous = new Point();
 			previous.set(xpix, ypix);
 		}
 		//gl.glColor4f(0f, 0f, 0f, 1f);
 		
 		fdata.add(data);
 	}
 	
 	private void drawAxes(GL10 gl)
 	{
 		gl.glColor4f(0, 0, 0, 1);
 		float[] buf = new float[]
 		{
 					YaxisX, 0, YaxisX, height,
 					0, XaxisY, width, XaxisY
 		};
 		gl.glVertexPointer(2, GL10.GL_FLOAT, 0, Util.toFloatBuffer(buf)); 
 		gl.glDrawArrays(GL10.GL_LINES, 0, 4);
 		
 		int pix;
 		
 		for(long x=WindowSettingsActivity.windowsettings.getXmin()+1;x<WindowSettingsActivity.windowsettings.getXmax();x++)
 		{
 			if(x==0) continue;
 			pix = (int) ((x-WindowSettingsActivity.windowsettings.getXmin()) * (width / (WindowSettingsActivity.windowsettings.getXmax() - WindowSettingsActivity.windowsettings.getXmin())));
 			if(pix==0) continue;
 		
 			buf = new float[] {pix, XaxisY-5, pix, XaxisY + 5};
 			gl.glVertexPointer(2, GL10.GL_FLOAT, 0, Util.toFloatBuffer(buf)); 
 			gl.glDrawArrays(GL10.GL_LINES, 0, 2);
 			
 		}
 		for(long y=WindowSettingsActivity.windowsettings.getYmin()+1;y<WindowSettingsActivity.windowsettings.getYmax();y++)
 		{
 			if(y==0) continue;
 			pix = (int) height - (int) ((y-WindowSettingsActivity.windowsettings.getYmin()) * (height / (WindowSettingsActivity.windowsettings.getYmax() - WindowSettingsActivity.windowsettings.getYmin())));
 			if(pix==0) continue;
 
 			buf = new float[] {YaxisX - 5, pix, YaxisX + 5, pix};
 			gl.glVertexPointer(2, GL10.GL_FLOAT, 0, Util.toFloatBuffer(buf)); 
 			gl.glDrawArrays(GL10.GL_LINES, 0, 2);
 		
 		}
 	}
 	private void drawGrid(GL10 gl)
 	{
 		float[] buf;
 		int pix;
 		gl.glColor4f(0, 186f / 255f, 1.0f, 1.0f);
 		for(long x=WindowSettingsActivity.windowsettings.getXmin()+1;x<WindowSettingsActivity.windowsettings.getXmax();x++)
 		{
 			if(x==0) continue;
 			pix = (int) ((x-WindowSettingsActivity.windowsettings.getXmin()) * (width / (WindowSettingsActivity.windowsettings.getXmax() - WindowSettingsActivity.windowsettings.getXmin())));
 			if(pix==0) continue;
 			buf = new float[] {pix, 0, pix, height};
 			gl.glVertexPointer(2, GL10.GL_FLOAT, 0, Util.toFloatBuffer(buf)); 
 			gl.glDrawArrays(GL10.GL_LINES, 0, 2);
 		}
 		for(long y=WindowSettingsActivity.windowsettings.getYmin()+1;y<WindowSettingsActivity.windowsettings.getYmax();y++)
 		{
 			if(y==0) continue;
 			pix = (int) height -  (int) ((y-WindowSettingsActivity.windowsettings.getYmin()) * (height/ (WindowSettingsActivity.windowsettings.getYmax() - WindowSettingsActivity.windowsettings.getYmin())));
 			if(pix==0) continue;
 			
 			buf = new float[] {0, pix, width, pix};
 			gl.glVertexPointer(2, GL10.GL_FLOAT, 0, Util.toFloatBuffer(buf)); 
 			gl.glDrawArrays(GL10.GL_LINES, 0, 2);
 		}
 	}
 			
 	@Override
 	public void onDrawFrame(GL10 gl)
 	{
 		long start = System.currentTimeMillis();
 		gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
 		gl.glColor4f(0, 0, 0, 1);
 		
 		if(windowerror) return;
 
 		
 		if(render_axes && WindowSettingsActivity.windowsettings.gridOn())
 		{
 			Log.d(TAG, "Drawing grid...");
 			gl.glLineWidth(2.0f);
 			drawGrid(gl);
 		}
 		
 		
 		if(render_functions)
 		{
 			Log.d(TAG, "Drawing functions...");
 			gl.glLineWidth(2.6f);
 			drawAllFunctions(gl);
 		}
 		if(render_axes)
 		{
 			gl.glLineWidth(2.0f);
 			Log.d(TAG, "Drawing axes...");
 			drawAxes(gl);
 		}
 		
 		
 		Log.d(TAG, "Drawing time was " + (System.currentTimeMillis() - start) + " ms");
 	}
 
 
 	
 	public static void recalculateWindowSettings()
 	{
 		if(auto_calc_ymin_ymax)
 		{
 			long dy = (WindowSettingsActivity.windowsettings.getXmax() - WindowSettingsActivity.windowsettings.getXmin()) * height / width;
 			WindowSettingsActivity.windowsettings.setYmax(dy / 2);
 			WindowSettingsActivity.windowsettings.setYmin(-dy / 2);
 			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
 			SharedPreferences.Editor edit = prefs.edit();
			edit.putInt("window_settings_ymin", (int) (dy / -2));
			edit.putInt("window_settings_ymax", (int) (dy / 2));
 			edit.apply();
 		}
 	}
 	public void recalculateFunctions(int filledTexts)
 	{
 		
 		long start = System.currentTimeMillis();
 		fdata.clear();
 		
 		int percent = 0;
 		Log.d(TAG, "Calculating functions...");
 		for (int i = 0; i < functions.size(); i++) 
 		{
 			//GraphActivity.changeLoadingDialogText("Loading (" + percent + "%)...");
 			GraphActivity.changeLoadingDialogText(String.format(context.getResources().getString(R.string.loading_format), percent));
 			
 			if(functions.get(i).drawOn())
 			{
 				calculateFunction(functions.get(i), false);
 				percent += (100 / filledTexts);
 				Log.d(TAG, percent + "%");
 			}
 			
 			 
 		}
 		Log.d(TAG, "Calculation time was " + (System.currentTimeMillis() - start) + " ms");
 	
 	}
 	private void ParseFunctions(int filledTexts)
 	{
 		int percent = 0;
 		GraphActivity.changeLoadingDialogText(String.format(context.getResources().getString(R.string.parsing_functions_format), percent));
 		
 		Log.d(TAG, "Parsing functions...");
 		functions.clear();
 		
 		for(int i=0;i<GraphActivity.MaxFunctions;i++)
 		{
 			Function f = new Function();
 			if(GraphActivity.itemTexts[i] == null)
 			{
 				f.setDraw(false);
 				functions.add(f);
 				continue;
 			}
 			else
 			{
 				String text = lorian.graph.function.Util.removeWhiteSpace(GraphActivity.itemTexts[i].trim());
 			if(text.isEmpty())
 				{
 					f.setDraw(false);
 					functions.add(f);
 				}
 				else if(!f.Parse(text))
 				{
 					Log.d(TAG, "Error: Unable to parse function Y" + (i+1));
 					f.clear();
 					f.setDraw(false);
 					functions.add(f);
 					percent += (100 / filledTexts);
 				}
 				else
 				{
 					Color c = GraphActivity.functionColors[i];
 					f.setColor(c);
 					//f.setDraw(getIfSomethingIsChecked);
 					functions.add(f);
 					Log.d(TAG, "Added function Y" + (i+1) + " with color " + c.getR() * 0xff + "," + c.getG() * 0xff + "," + c.getB() * 0xff );
 					percent += (100 / filledTexts);
 					
 				}
 				
 			}
 			
 			GraphActivity.changeLoadingDialogText(String.format(context.getResources().getString(R.string.parsing_functions_format), percent));
 		}
 	}
 	
 
 	public void recalculateAxes()
 	{
 		YaxisX = (int) (width * ((double) - WindowSettingsActivity.windowsettings.getXmin()) / ((double) (WindowSettingsActivity.windowsettings.getXmax() - WindowSettingsActivity.windowsettings.getXmin()))) - 1;
 		XaxisY = (int) height - (int) (height * ((double) -WindowSettingsActivity.windowsettings.getYmin()) / ((double) (WindowSettingsActivity.windowsettings.getYmax() - WindowSettingsActivity.windowsettings.getYmin()))) - 1;
 		windowerror = false;
 	}
 	
 	@Override
 	public void onSurfaceChanged(GL10 gl, int width, int height) 
 	{
 		Log.d(TAG, "Surface changed! width = " + String.valueOf(width) + ", height = " + String.valueOf(height));
		if(this.width != width || this.height != height || functions_need_recalculation || windowsettings_need_recalculation)
 		{
 			GraphActivity.showLoadingDialog();
 			Log.d(TAG, "Updating data...");
			this.width = width;
			this.height = height;
 			//tmp_InitWindowSettings(-8, 8, false);
 			//tmp_InitFunctions();
 			
 
 			int filledTexts = 0;
 			for(String s: GraphActivity.itemTexts)
 			{
 				if(s == null) continue;
 				if(!lorian.graph.function.Util.removeWhiteSpace(s.trim()).isEmpty()) filledTexts++;
 			}
 			if(filledTexts==0)filledTexts=1;
 		
 			recalculateWindowSettings();
 			recalculateAxes();
 
 			ParseFunctions(filledTexts);
 		
 			recalculateFunctions(filledTexts);
 			functions_need_recalculation = false;
 			windowsettings_need_recalculation = false;
 			GraphActivity.hideLoadingDialog();
 			
 		}
 		
 		
 		gl.glViewport(0, 0, width, height);
 		gl.glMatrixMode(GL10.GL_PROJECTION);
 		gl.glLoadIdentity();
 
 		gl.glOrthof(0, width, height, 0, 1, -1);
 		gl.glClearColor(1, 1, 1, 1);
 		
 		gl.glMatrixMode(GL10.GL_MODELVIEW);
 		gl.glLoadIdentity();
 		
 		gl.glShadeModel(GL10.GL_SMOOTH); // Enable Smooth Shading
 		
 		
 		//gl.glEnable(GL10.GL_LINE_SMOOTH);
 		gl.glEnable(GL10.GL_POINT_SMOOTH);
 		gl.glHint(GL10.GL_POINT_SMOOTH_HINT, GL10.GL_NICEST);
 		gl.glHint(GL10.GL_LINE_SMOOTH_HINT, GL10.GL_NICEST);
 		
 		gl.glEnable(GL10.GL_MULTISAMPLE);
 		
 		gl.glEnable(GL10.GL_BLEND);
 		gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
 		
 		gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
 		
 		
 		
 	}
 
 	@Override
 	public void onSurfaceCreated(GL10 gl, EGLConfig config)
 	{
 		//Log.d(TAG, "Surface Created!");
 	}
 	
 	public static void notifyFunctionsChanged()
 	{
 		functions_need_recalculation = true;
 	}
 	public static void notifyFunctionColorsChanged()
 	{
 		if(functions_need_recalculation || functions.size() == 0 || fdata.size() == 0) return;
 		for(int i=0;i<fdata.size();i++)
 		{
 			Function f = functions.get(i);
 			FunctionDataArray fd = fdata.get(i);
 			f.setColor(GraphActivity.functionColors[i]);
 			fd.color = f.getColor();
 		}
 	}
 	public static void notifyWindowSettingsChanged()
 	{
 		windowsettings_need_recalculation  = true;
 		try
 		{
 			GraphRenderer.recalculateWindowSettings();
 		}
 		catch(ArithmeticException e)
 		{
 			e.printStackTrace();
 		}
 	}
 
 	
 
 }
