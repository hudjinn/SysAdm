package br.com.sysadm.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import br.com.sysadm.model.Consulta;

@Repository
public interface ConsultaRepository extends JpaRepository<Consulta, Long> {
    List<Consulta> findByPacienteId(Long pacienteId);
    List<Consulta> findByPacienteCpf(String pacienteCpf);
    List<Consulta> findByAgendamentoId(Long agendamentoId);
}

