package javax.servlet.filter.logging;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.filter.logging.entity.LoggingEvent;
import javax.servlet.filter.logging.wrapper.LoggingHttpServletRequestWrapper;
import javax.servlet.filter.logging.wrapper.LoggingHttpServletResponseWrapper;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.util.Arrays.asList;
import static java.util.Collections.emptySet;
import static java.util.Objects.requireNonNull;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

public class LoggingFilter implements Filter {

	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
	MqClient mqClient;


	private int maxContentSize;

	private Set<String> excludedPaths;

	private String requestPrefix;

	private String responsePrefix;
/*
	private Marker requestMarker;

	private Marker responseMarker;*/

	private boolean disableMarker;

	private boolean disablePrefix;

	static {
		OBJECT_MAPPER.setSerializationInclusion(Include.NON_EMPTY);
	}

	public LoggingFilter() {
		this(Builder.create());
	}

	public LoggingFilter(Builder builder) {
		requireNonNull(builder, "builder must not be null");

		this.maxContentSize = builder.maxContentSize;
		this.excludedPaths = builder.excludedPaths;
/*		this.requestPrefix = builder.requestPrefix;
		this.responsePrefix = builder.responsePrefix;
		this.requestMarker = builder.requestMarker;
		this.responseMarker = builder.responseMarker;*/
		this.disableMarker = builder.disableMarker;
		this.disablePrefix = builder.disablePrefix;
	}

	@Override
	public void init(FilterConfig filterConfig) {

		try
		{
			mqClient = MqClient.getInstance();

			String maxContentSizeParam = filterConfig.getInitParameter("maxContentSize");
			if (maxContentSizeParam != null)
			{
				this.maxContentSize = Integer.parseInt(maxContentSizeParam);
			}

			String excludedPathsParam = filterConfig.getInitParameter("excludedPaths");
			if (isNotBlank(excludedPathsParam))
			{
				String[] paths = excludedPathsParam.split("\\s*,\\s*");
				this.excludedPaths = new HashSet<>(asList(paths));
			}

			String requestPrefixParam = filterConfig.getInitParameter("requestPrefix");
			if (isNotBlank(requestPrefixParam))
			{
				this.requestPrefix = requestPrefixParam;
			}

			String responsePrefixParam = filterConfig.getInitParameter("responsePrefix");
			if (isNotBlank(responsePrefixParam))
			{
				this.responsePrefix = responsePrefixParam;
			}

/*		String requestMarkerParam = filterConfig.getInitParameter("requestMarker");
		if (isNotBlank(requestMarkerParam)) {
			this.requestMarker = MarkerFactory.getMarker(requestMarkerParam);
		}

		String responseMarkerParam = filterConfig.getInitParameter("responseMarker");
		if (isNotBlank(responseMarkerParam)) {
			this.responseMarker = MarkerFactory.getMarker(responseMarkerParam);
		}*/

			String disablePrefixParam = filterConfig.getInitParameter("disablePrefix");
			if (isNotBlank(disablePrefixParam))
			{
				this.disablePrefix = Boolean.valueOf(disablePrefixParam);
			}

			String disableMarkerParam = filterConfig.getInitParameter("disableMarker");
			if (isNotBlank(disableMarkerParam))
			{
				this.disableMarker = Boolean.valueOf(disableMarkerParam);
			}
		}catch (Exception e)
		{
			Logger.getGlobal().log(Level.SEVERE, "Failed to initialize LoggingFilter", e);
		}
	}

