package com.ed.geospatial.writer.shared;

import com.google.common.collect.ImmutableMap;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class BasicExceptionMapper implements ExceptionMapper<RuntimeException> {

	private static final Logger LOGGER = LoggerFactory.getLogger(BasicExceptionMapper.class);

	@Override
	public Response toResponse(final RuntimeException e) {

		LOGGER.error("Error occur cause {}", e.getMessage(), e);

		Map<String, Object> errors = ImmutableMap.of(
				"errorCause", e.getMessage()
		);

		return Response.ok(
				new ResponseMessage<String>(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), errors)
		).build();
	}
}
