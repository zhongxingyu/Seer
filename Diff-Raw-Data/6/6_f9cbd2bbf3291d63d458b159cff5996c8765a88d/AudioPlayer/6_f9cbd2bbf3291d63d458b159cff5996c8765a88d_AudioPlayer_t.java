 package crescendo.base;
 
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.HashMap;
 
 import javax.sound.midi.Instrument;
 import javax.sound.midi.MidiChannel;
 import javax.sound.midi.MidiSystem;
 import javax.sound.midi.MidiUnavailableException;
 import javax.sound.midi.Synthesizer;
 
 import crescendo.base.song.Note;
 import crescendo.base.song.SongModel;
 import crescendo.base.song.Track;
 
 /**
  * Plays notes through the software synthesizer, as encapsulated in a NoteEvent object.
  */
 public class AudioPlayer implements NoteEventListener,FlowController
 {
 	/**
 	 * Allows association from track to channel and note data.
 	 */
 	private Map<Track,AudioPlayerChannel> channelMap;
 	
 	/**
 	 * The synthesizer object.
 	 */
 	private Synthesizer synth;
 	
 	/**
 	 * Creates a new AudioPlayer, contructed around a specific song.
 	 * 
 	 * @param songModel The song to tailor the AudioPlayer to.
 	 * @param activeTrack The index of the track being played by the user.
 	 */
 	public AudioPlayer(SongModel songModel,Track activeTrack)
 	{
 		// initialize relation map... thing
 		this.channelMap=new HashMap<Track,AudioPlayerChannel>();
 		
 		boolean retry=false;
 		do
 		{
 			try
 			{
 				// get synthesizer object
 				this.synth=MidiSystem.getSynthesizer();
 				// open it so we can get, well, anything out of it
 				this.synth.open();
 				retry=false;
 			}
 			catch (MidiUnavailableException e)
 			{
 				String title="MIDI Unavailable";
 				String message="A MIDI system could not be found. The program will now exit.";
 				if (ErrorHandler.showRetryFail(title,message)==ErrorHandler.Response.RETRY)
 				{
 					retry=true;
 				}
 				else
 				{
 					System.exit(1);
 				}
 			}
 		} while (retry);
 		
 		// get channels and instruments
 		MidiChannel[] channels=this.synth.getChannels();
 		Instrument[] instruments=this.synth.getAvailableInstruments();
 		
 		// match channels to AudioPlayerChannel objects, but don't add the active track
 		int currentChannel=0;
 		
 		for (int i=0; i<songModel.getNumTracks(); i++)
 		{
 			// hey remember, don't add the active track
 			Track track=songModel.getTrack(i);
 			if (track!=activeTrack)
 			{
 				Instrument instrument;
 				try
 				{
 					instrument=instruments[track.getVoice()];
 				}
 				catch (ArrayIndexOutOfBoundsException e)
 				{
 					instrument=instruments[0];
 				}
 				
 				MidiChannel channel=channels[currentChannel];
 				channel.programChange(instrument.getPatch().getBank(),instrument.getPatch().getProgram());
 				
 				AudioPlayerChannel playerChannel=new AudioPlayerChannel(channel);
 				
 				// add it to the map
 				this.channelMap.put(track,playerChannel);
 				
 				// don't forget to increment this... pretty sure someone did once
 				currentChannel++;
 			}
 		}
 	}
 	
 	/**
 	 * The current delay between giving the MIDI subsystem a note and the time
 	 * it gets played. This value will not likely change over time, but it is not
 	 * guaranteed to remain constant.
 	 * 
 	 * @return The current delay between giving the MIDI subsystem a note and the time
 	 * it gets played, in milliseconds.
 	 */
 	public double getLatency()
 	{
 		// the division converts microseconds to milliseconds
 		return this.synth.getLatency()/1000;
 	}
 	
 	/**
 	 * Processes a note, passing it along to the proper channel with the proper action.
 	 * 
 	 * @param noteEvent The NoteEvent to be processed.
 	 */
 	public void handleNoteEvent(NoteEvent noteEvent)
 	{
 		// pull relevant information out
 		Note note=noteEvent.getNote();
 		NoteAction action=noteEvent.getAction();
 		Track track=note.getTrack();
 		
 		// match the track to the proper channel object
 		AudioPlayerChannel channel=this.channelMap.get(track);
 		
 		if (channel!=null)
 		{
 			// execute the proper action
 			if (action==NoteAction.BEGIN)
 			{
 				channel.playNote(note);
 			}
 			else
 			{
 				channel.stopNote(note);
 			}
 		}
 	}
 	
 	public void pause()
 	{
 		for (AudioPlayerChannel channel : this.channelMap.values())
 		{
 			channel.pauseNotes();
 		}
 	}
 	
 	public void resume()
 	{
 		for (AudioPlayerChannel channel : this.channelMap.values())
 		{
 			channel.resumeNotes();
 		}
 	}
 	
 	public void suspend()
 	{
 		// don't need to do anything here because the timing is external,
 		// so notes will just keep playing
 		// (inherited from FlowController)
 	}
 	
 	public void stop()
 	{
 		for (AudioPlayerChannel channel : this.channelMap.values())
 		{
 			channel.stopAllNotes();
 		}
 	}
 	
 	public void songEnd()
 	{
 		// stop all playing notes
 		this.stop();
 		// close the synthesizer so it releases its resources
 		this.synth.close();
 	}
 	
 	private class AudioPlayerChannel
 	{
 		private MidiChannel channel;
 		
 		/**
 		 * Contains a list of active notes and their current velocities.
 		 */
 		private List<Note> activeNotes;
 		
 		public AudioPlayerChannel(MidiChannel channel)
 		{
 			this.channel=channel;
 			this.activeNotes=new LinkedList<Note>();
 		}
 		
 		public void playNote(Note note)
 		{
 			int pitch=note.getPitch();
 			int velocity=note.getDynamic();
 			this.channel.noteOn(pitch,velocity);
 			
 			for (Iterator<Note> iter=this.activeNotes.iterator(); iter.hasNext();)
 			{
 				Note iterNote=iter.next();
 				if (note.getPitch() == iterNote.getPitch())
 				{
 					iter.remove();
 				}
 			}
 			this.activeNotes.add(note);
 		}
 		
 		public void stopNote(Note note)
 		{
 			int pitch=note.getPitch();
 			this.channel.noteOff(pitch);
 			
 			this.activeNotes.remove(note);
 		}
 		
 		public void pauseNotes()
 		{
 			this.channel.allNotesOff();
 		}
 		
 		public void resumeNotes()
 		{
 			for (Note note : this.activeNotes)
 			{
 				int pitch=note.getPitch();
				int velocity=note.getDynamic();
 				
 				this.channel.noteOn(pitch,velocity);
 			}
 		}
 		
 		public void stopAllNotes()
 		{
 			this.channel.allNotesOff();
 			this.activeNotes=new LinkedList<Note>();
 		}
 	}
 }
