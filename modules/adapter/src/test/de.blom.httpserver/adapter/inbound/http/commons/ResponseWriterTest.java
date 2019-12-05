package de.blom.httpserver.adapter.inbound.http.commons;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import com.google.gson.Gson;
import de.blom.httpserver.crosscutting.representation.fileserver.DirectoryRequestDto;
import de.blom.httpserver.crosscutting.representation.fileserver.FileRequestDto;
import de.blom.httpserver.crosscutting.representation.wall.WallEntryOutboundDto;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import org.joda.time.DateTimeUtils;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ResponseWriterTest {

  private static final byte[] FILE_DATA = new byte[52];
  private static final int STATUS_CODE = 200;
  private static final String CONTENT_TYPE_HTML = "text/html";

  private static final String CONTENT_TYPE_JSON = "application/json";

  private static final String SERVER_INFO = "Server: Java HTTP Server";
  private static final String TEXT_HTML = "text/html";
  private static final int FILE_LENGTH = 123;
  private static final String DUMMY_ETAG = "akd2l13k12l3";

  @Spy
  private ResponseWriter responseWriter;

  @Mock
  private PrintWriter out;

  @Mock
  private BufferedOutputStream dataOut;

  private Date currentDate = new Date(System.currentTimeMillis());

  @Before
  public void setup() {
    DateTimeUtils.setCurrentMillisFixed(System.currentTimeMillis());
  }

  @Test
  public void expectToCall200Method() {
    this.responseWriter.writeResponseHeader(200, this.out);

    verify(this.out).println(eq("HTTP/1.1 200 OK"));
    verify(this.out).println(eq(SERVER_INFO));
    verify(this.out).println(eq("Date: " + this.currentDate));
  }

  @Test
  public void expectToCall501Method() {
    this.responseWriter.writeResponseHeader(501, this.out);

    verify(this.out).println(eq("HTTP/1.1 501 Not Implemented"));
    verify(this.out).println(eq(SERVER_INFO));
    verify(this.out).println(eq("Date: " + this.currentDate));
  }

  @Test
  public void expectToOutputCorrect400Status() {
    this.responseWriter.writeResponseHeader(400, out);

    verify(this.out).println(eq("HTTP/1.1 400 Bad Request"));
    verify(this.out).println(eq(SERVER_INFO));
    verify(this.out).println(eq("Date: " + this.currentDate));
  }

  @Test
  public void expectToOutputCorrect304tatus() {
    this.responseWriter.writeResponseHeader(304, out);

    verify(this.out).println(eq("HTTP/1.1 304 Not Modified"));
    verify(this.out).println(eq(SERVER_INFO));
    verify(this.out).println(eq("Date: " + this.currentDate));
  }

  @Test
  public void expectToOutputCorrect503Status() {
    this.responseWriter.writeResponseHeader(503, out);

    verify(this.out).println(eq("HTTP/1.1 503 Service Unavailable"));
    verify(this.out).println(eq(SERVER_INFO));
    verify(this.out).println(eq("Date: " + this.currentDate));
  }

  @Test
  public void expectToOutputCorrect201Status() {
    this.responseWriter.writeResponseHeader(201, out);

    verify(this.out).println(eq("HTTP/1.1 201 Created"));
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
  @Ignore
  public void expectToReturnValidHttpResponse() throws IOException {
    this.responseWriter
        .writeHttpResponse(this.out, this.dataOut, FILE_LENGTH, CONTENT_TYPE_HTML, FILE_DATA,
            STATUS_CODE);

    verify(this.responseWriter).writeResponseHeader(STATUS_CODE, this.out);
    verify(this.responseWriter)
        .writeResponseContentInformation(CONTENT_TYPE_HTML, FILE_LENGTH, this.out);

    verify(this.out).flush();

    verify(this.dataOut).write(FILE_DATA, 0, FILE_LENGTH);
    verify(this.dataOut).flush();
  }

  @Test
  @Ignore
  public void expectToNotCallDataOut() throws IOException {
    this.responseWriter
        .writeHttpResponse(this.out, null, FILE_LENGTH, CONTENT_TYPE_HTML, FILE_DATA, STATUS_CODE);

    verify(this.responseWriter).writeResponseHeader(STATUS_CODE, this.out);
    verify(this.responseWriter)
        .writeResponseContentInformation(CONTENT_TYPE_HTML, FILE_LENGTH, this.out);

    verify(this.out).flush();

    verifyNoMoreInteractions(this.dataOut);
  }

  @Test
  public void expectToWriteProperDirectoryHtmlList() throws IOException {
    DirectoryRequestDto directoryRequestDto = DirectoryRequestDto.builder()
        .files(Collections.singletonList("index.html"))
        .subdirectories(Collections.singletonList("subdirectory"))
        .build();

    String expectedHTML = "<ul><li>subdirectory/</li><li>index.html</li></ul>";

    this.responseWriter
        .writeHttpResponseWithDirectoryData(directoryRequestDto, this.out, this.dataOut);

    verify(this.responseWriter)
        .writeHttpResponse(out, dataOut, expectedHTML.getBytes().length, CONTENT_TYPE_HTML, null,
            expectedHTML.getBytes(), 200);

  }

  @Test
  public void expectToWriterProper503Response() throws IOException {
    this.responseWriter.respondeWith503(out, dataOut);

    verify(this.responseWriter).writeHttpResponse(out, dataOut,
        ResponseWriter.ResponseHtmlData.SERVICE_NOT_AVAILABLE.getBytes().length, CONTENT_TYPE_HTML,
        ResponseWriter.ResponseHtmlData.SERVICE_NOT_AVAILABLE.getBytes(),
        503);
  }

  @Test
  public void expectToWriterProper400Response() throws IOException {
    this.responseWriter.respondeWith400(this.out, this.dataOut);

    verify(this.responseWriter).writeHttpResponse(out, dataOut,
        ResponseWriter.ResponseHtmlData.BAD_REQUEST.getBytes().length, CONTENT_TYPE_HTML,
        ResponseWriter.ResponseHtmlData.BAD_REQUEST.getBytes(), 400);
  }

  @Test
  public void expectToWriterProper204Response() throws IOException {
    this.responseWriter.respondeWith201(this.out, this.dataOut);

    verify(this.responseWriter).writeHttpResponse(out, dataOut,
        ResponseWriter.ResponseHtmlData.WALL_ENTRY_CREATED.getBytes().length, CONTENT_TYPE_HTML,
        ResponseWriter.ResponseHtmlData.WALL_ENTRY_CREATED.getBytes(), 201);
  }

  @Test
  public void expectToWriteProperJsonFromDtoList() throws IOException {
    Date testDate = new Date();

    WallEntryOutboundDto entry1 = WallEntryOutboundDto.builder()
        .author("Max")
        .text("Test 1")
        .created(testDate)
        .build();

    WallEntryOutboundDto entry2 = WallEntryOutboundDto.builder()
        .author("Mustermann")
        .text("Test 2")
        .created(testDate)
        .build();

    List<WallEntryOutboundDto> list = Arrays.asList(entry1, entry2);
    final String expectedJson = new Gson().toJson(list);

    this.responseWriter.writeHttpResponse(list, this.out, this.dataOut);

    verify(this.responseWriter)
        .writeHttpResponse(out, dataOut, expectedJson.getBytes().length, CONTENT_TYPE_JSON,
            expectedJson.getBytes(), 200);
  }

  @Test
  public void expectToWriteEtagValueForDirectoryRequest() throws IOException {
    DirectoryRequestDto directoryRequestDto = DirectoryRequestDto.builder()
        .files(Collections.singletonList("index.html"))
        .eTag(DUMMY_ETAG)
        .subdirectories(Collections.singletonList("subdirectory"))
        .build();

    this.responseWriter
        .writeHttpResponseWithDirectoryData(directoryRequestDto, this.out, this.dataOut);

    verify(this.out).println("ETag: \"" + DUMMY_ETAG + "\"");
  }

  @Test
  public void expectToWriteEtagValueForFileRequest() throws IOException {
    FileRequestDto fileRequestDto = FileRequestDto.builder()
        .eTag(DUMMY_ETAG)
        .build();

    this.responseWriter.writeHttpResponseWithFileData(fileRequestDto, this.out, this.dataOut);

    verify(this.out).println("ETag: \"" + DUMMY_ETAG + "\"");
  }

}
