package com.opsflow.document_service.application.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.FutureOrPresent;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DocumentCreateDTO {
    
    @NotBlank(message = "El nombre es obligatorio")
    private String name;
    
    @NotNull(message = "El tipo de documento es obligatorio")
    private Long documentTypeId;
    
    @FutureOrPresent(message = "La fecha de expiración no puede ser pasada")
    private LocalDate expirationDate;
    
    private Long userId;
    
    private Long organizationId;
    
    private String targetEntityType;
    
    private Long targetEntityId;
}
