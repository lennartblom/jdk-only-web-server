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
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;
import java.util.StringTokenizer;

// The tutorial can be found just here on the SSaurel's Blog :
// https://www.ssaurel.com/blog/create-a-simple-http-web-server-in-java
// Each Client Connection will be managed in a dedicated Thread
public class JavaHTTPServer implements Runnable{

    static final File WEB_ROOT = new File("./dir/");
    static final String FILE_NOT_FOUND = "404.html";
    static final String METHOD_NOT_SUPPORTED = "not_supported.html";
    static final int PORT = 8080;
    static final boolean verbose = true;

    private HTTPResponseOutput httpResponseOutput;
    private Socket connect;

    public JavaHTTPServer(Socket c) {
        this.connect = c;
    }

    public JavaHTTPServer(Socket c, HTTPResponseOutput httpResponseOutput) {
        this(c);
        this.httpResponseOutput = httpResponseOutput;
    }


    public static void main(String[] args) {
        try {
            ServerSocket serverConnect = new ServerSocket(PORT);
            System.out.println("Server started.\nListening for connections on port : " + PORT + " ...\n");

            while (true) {
                JavaHTTPServer myServer = new JavaHTTPServer(serverConnect.accept());

                if (verbose) {
                    System.out.println("Connecton opened. (" + new Date() + ")");
                }

                // create dedicated thread to manage the client connection
                Thread thread = new Thread(myServer);
                thread.start();
            }

        } catch (IOException e) {
            System.err.println("Server Connection error : " + e.getMessage());
        }
    }

    public void run() {
        // we manage our particular client connection
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


            if (!HTTPMethod.GET.name().equals(method)  &&  !HTTPMethod.HEAD.name().equals(method)) {
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
                System.err.println("Error with file not found exception : " + ioException.getMessage());
            }

        } catch (IOException ioe) {
            System.err.println("Server error : " + ioe);
        } finally {
            try {
                this.closeElements(in, out, dataOut);
            } catch (Exception e) {
                System.err.println("Error closing stream : " + e.getMessage());
            }

            if (verbose) {
                System.out.println("Connection closed.\n");
            }
        }


    }

    private String getRequestedFile(StringTokenizer parse) {
        return parse.nextToken().toLowerCase();
    }

    private String getHttpMethod(StringTokenizer parse) {
        return parse.nextToken().toUpperCase();
    }

    private StringTokenizer parseInput(BufferedReader in) throws IOException {
        String input = in.readLine();
        return new StringTokenizer(input);
    }

    void closeElements(BufferedReader in, PrintWriter out, BufferedOutputStream dataOut) throws IOException {
        in.close();
        out.close();
        dataOut.close();
        connect.close();
    }

    void handleMethodNotRequested(String method, PrintWriter out, BufferedOutputStream dataOut) throws IOException {
        if (verbose) {
            System.out.println("501 Not Implemented : " + method + " method.");
        }

        // we return the not supported file to the client
        File file = this.retrieveFile(METHOD_NOT_SUPPORTED);
        int fileLength = (int) file.length();
        String contentMimeType = "text/html";
        //read content to return to client
        byte[] fileData = readFileData(file, fileLength);
        // we send HTTP Headers with data to client
        this.httpResponseOutput.writeResponseHeader(HttpStatus.SC_NOT_IMPLEMENTED, out);
        out.println("Content-type: " + contentMimeType);
        out.println("Content-length: " + fileLength);
        out.println(); // blank line between headers and content, very important !
        out.flush(); // flush character output stream buffer
        // file
        dataOut.write(fileData, 0, fileLength);
        dataOut.flush();
    }

    void handleFileRequest(String fileRequested, String method, PrintWriter out, BufferedOutputStream dataOut) throws IOException {
        System.out.println("File requested");
        File file = this.retrieveFile(fileRequested);
        int fileLength = (int) file.length();
        String content = getContentType(fileRequested);

        if (HTTPMethod.GET.name().equals(method)) {
            byte[] fileData = readFileData(file, fileLength);

            this.httpResponseOutput.writeResponseHeader(HttpStatus.SC_OK, out);
            out.println("Content-type: " + content);
            out.println("Content-length: " + fileLength);
            out.println(); // blank line between headers and content, very important !
            out.flush(); // flush character output stream buffer

            dataOut.write(fileData, 0, fileLength);
            dataOut.flush();
        }

        if (verbose) {
            System.out.println("File " + fileRequested + " of type " + content + " returned");
        }
    }

    private File retrieveFile(String fileRequested) {
        return new File(WEB_ROOT, fileRequested);
    }

    void handleDirectoryRequest() {
        System.out.println("Directory requested");
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

    String getContentType(String fileRequested) {
        if (fileRequested.endsWith(".htm")  ||  fileRequested.endsWith(".html"))
            return "text/html";
        else
            return "text/plain";
    }

    void handleFileNotFound(PrintWriter out, OutputStream dataOut, String fileRequested) throws IOException {
        File file = this.retrieveFile(FILE_NOT_FOUND);
        int fileLength = (int) file.length();
        String content = "text/html";
        byte[] fileData = readFileData(file, fileLength);

        this.httpResponseOutput.writeResponseHeader(HttpStatus.SC_NOT_FOUND, out);
        out.println("Content-type: " + content);
        out.println("Content-length: " + fileLength);
        out.println(); // blank line between headers and content, very important !
        out.flush(); // flush character output stream buffer

        dataOut.write(fileData, 0, fileLength);
        dataOut.flush();

        if (verbose) {
            System.out.println("File " + fileRequested + " not found");
        }
    }

}