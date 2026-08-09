package br.ufpb.dsc.republica.repository;

import br.ufpb.dsc.republica.domain.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByEmail(String email);
    boolean existsByEmail(String email);
    Optional<Usuario> findByTokenConfirmacao(String tokenConfirmacao);
    Optional<Usuario> findByTokenRedefinicaoSenha(String tokenRedefinicaoSenha);
}

