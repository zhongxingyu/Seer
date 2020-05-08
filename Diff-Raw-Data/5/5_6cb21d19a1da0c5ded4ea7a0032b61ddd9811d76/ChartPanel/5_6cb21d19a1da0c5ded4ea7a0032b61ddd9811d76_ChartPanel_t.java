 package com.k2yt.igarashiuitask.exchange;
 
 import java.awt.BasicStroke;
 import java.awt.Color;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.event.ActionEvent;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.event.MouseMotionListener;
 import java.beans.PropertyChangeListener;
 import java.text.SimpleDateFormat;
 import java.util.HashSet;
 import java.util.Set;
 
 import javax.swing.Action;
 import javax.swing.JPanel;
 import javax.swing.JPopupMenu;
 
 public final class ChartPanel extends JPanel implements MouseListener, MouseMotionListener {
     private static final long serialVersionUID = 1L;
     
     public enum Mode {
         YEAR, MONTH, DAY
     }
     
     // associated data
     private final ExchangeData mData;
     private final Set<Integer> mPinned;
     
     // delegate
     private PinManager mPinManager;
     
     // view parameter
     private int mMaxValue = 175;
     private int mMinValue = 55;
     private double mInterval;
     
     // view state
     private int mCenterIndex;
     private int mMouseLastX;
 
     // view
     private JPopupMenu mPopupMenu;
     
     public ChartPanel(ExchangeData data, Mode mode) {
         mData = data;
         mPinned = new HashSet<Integer>();
         mCenterIndex = 0;
         mMouseLastX = -1;
         mPopupMenu = new JPopupMenu();
         mPopupMenu.add(new MenuItemAction("マークを追加") {
             public void actionPerformed(ActionEvent e) { onMenuAddPin(e); }
         });
         mPopupMenu.add(new MenuItemAction("拡大") {
             public void actionPerformed(ActionEvent e) { onMenuZoomIn(e); }
         });
         mPopupMenu.add(new MenuItemAction("縮小") {
             public void actionPerformed(ActionEvent e) { onMenuZoomOut(e); }
         });
         addMouseListener(this);
         addMouseMotionListener(this);
         setMode(mode);
     }
 
     public void addPin(int index) {
         mPinned.add(index);
     }
     
     public void removePin(int index) {
         mPinned.remove(index);
     }
     
     public void setMode(Mode mode) {
         switch (mode) {
         case YEAR:
            mInterval = 0.125;
             break;
         case MONTH:
             mInterval = 2;
             break;
         case DAY:
             mInterval = 16;
             break;
         }
         repaint();
     }
 
     public void setPinManager(PinManager pinManager) {
         mPinManager = pinManager;
     }
     
     public void setCenterIndex(int index) {
         mCenterIndex = index;
     }
     
     private int getIndexFromX(int x, boolean hasValue) {
         int index = -1, dx = Integer.MAX_VALUE;
         for (int i = 0; i < mData.size(); i++) {
             if (hasValue && mData.getPrice(i) == null) continue;
             if (Math.abs(getXFromIndex(i) - mMouseLastX) < dx) {
                 dx = Math.abs(getXFromIndex(i) - mMouseLastX);
                 index = i;
             }
         }
         return index;
     }
     
     private int getXFromIndex(int index) {
         return (int)((index - mCenterIndex) * mInterval) + getWidth() / 2;
     }
     
     private int getYFromValue(double value) {
         final double r = (mMaxValue - value) / (mMaxValue - mMinValue);
         return (int)(getHeight() * r);
     }
     
     private void recalculateCenterIndex(int index, int x) {
         // re-calculate mCenterIndex so that
         // |getXFromIndex(index) - x| is minimized
         int cindex = -1, dx = Integer.MAX_VALUE;
         for (int i = 0; i < mData.size(); i++) {
             mCenterIndex = i;
             if (Math.abs(getXFromIndex(index) - x) < dx) {
                 dx = Math.abs(getXFromIndex(index) - x);
                 cindex = mCenterIndex;
             }
         }
         mCenterIndex = cindex;
     }
 
     @Override
     public void paint(Graphics g) {
         super.paint(g);
         g.setColor(Color.white);
         g.fillRect(0, 0, getWidth(), getHeight());
         paintFrame(g);
         paintGraph(g);
         paintCurrentPoint(g);
     }
 
     private void paintFrame(Graphics g) {
         for (int v = mMinValue / 10 * 10; v <= mMaxValue; v += 10) {
             final int y = getYFromValue(v);
             if (y > 30 && y < getHeight() - 30) {
                 g.setColor(Color.gray);
                 g.drawString(v + " 円/ドル", 0, y);
                 g.drawString(v + " 円/ドル", getWidth() - 70, y);
             }
             g.setColor(Color.lightGray);
             g.drawLine(0, y, getWidth(), y);
         }
         for (int y = mData.minYear(); y <= mData.maxYear(); y++) {
             final int index = mData.getFirstIndexOfYear(y);
             final int x = getXFromIndex(index);
             g.setColor(Color.gray);
             g.drawString(y + "年", x, getHeight() - 26);
             g.setColor(Color.lightGray);
             g.drawLine(x, 0, x, getHeight());
         }
         if (mInterval < 2) return;
         for (int y = mData.minYear(); y <= mData.maxYear(); y++) {
             for (int m = 1; m <= 12; m++) {
                 if (mData.getFirstIndexOfMonth(y, m) == null) continue;
                 final int index = mData.getFirstIndexOfMonth(y, m);
                 final int x = getXFromIndex(index);
                 g.setColor(Color.gray);
                 g.drawString(m + "月", x, getHeight() - 12);
                 g.setColor(Color.lightGray);
                 g.drawLine(x, 0, x, getHeight());
             }
         }
         if (mInterval < 16) return;
         final BasicStroke basicStroke = new BasicStroke();
         final BasicStroke dashStroke = new BasicStroke(
                 1.0f, BasicStroke.JOIN_ROUND, BasicStroke.CAP_BUTT,
                 1.0f, new float[] {10.0f, 10.0f}, 0.0f);
         for (int y = mData.minYear(); y <= mData.maxYear(); y++) {
             for (int m = 1; m <= 12; m++) {
                 for (int d = 5; d <= 31; d += 5) {
                     if (mData.existsDay(y, m, d) == false) continue;
                     final int index = mData.getIndexOfDay(y, m, d);
                     final int x = getXFromIndex(index);
                     if (x < -30 || x > getWidth() + 30) continue;
                     g.setColor(Color.gray);
                     final int strY = getHeight() - 2;
                     g.drawString(d + "日", x, strY);
                     ((Graphics2D)g).setStroke(dashStroke);
                     g.setColor(Color.lightGray);
                     g.drawLine(x, 0, x, getHeight());
                     ((Graphics2D)g).setStroke(basicStroke);
                 }
             }
         }
     }
     
     private void paintGraph(Graphics g) {
         g.setColor(Color.black);
         for (int i = 0; i < mData.size(); i++) {
             if (mData.getPrice(i) == null) continue;
             final int xi = getXFromIndex(i);
             final int yi = getYFromValue(mData.getPrice(i));
             if (xi < Short.MIN_VALUE || xi > Short.MAX_VALUE) continue;
             for (int j = i+1; j < mData.size(); j++) {
                 if (mData.getPrice(j) == null) continue;
                 final int xj = getXFromIndex(j);
                 final int yj = getYFromValue(mData.getPrice(j));
                 if (xj < Short.MIN_VALUE || xj > Short.MAX_VALUE) continue;
                if (mInterval >= 4) g.fillOval(xi-2, yi-2, 5, 5);
                 if (mPinned.contains(i)) {
                     g.setColor(Color.red);
                     g.fillOval(xi-3, yi-3, 7, 7);
                     g.setColor(Color.black);
                 }
                 g.drawLine(xi, yi, xj, yj);
                 break;
             }
         }
     }
     
     private void paintCurrentPoint(Graphics g) {
         final SimpleDateFormat fmt = new SimpleDateFormat("yyyy/M/d");
         final int curIndex = getIndexFromX(mMouseLastX, true);
         final int x = getXFromIndex(curIndex);
         final int y = getYFromValue(mData.getPrice(curIndex));
         g.setColor(Color.blue);
         g.drawString(fmt.format(mData.getDate(curIndex)), x - 80, getHeight() - 50);
         g.drawString(mData.getPrice(curIndex) + "円/ドル", x, getHeight() - 50);
         g.fillOval(x-3, y-3, 7, 7);
     }
     
     private void updateMouseState(MouseEvent me) {
         mMouseLastX = me.getX();
     }
 
     private void onMenuAddPin(ActionEvent e) {
         if (mPinManager != null) {
             mPinManager.addPin(getIndexFromX(mMouseLastX, true));
         }
     }
 
     private void onMenuZoomIn(ActionEvent e) {
         final int index = getIndexFromX(mMouseLastX, true);
         final int x = getXFromIndex(index);
         mInterval *= 2;
         recalculateCenterIndex(index, x);
         repaint();
     }
     
     private void onMenuZoomOut(ActionEvent e) {
         final int index = getIndexFromX(mMouseLastX, true);
         final int x = getXFromIndex(index);
         mInterval /= 2;
         recalculateCenterIndex(index, x);
         repaint();
     }
 
     @Override
     public void mouseDragged(MouseEvent me) {
         if (mPopupMenu.isVisible()) return ;
         if (mMouseLastX >= 0 && mMouseLastX < getWidth()) {
             final double diff = (mMouseLastX - me.getX()) / mInterval;
             if (Math.abs(diff) < 1) return ;
             mCenterIndex += diff;
         }
         updateMouseState(me);
         repaint();
     }
 
     @Override
     public void mouseMoved(MouseEvent me) {
         if (mPopupMenu.isVisible()) return ;
         updateMouseState(me);
         repaint();
     }
 
     @Override
     public void mouseClicked(MouseEvent me) {
     }
 
     @Override
     public void mouseEntered(MouseEvent me) {
     }
 
     @Override
     public void mouseExited(MouseEvent me) {
     }
 
     @Override
     public void mousePressed(MouseEvent me) {
         showPopup(me);
     }
 
     @Override
     public void mouseReleased(MouseEvent me) {
         showPopup(me);
     }
 
     private void showPopup(MouseEvent me) {
         if (me.isPopupTrigger()) {
             mPopupMenu.show(me.getComponent(), me.getX(), me.getY());
         }
     }
     
     private abstract class MenuItemAction implements Action {
         private final String mText;
         
         public MenuItemAction(String text) {
             mText = text;
         }
         
         @Override
         public void addPropertyChangeListener(PropertyChangeListener l) {
         }
 
         @Override
         public Object getValue(String key) {
             if (NAME.equals(key)) return mText;
             return null;
         }
 
         @Override
         public boolean isEnabled() {
             return true;
         }
 
         @Override
         public void putValue(String key, Object val) {
         }
 
         @Override
         public void removePropertyChangeListener(PropertyChangeListener arg0) {
         }
 
         @Override
         public void setEnabled(boolean b) {
         }
     }
 }
