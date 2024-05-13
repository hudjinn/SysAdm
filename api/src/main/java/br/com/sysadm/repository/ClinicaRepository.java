package br.com.sysadm.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import br.com.sysadm.model.Clinica;

@Repository
public interface ClinicaRepository extends JpaRepository<Clinica, Long> {
    // Buscar clínicas por nome
    List<Clinica> findByNome(String nome);

    // Buscar clínicas por endereço
    List<Clinica> findByEnderecoContaining(String endereco);

    @EntityGraph(attributePaths = {"medicos", "medicos.horarios"})
    Optional<Clinica> findClinicaWithMedicosById(Long id);
}

