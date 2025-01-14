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

import org.eclipse.serializer.io.XIO;

import static org.eclipse.serializer.util.X.notNull;

import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;

@FunctionalInterface
public interface NioPathResolver
{
	public Path resolvePath(final String... pathElements);
	
	
	public static NioPathResolver New()
	{
		return new Default(
			FileSystems.getDefault()
		);
	}
	
	public static NioPathResolver New(final FileSystem fileSystem)
	{
		return new Default(
			notNull(fileSystem)
		);
	}
	
	
	public static class Default implements NioPathResolver
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final FileSystem fileSystem;
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		Default(final FileSystem fileSystem)
		{
			super();
			this.fileSystem = fileSystem;
		}
		 
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public Path resolvePath(final String... pathElements)
		{
			return XIO.Path(this.fileSystem, pathElements);
		}
	}
	
}
