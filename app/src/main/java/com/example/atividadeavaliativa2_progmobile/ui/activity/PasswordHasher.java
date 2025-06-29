package com.example.atividadeavaliativa2_progmobile.ui.activity;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

// Classe auxiliar para criar e verificar hashes de senha
public class PasswordHasher {
    public static String hashPassword(String password) {
        try {
            // Usamos SHA-256
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashedPassword = md.digest(password.getBytes());

            // Convertemos o array de bytes para uma representação em String hexadecimal
            StringBuilder sb = new StringBuilder();
            for (byte b : hashedPassword) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();

        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Erro ao gerar hash da senha", e);
        }
    }
}