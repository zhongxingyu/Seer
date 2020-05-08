 //
 // typica - A client library for Amazon Web Services
 // Copyright (C) 2009 Xerox Corporation
 // 
 // Licensed under the Apache License, Version 2.0 (the "License");
 // you may not use this file except in compliance with the License.
 // You may obtain a copy of the License at
 //
 //     http://www.apache.org/licenses/LICENSE-2.0
 //
 // Unless required by applicable law or agreed to in writing, software
 // distributed under the License is distributed on an "AS IS" BASIS,
 // WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 // See the License for the specific language governing permissions and
 // limitations under the License.
 //
 
 package com.xerox.amazonws.ec2;
 
import java.util.Calendar;

 public class ReservedInstances extends ProductDescription {
	private Calendar start;
 	private int instanceCount;
 	private String state;
 
 	public ReservedInstances(String reservedInstancesId, InstanceType instanceType,
						String availabilityZone, Calendar start, long duration,
						double fixedPrice, double usagePrice,
						String productDescription, int instanceCount,
 						String state) {
 		super(reservedInstancesId, instanceType, availabilityZone,
 				duration, fixedPrice, usagePrice, productDescription);
		this.start = start;
 		this.instanceCount = instanceCount;
 		this.state = state;
 	}
 
	public Calendar getStart() {
		return start;
	}

 	public int getInstanceCount() {
 		return instanceCount;
 	}
 
 	public String getState() {
 		return state;
 	}
 
 	public String toString() {
 		return "ReservedInstances[id=" + getId() + ", type=" + getInstanceType().getTypeId() +
				", zone=" + getAvailabilityZone() + ", start=" + start.toString() +
				", duration=" + getDuration() + ", fixedPrice=" + getFixedPrice() +
 				", usagePrice=" + getUsagePrice() + ", description=" + getProductDescription() +
 				", instanceCount=" + instanceCount + ", state=" + state;
 	}
 }
