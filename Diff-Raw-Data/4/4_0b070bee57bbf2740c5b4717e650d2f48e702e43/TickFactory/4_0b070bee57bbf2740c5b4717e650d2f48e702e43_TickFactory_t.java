 /*
  * Copyright 2012 Diamond Light Source Ltd.
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *   http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package org.csstudio.swt.xygraph.linearscale;
 
 import java.math.BigDecimal;
 import java.text.DecimalFormat;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * Tick factory produces the different axis ticks. When specifying a format and
  * given the screen size parameters and range it will return a list of Ticks
  */
 
 public class TickFactory {
 	public enum TickFormatting {
 		/**
 		 * Plain mode no rounding no chopping maximum 6 figures before the 
 		 * fraction point and four after
 		 */
 		plainMode,
 		/**
 		 * Rounded or chopped to the nearest decimal
 		 */
 		roundAndChopMode,
 		/**
 		 * Use Exponent 
 		 */
 		useExponent,
 		/**
 		 * Use SI units (k,M,G,etc.)
 		 */
 		useSIunits,
 		/**
 		 * Use external scale provider
 		 */
 		useCustom;
 	}
 
 	private TickFormatting formatOfTicks;
 	private final static BigDecimal EPSILON = new BigDecimal("1.0E-20");
 	private static final int DIGITS_UPPER_LIMIT = 6; // limit for number of digits to display left of decimal point
 	private static final int DIGITS_LOWER_LIMIT = -6; // limit for number of zeros to display right of decimal point
 	private static final double ROUND_FRACTION = 2e-6; // fraction of denominator to round to
 	private static final BigDecimal BREL_ERROR = new BigDecimal("1e-15");
 	private static final double REL_ERROR = BREL_ERROR.doubleValue();
 
 	private double graphMin;
 	private double graphMax;
 	private String tickFormat;
 	private IScaleProvider scale;
 	private int intervals; // number of intervals
 	private boolean isReversed;
 
 	/**
 	 * @param format
 	 */
 	public TickFactory(IScaleProvider scale) {
 		this(TickFormatting.useCustom, scale);
 	}
 
 	/**
 	 * @param format
 	 */
 	public TickFactory(TickFormatting format, IScaleProvider scale) {
 	   formatOfTicks = format;
 	   this.scale = scale;
 	}
 
 	private String getTickString(double value) {
 		
 		if (scale!=null) value = scale.getLabel(value);
 		
 		String returnString = "";
 		if (Double.isNaN(value))
 			return returnString;
 
 		switch (formatOfTicks) {
 		case plainMode:
 			returnString = String.format(tickFormat, value);
 			break;
 		case useExponent:
 			returnString = String.format(tickFormat, value);
 			break;
 		case roundAndChopMode:
 			returnString = String.format("%d", Math.round(value));
 			break;
 		case useSIunits:
 			double absValue = Math.abs(value);
 			if (absValue == 0.0) {
 				returnString = String.format("%6.2f", value);
 			} else if (absValue <= 1E-15) {
 				returnString = String.format("%6.2ff", value * 1E15);
 			} else if (absValue <= 1E-12) {
 				returnString = String.format("%6.2fp", value * 1E12);
 			} else if (absValue <= 1E-9) {
 				returnString = String.format("%6.2fn", value * 1E9);
 			} else if (absValue <= 1E-6) {
 				returnString = String.format("%6.2fµ", value * 1E6);
 			} else if (absValue <= 1E-3) {
 				returnString = String.format("%6.2fm", value * 1E3);
 			} else if (absValue < 1E3) {
 				returnString = String.format("%6.2f", value);
 			} else if (absValue < 1E6) {
 				returnString = String.format("%6.2fk", value * 1E-3);
 			} else if (absValue < 1E9) {
 				returnString = String.format("%6.2fM", value * 1E-6);
 			} else if (absValue < 1E12) {
 				returnString = String.format("%6.2fG", value * 1E-9);
 			} else if (absValue < 1E15) {
 				returnString = String.format("%6.2fT", value * 1E-12);
 			} else if (absValue < 1E18)
 				returnString = String.format("%6.2fP", value * 1E-15);
 			break;
 		case useCustom:
 			returnString = scale.format(value);
 			break;
 		}
 		return returnString;
 	}
 
 	private void createFormatString(final int precision, final boolean b) {
 		switch (formatOfTicks) {
 		case plainMode:
 			tickFormat = b ? String.format("%%.%de", precision) : String.format("%%.%df", precision);
 			break;
 		case useExponent:
 			tickFormat = String.format("%%.%de", precision);
 			break;
 		default:
 			tickFormat = null;
 			break;
 		}
 	}
 
 	/**
 	 * Round numerator down to multiples of denominators 
 	 * @param n numerator
 	 * @param d denominator
 	 * @return
 	 */
 	protected static double roundDown(BigDecimal n, BigDecimal d) {
 		final int ns = n.signum();
 		if (ns == 0)
 			return 0;
 		final int ds = d.signum();
 		if (ds == 0)
 			throw new IllegalArgumentException("Zero denominator is not allowed");
 
 		n = n.abs();
 		d = d.abs();
 		final BigDecimal[] x = n.divideAndRemainder(d);
 		double rx = x[1].doubleValue();
 		if (rx > (1-ROUND_FRACTION)*d.doubleValue()) {
 			// trim up if close to denominator
 			x[1] = BigDecimal.ZERO;
 			x[0] = x[0].add(BigDecimal.ONE);
 		} else if (rx < ROUND_FRACTION*d.doubleValue()) {
 			x[1] = BigDecimal.ZERO;
 		}
 		final int xs = x[1].signum();
 		if (xs == 0) {
 			return ns != ds ? -x[0].multiply(d).doubleValue() : x[0].multiply(d).doubleValue();
 		} else if (xs < 0) {
 			throw new IllegalStateException("Cannot happen!");
 		}
 
 		if (ns != ds)
 			return x[0].signum() == 0 ? -d.doubleValue() : -x[0].add(BigDecimal.ONE).multiply(d).doubleValue();
 
 		return x[0].multiply(d).doubleValue();
 	}
 
 	/**
 	 * Round numerator up to multiples of denominators 
 	 * @param n numerator
 	 * @param d denominator
 	 * @return
 	 */
 	protected static double roundUp(BigDecimal n, BigDecimal d) {
 		final int ns = n.signum();
 		if (ns == 0)
 			return 0;
 		final int ds = d.signum();
 		if (ds == 0)
 			throw new IllegalArgumentException("Zero denominator is not allowed");
 
 		n = n.abs();
 		d = d.abs();
 		final BigDecimal[] x = n.divideAndRemainder(d);
 		double rx = x[1].doubleValue();
 		if (rx != 0) {
 			if (rx < ROUND_FRACTION*d.doubleValue()) {
 				// trim down if close to zero
 				x[1] = BigDecimal.ZERO;
 			} else if (rx > (1-ROUND_FRACTION)*d.doubleValue()) {
 				x[1] = BigDecimal.ZERO;
 				x[0] = x[0].add(BigDecimal.ONE);
 			}
 		}
 		final int xs = x[1].signum();
 		if (xs == 0) {
 			return ns != ds ? -x[0].multiply(d).doubleValue() : x[0].multiply(d).doubleValue();
 		} else if (xs < 0) {
 			throw new IllegalStateException("Cannot happen!");
 		}
 
 		if (ns != ds)
 			return x[0].signum() == 0 ? 0 : -x[0].multiply(d).doubleValue();
 
 		return x[0].add(BigDecimal.ONE).multiply(d).doubleValue();
 	}
 
 	/**
 	 * @param x
 	 * @return floor of log 10
 	 */
 	private static int log10(BigDecimal x) {
 		int c = x.compareTo(BigDecimal.ONE); 
 		int e = 0;
 		while (c < 0) {
 			e--;
 			x = x.scaleByPowerOfTen(1);
 			c = x.compareTo(BigDecimal.ONE);
 		}
 	
 		c = x.compareTo(BigDecimal.TEN);
 		while (c >= 0) {
 			e++;
 			x = x.scaleByPowerOfTen(-1);
 			c = x.compareTo(BigDecimal.TEN);
 		}
 	
 		return e;
 	}
 
 	/**
 	 * @param x
 	 * @param round if true, then round else take ceiling
 	 * @return a nice number
 	 */
 	protected static BigDecimal nicenum(BigDecimal x, boolean round) {
 			int expv; /* exponent of x */
 			double f; /* fractional part of x */
 			double nf; /* nice, rounded number */
 			BigDecimal bf;
 	
 			expv = log10(x);
 			bf = x.scaleByPowerOfTen(-expv);
 			f = bf.doubleValue(); /* between 1 and 10 */
 			if (round) {
 				if (f < 1.5)
 					nf = 1;
 				else if (f < 2.25)
 					nf = 2;
 				else if (f < 3.25)
 					nf = 2.5;
 				else if (f < 7.5)
 					nf = 5;
 				else
 					nf = 10;
 			}
 			else if (f <= 1.)
 				nf = 1;
 			else if (f <= 2.)
 				nf = 2;
 			else if (f <= 5.)
 				nf = 5;
 			else
 				nf = 10;
 		return BigDecimal.valueOf(BigDecimal.valueOf(nf).scaleByPowerOfTen(expv).doubleValue());
 	}
 
 	private double determineNumTicks(int size, double min, double max, int maxTicks,
 			boolean allowMinMaxOver, boolean isIndexBased) {
 		BigDecimal bMin = BigDecimal.valueOf(min);
 		BigDecimal bMax = BigDecimal.valueOf(max);
 		BigDecimal bRange = bMax.subtract(bMin);
 		if (bRange.signum() < 0) {
 			BigDecimal bt = bMin;
 			bMin = bMax;
 			bMax = bt;
 			bRange = bRange.negate();
 			isReversed = true;
 		} else {
 			isReversed = false;
 		}
 
 		BigDecimal magnitude = BigDecimal.valueOf(Math.max(Math.abs(min), Math.abs(max)));
 		// tick points too dense to do anything
 		if (bRange.compareTo(EPSILON.multiply(magnitude)) < 0) {
 			return 0;
 		}
 	
 		bRange = nicenum(bRange, false);
 		BigDecimal bUnit;
 		int nTicks = maxTicks - 1;
 		if (Math.signum(min)*Math.signum(max) < 0) {
 			// straddle case
 			nTicks++;
 		}
 		do {
 			long n;
 			do { // ensure number of ticks is less or equal to number requested
 				bUnit = nicenum(BigDecimal.valueOf(bRange.doubleValue() / nTicks), true);
 				n = bRange.divideToIntegralValue(bUnit).longValue();
 			} while (n > maxTicks && --nTicks > 0);
 
 			if (allowMinMaxOver) {
 				graphMin = roundDown(bMin, bUnit);
 				if (graphMin == 0) // ensure positive zero
 					graphMin = 0;
 				graphMax = roundUp(bMax, bUnit);
 				if (graphMax == 0)
 					graphMax = 0;
 			} else {
 				graphMin = min;
 				graphMax = max;
 			}
 			if (bUnit.compareTo(BREL_ERROR.multiply(magnitude)) <= 0) {
 				intervals = -1; // signal that we hit the limit of precision
 			} else {
 				intervals = (int) Math.round((graphMax - graphMin) / bUnit.doubleValue());
 			}
 		} while (intervals > maxTicks && --nTicks > 0);
 		if (isReversed) {
 			double t = graphMin;
 			graphMin = graphMax;
 			graphMax = t;
 		}
 		double tickUnit = isReversed ? -bUnit.doubleValue() : bUnit.doubleValue();
 
 		if (isIndexBased) {
 			switch (formatOfTicks) {
 			case plainMode:
 				tickFormat = "%g";
 				break;
 			case useExponent:
 				tickFormat = "%e";
 				break;
 			default:
 				tickFormat = null;
 				break;
 			}
 		} else {
 			/**
 			 * We get the labelled max and min for determining the precision
 			 * which the ticks should be shown at.
 			 */
 			int d = bUnit.scale() == bUnit.precision() ? -bUnit.scale() : bUnit.precision() - bUnit.scale() - 1;
 			int p = (int) Math.max(Math.floor(Math.log10(Math.abs(graphMin))),
 					Math.floor(Math.log10(Math.abs(graphMax))));
 			// System.err.println("P: " + bUnit.precision() + ", S: " +
 			// bUnit.scale() + " => " + d + ", " + p);
 			if (p <= DIGITS_LOWER_LIMIT || p >= DIGITS_UPPER_LIMIT) {
 				createFormatString(Math.max(p - d, 0), true);
 			} else {
 				createFormatString(Math.max(-d, 0), false);
 			}
 		}
 		return tickUnit;
 	}
 
 	private boolean inRange(double x, double min, double max) {
 		if (isReversed) {
 			return x >= max && x <= min;
 		}
 		return x >= min && x <= max;
 	}
 
 	private static final DecimalFormat INDEX_FORMAT = new DecimalFormat("#####0.###");
 
 	/**
 	 * Generate a list of ticks that span range given by min and max. The maximum number of
 	 * ticks is exceed by one in the case where the range straddles zero.
 	 * @param displaySize 
 	 * @param min
 	 * @param max
 	 * @param maxTicks 
 	 * @param allowMinMaxOver allow min/maximum overwrite
 	 * @param tight if true then remove ticks outside range 
 	 * @return a list of the ticks for the axis
 	 */
 	public List<Tick> generateTicks(int displaySize, double min, double max, int maxTicks,
 			boolean allowMinMaxOver, final boolean tight, final boolean isIndexBased) {
 		List<Tick> ticks = new ArrayList<Tick>();
 		double tickUnit = determineNumTicks(displaySize, min, max, maxTicks, allowMinMaxOver, isIndexBased);
 		if (tickUnit == 0)
 			return ticks;
 
 		for (int i = 0; i <= intervals; i++) {
 			double p = graphMin + i * tickUnit;
 			if (Math.abs(p/tickUnit) < REL_ERROR)
 				p = 0; // ensure positive zero
 			boolean r = inRange(p, min, max);
 			if (!tight || r) {
 				Tick newTick = new Tick();
 				newTick.setValue(p);
 				newTick.setText(getTickString(p));
 				ticks.add(newTick);
 			}
 		}
 
 		int imax = ticks.size();
 		if (imax > 1) {
 			if (!tight && allowMinMaxOver) {
 				Tick t = ticks.get(imax - 1);
 				if (!isReversed && t.getValue() < max) { // last is >= max
 					t.setValue(graphMax);
 					t.setText(getTickString(graphMax));
 				}
 			}
 
 			double lo = tight ? min : ticks.get(0).getValue();
 			double hi = tight ? max : ticks.get(imax - 1).getValue();
 			double range = hi - lo;
 
 			if (isReversed) {
 				for (Tick t : ticks) {
 					t.setPosition(1 - (t.getValue() - lo) / range);
 				}
 			} else {
 				for (Tick t : ticks) {
 					t.setPosition((t.getValue() - lo) / range);
 				}
 			}
 		} else if (maxTicks > 1) {
 			if (imax == 0) {
 				imax++;
 				Tick newTick = new Tick();
 				newTick.setValue(graphMin);
 				newTick.setText(getTickString(graphMin));
 				if (isReversed) {
 					newTick.setPosition(1);
 				} else {
 					newTick.setPosition(0);
 				}
 				ticks.add(newTick);
 			}
 			if (imax == 1) {
 				Tick t = ticks.get(0);
 				Tick newTick = new Tick();
 				if (t.getText().equals(getTickString(graphMax))) {
 					newTick.setValue(graphMin);
 					newTick.setText(getTickString(graphMin));
 					ticks.add(0, newTick);
 				} else {
 					newTick.setValue(graphMax);
 					newTick.setText(getTickString(graphMax));
 					ticks.add(newTick);
 				}
 				if (isReversed) {
 					ticks.get(0).setPosition(1);
 					ticks.get(1).setPosition(0);
 				} else {
					ticks.get(0).setPosition(0);
					ticks.get(1).setPosition(1);
 				}
 			}
 		}
 		if (isIndexBased && formatOfTicks == TickFormatting.plainMode) {
 			double vmin = Double.POSITIVE_INFINITY;
 			double vmax = Double.NEGATIVE_INFINITY;
 			for (Tick t : ticks) {
 				double v = Math.abs(scale.getLabel(t.getValue()));
 				if (v < vmin && v > 0)
 					vmin = v;
 				if (v > vmax)
 					vmax = v;
 			}
 			if (Math.log10(vmin) >= DIGITS_LOWER_LIMIT || Math.log10(vmax) <= DIGITS_UPPER_LIMIT) {
 				// override labels
 				for (Tick t : ticks) {
 					double v = scale.getLabel(t.getValue());
 					t.setText(INDEX_FORMAT.format(v));
 				}
 			}
 		}
 		return ticks;
 	}
 
 	private double determineNumLogTicks(int size, double min, double max, int maxTicks,
 			boolean allowMinMaxOver) {
 		final boolean isReverse = min > max;
 		final int loDecade; // lowest decade (or power of ten)
 		final int hiDecade;
 		if (isReverse) {
 			loDecade = (int) Math.floor(Math.log10(max));
 			hiDecade = (int) Math.ceil(Math.log10(min));
 		} else {
 			loDecade = (int) Math.floor(Math.log10(min));
 			hiDecade = (int) Math.ceil(Math.log10(max));
 		}
 	
 		int decades = hiDecade - loDecade;
 
 		int unit = 0;
 		int n;
 		do {
 			n = decades/++unit;
 		} while (n > maxTicks);	
 
 		double tickUnit = isReverse ? Math.pow(10, -unit) : Math.pow(10, unit);
 		if (allowMinMaxOver) {
 			graphMin = Math.pow(10, loDecade);
 			graphMax = Math.pow(10, hiDecade);
 		} else {
 			graphMin = min;
 			graphMax = max;
 		}
 		if (isReverse) {
 			double t = graphMin;
 			graphMin = graphMax;
 			graphMax = t;
 		}
 	
 		createFormatString((int) Math.max(-Math.floor(loDecade), 0), false);
 		return tickUnit;
 	}
 
 	/**
 	 * @param displaySize 
 	 * @param min
 	 * @param max
 	 * @param maxTicks
 	 * @param allowMinMaxOver allow min/maximum overwrite
 	 * @param tight if true then remove ticks outside range 
 	 * @return a list of the ticks for the axis
 	 */
 	public List<Tick> generateLogTicks(int displaySize, double min, double max, int maxTicks,
 			boolean allowMinMaxOver, final boolean tight) {
 		List<Tick> ticks = new ArrayList<Tick>();
 		double tickUnit = determineNumLogTicks(displaySize, min, max, maxTicks, allowMinMaxOver);
 		double p = graphMin;
 		if (tickUnit > 1) {
 			final double pmax = graphMax * Math.sqrt(tickUnit);
 			while (p < pmax) {
 				if (!tight || (p >= min && p <= max))
 				if (allowMinMaxOver || p <= max) {
 					Tick newTick = new Tick();
 					newTick.setValue(p);
 					newTick.setText(getTickString(p));
 					ticks.add(newTick);
 				}
 				double newTickValue = p * tickUnit;
 				if (p == newTickValue)
 					break;
 				p = newTickValue;
 			}
 			final int imax = ticks.size();
 			if (imax == 1) {
 				ticks.get(0).setPosition(0.5);
 			} else if (imax > 1) {
 				double lo = Math.log(tight ? min : ticks.get(0).getValue());
 				double hi = Math.log(tight ? max : ticks.get(imax - 1).getValue());
 				double range = hi - lo;
 				for (Tick t : ticks) {
 					t.setPosition((Math.log(t.getValue()) - lo) / range);
 				}
 			}
 		} else {
 			final double pmin = graphMax * Math.sqrt(tickUnit);
 			while (p > pmin) {
 				if (!tight || (p >= max && p <= min))
 				if (allowMinMaxOver || p <= max) {
 					Tick newTick = new Tick();
 					newTick.setValue(p);
 					newTick.setText(getTickString(p));
 				}
 				double newTickValue = p * tickUnit;
 				if (p == newTickValue)
 					break;
 				p = newTickValue;
 			}
 			final int imax = ticks.size();
 			if (imax == 1) {
 				ticks.get(0).setPosition(0.5);
 			} else if (imax > 1) {
 				double lo = Math.log(tight ? max : ticks.get(0).getValue());
 				double hi = Math.log(tight ? min : ticks.get(imax - 1).getValue());
 				double range = hi - lo;
 				for (Tick t : ticks) {
 					t.setPosition(1 - (Math.log(t.getValue()) - lo) / range);
 				}
 			}
 		}
 		return ticks;
 	}
 }
