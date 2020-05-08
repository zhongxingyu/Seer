 package org.chai.kevin.maps;
 
 /* 
  * Copyright (c) 2011, Clinton Health Access Initiative.
  *
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are met:
  *     * Redistributions of source code must retain the above copyright
  *       notice, this list of conditions and the following disclaimer.
  *     * Redistributions in binary form must reproduce the above copyright
  *       notice, this list of conditions and the following disclaimer in the
  *       documentation and/or other materials provided with the distribution.
  *     * Neither the name of the <organization> nor the
  *       names of its contributors may be used to endorse or promote products
  *       derived from this software without specific prior written permission.
  * 
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
  * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
  * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
  * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
  * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
  * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
  * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
  * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
  * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
  * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 
 import javax.persistence.Basic;
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.Id;
 import javax.persistence.ManyToOne;
 import javax.persistence.Table;
 
 import org.chai.kevin.data.Calculation;
import org.chai.kevin.entity.export.Exportable;
 import org.chai.kevin.reports.ReportEntity;
 import org.chai.kevin.util.Utils;
 
 @Entity(name="MapsTarget")
 @Table(name="dhsst_maps_target")
public class MapsTarget extends ReportEntity implements Exportable {
 
 	private Long id;
 	private Calculation<?> calculation;
 	private Double maxValue;
 	
 	@Id
 	@GeneratedValue
 	public Long getId() {
 		return id;
 	}
 	public void setId(Long id) {
 		this.id = id;
 	}
 	
 	@ManyToOne(targetEntity=Calculation.class)
 	public Calculation<?> getCalculation() {
 		return calculation;
 	}
 	
 	public void setCalculation(Calculation<?> calculation) {
 		this.calculation = calculation;
 	}
 	
 	@Basic(optional=true)
 	public Double getMaxValue() {
 		return maxValue;
 	}
 	
 	public void setMaxValue(Double maxValue) {
 		this.maxValue = maxValue;
 	}
 	
 	@Override
 	public String toExportString() {
 		return "[" + Utils.formatExportCode(getCode()) + "]";
 	}
 	
 }
