package br.com.sysadm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import br.com.sysadm.model.HorarioAtendimento;
import java.time.DayOfWeek;
import java.util.List;

@Repository
public interface HorarioAtendimentoRepository extends JpaRepository<HorarioAtendimento, Long> {
    // Encontrar horários por médico
    List<HorarioAtendimento> findByMedico_Id(String medicoId);

    // Encontrar horários por clínica
    List<HorarioAtendimento> findByClinica_Id(Long clinicaId);

    // Encontrar horários por dia da semana
    List<HorarioAtendimento> findByDiaSemana(DayOfWeek diaSemana);

    // Encontrar horários por médico e dia da semana
    List<HorarioAtendimento> findByMedico_IdAndDiaSemana(String medicoId, DayOfWeek diaSemana);

    // Encontrar horários por clínica e dia da semana
    List<HorarioAtendimento> findByClinica_IdAndDiaSemana(Long clinicaId, DayOfWeek diaSemana);
}
