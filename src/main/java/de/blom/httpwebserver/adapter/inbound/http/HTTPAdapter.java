package de.blom.httpwebserver.adapter.inbound.http;

import de.blom.httpwebserver.adapter.inbound.http.util.ResponseWriter;
import de.blom.httpwebserver.domain.FileRequestDto;
import de.blom.httpwebserver.domain.DirectoryService;
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
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

public class HTTPAdapter implements Runnable {

    private static final Logger log = Logger.getLogger(HTTPAdapter.class.getName());

    private static final String DIR = "./dir";
    private static final File WEB_ROOT = new File(DIR);
    private static final String FILE_NOT_FOUND = "404.html";
    private static final String METHOD_NOT_SUPPORTED = "not_supported.html";
    private static final int PORT = 8080;
    private static final boolean VERBOSE = true;
    private static final String CONTENT_TYPE_TEXT_HTML = "text/html";
    private static final String CONTENT_TYPE_TEXT_PLAIN = "text/plain";

    private ResponseWriter responseWriter;
    private Socket connect;
    private DirectoryService directoryService;

    HTTPAdapter(Socket c) {
        this.responseWriter = new ResponseWriter();
        this.connect = c;
        this.directoryService = new DirectoryService();
    }

    HTTPAdapter(Socket c, ResponseWriter responseWriter) {
        this(c);
        this.responseWriter = responseWriter;
    }


    public static void main(String[] args) {
        try {
            ServerSocket serverConnect = new ServerSocket(PORT);
            log.info("Server started.\nListening for connections on port : " + PORT + " ...\n");

            while (true) {
                HTTPAdapter myServer = new HTTPAdapter(serverConnect.accept());

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

        /*

        Copied from https://medium.com/@ssaurel/create-a-simple-http-web-server-in-java-3fc12b29d5fd

        BEGIN

         */

        // we manage our particular client connection
        BufferedReader in = null;
        PrintWriter out = null;
        BufferedOutputStream dataOut = null;
        String fileRequested = null;

        try {
            // we read characters from the client via input stream on the socket
            in = new BufferedReader(new InputStreamReader(connect.getInputStream()));
            // we get character output stream to client (for headers)
            out = new PrintWriter(connect.getOutputStream());
            // get binary output stream to client (for requested data)
            dataOut = new BufferedOutputStream(connect.getOutputStream());

            // get first line of the request from the client
            String input = in.readLine();
            // we parse the request with a string tokenizer
            StringTokenizer parse = new StringTokenizer(input);
            String method = parse.nextToken().toUpperCase(); // we get the HTTP method of the client
            // we get file requested
            fileRequested = parse.nextToken().toLowerCase();

            /*

            Copied from https://medium.com/@ssaurel/create-a-simple-http-web-server-in-java-3fc12b29d5fd

            END

             */

            HTTPMethod httpMethod = this.identifyHTTPMethod(method);

            switch (httpMethod) {
                case GET:
                case HEAD:
                    if (fileRequested.endsWith("/")) {

                        this.directoryService.handleDirectoryRequest(fileRequested);
                        this.handleDirectoryRequest(fileRequested, method, out, dataOut);
                    }else {
                        FileRequestDto response = this.directoryService.handleFileRequest(fileRequested);
                        this.responseWriter.writeHttpResponse(out, dataOut, response.getFileLength(), response.getContentType(), response.getFileContent(), HttpStatus.SC_OK);
                    }


                    break;
                default:
                    break;
            }


/*
            if (!HTTPMethod.GET.name().equals(method) && !HTTPMethod.HEAD.name().equals(method)) {
                this.handleMethodNotRequested(method, out, dataOut);

            } else {
                if (fileRequested.endsWith("/")) {
                    this.handleDirectoryRequest(fileRequested, method, out, dataOut);
                }else {
                    this.handleFileRequest(fileRequested, method, out, dataOut);
                }
            }*/

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

    String handleHTTPMethod(HTTPMethod method){
        return null;
    }

    HTTPMethod identifyHTTPMethod(String method) {
        method = method.toUpperCase();
        try {
            return HTTPMethod.valueOf(method);
        } catch (IllegalArgumentException e) {
            return null;
        }
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
        this.responseWriter.writeHttpResponse(out, dataOut, fileLength, CONTENT_TYPE_TEXT_HTML, fileData, HttpStatus.SC_NOT_IMPLEMENTED);
    }

    void handleFileRequest(String fileRequested, String method, PrintWriter out, BufferedOutputStream dataOut) throws IOException {
        log.info("File requested");
        File file = this.retrieveFile(fileRequested);
        int fileLength = (int) file.length();
        String content = this.getContentType(fileRequested);

        if (HTTPMethod.GET.name().equals(method)) {
            byte[] fileData = readFileData(file, fileLength);

            this.responseWriter.writeHttpResponse(out, dataOut, fileLength, content, fileData, HttpStatus.SC_OK);
        }

        if (VERBOSE) {
            log.info("File " + fileRequested + " of type " + content + " returned");
        }
    }

    void handleDirectoryRequest(String fileRequested, String method, PrintWriter out, BufferedOutputStream dataOut) throws IOException {
        log.info("Directory requested");

        File folder = new File(WEB_ROOT, fileRequested);
        File[] files = folder.listFiles();
        List<File> elements = Arrays.asList(folder.listFiles());

        String htmlDirectoryList = "<ul>";
        for (File file : files) {
            String filename = file.toString();
            filename = filename.replace(DIR + "//", "");
            htmlDirectoryList = htmlDirectoryList + "<li><a href=\"" + filename + "\">" + filename + "</a></li>";
        }
        htmlDirectoryList = htmlDirectoryList + "</ul>";

        this.responseWriter.writeHttpResponse(out, dataOut, htmlDirectoryList.getBytes().length, CONTENT_TYPE_TEXT_HTML, htmlDirectoryList.getBytes(), HttpStatus.SC_OK);

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

        this.responseWriter.writeHttpResponse(out, dataOut, fileLength, content, fileData, HttpStatus.SC_NOT_FOUND);

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
            return CONTENT_TYPE_TEXT_PLAIN;
    }

}