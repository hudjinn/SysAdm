package br.com.sysadm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import br.com.sysadm.model.Agendamento;
import java.util.List;
import java.time.LocalDateTime;

@Repository
public interface AgendamentoRepository extends JpaRepository<Agendamento, Long> {
    // Encontrar agendamentos por status
    List<Agendamento> findByStatus(String status);

    // Encontrar agendamentos por médico
    List<Agendamento> findByHorarioAtendimento_Medico_Id(String medicoId);

    // Encontrar agendamentos por clínica
    List<Agendamento> findByHorarioAtendimento_Clinica_Id(Long clinicaId);

    // Encontrar agendamentos em um intervalo de datas
    List<Agendamento> findByDataHoraAgendamentoBetween(LocalDateTime start, LocalDateTime end);

    // Encontrar agendamentos para um médico em um intervalo de datas
    List<Agendamento> findByHorarioAtendimento_Medico_IdAndDataHoraAgendamentoBetween(String medicoId, LocalDateTime start, LocalDateTime end);
    
    // Encotrar agendamentos pelo nome do Paciente
    List<Agendamento> findByNomePacienteContaining(String nomePaciente);
    
    //Encontrar agendamentos pelo email do Paciente
    List<Agendamento> findByEmailPaciente(String emailPaciente);
}
