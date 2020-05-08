 package org.GreenTeaScript.DShell;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.lang.reflect.Constructor;
 import java.lang.reflect.InvocationTargetException;
 import java.util.ArrayList;
 import java.util.EmptyStackException;
 import java.util.Stack;
 import java.util.regex.Pattern;
 
 public class Task {
 	private ProcMonitor monitor;
 	private DShellProcess dshellProc;
 	private boolean terminated = false;
 	private final boolean isAsyncTask;
 	private String stdoutMessage;
 	private String stderrMessage;
 	private StringBuilder sBuilder;
 
 	public Task(DShellProcess dshellProc) {
 		this.dshellProc = dshellProc;
 		this.isAsyncTask = DShellProcess.is(this.dshellProc.getOptionFlag(), DShellProcess.background);
 		this.monitor = new ProcMonitor(this, this.dshellProc, isAsyncTask);
 		this.monitor.start();
 		this.sBuilder = new StringBuilder();
 		if(this.isAsyncTask) {
 			this.sBuilder.append("#AsyncTask");
 		}
 		else {
 			this.sBuilder.append("#SyncTask");
 		}
 		this.sBuilder.append("\n");
 		this.sBuilder.append(this.dshellProc.toString());
 	}
 
 	@Override public String toString() {
 		return this.sBuilder.toString();
 	}
 
 	public void join() {
 		if(this.terminated) {
 			return;
 		}
 		try {
 			this.terminated = true;
 			this.stdoutMessage = dshellProc.stdoutHandler.waitTermination();
 			this.stderrMessage = dshellProc.stderrHandler.waitTermination();
			monitor.join();
 		} 
 		catch (InterruptedException e) {
 			throw new RuntimeException(e);
 		}
 		new ShellExceptionRaiser(this.dshellProc).raiseException();
 	}
 
 	public void join(long timeout) {
 		
 	}
 
 	public String getOutMessage() {
 		this.checkTermination();
 		return this.stdoutMessage;
 	}
 
 	public String getErrorMessage() {
 		this.checkTermination();
 		return this.stderrMessage;
 	}
 
 	public int getExitStatus() {
 		this.checkTermination();
 		PseudoProcess[] procs = this.dshellProc.getProcesses();
 		return procs[procs.length - 1].getRet();
 	}
 
 	private void checkTermination() {
 		if(!this.terminated) {
 			throw new IllegalThreadStateException("Task is not Terminated");
 		}
 	}
 }
 
 class ProcMonitor extends Thread {	// TODO: support exit handle
 	private Task task;
 	private DShellProcess dshellProc;
 	private final boolean isBackground;
 	public final long timeout;
 
 	public ProcMonitor(Task task, DShellProcess dShellProc, boolean isBackground) {
 		this.task = task;
 		this.dshellProc = dShellProc;
 		this.isBackground = isBackground;
 		this.timeout =  this.dshellProc.getTimeout();
 	}
 
 	@Override public void run() {
 		PseudoProcess[] processes = this.dshellProc.getProcesses();
 		int size = processes.length;
 		if(this.timeout > 0) { // timeout
 			try {
 				Thread.sleep(timeout);	// ms
 				StringBuilder msgBuilder = new StringBuilder();
 				msgBuilder.append("Timeout Task: ");
 				for(int i = 0; i < size; i++) {
 					processes[i].kill();
 					if(i != 0) {
 						msgBuilder.append("| ");
 					}
 					msgBuilder.append(processes[i].getCmdName());
 				}
 				System.err.println(msgBuilder.toString());
 				// run exit handler
 			} 
 			catch (InterruptedException e) {
 				throw new RuntimeException(e);
 			}
 			return;
 		}
 		if(!this.isBackground) {	// wait termination for sync task
 			for(int i = 0; i < size; i++) {
 				processes[i].waitTermination();
 			}
 		}
 		while(this.isBackground) {	// check termination for async task
 			int count = 0;
 			for(int i = 0; i < size; i++) {
 				SubProc subProc = (SubProc)processes[i];
 				try {
 					subProc.checkTermination();
 					count++;
 				}
 				catch(IllegalThreadStateException e) {
 					// process has not terminated yet. do nothing
 				}
 			}
 			if(count == size) {
 				StringBuilder msgBuilder = new StringBuilder();
 				msgBuilder.append("Terminated Task: ");
 				for(int i = 0; i < size; i++) {
 					if(i != 0) {
 						msgBuilder.append("| ");
 					}
 					msgBuilder.append(processes[i].getCmdName());
 				}
 				System.err.println(msgBuilder.toString());
 				// run exit handler
 				return;
 			}
 			try {
 				Thread.sleep(100); // sleep thread
 			}
 			catch (InterruptedException e) {
 				throw new RuntimeException(e);
 			}
 		}
 	}
 }
 
 class CauseInferencer {
 	// syscall filter
 	private static final Pattern syscallFilter = Pattern.compile("^[1-9][0-9]* .+(.+) *= *.+");
 	private static final Pattern failedSyscallFilter = Pattern.compile("^[1-9][0-9]* .+(.+) *= *-[1-9].+");
 	private static final Pattern localeFilter = Pattern.compile("^.+(.+/locale.+).+");
 	private static final Pattern gconvFilter = Pattern.compile("^.+(.+/usr/lib64/gconv.+).+");
 
 	// function filter
 	private static final Pattern functionFilter = Pattern.compile("^  > .+");
 	private static final Pattern dcigettextFilter = Pattern.compile("^  > __dcigettext().+");
 	private static final Pattern exitFilter = Pattern.compile("^  > .+exit.*().+");
 	private static final Pattern libcStartMainFilter = Pattern.compile("^  > __libc_start_main().+");
 
 	private int traceBackendType;
 
 	public CauseInferencer(int traceBackendType) {
 		this.traceBackendType = traceBackendType;
 	}
 
 	private boolean applyFilter(Pattern filter, String line) {
 		return filter.matcher(line).find();
 	}
 
 	private boolean applyFilterToGroup(Pattern filter, int startIndex, ArrayList<String> group) {
 		int size = group.size();
 		for(int i = startIndex; i < size; i++) {
 			if(applyFilter(filter, group.get(i))) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	private Stack<String[]> filterStraceLog(String logFilePath) {
 		try {
 			Stack<String[]> parsedSyscallStack = new Stack<String[]>();
 			BufferedReader br = new BufferedReader(new FileReader(logFilePath));
 			String line;
 			while((line = br.readLine()) != null) {
 				if(applyFilter(failedSyscallFilter, line) && 
 						!applyFilter(localeFilter, line) && !applyFilter(gconvFilter, line)) {
 					parsedSyscallStack.push(parseLine(line));
 				}
 			}
 			br.close();
 			return parsedSyscallStack;
 		} 
 		catch (FileNotFoundException e) {
 			throw new RuntimeException(e);
 		} 
 		catch (IOException e) {
 			throw new RuntimeException(e);
 		}
 	}
 
 	private String getFullExcutablePath(String executableFile) {
 		String[] path = System.getenv("PATH").split(":");
 		int i = 0;
 		while(i < path.length) {
 			String fullPath = path[i] + "/" + executableFile;
 			if(new File(fullPath).exists()) {
 				return fullPath;
 			}
 			i = i + 1;
 		}
 		return null;
 	}
 
 	private String applyPostProcess(String logPath) {
 		StringBuilder cmdBuilder = new StringBuilder();
 		String shapedLogPath = logPath + "-shaped.log";
 		String scriptPath = getFullExcutablePath("pretty_print_strace_out.py");
 		
 		cmdBuilder.append("python");
 		cmdBuilder.append(" " + scriptPath + " " + logPath + " --trace > ");
 		cmdBuilder.append(shapedLogPath);
 		String[] cmds = {"bash", "-c", cmdBuilder.toString()};
 		try {
 			Process launcher = new ProcessBuilder(cmds).start();
 			launcher.waitFor();
 		} 
 		catch (IOException e) {
 			throw new RuntimeException(e);
 		} 
 		catch (InterruptedException e) {
 			throw new RuntimeException(e);
 		}
 		return shapedLogPath;
 	}
 
 	private Stack<String[]> filterStracePlusLog(String logFilePath) {
 		try {
 			String newLogFilePath = applyPostProcess(logFilePath);
 			Stack<String[]> parsedSyscallStack = new Stack<String[]>();
 			ArrayList<String> syscallGroup = null;
 			ArrayList<ArrayList<String>> syscallGroupList = new ArrayList<ArrayList<String>>();
 			BufferedReader br = new BufferedReader(new FileReader(newLogFilePath));
 			String line;
 			while((line = br.readLine()) != null) {
 				if(applyFilter(syscallFilter, line)) {
 					if(syscallGroup != null) {
 						syscallGroupList.add(syscallGroup);
 						syscallGroup = null;
 					}
 					syscallGroup = new ArrayList<String>();
 					syscallGroup.add(line);
 				}
 				else if(applyFilter(functionFilter, line)) {
 					syscallGroup.add(line);
 				}
 			}
 			if(syscallGroup != null) {
 				syscallGroupList.add(syscallGroup);
 				syscallGroup = null;
 			}
 			br.close();
 			SubProc.deleteLogFile(newLogFilePath);
 			
 			int size = syscallGroupList.size();
 			for(int i = 0; i < size; i++) {
 				ArrayList<String> group = syscallGroupList.get(i);
 				String syscall = group.get(0);
 				if(!applyFilter(failedSyscallFilter, syscall)) {
 					continue;
 				}
 				if(applyFilter(gconvFilter, syscall)) {
 					continue;
 				}
 				if(applyFilter(localeFilter, syscall)) {
 					continue;
 				}
 				if(!applyFilterToGroup(libcStartMainFilter, 1, group)) {
 					continue;
 				}
 				if(applyFilterToGroup(exitFilter, 1, group)) {
 					continue;
 				}
 				if(applyFilterToGroup(dcigettextFilter, 1, group)) {
 					continue;
 				}
 				parsedSyscallStack.push(parseLine(syscall));
 			}
 			return parsedSyscallStack;
 		} 
 		catch (FileNotFoundException e) {
 			throw new RuntimeException(e);
 		} 
 		catch (IOException e) {
 			throw new RuntimeException(e);
 		}
 	}
 
 	private Stack<String[]> filterTraceLog(String logFilePath) {
 		if(traceBackendType == SubProc.traceBackend_strace) {
 			return filterStraceLog(logFilePath);
 		}
 		else if(traceBackendType == SubProc.traceBackend_strace_plus) {
 			return filterStracePlusLog(logFilePath);
 		}
 		else {
 			throw new RuntimeException("invalid trace backend type");
 		}
 	}
 
 	private String[] parseLine(String syscallLine) {
 		int index = 0;
 		int whiteSpaceCount = 0;
 		int openBracketCount = 0;
 		int closeBracketCount = 0;
 		String[] parsedSyscall = new String[3];
 		String[] parsedSyscallTemp = new String[4];
 		StringBuilder sBuilder= new StringBuilder();
 
 		for(int i = 0; i < syscallLine.length(); i++) {
 			char token = syscallLine.charAt(i);
 			switch(token) {
 			case '(':
 				if(openBracketCount++ == 0) {
 					parsedSyscallTemp[index++] = new String(sBuilder.toString());
 					sBuilder = new StringBuilder();
 				}
 				break;
 			case ')':
 				if(openBracketCount == ++closeBracketCount) {
 					parsedSyscallTemp[index++] = new String(sBuilder.toString());
 					sBuilder = new StringBuilder();
 					openBracketCount = closeBracketCount = 0;
 				}
 				break;
 			default:
 				if(whiteSpaceCount < 2 && token == ' ') {
 					if(i + 1 < syscallLine.length() && syscallLine.charAt(i + 1) != ' ') {
 						whiteSpaceCount++;
 					}
 				} 
 				else {
 					sBuilder.append(token);
 				}
 				break;
 			}
 		}
 		String[] splitStrings = parsedSyscallTemp[2].trim().split(" ");
 		parsedSyscall[0] = parsedSyscallTemp[0];
 		parsedSyscall[1] = parsedSyscallTemp[1];
 		parsedSyscall[2] = splitStrings[2];
 		return parsedSyscall;
 	}
 
 	public String[] doInference(String traceLogPath) {
 		Stack<String[]> syscallStack = this.filterTraceLog(traceLogPath);
 		try {
 			return syscallStack.peek();
 		}
 		catch(EmptyStackException e) {
 			return null;
 		}
 	}
 }
 
 class ShellExceptionRaiser {
 	private DShellProcess dshellProc;
 
 	public ShellExceptionRaiser(DShellProcess dshellProc) {
 		this.dshellProc = dshellProc;
 	}
 
 	public void raiseException() {
 		PseudoProcess[] procs = dshellProc.getProcesses();
 		boolean enableException = DShellProcess.is(this.dshellProc.getOptionFlag(), DShellProcess.throwable);
 		for(int i = 0; i < procs.length; i++) {
 			PseudoProcess targetProc = procs[i];
 			if(!enableException || dshellProc.getTimeout() > 0) {
 				continue;
 			}
 			String message = targetProc.getCmdName();
 			if(targetProc.isTraced() && targetProc instanceof SubProc) {
 				String logFilePath = ((SubProc)targetProc).getLogFilePath();
 				if(targetProc.getRet() != 0) {
 					CauseInferencer inferencer = new CauseInferencer(SubProc.traceBackendType);
 					String[] inferedSyscall = inferencer.doInference(logFilePath);
 					SubProc.deleteLogFile(logFilePath);
 					throw createException(message, inferedSyscall);
 				}
 				SubProc.deleteLogFile(logFilePath);
 			}
 			else {
 				if(targetProc.getRet() != 0) {
 					throw new DShellException(message);
 				}
 			}
 		}
 	}
 
 	private RuntimeException createException(String message, String[] syscall) {
 		// syscall: syscallName: 0, param: 1, errno: 2
 		Class<?>[] types = {String.class, String.class, String[].class};
 		Object[] args = {message, message, syscall};
 		try {
 			if(syscall == null) {
 				return new NotRelatedSyscallException(message);
 			}
 			Class<?> exceptionClass = ErrorToException.valueOf(syscall[2]).toException();
 			if(exceptionClass == null) {
 				return new DShellException(syscall[2] + " has not implemented yet!!");
 			}
 			else {
 				Constructor<?> constructor;
 				try {
 					constructor = exceptionClass.getConstructor(types);
 				}
 				catch (NoSuchMethodException e) {
 					throw new RuntimeException(e);
 				}
 				catch (SecurityException e) {
 					throw new RuntimeException(e);
 				}
 				try {
 					return (RelatedSyscallException) constructor.newInstance(args);
 				}
 				catch (InstantiationException e) {
 					throw new RuntimeException(e);
 				}
 				catch (IllegalAccessException e) {
 					throw new RuntimeException(e);
 				}
 				catch (InvocationTargetException e) {
 					throw new RuntimeException(e);
 				}
 			}
 		}
 		catch (IllegalArgumentException e) {
 			return new RuntimeException((syscall[2] + " is not syscall!!"));
 		}
 	}
 }
 
 enum Syscall {
 	open, openat, connect,
 }
 
 enum ErrorToException {
 	E2BIG {
 		public Class<?> toException() {
 			return TooManyArgsException.class;
 		}
 	}, 
 	EACCES {
 		public Class<?> toException() {
 			return NotPermittedException.class;
 		}
 	}, 
 	EADDRINUSE, 
 	EADDRNOTAVAIL, 
 	EAFNOSUPPORT,
 	EAGAIN {
 		public Class<?> toException() {
 			return TemporaryUnavailableException.class;
 		}
 	}, 
 	EALREADY, 
 	EBADE, 
 	EBADF {
 		public Class<?> toException() {
 			return BadFileDescriptorException.class;
 		}
 	}, 
 	EBADFD {
 		public Class<?> toException() {
 			return BadStateFileDescriptorException.class;
 		}
 	}, 
 	EBADMSG {
 		public Class<?> toException() {
 			return BadMessageException.class;
 		}
 	}, 
 	EBADR, 
 	EBADRQC, 
 	EBADSLT, 
 	EBUSY, 
 	ECANCELED, 
 	ECHILD {
 		public Class<?> toException() {
 			return NoChildException.class;
 		}
 	}, 
 	ECHRNG, 
 	ECOMM, 
 	ECONNABORTED,
 	ECONNREFUSED {
 		public Class<?> toException() {
 			return ConnectionRefusedException.class;
 		}
 	}, 
 	ECONNRESET, 
 	EDEADLK, 
 	EDEADLOCK, 
 	EDESTADDRREQ, 
 	EDOM,
 	EDQUOT, 
 	EEXIST {
 		public Class<?> toException() {
 			return FileExistException.class;
 		}
 	}, 
 	EFAULT, 
 	EFBIG {
 		public Class<?> toException() {
 			return TooLargeFileException.class;
 		}
 	}, 
 	EHOSTDOWN, 
 	EHOSTUNREACH {
 		public Class<?> toException() {
 			return UnreachableHostException.class;
 		}
 	}, 
 	EIDRM, 
 	EILSEQ,
 	EINPROGRESS, 
 	EINTR {
 		public Class<?> toException() {
 			return InterruptedBySignalException.class;
 		}
 	}, 
 	EINVAL {
 		public Class<?> toException() {
 			return InvalidArgumentException.class;
 		}
 	}, 
 	EIO {
 		public Class<?> toException() {
 			return org.GreenTeaScript.DShell.IOException.class;
 		}
 	}, 
 	EISCONN, 
 	EISDIR {
 		public Class<?> toException() {
 			return IsDirectoryException.class;
 		}
 	}, 
 	EISNAM, 
 	EKEYEXPIRED,
 	EKEYREJECTED, 
 	EKEYREVOKED, 
 	EL2HLT, 
 	EL2NSYNC, 
 	EL3HLT, 
 	EL3RST, 
 	ELIBACC, 
 	ELIBBAD, 
 	ELIBMAX, 
 	ELIBSCN, 
 	ELIBEXEC, 
 	ELOOP {
 		public Class<?> toException() {
 			return TooManyLinkException.class;
 		}
 	}, 
 	EMEDIUMTYPE, 
 	EMFILE {
 		public Class<?> toException() {
 			return TooManyFileOpenException.class;
 		}
 	}, 
 	EMLINK, 
 	EMSGSIZE {
 		public Class<?> toException() {
 			return TooLongMessageException.class;
 		}
 	}, 
 	EMULTIHOP, 
 	ENAMETOOLONG {
 		public Class<?> toException() {
 			return TooLongNameException.class;
 		}
 	}, 
 	ENETDOWN, 
 	ENETRESET, 
 	ENETUNREACH {
 		public Class<?> toException() {
 			return UnreachableNetworkException.class;
 		}
 	}, 
 	ENFILE {
 		public Class<?> toException() {
 			return FileTableOverflowException.class;
 		}
 	},
 	ENOBUFS {
 		public Class<?> toException() {
 			return NoBufferSpaceException.class;
 		}
 	}, 
 	ENODATA, 
 	ENODEV {
 		public Class<?> toException() {
 			return DeviceNotFoundException.class;
 		}
 	}, 
 	ENOENT {
 		public Class<?> toException() {
 			return org.GreenTeaScript.DShell.FileNotFoundException.class;
 		}
 	}, 
 	ENOEXEC, 
 	ENOKEY, 
 	ENOLCK, 
 	ENOLINK, 
 	ENOMEDIUM, 
 	ENOMEM {
 		public Class<?> toException() {
 			return NoFreeMemoryException.class;
 		}
 	},
 	ENOMSG, 
 	ENONET, 
 	ENOPKG, 
 	ENOPROTOOPT, 
 	ENOSPC {
 		public Class<?> toException() {
 			return NoFreeSpaceException.class;
 		}
 	}, 
 	ENOSR, 
 	ENOSTR,
 	ENOSYS, 
 	ENOTBLK, 
 	ENOTCONN, 
 	ENOTDIR {
 		public Class<?> toException() {
 			return NotDirectoryException.class;
 		}
 	}, 
 	ENOTEMPTY {
 		public Class<?> toException() {
 			return NotEmptyDirectoryException.class;
 		}
 	}, 
 	ENOTSOCK {
 		public Class<?> toException() {
 			return NotSocketException.class;
 		}
 	}, 
 	ENOTSUP, 
 	ENOTTY {
 		public Class<?> toException() {
 			return InappropriateOperateException.class;
 		}
 	}, 
 	ENOTUNIQ, 
 	ENXIO, 
 	EOPNOTSUPP, 
 	EOVERFLOW, 
 	EPERM{
 		public Class<?> toException() {
 			return NotPermittedOperateException.class;
 		}
 	}, 
 	EPFNOSUPPORT, 
 	EPIPE {
 		public Class<?> toException() {
 			return BrokenPipeException.class;
 		}
 	}, 
 	EPROTO, 
 	EPROTONOSUPPORT, 
 	EPROTOTYPE, 
 	ERANGE, 
 	EREMCHG, 
 	EREMOTE, 
 	EREMOTEIO {
 		public Class<?> toException() {
 			return RemoteIOException.class;
 		}
 	},
 	ERESTART, 
 	EROFS {
 		public Class<?> toException() {
 			return ReadOnlyException.class;
 		}
 	}, 
 	ESHUTDOWN, 
 	ESPIPE {
 		public Class<?> toException() {
 			return IllegalSeekException.class;
 		}
 	}, 
 	ESOCKTNOSUPPORT, 
 	ESRCH, 
 	ESTALE, 
 	ESTRPIPE, 
 	ETIME, 
 	ETIMEDOUT {
 		public Class<?> toException() {
 			return ConnectionTimeoutException.class;
 		}
 	}, 
 	ETXTBSY, 
 	EUCLEAN, 
 	EUNATCH, 
 	EUSERS {
 		public Class<?> toException() {
 			return TooManyUsersException.class;
 		}
 	}, 
 	EWOULDBLOCK {
 		public Class<?> toException() {
 			return EAGAIN.toException();
 		}
 	}, 
 	EXDEV, 
 	EXFULL;
 
 	public Class<?> toException() {
 		return null;
 	}
 }
