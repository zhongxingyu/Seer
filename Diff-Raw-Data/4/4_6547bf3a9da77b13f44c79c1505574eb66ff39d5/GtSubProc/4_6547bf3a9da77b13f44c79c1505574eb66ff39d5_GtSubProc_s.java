 package org.GreenTeaScript.JVM;
 
 import java.io.BufferedInputStream;
 import java.io.BufferedOutputStream;
 import java.io.BufferedReader;
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.lang.reflect.Field;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Stack;
 import java.util.Timer;
 import java.util.TimerTask;
 import java.util.concurrent.TimeUnit;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.GreenTeaScript.LibGreenTea;
 
 public class GtSubProc {
 	// option index
 	private static final int returnable = 1 << 0;
 	private static final int throwable = 1 << 1;
 	private static final int background = 1 << 2;
 	private static final int enableTrace = 1 << 3;
 	
 	
 	// called by VisitCommandNode at JavaByteCodeGenerator 
 	public static String ExecCommandString(String[]... cmds) throws Exception {
 		int option = returnable | throwable | enableTrace;
 		return runCommands(cmds, option).str;
 	}
 
 	public static boolean ExecCommandBool(String[]... cmds) throws Exception {
 		int option = returnable;
 		return runCommands(cmds, option).bool;
 	}
 
 	public static void ExecCommandVoid(String[]... cmds) throws Exception {
 		int option = throwable | enableTrace;
 		runCommands(cmds, option);
 	}
 	//---------------------------------------------
 
 	private static boolean checkTraceRequirements() {
 		if(System.getProperty("os.name").equals("Linux")) {
			return LibGreenTea.IsUnixCommand("strace");
 		}
 		return false;
 	}
 	
 	private static boolean is(int option, int flag) {
 		option &= flag;
 		return option == flag;
 	}
 	
 	private static int setFlag(int option, int flag, boolean set) {
 		if(set && !is(option, flag)) {
 			return option | flag;
 		}
 		else if(!set && is(option, flag)) {
 			return option & ~flag;
 		}
 		return option;
 	}
 	
 	private static PseudoProcess createProc(String[] cmds, boolean enableSyscallTrace) {
 		PseudoProcess proc = null;
 		String cmdSymbol = cmds[0];
 		if(LibGreenTea.EqualsString(cmdSymbol, "<")) {
 			proc = new InRedirectProc();
 			proc.setArgument(cmds);
 		}
 		else if(LibGreenTea.EqualsString(cmdSymbol, ">")) {
 			proc = new OutRedirectProc();
 			proc.setArgument(cmds);
 		}
 		else if(LibGreenTea.EqualsString(cmdSymbol, "checkpoint")) {
 			
 		}
 		else if(LibGreenTea.EqualsString(cmdSymbol, "rollback")) {
 			
 		}
 		else {
 			proc = new SubProc(enableSyscallTrace);
 			proc.setArgument(cmds);
 		}
 		return proc;
 	}
 	
 	private static RetPair runCommands(String[][] cmds, int option) throws Exception {
 		// prepare shell option
 		int size = 0;
 		long timeout = -1;
 		for(int i = 0; i < cmds.length; i++) {
 			if(LibGreenTea.EqualsString(cmds[i][0], "set")) {
 				if(cmds[i].length < 2) {
 					continue;
 				}
 				String subOption = cmds[i][1];
 				if(LibGreenTea.EqualsString(subOption, "trace=on")) {
 					option = setFlag(option, enableTrace, true);
 				}
 				else if(LibGreenTea.EqualsString(subOption, "trace=off")) {
 					option = setFlag(option, enableTrace, false);
 				}
 				else if(LibGreenTea.EqualsString(subOption, "background")) {
 					option = setFlag(option, background, true);
 				}
 				else if(subOption.startsWith("timeout=")) {
 					String num = LibGreenTea.SubString(subOption, "timeout=".length() - 1, subOption.length());
 					long parsedNum = LibGreenTea.ParseInt(num);
 					if(parsedNum >= 0) {
 						timeout = parsedNum;
 					}
 				}
 				continue;
 			}
 			size++;
 		}
 		if(is(option, enableTrace)) {
 			option = setFlag(option, enableTrace, checkTraceRequirements());
 		}
 		
 		// init process
 		ShellExceptionRaiser exceptionRaiser = new ShellExceptionRaiser(is(option, throwable));
 		PseudoProcess[] subProcs = new PseudoProcess[size];
 		for(int i = 0; i < size; i++) {
 			subProcs[i] = createProc(cmds[i], is(option, enableTrace));
 			exceptionRaiser.setProcess(subProcs[i]);
 		}
 		
 		// start process
 		int lastIndex = size - 1;
 		subProcs[0].start();
 		for(int i = 1; i < size; i++) { 
 			subProcs[i].start();
 			subProcs[i - 1].pipe(subProcs[i]);
 		}
 		subProcs[lastIndex].waitResult(is(option, returnable));
 		
 		// raise exception
 		exceptionRaiser.raiseException();
 		
 		// get result value
 		String stdout = subProcs[lastIndex].getStdout();
 		boolean ret = (subProcs[lastIndex].getRet() == 0);
 		return new RetPair(stdout, ret);
 	}
 }
 
 class RetPair {
 	public String str;
 	public boolean bool;
 
 	public RetPair(String str, boolean bool) {
 		this.str = str;
 		this.bool = bool;
 	}
 }
 
 class PseudoProcess {
 	protected PseudoProcess pipedPrevProc;
 	
 	protected OutputStream stdin = null;
 	protected InputStream stdout = null;
 	protected InputStream stderr = null;
 
 	protected StringBuilder cmdNameBuilder;
 	protected ArrayList<String> commandList;
 
 	protected boolean stdoutIsRedireted = false;
 	protected boolean stderrIsRedireted = false;
 	protected boolean streamIsLocked = false;
 
 	protected int retValue = 0;
 
 	public PseudoProcess() {
 		this.cmdNameBuilder = new StringBuilder();
 		this.commandList = new ArrayList<String>();
 	}
 
 	public void setArgument(String Arg) {
 		this.cmdNameBuilder.append(Arg + " ");
 		this.commandList.add(Arg);
 	}
 
 	public void setArgument(String[] Args) {
 		for(int i = 0; i < Args.length; i++) {
 			this.setArgument(Args[i]);
 		}
 	}
 
 	public void start() {
 	}
 
 	public void pipe(PseudoProcess destProc) {
 		destProc.pipedPrevProc = this;
 		new PipeStreamHandler(this.stdout, destProc.stdin, true).start();
 	}
 	
 	public void kill() {
 	}
 
 	public void waitFor() {
 	}
 
 	public void waitFor(long timeout) {
 	}
 
 	public void waitResult(boolean isExpr) {
 	}
 	
 	public String getStdout() {
 		return "";
 	}
 
 	public String getStderr() {
 		return "";
 	}
 	
 	public int getRet() {
 		return this.retValue;
 	}
 
 	public String getCmdName() {
 		return this.cmdNameBuilder.toString();
 	}
 	
 	public boolean isTraced() {
 		return false;
 	}
 }
 
 class SubProc extends PseudoProcess {
 	private final static String logdirPath = "/tmp/strace-log";
 	private static int logId = 0;
 	private Process proc;
 	
 	private boolean enableSyscallTrace = false;
 	public boolean isKilled = false;
 
 	public String logFilePath = null;
 
 	private ByteArrayOutputStream outBuf;
 	private ByteArrayOutputStream errBuf;
 
 	private static String createLogDirectory() {
 		Calendar cal = Calendar.getInstance();
 		StringBuilder pathBuilder = new StringBuilder();
 		
 		pathBuilder.append(logdirPath + "/");
 		pathBuilder.append(cal.get(Calendar.YEAR) + "-");
 		pathBuilder.append((cal.get(Calendar.MONTH) + 1) + "-");
 		pathBuilder.append(cal.get(Calendar.DATE));
 		
 		String subdirPath = pathBuilder.toString();
 		File subdir = new File(subdirPath);
 		subdir.mkdirs();
 		
 		return subdirPath;
 	}
 
 	private static String createLogNameHeader() {
 		Calendar cal = Calendar.getInstance();
 		StringBuilder logNameHeader = new StringBuilder();
 		
 		logNameHeader.append(cal.get((Calendar.HOUR) + 1) + ":");
 		logNameHeader.append(cal.get(Calendar.MINUTE) + "-");
 		logNameHeader.append(cal.get(Calendar.MILLISECOND));
 		logNameHeader.append("-" + logId++);
 
 		return logNameHeader.toString();
 	}
 	
 	public SubProc(boolean enableSyscallTrace) {
 		super();
 		this.enableSyscallTrace = enableSyscallTrace;
 		initTrace();
 	}
 
 	private void initTrace() {
 		if(this.enableSyscallTrace) {
 			String currentLogdirPath = createLogDirectory();
 			String logNameHeader = createLogNameHeader();
 			logFilePath = new String(currentLogdirPath + "/" + logNameHeader + ".log");
 			
 			String[] straceCmd = {"strace", "-t", "-f", "-F", "-o", logFilePath};
 			for(int i = 0; i < straceCmd.length; i++) {
 				this.commandList.add(straceCmd[i]);
 			}
 		}
 	}
 	
 	private void addCommand(String arg) {
 		if(LibGreenTea.EqualsString(arg, "sudo")) {
 			int size = this.commandList.size();
 			ArrayList<String> newCommandList = new ArrayList<String>();
 			newCommandList.add(arg);
 			for(int i = 0; i < size; i++) {
 				newCommandList.add(this.commandList.get(i));
 			}
 			this.commandList = newCommandList;
 		}
 		else {
 			this.commandList.add(arg);
 		}
 	}
 	
 	@Override public void setArgument(String Arg) {
 		this.cmdNameBuilder.append(Arg + " ");
 		this.addCommand(Arg);
 	}
 
 	@Override public void start() {
 		int size = this.commandList.size();
 		String[] cmd = new String[size];
 		for(int i = 0; i < size; i++) {
 			cmd[i] = this.commandList.get(i);
 		}
 
 		try {
 			this.proc = new ProcessBuilder(cmd).start();
 			this.stdin = this.proc.getOutputStream();
 			this.stdout = this.proc.getInputStream();
 			this.stderr = this.proc.getErrorStream();
 		}
 		catch (IOException e) {
 			throw new RuntimeException(e);
 		}
 	}
 
 	private void handleOutputStream(OutputStream out, OutputStream err, boolean closeStream) {
 		if(streamIsLocked) {
 			return;
 		}
 		streamIsLocked = true;
 		
 		PipeStreamHandler stdoutHandler = null;
 		PipeStreamHandler stderrHandler = null;
 		if(!stdoutIsRedireted) {
 			stdoutHandler = new PipeStreamHandler(stdout, out, closeStream);
 			stdoutHandler.start();
 		}
 		
 		if(!stderrIsRedireted) {
 			stderrHandler = new PipeStreamHandler(stderr, err, closeStream);
 			stderrHandler.start();
 		}
 
 		try {
 			if(stdoutHandler != null) {
 				stdoutHandler.join();
 			}
 			if(stderrHandler != null) {
 				stderrHandler.join();
 			}
 		}
 		catch (InterruptedException e) {
 			throw new RuntimeException(e);
 		}
 	}
 
 	@Override public void waitResult(boolean isExpr) {
 		if(isExpr) {
 			outBuf = new ByteArrayOutputStream();
 			errBuf = new ByteArrayOutputStream();
 			handleOutputStream(outBuf, errBuf, true);
 		}
 		else {
 			handleOutputStream(System.out, System.err, false);
 		}
 	}
 
 	@Override public String getStdout() {
 		return this.outBuf == null ? "" : this.outBuf.toString();
 	}
 
 	@Override public String getStderr() {
 		return this.errBuf == null ? "" : this.errBuf.toString();
 	}
 
 	@Override public void waitFor() {
 		try {
 			this.retValue = this.proc.waitFor();
 		}
 		catch (InterruptedException e) {
 			e.printStackTrace();
 		}
 	}
 
 	@Override public void waitFor(long timeout) {
 		try {
 			this.proc.exitValue();
 		}
 		catch (IllegalThreadStateException e) {
 			try {
 				Thread.sleep(timeout);
 			}
 			catch (InterruptedException e1) {
 				e1.printStackTrace();
 			} 
 			finally {
 				this.kill();
 			}
 		}
 	}
 
 	@Override public void kill() {
 		try {
 			// get target pid
 			Field pidField = this.proc.getClass().getDeclaredField("pid");
 			pidField.setAccessible(true);
 			int pid = pidField.getInt(this.proc);
 			
 			// kill process
 			String[] cmds = {"kill", "-9", Integer.toString(pid)};
 			Process procKiller = new ProcessBuilder(cmds).start();
 			procKiller.waitFor();
 			this.isKilled = true;
 		} 
 		catch (NoSuchFieldException e) {
 			e.printStackTrace();
 		} 
 		catch (SecurityException e) {
 			e.printStackTrace();
 		} 
 		catch (IllegalArgumentException e) {
 			e.printStackTrace();
 		} 
 		catch (IllegalAccessException e) {
 			e.printStackTrace();
 		} 
 		catch (IOException e) {
 			e.printStackTrace();
 		} 
 		catch (InterruptedException e) {
 			e.printStackTrace();
 		}
 	}
 
 	public String getLogFilePath() {
 		return this.logFilePath;
 	}
 	
 	@Override public boolean isTraced() {
 		return this.enableSyscallTrace;
 	}
 }
 
 class InRedirectProc extends PseudoProcess {
 	@Override public void start() {
 		String fileName = this.commandList.get(1);
 		try {
 			this.stdout = new BufferedInputStream(new FileInputStream(fileName));
 		} 
 		catch (FileNotFoundException e) {
 			e.printStackTrace();
 		}
 	}
 }
 
 class OutRedirectProc extends PseudoProcess {
 	@Override public void start() {
 		String fileName = this.commandList.get(1);	
 		try {
 			this.stdin = new BufferedOutputStream(new FileOutputStream(fileName));
 		} 
 		catch (FileNotFoundException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	@Override public int getRet() {
 		return this.pipedPrevProc.getRet();
 	}
 }
 
 class ProcessTimer {
 	public ProcessTimer(PseudoProcess[] targetProcs, long timeout) {
 		ProcessKiller procKiller = new ProcessKiller(targetProcs);
 		Timer timer = new Timer();
 		timer.schedule(procKiller, TimeUnit.SECONDS.toMillis(timeout));
 	}
 }
 
 class ProcessKiller extends TimerTask {
 	private PseudoProcess[] procs;
 	
 	public ProcessKiller(PseudoProcess[] targetProcs) {
 		this.procs = targetProcs;
 	}
 	
 	public void killProcs() {
 		for(int i = 0; i < this.procs.length; i++) {
 			this.procs[i].kill();
 		}
 	}
 	
 	@Override public void run() {
 		this.killProcs();
 	}
 }
 
 // copied from http://blog.art-of-coding.eu/piping-between-processes/
 class PipeStreamHandler extends Thread {
 	private InputStream input;
 	private OutputStream output;
 	private boolean closeStream;
 
 	public PipeStreamHandler(InputStream input, OutputStream output, boolean closeStream) {
 		this.input = input;
 		this.output = output;
 		this.closeStream = closeStream;
 	}
 
 	@Override public void run() {
 		try {
 			byte[] buffer = new byte[512];
 			int read = 0;
 			while(read > -1) {
 				read = this.input.read(buffer, 0, buffer.length);
 				if(read > -1) {
 					this.output.write(buffer, 0, read);
 				}
 			}
 			if(closeStream) {
 				this.input.close();
 				this.output.close();
 			}
 		}
 		catch (IOException e) {
 			throw new RuntimeException(e);
 		}
 	}
 }
 
 class ShellExceptionRaiser {
 	private ArrayList<PseudoProcess> procList;
 	private boolean enableException;
 	
 	public ShellExceptionRaiser(boolean enableException) {
 		this.enableException = enableException;
 		this.procList = new ArrayList<PseudoProcess>();
 	}
 
 	public void setProcess(PseudoProcess kproc) {
 		this.procList.add(kproc);
 	}
 
 	public void raiseException() throws Exception {
 		int size = procList.size();
 		for(int i = 0; i < size; i++) {
 			PseudoProcess targetProc = procList.get(i);
 			targetProc.waitFor();
 			
 			if(!this.enableException) {
 				continue;
 			}
 			
 			// throw exception
 			String message = targetProc.getCmdName();
 			if(targetProc.isTraced() && targetProc instanceof SubProc) {	// infer systemcall error
 				SubProc castedProc = (SubProc)targetProc;
 				String logFilePath = castedProc.getLogFilePath();
 				if(targetProc.getRet() != 0) {
 					Stack<String[]> syscallStack = parseTraceLog(logFilePath);
 					deleteLogFile(logFilePath);
 					throw createException(message, syscallStack.peek());
 				}
 				deleteLogFile(logFilePath);
 			}
 			else {
 				if(targetProc.getRet() != 0) {
 					throw new Exception(message);
 				}
 			}
 		}
 	}
 
 	private void deleteLogFile(String logFilePath) {
 		new File(logFilePath).delete();
 	}
 
 	private Stack<String[]> parseTraceLog(String logFilePath) {
 		try {
 			Stack<String[]> syscallStack = new Stack<String[]>();
 			BufferedReader br = new BufferedReader(new FileReader(logFilePath));
 			
 			String regex1 = "^[1-9][0-9]* .+(.+) *= *-[1-9].+";
 			Pattern p1 = Pattern.compile(regex1);
 			
 			String regex2 = "^.+(.+/locale.+).+";
 			Pattern p2 = Pattern.compile(regex2);
 			
 			String regex3 = "(^[1-9][0-9]*)( *)([0-9][0-9]:[0-9][0-9]:[0-9][0-9])( *)(.+)";
 			Pattern p3 = Pattern.compile(regex3);
 			
 			Matcher m1, m2, m3;
 			
 			String line;
 			while((line = br.readLine()) != null) {
 				m1 = p1.matcher(line);
 				m2 = p2.matcher(line);
 				if(m1.find() && !m2.find()) {
 					m3 = p3.matcher(line);
 					syscallStack.push(parseLine(m3.replaceAll("$5")));
 				}
 			}
 			br.close();
 			
 			return syscallStack;
 		} 
 		catch (FileNotFoundException e) {
 			throw new RuntimeException(e);
 		} 
 		catch (IOException e) {
 			throw new RuntimeException(e);
 		}
 	}
 
 	private String[] parseLine(String syscallLine) {
 		int p = 0;
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
 					parsedSyscallTemp[p++] = new String(sBuilder.toString());
 					sBuilder = new StringBuilder();
 				}
 				break;
 			case ')':
 				if(openBracketCount == ++closeBracketCount) {
 					parsedSyscallTemp[p++] = new String(sBuilder.toString());
 					sBuilder = new StringBuilder();
 					openBracketCount = closeBracketCount = 0;
 				}
 				break;
 			default:
 				sBuilder.append(token);
 				break;
 			}
 		}
 		String[] splitStrings = parsedSyscallTemp[2].trim().split(" ");
 		
 		parsedSyscall[0] = parsedSyscallTemp[0];
 		parsedSyscall[1] = parsedSyscallTemp[1];
 		parsedSyscall[2] = splitStrings[2];
 
 		return parsedSyscall;
 	}
 
 	private Exception createException(String message, String[] syscall) throws Exception {
 		try {
 			return ErrNo.valueOf(syscall[2]).toException(message, syscall[0], syscall[1]);
 		}
 		catch (IllegalArgumentException e) {
 			return new Exception((syscall[2] + " is not syscall!!"));
 		}
 	}
 }
 
 enum Syscall {
 	open, openat, connect,
 }
 
 enum ErrNo {
 	E2BIG, 
 	EACCES {
 		public Exception toException(String message, String syscallName, String param) {
 			return new NotPermittedException(message);
 		}
 	}, 
 	EADDRINUSE, 
 	EADDRNOTAVAIL, 
 	EAFNOSUPPORT,
 	EAGAIN, 
 	EALREADY, 
 	EBADE, 
 	EBADF, 
 	EBADFD, 
 	EBADMSG, 
 	EBADR, 
 	EBADRQC, 
 	EBADSLT, 
 	EBUSY, 
 	ECANCELED, 
 	ECHILD, 
 	ECHRNG, 
 	ECOMM, 
 	ECONNABORTED,
 	ECONNREFUSED {
 		public Exception toException(String message, String syscallName, String param) {
 			return new ConnectRefusedException(message);
 		}
 	}, 
 	ECONNRESET, 
 	EDEADLK, 
 	EDEADLOCK, 
 	EDESTADDRREQ, 
 	EDOM,
 	EDQUOT, 
 	EEXIST, 
 	EFAULT, 
 	EFBIG, 
 	EHOSTDOWN, 
 	EHOSTUNREACH, 
 	EIDRM, 
 	EILSEQ,
 	EINPROGRESS, 
 	EINTR {
 		public Exception toException(String message, String syscallName, String param) {
 			return new InterruptedBySignalException(message);
 		}
 	}, 
 	EINVAL, 
 	EIO, 
 	EISCONN, 
 	EISDIR, 
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
 		public Exception toException(String message, String syscallName, String param) {
 			return new TooManyLinkException(message);
 		}
 	}, 
 	EMEDIUMTYPE, 
 	EMFILE, 
 	EMLINK, 
 	EMSGSIZE, 
 	EMULTIHOP, 
 	ENAMETOOLONG {
 		public Exception toException(String message, String syscallName, String param) {
 			return new TooLongNameException(message);
 		}
 	}, 
 	ENETDOWN, 
 	ENETRESET, 
 	ENETUNREACH {
 		public Exception toException(String message, String syscallName, String param) {
 			return new UnreachableException(message);
 		}
 	}, 
 	ENFILE,
 	ENOBUFS, 
 	ENODATA, 
 	ENODEV, 
 	ENOENT {
 		public Exception toException(String message, String syscallName, String param) {
 			return new NotFoundException(message);
 		}
 	}, 
 	ENOEXEC, 
 	ENOKEY, 
 	ENOLCK, 
 	ENOLINK, 
 	ENOMEDIUM, 
 	ENOMEM {
 		public Exception toException(String message, String syscallName, String param) {
 			return new NoFreeMemoryException(message);
 		}
 	},
 	ENOMSG, 
 	ENONET, 
 	ENOPKG, 
 	ENOPROTOOPT, 
 	ENOSPC {
 		public Exception toException(String message, String syscallName, String param) {
 			return new NoFreeSpaceException(message);
 		}
 	}, 
 	ENOSR, 
 	ENOSTR,
 	ENOSYS, 
 	ENOTBLK, 
 	ENOTCONN, 
 	ENOTDIR{
 		public Exception toException(String message, String syscallName, String param) {
 			return new NotDirectoryException(message);
 		}
 	}, 
 	ENOTEMPTY, 
 	ENOTSOCK, 
 	ENOTSUP, 
 	ENOTTY, 
 	ENOTUNIQ, 
 	ENXIO, 
 	EOPNOTSUPP, 
 	EOVERFLOW, 
 	EPERM{
 		public Exception toException(String message, String syscallName, String param) {
 			return new NotPermittedOperateException(message);
 		}
 	}, 
 	EPFNOSUPPORT, 
 	EPIPE, 
 	EPROTO, 
 	EPROTONOSUPPORT, 
 	EPROTOTYPE, 
 	ERANGE, 
 	EREMCHG, 
 	EREMOTE, 
 	EREMOTEIO,
 	ERESTART, 
 	EROFS {
 		public Exception toException(String message, String syscallName, String param) {
 			return new ReadOnlyException(message);
 		}
 	}, 
 	ESHUTDOWN, 
 	ESPIPE {
 		public Exception toException(String message, String syscallName, String param) {
 			return new IllegalSeekException(message);
 		}
 	}, 
 	ESOCKTNOSUPPORT, 
 	ESRCH, 
 	ESTALE, 
 	ESTRPIPE, 
 	ETIME, 
 	ETIMEDOUT {
 		public Exception toException(String message, String syscallName, String param) {
 			return new NetworkTimeoutException(message);
 		}
 	}, 
 	ETXTBSY, 
 	EUCLEAN, 
 	EUNATCH, 
 	EUSERS, 
 	EWOULDBLOCK, 
 	EXDEV, 
 	EXFULL;
 
 	public Exception toException(String message, String syscallName, String param) {
 		return new Exception(this.toString() + " is not yet implemented!!");
 	}
 }
