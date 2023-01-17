package com.ed.geospatial.reader.shared;

import com.google.common.collect.ImmutableMap;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.MediaType;
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
				"errorMsg", e.getMessage()
		);

		int status = (e instanceof NotFoundException) ? Response.Status.NOT_FOUND.getStatusCode() : Response.Status.INTERNAL_SERVER_ERROR.getStatusCode();

		return Response.ok(
				new ResponseMessage<String>(status, errors)
		).type(MediaType.APPLICATION_JSON_TYPE).build();
	}
}
