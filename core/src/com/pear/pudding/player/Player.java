package com.pear.pudding.player;


import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.pear.pudding.card.*;
import com.pear.pudding.enums.Location;
import com.pear.pudding.model.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Collections;

import static com.pear.pudding.model.Constants.*;

@Getter
@Setter
public class Player {
    private Integer totalMana = 0;
    private Integer currentMana = 0;
    private Integer health = 30;
    private boolean isMyTurn;
    private Hand hand;
    private DrawDeck drawDeck;
    private DiscardPile discardPile;
    private Board board;
    private Vector2 healthPosition;
    BitmapFont font = new BitmapFont();

    public Player(boolean defaultTurn) {
        this.isMyTurn = defaultTurn;
    }

    public void initializeDeck(Stage stage) {
        int i = 0;
        Card card;
        for(Slot s: this.drawDeck.getSlots()){
            card = switch (i) {
                case 0, 3 -> new Ghost(s.getX(), s.getY(), CARD_WIDTH, CARD_HEIGHT, Color.BLACK, this);
                case 1, 4 -> new Skeleton(s.getX(), s.getY(), CARD_WIDTH, CARD_HEIGHT, Color.BLACK, this);
                case 2, 5 -> new Zombie(s.getX(), s.getY(), CARD_WIDTH, CARD_HEIGHT, Color.BLACK, this);
                default -> new Ghoul(s.getX(), s.getY(), CARD_WIDTH, CARD_HEIGHT, Color.BLACK, this);
            };
            card.setCurrentLocation(Location.DRAW);
            s.setCard(card);
            stage.addActor(card);
            i++;
        }
        Collections.shuffle(this.drawDeck.getSlots());
    }

    public void resolveMove(Player player, Board enemyBoard, Vector3 coords, Card c){
        Slot slot = null;
        if (player.hasEnoughMana(c)) {
            slot = player.getBoard().snapTo(coords, c);
        }
        if (slot != null) {
            player.getHand().removeCard(c);
            player.setCurrentMana(player.getCurrentMana() - c.getCost());
        } else {
            var enemySlot = enemyBoard.checkFight(coords, c);
            if(enemySlot != null){
                var enemyCard = enemySlot.getCard();
                if(enemyCard!= null){
                    c.fight(enemyCard);
                    c.setAttackCount(c.getAttackCount() - 1);
                }else {
                    // return to previous position
                    c.move(c.getPreviousPosition().getX(), c.getPreviousPosition().getY(), c.getPreviousPosition().getW(), c.getPreviousPosition().getH());
                    c.setPreviousPosition(new Bound(c.getX(), c.getY(), c.getWidth(), c.getHeight()));
                }
            }else {
                // return to previous position
                c.move(c.getPreviousPosition().getX(), c.getPreviousPosition().getY(), c.getPreviousPosition().getW(), c.getPreviousPosition().getH());
                c.setPreviousPosition(new Bound(c.getX(), c.getY(), c.getWidth(), c.getHeight()));
            }
        }
    }

    public void refreshBoard(){
        for(Slot s: getBoard().getSlots()){
            if(s.getCard()!= null){
                s.getCard().setAttackCount(1);
            }
        }
    }

    public boolean hasEnoughMana(Card card){
        if(this.getCurrentMana() >= card.getCost()){
            return true;
        }else {
            return false;
        }
    }

    public void draw(Batch batch){
        font.getData().markupEnabled = true;
        font.getData().setScale(2.5f);
        if(getHealth() <= 10){
            font.draw(batch, "[RED]" + this.getHealth(), this.getHealthPosition().x,this.getHealthPosition().y);
        }else{
            font.draw(batch, "[GREEN]" + this.getHealth(), this.getHealthPosition().x,this.getHealthPosition().y);
        }
        font.draw(batch, "[WHITE]" + this.getCurrentMana() + "/" + this.getTotalMana(), this.getHealthPosition().x + BUFFER*3,this.getHealthPosition().y);

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
