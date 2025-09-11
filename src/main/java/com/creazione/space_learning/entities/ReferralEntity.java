package com.creazione.space_learning.entities;

import jakarta.persistence.*;
import lombok.*;

import java.util.Date;

@Entity
@Table(name = "referrals")
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class ReferralEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "user_id")
    private Long userId;
    @Column(name = "referrer_id")
    private Long referrerId;
    @Column(name = "last_update")
    private Date lastUpdate;
    @Column(name = "time_create")
    private Date timeCreate;
}
