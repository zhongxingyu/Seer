 package sk.stuba.fiit.perconik.debug;
 
 import java.io.File;
 import java.io.IOException;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.Arrays;
 import java.util.Date;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Set;
 import javax.annotation.Nullable;
 import org.eclipse.core.commands.Category;
 import org.eclipse.core.commands.Command;
 import org.eclipse.core.commands.CommandEvent;
 import org.eclipse.core.commands.CommandManagerEvent;
 import org.eclipse.core.commands.ExecutionEvent;
 import org.eclipse.core.commands.IParameter;
 import org.eclipse.core.commands.ParameterType;
 import org.eclipse.core.commands.ParameterValuesException;
 import org.eclipse.core.commands.State;
 import org.eclipse.core.commands.common.NotDefinedException;
 import org.eclipse.core.commands.operations.IUndoableOperation;
 import org.eclipse.core.commands.operations.OperationHistoryEvent;
 import org.eclipse.core.filebuffers.IFileBuffer;
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.IResourceChangeEvent;
 import org.eclipse.core.resources.IResourceDelta;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.debug.core.DebugEvent;
 import org.eclipse.debug.core.ILaunch;
 import org.eclipse.debug.core.ILaunchConfiguration;
 import org.eclipse.debug.core.ILaunchConfigurationType;
 import org.eclipse.jdt.core.ElementChangedEvent;
 import org.eclipse.jdt.core.IJavaElement;
 import org.eclipse.jdt.core.IJavaElementDelta;
 import org.eclipse.jdt.core.IJavaProject;
 import org.eclipse.jdt.core.dom.CompilationUnit;
 import org.eclipse.jdt.junit.model.ITestCaseElement;
 import org.eclipse.jdt.junit.model.ITestElement;
 import org.eclipse.jdt.junit.model.ITestRunSession;
 import org.eclipse.jface.text.DocumentEvent;
 import org.eclipse.jface.text.IDocument;
 import org.eclipse.jface.text.IMarkSelection;
 import org.eclipse.jface.text.ITextSelection;
 import org.eclipse.jface.text.contentassist.ContentAssistEvent;
 import org.eclipse.jface.text.contentassist.ICompletionProposal;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jgit.events.ConfigChangedEvent;
 import org.eclipse.jgit.events.IndexChangedEvent;
 import org.eclipse.jgit.events.RefsChangedEvent;
 import org.eclipse.jgit.events.RepositoryEvent;
 import org.eclipse.jgit.events.RepositoryListener;
 import org.eclipse.jgit.lib.Repository;
 import org.eclipse.jgit.lib.RepositoryState;
 import org.eclipse.ltk.core.refactoring.RefactoringDescriptorProxy;
 import org.eclipse.ltk.core.refactoring.history.RefactoringExecutionEvent;
 import org.eclipse.ltk.core.refactoring.history.RefactoringHistoryEvent;
 import org.eclipse.ui.IEditorPart;
 import org.eclipse.ui.IEditorReference;
 import org.eclipse.ui.IPerspectiveDescriptor;
 import org.eclipse.ui.IWorkbench;
 import org.eclipse.ui.IWorkbenchPage;
 import org.eclipse.ui.IWorkbenchPart;
 import org.eclipse.ui.IWorkbenchPartReference;
 import org.eclipse.ui.IWorkbenchWindow;
 import sk.stuba.fiit.perconik.debug.plugin.Activator;
 import sk.stuba.fiit.perconik.debug.runtime.DebugConsole;
 import sk.stuba.fiit.perconik.eclipse.core.commands.operations.OperationHistoryEventType;
 import sk.stuba.fiit.perconik.eclipse.core.resources.ProjectBuildKind;
 import sk.stuba.fiit.perconik.eclipse.core.resources.ResourceDeltaFlag;
 import sk.stuba.fiit.perconik.eclipse.core.resources.ResourceDeltaKind;
 import sk.stuba.fiit.perconik.eclipse.core.resources.ResourceEventType;
 import sk.stuba.fiit.perconik.eclipse.core.resources.ResourceType;
 import sk.stuba.fiit.perconik.eclipse.core.runtime.StatusSeverity;
 import sk.stuba.fiit.perconik.eclipse.debug.core.DebugEventDetail;
 import sk.stuba.fiit.perconik.eclipse.debug.core.DebugEventKind;
 import sk.stuba.fiit.perconik.eclipse.jdt.core.JavaElementDeltaFlag;
 import sk.stuba.fiit.perconik.eclipse.jdt.core.JavaElementDeltaKind;
 import sk.stuba.fiit.perconik.eclipse.jdt.core.JavaElementEventType;
 import sk.stuba.fiit.perconik.eclipse.jdt.core.JavaElementType;
 import sk.stuba.fiit.perconik.eclipse.jdt.core.dom.AstNodeFlag;
 import sk.stuba.fiit.perconik.eclipse.jdt.core.dom.AstNodeType;
 import sk.stuba.fiit.perconik.eclipse.ltk.core.refactoring.history.RefactoringExecutionEventType;
 import sk.stuba.fiit.perconik.eclipse.ltk.core.refactoring.history.RefactoringHistoryEventType;
 import sk.stuba.fiit.perconik.utilities.SmartStringBuilder;
 
 public final class Debug
 {
 	private Debug()
 	{
 		throw new AssertionError();
 	}
 	
 	private static final class ConsoleHolder
 	{
 		static final DebugConsole console = DebugConsole.of(Activator.getDefault().getConsole());
 		
 		private ConsoleHolder()
 		{
 			throw new AssertionError();
 		}
 	}
 	
 	public static final DebugConsole getDefaultConsole()
 	{
 		return ConsoleHolder.console;
 	}
 	
 	private static final DebugConsole console()
 	{
 		return getDefaultConsole();
 	}
 
 	public static final void tab()
 	{
 		console().tab();
 	}
 	
 	public static final void untab()
 	{
 		console().untab();
 	}
 	
 	public static final void put(@Nullable final String message)
 	{
 		console().put(message);
 	}
 	
 	public static final void put(final String format, final Object ... args)
 	{
 		console().put(format, args);
 	}
 	
 	public static final void print(@Nullable final String message)
 	{
 		console().print(message);
 	}
 	
 	public static final void print(final String format, final Object ... args)
 	{
 		console().print(format, args);
 	}
 
 	public static final void notice(final String message)
 	{
 		console().notice(message);
 	}
 	
 	public static final void notice(final String format, Object ... args)
 	{
 		console().notice(format, args);
 	}
 	
 	public static final void warning(final String message)
 	{
 		console().warning(message);
 	}
 
 	public static final void warning(final String format, Object ... args)
 	{
 		console().warning(format, args);
 	}
 
 	public static final void error(final String message, final Throwable failure)
 	{
 		console().error(message, failure);
 	}
 	
 	private static final SmartStringBuilder builder()
 	{
 		return new SmartStringBuilder().tab();
 	}
 	
 	private static final String missing()
 	{
 		return builder().appendln("missing").toString();
 	}
 	
 	public static final String dumpHeader(final String title)
 	{
 		SmartStringBuilder builder = new SmartStringBuilder();
 		
 		builder.appendln().appendln(dumpTime()).appendln();
 		builder.format("%s:", title).appendln();
 		
 		return builder.toString();
 	}
 	
 	public static final String dumpClass(final Class<?> type)
 	{
 		String name = type.getCanonicalName();
 		
 		if (name != null)
 		{
 			return name;
 		}
 		
 		return type.getName();
 	}
 	
 	public static final String dumpBlock(final Object key, @Nullable final Object value)
 	{
 		SmartStringBuilder builder = builder();
 	
 		return builder.append(key).appendln(':').lines(value).toString();
 	}
 
 	public static final String dumpLine(final Object key, @Nullable final Object value)
 	{
 		SmartStringBuilder builder = builder();
 	
 		return builder.append(key).append(": ").appendln(value).toString();
 	}
 	
 	public static final String dumpTime()
 	{
 		return dumpTime(new Date());
 	}
 
 	public static final String dumpTime(final Date date)
 	{
 		return TimeUtilities.format(date);
 	}
 
 	private static final class TimeUtilities
 	{
 		private static final DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
 		
 		static synchronized final String format(final Date date)
 		{
 			return formatter.format(date);
 		}
 	}
 	
 	public static final String dumpCategory(final Category category) throws NotDefinedException
 	{
 		SmartStringBuilder builder = builder();
 		
 		Class<?> type = category.getClass();
 		String   id   = category.getId();
 		
 		String   name        = null;
 		String   description = null;
 		
 		boolean defined = category.isDefined();
 		
 		if (defined)
 		{
 			name        = category.getName();
 			description = category.getDescription();
 		}
 
 		builder.append("class: ").appendln(type);
 		builder.append("identifier: ").appendln(id);
 		
 		builder.append("name: ").appendln(name);
 		builder.append("description: ").appendln(description);
 		
 		builder.append("defined: ").appendln(defined);
 	
 		return builder.toString();
 	}
 	
 	public static final String dumpCommand(final Command command) throws NotDefinedException, ParameterValuesException
 	{
 		SmartStringBuilder builder = builder();
 		
 		Class<?> type = command.getClass();
 		String   id   = command.getId();
 		
 		String name        = null;
 		String description = null;
 		
 		Category category = null;
 		
 		IParameter[]  parameters = null;
 		ParameterType returnType = null;
 
 		String[] stateIds = command.getStateIds();
 		
 		boolean defined = command.isDefined();
 		boolean enabled = command.isEnabled();
 		boolean handled = command.isHandled();
 
 		if (defined)
 		{
 			name        = command.getName();
 			description = command.getDescription();
 			
 			category = command.getCategory();
 			
 			parameters = command.getParameters();
 			returnType = command.getReturnType();
 		}
 		
 		builder.append("class: ").appendln(dumpClass(type));
 		builder.append("identifier: ").appendln(id);
 		
 		builder.append("name: ").appendln(name);
 		builder.append("description: ").appendln(description);
 		
 		builder.appendln("category:").lines(category == null ? missing() : dumpCategory(category));
 
 		if (parameters == null)
 		{
 			parameters = new IParameter[0];
 		}
 		
 		builder.appendln("parameters:").lines(dumpParameters(parameters));
 		builder.append("return type: ").appendln(returnType);
 
 		builder.appendln("states:").tab();
 
 		if (stateIds.length != 0)
 		{
 			for (String stateId: stateIds)
 			{
 				State state = command.getState(stateId);
 				
 				builder.append(state.getId()).append(": ").appendln(state.getValue());
 			}
 		}
 		else
 		{
 			builder.appendln("none");
 		}
 		
 		builder.untab();
 		
 		builder.append("defined: ").appendln(defined);
 		builder.append("enabled: ").appendln(enabled);
 		builder.append("handled: ").appendln(handled);
 		
 		return builder.toString();
 	}
 	
 	public static final String dumpCommandEvent(final CommandEvent event) throws NotDefinedException, ParameterValuesException
 	{
 		SmartStringBuilder builder = builder();
 		
 		Command command = event.getCommand();
 		
 		boolean nameChanged        = event.isNameChanged();
 		boolean categoryChanged    = event.isCategoryChanged();
 		boolean descriptionChanged = event.isDescriptionChanged();
 
 		boolean definedChanged = event.isDefinedChanged();
 		boolean enabledChanged = event.isEnabledChanged();
 		boolean handledChanged = event.isHandledChanged();
 		
 		boolean parametersChanged = event.isParametersChanged();
 		boolean returnTypeChanged = event.isReturnTypeChanged();
 
 		boolean helpContextIdChanged = event.isHelpContextIdChanged();
 
 		builder.appendln("command:").lines(dumpCommand(command));
 		
 		builder.append("name changed: ").appendln(nameChanged);
 		builder.append("category changed: ").appendln(categoryChanged);
 		builder.append("description changed: ").appendln(descriptionChanged);
 		
 		builder.append("defined changed: ").appendln(definedChanged);
 		builder.append("enabled changed: ").appendln(enabledChanged);
 		builder.append("handled changed: ").appendln(handledChanged);
 		
 		builder.append("parameters changed: ").appendln(parametersChanged);
 		builder.append("return type changed: ").appendln(returnTypeChanged);
 		
 		builder.append("help context identifier changed: ").appendln(helpContextIdChanged);
 		
 		return builder.toString();
 	}
 	
 	public static final String dumpCommandManagerEvent(final CommandManagerEvent event)
 	{
 		SmartStringBuilder builder = builder();
 		
 		String  commandId      = event.getCommandId();
 		boolean commandDefined = event.isCommandDefined();
 		boolean commandChanged = event.isCommandChanged();
 		
 		String  categoryId      = event.getCategoryId();
 		boolean categoryDefined = event.isCategoryDefined();
 		boolean categoryChanged = event.isCategoryChanged();
 
 		String  parameterTypeId      = event.getParameterTypeId();
 		boolean parameterTypeDefined = event.isParameterTypeDefined();
 		boolean parameterTypeChanged = event.isParameterTypeChanged();
 		
 		builder.appendln("command:").tab();
 		
 		builder.append("identifier: ").appendln(commandId);
 		builder.append("defined: ").appendln(commandDefined);
 		builder.append("changed: ").appendln(commandChanged);
 		
 		builder.untab().appendln("category:").tab();
 		
 		builder.append("identifier: ").appendln(categoryId);
 		builder.append("defined: ").appendln(categoryDefined);
 		builder.append("changed: ").appendln(categoryChanged);
 		
 		builder.untab().appendln("parameter type:").tab();
 		
 		builder.append("identifier: ").appendln(parameterTypeId);
 		builder.append("defined: ").appendln(parameterTypeDefined);
 		builder.append("changed: ").appendln(parameterTypeChanged);
 		
 		return builder.toString();
 	}
 	
 	public static final String dumpCompliationUnit(final CompilationUnit unit)
 	{
 		SmartStringBuilder builder = builder();
 	
 		AstNodeType type = AstNodeType.valueOf(unit.getNodeType());
 		
 		Set<AstNodeFlag> flags = AstNodeFlag.setOf(unit.getFlags());
 		
 		int startPosition = unit.getStartPosition();
 		int length        = unit.getLength();
 		
 		builder.format("type: %s (%d)", type, type.getValue()).appendln();
 		
 		builder.append("flags: ").list(flags.isEmpty() ? Arrays.asList("none") : flags).appendln();
 		
 		builder.append("start position: ").appendln(startPosition);
 		builder.append("length: ").appendln(length);
 		
 		return builder.toString();
 	}
 
 	public static final String dumpCompletionProposal(final ICompletionProposal proposal)
 	{
 		SmartStringBuilder builder = builder();
 
 		String displayString  = proposal.getDisplayString();
 		String additionalInfo = proposal.getAdditionalProposalInfo();
 		
 		builder.append("display string: ").appendln(displayString);
 		builder.append("additional info: ").append(additionalInfo.length()).appendln(" characters");
 		
 		return builder.toString();
 	}
 
 	public static final String dumpContentAssistEvent(final ContentAssistEvent event)
 	{
 		SmartStringBuilder builder = builder();
 
 		boolean autoActivated = event.isAutoActivated;
 		
 		builder.append("auto activated: ").appendln(autoActivated);
 
 		return builder.toString();
 	}
 	
 	public static final String dumpDebugEvent(final DebugEvent event)
 	{
 		SmartStringBuilder builder = builder();
 		
 		Object data = event.getData();
 
 		Set<DebugEventKind>   kinds   = DebugEventKind.setOf(event.getKind());
 		Set<DebugEventDetail> details = DebugEventDetail.setOf(event.getDetail());
 
 		boolean evaluation = event.isEvaluation();
 		boolean stepStart  = event.isStepStart();
 	
 		builder.append("data: ").appendln(data);
 
 		builder.append("kinds: ").list(kinds.isEmpty() ? Arrays.asList("none") : kinds).appendln();
 		builder.append("details: ").list(details.isEmpty() ? Arrays.asList("none") : details).appendln();
 		
 		builder.append("evaluation: ").appendln(evaluation);
 		builder.append("step start: ").appendln(stepStart);
 		
 		return builder.toString();
 	}
 
 	public static final String dumpDebugEvents(final DebugEvent[] events)
 	{
 		SmartStringBuilder builder = builder();
 		
 		if (events.length != 0)
 		{
 			for (int i = 0; i < events.length; i ++)
 			{
 				builder.format("event %d:", i);
 				builder.lines(dumpDebugEvent(events[i]));
 			}
 		}
 		else
 		{
 			builder.appendln("none");
 		}
 		
 		return builder.toString();
 	}
 
 	public static final String dumpDocument(final IDocument document)
 	{
 		SmartStringBuilder builder = builder();
 		
 		int length = document.getLength();
 		int lines  = document.getNumberOfLines();
 	
 		builder.append("length: ").appendln(length);
 		builder.append("lines: ").appendln(lines);
 		
 		return builder.toString();
 	}
 	
 	public static final String dumpDocumentEvent(final DocumentEvent event)
 	{
 		SmartStringBuilder builder = builder();
 		
 		IDocument document = event.getDocument();
 		
 		int offset = event.getOffset();
 		int length = event.getLength();
 
 		String text = event.getText();
 
 		long modificationStamp = event.getModificationStamp();
 		
 		builder.appendln("document:").lines(dumpDocument(document));
 		
 		builder.append("offset: ").appendln(offset);
 		builder.append("length: ").appendln(length);
 
 		builder.append("text: \"").append(text).appendln("\"");
 		
 		builder.append("modification stamp: ").appendln(modificationStamp < 0 ? "unknown" : modificationStamp);
 		
 		return builder.toString();
 	}
 	
 	public static final String dumpEditor(final IEditorPart part)
 	{
 		return dumpPart(part);
 	}
 	
 	public static final String dumpEditorReference(final IEditorReference reference)
 	{
 		return dumpPartReference(reference);
 	}
 	
 	public static final String dumpExecutionEvent(final ExecutionEvent event) throws NotDefinedException, ParameterValuesException
 	{
 		SmartStringBuilder builder = builder();
 
 		Command command = event.getCommand();
 		
 		Map<?, ?> parameters = event.getParameters();
 		
 		builder.appendln("command:").lines(dumpCommand(command));
 		builder.appendln("parameters:").tab();
 		
 		if (!parameters.isEmpty())
 		{
 			for (Entry<?, ?> entry: parameters.entrySet())
 			{
 				builder.append(entry.getKey()).append(": ").appendln(entry.getValue());
 			}
 		}
 		else
 		{
 			builder.appendln("none");
 		}
 		
 		return builder.toString();
 	}
 
 	public static final String dumpFileBuffer(final IFileBuffer buffer)
 	{
 		SmartStringBuilder builder = builder();
 		
 		IPath   location = buffer.getLocation();
 		IStatus status   = buffer.getStatus();
 		
 		long modificationStamp = buffer.getModificationStamp();
 		
 		boolean commitable     = buffer.isCommitable();
 		boolean dirty          = buffer.isDirty();
 		boolean shared         = buffer.isShared();
 		boolean stateValidated = buffer.isStateValidated();
 		
 		boolean synchronizationContextRequested = buffer.isSynchronizationContextRequested();
 		boolean synchronizedWithFileSystem      = buffer.isSynchronized();
 
 		builder.append("location: ").appendln(location);
 		
 		if (status != null)
 		{
 			builder.appendln("status:").lines(dumpStatus(status));
 		}
 		
 		builder.append("modification stamp: ").appendln(modificationStamp < 0 ? "unknown" : modificationStamp);
 		
 		builder.append("commitable: ").appendln(commitable);
 		builder.append("dirty: ").appendln(dirty);
 		builder.append("shared: ").appendln(shared);
 		builder.append("state validated: ").appendln(stateValidated);
 		
 		builder.append("synchronization context requested: ").appendln(synchronizationContextRequested);
 		builder.append("synchronized with file system: ").appendln(synchronizedWithFileSystem);
 		
 		return builder.toString();
 	}
 
 	public static final String dumpGitConfigurationEvent(final ConfigChangedEvent event)
 	{
 		return dumpGitRepositoryEvent(event);
 	}
 
 	public static final String dumpGitIndexEvent(final IndexChangedEvent event)
 	{
 		return dumpGitRepositoryEvent(event);
 	}
 
 	public static final String dumpGitReferenceEvent(final RefsChangedEvent event)
 	{
 		return dumpGitRepositoryEvent(event);
 	}
 
 	public static final String dumpGitRepository(final Repository repository)
 	{
 		SmartStringBuilder builder = builder();
 
 		File directory = repository.getDirectory();
 		File indexFile = repository.getIndexFile();
 		File workTree  = repository.getWorkTree();
 		
 		String branch     = null;
 		String fullBranch = null;
 		
 		RepositoryState state = repository.getRepositoryState();
 		
 		boolean bare = repository.isBare();
 		
 		try
 		{
 			branch = repository.getBranch();
 		}
 		catch (IOException e)
 		{
 			branch = "?";
 		}
 		
 		try
 		{
 			fullBranch = repository.getFullBranch();
 		}
 		catch (IOException e)
 		{
 			fullBranch = "?";
 		}
 		
 		builder.append("directory: ").appendln(directory);
 		builder.append("index file: ").appendln(indexFile);
 		builder.append("work tree: ").appendln(workTree);
 		
 		builder.append("branch: ").append(branch).append(" (full ").append(fullBranch).appendln(")");
 		
 		builder.appendln("state:").lines(dumpGitRepositoryState(state));
 		
 		builder.append("bare: ").appendln(bare);
 		
 		return builder.toString();
 	}
 
 	public static final String dumpGitRepositoryEvent(final RepositoryEvent<?> event)
 	{
 		SmartStringBuilder builder = builder();
 
 		Class<? extends RepositoryListener> type = event.getListenerType();
 		
 		Repository repository = event.getRepository();
 		
 		builder.append("listener type: ").appendln(dumpClass(type));
 		builder.appendln("repository:").lines(dumpGitRepository(repository));
 		
 		return builder.toString();
 	}
 	
 	public static final String dumpGitRepositoryState(final RepositoryState state)
 	{
 		SmartStringBuilder builder = builder();
 
 		String value       = state.toString();
 		String description = state.getDescription();
 
 		boolean amend     = state.canAmend();
 		boolean checkout  = state.canCheckout();
 		boolean commit    = state.canCommit();
 		boolean resetHead = state.canResetHead();
 		
 		boolean rebasing  = state.isRebasing();
 		
 		builder.append("value: ").appendln(value);
 		builder.append("description: ").appendln(description);
 		
 		builder.append("can amend: ").appendln(amend);
 		builder.append("can checkout: ").appendln(checkout);
 		builder.append("can commit: ").appendln(commit);
 		builder.append("can reset head: ").appendln(resetHead);
 		
 		builder.append("rebasing: ").appendln(rebasing);
 		
 		return builder.toString();
 	}
 	
 	public static final String dumpJavaElement(final IJavaElement element)
 	{
 		SmartStringBuilder builder = builder();
 	
 		JavaElementType type = JavaElementType.valueOf(element.getElementType());
 	
 		String name = element.getElementName();
 		IPath  path = element.getPath();
 		
 		builder.format("type: %s (%d)", type, type.getValue()).appendln();
 		
 		builder.append("name: ").appendln(name);
 		builder.append("path: ").appendln(path);
 		
 		return builder.toString();
 	}
 
 	public static final String dumpJavaElementChangeEvent(final ElementChangedEvent event)
 	{
 		SmartStringBuilder builder = builder();
 	
 		JavaElementEventType type = JavaElementEventType.valueOf(event.getType());
 	
 		IJavaElementDelta delta = event.getDelta();
 		
 		builder.format("type: %s (%d)", type, type.getValue()).appendln();
 		
		builder.appendln("delta:").lines(dumpJavaElementDelta(delta));
 		
 		return builder.toString();
 	}
 
 	public static final String dumpJavaElementDelta(final IJavaElementDelta delta)
 	{
 		SmartStringBuilder builder = builder();
 	
 		JavaElementDeltaKind      kind  = JavaElementDeltaKind.valueOf(delta.getKind());
 		Set<JavaElementDeltaFlag> flags = JavaElementDeltaFlag.setOf(delta.getFlags());
 	
 		CompilationUnit unit    = delta.getCompilationUnitAST();
 		IJavaElement    element = delta.getElement();
 	
 		builder.append("kind: ").appendln(kind);
 		builder.append("flags: ").list(flags.isEmpty() ? Arrays.asList("none") : flags).appendln();
 	
 		builder.appendln("unit:").lines(unit == null ? missing() : dumpCompliationUnit(unit));
 		builder.appendln("element:").lines(element == null ? missing() : dumpJavaElement(element));
 		
 		return builder.toString();
 	}
 
 	public static final String dumpJavaProject(final IJavaProject project)
 	{
 		SmartStringBuilder builder = builder();
 
 		String name = project.getElementName();
 
 		IPath path = project.getPath();
 
 		builder.append("name: ").appendln(name);
 		builder.append("path: ").appendln(path);
 		
 		return builder.toString();
 	}
 
 	public static final String dumpLaunch(final ILaunch launch) throws CoreException
 	{
 		SmartStringBuilder builder = builder();
 		
 		ILaunchConfiguration configuration = launch.getLaunchConfiguration();
 		
 		String  mode = launch.getLaunchMode();
 		
 		builder.append("mode: ").appendln(mode);
 		builder.appendln("configuration:").lines(dumpLaunchConfiguration(configuration));
 	
 		return builder.toString();
 	}
 
 	public static final String dumpLaunches(final ILaunch[] launches) throws CoreException
 	{
 		SmartStringBuilder builder = builder();
 
 		if (launches.length != 0)
 		{
 			for (int i = 0; i < launches.length; i ++)
 			{
 				builder.format("launch %d:", i);
 				builder.lines(dumpLaunch(launches[i]));
 			}
 		}
 		else
 		{
 			builder.appendln("none");
 		}
 		
 		return builder.toString();
 	}
 	
 	public static final String dumpLaunchConfiguration(final ILaunchConfiguration configuration) throws CoreException
 	{
 		SmartStringBuilder builder = builder();
 		
 		ILaunchConfigurationType type = configuration.getType();
 		IFile file = configuration.getFile();
 		
 		String      name     = configuration.getName();
 		String      category = configuration.getCategory();
 		Set<String> modes    = configuration.getModes();
 		
 		String  application = configuration.getAttribute("application", "?");
 		String  product     = configuration.getAttribute("product", "?");
 		boolean useProduct  = configuration.getAttribute("useProduct", false);
 		
 		builder.append("name: ").appendln(name);
 		builder.append("category: ").appendln(category);
 		builder.appendln("type:").lines(dumpLaunchConfigurationType(type));
 		builder.append("modes: ").list(modes.isEmpty() ? Arrays.asList("none") : modes).appendln();
 		
 		if (file != null)
 		{
 			builder.append("full path: ").appendln(file.getFullPath());
 			builder.append("location: ").appendln(file.getLocation());
 			builder.append("URI: ").appendln(file.getLocationURI());
 		}
 		
 		builder.append("application: ").appendln(application);
 		builder.append("product: ").append(product).append(" (use ").append(useProduct).appendln(")");
 		
 //		Map<Object, Object> attributes = configuration.getAttributes();
 //		
 //		for (Entry<Object, Object> entry: attributes.entrySet())
 //		{
 //			builder.append(entry.getKey()).append(": ").appendln(entry.getValue());
 //		}
 
 		return builder.toString();
 	}
 
 	public static final String dumpLaunchConfigurationType(final ILaunchConfigurationType type)
 	{
 		SmartStringBuilder builder = builder();
 		
 		String name             = type.getName();
 		String category         = type.getCategory();
 		String identifier       = type.getIdentifier();
 		String pluginIdentifier = type.getPluginIdentifier();
 		String contributorName  = type.getContributorName();
 
 		builder.append("name: ").appendln(name);
 		builder.append("category: ").appendln(category);
 		builder.append("identifier: ").appendln(identifier);
 		builder.append("plugin identifier: ").appendln(pluginIdentifier);
 		builder.append("contributor name: ").appendln(contributorName);
 		
 		return builder.toString();
 	}	
 	
 	public static final String dumpMarkSelection(final IMarkSelection selection)
 	{
 		SmartStringBuilder builder = builder();
 	
 		boolean empty = selection.isEmpty();
 		
 		int offset = selection.getOffset();
 		int length = selection.getLength();
 	
 		builder.appendln("category: mark");
 		
 		builder.append("empty: ").appendln(empty);
 		
 		builder.append("offset: ").appendln(offset);
 		builder.append("length: ").appendln(length);
 		
 		return builder.toString();
 	}
 
 	public static final String dumpOperationHistoryEvent(final OperationHistoryEvent event)
 	{
 		SmartStringBuilder builder = builder();
 
 		IUndoableOperation operation = event.getOperation();
 		IStatus            status    = event.getStatus();
 
 		OperationHistoryEventType type = OperationHistoryEventType.valueOf(event.getEventType());
 		
 		builder.format("type: %s (%d)", type, type.getValue()).appendln();
 		
 		builder.appendln("operation:").lines(dumpUndoableOperation(operation));
 		
 		if (status != null)
 		{
 			builder.appendln("status:").lines(dumpStatus(status));
 		}
 		
 		return builder.toString();
 	}
 
 	public static final String dumpPage(final IWorkbenchPage page)
 	{
 		SmartStringBuilder builder = builder();
 		
 		Class<?> type  = page.getClass();
 		String   label = page.getLabel();
 	
 		builder.append("class: ").appendln(dumpClass(type));
 		builder.append("label: ").appendln(label);
 		
 		return builder.toString();
 	}
 
 	public static final String dumpParameter(final IParameter parameter) throws ParameterValuesException
 	{
 		SmartStringBuilder builder = builder();
 		
 		String id   = parameter.getId();
 		String name = parameter.getName();
 		
 		boolean optional = parameter.isOptional();
 		
 		Map<?, ?> values = parameter.getValues().getParameterValues();
 		
 		builder.append("identifier: ").appendln(id);
 		builder.append("name: ").appendln(name);
 		
 		builder.append("optional: ").appendln(optional);
 		
 		builder.appendln("values:").tab();
 
 		if (!values.isEmpty())
 		{
 			for (Entry<?, ?> entry: values.entrySet())
 			{
 				builder.append(entry.getKey()).append(": ").appendln(entry.getValue());
 			}
 		}
 		else
 		{
 			builder.appendln("none");
 		}
 		
 		return builder.toString();
 	}
 
 	public static final String dumpParameters(final IParameter[] parameters) throws ParameterValuesException
 	{
 		SmartStringBuilder builder = builder();
 		
 		if (parameters.length != 0)
 		{
 			for (int i = 0; i < parameters.length; i ++)
 			{
 				builder.format("parameter %d:", i);
 				builder.lines(dumpParameter(parameters[i]));
 			}
 		}
 		else
 		{
 			builder.appendln("none");
 		}
 		
 		return builder.toString();
 	}
 
 	public static final String dumpPart(final IWorkbenchPart part)
 	{
 		SmartStringBuilder builder = builder();
 		
 		Class<?> type    = part.getClass();
 		String   title   = part.getTitle();
 		String   tooltip = part.getTitleToolTip();
 		
 		builder.append("class: ").appendln(dumpClass(type));
 		builder.append("title: ").appendln(title);
 		builder.append("tooltip: ").appendln(tooltip);
 		
 		return builder.toString();
 	}
 	
 	public static final String dumpPartReference(final IWorkbenchPartReference reference)
 	{
 		SmartStringBuilder builder = builder();
 		
 		Class<?> type = reference.getClass();
 		String   id   = reference.getId();
 		
 		String   name    = reference.getPartName();
 		String   title   = reference.getTitle();
 		String   tooltip = reference.getTitleToolTip();
 		
 		boolean dirty = reference.isDirty();
 
 		builder.append("class: ").appendln(dumpClass(type));
 		builder.append("identifier: ").appendln(id);
 		
 		builder.append("name: ").appendln(name);
 		builder.append("title: ").appendln(title);
 		builder.append("tooltip: ").appendln(tooltip);
 		
 		builder.append("dirty: ").appendln(dirty);
 		
 		return builder.toString();
 	}
 	
 	public static final String dumpPerspectiveDescriptor(final IPerspectiveDescriptor descriptor)
 	{
 		SmartStringBuilder builder = builder();
 		
 		Class<?> type = descriptor.getClass();
 		String   id   = descriptor.getId();
 
 		String   label       = descriptor.getLabel();
 		String   description = descriptor.getDescription();
 
 		builder.append("class: ").appendln(dumpClass(type));
 		builder.append("identifier: ").appendln(id);
 		
 		builder.append("label: ").appendln(label);
 		builder.append("description: ").appendln(description);
 		
 		return builder.toString();
 	}
 
 	public static final String dumpRefactoringDescriptorProxy(final RefactoringDescriptorProxy proxy)
 	{
 		SmartStringBuilder builder = builder();
 		
 		String project     = proxy.getProject();
 		String description = proxy.getDescription();
 
 		long timestamp = proxy.getTimeStamp();
 	
 		builder.append("project: ").appendln(project);
 		builder.append("description: ").appendln(description);
 		builder.append("timestamp: ").appendln(timestamp);
 		
 		return builder.toString();
 	}
 
 	public static final String dumpRefactoringExecutionEvent(final RefactoringExecutionEvent event)
 	{
 		SmartStringBuilder builder = builder();
 		
 		RefactoringExecutionEventType type = RefactoringExecutionEventType.valueOf(event.getEventType());
 		
 		RefactoringDescriptorProxy descriptor = event.getDescriptor();
 		
 		builder.format("type: %s (%d)", type, type.getValue()).appendln();
 		builder.appendln("descriptor:").lines(dumpRefactoringDescriptorProxy(descriptor));
 		
 		return builder.toString();
 	}
 	
 	public static final String dumpRefactoringHistoryEvent(final RefactoringHistoryEvent event)
 	{
 		SmartStringBuilder builder = builder();
 		
 		RefactoringHistoryEventType type = RefactoringHistoryEventType.valueOf(event.getEventType());
 		
 		RefactoringDescriptorProxy descriptor = event.getDescriptor();
 		
 		builder.format("type: %s (%d)", type, type.getValue()).appendln();
 		builder.appendln("descriptor:").lines(dumpRefactoringDescriptorProxy(descriptor));
 		
 		return builder.toString();
 	}
 
 	public static final String dumpResource(final IResource resource)
 	{
 		SmartStringBuilder builder = builder();
 	
 		ResourceType type = ResourceType.valueOf(resource.getType());
 		
 		String name     = resource.getName();
 		IPath  location = resource.getLocation();
 	
 		long localStamp        = resource.getLocalTimeStamp();
 		long modificationStamp = resource.getModificationStamp();
 	
 		builder.format("type: %s (%d)", type, type.getValue()).appendln();
 		
 		builder.append("name: ").appendln(name);
 		builder.append("location: ").appendln(location);
 		
 		builder.append("local stamp: ").appendln(localStamp < 0 ? "unknown" : localStamp);
 		builder.append("modification stamp: ").appendln(modificationStamp < 0 ? "unknown" : modificationStamp);
 		
 		return builder.toString();
 	}
 
 	public static final String dumpResourceChangeEvent(final IResourceChangeEvent event)
 	{
 		SmartStringBuilder builder = builder();
 	
 		ResourceEventType type = ResourceEventType.valueOf(event.getType());
 		
 		int buildKind = event.getBuildKind();
 	
 		IResource      resource = event.getResource();
 		IResourceDelta delta    = event.getDelta();
 		
 		builder.format("type: %s (%d)", type, type.getValue()).appendln();
 		
 		builder.append("build kind: ").appendln(buildKind == 0 ? "not applicable" : ProjectBuildKind.valueOf(buildKind));
 		
 		builder.appendln("resource:").lines(resource == null ? missing() : dumpResource(resource));		
		builder.appendln("delta:").lines(dumpResourceDelta(delta));
 		
 		return builder.toString();
 	}
 
 	public static final String dumpResourceDelta(final IResourceDelta delta)
 	{
 		SmartStringBuilder builder = builder();
 	
 		ResourceDeltaKind      kind  = ResourceDeltaKind.valueOf(delta.getKind());
 		Set<ResourceDeltaFlag> flags = ResourceDeltaFlag.setOf(delta.getFlags());
 	
 		IResource resource = delta.getResource();
 	
 		builder.append("kind: ").appendln(kind);
 		builder.append("flags: ").list(flags.isEmpty() ? Arrays.asList("none") : flags).appendln();
 	
 		builder.appendln("resource:").lines(dumpResource(resource));
 		
 		return builder.toString();
 	}
 
 	public static final String dumpSelection(final ISelection selection)
 	{
 		if (selection instanceof IMarkSelection)
 		{
 			return dumpMarkSelection((IMarkSelection) selection);
 		}
 		else if (selection instanceof IStructuredSelection)
 		{
 			return dumpStructuredSelection((IStructuredSelection) selection);
 		}
 		else if (selection instanceof ITextSelection)
 		{
 			return dumpTextSelection((ITextSelection) selection);
 		}
 		else
 		{
 			SmartStringBuilder builder = builder();
 
 			Class<?> type = selection.getClass();
 			
 			boolean empty = selection.isEmpty();
 			
 			builder.append("class: ").appendln(dumpClass(type));
 			builder.append("empty: ").appendln(empty);
 			
 			return builder.toString();
 		}
 	}
 
 	public static final String dumpStatus(final IStatus status)
 	{
 		SmartStringBuilder builder = builder();
 	
 		int code = status.getCode();
 		
 		Set<StatusSeverity> severity = StatusSeverity.setOf(status.getSeverity());
 		
 		String plugin  = status.getPlugin();
 		String message = status.getMessage();
 		
 		boolean ok    = status.isOK();
 		boolean multi = status.isMultiStatus();
 	
 		Throwable throwable = status.getException();
 	
 		builder.append("code: ").appendln(code);
 		builder.append("severity: ").list(severity.isEmpty() ? Arrays.asList("unknown") : severity).appendln();
 		
 		builder.append("plugin: ").appendln(plugin);
 		builder.append("message: ").appendln(message);
 		
 		builder.append("ok: ").appendln(ok);
 		builder.append("multi: ").appendln(multi);
 	
 		if (throwable != null)
 		{
 			builder.appendln("exception:").lines(dumpThrowable(throwable));
 		}
 		
 		return builder.toString();
 	}
 
 	public static final String dumpStructuredSelection(final IStructuredSelection selection)
 	{
 		SmartStringBuilder builder = builder();
 
 		boolean empty = selection.isEmpty();
 		
 		int size = selection.size();
 
 		builder.appendln("category: structured");
 		
 		builder.append("empty: ").appendln(empty);
 		
 		builder.append("size: ").appendln(size);
 		
 		return builder.toString();
 	}
 	
 	public static final String dumpTestCaseElement(final ITestCaseElement element)
 	{
 		SmartStringBuilder builder = builder();
 
 		ITestRunSession session = element.getTestRunSession();
 
 		String className  = element.getTestClassName();
 		String methodName = element.getTestMethodName();
 
 		double elapsedTime = element.getElapsedTimeInSeconds();
 
 		ITestElement.ProgressState progressState = element.getProgressState();
 		
 		ITestElement.Result resultExcludingChildren = element.getTestResult(false);
 		ITestElement.Result resultIncludingChildren = element.getTestResult(true);
 
 		builder.appendln("session:").lines(dumpTestRunSession(session));
 		
 		builder.append("class name: ").appendln(className);
 		builder.append("method name: ").appendln(methodName);
 		
 		builder.append("elapsed time: ").append(elapsedTime).appendln(" (in seconds)");
 		
 		builder.append("progress state: ").appendln(progressState);
 		
 		builder.append("result excluding children: ").appendln(resultExcludingChildren);
 		builder.append("result including children: ").appendln(resultIncludingChildren);
 		
 		return builder.toString();
 	}
 
 	public static final String dumpTestRunSession(final ITestRunSession session)
 	{
 		SmartStringBuilder builder = builder();
 
 		IJavaProject project = session.getLaunchedProject();
 		
 		String name = session.getTestRunName();
 
 		double elapsedTime = session.getElapsedTimeInSeconds();
 
 		ITestElement.ProgressState progressState = session.getProgressState();
 		
 		ITestElement.Result resultExcludingChildren = session.getTestResult(false);
 		ITestElement.Result resultIncludingChildren = session.getTestResult(true);
 		
 		ITestElement[] children = session.getChildren();
 
 		builder.append("run name: ").appendln(name);
 		
 		builder.appendln("project:").lines(dumpJavaProject(project));
 		
 		builder.append("elapsed time: ").append(elapsedTime).appendln(" (in seconds)");
 		
 		builder.append("progress state: ").appendln(progressState);
 		
 		builder.append("result excluding children: ").appendln(resultExcludingChildren);
 		builder.append("result including children: ").appendln(resultIncludingChildren);
 
 		builder.append("children: ").appendln(children.length);
 
 		return builder.toString();
 	}
 	
 	public static final String dumpTextSelection(final ITextSelection selection)
 	{
 		SmartStringBuilder builder = builder();
 
 		boolean empty = selection.isEmpty();
 		
 		int start = selection.getStartLine();
 		int end   = selection.getEndLine();
 
 		int offset = selection.getOffset();
 		int length = selection.getLength();
 
 		String text = selection.getText();
 
 		builder.appendln("category: text");
 		
 		builder.append("empty: ").appendln(empty);
 		
 		builder.append("start line: ").appendln(start);
 		builder.append("end line: ").appendln(end);
 
 		builder.append("offset: ").appendln(offset);
 		builder.append("length: ").appendln(length);
 
 		builder.append("text: \"").append(text).appendln("\"");
 
 		return builder.toString();
 	}
 	
 	public static final String dumpThrowable(final Throwable throwable)
 	{
 		SmartStringBuilder builder = builder();
 
 		Class<?> type    = throwable.getClass();
 		String   message = throwable.getMessage();
 
 		builder.append("class: ").appendln(dumpClass(type));
 		builder.append("message: ").appendln(message);
 		
 		return builder.toString();
 	}
 	
 	public static final String dumpUndoableOperation(final IUndoableOperation operation)
 	{
 		SmartStringBuilder builder = builder();
 
 		String label = operation.getLabel();
 
 		boolean execute = operation.canExecute();
 		boolean redo    = operation.canRedo();
 		boolean undo    = operation.canUndo();
 		
 		builder.append("label: ").appendln(label);
 		
 		builder.append("can execute: ").appendln(execute);
 		builder.append("can redo: ").appendln(redo);
 		builder.append("can undo: ").appendln(undo);
 		
 		return builder.toString();		
 	}
 	
 	public static final String dumpWindow(final IWorkbenchWindow window)
 	{
 		SmartStringBuilder builder = builder();
 	
 		Class<?> type       = window.getClass();
 		int      pagesCount = window.getPages().length;
 
 		IWorkbenchPage activePage = window.getActivePage();
 		
 		builder.append("class: ").appendln(dumpClass(type));
 		builder.append("pages: ").appendln(pagesCount);
 		
 		builder.appendln("active page:").lines(dumpPage(activePage));
 		
 		return builder.toString();
 	}
 
 	public static final String dumpWorkbench(final IWorkbench workbench)
 	{
 		SmartStringBuilder builder = builder();
 
 		Class<?> type        = workbench.getClass();
 		int      windowCount = workbench.getWorkbenchWindowCount();
 		
 		boolean starting = workbench.isStarting();
 		boolean closing  = workbench.isClosing();
 
 		builder.append("class: ").appendln(dumpClass(type));
 		builder.append("windows: ").appendln(windowCount);
 		
 		builder.append("starting: ").appendln(starting);
 		builder.append("closing: ").appendln(closing);
 		
 		return builder.toString();
 	}
 }
