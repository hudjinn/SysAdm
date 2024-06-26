package br.com.sysadm.repository;

import java.time.LocalDate;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import br.com.sysadm.model.Usuario;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, String>{
    Optional<Usuario> findByCpf(String cpf);
    Optional<Usuario> findByEmail(String email);
    Optional<Usuario> findByEmailAndCpfAndDataNasc(String email, String cpf, LocalDate dataNasc);

}