	@Override
	@SuppressWarnings({"squid:S3457", "squid:S2629"})
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain) throws IOException, ServletException
	{
		if (!(request instanceof HttpServletRequest) || !(response instanceof HttpServletResponse))
		{
			throw new ServletException("LoggingFilter just supports HTTP requests");
		}
		HttpServletRequest httpRequest = (HttpServletRequest) request;
		HttpServletResponse httpResponse = (HttpServletResponse) response;

/*		if (!log.isDebugEnabled()) {
			filterChain.doFilter(httpRequest, httpResponse);
			return;
		}*/
		for (String excludedPath : excludedPaths)
		{
			String requestURI = httpRequest.getRequestURI();
			if (requestURI.contains(excludedPath))
			{
				filterChain.doFilter(httpRequest, httpResponse);
				return;
			}
		}

		LoggingHttpServletRequestWrapper requestWrapper = new LoggingHttpServletRequestWrapper(httpRequest);

		LoggingEvent loggingRequest = new LoggingEvent();
		loggingRequest.setEventId(UUID.randomUUID().toString());
		loggingRequest.setStartTime(System.currentTimeMillis());

		loggingRequest.getProperties().put("APP_NAME", "solid-eudy");
		loggingRequest.setRequestHttpHeaders(requestWrapper);
		loggingRequest.setRequestProperties(requestWrapper);

		LoggingHttpServletResponseWrapper responseWrapper = new LoggingHttpServletResponseWrapper(httpResponse);

		if (requestWrapper.isApiCall())
		{
			loggingRequest.setRequestData(requestWrapper.getContent());
			filterChain.doFilter(requestWrapper, responseWrapper);

		}else
		{
			filterChain.doFilter(requestWrapper, httpResponse);
			loggingRequest.setRequestData(requestWrapper.getContent());
		}

		int status = httpResponse.getStatus();
		System.out.println("TEST STATUS " + status);

		loggingRequest.setStatusCode(responseWrapper.getStatus());
		loggingRequest.setEndTime(System.currentTimeMillis());
		loggingRequest.setResponseHttpHeaders(responseWrapper);

		if (requestWrapper.isApiCall())
		{
			loggingRequest.setResponseData(responseWrapper.getContent());
		}

		String message = OBJECT_MAPPER.writeValueAsString(loggingRequest);
		System.out.println(message); //LOG request

		mqClient.send(message);
	}

	@Override
	public void destroy() {
		// nothing special
		mqClient.cleanup();
	}


	public static class Builder {

		private String loggerName = LoggingFilter.class.getName();

		private int maxContentSize = 1024;

		private Set<String> excludedPaths = emptySet();

/*		private Marker requestMarker = MarkerFactory.getMarker("REQUEST");
		private String requestPrefix = requestMarker.getName() + ": ";

		private Marker responseMarker = MarkerFactory.getMarker("RESPONSE");
		private String responsePrefix = responseMarker.getName() + ": ";*/

		private boolean disableMarker;
		private boolean disablePrefix;

		public static Builder create() {
			return new Builder();
		}

/*		public void loggerName(String loggerName) {
			requireNonNull(loggerName, "loggerName must not be null");
			this.loggerName = loggerName;
		}

		public Builder maxContentSize(int maxContentSize) {
			this.maxContentSize = maxContentSize;
			return this;
		}

		public Builder excludedPaths(String... excludedPaths) {
			requireNonNull(excludedPaths, "excludedPaths must not be null");
			this.excludedPaths = Stream.of(excludedPaths).collect(toSet());
			return this;
		}

		public Builder requestMarker(String marker) {
			requireNonNull(marker, "marker must not be null");
			this.requestMarker = MarkerFactory.getMarker(marker);
			return this;
		}

		public Builder requestPrefix(String requestPrefix) {
			requireNonNull(requestPrefix, "requestPrefix must not be null");
			this.requestPrefix = requestPrefix;
			return this;
		}

		public Builder responsePrefix(String responsePrefix) {
			requireNonNull(responsePrefix, "responsePrefix must not be null");
			this.responsePrefix = responsePrefix;
			return this;
		}*/

/*		public Builder responseMarker(String marker) {
			requireNonNull(marker, "marker must not be null");
			this.responseMarker = MarkerFactory.getMarker(marker);
			return this;
		}

		public Builder disableMarker(boolean disable) {
			this.disableMarker = disable;
			return this;
		}

		public Builder disablePrefix(boolean disable) {
			this.disablePrefix = disable;
			return this;
		}*/

		public LoggingFilter build() {
			return new LoggingFilter(this);
		}
	}
}