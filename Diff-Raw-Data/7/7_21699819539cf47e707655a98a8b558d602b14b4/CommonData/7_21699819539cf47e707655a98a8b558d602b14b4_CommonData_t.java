 package dataHistographs;
 
 import java.util.ArrayList;
 
 public class CommonData implements HistData
 {
     ArrayList<HistData> data;
     
     public CommonData()
     {
         data = new ArrayList<HistData>();
     }
     public CommonData(int len)
     {
         data = new ArrayList<HistData>(len);
     }
     public void nextPointFrom(HistData d)
     {
         data.add(d);
     }
     public float getCO2(int i)
     {
         return data.get(i).getCO2(i);
     }
     public HistData getDataSource(int i)
     {
         return data.get(i);
     }
     public float[] getAllCO2()
     {
         float[] returnMe = new float[data.get(0).size()];
        for(int i=0; i<this.size(); i++)
         {
            returnMe[i] = this.getCO2(i);
         }
         return returnMe;
     }
     public int size()
     {
         return data.size();
     }
     public boolean equals(Object other)
     {
         if (other instanceof HistData)
         {
             HistData o = (HistData) other;
             if(size() != o.size())
                 return false;
             
             boolean good = true;
             for (int i=0; i<size(); i++)
             {
             	float thisDat = getCO2(i);
     			float otherDat = o.getCO2(i);
             	good = good && (thisDat==otherDat);
             }
         }
         return false;
     }
     public int hashCode()
     {
         return data.hashCode();
     }
 }
