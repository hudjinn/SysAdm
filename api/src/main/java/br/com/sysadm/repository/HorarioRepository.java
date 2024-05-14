package br.com.sysadm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import br.com.sysadm.model.Horario;
import br.com.sysadm.model.Clinica;
import br.com.sysadm.model.Medico;

import java.util.List;

@Repository
public interface HorarioRepository extends JpaRepository<Horario, Long> {
    List<Horario> findByClinicaAndMedico(Clinica clinica, Medico medico);
    List<Horario> findByClinicaIdAndMedicoId(Long clinicaId, Long medicoId);

}
