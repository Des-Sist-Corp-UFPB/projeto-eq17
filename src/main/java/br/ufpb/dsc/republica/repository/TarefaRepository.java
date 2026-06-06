package br.ufpb.dsc.republica.repository;

import br.ufpb.dsc.republica.domain.Tarefa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TarefaRepository extends JpaRepository<Tarefa, Long> {
    List<Tarefa> findByCasaIdOrderByCriadoEmDesc(Long casaId);
    List<Tarefa> findByResponsavelId(Long responsavelId);
}

