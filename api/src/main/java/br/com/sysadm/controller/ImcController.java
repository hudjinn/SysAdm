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
import br.com.sysadm.model.Imc;
import br.com.sysadm.repository.ImcRepository;

@RestController
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping("/imcs")
public class ImcController {

    @Autowired
    private ImcRepository imcRepository;

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

    @PostMapping("/cadastrar")
    public ResponseEntity<?> cadastrarImc(@RequestBody Imc imc) {
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
