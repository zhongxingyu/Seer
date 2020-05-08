 package edu.mayo.cts2.framework.plugin.service.valueSetDefinitionResolutionServices.ctsUtility;
 
 import java.util.Collection;
 import java.util.Iterator;
 import edu.mayo.cts2.framework.model.core.types.SetOperator;
 
 /**
  * Just some set operations methods that are more efficient than the ones in typical libraries 
  * (because these destroy one of the incoming sets, while library implementations typically dont)
  * 
  * @author darmbrust
  *
  * @param <T>
  */
 
 public class SetUtilities<T>
 {
 	public void handleSet(SetOperator operator, Collection<T> main, Collection<T> newItems)
 	{
 		switch (operator)
 		{
 			case INTERSECT:
 			{
 				destructiveIntersect(main, newItems);
 				break;
 			}
 			case SUBTRACT:
 			{
 				destructiveSubtract(main, newItems);
 				break;
 			}
 			case UNION:
 			{
 				main.addAll(newItems);
 				break;
 			}
 			default:
 			{
 				throw new RuntimeException("Unexpected set operation!");
 			}
 		}
 	}
 
 	/**
 	 * Intersect main with the values from newItems. newItems will be destroyed.
 	 */
 	public void destructiveIntersect(Collection<T> main, Collection<T> newItems)
 	{
 		Iterator<T> iterator = main.iterator();
 		while (iterator.hasNext())
 		{
 			Object o = iterator.next();
 			if (!newItems.remove(o))
 			{
 				iterator.remove();
 			}
 		}
 	}
 
 	/**
 	 * Subtract newItems from main. newItems will be destroyed.
 	 */
 	public void destructiveSubtract(Collection<T> main, Collection<T> newItems)
 	{
 		Iterator<T> iterator = main.iterator();
 		while (iterator.hasNext())
 		{
 			Object o = iterator.next();
 			if (newItems.remove(o))
 			{
 				iterator.remove();
 			}
 		}
 	}
 }
