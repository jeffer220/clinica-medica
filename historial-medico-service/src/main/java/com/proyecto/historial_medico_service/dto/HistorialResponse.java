package com.proyecto.historial_medico_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class HistorialResponse {

    private String status;
    private DataHistorial data;

    @Data
    @AllArgsConstructor
    public static class DataHistorial {

        private Long pacienteId;
        private PaginacionDto paginacion;
        private List<RegistroClinicoResponse> registros;
    }
}