package de.blom.httpserver.adapter.inbound.http;

import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.blom.httpserver.adapter.inbound.http.commons.HttpRequest;
import de.blom.httpserver.adapter.inbound.http.commons.ResponseWriter;
import de.blom.httpserver.crosscutting.enums.HttpMethod;
import de.blom.httpserver.crosscutting.exception.DataNotModifiedException;
import de.blom.httpserver.crosscutting.exception.ETagException;
import de.blom.httpserver.crosscutting.exception.InvalidDataException;
import de.blom.httpserver.crosscutting.exception.NotFoundException;
import de.blom.httpserver.crosscutting.exception.WrongContentTypeException;
import de.blom.httpserver.crosscutting.representation.fileserver.CacheableData;
import de.blom.httpserver.crosscutting.representation.fileserver.DirectoryRequestDto;
import de.blom.httpserver.crosscutting.representation.fileserver.FileRequestDto;
import de.blom.httpserver.crosscutting.representation.wall.WallEntryInboundDto;
import de.blom.httpserver.crosscutting.representation.wall.WallEntryOutboundDto;
import de.blom.httpwebserver.fileserver.DirectoryService;
import de.blom.httpwebserver.fileserver.wall.WallContentService;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class HttpAdapterTest {

  private static final String INDEX_HTML = "/index.html";
  private static final String WALL_ENTRIES_URI = "/wall_entries";
  private static final String ROOT_DIRECTORY = "/";
  private static final String WALL_ENTRIES_QUERY = "/wall_entries/query";

  @Spy
  @InjectMocks
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

  @Mock
  private DirectoryService directoryService;

  @Mock
  private DirectoryRequestDto mockedDirectoryRequestDtoNotFound;

  @Mock
  private DirectoryRequestDto mockedDirectoryRequestDtoFound;

  @Mock
  private FileRequestDto mockedFileRequestDtoNotFound;

  @Mock
  private FileRequestDto mockedFileRequestDtoFound;

  @Mock
  private WallContentService wallContentService;

  @Mock
  private HttpRequest httpRequest;

  @Mock
  private HttpRequest.CacheHeaders cacheHeaders;

  @Before
  public void setup() {

    this.httpAdapter =
        new HttpAdapter(this.mockedSocket, this.responseWriter, this.directoryService,
            this.wallContentService);
    this.httpAdapter = Mockito.spy(this.httpAdapter);

    when(mockedDirectoryRequestDtoNotFound.getFound()).thenReturn(false);

    when(mockedDirectoryRequestDtoFound.getFound()).thenReturn(true);

    when(mockedFileRequestDtoFound.getFound()).thenReturn(false);

    when(mockedFileRequestDtoFound.getFound()).thenReturn(true);
  }

  @Test
  public void expectToCallHandleGetMethod() throws IOException {
    HttpRequest incomingRequest = new HttpRequest("GET", INDEX_HTML, null, null);
    doNothing().when(this.httpAdapter)
        .handleDirectoryServerRequest(incomingRequest, this.httpResponseHeader,
            this.httpResponseBody);

    this.httpAdapter
        .processRequest(this.httpResponseHeader, this.httpResponseBody, incomingRequest);

    verify(this.httpAdapter).handleDirectoryServerRequest(incomingRequest, this.httpResponseHeader,
        this.httpResponseBody);
  }

  @Test
  public void expectToCallHandlePostMethod() throws IOException {
    HttpRequest incomingRequest = new HttpRequest("POST", WALL_ENTRIES_URI, null, null);
    doNothing().when(this.httpAdapter)
        .handlePostRequest(incomingRequest, this.httpResponseHeader, this.httpResponseBody);

    this.httpAdapter
        .processRequest(this.httpResponseHeader, this.httpResponseBody, incomingRequest);

    verify(this.httpAdapter)
        .handlePostRequest(incomingRequest, this.httpResponseHeader, this.httpResponseBody);
  }

  @Test
  public void expectHandleNotSupportedMethodWith501() throws IOException {
    HttpRequest incomingRequest = new HttpRequest("PUT", WALL_ENTRIES_URI, null, null);
    this.httpAdapter
        .processRequest(this.httpResponseHeader, this.httpResponseBody, incomingRequest);

    verify(this.responseWriter).respondeWith501(this.httpResponseHeader, this.httpResponseBody);
  }

  @Test
  public void expectToCallDirectoryHandling() throws IOException {
    HttpRequest incomingRequest = new HttpRequest("GET", ROOT_DIRECTORY, null, null);
    when(this.directoryService.handleDirectoryRequest(ROOT_DIRECTORY))
        .thenReturn(this.mockedDirectoryRequestDtoFound);

    this.httpAdapter.handleDirectoryServerRequest(incomingRequest, this.httpResponseHeader,
        this.httpResponseBody);

    verify(this.httpAdapter)
        .handleDirectoryRequest(incomingRequest, this.httpResponseHeader, this.httpResponseBody);
  }

  @Test
  public void expectToCallFileHandling() throws IOException {
    HttpRequest incomingRequest = new HttpRequest("GET", INDEX_HTML, null, null);
    when(this.directoryService.handleFileRequest(INDEX_HTML)).thenReturn(mockedFileRequestDtoFound);

    this.httpAdapter.handleDirectoryServerRequest(incomingRequest, this.httpResponseHeader,
        this.httpResponseBody);

    verify(this.httpAdapter)
        .handleFileRequest(incomingRequest, this.httpResponseHeader, this.httpResponseBody);
  }

  @Test
  public void expectToCallFileHandlingNotFoundWithoutHttpResponseBody() throws IOException {
    HttpRequest incomingRequest = new HttpRequest("HEAD", INDEX_HTML, null, null);
    when(this.directoryService.handleFileRequest(INDEX_HTML))
        .thenReturn(this.mockedFileRequestDtoNotFound);

    this.httpAdapter
        .handleFileRequest(incomingRequest, this.httpResponseHeader, this.httpResponseBody);

    verify(this.responseWriter).respondeWith404(this.httpResponseHeader, null);
  }

  @Test
  public void expectToCallFileHandlingWithoutHttpResponseBody() throws IOException {
    when(this.directoryService.handleFileRequest(INDEX_HTML))
        .thenReturn(this.mockedFileRequestDtoFound);
    HttpRequest incomingRequest = new HttpRequest("HEAD", INDEX_HTML, null, null);


    this.httpAdapter
        .handleFileRequest(incomingRequest, this.httpResponseHeader, this.httpResponseBody);

    verify(this.responseWriter)
        .writeHttpResponseWithFileData(this.mockedFileRequestDtoFound, this.httpResponseHeader,
            null);
  }

  @Test
  public void expectToCallDirectoryHandling404WithoutHttpResponseBody() throws IOException {
    HttpRequest incomingRequest = new HttpRequest("HEAD", ROOT_DIRECTORY, null, null);
    when(this.directoryService.handleDirectoryRequest(ROOT_DIRECTORY))
        .thenReturn(this.mockedDirectoryRequestDtoNotFound);

    this.httpAdapter
        .handleDirectoryRequest(incomingRequest, this.httpResponseHeader, this.httpResponseBody);

    verify(this.responseWriter).respondeWith404(this.httpResponseHeader, null);
  }

  @Test
  public void expectToCallDirectoryHandlingWithoutHttpResponseBody() throws IOException {
    HttpRequest incomingRequest = new HttpRequest("HEAD", ROOT_DIRECTORY, null, null);
    when(this.directoryService.handleDirectoryRequest(ROOT_DIRECTORY))
        .thenReturn(this.mockedDirectoryRequestDtoFound);

    this.httpAdapter
        .handleDirectoryRequest(incomingRequest, this.httpResponseHeader, this.httpResponseBody);

    verify(this.responseWriter)
        .writeHttpResponseWithDirectoryData(this.mockedDirectoryRequestDtoFound,
            this.httpResponseHeader, null);
  }

  @Test
  public void expectToCallDirectoryServiceWithSuitableDto() throws IOException {
    String rawBody = "{\n" +
        "\t\"author\": \"Max Mustermann\",\n" +
        "\t\"text\": \"Lorem ipsum dolor\"\n" +
        "}";

    Map<String, String> correctHeader = new HashMap<>();
    correctHeader.put("Content-Type", "application/json");

    WallEntryInboundDto expectedIncomingDto =
        new WallEntryInboundDto("Max Mustermann", "Lorem ipsum dolor");
    HttpRequest incomingRequest = new HttpRequest("POST", WALL_ENTRIES_URI, correctHeader, rawBody);

    this.httpAdapter
        .handlePostRequest(incomingRequest, this.httpResponseHeader, this.httpResponseBody);

    ArgumentCaptor<WallEntryInboundDto> varArgs =
        ArgumentCaptor.forClass(WallEntryInboundDto.class);
    verify(this.wallContentService).createNewEntry(varArgs.capture());

    assertThat(varArgs.getValue(), Matchers.samePropertyValuesAs(expectedIncomingDto));
    verify(this.responseWriter).respondeWith201(this.httpResponseHeader, this.httpResponseBody);
  }

  @Test(expected = WrongContentTypeException.class)
  public void expectToThrowBadRequestException_contentTypeMissing() throws IOException {
    Map<String, String> header = new HashMap<>();

    HttpRequest incomingRequest = new HttpRequest("POST", WALL_ENTRIES_URI, header, "");

    this.httpAdapter
        .handlePostRequest(incomingRequest, this.httpResponseHeader, this.httpResponseBody);
  }

  @Test(expected = InvalidDataException.class)
  public void expectToThrowBadRequestException_jsonContentIsWrong() throws IOException {
    String rawBody = "{\n" +
        "\t\"invalid\": \"Max Mustermann\",\n" +
        "\t\"invalid_2\": \"Lorem ipsum dolor\"\n" +
        "}";

    Map<String, String> correctHeader = new HashMap<>();
    correctHeader.put("Content-Type", "application/json");

    HttpRequest incomingRequest = new HttpRequest("POST", WALL_ENTRIES_URI, correctHeader, rawBody);

    this.httpAdapter
        .handlePostRequest(incomingRequest, this.httpResponseHeader, this.httpResponseBody);
  }

  @Test
  public void expectToWrite400ResponseForInvalidDataException() throws IOException {
    when(this.httpRequest.getMethod()).thenReturn(HttpMethod.POST);
    doThrow(new InvalidDataException())
        .when(this.httpAdapter)
        .handlePostRequest(this.httpRequest, this.httpResponseHeader, this.httpResponseBody);

    this.httpAdapter
        .processRequest(this.httpResponseHeader, this.httpResponseBody, this.httpRequest);

    verify(this.responseWriter).respondeWith400(this.httpResponseHeader, this.httpResponseBody);
  }

  @Test
  public void expectToWrite400ResponseForWrongContentTypeException() throws IOException {
    when(this.httpRequest.getMethod()).thenReturn(HttpMethod.POST);
    doThrow(new WrongContentTypeException())
        .when(this.httpAdapter)
        .handlePostRequest(this.httpRequest, this.httpResponseHeader, this.httpResponseBody);

    this.httpAdapter
        .processRequest(this.httpResponseHeader, this.httpResponseBody, this.httpRequest);

    verify(this.responseWriter).respondeWith400(this.httpResponseHeader, this.httpResponseBody);
  }

  @Test
  public void expectToWrite404ResponseForNotFoundException() throws IOException {
    when(this.httpRequest.getMethod()).thenReturn(HttpMethod.POST);
    doThrow(new NotFoundException())
        .when(this.httpAdapter)
        .handlePostRequest(this.httpRequest, this.httpResponseHeader, this.httpResponseBody);

    this.httpAdapter
        .processRequest(this.httpResponseHeader, this.httpResponseBody, this.httpRequest);

    verify(this.responseWriter).respondeWith404(this.httpResponseHeader, this.httpResponseBody);
  }

  @Test
  @Ignore
  public void expectToWrite503ResponseForTimeout() throws IOException {
    when(this.httpRequest.getMethod()).thenReturn(HttpMethod.POST);

    this.httpAdapter
        .processRequest(this.httpResponseHeader, this.httpResponseBody, this.httpRequest);

    verify(this.responseWriter).respondeWith503(this.httpResponseHeader, this.httpResponseBody);
  }

  @Test(expected = NotFoundException.class)
  public void expectToThrowNotFoundExceptionWrongPath() throws IOException {
    when(this.httpRequest.getUri()).thenReturn("/unknown");

    this.httpAdapter
        .handlePostRequest(this.httpRequest, this.httpResponseHeader, this.httpResponseBody);
  }

  @Test
  public void expectToCallService() throws IOException {
    when(this.httpRequest.getUri()).thenReturn(WALL_ENTRIES_QUERY);
    List<WallEntryOutboundDto> mockedElements =
        Arrays.asList(mock(WallEntryOutboundDto.class), mock(WallEntryOutboundDto.class));
    when(this.wallContentService.getAllEntries()).thenReturn(mockedElements);

    this.httpAdapter
        .handlePostRequest(this.httpRequest, this.httpResponseHeader, this.httpResponseBody);

    verify(this.responseWriter)
        .writeHttpResponse(mockedElements, this.httpResponseHeader, this.httpResponseBody);
    verify(this.wallContentService).getAllEntries();
  }

  @Test
  public void expectToRespondeWith304_dataModified() throws IOException {
    when(this.httpRequest.getMethod()).thenReturn(HttpMethod.GET);
    doThrow(new DataNotModifiedException())
        .when(this.httpAdapter)
        .handleDirectoryServerRequest(this.httpRequest, this.httpResponseHeader,
            this.httpResponseBody);

    this.httpAdapter
        .processRequest(this.httpResponseHeader, this.httpResponseBody, this.httpRequest);

    verify(this.responseWriter).respondeWith304(this.httpResponseHeader);
  }

  @Test
  public void expectToRespondeWith304_ETag() throws IOException {
    when(this.httpRequest.getMethod()).thenReturn(HttpMethod.GET);
    doThrow(new ETagException())
        .when(this.httpAdapter)
        .handleDirectoryServerRequest(this.httpRequest, this.httpResponseHeader,
            this.httpResponseBody);

    this.httpAdapter
        .processRequest(this.httpResponseHeader, this.httpResponseBody, this.httpRequest);

    verify(this.responseWriter).respondeWith304(this.httpResponseHeader);
  }

}
