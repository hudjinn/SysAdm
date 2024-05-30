package br.com.sysadm.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import br.com.sysadm.model.Imc;

@Repository
public interface ImcRepository extends JpaRepository<Imc, Long> {
    List<Imc> findByPacienteId(Long pacienteId);
    List<Imc> findByPacienteCpf(String cpf);
}