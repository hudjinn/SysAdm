package br.com.sysadm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import br.com.sysadm.model.Clinica;

@Repository
public interface ClinicaRepository extends JpaRepository<Clinica, Long> {
}
