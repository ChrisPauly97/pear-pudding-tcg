package com.pear.pudding.card;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.pear.pudding.enums.CardClass;
import com.pear.pudding.enums.CardType;
import com.pear.pudding.enums.Location;
import com.pear.pudding.model.Bound;
import com.pear.pudding.player.Player;
import lombok.Getter;
import lombok.Setter;

import static com.pear.pudding.enums.Location.DRAW;
import static com.pear.pudding.enums.Location.ZOOM;
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
    /**
     * Stores the previous position of the card
     * Used to reverse the last action carried out on the card
     */
    private Bound previousPosition;


    BitmapFont font = new BitmapFont();
    public void adjustHealth(Integer deltaHealth) {
        this.health += deltaHealth;
    }

    public void adjustCost(Integer deltaCost) {
        this.cost += deltaCost;
    }

    private Location currentLocation;

    public Card(float x, float y, float width, float height, Color color, Integer cost, Integer attack, Integer health,
                CardType type, CardClass cardClass, String cardText, Player player) {
        setBounds(x, y, width, height);
        setCurrentLocation(DRAW);
        setAttack(attack);
        setPreviousPosition(new Bound(x,y,width,height));
        setHealth(health);
        setCost(cost);
        setCardType(type);
        setCardClass(cardClass);
        setCardText(cardText);
        setColor(color);
        setPlayer(player);
        Texture texture = new Texture(Gdx.files.internal("cardback.jpg"));
        var img = new Image(texture);
        img.setBounds(x, y, width, height);
        setCardBack(img);
        cardBackground = new Image(new Texture(Gdx.files.internal("card.png")));
        cardBackground.setBounds(x,y,width,height);
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

    public void zoom() {
        this.previousPosition.setBounds(getX(),getY(),getWidth(),getHeight());

        if (this.image != null) {
            setBounds(getStage().getWidth() / 2, getStage().getHeight() / 2, getWidth() * 3, getHeight() * 3);
            Bound b = imagePos(getX(), getY(), getWidth(), getHeight());
            this.cardBackground.setBounds(getStage().getWidth() / 2, getStage().getHeight() / 2, this.cardBackground.getWidth() * 3, this.cardBackground.getHeight() * 3);
            this.image.setBounds(b.getX(), b.getY(), b.getW(), b.getH());
        }
    }

    public Bound imagePos(float cardX, float cardY, float cardW, float cardH){
        return new Bound(cardX, cardY+ cardH/2,cardW, cardH/2);
    }

    public void reverseZoom() {
        if (this.image != null) {
            Bound b = imagePos(this.previousPosition.getX(), this.previousPosition.getY(), this.previousPosition.getW(), this.previousPosition.getH());
            this.image.setBounds(b.getX(), b.getY(), b.getW(), b.getH());
            this.cardBackground.setBounds(this.previousPosition.getX(), this.previousPosition.getY(), this.previousPosition.getW(), this.previousPosition.getH());
        }
        setBounds(this.previousPosition.getX(), this.previousPosition.getY(), this.previousPosition.getW(), this.previousPosition.getH());
        this.previousPosition.setBounds(getX(),getY(),getWidth(),getHeight());
    }

    public void move(float x, float y) {
        this.setPosition(x,y);
        this.cardBack.setPosition(x, y);
        this.cardBackground.setPosition(x,y);
        if (this.getImage() != null) {
            this.image.setPosition(x, y + this.image.getHeight());
        }
    }

    public void move(float x, float y, float w, float h) {
        this.setBounds(x,y,w,h);
        this.cardBack.setBounds(x, y, w, h);
        this.cardBackground.setBounds(x,y,w,h);
        if (this.getImage() != null) {
            var bound = imagePos(x,y,w,h);
            this.image.setBounds(bound.getX(), bound.getY(), bound.getW(), bound.getH());
        }
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        this.cardBackground.draw(batch, 1f);
        if(this.faceUp){
            if(this.image != null){
                this.image.draw(batch, 1f);
            }
            float width = (float) ((float) getWidth()*0.85);
            font.getData().markupEnabled = true;
            if(this.currentLocation == ZOOM) {
                font.getData().setScale(2.5f);
            }else{
                font.getData().setScale(1f);
            }
            font.draw(batch, "[SLATE]" + this.attack, getX() + TEXT_BUFFER, getY() + getHeight(), getWidth()/10, 0,true);
            font.draw(batch, "[WHITE]" + this.getClass().getSimpleName(), getX() , getY() +getHeight(), getWidth(), 1, true);
            font.draw(batch, "[RED]" + this.health, getX() + getWidth() - BUFFER, getY() + getHeight(), getWidth()/10, 1,true);
            if(this.currentLocation == ZOOM) {
                font.getData().setScale(2.5f);
            }else{
                font.getData().setScale(.8f);
            }
            font.draw(batch, "[WHITE]" + this.cardText, getX(), getY() +getHeight()/2 , width, 1, true);
        }else{
            if(this.cardBack != null){
                this.cardBack.draw(batch, 1f);
            }
        }

    }
}