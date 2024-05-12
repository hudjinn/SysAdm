package br.com.sysadm.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import br.com.sysadm.model.Clinica;
import br.com.sysadm.model.Medico;
import br.com.sysadm.repository.ClinicaRepository;
import br.com.sysadm.repository.MedicoRepository;

@RestController
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping("/clinicas")
public class ClinicaController {

    @Autowired
    private ClinicaRepository clinicaRepository;

    @Autowired
    private MedicoRepository medicoRepository;

    @GetMapping
    public List<Clinica> listarTodas() {
        return clinicaRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Clinica> buscarClinica(@PathVariable Long id) {
        Optional<Clinica> clinica = clinicaRepository.findById(id);
        return clinica.map(ResponseEntity::ok)
                      .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/cadastrar")
    public ResponseEntity<Clinica> cadastrarClinica(@RequestBody Clinica clinica) {
        Clinica novaClinica = clinicaRepository.save(clinica);
        return new ResponseEntity<>(novaClinica, HttpStatus.CREATED);
    }
    @PostMapping("/cadastrar/lote")
    public ResponseEntity<?> cadastrarLoteClinicas(@RequestBody List<Clinica> clinicas) {
        List<Clinica> clinicasCadastradas = new ArrayList<>();
        List<String> erros = new ArrayList<>();
    
        for (Clinica clinica : clinicas) {
            try {
                // Tenta salvar a clínica e adiciona na lista de cadastradas
                Clinica clinicaSalva = clinicaRepository.save(clinica);
                clinicasCadastradas.add(clinicaSalva);
            } catch (Exception e) {
                // Adiciona uma mensagem de erro indicando o problema com a clínica específica
                erros.add("Falha ao cadastrar clínica com nome: " + clinica.getNome() + "; Erro: " + e.getMessage());
            }
        }
    
        // Cria um objeto de resposta contendo as clínicas cadastradas e qualquer erro que tenha ocorrido
        Map<String, Object> resposta = new HashMap<String, Object>();
        resposta.put("clinicasCadastradas", clinicasCadastradas);
        resposta.put("erros", erros);
    
        // Verifica se houve erros e retorna a resposta apropriada
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
    @PostMapping("/{clinicaId}/medicos/{medicoCpf}")
    public ResponseEntity<?> adicionarMedicoAClinica(@PathVariable Long clinicaId, @PathVariable String medicoCpf) {
        Clinica clinica = clinicaRepository.findById(clinicaId).orElseThrow(() -> new RuntimeException("Clinica não encontrada"));
        Medico medico = medicoRepository.findById(medicoCpf).orElseThrow(() -> new RuntimeException("Medico não encontrado"));
        clinica.getMedicos().add(medico);
        clinicaRepository.save(clinica);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{clinicaId}/medicos/{medicoCpf}")
    public ResponseEntity<?> removerMedicoDaClinica(@PathVariable Long clinicaId, @PathVariable String medicoCpf) {
        Clinica clinica = clinicaRepository.findById(clinicaId).orElseThrow(() -> new RuntimeException("Clinica não encontrada"));
        Medico medico = medicoRepository.findById(medicoCpf).orElseThrow(() -> new RuntimeException("Medico não encontrado"));
        clinica.getMedicos().remove(medico);
        clinicaRepository.save(clinica);
        return ResponseEntity.ok().build();
    }

}
