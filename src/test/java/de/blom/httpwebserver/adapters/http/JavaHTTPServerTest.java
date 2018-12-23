package de.blom.httpwebserver.adapters.http;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

import static org.junit.Assert.*;

public class JavaHTTPServerTest {

    private JavaHTTPServer javaHTTPServer;

    @Mock
    private BufferedReader in;

    @Mock
    private PrintWriter out;

    @Mock
    private BufferedOutputStream dataOut;

    @Mock
    private Socket mockedSocket;

    private static final String DUMMY_FILE_REQUESTED = "index.html";

    private static final String GET = "GET";

    @Before
    public void setup(){
        this.javaHTTPServer = new JavaHTTPServer(this.mockedSocket);
    }

    @Test
    public void expectToCloseElementsProperly() throws IOException {


        this.javaHTTPServer.closeElements(in, out, dataOut);

        Mockito.verify(this.mockedSocket).close();
        Mockito.verify(this.in).close();
        Mockito.verify(this.out).close();
        Mockito.verify(this.dataOut).close();
    }

    @Test
    public void expectToWrite200WhenFileRequestIsHandled() throws IOException {
        this.javaHTTPServer.handleFileRequest(DUMMY_FILE_REQUESTED, GET, this.out, this.dataOut);


    }

}