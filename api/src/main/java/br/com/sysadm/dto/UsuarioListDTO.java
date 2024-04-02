package br.com.sysadm.dto;

import java.time.LocalDate;

public class UsuarioListDTO {
    private String cpf;
    private String nome;
    private String email;
    private LocalDate dataNasc;
    private LocalDate dataCad;
    private boolean ativo;

    // Construtor padrão necessário para o framework
    public UsuarioListDTO() {
    }

    // Construtor com todos os campos
    public UsuarioListDTO(String cpf, String nome, String email, LocalDate dataNasc, LocalDate dataCad, boolean ativo) {
        this.cpf = cpf;
        this.nome = nome;
        this.email = email;
        this.dataNasc = dataNasc;
        this.dataCad = dataCad;
        this.ativo = ativo;
    }

    // Getters e Setters
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
    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }
    public LocalDate getDataNasc() {
        return dataNasc;
    }
    public void setDataNasc(LocalDate dataNasc) {
        this.dataNasc = dataNasc;
    }
    public LocalDate getDataCad() {
        return dataCad;
    }
    public void setDataCad(LocalDate dataCad) {
        this.dataCad = dataCad;
    }
    public boolean isAtivo() {
        return ativo;
    }
    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }
}
