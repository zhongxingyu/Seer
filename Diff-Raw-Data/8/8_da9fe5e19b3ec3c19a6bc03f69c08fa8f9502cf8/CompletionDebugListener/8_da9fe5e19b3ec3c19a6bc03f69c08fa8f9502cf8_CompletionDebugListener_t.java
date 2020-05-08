 package sk.stuba.fiit.perconik.debug.listeners;
 
 import org.eclipse.jface.text.contentassist.ContentAssistEvent;
 import org.eclipse.jface.text.contentassist.ICompletionProposal;
 import sk.stuba.fiit.perconik.core.listeners.CompletionListener;
 import sk.stuba.fiit.perconik.debug.AbstractDebugListener;
 import sk.stuba.fiit.perconik.debug.Debug;
 import sk.stuba.fiit.perconik.eclipse.core.runtime.PluginConsole;
 
 public final class CompletionDebugListener extends AbstractDebugListener implements CompletionListener
 {
 	public CompletionDebugListener()
 	{
 	}
 	
 	public CompletionDebugListener(final PluginConsole console)
 	{
 		super(console);
 	}
 	
 	public final void assistSessionStarted(final ContentAssistEvent event)
 	{
		this.printHeader("Content assist session started");
 		this.printContentAssistEvent(event);
 	}
 
 	public final void assistSessionRestarted(final ContentAssistEvent event)
 	{
		this.printHeader("Content assist session ");
 		this.printContentAssistEvent(event);
 	}
 
 	public final void assistSessionEnded(final ContentAssistEvent event)
 	{
		this.printHeader("Content assist session ");
 		this.printContentAssistEvent(event);
 	}
 
 	public final void applied(final ICompletionProposal proposal)
 	{
 		this.printHeader("Completion proposal applied");
 		this.printCompletionProposal(proposal);
 	}
 
 	public final void selectionChanged(final ICompletionProposal proposal, final boolean smart)
 	{
 		this.printHeader("Completion proposal selection changed");
 		this.printCompletionProposal(proposal);
 		this.printLine("smart", smart);
 	}
 	
 	private final void printContentAssistEvent(final ContentAssistEvent event)
 	{
 		this.put(Debug.dumpContentAssistEvent(event));
 	}
 	
 	private final void printCompletionProposal(final ICompletionProposal proposal)
 	{
 		this.put(Debug.dumpCompletionProposal(proposal));
 	}
 }
