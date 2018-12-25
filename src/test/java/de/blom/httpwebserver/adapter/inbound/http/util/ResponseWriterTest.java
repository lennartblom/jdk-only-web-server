package de.blom.httpwebserver.adapter.inbound.http.util;

import org.apache.commons.httpclient.HttpStatus;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ResponseWriterTest {


    private static final byte[] FILE_DATA = new byte[52];
    private static final int STATUS_CODE = HttpStatus.SC_OK;
    private static final String MIME_TYPE = "text/html";

    private static final String SERVER_INFO = "Server: Java HTTP Server";
    private static final String TEXT_HTML = "text/html";
    private static final int FILE_LENGTH = 123;

    @Spy
    private ResponseWriter responseWriter;

    @Mock
    private PrintWriter out;

    @Mock
    private BufferedOutputStream dataOut;

    private Date currentDate = new Date();

    @Before
    public void setup() {
        when(this.responseWriter.getCurrentDate()).thenReturn(this.currentDate);
    }

    @Test
    public void expectToCall200Method() {
        this.responseWriter.writeResponseHeader(HttpStatus.SC_OK, this.out);

        verify(this.responseWriter).write200Response(this.out);
    }

    @Test
    public void expectToCall404Method() {
        this.responseWriter.writeResponseHeader(HttpStatus.SC_NOT_FOUND, this.out);

        verify(this.responseWriter).write404Response(this.out);
    }

    @Test
    public void expectToCall501Method() {
        this.responseWriter.writeResponseHeader(HttpStatus.SC_NOT_IMPLEMENTED, this.out);

        verify(this.responseWriter).write501Response(this.out);
    }

    @Test
    public void expectToOutputCorrect200Status() {
        this.responseWriter.write200Response(out);

        verify(this.out).println(eq("HTTP/1.1 200 OK"));
        verify(this.out).println(eq(SERVER_INFO));
        verify(this.out).println(eq("Date: " + this.currentDate));
    }

    @Test
    public void expectToOutputCorrect501Status() {
        this.responseWriter.write501Response(out);

        verify(this.out).println(eq("HTTP/1.1 501 Not Implemented"));
        verify(this.out).println(eq(SERVER_INFO));
        verify(this.out).println(eq("Date: " + this.currentDate));
    }

    @Test
    public void expectToOutputCorrect404Status() {
        this.responseWriter.write404Response(out);

        verify(this.out).println(eq("HTTP/1.1 404 File Not Found"));
        verify(this.out).println(eq(SERVER_INFO));
        verify(this.out).println(eq("Date: " + this.currentDate));
    }

    @Test
    public void expectToOutputCorrectContentInformation() {
        this.responseWriter.writeResponseContentInformation(TEXT_HTML, FILE_LENGTH, this.out);
        verify(this.out).println("Content-type: " + TEXT_HTML);
        verify(this.out).println("Content-length: " + FILE_LENGTH);
    }

    @Test
    public void expectToReturnValidHttpResponse() throws IOException {
        this.responseWriter.writeHttpResponse(this.out, this.dataOut, FILE_LENGTH, MIME_TYPE, FILE_DATA, STATUS_CODE);

        verify(this.responseWriter).writeResponseHeader(STATUS_CODE, this.out);
        verify(this.responseWriter).writeResponseContentInformation(MIME_TYPE, FILE_LENGTH, this.out);

        verify(this.out).flush();

        verify(this.dataOut).write(FILE_DATA, 0, FILE_LENGTH);
        verify(this.dataOut).flush();
    }


}