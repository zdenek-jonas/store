package org.eclipse.storage.restadapter.types;

/*-
 * #%L
 * Eclipse Storage REST Adapter
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

import org.eclipse.serializer.persistence.binary.types.Binary;
import org.eclipse.serializer.persistence.types.PersistenceTypeDefinitionMember;

public class ValueReaderStringList extends ValueReaderVariableLength
{
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public ValueReaderStringList(final PersistenceTypeDefinitionMember member)
	{
		super(member);
	}

	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	@Override
	public Object readValue(final Binary binary, final long offset)
	{
		long listOffset = Binary.toBinaryListElementsOffset(offset);
		final int elementCount = (int) binary.getBinaryListElementCountUnvalidating(offset);

		final String strings[] = new String[elementCount];

		for(int i = 0; i < elementCount; i++)
		{
			final long stringLength = binary.getBinaryListTotalByteLength(listOffset);
			final char[] array = binary.build_chars(listOffset);

			listOffset += stringLength;
			strings[i] = new String(array);
		}

		return strings;
	}
}
