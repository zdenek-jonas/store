package org.eclipse.storage.configuration.yaml.types;

/*-
 * #%L
 * Eclipse Store Configuration YAML
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

import org.yaml.snakeyaml.Yaml;

import org.eclipse.storage.configuration.types.ConfigurationMapperMap;
import org.eclipse.storage.configuration.types.ConfigurationParser;
import org.eclipse.storage.configuration.types.Configuration.Builder;

public interface ConfigurationParserYaml extends ConfigurationParser
{
	public static ConfigurationParserYaml New()
	{
		return new ConfigurationParserYaml.Default(
			ConfigurationMapperMap.New()
		);
	}
	
	public static ConfigurationParserYaml New(
		final ConfigurationMapperMap mapper
	)
	{
		return new ConfigurationParserYaml.Default(
			notNull(mapper)
		);
	}
	
	
	public static class Default implements ConfigurationParserYaml
	{
		private final ConfigurationMapperMap mapper;
		
		Default(
			final ConfigurationMapperMap mapper
		)
		{
			super();
			this.mapper = mapper;
		}
	
		@Override
		public Builder parseConfiguration(
			final Builder builder,
			final String  input
		)
		{
			return this.mapper.mapConfiguration(
				builder,
				new Yaml().load(input)
			);
		}
		
	}
}
