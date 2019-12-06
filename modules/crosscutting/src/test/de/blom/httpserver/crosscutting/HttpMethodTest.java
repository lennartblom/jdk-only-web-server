package de.blom.httpserver.crosscutting;

import de.blom.httpserver.crosscutting.enums.HttpMethod;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

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
