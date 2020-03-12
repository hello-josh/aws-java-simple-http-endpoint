package com.serverless;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Collections;
import java.util.Map;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.apache.log4j.Logger;

public class ApiGatewayResponse {

	@Getter
	private final int statusCode;

	@Getter
	private final String body;

	@Getter
	private final Map<String, String> headers;

	@Getter
	private final boolean isBase64Encoded;

	public ApiGatewayResponse(int statusCode, String body, Map<String, String> headers, boolean isBase64Encoded) {
		this.statusCode = statusCode;
		this.body = body;
		this.headers = headers;
		this.isBase64Encoded = isBase64Encoded;
	}

	public static Builder builder() {
		return new Builder();
	}

	@NoArgsConstructor
	@Accessors(fluent = true)
	public static class Builder {

		private static final Logger LOG = Logger.getLogger(ApiGatewayResponse.Builder.class);

		private static final ObjectMapper objectMapper = new ObjectMapper();

		@Setter
		private int statusCode = 200;
		@Setter
		private Map<String, String> headers = Collections.emptyMap();
		@Setter
		private String rawBody;
		@Setter
		private Object objectBody;

		private byte[] binaryBody;
		@Setter()
		private boolean base64Encoded;

		/**
		 * Builds the {@link ApiGatewayResponse} using the passed binary body
		 * encoded as base64. {@link #base64Encoded(boolean)
		 * setBase64Encoded(true)} will be in invoked automatically.
		 */
		public Builder setBinaryBody(byte[] binaryBody) {
			this.binaryBody = binaryBody;
			base64Encoded(true);
			return this;
		}

		public ApiGatewayResponse build() {
			String body = null;
			if (rawBody != null) {
				body = rawBody;
			} else if (objectBody != null) {
				try {
					body = objectMapper.writeValueAsString(objectBody);
				} catch (JsonProcessingException e) {
					LOG.error("failed to serialize object", e);
					throw new RuntimeException(e);
				}
			} else if (binaryBody != null) {
				body = new String(Base64.getEncoder().encode(binaryBody), StandardCharsets.UTF_8);
			}
			return new ApiGatewayResponse(statusCode, body, headers, base64Encoded);
		}
	}
}
