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

import org.eclipse.storage.exceptions.StorageException;
import org.eclipse.serializer.afs.types.ADirectory;
import org.eclipse.serializer.afs.types.AFile;
import org.eclipse.serializer.afs.types.AWritableFile;
import org.eclipse.serializer.persistence.types.PersistenceTypeDefinition;


public interface StorageEntityTypeExportFileProvider
{
	public AWritableFile provideExportFile(StorageEntityTypeHandler entityType);

	
	
	public static String uniqueTypeFileNameSeparator()
	{
		return "_";
	}

	public static String toUniqueTypeFileName(final PersistenceTypeDefinition type)
	{
		return StorageEntityTypeExportFileProvider.toUniqueTypeFileName(type.typeName(), type.typeId());
	}
	
	public static String toUniqueTypeFileName(final String typeName, final long typeId)
	{
		// TypeId must be included since only that is the unique identifier of a type.
		return typeName + uniqueTypeFileNameSeparator() + typeId;
	}
	
	/* (20.02.2020 TM)XXX: abstract import/export filename logic
	 * These static methods are just a hotfix.
	 * The proper solution must be to introduce a StorageImportExportFileNameHandler or something like that
	 * that handles export file name creation and conversion typeId parsing.
	 * And then StorageDataConverterTypeCsvToBinary and StorageEntityTypeExportFileProvider must reference
	 * the SAME instance to have compatible logic.
	 */
	public static long getTypeIdFromUniqueTypeFileName(final String uniqueTypeFileName)
	{
		final int lastIndexOfSeparator = uniqueTypeFileName.lastIndexOf(uniqueTypeFileNameSeparator());
		if(lastIndexOfSeparator < 0)
		{
			throw new StorageException(
				"UniqueTypeFileNameSeparator '"
				+ uniqueTypeFileNameSeparator()
				+ "' was not found in file name \""
				+ uniqueTypeFileName + "\"."
			);
		}
		
		final String typeIdString = uniqueTypeFileName.substring(lastIndexOfSeparator + 1);
		try
		{
			return Long.parseLong(typeIdString);
		}
		catch(final NumberFormatException e)
		{
			throw new StorageException("Invalid TypeId String in file name \"" + uniqueTypeFileName + "\".", e);
		}
	}
	

	public final class Default implements StorageEntityTypeExportFileProvider
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
			this.fileSuffix = fileSuffix;
		}
		
		

		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		public final String fileSuffix()
		{
			return this.fileSuffix;
		}

		@Override
		public final AWritableFile provideExportFile(final StorageEntityTypeHandler entityType)
		{
			final String        name  = StorageEntityTypeExportFileProvider.toUniqueTypeFileName(entityType);
			final AFile file  = this.directory.ensureFile(null, name, this.fileSuffix);
			final AWritableFile wFile = file.useWriting();
			
			return wFile;
		}

	}

}
