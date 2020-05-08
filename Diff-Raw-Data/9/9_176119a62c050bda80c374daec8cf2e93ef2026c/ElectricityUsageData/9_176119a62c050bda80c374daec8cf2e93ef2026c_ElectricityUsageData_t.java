 /*
  * Copyright (C) 2011-2013 Kuropen.
  * 
  * This file is part of the Electricity Warning Crawler, Version 3.
  * 
  * The Electricity Warning Crawler is free software:
  * you can redistribute it and/or modify it
  * under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * The Electricity Warning Crawler is distributed in the hope that
  * it will be useful, but WITHOUT ANY WARRANTY; without even the implied 
  * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
  * See the GNU General Public License for more details.
  * You should have received a copy of the GNU General Public License
  * along with The Electricity Warning Crawler.
  * If not, see <http://www.gnu.org/licenses/>.
  */
 package co.akabe.elecwarn;
 
 import java.util.Calendar;
 
 import co.akabe.elecwarn.util.EPCOInfo;
 import co.akabe.elecwarn.util.ElecwarnUtil;
 
 /**
  * 現在の電力使用データを示すクラス。 このクラスはソート可能である。ソート基準は使用率({@link #getPercentage()}) である。
  * @author Kuropen
  */
 public class ElectricityUsageData implements EPCOInfo,
 		Comparable<ElectricityUsageData> {
 
 	private String companyKey;
 	private String companyName;
 	private int usage;
 	private int capacity;
 	private Calendar cal;
 
 	/**
 	 * コンストラクタ。
 	 * 
 	 * @param key
 	 *            Yahoo! APIキー
 	 * @param usage
 	 *            使用量
 	 * @param capacity
 	 *            供給力
 	 * @param lastUpdate
 	 *            更新日時
 	 */
 	public ElectricityUsageData(String key, int usage, int capacity,
 			Calendar cal) {
 		companyKey = key;
 		companyName = ElecwarnUtil.getLongName(key);
 		this.usage = usage;
 		this.capacity = capacity;
 		this.cal = cal;
 	}
 
 	@Override
 	public String getCompanyKey() {
 		return companyKey;
 	}
 
 	@Override
 	public String getCompanyName() {
 		return companyName;
 	}
 
 	@Override
 	public int compareTo(ElectricityUsageData o) {
 		if (o.getPercentage() > this.getPercentage()) {
 			return 1;
 		} else if (o.getPercentage() < this.getPercentage()) {
 			return -1;
 		} else {
 			return 0;
 		}
 	}
 
 	/**
 	 * 電力使用率を取得する。
 	 * 
 	 * @return 使用率(%)
 	 */
 	public float getPercentage() {
 		return (float) usage / (float) capacity * 100;
 	}
 
 	/**
 	 * 電力使用量を取得する。
 	 * 
 	 * @return 電力使用量 (万kW)
 	 */
 	public int getUsage() {
 		return usage;
 	}
 
 	/**
 	 * 供給力を取得する。
 	 * 
 	 * @return 供給力 (万kW)
 	 */
 	public int getCapacity() {
 		return capacity;
 	}
 
 	/**
 	 * 使用量をセットする。
 	 * 
 	 * @param u
 	 */
 	public void setUsage(int u) {
 		usage = u;
 	}
 
 	/**
 	 * 供給力をセットする。
 	 * 
 	 * @param c
 	 */
 	public void setCapacity(int c) {
 		capacity = c;
 	}
 
 	/**
 	 * 集約ツイート用の、略称と使用率のみの簡易文字列を生成する。
 	 * 
 	 * @return 簡易文字列
 	 */
 	public String toShortString() {
 		String ret = ElecwarnUtil.getShortName(this) + ":"
 				+ String.format("%.2f", getPercentage()) + "%";
 		return ret;
 	}
 
 	@Override
 	public String toString() {
 		String h = String.format("%02d", cal.get(Calendar.HOUR_OF_DAY));
 		String m = String.format("%02d", cal.get(Calendar.MINUTE));
		String mo = Integer.toString(cal.get(Calendar.MONTH) + 1);
 		String d = Integer.toString(cal.get(Calendar.DAY_OF_MONTH));
 		String ret = "【" + companyName + "管内 電力使用状況" + getWarningLevel() + "】 "
 				+ mo + "/" + d + " " + h + ":" + m + "現在 "
 				+ String.format("%.2f", getPercentage()) + "% (予備力"
 				+ getMargin() + "万kW) #elecwarn_" + companyKey;
 		return ret;
 	}
 
 	/**
 	 * 現在の警報レベルを示す文字列を生成する。
 	 * 
 	 * @return 現在の警報レベル。97%以上：緊急警報、95%以上：警報、90%以上：注意報、それ未満は空文字列。
 	 *         ただし、空文字列以外の場合は連結を考慮して先頭にスペースがあるため、注意すること。
 	 */
 	public String getWarningLevel() {
 		float percentage = getPercentage();
 		int margin = getMargin();
 		if (percentage >= 97.0f || margin <= 15) {
 			return " 緊急警報";
 		} else if (percentage >= 95.0f || margin <= 25) {
 			return " 警報";
 		} else if (percentage >= 90.0f || margin <= 60) {
 			return " 注意報";
 		} else {
 			return "";
 		}
 	}
 
 	/**
 	 * 予備力を取得する。
 	 * 
 	 * @return 予備力 (万kW)
 	 */
 	public int getMargin() {
 		return capacity - usage;
 	}
 
 	/**
 	 * 予備力を取得する。
 	 * 
 	 * @return 予備力 (万kW)
 	 * @deprecated スペルミスのため。今後は {@link #getMargin()}を使う。
 	 */
 	public int getMergin() {
 		return getMargin();
 	}
 
 }
