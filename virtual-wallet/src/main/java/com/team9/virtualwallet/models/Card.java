package com.team9.virtualwallet.models;

import com.fasterxml.jackson.annotation.JsonFormat;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Entity
@Table(name = "cards")
public class Card {

    @Id
    @Column(name = "card_id")
    private int id;

    @Column(name = "card_number")
    private String cardNumber;

    @JsonFormat(pattern = "MM/yy")
    @Column(name = "expiration_date")
    private LocalDate expirationDate;

    @Column(name = "card_holder")
    private String cardHolder;

    @Column(name = "cvv")
    private String cvv;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "deleted")
    private boolean isDeleted;

    public Card() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public LocalDate getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(LocalDate expirationDate) {
        this.expirationDate = expirationDate;
    }

    public String getCardHolder() {
        return cardHolder;
    }

    public void setCardHolder(String cardHolder) {
        this.cardHolder = cardHolder;
    }

    public String getMaskedCardNumber() {
        StringBuilder maskedNumber = new StringBuilder();
        String mask = "xxxx-xxxx-xxxx-####";
        int index = 0;
        for (int i = 0; i < mask.length(); i++) {
            char c = mask.charAt(i);
            if (c == '#') {
                maskedNumber.append(cardNumber.charAt(index));
                index++;
            } else if (c == 'x') {
                maskedNumber.append(c);
                index++;
            } else {
                maskedNumber.append(c);
            }
        }
        return maskedNumber.toString();
    }

    public String getExpirationDateFormatted() {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("MM.yyyy");
        return dtf.format(expirationDate);
    }

    public String getExpirationDateFormattedCVV() {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("MM/yy");
        return dtf.format(expirationDate);
    }

    public String getCvv() {
        return cvv;
    }

    public void setCvv(String cvv) {
        this.cvv = cvv;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public boolean isDeleted() {
        return isDeleted;
    }

    public void setDeleted(boolean deleted) {
        isDeleted = deleted;
    }
}
