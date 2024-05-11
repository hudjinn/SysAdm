package br.com.sysadm.model;

import java.time.LocalDate;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;

import java.util.Set;
import java.util.HashSet;

@Entity
public class Medico {

    @Id
    private String cpf;

    @Column(nullable = false)
    private String nome;

    @Column(nullable = false)
    private String especialidade;

    @Column(nullable = false)
    private LocalDate dataCad;

    @ManyToMany(mappedBy = "medicos")
    private Set<Clinica> clinicas = new HashSet<>();

    @OneToMany(mappedBy = "medico", fetch = FetchType.LAZY)
    private Set<HorarioAtendimento> horarios = new HashSet<>();

    @PrePersist
    protected void onCreate() {
        dataCad = LocalDate.now();
    }

    public Medico() {
    }

    public Medico(String cpf, String nome, String especialidade) {
        this.cpf = cpf;
        this.nome = nome;
        this.especialidade = especialidade;
    }
    public String getCpf() {
        return cpf;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getEspecialidade() {
        return especialidade;
    }

    public void setEspecialidade(String especialidade) {
        this.especialidade = especialidade;
    }

    public LocalDate getDataCad() {
        return dataCad;
    }

    public void setDataCad(LocalDate dataCad) {
        this.dataCad = dataCad;
    }

    public Set<Clinica> getClinicas() {
        return clinicas;
    }


    public void setClinicas(Set<Clinica> clinicas) {
        this.clinicas = clinicas;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((cpf == null) ? 0 : cpf.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;
        Medico other = (Medico) obj;
        if (cpf == null) {
            return other.cpf == null;
        } else return cpf.equals(other.cpf);
    }

    @Override
    public String toString() {
        return "Medico{" +
            "cpf='" + cpf + '\'' +
            ", nome='" + nome + '\'' +
            ", especialidade='" + especialidade + '\'' +
            ", dataCad=" + dataCad +
            '}';
    }
}
