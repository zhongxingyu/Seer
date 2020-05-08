 package org.multigraph;
 
 import java.util.ArrayList;
 
 import org.multigraph.datatypes.DataMeasure;
 import org.multigraph.datatypes.DataValue;
 import org.multigraph.datatypes.DataType;
 import org.multigraph.datatypes.Labeler;
 import org.multigraph.datatypes.number.NumberLabeler;
 
 public class Axis {
         
     private org.multigraph.jaxb.Axis mState;
     private Graph mParent;
 
     private String mId;
     public String getId() { return mId; }
 
     private int mLength;
     public int getLength() { return mLength; }
 
     private int mPerpOffset;
     public int getPerpOffset() { return mPerpOffset; }
 
     private int mParallelOffset;
     public int getParallelOffset() { return mParallelOffset; }
 
     private int mMinOffset;
     public int getMinOffset() { return mMinOffset; }
 
     private int mMaxOffset;
     public int getMaxOffset() { return mMaxOffset; }
 
     private double mAxisToDataRatio;
     public double getAxisToDataRatio() { return mAxisToDataRatio; }
 
     private ArrayList<Labeler> mLabelers;
     
     private double mDensity;
     private Labeler mLabeler;
     
     //
     // _dataMin is the min data value; access through dataMin getter/setter property in order to keep
     // record of whether it is set, and to update the axisToDataRatio accordingly.
     //
     private DataValue mDataMin;
     private boolean mHaveDataMin = false;
     public boolean haveDataMin(){ return mHaveDataMin; }
     public DataValue getDataMin() { return mDataMin; }
     public void setDataMin(DataValue min) {
         mDataMin = min;
         mHaveDataMin = true;
         if (mHaveDataMin && mHaveDataMax) {
            mAxisToDataRatio = (mLength - mMaxOffset - mMaxOffset) / (mDataMax.getRealValue() - mDataMin.getRealValue());
         }
     }
 
     //
     // _dataMax is the max data value; access through dataMax getter/setter property in order to keep
     // record of whether it is set, and to update the axisToDataRatio accordingly.
     //
     private DataValue mDataMax;
     private boolean mHaveDataMax = false;
     public boolean haveDataMax(){ return mHaveDataMax; }
     public DataValue getDataMax() { return mDataMax; }
     public void setDataMax(DataValue max) {
         mDataMax = max;
         mHaveDataMax = true;
         if (mHaveDataMax && mHaveDataMax) {
            mAxisToDataRatio = (mLength - mMaxOffset - mMaxOffset) / (mDataMax.getRealValue() - mDataMin.getRealValue());
         }
     }
 
     //private DataValue.Type mType = DataValue.Type.UNKNOWN;
     //public DataValue.Type getType() { return mType; }
     private org.multigraph.datatypes.DataType mType = org.multigraph.datatypes.DataType.NUMBER;
     public org.multigraph.datatypes.DataType getType() { return mType; }
 
     private AxisOrientation mOrientation;
     public AxisOrientation getOrientation() { return mOrientation; }
     
     private void prepareState() {
         if (!mState.isSetGrid()) { mState.setGrid( new org.multigraph.jaxb.Grid() ); }
         if (!mState.isSetLabels()) { mState.setLabels(new org.multigraph.jaxb.Labels() ); }
         if (mState.getLabels().getLabel().isEmpty()) {
             mState.getLabels().getLabel().add(new org.multigraph.jaxb.Label());
         }
     }
 
     public Axis(Graph parent, org.multigraph.jaxb.Axis state) throws DataTypeException  {
         this.mParent      = parent;
         this.mState       = state;
         this.mOrientation = state.getOrientation();
         this.mId          = state.getId();
         this.mType        = state.getType();
         
         prepareState();
 
         this.mLength = (int)Math.round(mState.getLength() * ((mOrientation == AxisOrientation.HORIZONTAL)
                                                              ? mParent.getPlotbox().getWidth()
                                                              : mParent.getPlotbox().getHeight()));
 
         if (!state.getMin().isAuto()) {
             setDataMin(state.getMin().toDataValue(state.getType()));
         }
 
         if (!state.getMax().isAuto()) {
             setDataMax(state.getMax().toDataValue(state.getType()));
         }
 
         buildLabelers();
     }
 
 
     private void buildLabelers() throws DataTypeException {
         this.mLabelers = new ArrayList<Labeler>();
 
         ////////////////////////////////////////////////////////////////////////
         int numLabelSubtags = mState.getLabels()!=null ? mState.getLabels().getLabel().size() : 0; 
         if (numLabelSubtags > 0) {
             // This is the case where we have <labels><label>...</label>...</labels>,
             // i.e. single <label> tags nested inside the <labels> tag
             for(int k = 0; k < numLabelSubtags; ++k) {
                 String spacingAttrValue = null;
                 if (mState.getLabels().getLabel().get(k).isSetSpacing()) {
                     spacingAttrValue = mState.getLabels().getLabel().get(k).getSpacing();
                 }
                 if (spacingAttrValue==null && mState.getLabels().isSetSpacing()) {
                     spacingAttrValue = mState.getLabels().getSpacing();
                 }
                 if (spacingAttrValue==null && mType==DataType.DATETIME) {
                     spacingAttrValue = mState.getLabels().DEFAULT_DATETIME_SPACING;
                 }
                 // spacingAttrValue better not be null here!!!
                 String hlabelSpacings[] = spacingAttrValue.split("[ \t]+");
 
                 String format = ( mState.getLabels().getLabel().get(k).isSetFormat()
                                   ? mState.getLabels().getLabel().get(k).getFormat()
                                   : mState.getLabels().getFormat() );
                 DataValue start = ( mState.getLabels().getLabel().get(k).isSetStart()
                                     ? DataValue.create(mType, mState.getLabels().getLabel().get(k).getStart())
                                     : DataValue.create(mType, mState.getLabels().getStart()) );
                 DPoint position = ( mState.getLabels().getLabel().get(k).isSetPosition()
                                     ? mState.getLabels().getLabel().get(k).getPosition()
                                     : mState.getLabels().getPosition() );
                 double angle = ( mState.getLabels().getLabel().get(k).isSetAngle()
                                  ? mState.getLabels().getLabel().get(k).getAngle()
                                  : mState.getLabels().getAngle() );
                 DPoint anchor = ( mState.getLabels().getLabel().get(k).isSetAnchor()
                                   ? mState.getLabels().getLabel().get(k).getAnchor()
                                   : mState.getLabels().getAnchor() );
                 for (int j=0; j<hlabelSpacings.length; ++j) {
                     DataMeasure spacing = DataMeasure.create(mType, hlabelSpacings[j]);
                     Labeler labeler = Labeler.create(mType,
                                                      this,
                                                      spacing, 
                                                      format,
                                                      start,
                                                      position,
                                                      angle,
                                                      anchor);
                     this.mLabelers.add(labeler);
                 }
             }
         } else {
 
             ///
             /// This better not happen any more; prepareState() has ensured we have at least one <label> subelement
             /// inside a <labels> element.
             ///
             System.out.printf("Axis.java.buildLabels(): labels state error!!!!\n");
 
 
             /*
             // This is the case where we have no <label> tags nested inside the <labels> tag
             String spacingAttrValue = null; 
             if (!mState.getLabels().isSetSpacing() && mType==DataType.DATETIME) {
             spacingAttrValue = mState.getLabels().DEFAULT_DATETIME_SPACING;
             } else {
             spacingAttrValue = mState.getLabels().getSpacing();
             }
             String hlabelSpacings[] = spacingAttrValue.split("[ \t]+");
             for (int k=0; k<hlabelSpacings.length; ++k) {
             DataMeasure spacing = DataMeasure.create(mType, hlabelSpacings[k]);
             DataValue start = DataValue.create(mType, mState.getLabels().getStart());
             Labeler labeler = Labeler.create(this,
             mType,
             spacing, 
             mState.getLabels().getFormat(),
             start,
             mState.getLabels().getPosition(),
             mState.getLabels().getAngle(),
             mState.getLabels().getAnchor());
             this.mLabelers.add(labeler);
             }
             */
 
 
         }
         ////////////////////////////////////////////////////////////////////////
 
         /*
           if (mOrientation == AxisOrientation.HORIZONTAL) {
           for (Labeler lab : mLabelers) {
           ((org.multigraph.datatypes.datetime.DatetimeLabeler)lab).dump();
           }
           }
         */
         
         
     }
 
 
 
     public double dataValueToAxisValue(DataValue v) {
         return mAxisToDataRatio * ( v.getRealValue() - mDataMin.getRealValue() ) + mMinOffset + mParallelOffset;
     }
     public double dataValueToAxisValue(double v) {
         return mAxisToDataRatio * ( v                  - mDataMin.getRealValue() ) + mMinOffset + mParallelOffset;
     }
 
     public double axisValueToDataValueDouble(double v) {
         return                         (v - mMinOffset - mParallelOffset) / mAxisToDataRatio + mDataMin.getRealValue();
     }
     public DataValue axisValueToDataValue(double v) throws DataTypeException {
         return DataValue.create(mType, (v - mMinOffset - mParallelOffset) / mAxisToDataRatio + mDataMin.getRealValue());
     }
 
     private void prepareRender() {
         // Decide which labeler to use: take the one with the largest density <= 0.8.
         // Unless all have density > 0.8, in which case we take the first one.  This assumes
         // that the labelers list is ordered in increasing order of label density.
         // This function sets the _labeler and _density private properties.
         mLabeler = mLabelers.get(0);
         mDensity = mLabeler.getLabelDensity();
         if (mDensity < 0.8) {
             for (int i = 1; i < mLabelers.size(); i++) {
                 double density = mLabelers.get(i).getLabelDensity();
                 if (density > 0.8) { break; }
                 mLabeler = mLabelers.get(i);
                 mDensity = density;
             }
         }
     }
 
     public void render(GraphicsContext g, int step) {
 
         switch (step) {
         case 0:  // in step 0, render the grid lines associated with this axis, if any
             prepareRender();
             if (mState.getGrid().isVisible()) {
                 if (mLabelers.size() > 0 && mDensity <= 1.5) {
                     mLabeler.prepare(mDataMin, mDataMax);
                     boolean first = true;
                     while (mLabeler.hasNext()) {
                         DataValue v = mLabeler.next();
                         if (first) {
                             first = false;
                         }
                         double a = dataValueToAxisValue(v);
                         g.setLineWidth(1.0);
                         g.setColor(mState.getGrid().getColor());
                         if (mOrientation == AxisOrientation.HORIZONTAL) {
                             g.drawLine(a, mPerpOffset, a, mParent.getPlotbox().getHeight() - mPerpOffset);
                         } else {
                             g.drawLine(mPerpOffset, a, mParent.getPlotbox().getWidth() - mPerpOffset, a);
                         }
                     }
                 }
             }
             break;
 
         default:
         case 1:  // in step 1, render everything else
             // render the axis itself:
             if (mState.getLinewidth() > 0) {
                 g.setLineWidth(mState.getLinewidth());
                 g.setColor(mState.getColor());
 
                 if (mOrientation == AxisOrientation.HORIZONTAL) {
                     g.drawLine(mParallelOffset, mPerpOffset, mParallelOffset + mLength, mPerpOffset);
                 } else {
                     g.drawLine(mPerpOffset, mParallelOffset, mPerpOffset, mParallelOffset + mLength);
                 }
             }
 
             // render the axis title
             // ... NYI ...
 
             // render the tick marks and labels
             if (mLabelers.size() > 0 && mDensity <= 1.5) {
                 int tickThickness = 1;
                 g.setLineWidth(tickThickness);
                 g.setColor(mState.getColor());
                 mLabeler.prepare(mDataMin, mDataMax);
                 while (mLabeler.hasNext()) {
                     DataValue v = mLabeler.next();
                     double    a = dataValueToAxisValue(v);
                     if (mOrientation == AxisOrientation.HORIZONTAL) {
                         g.drawLine(a, mPerpOffset+mState.getTickmax(), a, mPerpOffset+mState.getTickmin());
                     } else {
                         g.drawLine(mPerpOffset+mState.getTickmin(), a, mPerpOffset+mState.getTickmax(), a);
                     }
                     mLabeler.renderLabel(g, v);
                 }
             }
             break;
 
         }
 
     }
 
 
 }
