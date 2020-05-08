 /*   
   This file is part of opensearch.
   Copyright © 2009, Dansk Bibliotekscenter a/s,
   Tempovej 7-11, DK-2750 Ballerup, Denmark. CVR: 15149043
 
   opensearch is free software: you can redistribute it and/or modify
   it under the terms of the GNU General Public License as published by
   the Free Software Foundation, either version 3 of the License, or
   (at your option) any later version.
 
   opensearch is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU General Public License for more details.
 
   You should have received a copy of the GNU General Public License
   along with opensearch.  If not, see <http://www.gnu.org/licenses/>.
 */
 
 /**
  * \file
  * \brief
  */
 
 
 package dk.dbc.opensearch.components.harvest;
 
 
 /**
  * {@link JobStatus} identifies the status of a {@link Job} in the
  * harvester. A client is allowed to set the status of a {@link Job}
  * in accordance with the outcome of the processing within that
  * client. A {@link Job} will initially be unset and can only be set
  * by the client. Restrictions on the state transitions must be
 * implemented by the {@link IHarvest}.
  * 
  * The Harvester will recognise three states of a {@link Job};
  * Success, failure or retry. The initial status of a {@link Job} in
 * {@link IHarvest} will be unset and the client is allowed to set
  * the status of a {@link Job} to one of the values in {@link JobStatus}.
  * 
  */
 public enum JobStatus
 {
     /**
      * Indicates that the job was processed correctly and no errors
      * encountered
      */
     SUCCESS,
 
     /**
      * Indicates that an unrecoverable exception during job
      * processing has occured and that the client is unable to
      * process the job (at all).
      */
     FAILURE,
 
     /**
      * Indicates the the client has encountered an error in the
      * processing of the {@link Job}, not related to the {@link
      * Job} itself, but rather to the business logic within the
      * client. 
      * 
     * The {@link IHarvest} implementation will specify
      * the correct course of action taken, if a {@link Job} is
      * marked {@code RETRY}. 
      */
      RETRY;
 
 }
