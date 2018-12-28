package de.blom.httpwebserver.enums;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

public class HttpMethodTest {

    @Test
    public void expectToUppercaseGetValueToBeGetEnum(){
        HttpMethod value = HttpMethod.valueOf("GET");

        assertThat(value, is(HttpMethod.GET));
    }

    @Test(expected = IllegalArgumentException.class)
    public void expectToThrowExepction(){
        HttpMethod value = HttpMethod.valueOf("TEST");

        assertThat(value, is(HttpMethod.GET));
    }

}