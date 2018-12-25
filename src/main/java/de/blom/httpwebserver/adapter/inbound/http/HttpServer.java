package de.blom.httpwebserver.adapter.inbound.http;

import de.blom.httpwebserver.adapter.inbound.http.util.ResponseWriter;
import de.blom.httpwebserver.domain.fileserver.DirectoryRequestDto;
import de.blom.httpwebserver.domain.fileserver.DirectoryService;
import de.blom.httpwebserver.domain.fileserver.FileRequestDto;
import de.blom.httpwebserver.enums.HTTPMethod;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

public class HttpServer implements Runnable {

    private static final Logger log = Logger.getLogger(HttpServer.class.getName());

    private static final int PORT = 8080;
    private static final boolean VERBOSE = true;

    private ResponseWriter responseWriter;
    private Socket connect;
    private DirectoryService directoryService;

    private HttpServer(Socket c) {
        this.responseWriter = new ResponseWriter();
        this.connect = c;
        this.directoryService = new DirectoryService();
    }

    HttpServer(Socket c, ResponseWriter responseWriter) {
        this(c);
        this.responseWriter = responseWriter;
    }

    public static void main(String[] args) {
        try {
            ServerSocket serverConnect = new ServerSocket(PORT);
            log.info("Server started.\nListening for connections on port : " + PORT + " ...\n");

            while (true) {
                HttpServer httpServer = new HttpServer(serverConnect.accept());

                if (VERBOSE) {
                    log.info("Connection opened. (" + new Date() + ")");
                }

                // create dedicated thread to manage the client connection
                Thread thread = new Thread(httpServer);
                thread.start();
            }
        } catch (IOException e) {
            log.log(Level.SEVERE, "Server Connection error : " + e.getMessage(), e);
        }
    }

    public void run() {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(this.connect.getInputStream()));
             PrintWriter httpResponseHead = new PrintWriter(this.connect.getOutputStream());
             BufferedOutputStream httpResponseBody = new BufferedOutputStream(this.connect.getOutputStream())
        ) {
            String httpRequestContentLine = in.readLine();
            if (httpRequestContentLine == null) {
                return;
            }

            StringTokenizer httpRequestLineElements = new StringTokenizer(httpRequestContentLine);
            String rawHttpMethod = httpRequestLineElements.nextToken().toUpperCase();

            String httpUri = httpRequestLineElements.nextToken().toLowerCase();

            HTTPMethod httpMethod = this.identifyHTTPMethod(rawHttpMethod);

            this.handleHttpMethod(httpResponseHead, httpResponseBody, httpUri, httpMethod);

        } catch (IOException ioe) {
            log.log(Level.SEVERE, "Server error : " + ioe.getMessage(), ioe);
        } finally {
            if (VERBOSE) {
                log.info("Connection closed.\n");
            }
        }


    }

    void handleHttpMethod(PrintWriter httpResponseHead, BufferedOutputStream httpResponseBody, String uri, HTTPMethod httpMethod) throws IOException {
        switch (httpMethod) {
            case POST:
                this.handlePostRequest(uri);
                break;

            case HEAD:

                break;
            case GET:
                this.handleGetRequest(uri, httpResponseHead, httpResponseBody);
                break;

            default:
                this.responseWriter.respondeWith501(httpResponseHead, httpResponseBody);
                break;
        }
    }

    void handleGetRequest(String uri, PrintWriter httpResponseHead, BufferedOutputStream httpResponseBody) throws IOException {
        if (uri.endsWith("/")) {
            this.handleGetDirectory(uri, httpResponseHead, httpResponseBody);
        } else {
            this.handleGetFile(uri, httpResponseHead, httpResponseBody);
        }
    }

    void handlePostRequest(String uri) {
        log.info("POST uri='" + uri + "'");
        switch (uri) {
            case "/comments":
            case "/comments/":
                log.info("Comment creation");
                break;

            case "/comments/query":
            case "/comments/query/":
                log.info("Comment retrievement");
                break;

            default:
                break;
        }
    }

    void handleGetFile(String uri, PrintWriter httpResponseHead, BufferedOutputStream httpResponseBody) throws IOException {
        FileRequestDto response = this.directoryService.handleFileRequest(uri);

        if (!response.getFound()) {
            this.responseWriter.respondeWith404(httpResponseHead, httpResponseBody);
        } else {
            this.responseWriter.writeHttpResponse(response, httpResponseHead, httpResponseBody);
        }
    }

    void handleGetDirectory(String uri, PrintWriter httpResponseHead, BufferedOutputStream httpResponseBody) throws IOException {
        DirectoryRequestDto directoryRequestDto = this.directoryService.handleDirectoryRequest(uri);
        if (!directoryRequestDto.getFound()) {
            this.responseWriter.respondeWith404(httpResponseHead, httpResponseBody);
        } else {
            this.responseWriter.writeHttpResponse(directoryRequestDto, httpResponseHead, httpResponseBody);
        }
    }

    HTTPMethod identifyHTTPMethod(String method) {
        method = method.toUpperCase();
        try {
            return HTTPMethod.valueOf(method);
        } catch (IllegalArgumentException e) {
            log.log(Level.SEVERE, "Given method can not be handled yet", e);
            return HTTPMethod.NOT_IMPLEMENTED_YET;
        }
    }

}