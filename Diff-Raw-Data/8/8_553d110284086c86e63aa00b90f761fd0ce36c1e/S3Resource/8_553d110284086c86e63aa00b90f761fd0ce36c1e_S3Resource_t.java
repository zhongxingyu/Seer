 /*
  * Copyright 2010 SpringSource
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
 
 package org.springframework.aws.ivy;
 
 import java.io.IOException;
 import java.io.InputStream;
 
 import org.apache.ivy.plugins.repository.Resource;
 
 import com.amazonaws.services.s3.AmazonS3;
 import com.amazonaws.services.s3.model.ObjectListing;
 import com.amazonaws.services.s3.model.S3ObjectSummary;
 
 /**
  * A Resource implementation that extracts its data from an S3 resource.
  * 
  * @author Ben Hale
  */
 public class S3Resource implements Resource {
 
 	private final AmazonS3 service;
 	private final S3ObjectSummary summary;
	private final String uri;
 
 	public S3Resource(AmazonS3 service, S3ObjectSummary summary) {
 		this.service = service;
 		this.summary = summary;
		this.uri = "s3://" + summary.getBucketName() + "/" + summary.getKey();
 	}
 
 	public S3Resource(AmazonS3 service, String uri) {
 		this.service = service;
		this.uri = uri;
 		ObjectListing objects = service.listObjects(S3Utils.getBucket(uri), S3Utils.getKey(uri));
 		if (objects.getObjectSummaries().size() == 0) {
 			summary = null;
 		} else {
 			summary = objects.getObjectSummaries().get(0);
 		}
 	}
 
 	public Resource clone(String newUri) {
 		return new S3Resource(service, newUri);
 	}
 
 	public boolean exists() {
 		return summary != null;
 	}
 
 	public long getContentLength() {
 		return summary == null ? 0 : summary.getSize();
 	}
 
 	public long getLastModified() {
 		return summary == null ? 0 : summary.getLastModified().getTime();
 	}
 
 	public String getName() {
		return uri;
 	}
 
 	public boolean isLocal() {
 		return false;
 	}
 
 	public InputStream openStream() throws IOException {
 		return service.getObject(summary.getBucketName(), summary.getKey()).getObjectContent();
 	}
 
 	@Override
 	public String toString() {
 		return getName();
 	}
 }
