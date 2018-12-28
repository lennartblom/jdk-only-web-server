package de.blom.httpwebserver.adapter.inbound.http.commons;

import de.blom.httpwebserver.enums.HttpMethod;
import org.hamcrest.beans.SamePropertyValuesAs;
import org.junit.Assert;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

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

    private static final String TEST_CHROM_REQUEST = "GET / HTTP/1.1\n" +
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
    public void expectToSetHeadersFromChromGetRequest() throws IOException {

        this.prepareBufferedReader(TEST_CHROM_REQUEST);
        Map<String, String> expectedHeaders = new HashMap<>();
        expectedHeaders.put("Content-Type", "application/json");
        expectedHeaders.put("Host", "localhost");
        expectedHeaders.put("cache-control", "no-cache");
        expectedHeaders.put("Postman-Token", "96621bec-a11a-4c9c-a7ff-6a87c9369e02");
        HttpRequest httpRequest = HttpRequest.parseFrom(this.bufferedReader);

        assertThat(httpRequest.getMethod(), is(HttpMethod.GET));
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


}