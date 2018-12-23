package de.blom.httpwebserver.common;

import org.apache.commons.httpclient.HttpStatus;

import java.io.PrintWriter;
import java.util.Date;

public class HTTPResponseOutput {

    public void writeResponse(int httpStatus, PrintWriter out) {
        switch (httpStatus) {
            case HttpStatus.SC_NOT_FOUND:
                write404Response(out);
                break;

            case HttpStatus.SC_OK:
                write200Response(out);
                break;

            default:

                break;
        }
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
