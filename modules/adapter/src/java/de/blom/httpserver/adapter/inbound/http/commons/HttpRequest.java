package de.blom.httpserver.adapter.inbound.http.commons;

import de.blom.httpserver.crosscutting.enums.HttpMethod;
import java.io.BufferedReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
public class HttpRequest {
  private static final Logger log = Logger.getLogger(HttpRequest.class.getName());

  private final HttpMethod method;
  private final String uri;
  private final Map<String, String> headers;
  private final String rawBody;

  public HttpRequest(
      final String method, final String uri, final Map<String, String> headers,
      final String rawBody) {
    this.method = identifyHTTPMethod(method);
    this.uri = uri;
    this.headers = Objects.requireNonNullElseGet(headers, HashMap::new);
    this.rawBody = rawBody;

  }

  static HttpMethod identifyHTTPMethod(String method) {
    method = method.toUpperCase();
    try {
      return HttpMethod.valueOf(method);
    } catch (final IllegalArgumentException e) {
      log.log(Level.SEVERE, "Given method can not be handled yet", e);
      return HttpMethod.NOT_IMPLEMENTED_YET;
    }
  }

  public boolean isContentTypeApplicationJson() {
    return this.headers.containsKey(HeaderKeys.CONTENT_TYPE) &&
        HeaderValues.CONTENT_TYPE_APPLICATION_JSON
            .equals(this.headers.get(HeaderKeys.CONTENT_TYPE));
  }

  public static HttpRequest parseFrom(final BufferedReader in) throws IOException {
    final Map<String, String> headers = new HashMap<>();

    final String firstLine = in.readLine();
    if (firstLine == null) {
      return null;
    }
    final StringTokenizer firstHttpLine = new StringTokenizer(firstLine);

    final String method = firstHttpLine.nextToken().toUpperCase();
    final String uri = firstHttpLine.nextToken();

    String nextLine = in.readLine();

    nextLine = parseHeaderElements(in, headers, nextLine);
    final String httpBody = parseHttpBody(in, nextLine);

    return new HttpRequest(method, uri, headers, httpBody);
  }

  private static String parseHttpBody(final BufferedReader in, String nextLine) throws IOException {
    String httpBody = "";
    while (nextLine != null && in.ready()) {
      httpBody = httpBody.concat(nextLine);
      nextLine = in.readLine();
    }
    if (nextLine != null) {
      httpBody = httpBody.concat(nextLine);
    }
    return httpBody;
  }

  private static String parseHeaderElements(final BufferedReader in,
      final Map<String, String> headers,
      String nextLine) throws IOException {
    Header headerElement = parseHttpHeaderFromLine(nextLine);

    while (headerElement != null) {
      headers.put(headerElement.getName(), headerElement.getValue());

      nextLine = in.readLine();
      headerElement = parseHttpHeaderFromLine(nextLine);
    }
    return nextLine;
  }

  static Header parseHttpHeaderFromLine(final String line) {
    if (line == null) {
      return null;
    }
    final StringTokenizer httpHeaderLineTokens = new StringTokenizer(line);

    if (!httpHeaderLineTokens.hasMoreTokens()) {
      return null;
    }

    String headerName = httpHeaderLineTokens.nextToken();

    if (headerName.endsWith(":")) {
      headerName = headerName.replace(":", "");
      final StringBuilder headerValue = new StringBuilder(httpHeaderLineTokens.nextToken());
      while (httpHeaderLineTokens.hasMoreTokens()) {
        headerValue.append(" ").append(httpHeaderLineTokens.nextToken());
      }
      return new Header(headerName, headerValue.toString());

    } else {
      return null;
    }
  }

  public CacheHeaders getCacheHeaders() {
    Date ifModifiedSince = null;
    String ifMatch = null;
    String ifNoneMatch = null;

    if (this.headers.containsKey(HeaderKeys.IF_MODIFIED_SINCE)) {
      final String rawDateValue = this.headers.get(HeaderKeys.IF_MODIFIED_SINCE);
      final SimpleDateFormat format = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz");
      try {
        ifModifiedSince = format.parse(rawDateValue);

      } catch (final ParseException e) {
        log.info("If-Modified-Since header could not be parsed");
      }
    }
    if (this.headers.containsKey(HeaderKeys.IF_MATCH)) {
      ifMatch = this.headers.get(HeaderKeys.IF_MATCH).replace("\"", "");
    }
    if (this.headers.containsKey(HeaderKeys.IF_NONE_MATCH)) {
      ifNoneMatch = this.headers.get(HeaderKeys.IF_NONE_MATCH).replace("\"", "");
    }
    if (ifModifiedSince == null && ifMatch == null && ifNoneMatch == null) {
      return null;
    } else {
      return new CacheHeaders(ifNoneMatch, ifMatch, ifModifiedSince);
    }

  }

  public boolean keepConnectionAlive() {
    if (this.headers.containsKey(HeaderKeys.CONNECTION)) {
      if (HeaderValues.CONNECTION_CLOSE.equals(this.headers.get(HeaderKeys.CONNECTION))) {
        return false;
      } else {
        return HeaderValues.CONNECTION_KEEP_ALIVE
            .equals(this.headers.get(HeaderKeys.CONNECTION));
      }
    } else {
      return false;
    }
  }

  private static class HeaderKeys {
    private static final String IF_MODIFIED_SINCE = "If-Modified-Since";
    private static final String IF_MATCH = "If-Match";
    private static final String IF_NONE_MATCH = "If-None-Match";
    private static final String CONNECTION = "Connection";
    private static final String CONTENT_TYPE = "Content-Type";
  }

  private static class HeaderValues {
    private static final String CONTENT_TYPE_APPLICATION_JSON = "application/json";
    private static final String CONNECTION_KEEP_ALIVE = "keep-alive";
    private static final String CONNECTION_CLOSE = "close";
  }

  @Data
  @AllArgsConstructor
  @EqualsAndHashCode
  public static class Header {
    private String name;
    private String value;
  }

  @Getter
  @AllArgsConstructor
  @NoArgsConstructor
  @Builder
  @EqualsAndHashCode
  public static class CacheHeaders {
    private String ifNonMatch;
    private String ifMatch;
    private Date ifModifiedSince;
  }
}
