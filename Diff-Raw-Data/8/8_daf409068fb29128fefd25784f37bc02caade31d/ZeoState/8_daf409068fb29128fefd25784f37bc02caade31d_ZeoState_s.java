 package org.bodytrack.client;
 
 import gwt.g2d.client.graphics.Color;
 
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Map;
 
 /**
  * @author Chris Bartley (bartley@cmu.edu)
  */
 public enum ZeoState {
    NO_DATA(0, "No Data", new Color(0,0,0)),
   DEEP(1, "Deep", new Color(0x22, 0x8B, 0x22)),
   LIGHT(2, "Light", new Color(0xA9, 0xA9, 0xA9)),
   REM(3, "REM", new Color(0x90, 0xEE, 0x90)),
   WAKE(4, "Wake", new Color(0xFF, 0x45, 0x00));
 
    private static final Map<Integer, ZeoState> VALUE_TO_STATE_MAP;
 
    static {
       final Map<Integer, ZeoState> valueToStateMap = new HashMap<Integer, ZeoState>(ZeoState.values().length);
       for (final ZeoState zeoState : ZeoState.values()) {
          valueToStateMap.put(zeoState.getValue(), zeoState);
       }
       VALUE_TO_STATE_MAP = Collections.unmodifiableMap(valueToStateMap);
    }
 
    /**
     * Returns the <code>ZeoState</code> associated with the given <code>value</code>, or <code>null</code> if no such
     * state exists.
     */
    public static ZeoState findByValue(final int value) {
       return VALUE_TO_STATE_MAP.get(value);
    }
 
    private final int value;
    private final String name;
    private final Color color;
 
    ZeoState(final int value, final String name, final Color color) {
       this.value = value;
       this.name = name;
       this.color = color;
    }
 
    public int getValue() {
       return value;
    }
 
    public String getName() {
       return name;
    }
 
    public Color getColor() {
       return color;
    }
 
    @Override
    public String toString() {
       final StringBuilder sb = new StringBuilder();
       sb.append("ZeoState");
       sb.append("{value=").append(value);
       sb.append(", name='").append(name).append('\'');
       sb.append('}');
       return sb.toString();
    }
 }
