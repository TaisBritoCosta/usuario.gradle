package com.taisbri.usuario.business;

import com.taisbri.usuario.business.converter.UsuarioConverter;
import com.taisbri.usuario.business.dto.EnderecoDTO;
import com.taisbri.usuario.business.dto.TelefoneDTO;
import com.taisbri.usuario.business.dto.UsuarioDTO;
import com.taisbri.usuario.infraestructure.entity.Endereco;
import com.taisbri.usuario.infraestructure.entity.Telefone;
import com.taisbri.usuario.infraestructure.entity.Usuario;
import com.taisbri.usuario.infraestructure.exceptions.ConflictException;
import com.taisbri.usuario.infraestructure.exceptions.ResourceNotFoundException;
import com.taisbri.usuario.infraestructure.repository.EnderecoRepository;
import com.taisbri.usuario.infraestructure.repository.TelefoneRepository;
import com.taisbri.usuario.infraestructure.repository.UsuarioRepository;
import com.taisbri.usuario.infraestructure.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final UsuarioConverter usuarioConverter;
    public final PasswordEncoder passwordEncoder;
    public final JwtUtil jwtUtil;
    public final EnderecoRepository enderecoRepository;
    public final TelefoneRepository telefoneRepository;



    public UsuarioDTO salvaUsuario(UsuarioDTO usuarioDTO) {
        emailExiste(usuarioDTO.getEmail());
        usuarioDTO.setSenha(passwordEncoder.encode(usuarioDTO.getSenha()));

        // Converte o DTO para entidade
        Usuario usuario = usuarioConverter.paraUsuario(usuarioDTO);

        // Aqui é o ponto onde você faz o set nos endereços
        if (usuario.getEnderecos() != null) {
            usuario.getEnderecos().forEach(endereco -> endereco.setUsuario(usuario));
        }

        if (usuario.getTelefones() != null) {
            usuario.getTelefones().forEach(telefone -> telefone.setUsuario(usuario));
        }

        // Salva e retorna o DTO convertido
        return usuarioConverter.paraUsuarioDTO(usuarioRepository.save(usuario));
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

    public UsuarioDTO buscarUsuarioPorEmail(String email){
        try {
            return usuarioConverter.paraUsuarioDTO(
                    usuarioRepository.findByEmail(email)
                            .orElseThrow(
                    () -> new ResourceNotFoundException("Email não encontrado " + email)
                                    )
                            );
        } catch (ResourceNotFoundException e){
                throw new ResourceNotFoundException("Email não encontrado" + email);

        }
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

    public EnderecoDTO atualizaEndereco(Long idEndereco, EnderecoDTO enderecoDTO){
        Endereco entity = enderecoRepository.findById(idEndereco).orElseThrow(() ->
            new ResourceNotFoundException("Id não encontrado " + idEndereco)
        );

        Endereco endereco = usuarioConverter.updateEndereco(enderecoDTO, entity);

        return  usuarioConverter.paraEnderecoDTO(enderecoRepository.save(endereco));

    }

    public TelefoneDTO atualizaTelefone(Long idTelefone, TelefoneDTO dto){

        Telefone entity = telefoneRepository.findById(idTelefone).orElseThrow(() ->
                new ResourceNotFoundException("Id não encontrado " + idTelefone)
        );

        Telefone telefone = usuarioConverter.updateTelefone(dto, entity);

        return  usuarioConverter.paraTelefonesDTO(telefoneRepository.save(telefone));

    }

}

