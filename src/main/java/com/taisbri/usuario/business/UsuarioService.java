package com.taisbri.usuario.business;

import com.taisbri.usuario.business.converter.UsuarioConverter;
import com.taisbri.usuario.business.dto.UsuarioDTO;
import com.taisbri.usuario.infraestructure.entity.Usuario;
import com.taisbri.usuario.infraestructure.exceptions.ConflictException;
import com.taisbri.usuario.infraestructure.exceptions.ResourceNotFoundException;
import com.taisbri.usuario.infraestructure.repository.UsuarioRepository;
import com.taisbri.usuario.infraestructure.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.config.ConfigDataResourceNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final UsuarioConverter usuarioConverter;
    public final PasswordEncoder passwordEncoder;
    public final JwtUtil jwtUtil;

    public UsuarioDTO salvaUsuario(UsuarioDTO usuarioDTO){
        emailExiste(usuarioDTO.getEmail());
        usuarioDTO.setSenha(passwordEncoder.encode(usuarioDTO.getSenha()));
        Usuario usuario = usuarioConverter.paraUsuario(usuarioDTO);
        return usuarioConverter.paraUsuarioDTO(
                usuarioRepository.save(usuario));
    }
    public void emailExiste(String email){
        try {
            boolean existe = verificaEmailExistente(email);
            if (existe){
                throw new ConflictException("Email já cadast" +
                        "rado" + email);
            }

        } catch (ConflictException e){
            throw  new ConflictException("Email já cadastrado", e.getCause());
        }
    }
    public boolean verificaEmailExistente (String email){
        return usuarioRepository.existsByEmail(email);
    }
    public Usuario buscaUsuarioPorEmail(String email) {
        return usuarioRepository.findByEmail(email).orElseThrow(
                () -> new ResourceNotFoundException("Email não encontrado" + email)
        );
    }
    public Usuario buscarUsuarioPorEmail(String email){
        return usuarioRepository.findByEmail(email).orElseThrow(
                () -> new ResourceNotFoundException("Email não encontrado" + email));

    }

    public void deletaUsuarioPorEmail(String email){
        usuarioRepository.deleteByEmail(email);
    }

    public UsuarioDTO atualizaDadosUsuario(String token, UsuarioDTO dto){
        //Aqui buscamos o email do usuario através do token(tira a obrigatoriedade de passar o email)
        String email = jwtUtil.extrairEmailToken(token.substring(7));

        //Criptografia de senha
        dto.setSenha(dto.getSenha() != null ? passwordEncoder.encode(dto.getSenha()) : null);
        //Busca os dados do usuario no banco de dados
        Usuario usuarioEntity = usuarioRepository.findByEmail(email).orElseThrow(() ->
                new ResourceNotFoundException("Email não localizado"));

        //Mesclou os dados que recebemos na requisição DTO com os dados do banco de dados
        Usuario usuario = usuarioConverter.updateUsuario(dto, usuarioEntity);
        //Colocou criptografia na senha
        usuario.setSenha((passwordEncoder.encode(usuario.getSenha())));
        //Salvou os dados dos usuario convertidos e depois pegou o retorno e converteu para UsuarioDto
        return usuarioConverter.paraUsuarioDTO(usuarioRepository.save(usuario));

    }
}

