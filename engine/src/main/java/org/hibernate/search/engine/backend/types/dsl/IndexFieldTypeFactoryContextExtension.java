/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.engine.backend.types.dsl;


/**
 * An extension to the index field type DSL, allowing to create non-standard types in an index schema.
 * <p>
 * <strong>WARNING:</strong> while this type is API, because instances should be manipulated by users,
 * all of its methods are considered SPIs and therefore should never be called or implemented directly by users.
 * In short, users are only expected to get instances of this type from an API ({@code SomeExtension.get()})
 * and pass it to another API.
 *
 * @param <T> The type of extended schema field contexts. Should generally extend {@link IndexFieldTypeFactoryContext}.
 *
 * @see IndexFieldTypeFactoryContext#extension(IndexFieldTypeFactoryContextExtension)
 */
public interface IndexFieldTypeFactoryContextExtension<T> {

	/**
	 * Attempt to extend a given context, throwing an exception in case of failure.
	 * <p>
	 * <strong>WARNING:</strong> this method is not API, see comments at the type level.
	 *
	 * @param original The original, non-extended {@link IndexFieldTypeFactoryContext}.
	 * @return An extended search predicate factory context ({@link T})
	 */
	T extendOrFail(IndexFieldTypeFactoryContext original);

}
