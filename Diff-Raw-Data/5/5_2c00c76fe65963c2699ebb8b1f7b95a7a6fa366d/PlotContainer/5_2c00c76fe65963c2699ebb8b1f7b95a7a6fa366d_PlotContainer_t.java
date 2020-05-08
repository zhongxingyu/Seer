 package org.bodytrack.client;
 
 import com.google.gwt.event.dom.client.MouseDownEvent;
 import com.google.gwt.event.dom.client.MouseDownHandler;
 import com.google.gwt.event.dom.client.MouseMoveEvent;
 import com.google.gwt.event.dom.client.MouseMoveHandler;
 import com.google.gwt.event.dom.client.MouseOutEvent;
 import com.google.gwt.event.dom.client.MouseOutHandler;
 import com.google.gwt.event.dom.client.MouseUpEvent;
 import com.google.gwt.event.dom.client.MouseUpHandler;
 import com.google.gwt.user.client.ui.RootPanel;
 import gwt.g2d.client.graphics.Color;
 import gwt.g2d.client.graphics.KnownColor;
 import gwt.g2d.client.graphics.Surface;
 import gwt.g2d.client.graphics.TextAlign;
 import gwt.g2d.client.graphics.canvas.CanvasElement;
 import gwt.g2d.client.math.Vector2;
 
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Set;
 
 public class PlotContainer implements Resizable {
    /**
     * The default loading message for this widget to show.  This class
     * never actually uses this value, but makes it available for classes
     * that use the loading API.
     */
    public static final String DEFAULT_LOADING_MESSAGE = "Loading...";
 
    /**
     * The maximum number of value messages that is <em>guaranteed</em>
     * to be displayed.  It is possible that, if there are more than
     * this many value messages, all will be displayed, but no guarantee
     * is made.  Also, no guarantee is made as to which value messages
     * will be displayed.
     *
     * <p>In order to guarantee display of a message, then, a caller of
     * {@link #addValueMessage(String)} should ensure that the number of
     * value messages, as returned by {@link #countValueMessages()}, is
     * less than this.  If this is not so, messages can always be removed
     * using {@link #removeValueMessage(int)}.</p>
     */
    public static final int VALUE_MESSAGES_CAPACITY = 4;
 
    private static final int INITIAL_MESSAGE_ID = 1;
    private static final Color LOADING_MSG_COLOR = KnownColor.DARK_GRAY;
    private static final double LOADING_MSG_X_MARGIN = 5;
    private static final double LOADING_MSG_Y_MARGIN = 3;
    private static final double VALUE_MSG_X_MARGIN = 5;
    private static final double VALUE_MSG_Y_MARGIN = 3;
    private static final double VALUE_MSG_GAP = 2;
    private static final double TEXT_HEIGHT = 12;
    private static final double TEXT_LINE_WIDTH = 0.75;
 
    private final Surface drawing;
    private final Set<Plot> containedPlots = new HashSet<Plot>();
 
    // For the loading message API, which shows one message at a time
    // on the bottom left, without regard to width
    // Invariants: all IDs in loadingMessages conform to
    // INITIAL_MESSAGE_ID <= id < nextLoadingMessageId, with all IDs
    // unique, and there is never a null message string in loadingMessages
    private int nextLoadingMessageId;
    private final List<DisplayMessage> loadingMessages;
 
    // For the value message API, which shows multiple messages at a time
    // on the bottom right, with strict controls on size
    // Invariants: all IDs in valueMessages conform to
    // INITIAL_MESSAGE_ID <= id < nextValueMessageId, with all IDs
    // unique, and there is never a null message string in valueMessages
    private int nextValueMessageId;
    private final List<DisplayMessage> valueMessages;
 
    private int width;
    private int height;
 
    private Vector2 mouseDragLastPos;
 
    private int previousPaintEventId = 0;
    private final String placeholderElementId;
 
    public PlotContainer(final String placeholderElementId) {
       if (placeholderElementId == null) {
          throw new NullPointerException("The placeholder element ID cannot be null");
       }
       this.placeholderElementId = placeholderElementId;
 
       final RootPanel placeholderElement = RootPanel.get(placeholderElementId);
       this.width = placeholderElement.getElement().getClientWidth();
       this.height = placeholderElement.getElement().getClientHeight();
       drawing = new Surface(width, height);
       placeholderElement.add(drawing);
 
       nextLoadingMessageId = INITIAL_MESSAGE_ID;
       loadingMessages = new ArrayList<DisplayMessage>();
 
       nextValueMessageId = INITIAL_MESSAGE_ID;
       valueMessages = new ArrayList<DisplayMessage>();
 
       drawing.addMouseDownHandler(new MouseDownHandler() {
          @Override
          public void onMouseDown(final MouseDownEvent event) {
             handleMouseDownEvent(event);
          }
       });
 
       drawing.addMouseMoveHandler(new MouseMoveHandler() {
          @Override
          public void onMouseMove(final MouseMoveEvent event) {
             handleMouseMoveEvent(event);
          }
       });
 
       drawing.addMouseUpHandler(new MouseUpHandler() {
          @Override
          public void onMouseUp(final MouseUpEvent event) {
             handleMouseUpEvent(event);
          }
       });
 
       drawing.addMouseOutHandler(new MouseOutHandler() {
          @Override
          public void onMouseOut(final MouseOutEvent event) {
             handleMouseOutEvent(event);
          }
       });
    }
 
    private void handleMouseDownEvent(final MouseDownEvent event) {
       mouseDragLastPos = new Vector2(event.getX(), event.getY());
    }
 
    private void handleMouseMoveEvent(final MouseMoveEvent event) {
       final Vector2 pos = new Vector2(event.getX(), event.getY());
 
       // We can be dragging exactly one of: one or
       // more plots, the whole viewing window, and nothing
       if (mouseDragLastPos != null) {
          // We are either dragging either one or more plots,
          // or the whole viewing window. If there's one or more
          // highlighted plot, then just drag the axes
          // for those plots.  Otherwise, drag all axes.
 
          // build a set of the highlighted plots
          final Set<Plot> highlightedPlots = new HashSet<Plot>();
          for (final Plot plot : containedPlots) {
             if (plot.isHighlighted()) {
                highlightedPlots.add(plot);
             }
          }
 
          // determine whether we're dragging only the highlighted plots
          final Set<Plot> plots = (highlightedPlots.size() > 0) ? highlightedPlots : containedPlots;
 
          // build a Set of axes to eliminate dupes
          final Set<GraphAxis> axes = new HashSet<GraphAxis>();
          for (final Plot plot : plots) {
             axes.add(plot.getXAxis());
             axes.add(plot.getYAxis());
          }
 
          // drag the axes
          for (final GraphAxis axis : axes) {
             axis.drag(mouseDragLastPos, pos, SequenceNumber.getNext());
          }
 
          mouseDragLastPos = pos;
       } else {
          // We are not dragging anything, so we just update the
          // highlighting on the plots and axes
 
          final Set<Plot> highlightedPlots = new HashSet<Plot>();
          for (final Plot plot : containedPlots) {
             if (plot.highlightIfNear(pos)) {
                highlightedPlots.add(plot);
             } else {
                plot.unhighlight();
             }
          }
 
          // Now we handle highlighting of the axes--first build a set of the unhighlighted plots
          final Set<Plot> unhighlightedPlots = new HashSet<Plot>(containedPlots);
          unhighlightedPlots.removeAll(highlightedPlots);
 
          // unhighlight the axes of the unhighlighted plots
          final Set<GraphAxis> unhighlightedAxes = new HashSet<GraphAxis>();
          for (final Plot plot : unhighlightedPlots) {
             unhighlightedAxes.add(plot.getXAxis());
             unhighlightedAxes.add(plot.getYAxis());
          }
          for (final GraphAxis axis : unhighlightedAxes) {
             axis.unhighlight();
          }
 
          // now highlight the axes of the highlighted plots
          for (final Plot plot : highlightedPlots) {
             final PlottablePoint highlightedPoint = plot.getHighlightedPoint();
             plot.getXAxis().highlight(highlightedPoint);
             plot.getYAxis().highlight(highlightedPoint);
          }
 
          paint();
       }
    }
 
    private void handleMouseUpEvent(final MouseUpEvent event) {
       mouseDragLastPos = null;
 
       // Alert all the plots to the click event
       final Vector2 pos = new Vector2(event.getX(), event.getY());
 
       for (final Plot plot: containedPlots)
          plot.onClick(pos);
    }
 
    private void handleMouseOutEvent(final MouseOutEvent event) {
       mouseDragLastPos = null;
 
       // Ensure that all plots are unhighlighted, as are all axes
       for (final Plot plot : containedPlots) {
          plot.unhighlight();
          plot.getXAxis().unhighlight();
          plot.getYAxis().unhighlight();
       }
 
       paint();
    }
 
    private void layout() {
       final Set<GraphAxis> axes = new HashSet<GraphAxis>();
       for (final Plot plot : containedPlots) {
          axes.add(plot.getXAxis());
          axes.add(plot.getYAxis());
       }
       for (final GraphAxis axis : axes) {
          axis.layout();
       }
    }
 
    @Override
    public void setSize(final int widthInPixels, final int heightInPixels, final int newPaintEventId) {
       final CanvasElement canvas = drawing.getCanvas();
 
       if ((canvas.getClientWidth() != widthInPixels) ||
           (canvas.getClientHeight() != heightInPixels)) {
          drawing.setSize(widthInPixels, heightInPixels);
          paint(newPaintEventId);
       }
    }
 
    public void paint() {
       paint(SequenceNumber.getNext());
    }
 
    public void paint(final int newPaintEventId) {
       // guard against redundant paints
       if (previousPaintEventId != newPaintEventId) {
          previousPaintEventId = newPaintEventId;
 
          layout();
          drawing.clear();
          drawing.save();
          drawing.translate(.5, .5);
 
          // Draw any Loading... messages that might be requested
          if (loadingMessages.size() > 0) {
             showLoadingMessage(loadingMessages.get(0));
          }
 
          // Draw any value messages that might be requested
          if (valueMessages.size() > 0) {
             // We use the first (oldest) VALUE_MESSAGES_CAPACITY
             // messages in valueMessages, at least for now
             final int numMessages = Math.min(VALUE_MESSAGES_CAPACITY,
                                              valueMessages.size());
 
             showValueMessages(valueMessages.subList(0, numMessages));
          }
 
          // Draw the axes
          for (final Plot plot : containedPlots) {
             plot.getXAxis().paint(newPaintEventId);
             plot.getYAxis().paint(newPaintEventId);
          }
 
          // Now draw the data
          final Canvas canvas = Canvas.buildCanvas(drawing);
          for (final Plot plot : containedPlots) {
             plot.paint(canvas, newPaintEventId);
          }
 
          drawing.restore();
       }
    }
 
    /**
     * Shows the specified loading message.
     *
     * @param msg
     * 		a message to be shown at the bottom left corner of
     * 		the grapher.  Note that we require that msg is not
     * 		<tt>null</tt>
     */
    private void showLoadingMessage(DisplayMessage msg) {
       // Save old data to be restored later
       TextAlign oldTextAlign = drawing.getTextAlign();
       double oldLineWidth = drawing.getLineWidth();
 
       // Change settings
       drawing.setTextAlign(TextAlign.LEFT);
       drawing.setLineWidth(TEXT_LINE_WIDTH);
       drawing.setStrokeStyle(msg.getColor());
 
       // Actually write the text
       double bottom = height - LOADING_MSG_Y_MARGIN;
       double textTop = bottom - TEXT_HEIGHT;
       drawing.strokeText(msg.getText(), LOADING_MSG_X_MARGIN, textTop);
 
       // Restore old settings
       drawing.setTextAlign(oldTextAlign);
       drawing.setLineWidth(oldLineWidth);
       drawing.setStrokeStyle(Canvas.DEFAULT_COLOR);
    }
 
    /**
     * Shows the specified value messages.
     *
     * @param messages
     * 		a list of messages to be shown at the bottom right
     * 		of the grapher.  Note that we require that messages
     * 		is neither <tt>null</tt> nor empty, contains no
     * 		<tt>null</tt> elements, and has length less than or
     * 		equal to {@link #VALUE_MESSAGES_CAPACITY}.
     */
    private void showValueMessages(List<DisplayMessage> messages) {
       // Save old data to be restored later
       TextAlign oldTextAlign = drawing.getTextAlign();
       double oldLineWidth = drawing.getLineWidth();
 
       // Change settings
       drawing.setTextAlign(TextAlign.RIGHT);
       drawing.setLineWidth(TEXT_LINE_WIDTH);
 
       // Actually write the text
       double bottom = height - VALUE_MSG_Y_MARGIN;
       double x = width - VALUE_MSG_X_MARGIN;
       // Right edge X-value with right text alignment
 
       for (DisplayMessage msg : messages) {
          drawing.setStrokeStyle(msg.getColor());
 
          double textTop = bottom - TEXT_HEIGHT;
          String text = msg.getText();
 
          // Find left edge, given that we know right edge
          drawing.strokeText(text, x, textTop);
 
          // Move upwards for next loop iteration
          bottom = textTop - VALUE_MSG_GAP;
          textTop = bottom - TEXT_HEIGHT;
       }
 
       // Restore old settings
       drawing.setTextAlign(oldTextAlign);
       drawing.setLineWidth(oldLineWidth);
       drawing.setStrokeStyle(Canvas.DEFAULT_COLOR);
    }
 
    /**
     * Adds the given {@link Plot} to the collection of plots to be drawn.
     *
     * Note that a plot can only be added once to this PlotContainer's internal collection.
     *
     * @throws NullPointerException if plot is <code>null</code>
     */
    public void addPlot(final Plot plot) {
       if (plot == null) {
          throw new NullPointerException("Cannot add null plot");
       }
       containedPlots.add(plot);
       plot.registerPlotContainer(this);
 
       paint();
    }
 
    /**
     * Removes the given {@link Plot} from the collection of plots to be drawn.
     *
     * <p>Does nothing if plot is <code>null</code> or not contained by this {@link PlotContainer}<p>
     */
    public void removePlot(final Plot plot) {
       if (plot == null) {
          return;
       }
       containedPlots.remove(plot);
       plot.unregisterPlotContainer(this);
 
       paint();
    }
 
    /**
     * Adds a loading message
     *
     * <p>Note that we implement a FIFO policy for displaying loading
     * messages.  This means that messages that are posted early will
     * display until removed.</p>
     *
     * <p>This system of assigning integer IDs to messages and then
     * removing messages by ID is necessitated by the fact that data
     * is not required to come into the page in the same order
     * that it is requested.  A simple queue system thus doesn't
     * work for removal purposes.</p>
     *
     * @param message
     * 		the message to display whenever there are no older
     * 		messages left in the queue
     * @return
     * 		an integer ID that can be used to remove message whenever
     * 		it should no longer appear
     * @throws NullPointerException
     * 		if message is <tt>null</tt>
     * @see #removeLoadingMessage(int)
     */
    public final int addLoadingMessage(String message) {
       if (message == null) {
          throw new NullPointerException(
                "Null loading message not allowed");
       }
 
       int id = nextLoadingMessageId;
       nextLoadingMessageId++;
 
       loadingMessages.add(
             new DisplayMessage(id, message, LOADING_MSG_COLOR));
 
       return id;
    }
 
    /**
     * Removes the specified message from the queue of loading messages
     * to show.
     *
     * <p>It is guaranteed that this method will remove either 0
     * or 1 messages from the set of messages that could be shown.</p>
     *
     * @param messageId
     * 		the ID of the message to remove
     * @return
     * 		the string that was previously associated with messageId,
     * 		at least if messageId is in the list.  If messageId is
     * 		not in the list, returns <tt>null</tt>.
     * @see #addLoadingMessage(String)
     */
    public final String removeLoadingMessage(int messageId) {
       // Since IDs are assigned sequentially, starting at
       // INITIAL_MESSAGE_ID, this allows us to avoid a search
       // in some cases
       if (messageId < INITIAL_MESSAGE_ID ||
           messageId >= nextLoadingMessageId) {
          return null;
       }
 
       return removeMessage(loadingMessages, messageId);
    }
 
    /**
     * Removes the message with the specified ID from messages, if
     * such a message exists.  Otherwise, does nothing
     *
     * @param messages
     * 		the list of messages from which to remove a message
     * @param messageId
     * 		the ID of the message to remove from messages
     * @return
     * 		the string message that was removed from messages, or
     * 		<tt>null</tt> if there is no message in messages with
     * 		the messageId
     */
    private static String removeMessage(List<DisplayMessage> messages,
                                        int messageId) {
       Iterator<DisplayMessage> it = messages.iterator();
 
       String result = null;
 
       while (it.hasNext()) {
          DisplayMessage curr = it.next();
 
          if (curr.getId() == messageId) {
             result = curr.getText();
             it.remove();
          }
       }
 
       return result;
    }
 
    /**
     * Adds a value message
     *
     * <p>Note that we try to display all value messages, and indeed
     * guarantee that we will display all of them, as long as there
     * are less than or equal to {@link #VALUE_MESSAGES_CAPACITY}.
     * If there are more than this many messages, it is still possible
     * that all the messages will be drawn, but no guarantees are made
     * to which value messages will be drawn, whether some or all.
     * The count of value messages is always available through a call
     * to {@link #countValueMessages()}.</p>
     *
     * @param message
     * 		the value message to display if possible
     * @return
     * 		an integer ID that can be used to remove message whenever
     * 		it should no longer appear
     * @throws NullPointerException
     * 		if message or color is <tt>null</tt>
     * @see #removeValueMessage(int)
     */
    public final int addValueMessage(String message, Color color) {
       if (message == null) {
          throw new NullPointerException(
                "Null value message not allowed");
       }
 
       if (color == null) {
          throw new NullPointerException("Null color not allowed");
       }
 
       int id = nextValueMessageId;
       nextValueMessageId++;
 
       valueMessages.add(new DisplayMessage(id, message, color));
 
       return id;
    }
 
    /**
     * Removes the specified message from the list of value messages
     * to show.
     *
     * <p>It is guaranteed that this method will remove either 0
     * or 1 messages from the set of messages that could be shown.</p>
     *
     * @param messageId
     * 		the ID of the message to remove
     * @return
     * 		the string that was previously associated with messageId,
     * 		at least if messageId is in the list.  If messageId is
     * 		not in the list, returns <tt>null</tt>.
     * @see #addValueMessage(String)
     */
    public final String removeValueMessage(int messageId) {
       // Since IDs are assigned sequentially, starting at
       // INITIAL_MESSAGE_ID, this allows us to avoid a search
       // in some cases
       if (messageId < INITIAL_MESSAGE_ID ||
           messageId >= nextValueMessageId) {
          return null;
       }
 
       return removeMessage(valueMessages, messageId);
    }
 
    /**
     * Returns the number of value messages currently held in this
     * <tt>PlotContainer</tt>.
     *
     * @return
     * 		the number of messages that have been added using
     * 		{@link #addValueMessage(String)} but have not been
     * 		removed using {@link #removeValueMessage(int)}
     */
    public final int countValueMessages() {
       return valueMessages.size();
    }
 
    /**
     * A class representing a message ID and loading message.
     *
     * <p>Note that objects of this class are immutable and therefore
     * unconditionally thread-safe.  Or at least, in Java this holds.
     * In JavaScript every object is mutable, and there is only one
     * thread, so that doesn't really hold anymore.</p>
     */
    private static final class DisplayMessage
          implements Comparable<DisplayMessage> {
       private final int id;
       private final String text;
       private final Color color;
 
       /**
       * Creates a new {@link DisplayMessage DisplayMessage} object
        *
        * @param id
        * 		the ID this object should hold
        * @param text
        * 		the message this object should hold
        * @param color
        * 		the color at which the message should be drawn
        * @throws NullPointerException
        * 		if either text or color is <tt>null</tt>
        */
       public DisplayMessage(int id, String text, Color color) {
          if (text == null || color == null) {
             throw new NullPointerException(
                   "Null constructor parameter not allowed");
          }
 
          this.id = id;
          this.text = text;
          this.color = color;
       }
 
       /**
        * Returns the ID passed to this object's constructor when it
        * was created.
        *
        * @return
        * 		the ID this object holds
        */
       public int getId() {
          return id;
       }
 
       /**
        * Returns the message parameter passed to this object's
        * constructor when it was created.
        *
        * @return
        * 		the message this object holds
        */
       public String getText() {
          return text;
       }
 
       /**
        * Returns the color parameter passed to this object's
        * constructor when it was created.
        *
        * @return
        * 		the color this object holds
        */
       public Color getColor() {
          return color;
       }
 
       /**
        * Computes a hash code for this object.
        */
       @Override
       public int hashCode() {
          return id;
       }
 
       /**
        * Tells if this is logically equal to o.  Note that, if o
       * is not a DisplayMessage, then this and o must be unequal.
        * Note that equality is determined by ID.
        */
       @Override
       public boolean equals(Object o) {
          if (this == o) {
             return true;
          }
          if (!(o instanceof DisplayMessage)) {
             return false;
          }
          DisplayMessage other = (DisplayMessage)o;
          return compareTo(other) == 0;
       }
 
       /**
        * Compares this to other.  Note that comparison is by ID.
        */
       @Override
       public int compareTo(DisplayMessage other) {
          if (id > other.id) {
             return 1;
          }
          if (id < other.id) {
             return -1;
          }
          return 0;
       }
    }
 }
