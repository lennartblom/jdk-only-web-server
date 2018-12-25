package de.blom.httpwebserver.adapter.inbound.http;

import de.blom.httpwebserver.adapter.inbound.http.util.ResponseWriter;
import de.blom.httpwebserver.enums.HTTPMethod;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.*;
import java.net.Socket;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@RunWith(MockitoJUnitRunner.class)
public class HttpServerProcessTest {


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

    @Mock
    private InputStream mockedInputStream;

    @Mock
    private OutputStream mockedOutputStream;

    @Before
    public void setup() throws IOException {
        when(this.mockedSocket.getInputStream()).thenReturn(this.mockedInputStream);
        when(this.mockedSocket.getOutputStream()).thenReturn(this.mockedOutputStream);
        this.httpServer = new HttpServer(this.mockedSocket, this.responseWriter);
        this.httpServer = Mockito.spy(this.httpServer);
    }

    @Test
    @Ignore
    public void expectToHandleHttpMethod() throws IOException {
        Mockito.doReturn(HTTPMethod.POST).when(this.httpServer).identifyHTTPMethod(any(String.class));

        this.httpServer.run();

        verify(this.httpServer).handleHttpMethod(any(PrintWriter.class), any(BufferedOutputStream.class), any(String.class), eq(HTTPMethod.POST));
    }

}