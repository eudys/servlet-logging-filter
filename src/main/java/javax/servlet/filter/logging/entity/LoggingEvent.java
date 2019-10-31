package javax.servlet.filter.logging.entity;

import javax.servlet.filter.logging.wrapper.LoggingHttpServletRequestWrapper;
import javax.servlet.filter.logging.wrapper.LoggingHttpServletResponseWrapper;
import java.io.Serializable;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import static org.apache.commons.lang3.StringUtils.trimToEmpty;

public class LoggingEvent implements Serializable
{

	private static final long serialVersionUID = -4702574169916528738L;

	private String requestData = "";
	private String responseData = "";
	private long startTime;
	private long endTime;
	private int statusCode;
	private String eventId;
	private Map<String, Object> properties = new HashMap<>();

	public LoggingEvent()
	{
	}

	public String getRequestData()
	{
		return requestData;
	}

	public void setRequestData(String requestData)
	{
		this.requestData = requestData;
	}

	public String getResponseData()
	{
		return responseData;
	}

	public void setResponseData(String responseData)
	{
		this.responseData = responseData;
	}

	public long getStartTime()
	{
		return startTime;
	}

	public void setStartTime(long startTime)
	{
		this.startTime = startTime;
	}

	public long getEndTime()
	{
		return endTime;
	}

	public void setEndTime(long endTime)
	{
		this.endTime = endTime;
	}

	public int getStatusCode()
	{
		return statusCode;
	}

	public void setStatusCode(int statusCode)
	{
		this.statusCode = statusCode;
	}

	public String getEventId()
	{
		return eventId;
	}

	public void setEventId(String eventId)
	{
		this.eventId = eventId;
	}

	public Map<String, Object> getProperties()
	{
		return properties;
	}

	public void setRequestHttpHeaders(LoggingHttpServletRequestWrapper requestWrapper)
	{
		Enumeration<String> headerNames = requestWrapper.getHeaderNames();

		while (headerNames.hasMoreElements())
		{
			String headerName = headerNames.nextElement();
			String value = requestWrapper.getHeader(headerName);
			properties.put("HTTP_REQUEST_HEADER_" + headerName, value);
		}
	}

	public void setRequestProperties(LoggingHttpServletRequestWrapper requestWrapper)
	{
		properties.put("HTTP_REQUEST_AUTH_TYPE", trimToEmpty(requestWrapper.getAuthType()));
		properties.put("HTTP_REQUEST_CHAR_ENCODING", trimToEmpty(requestWrapper.getCharacterEncoding()));
		properties.put("HTTP_REQUEST_CONTENT_TYPE", trimToEmpty(requestWrapper.getContentType()));
		properties.put("HTTP_REQUEST_LOCAL_IP", trimToEmpty(requestWrapper.getLocalAddr()));
		properties.put("HTTP_REQUEST_LOCAL_HOST", trimToEmpty(requestWrapper.getLocalName()));
		properties.put("HTTP_REQUEST_METHOD", trimToEmpty(requestWrapper.getMethod()));
		properties.put("HTTP_REQUEST_REMOTE_IP", trimToEmpty(requestWrapper.getRemoteAddr()));
		properties.put("HTTP_REQUEST_REMOTE_HOST", trimToEmpty(requestWrapper.getRemoteHost()));
		properties.put("HTTP_REQUEST_URI", trimToEmpty(requestWrapper.getRequestURI()));
	}

	public void setResponseHttpHeaders(LoggingHttpServletResponseWrapper responseWrapper)
	{
		Collection<String> headerNames = responseWrapper.getHeaderNames();
		for(String headerName: headerNames)
		{
			String value = responseWrapper.getHeader(headerName);
			properties.put("HTTP_RESPONSE_HEADER_"+ headerName, value);
		}
	}
}
