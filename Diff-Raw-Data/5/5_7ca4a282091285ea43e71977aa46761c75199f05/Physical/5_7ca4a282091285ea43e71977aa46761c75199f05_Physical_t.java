 package de.futjikato.mrwhiz;
 
 public abstract class Physical {
 
 	private float x;
 	private float y;
 
 	private float yvel = 0;
 	private float xvel = 0;
 
 	private float maxYVal;
 	private float grip;
 
 	public float getYVel() {
 		return this.yvel;
 	}
 
 	public void setYVel(float vel) {
 		this.yvel = vel;
 	}
 
 	public float getXvel() {
 		return xvel;
 	}
 
 	public void setXvel(float xvel) {
 		this.xvel = xvel;
 	}
 
 	public float getMaxYVal() {
 		return maxYVal;
 	}
 
 	public void setMaxYVal(float maxYVal) {
 		this.maxYVal = maxYVal;
 	}
 
 	public float getGrip() {
 		return grip;
 	}
 
 	public void setGrip(float grip) {
 		this.grip = grip;
 	}
 
 	public float getX() {
 		return x;
 	}
 
 	public void setX(float x) {
 		this.x = x;
 	}
 
 	public float getY() {
 		return y;
 	}
 
 	public void setY(float y) {
 		this.y = y;
 	}
 
 	protected void calcNewPos(float x, float y, int blocksize, long delta) {
 		// calc new velocitys
 		float yv = this.getYVel();
 		float xv = this.getXvel();
 
 		// calculate new falling speed
 		if (yv < 0) {
 			yv += delta * 0.005f;
 		} else {
 			yv += delta * 0.002f;
 		}
 
 		// calculate new x-axis speed
 		if (xv > 0) {
			xv -= delta * 0.01f;
 			if (xv < 0)
 				xv = 0;
 		} else if (xv < 0) {
			xv += delta * 0.01f;
 			if (xv > 0)
 				xv = 0;
 		}
 
 		// limit fall speed
 		if (yv > this.getMaxYVal()) {
 			yv = this.getMaxYVal();
 		}
 
 		// set new velocitys
 		this.setYVel(yv);
 		this.setXvel(xv);
 
 		// set new y position
 		float ny = delta * yv;
 		float nx = delta * xv;
 
 		if (this.yCol(x, y + ny, blocksize)) {
 			this.setY(this.getY() + ny);
 		} else {
 			// reset y velocity on landing somewhere
 			this.setYVel(0);
 		}
 
 		if (this.xCol(x + nx, y, blocksize)) {
 			this.setX(this.getX() + nx);
 		}
 	}
 
 	protected abstract boolean yCol(float x, float y, int blocksize);
 
 	protected abstract boolean xCol(float x, float y, int blocksize);
 }
