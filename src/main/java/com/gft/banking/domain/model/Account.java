package com.gft.banking.domain.model;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity // le dice a JPA que esta clase es una tabla en BD
@Table(name = "accounts") // nombre de la tabla en BD
@Data // Lombok genera getters, setters, toString automáticamente
@Builder // Lombok permite construir objetos así: Account.builder().ownerName("Pau").build()
//  Lombok genera constructores, JPA los necesita:
@NoArgsConstructor
@AllArgsConstructor

public class Account {

    @Id //  esta es la clave primaria
    @GeneratedValue(strategy = GenerationType.IDENTITY) // el id se genera automáticamente
    private Long id;

    @Column(nullable = false) //  configuración de la columna
    private String ownerName;

    @Column(nullable = false)
    private BigDecimal balance;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist //  llama automáticamente a método y rellena createdAt con la fecha y hora actual.
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}