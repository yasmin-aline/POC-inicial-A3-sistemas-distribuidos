package br.com.pixseguro.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Locale;
import org.springframework.stereotype.Component;

@Component
public class ChaveHasher {

    public String hash(String chave) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(md.digest(normalizar(chave).getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    /** E-mail so em minusculas; CPF, CNPJ, telefone e chave aleatoria sem pontuacao nem espacos. */
    static String normalizar(String chave) {
        String c = chave.trim().toLowerCase(Locale.ROOT);
        return c.contains("@") ? c : c.replaceAll("[\\s.\\-/()+]", "");
    }
}
