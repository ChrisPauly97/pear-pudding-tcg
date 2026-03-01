package com.pear.pudding.model;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.pear.pudding.card.EffectTrigger;
import com.pear.pudding.card.StatusEffect;
import com.pear.pudding.card.StatusEffects;
import com.pear.pudding.enums.CardClass;
import com.pear.pudding.enums.CardType;
import com.pear.pudding.enums.Location;
import com.pear.pudding.player.Hero;
import com.pear.pudding.player.Player;
import lombok.Getter;
import lombok.Setter;

import static com.pear.pudding.enums.Location.*;
import static com.pear.pudding.model.Constants.BUFFER;
import static com.pear.pudding.model.Constants.TEXT_BUFFER;

@Getter
@Setter
public class Card extends Actor {
    private Image image;
    private Image cardBack;
    private Image cardBackground;
    private Image cardCanPlayBackground;
    private Integer cost = 1;
    private Integer attack = 1;
    private Integer health = 1;
    private StatusEffect statusEffect;
    private CardType cardType;
    private Integer attackCount = 1;
    private Integer outOfPlay = 0;
    /**
     * True if this card was just played this turn and cannot attack yet (summoning sickness)
     */
    private boolean summoningSick = false;
    /**
     * The player associated to the card
     * Every card belongs to either player 1 or player 2
     */
    private CardClass cardClass;
    /**
     * The player associated to the card
     * Every card belongs to either player 1 or player 2
     */
    private Player player;
    /**
     * Whether a card is face up or down
     * Determines whether the card display its text and image or the card back.
     */
    private boolean faceUp = false;
    private AssetManager manager;
    private Location currentLocation;

    /** No-arg constructor for unit testing — does not initialize LibGDX resources. */
    Card() {}

    // TODO set a min health of 0 for every unit to they can't go above it when getting healed
    public void takeDamage(int damage){
        health -= damage;
    }

    // TODO set a max health for every unit to they can't go above it when getting healed
    public void getHealing(int healing){
        health += healing;
    }

    public void handleDiscard(){

    }
    public Card(float x, float y, float width, float height, Color color, Integer cost, Integer attack, Integer health,
                CardType type, CardClass cardClass, StatusEffect statusEffect, Player player) {
        setManager(player.getManager());
        setBounds(x, y, width, height);
        setCurrentLocation(DRAWDECK);
        setAttack(attack);
        setHealth(health);
        setCost(cost);
        setCardType(type);
        setCardClass(cardClass);
        setStatusEffect(statusEffect);
        setColor(color);
        setPlayer(player);
        setFaceUp(false);
        cardBack = new Image(manager.get("cardback.jpg", Texture.class));
        cardBack.setBounds(x, y, width, height);
        cardBackground = new Image(manager.get("card.png", Texture.class));
        cardBackground.setBounds(x, y, width, height);
        cardCanPlayBackground = new Image(manager.get("card-can-play.png", Texture.class));
        cardCanPlayBackground.setBounds(x, y, width, height);
        image = new Image(manager.get("ghostnew.png", Texture.class));
        image.setBounds(x, y, width, height);
    }

    public boolean handleSummonEffect(){
        if(statusEffect.getEffectTrigger().equals(EffectTrigger.SUMMON)){
          switch (statusEffect.getEffectType()){
            case HEAL:
              health += statusEffect.getValue();
              break;
            case DAMAGE:
              return true;
            case REMOVE:
              return true;
            default:
              return false;
          }
        }
        return false;
    }

    public boolean contains(Vector3 point) {
        float minX = getX();
        float minY = getY();
        float maxX = getX() + getWidth();
        float maxY = getY() + getHeight();

        return point.x >= minX && point.x <= maxX && point.y >= minY && point.y <= maxY;
    }

    public boolean canAttack() {
        return this.attackCount > 0 && !this.summoningSick && this.currentLocation == BOARD && this.outOfPlay == 0;
    }

    public boolean canPlay() {
        return this.currentLocation == HAND && this.player.hasEnoughMana(this);
    }

    public boolean triggerAttackEffect(Card card, Card enemyCard) {
        if (statusEffect.getEffectTrigger().equals(EffectTrigger.FIGHT)) {
            switch (card.getStatusEffect().getEffectType()) {
                case HEAL:
                    card.health += card.getStatusEffect().getValue();
                    return true;
                case DAMAGE:
                    enemyCard.health -= card.getStatusEffect().getValue();
                    return true;
                case REMOVE:
                    enemyCard.setOutOfPlay(card.getStatusEffect().getValue());
                    return true;
                default:
                    return false;
            }
        }
        return false;
    }

    public void handleRemoveEffect(int value) {
      this.outOfPlay = value;
    }

    public boolean fight(Hero hero) {
        if (!canAttack()) {
            return false;
        }
        this.attackCount--;
        this.health -= hero.getAttack();
        hero.health -= getAttack();
        // Check card death FIRST so it's always handled, even if hero also dies simultaneously
        if (this.health <= 0) {
            moveToDiscardPile();
        }
        if (hero.health <= 0) {
            return true;
        }
        return false;
    }

    public void checkDiscard(){
        if (this.health <= 0) {
            moveToDiscardPile();
        } else {
            player.getBoard().restoreSnapshot();
        }
    }

