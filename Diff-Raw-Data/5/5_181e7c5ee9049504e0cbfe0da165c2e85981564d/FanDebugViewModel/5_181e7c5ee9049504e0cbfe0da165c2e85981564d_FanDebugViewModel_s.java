 /*
  * Thibaut Colar Aug 27, 2009
  */
 
 package net.colar.netbeans.fan.debugger;
 
 import net.colar.netbeans.fan.FanUtilities;
 import org.netbeans.api.debugger.jpda.JPDABreakpoint;
 import org.netbeans.api.debugger.jpda.LineBreakpoint;
 import org.netbeans.spi.viewmodel.ModelListener;
 import org.netbeans.spi.viewmodel.NodeModel;
 import org.netbeans.spi.viewmodel.UnknownTypeException;
 
 /**
  * //TODO: test/ fix / use FanDebugViewModel
  *
 * Doesn't always work even when called when regsitered in META-INF ????
  *
  * Debugger breakpoint view pane impl.
  * @author thibautc
  */
 public class FanDebugViewModel implements NodeModel
 {
 	public static final String ICON_FAN="net/colar/netbeans/fan/project/resources/fanFile.png";
 	public static final String ICON_FWT="net/colar/netbeans/fan/project/resources/fanFwt.png";
 	public static final String ICON_JAVA="net/colar/netbeans/fan/project/resources/java.png";
 
	static{FanUtilities.GENERIC_LOGGER.info("Fantom - Init "+FanDebugViewModel.class);}
 
 	public String getDisplayName(Object node) throws UnknownTypeException
 	{
 		if(node instanceof LineBreakpoint)
 		{
 			LineBreakpoint lbp=(LineBreakpoint)node;
 			return lbp.getPrintText()+" : "+lbp.getLineNumber();
 		}
 		if(node instanceof JPDABreakpoint)
 		{
 			return ((JPDABreakpoint)node).getPrintText();
 		}
 		throw new UnknownTypeException(getClass().getName()+": Unknown type: "+node.getClass().getName());
 	}
 
 	public String getIconBase(Object node) throws UnknownTypeException
 	{
 		if(node instanceof LineBreakpoint)
 		{
 			LineBreakpoint lbp=(LineBreakpoint)node;
 			String url=lbp.getURL();
 			if(url.endsWith(".fan"))
 				return ICON_FAN;
 			else if(url.endsWith(".fwt"))
 				return ICON_FWT;
 			else if(url.endsWith(".java"))
 				return ICON_JAVA;
 		}
 		throw new UnknownTypeException(getClass().getName()+": Unknown type: "+node.getClass().getName());
 	}
 
 	public String getShortDescription(Object node) throws UnknownTypeException
 	{
 		return getDisplayName(node);
 	}
 
 	public void addModelListener(ModelListener l)
 	{
 	}
 
 	public void removeModelListener(ModelListener l)
 	{
 	}
 
 }
