package de.blom.httpwebserver.adapter.inbound.http;

import de.blom.httpwebserver.adapter.inbound.http.commons.HttpRequest;
import de.blom.httpwebserver.adapter.inbound.http.commons.ResponseWriter;
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

@RunWith(MockitoJUnitRunner.class)
public class HttpAdapterTest {


    private static final String INDEX_HTML = "/index.html";
    private static final String COMMENTS_URI = "/comments";
    private static final String ROOT_DIRECTORY = "/";

    private HttpAdapter httpAdapter;

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
        this.httpAdapter = new HttpAdapter(this.mockedSocket, this.responseWriter);
        this.httpAdapter = Mockito.spy(this.httpAdapter);
    }


    @Test
    public void expectToCallHandleGetMethod() throws IOException {
        HttpRequest incomingRequest = new HttpRequest("GET", INDEX_HTML, null, null);
        this.httpAdapter.handleHttpMethod(this.httpResponseHeader, this.httpResponseBody, incomingRequest);

        verify(this.httpAdapter).handleGetRequest(INDEX_HTML, this.httpResponseHeader, this.httpResponseBody);
    }

    @Test
    public void expectToCallHandlePostMethod() throws IOException {
        HttpRequest incomingRequest = new HttpRequest("POST", COMMENTS_URI, null, null);
        this.httpAdapter.handleHttpMethod(this.httpResponseHeader, this.httpResponseBody, incomingRequest);

        verify(this.httpAdapter).handlePostRequest(COMMENTS_URI);
    }

    @Test
    public void expectHandleNotSupportedMethodWith501() throws IOException {
        HttpRequest incomingRequest = new HttpRequest("PUT", COMMENTS_URI, null, null);
        this.httpAdapter.handleHttpMethod(this.httpResponseHeader, this.httpResponseBody, incomingRequest);

        verify(this.responseWriter).respondeWith501(this.httpResponseHeader, this.httpResponseBody);
    }

    @Test
    public void expectToCallDirectoryHandling() throws IOException {
        this.httpAdapter.handleGetRequest(ROOT_DIRECTORY, this.httpResponseHeader, this.httpResponseBody);

        verify(this.httpAdapter).handleGetDirectory(ROOT_DIRECTORY, this.httpResponseHeader, this.httpResponseBody);
    }

    @Test
    public void expectToCallFileHandling() throws IOException {
        this.httpAdapter.handleGetRequest(INDEX_HTML, this.httpResponseHeader, this.httpResponseBody);

        verify(this.httpAdapter).handleGetFile(INDEX_HTML, this.httpResponseHeader, this.httpResponseBody);
    }


}