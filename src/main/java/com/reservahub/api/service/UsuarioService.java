package com.reservahub.api.service;

import com.reservahub.api.dto.UsuarioCadastro;
import com.reservahub.api.model.Usuario;
import com.reservahub.api.repository.UsuarioRepository;
import com.reservahub.api.security.UsuarioAutenticado;
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
            PasswordEncoder passwordEncoder) {

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

        return usuarioRepository.save(usuario);
    }

    @Override
    public UsuarioAutenticado loadUserByUsername(String email)
            throws UsernameNotFoundException {

        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() ->
                        new UsernameNotFoundException(
                                "Usuário não encontrado."
                        )
                );

        return new UsuarioAutenticado(usuario);
    }
}