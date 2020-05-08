 package jworldgen.generator.worldStructure.modifiers;
 
 import java.util.ArrayList;
 import java.util.Hashtable;
 
 import jworldgen.generator.VariableResolver;
 import jworldgen.parser.parseStructure.ParseAssignment;
 
 public class WeightedPerlinModifier extends PerlinModifier{
 	
 	public WeightedPerlinModifier(Hashtable<Integer, Integer> typeIDs, String identifier, ArrayList<ParseAssignment> assignments) 
 	{
 		super(new Hashtable<Integer, Integer>(), typeIDs, identifier, assignments);
 		this.type = ModifierType.WEIGHTED_PERLIN;
 	}
 
 	@Override
 	public int getValue(int x, int y, int z) {
 		VariableResolver resolver = new VariableResolver();
 		if (assignments != null)
 		{
 			for (ParseAssignment assignment : assignments)
 			{
 				assignment.evaluate(rng, resolver);
 			}
 		}
 		double noiseValue = perlin.noise(x, y, z, (Integer) resolver.getVariable("scale"));
		int typeCount = typeIDs.size();
		for (int i = 0; i < typeCount-1; i++)
 		{
			float percentage = (i/(float) typeCount)+2*(((float) y - minY)/(maxY-minY))/typeCount;
 			if (noiseValue < percentage)
 				return typeIDs.get(i+1);
 		}
		return typeIDs.get(typeCount);
 	}
 	
 	@Override
 	public Modifier clone() {
 		return new WeightedPerlinModifier(typeIDs, identifier, assignments);
 	}
 }
