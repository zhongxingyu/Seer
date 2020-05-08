 /*
  * Copyright 2010 Andreas Brauchli, René Buffat, Florian Widmer
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package org.footware.server.db;
 
 import java.io.Serializable;
 import java.util.LinkedList;
 import java.util.List;
 
 import javax.persistence.CascadeType;
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.FetchType;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.JoinColumn;
 import javax.persistence.ManyToOne;
 import javax.persistence.OneToMany;
 
 import org.footware.server.gpx.model.GPXTrackPoint;
 import org.footware.server.gpx.model.GPXTrackSegment;
 import org.footware.shared.dto.TrackSegmentDTO;
import org.footware.shared.dto.TrackpointDTO;
 import org.hibernate.Hibernate;
 
 /**
  * Class for ER Mapping of persisted TrackSegments, the ("sub-")tracks in a
  * single track file
  */
 @Entity
 public class TrackSegment extends DbEntity implements Serializable {
 
 	private static final long serialVersionUID = 1L;
 
 	@Id
 	@GeneratedValue(strategy = GenerationType.IDENTITY)
 	private long id;
 
 	// Statistics
 	@Column(name = "max_speed")
 	private double maxSpeed = 0.0;
 
 	@Column(name = "min_elevation")
 	private int minElevation = Integer.MAX_VALUE;
 
 	@Column(name = "max_elevation")
 	private int maxElevation = Integer.MIN_VALUE;
 
 	private double length = 0.0;
 
 	@ManyToOne(fetch=FetchType.EAGER)
 	@JoinColumn(name="track_id")
 	private Track track;
 
 	@OneToMany(fetch=FetchType.EAGER,cascade=CascadeType.ALL)
 	private List<Trackpoint> trackpoints = new LinkedList<Trackpoint>();
 
 	/**
 	 * Constructor for hibernate retrieval
 	 */
 	public TrackSegment() {
 	}
 
 	/**
 	 * Create a track segment from a GPXTrackSegment for persistence This also
 	 * creates Trackpoints from the linked GPXTrackPoints
 	 * 
 	 * @param gpx
 	 *            GPXTrackSegment to create from
 	 */
 	public TrackSegment(GPXTrackSegment gpx) {
 		this.maxSpeed = gpx.getMaxSpeed();
 		this.minElevation = gpx.getMinElevation().intValue();
 		this.maxElevation = gpx.getMaxElevation().intValue();
 		this.length = (long) gpx.getLength();
 
 		// Deep replication
 		for (GPXTrackPoint gpt : gpx.getPoints()) {
 			Trackpoint tp = new Trackpoint(this, gpt);
 			this.trackpoints.add(tp);
 			// TODO: Check if we should persist here
 		}
 	}
 	
 	/**
 	 * Create a track segment from a TrackSegmentDTO for persistence This also
 	 * creates Trackpoints from the linked TrackSegmentDTO
 	 * 
 	 * @param gpx
 	 *            GPXTrackSegment to create from
 	 */
 	
 	
 	public TrackSegment(TrackSegmentDTO segment) {
		// Deep replication
		for (TrackpointDTO gpt : segment.getPoints()) {
			Trackpoint tp = new Trackpoint(this,gpt.getLatitude(),gpt.getLongitude(),gpt.getElevation(),gpt.getTime(),0);
			this.trackpoints.add(tp);
		}
 	}
 
 	/**
 	 * Gets the id of the corresponding DB row
 	 * 
 	 * @return the ID of the row in the DB
 	 */
 	public long getId() {
 		return id;
 	}
 
 	/**
 	 * Gets the track belonging to this segment
 	 * 
 	 * @return this segment's corresponding track
 	 */
 	public Track getTrack() {
 		return track;
 	}
 
 	/**
 	 * Sets the track this segment belongs to
 	 * 
 	 * @param track
 	 *            new owner of this segment
 	 */
 	public void setTrack(Track track) {
 		this.track = track;
 	}
 
 	/**
 	 * Gets the maximum speed encountered in this segment
 	 * 
 	 * @return maximum speed encountered in this segment
 	 */
 	public double getMaxSpeed() {
 		return maxSpeed;
 	}
 
 	/**
 	 * Gets the maximum speed encountered in this segment
 	 * 
 	 * @return maximum speed encountered in this segment
 	 */
 	public void setMaxSpeed(double maxSpeed) {
 		this.maxSpeed = maxSpeed;
 	}
 
 	/**
 	 * Gets the maximum speed encountered in this segment
 	 * 
 	 * @return maximum speed encountered in this segment
 	 */
 	public long getMinElevation() {
 		return minElevation;
 	}
 
 	/**
 	 * Gets the maximum speed encountered in this segment
 	 * 
 	 * @return maximum speed encountered in this segment
 	 */
 	public void setMinElevation(int minElevation) {
 		this.minElevation = minElevation;
 	}
 
 	/**
 	 * Gets the maximal elevation encountered in this segment
 	 * 
 	 * @return the maximal elevation encountered in this segment
 	 */
 	public long getMaxElevation() {
 		return maxElevation;
 	}
 
 	/**
 	 * Sets the maximal elevation encountered in this segment
 	 * 
 	 * @param maxElevation
 	 *            the maximal elevation encountered in this segment
 	 */
 	public void setMaxElevation(int maxElevation) {
 		this.maxElevation = maxElevation;
 	}
 
 	/**
 	 * Gets the segments length in meters
 	 * 
 	 * @return gets the segments length
 	 */
 	public double getLength() {
 		return length;
 	}
 
 	/**
 	 * Sets the segments length
 	 * 
 	 * @param length
 	 *            sets the segment length
 	 */
 	public void setLength(double length) {
 		this.length = length;
 	}
 
 	/**
 	 * Gets the trackpoints of this segment
 	 * 
 	 * @return all segment's trackpoints
 	 */
 	public List<Trackpoint> getTrackpoints() {
 		//Hibernate.initialize(trackpoints);
 		return trackpoints;
 	}
 
 	/**
 	 * Appends a trackpoint to this segment
 	 * 
 	 * @param p
 	 *            trackpoint to add
 	 */
 	public void addTrackpoint(Trackpoint p) {
 		this.trackpoints.add(p);
 	}
 
 	/**
 	 * Gets the DTO for this TrackSegment's current state
 	 * 
 	 * @return DTO for this TrackSegment's current state
 	 */
 	public TrackSegmentDTO getTrackSegmentDTO() {
 		TrackSegmentDTO s = new TrackSegmentDTO();
 		for (Trackpoint p : getTrackpoints())
 			s.addPoint(p.getTrackpointDTO());
 		return s;
 	}
 }
