package main.util;

import main.models.Usuario;

/**
 * Session manager singleton that holds the currently authenticated user.
 */
public class SessionManager {

    private static volatile Usuario usuarioAtual;

    private SessionManager() {}

    public static Usuario getUsuarioAtual() {
        return usuarioAtual;
    }

    public static void setUsuarioAtual(Usuario usuario) {
        usuarioAtual = usuario;
    }

    public static void clear() {
        usuarioAtual = null;
    }
}
