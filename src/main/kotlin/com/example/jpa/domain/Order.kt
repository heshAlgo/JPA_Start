package com.example.jpa.domain

import java.time.LocalDateTime
import javax.persistence.*

/**
 * @author Rasung Ki
 */
@Entity
@Table(name = "orders")
data class Order(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id")
    private val id: Long,

//    @Column(name = "member_id")
//    private val memberId: Long,

    @ManyToOne
    @JoinColumn(name = "member_id")
    private val member: Member,

    @OneToMany(mappedBy = "order")
    private val orderItems: MutableList<OrderItem>,

    private val orderDate: LocalDateTime,

    @Enumerated(EnumType.STRING)
    private val orderStatus: OrderStatus,
) {
    fun addOrderItem(orderItem: OrderItem) {
        this.orderItems.add(orderItem)
        orderItem.copy(order = this)
    }
}
