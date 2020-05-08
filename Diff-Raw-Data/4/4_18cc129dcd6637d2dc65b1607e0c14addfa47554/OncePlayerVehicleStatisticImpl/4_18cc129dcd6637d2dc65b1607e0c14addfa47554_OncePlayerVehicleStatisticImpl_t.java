 /**
  * Copyright (C) 2013 MK124
  *
  * This program is free software; you can redistribute it and/or modify
  * it under the terms of the GNU General Public License version 2 as
  * published by the Free Software Foundation.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  */
 
 package net.gtaun.wl.vehicle.stat;
 
import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Date;
 import java.util.List;
 
 import net.gtaun.shoebill.Shoebill;
 import net.gtaun.shoebill.constant.PlayerState;
 import net.gtaun.shoebill.object.Player;
 import net.gtaun.shoebill.object.Vehicle;
 import net.gtaun.util.event.EventManager;
 
 public class OncePlayerVehicleStatisticImpl extends AbstractPlayerVehicleProbe implements OncePlayerVehicleStatistic
 {
 	private StatisticType type;
 	
 	private List<Integer> modelIds;
 	
 	private double damageCount;
 	private long driveSecondCount;
 	private double driveOdometer;
 	private float currentSpeed;
 	private float maxSpeed;
 	
 	private Date startTime;
 	private Date endTime;
 	
 	
 	public OncePlayerVehicleStatisticImpl(Shoebill shoebill, EventManager rootEventManager, Player player, StatisticType type)
 	{
 		super(shoebill, rootEventManager, player);
 		this.type = type;
 		
		modelIds = new ArrayList<>();
		
 		Vehicle vehicle = player.getVehicle();
 		if (vehicle != null) modelIds.add(vehicle.getModelId());
 		else modelIds.add(0);
 		
 		damageCount = 0.0;
 		driveOdometer = 0.0;
 		maxSpeed = 0.0f;
 		
 		allowState(PlayerState.PASSENGER);
 	}
 	
 	@Override
 	protected void onVehicleUpdate(Vehicle vehicle)
 	{
 		currentSpeed = vehicle.getVelocity().speed3d() * 50;
 		if (currentSpeed > maxSpeed) maxSpeed = currentSpeed;
 	}
 
 	@Override
 	protected void onVehicleMove(Vehicle vehicle, float distance)
 	{
 		driveOdometer += distance;
 	}
 
 	@Override
 	protected void onVehicleTick(Vehicle vehicle)
 	{
 		driveSecondCount++;
 	}
 
 	@Override
 	protected void onVehicleDamage(Vehicle vehicle, float damage)
 	{
 		damageCount += damage;
 	}
 	
 	@Override
 	protected void onBecomePassenger(Vehicle vehicle)
 	{
 		modelIds.add(vehicle.getModelId());
 	}
 	
 	@Override
 	protected void onDriveVehicle(Vehicle vehicle)
 	{
 		modelIds.add(vehicle.getModelId());
 	}
 	
 	@Override
 	protected void onLeaveVehicle(Vehicle vehicle)
 	{
 		modelIds.add(0);
 	}
 	
 	@Override
 	public boolean isActive()
 	{
 		return isDestroyed() == false;
 	}
 	
 	public void start()
 	{
 		startTime = new Date();
 		init();
 	}
 	
 	public void end()
 	{
 		if (isActive() == false) return;
 
 		endTime = new Date();
 		super.destroy();
 	}
 	
 	@Override
 	public StatisticType getType()
 	{
 		return type;
 	}
 	
 	@Override
 	public List<Integer> getModelIds()
 	{
 		return Collections.unmodifiableList(modelIds);
 	}
 	
 	@Override
 	public double getDamageCount()
 	{
 		return damageCount;
 	}
 	
 	@Override
 	public long getDriveSecondCount()
 	{
 		return driveSecondCount;
 	}
 	
 	@Override
 	public double getDriveOdometer()
 	{
 		return driveOdometer;
 	}
 	
 	@Override
 	public float getCurrentSpeed()
 	{
 		return currentSpeed;
 	}
 
 	@Override
 	public float getMaxSpeed()
 	{
 		return maxSpeed;
 	}
 	
 	@Override
 	public Date getStartTime()
 	{
 		return startTime;
 	}
 	
 	@Override
 	public Date getEndTime()
 	{
 		return endTime;
 	}
 }
