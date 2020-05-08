 package com.tngtech.internal.perceptual.components;
 
 import com.google.common.collect.Lists;
 import com.google.common.collect.Maps;
 import com.google.common.collect.Sets;
 import com.tngtech.internal.perceptual.PerceptualPipeline;
 import com.tngtech.internal.perceptual.data.events.GestureData;
 import com.tngtech.internal.perceptual.listeners.GestureListener;
 import intel.pcsdk.PXCMGesture;
 
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 public class GestureComponent implements PerceptualQueryComponent {
    private static final int GESTURE_NUBMER_THRESHOLD = 2;
 
    private static final int NUMBER_OF_GESTURE_SAMPLES = 5;
 
     private Set<GestureListener> gestureListeners = Sets.newHashSet();
 
     private PXCMGesture.Gesture gesture;
 
     private List<Integer> lastGestureLabels = Lists.newArrayList();
 
     private GestureData.GestureType currentlyActiveGestureType;
 
     private final Map<Integer, GestureData.GestureType> gestureTypeMap = new LinkedHashMap<Integer, GestureData.GestureType>() {{
         put(PXCMGesture.Gesture.LABEL_ANY, GestureData.GestureType.NONE);
         put(PXCMGesture.Gesture.LABEL_POSE_THUMB_UP, GestureData.GestureType.THUMBS_UP);
         put(PXCMGesture.Gesture.LABEL_POSE_THUMB_DOWN, GestureData.GestureType.THUMBS_DOWN);
         put(PXCMGesture.Gesture.LABEL_POSE_BIG5, GestureData.GestureType.BIG_FIVE);
     }};
 
     public GestureComponent() {
         currentlyActiveGestureType = GestureData.GestureType.NONE;
     }
 
     @Override
     public void queryFeatures(PerceptualPipeline pipeline) {
         gesture = new PXCMGesture.Gesture();
         pipeline.QueryGesture(PXCMGesture.Gesture.LABEL_ANY, gesture);
     }
 
     @Override
     public void processFeatures() {
         addLabelToLastGestureLabels();
         Map<Integer, Integer> labelCountMap = determineLabelCountMap();
         Integer detectedLabel = getDetectedLabel(labelCountMap);
 
         if (!gestureTypeMap.containsKey(detectedLabel)) {
             return;
         }
 
         GestureData.GestureType gestureType = gestureTypeMap.get(detectedLabel);
         invokeGesture(gestureType);
     }
 
     private void addLabelToLastGestureLabels() {
         lastGestureLabels.add(gesture.label);
         while (lastGestureLabels.size() > NUMBER_OF_GESTURE_SAMPLES) {
             lastGestureLabels.remove(0);
         }
     }
 
     private Map<Integer, Integer> determineLabelCountMap() {
         Map<Integer, Integer> gestureLabelCount = Maps.newHashMap();
         for (Integer label : lastGestureLabels) {
             if (label == PXCMGesture.Gesture.LABEL_ANY) {
                 continue;
             }
 
             if (!gestureLabelCount.containsKey(label)) {
                 gestureLabelCount.put(label, 0);
             }
 
             int oldCount = gestureLabelCount.get(label);
             gestureLabelCount.put(label, oldCount + 1);
         }
         return gestureLabelCount;
     }
 
     private Integer getDetectedLabel(Map<Integer, Integer> gestureLabelCount) {
         Integer labelWithHighestEntryCount = PXCMGesture.Gesture.LABEL_ANY;
         Integer maxCount = 0;
         for (Map.Entry<Integer, Integer> entry : gestureLabelCount.entrySet()) {
             int currentLabel = entry.getKey();
             int currentCount = entry.getValue();
 
             if (currentCount > maxCount) {
                 labelWithHighestEntryCount = currentLabel;
                 maxCount = currentCount;
             }
         }
         return maxCount > GESTURE_NUBMER_THRESHOLD ? labelWithHighestEntryCount : PXCMGesture.Gesture.LABEL_ANY;
     }
 
     private void invokeGesture(GestureData.GestureType gestureType) {
         if (currentlyActiveGestureType != gestureType) {
             currentlyActiveGestureType = gestureType;
             invokeGestureListeners(new GestureData(gestureType));
         }
     }
 
     public void addGestureListener(GestureListener gestureListener) {
         if (!gestureListeners.contains(gestureListener)) {
             gestureListeners.add(gestureListener);
         }
     }
 
     public void removeGestureListener(GestureListener gestureListener) {
         if (gestureListeners.contains(gestureListener)) {
             gestureListeners.remove(gestureListener);
         }
     }
 
     private void invokeGestureListeners(GestureData data) {
         for (GestureListener listener : gestureListeners) {
             listener.onGesture(data);
         }
     }
 }
