package org.cine.modelo;

public class Cliente extends Persona {


    public Cliente() {
        super();
    }

    public Cliente(Integer id, String nombre, String apellido, String dni, String email, String claveAcceso) {
        super(id, nombre, apellido, dni, email, claveAcceso);
    }



    @Override
    public String toString() {  //podemos usar el super.toString() para llamar al metodo toString de la clase padre y asi no repetir codigo.
        return "Cliente: " + super.toString() ;

    }
}
