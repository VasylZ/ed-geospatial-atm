package com.ed.geospatial.core.shared;

import com.bedatadriven.jackson.datatype.jts.JtsModule;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author Oleg Tarapata (o.tarapata@epom.com)
 */
public enum ObjectMapperHolder {

	HOLDER;

	private final ObjectMapper mapper;

	ObjectMapperHolder() {
		this.mapper = new ObjectMapper();
		mapper.setSerializationInclusion(Include.NON_NULL);

		// deserialization features
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		mapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);

		mapper.registerModule(new JtsModule());
	}

	public ObjectMapper mapper() {
		return mapper;
	}
}
