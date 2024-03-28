package br.com.sysadm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import br.com.sysadm.model.Usuario;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, String>{

}
