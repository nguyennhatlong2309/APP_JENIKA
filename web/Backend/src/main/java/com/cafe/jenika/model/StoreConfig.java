package com.cafe.jenika.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "store_config")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StoreConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "shop_name")
    private String shopName;

    @Column(name = "shop_name_pnh")
    private String shopNamePnh;

    @Column(name = "shop_addr")
    private String shopAddr;

    @Column(name = "shop_tel")
    private String shopTel;

    @Column(name = "shop_bank")
    private String shopBank;

    @Column(name = "shop_notes", columnDefinition = "TEXT")
    private String shopNotes;

    @Column(name = "shop_policy", columnDefinition = "TEXT")
    private String shopPolicy;

    @Column(name = "shop_warranty", columnDefinition = "TEXT")
    private String shopWarranty;

    @Column(name = "shop_warranty_limit")
    private String shopWarrantyLimit;
}
