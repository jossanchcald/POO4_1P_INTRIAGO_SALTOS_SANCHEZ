package com.proyectopoo1;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Properties;
import java.util.Scanner;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import io.github.cdimascio.dotenv.Dotenv;


public class Administrador extends Usuario{

    // Variables de instancia
    private String cargo;

    // Constructores
    public Administrador(String cargo, String codUnico, String numCedula, String nombres, String apellidos, String user, String password, String correo){
        super(codUnico, numCedula, nombres, apellidos, user, password, correo);
        this.cargo = cargo;
    }

    // Getters
    public String getCargo(){
        return cargo;
    }

    // Setters
    public void serCargo(String cargo){
        this.cargo = cargo;
    }
    //enviar correo sobrescrito para administrador
    //falta configurar el mensaje
    @Override
    public void enviarCorreo(String correoPE, String codReserva, String motivo, EstadoReserva decision){

        Dotenv dot = Dotenv.load();

        String host = dot.get("MAIL_HOST");
        String port = dot.get("MAIL_PORT");
        String user = dot.get("MAIL_USER");
        String pass = dot.get("MAIL_PASS");

        Properties prop = new Properties();
        prop.put("mail.smtp.host",host);
        prop.put("mail.smtp.port",port);
        prop.put("mail.smtp.auth",true);
        prop.put("mail.smtp.starttls.enable",true);

        Session sesion = Session.getInstance(prop, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication(){
                return new PasswordAuthentication(user, pass);
            }
        });

        try {
            Message message = new MimeMessage(sesion);
            message.setFrom(new InternetAddress(user,"APP DE RESERVAS"));
            //el atributo de la clase que va a enviar el correo debe recibir correo
            message.setRecipients(Message.RecipientType.TO,InternetAddress.parse("kelvintr@espol.edu.ec"));
            if(decision == EstadoReserva.APROBADO){
                message.setSubject("Reserva aprobada");
                message.setText("Se ha aprobado su reserva con código " + codReserva + "."
                                + "\n\nAtentamente,"
                                + "\nDepartamento Administrativo");
                Transport.send(message);
            }else if(decision == EstadoReserva.RECHAZADO){
                message.setSubject("Reserva rechazada");
                message.setText("Se ha rechazado su reserva con código " + codReserva + " por el siguiente motivo: " + motivo
                                + "\n\nAtentamente,"
                                + "\nDepartamento Administrativo");
                Transport.send(message);
            }
            
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void gestionarReserva(ArrayList<Espacio> espacios, ArrayList<Reserva> reservas) {
        Scanner sc = new Scanner(System.in);
        boolean codValido = false;
        String input;
        Reserva reservaSol = null;

        // Valida y retorna el codigo ingresado que pertenece a alguna reserva pendiente hecha
        while (!codValido) {
            System.out.println("Ingresa el código de la reserva: ");
            input = sc.nextLine();

            for (Reserva reserv : reservas) {
                if(reserv.getCodUnico().equals(input) && reserv.getEstadoReserva().equals(EstadoReserva.PENDIENTE)){
                    codValido = true;
                    reservaSol = reserv;
                }
            }

            if(!codValido){
                System.out.println("No existe una reserva con ese código. Intente nuevamente\n");
            }
        }

        System.out.println("\nCod. | Fecha Res. | Tipo Esp. | Nombre esp.  |  Capacidad esp.  | Nombres | Apellidos");
        System.out.println(reservaSol.getCodUnico() + " | " + reservaSol.getFechaReserva() + " | " + reservaSol.getEspacio().getTipoEspacio() + " | " + reservaSol.getEspacio().getNombreEsp() + " | " + reservaSol.getEspacio().getCapacidadEsp() + " | " + reservaSol.getUser().getNombres() + " | " + reservaSol.getUser().getApellidos());

        System.out.println("\nOpciones:");
        System.out.println("1. Aprobar");
        System.out.println("2. Rechazar");
        System.out.print("\nIngrese la opcion a realizar (1,2): ");
        int decs = sc.nextInt();
        sc.nextLine();
        
        while (decs < 1 || decs > 2) {
            System.out.print("\nOpción invalida. Ingresa una opción válida: ");
            decs = sc.nextInt();
            sc.nextLine();
        }

        switch (decs) {
            case 1:
                this.enviarCorreo(reservaSol.getUser().getCorreo(), reservaSol.getCodUnico(), null, EstadoReserva.APROBADO);
                reservaSol.setEstadoReserva(EstadoReserva.APROBADO);
                break;
        
            case 2:
                System.out.println("Indique el motivo del rechazo: ");
                String motivo = sc.nextLine();
                this.enviarCorreo(reservaSol.getUser().getCorreo(), reservaSol.getCodUnico(), motivo, EstadoReserva.RECHAZADO);
                reservaSol.setEstadoReserva(EstadoReserva.RECHAZADO);
                break;

        }
        sc.close();
    }
}
