 package complexion.common;
 
 public class Utils {
 	@SuppressWarnings("rawtypes")
 	public static  final Class[] toClass(Object[] args)
 	{
 		Class[] value = new Class[args.length];
 		int iter = 0;
 		for(Object A : args)
 		{
 			value[iter] = A.getClass();
 		}
 		return value;
 	}
 }
