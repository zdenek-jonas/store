package org.eclipse.storage.types;

/*-
 * #%L
 * Eclipse Store Storage
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

import org.eclipse.serializer.afs.types.ADirectory;
import org.eclipse.serializer.afs.types.AFile;
import org.eclipse.serializer.afs.types.AWritableFile;
import org.eclipse.serializer.persistence.types.PersistenceTypeDefinition;


public interface StorageEntityTypeConversionFileProvider
{
	public AWritableFile provideConversionFile(PersistenceTypeDefinition typeDescription, AFile sourceFile);



	public final class Default implements StorageEntityTypeConversionFileProvider
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		private final ADirectory directory ;
		private final String     fileSuffix;




		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		public Default(final ADirectory directory, final String fileSuffix)
		{
			super();
			this.directory  = notNull(directory);
			this.fileSuffix = fileSuffix        ;
		}



		///////////////////////////////////////////////////////////////////////////
		// declared methods //
		/////////////////////

		public final String fileSuffix()
		{
			return this.fileSuffix;
		}



		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public AWritableFile provideConversionFile(
			final PersistenceTypeDefinition typeDescription,
			final AFile                     sourceFile
		)
		{
			// TypeId must be included since only that is the unique identifier of a type.
			
			final String fileName = typeDescription.typeName() + "_" + typeDescription.typeId();
			final AFile targetFile = this.directory.ensureFile(fileName, this.fileSuffix);
			targetFile.ensureExists();
			return targetFile.useWriting();
		}

	}

}
