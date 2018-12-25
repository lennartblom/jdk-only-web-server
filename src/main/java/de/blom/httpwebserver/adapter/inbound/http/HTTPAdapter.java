package de.blom.httpwebserver.adapter.inbound.http;

import de.blom.httpwebserver.adapter.inbound.http.util.ResponseWriter;
import de.blom.httpwebserver.domain.fileserver.DirectoryRequestDto;
import de.blom.httpwebserver.domain.fileserver.DirectoryService;
import de.blom.httpwebserver.domain.fileserver.FileRequestDto;
import de.blom.httpwebserver.enums.HTTPMethod;
import org.apache.commons.httpclient.HttpStatus;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

public class HTTPAdapter implements Runnable {

    private static final Logger log = Logger.getLogger(HTTPAdapter.class.getName());

    private static final int PORT = 8080;
    private static final boolean VERBOSE = true;

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
                    log.info("Connection opened. (" + new Date() + ")");
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
        String uri = null;

        try (BufferedReader in = new BufferedReader(new InputStreamReader(connect.getInputStream()));
             PrintWriter out = new PrintWriter(connect.getOutputStream());
             BufferedOutputStream dataOut = new BufferedOutputStream(connect.getOutputStream())
        ) {
            String input = in.readLine();
            if (input != null) {

                StringTokenizer parse = new StringTokenizer(input);
                String method = parse.nextToken().toUpperCase();
                uri = parse.nextToken().toLowerCase();


                HTTPMethod httpMethod = this.identifyHTTPMethod(method);
                if (httpMethod == null) {
                    this.responseWriter.respondeWith501(out, dataOut);

                } else {
                    switch (httpMethod) {
                        case POST:

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

                            break;
                        case HEAD:
                        case GET:
                            if (uri.endsWith("/")) {

                                DirectoryRequestDto directoryRequestDto = this.directoryService.handleDirectoryRequest(uri);
                                if (!directoryRequestDto.getFound()) {
                                    this.responseWriter.respondeWith404(out, dataOut);
                                } else {
                                    this.handleDirectoryRequest(directoryRequestDto, out, dataOut);
                                }
                            } else {
                                FileRequestDto response = this.directoryService.handleFileRequest(uri);

                                if (!response.getFound()) {
                                    this.responseWriter.respondeWith404(out, dataOut);
                                } else {
                                    this.responseWriter.writeHttpResponse(out, dataOut, response.getFileLength(), response.getContentType(), response.getFileContent(), HttpStatus.SC_OK);
                                }

                            }
                            break;
                        default:
                            this.responseWriter.respondeWith501(out, dataOut);
                            break;
                    }
                }


            }

        } catch (IOException ioe) {
            log.log(Level.SEVERE, "Server error : " + ioe.getMessage(), ioe);
        } finally {
            if (VERBOSE) {
                log.info("Connection closed.\n");
            }
        }


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

    void handleDirectoryRequest(DirectoryRequestDto directoryInformation, PrintWriter out, BufferedOutputStream dataOut) throws IOException {
        log.info("Directory requested");
        this.responseWriter.writeHttpResponse(directoryInformation, out, dataOut);
    }


}