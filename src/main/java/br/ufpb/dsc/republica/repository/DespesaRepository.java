package br.ufpb.dsc.republica.repository;

import br.ufpb.dsc.republica.domain.Despesa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DespesaRepository extends JpaRepository<Despesa, Long> {
    List<Despesa> findByCasaIdAndExcluidoFalseOrderByVencimentoAsc(Long casaId);
    List<Despesa> findByResponsavelIdAndExcluidoFalse(Long responsavelId);
}

