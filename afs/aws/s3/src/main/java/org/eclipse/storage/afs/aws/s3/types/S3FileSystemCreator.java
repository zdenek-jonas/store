package org.eclipse.storage.afs.aws.s3.types;

/*-
 * #%L
 * store-afs-aws-s3
 * %%
 * Copyright (C) 2019 - 2023 Eclipse Foundation
 * %%
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 * 
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the Eclipse
 * Public License, v. 2.0 are satisfied: GNU General Public License, version 2
 * with the GNU Classpath Exception which is
 * available at https://www.gnu.org/software/classpath/license.html.
 * 
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 * #L%
 */

import org.eclipse.storage.afs.aws.types.AwsFileSystemCreator;
import org.eclipse.storage.afs.blobstore.types.BlobStoreFileSystem;
import org.eclipse.storage.configuration.types.Configuration;
import org.eclipse.serializer.afs.types.AFileSystem;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;

public class S3FileSystemCreator extends AwsFileSystemCreator
{
	public S3FileSystemCreator()
	{
		super();
	}
	
	@Override
	public AFileSystem create(
		final Configuration configuration
	)
	{
		final Configuration s3Configuration = configuration.child("aws.s3");
		if(s3Configuration == null)
		{
			return null;
		}
		
		final S3ClientBuilder clientBuilder = S3Client.builder();
		this.populateBuilder(clientBuilder, s3Configuration);
		
		final S3Client    client    = clientBuilder.build();
		final boolean     cache     = configuration.optBoolean("cache").orElse(true);
		final S3Connector connector = cache
			? S3Connector.Caching(client)
			: S3Connector.New(client)
		;
		return BlobStoreFileSystem.New(connector);
	}
	
}
