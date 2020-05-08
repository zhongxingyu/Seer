 package ecologylab.appframework.types.prefs;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.ArrayList;
 
 import javax.swing.Timer;
 
 import ecologylab.collections.Scope;
 import ecologylab.services.logging.MixedInitiativeOp;
 import ecologylab.xml.ElementState;
 import ecologylab.xml.TranslationScope;
 import ecologylab.xml.XMLTranslationException;
 import ecologylab.xml.xml_inherit;
 
 @xml_inherit
 public class PrefDelayedOp<O extends MixedInitiativeOp> extends PrefOp<O> implements ActionListener
 {
 	ArrayList<ElementState> nestedOps;
 	/**
 	 * delay in seconds
 	 */
 	@xml_attribute	int			delay;
 	@xml_attribute 	boolean repeat 				= false;
 	@xml_attribute	int 		initialDelay 	= 0;
 	Timer timer;
 	public PrefDelayedOp()
 	{
 		super();
 	}
 	
 	public PrefDelayedOp(String name, int delay)
 	{
 		this();
 		this.name 		= name;
 		this.delay 		= delay;
 	}
 	
 	public PrefDelayedOp(String name, int delay, boolean repeat, int initialDelay, ArrayList<ElementState> set)
 	{
 		this(name, delay);
 		this.repeat = repeat;
 		this.initialDelay = initialDelay;
 		this.nestedOps = set;
 	}
 	@Override
 	public void postLoadHook(Scope scope)
 	{
 		
 		for (int i=0; i < nestedOps.size(); i++)
 			((PreferenceOp) nestedOps.get(i)).setScope(scope);
 		
 		debug("delayed op: " + op.action() + " initialized with delay: " + delay + " seconds");
 		timer = new Timer(delay * 1000, this);
 		timer.setInitialDelay(delay * 1000);
 		timer.start();
 	}
 
 	public void actionPerformed(ActionEvent arg0)
 	{
 		
 		for (int i=0; i < nestedOps.size(); i++)
 		{
 			PreferenceOp op = (PreferenceOp) nestedOps.get(i);
 			if(op == null)
 				continue;
 			debug("Performing delayed op: " + op.action());
 			op.performAction(false);		
 		}
 		if(!repeat)
 			timer.stop();
 	}
 	@Override
 	public String toString()
 	{
 		return "PrefDelayedOp: " + name;
 	}
 	
 	/**
 	 * See Pref.clone for why this is important.
 	 * @see ecologylab.appframework.types.prefs.Pref#clone()
 	 */
 	@Override
 	public PrefDelayedOp<O> clone()
 	{
 		PrefDelayedOp<O> prefDelayedOp = new PrefDelayedOp(name, delay, repeat, initialDelay, nestedOps);
 		return prefDelayedOp;
 	}
 }
