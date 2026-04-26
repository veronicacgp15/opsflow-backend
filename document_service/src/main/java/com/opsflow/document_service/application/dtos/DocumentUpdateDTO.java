package com.opsflow.document_service.application.dtos;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DocumentUpdateDTO {
    @Size(min = 3, max = 255)
    private String name;
    
    private Long documentTypeId;
    
    @FutureOrPresent
    private LocalDate expirationDate;
    
    private String targetEntityType;
    
    private Long targetEntityId;
    
    private Long locationId;
}
