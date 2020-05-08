 package org.concord.waba.extra.ui;
 
 public class CCColorChooser extends waba.ui.Control{
 waba.fx.Color	[]colors = new waba.fx.Color[256];
 public int colorIndex = -1;
 waba.fx.Image bufIm = null;
 int dx = 8;
 int dy = 6;
 	public CCColorChooser(){
 		int r,g,b,i;
 		int count = 0;
 		colors[count++] = new waba.fx.Color(0,0,0);
 		colors[count++] = new waba.fx.Color(255,255,255);
 		for(r = 0; r <=255; r += 51){
 			for(b = 0; b <=255; b += 51){
 				for(g = 0; g <=255; g += 51){
 					if(r == g && r == b && r == 0) continue;
 					if(r == g && r == b && r == 255) continue;
 					colors[count++] = new waba.fx.Color(r,g,b);
 				}
 			}
 		}
 
 		g = b = 0;
 		for(i = 1; i <= 10; i++){
 			r = (int)(255f*i/11f+0.5);
 			colors[count++] = new waba.fx.Color(r,g,b);
 		} 
 
 		r = b = 0;
 		for(i = 1; i <= 10; i++){
 			g = (int)(255f*i/11f+0.5);
 			colors[count++] = new waba.fx.Color(r,g,b);
 		} 
 
 		r = g = 0;
 		for(i = 1; i <= 10; i++){
 			b = (int)(255f*i/11f+0.5);
 			colors[count++] = new waba.fx.Color(r,g,b);
 		} 
 
 		for(i = 1; i <= 10; i++){
 			r = (int)(255f*i/11f+0.5);
 			colors[count++] = new waba.fx.Color(r,r,r);
 		} 
 	}
 	
 	public void setRect(int x,int y,int w,int h){
 		super.setRect(x,y,128,96);
 	}
 	
    public void destroy()
    {
	if(bufIm != null){
	    bufIm.free();
	    bufIm = null;
	}
    }

 	public void createOffImage(){
 		if(bufIm != null) return;
 		bufIm=new waba.fx.Image(width,height);
 		 waba.fx.Graphics ig = new waba.fx.Graphics(bufIm);
 		int xx = 0;
 		int yy = 0;
 		for(int i = 0; i < colors.length; i++){
 			ig.setColor(colors[i].getRed(),colors[i].getGreen(),colors[i].getBlue());
 			ig.fillRect(xx,yy,dx,dy);
 			if(((i+1) % 16) == 0){
 				xx = 0;
 				yy += dy;
 			}else{
 				xx+=dx;
 			}
 		}
 		ig.setColor(0,0,0);
 		ig.drawRect(0,0,width,height);
      		ig.free();
 	}
 	
 	public void onPaint(waba.fx.Graphics g){
 		if(colors == null) return;
 		createOffImage();
      		g.copyRect(bufIm,0,0,width,height,0,0);
 		int xCurr = dx * (colorIndex % 16);
 		int yCurr = dy * (colorIndex / 16);
 		drawChosenRectFrame(g,xCurr,yCurr);
 	}
 
 	public void drawChosenRectFrame(waba.fx.Graphics g, int x, int y){
 		if(x >= 0 && y >= 0){
 			g.setColor(255,255,255);
 //			g.drawRect(x-1,y-1,dx+2,dy+2);
 			g.drawRect(x,y,dx,dy);
 			g.setColor(0,0,0);
 //			g.drawRect(x,y,dx,dy);
 			g.drawRect(x+1,y+1,dx-2,dy-2);
 		}
 	}
 
 	public void onEvent(waba.ui.Event event){
 		if (event.type == waba.ui.PenEvent.PEN_DOWN){
 			waba.ui.PenEvent penEvent = (waba.ui.PenEvent)event;
 			int px = penEvent.x / dx;
 			int py = penEvent.y / dy;
 			if(px < 0 || px > 15) return;
 			if(py < 0 || py > 15) return;
 			int oldIndex = colorIndex;
 			colorIndex = py*16 + px;
 			waba.fx.Graphics g =  createGraphics();
 			if(g == null){
 				repaint();
 				return;
 			}else{
 				if(oldIndex >= 0){
 					int xOld = dx * (oldIndex % 16);
 					int yOld = dy * (oldIndex / 16);
 					g.setColor(colors[oldIndex].getRed(),colors[oldIndex].getGreen(),colors[oldIndex].getBlue());
 					g.fillRect(xOld,yOld,dx,dy);
 				}
 				int xNew = dx * (colorIndex % 16);
 				int yNew = dy * (colorIndex / 16);
 				drawChosenRectFrame(g,xNew,yNew);
 			} 
 		}
 	}
 	
 	public waba.fx.Color getChosenColor(){
 		if(colorIndex >= 0){
 			return colors[colorIndex];
 		}
 		return null;
 	}
 
 }
