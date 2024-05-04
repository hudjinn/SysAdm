package br.com.sysadm.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import br.com.sysadm.model.Clinica;

@Repository
public interface ClinicaRepository extends JpaRepository<Clinica, Long> {
    // Buscar clínicas por nome
    List<Clinica> findByNome(String nome);

    // Buscar clínicas por endereço
    List<Clinica> findByEnderecoContaining(String endereco);
}
