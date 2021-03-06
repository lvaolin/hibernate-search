/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.backend.elasticsearch.orchestration.impl;

import java.util.concurrent.CompletableFuture;

import org.hibernate.search.backend.elasticsearch.work.impl.BulkableElasticsearchWork;

/**
 * Aggregates bulkable works into bulks and adds all resulting works
 * to a sequence builder.
 * <p>
 * Implementations are mutable and unlikely to be thread-safe.
 *
 * @author Yoann Rodiere
 */
public interface ElasticsearchWorkBulker {

	/**
	 * @param work A work to add to the current bulk
	 * @return A future that will ultimately contain the result of executing the work, or an exception.
	 */
	<T> CompletableFuture<T> add(BulkableElasticsearchWork<T> work);

	/**
	 * Ensure that all bulked works that haven't been added to a sequence yet
	 * are added to the underlying sequence builder.
	 * <p>
	 * After this method is called, the underlying sequence builder can
	 * safely be used to build a new sequence, but the execution of bulked works
	 * in this sequence will block until {@link #finalizeBulkWork()} has been called.
	 *
	 * @return {@code true} if works have been added to the sequence builder
	 * <strong>as part of the current bulk</strong>,
	 * {@code false} if they have been added as non-bulked works
	 * (for instance if there was only one work) or if no work
	 * was added to the sequence builder.
	 */
	boolean addWorksToSequence();

	/**
	 * Ensure that the bulk work (if any) is created.
	 * <p>
	 * This method expects that all works have been added to the sequence builder
	 * using {@link #addWorksToSequence()} beforehand.
	 * <p>
	 * After this method is called, any new work added through {@link #add(BulkableElasticsearchWork)}
	 * will be added to a new bulk.
	 */
	void finalizeBulkWork();

	/**
	 * Reset internal state.
	 */
	void reset();

}
