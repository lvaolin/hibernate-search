/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.engine.environment.classpath.spi;

import java.io.InputStream;
import java.lang.invoke.MethodHandles;
import java.net.URL;
import java.util.LinkedHashSet;
import java.util.ServiceLoader;
import java.util.Set;

import org.hibernate.search.engine.environment.classpath.impl.AggregatedClassLoader;
import org.hibernate.search.engine.logging.impl.Log;
import org.hibernate.search.util.impl.common.LoggerFactory;

/**
 * Default implementation of {@code ClassResolver} using the old pre class loader service approach of
 * attempting to load from the current and thread context class loaders.
 *
 * @author Hardy Ferentschik
 */
public final class DefaultClassAndResourceResolver implements ClassResolver, ResourceResolver {

	private static final Log log = LoggerFactory.make( Log.class, MethodHandles.lookup() );

	private final AggregatedClassLoader aggregatedClassLoader;

	/**
	 * Constructs a ClassLoaderServiceImpl with standard set-up
	 */
	public DefaultClassAndResourceResolver() {
		final LinkedHashSet<ClassLoader> orderedClassLoaderSet = new LinkedHashSet<>();

		//  adding known class-loaders...
		orderedClassLoaderSet.add( DefaultClassAndResourceResolver.class.getClassLoader() );

		// then the TCCL, if one...
		final ClassLoader tccl = locateTCCL();
		if ( tccl != null ) {
			orderedClassLoaderSet.add( tccl );
		}

		// finally the system classloader
		final ClassLoader sysClassLoader = locateSystemClassLoader();
		if ( sysClassLoader != null ) {
			orderedClassLoaderSet.add( sysClassLoader );
		}

		// now build the aggregated class loader...
		this.aggregatedClassLoader = new AggregatedClassLoader(
				orderedClassLoaderSet.toArray( new ClassLoader[0] )
		);
	}

	@Override
	@SuppressWarnings({ "unchecked" })
	public <T> Class<T> classForName(String className) {
		try {
			return (Class<T>) Class.forName( className, true, aggregatedClassLoader );
		}
		catch (Exception | LinkageError e) {
			throw log.unableToLoadTheClass( className, e );
		}
	}

	@Override
	public URL locateResource(String name) {
		try {
			return aggregatedClassLoader.getResource( name );
		}
		catch (Exception ignore) {
			// Ignore
		}

		return null;
	}

	@Override
	public InputStream locateResourceStream(String name) {
		try {
			final InputStream stream = aggregatedClassLoader.getResourceAsStream( name );
			if ( stream != null ) {
				return stream;
			}
		}
		catch (Exception ignore) {
			// Ignore
		}

		final String stripped = name.startsWith( "/" ) ? name.substring( 1 ) : null;

		if ( stripped != null ) {
			try {
				return new URL( stripped ).openStream();
			}
			catch (Exception ignore) {
				// Ignore
			}

			try {
				final InputStream stream = aggregatedClassLoader.getResourceAsStream( stripped );
				if ( stream != null ) {
					return stream;
				}
			}
			catch (Exception ignore) {
				// Ignore
			}
		}

		return null;
	}


	@Override
	public <S> Set<S> loadJavaServices(Class<S> serviceContract) {
		ServiceLoader<S> serviceLoader = ServiceLoader.load( serviceContract, aggregatedClassLoader );
		final LinkedHashSet<S> services = new LinkedHashSet<>();
		for ( S service : serviceLoader ) {
			services.add( service );
		}
		return services;
	}

	private static ClassLoader locateSystemClassLoader() {
		try {
			return ClassLoader.getSystemClassLoader();
		}
		catch (Exception e) {
			return null;
		}
	}

	private static ClassLoader locateTCCL() {
		try {
			return Thread.currentThread().getContextClassLoader();
		}
		catch (Exception e) {
			return null;
		}
	}
}



