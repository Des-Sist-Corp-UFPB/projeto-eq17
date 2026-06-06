package br.ufpb.dsc.republica.repository;

import br.ufpb.dsc.republica.domain.Morador;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MoradorRepository extends JpaRepository<Morador, Long> {
    List<Morador> findByCasaId(Long casaId);
    Optional<Morador> findByCasaIdAndUsuarioId(Long casaId, Long usuarioId);
    Optional<Morador> findByCasaIdAndUsuarioEmail(Long casaId, String email);
    boolean existsByCasaIdAndUsuarioId(Long casaId, Long usuarioId);
}

