package com.pulsepass.dto.request;

import java.time.LocalDate;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record RegisterUserRequest(

    @NotBlank(message = "El username es obligatorio")
    String username,

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El formato email no es valido")
    String email,
    
    @NotBlank(message = "El nombre es obligatorio")
    String firstName,

    @NotBlank(message = "El apellido es obligatorio")
    String lastName,

    @NotBlank(message = "El numero de telefono es obligatorio")
    String phone,

    @NotBlank(message = "La ciudad es obligatoria")
    String city,
    
    @NotNull(message = "La fecha de nacimiento es obligatoria")
    LocalDate birthDate
) {
    
}
