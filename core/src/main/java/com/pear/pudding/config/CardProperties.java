package com.pear.pudding.config;

import com.pear.pudding.model.Card;
import com.pear.pudding.model.CardPropList;
import lombok.Getter;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.List;

public class CardProperties{

  @Getter
  public static List<Card> cardProps;

  public static CardPropList loadYml() {
    Yaml yaml = new Yaml();
    InputStream inputStream = CardProperties.class.getClassLoader()
            .getResourceAsStream("cards.yml"); //This assumes that youryamlfile.yaml is on the classpath
    CardPropList obj = yaml.loadAs(inputStream, CardPropList.class);
    obj.getCards().forEach(card -> {
      System.out.println("key: " + card.getHealth() + " value: " + card.getAttack());
    });
    return obj;
  }
}
