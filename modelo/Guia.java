package modelo;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import modelo.enums.CategoriaTour;

public class Guia extends Personal {
    private List<String> idiomas;
    private List<CategoriaTour> categoriasHabilitadas;
    private double honorarioPorDia;

    public Guia(int legajo, String nombre, String documento, String telefono, double honorarioPorDia) {
        super(legajo, nombre, documento, telefono);
        this.idiomas = new ArrayList<>();
        this.categoriasHabilitadas = new ArrayList<>();
        setHonorarioPorDia(honorarioPorDia);
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
        if (honorarioPorDia <= 0) {
            throw new IllegalArgumentException("El honorario por día debe ser mayor a cero.");
        }
        this.honorarioPorDia = honorarioPorDia;
    }

    public void agregarIdioma(String idioma) {
        if (!idiomas.contains(idioma)) {
            idiomas.add(idioma);
        }
    }

    public void agregarCategoria(CategoriaTour categoria) {
        if (!categoriasHabilitadas.contains(categoria)) {
            categoriasHabilitadas.add(categoria);
        }
    }

    public boolean estaHabilitado(CategoriaTour categoria) {
        return categoriasHabilitadas.contains(categoria);
    }

    public double calcularHonorarios(Salida salida) {
        int dias = salida.getTour().getDuracionDias();
        return dias * honorarioPorDia;
    }

    // Verificar disponibilidad
    // Pendiente: requiere conocer las salidas asignadas.
}
