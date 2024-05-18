package br.com.sysadm.controller;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.sysadm.dto.UsuarioListDTO;
import br.com.sysadm.model.Usuario;
import br.com.sysadm.repository.UsuarioRepository;

@RestController
@CrossOrigin(origins = "*", allowedHeaders = "*") 
@RequestMapping("/usuarios")

public class UsuarioController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @GetMapping
    public List<UsuarioListDTO> listarTodos() {
        List<Usuario> usuarios = usuarioRepository.findAll();
        return usuarios.stream()
                       .map(usuario -> new UsuarioListDTO(usuario.getCpf(), usuario.getNome(), usuario.getEmail(), usuario.getDataNasc(), usuario.getDataCad(), usuario.isAtivo()))
                       .collect(Collectors.toList());
    }


    @PostMapping("/cadastrar")
    public ResponseEntity<?> cadastrarUsuario(@RequestBody Usuario usuario) {
        // Verifica se o usuário já existe pela chave primária
        Optional<Usuario> usuarioExistente = usuarioRepository.findByCpf(usuario.getCpf());
        if (usuarioExistente.isPresent()) {
            // Retorna uma resposta indicando que o usuário já existe
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Usuário com este CPF já cadastrado.");
        }
        
        // Se não existir, salva o novo usuário e retorna a resposta adequada
        Usuario novoUsuario = usuarioRepository.save(usuario);
        return ResponseEntity.status(HttpStatus.CREATED).body(novoUsuario);
    }
    @PostMapping("/cadastrar/lote")
    public ResponseEntity<?> cadastrarLoteUsuario(@RequestBody List<Usuario> usuarios) {
        List<Usuario> usuariosCadastrados = new ArrayList<>();
        List<String> erros = new ArrayList<>();

        for (Usuario usuario : usuarios) {
            // Verifica se o usuário já existe pela chave primária
            Optional<Usuario> usuarioExistente = usuarioRepository.findByCpf(usuario.getCpf());
            if (usuarioExistente.isPresent()) {
                // Adiciona erro na lista de erros
                erros.add("Usuário com CPF " + usuario.getCpf() + " já cadastrado.");
                continue;  // Continua para o próximo usuário na lista
            }
            
            // Se não existir, salva o novo usuário
            Usuario novoUsuario = usuarioRepository.save(usuario);
            usuariosCadastrados.add(novoUsuario);
        }

        // Cria um objeto de resposta contendo os usuários cadastrados e qualquer erro que tenha ocorrido
        Map<String, Object> resposta = new HashMap<>();
        resposta.put("usuariosCadastrados", usuariosCadastrados);
        resposta.put("erros", erros);

        // Retorna a lista de usuários cadastrados e os erros
        return ResponseEntity.ok(resposta);
    }

    @PatchMapping("/atualizar/{cpf}")
    public ResponseEntity<?> atualizarUsuario(@PathVariable String cpf, @RequestBody Map<String, Object> updates) {
        Optional<Usuario> usuarioOptional = usuarioRepository.findByCpf(cpf);
        if (usuarioOptional.isPresent()) {
            Usuario usuario = usuarioOptional.get();

            // Atualiza atributos com base no que está presente no JSON
            updates.forEach((atributo, valor) -> {
                switch (atributo) {
                    case "nome":
                        if (valor instanceof String) usuario.setNome((String) valor);
                        break;
                    case "email":
                        if (valor instanceof String) usuario.setEmail((String) valor);
                        break;
                    case "senha":
                        if (valor instanceof String) usuario.setSenha((String) valor);
                        break;
                    case "dataNasc":
                        if (valor instanceof String) // Aqui você deve converter o String para LocalDate
                            usuario.setDataNasc(LocalDate.parse((String) valor));
                        break;
                    case "ativo":
                        if (valor instanceof Boolean) 
                            usuario.setAtivo((Boolean) valor);
                        break;
                }
            });

            usuarioRepository.save(usuario);
            return ResponseEntity.ok(usuario);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    @DeleteMapping("/remover/{cpf}")
    public ResponseEntity<Void> deletarUsuario(@PathVariable String cpf) {
        if (usuarioRepository.existsById(cpf)) {
            usuarioRepository.deleteById(cpf);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }
    @PostMapping("/login")
    public ResponseEntity<UsuarioListDTO> login(@RequestBody Usuario usuario) {
        Optional<Usuario> usuarioEncontrado = usuarioRepository.findByEmail(usuario.getEmail());
    
        if(usuarioEncontrado.isPresent() && usuarioEncontrado.get().getSenha().equals(usuario.getSenha())) {
            Usuario usuarioAtual = usuarioEncontrado.get();
        
            // Criar e preencher um DTO do usuário
            UsuarioListDTO usuarioDTO = new UsuarioListDTO();
            usuarioDTO.setCpf(usuarioAtual.getCpf());
            usuarioDTO.setNome(usuarioAtual.getNome());
            usuarioDTO.setEmail(usuarioAtual.getEmail());
            usuarioDTO.setDataNasc(usuarioAtual.getDataNasc());
            usuarioDTO.setDataCad(usuarioAtual.getDataCad());
            return ResponseEntity.ok(usuarioDTO);
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }
    @PostMapping("/recuperar-senha")
    public ResponseEntity<?> recuperarSenha(@RequestBody Usuario usuarioRequisicao) {
        // Supondo que 'dataNasc' na classe Usuario seja do tipo String ou você ajuste conforme necessário
        Optional<Usuario> usuario = usuarioRepository.findByEmailAndCpfAndDataNasc(
                usuarioRequisicao.getEmail(), usuarioRequisicao.getCpf(), usuarioRequisicao.getDataNasc());

        if (usuario.isPresent()) {
            // Lógica para enviar e-mail de recuperação de senha aqui
            return ResponseEntity.ok().body("");
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("");
        }
    }

}
