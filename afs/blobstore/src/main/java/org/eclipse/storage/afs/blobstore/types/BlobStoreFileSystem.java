package org.eclipse.storage.afs.blobstore.types;

/*-
 * #%L
 * store-afs-blobstore
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

import static org.eclipse.serializer.util.X.notNull;

import org.eclipse.storage.base.chars.CharsUtils;
import org.eclipse.storage.base.io.IOUtils;
import org.eclipse.serializer.afs.types.ADirectory;
import org.eclipse.serializer.afs.types.AFile;
import org.eclipse.serializer.afs.types.AFileSystem;
import org.eclipse.serializer.afs.types.AItem;
import org.eclipse.serializer.afs.types.AReadableFile;
import org.eclipse.serializer.afs.types.AResolver;
import org.eclipse.serializer.afs.types.AWritableFile;
import org.eclipse.serializer.chars.VarString;
import org.eclipse.serializer.io.XIO;

public interface BlobStoreFileSystem extends AFileSystem, AResolver<BlobStorePath, BlobStorePath>
{
	public static BlobStorePath toPath(
		final AItem item
	)
	{
		return toPath(
			item.toPath()
		);
	}

	public static BlobStorePath toPath(
		final String... pathElements
	)
	{
		return BlobStorePath.New(
			notNull(pathElements)
		);
	}


	public static BlobStoreFileSystem New(
		final BlobStoreConnector connector
	)
	{
		return New(
			BlobStoreIoHandler.New(connector)
		);
	}

	public static BlobStoreFileSystem New(
		final BlobStoreIoHandler ioHandler
	)
	{
		return new BlobStoreFileSystem.Default(
			notNull(ioHandler)
		);
	}

	@Override
	public BlobStoreIoHandler ioHandler();


	public static class Default
	extends    AFileSystem.Abstract<BlobStoreIoHandler, BlobStorePath, BlobStorePath>
	implements BlobStoreFileSystem
	{
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		protected Default(
			final BlobStoreIoHandler ioHandler
		)
		{
			super(
				"http://",
				ioHandler
			);
		}


		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public String deriveFileIdentifier(
			final String fileName,
			final String fileType
		)
		{
			return IOUtils.addFileSuffix(fileName, fileType);
		}

		@Override
		public String deriveFileName(
			final String fileIdentifier
		)
		{
			return IOUtils.getFilePrefix(fileIdentifier);
		}

		@Override
		public String deriveFileType(
			final String fileIdentifier
		)
		{
			return XIO.getFileSuffix(fileIdentifier);
		}

		@Override
		public String getFileName(
			final AFile file
		)
		{
			return IOUtils.getFilePrefix(file.identifier());
		}

		@Override
		public String getFileType(
			final AFile file
		)
		{
			return XIO.getFileSuffix(file.identifier());
		}

		@Override
		public String[] resolveDirectoryToPath(
			final BlobStorePath directory
		)
		{
			return directory.pathElements();
		}

		@Override
		public String[] resolveFileToPath(
			final BlobStorePath file
		)
		{
			return file.pathElements();
		}

		@Override
		public BlobStorePath resolve(
			final ADirectory directory
		)
		{
			return BlobStoreFileSystem.toPath(directory);
		}

		@Override
		public BlobStorePath resolve(
			final AFile file
		)
		{
			return BlobStoreFileSystem.toPath(file);
		}

		@Override
		protected VarString assembleItemPath(
			final AItem     item,
			final VarString vs
		)
		{
			return CharsUtils.assembleSeparated(
				vs,
				BlobStorePath.SEPARATOR_CHAR,
				item.toPath()
			);
		}

		@Override
		public AReadableFile wrapForReading(
			final AFile  file,
			final Object user
		)
		{
			final BlobStorePath path = this.resolve(file);
			return BlobStoreReadableFile.New(file, user, path);
		}

		@Override
		public AWritableFile wrapForWriting(
			final AFile  file,
			final Object user
		)
		{
			final BlobStorePath path = this.resolve(file);
			return BlobStoreWritableFile.New(file, user, path);
		}

		@Override
		public AReadableFile convertToReading(
			final AWritableFile file
		)
		{
			return BlobStoreReadableFile.New(
				file,
				file.user(),
				((BlobStoreWritableFile)file).path()
			);
		}

		@Override
		public AWritableFile convertToWriting(
			final AReadableFile file
		)
		{
			return BlobStoreWritableFile.New(
				file,
				file.user(),
				((BlobStoreReadableFile)file).path()
			);
		}

	}

}
