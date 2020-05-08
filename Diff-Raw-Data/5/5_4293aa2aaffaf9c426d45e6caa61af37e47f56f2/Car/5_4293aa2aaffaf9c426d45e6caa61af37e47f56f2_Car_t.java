 ﻿package tk.bnbm.clockdrive4j.model;
 
 import static java.lang.Math.PI;
 import static java.util.Calendar.SECOND;
 
 import java.awt.geom.Point2D;
 import java.util.Calendar;
 import java.util.Date;
 
 public class Car {
 	/** 与えられた時刻 */
 	private Date time;
 
 	/** s 車の位置や角度を算出するためのRoadインスタンス */
 	private Road road;
 
 	/**
 	 * コンストラクタ。<br>
 	 * 引数にRoadインスタンスを受け取って、格納する。
 	 *
 	 * @param road 自身が走ることになる"道路"オブジェクト。
 	 */
 	public Car(Road road) {
 		this.road = road;
 	}
 
 	/**
 	 * 基準時刻を与える。
 	 *
 	 * @param t 基準時刻。
 	 */
 	public void setTime(Date t) {
 		time = t;
 	}
 
 	/**
 	 * 車を描くべき位置を取得する。
 	 *
 	 * @return 座標オブジェクト。
 	 */
 	public Point2D.Double getPosition() {
 		return road.getRoadPosition(this.time);
 	}
 
 	/**
 	 * 車をの向き(描くべき角度を360度法)を取得する。<br>
 	 * 前後２点から滑らかに補完する。
 	 *
	 * @return 角度(360度法)
 	 */
 	public double getAngle() {
 
 		Calendar c = Calendar.getInstance();
 
 		// 自身が保持している「時刻」から前後５秒算出。
 		c.setTime(this.time);
 		c.add(SECOND, -5);
 		Date before5Sec = c.getTime();
 
 		c.setTime(this.time);
 		c.add(SECOND, +5);
 		Date after5Sec = c.getTime();
 
 		// 座標算出。
 		Point2D.Double pA = road.getRoadPosition(before5Sec);
 		Point2D.Double pB = road.getRoadPosition(after5Sec);
 		double xDiff = (pA.getX() - pB.getX());
 		double yDiff = (pA.getY() - pB.getY());
 		double angle = Math.atan2(yDiff, xDiff);
		return angle / PI / 2.0D * 360.0D + 90.0D;
 	}
 }
