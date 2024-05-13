package br.com.sysadm.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Column;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.JoinTable;
import jakarta.persistence.JoinColumn;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import java.util.HashSet;

@Entity
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class Clinica {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nome;

    @Column(nullable = false, length = 200)
    private String endereco;

    @ManyToMany
    @JoinTable(
        name = "clinica_medico",
        joinColumns = @JoinColumn(name = "clinica_id"), 
        inverseJoinColumns = @JoinColumn(name = "medico_cpf") 
    )
    private Set<Medico> medicos = new HashSet<>();

    @OneToMany(mappedBy = "clinica")
    private Set<MedicoClinicaHorario> medicoHorarios = new HashSet<>();

    public Set<Medico> getMedicos() {
        return medicos;
    }

    public void setMedicos(Set<Medico> medicos) {
        this.medicos = medicos;
    }

    public void addMedico(Medico medico) {
        this.medicos.add(medico);
        medico.getClinicas().add(this);
    }

    public void removeMedico(Medico medico) {
        this.medicos.remove(medico);
        medico.getClinicas().remove(this);
    }

    public Clinica() {
    }

    public Clinica(String nome, String endereco) {
        this.nome = nome;
        this.endereco = endereco;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getEndereco() {
        return endereco;
    }

    public void setEndereco(String endereco) {
        this.endereco = endereco;
    }
    
}
