package br.com.sysadm.repository;

import br.com.sysadm.model.Agendamento;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;

public interface AgendamentoRepository extends JpaRepository<Agendamento, Long> {
    List<Agendamento> findByClinicaIdAndMedicoIdAndDataHoraAgendamentoBetween(
            Long clinicaId, Long medicoId, LocalDateTime start, LocalDateTime end);
}
