/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.engine.search.dsl.predicate.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import org.hibernate.search.engine.search.SearchPredicate;
import org.hibernate.search.engine.search.dsl.predicate.MatchAllPredicateContext;
import org.hibernate.search.engine.search.dsl.predicate.SearchPredicateFactoryContext;
import org.hibernate.search.engine.search.dsl.predicate.SearchPredicateTerminalContext;
import org.hibernate.search.engine.search.dsl.predicate.spi.AbstractSearchPredicateTerminalContext;
import org.hibernate.search.engine.search.predicate.spi.BooleanJunctionPredicateBuilder;
import org.hibernate.search.engine.search.predicate.spi.MatchAllPredicateBuilder;
import org.hibernate.search.engine.search.predicate.spi.SearchPredicateBuilderFactory;


class MatchAllPredicateContextImpl<B>
		extends AbstractSearchPredicateTerminalContext<B>
		implements MatchAllPredicateContext {

	private final SearchPredicateFactoryContext factoryContext;

	private final MatchAllPredicateBuilder<B> matchAllBuilder;
	private MatchAllExceptContext exceptContext;

	MatchAllPredicateContextImpl(SearchPredicateBuilderFactory<?, B> factory, SearchPredicateFactoryContext factoryContext) {
		super( factory );
		this.factoryContext = factoryContext;
		this.matchAllBuilder = factory.matchAll();
	}

	@Override
	public MatchAllPredicateContext boostedTo(float boost) {
		this.matchAllBuilder.boost( boost );
		return this;
	}

	@Override
	public MatchAllPredicateContext except(SearchPredicate searchPredicate) {
		getExceptContext().addClause( searchPredicate );
		return this;
	}

	@Override
	public MatchAllPredicateContext except(
			Function<? super SearchPredicateFactoryContext, ? extends SearchPredicateTerminalContext> clauseContributor) {
		getExceptContext().addClause( clauseContributor );
		return this;
	}

	@Override
	protected B toImplementation() {
		if ( exceptContext != null ) {
			return exceptContext.toImplementation( matchAllBuilder.toImplementation() );
		}
		else {
			return matchAllBuilder.toImplementation();
		}
	}

	private MatchAllExceptContext getExceptContext() {
		if ( exceptContext == null ) {
			exceptContext = new MatchAllExceptContext();
		}
		return exceptContext;
	}

	private class MatchAllExceptContext {

		private final BooleanJunctionPredicateBuilder<B> booleanBuilder;
		private final List<B> clauseBuilders = new ArrayList<>();

		MatchAllExceptContext() {
			this.booleanBuilder = MatchAllPredicateContextImpl.this.factory.bool();
		}

		void addClause(Function<? super SearchPredicateFactoryContext, ? extends SearchPredicateTerminalContext> clauseContributor) {
			addClause( clauseContributor.apply( factoryContext ).toPredicate() );
		}

		void addClause(SearchPredicate predicate) {
			clauseBuilders.add( factory.toImplementation( predicate ) );
		}

		B toImplementation(B matchAllBuilder) {
			booleanBuilder.must( matchAllBuilder );
			for ( B builder : clauseBuilders ) {
				booleanBuilder.mustNot( builder );
			}
			return booleanBuilder.toImplementation();
		}

	}
}
