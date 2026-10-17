
package com.reservahub.api.service;

import com.reservahub.api.model.Usuario;
import com.reservahub.api.repository.UsuarioRepository;
import com.reservahub.api.security.UsuarioAutenticado;
import com.reservahub.api.dto.UsuarioCadastro;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UsuarioService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Usuario cadastrar(UsuarioCadastro usuarioCadastro) {
        if (usuarioRepository.existsByEmail(usuarioCadastro.email())) {
            throw new IllegalArgumentException("E-mail já cadastrado.");
        }

        Usuario usuario = new Usuario(
                usuarioCadastro.nome(),
                usuarioCadastro.email(),
                passwordEncoder.encode(usuarioCadastro.senha())
        );

        usuario.setCep(usuarioCadastro.cep());
        usuario.setRua(usuarioCadastro.street());
        usuario.setBairro(usuarioCadastro.neighborhood());
        usuario.setCidade(usuarioCadastro.city());
        usuario.setUf(usuarioCadastro.state());

        return usuarioRepository.save(usuario);
    }

    public Usuario buscarPorEmail(String email) {
        return usuarioRepository.findByEmailIgnoreCase(email.trim())
                .orElseThrow(() ->
                        new UsernameNotFoundException(
                                "Usuário não encontrado."
                        )
                );
    }

    @Override
    public UsuarioAutenticado loadUserByUsername(String email) {
        Usuario usuario = buscarPorEmail(email);
        return new UsuarioAutenticado(usuario);
    }
}
