package br.com.sysadm.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import br.com.sysadm.model.Clinica;
import br.com.sysadm.model.Horario;
import br.com.sysadm.model.Medico;
import br.com.sysadm.repository.ClinicaRepository;
import br.com.sysadm.repository.HorarioRepository;
import br.com.sysadm.repository.MedicoRepository;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/clinicas")
public class ClinicaController {

    @Autowired
    private ClinicaRepository clinicaRepository;

    @Autowired
    private MedicoRepository medicoRepository;

    @Autowired
    private HorarioRepository horarioRepository;

    @GetMapping
    public List<Clinica> getAllClinicas() {
        return clinicaRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Clinica> getClinicaById(@PathVariable Long id) {
        Optional<Clinica> clinica = clinicaRepository.findById(id);
        return clinica.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/cadastrar")
    public Clinica createClinica(@RequestBody Clinica clinica) {
        return clinicaRepository.save(clinica);
    }

    @PatchMapping("/atualizar/{id}")
    public ResponseEntity<Clinica> partialUpdateClinica(@PathVariable Long id, @RequestBody Clinica clinicaDetails) {
        Optional<Clinica> clinica = clinicaRepository.findById(id);
        if (clinica.isPresent()) {
            Clinica updatedClinica = clinica.get();
            if (clinicaDetails.getNome() != null) {
                updatedClinica.setNome(clinicaDetails.getNome());
            }
            if (clinicaDetails.getEndereco() != null) {
                updatedClinica.setEndereco(clinicaDetails.getEndereco());
            }
            return ResponseEntity.ok(clinicaRepository.save(updatedClinica));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/remover/{id}")
    public ResponseEntity<Void> deleteClinica(@PathVariable Long id) {
        if (clinicaRepository.existsById(id)) {
            clinicaRepository.deleteById(id);
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{id}/medicos")
    public ResponseEntity<Set<Medico>> getMedicosByClinicaId(@PathVariable Long id) {
        Optional<Clinica> clinica = clinicaRepository.findById(id);
        return clinica.map(value -> ResponseEntity.ok(value.getMedicos())).orElseGet(() -> ResponseEntity.notFound().build());
    }
    /**
     * Get a specific Medico in a Clinica by IDs
     * 
     * GET /clinicas/{id}/medicos/{id_medico}
     * 
     * Response:
     * {
     *   "id": 1,
     *   "nome": "Dr. John Doe",
     *   "especialidade": "Cardiologia"
     * }
     */
    @GetMapping("/{id}/medicos/{id_medico}")
    public ResponseEntity<Medico> getMedicoInClinica(
            @PathVariable Long id,
            @PathVariable Long id_medico) {
        Optional<Clinica> clinicaOpt = clinicaRepository.findById(id);
        Optional<Medico> medicoOpt = medicoRepository.findById(id_medico);

        if (clinicaOpt.isPresent() && medicoOpt.isPresent()) {
            Clinica clinica = clinicaOpt.get();
            Medico medico = medicoOpt.get();
            Set<Medico> medicos = clinica.getMedicos();

            if (medicos.contains(medico)) {
                return ResponseEntity.ok(medico);
            } else {
                return ResponseEntity.status(404).body(null);
            }
        } else {
            return ResponseEntity.status(404).body(null);
        }
    }
    @PostMapping("/{id}/medicos/{id_medico}")
    public ResponseEntity<Clinica> addMedicoToClinica(@PathVariable Long id, @PathVariable Long id_medico) {
        Optional<Clinica> clinica = clinicaRepository.findById(id);
        Optional<Medico> medico = medicoRepository.findById(id_medico);
        if (clinica.isPresent() && medico.isPresent()) {
            Clinica updatedClinica = clinica.get();
            Set<Medico> medicos = updatedClinica.getMedicos();
            medicos.add(medico.get());
            updatedClinica.setMedicos(medicos);
            return ResponseEntity.ok(clinicaRepository.save(updatedClinica));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/{clinicaId}/medicos/cpf/{medicoCpf}/horarios")
    public ResponseEntity<String> addHorarioToMedicoByCpf(
            @PathVariable Long clinicaId, 
            @PathVariable String medicoCpf, 
            @RequestBody Horario horario) {
        Optional<Medico> medicoOpt = medicoRepository.findByCpf(medicoCpf);
        if (!medicoOpt.isPresent()) {
            return ResponseEntity.badRequest().body("Médico não encontrado com o CPF fornecido.");
        }
        Optional<Clinica> clinicaOpt = clinicaRepository.findById(clinicaId);
        if (!clinicaOpt.isPresent()) {
            return ResponseEntity.badRequest().body("Clínica não encontrada com o ID fornecido.");
        }
        Medico medico = medicoOpt.get();
        Clinica clinica = clinicaOpt.get();
        horario.setMedico(medico);
        horario.setClinica(clinica);
        horarioRepository.save(horario);
        return ResponseEntity.ok("Horário adicionado com sucesso.");
    }

    @PostMapping("/{clinicaId}/medicos/cpf/{medicoCpf}")
    public ResponseEntity<String> addMedicoToClinica(
            @PathVariable Long clinicaId,
            @PathVariable String medicoCpf) {
        Optional<Medico> medicoOpt = medicoRepository.findByCpf(medicoCpf);
        if (!medicoOpt.isPresent()) {
            return ResponseEntity.badRequest().body("Médico não encontrado com o CPF fornecido.");
        }
        Optional<Clinica> clinicaOpt = clinicaRepository.findById(clinicaId);
        if (!clinicaOpt.isPresent()) {
            return ResponseEntity.badRequest().body("Clínica não encontrada com o ID fornecido.");
        }
        Medico medico = medicoOpt.get();
        Clinica clinica = clinicaOpt.get();

        if (medico.getClinicas().contains(clinica)) {
            return ResponseEntity.badRequest().body("Médico já está associado a esta clínica.");
        }

        medico.getClinicas().add(clinica);
        medicoRepository.save(medico);
        return ResponseEntity.ok("Médico adicionado à clínica com sucesso.");
    }

    @DeleteMapping("/{clinicaId}/medicos/cpf/{medicoCpf}")
    public ResponseEntity<String> removeMedicoFromClinica(
            @PathVariable Long clinicaId,
            @PathVariable String medicoCpf) {
        Optional<Medico> medicoOpt = medicoRepository.findByCpf(medicoCpf);
        if (!medicoOpt.isPresent()) {
            return ResponseEntity.badRequest().body("Médico não encontrado com o CPF fornecido.");
        }
        Optional<Clinica> clinicaOpt = clinicaRepository.findById(clinicaId);
        if (!clinicaOpt.isPresent()) {
            return ResponseEntity.badRequest().body("Clínica não encontrada com o ID fornecido.");
        }
        Medico medico = medicoOpt.get();
        Clinica clinica = clinicaOpt.get();

        if (!medico.getClinicas().contains(clinica)) {
            return ResponseEntity.badRequest().body("Médico não está associado a esta clínica.");
        }

        medico.getClinicas().remove(clinica);
        medicoRepository.save(medico);
        return ResponseEntity.ok("Médico removido da clínica com sucesso.");
    }

    @PatchMapping("/{id}/medicos/{id_medico}")
    public ResponseEntity<Clinica> updateMedicoInClinica(@PathVariable Long id, @PathVariable Long id_medico, @RequestBody Medico medicoDetails) {
        Optional<Clinica> clinica = clinicaRepository.findById(id);
        Optional<Medico> medico = medicoRepository.findById(id_medico);
        if (clinica.isPresent() && medico.isPresent()) {
            Medico updatedMedico = medico.get();
            if (medicoDetails.getNome() != null) {
                updatedMedico.setNome(medicoDetails.getNome());
            }
            if (medicoDetails.getEspecialidade() != null) {
                updatedMedico.setEspecialidade(medicoDetails.getEspecialidade());
            }
            medicoRepository.save(updatedMedico);
            return ResponseEntity.ok(clinica.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}/medicos/{id_medico}")
    public ResponseEntity<Void> removeMedicoFromClinica(@PathVariable Long id, @PathVariable Long id_medico) {
        Optional<Clinica> clinica = clinicaRepository.findById(id);
        Optional<Medico> medico = medicoRepository.findById(id_medico);
        if (clinica.isPresent() && medico.isPresent()) {
            Clinica updatedClinica = clinica.get();
            Set<Medico> medicos = updatedClinica.getMedicos().stream()
                .filter(m -> !m.getId().equals(id_medico))
                .collect(Collectors.toSet());
            updatedClinica.setMedicos(medicos);
            clinicaRepository.save(updatedClinica);
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{id}/especialidades")
    public ResponseEntity<List<String>> getEspecialidadesByClinicaId(@PathVariable Long id) {
        Optional<Clinica> clinica = clinicaRepository.findById(id);
        if (clinica.isPresent()) {
            List<String> especialidades = medicoRepository.findDistinctEspecialidadesByClinicaId(id);
            return ResponseEntity.ok(especialidades);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{id}/especialidades/{especialidade}/medicos")
    public ResponseEntity<List<Medico>> getMedicosByEspecialidade(
            @PathVariable Long id, 
            @PathVariable String especialidade) {
        Optional<Clinica> clinica = clinicaRepository.findById(id);
        if (clinica.isPresent()) {
            List<Medico> medicos = medicoRepository.findByClinicaIdAndEspecialidade(id, especialidade);
            return ResponseEntity.ok(medicos);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/cadastrar/lote")
    public ResponseEntity<List<Clinica>> createClinicasLote(@RequestBody List<Clinica> clinicas) {
        List<Clinica> savedClinicas = clinicaRepository.saveAll(clinicas);
        return ResponseEntity.ok(savedClinicas);
    }
}
