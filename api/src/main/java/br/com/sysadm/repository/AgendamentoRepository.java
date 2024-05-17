package br.com.sysadm.repository;

import br.com.sysadm.model.Agendamento;
import br.com.sysadm.model.Medico;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public interface AgendamentoRepository extends JpaRepository<Agendamento, Long> {
    boolean existsByMedicoAndDataAgendamentoAndHoraAgendamento(Medico medico, LocalDate dataAgendamento, LocalTime horaAgendamento);
    List<Agendamento> findByClinicaIdAndMedicoId(Long clinicaId, Long medicoId);
    List<Agendamento> findByClinicaIdAndMedicoIdAndDataAgendamento(Long clinicaId, Long medicoId, LocalDate dataAgendamento);
    Optional<Agendamento> findByMedicoAndDataAgendamentoAndHoraAgendamento(Medico medico, LocalDate dataAgendamento, LocalTime horaAgendamento);

}


