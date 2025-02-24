package com.taisbri.usuario.infraestructure.repository;


import com.taisbri.usuario.infraestructure.entity.Endereco;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository

public interface EnderecoRepository extends JpaRepository<Endereco,Long> {

    boolean existsByEmail(String email);
}
