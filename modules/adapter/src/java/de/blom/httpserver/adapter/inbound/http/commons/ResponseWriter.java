package de.blom.httpserver.adapter.inbound.http.commons;

import com.google.gson.Gson;
import de.blom.httpserver.crosscutting.representation.fileserver.DirectoryRequestDto;
import de.blom.httpserver.crosscutting.representation.fileserver.FileRequestDto;
import de.blom.httpserver.crosscutting.representation.wall.WallEntryOutboundDto;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.List;

public class ResponseWriter {

  public void writeHttpResponse(final List<WallEntryOutboundDto> wallEntries, final PrintWriter out,
      final BufferedOutputStream dataOut) throws IOException {
    final String json = new Gson().toJson(wallEntries);
    final int contentLength = json.getBytes().length;
    final byte[] content = json.getBytes();

    this.writeHttpResponse(out, dataOut, contentLength, ContentTypes.APPLICATION_JSON, content,
        200);
  }

  public void writeHttpResponseWithFileData(final FileRequestDto fileRequestDto,
      final PrintWriter out,
      final BufferedOutputStream dataOut) throws IOException {
    final int fileLength = fileRequestDto.getFileLength();
    final String contentType = fileRequestDto.getContentType();
    final byte[] fileContent = fileRequestDto.getFileContent();
    this.writeHttpResponse(out, dataOut, fileLength, contentType, fileRequestDto.getETag(),
        fileContent, 200);
  }

  public void writeHttpResponseWithDirectoryData(final DirectoryRequestDto directoryRequestDto,
      final PrintWriter out, final BufferedOutputStream dataOut) throws IOException {

    final StringBuilder htmlDirectoryHtmlList = new StringBuilder("<ul>");

    ResponseWriter.createListEntriesForDirectoryElements(htmlDirectoryHtmlList,
        directoryRequestDto.getSubdirectories(), HtmlTemplateData.DIRECTORY_LIST_ENTRY_TEMPLATE);
    ResponseWriter.createListEntriesForDirectoryElements(htmlDirectoryHtmlList,
        directoryRequestDto.getFiles(), HtmlTemplateData.FILE_LIST_ENTRY_TEMPLATE);

    htmlDirectoryHtmlList.append("</ul>");

    this.writeHttpResponse(
        out,
        dataOut,
        htmlDirectoryHtmlList.toString().getBytes().length,
        ContentTypes.TEXT_HTML,
        directoryRequestDto.getETag(),
        htmlDirectoryHtmlList.toString().getBytes(),
        200
    );

  }

  private static void createListEntriesForDirectoryElements(
      final StringBuilder htmlDirectoryHtmlList,
      final List<String> subdirectories, final String directoryListEntryTemplate) {
    for (final String subdirectory : subdirectories) {
      final String subdirectoryListEntry =
          String.format(directoryListEntryTemplate, subdirectory, subdirectory);
      htmlDirectoryHtmlList.append(subdirectoryListEntry);
    }
  }

  void writeHttpResponse(final PrintWriter head, final BufferedOutputStream body,
      final int contentLength,
      final String contentType, final byte[] data, final int statusCode) throws IOException {
    this.writeResponseHeader(statusCode, head);
    ResponseWriter.writeResponseContentInformation(contentType, contentLength, head);
    head.println();
    head.flush();

    if (body != null) {
      body.write(data, 0, contentLength);
      body.flush();
    }

  }

  void writeHttpResponse(final PrintWriter head, final BufferedOutputStream body,
      final int contentLength,
      final String contentType, final String eTag, final byte[] data, final int statusCode)
      throws IOException {
    this.writeResponseHeader(statusCode, head);
    head.println("ETag: \"" + eTag + "\"");
    ResponseWriter.writeResponseContentInformation(contentType, contentLength, head);
    head.println();
    head.flush();

    if (body != null) {
      body.write(data, 0, contentLength);
      body.flush();
    }

  }

  void writeResponseHeader(final int httpStatus, final PrintWriter httpResponseHead) {
    switch (httpStatus) {
      case 404:
        httpResponseHead.println("HTTP/1.1 404 File Not Found");
        break;

      case 200:
        httpResponseHead.println("HTTP/1.1 200 OK");
        break;

      case 304:
        httpResponseHead.println("HTTP/1.1 304 Not Modified");
        break;

      case 501:
        httpResponseHead.println("HTTP/1.1 501 Not Implemented");
        break;

      case 400:
        httpResponseHead.println("HTTP/1.1 400 Bad Request");
        break;

      case 503:
        httpResponseHead.println("HTTP/1.1 503 Service Unavailable");
        break;
      case 201:
        httpResponseHead.println("HTTP/1.1 201 Created");
        break;
      default:
        break;
    }

    writeServerAndDateInformation(httpResponseHead);
  }

