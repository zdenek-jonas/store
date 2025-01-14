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

import static org.eclipse.serializer.util.X.notNull;

import org.eclipse.storage.restadapter.exceptions.StorageRestAdapterException;
import org.eclipse.storage.restadapter.types.StorageRestAdapter;
import org.eclipse.storage.restservice.sparkjava.exceptions.InvalidRouteParametersException;
import org.eclipse.storage.restservice.types.StorageRestService;
import org.eclipse.storage.types.StorageManager;
import spark.Service;
import spark.route.HttpMethod;

public class StorageRestServiceSparkJava implements StorageRestService
{
	public static StorageRestServiceSparkJava New(
		final StorageManager storage
	)
	{
		return new StorageRestServiceSparkJava(
			StorageRestAdapter.New(storage)
		);
	}
	
	public static StorageRestServiceSparkJava New(
		final StorageRestAdapter storageRestAdapter
	)
	{
		return new StorageRestServiceSparkJava(
			notNull(storageRestAdapter)
		);
	}
	
	
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	private final StorageRestAdapter storageRestAdapter;
	private Service                  sparkService;
	private String                   storageName = "store-data";
	private RouteManager             routeManager;

	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	StorageRestServiceSparkJava(
		final StorageRestAdapter storageRestAdapter
	)
	{
		super();
		this.storageRestAdapter = storageRestAdapter;
	}


	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	public void setInstanceName(final String name)
	{
		this.storageName = name;
	}

	public void setSparkService(final Service sparkService)
	{
		this.sparkService = sparkService;
	}

	public void setDefaultDataLength(final long defaultDataLength)
	{
		this.storageRestAdapter.setDefaultValueLength(defaultDataLength);
	}

	@Override
	public void start()
	{
		if(this.sparkService == null)
		{
			this.sparkService = Service.ignite();
		}

		this.setupRoutes();

		this.sparkService.init();
		this.sparkService.awaitInitialization();
	}

	private void setupRoutes()
	{
		this.routeManager = new DocumentationManager(this.sparkService);

		this.routeManager.registerRoute(HttpMethod.get, "/",
			new RouteAllRoutes((DocumentationManager) this.routeManager));

		this.routeManager.registerRoute(HttpMethod.get, "/" + this.storageName + "/root",
			new RouteGetRoot(this.storageRestAdapter));

		this.routeManager.registerRoute(HttpMethod.get, "/" + this.storageName + "/dictionary",
			new RouteTypeDictionary(this.storageRestAdapter));

		this.routeManager.registerRoute(HttpMethod.get, "/" + this.storageName + "/object/:oid",
			new RouteGetObject(this.storageRestAdapter));

		this.routeManager.registerRoute(HttpMethod.get, "/" + this.storageName + "/maintenance/filesStatistics",
			new RouteStorageFilesStatistics(this.storageRestAdapter));

		this.sparkService.exception(InvalidRouteParametersException.class, (e, request, response) ->
			{
				response.status(404);
				response.body(e.getMessage());
			});

		this.sparkService.exception(StorageRestAdapterException.class, (e, request, response) ->
			{
				response.status(404);
				response.body(e.getMessage());
			});
	}

	@Override
	public void stop()
	{
		if(this.sparkService != null)
		{
			this.sparkService.stop();
			this.sparkService.awaitStop();
		}
	}
}
