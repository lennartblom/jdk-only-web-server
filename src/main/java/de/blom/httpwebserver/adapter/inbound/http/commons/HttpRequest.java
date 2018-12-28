package de.blom.httpwebserver.adapter.inbound.http.commons;

import de.blom.httpwebserver.enums.HttpMethod;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

@Getter
public class HttpRequest {
    private static final Logger log = Logger.getLogger(HttpRequest.class.getName());

    private HttpMethod method;
    private String uri;
    private Map<String, String> headers;
    private String rawBody;

    public HttpRequest(String method, String uri, Map<String, String> headers, String rawBody) {
        this.method = identifyHTTPMethod(method);
        this.uri = uri;
        if(headers != null){
            this.headers = headers;
        }else {
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

    public static HttpRequest parseFrom(BufferedReader in) throws IOException {
        Map<String, String> headers = new HashMap<>();

        String firstLine = in.readLine();
        if(firstLine == null){
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
        if(nextLine != null){
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

    static Header parseHttpHeaderFromLine(String line){
        if(line == null){
            return null;
        }
        StringTokenizer httpHeaderLineTokens = new StringTokenizer(line);

        if(!httpHeaderLineTokens.hasMoreTokens()){
            return null;
        }

        String headerName = httpHeaderLineTokens.nextToken();

        if(headerName.endsWith(":")){
            headerName = headerName.replace(":", "");
            StringBuilder headerValue = new StringBuilder(httpHeaderLineTokens.nextToken());
            while(httpHeaderLineTokens.hasMoreTokens()){
                headerValue.append(" ").append(httpHeaderLineTokens.nextToken());
            }
            return new Header(headerName, headerValue.toString());

        }else {
            return null;
        }
    }


    @AllArgsConstructor
    @Getter
    public static class Header {
        private String name;
        private String value;
    }
}
