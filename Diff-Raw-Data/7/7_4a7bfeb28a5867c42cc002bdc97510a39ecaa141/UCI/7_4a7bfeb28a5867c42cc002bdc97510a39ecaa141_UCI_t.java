 package sf.pnr.io;
 
 import sf.pnr.base.BestMoveListener;
 import sf.pnr.base.Board;
 import sf.pnr.base.Configurable;
 import sf.pnr.base.Configuration;
 import sf.pnr.base.StringUtils;
 
 import java.io.BufferedReader;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.OutputStream;
 import java.io.PrintStream;
 import java.lang.reflect.Method;
 import java.lang.reflect.UndeclaredThrowableException;
 import java.util.concurrent.Callable;
 import java.util.concurrent.ExecutionException;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.Future;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 /**
  */
 public class UCI implements UciProcess {
 
     private static final int CANCEL_THRESHOLD = 50;
     private static final Pattern POSITION_PATTERN = Pattern.compile(
         "(fen [1-8/pnbrqkPNBRQK]+ [wb] [KQkq-]+ [-a-h1-8]+ [0-9]+ [0-9]+|startpos)( moves( [a-h][1-8][a-h][1-8][nbrq]?)+)?");
 
     private static boolean verbose = false;
 
     private enum State {START, UCI_RECEIVED, SEARCHING, SEARCHING_PONDER}
 
     private final ExecutorService threadPool = Executors.newCachedThreadPool();
     private final BufferedReader in;
     private final InputStream inputStream;
     private final PrintStream out;
     private UciBestMoveListener uciListener;
     private final PawnsNRoses chess;
     private Future<String> future;
     private volatile State state = State.START;
 
     public UCI(final InputStream in, final OutputStream out) {
         inputStream = in;
         this.in = new BufferedReader(new InputStreamReader(in));
         this.out = new PrintStream(out, true);
         chess = new PawnsNRoses();
         uciListener = new UciBestMoveListener(this.out, true);
         chess.setBestMoveListener(uciListener);
     }
 
     public static void main(final String[] args) throws IOException, ExecutionException, InterruptedException {
         final Configuration config = Configuration.getInstance();
         final String configurationFile = System.getProperty("configuration.file");
         if (configurationFile != null) {
             config.loadFromFile(configurationFile);
         }
         config.loadFromSystemProperties();
 
         final OutputStream os;
         if (args.length > 0) {
             os = new TeeOutputStream(System.out, new FileOutputStream(args[0], true));
         } else {
             os = System.out;
         }
 
         MultiInputStream is = new MultiInputStream();
         if (System.getProperty("startup.file") != null) {
             is.addInputStream(new FileInputStream(System.getProperty("startup.file")));
         }
         is.addInputStream(System.in);
         final UCI protocol = new UCI(is, os);
         protocol.run();
     }
 
     public void run() throws IOException, ExecutionException, InterruptedException {
         while (true) {
             String line = in.readLine().trim();
             if ("uci".equals(line)) {
                 assert state == State.START;
                 state = State.UCI_RECEIVED;
                 out.println("id name Pawns N' Roses");
                 out.println("id author George Koch");
                 out.println("uciok");
                 final Configuration config = Configuration.getInstance();
                 for (Configurable.Key key: Configurable.Key.values()) {
                     out.printf("option name %s type %s default %s\r\n", toUciOption(key),
                         toUciType(config.getType(key)), config.getString(key));
                 }
             } else if (line.startsWith("debug ")) {
                 uciListener.setDebug("on".equals(line.substring(6).trim()));
             } else if ("isready".equals(line)) {
                 ensureReady();
                 print("readyok");
             } else if (line.startsWith("setoption ")) {
                 final String content = line.substring(10).trim();
                 final String name;
                 final String value;
                 if (content.startsWith("name ")) {
                     final int pos = content.indexOf(" value ");
                     if (pos == -1) {
                         name = content.substring(5).trim();
                         value = "";
                     } else {
                         name = content.substring(5, pos).trim();
                         value = content.substring(pos + 7).trim();
                     }
                 } else if (content.startsWith("value ")) {
                     final int pos = content.indexOf(" name ");
                     if (pos == -1) {
                         name = "";
                         value = "";
                     } else {
                         value = content.substring(6, pos).trim();
                         name = content.substring(pos + 6).trim();
                     }
                 } else {
                     name = "";
                     value = "";
                 }
                 if (name.length() == 0) {
                     // missing name
                     continue;
                 }
                 if ("Command".equals(name)) {
                     executeCommand(value);
                 } else {
                     final String keyStr = fromUciOption(name);
                     final Configurable.Key key = Configuration.getKey(keyStr);
                     Configuration.getInstance().setProperty(key, value);
                 }
             } else if ("ucinewgame".equals(line)) {
                 ensureReady();
                 chess.restart();
             } else if (line.startsWith("position ")) {
                 ensureReady();
                 final String position = line.substring(9).trim();
                 final Matcher matcher = POSITION_PATTERN.matcher(position);
                 if (matcher.matches()) {
                     final String type = matcher.group(1);
                     if (type.equals("startpos")) {
                         chess.restartBoard();
                     } else if (type.startsWith("fen")) {
                         chess.setBoard(type.substring(4));
                     }
                     if (matcher.groupCount() > 2) {
                         final String groupStr = matcher.group(2);
                         if (groupStr != null && groupStr.length() > 0) {
                             final String movesStr = groupStr.substring(6).trim();
                             final String moves[] = movesStr.split(" ");
                             final Board board = chess.getBoard();
                             for (String move: moves) {
                                 board.move(StringUtils.fromLong(board, move.trim()));
                             }
                         }
                     }
                 }
             } else if (line.startsWith("go ")) {
                 ensureReady();
                 processGo(line.substring(3).trim());
             } else if ("stop".equals(line)) {
                 ensureReady();
             } else if ("ponderhit".equals(line)) {
                 if (state == State.SEARCHING_PONDER) {
                     state = State.SEARCHING;
                 }
             } else if ("quit".equals(line)) {
                 break;
             }
         }
         threadPool.shutdownNow();
     }
 
     private void processGo(final String paramsStr) throws ExecutionException, InterruptedException {
         final String[] params = paramsStr.split(" ");
         state = State.SEARCHING;
         int timeWhite = -1;
         int timeBlack = -1;
         int incrementWhite = -1;
         int incrementBlack = -1;
         int movesToGo = -1;
         int depth = 0;
         int time = -1;
         for (int i = 0; i < params.length; i++) {
             final String param = params[i];
             if (param.equals("searchmoves")) {
                 // not supported yet
             } else if (param.equals("ponder")) {
                 state = State.SEARCHING_PONDER;
                 // ponder move not supported
             } else if (param.equals("wtime")) {
                 timeWhite = Integer.parseInt(params[i + 1]);
                 i++;
             } else if (param.equals("btime")) {
                 timeBlack = Integer.parseInt(params[i + 1]);
                 i++;
             } else if (param.equals("winc")) {
                 incrementWhite = Integer.parseInt(params[i + 1]);
                 i++;
             } else if (param.equals("binc")) {
                 incrementBlack = Integer.parseInt(params[i + 1]);
                 i++;
             } else if (param.equals("movestogo")) {
                 movesToGo = Integer.parseInt(params[i + 1]);
                 i++;
             } else if (param.equals("depth")) {
                 depth = Integer.parseInt(params[i + 1]);
                 i++;
             } else if (param.equals("nodes")) {
                 // not supported
             } else if (param.equals("mate")) {
                 // not supported
             } else if (param.equals("movetime")) {
                 time = Integer.parseInt(params[i + 1]);
                 i++;
             } else if (param.equals("infinite")) {
                 time = Integer.MAX_VALUE;
             }
         }
         ensureReady();
         int searchTime = 0;
         boolean whiteToMove = chess.isWhiteToMove();
         if (time >= 0) {
             searchTime = time;
         } else if (whiteToMove && timeWhite >= 0) {
             final TimeControl timeControl = getTimeControl(timeWhite, incrementWhite, movesToGo);
             searchTime = timeControl.getNextMoveTime();
         } else if (!whiteToMove && timeBlack >= 0) {
             final TimeControl timeControl = getTimeControl(timeBlack, incrementBlack, movesToGo);
             searchTime = timeControl.getNextMoveTime();
         }
         chess.setDepth(depth);
         if (searchTime != 0) {
             chess.setTime(Math.max(searchTime - CANCEL_THRESHOLD, 10));
         }
         future = threadPool.submit(new Callable<String>() {
             @Override
             public String call() throws Exception {
                 try {
                     if (state == State.SEARCHING_PONDER) {
                         out.println("bestmove ponder");
                         return "ponder";
                     } else {
                         final int move = chess.move();
                         final String moveStr = StringUtils.toLong(move);
                         out.println("bestmove " + moveStr);
                         return moveStr;
                     }
                 } catch (Exception e) {
                     out.println("info string " + e.getMessage());
                     e.printStackTrace();
                     throw e;
                 } catch (Error e) {
                     out.println("info string " + e.getMessage());
                     e.printStackTrace();
                     throw e;
                 }
             }
         });
     }
 
     private TimeControl getTimeControl(final int timeLeft, final int increment, final int movesToGo) {
         if (increment > 0) {
             if (movesToGo > 0) {
                 return new IncrementalTimeControl(timeLeft, increment, movesToGo);
             } else {
                 return new IncrementalTimeControl(timeLeft, increment);
             }
         } else {
             if (movesToGo > 0) {
                 return new ConventionalTimeControl(movesToGo, timeLeft);
             } else {
                 return new ConventionalTimeControl(timeLeft);
             }
         }
     }
 
     private void executeCommand(final String fullCommand) {
         final String[] parts = fullCommand.split(" ", 2);
         final String command = parts[0];
         try {
             final String[] args;
             if (parts.length > 1 && parts[1].trim().length() > 0) {
                 args = parts[1].split(",", -1);
             } else {
                 args = new String[0];
             }
             final Method method = getMethod(command, args.length);
             final Executable annotation = method.getAnnotation(Executable.class);
             method.invoke(chess, getArgs(annotation, args));
         } catch (Exception e) {
            out.printf("info string Failed to execute command '%s': %s\r\n", fullCommand, e.getMessage());
         }
     }
 
     private Object[] getArgs(final Executable annotation, final String[] args) {
         final Object[] result = new Object[args.length];
         final Class<?>[] types = annotation.values();
         for (int i = 0, length = types.length; i < length; i++) {
             final Class<?> clazz = types[i];
             if (int.class.equals(clazz)) {
                 result[i] = Integer.parseInt(args[i]);
             } else if (String.class.equals(clazz)) {
                 result[i] = args[i];
             } else {
                 throw new IllegalStateException("Unexpected parameter type: " + clazz);
             }
         }
         return result;
     }
 
     private Method getMethod(final String command, final int argCount) throws NoSuchMethodException {
         final Method[] methods = PawnsNRoses.class.getDeclaredMethods();
         for (Method method: methods) {
             if (method.getName().equals(command)) {
                 final Executable annotation = method.getAnnotation(Executable.class);
                 if (annotation != null && annotation.values().length == argCount) {
                     return method;
                 }
             }
         }
         throw new NoSuchMethodException(
             String.format("Couldn't find executable method '%s' with %d arguments", command, argCount));
     }
 
     private void print(final String message) {
         out.println(message);
     }
 
     private void ensureReady() throws InterruptedException, ExecutionException {
         if (future != null) {
             if (verbose) {
                 out.print("info string cancelling game");
             }
             chess.cancel();
             future.get();
             future = null;
         }
     }
 
     @Override
     public OutputStream getOutputStream() {
         return out;
     }
 
     @Override
     public InputStream getInputStream() {
         return inputStream;
     }
 
     @Override
     public void restart() throws IOException {
         throw new UnsupportedOperationException();
     }
 
     @Override
     public void destroy() {
         try {
             threadPool.shutdownNow();
             ensureReady();
         } catch (InterruptedException e) {
             e.printStackTrace();
             throw new UndeclaredThrowableException(e);
         } catch (ExecutionException e) {
             e.printStackTrace();
             throw new UndeclaredThrowableException(e);
         }
     }
 
     public static String toUciOption(final Configurable.Key key) {
         final String keyStr = key.getKey();
         final StringBuilder builder = new StringBuilder(keyStr.length());
         boolean upperCase = true;
         for (int i = 0; i < keyStr.length(); i++) {
             final char ch = keyStr.charAt(i);
             if (ch == '.') {
                 builder.append(' ');
                 upperCase = true;
             } else if (upperCase) {
                 builder.append(Character.toUpperCase(ch));
                 upperCase = false;
             } else {
                 builder.append(ch);
                 upperCase = false;
             }
         }
         return builder.toString();
     }
 
     public static String fromUciOption(final String key) {
         final StringBuilder builder = new StringBuilder(key.length());
         boolean lowerCase = true;
         for (int i = 0; i < key.length(); i++) {
             final char ch = key.charAt(i);
             if (ch == ' ') {
                 builder.append('.');
                 lowerCase = true;
             } else if (lowerCase) {
                 builder.append(Character.toLowerCase(ch));
                 lowerCase = false;
             } else {
                 builder.append(ch);
                 lowerCase = false;
             }
         }
         return builder.toString();
     }
 
     public static String toUciType(final Class<?> clazz) {
         return "string";
     }
 
     public static class UciBestMoveListener implements BestMoveListener {
         private final PrintStream out;
         private boolean debug = false;
 
         public UciBestMoveListener(final PrintStream out, final boolean debug) {
             this.out = out;
             this.debug = debug;
         }
 
         @Override
         public void bestMoveChanged(final int depth, final int bestMove, final int value, final long time,
                                     final int[] bestLine, final long nodes) {
             if (debug) {
                 final String message = String.format("info depth %d currmove %s score cp %d time %d nps %d pv %s nodes %d",
                     depth, StringUtils.toLong(bestMove), value, time, time > 0? nodes * 1000 / time: 0,
                     StringUtils.toLong(bestLine, " "), nodes);
                 out.println(message);
             }
         }
 
         public void setDebug(final boolean debug) {
             this.debug = debug;
         }
     }
 }
