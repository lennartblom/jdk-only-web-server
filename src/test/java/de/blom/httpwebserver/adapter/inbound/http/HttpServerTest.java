package de.blom.httpwebserver.adapter.inbound.http;

import de.blom.httpwebserver.adapter.inbound.http.util.ResponseWriter;
import de.blom.httpwebserver.enums.HTTPMethod;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class HttpServerTest {


    private static final String INDEX_HTML = "/index.html";
    private static final String COMMENTS_URI = "/comments";
    private static final String ROOT_DIRECTORY = "/";

    private HttpServer httpServer;

    @Mock
    private BufferedReader in;

    @Mock
    private PrintWriter httpResponseHeader;

    @Mock
    private ResponseWriter responseWriter;

    @Mock
    private BufferedOutputStream httpResponseBody;

    @Mock
    private Socket mockedSocket;

    @Before
    public void setup() {
        this.httpServer = new HttpServer(this.mockedSocket, this.responseWriter);
        this.httpServer = Mockito.spy(this.httpServer);
    }

    @Test
    public void expectToIdentifyGetMethod() {
        HTTPMethod enumEntry = this.httpServer.identifyHTTPMethod("Get");

        assertThat(enumEntry, is(HTTPMethod.GET));
    }

    @Test
    public void expectToIdentifyHeadMethod() {
        HTTPMethod enumEntry = this.httpServer.identifyHTTPMethod("Head");

        assertThat(enumEntry, is(HTTPMethod.HEAD));
    }

    @Test
    public void expectToReturnNull() {
        HTTPMethod enumEntry = this.httpServer.identifyHTTPMethod("Put");

        assertThat(enumEntry, is(HTTPMethod.NOT_IMPLEMENTED_YET));
    }

    @Test
    public void expectToCallHandleGetMethod() throws IOException {
        this.httpServer.handleHttpMethod(this.httpResponseHeader, this.httpResponseBody, INDEX_HTML, HTTPMethod.GET);

        verify(this.httpServer).handleGetRequest(INDEX_HTML, this.httpResponseHeader, this.httpResponseBody);
    }

    @Test
    public void expectToCallHandlePostMethod() throws IOException {
        this.httpServer.handleHttpMethod(this.httpResponseHeader, this.httpResponseBody, COMMENTS_URI, HTTPMethod.POST);

        verify(this.httpServer).handlePostRequest(COMMENTS_URI);
    }

    @Test
    public void expectHandleNotSupportedMethodWith501() throws IOException {
        this.httpServer.handleHttpMethod(this.httpResponseHeader, this.httpResponseBody, COMMENTS_URI, HTTPMethod.NOT_IMPLEMENTED_YET);

        verify(this.responseWriter).respondeWith501(this.httpResponseHeader, this.httpResponseBody);
    }

    @Test
    public void expectToCallDirectoryHandling() throws IOException {
        this.httpServer.handleGetRequest(ROOT_DIRECTORY, this.httpResponseHeader, this.httpResponseBody);

        verify(this.httpServer).handleGetDirectory(ROOT_DIRECTORY, this.httpResponseHeader, this.httpResponseBody);
    }

    @Test
    public void expectToCallFileHandling() throws IOException {
        this.httpServer.handleGetRequest(INDEX_HTML, this.httpResponseHeader, this.httpResponseBody);

        verify(this.httpServer).handleGetFile(INDEX_HTML, this.httpResponseHeader, this.httpResponseBody);
    }


}