package br.com.sysadm.controller;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import br.com.sysadm.dto.MedicoComHorariosDTO;
import br.com.sysadm.model.Clinica;
import br.com.sysadm.model.HorarioAtendimento;
import br.com.sysadm.model.Medico;
import br.com.sysadm.repository.ClinicaRepository;
import br.com.sysadm.repository.HorarioAtendimentoRepository;
import br.com.sysadm.repository.MedicoRepository;
import jakarta.transaction.Transactional;

@RestController
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping("/clinicas")
public class ClinicaController {

    @Autowired
    private ClinicaRepository clinicaRepository;

    @Autowired
    private MedicoRepository medicoRepository;

    @Autowired
    private HorarioAtendimentoRepository horarioAtendimentoRepository;

    @GetMapping
    public List<Clinica> listarTodas() {
        return clinicaRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Clinica> buscarClinicaComMedicosEHorarios(@PathVariable Long id) {
        return clinicaRepository.findClinicaWithMedicosById(id)
            .map(clinica -> ResponseEntity.ok(clinica))
            .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Cadastro de clínica sem médicos
    @PostMapping("/cadastrar")
    public ResponseEntity<?> cadastrarClinica(@RequestBody Clinica clinica) {
        Clinica novaClinica = clinicaRepository.save(clinica);
        return ResponseEntity.status(HttpStatus.CREATED).body(novaClinica);
    }

    @PostMapping("/cadastrar/lote")
    public ResponseEntity<?> cadastrarLoteClinicas(@RequestBody List<Clinica> clinicas) {
        List<Clinica> clinicasCadastradas = new ArrayList<>();
        List<String> erros = new ArrayList<>();

        for (Clinica clinica : clinicas) {
            try {
                Clinica clinicaSalva = clinicaRepository.save(clinica);
                clinicasCadastradas.add(clinicaSalva);
            } catch (DataIntegrityViolationException e) {
                erros.add("Falha ao cadastrar clínica com nome: " + clinica.getNome() + "; Erro: integridade de dados.");
            } catch (Exception e) {
                erros.add("Erro desconhecido ao cadastrar clínica com nome: " + clinica.getNome());
            }
        }

        Map<String, Object> resposta = new HashMap<>();
        resposta.put("clinicasCadastradas", clinicasCadastradas);
        resposta.put("erros", erros);

        if (erros.isEmpty()) {
            return ResponseEntity.status(HttpStatus.CREATED).body(resposta);
        } else {
            return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT).body(resposta);
        }
    }   

    @PutMapping("/atualizar/{id}")
    public ResponseEntity<Clinica> atualizarClinica(@PathVariable Long id, @RequestBody Clinica clinicaAtualizada) {
        return clinicaRepository.findById(id)
            .map(clinica -> {
                clinica.setNome(clinicaAtualizada.getNome());
                clinica.setEndereco(clinicaAtualizada.getEndereco());
                clinicaRepository.save(clinica);
                return new ResponseEntity<>(clinica, HttpStatus.OK);
            })
            .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/remover/{id}")
    public ResponseEntity<Void> deletarClinica(@PathVariable Long id) {
        if (clinicaRepository.existsById(id)) {
            clinicaRepository.deleteById(id);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }

    // Método para buscar clínicas por nome (adicional)
    @GetMapping("/nome/{nome}")
    public ResponseEntity<List<Clinica>> buscarPorNome(@PathVariable String nome) {
        List<Clinica> clinicas = clinicaRepository.findByNome(nome);
        return ResponseEntity.ok(clinicas);  // Sempre retorna 200 OK, mesmo para lista vazia
    }
    // Adicionar médico e horários a uma clínica
    @PostMapping("/{clinicaId}/medicos")
    @Transactional
    public ResponseEntity<?> adicionarMedicoEHorariosAClinica(@PathVariable Long clinicaId, @RequestBody MedicoComHorariosDTO medicoHorarios) {
        try {
            Optional<Clinica> clinicaOpt = clinicaRepository.findById(clinicaId);
            Optional<Medico> medicoOpt = medicoRepository.findById(medicoHorarios.getMedicoCpf());
            
            if (!clinicaOpt.isPresent() || !medicoOpt.isPresent()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Clínica ou Médico não encontrado.");
            }

            Clinica clinica = clinicaOpt.get();
            Medico medico = medicoOpt.get();

            // Processa os horários recebidos no DTO
            medicoHorarios.getHorarios().forEach((dia, intervalos) -> {
                DayOfWeek dayOfWeek = DayOfWeek.valueOf(dia.toUpperCase());
                LocalTime inicio = LocalTime.parse(intervalos[0]);
                LocalTime fim = LocalTime.parse(intervalos[1]);

                // Cria um horário de atendimento e o associa com o médico e a clínica
                HorarioAtendimento novoHorario = new HorarioAtendimento(medico, clinica, dayOfWeek, inicio, fim);
                horarioAtendimentoRepository.save(novoHorario);
            });

            clinica.getMedicos().add(medico);  // Adiciona o médico à clínica
            clinicaRepository.save(clinica);  // Salva a clínica com o novo médico e horários

            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro ao processar a requisição: " + e.getMessage());
        }
    }
    @DeleteMapping("/{clinicaId}/medicos/{medicoCpf}")
    public ResponseEntity<?> removerMedicoDaClinica(@PathVariable Long clinicaId, @PathVariable String medicoCpf) {
        Optional<Clinica> clinicaOpt = clinicaRepository.findById(clinicaId);
        Optional<Medico> medicoOpt = medicoRepository.findById(medicoCpf);
    
        if (clinicaOpt.isPresent() && medicoOpt.isPresent()) {
            Clinica clinica = clinicaOpt.get();
            Medico medico = medicoOpt.get();
    
            if (clinica.getMedicos().contains(medico)) {
                clinica.getMedicos().remove(medico); // Remove o médico da clínica
                horarioAtendimentoRepository.deleteByMedicoAndClinica(medico, clinica); // Remove os horários de atendimento associados
                clinicaRepository.save(clinica); // Salva as alterações
    
                return ResponseEntity.ok().build(); // Retorna 200 OK se o médico foi removido com sucesso
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Médico não está nesta clínica.");
            }
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Clínica ou Médico não encontrado.");
        }
    }
}
