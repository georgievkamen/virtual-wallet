package com.team9.virtualwallet.services.contracts;

import com.team9.virtualwallet.models.Card;
import com.team9.virtualwallet.models.User;

import java.util.List;

public interface CardService {
    List<Card> getAll(User user);

    Card getById(User user, int id);

    void create(Card card);

    void update(User user, Card card);

    //TODO HANDLE SQL EXCEPTIONS
    void delete(User userExecuting, int id);
}
