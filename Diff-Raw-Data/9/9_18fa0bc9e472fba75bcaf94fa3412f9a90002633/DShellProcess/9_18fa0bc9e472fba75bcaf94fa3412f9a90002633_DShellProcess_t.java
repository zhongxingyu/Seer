 package org.GreenTeaScript.DShell;
 
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
 import java.util.EmptyStackException;
 import java.util.Stack;
 import java.util.Timer;
 import java.util.TimerTask;
 import java.util.concurrent.TimeUnit;
 import java.util.regex.Pattern;
 
 import org.GreenTeaScript.DShellGrammar;
 import org.GreenTeaScript.LibGreenTea;
 
 public class DShellProcess {
 	// option flag
 	private static final int returnable = 1 << 0;
 	private static final int printable = 1 << 1;
 	private static final int throwable = 1 << 2;
 	private static final int background = 1 << 3;
 	private static final int enableTrace = 1 << 4;
 
 	// return type
 	private static final int VoidType = 0;
 	private static final int BooleanType = 1;
 	private static final int StringType = 2;
 
 	private static SubProc prevSubProc = null;
 
 	private String Result = "";
 	private long ReturnValue = -1;
 	private final int CommandFlag;
 	private final PseudoProcess LastProcess;
 	private final PseudoProcess[] Processes;
 	private long timeout;
 	private ShellExceptionRaiser ExceptionManager = null;
 	
 	public DShellProcess(int option, String[][] cmds, int ProcessSize, long timeout) {
 		// init process
 		this.ExceptionManager = new ShellExceptionRaiser(is(option, throwable));
 		this.Processes = new PseudoProcess[ProcessSize];
 		for(int i = 0; i < ProcessSize; i++) {
 			this.Processes[i] = DShellProcess.createProc(cmds[i], is(option, enableTrace));
 			this.ExceptionManager.setProcess(this.Processes[i]);
 		}
 		ErrorStreamHandler Handler = new ErrorStreamHandler(this.Processes);
 
 		// start process
 		int lastIndex = ProcessSize - 1;
 		this.Processes[0].start();
 		for(int i = 1; i < ProcessSize; i++) {
 			this.Processes[i].start();
 			this.Processes[i].pipe(this.Processes[i - 1]);
 		}
 		Handler.showErrorMessage();
 		this.CommandFlag = option;
 		this.LastProcess = this.Processes[lastIndex];
 		this.timeout = timeout;
 	}
 	private boolean IsBackGroundProcess() {
 		return is(this.CommandFlag, background);
 	}
 	private DShellProcess Detach() {
 		this.LastProcess.showResult();
 		if(timeout > 0) {
 			new ProcessTimer(this.Processes, timeout);
 		}
 		return this;
 	}
 	private Object GetResult(int ReturnType) throws Exception {
 		this.waitResult();
 		// raise exception
 		this.ExceptionManager.raiseException();
 		this.Result = LastProcess.getStdout();
 		this.ReturnValue = LastProcess.getRet();
 		// get result value
 		if(is(this.CommandFlag, returnable)) {
 			if(ReturnType == StringType) {
 				return this.Result;
 			}
 			else if(ReturnType == BooleanType) {
 				return new Boolean(this.ReturnValue == 0);
 			}
 		}
 		return null;
 	}
 	private void waitResult() {
 		this.LastProcess.waitResult(is(this.CommandFlag, printable));
 	}
 
 	// called by JavaByteCodeGenerator.VisitCommandNode 
 	public static void ExecCommandVoid(String[]... cmds) throws Exception {
 		int option = printable | throwable | enableTrace;
 		runCommands(cmds, option, VoidType);
 	}
 	public static String ExecCommandString(String[]... cmds) throws Exception {
 		int option = returnable | throwable | enableTrace;
 		return (String) runCommands(cmds, option, StringType);
 	}
 	public static boolean ExecCommandBool(String[]... cmds) throws Exception {
 		int option = returnable | printable;
 		return ((Boolean) runCommands(cmds, option, BooleanType)).booleanValue();
 	}
 
 	// file system roll back function
 	// TargetLV: vg_name/lv_name
 	public static boolean CreateSnapshot(String SnapshotLabel, String TargetLV) throws Exception {
 		if(System.getProperty("os.name").equals("Linux") && new File("/sbin/lvm").canExecute()) {
 			String[] cmds = {"sudo", "lvcreate", "-s", "-n", SnapshotLabel, TargetLV};
 			return ExecCommandBool(cmds);
 		}
 		return false;
 	}
 
 	public static boolean RevertSnapshot(String SnapshotLabel, String TargetLV) throws Exception {
 		if(System.getProperty("os.name").equals("Linux") && new File("/sbin/lvm").canExecute()) {
 			String[] volNames = TargetLV.split("/");
 			String VG_Name = volNames[0];
 			String LV_Name = volNames[1];
 			String mountableId = VG_Name + "-" + LV_Name;
 			
 			// get target LV mount point
 			int option = returnable | throwable;
 			String[][] mountPoint_cmds = {{"mount"}, {"grep", mountableId}};
 			String mountPoint;
 			try {
 				String[] results = ((String) runCommands(mountPoint_cmds, option, StringType)).split(" ");
 				mountPoint = results[2];
 			} catch (Exception e) {
 				return false;
 			}
 			
 			// umount target LV
 			String[] umount_cmds = {"sudo", "umount", mountPoint};
 			if(!ExecCommandBool(umount_cmds)) {
 				return false;
 			}
 			
 			// remove current LV
 			String[] lvremove_cmds = {"sudo", "lvremove", TargetLV};
 			if(!ExecCommandBool(lvremove_cmds)) {
 				return false;
 			}
 			
 			// rename snapshot
 			String[] lvrename_cmds = {"sudo", "lvrename", VG_Name + "/" + SnapshotLabel, LV_Name};
 			if(!ExecCommandBool(lvrename_cmds)) {
 				return false;
 			}
 			
 			// mount LV
 			String[] mount_cmds = {"sudo", "mount", "/dev/" + TargetLV, mountPoint};
 			return ExecCommandBool(mount_cmds);
 		}
 		return false;
 	}
 
 	//---------------------------------------------
 
 	private static boolean checkTraceRequirements() {
 		if(System.getProperty("os.name").equals("Linux")) {
 			boolean flag = DShellGrammar.IsUnixCommand("strace+") && 
 					DShellGrammar.IsUnixCommand("pretty_print_strace_out.py");
 			if(flag) {
 				SubProc.traceBackendType = SubProc.traceBackend_strace_plus;
 				return true;
 			}
 			else {
 				SubProc.traceBackendType = SubProc.traceBackend_strace;
 				return DShellGrammar.IsUnixCommand("strace");
 			}
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
 		else if(LibGreenTea.EqualsString(cmdSymbol, "1>") || LibGreenTea.EqualsString(cmdSymbol, ">")) {
 			proc = new OutRedirectProc();
 			proc.setArgument(cmds);
 		}
 		else if(LibGreenTea.EqualsString(cmdSymbol, "1>>") || LibGreenTea.EqualsString(cmdSymbol, ">>")) {
 			proc = new OutRedirectProc();
 			proc.setArgument(cmds);
 			((OutRedirectProc) proc).enablePostscriptMode();
 		}
 		else if(LibGreenTea.EqualsString(cmdSymbol, "2>")) {
 			proc = new OutRedirectProc();
 			proc.setArgument(cmds);
 			((OutRedirectProc) proc).enableErrorRedirectMode();
 		}
 		else if(LibGreenTea.EqualsString(cmdSymbol, "2>>")) {
 			proc = new OutRedirectProc();
 			proc.setArgument(cmds);
 			((OutRedirectProc) proc).enableErrorRedirectMode();
 			((OutRedirectProc) proc).enablePostscriptMode();
 		}
 		else if(LibGreenTea.EqualsString(cmdSymbol, "&>") || LibGreenTea.EqualsString(cmdSymbol, ">&")) {
 			proc = new OutRedirectProc();
 			proc.setArgument(cmds);
 			prevSubProc.setMergeType(SubProc.mergeErrorToOut);
 			prevSubProc = null;
 		}
 		else if(LibGreenTea.EqualsString(cmdSymbol, "&>>")) {
 			proc = new OutRedirectProc();
 			proc.setArgument(cmds);
 			((OutRedirectProc) proc).enablePostscriptMode();
 			prevSubProc.setMergeType(SubProc.mergeErrorToOut);
 			prevSubProc = null;
 		}
 		else if(LibGreenTea.EqualsString(cmdSymbol, ">&1") || 
 				LibGreenTea.EqualsString(cmdSymbol, "1>&1") || LibGreenTea.EqualsString(cmdSymbol, "2>&2")) {
 			proc = new EmptyProc();
 			proc.setArgument(cmds);
 		}
 		else if(LibGreenTea.EqualsString(cmdSymbol, "1>&2")) {
 			proc = new EmptyProc();
 			proc.setArgument(cmds);
 			prevSubProc.setMergeType(SubProc.mergeOutToError);
 			prevSubProc = null;
 		}
 		else if(LibGreenTea.EqualsString(cmdSymbol, "2>&1")) {
 			proc = new EmptyProc();
 			proc.setArgument(cmds);
 			prevSubProc.setMergeType(SubProc.mergeErrorToOut);
 			prevSubProc = null;
 		}
 		else {
 			proc = new SubProc(enableSyscallTrace);
 			proc.setArgument(cmds);
 			prevSubProc = (SubProc) proc;
 		}
 		return proc;
 	}
 
 	private static Object runCommands(String[][] cmds, int option, int retType) throws Exception {
 		prevSubProc = null;
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
 					option = setFlag(option, background, !is(option, returnable));
 				}
 				else if(subOption.startsWith("timeout=")) {
 					String num = LibGreenTea.SubString(subOption, "timeout=".length(), subOption.length());
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
 		
 		DShellProcess Process = new DShellProcess(option, cmds, size, timeout);
 		if(Process.IsBackGroundProcess()) {
 			return Process.Detach();
 		}
 		return Process.GetResult(retType);
 	}
 }
 
 class PseudoProcess {
 	public final static int mergeErrorToOut = 0;
 	public final static int mergeOutToError = 1;
 
 	protected PseudoProcess pipedPrevProc;
 
 	protected OutputStream stdin = null;
 	protected InputStream stdout = null;
 	protected InputStream stderr = null;
 
 	protected StringBuilder cmdNameBuilder;
 	protected ArrayList<String> commandList;
 
 	protected boolean stdoutIsDirty = false;
 	protected boolean stderrIsDirty = false;
 
 	protected int mergeType = -1;
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
 
 	public void setMergeType(int mergeType) {
 		this.mergeType = mergeType;
 	}
 
 	public void start() {
 	}
 
 	public void pipe(PseudoProcess srcProc) {
 		this.pipedPrevProc = srcProc;
 		new PipeStreamHandler(srcProc.accessOutStream(), this.stdin, true).start();
 	}
 
 	public void kill() {
 	}
 
 	public void waitFor() {
 	}
 
 	public void waitResult(boolean isPrintable) {
 	}
 	
 	public void showResult() {
 	}
 
 	public String getStdout() {
 		return "";
 	}
 
 	public String getStderr() {
 		return "";
 	}
 
 	public InputStream accessOutStream() {
 		if(!this.stdoutIsDirty) {
 			this.stdoutIsDirty = true;
 			return this.stdout;
 		}
 		return null;
 	}
 
 	public InputStream accessErrorStream() {
 		if(!this.stderrIsDirty) {
 			this.stderrIsDirty = true;
 			return this.stderr;
 		}
 		return null;
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
 	public final static int traceBackend_strace = 0;
 	public final static int traceBackend_strace_plus = 1;
 	public static int traceBackendType = traceBackend_strace;
 
 	private final static String logdirPath = "/tmp/strace-log";
 	private static int logId = 0;
 
 	private Process proc;
 	private boolean enableSyscallTrace = false;
 	public boolean isKilled = false;
 	public String logFilePath = null;
 
 	private ByteArrayOutputStream outBuf;
 
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
 
 	public static void deleteLogFile(String logFilePath) {
 		new File(logFilePath).delete();
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
 
 			String[] traceCmd;
 			if(traceBackendType == traceBackend_strace) {
 				String[] backend_strace = {"strace", "-t", "-f", "-F", "-o", logFilePath};
 				traceCmd = backend_strace;
 			}
 			else if(traceBackendType == traceBackend_strace_plus) {
 				String[] backend_strace_plus = {"strace+", "-k", "-t", "-f", "-F", "-o", logFilePath};
 				traceCmd = backend_strace_plus;
 			}
 			else {
 				throw new RuntimeException("invalid trace backend type");
 			}
 			
 			for(int i = 0; i < traceCmd.length; i++) {
 				this.commandList.add(traceCmd[i]);
 			}
 		}
 	}
 
 	@Override public void setArgument(String[] Args) {
 		String arg = Args[0];
 		this.cmdNameBuilder.append(arg + " ");
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
 		
 		for(int i = 1; i < Args.length; i++) {
 			this.setArgument(Args[i]);
 		}
 	}
 
 	@Override public void start() {
 		int size = this.commandList.size();
 		String[] cmd = new String[size];
 		for(int i = 0; i < size; i++) {
 			cmd[i] = this.commandList.get(i);
 		}
 
 		try {
 			ProcessBuilder procBuilder = new ProcessBuilder(cmd);
 			if(this.mergeType == mergeErrorToOut || this.mergeType == mergeOutToError) {
 				procBuilder.redirectErrorStream(true);
 			}
 			this.proc = procBuilder.start();
 			this.stdin = this.proc.getOutputStream();
 			if(this.mergeType == mergeOutToError) {
 				this.stdout = this.proc.getErrorStream();
 				this.stderr = this.proc.getInputStream();
 			}
 			else {
 				this.stdout = this.proc.getInputStream();
 				this.stderr = this.proc.getErrorStream();
 			}
 		}
 		catch (IOException e) {
 			throw new RuntimeException(e);
 		}
 	}
 
 	@Override public void waitResult(boolean isPrintable) {
 		InputStream inStream = this.accessOutStream();
 		OutputStream outStream;
 		boolean closeStream;
 		if(isPrintable) {
 			outStream = System.out;
 			closeStream = false;
 		}
 		else {
 			outBuf = new ByteArrayOutputStream();
 			outStream = outBuf;
 			closeStream = true;
 		}
 		
 		PipeStreamHandler stdoutHandler = new PipeStreamHandler(inStream, outStream, closeStream);
 		stdoutHandler.start();
 		try {
 			stdoutHandler.join();
 		}
 		catch (InterruptedException e) {
 			throw new RuntimeException(e);
 		}
 	}
 	
 	@Override public void showResult() {
 		new PipeStreamHandler(this.accessOutStream(), System.out, false).start();
 	}
 
 	@Override public String getStdout() {
 		return this.outBuf == null ? "" : this.outBuf.toString();
 	}
 
 	@Override public void waitFor() {
 		try {
 			this.retValue = this.proc.waitFor();
 		}
 		catch (InterruptedException e) {
 			e.printStackTrace();
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
 			LibGreenTea.print("[killed]: " + this.getCmdName());
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
 	private boolean postscriptMode = false;
 	public boolean errorRedirectMode = false;
 	@Override public void start() {
 		String fileName = this.commandList.get(1);
 		try {
 			this.stdin = new BufferedOutputStream(new FileOutputStream(fileName, this.postscriptMode));
 		} 
 		catch (FileNotFoundException e) {
 			e.printStackTrace();
 		}
 	}
 
 	@Override public void pipe(PseudoProcess srcProc) {
 		InputStream srcStream;
 		this.pipedPrevProc = srcProc;
 		if(errorRedirectMode) {
 			srcStream = srcProc.accessErrorStream();
 			if(srcProc instanceof OutRedirectProc && !((OutRedirectProc) srcProc).errorRedirectMode) {
 				srcStream = srcProc.pipedPrevProc.accessErrorStream();
 			}
 		}
 		else {
 			srcStream = srcProc.accessOutStream();
 			if(srcProc instanceof OutRedirectProc && ((OutRedirectProc) srcProc).errorRedirectMode) {
 				srcStream = srcProc.pipedPrevProc.accessOutStream();
 			}
 		}
 		new PipeStreamHandler(srcStream, this.stdin, true).start();
 	}
 
 	@Override public int getRet() {
 		return this.pipedPrevProc.getRet();
 	}
 
 	public void enablePostscriptMode() {
 		this.postscriptMode = true;
 	}
 
 	public void enableErrorRedirectMode() {
 		this.errorRedirectMode = true;
 	}
 }
 
 class EmptyProc extends PseudoProcess {
 	@Override public void pipe(PseudoProcess srcProc) {
 		this.pipedPrevProc = srcProc;
 		this.stdout = srcProc.stdout;
 		this.stderr = srcProc.stderr;
 	}
 
 	@Override public void waitResult(boolean isExpr) {
 		this.pipedPrevProc.waitResult(isExpr);
 	}
 
 	@Override public void showResult() {
 		this.pipedPrevProc.showResult();
 	}
 
 	@Override public String getStdout() {
 		return this.pipedPrevProc.getStdout();
 	}
 
 	@Override public String getStderr() {
 		return this.pipedPrevProc.getStderr();
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
 		LibGreenTea.println("processes are time out!!");
 		for(int i = 0; i < this.procs.length; i++) {
 			this.procs[i].kill();
 		}
 	}
 
 	@Override public void run() {
 		this.killProcs();
 	}
 }
 
 class ErrorStreamHandler {
 	private PseudoProcess[] targetProcs;
 	
 	public ErrorStreamHandler(PseudoProcess[] targetProcs) {
 		this.targetProcs = targetProcs;
 	}
 	
 	public void showErrorMessage() {
 		for(int i = 0; i < targetProcs.length; i++) {
 			new PipeStreamHandler(this.targetProcs[i].accessErrorStream(), System.err, false).start();
 		}
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
 		if(this.input == null || this.output == null) {
 			return;
 		}
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
 
 	private Exception createException(String message, String[] syscall) throws Exception {
 		try {
 			if(syscall == null) {
 				return new NoRelatedSyscallException(message);
 			}
 			return ErrorToException.valueOf(syscall[2]).toException(message, syscall[0], syscall[1]);
 		}
 		catch (IllegalArgumentException e) {
 			return new Exception((syscall[2] + " is not syscall!!"));
 		}
 	}
 }
 
 enum Syscall {
 	open, openat, connect,
 }
 
 enum ErrorToException {
 	E2BIG {
 		public Exception toException(String message, String syscallName, String param) {
 			return new TooManyArgsException(message);
 		}
 	}, 
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
 	ECHILD {
 		public Exception toException(String message, String syscallName, String param) {
 			return new NoChildException(message);
 		}
 	}, 
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
 	EISDIR {
 		public Exception toException(String message, String syscallName, String param) {
 			return new IsDirectoryException(message);
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
 	ENOTDIR {
 		public Exception toException(String message, String syscallName, String param) {
 			return new NotDirectoryException(message);
 		}
 	}, 
 	ENOTEMPTY, 
 	ENOTSOCK, 
 	ENOTSUP, 
 	ENOTTY {
 		public Exception toException(String message, String syscallName, String param) {
 			return new IllegalIOOperateException(message);
 		}
 	}, 
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
