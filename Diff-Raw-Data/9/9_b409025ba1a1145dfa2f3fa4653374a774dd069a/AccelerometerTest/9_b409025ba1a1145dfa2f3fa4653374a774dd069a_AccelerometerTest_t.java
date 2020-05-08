 package ece5984.phase2.truerandomstudy;
 
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 
 import android.content.Context;
 import android.hardware.Sensor;
 import android.hardware.SensorEvent;
 import android.hardware.SensorEventListener;
 import android.hardware.SensorManager;
 import android.util.Log;
 
 public class AccelerometerTest implements Test, SensorEventListener
 {
 	float[] values = {0,0,0};
 	boolean change = false;
 	SensorManager sensorManager;
 	ByteArrayOutputStream baos;
 	/**
 	 * All of tests that will be run by the framework
 	 */
 	static String[] planned_tests = {"1 bit cycle between axis", "1 bit one axis", "1 bit per axis", "2 bit cycle","2 bit per axis","3 bit cycle","3 bit per axis","LS 8 bits per axis","4 bits per axis 0xF0","8 bits per axis 0xFF0","2 bits per axis 0x6","XOR 1 bit per axis","XOR 2 bits per axis"};
 	
 	int[] bits;
 	int[] random;
 	@Override
 	public int initialize(Context context, ByteArrayOutputStream raw) 
 	{
 		Log.d("Accel_test","Initialize Accelerometer");
		this.baos = raw;
 		sensorManager = (SensorManager)context.getSystemService(Context.SENSOR_SERVICE);
         sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
         bits = new int[planned_tests.length];
         random = new int[planned_tests.length];
         return planned_tests.length;
 	}
 	int cycle=0;
 	@Override
 	public DataPair getData(int type) 
 	{
 		DataPair toReturn = new DataPair(bits[type],random[type]);
 		bits[type] = 0;
 		random[type] = 0;
 		return toReturn;
 	}
 
 	@Override
 	public void finish() 
 	{
 		sensorManager.unregisterListener(this);
 	}
 
 	@Override
 	public void onAccuracyChanged(Sensor arg0, int arg1) {}
 	
 	@Override
 	public void onSensorChanged(SensorEvent event) 
 	{
 		values = event.values;
 		try {
 			baos.write((values[0]+","+values[1]+","+values[2]).getBytes());
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		int x,y,z;
 		for (int type=0; type<planned_tests.length;type++)
 		{
 			switch (type)
 			{
 			/*
 			 * It is worth noting that the mantissa's LSB is the floats LSB
 			 */
 			case 0://1 bit cycle
 				cycle = (cycle + 1) % 3;
 				bits[type] = 1;
 				x = Float.floatToIntBits(values[cycle]);
 				random[type] = x & 0x1;
 				break;
 			case 1://1 bit one axis
 				bits[type] = 1;
 				x = Float.floatToIntBits(values[0]);
 				random[type] = x & 0x1;
 				break;
 			case 2://1 bit per axis
 				bits[type] = 3;
 				x = Float.floatToIntBits(values[0])&0x1;
 				y = Float.floatToIntBits(values[1])&0x1;
 				z = Float.floatToIntBits(values[2])&0x1;
 				random[type] = (x<<2)|(y<<1)|z;
 				break;
 			case 3://2 bit cycle
 				bits[type] = 2;
 				x = Float.floatToIntBits(values[cycle]);
 				random[type] = x & 0x3;
 				break;
 			case 4://2 bit per axis
 				bits[type] = 6;
 				x = Float.floatToIntBits(values[0])&0x3;
 				y = Float.floatToIntBits(values[1])&0x3;
 				z = Float.floatToIntBits(values[2])&0x3;
 				random[type] = (x<<4)|(y<<2)|z;
 				break;
 			case 5://3 bit cycle
 				bits[type] = 3;
 				x = Float.floatToIntBits(values[cycle]);
 				random[type] = x & 0x7;
 				break;
 			case 6://3 bit per axis
 				bits[type] = 9;
 				x = Float.floatToIntBits(values[0])&0x7;
 				y = Float.floatToIntBits(values[1])&0x7;
 				z = Float.floatToIntBits(values[2])&0x7;
 				random[type] = (x<<6)|(y<<3)|z;
 				break;
 			case 7://8 bits per axis
 				bits[type] = 24;
 				x = Float.floatToIntBits(values[0])&0xFF;
 				y = Float.floatToIntBits(values[1])&0xFF;
 				z = Float.floatToIntBits(values[2])&0xFF;
 				random[type] = (x<<16)|(y<<8)|z;
 				break;
 			case 8://4 bits per axis, but not LS 0xF0
 				bits[type] = 12;
 				x = Float.floatToIntBits(values[0])&0xF0;
 				y = Float.floatToIntBits(values[1])&0xF0;
 				z = Float.floatToIntBits(values[2])&0xF0;
 				random[type] = (x<<4)|y|(z>>4);
 				break;
 			case 9://8 bits per axis, but not LSB 0xFF0
 				bits[type] = 24;
 				x = Float.floatToIntBits(values[0])&0xFF0;
 				y = Float.floatToIntBits(values[1])&0xFF0;
 				z = Float.floatToIntBits(values[2])&0xFF0;
 				random[type] = (x<<12)|(y<<4)|(z>>4);
 				break;
 			case 10://2 bits per axis, ignore LSb = 0x6
 				bits[type] = 6;
 				x = Float.floatToIntBits(values[0])&0x6;
 				y = Float.floatToIntBits(values[1])&0x6;
 				z = Float.floatToIntBits(values[2])&0x6;
 				random[type] = (x<<3)|(y<<1)|(z>>1);
 				break;
 			case 11://XOR of 1 bit per axis
 				bits[type] = 1;
 				x = Float.floatToIntBits(values[0])&0x1;
 				y = Float.floatToIntBits(values[1])&0x1;
 				z = Float.floatToIntBits(values[2])&0x1;
 				random[type] = (x)^(y)^(z);
 				break;
 			case 12://XOR of 2 bits per axis
 				bits[type] = 1;
 				x = Float.floatToIntBits(values[0])&0x3;
 				y = Float.floatToIntBits(values[1])&0x3;
 				z = Float.floatToIntBits(values[2])&0x3;
 				int temp = (x)^(y)^(z);
 				random[type] = (temp==0x3 || temp==0?0:1);
 			}
 			
 			 
 		}
 		x = Float.floatToIntBits(values[0]);
 		y = Float.floatToIntBits(values[1]);
 		z = Float.floatToIntBits(values[2]);
 		/*
 		baos.write((int)System.currentTimeMillis());
 		baos.write(x);
 		baos.write(y);
 		baos.write(z);*/
 		Log.d("Data: ", ""+x+" "+y+" "+z);
 	}
 
 	@Override
 	public String[] describeTests() {
 		return planned_tests;
 	}
 
 	public boolean timeMatters() { return true;}
 }
