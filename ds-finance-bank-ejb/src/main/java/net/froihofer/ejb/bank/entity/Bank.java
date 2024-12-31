package net.froihofer.ejb.bank.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;
import java.math.BigDecimal;

@Entity
@Table(name = "BANK")
public class Bank implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "ID")
    private Long id;

    @Max(1_000_000_000)
    @Column(name = "AVAILABLE_VOLUME", nullable = false)
    private BigDecimal availableVolume;

    public Bank() {
    }

    public Bank(BigDecimal availableVolume) {
        this.availableVolume = availableVolume;
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public BigDecimal getAvailableVolume() {
        return availableVolume;
    }

    public void setAvailableVolume(BigDecimal availableVolume) {
        this.availableVolume = availableVolume;
    }
}
