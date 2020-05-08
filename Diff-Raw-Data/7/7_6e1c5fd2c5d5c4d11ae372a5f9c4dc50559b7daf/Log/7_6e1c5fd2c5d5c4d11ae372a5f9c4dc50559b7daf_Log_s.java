 package pleocmd;
 
 import pleocmd.itfc.gui.MainFrame;
 
 public final class Log {
 
 	public enum Type {
 		Detail, Info, Warn, Error, Console
 	}
 
 	private static final boolean PRINT_DETAIL = true;
 
 	private final Type type;
 
 	private final String caller;
 
 	private final String msg;
 
 	private Log(final Type type, final String caller, final String msg) {
 		this.type = type;
 		this.caller = caller;
 		this.msg = msg;
 	}
 
 	public Type getType() {
 		return type;
 	}
 
 	public String getCaller() {
 		return caller;
 	}
 
 	public String getMsg() {
 		return msg;
 	}
 
 	public String getTypeColor() {
 		switch (type) {
 		case Detail:
 			return "#A0A0A0"; // gray
 		case Info:
 			return "#0000FF"; // blue
 		case Warn:
 			return "#FFA020"; // orange
 		case Error:
 			return "#FF0000"; // red
 		case Console:
 		default:
 			return "#000000"; // black
 		}
 	}
 
 	public String getTypeShortString() {
 		switch (type) {
 		case Detail:
 			return "DTL";
 		case Info:
 			return "INF";
 		case Warn:
 			return "WRN";
 		case Error:
 			return "ERR";
 		case Console:
 			return " > ";
 		default:
 			return "!?!";
 		}
 	}
 
 	@Override
 	public String toString() {
 		return String.format("%s %-50s %s", getTypeShortString(), caller, msg);
 	}
 
 	/**
 	 * Print messages to the GUI's log, if any, or otherwise to the standard
 	 * error.<br>
 	 * Always print messages of type Console to the standard output (instead of
 	 * standard error) no matter if a GUI exists or not.
 	 */
 	private void process() {
 		if (type != Type.Detail || PRINT_DETAIL) {
 			if (type == Type.Console) System.out.println(getMsg()); // CS_IGNORE
 			if (MainFrame.hasGUI())
 				MainFrame.the().addLog(this);
 			else if (type != Type.Console) System.err.println(toString()); // CS_IGNORE
 		}
 	}
 
 	private static String getCallersName(final int stepsBack) {
 		return getCallersName(new Throwable(), stepsBack);
 	}
 
 	private static String getCallersName(final Throwable t, final int stepsBack) {
 		final StackTraceElement[] st = t.getStackTrace();
 		return String.format("%s.%s()", st.length < stepsBack ? "???"
 				: st[stepsBack].getClassName().replaceFirst("^.*\\.([^.]*)$",
 						"$1"), st[stepsBack].getMethodName());
 	}
 
 	private static void msg(final Type type, final String msg,
 			final Object... args) {
 		new Log(type, getCallersName(3), args.length == 0 ? msg : String
 				.format(msg, args)).process();
 	}
 
 	public static void detail(final String msg, final Object... args) {
 		msg(Type.Detail, msg, args);
 	}
 
 	public static void info(final String msg, final Object... args) {
 		msg(Type.Info, msg, args);
 	}
 
 	public static void warn(final String msg, final Object... args) {
 		msg(Type.Warn, msg, args);
 	}
 
 	public static void error(final String msg, final Object... args) {
 		msg(Type.Error, msg, args);
 	}
 
 	public static void consoleOut(final String msg, final Object... args) {
 		msg(Type.Console, msg, args);
 	}
 
 	public static void error(final Throwable throwable) {
 		error(throwable, "");
 	}
 
 	public static void error(final Throwable throwable, final String msg,
 			final Object... args) {
		final StringBuilder sb = new StringBuilder(args.length == 0 ? msg
				: String.format(msg, args));
 		Throwable t = throwable;
 		while (true) {
 			sb.append(t.getClass().getSimpleName());
 			sb.append(": '");
 			sb.append(t.getMessage());
 			sb.append("'");
 			if (t.getCause() == null) break;
 			t = t.getCause();
 			sb.append(" caused by ");
 		}
 
 		new Log(Type.Error, getCallersName(t, 0), sb.toString()).process();
 	}
 
 }
