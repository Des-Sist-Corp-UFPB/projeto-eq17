package br.ufpb.dsc.republica.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controller para tratar o roteamento da SPA (Single Page Application).
 * Qualquer rota que não seja da API, do Actuator, de recursos estáticos ou ping é redirecionada
 * (forwarded) para o index.html, permitindo ao React Router gerenciar as rotas.
 */
@Controller
public class SpaController {

    @GetMapping(value = "/{path:^(?!api|webjars|css|js|assets|favicon\\.ico|actuator|ping|index\\.html).*$}/**")
    public String forward() {
        return "forward:/index.html";
    }
}
