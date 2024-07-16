package com.pear.pudding.model;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Rectangle;
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
        image = new Image(manager.get("ghost.png", Texture.class));
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

    public boolean triggerAttackEffect(Card card, Card enemyCard) {
        if (statusEffect.getEffectTrigger().equals(EffectTrigger.FIGHT)) {
            switch (card.getStatusEffect().getEffectType()) {
                case HEAL:
                    card.health += card.getStatusEffect().getValue();
                    break;
                case DAMAGE:
                    enemyCard.health -= card.getStatusEffect().getValue();
                    break;
                case REMOVE:
                    enemyCard.setOutOfPlay(card.getStatusEffect().getValue());
                    break;
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
        this.health -= hero.getAttack();
        hero.health -= getAttack();
        if (hero.health <= 0) {
            return true;
        }
        if (this.health <= 0) {
            moveToDiscardPile();
        } else {
            player.getBoard().restoreSnapshot();
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
                var nearestFreeSlot = board.nearestFreeSlot();
                board.addCard(this, nearestFreeSlot);
                board.setPreviousTargetSlot(-1);
                break;
            case HAND:
                hand.restoreSnapshot();
                board.restoreSnapshot();
                hand.addCard(this, hand.firstEmptySlot());
                hand.rebalance(-1);
                hand.setPreviousTargetSlot(-1);
        }
    }

    public void fight(Card enemy) {
        if (enemy.health <= 0) {
            enemy.moveToDiscardPile();
        } else {
            this.health -= enemy.getAttack();
            enemy.health -= getAttack();
        }
        if (enemy.health <= 0) {
            enemy.moveToDiscardPile();
        }
        if (this.health <= 0) {
            moveToDiscardPile();
        } else {
            player.getBoard().restoreSnapshot();
            resetToPreviousLocation();
//            this.player.getBoard().onHover(player.getBoard().getSlots().get(player.getBoard().getSlots().size()/2), this, null);
//             return to previous position
//            this.moveToPreviousPosition();
        }
    }
//

    public void moveToDiscardPile() {
        var myDiscardPile = this.getPlayer().getDiscardPile();
        var emptyDiscardSlot = myDiscardPile.firstEmptySlot();
        myDiscardPile.addCard(this, emptyDiscardSlot);
        switch (currentLocation) {
            case BOARD:
                this.player.getBoard().removeCard(this);
                break;
            case HAND:
                this.player.getHand().removeCard(this);
                break;
        }
        this.move(myDiscardPile.getSlotPositionAtIndex(0).x, myDiscardPile.getSlotPositionAtIndex(0).y, DISCARD);
        getStage().getBatch().begin();
        this.draw(getStage().getBatch(), 1f);
        getStage().getBatch().end();
    }


    public void zoom() {
        if (this.image != null) {
            setBounds(getStage().getWidth() / 2, getStage().getHeight() / 2, getWidth() * 3, getHeight() * 3);
            this.cardBackground.setBounds(getStage().getWidth() / 2, getStage().getHeight() / 2, this.cardBackground.getWidth() * 3, this.cardBackground.getHeight() * 3);
            this.image.setBounds(getX(), getY(), getWidth(), getHeight());
        }
    }

//
//    public void reverseZoom() {
//        if (this.image != null) {
//            this.image.setBounds(this.previousSlot.getX(), this.previousSlot.getY(), this.previousSlot.getWidth(), this.previousSlot.getHeight());
//            this.cardBackground.setBounds(this.previousSlot.getX(), this.previousSlot.getY(), this.previousSlot.getWidth(), this.previousSlot.getHeight());
//        }
//        moveToPreviousPosition();
//        setBounds(this.previousSlot.getX(), this.previousSlot.getY(), this.previousSlot.getWidth(), this.previousSlot.getHeight());
//    }

    public void move(float x, float y, Location location) {
        setPosition(x, y);
        this.cardBack.setPosition(x, y);
        this.cardBackground.setPosition(x, y);
        this.cardCanPlayBackground.setPosition(x, y);
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
        this.cardBack.setBounds(x, y, w, h);
        this.cardBackground.setBounds(x, y, w, h);
        this.cardCanPlayBackground.setPosition(x, y);
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
            if (this.player.hasEnoughMana(this) && this.player.isMyTurn() && this.currentLocation == HAND) {
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