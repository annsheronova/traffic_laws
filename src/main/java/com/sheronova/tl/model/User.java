package com.sheronova.tl.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "tl_users")
public class User {
    @Id
    @Column(name = "id")
    private Integer id;

    @Column(name = "created_date")
    private LocalDateTime createdDate;

    @Column(name = "last_payment")
    private LocalDateTime lastPayment;

    @Column(name = "tariff")
    @Enumerated(EnumType.STRING)
    private TariffType tariff;
}
