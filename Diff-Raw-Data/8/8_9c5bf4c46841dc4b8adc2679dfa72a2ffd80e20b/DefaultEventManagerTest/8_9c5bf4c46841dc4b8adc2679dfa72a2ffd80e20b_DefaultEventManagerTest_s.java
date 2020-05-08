 package ru.vmsoftware.events;
 
 import org.fest.assertions.api.Assertions;
 import org.junit.Before;
 import org.junit.Test;
 import org.mockito.*;
 import ru.vmsoftware.events.adapters.MethodAdapter;
 import ru.vmsoftware.events.adapters.SimpleAdapter;
 import ru.vmsoftware.events.annotations.ManagedBy;
 import ru.vmsoftware.events.filters.EqualsFilter;
 import ru.vmsoftware.events.filters.Filter;
 import ru.vmsoftware.events.filters.Filters;
 import ru.vmsoftware.events.references.ManagementType;
 
 import static org.fest.assertions.api.Assertions.assertThat;
 import static org.mockito.Mockito.never;
 import static org.mockito.Mockito.verify;
 
 /**
  * @author Vyacheslav Mayorov
  * @since 2013-28-04
  */
public class DefaultEventManagerTest {
 
     @Mock
     private EventListener<Object> listener;
 
     @Mock
     private EventListener<Object> listener2;
 
     
     private Object eventType = new Object();
     private Object eventData = new Object();
     private GenericEvent<Object,Object,Object> event =
             new GenericEvent<Object, Object, Object>(this, eventType, eventData);
 
 
     private ArgumentCaptor<Object> eventCaptor = ArgumentCaptor.forClass(Object.class);
     private ArgumentCaptor<Object> eventCaptor2 = ArgumentCaptor.forClass(Object.class);
 
     private DefaultEventManager manager = new DefaultEventManager();
 
     private void assertGenericEvent(Object event, Object emitter, Object type, Object data) {
         assertThat(event).isInstanceOf(GenericEvent.class);
         final GenericEvent<?,?,?> genericEvent = (GenericEvent<?,?,?>) event;
         assertThat(genericEvent.getEmitter()).isSameAs(emitter);
         assertThat(genericEvent.getType()).isSameAs(type);
         assertThat(genericEvent.getData()).isSameAs(data);
     }
 
     @Before
     public void setUp() throws Exception {
         MockitoAnnotations.initMocks(this);
         Mockito.when(listener.onEvent(eventCaptor.capture())).thenReturn(true);
         Mockito.when(listener2.onEvent(eventCaptor2.capture())).thenReturn(true);
     }
 
     @Test(expected = NullPointerException.class)
     public void testListenDoesntAcceptNullPointersForEmitter() throws Exception {
         manager.listen(null, Filters.any(), listener);
     }
 
     @Test(expected = NullPointerException.class)
     public void testListenDoesntAcceptNullPointersForType() throws Exception {
         manager.listen(this, null, listener);
     }
 
     @Test(expected = NullPointerException.class)
     public void testListenDoesntAcceptNullPointersForListener2() throws Exception {
         manager.listen(this, Filters.any(), null);
     }
 
     @Test(expected = NullPointerException.class)
     public void testListenDoesntAcceptNullPointersForFilter() throws Exception {
         manager.listen(null, listener);
     }
 
     @Test(expected = NullPointerException.class)
     public void testListenDoesntAcceptNullPointersForListener() throws Exception {
         manager.listen(Filters.any(), null);
     }
 
     @Test(expected = NullPointerException.class)
     public void testEmitDoesntAcceptNullPointersForEmitter() throws Exception {
         manager.emit(null, eventType, eventData);
     }
 
     @Test(expected = NullPointerException.class)
     public void testEmitDoesntAcceptNullPointersForType() throws Exception {
         manager.emit(this, null, eventData);
     }
 
     @Test(expected = NullPointerException.class)
     public void testEmitDoesntAcceptNullPointersForEvent() throws Exception {
         manager.emit(null);
     }
 
     @Test
     public void testEmitAcceptNullPointersForData() throws Exception {
         manager.emit(this, eventType, null);
     }
 
     @Test
     public void testEmittedEventDeliveredCorrectly() throws Exception {
         manager.listen(this, Filters.any(), listener);
         manager.emit(event);
         verify(listener).onEvent(event);
     }
 
