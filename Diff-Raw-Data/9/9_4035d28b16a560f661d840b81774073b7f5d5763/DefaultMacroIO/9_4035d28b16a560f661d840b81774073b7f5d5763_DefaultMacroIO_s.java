 package yang.events.macro;
 
 import java.io.DataInputStream;
 import java.io.DataOutputStream;
 import java.io.IOException;
 
 import yang.events.eventtypes.SurfacePointerEvent;
 import yang.events.eventtypes.YangEvent;
 import yang.events.eventtypes.YangInput3DEvent;
 import yang.events.eventtypes.YangKeyEvent;
 import yang.events.eventtypes.YangSensorEvent;
 import yang.events.eventtypes.YangZoomEvent;
 import yang.math.objects.Quaternion;
 import yang.surface.YangSurface;
 
 public class DefaultMacroIO extends AbstractMacroIO {
 
 	public static final int ID_POINTER_EVENT = 0;
 	public static final int COUNT_POINTER_ACTIONS = 4;
 	public static final int ID_KEY_EVENT = ID_POINTER_EVENT+COUNT_POINTER_ACTIONS;
 	public static final int COUNT_KEY_ACTIONS = 2;
 	public static final int ID_ZOOM_EVENT = 6;
 	public static final int ID_SENSOR_EVENT = 7;
 	public static final int ID_INPUT_3D_EVENT = 8;
 
 	public DefaultMacroIO(YangSurface surface) {
 		super(surface);
 	}
 
 	@Override
 	protected void writeEvent(DataOutputStream stream, YangEvent event) throws IOException {
 		if(event instanceof SurfacePointerEvent) {
 			final SurfacePointerEvent pointerEvent = (SurfacePointerEvent)event;
 			stream.writeByte(ID_POINTER_EVENT+pointerEvent.mAction);
 			stream.writeByte(pointerEvent.mId | pointerEvent.mButton<<4);
 			stream.writeFloat(pointerEvent.mX);
 			stream.writeFloat(pointerEvent.mY);
 		} else if(event instanceof YangKeyEvent) {
 			final YangKeyEvent keyEvent = (YangKeyEvent)event;
 			stream.writeByte(ID_KEY_EVENT+keyEvent.mAction);
 			stream.writeShort(keyEvent.mKey);
 		} else if(event instanceof YangZoomEvent) {
 			stream.writeByte(ID_ZOOM_EVENT);
 			stream.writeFloat(((YangZoomEvent)event).mValue);
 		} else if(event instanceof YangSensorEvent){
 			stream.writeByte(ID_SENSOR_EVENT);
 			final YangSensorEvent sensorEvent = (YangSensorEvent)event;
 			stream.writeByte(sensorEvent.mType);
 			stream.writeFloat(sensorEvent.mX);
 			stream.writeFloat(sensorEvent.mY);
 			stream.writeFloat(sensorEvent.mZ);
 			stream.writeFloat(sensorEvent.mW);
 		} else if(event instanceof YangInput3DEvent) {
 			stream.writeByte(ID_INPUT_3D_EVENT);
 			final YangInput3DEvent inp3DEv = (YangInput3DEvent)event;
 			stream.writeByte(inp3DEv.mId);
 			stream.writeFloat(inp3DEv.mPosition.mX);
 			stream.writeFloat(inp3DEv.mPosition.mY);
 			stream.writeFloat(inp3DEv.mPosition.mZ);
 			final Quaternion orientation = inp3DEv.mOrientation;
 			stream.writeFloat(orientation.mX);
 			stream.writeFloat(orientation.mY);
 			stream.writeFloat(orientation.mZ);
 			stream.writeFloat(orientation.mW);
 		}
 	}
 
 	@Override
 	protected YangEvent readEvent(DataInputStream stream) throws IOException {
 		final int id = stream.readByte();
 		if(id>=ID_POINTER_EVENT && id<ID_POINTER_EVENT+COUNT_POINTER_ACTIONS) {
 			final SurfacePointerEvent pointerEvent = mEventQueue.newPointerEvent();
 			pointerEvent.mAction = id-ID_POINTER_EVENT;
 			final int read = stream.readByte();
 			pointerEvent.mButton = read>>4;
 			pointerEvent.mId = read & 0x0F;
 			pointerEvent.mX = stream.readFloat();
 			pointerEvent.mY = stream.readFloat();
 			return pointerEvent;
 		}else if(id>=ID_KEY_EVENT && id<ID_KEY_EVENT+COUNT_KEY_ACTIONS) {
 			final YangKeyEvent keyEvent = mEventQueue.newKeyEvent();
 			keyEvent.mAction = id-ID_KEY_EVENT;
 			keyEvent.mKey = stream.readShort();
 			return keyEvent;
 		}else if(id==ID_ZOOM_EVENT) {
 			final YangZoomEvent zoomEvent = mEventQueue.newZoomEvent();
 			zoomEvent.mValue = stream.readFloat();
 			return zoomEvent;
 		}else if(id==ID_SENSOR_EVENT) {
 			final YangSensorEvent sensorEvent = mEventQueue.newSensorEvent();
 			sensorEvent.mType = stream.readByte();
 			sensorEvent.mX = stream.readFloat();
 			sensorEvent.mY = stream.readFloat();
 			sensorEvent.mZ = stream.readFloat();
 			sensorEvent.mW = stream.readFloat();
 			return sensorEvent;
 		}else if(id==ID_INPUT_3D_EVENT) {
 			final YangInput3DEvent input3DEvent = mEventQueue.newInput3DEvent();
 			input3DEvent.mId = stream.readByte();
 			input3DEvent.mPosition.set(stream.readFloat(),stream.readFloat(),stream.readFloat());
 			input3DEvent.mOrientation.set(stream.readFloat(),stream.readFloat(),stream.readFloat(),stream.readFloat());
 			return input3DEvent;
 		}
 
 		return null;
 	}
 
 
 
 }
