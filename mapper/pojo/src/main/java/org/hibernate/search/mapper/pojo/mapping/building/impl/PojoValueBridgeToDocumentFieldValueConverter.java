/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.mapper.pojo.mapping.building.impl;

import java.util.Optional;

import org.hibernate.search.engine.backend.types.converter.ToDocumentFieldValueConverter;
import org.hibernate.search.engine.backend.types.converter.runtime.ToDocumentFieldValueConvertContext;
import org.hibernate.search.engine.backend.types.converter.runtime.ToDocumentFieldValueConvertContextExtension;
import org.hibernate.search.engine.mapper.mapping.context.spi.MappingContextImplementor;
import org.hibernate.search.mapper.pojo.bridge.ValueBridge;
import org.hibernate.search.mapper.pojo.bridge.runtime.ValueBridgeToIndexedValueContext;
import org.hibernate.search.mapper.pojo.mapping.context.spi.AbstractPojoMappingContextImplementor;

final class PojoValueBridgeToDocumentFieldValueConverter<U, V extends U, F> implements
		ToDocumentFieldValueConverter<V, F> {

	private final ValueBridge<U, F> bridge;

	PojoValueBridgeToDocumentFieldValueConverter(ValueBridge<U, F> bridge) {
		this.bridge = bridge;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "[" + bridge + "]";
	}

	@Override
	public F convert(V value, ToDocumentFieldValueConvertContext context) {
		return bridge.toIndexedValue( value, context.extension( PojoValueBridgeContextExtension.INSTANCE ) );
	}

	@Override
	public F convertUnknown(Object value, ToDocumentFieldValueConvertContext context) {
		return bridge.toIndexedValue( bridge.cast( value ), context.extension( PojoValueBridgeContextExtension.INSTANCE ) );
	}

	@Override
	public boolean isCompatibleWith(ToDocumentFieldValueConverter<?, ?> other) {
		if ( other == null || !getClass().equals( other.getClass() ) ) {
			return false;
		}
		PojoValueBridgeToDocumentFieldValueConverter<?, ?, ?> castedOther =
				(PojoValueBridgeToDocumentFieldValueConverter<?, ?, ?>) other;
		return bridge.isCompatibleWith( castedOther.bridge );
	}

	private static class PojoValueBridgeContextExtension
			implements ToDocumentFieldValueConvertContextExtension<ValueBridgeToIndexedValueContext> {
		private static final PojoValueBridgeContextExtension INSTANCE = new PojoValueBridgeContextExtension();

		@Override
		public Optional<ValueBridgeToIndexedValueContext> extendOptional(ToDocumentFieldValueConvertContext original,
			MappingContextImplementor mappingContext) {
			if ( mappingContext instanceof AbstractPojoMappingContextImplementor ) {
				AbstractPojoMappingContextImplementor pojoMappingContext = (AbstractPojoMappingContextImplementor) mappingContext;
				return Optional.of( pojoMappingContext.getToIndexedValueContext() );
			}
			else {
				return Optional.empty();
			}
		}
	}
}