     @Test
     public void testEmittedEventDeliveredCorrectly2() throws Exception {
         manager.listen(Filters.any(), listener);
         manager.emit(this, eventType, eventData);
         assertGenericEvent(eventCaptor.getValue(), this, eventType, eventData);
     }
 
     @Test
     public void testEmitShouldCallListenersRegisteredWithInterfaceOrParentClass() throws Exception {
        manager.listen(CharSequence.class, Filters.any(), listener);
         manager.emit(event);
         verify(listener).onEvent(event);
     }
 
     @Test
     public void testEmittedEventsDeliveredIfListenerAddForEmitterClass() throws Exception {
         manager.listen(DefaultEventManagerTest.class, Filters.any(), listener);
         manager.emit(event);
         verify(listener).onEvent(event);
     }
 
     @Test
     public void testManagerDoesntCallListenerIfEventTypeNotMatched() throws Exception {
         manager.listen(this, new EqualsFilter<Object>(eventType), listener);
         manager.listen(this, new EqualsFilter<String>("xyz"), listener2);
         verifyOnlyFirstCalled();
     }
 
     @Test
     public void testManagerDoesntCallListenerIfEmitterNotMatched() throws Exception {
         manager.listen(this, Filters.any(), listener);
         manager.listen(Filter.class, Filters.any(), listener2);
         verifyOnlyFirstCalled();
     }
 
     @Test
     public void testManagerBreaksExecutionIfSomeoneListenerReturnsFalse() throws Exception {
         Mockito.when(listener.onEvent(event)).thenReturn(false);
 
         manager.listen(this, Filters.any(), listener);
         manager.listen(this, Filters.any(), listener2);
         manager.emit(event);
 
         verify(listener).onEvent(event);
         verify(listener2, never()).onEvent(Matchers.any());
     }
 
     @ManagedBy(ManagementType.CONTAINER)
     private static class ManagedContainerListener extends SimpleAdapter {
         @Override
         public boolean onEvent(Object event) {
             return true;
         }
     }
 
     @Test
     public void testManagerCleanupsDataIfListenerIsManagedByContainer() throws Exception {
         ManagedContainerListener l = new ManagedContainerListener();
         manager.listen(this, Filters.any(), l);
         assertThat(manager.isClean()).isFalse();
         l = null;
 
         TestUtils.forceGC();
 
         manager.emit(TestUtils.NULL, eventType, null);
         assertThat(manager.isClean()).isTrue();
     }
 
     @ManagedBy(ManagementType.MANUAL)
     private static class ManualManagedListener extends SimpleAdapter {
         @Override
         public boolean onEvent(Object event) {
             return true;
         }
     }
 
     @Test
     public void testManagerShouldntCleanupMethodAdapterIfObjectManagedManual() throws Exception {
 
         ManualManagedListener l = new ManualManagedListener();
         final MethodAdapter methodAdapter = new MethodAdapter(l, "onEvent");
         manager.listen(this, Filters.any(), methodAdapter);
         l = null;
 
         TestUtils.forceGC();
 
         manager.emit(TestUtils.NULL, eventType, null);
         assertThat(manager.isClean()).isFalse();
 
     }
 
     @Test
     public void testManagerCleanupsDataIfEmitterNoMoreReachableByStrongRef() throws Exception {
 
         byte[] e = new byte[1024];
         manager.listen(e, Filters.any(), listener);
         assertThat(manager.isClean()).isFalse();
         e = null;
 
         TestUtils.forceGC();
 
         // to force cleanup of stales
         manager.emit(TestUtils.NULL, eventType, null);
         assertThat(manager.isClean()).isTrue();
     }
 
     @Test
     public void testManagerHoldsListenerByStrongRef() throws Exception {
         EventListener<Object> l = new SimpleAdapter<Object>() {
             @Override
             public boolean onEvent(Object event) {
                 return true;
             }
         };
         manager.listen(this, Filters.any(), l);
         assertThat(manager.isClean()).isFalse();
 
         l = null;
         TestUtils.forceGC();
 
         // to force cleanup of stales
         manager.emit(TestUtils.NULL, eventType, null);
         assertThat(manager.isClean()).isFalse();
     }
 
     @Test
     public void testMuteCorrectlyRemovesListener() throws Exception {
 
         manager.listen(this, Filters.any(), listener);
         manager.listen(this, Filters.any(), listener2);
 
         assertThat(manager.isClean()).isFalse();
         manager.mute(listener2);
 
         Assertions.assertThat((Object) manager.list.iterator().next().listenerProvider.get()).isEqualTo(listener);
         manager.mute(listener);
 
         assertThat(manager.isClean()).isTrue();
     }
 
