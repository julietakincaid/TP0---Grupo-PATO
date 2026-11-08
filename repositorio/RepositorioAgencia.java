package repositorio;

import servicio.Agencia;

import java.io.IOException;

public interface RepositorioAgencia {
    Agencia cargar() throws IOException;
    void guardar(Agencia agencia) throws IOException;
}