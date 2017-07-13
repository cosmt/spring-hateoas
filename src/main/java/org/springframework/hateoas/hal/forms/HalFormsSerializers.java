/*
 * Copyright 2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.hateoas.hal.forms;

import static org.springframework.hateoas.hal.forms.HalFormsUtils.*;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;

import org.springframework.hateoas.Resource;
import org.springframework.hateoas.hal.Jackson2HalModule;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.ser.ContainerSerializer;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;

/**
 * @author Greg Turnquist
 */
public class HalFormsSerializers {

	static class HalFormsResourceSerializer extends ContainerSerializer<Resource<?>> implements ContextualSerializer {

		private final BeanProperty property;

		HalFormsResourceSerializer(BeanProperty property) {

			super(Resource.class, false);
			this.property = property;
		}

		HalFormsResourceSerializer() {
			this(null);
		}

		@Override
		public void serialize(Resource<?> value, JsonGenerator gen, SerializerProvider provider) throws IOException {

			ObjectMapper mapper = (ObjectMapper) gen.getCodec();

			HalFormsDocument doc = toHalFormsDocument(value, mapper);

			provider
				.findValueSerializer(HalFormsDocument.class, property)
				.serialize(doc, gen, provider);
		}

		@Override
		public JavaType getContentType() {
			return null;
		}

		@Override
		public JsonSerializer<?> getContentSerializer() {
			return null;
		}

		@Override
		public boolean hasSingleElement(Resource<?> resource) {
			return false;
		}

		@Override
		protected ContainerSerializer<?> _withValueTypeSerializer(TypeSerializer typeSerializer) {
			return null;
		}

		@Override
		public JsonSerializer<?> createContextual(SerializerProvider prov, BeanProperty property) throws JsonMappingException {
			return new HalFormsResourceSerializer(property);
		}
	}

	static class HalFormsEmbeddedResourcesSerializer extends ContainerSerializer<Collection<?>>
			implements ContextualSerializer {

		private static final long serialVersionUID = 1L;
		private final BeanProperty property;
		private final Jackson2HalModule.EmbeddedMapper embeddedMapper;

		public HalFormsEmbeddedResourcesSerializer(Jackson2HalModule.EmbeddedMapper embeddedMapper) {
			this(null, embeddedMapper);
		}

		public HalFormsEmbeddedResourcesSerializer(BeanProperty property, Jackson2HalModule.EmbeddedMapper embeddedMapper) {
			super(Collection.class, false);
			this.embeddedMapper = embeddedMapper;
			this.property = property;
		}

		@Override
		public void serialize(Collection<?> value, JsonGenerator jgen, SerializerProvider provider)
				throws IOException {
			Map<String, Object> embeddeds = embeddedMapper.map(value);

			provider.findValueSerializer(Map.class, property).serialize(embeddeds, jgen, provider);
		}

		@Override
		public JsonSerializer<?> createContextual(SerializerProvider prov, BeanProperty property)
				throws JsonMappingException {
			return new HalFormsEmbeddedResourcesSerializer(property, embeddedMapper);
		}

		@Override
		public JavaType getContentType() {
			return null;
		}

		@Override
		public JsonSerializer<?> getContentSerializer() {
			return null;
		}

		@Override
		public boolean hasSingleElement(Collection<?> value) {
			return value.size() == 1;
		}

		@Override
		protected ContainerSerializer<?> _withValueTypeSerializer(TypeSerializer vts) {
			return null;
		}

		@Override
		public boolean isEmpty(SerializerProvider provider, Collection<?> value) {
			return value.isEmpty();
		}
	}
	
}
