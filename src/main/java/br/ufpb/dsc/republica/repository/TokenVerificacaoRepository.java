package br.ufpb.dsc.republica.repository;

import br.ufpb.dsc.republica.domain.TokenVerificacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TokenVerificacaoRepository extends JpaRepository<TokenVerificacao, Long> {
    Optional<TokenVerificacao> findByToken(String token);
    void deleteByUsuarioId(Long usuarioId);
}
