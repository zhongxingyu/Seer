 package sk.stuba.fiit.perconik.core.java;
 
 import java.util.LinkedList;
 import javax.annotation.Nullable;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.jdt.core.IJavaElement;
 import org.eclipse.jdt.core.JavaModelException;
 import com.google.common.collect.Lists;
 
 public final class JavaElements
 {
 	private JavaElements()
 	{
 		throw new AssertionError();
 	}
 	
 	public static final IJavaElement parent(@Nullable final IJavaElement element)
 	{
 		return element != null ? element.getParent() : null;
 	}
 	
 	public static final LinkedList<IJavaElement> ancestors(@Nullable IJavaElement element)
 	{
 		final LinkedList<IJavaElement> ancestors = Lists.newLinkedList();
 		
 		while (element != null)
 		{
 			ancestors.add(element = element.getParent());
 		}
 		
 		return ancestors;
 	}
 	
 	public static final LinkedList<IJavaElement> upToRoot(@Nullable IJavaElement element)
 	{
 		LinkedList<IJavaElement> branch = Lists.newLinkedList();
 		
 		if (element != null)
 		{
 			do
 			{
 				branch.add(element);
 			}
 			while ((element = element.getParent()) != null);
 		}
 		
 		return branch;
 	}
 	
 	public static final IResource resource(@Nullable final IJavaElement element)
 	{
 		return element != null ? element.getResource() : null;
 	}
 
 	public static final IResource correspondingResource(@Nullable final IJavaElement element)
 	{
 		try
 		{
			return element != null ? element.getCorrespondingResource() : null;
 		}
 		catch (JavaModelException e)
 		{
 			return JavaExceptions.handle(e);
 		}
 	}
 	
 	public static final IResource underlyingResource(@Nullable final IJavaElement element)
 	{
 		try
 		{
			return element != null ? element.getUnderlyingResource() : null;
 		}
 		catch (JavaModelException e)
 		{
 			return JavaExceptions.handle(e);
 		}
 	}
 }
