package de.blom.httpwebserver.adapter.inbound.http.commons;

import com.google.gson.Gson;
import de.blom.httpwebserver.representation.fileserver.DirectoryRequestDto;
import de.blom.httpwebserver.representation.fileserver.FileRequestDto;
import de.blom.httpwebserver.representation.wall.WallEntryOutboundDto;
import org.apache.commons.httpclient.HttpStatus;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.List;

public class ResponseWriter {

    private static final String DIRECTORY_LIST_ENTRY_TEMPLATE = "<li>%s/</li>";
    private static final String FILE_LIST_ENTRY_TEMPLATE = "<li>%s</li>";
    private static final String FILE_NOT_FOUND_HTML = "<h1>404 not Found</h1>";
    static final String BAD_REQUEST = "<h1>400 Bad Request</h1>";
    private static final String METHOD_NOT_IMPLEMENTED_HTML = "<h1>501 method not implemented</h1>";
    static final String SERVICE_NOT_AVAILABLE = "<h1>503 Service not available</h1>";
    static final String WALL_ENTRY_CREATED = "<h1>Wall entry created</h1>";

    public void writeHttpResponse(FileRequestDto fileRequestDto, PrintWriter out, BufferedOutputStream dataOut) throws IOException {
        int fileLength = fileRequestDto.getFileLength();
        String contentType = fileRequestDto.getContentType();
        byte[] fileContent = fileRequestDto.getFileContent();
        this.writeHttpResponse(out, dataOut, fileLength, contentType, fileContent, HttpStatus.SC_OK);
    }

    public void writeHttpResponse(List<WallEntryOutboundDto> wallEntries, PrintWriter out, BufferedOutputStream dataOut) throws IOException {
        String json = new Gson().toJson(wallEntries);
        int contentLength = json.getBytes().length;
        byte[] content = json.getBytes();

        this.writeHttpResponse(out, dataOut, contentLength, "application/json", content, HttpStatus.SC_OK);
    }

    public void writeHttpResponse(DirectoryRequestDto directoryRequestDto, PrintWriter out, BufferedOutputStream dataOut) throws IOException {

        StringBuilder htmlDirectoryHtmlList = new StringBuilder("<ul>");

        this.createListEntriesForDirectoryElements(htmlDirectoryHtmlList, directoryRequestDto.getSubdirectories(), DIRECTORY_LIST_ENTRY_TEMPLATE);
        this.createListEntriesForDirectoryElements(htmlDirectoryHtmlList, directoryRequestDto.getFiles(), FILE_LIST_ENTRY_TEMPLATE);

        htmlDirectoryHtmlList.append("</ul>");

        this.writeHttpResponse(out, dataOut, htmlDirectoryHtmlList.toString().getBytes().length, "text/html", htmlDirectoryHtmlList.toString().getBytes(), HttpStatus.SC_OK);

    }

    private void createListEntriesForDirectoryElements(StringBuilder htmlDirectoryHtmlList, List<String> subdirectories, String directoryListEntryTemplate) {
        for (String subdirectory : subdirectories) {
            String subdirectoryListEntry = String.format(directoryListEntryTemplate, subdirectory, subdirectory);
            htmlDirectoryHtmlList.append(subdirectoryListEntry);
        }
    }

    void writeHttpResponse(PrintWriter httpResponseHead, BufferedOutputStream httpResponseBody, int contentLength, String contentMimeType, byte[] fileData, int statusCode) throws IOException {
        this.writeResponseHeader(statusCode, httpResponseHead);
        this.writeResponseContentInformation(contentMimeType, contentLength, httpResponseHead);
        httpResponseHead.println();
        httpResponseHead.flush();

        if (httpResponseBody != null) {
            httpResponseBody.write(fileData, 0, contentLength);
            httpResponseBody.flush();
        }

    }

    void writeResponseHeader(int httpStatus, PrintWriter httpResponseHead) {
        switch (httpStatus) {
            case HttpStatus.SC_NOT_FOUND:
                httpResponseHead.println("HTTP/1.1 404 File Not Found");
                break;

            case HttpStatus.SC_OK:
                httpResponseHead.println("HTTP/1.1 200 OK");
                break;

            case HttpStatus.SC_NOT_IMPLEMENTED:
                httpResponseHead.println("HTTP/1.1 501 Not Implemented");
                break;

            case HttpStatus.SC_BAD_REQUEST:
                httpResponseHead.println("HTTP/1.1 400 Bad Request");
                break;

            case HttpStatus.SC_SERVICE_UNAVAILABLE:
                httpResponseHead.println("HTTP/1.1 503 Service Unavailable");
                break;
            case HttpStatus.SC_CREATED:
                httpResponseHead.println("HTTP/1.1 201 Created");
                break;
            default:
                break;
        }

        writeServerAndDateInformation(httpResponseHead);
    }

    public void writeResponseContentInformation(String contentType, int fileLength, PrintWriter httpResponseHead) {
        httpResponseHead.println("Content-type: " + contentType);
        httpResponseHead.println("Content-length: " + fileLength);
    }

    public void respondeWith404(PrintWriter httpResponseHead, BufferedOutputStream dataOut) throws IOException {
        this.writeHttpResponse(httpResponseHead, dataOut, FILE_NOT_FOUND_HTML.getBytes().length, "text/html", FILE_NOT_FOUND_HTML.getBytes(), HttpStatus.SC_NOT_FOUND);
    }

    public void respondeWith400(PrintWriter httpResponseHead, BufferedOutputStream httpResponseBody) throws IOException {
        this.writeHttpResponse(httpResponseHead, httpResponseBody, BAD_REQUEST.getBytes().length, "text/html", BAD_REQUEST.getBytes(), HttpStatus.SC_BAD_REQUEST);
    }

    public void respondeWith503(PrintWriter httpResponseHead, BufferedOutputStream httpResponseBody) throws IOException {
        this.writeHttpResponse(httpResponseHead, httpResponseBody, SERVICE_NOT_AVAILABLE.getBytes().length, "text/html", SERVICE_NOT_AVAILABLE.getBytes(), HttpStatus.SC_SERVICE_UNAVAILABLE);
    }

    public void respondeWith501(PrintWriter httpResponseHead, BufferedOutputStream dataOut) throws IOException {
        this.writeHttpResponse(httpResponseHead, dataOut, METHOD_NOT_IMPLEMENTED_HTML.getBytes().length, "text/html", METHOD_NOT_IMPLEMENTED_HTML.getBytes(), HttpStatus.SC_NOT_IMPLEMENTED);
    }

    public void respondeWith201(PrintWriter httpResponseHead, BufferedOutputStream dataOut) throws IOException {
        this.writeHttpResponse(httpResponseHead, dataOut, WALL_ENTRY_CREATED.getBytes().length, "text/html", WALL_ENTRY_CREATED.getBytes(), HttpStatus.SC_CREATED);
    }

    void write404Response(PrintWriter httpResponseHead) {
        httpResponseHead.println("HTTP/1.1 404 File Not Found");
        this.writeServerAndDateInformation(httpResponseHead);
    }

    private void writeServerAndDateInformation(PrintWriter httpResponseHead) {
        httpResponseHead.println("Server: Java HTTP Server");
        httpResponseHead.println("Date: " + this.getCurrentDate());
    }

    Date getCurrentDate() {
        return new Date();
    }


}
