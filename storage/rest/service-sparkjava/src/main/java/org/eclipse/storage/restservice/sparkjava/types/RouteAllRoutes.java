package org.eclipse.storage.restservice.sparkjava.types;

/*-
 * #%L
 * Eclipse Storage REST Service Sparkjava
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

import spark.Request;
import spark.Response;

public class RouteAllRoutes extends RouteBase<DocumentationManager>
{
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public RouteAllRoutes(final DocumentationManager apiAdapter)
	{
		super(apiAdapter);
	}

	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	@Override
	public Object handle(final Request request, final Response response)
	{
		response.type("application/json");

		String host = request.host();
		if(request.contextPath() != null)
		{
			host += request.contextPath();
		}

		return this.apiAdapter.getAllRoutes(host);
	}

}
