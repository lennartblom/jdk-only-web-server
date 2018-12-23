package de.blom.httpwebserver.common;

import org.apache.commons.httpclient.HttpStatus;

import java.io.PrintWriter;
import java.util.Date;

public class HTTPResponseOutput {

    public void writeResponseContent(String contentType, int fileLength, PrintWriter out){

    }

    public void writeResponseHeader(int httpStatus, PrintWriter out) {
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
