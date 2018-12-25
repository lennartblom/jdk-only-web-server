package de.blom.httpwebserver.adapter.inbound.http;

import de.blom.httpwebserver.adapter.inbound.http.util.ResponseWriter;
import de.blom.httpwebserver.domain.fileserver.DirectoryRequestDto;
import de.blom.httpwebserver.enums.HTTPMethod;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class HTTPAdapterTest {


    @InjectMocks
    private HTTPAdapter httpAdapter;

    @Mock
    private BufferedReader in;

    @Mock
    private PrintWriter out;

    @Mock
    private ResponseWriter responseWriter;

    @Mock
    private BufferedOutputStream dataOut;

    @Mock
    private Socket mockedSocket;

    private static final String DUMMY_FILE_REQUESTED = "index.html";

    private static final String GET = "GET";
    private static final String PUT = "PUT";

    @Before
    public void setup() {
        this.httpAdapter = new HTTPAdapter(this.mockedSocket, this.responseWriter);
    }

    @Test
    public void expectToCloseElementsProperly() throws IOException {
        this.httpAdapter.closeElements(this.in, this.out, this.dataOut);

        verify(this.mockedSocket).close();
        verify(this.in).close();
        verify(this.out).close();
        verify(this.dataOut).close();
    }

    @Test(expected = IOException.class)
    public void expectToHandleNullpointer() throws IOException {
        when(this.in.readLine()).thenReturn(null);

        this.httpAdapter.parseInput(this.in);
    }

    @Test
    public void expectToIdentifyGetMethod(){
        HTTPMethod enumEntry = this.httpAdapter.identifyHTTPMethod("Get");

        assertThat(enumEntry, is(HTTPMethod.GET));
    }

    @Test
    public void expectToIdentifyHeadMethod(){
        HTTPMethod enumEntry = this.httpAdapter.identifyHTTPMethod("Head");

        assertThat(enumEntry, is(HTTPMethod.HEAD));
    }

    @Test
    public void expectToReturnNull(){
        HTTPMethod enumEntry = this.httpAdapter.identifyHTTPMethod("Put");

        assertNull(enumEntry);
    }

    @Test
    public void expectToPassDtoToHttpResponseWriter() throws IOException {
        DirectoryRequestDto directoryRequestDto = Mockito.mock(DirectoryRequestDto.class);

        this.httpAdapter.handleDirectoryRequest(directoryRequestDto, this.out, this.dataOut);

        verify(this.responseWriter).writeHttpResponse(directoryRequestDto, this.out, this.dataOut);


    }

}