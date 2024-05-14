package br.com.sysadm.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import br.com.sysadm.model.Medico;

@Repository
public interface MedicoRepository extends JpaRepository<Medico, Long> {
    Optional<Medico> findByCpf(String cpf);
    Optional<Medico> findByEspecialidade(String especialidade);
    
    // Método para encontrar todas as especialidades únicas de médicos em uma clínica específica
    @Query("SELECT DISTINCT m.especialidade FROM Medico m JOIN m.clinicas c WHERE c.id = :clinicaId")
    List<String> findDistinctEspecialidadesByClinicaId(Long clinicaId);

    @Query("SELECT m FROM Medico m JOIN m.clinicas c WHERE c.id = :clinicaId AND m.especialidade = :especialidade")
    List<Medico> findByClinicaIdAndEspecialidade(Long clinicaId, String especialidade);

}
