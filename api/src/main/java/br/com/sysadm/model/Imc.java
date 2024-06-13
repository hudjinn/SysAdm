package br.com.sysadm.model;

import java.time.LocalDate;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;

@Entity
public class Imc {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Double peso;

    @Column(nullable = false)
    private Double altura;

    @Column(nullable = false)
    private LocalDate dataImc;

    @Column(nullable = false)
    private Double resultado;

    @Column(nullable = false)
    private String classificacao;

    @ManyToOne(cascade = CascadeType.ALL)
    private Paciente paciente;

    public Imc() {
    }

    public Imc(Double peso, Double altura, Paciente paciente) {
        this.peso = peso;
        this.altura = altura;
        this.paciente = paciente;
        this.dataImc = LocalDate.now();
        calcularImc();
    }

    public void calcularImc() {
        this.resultado = this.peso / (this.altura * this.altura);
        this.classificacao = classificarImc(this.resultado);
    }

    public String classificarImc(Double imc) {
        if (imc < 18.5) {
            return "Abaixo do peso";
        } else if (imc < 24.9) {
            return "Peso normal";
        } else if (imc < 29.9) {
            return "Sobrepeso";
        } else if (imc < 34.9) {
            return "Obesidade grau 1";
        } else if (imc < 39.9) {
            return "Obesidade grau 2";
        } else {
            return "Obesidade grau 3";
        }
    }

    @PrePersist
    protected void onCreate() {
        dataImc = LocalDate.now();
        calcularImc();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Double getPeso() {
        return peso;
    }

    public void setPeso(Double peso) {
        this.peso = peso;
    }

    public Double getAltura() {
        return altura;
    }

    public void setAltura(Double altura) {
        this.altura = altura;
    }

    public LocalDate getDataImc() {
        return dataImc;
    }

    public void setDataImc(LocalDate dataImc) {
        this.dataImc = dataImc;
    }

    public Double getResultado() {
        return resultado;
    }

    public String getClassificacao() {
        return classificacao;
    }

    public Paciente getPaciente() {
        return paciente;
    }

    public void setPaciente(Paciente paciente) {
        this.paciente = paciente;
    }
}
