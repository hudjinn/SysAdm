package br.com.sysadm.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import br.com.sysadm.model.HorarioAtendimento;
import br.com.sysadm.repository.HorarioAtendimentoRepository;

@RestController
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping("/horarios")
public class HorarioAtendimentoController {

    @Autowired
    private HorarioAtendimentoRepository horarioAtendimentoRepository;

    @GetMapping
    public List<HorarioAtendimento> listarTodos() {
        return horarioAtendimentoRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<HorarioAtendimento> buscarHorarioAtendimento(@PathVariable Long id) {
        Optional<HorarioAtendimento> horarioAtendimento = horarioAtendimentoRepository.findById(id);
        return horarioAtendimento.map(ResponseEntity::ok)
                                 .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<HorarioAtendimento> criarHorarioAtendimento(@RequestBody HorarioAtendimento horarioAtendimento) {
        HorarioAtendimento novoHorario = horarioAtendimentoRepository.save(horarioAtendimento);
        return new ResponseEntity<>(novoHorario, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<HorarioAtendimento> atualizarHorarioAtendimento(@PathVariable Long id, @RequestBody HorarioAtendimento horarioAtualizado) {
        return horarioAtendimentoRepository.findById(id)
            .map(horario -> {
                horario.setMedico(horarioAtualizado.getMedico());
                horario.setClinica(horarioAtualizado.getClinica());
                horario.setDiaSemana(horarioAtualizado.getDiaSemana());
                horario.setHoraInicio(horarioAtualizado.getHoraInicio());
                horario.setHoraFim(horarioAtualizado.getHoraFim());
                horarioAtendimentoRepository.save(horario);
                return new ResponseEntity<>(horario, HttpStatus.OK);
            })
            .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletarHorarioAtendimento(@PathVariable Long id) {
        if (horarioAtendimentoRepository.existsById(id)) {
            horarioAtendimentoRepository.deleteById(id);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }
}
