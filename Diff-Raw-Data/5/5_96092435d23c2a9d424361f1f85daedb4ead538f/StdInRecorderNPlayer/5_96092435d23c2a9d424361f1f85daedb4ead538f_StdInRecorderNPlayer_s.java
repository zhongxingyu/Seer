 package org.akquinet.audit.ui;
 
 import java.io.IOException;
 import java.io.InputStream;
 
 public class StdInRecorderNPlayer extends InputStream
 {
 	private enum State
 	{
 		PLAY,
 		STOP,
 		RECORD,
 		STOP_GOT_DATA
 	}
 	
 	private State _state;
 	private String _playData;
 	private int _alreadyPlayedChars;
 	private static final InputStream _realIn = System.in;
 	
 	private StringBuffer _recordedData;
 
 	public StdInRecorderNPlayer()
 	{
 		super();
 		_state = State.STOP;
 		_playData = "";
 		_alreadyPlayedChars = 0;
 		_recordedData = null;
 	}
 	
 	@Override
 	public int read() throws IOException
 	{
 		switch(_state)
 		{
 		case RECORD:
 			int i = _realIn.read();
 			if( i == (int) ((char)i) )
 			{
 				_recordedData = _recordedData.append((char)i);
 			}
 			return i;
 		case PLAY:
 			if(_alreadyPlayedChars < _playData.length())
 			{
 				return _playData.charAt(_alreadyPlayedChars++);
 			}
 			else
 			{
 				stop();
 			}
 		case STOP:
 		case STOP_GOT_DATA:
 			return _realIn.read();
 		}
 
		return 0;
 	}
 	
 	public void play(String tape)
 	{
 		_playData = tape;
 		_alreadyPlayedChars = 0;
 		
 		_state = State.PLAY;
 		_recordedData = null;
 	}
 	
 	public void record()
 	{
 		_state = State.RECORD;
 		_recordedData = new StringBuffer("");
 	}
 	
 	public void stop()
 	{
 		if(_recordedData == null)
 		{
 			_state = State.STOP;
 		}
 		else
 		{
 			_state = State.STOP_GOT_DATA;
 		}
 	}
 	
 	public String save()
 	{
 		String ret = _recordedData.toString();
 		_recordedData = null;
 		_state = State.STOP;
 		return ret;
 	}
 }
