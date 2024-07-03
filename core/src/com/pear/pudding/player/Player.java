package com.pear.pudding.player;


import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.math.Vector2;
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
    // In your Player class
    private Hero hero;
    private AssetManager manager;

    public Player(boolean isPlayer1, AssetManager manager) {
        this.isMyTurn = isPlayer1;
        this.manager = manager;
        this.board = new Board(WINDOW_WIDTH / 2 - (CARD_WIDTH * ((float) NUMBER_OF_BOARD_SLOTS / 2)), isPlayer1 ? BOARD_BUFFER : WINDOW_HEIGHT - BOARD_BUFFER - CARD_HEIGHT, NUMBER_OF_BOARD_SLOTS * CARD_WIDTH, CARD_HEIGHT);
        this.hand = new Hand(WINDOW_WIDTH / 2 - (CARD_WIDTH * (NUMBER_OF_HAND_SLOTS / 2)), isPlayer1 ? BUFFER : WINDOW_HEIGHT - CARD_HEIGHT, NUMBER_OF_HAND_SLOTS * CARD_WIDTH, CARD_HEIGHT);
        this.drawDeck = new DrawDeck(WINDOW_WIDTH - 2 * CARD_WIDTH, isPlayer1 ? BUFFER : WINDOW_HEIGHT - BUFFER - CARD_HEIGHT, CARD_WIDTH, CARD_HEIGHT);
        this.healthPosition = new Vector2(WINDOW_WIDTH / 2, isPlayer1 ? BUFFER * 3 + CARD_HEIGHT: WINDOW_HEIGHT - CARD_HEIGHT - BUFFER);
        this.discardPile = new DiscardPile(WINDOW_WIDTH - 2 * CARD_WIDTH, isPlayer1 ? BUFFER * 2 + CARD_HEIGHT : WINDOW_HEIGHT - BUFFER * 2 - CARD_HEIGHT * 2, CARD_WIDTH, CARD_HEIGHT);
        Texture heroTexture = manager.get("ghost.png", Texture.class);
        Texture hero2Texture = manager.get("ghoul.png", Texture.class);
        hero = new Hero(isPlayer1 ? heroTexture : hero2Texture, WINDOW_WIDTH / 2, isPlayer1 ? (CARD_HEIGHT * 2)- BUFFER * 2: WINDOW_HEIGHT - (CARD_HEIGHT * 2) +BUFFER*2, isPlayer1 ? 0 : 180);
        if(!isPlayer1){
            hero.rotateBy(180f);
        }
    }

    public void initializeDeck(Stage stage) {
        int i = 0;
        Card card;
        for(Slot s: this.drawDeck.getSlots()){
            card = switch (i) {
                case 0, 3 -> new Ghost(s.getX(), s.getY(), CARD_WIDTH, CARD_HEIGHT, Color.BLACK, this, s);
                case 1, 4 -> new Skeleton(s.getX(), s.getY(), CARD_WIDTH, CARD_HEIGHT, Color.BLACK, this, s);
                case 2, 5 -> new Zombie(s.getX(), s.getY(), CARD_WIDTH, CARD_HEIGHT, Color.BLACK, this, s);
                default -> new Ghoul(s.getX(), s.getY(), CARD_WIDTH, CARD_HEIGHT, Color.BLACK, this, s);
            };
            card.setCurrentLocation(Location.DRAW);
            s.setCard(card);
            stage.addActor(card);
            i++;
        }
        Collections.shuffle(this.drawDeck.getSlots());
    }

    public void refreshBoard(){
        for(Slot s: getBoard().getSlots()){
            if(s.getCard()!= null){
                s.getCard().setAttackCount(1);
            }
        }
    }

    public boolean hasEnoughMana(Card card){
        return this.getCurrentMana() >= card.getCost();
    }

    public void draw(Batch batch){
        var font = this.manager.get("fonts/Satoshi-Variable.ttf", BitmapFont.class);

        font.getData().markupEnabled = true;
        font.getData().setScale(1f);
            if(getHealth() <= 10){
                font.draw(batch, "[RED]" + this.getHealth(), this.getHealthPosition().x ,this.getHealthPosition().y);
        }else{
            font.draw(batch, "[GREEN]" + this.getHealth(), this.getHealthPosition().x - BUFFER * 3,this.getHealthPosition().y);
        }
        font.draw(batch, "[WHITE]" + this.getCurrentMana() + "/" + this.getTotalMana(), this.getHealthPosition().x + BUFFER*3 / 2,this.getHealthPosition().y);
        getHero().draw(batch);
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
                    card.moveToSlot(slot);
                }else{
                    card.setCurrentLocation(Location.DISCARD);
                }
                break;
            }
        }
    }
}
