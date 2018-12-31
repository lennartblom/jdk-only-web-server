package de.blom.httpwebserver.adapter.inbound.http.commons;

import de.blom.httpwebserver.enums.HttpMethod;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.BufferedReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

@Getter
public class HttpRequest {
    private static final Logger log = Logger.getLogger(HttpRequest.class.getName());
    private static final String CONTENT_TYPE = "Content-Type";
    private static final String APPLICATION_JSON = "application/json";

    private HttpMethod method;
    private String uri;
    private Map<String, String> headers;
    private String rawBody;

    public HttpRequest(String method, String uri, Map<String, String> headers, String rawBody) {
        this.method = identifyHTTPMethod(method);
        this.uri = uri;
        if (headers != null) {
            this.headers = headers;
        } else {
            this.headers = new HashMap<>();
        }
        this.rawBody = rawBody;

    }

    static HttpMethod identifyHTTPMethod(String method) {
        method = method.toUpperCase();
        try {
            return HttpMethod.valueOf(method);
        } catch (IllegalArgumentException e) {
            log.log(Level.SEVERE, "Given method can not be handled yet", e);
            return HttpMethod.NOT_IMPLEMENTED_YET;
        }
    }

    public boolean isContentTypeApplicationJson() {
        return this.headers.containsKey(CONTENT_TYPE) && APPLICATION_JSON.equals(this.headers.get(CONTENT_TYPE));
    }

    public static HttpRequest parseFrom(BufferedReader in) throws IOException {
        Map<String, String> headers = new HashMap<>();

        String firstLine = in.readLine();
        if (firstLine == null) {
            return null;
        }
        StringTokenizer firstHttpLine = new StringTokenizer(firstLine);

        String method = firstHttpLine.nextToken().toUpperCase();
        String uri = firstHttpLine.nextToken();

        String nextLine = in.readLine();

        nextLine = parseHeaderElements(in, headers, nextLine);
        String httpBody = parseHttpBody(in, nextLine);

        return new HttpRequest(method, uri, headers, httpBody);
    }

    private static String parseHttpBody(BufferedReader in, String nextLine) throws IOException {
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

    private static String parseHeaderElements(BufferedReader in, Map<String, String> headers, String nextLine) throws IOException {
        Header headerElement = parseHttpHeaderFromLine(nextLine);

        while (headerElement != null) {
            headers.put(headerElement.getName(), headerElement.getValue());

            nextLine = in.readLine();
            headerElement = parseHttpHeaderFromLine(nextLine);
        }
        return nextLine;
    }

    static Header parseHttpHeaderFromLine(String line) {
        if (line == null) {
            return null;
        }
        StringTokenizer httpHeaderLineTokens = new StringTokenizer(line);

        if (!httpHeaderLineTokens.hasMoreTokens()) {
            return null;
        }

        String headerName = httpHeaderLineTokens.nextToken();

        if (headerName.endsWith(":")) {
            headerName = headerName.replace(":", "");
            StringBuilder headerValue = new StringBuilder(httpHeaderLineTokens.nextToken());
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

        if (this.headers.containsKey(HeaderKeys.IF_MODIFIED_SINCE)){
            String rawDateValue = this.headers.get(HeaderKeys.IF_MODIFIED_SINCE);
            SimpleDateFormat format = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz");
            try {
                ifModifiedSince = format.parse(rawDateValue);

            } catch (ParseException e) {
                log.info("If-Modified-Since header could not be parsed");
            }
        }
        if (this.headers.containsKey(HeaderKeys.IF_MATCH)){
            ifMatch = this.headers.get(HeaderKeys.IF_MATCH);
        }
        if (this.headers.containsKey(HeaderKeys.IF_NONE_MATCH)){
            ifNoneMatch = this.headers.get(HeaderKeys.IF_NONE_MATCH);
        }
        if(ifModifiedSince == null && ifMatch == null && ifNoneMatch == null){
            return null;
        }else {
            return new CacheHeaders(ifNoneMatch, ifMatch, ifModifiedSince);
        }

    }

    private static class HeaderKeys {
        private static final String IF_MODIFIED_SINCE = "If-Modified-Since";
        private static final String IF_MATCH = "If-Match";
        private static final String IF_NONE_MATCH = "If-None-Match";
    }


    @AllArgsConstructor
    @Getter
    public static class Header {
        private String name;
        private String value;
    }

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class CacheHeaders {
        private String ifNonMatch;
        private String ifMatch;
        private Date ifModifiedSince;
    }
}
