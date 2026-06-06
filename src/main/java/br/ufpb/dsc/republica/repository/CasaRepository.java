package br.ufpb.dsc.republica.repository;

import br.ufpb.dsc.republica.domain.Casa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CasaRepository extends JpaRepository<Casa, Long> {
    
    // Busca todas as casas em que o usuário de id X é morador
    @Query("SELECT c FROM Casa c JOIN c.moradores m WHERE m.usuario.id = :usuarioId")
    List<Casa> findCasasByUsuarioId(@Param("usuarioId") Long usuarioId);
}

