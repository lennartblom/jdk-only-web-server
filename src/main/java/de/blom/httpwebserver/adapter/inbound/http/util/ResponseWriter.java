package de.blom.httpwebserver.adapter.inbound.http.util;

import de.blom.httpwebserver.domain.DirectoryRequestDto;
import de.blom.httpwebserver.domain.FileRequestDto;
import org.apache.commons.httpclient.HttpStatus;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;

public class ResponseWriter {

    public void writeHttpResponse(FileRequestDto fileRequestDto, PrintWriter out, BufferedOutputStream dataOut) throws IOException {
        int fileLength = fileRequestDto.getFileLength();
        String contentType = fileRequestDto.getContentType();
        byte[] fileContent = fileRequestDto.getFileContent();
        this.writeHttpResponse(out, dataOut, fileLength, contentType, fileContent, HttpStatus.SC_OK);
    }

    public void writeHttpResponse(DirectoryRequestDto directoryRequestDto, PrintWriter out, BufferedOutputStream dataOut) throws IOException {

        String htmlDirectoryHtmlList = "<ul>";
        for (String subdirectory: directoryRequestDto.getSubdirectories()) {
            htmlDirectoryHtmlList = htmlDirectoryHtmlList + "<li><a href=\"" + subdirectory + "/\">" + subdirectory + "/</a></li>";
        }
        for (String file: directoryRequestDto.getFiles()) {
            htmlDirectoryHtmlList = htmlDirectoryHtmlList + "<li><a href=\"" + file + "\">" + file + "</a></li>";
        }

        htmlDirectoryHtmlList = htmlDirectoryHtmlList + "</ul>";

        this.writeHttpResponse(out, dataOut, htmlDirectoryHtmlList.getBytes().length, "text/html", htmlDirectoryHtmlList.getBytes(), HttpStatus.SC_OK);

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
