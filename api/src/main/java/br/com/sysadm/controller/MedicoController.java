package br.com.sysadm.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import br.com.sysadm.model.Medico;
import br.com.sysadm.repository.MedicoRepository;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/medicos")
public class MedicoController {

    @Autowired
    private MedicoRepository medicoRepository;

    /**
     * Get all Medicos
     * 
     * GET /medicos
     * 
     * Response:
     * [
     *   {
     *     "id": 1,
     *     "cpf": "12345678900",
     *     "nome": "Dr. João",
     *     "especialidade": "Cardiologia"
     *   },
     *   ...
     * ]
     */
    @GetMapping
    public List<Medico> getAllMedicos() {
        return medicoRepository.findAll();
    }

    /**
     * Get Medico by ID
     * 
     * GET /medicos/{id}
     * 
     * Response:
     * {
     *   "id": 1,
     *   "cpf": "12345678900",
     *   "nome": "Dr. João",
     *   "especialidade": "Cardiologia"
     * }
     */
    @GetMapping("/{id}")
    public ResponseEntity<Medico> getMedicoById(@PathVariable Long id) {
        Optional<Medico> medico = medicoRepository.findById(id);
        return medico.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Create a new Medico
     * 
     * POST /medicos/cadastrar
     * 
     * Request Body:
     * {
     *   "cpf": "12345678900",
     *   "nome": "Dr. João",
     *   "especialidade": "Cardiologia"
     * }
     * 
     * Response:
     * {
     *   "id": 1,
     *   "cpf": "12345678900",
     *   "nome": "Dr. João",
     *   "especialidade": "Cardiologia"
     * }
     */
    @PostMapping("/cadastrar")
    public Medico createMedico(@RequestBody Medico medico) {
        return medicoRepository.save(medico);
    }

    /**
     * Partially update an existing Medico
     * 
     * PATCH /medicos/atualizar/{id}
     * 
     * Request Body:
     * {
     *   "nome": "Dr. João Atualizado", // Optional
     *   "especialidade": "Cardiologia Atualizada" // Optional
     * }
     * 
     * Response:
     * {
     *   "id": 1,
     *   "cpf": "12345678900",
     *   "nome": "Dr. João Atualizado",
     *   "especialidade": "Cardiologia Atualizada"
     * }
     */
    @PatchMapping("/atualizar/{id}")
    public ResponseEntity<Medico> partialUpdateMedico(@PathVariable Long id, @RequestBody Medico medicoDetails) {
        Optional<Medico> medico = medicoRepository.findById(id);
        if (medico.isPresent()) {
            Medico updatedMedico = medico.get();
            if (medicoDetails.getNome() != null) {
                updatedMedico.setNome(medicoDetails.getNome());
            }
            if (medicoDetails.getEspecialidade() != null) {
                updatedMedico.setEspecialidade(medicoDetails.getEspecialidade());
            }
            return ResponseEntity.ok(medicoRepository.save(updatedMedico));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Delete a Medico
     * 
     * DELETE /medicos/remover/{id}
     * 
     * Response:
     * HTTP 200 OK
     */
    @DeleteMapping("/remover/{id}")
    public ResponseEntity<Void> deleteMedico(@PathVariable Long id) {
        if (medicoRepository.existsById(id)) {
            medicoRepository.deleteById(id);
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Create Medicos in Batch
     * 
     * POST /medicos/cadastrar/lote
     * 
     * Request Body:
     * [
     *   {
     *     "cpf": "12345678900",
     *     "nome": "Dr. João",
     *     "especialidade": "Cardiologia"
     *   },
     *   ...
     * ]
     * 
     * Response:
     * [
     *   {
     *     "id": 1,
     *     "cpf": "12345678900",
     *     "nome": "Dr. João",
     *     "especialidade": "Cardiologia"
     *   },
     *   ...
     * ]
     */
    @PostMapping("/cadastrar/lote")
    public ResponseEntity<List<Medico>> createMedicosLote(@RequestBody List<Medico> medicos) {
        List<Medico> savedMedicos = medicoRepository.saveAll(medicos);
        return ResponseEntity.ok(savedMedicos);
    }
}
