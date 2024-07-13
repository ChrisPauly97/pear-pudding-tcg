package com.pear.pudding.player;


import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.pear.pudding.card.*;
import com.pear.pudding.enums.Location;
import com.pear.pudding.model.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.pear.pudding.enums.Location.HAND;
import static com.pear.pudding.model.Constants.*;

@Getter
@Setter
public class Player extends Actor {
    private Integer totalMana = 0;
    private Integer currentMana = 0;
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
        this.healthPosition = new Vector2(WINDOW_WIDTH / 2, isPlayer1 ? BUFFER * 3 + CARD_HEIGHT : WINDOW_HEIGHT - CARD_HEIGHT - BUFFER);
        this.discardPile = new DiscardPile(WINDOW_WIDTH - 2 * CARD_WIDTH, isPlayer1 ? BUFFER * 2 + CARD_HEIGHT : WINDOW_HEIGHT - BUFFER * 2 - CARD_HEIGHT * 2, CARD_WIDTH, CARD_HEIGHT);
        Texture heroTexture = manager.get("ghost.png", Texture.class);
        Texture hero2Texture = manager.get("ghoul.png", Texture.class);
        hero = new Hero(isPlayer1 ? heroTexture : hero2Texture, WINDOW_WIDTH / 2, isPlayer1 ? (CARD_HEIGHT * 2) - BUFFER * 2 : WINDOW_HEIGHT - (CARD_HEIGHT * 2) + BUFFER * 2, isPlayer1 ? 0 : 180);
        if (!isPlayer1) {
            hero.rotateBy(180f);
        }
    }

    public void initializeDeck(Stage stage) {
        Card card;
        for (int i = 0; i < drawDeck.getCards().length; i++) {
            Vector3 slotPos = drawDeck.getSlotPositionAtIndex(1);
            card = switch (i) {
                case 0, 3 -> new Ghost(slotPos.x, slotPos.y, CARD_WIDTH, CARD_HEIGHT, Color.BLACK, this);
                case 1, 4 -> new Skeleton(slotPos.x, slotPos.y, CARD_WIDTH, CARD_HEIGHT, Color.BLACK, this);
                case 2, 5 -> new Zombie(slotPos.x, slotPos.y, CARD_WIDTH, CARD_HEIGHT, Color.BLACK, this);
                default -> new Ghoul(slotPos.x, slotPos.y, CARD_WIDTH, CARD_HEIGHT, Color.BLACK, this);
            };
            card.setCurrentLocation(Location.DRAWDECK);
            this.drawDeck.addCard(card, i);
        }
        this.drawDeck.shuffle();
    }

    public void refreshBoard() {
        for (Card s : getBoard().getCards()) {
            if (s != null) {
                s.setAttackCount(1);
            }
        }
    }

    public boolean hasEnoughMana(Card card) {
        return this.getCurrentMana() >= card.getCost();
    }

    public void draw(Batch batch) {
        var font = this.manager.get("fonts/Satoshi-Variable.ttf", BitmapFont.class);

        font.getData().markupEnabled = true;
        font.getData().setScale(1f);
        if (this.getHero().getHealth() <= 10) {
            font.draw(batch, "[RED]" + this.getHero().getHealth(), this.getHealthPosition().x- BUFFER * 3, this.getHealthPosition().y);
        } else {
            font.draw(batch, "[GREEN]" + this.getHero().getHealth(), this.getHealthPosition().x - BUFFER * 3, this.getHealthPosition().y);
        }
        font.draw(batch, "[WHITE]" + this.getCurrentMana() + "/" + this.getTotalMana(), this.getHealthPosition().x + BUFFER * 3 / 2, this.getHealthPosition().y);
        for(int i = 0; i < currentMana; i++){
            font.draw(batch, "[BLUE] *", WINDOW_WIDTH - CARD_WIDTH*2 + (BUFFER * i) , this.getHealthPosition().y);
        }
        getHero().draw(batch);
        getDrawDeck().draw(batch);
        getBoard().draw(batch);
        getHand().draw(batch);
    }

    public void drawCard() {
        for (int i = this.drawDeck.getCards().length - 1; i >= 0; i--) {
            if (this.drawDeck.getCards()[i] != null) {
                var card = this.drawDeck.getCards()[i];
                this.drawDeck.removeCard(i);
                var emptySlot = this.hand.firstEmptySlot();
                var handPos = this.hand.getSlotPositionAtIndex(emptySlot);
                if (card != null) {
                    card.move(handPos.x, handPos.y, HAND);
                    this.hand.addCard(card, emptySlot);
                }
                break;
            }
        }
    }
}
