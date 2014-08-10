package org.stagemonitor.web.monitor;

import net.sf.uadetector.ReadableUserAgent;
import net.sf.uadetector.UserAgentStringParser;
import net.sf.uadetector.service.UADetectorServiceFactory;
import org.stagemonitor.core.StageMonitor;
import org.stagemonitor.requestmonitor.RequestTrace;

import java.util.LinkedHashMap;
import java.util.Map;

public class HttpRequestTrace extends RequestTrace {

	private final UserAgentStringParser parser = UADetectorServiceFactory.getResourceModuleParser();
	private static final int MAX_ELEMENTS = 100;
	private static final Map<String, ReadableUserAgent> userAgentCache =
			new LinkedHashMap<String, ReadableUserAgent>(MAX_ELEMENTS + 1, 0.75f, true) {
				@Override
				protected boolean removeEldestEntry(Map.Entry eldest) {
					return size() > MAX_ELEMENTS;
				}
			};

	private final String url;
	private Integer statusCode;
	private final  Map<String, String> headers;
	private final String method;
	private Integer bytesWritten;
	private final UserAgentInformation userAgent;

	public HttpRequestTrace(GetNameCallback getNameCallback, String url, Map<String, String> headers, String method) {
		super(getNameCallback);
		this.url = url;
		this.headers = headers;
		userAgent = getUserAgentInformation(headers);
		this.method = method;
	}

	public static class UserAgentInformation {
		private final String type;
		private final String device;
		private final String os;
		private final String osFamily;
		private final String osVersion;
		private final String browser;
		private final String browserVersion;

		public UserAgentInformation(ReadableUserAgent userAgent) {
			type = userAgent.getTypeName();
			device = userAgent.getDeviceCategory().getName();
			os = userAgent.getOperatingSystem().getName();
			osFamily = userAgent.getOperatingSystem().getFamilyName();
			osVersion = userAgent.getOperatingSystem().getVersionNumber().toVersionString();
			browser = userAgent.getName();
			browserVersion = userAgent.getVersionNumber().toVersionString();
		}

		public String getType() {
			return type;
		}

		public String getDevice() {
			return device;
		}

		public String getOs() {
			return os;
		}

		public String getOsFamily() {
			return osFamily;
		}

		public String getOsVersion() {
			return osVersion;
		}

		public String getBrowser() {
			return browser;
		}

		public String getBrowserVersion() {
			return browserVersion;
		}
	}

	public String getUrl() {
		return url;
	}

	public Integer getStatusCode() {
		return statusCode;
	}

	public void setStatusCode(Integer statusCode) {
		this.statusCode = statusCode;
	}

	public Map<String, String> getHeaders() {
		return headers;
	}

	private UserAgentInformation getUserAgentInformation(Map<String, String> headers) {
		if (headers != null && StageMonitor.getConfiguration().isParseUserAgent()) {
			final String userAgentHeader = headers.get("user-agent");
			if (userAgentHeader != null) {
				ReadableUserAgent readableUserAgent = userAgentCache.get(userAgentHeader);
				if (readableUserAgent == null) {
					readableUserAgent = parser.parse(userAgentHeader);
					userAgentCache.put(userAgentHeader, readableUserAgent);
				}
				return new UserAgentInformation(readableUserAgent);
			}
		}
		return null;
	}

	public String getMethod() {
		return method;
	}

	public void setBytesWritten(Integer bytesWritten) {
		this.bytesWritten = bytesWritten;
	}

	public Integer getBytesWritten() {
		return bytesWritten;
	}

	public UserAgentInformation getUserAgent() {
		return userAgent;
	}

	@Override
	public String toString() {
		return toString(false);
	}

	public String toString(boolean asciiArt, boolean callStack) {
		StringBuilder sb = new StringBuilder(3000);
		sb.append(method).append(' ').append(url);
		if (getParameter() != null) {
			sb.append(getParameter());
		}
		sb.append(" (").append(statusCode).append(")\n");
		sb.append("id:     ").append(getId()).append('\n');
		sb.append("name:   ").append(getName()).append('\n');
		if (headers != null) {
			for (Map.Entry<String, String> entry : headers.entrySet()) {
				sb.append(entry.getKey()).append(": ").append(entry.getValue()).append('\n');
			}
		}
		if (callStack) {
			appendCallStack(sb, asciiArt);
		}
		return sb.toString();
	}
}
