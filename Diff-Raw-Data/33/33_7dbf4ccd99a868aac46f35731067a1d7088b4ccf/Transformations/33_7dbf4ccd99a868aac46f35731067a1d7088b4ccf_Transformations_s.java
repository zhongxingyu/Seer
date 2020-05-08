 package transformation;
 
 import petrinetze.IPetrinet;
 
 /**
  * 
  * @author Philipp Khn
  *
  */
 public final class Transformations 
 {
 	private Transformations()
 	{
 		
 	}
 	
 	/**
 	 * Will join both petrinets, using the given transformation.
 	 * The right petrinet will be modified and will be the resulting net.
 	 * @param left the left petrinet.
 	 * @param right the right petrinet.
 	 * @param transformation the transformation to use.
 	 */
 	public static void join(IPetrinet left, IPetrinet right, ITransformation transformation)
 	{
 		left.addNet(right);
 		transformation.transform();
 	}
 
 	/**
 	 * Will join both petrinets, using the given morphism and rule.
 	 * The right petrinet will be modified and will be the resulting net.
 	 * @param left the left petrinet.
 	 * @param right the right petrinet.
 	 * @param morphism the morphism to use.
 	 * @param rule the rule to use.
 	 */
 	public static void join(IPetrinet left, IPetrinet right, IMorphism morphism, IRule rule)
 	{
 		left.addNet(right);
 		new Transformation(left, morphism, rule).transform();
 	}
 
 	/**
 	 * Will join both petrinets, using the given rule and a random morphism.
 	 * The right petrinet will be modified and will be the resulting net.
 	 * @param left the left petrinet.
 	 * @param right the right petrinet.
 	 * @param rule the rule to use.
 	 */
 	public static void join(IPetrinet left, IPetrinet right, IRule rule)
 	{
 		left.addNet(right);
 		new Transformation(left, rule).transform();
 	}
 	
 	/**
 	 * Will transform the given petrinet using the given rule and a random morphism.
 	 * @param net the petrinet.
 	 * @param rule the rule to use.
 	 */
 	public static void transform(IPetrinet net, IRule rule)
 	{
 		new Transformation(net, rule).transform();
 	}
 
 }
