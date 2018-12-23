package de.blom.httpwebserver.adapters.http;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mockito.Mock;

import java.net.Socket;
import java.util.Arrays;
import java.util.Collection;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

@RunWith(Parameterized.class)
public class JavaHTTPServerContentTypeTest {

    private JavaHTTPServer javaHTTPServer;

    @Mock
    private Socket connection;

    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {"index.html", "text/html"},
                {"index.htm", "text/html"},
                {"index.txt", "text/plain"},
        });
    }

    private String fileName;

    private String expectedContentType;

    public JavaHTTPServerContentTypeTest(String fileName, String expected) {
        this.fileName = fileName;
        this.expectedContentType = expected;

        this.javaHTTPServer = new JavaHTTPServer(connection);
    }

    @Test
    public void test() {
        assertThat(this.javaHTTPServer.getContentType(this.fileName), is(expectedContentType));
    }
}