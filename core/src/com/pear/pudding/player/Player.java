package com.pear.pudding.player;


import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.pear.pudding.card.*;
import com.pear.pudding.enums.Location;
import com.pear.pudding.model.*;
import lombok.Getter;
import lombok.Setter;

import static com.pear.pudding.model.Constants.*;

@Getter
@Setter
public class Player {
    private Integer mana = 0;
    private Integer health = 30;
    private boolean isMyTurn;
    private Hand hand;
    private DrawDeck drawDeck;
    private Board board;
    private Vector2 healthPosition;
    BitmapFont font = new BitmapFont();

    public Player(boolean defaultTurn) {
        this.isMyTurn = defaultTurn;
    }

    public void initializeDeck(Stage stage) {
        for(Slot s: this.drawDeck.getSlots()){
            var card = new Ghost(s.getX(), s.getY(), CARD_WIDTH, CARD_HEIGHT, Color.BLACK, this);
            card.setCurrentLocation(Location.DRAW);
            s.setCard(card);
            stage.addActor(card);
        }
    }

    public boolean hasEnoughMana(Card card){
        if(this.getMana() >= card.getCost()){
            return true;
        }else {
            return false;
        }
    }

    public void draw(Batch batch){
        font.getData().markupEnabled = true;
        if(getHealth() <= 10){
            font.draw(batch, "[RED]" + this.getHealth(), this.getHealthPosition().x,this.getHealthPosition().y);
        }else{
            font.draw(batch, "[GREEN]" + this.getHealth(), this.getHealthPosition().x,this.getHealthPosition().y);
        }
        font.draw(batch, "[WHITE]" + this.getMana(), this.getHealthPosition().x + BUFFER*2,this.getHealthPosition().y);

        getDrawDeck().draw(batch);
        getBoard().draw(batch);
        getHand().draw(batch);
    }

    public void drawCard(){
        for(int i = this.drawDeck.getSlots().size()-1; i >= 0; i--){
            if(this.drawDeck.getSlots().get(i).getCard() != null){
                var card = this.drawDeck.getSlots().get(i).getCard();
                this.drawDeck.removeCard(card);
                var slot = this.hand.firstEmptySlot();
                if(slot.getCard() == null){
                    slot.setCard(card);
                    card.move(slot.getX(), slot.getY());
                    card.setFaceUp(true);
                    card.setPreviousPosition(new Bound(slot.getX(), slot.getY(), slot.getWidth(), slot.getHeight()));
                    card.setCurrentLocation(Location.HAND);
                }else{
                    card.setCurrentLocation(Location.DISCARD);
                }
                break;
            }
        }
    }
}
