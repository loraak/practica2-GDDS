package com.practica2.practica2pom;

public class CredencialesInvalidasException extends RuntimeException {
    public CredencialesInvalidasException() { super("Correo o contraseña incorrectos"); }
}