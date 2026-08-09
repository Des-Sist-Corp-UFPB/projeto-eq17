package br.ufpb.dsc.republica.repository;

import br.ufpb.dsc.republica.domain.DespesaRateio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DespesaRateioRepository extends JpaRepository<DespesaRateio, Long> {
    List<DespesaRateio> findByDespesaId(Long despesaId);
    List<DespesaRateio> findByMoradorId(Long moradorId);
}

