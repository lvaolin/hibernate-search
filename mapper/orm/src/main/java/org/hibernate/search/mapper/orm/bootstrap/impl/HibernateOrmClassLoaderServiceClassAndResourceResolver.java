/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.mapper.orm.bootstrap.impl;

import java.io.InputStream;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.Map;

import org.hibernate.boot.registry.classloading.spi.ClassLoadingException;
import org.hibernate.search.engine.environment.classpath.spi.ClassResolver;
import org.hibernate.search.engine.environment.classpath.spi.DefaultClassAndResourceResolver;
import org.hibernate.search.engine.environment.classpath.spi.ResourceResolver;

/**
 * An implementation of {@link ClassResolver} which delegates to the ORM-provided {@code ClassResolver}.
 * If class, resource or service loading fails in ORM class loader, the current class loader is checked.
 *
 * @author Hardy Ferentschik
 */
final class HibernateOrmClassLoaderServiceClassAndResourceResolver implements ClassResolver, ResourceResolver {
	/**
	 * {@code ClassResolver] as provided by Hibernate ORM. This is the class loader which we attempt to use first.
	 */
	private final org.hibernate.boot.registry.classloading.spi.ClassLoaderService hibernateClassLoaderService;

	/**
	 * A Search internal class loader service which in particular tries to use the current class loader. This can be
	 * necessary in case the ORM class loader can due to modularity not access the required resources
	 */
	private final DefaultClassAndResourceResolver internalClassResolver;


	HibernateOrmClassLoaderServiceClassAndResourceResolver(org.hibernate.boot.registry.classloading.spi.ClassLoaderService hibernateClassLoaderService) {
		this.hibernateClassLoaderService = hibernateClassLoaderService;
		this.internalClassResolver = new DefaultClassAndResourceResolver();
	}

	@Override
	public <T> Class<T> classForName(String className) {
		try {
			return hibernateClassLoaderService.classForName( className );
		}
		catch (ClassLoadingException | LinkageError e) {
			return internalClassResolver.classForName( className );
		}
	}

	@Override
	public URL locateResource(String name) {
		URL url = hibernateClassLoaderService.locateResource( name );
		if ( url == null ) {
			url = internalClassResolver.locateResource( name );
		}
		return url;
	}

	@Override
	public InputStream locateResourceStream(String name) {
		InputStream in = hibernateClassLoaderService.locateResourceStream( name );
		if ( in == null ) {
			in = internalClassResolver.locateResourceStream( name );
		}
		return in;
	}

	@Override
	public <T> Iterable<T> loadJavaServices(Class<T> serviceContract) {
		// when it comes to services, we need to search in both services and the de-duplicate
		// however, we cannot rely on 'equals' for comparison. Instead compare class names
		Iterable<T> servicesFromORMCLassLoader = hibernateClassLoaderService.loadJavaServices( serviceContract );
		Iterable<T> servicesFromLocalClassLoader = internalClassResolver.loadJavaServices( serviceContract );

		//LinkedHashMap to maintain order; elements from Hibernate ORM first.
		Map<String,T> combined = new LinkedHashMap<>();

		addAllServices( servicesFromORMCLassLoader, combined );
		addAllServices( servicesFromLocalClassLoader, combined );

		return combined.values();
	}

	private <T> void addAllServices(Iterable<T> services, Map<String,T> combined ) {
		for ( T service : services ) {
			combined.put( service.getClass().getName(), service );
		}
	}

}

