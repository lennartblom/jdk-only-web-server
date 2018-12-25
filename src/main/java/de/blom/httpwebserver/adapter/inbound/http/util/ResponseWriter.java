package de.blom.httpwebserver.adapter.inbound.http.util;

import de.blom.httpwebserver.domain.DirectoryRequestDto;
import de.blom.httpwebserver.domain.FileRequestDto;
import org.apache.commons.httpclient.HttpStatus;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.List;

public class ResponseWriter {

    private static final String DIRECTORY_LIST_ENTRY_TEMPLATE = "<li><a href=\"%s/\">%s/</a></li>";
    private static final String FILE_LIST_ENTRY_TEMPLATE = "<li><a href=\"%s\">%s</a></li>";
    private static final String FILE_NOT_FOUND_HTML = "<h1>404 not Found</h1>";

    public void writeHttpResponse(FileRequestDto fileRequestDto, PrintWriter out, BufferedOutputStream dataOut) throws IOException {
        int fileLength = fileRequestDto.getFileLength();
        String contentType = fileRequestDto.getContentType();
        byte[] fileContent = fileRequestDto.getFileContent();
        this.writeHttpResponse(out, dataOut, fileLength, contentType, fileContent, HttpStatus.SC_OK);
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

    public void writeHttpResponse(PrintWriter out, BufferedOutputStream dataOut, int fileLength, String contentMimeType, byte[] fileData, int statusCode) throws IOException {
        this.writeResponseHeader(statusCode, out);
        this.writeResponseContentInformation(contentMimeType, fileLength, out);
        out.println();
        out.flush();
        dataOut.write(fileData, 0, fileLength);
        dataOut.flush();
    }

    void writeResponseHeader(int httpStatus, PrintWriter out) {
        switch (httpStatus) {
            case HttpStatus.SC_NOT_FOUND:
                this.write404Response(out);
                break;

            case HttpStatus.SC_OK:
                this.write200Response(out);
                break;

            case HttpStatus.SC_NOT_IMPLEMENTED:
                this.write501Response(out);
                break;

            default:

                break;
        }
    }

    public void writeResponseContentInformation(String contentType, int fileLength, PrintWriter out){
        out.println("Content-type: " + contentType);
        out.println("Content-length: " + fileLength);
    }

    void write501Response(PrintWriter out) {
        out.println("HTTP/1.1 501 Not Implemented");
        writeServerAndDateInformation(out);
    }

    void write200Response(PrintWriter out) {
        out.println("HTTP/1.1 200 OK");
        writeServerAndDateInformation(out);
    }

    public void respondeWith404(PrintWriter out, BufferedOutputStream dataOut) throws IOException {
        this.writeHttpResponse(out, dataOut, FILE_NOT_FOUND_HTML.getBytes().length, "text/html", FILE_NOT_FOUND_HTML.getBytes(), HttpStatus.SC_NOT_FOUND);
    }

    void write404Response(PrintWriter out) {
        out.println("HTTP/1.1 404 File Not Found");
        this.writeServerAndDateInformation(out);
    }

    private void writeServerAndDateInformation(PrintWriter out) {
        out.println("Server: Java HTTP Server");
        out.println("Date: " + this.getCurrentDate());
    }

    Date getCurrentDate() {
        return new Date();
    }
}
