 package com.iteye.weimingtom.tween;
 
 /**
  * @see http://code.google.com/p/tweener/source/browse/trunk/as3/caurina/transitions/Equations.as
  * @author Administrator
  *
  */
 public abstract class SimpleTweener {
 	private static double CHECK_RANGE_MIN = 0.001;
 	
 	private long currentTime, totoalTime;
 	private float x, y, dx, dy, cx, cy;
 	private boolean isStarted;
 	private boolean enableTimeCheck = true;
 	
 	public SimpleTweener() {
 		
 	}
 	
 	public boolean isTweening() {
 		return isStarted;
 	}
 	
 	public void startTween(float x1, float x2, float y1, float y2, long t) {
 		this.x = x1;
 		this.y = y1;
 		this.dx = x2 - x1;
 		this.dy = y2 - y1;
 		this.totoalTime = t;
 		this.currentTime = 0;
 		this.isStarted = true;
 	}
 	
 	public boolean update() {
 		if (isStarted) {
 			if (enableTimeCheck) {
				if (this.currentTime > this.totoalTime) {
 					this.isStarted = false;
 					this.cx = this.cy = 0;
 					return false;
				} else if (this.currentTime == this.totoalTime) {
					this.cx = this.x + this.dx;
					this.cy = this.y + this.dy;
					++this.currentTime;
					return true;
 				} else {
 					this.cx = interpolator(currentTime, x, dx, totoalTime);
 					this.cy = interpolator(currentTime, y, dy, totoalTime);
 					++this.currentTime;
 					return true;
 				}
 			} else {
 				this.cx = interpolator(currentTime, x, dx, totoalTime);
 				this.cy = interpolator(currentTime, y, dy, totoalTime);
 				if (Math.abs(this.x + this.dx - this.cx) < CHECK_RANGE_MIN && 
 					Math.abs(this.y + this.dy - this.cy) < CHECK_RANGE_MIN) {
 					this.isStarted = false;
 					this.cx = this.x + this.dx;
 					this.cy = this.y + this.dy;
 					//System.out.println("SimpleTweener update is over...");
 					return true;
 				} else {
 					++this.currentTime;
 					return true;
 				}
 			}
 		} else {
 			this.cx = this.cy = 0;
 			return false;
 		}
 	}
 	
 	public float currentX() {
 		return cx;
 	}
 	
 	public float currentY() {
 		return cy;
 	}
 	
 	public void setEnableTimeCheck(boolean enableTimeCheck) {
 		this.enableTimeCheck = enableTimeCheck;
 	}
 	
 	@Override
 	public String toString() {
 		return "currentTime = " + currentTime + ", " +
 			"totoalTime = " + totoalTime + ", " +
 			"cx = " + cx + ", " + 
 			"cy = " + cy + ", " + 
 			"x = " + (x + dx) + ", " + 
 			"y = " + (y + dy);
 	}
 	
 	/*
 	 * c*t/d + b
 	 */
 	protected abstract float interpolator(float t, float b, float c, float d);
 	
 	public static class EaseNone extends SimpleTweener {
 		@Override
 		protected float interpolator(float t, float b, float c, float d) {
 			return c * t / d + b;
 		}
 	}
 	
 	public static class EaseInQuad extends SimpleTweener {
 		@Override
 		protected float interpolator(float t, float b, float c, float d) {
 			t /= d;
 			return c * t * t + b;
 		}
 	}
 	
 	public static class EaseOutQuad extends SimpleTweener {
 		@Override
 		protected float interpolator(float t, float b, float c, float d) {
 			t /= d;
 			return -c * t * (t - 2) + b;
 		}
 	}	
 
 	public static class EaseInOutQuad extends SimpleTweener {
 		@Override
 		protected float interpolator(float t, float b, float c, float d) {
 			t /= (d / 2.0f);
 			if (t < 1) {
 				return c / 2.0f * t * t + b;
 			}
 			--t;
 			return - c / 2.0f * (t * (t - 2.0f) - 1.0f) + b;
 		}
 	}
 	
 	//FIXME:easeOutInQuad 
 	public static class EaseOutInQuad extends SimpleTweener {
 		private EaseInQuad easeInQuad = new EaseInQuad();
 		private EaseOutQuad easeOutQuad = new EaseOutQuad();
 		
 		@Override
 		protected float interpolator(float t, float b, float c, float d) {
             if (t < d / 2) 
             	return easeOutQuad.interpolator(t * 2, b, c / 2, d);
             return easeInQuad.interpolator((t * 2) - d, b + c / 2, c / 2, d);
 		}
 	}
 	
 	public static class EaseInCubic extends SimpleTweener {
 		@Override
 		protected float interpolator(float t, float b, float c, float d) {
 			t /= d;
 			return c * t * t * t + b;
 		}
 	}
 
 	public static class EaseOutCubic extends SimpleTweener {
 		@Override
 		protected float interpolator(float t, float b, float c, float d) {
 			t = t / d - 1.0f;
 			return c * (t * t * t + 1) + b;
 		}
 	}
 	
 	public static class EaseInOutCubic extends SimpleTweener {
 		@Override
 		protected float interpolator(float t, float b, float c, float d) {
 			t /= (d / 2.0f);
 			if (t < 1.0f) {
 				return c / 2.0f * t * t * t + b;
 			}
 			t -= 2.0f;
 			return c / 2.0f * (t * t * t + 2.0f) + b;
 		}
 	}
 
 	//FIXME: easeOutInCubic 
 	public static class EaseOutInCubic extends SimpleTweener {
 		private EaseInCubic easeInCubic = new EaseInCubic();
 		private EaseOutCubic easeOutCubic = new EaseOutCubic();
 		
 		@Override
 		protected float interpolator(float t, float b, float c, float d) {
             if (t < d / 2) 
             	return easeOutCubic.interpolator(t * 2, b, c / 2, d);
             return easeInCubic.interpolator((t * 2) - d, b + c / 2, c / 2, d);
 		}
 	}
 	
 	public static class EaseInQuart extends SimpleTweener {
 		@Override
 		protected float interpolator(float t, float b, float c, float d) {
 			t /= d;
 			return c * t * t * t * t + b;
 		}
 	}
 
 	public static class EaseOutQuart extends SimpleTweener {
 		@Override
 		protected float interpolator(float t, float b, float c, float d) {
 			t = t / d - 1.0f;
 			return -c * (t * t * t * t - 1.0f) + b;
 		}
 	}
 	
 	public static class EaseInOutQuart extends SimpleTweener {
 		@Override
 		protected float interpolator(float t, float b, float c, float d) {
 			t /= d / 2.0f;
 			if (t < 1) 
             	return c / 2.0f * t * t * t * t + b;
 			t -= 2.0f;
 			return -c / 2.0f * (t * t * t * t - 2.0f) + b;
 		}
 	}
 	
 	//FIXME:easeOutInQuart 
 	public static class EaseOutInQuart extends SimpleTweener {
 		private EaseInQuart easeInQuart = new EaseInQuart();
 		private EaseOutQuart easeOutQuart = new EaseOutQuart();
 		
 		@Override
 		protected float interpolator(float t, float b, float c, float d) {
             if (t < d / 2) 
             	return easeOutQuart.interpolator(t * 2, b, c / 2, d);
             return easeInQuart.interpolator((t * 2) - d, b + c / 2, c / 2, d);
 		}
 	}
 	
 	public static class EaseInQuint extends SimpleTweener {
 		@Override
 		protected float interpolator(float t, float b, float c, float d) {
 			t /= d;
 			return c * t * t * t * t * t + b;
 		}
 	}
 	
 	public static class EaseOutQuint extends SimpleTweener {
 		@Override
 		protected float interpolator(float t, float b, float c, float d) {
 			t = t / d - 1.0f;
 			return c * (t * t * t * t * t + 1.0f) + b;
 		}
 	}
 	
 	public static class EaseInOutQuint extends SimpleTweener {
 		@Override
 		protected float interpolator(float t, float b, float c, float d) {
 			t /= d / 2.0f;
 			if (t < 1) 
 				return c / 2.0f * t * t * t * t * t + b;
 			t -= 2.0f;
 			return c / 2.0f * (t * t * t * t * t + 2.0f) + b;
 		}
 	}
 	
 	//FIXME:easeOutInQuint 
 	public static class EaseOutInQuint extends SimpleTweener {
 		private EaseInQuint easeInQuint = new EaseInQuint();
 		private EaseOutQuint easeOutQuint = new EaseOutQuint();
 		
 		@Override
 		protected float interpolator(float t, float b, float c, float d) {
             if (t < d / 2) 
             	return easeOutQuint.interpolator(t * 2, b, c / 2, d);
             return easeInQuint.interpolator((t * 2) - d, b + c / 2, c / 2, d);
 		}
 	}
 	
 	public static class EaseInSine extends SimpleTweener {
 		@Override
 		protected float interpolator(float t, float b, float c, float d) {
 			return -c * (float)Math.cos(t / d * (Math.PI / 2)) + c + b;
 		}
 	}
 
 	public static class EaseOutSine extends SimpleTweener {
 		@Override
 		protected float interpolator(float t, float b, float c, float d) {
 			return c * (float)Math.sin(t / d * (Math.PI / 2)) + b;
 		}
 	}
 
 	public static class EaseInOutSine extends SimpleTweener {
 		@Override
 		protected float interpolator(float t, float b, float c, float d) {
 			return -c / 2 * ((float)Math.cos(Math.PI * t / d) - 1) + b;
 		}
 	}
 	
 	//FIXME:easeOutInSine
 	public static class EaseOutInSine extends SimpleTweener {
 		private EaseInSine easeInSine = new EaseInSine();
 		private EaseOutSine easeOutSine = new EaseOutSine();
 		
 		@Override
 		protected float interpolator(float t, float b, float c, float d) {
             if (t < d / 2) 
             	return easeOutSine.interpolator(t * 2, b, c / 2, d);
             return easeInSine.interpolator((t * 2) - d, b + c / 2, c / 2, d);
 		}
 	}
 	
 	public static class EaseInExpo extends SimpleTweener {
 		@Override
 		protected float interpolator(float t, float b, float c, float d) {
 			return (t == 0) ? b : c * (float)Math.pow(2, 10 * (t / d - 1)) + b - c * 0.001f;
 		}
 	}	
 
 	public static class EaseOutExpo extends SimpleTweener {
 		@Override
 		protected float interpolator(float t, float b, float c, float d) {
 			return (t == d) ? b + c : c * 1.001f * (-(float)Math.pow(2, -10 * t / d) + 1) + b;
 		}
 	}
 	
 	public static class EaseInOutExpo extends SimpleTweener {
 		@Override
 		protected float interpolator(float t, float b, float c, float d) {
             if (t == 0) 
             	return b;
             if (t == d) 
             	return b + c;
             t /= d / 2;
             if (t < 1)
             	return c / 2 * (float)Math.pow(2, 10 * (t - 1)) + b - c * 0.0005f;
             --t;
             return c / 2 * 1.0005f * (-(float)Math.pow(2, -10 * t) + 2f) + b;
 		}
 	}
 	
 	//FIXME:easeOutInExpo
 	public static class EaseOutInExpo extends SimpleTweener {
 		private EaseInExpo easeInExpo = new EaseInExpo();
 		private EaseOutExpo easeOutExpo = new EaseOutExpo();
 		
 		@Override
 		protected float interpolator(float t, float b, float c, float d) {
             if (t < d/2) 
             	return easeOutExpo.interpolator(t * 2, b, c / 2, d);
             return easeInExpo.interpolator((t * 2) - d, b + c / 2, c / 2, d);
 		}
 	}
 	
 	public static class EaseInCirc extends SimpleTweener {
 		@Override
 		protected float interpolator(float t, float b, float c, float d) {
 			t /= d;
 			return -c * ((float)Math.sqrt(1 - t * t) - 1) + b;
 		}
 	}
 	
 	public static class EaseOutCirc extends SimpleTweener {
 		@Override
 		protected float interpolator(float t, float b, float c, float d) {
 			t = t / d - 1;
 			return c * (float)Math.sqrt(1 - t * t) + b;
 		}
 	}	
 	
 	public static class EaseInOutCirc extends SimpleTweener {
 		@Override
 		protected float interpolator(float t, float b, float c, float d) {
 			t /= d / 2;
 			if (t < 1) 
 				return -c / 2 * ((float)Math.sqrt(1 - t * t) - 1) + b;
 			t -= 2;
 			return c / 2 * ((float)Math.sqrt(1 - t * t) + 1) + b;
 		}
 	}
 	
 	//FIXME: easeOutInCirc
 	public static class EaseOutInCirc extends SimpleTweener {
 		private EaseInCirc easeInCirc = new EaseInCirc();
 		private EaseOutCirc easeOutCirc = new EaseOutCirc();
 		
 		@Override
 		protected float interpolator(float t, float b, float c, float d) {
             if (t < d / 2) 
             	return easeOutCirc.interpolator(t * 2, b, c / 2, d);
             return easeInCirc.interpolator((t * 2) - d, b + c / 2, c / 2, d);
 		}
 	}
 	
 	public static class EaseInElastic extends SimpleTweener {
 		private double amplitude;
 		private double period;
 		
 		public EaseInElastic(double period, double amplitude) {
 			this.period = period;
 			this.amplitude = amplitude;
 		}
 		
 		@Override
 		protected float interpolator(float t, float b, float c, float d) {
             if (t == 0) 
             	return b;
             t /= d;
             if (t == 1) 
             	return b + c;
             double p = Double.isNaN(this.period) ? d * 0.3 : this.period;
             double s;
             double a = Double.isNaN(this.amplitude) ? 0 : this.amplitude;
             if (a == 0 || a < Math.abs(c)) {
             	a = c;
                 s = p / 4;
             } else {
                 s = (p / (2 * Math.PI) * Math.asin(c / a));
             }
             t -= 1;
             return -((float)a * (float)Math.pow(2, 10 * t) * (float)Math.sin((t * d - s) * (2 * Math.PI) / p )) + b;
 		}
 	}
 	
 	public static class EaseOutElastic extends SimpleTweener {
 		private double amplitude;
 		private double period;
 		
 		public EaseOutElastic(double period, double amplitude) {
 			this.period = period;
 			this.amplitude = amplitude;
 		}
 		
 		@Override
 		protected float interpolator(float t, float b, float c, float d) {
             if (t == 0) 
             	return b;
             t /= d;
             if (t == 1) 
             	return b + c;
             double p = Double.isNaN(this.period) ? d * 0.3 : this.period;
             double s;
             double a = Double.isNaN(this.amplitude) ? 0 : this.amplitude;
             if (a == 0 || a < Math.abs(c)) {
             	a = c;
                 s = p / 4;
             } else {
                 s = (p / (2 * Math.PI) * Math.asin(c / a));
             }
             return ((float)a * (float)Math.pow(2, -10 * t) * (float)Math.sin((t * d - s) * (2 * Math.PI) / p) + c + b);
 		}
 	}
 	
 	public static class EaseInOutElastic extends SimpleTweener {
 		private double amplitude;
 		private double period;
 		
 		public EaseInOutElastic(double period, double amplitude) {
 			this.period = period;
 			this.amplitude = amplitude;
 		}
 		
 		@Override
 		protected float interpolator(float t, float b, float c, float d) {
             if (t == 0) 
             	return b;
             t /= d / 2;
             if (t == 2) 
             	return b + c;
             double p = Double.isNaN(this.period) ? d * (0.3 * 1.5) : this.period;
             double s;
             double a = Double.isNaN(this.amplitude) ? 0 : this.amplitude;
             if (a == 0 || a < Math.abs(c)) {
             	a = c;
                 s = p / 4;
             } else {
                 s = (p / (2 * Math.PI) * Math.asin(c / a));
             }
             if (t < 1) { 
             	t -= 1;
             	return - 0.5f *((float)a * (float)Math.pow(2, 10 * t) * (float)Math.sin((t * d - s) * (2 * Math.PI) / p)) + b;
             }
             t -= 1;
             return (float)a * (float)Math.pow(2, -10 * t) * (float)Math.sin((t * d - s) * (2 * Math.PI) / p) * 0.5f + c + b;
 		}
 	}
 	
 	//FIXME: easeOutInElastic
 	public static class EaseOutInElastic extends SimpleTweener {
 		private EaseInElastic easeInElastic;
 		private EaseOutElastic easeOutElastic;
 		
 		public EaseOutInElastic(double period, double amplitude) {
 			easeInElastic = new EaseInElastic(period, amplitude);
 			easeOutElastic = new EaseOutElastic(period, amplitude);
 		}
 		
 		@Override
 		protected float interpolator(float t, float b, float c, float d) {
             if (t < d / 2) 
             	return easeOutElastic.interpolator(t * 2, b, c / 2, d);
             return easeInElastic.interpolator((t * 2) - d, b + c / 2, c / 2, d);
 		}
 	}
 	
 	public static class EaseInBack extends SimpleTweener {
 		private double overshoot;
 		
 		public EaseInBack(double overshoot) {
 			this.overshoot = overshoot;
 		}
 		
 		@Override
 		protected float interpolator(float t, float b, float c, float d) {
 			float s = Double.isNaN(overshoot) ? 1.70158f : (float)overshoot;
 			t /= d;
 			return c * t * t * ((s + 1) * t - s) + b;
 		}
 	}
 	
 	public static class EaseOutBack extends SimpleTweener {
 		private double overshoot;
 		
 		public EaseOutBack(double overshoot) {
 			this.overshoot = overshoot;
 		}
 		
 		@Override
 		protected float interpolator(float t, float b, float c, float d) {
 			float s = Double.isNaN(overshoot) ? 1.70158f : (float)overshoot;
 			t = t / d - 1;
 			return c*(t * t * ((s + 1) * t + s) + 1) + b;
 		}
 	}
 	
 	public static class EaseInOutBack extends SimpleTweener {
 		private double overshoot;
 		
 		public EaseInOutBack(double overshoot) {
 			this.overshoot = overshoot;
 		}
 		
 		@Override
 		protected float interpolator(float t, float b, float c, float d) {
 			float s = Double.isNaN(overshoot) ? 1.70158f : (float)overshoot;
 			t /= d / 2;
 			if (t < 1) {
 				overshoot *= (1.525);
             	return c/2*(t*t*((s + 1) * t - s)) + b;
 			}
 			t -= 2;
 			s *= (1.525);
 			return c / 2 * (t * t * ((s + 1) * t + s) + 2) + b;
 		}
 	}
 	
 	//FIXME:easeOutInBack 
 	public static class EaseOutInBack extends SimpleTweener {
 		private EaseInBack easeInBack;
 		private EaseOutBack easeOutBack;
 		
 		public EaseOutInBack(double overshoot) {
 			easeInBack = new EaseInBack(overshoot);
 			easeOutBack = new EaseOutBack(overshoot);
 		}
 		
 		@Override
 		protected float interpolator(float t, float b, float c, float d) {
             if (t < d / 2) 
             	return easeOutBack.interpolator(t * 2, b, c / 2, d);
             return easeInBack.interpolator((t * 2) - d, b + c / 2, c / 2, d);
 		}
 	}
 	
 	//FIXME:easeInBounce
 	public static class EaseInBounce extends SimpleTweener {
 		private EaseOutBounce easeOutBounce = new EaseOutBounce();
 		
 		@Override
 		protected float interpolator(float t, float b, float c, float d) {
 			return c - easeOutBounce.interpolator(d - t, 0, c, d) + b;
 		}
 	}
 	
 	public static class EaseOutBounce extends SimpleTweener {
 		@Override
 		protected float interpolator(float t, float b, float c, float d) {
 			t /= d;
 			if (t < (1.0 / 2.75)) {
                 return c * (7.5625f * t * t) + b;
             } else if (t < (2.0 / 2.75)) {
             	t -= (1.5 / 2.75);
                 return c *(7.5625f * t * t + 0.75f) + b;
             } else if (t < (2.5 / 2.75)) {
             	t -= (2.25 / 2.75);
                 return c * (7.5625f * t * t + 0.9375f) + b;
             } else {
             	t -= (2.625 / 2.75);
                 return c * (7.5625f * t * t + 0.984375f) + b;
             }
 		}
 	}
 	
 	//FIXME:easeInOutBounce
 	public static class EaseInOutBounce extends SimpleTweener {
 		private EaseInBounce easeInBounce = new EaseInBounce();
 		private EaseOutBounce easeOutBounce = new EaseOutBounce();
 		
 		@Override
 		protected float interpolator(float t, float b, float c, float d) {
             if (t < d / 2) 
             	return easeInBounce.interpolator(t * 2, 0, c, d) * 0.5f + b;
             else return easeOutBounce.interpolator(t * 2 - d, 0, c, d) * 0.5f + c * 0.5f + b;
 		}
 	}
 	
 	//FIXME:easeOutInBounce
 	public static class EaseOutInBounce extends SimpleTweener {
 		private EaseInBounce easeInBounce = new EaseInBounce();
 		private EaseOutBounce easeOutBounce = new EaseOutBounce();
 		
 		@Override
 		protected float interpolator(float t, float b, float c, float d) {
             if (t < d / 2) 
             	return easeOutBounce.interpolator(t * 2, b, c/2, d);
             return easeInBounce.interpolator((t * 2) - d, b + c / 2, c / 2, d);
 		}
 	}	
 }
 
