package de.blom.httpwebserver.adapter.inbound.http.commons;

import de.blom.httpwebserver.enums.HttpMethod;
import org.hamcrest.Matchers;
import org.hamcrest.beans.SamePropertyValuesAs;
import org.junit.Assert;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class HttpRequestTest {

    private static final String TEST_HTTP_REQUEST_NO_BODY = "GET /pub/WWW/TheProject.html HTTP/1.1\n" +
            "Host: google.com\n" +
            "cache-control: no-cache\n" +
            "Postman-Token: 96621bec-a11a-4c9c-a7ff-6a87c9369e02\n" +
            "";

    private static final String TEST_HTTP_REQUEST_PLAIN_TEXT_BODY = "POST /testRoute HTTP/1.1\n" +
            "Host: localhost\n" +
            "Content-Type: text/plain\n" +
            "cache-control: no-cache\n" +
            "Postman-Token: 96621bec-a11a-4c9c-a7ff-6a87c9369e02\n" +
            "Lorem ipsum dolor\n" +
            "\n";

    private static final String TEST_HTTP_REQUEST_JSON_BODY = "POST /testRoute HTTP/1.1\n" +
            "Host: localhost\n" +
            "Content-Type: application/json\n" +
            "cache-control: no-cache\n" +
            "Postman-Token: 96621bec-a11a-4c9c-a7ff-6a87c9369e02\n" +
            "{\n" +
            "  \"name\": \"1531923956.517\",\n" +
            "  \"login\": \"asd\",\n" +
            "  \"password\": \"testpassword\"\n" +
            "}\n" +
            "\n";

    private static final String TEST_HTTP_REQUEST_JSON_BODY_NO_BLANK_LAST_LINE = "POST /testRoute HTTP/1.1\n" +
            "Host: localhost\n" +
            "Content-Type: application/json\n" +
            "cache-control: no-cache\n" +
            "Postman-Token: 96621bec-a11a-4c9c-a7ff-6a87c9369e02\n" +
            "{\n" +
            "  \"name\": \"1531923956.517\",\n" +
            "  \"login\": \"asd\",\n" +
            "  \"password\": \"testpassword\"\n" +
            "}\n";

    private static final String TEST_CHROME_REQUEST = "GET / HTTP/1.1\n" +
            "Host: localhost:8080\n" +
            "Connection: keep-alive\n" +
            "Dec 28, 2018 6:11:18 PM de.blom.httpwebserver.adapter.inbound.http.HttpAdapter main\n" +
            "INFO: Connection opened. (Fri Dec 28 18:11:18 CET 2018)\n" +
            "Cache-Control: max-age=0\n" +
            "Upgrade-Insecure-Requests: 1\n" +
            "User-Agent: Mozilla/5.0 (Macintosh; Intel Mac OS X 10_13_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/71.0.3578.98 Safari/537.36\n" +
            "Accept: text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8\n" +
            "Accept-Encoding: gzip, deflate, br\n" +
            "Accept-Language: de,en-US;q=0.9,en;q=0.8\n" +
            "Cookie: PHPSESSID=3d8ujruupf7hf7su1gs2c7jcj6; JSESSIONID=1067490977143665192; grafana_sess=32da410c3a4292d1; redirect_to=%252F; io=kucVFKNOXuMq3nDRAAAa\n" +
            " \n";

    private static final String TEST_JQUERY_AJAX_CALL = "POST /wall_entries HTTP/1.1\n" +
            "Host: localhost:8080\n" +
            "Connection: keep-alive\n" +
            "Content-Length: 25\n" +
            "Pragma: no-cache\n" +
            "Cache-Control: no-cache\n" +
            "Accept: application/json, text/javascript, */*\n" +
            "Origin: http://localhost:8080\n" +
            "X-Requested-With: XMLHttpRequest\n" +
            "User-Agent: Mozilla/5.0 (Macintosh; Intel Mac OS X 10_13_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/71.0.3578.98 Safari/537.36\n" +
            "Content-Type: application/x-www-form-urlencoded\n" +
            "Referer: http://localhost:8080/wall_entry_creation.html\n" +
            "Accept-Encoding: gzip, deflate, br\n" +
            "Accept-Language: de,en-US;q=0.9,en;q=0.8\n" +
            "Cookie: PHPSESSID=3d8ujruupf7hf7su1gs2c7jcj6; JSESSIONID=1067490977143665192; grafana_sess=32da410c3a4292d1; redirect_to=%252F; io=kucVFKNOXuMq3nDRAAAa\n";


    private BufferedReader bufferedReader;

    private void prepareBufferedReader(String rawData) {
        this.bufferedReader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(rawData.getBytes())));
    }

    @Test
    public void expectToRetrieveHttp_noBody() throws IOException {
        this.prepareBufferedReader(TEST_HTTP_REQUEST_NO_BODY);
        Map<String, String> expectedHeaders = new HashMap<>();
        expectedHeaders.put("Host", "google.com");
        expectedHeaders.put("cache-control", "no-cache");
        expectedHeaders.put("Postman-Token", "96621bec-a11a-4c9c-a7ff-6a87c9369e02");

        HttpRequest httpRequest = HttpRequest.parseFrom(this.bufferedReader);

        assertThat(httpRequest.getMethod(), is(HttpMethod.GET));
        assertThat(httpRequest.getUri(), is("/pub/WWW/TheProject.html"));
        assertThat(httpRequest.getHeaders(), SamePropertyValuesAs.samePropertyValuesAs(expectedHeaders));
    }

    @Test
    public void expectToReturnFalsWithNotExistingContentTypeApplicationJson() {
        Map<String, String> headers = new HashMap<>();
        headers.put("Host", "google.com");
        HttpRequest httpRequest = new HttpRequest("POST", "/", headers, "");

        assertFalse(httpRequest.isContentTypeApplicationJson());
    }

    @Test
    public void expectToReturnTrueWithExistingContentTypeApplicationJson() {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        HttpRequest httpRequest = new HttpRequest("POST", "/", headers, "");

        assertTrue(httpRequest.isContentTypeApplicationJson());
    }

    @Test
    public void expectToRetrieveHttpWith_plainTextBody() throws IOException {
        this.prepareBufferedReader(TEST_HTTP_REQUEST_PLAIN_TEXT_BODY);
        Map<String, String> expectedHeaders = new HashMap<>();
        expectedHeaders.put("Content-Type", "text/plain");
        expectedHeaders.put("Host", "localhost");
        expectedHeaders.put("cache-control", "no-cache");
        expectedHeaders.put("Postman-Token", "96621bec-a11a-4c9c-a7ff-6a87c9369e02");

        HttpRequest httpRequest = HttpRequest.parseFrom(this.bufferedReader);

        assertThat(httpRequest.getMethod(), is(HttpMethod.POST));
        assertThat(httpRequest.getUri(), is("/testRoute"));
        assertThat(httpRequest.getHeaders(), SamePropertyValuesAs.samePropertyValuesAs(expectedHeaders));
        assertThat(httpRequest.getRawBody(), is("Lorem ipsum dolor"));
    }

    @Test
    public void expectToRetrieveHttpWith_jsonBody() throws IOException {
        this.prepareBufferedReader(TEST_HTTP_REQUEST_JSON_BODY);
        Map<String, String> expectedHeaders = new HashMap<>();
        expectedHeaders.put("Content-Type", "application/json");
        expectedHeaders.put("Host", "localhost");
        expectedHeaders.put("cache-control", "no-cache");
        expectedHeaders.put("Postman-Token", "96621bec-a11a-4c9c-a7ff-6a87c9369e02");

        HttpRequest httpRequest = HttpRequest.parseFrom(this.bufferedReader);

        assertThat(httpRequest.getMethod(), is(HttpMethod.POST));
        assertThat(httpRequest.getUri(), is("/testRoute"));
        assertThat(httpRequest.getHeaders(), SamePropertyValuesAs.samePropertyValuesAs(expectedHeaders));
        assertThat(httpRequest.getRawBody(), is("{  \"name\": \"1531923956.517\",  \"login\": \"asd\",  \"password\": \"testpassword\"}"));
    }

    @Test
    public void expectToRetrieveHttpWith_jsonBodyNoBlankLine() throws IOException {
        this.prepareBufferedReader(TEST_HTTP_REQUEST_JSON_BODY_NO_BLANK_LAST_LINE);

        HttpRequest httpRequest = HttpRequest.parseFrom(this.bufferedReader);

        assertThat(httpRequest.getMethod(), is(HttpMethod.POST));
        assertThat(httpRequest.getUri(), is("/testRoute"));
        assertThat(httpRequest.getRawBody(), is("{  \"name\": \"1531923956.517\",  \"login\": \"asd\",  \"password\": \"testpassword\"}"));
    }

    @Test
    public void expectToSetHeadersFromChromGetRequest() throws IOException {

        this.prepareBufferedReader(TEST_CHROME_REQUEST);
        Map<String, String> expectedHeaders = new HashMap<>();
        expectedHeaders.put("Content-Type", "application/json");
        expectedHeaders.put("Host", "localhost");
        expectedHeaders.put("cache-control", "no-cache");
        expectedHeaders.put("Postman-Token", "96621bec-a11a-4c9c-a7ff-6a87c9369e02");
        HttpRequest httpRequest = HttpRequest.parseFrom(this.bufferedReader);

        assertThat(httpRequest.getMethod(), is(HttpMethod.GET));
    }


    @Test
    public void expecToParseHttpRequestWithJqueryContent() throws IOException {
        this.prepareBufferedReader(TEST_JQUERY_AJAX_CALL);

        HttpRequest httpRequest = HttpRequest.parseFrom(this.bufferedReader);


        assertThat(httpRequest.getMethod(), is(HttpMethod.POST));
    }

    @Test
    public void expectToIdentifyGetMethod() {
        HttpMethod enumEntry = HttpRequest.identifyHTTPMethod("Get");

        Assert.assertThat(enumEntry, is(HttpMethod.GET));
    }

    @Test
    public void expectToIdentifyHeadMethod() {
        HttpMethod enumEntry = HttpRequest.identifyHTTPMethod("Head");

        Assert.assertThat(enumEntry, is(HttpMethod.HEAD));
    }

    @Test
    public void expectToReturnNull() {
        HttpMethod enumEntry = HttpRequest.identifyHTTPMethod("Put");

        Assert.assertThat(enumEntry, is(HttpMethod.NOT_IMPLEMENTED_YET));
    }

    @Test
    public void expectToReturnCacheInformation() throws ParseException {
        String dateString = "Wed, 21 Oct 2015 07:28:00 GMT";
        Map<String, String> headers = new HashMap<>();
        headers.put("If-Modified-Since", dateString);
        headers.put("If-Match", "\"33a64df551425fcc55e4d42a148795d9f25f89d4\"");
        headers.put("If-None-Match", "\"33a64df551425fccasdasd55e4d42a148795d9f25f89d4\"");
        SimpleDateFormat format = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz");
        Date expectedDate = format.parse(dateString);

        HttpRequest.CacheHeaders expectedCacheHeaders = HttpRequest.CacheHeaders.builder()
                .ifMatch("33a64df551425fcc55e4d42a148795d9f25f89d4")
                .ifNonMatch("33a64df551425fccasdasd55e4d42a148795d9f25f89d4")
                .ifModifiedSince(expectedDate)
                .build();

        HttpRequest httpRequest = new HttpRequest("GET", "/", headers, "");


        assertThat(httpRequest.getCacheHeaders(), Matchers.samePropertyValuesAs(expectedCacheHeaders));
    }

    @Test
    public void expectToReturnNoCacheInformation() {
        String dateString = "Wed, 21 Oct 2015 07:28:00 GMT";
        Map<String, String> headers = new HashMap<>();
        HttpRequest httpRequest = new HttpRequest("GET", "/", headers, "");


        assertNull(httpRequest.getCacheHeaders());
    }


}