     @Test
     public void testMuteCorrectlyRemovesByCounterpart() throws Exception {
         manager.listen(this, Filters.any(), new MethodAdapter(listener, "equals"));
         manager.mute(listener);
         assertThat(manager.isClean()).isTrue();
     }
 
     @Test
     public void testManagerCanCreateRegistrar() throws Exception {
         final Registrar registrar = manager.createRegistrar();
         Assertions.assertThat(registrar).isNotNull();
     }
 
     @Test
     public void testRegistrarHoldsStateByWeakReferences() throws Exception {
 
         ManagedContainerListener l = new ManagedContainerListener();
         final Registrar registrar = manager.createRegistrar();
         registrar.listen(this, Filters.any(), l);
         manager.mute(l);
         l = null;
 
         TestUtils.forceGC();
 
         assertThat(registrar.isClean()).isTrue();
         assertThat(manager.isClean()).isTrue();
     }
 
     @Test
     public void testRegistrarCorrectlyRegisterListeners() throws Exception {
         final Registrar registrar = manager.createRegistrar();
         registrar.listen(Filters.any(), listener);
         registrar.listen(Filters.any(), listener2);
         verifyBothCalled();
     }
 
     @Test
     public void testRegistrarRegisterListeners() throws Exception {
         final Registrar registrar = manager.createRegistrar();
         registrar.listen(this, Filters.any(), listener);
         registrar.listen(this, Filters.any(), listener2);
         verifyBothCalled();
     }
 
     @Test
     public void testRegistrarCleanupListeners() throws Exception {
         final Registrar registrar = manager.createRegistrar();
         registrar.listen(this, Filters.any(), listener);
         registrar.listen(this, Filters.any(), listener2);
         registrar.cleanup();
         verifyNoneCalled();
     }
 
     @Test
     public void testRegistrarRemoveListeners() throws Exception {
         final Registrar registrar = manager.createRegistrar();
         registrar.listen(this, Filters.any(), listener);
         registrar.listen(this, Filters.any(), listener2);
         registrar.mute(listener2);
         verifyOnlyFirstCalled();
     }
 
     @Test
     public void testRegisterCanRemoveOnlyRegisteredListeners() throws Exception {
         final Registrar registrar = manager.createRegistrar();
         registrar.listen(this, Filters.any(), listener2);
         manager.listen(this, Filters.any(), listener);
         registrar.mute(listener);
         registrar.mute(listener2);
         verifyOnlyFirstCalled();
     }
 
     @Test
     public void testRegistrarCleanupOnlyRegisteredListeners() throws Exception {
         final Registrar registrar = manager.createRegistrar();
         registrar.listen(this, Filters.any(), listener2);
         manager.listen(this, Filters.any(), listener);
         registrar.cleanup();
         verifyOnlyFirstCalled();
     }
 
     @Test
     public void testManagerShouldCleanupMethodAdapterIfObjectNoMoreReachable() throws Exception {
 
         Object l = new Object();
 
         final MethodAdapter methodAdapter = new MethodAdapter(l, "hashCode");
         manager.listen(this, Filters.any(), methodAdapter);
 
         l = null;
         TestUtils.forceGC();
 
         manager.emit(TestUtils.NULL, eventType, null);
         assertThat(manager.isClean()).isTrue();
     }
 
     @Test
     public void testCleanupRemovesAllListeners() throws Exception {
 
         manager.listen(this, Filters.any(), listener);
         manager.listen(this, Filters.any(), listener2);
         assertThat(manager.isClean()).isFalse();
 
         manager.cleanup();
         assertThat(manager.isClean()).isTrue();
 
     }
 
     private void verifyOnlyFirstCalled() {
         manager.emit(event);
         verify(listener).onEvent(event);
         verify(listener2, never()).onEvent(Matchers.any());
     }
 
     private void verifyBothCalled() {
         manager.emit(event);
         verify(listener).onEvent(event);
         verify(listener2).onEvent(event);
     }
 
     private void verifyNoneCalled() {
         manager.emit(event);
         verify(listener, never()).onEvent(Matchers.any());
         verify(listener2, never()).onEvent(Matchers.any());
     }
 
 
 }
