package com.pear.pudding.model;

import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
public class CardPropList {

  public CardPropList(List<Card> cards) {
    this.cards = cards;
  }

  List<Card> cards;

}
