package de.blom.httpwebserver.adapters.http;

import org.junit.Test;
import org.mockito.Mockito;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

import static org.junit.Assert.*;

public class JavaHTTPServerTest {

    private JavaHTTPServer javaHTTPServer;

    @Test
    public void expectToCloseElementsProperly() throws IOException {
        Socket mockedSocket = Mockito.mock(Socket.class);
        this.javaHTTPServer = new JavaHTTPServer(mockedSocket);

        BufferedReader in = Mockito.mock(BufferedReader.class);
        PrintWriter out = Mockito.mock(PrintWriter.class);
        BufferedOutputStream dataOut = Mockito.mock(BufferedOutputStream.class);

        this.javaHTTPServer.closeElements(in, out, dataOut);

        Mockito.verify(mockedSocket).close();
        Mockito.verify(in).close();
        Mockito.verify(out).close();
        Mockito.verify(dataOut).close();
    }

}