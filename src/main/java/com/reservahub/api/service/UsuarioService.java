package com.reservahub.api.service;

import com.reservahub.api.dto.UsuarioCadastro;
import com.reservahub.api.model.Usuario;
import com.reservahub.api.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public void cadastrar(UsuarioCadastro usuarioCadastro) {
        if (usuarioRepository.existsByEmail(usuarioCadastro.email())) {
            throw new IllegalArgumentException("E-mail já cadastrado");
        }

        Usuario usuario = new Usuario();
        usuario.setNome(usuarioCadastro.nome());
        usuario.setEmail(usuarioCadastro.email());
        usuario.setSenha(passwordEncoder.encode(usuarioCadastro.senha()));

        usuarioRepository.save(usuario);
    }
}