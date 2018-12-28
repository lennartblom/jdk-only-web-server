package de.blom.httpwebserver.adapter.inbound.http.commons;

import de.blom.httpwebserver.domain.fileserver.DirectoryRequestDto;
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
import java.util.Collections;
import java.util.Date;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ResponseWriterTest {


    private static final byte[] FILE_DATA = new byte[52];
    private static final int STATUS_CODE = HttpStatus.SC_OK;
    private static final String CONTENT_TYPE_HTML = "text/html";

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
    public void expectToOutputCorrectContentInformation() {
        this.responseWriter.writeResponseContentInformation(TEXT_HTML, FILE_LENGTH, this.out);
        verify(this.out).println("Content-type: " + TEXT_HTML);
        verify(this.out).println("Content-length: " + FILE_LENGTH);
    }

    @Test
    public void expectToReturnValidHttpResponse() throws IOException {
        this.responseWriter.writeHttpResponse(this.out, this.dataOut, FILE_LENGTH, CONTENT_TYPE_HTML, FILE_DATA, STATUS_CODE);

        verify(this.responseWriter).writeResponseHeader(STATUS_CODE, this.out);
        verify(this.responseWriter).writeResponseContentInformation(CONTENT_TYPE_HTML, FILE_LENGTH, this.out);

        verify(this.out).flush();

        verify(this.dataOut).write(FILE_DATA, 0, FILE_LENGTH);
        verify(this.dataOut).flush();
    }

    @Test
    public void expectToNotCallDataOut() throws IOException {
        this.responseWriter.writeHttpResponse(this.out, null, FILE_LENGTH, CONTENT_TYPE_HTML, FILE_DATA, STATUS_CODE);

        verify(this.responseWriter).writeResponseHeader(STATUS_CODE, this.out);
        verify(this.responseWriter).writeResponseContentInformation(CONTENT_TYPE_HTML, FILE_LENGTH, this.out);

        verify(this.out).flush();

        verify(this.dataOut, never()).write(FILE_DATA, 0, FILE_LENGTH);
        verify(this.dataOut, never()).flush();
    }

    @Test
    public void expectToWriteProperDirectoryHtmlList() throws IOException {
        DirectoryRequestDto directoryRequestDto = DirectoryRequestDto.builder()
                .files(Collections.singletonList("index.html"))
                .subdirectories(Collections.singletonList("subdirectory"))
                .build();

        String expectedHTML = "<ul><li>subdirectory/</li><li>index.html</li></ul>";

        this.responseWriter.writeHttpResponse(directoryRequestDto, this.out, this.dataOut);

        verify(this.responseWriter).writeHttpResponse(out, dataOut, expectedHTML.getBytes().length, CONTENT_TYPE_HTML, expectedHTML.getBytes(), HttpStatus.SC_OK);

    }


}