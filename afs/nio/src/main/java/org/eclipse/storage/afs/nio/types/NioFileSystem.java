package org.eclipse.storage.afs.nio.types;

/*-
 * #%L
 * Eclipse Store Abstract File System - Java NIO
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

import java.nio.file.FileSystem;
import java.nio.file.Path;

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


public interface NioFileSystem extends AFileSystem, AResolver<Path, Path>
{
	@Override
	public NioIoHandler ioHandler();
	
	/**
	 * Returns the default root directory, depending on the NIO implementation.
	 * In local file systems it is the current working directory of this JVM instance.
	 */
	@Override
	public ADirectory ensureDefaultRoot();
		
	
	public interface Defaults
	{
		public static String defaultProtocol()
		{
			/* (29.05.2020 TM)TODO: priv#49: standard protocol strings? Constants, Enums?
			 * (02.06.2020 TM)Note: the JDK does not define such constants.
			 * E.g. the class FileSystems just uses a plain String "file:///".
			 * All other search results are false positives in JavaDoc and comments.
			 * 
			 * (19.07.2020 TM):
			 * Downgraded to T0D0.
			 */
			return "file:///";
		}
		
	}
			
	public static NioFileSystem New()
	{
		return New(Defaults.defaultProtocol());
	}
	
	public static NioFileSystem New(
		final String defaultProtocol
	)
	{
		return New(
			defaultProtocol,
			NioIoHandler.New()
		);
	}
	
	public static NioFileSystem New(
		final NioIoHandler ioHandler
	)
	{
		return New(
			Defaults.defaultProtocol(),
			notNull(ioHandler)
		);
	}
	
	public static NioFileSystem New(
		final String       defaultProtocol,
		final NioIoHandler ioHandler
	)
	{
		return new NioFileSystem.Default(
			notNull(defaultProtocol),
			notNull(ioHandler)
		);
	}
	
	public static NioFileSystem New(
		final FileSystem fileSystem
	)
	{
		return New(
			NioIoHandler.New(
				NioPathResolver.New(fileSystem)
			)
		);
	}
	
	public class Default extends AFileSystem.Abstract<NioIoHandler, Path, Path> implements NioFileSystem
	{
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		protected Default(
			final String       defaultProtocol,
			final NioIoHandler ioHandler
		)
		{
			super(defaultProtocol, ioHandler);
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		@Override
		public final synchronized ADirectory ensureDefaultRoot()
		{
			return this.ensureRoot("./");
		}
				
		@Override
		public String deriveFileIdentifier(final String fileName, final String fileType)
		{
			return IOUtils.addFileSuffix(fileName, fileType);
		}
		
		@Override
		public String deriveFileName(final String fileIdentifier)
		{
			return IOUtils.getFilePrefix(fileIdentifier);
		}
		
		@Override
		public String deriveFileType(final String fileIdentifier)
		{
			return XIO.getFileSuffix(fileIdentifier);
		}
		
		@Override
		public String getFileName(final AFile file)
		{
			return IOUtils.getFilePrefix(file.identifier());
		}
		
		@Override
		public String getFileType(final AFile file)
		{
			return XIO.getFileSuffix(file.identifier());
		}
		
		@Override
		public AFile createFile(
			final ADirectory parent    ,
			final String     identifier,
			final String     name      ,
			final String     type
		)
		{
			if(identifier != null)
			{
				return super.createFile(parent, identifier, name, type);
			}
			
			if(type == null)
			{
				return this.createFile(parent, name);
			}
			
			return this.createFile(parent, IOUtils.addFileSuffix(name, type));
		}
		
		@Override
		public String[] resolveDirectoryToPath(final Path directory)
		{
			return IOUtils.splitPath(directory);
		}

		@Override
		public String[] resolveFileToPath(final Path file)
		{
			return IOUtils.splitPath(file);
		}

		@Override
		public Path resolve(final ADirectory directory)
		{
			// does not need synchronization since it only reads immutable state and creates only thread local state.
			return this.ioHandler().toPath(directory);
		}

		@Override
		public Path resolve(final AFile file)
		{
			// does not need synchronization since it only reads immutable state and creates only thread local state.
			return this.ioHandler().toPath(file);
		}
				
		@Override
		protected VarString assembleItemPath(final AItem item, final VarString vs)
		{
			return IOUtils.assemblePath(vs, item.toPath());
		}
		
		@Override
		public AReadableFile wrapForReading(final AFile file, final Object user)
		{
			// note: no locking required for thread-safe trivial logic here.
			final Path path = this.resolve(file);
			
			return NioReadableFile.New(file, user, path);
		}

		@Override
		public AWritableFile wrapForWriting(final AFile file, final Object user)
		{
			// note: no locking required for thread-safe trivial logic here.
			final Path path = this.resolve(file);
			
			return NioWritableFile.New(file, user, path);
		}
		
		@Override
		public AReadableFile convertToReading(final AWritableFile file)
		{
			final NioWritableFile wf = this.ioHandler().castWritableFile(file);
			final boolean actuallyClosedChannel = wf.closeChannel();
			
			final NioReadableFile rf = NioReadableFile.New(
				file,
				file.user(),
				wf.path(),
				null
			);
			
			// replicate opened channel (ONLY!) if necessary
			if(actuallyClosedChannel)
			{
				rf.ensureOpenChannel();
			}
			
			return rf;
		}
		
		@Override
		public AWritableFile convertToWriting(final AReadableFile file)
		{
			final NioReadableFile wf = this.ioHandler().castReadableFile(file);
			final boolean actuallyClosedChannel = wf.closeChannel();
			
			final NioWritableFile rf = NioWritableFile.New(
				file,
				file.user(),
				wf.path(),
				null
			);
			
			// replicate opened channel (ONLY!) if necessary
			if(actuallyClosedChannel)
			{
				rf.ensureOpenChannel();
			}
			
			return rf;
		}
		
	}
	
}
