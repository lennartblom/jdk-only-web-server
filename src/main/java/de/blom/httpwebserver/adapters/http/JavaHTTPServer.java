package de.blom.httpwebserver.adapters.http;

import de.blom.httpwebserver.common.HTTPResponseOutput;
import de.blom.httpwebserver.enums.HTTPMethod;
import org.apache.commons.httpclient.HttpStatus;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

public class JavaHTTPServer implements Runnable {

    private static final Logger log = Logger.getLogger(JavaHTTPServer.class.getName());

    private static final File WEB_ROOT = new File("./dir/");
    private static final String FILE_NOT_FOUND = "404.html";
    private static final String METHOD_NOT_SUPPORTED = "not_supported.html";
    private static final int PORT = 8080;
    private static final boolean VERBOSE = true;
    private static final String CONTENT_TYPE_TEXT_HTML = "text/html";

    private HTTPResponseOutput httpResponseOutput;
    private Socket connect;

    JavaHTTPServer(Socket c) {
        this.httpResponseOutput = new HTTPResponseOutput();
        this.connect = c;
    }

    JavaHTTPServer(Socket c, HTTPResponseOutput httpResponseOutput) {
        this(c);
        this.httpResponseOutput = httpResponseOutput;
    }


    public static void main(String[] args) {
        try {
            ServerSocket serverConnect = new ServerSocket(PORT);
            log.info("Server started.\nListening for connections on port : " + PORT + " ...\n");

            while (true) {
                JavaHTTPServer myServer = new JavaHTTPServer(serverConnect.accept());

                if (VERBOSE) {
                    log.info("Connecton opened. (" + new Date() + ")");
                }

                // create dedicated thread to manage the client connection
                Thread thread = new Thread(myServer);
                thread.start();
            }

        } catch (IOException e) {
            log.log(Level.SEVERE, "Server Connection error : " + e.getMessage(), e);
        }
    }

    public void run() {
        BufferedReader in = null;
        PrintWriter out = null;
        BufferedOutputStream dataOut = null;
        String fileRequested = null;
        String method;

        try {
            in = new BufferedReader(new InputStreamReader(connect.getInputStream()));
            out = new PrintWriter(connect.getOutputStream());

            dataOut = new BufferedOutputStream(connect.getOutputStream());
            StringTokenizer parse = this.parseInput(in);

            method = this.getHttpMethod(parse);
            fileRequested = this.getRequestedFile(parse);


            if (!HTTPMethod.GET.name().equals(method) && !HTTPMethod.HEAD.name().equals(method)) {
                this.handleMethodNotRequested(method, out, dataOut);

            } else {
                if (fileRequested.endsWith("/")) {
                    this.handleDirectoryRequest();
                }

                this.handleFileRequest(fileRequested, method, out, dataOut);

            }

        } catch (FileNotFoundException fileNotFoundException) {
            try {
                this.handleFileNotFound(out, dataOut, fileRequested);
            } catch (IOException ioException) {

                log.log(Level.SEVERE, "Error with file not found exception : " + ioException.getMessage(), ioException);
            }

        } catch (IOException ioe) {
            log.log(Level.SEVERE, "Server error : " + ioe.getMessage(), ioe);
        } finally {
            try {
                this.closeElements(in, out, dataOut);
            } catch (Exception e) {
                log.log(Level.SEVERE, "Error closing stream : " + e.getMessage(), e);
            }

            if (VERBOSE) {
                log.info("Connection closed.\n");
            }
        }


    }

    void writeHttpResponse(PrintWriter out, BufferedOutputStream dataOut, int fileLength, String contentMimeType, byte[] fileData, int statusCode) throws IOException {
        this.httpResponseOutput.writeResponseHeader(statusCode, out);
        this.httpResponseOutput.writeResponseContentInformation(contentMimeType, fileLength, out);
        out.println();
        out.flush();
        dataOut.write(fileData, 0, fileLength);
        dataOut.flush();
    }

    private String getRequestedFile(StringTokenizer parse) {
        return parse.nextToken().toLowerCase();
    }

    private String getHttpMethod(StringTokenizer parse) {
        return parse.nextToken().toUpperCase();
    }

    StringTokenizer parseInput(BufferedReader in) throws IOException {
        String input = in.readLine();
        if (input != null) {
            return new StringTokenizer(input);
        } else {
            throw new IOException();
        }

    }

    void closeElements(BufferedReader in, PrintWriter out, BufferedOutputStream dataOut) throws IOException {
        in.close();
        out.close();
        dataOut.close();
        connect.close();
    }

    void handleMethodNotRequested(String method, PrintWriter out, BufferedOutputStream dataOut) throws IOException {
        if (VERBOSE) {
            log.info("501 Not Implemented : " + method + " method.");
        }


        File file = this.retrieveFile(METHOD_NOT_SUPPORTED);
        int fileLength = (int) file.length();

        byte[] fileData = readFileData(file, fileLength);
        this.writeHttpResponse(out, dataOut, fileLength, CONTENT_TYPE_TEXT_HTML, fileData, HttpStatus.SC_NOT_IMPLEMENTED);
    }

    void handleFileRequest(String fileRequested, String method, PrintWriter out, BufferedOutputStream dataOut) throws IOException {
        System.out.println("File requested");
        File file = this.retrieveFile(fileRequested);
        int fileLength = (int) file.length();
        String content = getContentType(fileRequested);

        if (HTTPMethod.GET.name().equals(method)) {
            byte[] fileData = readFileData(file, fileLength);

            this.writeHttpResponse(out, dataOut, fileLength, content, fileData, HttpStatus.SC_OK);
        }

        if (VERBOSE) {
            System.out.println("File " + fileRequested + " of type " + content + " returned");
        }
    }

    void handleDirectoryRequest() {
        log.info("Directory requested");
    }

    private byte[] readFileData(File file, int fileLength) throws IOException {
        FileInputStream fileIn = null;
        byte[] fileData = new byte[fileLength];

        try {
            fileIn = new FileInputStream(file);
            fileIn.read(fileData);
        } finally {
            if (fileIn != null)
                fileIn.close();
        }

        return fileData;
    }


    void handleFileNotFound(PrintWriter out, BufferedOutputStream dataOut, String fileRequested) throws IOException {
        File file = this.retrieveFile(FILE_NOT_FOUND);
        int fileLength = (int) file.length();
        String content = CONTENT_TYPE_TEXT_HTML;
        byte[] fileData = readFileData(file, fileLength);

        this.writeHttpResponse(out, dataOut, fileLength, content, fileData, HttpStatus.SC_NOT_FOUND);

        if (VERBOSE) {
            log.info("File " + fileRequested + " not found");
        }
    }


    private File retrieveFile(String fileRequested) {
        return new File(WEB_ROOT, fileRequested);
    }


    String getContentType(String fileRequested) {
        if (fileRequested.endsWith(".htm") || fileRequested.endsWith(".html"))
            return CONTENT_TYPE_TEXT_HTML;
        else
            return "text/plain";
    }

}