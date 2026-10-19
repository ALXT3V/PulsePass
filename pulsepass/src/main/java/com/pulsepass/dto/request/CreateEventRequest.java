package com.pulsepass.dto.request;


import java.time.LocalDateTime;

import com.pulsepass.domain.enums.EventCategory;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;


public record CreateEventRequest (

    @NotBlank(message = "El codigo del evento es obligatorio") 
    String eventCode,

    @NotBlank(message = "El nombre del evento es obligatorio")
    String name,

    @Size(max = 1000, message = "La descripcion no puede superar los 1000 caracteres")
    String description,

    @NotNull(message = "La categoria es obligatoria")
    EventCategory category,

    @NotNull(message = "La fecha del evento es obligatoria")
    LocalDateTime eventDate,

    @NotNull(message = "La edad minima es obligatoria")
    @Min(value=0, message = "La edad minima no puede ser negativa")
    Integer minimumAge,

    @NotBlank(message = "El codigo del venue es obligatorio")
    String venueCode
){
    
}
