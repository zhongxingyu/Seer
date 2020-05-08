 package com.bluebarracudas.model;
 
 /**
  * A real-life MBTA train stop.
  */
 public class TStop {
	/** Our station meta data */
	private TStationData m_pStationData;
 	/** The TStation we are a part of */
 	private TStation m_pStation;
 	/** Our stop meta data */
 	private TStopData m_pStopData;
 	/** The next TStop in our line */
 	private TStop m_pNextStop;
 	/** The previous TStop in our line */
 	private TStop m_pPrevStop;
 
 	/** Package visible constructor */
 	public TStop( TStationData pStationData, TStopData pStopData ) {
		m_pStationData = pStationData;
 		m_pStopData = pStopData;
 	}
 
 	/** Convert this object to a String */
 	public String toString() {
 		return m_pStopData.name();
 	}
 
 	/** Returns the unique identifier for this stop */
 	public int getID() {
 		return m_pStopData.getID();
 	}
 
 	/** Returns the Station we are in */
 	public TStation getStation() {
		if(m_pStation == null)
			m_pStation = TFactory.getStation(m_pStationData.getID());
 		return m_pStation;
 	}
 
 	/** Returns the previous TStop on our line */
 	public TStop getNextStop() {
 		return m_pNextStop;
 	}
 
 	/** Sets the next TStop on our line. */
 	public void setNextStop(TStop pStop) {
 		m_pNextStop = pStop;
 	}
 
 	/** Returns the previous TStop on our line. */
 	public TStop getPrevStop() {
 		return m_pPrevStop;
 	}
 
 	/** Sets the previous TStop on our line. */
 	public void setPrevStop(TStop pStop) {
 		m_pPrevStop = pStop;
 	}
 }
