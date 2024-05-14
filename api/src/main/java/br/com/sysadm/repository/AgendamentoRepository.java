package br.com.sysadm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import br.com.sysadm.model.Agendamento;
import br.com.sysadm.model.Clinica;
import br.com.sysadm.model.Medico;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AgendamentoRepository extends JpaRepository<Agendamento, Long> {
    List<Agendamento> findByClinicaAndMedicoAndDataHoraAgendamentoBetween(Clinica clinica, Medico medico, LocalDateTime start, LocalDateTime end);
        
}
