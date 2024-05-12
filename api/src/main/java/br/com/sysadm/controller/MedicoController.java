package br.com.sysadm.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import br.com.sysadm.model.Medico;
import br.com.sysadm.model.Usuario;
import br.com.sysadm.repository.MedicoRepository;

@RestController
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping("/medicos")
public class MedicoController {

    @Autowired
    private MedicoRepository medicoRepository;

    @GetMapping
    public List<Medico> listarTodos() {
        return medicoRepository.findAll();
    }

    @GetMapping("/especialidade/{especialidade}")
    public ResponseEntity<List<Medico>> listarPorEspecialidade(@PathVariable String especialidade) {
        List<Medico> medicos = medicoRepository.findByEspecialidade(especialidade);
        if (medicos.isEmpty()) {
            return ResponseEntity.noContent().build(); // Retorna 204 No Content se não houver médicos
        }
        return ResponseEntity.ok(medicos); // Retorna 200 OK com a lista de médicos
    }
    

    @PostMapping("/cadastrar")
    public ResponseEntity<?> cadastrarMedico(@RequestBody Medico medico) {
        Optional<Medico> medicoExistente = medicoRepository.findById(medico.getCpf());
        if (medicoExistente.isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Médico com este CPF já cadastrado.");
        }
        Medico novoMedico = medicoRepository.save(medico);
        return ResponseEntity.status(HttpStatus.CREATED).body(novoMedico);
    }
    @PostMapping("/cadastrar/lote")
    public ResponseEntity<?> cadastrarLoteUsuario(@RequestBody List<Medico> medicos) {
        List<Medico> medicosCadastrados = new ArrayList<>();
        List<String> erros = new ArrayList<>();

        for (Medico medico : medicos) {
            // Verifica se o médico já existe pela chave primária
            Optional<Medico> medicoExistente = medicoRepository.findById(medico.getCpf());
            if (medicoExistente.isPresent()) {
                // Adiciona erro na lista de erros
                erros.add("Médico com CPF " + medico.getCpf() + " já cadastrado.");
                continue;  // Continua para o próximo usuário na lista
            }
            
            // Se não existir, salva o novo usuário
            Medico novoMedico = medicoRepository.save(medico);
            medicosCadastrados.add(novoMedico);
        }

        // Cria um objeto de resposta contendo os usuários cadastrados e qualquer erro que tenha ocorrido
        Map<String, Object> resposta = new HashMap<>();
        resposta.put("medicosCadastrados", medicosCadastrados);
        resposta.put("erros", erros);

        // Retorna a lista de usuários cadastrados e os erros
        return ResponseEntity.ok(resposta);
    }

    @PatchMapping("/atualizar/{cpf}")
    public ResponseEntity<?> atualizarMedico(@PathVariable String cpf, @RequestBody Map<String, Object> updates) {
        Optional<Medico> medicoOptional = medicoRepository.findById(cpf);
        if (medicoOptional.isPresent()) {
            Medico medico = medicoOptional.get();
            updates.forEach((atributo, valor) -> {
                switch (atributo) {
                    case "nome":
                        if (valor instanceof String) medico.setNome((String) valor);
                        break;
                    case "especialidade":
                        if (valor instanceof String) medico.setEspecialidade((String) valor);
                        break;
                    // Adicione outros campos conforme necessário
                }
            });
            medicoRepository.save(medico);
            return ResponseEntity.ok(medico);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/remover/{cpf}")
    public ResponseEntity<Void> deletarMedico(@PathVariable String cpf) {
        if (medicoRepository.existsById(cpf)) {
            medicoRepository.deleteById(cpf);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }

}
