 package uk.ac.shef.dcs.oak.musicview;
 
 /**
  * The event class represents a single music event
  * 
  * @author sat
  * 
  */
 public class Event
 {
    /** The intensity of the note */
    private final double intenstiy;
 
    /** The onset time of the event in seconds */
    private double onsetTime;
 
    /** The pitch/voice of the note */
    private final double pitch;
 
    /** The time in relation to the score that the event should occur */
    private final double scoreTime;
 
    /** The proposed time of the event */
   private double targetOnset;
 
    /** The proposed velocity of the event */
    private final double targetVelocity;
 
    /**
     * Constructor
     * 
     * @param intense
     *           THe intensity of the note
     * @param onTime
     *           The onset time of the note
     * @param voice
     *           The voice of the note
     * @param score
     *           The score position of the note
     */
    public Event(final double intense, final double onTime, final double voice, final double score,
          final double targetOns, final double targetVel)
    {
       intenstiy = intense;
       onsetTime = onTime;
       pitch = voice;
       scoreTime = score;
       targetOnset = targetOns;
       targetVelocity = targetVel;
    }
 
    /**
     * Gets the bar number of this event
     * 
     * @return THe bar number X.Y where X is bar, and Y is perc of bar
     */
    public final double getBar()
    {
       return scoreTime;
    }
 
    /**
     * Gets the onset time in seconds of the actual event
     * 
     * @return Onset time as a double
     */
    public final double getOnset()
    {
       return onsetTime;
    }
 
    /**
     * Gets the pitch of the event
     * 
     * @return The pitch of the event as a double
     */
    public final double getPitch()
    {
       return pitch;
    }
 
    public double getTargetOnset()
    {
       return targetOnset;
    }
 
    public double getTargetVelocity()
    {
       return targetVelocity;
    }
 
    /**
     * Gets the velocity of the event
     * 
     * @return double value of the the velocity of the event
     */
    public final double getVelocity()
    {
       return intenstiy;
    }
 
    /**
     * The offset of the onset time ho ho
     * 
     * @param offsetValue
     *           offset value in seconds
     */
    public final void offsetOnsetTime(final double offsetValue)
    {
       onsetTime = onsetTime - offsetValue;
       targetOnset = targetOnset - offsetValue;
    }
 }
