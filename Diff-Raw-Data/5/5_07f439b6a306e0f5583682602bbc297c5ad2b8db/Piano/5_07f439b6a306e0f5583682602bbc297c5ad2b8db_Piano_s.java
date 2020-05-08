 package core.midi.sheet.music;
 
import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.DisplayMode;
 import java.awt.Graphics;
 import java.awt.GraphicsDevice;
 import java.awt.GraphicsEnvironment;
 import java.util.List;
 
 import javax.swing.JDialog;
 import javax.swing.JPanel;
 
 /**
  * @class Piano
  * 
  *        The Piano Control is the panel at the top that displays the piano, and
  *        highlights the piano notes during playback. The main methods are:
  * 
  *        SetMidiFile() - Set the Midi file to use for shading. The Midi file is
  *        needed to determine which notes to shade.
  * 
  *        ShadeNotes() - Shade notes on the piano that occur at a given pulse
  *        time.
  * 
  */
 
 public class Piano extends JPanel
 {
    public static int KeysPerOctave = 7;
    public static int MaxOctave = 7;
 
    private static int WhiteKeyWidth;
    /** Width of a single white key */
    private static int WhiteKeyHeight;
    /** Height of a single white key */
    private static int BlackKeyWidth;
    /** Width of a single black key */
    private static int BlackKeyHeight;
    /** Height of a single black key */
    private static int margin;
    /** The top/left margin to the piano */
    private static int BlackBorder;
    /** The width of the black border around the keys */
 
    private static int[] blackKeyOffsets;
    /** The x pixles of the black keys */
 
    // /* The gray1Pens for drawing black/gray lines */
    // private Pen gray1Pen, gray2Pen, gray3Pen;
    //
    // /* The brushes for filling the keys */
    // private Brush gray1Brush, gray2Brush, shadeBrush, shade2Brush;
 
    private boolean useTwoColors;
    /** If true, use two colors for highlighting */
    private List<MidiNote> notes;
    /** The Midi notes for shading */
    private int maxShadeDuration;
    /** The maximum duration we'll shade a note for */
    private int showNoteLetters;
    /** Display the letter for each piano note */
    private Graphics graphics;
    /** The graphics for shading the notes */
    private Color gray2Pen;
    private Color gray1Pen;
    private Color gray3Pen;
    private Color shade1Pen;
    private Color shade2Pen;
    public JSheetMusicWindow Parent;
 
    /** Create a new Piano. */
    public Piano()
    {
       GraphicsEnvironment ge = GraphicsEnvironment
             .getLocalGraphicsEnvironment();
       GraphicsDevice[] gs = ge.getScreenDevices();
 
       int screenwidth = 0;
 
       // Get size of each screen
       for (int i = 0; i < gs.length; i++)
       {
          DisplayMode dm = gs[i].getDisplayMode();
          screenwidth = dm.getWidth();
          int screenheight = dm.getHeight();
       }
       screenwidth = screenwidth * 95 / 100;
       WhiteKeyWidth = (int) (screenwidth / (2.0 + KeysPerOctave * MaxOctave));
       if (WhiteKeyWidth % 2 != 0)
       {
          WhiteKeyWidth--;
       }
 
       margin = 0;
       BlackBorder = WhiteKeyWidth / 2;
       WhiteKeyHeight = WhiteKeyWidth * 5;
       BlackKeyWidth = WhiteKeyWidth / 2;
       BlackKeyHeight = WhiteKeyHeight * 5 / 9;
 
       int Width = margin * 2 + BlackBorder * 2 + WhiteKeyWidth * KeysPerOctave
             * MaxOctave;
       int Height = margin * 2 + BlackBorder * 3 + WhiteKeyHeight;
       setSize(Width, Height);
 
       if (blackKeyOffsets == null)
       {
          blackKeyOffsets = new int[] { WhiteKeyWidth - BlackKeyWidth / 2 - 1,
                WhiteKeyWidth + BlackKeyWidth / 2 - 1,
                2 * WhiteKeyWidth - BlackKeyWidth / 2,
                2 * WhiteKeyWidth + BlackKeyWidth / 2,
                4 * WhiteKeyWidth - BlackKeyWidth / 2 - 1,
                4 * WhiteKeyWidth + BlackKeyWidth / 2 - 1,
                5 * WhiteKeyWidth - BlackKeyWidth / 2,
                5 * WhiteKeyWidth + BlackKeyWidth / 2,
                6 * WhiteKeyWidth - BlackKeyWidth / 2,
                6 * WhiteKeyWidth + BlackKeyWidth / 2 };
       }
       gray1Pen = new Color(16, 16, 16);
       gray2Pen = new Color(90, 90, 90);
       gray3Pen = new Color(200, 200, 200);
       shade1Pen = new Color(210, 205, 220);
       shade2Pen = new Color(150, 200, 220);
 
       // gray1Pen = new Pen(gray1, 1);
       // gray2Pen = new Pen(gray2, 1);
       // gray3Pen = new Pen(gray3, 1);
       //
       // gray1Brush = new SolidBrush(gray1);
       // gray2Brush = new SolidBrush(gray2);
       // shadeBrush = new SolidBrush(shade1);
       // shade2Brush = new SolidBrush(shade2);
       showNoteLetters = -1; // MidiOptions.NoteNameNone;
       setBackground(Color.lightGray);
    }
 
    /**
     * Set the MidiFile to use. Save the list of midi notes. Each midi note
     * includes the note Number and StartTime (in pulses), so we know which notes
     * to shade given the current pulse time.
     */
    // public void SetMidiFile(MidiFile midifile, MidiOptions options) {
    // if (midifile == null) {
    // notes = null;
    // useTwoColors = false;
    // return;
    // }
    //
    // List<MidiTrack> tracks = midifile.ChangeMidiNotes(options);
    // MidiTrack track = MidiFile.CombineToSingleTrack(tracks);
    // notes = track.Notes;
    //
    // maxShadeDuration = midifile.Time.Quarter * 2;
    //
    // /* We want to know which track the note came from.
    // * Use the 'channel' field to store the track.
    // */
    // for (int tracknum = 0; tracknum < tracks.Count; tracknum++) {
    // foreach (MidiNote note in tracks[tracknum].Notes) {
    // note.Channel = tracknum;
    // }
    // }
    //
    // /* When we have exactly two tracks, we assume this is a piano song,
    // * and we use different colors for highlighting the left hand and
    // * right hand notes.
    // */
    // useTwoColors = false;
    // if (tracks.Count == 2) {
    // useTwoColors = true;
    // }
    //
    // showNoteLetters = options.showNoteLetters;
    // this.Invalidate();
    // }
 
    /** Set the colors to use for shading */
    public void SetShadeColors(Color c1, Color c2)
    {
       // shadeBrush.Dispose();
       // shade2Brush.Dispose();
       // shadeBrush = new SolidBrush(c1);
       // shade2Brush = new SolidBrush(c2);
    }
 
    /** Draw the outline of a 12-note (7 white note) piano octave */
    private void DrawOctaveOutline(Graphics g)
    {
       int right = WhiteKeyWidth * KeysPerOctave;
 
       // Draw the bounding rectangle, from C to B
 
       g.setColor(gray1Pen);
       g.drawLine(0, 0, 0, WhiteKeyHeight);
       g.drawLine(right, 0, right, WhiteKeyHeight);
       g.drawLine(0, 0, right, 0);
       g.drawLine(0, WhiteKeyHeight, right, WhiteKeyHeight);
       g.setColor(gray3Pen);
       g.drawLine(right - 1, 0, right - 1, WhiteKeyHeight);
       g.drawLine(1, 0, 1, WhiteKeyHeight);
 
       // Draw the line between E and F
       g.setColor(gray1Pen);
       g.drawLine(3 * WhiteKeyWidth, 0, 3 * WhiteKeyWidth, WhiteKeyHeight);
       g.setColor(gray3Pen);
       g.drawLine(3 * WhiteKeyWidth - 1, 0, 3 * WhiteKeyWidth - 1,
             WhiteKeyHeight);
       g.drawLine(3 * WhiteKeyWidth + 1, 0, 3 * WhiteKeyWidth + 1,
             WhiteKeyHeight);
 
       // Draw the sides/bottom of the black keys
       for (int i = 0; i < 10; i += 2)
       {
          int x1 = blackKeyOffsets[i];
          int x2 = blackKeyOffsets[i + 1];
 
          g.setColor(gray1Pen);
          g.drawLine(x1, 0, x1, BlackKeyHeight);
          g.drawLine(x2, 0, x2, BlackKeyHeight);
          g.drawLine(x1, BlackKeyHeight, x2, BlackKeyHeight);
          g.setColor(gray2Pen);
          g.drawLine(x1 - 1, 0, x1 - 1, BlackKeyHeight + 1);
          g.drawLine(x2 + 1, 0, x2 + 1, BlackKeyHeight + 1);
          g.drawLine(x1 - 1, BlackKeyHeight + 1, x2 + 1, BlackKeyHeight + 1);
          g.setColor(gray3Pen);
          g.drawLine(x1 - 2, 0, x1 - 2, BlackKeyHeight + 2);
          g.drawLine(x2 + 2, 0, x2 + 2, BlackKeyHeight + 2);
          g.drawLine(x1 - 2, BlackKeyHeight + 2, x2 + 2, BlackKeyHeight + 2);
       }
 
       // Draw the bottom-half of the white keys
       for (int i = 1; i < KeysPerOctave; i++)
       {
          if (i == 3)
          {
             continue; // we draw the line between E and F above
          }
          g.setColor(gray1Pen);
          g.drawLine(i * WhiteKeyWidth, BlackKeyHeight, i * WhiteKeyWidth,
                WhiteKeyHeight);
          // Pen pen1 = gray2Pen;
          // Pen pen2 = gray3Pen;
          g.setColor(gray2Pen);
          g.drawLine(i * WhiteKeyWidth - 1, BlackKeyHeight + 1, i
                * WhiteKeyWidth - 1, WhiteKeyHeight);
          g.setColor(gray3Pen);
          g.drawLine(i * WhiteKeyWidth + 1, BlackKeyHeight + 1, i
                * WhiteKeyWidth + 1, WhiteKeyHeight);
       }
 
    }
 
    /** Draw an outline of the piano for 7 octaves */
    private void DrawOutline(Graphics g)
    {
       for (int octave = 0; octave < MaxOctave; octave++)
       {
          g.translate(octave * WhiteKeyWidth * KeysPerOctave, 0);
          DrawOctaveOutline(g);
          g.translate(-(octave * WhiteKeyWidth * KeysPerOctave), 0);
       }
    }
 
    /* Draw the Black keys */
    private void DrawBlackKeys(Graphics g)
    {
       for (int octave = 0; octave < MaxOctave; octave++)
       {
          g.translate(octave * WhiteKeyWidth * KeysPerOctave, 0);
          for (int i = 0; i < 10; i += 2)
          {
             int x1 = blackKeyOffsets[i];
             int x2 = blackKeyOffsets[i + 1];
             g.setColor(gray1Pen);
             g.fillRect(x1, 0, BlackKeyWidth, BlackKeyHeight);
             g.setColor(gray3Pen);
             g.fillRect(x1 + 1, BlackKeyHeight - BlackKeyHeight / 8,
                   BlackKeyWidth - 2, BlackKeyHeight / 8);
          }
          g.translate(-(octave * WhiteKeyWidth * KeysPerOctave), 0);
       }
    }
 
    /*
     * Draw the black border area surrounding the piano keys. Also, draw gray
     * outlines at the bottom of the white keys.
     */
    private void DrawBlackBorder(Graphics g)
    {
       int PianoWidth = WhiteKeyWidth * KeysPerOctave * MaxOctave;
       g.setColor(gray1Pen);
       g.fillRect(margin, margin, PianoWidth + BlackBorder * 2, BlackBorder - 2);
       g.fillRect(margin, margin, BlackBorder, WhiteKeyHeight + BlackBorder * 3);
       g.fillRect(margin, margin + BlackBorder + WhiteKeyHeight, BlackBorder * 2
             + PianoWidth, BlackBorder * 2);
       g.fillRect(margin + BlackBorder + PianoWidth, margin, BlackBorder,
             WhiteKeyHeight + BlackBorder * 3);
       g.setColor(gray2Pen);
       g.drawLine(margin + BlackBorder, margin + BlackBorder - 1, margin
             + BlackBorder + PianoWidth, margin + BlackBorder - 1);
 
       g.translate(margin + BlackBorder, margin + BlackBorder);
 
       // Draw the gray bottoms of the white keys
       for (int i = 0; i < KeysPerOctave * MaxOctave; i++)
       {
          g.fillRect(i * WhiteKeyWidth + 1, WhiteKeyHeight + 2,
                WhiteKeyWidth - 2, BlackBorder / 2);
       }
       g.translate(-(margin + BlackBorder), -(margin + BlackBorder));
    }
 
    /** Draw the note letters underneath each white note */
    private void DrawNoteLetters(Graphics g)
    {
       String[] letters = { "C", "D", "E", "F", "G", "A", "B" };
       String[] numbers = { "1", "3", "5", "6", "8", "10", "12" };
       String[] names;
       // if (showNoteLetters == MidiOptions.NoteNameLetter) {
       // names = letters;
       // }
       // else if (showNoteLetters == MidiOptions.NoteNameFixedNumber) {
       // names = numbers;
       // }
       // else {
       // return;
       // }
       g.translate(margin + BlackBorder, margin + BlackBorder);
       for (int octave = 0; octave < MaxOctave; octave++)
       {
          for (int i = 0; i < KeysPerOctave; i++)
          {
             // g.DrawString(names[i], SheetMusic.LetterFont, Brushes.White,
             // (octave*KeysPerOctave + i) * WhiteKeyWidth + WhiteKeyWidth/3,
             // WhiteKeyHeight + BlackBorder * 3/4);
          }
       }
       g.translate(-(margin + BlackBorder), -(margin + BlackBorder));
    }
 
    /** Draw the Piano. */
    @Override
    public void paint(Graphics g)
    {
       // g.SmoothingMode = System.Drawing.Drawing2D.SmoothingMode.None;
       g.translate(margin + BlackBorder, margin + BlackBorder);
       g.setColor(Color.WHITE);
       g.fillRect(0, 0, WhiteKeyWidth * KeysPerOctave * MaxOctave,
             WhiteKeyHeight);
       DrawBlackKeys(g);
       DrawOutline(g);
       g.translate(-(margin + BlackBorder), -(margin + BlackBorder));
       DrawBlackBorder(g);
       // if (showNoteLetters != MidiOptions.NoteNameNone) {
       // DrawNoteLetters(g);
       // }
       // g.SmoothingMode = System.Drawing.Drawing2D.SmoothingMode.AntiAlias;
    }
 
    /*
     * Shade the given note with the given brush. We only draw notes from
     * notenumber 24 to 96. (Middle-C is 60).
     */
    private void ShadeOneNote(Graphics g, int notenumber, Color brush)
    {
       int octave = notenumber / 12;
       int notescale = notenumber % 12;
 
       octave -= 2;
       if (octave < 0 || octave >= MaxOctave)
          return;
 
       g.translate(octave * WhiteKeyWidth * KeysPerOctave, 0);
       int x1, x2, x3;
 
       int bottomHalfHeight = WhiteKeyHeight - (BlackKeyHeight + 3);
 
       /* notescale goes from 0 to 11, from C to B. */
       g.setColor(brush);
       switch (notescale)
       {
       case 0: /* C */
          x1 = 2;
          x2 = blackKeyOffsets[0] - 2;
          g.fillRect(x1, 0, x2 - x1, BlackKeyHeight + 3);
          g.fillRect(x1, BlackKeyHeight + 3, WhiteKeyWidth - 3, bottomHalfHeight);
          break;
       case 1: /* C# */
          x1 = blackKeyOffsets[0];
          x2 = blackKeyOffsets[1];
          g.fillRect(x1, 0, x2 - x1, BlackKeyHeight);
          if (brush == gray1Pen)
          {
             g.setColor(gray2Pen);
             g.fillRect(x1 + 1, BlackKeyHeight - BlackKeyHeight / 8,
                   BlackKeyWidth - 2, BlackKeyHeight / 8);
          }
          break;
       case 2: /* D */
          x1 = WhiteKeyWidth + 2;
          x2 = blackKeyOffsets[1] + 3;
          x3 = blackKeyOffsets[2] - 2;
          g.fillRect(x2, 0, x3 - x2, BlackKeyHeight + 3);
          g.fillRect(x1, BlackKeyHeight + 3, WhiteKeyWidth - 3, bottomHalfHeight);
          break;
       case 3: /* D# */
          x1 = blackKeyOffsets[2];
          x2 = blackKeyOffsets[3];
          g.fillRect(x1, 0, BlackKeyWidth, BlackKeyHeight);
          if (brush == gray1Pen)
          {
             g.setColor(gray2Pen);
             g.fillRect(x1 + 1, BlackKeyHeight - BlackKeyHeight / 8,
                   BlackKeyWidth - 2, BlackKeyHeight / 8);
          }
          break;
       case 4: /* E */
          x1 = WhiteKeyWidth * 2 + 2;
          x2 = blackKeyOffsets[3] + 3;
          x3 = WhiteKeyWidth * 3 - 1;
          g.fillRect(x2, 0, x3 - x2, BlackKeyHeight + 3);
          g.fillRect(x1, BlackKeyHeight + 3, WhiteKeyWidth - 3, bottomHalfHeight);
          break;
       case 5: /* F */
          x1 = WhiteKeyWidth * 3 + 2;
          x2 = blackKeyOffsets[4] - 2;
          x3 = WhiteKeyWidth * 4 - 2;
          g.fillRect(x1, 0, x2 - x1, BlackKeyHeight + 3);
          g.fillRect(x1, BlackKeyHeight + 3, WhiteKeyWidth - 3, bottomHalfHeight);
          break;
       case 6: /* F# */
          x1 = blackKeyOffsets[4];
          x2 = blackKeyOffsets[5];
          g.fillRect(x1, 0, BlackKeyWidth, BlackKeyHeight);
          if (brush == gray1Pen)
          {
             g.setColor(gray2Pen);
             g.fillRect(x1 + 1, BlackKeyHeight - BlackKeyHeight / 8,
                   BlackKeyWidth - 2, BlackKeyHeight / 8);
          }
          break;
       case 7: /* G */
          x1 = WhiteKeyWidth * 4 + 2;
          x2 = blackKeyOffsets[5] + 3;
          x3 = blackKeyOffsets[6] - 2;
          g.fillRect(x2, 0, x3 - x2, BlackKeyHeight + 3);
          g.fillRect(x1, BlackKeyHeight + 3, WhiteKeyWidth - 3, bottomHalfHeight);
          break;
       case 8: /* G# */
          x1 = blackKeyOffsets[6];
          x2 = blackKeyOffsets[7];
          g.fillRect(x1, 0, BlackKeyWidth, BlackKeyHeight);
          if (brush == gray1Pen)
          {
             g.setColor(gray2Pen);
             g.fillRect(x1 + 1, BlackKeyHeight - BlackKeyHeight / 8,
                   BlackKeyWidth - 2, BlackKeyHeight / 8);
          }
          break;
       case 9: /* A */
          x1 = WhiteKeyWidth * 5 + 2;
          x2 = blackKeyOffsets[7] + 3;
          x3 = blackKeyOffsets[8] - 2;
          g.fillRect(x2, 0, x3 - x2, BlackKeyHeight + 3);
          g.fillRect(x1, BlackKeyHeight + 3, WhiteKeyWidth - 3, bottomHalfHeight);
          break;
       case 10: /* A# */
          x1 = blackKeyOffsets[8];
          x2 = blackKeyOffsets[9];
          g.fillRect(x1, 0, BlackKeyWidth, BlackKeyHeight);
          if (brush == gray1Pen)
          {
             g.setColor(gray2Pen);
             g.fillRect(x1 + 1, BlackKeyHeight - BlackKeyHeight / 8,
                   BlackKeyWidth - 2, BlackKeyHeight / 8);
          }
          break;
       case 11: /* B */
          x1 = WhiteKeyWidth * 6 + 2;
          x2 = blackKeyOffsets[9] + 3;
          x3 = WhiteKeyWidth * KeysPerOctave - 1;
          g.fillRect(x2, 0, x3 - x2, BlackKeyHeight + 3);
          g.fillRect(x1, BlackKeyHeight + 3, WhiteKeyWidth - 3, bottomHalfHeight);
          break;
       default:
          break;
       }
       g.translate(-(octave * WhiteKeyWidth * KeysPerOctave), 0);
    }
 
    /**
     * Find the MidiNote with the startTime closest to the given time. Return the
     * index of the note. Use a binary search method.
     */
    private int FindClosestStartTime(int pulseTime)
    {
       int left = 0;
       int right = notes.size() - 1;
 
       while (right - left > 1)
       {
          int i = (right + left) / 2;
          if (notes.get(left).getStartTime() == pulseTime)
             break;
          else if (notes.get(i).getStartTime() <= pulseTime)
             left = i;
          else
             right = i;
       }
       while (left >= 1
             && (notes.get(left - 1).getStartTime() == notes.get(left)
                   .getStartTime()))
       {
          left--;
       }
       return left;
    }
 
    /**
     * Return the next StartTime that occurs after the MidiNote at offset i, that
     * is also in the same track/channel.
     */
    private int NextStartTimeSameTrack(int i)
    {
       int start = notes.get(i).getStartTime();
       int end = notes.get(i).getEndTime();
       int track = notes.get(i).getChannel();
 
       while (i < notes.size())
       {
          if (notes.get(i).getChannel() != track)
          {
             i++;
             continue;
          }
          if (notes.get(i).getStartTime() > start)
          {
             return notes.get(i).getStartTime();
          }
          end = Math.max(end, notes.get(i).getEndTime());
          i++;
       }
       return end;
    }
 
    /**
     * Return the next StartTime that occurs after the MidiNote at offset i. If
     * all the subsequent notes have the same StartTime, then return the largest
     * EndTime.
     */
    private int NextStartTime(int i)
    {
       int start = notes.get(i).getStartTime();
       int end = notes.get(i).getEndTime();
 
       while (i < notes.size())
       {
          if (notes.get(i).getStartTime() > start)
          {
             return notes.get(i).getStartTime();
          }
          end = Math.max(end, notes.get(i).getEndTime());
          i++;
       }
       return end;
    }
 
    /**
     * Find the Midi notes that occur in the current time. Shade those notes on
     * the piano displayed. Un-shade the those notes played in the previous time.
     */
    public void ShadeNotes(int currentPulseTime, int prevPulseTime)
    {
       if (notes == null || notes.size() == 0)
       {
          return;
       }
       // if (graphics == null) {
       // graphics = CreateGraphics();
       // }
       // graphics.SmoothingMode = System.Drawing.Drawing2D.SmoothingMode.None;
       graphics.translate(margin + BlackBorder, margin + BlackBorder);
 
       /*
        * Loop through the Midi notes. Unshade notes where StartTime <=
        * prevPulseTime < next StartTime Shade notes where StartTime <=
        * currentPulseTime < next StartTime
        */
       int lastShadedIndex = FindClosestStartTime(prevPulseTime
             - maxShadeDuration * 2);
       for (int i = lastShadedIndex; i < notes.size(); i++)
       {
          int start = notes.get(i).getStartTime();
          int end = notes.get(i).getEndTime();
          int notenumber = notes.get(i).getNoteNumber();
          int nextStart = NextStartTime(i);
          int nextStartTrack = NextStartTimeSameTrack(i);
          end = Math.max(end, nextStartTrack);
          end = Math.min(end, start + maxShadeDuration - 1);
 
          /* If we've past the previous and current times, we're done. */
          if ((start > prevPulseTime) && (start > currentPulseTime))
          {
             break;
          }
 
          /* If shaded notes are the same, we're done */
          if ((start <= currentPulseTime) && (currentPulseTime < nextStart)
                && (currentPulseTime < end) && (start <= prevPulseTime)
                && (prevPulseTime < nextStart) && (prevPulseTime < end))
          {
             break;
          }
 
          /* If the note is in the current time, shade it */
          if ((start <= currentPulseTime) && (currentPulseTime < end))
          {
             if (useTwoColors)
             {
                if (notes.get(i).getChannel() == 1)
                {
                   ShadeOneNote(graphics, notenumber, gray2Pen);
                }
                else
                {
                   ShadeOneNote(graphics, notenumber, gray1Pen);
                }
             }
             else
             {
                ShadeOneNote(graphics, notenumber, gray1Pen);
             }
          }
 
          /* If the note is in the previous time, un-shade it, draw it white. */
          else if ((start <= prevPulseTime) && (prevPulseTime < end))
          {
             int num = notenumber % 12;
             if (num == 1 || num == 3 || num == 6 || num == 8 || num == 10)
             {
                ShadeOneNote(graphics, notenumber, gray1Pen);
             }
             else
             {
                ShadeOneNote(graphics, notenumber, Color.white);
             }
          }
       }
       graphics.translate(-(margin + BlackBorder), -(margin + BlackBorder));
       // graphics.SmoothingMode =
       // System.Drawing.Drawing2D.SmoothingMode.AntiAlias;
    }
 
    public static void main(String[] args)
    {
       JDialog dialog = new JDialog();
       Piano piano = new Piano();
      dialog.setSize((int)(piano.getWidth()*1.05), piano.getHeight()*2);
       dialog.setVisible(true);
    }
 }
