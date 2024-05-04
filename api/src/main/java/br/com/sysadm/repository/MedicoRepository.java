package br.com.sysadm.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import br.com.sysadm.model.Medico;

@Repository
public interface MedicoRepository extends JpaRepository<Medico, String> {
    Optional<Medico> findByCpf(String cpf); 
    List<Medico> findByEspecialidade(String especialidade); 
}
