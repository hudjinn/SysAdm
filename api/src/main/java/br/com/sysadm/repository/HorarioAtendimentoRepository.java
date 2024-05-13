package br.com.sysadm.repository;

import br.com.sysadm.model.Clinica;
import br.com.sysadm.model.HorarioAtendimento;
import br.com.sysadm.model.Medico;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

public interface HorarioAtendimentoRepository extends JpaRepository<HorarioAtendimento, Long> {

    List<HorarioAtendimento> findByMedicoCpf(String medicoCpf);

    List<HorarioAtendimento> findByClinicaId(Long clinicaId);

    List<HorarioAtendimento> findByClinicaIdAndMedicoCpfAndDiaSemana(Long clinicaId, String medicoCpf, DayOfWeek diaSemana);

    List<HorarioAtendimento> findByMedicoCpfAndDiaSemanaAndHoraInicioBetween(String medicoCpf, DayOfWeek diaSemana, LocalTime inicio, LocalTime fim);

    List<HorarioAtendimento> findByMedicoCpfAndDiaSemanaAndHoraInicioLessThanEqualAndHoraFimGreaterThanEqual(
        String medicoCpf, DayOfWeek diaSemana, LocalTime horaInicio, LocalTime horaFim);

    void deleteByMedicoAndClinica(Medico medico, Clinica clinica);

    @Query("SELECT h FROM HorarioAtendimento h JOIN h.medico m WHERE h.clinica.id = :clinicaId AND m.especialidade = :especialidade")
    List<HorarioAtendimento> findHorariosByEspecialidadeInClinica(
        @Param("clinicaId") Long clinicaId, 
        @Param("especialidade") String especialidade);
    

}
