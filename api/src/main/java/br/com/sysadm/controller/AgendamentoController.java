package br.com.sysadm.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import br.com.sysadm.model.Agendamento;
import br.com.sysadm.repository.AgendamentoRepository;

@RestController
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping("/agendamentos")
public class AgendamentoController {

    @Autowired
    private AgendamentoRepository agendamentoRepository;

    @GetMapping
    public List<Agendamento> listarTodos() {
        return agendamentoRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Agendamento> buscarAgendamento(@PathVariable Long id) {
        Optional<Agendamento> agendamento = agendamentoRepository.findById(id);
        return agendamento.map(ResponseEntity::ok)
                          .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Agendamento> criarAgendamento(@RequestBody Agendamento agendamento) {
        Agendamento novoAgendamento = agendamentoRepository.save(agendamento);
        return new ResponseEntity<>(novoAgendamento, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Agendamento> atualizarAgendamento(@PathVariable Long id, @RequestBody Agendamento agendamentoAtualizado) {
        return agendamentoRepository.findById(id)
            .map(agendamento -> {
                agendamento.setDataHoraAgendamento(agendamentoAtualizado.getDataHoraAgendamento());
                agendamento.setStatus(agendamentoAtualizado.getStatus());
                agendamentoRepository.save(agendamento);
                return new ResponseEntity<>(agendamento, HttpStatus.OK);
            })
            .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletarAgendamento(@PathVariable Long id) {
        if (agendamentoRepository.existsById(id)) {
            agendamentoRepository.deleteById(id);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }

    // Método adicional para buscar agendamentos por status
    @GetMapping("/status/{status}")
    public ResponseEntity<List<Agendamento>> buscarPorStatus(@PathVariable String status) {
        List<Agendamento> agendamentos = agendamentoRepository.findByStatus(status);
        if (agendamentos.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(agendamentos);
    }
}
