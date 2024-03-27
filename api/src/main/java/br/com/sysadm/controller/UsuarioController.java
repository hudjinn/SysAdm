package br.com.sysadm.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/clientes")
public class UsuarioController {

    @GetMapping
    public String hello() {
        return "Olá, mundo";
    }
}
