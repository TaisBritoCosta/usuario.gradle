package com.taisbri.usuario.infraestructure.repository;

import com.taisbri.usuario.infraestructure.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario,Long> {

    boolean existsByEmail(String email);

    //optional é uma classe do java util para evitar o retorno de informações nullas
    //Ao inves de retornar um null não quebre, ele tarta de uma forma melhro o retorno quando isso acontece

    Optional<Usuario> findByEmail(String email);
}
