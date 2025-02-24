package com.taisbri.usuario.infraestructure.repository;


import com.taisbri.usuario.infraestructure.entity.Telefone;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TelefoneRepository extends JpaRepository<Telefone,Long> {

    boolean existsByEmail(String email);
}
