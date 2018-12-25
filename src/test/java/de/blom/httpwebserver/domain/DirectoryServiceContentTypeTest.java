package de.blom.httpwebserver.domain;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mockito.Mock;

import java.net.Socket;
import java.util.Arrays;
import java.util.Collection;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

@RunWith(Parameterized.class)
public class DirectoryServiceContentTypeTest {

    private DirectoryService directoryService;

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

    public DirectoryServiceContentTypeTest(String fileName, String expected) {
        this.fileName = fileName;
        this.expectedContentType = expected;

        this.directoryService = new DirectoryService();
    }

    @Test
    public void test() {
        assertThat(this.directoryService.getContentType(this.fileName), is(expectedContentType));
    }

}