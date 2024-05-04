package br.com.sysadm.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import br.com.sysadm.model.Clinica;
import br.com.sysadm.repository.ClinicaRepository;

@RestController
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping("/clinicas")
public class ClinicaController {

    @Autowired
    private ClinicaRepository clinicaRepository;

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

    @PostMapping
    public ResponseEntity<Clinica> cadastrarClinica(@RequestBody Clinica clinica) {
        Clinica novaClinica = clinicaRepository.save(clinica);
        return new ResponseEntity<>(novaClinica, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
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

    @DeleteMapping("/{id}")
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
}
