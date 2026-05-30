package com.proyecto.historial_medico_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PaginacionDto {

    private long totalRegistros;
    private int paginaActual;
    private int totalPaginas;
}