    public void resetToPreviousLocation() {
        Gdx.app.log("Reset", this.getCurrentLocation().toString());
        var board = getPlayer().getBoard();
        var hand = getPlayer().getHand();
        switch (this.getCurrentLocation()) {
            case BOARD:
                board.restoreSnapshot();
                hand.restoreSnapshot();
                int nearestFreeSlot = board.nearestFreeSlot();
                if (nearestFreeSlot != -1) {
                    board.addCard(this, nearestFreeSlot);
                } else {
                    // Board is full — prevent limbo by sending to discard
                    moveToDiscardPile();
                }
                board.setPreviousTargetSlot(-1);
                break;
            case HAND:
                hand.restoreSnapshot();
                board.restoreSnapshot();
                int emptySlot = hand.firstEmptySlot();
                if (emptySlot != -1) {
                    hand.addCard(this, emptySlot);
                }
                hand.rebalance(-1);
                hand.setPreviousTargetSlot(-1);
                break;
            case DISCARD:
                // Card died during combat — already in discard, nothing to do
                break;
        }
    }

    public void fight(Card enemy) {
        if (!canAttack()) {
            return;
        }
        this.attackCount--;
        this.health -= enemy.getAttack();
        enemy.health -= getAttack();
        if (enemy.health <= 0) {
            enemy.moveToDiscardPile();
        }
        if (this.health <= 0) {
            moveToDiscardPile();
        }
    }
//

    public void moveToDiscardPile() {
        // Already discarded — nothing to do
        if (currentLocation == DISCARD) {
            return;
        }

        var myDiscardPile = this.getPlayer().getDiscardPile();
        var emptyDiscardSlot = myDiscardPile.firstEmptySlot();
        if (emptyDiscardSlot == -1) {
            return; // Discard pile full — prevent limbo by leaving card in place
        }

        // Use atomic move
        Deck sourceDeck = null;
        switch(currentLocation){
            case BOARD:
                sourceDeck = this.player.getBoard();
                break;
            case HAND:
                sourceDeck = this.player.getHand();
                break;
        }

        Deck.moveCardBetweenDecks(this, sourceDeck, myDiscardPile, emptyDiscardSlot);
    }


    public void zoom() {
        if (this.image != null) {
            setBounds(getStage().getWidth() / 2, getStage().getHeight() / 2, getWidth() * 3, getHeight() * 3);
            this.cardBackground.setBounds(getStage().getWidth() / 2, getStage().getHeight() / 2, this.cardBackground.getWidth() * 3, this.cardBackground.getHeight() * 3);
            this.image.setBounds(getX(), getY(), getWidth(), getHeight());
        }
    }

    public void unzoom() {
        // Find the card's index in the hand
        Hand hand = this.player.getHand();
        for (int i = 0; i < hand.getCards().length; i++) {
            if (hand.getCards()[i] == this) {
                // Found the card, move it back to its proper position with normal size
                var handPos = hand.getSlotPositionAtIndex(i);
                move(handPos.x, handPos.y, Constants.CARD_WIDTH, Constants.CARD_HEIGHT);
                return;
            }
        }
    }

    public void move(float x, float y, Location location) {
        setPosition(x, y);
        if (cardBack != null) this.cardBack.setPosition(x, y);
        if (cardBackground != null) this.cardBackground.setPosition(x, y);
        if (cardCanPlayBackground != null) this.cardCanPlayBackground.setPosition(x, y);
        this.currentLocation = location;
        switch (location) {
            case BOARD, HAND:
                setFaceUp(outOfPlay >= 0);
                break;
            case DISCARD, DRAWDECK:
                faceUp = false;
                break;
        }
        if (getImage() != null) {
            this.image.setPosition(x, y + this.image.getHeight());
        }
    }

    public void move(float x, float y, float w, float h) {
        setBounds(x, y, w, h);
        if (cardBack != null) this.cardBack.setBounds(x, y, w, h);
        if (cardBackground != null) this.cardBackground.setBounds(x, y, w, h);
        if (cardCanPlayBackground != null) this.cardCanPlayBackground.setPosition(x, y);
        if (getImage() != null) {
            this.image.setBounds(getX(), getY(), getWidth(), getHeight());
        }
    }

    public Vector2 calculatePosDelta(float mouseX, float mouseY) {
        return new Vector2(mouseX - getX(), mouseY - getY());
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        if (this.faceUp) {
            if (this.image != null) {
                this.image.draw(batch, 1f);
            }
            float width = (float) ((float) getWidth() * 0.85);
            BitmapFont fontToUse = manager.get("fonts/Satoshi-Variable.ttf", BitmapFont.class);
            if (this.currentLocation != ZOOM) {
                fontToUse.getData().setScale(0.5f);
            }

            // Show green/playable background for cards that can be played from hand or can attack from board
            if ((this.player.hasEnoughMana(this) && this.player.isMyTurn() && this.currentLocation == HAND) ||
                (canAttack() && this.currentLocation == BOARD)) {
                this.cardCanPlayBackground.draw(batch, 1f);
            } else {
                this.cardBackground.draw(batch, 1f);
            }

            fontToUse.draw(batch, "[SLATE]" + this.attack, getX() + TEXT_BUFFER, getY() + getHeight(), getWidth() / 10, 0, true);
            fontToUse.draw(batch, "[WHITE]" + getClass().getSimpleName(), getX(), getY() + getHeight(), getWidth(), 1, true);
            fontToUse.draw(batch, "[RED]" + this.health, getX() + getWidth() - BUFFER, getY() + getHeight(), getWidth() / 10, 1, true);
            fontToUse.draw(batch, "[WHITE]" + this.statusEffect.getCardText(), getX(), getY() + getHeight() / 2, width, 1, true);
            fontToUse.getData().setScale(1f);
        } else {
            if (this.cardBack != null) {
                this.cardBack.draw(batch, 1f);
            }
        }

    }
}