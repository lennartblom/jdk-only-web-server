package de.blom.httpwebserver.adapters.http;

import de.blom.httpwebserver.common.HTTPResponseOutput;
import org.apache.commons.httpclient.HttpStatus;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class JavaHTTPServerTest {

    @InjectMocks
    private JavaHTTPServer javaHTTPServer;

    @Mock
    private BufferedReader in;

    @Mock
    private PrintWriter out;

    @Mock
    private HTTPResponseOutput httpResponseOutput;

    @Mock
    private BufferedOutputStream dataOut;

    @Mock
    private Socket mockedSocket;

    private static final String DUMMY_FILE_REQUESTED = "index.html";

    private static final String GET = "GET";
    private static final String PUT = "PUT";

    @Before
    public void setup(){
        this.javaHTTPServer = new JavaHTTPServer(this.mockedSocket, this.httpResponseOutput);
    }

    @Test
    public void expectToCloseElementsProperly() throws IOException {
        this.javaHTTPServer.closeElements(this.in, this.out, this.dataOut);

        verify(this.mockedSocket).close();
        verify(this.in).close();
        verify(this.out).close();
        verify(this.dataOut).close();
    }

    @Test
    public void expectToWrite200WhenFileRequestIsHandled() throws IOException {
        this.javaHTTPServer.handleFileRequest(DUMMY_FILE_REQUESTED, GET, this.out, this.dataOut);

        verify(this.httpResponseOutput).writeResponseHeader(HttpStatus.SC_OK, this.out);
    }

    @Test
    public void expectToWrite501WhenUnsupportedMethodHandled() throws IOException {
        this.javaHTTPServer.handleMethodNotRequested(PUT, this.out, this.dataOut);

        verify(this.httpResponseOutput).writeResponseHeader(HttpStatus.SC_NOT_IMPLEMENTED, this.out);
        verify(this.httpResponseOutput).writeResponseContentInformation(eq("text/html"), any(int.class), eq(this.out));
    }

    @Test
    public void expectToWrite404WhenNotFoundIsHandled() throws IOException {
        this.javaHTTPServer.handleFileNotFound(this.out, this.dataOut, DUMMY_FILE_REQUESTED);

        verify(this.httpResponseOutput).writeResponseHeader(HttpStatus.SC_NOT_FOUND, this.out);
        verify(this.httpResponseOutput).writeResponseContentInformation(eq("text/html"), any(int.class), eq(this.out));
    }

}