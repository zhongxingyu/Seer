 package io.metacake.core;
 
 import io.metacake.core.common.window.CakeWindow;
 import io.metacake.core.input.InputDeviceName;
 import io.metacake.core.input.system.InputDevice;
 import io.metacake.core.output.OutputDeviceName;
 import io.metacake.core.output.system.OutputDevice;
 import io.metacake.core.process.state.GameState;
 
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.Map;
 
 /**
  * The {@code BootstrapBuilder} provides a mechanism for building a {@link Bootstrapper}.
  * <p>
  *     In order to create a {@link Bootstrapper} the withWindow and withInitialState methods must be called before
  *     a call to createBootstrap. The other methods may also be called to change some options for the game, but at a
  *     minimum withWindow and withInitialState must be called with non-null arguments.
  *
  * @author florence
  * @author rpless
  */
 public class BootstrapBuilder {
     private CakeWindow window;
     private Map<InputDeviceName, InputDevice> inputDevices;
     private Map<OutputDeviceName, OutputDevice> outputDevices;
     private GameState initialState;
     private long loopTime;
 
     public BootstrapBuilder() {
         inputDevices = new HashMap<>();
         outputDevices = new HashMap<>();
         loopTime = Bootstrapper.DEFAULT_LOOP_MILLIS;
     }
 
     /**
      * Creates a {@link Bootstrapper} that is ready to run a game.
      * <p>
      *     If the caller has not provided this builder with a {@link CakeWindow} and an initial {@link GameState},
      *     then a {@link RuntimeException} will be raised.
      * @return Creates a {@link Bootstrapper} that will use the given {@link CakeWindow},
      * devices, initial {@link GameState}, and loop time,
      */
     public Bootstrapper createBootstrap() {
         validateCreateBootstrapInputs();
         return new Bootstrapper(window, inputDevices, outputDevices, initialState, loopTime);
     }
 
     /**
      * Validates the input contract for the createBootstrap method.
      */
     private void validateCreateBootstrapInputs() {
        if(window == null) {
            throw new IllegalStateException("Cannot create bootstrapper without a window");
        } else if (initialState == null) {
            throw new IllegalStateException("Cannot create bootstrapper without an initial state");
        } else if (outputDevices.isEmpty()) {
            throw new IllegalStateException("Cannot create bootstrapper without an output device");
         }
     }
 
     public void createAndLaunch() {
         createBootstrap().setupAndLaunchGame();
     }
 
     /**
      * @param window The {@link CakeWindow} that this will use to generate a {@link Bootstrapper}.
      * @return Returns this {@link BootstrapBuilder}
      */
     public BootstrapBuilder withWindow(CakeWindow window) {
         this.window = window;
         return this;
     }
 
     /**
      * @param devices The collection of {@link InputDevice}s that this will use to generate a {@link Bootstrapper}.
      * @return Returns this {@link BootstrapBuilder}
      */
     public BootstrapBuilder withInputDevices(InputDevice...devices) {
         Arrays.asList(devices).forEach(d -> inputDevices.put(d.name(),d));
         return this;
     }
 
     /**
      * @param devices The collection of {@link OutputDevice}s that this will use to generate a {@link Bootstrapper}.
      * @return Returns this {@link BootstrapBuilder}
      */
     public BootstrapBuilder withOutputDevices(OutputDevice...devices) {
         Arrays.asList(devices).forEach(d -> outputDevices.put(d.name(),d));
         return this;
     }
 
     /**
      * @param initialState The {@link GameState}s that this will use to generate a {@link Bootstrapper}.
      * @return Returns this {@link BootstrapBuilder}
      */
     public BootstrapBuilder withInitialState(GameState initialState) {
         this.initialState = initialState;
         return this;
     }
 
     /**
      * @param loopTime The loop time that this will use to generate a {@link Bootstrapper}.
      * @return Returns this {@link BootstrapBuilder}
      */
     public BootstrapBuilder withLoopTime(long loopTime) {
         this.loopTime = loopTime;
         return this;
     }
 }
