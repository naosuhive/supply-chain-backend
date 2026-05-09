package com.noasuhive.supply_chain_management.exceptions;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GlobalExceptionHandlerTests {

    private final GlobalExceptionHandler globalExceptionHandler = new GlobalExceptionHandler();

    @Test
    void missingQueryParameterReturnsBadRequest() {
        WebRequest request = new ServletWebRequest(new MockHttpServletRequest("GET", "/api/inventory/search"));

        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response =
                globalExceptionHandler.handleMissingServletRequestParameterException(
                        new MissingServletRequestParameterException("itemName", "String"),
                        request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Bad request", response.getBody().getError());
        assertEquals("Required query parameter 'itemName' is missing", response.getBody().getMessage());
        assertEquals("uri=/api/inventory/search", response.getBody().getPath());
    }
}
