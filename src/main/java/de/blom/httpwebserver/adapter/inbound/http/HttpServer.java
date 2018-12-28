package de.blom.httpwebserver.adapter.inbound.http;

import de.blom.httpwebserver.adapter.inbound.http.commons.HttpRequest;
import de.blom.httpwebserver.adapter.inbound.http.commons.ResponseWriter;
import de.blom.httpwebserver.domain.fileserver.DirectoryRequestDto;
import de.blom.httpwebserver.domain.fileserver.DirectoryService;
import de.blom.httpwebserver.domain.fileserver.FileRequestDto;
import de.blom.httpwebserver.enums.HttpMethod;

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
            HttpRequest httpRequest = HttpRequest.parseFrom(in);

            if(httpRequest == null){
                return;
            }

            this.handleHttpMethod(httpResponseHead, httpResponseBody, httpRequest);

        } catch (IOException ioe) {
            log.log(Level.SEVERE, "Server error : " + ioe.getMessage(), ioe);
        } finally {
            if (VERBOSE) {
                log.info("Connection closed.\n");
            }
        }


    }

    void handleHttpMethod(PrintWriter httpResponseHead, BufferedOutputStream httpResponseBody, HttpRequest httpRequest) throws IOException {
        switch (httpRequest.getMethod()) {
            case POST:
                this.handlePostRequest(httpRequest.getUri());
                break;

            case HEAD:

                break;
            case GET:
                this.handleGetRequest(httpRequest.getUri(), httpResponseHead, httpResponseBody);
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
        log.info("HTTP Request uri='" + uri + "'");
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


}