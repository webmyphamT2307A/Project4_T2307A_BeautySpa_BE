package org.aptech.backendmypham.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "appointments")
public class Appointment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "appointment_id", nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.EAGER)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "service_id")
    private org.aptech.backendmypham.models.Service service;

    @ManyToOne(fetch = FetchType.EAGER)
    @OnDelete(action = OnDeleteAction.SET_NULL)
    @JoinColumn(name = "user_id")
    private org.aptech.backendmypham.models.User user;

    @ManyToOne(fetch = FetchType.EAGER)
    @OnDelete(action = OnDeleteAction.SET_NULL)
    @JoinColumn(name = "customer_id")
    private org.aptech.backendmypham.models.Customer customer;
    @ManyToOne
    @JoinColumn(name = "slot_id", nullable = false)
    private Timeslots timeSlot;

    @Column(name = "appointment_date", nullable = false)
    private Instant appointmentDate;

    @Column(name = "end_time", nullable = false)
    private Instant endTime;

    @ColumnDefault("'pending'")
    @Lob
    @Column(name = "status")
    private String status;

    @Column(name = "slot", nullable = false, length = 50)
    private String slot;

    @Lob
    @Column(name = "notes")
    private String notes;

    @Column(name = "phone_number", nullable = false, length = 15)
    private String phoneNumber;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "branch_id", nullable = false)
    private org.aptech.backendmypham.models.Branch branch;

    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @ColumnDefault("(now())")
    @Column(name = "created_at")
    private Instant createdAt;

    @ColumnDefault("(now())")
    @Column(name = "updated_at")
    private Instant updatedAt;

    @ColumnDefault("1")
    @Column(name = "is_active")
    private Boolean isActive;

}