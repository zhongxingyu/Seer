 import java.awt.*;
 import java.awt.geom.*;
 import java.awt.event.*;
 
 import javax.swing.*;
 
 public class Five_Board extends JPanel implements MouseListener {
 	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
 	 * @param args
 	 */
 	int row;
 
 	int DefaultWidth = 1200;
 	int rowPixels = 20;
 	Five_Game fg;
 	int xOffset = 10;
 	int yOffset = 10;
 	int fontWidth = 10;
 	int fontHeight = 10;
 	int numHeight = 10;
 	int numWidth = 6;
 	float lineWidth = 1.0f;
 	double chessDiameter = 1;
 
 	public Five_Board(Five_Game f, int r) {
 		super();
 		fg = f;
 		row = fg.DefaultRow;
 
 	}
 
 	public Five_Board(Five_Game f) {
 		super();
 		fg = f;
 		row = fg.DefaultRow;
 		addMouseListener(this);
 		DefaultWidth = (DefaultWidth / 24) * 24;
 		xOffset = yOffset = DefaultWidth / 12;
 		rowPixels = xOffset / 2;
 		chessDiameter = ((double) rowPixels) * 0.8;
 	}
 
 	private void paintBoard(Graphics2D g2d) {
 		g2d.setPaint(new Color(200, 135, 5));
 		g2d.fillRect(0, 0, DefaultWidth, DefaultWidth);
 		g2d.setStroke(new BasicStroke(lineWidth));// set the width of line
 		g2d.setPaint(Color.black);
 		for (int i = 0; i < fg.DefaultRow + 1; i++) {
 			g2d.draw(new Line2D.Double(xOffset, yOffset + i * rowPixels,
 					DefaultWidth - xOffset, yOffset + i * rowPixels));
 			g2d.drawString("" + (char) ('A' + i), xOffset - fontWidth - 3,
 					yOffset + i * rowPixels + fontHeight / 2);
 		}
 		for (int i = 0; i < fg.DefaultRow + 1; i++) {
 			g2d.draw(new Line2D.Double(xOffset + i * rowPixels, yOffset,
 					xOffset + i * rowPixels, DefaultWidth - yOffset));
 			g2d.drawString("" + i, xOffset + i * rowPixels - (1 + i / 10)
 					* numWidth / 2, yOffset - (numHeight-3));
 		}
 
 	}
 
 	private void paintChess(Graphics2D g2d) {
 		int[][] stat = fg.getStat();
 		g2d.setPaint(Color.black);
 		for (int i = 0; i < (fg.DefaultRow + 1); i++) {
 			for (int j = 0; j < (fg.DefaultRow + 1); j++) {
 				if (stat[i][j] == fg.ChessBlack) {
 					g2d.fill(new Ellipse2D.Double(
 							xOffset + i * rowPixels	- (chessDiameter - (double) lineWidth) / 2-0.5,
 							yOffset	+ j * rowPixels	- (chessDiameter - (double) lineWidth) / 2-0.5,
 							chessDiameter, chessDiameter));
 				}
 			}
 		}
 		g2d.setPaint(Color.white);
 		for (int i = 0; i < (fg.DefaultRow + 1); i++) {
 			for (int j = 0; j < (fg.DefaultRow + 1); j++) {
 				if (stat[i][j] == fg.ChessWhite) {
 					g2d.fill(new Ellipse2D.Double(
 							xOffset + i * rowPixels	- (chessDiameter - (double) lineWidth) / 2-0.5,
 							yOffset	+ j * rowPixels	- (chessDiameter - (double) lineWidth) / 2-0.5,
 							chessDiameter, chessDiameter));
 				}
 			}
 		}
 		P last = fg.getLastStep();
 		if(last != null){	
 			if(last.color == 2)
 			{
 				g2d.setPaint(Color.black);
 			}else
 			{
 				g2d.setPaint(Color.white);
 			}
 			g2d.drawArc(xOffset + last.x * rowPixels - rowPixels/2, yOffset + last.y * rowPixels - rowPixels/2, rowPixels, rowPixels, 30, 30);
 			g2d.drawArc(xOffset + last.x * rowPixels - rowPixels/2, yOffset + last.y * rowPixels - rowPixels/2, rowPixels, rowPixels, 120, 30);
 			g2d.drawArc(xOffset + last.x * rowPixels - rowPixels/2, yOffset + last.y * rowPixels - rowPixels/2, rowPixels, rowPixels, -30, -30);
 			g2d.drawArc(xOffset + last.x * rowPixels - rowPixels/2, yOffset + last.y * rowPixels - rowPixels/2, rowPixels, rowPixels, -120, -30);
 		}
 	}
 	private void paintScore(Graphics2D g2d) {
 		int[][][] score = fg.getScore();
 		g2d.setPaint(Color.black);
 		for (int i = 0; i < (fg.DefaultRow + 1); i++) {
 			for (int j = 0; j < (fg.DefaultRow + 1); j++) {
 				if (score[1][i][j] != 0) {
 					g2d.drawString(""+score[1][i][j], xOffset + i * rowPixels, yOffset	+ j * rowPixels);
 				}
 			}
 		}
 		g2d.setPaint(Color.white);
 		for (int i = 0; i < (fg.DefaultRow + 1); i++) {
 			for (int j = 0; j < (fg.DefaultRow + 1); j++) {
 				if (score[0][i][j] != 0) {
 					g2d.drawString(""+score[0][i][j], xOffset + i * rowPixels, yOffset	+ j * rowPixels - 15);
 				}
 			}
 		}
 	}
 	public void paint(Graphics g) {
 		super.paint(g);
 		// setSize(DefaultWidth,DefaultWidth);
 		Graphics2D g2d = (Graphics2D) g;
 		paintBoard(g2d);
 		paintChess(g2d);
 		paintScore(g2d);
 	}
 
 	@Override
 	public void mouseClicked(MouseEvent event) {
 		if(!fg.started||!fg.enableMouse)return;
 		int x = event.getX();
 		int y = event.getY();
 		int r = (x - xOffset + (rowPixels / 2)) / rowPixels;
 		int c = (y - yOffset + (rowPixels / 2)) / rowPixels;
 //		System.out.println("X:" + x + " Y:" + y + "R:" + r + " C:" + c
 //				+ "Defw:" + this.DefaultWidth + " xoff:" + this.xOffset);
 		if (Math.abs(x - (r * rowPixels + xOffset)) < rowPixels*8/10 && Math.abs(y - (c * rowPixels + yOffset)) < rowPixels*8/10) {
 			
 			fg.move(r, c);
 			System.out.println("X:" + x + " Y:" + y + "R:" + r + " C:" + c);
 		}
 	}
 
 	@Override
 	public void mouseEntered(MouseEvent arg0) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void mouseExited(MouseEvent arg0) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void mousePressed(MouseEvent arg0) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void mouseReleased(MouseEvent arg0) {
 		// TODO Auto-generated method stub
 
 	}
 
 }
