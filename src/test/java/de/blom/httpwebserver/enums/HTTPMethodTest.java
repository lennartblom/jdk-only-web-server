package de.blom.httpwebserver.enums;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

public class HTTPMethodTest {

    @Test
    public void expectToUppercaseGetValueToBeGetEnum(){
        HTTPMethod value = HTTPMethod.valueOf("GET");

        assertThat(value, is(HTTPMethod.GET));
    }

    @Test(expected = IllegalArgumentException.class)
    public void expectToThrowExepction(){
        HTTPMethod value = HTTPMethod.valueOf("TEST");

        assertThat(value, is(HTTPMethod.GET));
    }

}