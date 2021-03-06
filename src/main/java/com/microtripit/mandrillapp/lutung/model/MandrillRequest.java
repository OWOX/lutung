/**
 * 
 */
package com.microtripit.mandrillapp.lutung.model;

import com.microtripit.mandrillapp.lutung.logging.Logger;
import com.microtripit.mandrillapp.lutung.logging.LoggerFactory;
import org.apache.commons.io.IOUtils;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.StringEntity;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.PropertyNamingStrategy;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

/**
 * @author rschreijer
 * @since Mar 16, 2013
 */
public final class MandrillRequest<OUT> implements RequestModel<OUT> {
    private static final Logger log = LoggerFactory.getLogger(MandrillRequest.class);

	private final String url;
	private final Class<OUT> responseContentType;
	private final Map<String,? extends Object> requestParams;
	
	public MandrillRequest( final String url, 
			final Map<String,? extends Object> params, 
			final Class<OUT> responseType ) {
		
		if(responseType == null) {
			throw new NullPointerException();
			
		}
		this.url = url;
		this.requestParams = params;
		this.responseContentType = responseType;
	}

	public final String getUrl() {
		return url;
	}

	public final HttpRequestBase getRequest() throws IOException {
		final String paramsStr = new ObjectMapper()
				.setSerializationInclusion(JsonSerialize.Inclusion.NON_NULL)
				.setPropertyNamingStrategy(PropertyNamingStrategy.CAMEL_CASE_TO_LOWER_CASE_WITH_UNDERSCORES)
				.writeValueAsString(requestParams);
        log.debug("raw content for request:\n" +paramsStr);
		final StringEntity entity = new StringEntity(paramsStr, "UTF-8");
		entity.setContentType("application/json");
		final HttpPost request = new HttpPost(url);
		request.setEntity(entity);
		return request;
		
	}

	public final boolean validateResponseStatus(final int httpResponseStatus) {
		return (httpResponseStatus == 200);
	}

	public final OUT handleResponse(final InputStream is) 
			throws HandleResponseException {
		
		String raw = null;
		try {
			raw = IOUtils.toString(is);
            log.debug("raw content from response:\n" +raw);
			return LutungGsonUtils.getGson().fromJson(
					raw, responseContentType);
			
		} catch(final Throwable t) {
			String msg = "Error handling Mandrill response " +
					((raw != null)?": '"+raw+"'" : "");
			throw new HandleResponseException(msg, t);
			
		}
	}

}
