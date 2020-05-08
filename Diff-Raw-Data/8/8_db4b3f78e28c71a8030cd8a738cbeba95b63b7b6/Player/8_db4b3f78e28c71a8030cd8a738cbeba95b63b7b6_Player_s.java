 package pro.trousev.cleer;
 
 import pro.trousev.cleer.android.PlayerAndroid.PlayerException;
 
 public interface Player  {
 	
 	enum Error
 	{
 		FileNotFound
 	}
 	enum Status
 	{
		Closed,Stopped,Playing,Paused,Processing
 	}
 	enum Reason
 	{
 		UserBreak, EndOfTrack, PlayerError, SystemBreak
 	}
 	public interface SongState
 	{
 		/**
 		 * Вызывается после того, как песня начала играть.
 		 */
 		void started(Player sender, Item track);
 		/**
 		 * Вызывается после того, как песна закончила играть
 		 * @param reason Строковый комментарий, посвященный ошибке, если таковая была. Может быть null
 		 */
 		void finished(Player sender, Item track, Reason reason);
 		/**
 		 * Вызывается, если произошла какая-то ошибка
 		 * @param errorMessage
 		 */
 		void error(Player sender, Error errorCode, String errorMessage);
 		/**
 		 * Вызывается, если песню приостановили
 		 */
 		void paused(Player sender, Item track);
 		/**
 		 * Вызывается, если песню продолжили играть
 		 */
 		void resumed(Player sender, Item track);
 		/**
 		 * Вызывается, если песня уничтожилась и плеер ее больше не играет
 		 */
 		void destroyed(Player sender);
 	}
 	
 	public void open(Item track, SongState state) throws PlayerException;
 	public void close();
 
 	public void play();
 	public void stop(Reason reason);
 	public void pause();
 	public void resume();
 	
 	public Item now_playing();
 	Status getStatus();
 	
 }
