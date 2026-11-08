package repositorio;

import servicio.Agencia;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

// Guarda toda la agencia en un único archivo, así se conservan las referencias entre objetos
public class RepositorioAgenciaArchivo implements RepositorioAgencia {
    private final Path ruta;

    public RepositorioAgenciaArchivo(String ruta) {
        if (ruta == null || ruta.isBlank()) {
            throw new IllegalArgumentException("La ruta del archivo es obligatoria.");
        }
        this.ruta = Paths.get(ruta.strip());
    }

    // dónde queda realmente el archivo (una ruta relativa se cuenta desde la carpeta de ejecución)
    public String getRuta() {
        return ruta.toAbsolutePath().toString();
    }

    @Override
    public Agencia cargar() throws IOException {
        if (!Files.exists(ruta)) {
            return new Agencia();   // primera ejecución: agencia nueva, con las promociones del enunciado
        }
        try (ObjectInputStream entrada = new ObjectInputStream(Files.newInputStream(ruta))) {
            return (Agencia) entrada.readObject();
        } catch (ClassNotFoundException e) {
            throw new IOException("El archivo de datos no corresponde a esta versión del programa.", e);
        }
    }

    // Primero escribe en un archivo temporal y recién cuando terminó bien lo reemplaza por el definitivo.
    // Así, si algo falla a mitad de camino, el archivo anterior queda intacto.
    @Override
    public void guardar(Agencia agencia) throws IOException {
        if (agencia == null) {
            throw new IllegalArgumentException("La agencia es obligatoria.");
        }
        Files.createDirectories(ruta.toAbsolutePath().getParent());
        Path temporal = Paths.get(ruta.toString() + ".tmp");
        try {
            try (ObjectOutputStream salida = new ObjectOutputStream(Files.newOutputStream(temporal))) {
                salida.writeObject(agencia);
            }
            Files.move(temporal, ruta, StandardCopyOption.REPLACE_EXISTING);
        } finally {
            Files.deleteIfExists(temporal);   // si falló, no queda basura; si salió bien, ya no existe
        }
    }
}