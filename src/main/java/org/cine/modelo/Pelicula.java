package org.cine.modelo;

public class Pelicula {
    private Integer idPelicula;
    private String titulo;
    private String genero;
    private int duracionMinutos;
    private String clasificacion;
    private boolean activa;

    public Pelicula() {
    }

    public Pelicula(Integer idPelicula, String titulo, String genero, int duracionMinutos, String clasificacion, boolean activa) {
        this.idPelicula = idPelicula;
        this.titulo = titulo;
        this.genero = genero;
        this.duracionMinutos = duracionMinutos;
        this.clasificacion = clasificacion;
        this.activa = activa;
    }

    public Integer getIdPelicula() {
        return idPelicula;
    }

    public String getTitulo() {
        return titulo;
    }

    public String getGenero() {
        return genero;
    }

    public int getDuracionMinutos() {
        return duracionMinutos;
    }

    public String getClasificacion() {
        return clasificacion;
    }

    public boolean estaActiva() {
        return activa;
    }

    public void setIdPelicula(int idPelicula) {
        this.idPelicula = idPelicula;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public void setActiva(boolean activa) {
        this.activa = activa;
    }

    @Override
    public String toString() {
        return "Pelicula: " + titulo +
                " | Genero: " + genero +
                " | Duracion: " + duracionMinutos + " minutos" +
                " | Clasificacion: " + clasificacion +
                " | Activa: " + activa;
    }

}
