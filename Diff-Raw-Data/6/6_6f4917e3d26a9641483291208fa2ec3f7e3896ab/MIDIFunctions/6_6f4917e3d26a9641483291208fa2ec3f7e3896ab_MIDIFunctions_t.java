 package com.hekta.chmidi.functions;
 
 import java.io.File;
 
 import org.bukkit.block.BlockState;
 import org.bukkit.block.NoteBlock;
 import org.bukkit.Location;
 
 import com.laytonsmith.abstraction.MCLocation;
 import com.laytonsmith.abstraction.StaticLayer;
 import com.laytonsmith.annotations.api;
 import com.laytonsmith.core.CHVersion;
 import com.laytonsmith.core.constructs.CArray;
 import com.laytonsmith.core.constructs.CBoolean;
 import com.laytonsmith.core.constructs.CDouble;
 import com.laytonsmith.core.constructs.CInt;
 import com.laytonsmith.core.constructs.CNull;
 import com.laytonsmith.core.constructs.Construct;
 import com.laytonsmith.core.constructs.CString;
 import com.laytonsmith.core.constructs.CVoid;
 import com.laytonsmith.core.constructs.Target;
 import com.laytonsmith.core.environments.Environment;
 import com.laytonsmith.core.environments.GlobalEnv;
 import com.laytonsmith.core.exceptions.ConfigRuntimeException;
 import com.laytonsmith.core.functions.AbstractFunction;
 import com.laytonsmith.core.functions.Exceptions.ExceptionType;
 import com.laytonsmith.core.ObjectGenerator;
 import com.laytonsmith.core.Security;
 import com.laytonsmith.core.Static;
 import com.laytonsmith.PureUtilities.RunnableQueue;
 import com.laytonsmith.PureUtilities.Version;
 
 import com.hekta.chmidi.util.CHMIDINotePlayer;
 import com.hekta.chmidi.util.CHMIDISequencer;
 import com.hekta.chmidi.util.CHMIDISequencerManager;
 
 /*
 *
 * @author Hekta
 */
 public class MIDIFunctions {
 
 	public static String docs() {
 		return "A class of functions that add MIDI features.";
 	}
 
 	@api
 	public static class midi_play extends AbstractFunction {
 
 		RunnableQueue queue;
 		boolean started = false;
 
 		private void startup(){
 			if(!started){
 				queue.invokeLater(null, new Runnable() {
 					public void run() {
 					}
 				});
 				StaticLayer.GetConvertor().addShutdownHook(new Runnable() {
 					public void run() {
 						queue.shutdown();
 						started = false;
 					}
 				});
 				started = true;
 			}
 		}
 
 		public String getName() {
 			return "midi_play";
 		}
 
 		public Integer[] numArgs() {
 			return new Integer[]{2, 3, 4};
 		}
 
 		public ExceptionType[] thrown() {
 			return new ExceptionType[]{ExceptionType.IOException, ExceptionType.SecurityException, ExceptionType.FormatException, ExceptionType.PluginInternalException, ExceptionType.RangeException, ExceptionType.CastException};
 		}
 
 		public boolean isRestricted() {
 			return true;
 		}
 
 		public Boolean runAsync() {
 			return false;
 		}
 
 		public String docs() {
 			return "string {file, location, [id, [playNow]]} Creates a new MIDI sequence from the MIDI file that will be play at the given location, and returns the sequenceID."
 					 + " The block at the location must be a Note Block, the id must be unique, if it is not given or null, an unique id is created, playNow take a boolean,"
 					 + " if false, the sequence is paused and it is not played immediatly, default to true. When a sequence is finished, it is removed."
 					 + " There are somes limitations imposed by Minecraft, the range of pitches is between F#3 to F#5 included, the pan, volume, sustain and others features are ignored,"
 					 + " and Minecraft only contains four instruments. You can get more informations about the MIDI format [http://en.wikipedia.org/wiki/General_MIDI|here].";
 		}
 
 		public Version since() {
 			return CHVersion.V3_3_1;
 		}
 
 		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
 			MCLocation location = ObjectGenerator.GetGenerator().location(args[1], null, t);
 			BlockState blockState = ((Location) location.getHandle()).getBlock().getState();
 			if (!(blockState instanceof NoteBlock)) {
 				throw new ConfigRuntimeException("The block at the specified location is not a note block", ExceptionType.RangeException, t);
 			}
 			NoteBlock noteBlock = (NoteBlock) blockState;
 			File midiFile = new File(t.file().getParentFile(), args[0].val());
 			if (!Security.CheckSecurity(midiFile.getAbsolutePath())) {
 				throw new ConfigRuntimeException("You do not have permission to access the file '" + midiFile.getAbsolutePath() + "'", ExceptionType.SecurityException, t);
 			}
 
 			//id
 			String id;
 			if ((args.length == 2) || (args[2] instanceof CNull)) {
 				id = String.valueOf(System.nanoTime());
 				while (CHMIDISequencerManager.sequencerExists(id)) {
 					id = String.valueOf(System.nanoTime());
 				}
 			} else {
 				if (CHMIDISequencerManager.sequencerExists(args[2].val())) {
 					throw new ConfigRuntimeException("A sequence with the given id ('" + args[2].val() + "') already exists.", ExceptionType.PluginInternalException, t);
 				} else {
 					id = args[2].val();
 				}
 			}
 			//create
 			final CHMIDISequencer sequencer = new CHMIDISequencer(id, midiFile, new CHMIDINotePlayer(noteBlock), t);
 			//playNow
 			final boolean startNow;
 			if (args.length == 4) {
 				startNow = Static.getBoolean(args[3]);
 			} else {
 				startNow = true;
 			}
 
 			CHMIDISequencerManager.addSequencer(sequencer, t);
 			queue = new RunnableQueue("MethodScript-CHMIDI-" + id);
 			queue.invokeLater(environment.getEnv(GlobalEnv.class).GetDaemonManager(), new Runnable() {
 				public void run() {
 					try {
 						sequencer.open();
 						if (startNow) {
 							sequencer.start();
 						}
 						while (sequencer.getMIDISequencer().isRunning() || sequencer.isPaused()) {
 							Thread.sleep(500);
 						}
 					} catch (Exception exception) {
 					} finally {
 						CHMIDISequencerManager.deleteSequencer(sequencer, Target.UNKNOWN);
 					}
 				}
 			});
 
 			return new CString(id, t);
 		}
 	}
 
 	@api
 	public static class midi_stop extends AbstractFunction {
 
 		public String getName() {
 			return "midi_stop";
 		}
 
 		public Integer[] numArgs() {
 			return new Integer[]{1};
 		}
 
 		public ExceptionType[] thrown() {
 			return new ExceptionType[]{ExceptionType.PluginInternalException};
 		}
 
 		public boolean isRestricted() {
 			return true;
 		}
 
 		public Boolean runAsync() {
 			return false;
 		}
 
 		public String docs() {
 			return "void {sequenceID} Stops the playing of the midi sequence and remove it.";
 		}
 
 		public Version since() {
 			return CHVersion.V3_3_1;
 		}
 
 		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
 			CHMIDISequencerManager.deleteSequencer(args[0].val(), t);
 			return new CVoid(t);
 		}
 	}
 
 	@api
 	public static class midi_exists extends AbstractFunction {
 
 		public String getName() {
 			return "midi_exists";
 		}
 
 		public Integer[] numArgs() {
 			return new Integer[]{1};
 		}
 
 		public ExceptionType[] thrown() {
 			return new ExceptionType[]{};
 		}
 
 		public boolean isRestricted() {
 			return true;
 		}
 
 		public Boolean runAsync() {
 			return false;
 		}
 
 		public String docs() {
 			return "boolean {sequenceID} Returns if the sequence with the given ID exists.";
 		}
 
 		public Version since() {
 			return CHVersion.V3_3_1;
 		}
 
 		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
 			return new CBoolean(CHMIDISequencerManager.sequencerExists(args[0].val()), t);
 		}
 	}
 
 	@api
 	public static class midi_all_sequences extends AbstractFunction {
 
 		public String getName() {
 			return "midi_all_sequences";
 		}
 
 		public Integer[] numArgs() {
 			return new Integer[]{0};
 		}
 
 		public ExceptionType[] thrown() {
 			return new ExceptionType[]{};
 		}
 
 		public boolean isRestricted() {
 			return true;
 		}
 
 		public Boolean runAsync() {
 			return false;
 		}
 
 		public String docs() {
 			return "array {} Returns an array containing all sequence IDs.";
 		}
 
 		public Version since() {
 			return CHVersion.V3_3_1;
 		}
 
 		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			CArray sequenceList = new CArray(t);
			for (String sequencerID : CHMIDISequencerManager.getSequencers().keySet()) {
				sequenceList.push(new CString(sequencerID, t));
 			}
 			return sequenceList;
 		}
 	}
 
 	@api
 	public static class midi_pause extends AbstractFunction {
 
 		public String getName() {
 			return "midi_pause";
 		}
 
 		public Integer[] numArgs() {
 			return new Integer[]{1};
 		}
 
 		public ExceptionType[] thrown() {
 			return new ExceptionType[]{ExceptionType.PluginInternalException};
 		}
 
 		public boolean isRestricted() {
 			return true;
 		}
 
 		public Boolean runAsync() {
 			return false;
 		}
 
 		public String docs() {
 			return "void {sequencerID} Pauses the playing of the midi sequence.";
 		}
 
 		public Version since() {
 			return CHVersion.V3_3_1;
 		}
 
 		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
 			CHMIDISequencerManager.getSequencer(args[0].val(), t).setPaused(true);
 			return new CVoid(t);
 		}
 	}
 
 	@api
 	public static class midi_unpause extends AbstractFunction {
 
 		public String getName() {
 			return "midi_unpause";
 		}
 
 		public Integer[] numArgs() {
 			return new Integer[]{1};
 		}
 
 		public ExceptionType[] thrown() {
 			return new ExceptionType[]{ExceptionType.PluginInternalException};
 		}
 
 		public boolean isRestricted() {
 			return true;
 		}
 
 		public Boolean runAsync() {
 			return false;
 		}
 
 		public String docs() {
 			return "void {sequenceID} Unpauses the playing of the midi sequence.";
 		}
 
 		public Version since() {
 			return CHVersion.V3_3_1;
 		}
 
 		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
 			CHMIDISequencerManager.getSequencer(args[0].val(), t).setPaused(false);
 			return new CVoid(t);
 		}
 	}
 
 	@api
 	public static class midi_length extends AbstractFunction {
 
 		public String getName() {
 			return "midi_length";
 		}
 
 		public Integer[] numArgs() {
 			return new Integer[]{1};
 		}
 
 		public ExceptionType[] thrown() {
 			return new ExceptionType[]{ExceptionType.PluginInternalException};
 		}
 
 		public boolean isRestricted() {
 			return true;
 		}
 
 		public Boolean runAsync() {
 			return false;
 		}
 
 		public String docs() {
 			return "int {sequenceID} Returns the length in microseconds of the MIDI sequence.";
 		}
 
 		public Version since() {
 			return CHVersion.V3_3_1;
 		}
 
 		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
 			return new CInt(CHMIDISequencerManager.getSequencer(args[0].val(), t).getMIDISequencer().getMicrosecondLength(), t);
 		}
 	}
 
 	@api
 	public static class midi_position extends AbstractFunction {
 
 		public String getName() {
 			return "midi_position";
 		}
 
 		public Integer[] numArgs() {
 			return new Integer[]{1};
 		}
 
 		public ExceptionType[] thrown() {
 			return new ExceptionType[]{ExceptionType.PluginInternalException};
 		}
 
 		public boolean isRestricted() {
 			return true;
 		}
 
 		public Boolean runAsync() {
 			return false;
 		}
 
 		public String docs() {
 			return "int {sequenceID} Returns the current position in microseconds in the MIDI sequence.";
 		}
 
 		public Version since() {
 			return CHVersion.V3_3_1;
 		}
 
 		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
 			return new CInt(CHMIDISequencerManager.getSequencer(args[0].val(), t).getMIDISequencer().getMicrosecondPosition(), t);
 		}
 	}
 
 	@api
 	public static class midi_set_position extends AbstractFunction {
 
 		public String getName() {
 			return "midi_set_position";
 		}
 
 		public Integer[] numArgs() {
 			return new Integer[]{2};
 		}
 
 		public ExceptionType[] thrown() {
 			return new ExceptionType[]{ExceptionType.PluginInternalException, ExceptionType.RangeException};
 		}
 
 		public boolean isRestricted() {
 			return true;
 		}
 
 		public Boolean runAsync() {
 			return false;
 		}
 
 		public String docs() {
 			return "void {sequenceID, position} Sets the position in the midi sequence, position is in microseconds.";
 		}
 
 		public Version since() {
 			return CHVersion.V3_3_1;
 		}
 
 		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
 			long givenPosition = Static.getInt(args[1], t);
 			if (givenPosition > CHMIDISequencerManager.getSequencer(args[0].val(), t).getMIDISequencer().getMicrosecondLength()) {
 				throw new ConfigRuntimeException("You depassed the length of the MIDI sequence.", ExceptionType.RangeException, t);
 			} else {
 				CHMIDISequencerManager.getSequencer(args[0].val(), t).getMIDISequencer().setMicrosecondPosition(givenPosition);
 			}
 			return new CVoid(t);
 		}
 	}
 
 	@api
 	public static class midi_tempo extends AbstractFunction {
 
 		public String getName() {
 			return "midi_tempo";
 		}
 
 		public Integer[] numArgs() {
 			return new Integer[]{1};
 		}
 
 		public ExceptionType[] thrown() {
 			return new ExceptionType[]{ExceptionType.PluginInternalException};
 		}
 
 		public boolean isRestricted() {
 			return true;
 		}
 
 		public Boolean runAsync() {
 			return false;
 		}
 
 		public String docs() {
 			return "double {sequenceID} Returns the tempo of the midi sequence, in BPM (Beats Per Minute).";
 		}
 
 		public Version since() {
 			return CHVersion.V3_3_1;
 		}
 
 		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
 			return new CDouble(CHMIDISequencerManager.getSequencer(args[0].val(), t).getMIDISequencer().getTempoInBPM(), t);
 		}
 	}
 
 	@api
 	public static class midi_set_tempo extends AbstractFunction {
 
 		public String getName() {
 			return "midi_set_tempo";
 		}
 
 		public Integer[] numArgs() {
 			return new Integer[]{2};
 		}
 
 		public ExceptionType[] thrown() {
 			return new ExceptionType[]{ExceptionType.PluginInternalException, ExceptionType.RangeException};
 		}
 
 		public boolean isRestricted() {
 			return true;
 		}
 
 		public Boolean runAsync() {
 			return false;
 		}
 
 		public String docs() {
 			return "void {sequenceID, tempo} Sets the tempo of the midi sequence, a double, in BPM (Beats Per Minute).";
 		}
 
 		public Version since() {
 			return CHVersion.V3_3_1;
 		}
 
 		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
 			CHMIDISequencerManager.getSequencer(args[0].val(), t).getMIDISequencer().setTempoInBPM(Static.getDouble32(args[1], t));
 			return new CVoid(t);
 		}
 	}
 
 	@api
 	public static class midi_repeat_count extends AbstractFunction {
 
 		public String getName() {
 			return "midi_repeat_count";
 		}
 
 		public Integer[] numArgs() {
 			return new Integer[]{1};
 		}
 
 		public ExceptionType[] thrown() {
 			return new ExceptionType[]{ExceptionType.PluginInternalException};
 		}
 
 		public boolean isRestricted() {
 			return true;
 		}
 
 		public Boolean runAsync() {
 			return false;
 		}
 
 		public String docs() {
 			return "int {sequenceID} Returns the repeat count of the midi sequence. For example if count is 1 the sequence will be played twice.";
 		}
 
 		public Version since() {
 			return CHVersion.V3_3_1;
 		}
 
 		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
 			return new CInt(CHMIDISequencerManager.getSequencer(args[0].val(), t).getMIDISequencer().getLoopCount(), t);
 		}
 	}
 
 	@api
 	public static class midi_set_repeat_count extends AbstractFunction {
 
 		public String getName() {
 			return "midi_set_repeat_count";
 		}
 
 		public Integer[] numArgs() {
 			return new Integer[]{2};
 		}
 
 		public ExceptionType[] thrown() {
 			return new ExceptionType[]{ExceptionType.PluginInternalException, ExceptionType.RangeException};
 		}
 
 		public boolean isRestricted() {
 			return true;
 		}
 
 		public Boolean runAsync() {
 			return false;
 		}
 
 		public String docs() {
 			return "void {sequenceID, count} Sets the repeat count of the midi sequence, an integer. For example if count is 1 the sequence will be played twice.";
 		}
 
 		public Version since() {
 			return CHVersion.V3_3_1;
 		}
 
 		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
 			CHMIDISequencerManager.getSequencer(args[0].val(), t).getMIDISequencer().setLoopCount(Static.getInt32(args[1], t));
 			return new CVoid(t);
 		}
 	}
 }
