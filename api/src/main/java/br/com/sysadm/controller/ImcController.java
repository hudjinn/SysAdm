package br.com.sysadm.controller;

import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import br.com.sysadm.model.Imc;
import br.com.sysadm.model.Paciente;
import br.com.sysadm.repository.ImcRepository;
import br.com.sysadm.repository.PacienteRepository;

@RestController
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping("/imcs")
@Validated
public class ImcController {

    @Autowired
    private ImcRepository imcRepository;

    @Autowired 
    private PacienteRepository pacienteRepository;

    @GetMapping
    public List<Imc> listarTodos() {
        return imcRepository.findAll();
    }

    @GetMapping("/paciente/{pacienteId}")
    public ResponseEntity<List<Imc>> listarPorPaciente(@PathVariable Long pacienteId) {
        List<Imc> imcs = imcRepository.findByPacienteId(pacienteId);
        if (imcs.isEmpty()) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.ok(imcs);
        }
    }

    @GetMapping("/paciente/cpf/{cpf}")
    public ResponseEntity<List<Imc>> listarPorCpf(@PathVariable String cpf) {
        List<Imc> imcs = imcRepository.findByPacienteCpf(cpf);
        if (imcs.isEmpty()) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.ok(imcs);
        }
    }

    @PostMapping("/cadastrar/{pacienteId}")
    public ResponseEntity<?> cadastrarImc(@PathVariable Long pacienteId, @RequestBody Imc imc) {
        if (pacienteId == null) {
            return ResponseEntity.badRequest().body("Paciente ID é obrigatório");
        }
        Optional<Paciente> pacienteOptional = pacienteRepository.findById(pacienteId);
        if (!pacienteOptional.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Paciente não encontrado");
        }
        if (imc.getPeso() == null || imc.getAltura() == null) {
            return ResponseEntity.badRequest().body("Peso e altura são obrigatórios");
        }
        imc.setPaciente(pacienteOptional.get());
        imc.calcularImc(); // Certificar que o cálculo do IMC é feito
        imcRepository.save(imc);
        return ResponseEntity.status(HttpStatus.CREATED).body(imc);
    }

    @PostMapping("/cadastrar/cpf/{cpf}")
    public ResponseEntity<?> cadastrarImcPorCpf(@PathVariable String cpf, @RequestBody Imc imc) {
        if (cpf == null || cpf.isEmpty()) {
            return ResponseEntity.badRequest().body("Paciente CPF é obrigatório");
        }
        Optional<Paciente> pacienteOptional = pacienteRepository.findByCpf(cpf);
        if (!pacienteOptional.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Paciente não encontrado");
        }
        if (imc.getPeso() == null || imc.getAltura() == null) {
            return ResponseEntity.badRequest().body("Peso e altura são obrigatórios");
        }
        imc.setPaciente(pacienteOptional.get());
        imc.calcularImc(); // Certificar que o cálculo do IMC é feito
        imcRepository.save(imc);
        return ResponseEntity.status(HttpStatus.CREATED).body(imc);
    }

    @PatchMapping("/atualizar/{id}")
    public ResponseEntity<?> atualizarParcial(@PathVariable Long id, @RequestBody Imc imcAtualizado) {
        Optional<Imc> imcExistente = imcRepository.findById(id);
        if (imcExistente.isPresent()) {
            Imc imc = imcExistente.get();
            if (imcAtualizado.getPeso() != null) imc.setPeso(imcAtualizado.getPeso());
            if (imcAtualizado.getAltura() != null) imc.setAltura(imcAtualizado.getAltura());
            if (imcAtualizado.getDataImc() != null) imc.setDataImc(imcAtualizado.getDataImc());
            imc.calcularImc();
            imcRepository.save(imc);
            return ResponseEntity.ok(imc);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("IMC não encontrado!");
        }
    }

    @DeleteMapping("/deletar/{id}")
    public ResponseEntity<?> deletarImc(@PathVariable Long id) {
        Optional<Imc> imc = imcRepository.findById(id);
        if (imc.isPresent()) {
            imcRepository.deleteById(id);
            return ResponseEntity.ok().body("IMC deletado com sucesso!");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("IMC não encontrado!");
        }
    }
}
