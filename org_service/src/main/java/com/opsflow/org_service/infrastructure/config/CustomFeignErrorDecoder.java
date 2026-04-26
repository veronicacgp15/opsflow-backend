package com.opsflow.org_service.infrastructure.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.Response;
import feign.codec.ErrorDecoder;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

public class CustomFeignErrorDecoder implements ErrorDecoder {

    private final ErrorDecoder defaultErrorDecoder = new Default();

    @Override
    public Exception decode(String methodKey, Response response) {
        try (InputStream bodyIs = response.body().asInputStream()) {
            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> errorAttributes = mapper.readValue(bodyIs, Map.class);
            String message = (String) errorAttributes.getOrDefault("message", "Error desconocido en servicio externo");

            return new ResponseStatusException(HttpStatus.valueOf(response.status()), message);
        } catch (IOException e) {
            return defaultErrorDecoder.decode(methodKey, response);
        }
    }
}
