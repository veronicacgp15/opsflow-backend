package com.opsflow.document_service.application.dtos.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class DocumentTypeActivePatchRequest {

    @NotNull(message = "El campo active es obligatorio")
    private Boolean active;
}
