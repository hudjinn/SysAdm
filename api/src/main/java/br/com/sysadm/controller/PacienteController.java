package br.com.sysadm.controller;

import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import br.com.sysadm.model.Paciente;
import br.com.sysadm.repository.PacienteRepository;

@RestController
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping("/pacientes")
public class PacienteController {

    @Autowired
    private PacienteRepository pacienteRepository;

    @GetMapping
    public List<Paciente> listarTodos() {
        return pacienteRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Paciente> obterPorId(@PathVariable Long id) {
        Optional<Paciente> paciente = pacienteRepository.findById(id);
        if (paciente.isPresent()) {
            return ResponseEntity.ok(paciente.get());
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    @GetMapping("/cpf/{cpf}")
    public ResponseEntity<Paciente> obterPorCpf(@PathVariable String cpf) {
        Optional<Paciente> paciente = pacienteRepository.findByCpf(cpf);
        if (paciente.isPresent()) {
            return ResponseEntity.ok(paciente.get());
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    @PostMapping("/cadastrar")
    public ResponseEntity<?> cadastrarPaciente(@RequestBody Paciente paciente) {
        Optional<Paciente> pacienteExistente = pacienteRepository.findByCpf(paciente.getCpf());
        if (pacienteExistente.isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Paciente com o CPF já existe!");
        } else {
            pacienteRepository.save(paciente);
            return ResponseEntity.status(HttpStatus.CREATED).body(paciente);
        }
    }
    @PatchMapping("/atualizar/{id}")
    public ResponseEntity<?> atualizarPaciente(@PathVariable Long id, @RequestBody Paciente pacienteAtualizado) {
        Optional<Paciente> pacienteExistente = pacienteRepository.findById(id);
        if (pacienteExistente.isPresent()) {
            Paciente paciente = pacienteExistente.get();
            if (pacienteAtualizado.getCpf() != null) paciente.setCpf(pacienteAtualizado.getCpf());
            if (pacienteAtualizado.getSexo() != null) paciente.setSexo(pacienteAtualizado.getSexo());
            if (pacienteAtualizado.getDataNasc() != null) paciente.setDataNasc(pacienteAtualizado.getDataNasc());
            if (pacienteAtualizado.getNome() != null) paciente.setNome(pacienteAtualizado.getNome());
            pacienteRepository.save(paciente);
            return ResponseEntity.ok(paciente);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Paciente não encontrado!");
        }
    }
    @DeleteMapping("/deletar/{id}")
    public ResponseEntity<?> deletarPaciente(@PathVariable Long id) {
        Optional<Paciente> paciente = pacienteRepository.findById(id);
        if (paciente.isPresent()) {
            pacienteRepository.deleteById(id);
            return ResponseEntity.ok().body("Paciente deletado com sucesso!");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Paciente não encontrado!");
        }
    }
}
