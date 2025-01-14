package org.eclipse.storage.afs.sql.types;

/*-
 * #%L
 * Eclipse Store Abstract File System - SQL
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

public interface SqlFileSystem extends AFileSystem, AResolver<SqlPath, SqlPath>
{
	public static SqlPath toPath(
		final AItem item
	)
	{
		return toPath(
			item.toPath()
		);
	}

	public static SqlPath toPath(
		final String... pathElements
	)
	{
		return SqlPath.New(
			notNull(pathElements)
		);
	}


	public static SqlFileSystem New(
		final SqlProvider  provider
	)
	{
		return New(
			SqlConnector.New(provider)
		);
	}

	public static SqlFileSystem New(
		final SqlConnector connector
	)
	{
		return New(
			SqlIoHandler.New(connector)
		);
	}

	public static SqlFileSystem New(
		final SqlIoHandler ioHandler
	)
	{
		return new SqlFileSystem.Default(
			notNull(ioHandler)
		);
	}


	public static class Default extends AFileSystem.Abstract<SqlIoHandler, SqlPath, SqlPath> implements SqlFileSystem
	{
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		protected Default(
			final SqlIoHandler ioHandler
		)
		{
			super(
				"jdbc:",
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
			final SqlPath directory
		)
		{
			return directory.pathElements();
		}

		@Override
		public String[] resolveFileToPath(
			final SqlPath file
		)
		{
			return file.pathElements();
		}

		@Override
		public SqlPath resolve(
			final ADirectory directory
		)
		{
			return SqlFileSystem.toPath(directory);
		}

		@Override
		public SqlPath resolve(
			final AFile file
		)
		{
			return SqlFileSystem.toPath(file);
		}

		@Override
		protected VarString assembleItemPath(
			final AItem     item,
			final VarString vs
		)
		{
			return CharsUtils.assembleSeparated(
				vs,
				SqlPath.DIRECTORY_TABLE_NAME_SEPARATOR_CHAR,
				item.toPath()
			);
		}

		@Override
		public AReadableFile wrapForReading(
			final AFile  file,
			final Object user
		)
		{
			final SqlPath path = this.resolve(file);
			return SqlReadableFile.New(file, user, path);
		}

		@Override
		public AWritableFile wrapForWriting(
			final AFile  file,
			final Object user
		)
		{
			final SqlPath path = this.resolve(file);
			return SqlWritableFile.New(file, user, path);
		}

		@Override
		public AReadableFile convertToReading(
			final AWritableFile file
		)
		{
			return SqlReadableFile.New(
				file                          ,
				file.user()                   ,
				((SqlWritableFile)file).path()
			);
		}

		@Override
		public AWritableFile convertToWriting(
			final AReadableFile file
		)
		{
			return SqlWritableFile.New(
				file                          ,
				file.user()                   ,
				((SqlReadableFile)file).path()
			);
		}

	}

}
