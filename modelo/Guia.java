package modelo;

import java.util.ArrayList;
import java.util.List;

import excepcion.ReglaNegocioException;
import modelo.enums.CategoriaTour;

public class Guia extends Personal {
    private final List<String> idiomas;
    private final List<CategoriaTour> categoriasHabilitadas;
    private double honorarioPorDia;

    // recibe un idioma y una categoría para que el guía siempre tenga al menos uno de cada
    public Guia(int legajo, String nombre, String documento, String telefono, double honorarioPorDia, String idioma, CategoriaTour categoria) {
        super(legajo, nombre, documento, telefono);
        this.idiomas = new ArrayList<>();
        this.categoriasHabilitadas = new ArrayList<>();
        setHonorarioPorDia(honorarioPorDia);
        agregarIdioma(idioma);
        agregarCategoria(categoria);
    }

    public List<String> getIdiomas() {
        return new ArrayList<>(idiomas);
    }
    public List<CategoriaTour> getCategoriasHabilitadas() {
        return new ArrayList<>(categoriasHabilitadas);
    }
    public double getHonorarioPorDia() {
        return honorarioPorDia;
    }

    public void setHonorarioPorDia(double honorarioPorDia) {
        if (!Double.isFinite(honorarioPorDia) || honorarioPorDia <= 0) {
            throw new IllegalArgumentException("El honorario por día debe ser mayor a cero.");
        }
        this.honorarioPorDia = honorarioPorDia;
    }

    // idiomas
    public void agregarIdioma(String idioma) {
        if (idioma == null || idioma.isBlank()) {
            throw new IllegalArgumentException("El idioma no puede estar vacío.");
        }
        String limpio = idioma.strip();
        if (!tieneIdioma(limpio)) {
            idiomas.add(limpio);
        }
    }

    public void quitarIdioma(String idioma) throws ReglaNegocioException {
        if (idioma == null || idioma.isBlank()) {
            throw new IllegalArgumentException("El idioma no puede estar vacío.");
        }
        String limpio = idioma.strip();
        if (!tieneIdioma(limpio)) {
            throw new ReglaNegocioException("El guía no maneja ese idioma.");
        }
        if (idiomas.size() == 1) {
            throw new ReglaNegocioException("El guía debe conservar al menos un idioma.");
        }
        // lo busco ignorando mayúsculas para sacar el correcto
        for (String actual : idiomas) {
            if (actual.equalsIgnoreCase(limpio)) {
                idiomas.remove(actual);
                return;
            }
        }
    }

    public boolean tieneIdioma(String idioma) {
        for (String actual : idiomas) {
            if (actual.equalsIgnoreCase(idioma)) {
                return true;
            }
        }
        return false;
    }

    // categorias habilitadas
    public void agregarCategoria(CategoriaTour categoria) {
        if (categoria == null) {
            throw new IllegalArgumentException("La categoría no puede ser nula.");
        }
        if (!categoriasHabilitadas.contains(categoria)) {
            categoriasHabilitadas.add(categoria);
        }
    }

    // el controlador verifica antes que no tenga salidas futuras de esa categoría
    public void quitarCategoria(CategoriaTour categoria) throws ReglaNegocioException {
        if (categoria == null) {
            throw new IllegalArgumentException("La categoría no puede ser nula.");
        }
        if (!categoriasHabilitadas.contains(categoria)) {
            throw new ReglaNegocioException("El guía no está habilitado para esa categoría.");
        }
        if (categoriasHabilitadas.size() == 1) {
            throw new ReglaNegocioException("El guía debe estar habilitado al menos para una categoría.");
        }
        categoriasHabilitadas.remove(categoria);
    }

    public boolean estaHabilitado(CategoriaTour categoria) {
        return categoriasHabilitadas.contains(categoria);
    }

    public double calcularHonorarios(int dias) {
        if (dias <= 0) {
            throw new IllegalArgumentException("Los días deben ser mayores a cero.");
        }
        return dias * honorarioPorDia;
    }
}
