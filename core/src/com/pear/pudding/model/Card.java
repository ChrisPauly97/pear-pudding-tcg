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
import com.pear.pudding.enums.CardClass;
import com.pear.pudding.enums.CardType;
import com.pear.pudding.enums.Location;
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
    private Integer cost = 1;
    private Integer attack = 1;
    private Integer health = 1;
    private String cardText = "";
    private CardType cardType;
    private Integer attackCount = 1;
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
    private Slot currentSlot;
    private Slot previousSlot;
    private AssetManager manager;
    private Location currentLocation;


    public Card(float x, float y, float width, float height, Color color, Integer cost, Integer attack, Integer health,
                CardType type, CardClass cardClass, String cardText, Player player, Slot slot) {
        setManager(player.getManager());
        setBounds(x, y, width, height);
        setCurrentLocation(DRAW);
        setAttack(attack);
        setPreviousSlot(slot);
        setCurrentSlot(slot);
        setHealth(health);
        setCost(cost);
        setCardType(type);
        setCardClass(cardClass);
        setCardText(cardText);
        setColor(color);
        setPlayer(player);
        setFaceUp(true);
        Texture texture = new Texture(Gdx.files.internal("cardback.jpg"));
        var img = new Image(texture);
        img.setBounds(x, y, width, height);
        setCardBack(img);
        cardBackground = new Image(new Texture(Gdx.files.internal("card.png")));
        cardBackground.setBounds(x, y, width, height);
//        cardBack.addListener(new ClickListener(){
//            @Override
//            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor){
//                zoom();
//            }
//            @Override
//            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor){
//                reverseZoom();
//            }
//        });
    }

    public void moveToPreviousPosition() {
        if(currentSlot != previousSlot) {
            move(getPreviousSlot().getX(), getPreviousSlot().getY());
            var oldSlot = previousSlot;
            currentSlot.setCard(null);
            previousSlot = currentSlot;
            currentSlot = oldSlot;
            currentSlot.setCard(this);
            currentLocation = oldSlot.location;
        }
    }

    public void moveToSlot(Slot s) {
        this.currentLocation = s.getLocation();
        move(s.getX(), s.getY());
        previousSlot.setCard(null);
        this.currentSlot.setCard(null);
        previousSlot = this.currentSlot;
        s.setCard(this);
        currentSlot = s;
        Gdx.app.log("Test", "Hi");
    }

//    public void resolveMove(Vector3 coordinates, Board enemyBoard) {
//        if (player.isMyTurn()) {
//            Slot slot = null;
//            if (player.hasEnoughMana(this) && this.currentLocation.equals(HAND)) {
//                var initialTargetSlot = player.getBoard().findSlot(coordinates);
//                slot = player.getBoard().snapTo(initialTargetSlot,this, null);
//            }
//
//            if (slot != null) {
//                player.getHand().removeCard(this);
//                player.setCurrentMana(player.getCurrentMana() - getCost());
//            } else {
//                Slot enemySlot = enemyBoard.findSlot(coordinates);
//                if (enemySlot != null) {
//                    Card enemyCard = enemySlot.getCard();
//                    if (enemyCard != null && this.attackCount > 0) {
//                        fight(enemyCard);
//                    } else {
//                        moveToPreviousPosition();
//                    }
//                } else {
//                    moveToPreviousPosition();
//                }
//            }
//        } else {
//            moveToPreviousPosition();
//        }
//    }


    public void fight(Card enemy) {
        this.health -= enemy.getAttack();
        enemy.health -= getAttack();
        if (enemy.health <= 0) {
            enemy.setCurrentLocation(DISCARD);
            for(Slot s: enemy.getPlayer().getDiscardPile().getSlots()){
                if (s.getCard() == null) {
                    enemy.move(s.getX(), s.getY());
                    break;
                }
            }
        }else {
            // return to previous position
            enemy.moveToPreviousPosition();
        }
        if (this.health <= 0) {
            setCurrentLocation(DISCARD);
            for(Slot s: getPlayer().getDiscardPile().getSlots()){
                if (s.getCard() == null) {
                    move(s.getX(), s.getY());
                    break;
                }
            }
        }else {
            this.player.getBoard().onHover(player.getBoard().getSlots().get(player.getBoard().getSlots().size()/2), this, null);
            // return to previous position
            this.moveToPreviousPosition();
        }
    }




    public void zoom() {
        if (this.image != null) {
            setBounds(getStage().getWidth() / 2, getStage().getHeight() / 2, getWidth() * 3, getHeight() * 3);
            Bound b = imagePos(getX(), getY(), getWidth(), getHeight());
            this.cardBackground.setBounds(getStage().getWidth() / 2, getStage().getHeight() / 2, this.cardBackground.getWidth() * 3, this.cardBackground.getHeight() * 3);
            this.image.setBounds(b.getX(), b.getY(), b.getW(), b.getH());
        }
    }

    public Bound imagePos(float cardX, float cardY, float cardW, float cardH) {
        return new Bound(cardX, cardY + cardH / 2, cardW, cardH / 2);
    }

    public void reverseZoom() {
        if (this.image != null) {
            this.image.setBounds(this.previousSlot.getX(), this.previousSlot.getY(), this.previousSlot.getWidth(), this.previousSlot.getHeight());
            this.cardBackground.setBounds(this.previousSlot.getX(), this.previousSlot.getY(), this.previousSlot.getWidth(), this.previousSlot.getHeight());
        }
        moveToPreviousPosition();
        setBounds(this.previousSlot.getX(), this.previousSlot.getY(), this.previousSlot.getWidth(), this.previousSlot.getHeight());
    }

    public void move(float x, float y) {
        setPosition(x, y);
        this.cardBack.setPosition(x, y);
        this.cardBackground.setPosition(x, y);
        if (getImage() != null) {
            this.image.setPosition(x, y + this.image.getHeight());
        }
    }

    public void move(float x, float y, float w, float h) {
        setBounds(x, y, w, h);
        this.cardBack.setBounds(x, y, w, h);
        this.cardBackground.setBounds(x, y, w, h);
        if (getImage() != null) {
            var bound = imagePos(x, y, w, h);
            this.image.setBounds(bound.getX(), bound.getY(), bound.getW(), bound.getH());
        }
    }

    public Vector2 calculatePosDelta(float mouseX, float mouseY) {
        return new Vector2(mouseX - getX(), mouseY - getY());
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        this.cardBackground.draw(batch, 1f);
        if (this.faceUp) {
            if (this.image != null) {
                this.image.draw(batch, 1f);
            }
            float width = (float) ((float) getWidth() * 0.85);
            BitmapFont fontToUse = manager.get("fonts/Satoshi-Variable.ttf", BitmapFont.class);
            if (this.currentLocation != ZOOM) {
                fontToUse.getData().setScale(0.5f);
            }
            fontToUse.draw(batch, "[SLATE]" + this.attack, getX() + TEXT_BUFFER, getY() + getHeight(), getWidth() / 10, 0, true);
            fontToUse.draw(batch, "[WHITE]" + getClass().getSimpleName(), getX(), getY() + getHeight(), getWidth(), 1, true);
            fontToUse.draw(batch, "[RED]" + this.health, getX() + getWidth() - BUFFER, getY() + getHeight(), getWidth() / 10, 1, true);
            fontToUse.draw(batch, "[WHITE]" + this.cardText, getX(), getY() + getHeight() / 2, width, 1, true);
            fontToUse.getData().setScale(1f);
        } else {
            if (this.cardBack != null) {
                this.cardBack.draw(batch, 1f);
            }
        }

    }
}