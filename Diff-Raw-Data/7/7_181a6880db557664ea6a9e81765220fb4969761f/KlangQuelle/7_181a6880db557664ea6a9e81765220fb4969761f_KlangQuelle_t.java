 import com.jsyn.unitgen.UnitOscillator;
 
 
 public class KlangQuelle extends UnitOscillator {
 
	private double sampleCount = 0;
 	
 	@Override
 	public void generate(int start, int limit) {
 		
 		double[] outputs = output.getValues();
 		System.out.println("start: " + start + "  limit: " + limit );
 		for( int i = start; i < limit; i++)
 		{
 			
 
 				// test
				outputs[i] = Math.sin( sampleCount/100.0 * Math.sin(sampleCount/1000)  ) * 10;
				sampleCount++;
 
 			
 		}
 
 	}
 
 }
