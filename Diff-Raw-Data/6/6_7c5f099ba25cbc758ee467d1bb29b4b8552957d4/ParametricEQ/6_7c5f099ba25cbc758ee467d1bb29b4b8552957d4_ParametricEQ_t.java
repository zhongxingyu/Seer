 package cadenza.core.plugins;
 
import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 import cadenza.gui.plugins.view.ParametricEQView;
 import cadenza.gui.plugins.view.PluginView;
 
 import common.Utils;
 
 public class ParametricEQ implements Plugin {
 	private static final long serialVersionUID = 1L;
 	
	public static class Band implements Serializable {
		private static final long serialVersionUID = 1L;
		
 		private int _frequency;
 		private double _gain;
 		private double _quality;
 		private boolean _lowShelf;
 		private boolean _highShelf;
 		
 		private double[] _levels;
 		
 		public Band(int frequency, double gain, double quality) {
 			_frequency = frequency;
 			_gain = gain;
 			_quality = quality;
 			_lowShelf = _highShelf = false;
 			
 			_levels = new double[128];
 			recalculate();
 		}
 		
 		public int getFrequency() {
 			return _frequency;
 		}
 		
 		public void setFrequency(int frequency) {
 			_frequency = frequency;
 			recalculate();
 		}
 		
 		public double getGain() {
 			return _gain;
 		}
 		
 		public void setGain(double gain) {
 			_gain = gain;
 			recalculate();
 		}
 		
 		public double getQuality() {
 			return _quality;
 		}
 		
 		public void setQuality(double quality) {
 			_quality = quality;
 			recalculate();
 		}
 		
 		public boolean isLowShelf() {
 			return _lowShelf;
 		}
 		
 		public void setLowShelf(boolean lowShelf) {
 			_lowShelf = lowShelf;
 			if (lowShelf)
 				_highShelf = false;
 			recalculate();
 		}
 		
 		public boolean isHighShelf() {
 			return _highShelf;
 		}
 		
 		public void setHighShelf(boolean highShelf) {
 			_highShelf = highShelf;
 			if (highShelf)
 				_lowShelf = false;
 			recalculate();
 		}
 		
 		private void recalculate() {
 			for (int i = 0; i < 128; ++i) {
 				if ((_lowShelf && i < _frequency) || (_highShelf && i > _frequency))
 					_levels[i] = _gain;
 				else
 					_levels[i] = _gain / (Math.pow((_quality/10.0)*(i-_frequency), 2.0) + 1);
 			}
 		}
 		
 		private Band copy() {
 			final Band result = new Band(_frequency, _gain, _quality);
 			if (isHighShelf())
 				result.setHighShelf(true);
 			if (isLowShelf())
 				result.setLowShelf(true);
 			return result;
 		}
 	}
 	
 	private final List<Band> _bands;
 	
 	private double[] _fineLevels;
 	private int[] _levels;
 	
 	public ParametricEQ(List<Band> bands) {
 		_bands = bands;
 		
 		_fineLevels = new double[128];
 		_levels = new int[128];
 		update();
 	}
 	
 	public List<Band> getBands() {
 		final List<Band> result = new ArrayList<>(_bands.size());
 		for (final Band band : _bands) {
 			result.add(band.copy());
 		}
 		return result;
 	}
 	
 	public void update() {
 		Arrays.fill(_fineLevels, 0.0);
 		
 		for (final Band band : _bands) {
 			for (int i = 0; i < 128; ++i)
 				_fineLevels[i] += band._levels[i];
 		}
 		
 		for (int i = 0; i < 128; ++i) {
 			_levels[i] = (int) Math.round(_fineLevels[i]);
 		}
 	}
 	
 	public double[] getFineLevels() {
 		return Arrays.copyOf(_fineLevels, 128);
 	}
 
 	@Override
 	public int process(int midiNumber, int velocity) {
 		return velocity + _levels[midiNumber];
 	}
 	
 	@Override
 	public Plugin copy() {
 		return new ParametricEQ(getBands());
 	}
 	
 	@Override
 	public PluginView createView() {
 		return new ParametricEQView(this);
 	}
 	
 	public GraphicEQ convertToGraphicEQ() {
 		return new GraphicEQ(Arrays.copyOf(_levels, 128));
 	}
 	
 	@Override
 	public String toString() {
 		return "PEQ (" + Utils.countItems(_bands, "band") + ")";
 	}
 }
