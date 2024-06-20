package com.jonggae.yakku.customers.entity;

import com.jonggae.yakku.address.Address;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "customers")

public class Customer {

    @Id
    @Column(name = "customer_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "customer_name", unique = true, nullable = false)
    private String customerName;

    @Column(name = "customer_password", nullable = false)
    private String password;

    @Column(name = "customer_email", unique = true, nullable = false)
    private String email;

    @Column(name = "activated_account") // 이후 softdelete에도 쓸수 있을듯, 활성화된 계정
    private boolean enabled;

    // todo: Customer 엔티티의 생성 일자는 필요한가?
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "address_id", referencedColumnName = "id")
    private Address address;
}
