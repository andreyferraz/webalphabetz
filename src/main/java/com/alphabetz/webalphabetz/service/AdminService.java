package com.alphabetz.webalphabetz.service;

import java.util.Optional;
import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alphabetz.webalphabetz.model.Admin;
import com.alphabetz.webalphabetz.repository.AdminRepository;
import com.alphabetz.webalphabetz.utils.ValidationUtils;

@Service
@Transactional
public class AdminService {

     private static final String PASSWORD_FIELD = "password";
    private static final String USERNAME_FIELD = "username";
    private static final String ADMIN_NOT_FOUND = "Admin não encontrado.";

    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminService(AdminRepository adminRepository, PasswordEncoder passwordEncoder) {
        this.adminRepository = adminRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Admin create(Admin admin) {
        ValidationUtils.validarCampoStringObrigatorio(admin.getUsername(), USERNAME_FIELD);
        ValidationUtils.validarCampoStringObrigatorio(admin.getPassword(), PASSWORD_FIELD);

        if (admin.getId() == null) {
            admin.setId(UUID.randomUUID());
        }
        admin.setPassword(passwordEncoder.encode(admin.getPassword()));
        admin.setNew(true);
        return adminRepository.save(admin);
    }

    public Admin update(UUID id, Admin admin) {
        ValidationUtils.validarCampoObrigatorio(id, "id");
        Admin existing = adminRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException(ADMIN_NOT_FOUND));

        ValidationUtils.validarCampoStringObrigatorio(admin.getUsername(), USERNAME_FIELD);
        ValidationUtils.validarCampoStringObrigatorio(admin.getPassword(), PASSWORD_FIELD);

        existing.setUsername(admin.getUsername());
        existing.setPassword(passwordEncoder.encode(admin.getPassword()));
        existing.setNew(false);
        return adminRepository.save(existing);
    }

    public Optional<Admin> findById(UUID id) {
        ValidationUtils.validarCampoObrigatorio(id, "id");
        return adminRepository.findById(id);
    }

    public Optional<Admin> findByUsername(String username) {
        ValidationUtils.validarCampoStringObrigatorio(username, USERNAME_FIELD);
        return adminRepository.findByUsername(username);
    }

    public Admin changePassword(UUID id, String newPassword) {
        ValidationUtils.validarCampoObrigatorio(id, "id");
        ValidationUtils.validarCampoStringObrigatorio(newPassword, PASSWORD_FIELD);

        Admin existing = adminRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException(ADMIN_NOT_FOUND));

        existing.setPassword(passwordEncoder.encode(newPassword));
        existing.setNew(false);
        return adminRepository.save(existing);
    }

    public Admin changePasswordByUsername(String username, String currentPassword, String newPassword,
            String confirmPassword) {
        ValidationUtils.validarCampoStringObrigatorio(username, USERNAME_FIELD);
        ValidationUtils.validarCampoStringObrigatorio(currentPassword, "currentPassword");
        ValidationUtils.validarCampoStringObrigatorio(newPassword, PASSWORD_FIELD);
        ValidationUtils.validarCampoStringObrigatorio(confirmPassword, "confirmPassword");

        Admin existing = adminRepository.findByUsername(username)
            .orElseThrow(() -> new IllegalArgumentException(ADMIN_NOT_FOUND));

        if (!passwordEncoder.matches(currentPassword, existing.getPassword())) {
            throw new IllegalArgumentException("Senha atual inválida.");
        }

        if (!newPassword.equals(confirmPassword)) {
            throw new IllegalArgumentException("A confirmação da nova senha não coincide.");
        }

        validateStrongPassword(newPassword);

        if (passwordEncoder.matches(newPassword, existing.getPassword())) {
            throw new IllegalArgumentException("A nova senha deve ser diferente da senha atual.");
        }

        existing.setPassword(passwordEncoder.encode(newPassword));
        existing.setNew(false);
        return adminRepository.save(existing);
    }

    private void validateStrongPassword(String password) {
        if (password.length() < 8 || password.length() > 72) {
            throw new IllegalArgumentException("A nova senha deve ter entre 8 e 72 caracteres.");
        }

        boolean hasNumber = password.chars().anyMatch(Character::isDigit);
        boolean hasLowercase = password.chars().anyMatch(Character::isLowerCase);
        boolean hasUppercase = password.chars().anyMatch(Character::isUpperCase);
        boolean hasSymbol = password.chars().anyMatch(character -> !Character.isLetterOrDigit(character));

        if (!hasNumber || !hasLowercase || !hasUppercase || !hasSymbol) {
            throw new IllegalArgumentException(
                    "A nova senha deve conter número, letras maiúsculas e minúsculas e símbolo.");
        }
    }

}