  static void writeResponseContentInformation(final String contentType, final int fileLength,
      final PrintWriter httpResponseHead) {
    httpResponseHead.println("Content-type: " + contentType);
    httpResponseHead.println("Content-length: " + fileLength);
  }

  public void respondeWith404(final PrintWriter httpResponseHead,
      final BufferedOutputStream dataOut)
      throws IOException {
    this.writeHttpResponse(httpResponseHead, dataOut,
        ResponseHtmlData.FILE_NOT_FOUND_HTML.getBytes().length, ContentTypes.TEXT_HTML,
        ResponseHtmlData.FILE_NOT_FOUND_HTML.getBytes(), 404);
  }

  public void respondeWith400(final PrintWriter httpResponseHead,
      final BufferedOutputStream httpResponseBody)
      throws IOException {
    this.writeHttpResponse(httpResponseHead, httpResponseBody,
        ResponseHtmlData.BAD_REQUEST.getBytes().length, ContentTypes.TEXT_HTML,
        ResponseHtmlData.BAD_REQUEST.getBytes(), 400);
  }

  public void respondeWith503(final PrintWriter httpResponseHead,
      final BufferedOutputStream httpResponseBody)
      throws IOException {
    this.writeHttpResponse(httpResponseHead, httpResponseBody,
        ResponseHtmlData.SERVICE_NOT_AVAILABLE.getBytes().length, ContentTypes.TEXT_HTML,
        ResponseHtmlData.SERVICE_NOT_AVAILABLE.getBytes(), 503);
  }

  public void respondeWith501(final PrintWriter httpResponseHead,
      final BufferedOutputStream dataOut)
      throws IOException {
    this.writeHttpResponse(httpResponseHead, dataOut,
        ResponseHtmlData.METHOD_NOT_IMPLEMENTED_HTML.getBytes().length, ContentTypes.TEXT_HTML,
        ResponseHtmlData.METHOD_NOT_IMPLEMENTED_HTML.getBytes(), 501);
  }

  public void respondeWith201(final PrintWriter httpResponseHead,
      final BufferedOutputStream dataOut)
      throws IOException {
    this.writeHttpResponse(httpResponseHead, dataOut,
        ResponseHtmlData.WALL_ENTRY_CREATED.getBytes().length, ContentTypes.TEXT_HTML,
        ResponseHtmlData.WALL_ENTRY_CREATED.getBytes(), 201);
  }

  public void respondeWith304(final PrintWriter httpResponseHead) {
    this.writeResponseHeader(304, httpResponseHead);
    httpResponseHead.println();
    httpResponseHead.flush();
  }

  private void writeServerAndDateInformation(final PrintWriter httpResponseHead) {
    httpResponseHead.println("Server: Java HTTP Server");
    httpResponseHead.println("Date: " + ResponseWriter.getCurrentDate());
  }

  public static Date getCurrentDate() {
    return new Date();
  }

  private static class HtmlTemplateData {
    private HtmlTemplateData() {
    }

    private static final String DIRECTORY_LIST_ENTRY_TEMPLATE = "<li>%s/</li>";
    private static final String FILE_LIST_ENTRY_TEMPLATE = "<li>%s</li>";
  }

  static class ResponseHtmlData {
    private ResponseHtmlData() {
    }

    static final String SERVICE_NOT_AVAILABLE = "<h1>503 Service not available</h1>";
    private static final String FILE_NOT_FOUND_HTML = "<h1>404 not Found</h1>";
    static final String BAD_REQUEST = "<h1>400 Bad Request</h1>";
    private static final String METHOD_NOT_IMPLEMENTED_HTML = "<h1>501 method not implemented</h1>";
    static final String WALL_ENTRY_CREATED = "<h1>Wall entry created</h1>";

  }

  private static class ContentTypes {
    private ContentTypes() {
    }

    private static final String TEXT_HTML = "text/html";
    private static final String APPLICATION_JSON = "application/json";

  }

}
