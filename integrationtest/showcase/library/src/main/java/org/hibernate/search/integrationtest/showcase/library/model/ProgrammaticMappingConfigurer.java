/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.integrationtest.showcase.library.model;

import org.hibernate.search.engine.backend.document.model.dsl.ObjectFieldStorage;
import org.hibernate.search.engine.backend.document.model.dsl.Sortable;
import org.hibernate.search.integrationtest.showcase.library.analysis.LibraryAnalysisConfigurer;
import org.hibernate.search.mapper.orm.mapping.HibernateOrmMappingDefinitionContainerContext;
import org.hibernate.search.mapper.orm.mapping.HibernateOrmSearchMappingConfigurer;
import org.hibernate.search.mapper.pojo.bridge.builtin.spatial.GeoPointBridge;
import org.hibernate.search.mapper.pojo.bridge.builtin.spatial.LatitudeMarker;
import org.hibernate.search.mapper.pojo.bridge.builtin.spatial.LongitudeMarker;
import org.hibernate.search.mapper.pojo.mapping.definition.programmatic.ProgrammaticMappingDefinitionContext;
import org.hibernate.search.integrationtest.showcase.library.bridge.AccountBorrowalSummaryBridge;
import org.hibernate.search.integrationtest.showcase.library.bridge.ISBNBridge;
import org.hibernate.search.integrationtest.showcase.library.bridge.MultiKeywordStringBridge;

/**
 * A programmatic mapping configurer that reproduces the exact same mapping as the annotations on model types.
 * <p>
 * While such a programmatic mapping would normally make no sense (we could just use the annotation mapping),
 * it is useful for demonstration purposes.
 */
public class ProgrammaticMappingConfigurer implements HibernateOrmSearchMappingConfigurer {
	@Override
	public void configure(HibernateOrmMappingDefinitionContainerContext context) {
		ProgrammaticMappingDefinitionContext mapping = context.programmaticMapping();

		mapping.type( Library.class ).indexed( Library.INDEX )
				.bridge( new GeoPointBridge.Builder().fieldName( "location" ) )
				.property( "id" ).documentId()
				.property( "name" )
						.field().analyzer( LibraryAnalysisConfigurer.ANALYZER_DEFAULT )
						.field( "name_sort" )
								.normalizer( LibraryAnalysisConfigurer.NORMALIZER_SORT )
								.sortable( Sortable.YES )
				.property( "collectionSize" )
						.field().sortable( Sortable.YES )
				.property( "latitude" ).marker( new LatitudeMarker.Builder() )
				.property( "longitude" ).marker( new LongitudeMarker.Builder() )
				.property( "services" )
						.field();

		mapping.type( Document.class )
				.property( "id" ).documentId()
				.property( "title" )
						.field().analyzer( LibraryAnalysisConfigurer.ANALYZER_DEFAULT )
						.field( "title_sort" )
								.normalizer( LibraryAnalysisConfigurer.NORMALIZER_SORT )
								.sortable( Sortable.YES )
				.property( "summary" )
						.field().analyzer( LibraryAnalysisConfigurer.ANALYZER_DEFAULT )
				.property( "tags" )
						.bridge(
								new MultiKeywordStringBridge.Builder()
								.fieldName( "tags" )
						)
				.property( "copies" )
						.indexedEmbedded()
								.includePaths( "medium", "library.location", "library.services" )
								.storage( ObjectFieldStorage.NESTED );
		mapping.type( Book.class ).indexed( Book.INDEX )
				.property( "isbn" )
						.field().valueBridge( ISBNBridge.class );
		mapping.type( Video.class ).indexed( Video.INDEX );

		mapping.type( DocumentCopy.class )
				.property( "document" )
						.indexedEmbedded()
				.property( "library" )
						.indexedEmbedded().maxDepth( 1 );
		mapping.type( BookCopy.class )
				.property( "medium" )
						.field();
		mapping.type( VideoCopy.class )
				.property( "medium" )
						.field();

		mapping.type( Person.class ).indexed( Person.INDEX )
				.property( "firstName" )
						.field().analyzer( LibraryAnalysisConfigurer.ANALYZER_DEFAULT )
						.field( "firstName_sort" )
								.normalizer( LibraryAnalysisConfigurer.NORMALIZER_SORT )
								.sortable( Sortable.YES )
				.property( "lastName" )
						.field().analyzer( LibraryAnalysisConfigurer.ANALYZER_DEFAULT )
						.field( "lastName_sort" )
								.normalizer( LibraryAnalysisConfigurer.NORMALIZER_SORT )
								.sortable( Sortable.YES )
				.property( "account" )
						.indexedEmbedded();

		mapping.type( Account.class )
				.bridge( AccountBorrowalSummaryBridge.class );
	}
}