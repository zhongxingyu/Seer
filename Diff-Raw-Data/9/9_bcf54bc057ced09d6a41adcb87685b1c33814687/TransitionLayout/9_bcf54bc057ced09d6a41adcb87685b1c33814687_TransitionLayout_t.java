 /*
  * Copyright 2009 the original author or authors.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  */
 
 package griffon.transitions;
 
 import org.pushingpixels.trident.Timeline;
 import org.pushingpixels.trident.callback.TimelineCallback;
 import static org.pushingpixels.trident.Timeline.TimelineState;
 import com.bric.image.transition.Transition2D;
 import com.bric.image.transition.Transition2DInstruction;
 import com.bric.image.transition.vanilla.BlendTransition2D;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.CardLayout;
 import java.awt.Container;
 import java.awt.Component;
 import java.awt.Dimension;
 import java.awt.GraphicsEnvironment;
 import java.awt.GraphicsDevice;
 import java.awt.GraphicsConfiguration;
 import java.awt.RenderingHints;
 import java.awt.image.BufferedImage;
 import java.util.Map;
 import java.util.LinkedHashMap;
 import javax.swing.JComponent;
 
 /**
  * @author Andres Almiray <aalmiray@users.sourceforge.net>
  */
 public class TransitionLayout extends CardLayout {
    public static final String NAME = "name";
    public static final String TRANSITION = "transition";
    public static final String DURATION = "duration";
 
    private static final Transition2D DEFAULT_TRANSITION = new BlendTransition2D();
    private static final long DEFAULT_DURATION = 500L;
    private static final long UNSPECIFIED_DURATION = -1L;
 
    // properties
    private final Map<String, TransitionInfo> _components = new LinkedHashMap<String, TransitionInfo>();
    private long _defaultDuration = DEFAULT_DURATION;
    private Transition2D _defaultTransition = DEFAULT_TRANSITION;
    private boolean _mirrorTransition = true;
    private boolean _skipTransitions = false;
 
    // fields
    private TransitionAnimator _transitionAnimator;
 
    private enum Direction {
       FORWARD, REVERSE
    }
 
    public TransitionLayout() {
       super();
    }
 
    public Transition2D getDefaultTransition() {
       return _defaultTransition;
    }
 
    public void setDefaultTransition(Transition2D defaultTransition){
       _defaultTransition = (defaultTransition == null) ? DEFAULT_TRANSITION : defaultTransition;
    }
 
    public long getDefaultDuration() {
       return _defaultDuration;
    }
 
    public void setDefaultDuration(long defaultDuration){
       _defaultDuration = (defaultDuration < DEFAULT_DURATION) ? DEFAULT_DURATION : defaultDuration;
    }
 
    public boolean isMirrorTransition() {
       return _mirrorTransition;
    }
 
    public void setMirrorTransition(boolean mirrorTransition) {
       _mirrorTransition = mirrorTransition;
    }
 
    public boolean isSkipTransitions() {
       return _skipTransitions;
    }
 
    public void setSkipTransitions(boolean skipTransitions) {
       _skipTransitions = skipTransitions;
    }
 
    /**
     * Adds the specified component to this card layout's internal table of names.<p>
     * The object specified by constraints may be a string or a Map.
     * This layout stores the string as a key-value pair that can be used for random
     * access to a particular card. By calling the show method, an application can
     * display the component with the specified name.<p>
     * If the constraints object is a Map it must contain the following keys:<ul>
     * <li>name - String: required</li>
     * <li>transition - Transition2D: optional</li>
     * <li>duration - long: optional</li>
     * </ul>
     */
    public void addLayoutComponent(Component component, Object constraints) {
       String name = null;
       Transition2D transition = null;
       long duration = UNSPECIFIED_DURATION;
       if(constraints instanceof Map) {
          Map map = (Map) constraints;
          name = (String) map.get(NAME);
          if(name == null ) name = "card" + _components.size();
          transition = (Transition2D) map.get(TRANSITION);
          if(map.containsKey(DURATION)) {
             try {
                duration = ((Number) map.get(DURATION)).longValue();
             } catch( Exception e ) {
                duration = UNSPECIFIED_DURATION;
             }
          }
       } else {
          name = String.valueOf(constraints);
          if("null".equals(name)) name = "card" + _components.size();
       }
       if(transition == null) transition = NullTransition.getInstance();
       if(!(component instanceof TransitionPanel)) {
          _components.put(name, new TransitionInfo(component, transition, duration));
       }
       super.addLayoutComponent(component, name);
    }
 
    public void removeLayoutComponent(Component component) {
       if(!(component instanceof TransitionPanel)) {
          for(Map.Entry<String, TransitionInfo> entry: _components.entrySet()) {
             if(entry.getValue().getComponent() == component) {
                _components.remove(entry.getKey());
                break;
             }
          }
       }
       super.removeLayoutComponent(component);
    }
 
    public void first(Container parent) {
      if(_components.size() == 0) return;
       if(_skipTransitions) {
          super.first(parent);
          return;
       }
       if(isAnimating()) return;
       synchronized(parent.getTreeLock()) {
          int currentCardIndex = getCurrentCardIndex(parent);
          if(currentCardIndex == 0) return;
          triggerTransition(parent, 0, currentCardIndex, Direction.REVERSE);
       }
    }
 
    public void next(Container parent) {
      if(_components.size() == 0) return;
       if(_skipTransitions) {
          super.next(parent);
          return;
       }
       if(isAnimating()) return;
       synchronized (parent.getTreeLock()) {
          int currentCardIndex = getCurrentCardIndex(parent);
          int nextCardIndex = (currentCardIndex + 1) % _components.size();
          if(currentCardIndex == nextCardIndex) return;
          triggerTransition(parent, currentCardIndex, nextCardIndex, Direction.FORWARD);
       }
    }
 
    public void previous(Container parent) {
      if(_components.size() == 0) return;
       if(_skipTransitions) {
          super.previous(parent);
          return;
       }
       if(isAnimating()) return;
       synchronized (parent.getTreeLock()) {
          int currentCardIndex = getCurrentCardIndex(parent);
          int nextCardIndex = currentCardIndex > 0 ? currentCardIndex - 1 : _components.size() - 1;
          if(currentCardIndex == nextCardIndex) return;
          triggerTransition(parent, nextCardIndex, currentCardIndex, Direction.REVERSE);
       }
    }
 
    public void last(Container parent) {
      if(_components.size() == 0) return;
       if(_skipTransitions) {
          super.last(parent);
          return;
       }
       if(isAnimating()) return;
       synchronized (parent.getTreeLock()) {
          int currentCardIndex = getCurrentCardIndex(parent);
          int nextCardIndex = _components.size() - 1;
          if(currentCardIndex == nextCardIndex) return;
          triggerTransition(parent, currentCardIndex, nextCardIndex, Direction.FORWARD);
       }
    }
 
    public void show(Container parent, String name) {
      if(_components.size() == 0) return;
       if(_skipTransitions) {
          super.show(parent, name);
          return;
       }
       if(isAnimating()) return;
       synchronized (parent.getTreeLock()) {
          int nextCardIndex = cardIndexOf(name);
          int currentCardIndex = getCurrentCardIndex(parent);
          if(nextCardIndex != -1 && currentCardIndex != nextCardIndex) {
             triggerTransition(parent,
                currentCardIndex < nextCardIndex ? currentCardIndex : nextCardIndex,
                currentCardIndex < nextCardIndex ? nextCardIndex : currentCardIndex,
                currentCardIndex < nextCardIndex ? Direction.FORWARD : Direction.REVERSE);
          }
       }
    }
 
    private TransitionInfo transitionInfoAt(int index) {
       int count = 0;
       for(Map.Entry<String, TransitionInfo> entry: _components.entrySet()) {
          if(count == index) {
             return entry.getValue();
          }
          count++;
       }
       return null;
    }
 
    private int cardIndexOf(String name) {
       int index = 0;
       for(Map.Entry<String, TransitionInfo> entry: _components.entrySet()) {
          if(entry.getKey().equals(name)) {
             return index;
          }
          index++;
       }
       return -1;
    }
 
    private int getCurrentCardIndex(Container parent) {
       int size = _components.size();
       for(int i = 0; i < size; i++) {
          if(parent.getComponent(i).isVisible()) {
             return i;
          }
       }
       return -1;
    }
 
    private boolean isAnimating() {
       return _transitionAnimator != null &&
              _transitionAnimator.isAnimating();
    }
 
    private void triggerTransition(Container parent, int fromIndex, int toIndex, Direction direction) {
       if(_transitionAnimator != null &&
          _transitionAnimator.isAnimating()) return;
 
       if(fromIndex < 0 || toIndex < 0) return;
       if(!_mirrorTransition && direction == Direction.REVERSE) {
          direction = direction.FORWARD;
          int index = fromIndex;
          fromIndex = toIndex;
          toIndex = index;
       }
       final TransitionInfo from = transitionInfoAt(fromIndex);
       final TransitionInfo to   = transitionInfoAt(toIndex);
       if(from.getComponent() == null || to.getComponent() == null) return;
       long duration = from.getDuration();
       if(duration == UNSPECIFIED_DURATION) duration = _defaultDuration;
       Transition2D transition = from.getTransition() != NullTransition.getInstance() ? from.getTransition() : _defaultTransition;
 
       _transitionAnimator = new TransitionAnimator(from, to, parent, duration, transition, direction);
       _transitionAnimator.play();
    }
 
    private static class NullTransition extends Transition2D {
       private static NullTransition _instance = new NullTransition();
 
       private static NullTransition getInstance() {
          return _instance;
       }
 
       public Transition2DInstruction[] getInstructions(float progress, Dimension size) {
          return null;
       }
    }
 
    private static class TransitionInfo {
       private final Component _component;
       private final Transition2D _transition;
       private final long _duration;
 
       public TransitionInfo(Component component, Transition2D transition, long duration) {
          _component = component;
          _transition = transition;
          _duration = duration;
       }
 
       public Component getComponent() {
          return _component;
       }
 
       public Transition2D getTransition() {
          return _transition;
       }
 
       public long getDuration() {
          return _duration;
       }
    }
 
    private static class TransitionAnimator {
       private static final String NAME_CONSTRAINT = "__TransitionLayout.panel__";
       private final Timeline _timeline;
       private final Direction _direction;
 
       public TransitionAnimator(final TransitionInfo from, final TransitionInfo to, final Container container, long duration, Transition2D transition, final Direction direction) {
          _direction = direction;
          final TransitionPanel panel = new TransitionPanel(from.getComponent(), to.getComponent(), transition);
          _timeline = new Timeline(container);
          _timeline.setDuration(duration);
          _timeline.addCallback(new TimelineCallback() {
             public void onTimelineStateChanged(TimelineState oldState,
                                                TimelineState newState,
                                                float durationFraction,
                                                float timelinePosition) {
                if(newState == TimelineState.READY) {
                   container.add(panel, NAME_CONSTRAINT);
                   panel.setVisible(true);
                   if(direction == Direction.FORWARD) {
                      from.getComponent().setVisible(false);
                   } else {
                      to.getComponent().setVisible(false);
                   }
                } else if(newState == TimelineState.DONE) {
                   container.remove(panel);
                   if(direction == Direction.FORWARD) {
                      to.getComponent().setVisible(true);
                   } else {
                      from.getComponent().setVisible(true);
                   }
                   _timeline.cancel();
                }
             }
             public void onTimelinePulse(float durationFraction,
                                        float timelinePosition) {
                panel.setProgress(durationFraction);
             }
          });
       }
 
       public boolean isAnimating() {
          TimelineState state = _timeline.getState();
          return state != TimelineState.DONE && state != TimelineState.IDLE;
       }
 
       public void play() {
           if(_direction == Direction.FORWARD) {
              _timeline.play();
           } else {
              _timeline.playReverse();
           }
       }
    }
 
    private static class TransitionPanel extends JComponent {
       private final BufferedImage _img1;
       private final BufferedImage _img2;
       private final Transition2D _transition;
       private float _progress = 0f;
 
       public TransitionPanel(Component c1, Component c2, Transition2D transition) {
          _img1 = grabImage(c1);
          _img2 = grabImage(c2);
          _transition = transition;
       }
 
       public void setProgress(float progress) {
          _progress = progress;
       }
 
       public void paintComponent(Graphics g) {
          Graphics2D g2d = (Graphics2D) g;
          g2d.setRenderingHints(getRenderingHints());
          _transition.paint(g2d, _img1, _img2, _progress);
       }
    }
 
    private static BufferedImage grabImage(Component component) {
       BufferedImage image = null;
       GraphicsEnvironment genv = GraphicsEnvironment.getLocalGraphicsEnvironment();
       GraphicsDevice gd = genv.getDefaultScreenDevice();
       GraphicsConfiguration gc = gd.getDefaultConfiguration();
       if(gc.getColorModel().hasAlpha()) {
          image = gc.createCompatibleImage(component.getSize().width,
                                           component.getSize().height);
       } else {
          image = new BufferedImage(component.getSize().width,
                                    component.getSize().height,
                                    BufferedImage.TYPE_INT_ARGB);
       }
       Graphics g = image.getGraphics();
       component.paint(g);
       g.dispose();
       return image;
    }
 
    private static RenderingHints getRenderingHints() {
       RenderingHints hints = new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
       hints.put(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
       hints.put(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
       hints.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
       hints.put(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
       return hints;
    }
 }
