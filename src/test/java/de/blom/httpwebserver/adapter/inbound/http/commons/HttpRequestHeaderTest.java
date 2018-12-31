package de.blom.httpwebserver.adapter.inbound.http.commons;

import org.hamcrest.Matchers;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertNull;

public class HttpRequestHeaderTest {
    private static final String TEST_CONTENT_TYPE_HEADER = "Content-Type: application/json";
    private static final String TEST_INVALID_HEADER_FORMAT_NO_COLON = "Invalid application/json";
    private static final String TEST_MODIFIED_SINCE = "If-Modified-Since: Sat, 29 Oct 1994 19:43:31 GMT";

    @Test
    public void expectToCreateHttpHeaderElement(){

        HttpRequest.Header expectedHeader = new HttpRequest.Header("Content-Type", "application/json");

        HttpRequest.Header returnedHeader = HttpRequest.parseHttpHeaderFromLine(TEST_CONTENT_TYPE_HEADER);
        assertThat(returnedHeader, Matchers.samePropertyValuesAs(expectedHeader));
    }


    @Test
    public void expectToReturnNullWithInvalidHeader_noColon() {
        HttpRequest.Header returnedHeader = HttpRequest.parseHttpHeaderFromLine(TEST_INVALID_HEADER_FORMAT_NO_COLON);
        assertNull(returnedHeader);
    }

    @Test
    public void expectToReturnNullWithInvalidHeader_twoSpaces() {
        HttpRequest.Header expectedHeader = new HttpRequest.Header("If-Modified-Since", "Sat, 29 Oct 1994 19:43:31 GMT");
        HttpRequest.Header returnedHeader = HttpRequest.parseHttpHeaderFromLine(TEST_MODIFIED_SINCE);
        assertThat(returnedHeader, Matchers.samePropertyValuesAs(expectedHeader));
    }

    @Test
    public void expectToReturnNullWithInvalidHeader_nullString() {
        HttpRequest.Header returnedHeader = HttpRequest.parseHttpHeaderFromLine(null);
        assertNull(returnedHeader);
    }

}