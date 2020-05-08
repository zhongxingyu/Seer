 package net.qmat.qmhh.models.creatures;
 
 import net.qmat.qmhh.Main;
 
 public class Mott extends CreatureBase {
 
 	int maxStage = 2;
 
 	float flyMin = -w / 9; //how wings can move
 	float flyMax = w / 13; //how wings can move
 	float flyPos = 0; //temp position for wings animation
 
 	int scale_state = 0; //is it scaling?
 	int scale; //for scaling up and down when flying
 	int alpha;
 
 	Mott() {
 		super();
 		//w = 37; 
 		//h = 30;
 		scale = 100;
 		setAlpha(scale);
 	}
 
 	private void setAlpha(int a) {
 		alpha = a > 100 ? 100 : a;
 	}
 
 	public void draw() {
 
 		// the amount of body segments to draw
 		int tempCount = 1;  
 		if(stage == 0) 
 			tempCount = 2;
 		if(stage == 1)
 			tempCount = 3;
 		if(stage == 2)
 			tempCount = 4;
 
 		p.stroke(255, 255, 255, alpha);
 
 		p.pushMatrix();  
		//p.translate(x, y);
		//p.rotate(angle);
 		p.scale(scale / 100.0f);
 
 		drawWings(tempCount);
 
 		p.rotate(-Main.PI);
 
 		//body parts
 		for(int i=tempCount; i > 0; i--) {
 
 			//for the eyes in the first iteration
 			if(i == tempCount) drawEyes(i, alpha);
 			// main body part
 			drawBodyPart(i, alpha);
 			//for the tail arcs in the last iteration
 			if(i == 1) {
 				drawTail();
 			}
 			// get ready to draw the next bit
 			p.translate(0, h * i / 6);
 		}
 		p.popMatrix();
 	}
 
 	private void drawBodyPart(int i, float a) {
 		p.noFill(); 
 		float eW = h/2 * i/2;
 		float eH = h * i/3;
 		float[] offsets = {0, 3, 4, 5, 9};
 		float a2 = a;
 		for(int j=0; j<offsets.length; j++) {
 			a2 /= 1.7;
 			p.stroke(255, 255, 255, a2);
 			p.ellipse(0, 0, eW + offsets[j], eH + offsets[j]); 
 		}
 	}
 
 	private void drawEyes(int i, float a) {
 		float[] offsets = {0, 2, 5, 9};
 		float eW = h / 2 * i / 20;
 		float eH = h * i / 30;
 		float eY = -h * i / 6;
 		float eX = -h / 2 * i / 8;
 		float a2 = a;
 		for(int j=0; j<offsets.length; j++) {
 			a2 /= 1.7;
 			p.stroke(255, 255, 255, a2);
 			p.ellipse(eX, eY, eW + offsets[j], eH + offsets[j]); 
 			p.ellipse(-eX, eY, eW + offsets[j], eH + offsets[j]); 
 		}
 	}
 
 	private void drawTail() {
 		p.stroke(255, 255, 255, alpha);
 		p.translate(0, h / 3);
 		p.noFill();
 		p.arc(0, 0,        h / 3,   h / 4,  Main.PI - Main.PI / 10, Main.TWO_PI + Main.PI / 10);
 		p.arc(0, h / 16,   h / 2.2f, h / 4,  Main.PI,         Main.TWO_PI);
 	}
 
 	private void drawWings(int tempCount) {
 		//for the wings
 		p.rotate(Main.PI);
 		p.strokeWeight(2);
 		p.noFill();
 		float hDiff = (h * tempCount / 3.0f - w / 3.0f * tempCount / 1.5f) / 2.0f;
 
 		p.ellipse(0, hDiff, w * tempCount / 1.5f, w / 3 * tempCount / 1.5f); 
 		p.ellipse(0, flyPos, w * tempCount / 1.5f + flyPos * 2, w / 3 * tempCount/1.5f); 
 
 		p.stroke(255, 255, 255, alpha/1.7f);
 		p.ellipse(0, hDiff, w * tempCount / 1.5f + 2, w / 3 * tempCount / 1.5f + 2); 
 		p.ellipse(0, flyPos, w * tempCount / 1.5f + flyPos * 1.3f + 2, w / 3 * tempCount / 1.5f + 2); 
 
 		p.stroke(255, 255, 255, alpha / 2.3f);
 		p.ellipse(0, hDiff, w * tempCount / 1.5f + 5, w / 3 * tempCount / 1.5f + 5); 
 		p.ellipse(0, flyPos, w * tempCount / 1.5f + flyPos + 5, w / 3 * tempCount / 1.5f + 5); 
 
 		p.stroke(255, 255, 255, alpha / 5);
 		p.ellipse(0, hDiff, w * tempCount / 1.5f + 9, w / 3 * tempCount / 1.5f + 9); 
 		p.ellipse(0, flyPos, w * tempCount / 1.5f + flyPos / 1.3f + 9, w / 3 * tempCount / 1.5f + 9);
 
 		p.stroke(255, 255, 255, alpha / 5.3f);
 		p.ellipse(0, hDiff, w * tempCount / 1.5f + 11, w / 3 * tempCount / 1.5f + 11); 
 		p.ellipse(0, flyPos, w * tempCount / 1.5f + flyPos / 2 + 11, w / 3 * tempCount / 1.5f + 11); 
 	}
 
 	//on and off wings
 	public void update() {
 		if(flyPos >= 0) {
 			if(flyPos < flyMax)
 				flyPos = flyPos + 0.7f;
 			else
 				flyPos = -1;
 		} else if(flyPos < 0) {
 			if(flyPos > flyMin)
 				flyPos = flyPos - 0.3f;
 			else
 				flyPos = 0.0f;
 		}
 	}
 
 
 	//on and off scale
 	/*
 	void scaleMott() {
 		if(scale_state==0) {
 			m_scale_state=1;
 		} else if(m_scale_state==-1) {
 			if(m_scale<m_min_scale)
 				m_scale_state=1;
 		}
 		else if(m_scale_state==1) {
 			if(m_scale>m_max_scale)
 				m_scale_state = -1;
 		}
 		m_scale=m_scale+m_scale_state;
 		if(m_scale<=100) 
 			m_alpha=m_scale;
 	}
 	*/
 
 }
 
 
