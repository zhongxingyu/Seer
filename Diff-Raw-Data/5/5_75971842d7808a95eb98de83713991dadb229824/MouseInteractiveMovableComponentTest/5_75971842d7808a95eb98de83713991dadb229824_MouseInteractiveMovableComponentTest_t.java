 /*
  *  Copyright Pastuszka Przemyslaw, University of Wroclaw, Poland (c) 2013.
  */
 
 package pl.rtshadow.bezier.components;
 
 import static com.google.common.collect.HashMultimap.create;
 import static org.mockito.Matchers.eq;
 import static org.mockito.Mockito.atLeastOnce;
 import static org.mockito.Mockito.never;
 import static org.mockito.Mockito.verify;
 import static org.mockito.Mockito.when;
 import static pl.rtshadow.bezier.bridge.events.MouseAction.MOUSE_DRAGGED;
 import static pl.rtshadow.bezier.bridge.events.MouseAction.MOUSE_PRESSED;
 import static pl.rtshadow.bezier.bridge.events.MouseActionData.ButtonPressed.LEFT;
 import static pl.rtshadow.bezier.bridge.events.MouseActionData.ButtonPressed.RIGHT;
 import static pl.rtshadow.bezier.components.actions.ComponentAction.REMOVED;
 
import java.util.EnumSet;

 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.mockito.ArgumentCaptor;
 import org.mockito.InjectMocks;
 import org.mockito.Mock;
 import org.mockito.runners.MockitoJUnitRunner;
 
 import com.google.common.collect.Multimap;
 
 import pl.rtshadow.bezier.bridge.components.ExternalMouseDrivenComponent;
 import pl.rtshadow.bezier.bridge.events.MouseAction;
 import pl.rtshadow.bezier.bridge.events.MouseActionData;
 import pl.rtshadow.bezier.bridge.events.MouseActionListener;
 import pl.rtshadow.bezier.components.listeners.ComponentActionListener;
 
 @RunWith(MockitoJUnitRunner.class)
 public class MouseInteractiveMovableComponentTest {
   private static final Coordinates ZERO_POINT = new Coordinates(0, 0);
 
   @Mock
   private ComponentActionListener removalListener;
   @Mock
   private ExternalMouseDrivenComponent externalMouseDrivenComponent;
   @InjectMocks
   private MouseInteractiveMovableComponent mouseInteractiveMovableComponent;
 
   private Multimap<MouseAction, MouseActionListener> registeredListeners = create();
 
   @Before
   public void setup() {
     mouseInteractiveMovableComponent.addListener(REMOVED, removalListener);
 
     when(externalMouseDrivenComponent.getCoordinates()).thenReturn(ZERO_POINT);
 
     retrieveRegisteredListeners();
   }
 
   @Test
   public void notifiesListenersOnRightClickAndRemovesUnderlyingComponent() {
     notifyAll(MOUSE_PRESSED, new MouseActionData(ZERO_POINT, RIGHT));
 
     verify(removalListener).onComponentAction(REMOVED);
     verify(externalMouseDrivenComponent).remove();
   }
 
   @Test
   public void doesNothingForLeftClick() {
     notifyAll(MOUSE_PRESSED, new MouseActionData(ZERO_POINT, LEFT));
 
     verify(removalListener, never()).onComponentAction(REMOVED);
     verify(externalMouseDrivenComponent, never()).remove();
   }
 
   @Test
   public void movesComponentWhenDraggedByMouse() {
     notifyAll(MOUSE_PRESSED, new MouseActionData(new Coordinates(100, 100), LEFT));
     notifyAll(MOUSE_DRAGGED, new MouseActionData(new Coordinates(200, 300), LEFT));
 
     verify(externalMouseDrivenComponent).setCoordinates(new Coordinates(100, 200));
   }
 
   private void retrieveRegisteredListeners() {
    for (MouseAction action : EnumSet.of(MOUSE_PRESSED, MOUSE_DRAGGED)) {
       ArgumentCaptor<MouseActionListener> captor = ArgumentCaptor.forClass(MouseActionListener.class);
 
       verify(externalMouseDrivenComponent, atLeastOnce()).addMouseActionListener(eq(action), captor.capture());
       registeredListeners.putAll(action, captor.getAllValues());
     }
   }
 
   private void notifyAll(MouseAction action, MouseActionData mouseActionData) {
     for (MouseActionListener listener : registeredListeners.get(action)) {
       listener.onMouseAction(mouseActionData);
     }
   }
 }
