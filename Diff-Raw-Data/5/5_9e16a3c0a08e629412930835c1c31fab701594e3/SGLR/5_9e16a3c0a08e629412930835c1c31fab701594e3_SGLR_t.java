 package org.spoofax.jsglr.io;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import org.spoofax.interpreter.terms.ITermFactory;
 import org.spoofax.jsglr.client.ITreeBuilder;
 import org.spoofax.jsglr.client.ParseException;
 import org.spoofax.jsglr.client.ParseTable;
 import org.spoofax.jsglr.client.ParseTimeoutException;
 import org.spoofax.jsglr.shared.BadTokenException;
 import org.spoofax.jsglr.shared.SGLRException;
 import org.spoofax.jsglr.shared.TokenExpectedException;
 
 /**
  * @author Lennart Kats <lennart add lclnet.nl>
  */
 public class SGLR extends org.spoofax.jsglr.client.SGLR {
 
 	private static final Timer abortTimer = new Timer(true);
 	
 	private int timeout;
 
 	private int abortTimerJobId;
 
 	/**
 	 * Sets the maximum amount of time to try and parse a file, before a
 	 * {@link ParseTimeoutException} is thrown.
 	 * 
 	 * @param timeout
 	 *            The maximum time to parse, in milliseconds.
 	 */
 	@Override
 	public void setTimeout(int timeout) {
 		this.timeout = timeout;
 	}
 
 	@Override
 	protected void initParseTimer() {
 		if (timeout > 0) {
 			// We use a single shared timer to conserve native threads
 			// and a jobId to recognize stale abort events
 			synchronized (abortTimer) {
 				asyncAborted = false;
 				++abortTimerJobId;
 			}
 			final int jobId = abortTimerJobId;
 			abortTimer.schedule(new TimerTask() {
 				@Override
 				public void run() {
 					synchronized (abortTimer) {
 						if (abortTimerJobId == jobId)
 							asyncCancel();
 					}
 				}
 			}, timeout);
 		} else {
 			asyncAborted = false;
 		}
 	}
 
 	@Deprecated
 	public SGLR(ITermFactory pf, ParseTable parseTable) {
 		super(pf, parseTable);
 	}
 	
 	@Deprecated
 	public SGLR(ParseTable parseTable) {
 		super(parseTable);
 	}
 	
 	public SGLR(ITreeBuilder treeBuilder, ParseTable parseTable) {
 		super(treeBuilder, parseTable);
 	}
 
 	/**
 	 * @deprecated Call {@link #parse(InputStream, String)} instead.
 	 */
 	public final Object parse(InputStream fis)
 			throws BadTokenException, TokenExpectedException, ParseException,
 			SGLRException, SGLRException, IOException {
		return parse(fis, null, null);
 	}
 
 	public final Object parse(InputStream fis, String filename)
 			throws BadTokenException, TokenExpectedException, ParseException,
 			SGLRException, SGLRException, IOException {
		return parse(fis, null, null);
 	}
 
 	public Object parse(InputStream fis, String filename, String startSymbol)
 			throws BadTokenException, TokenExpectedException, ParseException,
 			SGLRException, IOException {
 		String input = FileTools.loadFileAsString(new InputStreamReader(fis));
 		return parse(input, filename, startSymbol);
 	}
 
 }
