package br.com.sysadm.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import br.com.sysadm.model.Horario;
import br.com.sysadm.repository.HorarioRepository;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/horarios")
public class HorarioController {

    @Autowired
    private HorarioRepository horarioRepository;

    /**
     * Get all Horarios
     * 
     * GET /horarios
     * 
     * Response:
     * [
     *   {
     *     "id": 1,
     *     "diaSemana": "MONDAY",
     *     "horarioInicio": "08:00",
     *     "horarioFim": "12:00",
     *     "medico": {
     *       "id": 1,
     *       "nome": "Dr. João",
     *       "especialidade": "Cardiologia"
     *     },
     *     "clinica": {
     *       "id": 1,
     *       "nome": "Clinica A",
     *       "endereco": "Rua 1, 123"
     *     }
     *   },
     *   ...
     * ]
     */
    @GetMapping
    public List<Horario> getAllHorarios() {
        return horarioRepository.findAll();
    }

    /**
     * Get Horario by ID
     * 
     * GET /horarios/{id}
     * 
     * Response:
     * {
     *   "id": 1,
     *   "diaSemana": "MONDAY",
     *   "horarioInicio": "08:00",
     *   "horarioFim": "12:00",
     *   "medico": {
     *     "id": 1,
     *     "nome": "Dr. João",
     *     "especialidade": "Cardiologia"
     *   },
     *   "clinica": {
     *     "id": 1,
     *     "nome": "Clinica A",
     *     "endereco": "Rua 1, 123"
     *   }
     * }
     */
    @GetMapping("/{id}")
    public ResponseEntity<Horario> getHorarioById(@PathVariable Long id) {
        Optional<Horario> horario = horarioRepository.findById(id);
        return horario.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Create a new Horario
     * 
     * POST /horarios/cadastrar
     * 
     * Request Body:
     * {
     *   "diaSemana": "MONDAY",
     *   "horarioInicio": "08:00",
     *   "horarioFim": "12:00",
     *   "medicoId": 1,
     *   "clinicaId": 1
     * }
     * 
     * Response:
     * {
     *   "id": 1,
     *   "diaSemana": "MONDAY",
     *   "horarioInicio": "08:00",
     *   "horarioFim": "12:00",
     *   "medico": {
     *     "id": 1,
     *     "nome": "Dr. João",
     *     "especialidade": "Cardiologia"
     *   },
     *   "clinica": {
     *     "id": 1,
     *     "nome": "Clinica A",
     *     "endereco": "Rua 1, 123"
     *   }
     * }
     */
    @PostMapping("/cadastrar")
    public Horario createHorario(@RequestBody Horario horario) {
        return horarioRepository.save(horario);
    }

    /**
     * Partially update an existing Horario
     * 
     * PATCH /horarios/atualizar/{id}
     * 
     * Request Body:
     * {
     *   "diaSemana": "TUESDAY", // Optional
     *   "horarioInicio": "09:00", // Optional
     *   "horarioFim": "13:00" // Optional
     * }
     * 
     * Response:
     * {
     *   "id": 1,
     *   "diaSemana": "TUESDAY",
     *   "horarioInicio": "09:00",
     *   "horarioFim": "13:00",
     *   "medico": {
     *     "id": 1,
     *     "nome": "Dr. João",
     *     "especialidade": "Cardiologia"
     *   },
     *   "clinica": {
     *     "id": 1,
     *     "nome": "Clinica A",
     *     "endereco": "Rua 1, 123"
     *   }
     * }
     */
    @PatchMapping("/atualizar/{id}")
    public ResponseEntity<Horario> partialUpdateHorario(@PathVariable Long id, @RequestBody Horario horarioDetails) {
        Optional<Horario> horario = horarioRepository.findById(id);
        if (horario.isPresent()) {
            Horario updatedHorario = horario.get();
            if (horarioDetails.getDiaSemana() != null) {
                updatedHorario.setDiaSemana(horarioDetails.getDiaSemana());
            }
            if (horarioDetails.getHorarioInicio() != null) {
                updatedHorario.setHorarioInicio(horarioDetails.getHorarioInicio());
            }
            if (horarioDetails.getHorarioFim() != null) {
                updatedHorario.setHorarioFim(horarioDetails.getHorarioFim());
            }
            return ResponseEntity.ok(horarioRepository.save(updatedHorario));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Delete a Horario
     * 
     * DELETE /horarios/remover/{id}
     * 
     * Response:
     * HTTP 200 OK
     */
    @DeleteMapping("/remover/{id}")
    public ResponseEntity<Void> deleteHorario(@PathVariable Long id) {
        if (horarioRepository.existsById(id)) {
            horarioRepository.deleteById(id);
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
