 /**
  * File PlayerConsole.java
  * ---------------------------------------------------------
  *
  * Copyright (C) 2012 Martin Braun (martinbraun123@aol.com)
  *
  * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"),
  * to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense,
  * and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
  *
  * - The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
  * - The origin of the software must not be misrepresented.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
  * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
  *
  * TL;DR: As long as you clearly give me credit for this Software, you are free to use as you like, even in commercial software, but don't blame me
  *   if it breaks something.
  */
 package de.hotware.puremp3.console;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.PrintStream;
 import java.net.URL;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Scanner;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.TimeUnit;
 import java.util.regex.Pattern;
 
 import javax.sound.sampled.DataLine;
 import javax.sound.sampled.FloatControl;
 
 import de.hotware.hotsound.audio.data.IAudioDevice;
 import de.hotware.hotsound.audio.data.IPlaybackAudioDevice;
 import de.hotware.hotsound.audio.data.SavingAudioDevice;
 import de.hotware.hotsound.audio.player.BasicSong;
 import de.hotware.hotsound.audio.player.SavingSong;
 import de.hotware.hotsound.audio.player.IMusicListener;
 import de.hotware.hotsound.audio.player.MusicPlayerException;
 import de.hotware.hotsound.audio.playlist.IPlaylistParser;
 import de.hotware.hotsound.audio.playlist.StockParser;
 import de.hotware.puremp3.console.ICommand.ExecutionException;
 import de.hotware.puremp3.console.ICommand.UsageException;
 
 public class PlayerConsole implements Runnable {
 
 	private IListMusicPlayer mMusicPlayer;
 	private PrintStream mPrintStream;
 	private Map<String, ICommand> mCommands;
 	private Map<String, IPlaylistParser> mPlaylistParsers;
 	private Scanner mScanner;
 	private Pattern mSplitPattern;
 	private ExecutorService mExecutorService;
 	private ExecutorService mPlayerExecutorService;
 	private boolean mStop;
 
 	public PlayerConsole(ExecutorService pExecutorService,
 			ExecutorService pPlayerExecutorService,
 			PrintStream pPrintStream,
 			InputStream pInputStream) {
 		this.mExecutorService = pExecutorService;
 		this.mPlayerExecutorService = pPlayerExecutorService;
 		this.mPrintStream = pPrintStream;
 		this.mCommands = new HashMap<String, ICommand>();
 		this.mPlaylistParsers = new HashMap<String, IPlaylistParser>();
 		this.mScanner = new Scanner(pInputStream);
 		for(BasicCommand cmd : BasicCommand.values()) {
 			for(String key : cmd.getKeys()) {
 				if(this.mCommands.containsKey(key)) {
 					throw new IllegalStateException("command key was being used twice");
 				}
 				this.mCommands.put(key, cmd);
 			}
 			cmd.setConsole(this);
 		}
 		for(IPlaylistParser parser : StockParser.values()) {
 			for(String key : parser.getKeys()) {
 				if(this.mPlaylistParsers.containsKey(key)) {
 					throw new IllegalStateException("parser key was being used twice");
 				}
 				this.mPlaylistParsers.put(key, parser);
 			}
 		}
 		this.mSplitPattern = Pattern.compile("\\s");
 		this.mStop = false;
 	}
 
 	@Override
 	public void run() {
 		while(!this.mStop) {
 			String input = this.mScanner.nextLine();
 			String[] args = this.mSplitPattern.split(input);
 			ICommand cmd = this.mCommands.get(args[0]);
 			try {
 				if(cmd != null) {
 					cmd.execute(args);
 				} else {
 					BasicCommand.HELP.execute(new String[] {});
 				}
 			} catch(UsageException e) {
 				this.mPrintStream.println(e.getCommand().usage());
 			} catch(InterruptedException
 					| MusicPlayerException
 					| IOException
 					| RuntimeException e) {
 				e.printStackTrace(this.mPrintStream);
 			}
 		}
 		try {
 			this.cleanUp();
 		} catch(MusicPlayerException e) {
 			e.printStackTrace(this.mPrintStream);
 		}
 		this.mExecutorService.shutdown();
 		this.mPlayerExecutorService.shutdown();
 	}
 
 	public void runOnConsoleThread(Runnable pRunnable) {
 		try {
 			this.mExecutorService.execute(pRunnable);
 		} catch(ExecutionException e) {
 			e.printStackTrace(this.mPrintStream);
 		}
 	}
 
 	private void initPlayer(final IAudioDevice pAudioDevice) {
 		this.mMusicPlayer = new ListStreamMusicPlayer(new IMusicListener() {
 
 			@Override
			public void onEnd(MusicEndEvent pEvent) {
 				try {
 					if(PlayerConsole.this.mMusicPlayer.size() > 1) {
 						PlayerConsole.this.mMusicPlayer.next();
 					}
 				} catch(MusicPlayerException e) {
 					PlayerConsole.this.runOnConsoleThread(new Runnable() {
 
 						@Override
 						public void run() {
 							IListMusicPlayer player = PlayerConsole.this.mMusicPlayer;
 							boolean fail = true;
 							while(player.size() > 0 && fail) {
 								player.removeAt(player.getCurrent());
 								try {
 									player.next();
 									fail = false;
 								} catch(MusicPlayerException e) {
 									e.printStackTrace(PlayerConsole.this.mPrintStream);
 								}
 							}
 						}
 
 					});
 				}
 			}
 
			@Override
			public void onExeption(MusicExceptionEvent pEvent) {
					pEvent.getException().printStackTrace(PlayerConsole.this.mPrintStream);
			}

 		},
 			this.mPlayerExecutorService);
 	}
 	
 
 	private void cleanUp() throws MusicPlayerException {
 		if(this.mMusicPlayer != null) {
 			this.mMusicPlayer.close();
 		}
 	}
 
 	public static interface ConsoleRunnable extends Runnable {
 
 		public void run() throws ExecutionException;
 
 	}
 
 	/**
 	 * TODO: usage
 	 */
 	public enum BasicCommand implements ICommand {
 		PLAY("play") {
 
 			@Override
 			public void execute(String... pArgs) throws UsageException,
 					ExecutionException,
 					IOException,
 					MusicPlayerException {
 				int length = pArgs.length;
 				if(length < 1) {
 					throw new UsageException("play was used in a wrong way",
 							this);
 				}
 				if(this.mConsole.mMusicPlayer == null) {
 					this.mConsole.initPlayer(null);
 				}
 				if(length == 1) {
 					this.mConsole.mMusicPlayer.unpause();
 				} else {
 					String first = pArgs[1];
 					boolean error = false;
 					if(first.equals("-list")) {
 						error = length < 4;
 						if(!error) {
 							String playlistType = pArgs[2];
 							String playlistLocation = pArgs[3];
 							IPlaylistParser parser = this.mConsole.mPlaylistParsers
 									.get(playlistType);
 							if(parser == null) {
 								throw new IllegalArgumentException("playlist type " +
 										playlistType + " not supported");
 							}
 							try {
 								this.mConsole.mMusicPlayer.setPlaylist(parser
 										.parse(new URL(playlistLocation)));
							} catch(MusicPlayerException | IOException e) {
 								throw new ExecutionException(e, this);
 							}
 							if(this.mConsole.mMusicPlayer.size() == 0) {
 								this.mConsole.mPrintStream
 										.println("No songs to play!");
 							} else if(this.mConsole.mMusicPlayer.isStopped()) {
 								this.mConsole.mMusicPlayer.start();
 							}
 						}
 					} else {
 						IAudioDevice audioDevice = null;
 						BasicSong song = null;
 						String insertionString = "";
 						if(first.equals("-url")) {
 							error = length < 3;
 							if(!error) {
 								insertionString = pArgs[2];
 							}
 							if(length > 3) {
 								if(pArgs[3].equals("-save")) {
 									error = length < 5;
 									if(!error) {
 										audioDevice = new SavingAudioDevice(new File(pArgs[4]));
 									}
 								}
 								song = new SavingSong(new URL(insertionString));
 							}
 						} else {
 							insertionString = "file:" + pArgs[1];
 						}
 						if(song == null) {
 							song = new BasicSong(new URL(insertionString));
 						}
 						try {
 							if(audioDevice == null) {
 								this.mConsole.mMusicPlayer.insert(song);
 							} else {
 								this.mConsole.mMusicPlayer.insert(song,
 										audioDevice);
 							}
						} catch(MusicPlayerException e) {
 							throw new ExecutionException(e, this);
 						}
 						if(this.mConsole.mMusicPlayer.isStopped()) {
 							this.mConsole.mMusicPlayer.start();
 						}
 					}
 					if(error) {
 						throw new UsageException("play was used in a wrong way",
 								this);
 					}
 				}
 			}
 
 		},
 		PAUSE("pause") {
 
 			@Override
 			public void execute(String... pArgs) {
 				this.mConsole.mMusicPlayer.pause();
 			}
 
 		},
 		STOP("stop") {
 
 			@Override
 			public void execute(String... pArgs) throws MusicPlayerException {
 				this.mConsole.mMusicPlayer.stop();
 			}
 
 		},
 		EXIT("exit") {
 
 			@Override
 			public void execute(String... pArgs) throws MusicPlayerException,
 					InterruptedException {
 				if(this.mConsole.mMusicPlayer != null &&
 						!this.mConsole.mMusicPlayer.isStopped()) {
 					this.mConsole.mMusicPlayer.stop();
 					this.mConsole.mExecutorService.shutdown();
 					this.mConsole.mExecutorService.awaitTermination(1000, TimeUnit.MINUTES);
 				}
 				this.mConsole.mStop = true;
 			}
 
 		},
 		EMPTY("") {
 
 			@Override
 			public void execute(String... pArgs) {
 
 			}
 
 		},
 		HELP("help") {
 
 			@Override
 			public void execute(String... pArgs) {
 
 			}
 
 		},
 		VOLUME("volume", "vol") {
 
 			@Override
 			public void execute(String... pArgs) throws UsageException {
 				int length = pArgs.length;
 				DataLine dataLine = ((IPlaybackAudioDevice) this.mConsole.mMusicPlayer
 						.getAudioDevice()).getDataLine();
 				if(dataLine.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
 					FloatControl floatControl = (FloatControl) dataLine
 							.getControl(FloatControl.Type.MASTER_GAIN);
 					if(length == 1) {
 						this.mConsole.mPrintStream.println("Volume: " +
 								floatControl.getValue() + ", Min: " +
 								floatControl.getMinimum() + ", Max: " +
 								floatControl.getMaximum());
 					} else if(length == 2) {
 						float min = floatControl.getMinimum();
 						float max = floatControl.getMaximum();
 						float val = Integer.parseInt(pArgs[1]);
 						if(val < min) {
 							val = min;
 						} else if(val > max) {
 							val = max;
 						}
 						floatControl.setValue(val);
 					}
 				} else {
 					this.mConsole.mPrintStream
 							.println("Volume changes not supported on your platform!");
 				}
 			}
 
 		};
 
 		protected String[] mKeys;
 		protected PlayerConsole mConsole;
 
 		private BasicCommand(String... pKeys) {
 			this.mKeys = pKeys;
 		}
 
 		public void setConsole(PlayerConsole pConsole) {
 			this.mConsole = pConsole;
 		}
 
 		@Override
 		public String usage() {
 			return "lol";
 		}
 
 		@Override
 		public String[] getKeys() {
 			return this.mKeys;
 		}
 
 	}
 
 	public static void main(String args[]) {
 		ExecutorService exec = Executors.newSingleThreadExecutor();
 		ExecutorService exec2 = Executors.newSingleThreadExecutor();
 		PlayerConsole console = new PlayerConsole(exec, exec2, System.out, System.in);
 		console.run();
 	}
 
 }
