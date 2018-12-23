package de.blom.httpwebserver.common;

import org.apache.commons.httpclient.HttpStatus;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.PrintWriter;
import java.util.Date;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class HTTPResponseOutputTest {

    public static final String SERVER_INFO = "Server: Java HTTP Server";
    @Spy
    private HTTPResponseOutput httpResponseOutput;

    @Mock
    private PrintWriter out;

    private Date currentDate = new Date();

    @Before
    public void setup(){
        when(this.httpResponseOutput.getCurrentDate()).thenReturn(this.currentDate);
    }

    @Test
    public void expectToCall200Method() {
        this.httpResponseOutput.writeResponseHeader(HttpStatus.SC_OK, this.out);

        verify(this.httpResponseOutput).write200Response(this.out);
    }

    @Test
    public void expectToCall404Method() {
        this.httpResponseOutput.writeResponseHeader(HttpStatus.SC_NOT_FOUND, this.out);

        verify(this.httpResponseOutput).write404Response(this.out);
    }

    @Test
    public void expectToCall501Method() {
        this.httpResponseOutput.writeResponseHeader(HttpStatus.SC_NOT_IMPLEMENTED, this.out);

        verify(this.httpResponseOutput).write501Response(this.out);
    }

    @Test
    public void expectToOutputCorrect200Status() {
        this.httpResponseOutput.write200Response(out);

        verify(this.out).println(eq("HTTP/1.1 200 OK"));
        verify(this.out).println(eq(SERVER_INFO));
        verify(this.out).println(eq("Date: " + this.currentDate));
    }

    @Test
    public void expectToOutputCorrect501Status() {
        this.httpResponseOutput.write501Response(out);

        verify(this.out).println(eq("HTTP/1.1 501 Not Implemented"));
        verify(this.out).println(eq(SERVER_INFO));
        verify(this.out).println(eq("Date: " + this.currentDate));
    }

    @Test
    public void expectToOutputCorrect404Status() {
        this.httpResponseOutput.write404Response(out);

        verify(this.out).println(eq("HTTP/1.1 404 File Not Found"));
        verify(this.out).println(eq(SERVER_INFO));
        verify(this.out).println(eq("Date: " + this.currentDate));
    }


}