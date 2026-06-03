package com.antoniofaccioli.fishing.ecommerce_backend.entities;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import com.antoniofaccioli.fishing.ecommerce_backend.support.enums.OrderStatus;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "Order")
@Table(name = "orders")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "date_order")
    private LocalDateTime dateCreated;

    @ManyToOne
    @OnDelete(action = OnDeleteAction.CASCADE) //se un utente viene cancellato, cancella anche i suoi ordini
    @JoinColumn(name = "user_id")
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "order_status")
    private OrderStatus orderStatus;

    @Version //questo campo viene utilizzato per la gestione della concorrenza ottimistica. Quando più transazioni tentano di aggiornare lo stesso record contemporaneamente, il sistema utilizza il campo version per rilevare conflitti e prevenire sovrascritture accidentali dei dati. Ogni volta che un record viene aggiornato, il valore del campo version viene incrementato automaticamente. Se una transazione tenta di aggiornare un record con un valore di version obsoleto, viene sollevata un'eccezione, consentendo al sistema di gestire la situazione in modo appropriato (ad esempio, ritentando l'operazione o informando l'utente del conflitto).
    private Long version;

    @Column(name = "total_amount")
    private Double totalAmount; //campo che rappresenta l'importo totale dell'ordine. Viene calcolato sommando il prezzo
                                // di ogni prodotto moltiplicato per la quantita' acquistata.

    @Column(name = "shipping_address")
    private String shippingAddress;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "recipient_name")
    private String recipientName;   //serve per indicare il nome del destinatario dell'ordine, che potrebbe essere
                                    //diverso dal nome dell'utente che effettua l'acquisto. Particolarmente utile quando
                                    //l'utente acquista un prodotto come regalo per qualcun altro, o quando desidera che
                                    //l'ordine venga consegnato a un indirizzo diverso da quello associato al proprio account.

    //relazione uno-a-molti tra Order e OrderProduct, un ordine può contenere più prodotti.
    @OneToMany (mappedBy = "pk.order", cascade = CascadeType.ALL, orphanRemoval = true) //se un ordine viene cancellato, cancella anche i prodotti associati a quell'ordine
    @JsonManagedReference //gestisce la serializzazione JSON e previene problemi di riferimento circolare quando si serializzano oggetti che hanno relazioni bidirezionali. In questo caso, indica che questa parte della relazione è quella "gestita" (managed) e che l'altra parte (in User) sarà quella "indietro" (back).
    @Valid //assicura che quando si crea o aggiorna un ordine, l'utente associato sia valido secondo le regole di validazione definite nella classe User.
    private java.util.List<OrderProduct> orderProducts = new java.util.ArrayList<>(); //lista dei prodotti associati a questo ordine

    @Transient
    public Double getTotalPrice(){
        double sum= 0D;
        List<OrderProduct> orderProducts = getOrderProducts(); //lista dei prodotti associati a questo ordine
        for(OrderProduct op : orderProducts ){
            sum += op.getProductFinalPrice(); //calcola il prezzo totale dell'ordine sommando il prezzo finale di ogni prodotto moltiplicato per la quantità
        }
        return sum;
    }

    @Transient
    public int getNumberOfProductsInCart(){
        return this.orderProducts.size();
    }

}
