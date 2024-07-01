package com.pear.pudding.input;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.pear.pudding.player.Player;

public class EndTurnClickListener extends ClickListener {
    private final Player player1;
    private final Player player2;

    public EndTurnClickListener(Player player1, Player player2){
        super();
        this.player1 = player1;
        this.player2 = player2;
    }

    @Override
    public void clicked(InputEvent event, float x, float y){
        Gdx.app.log("Button", "Pressed");
        if(player1.isMyTurn()){
            player1.setMyTurn(false);
            player2.setMyTurn(true);
            player2.drawCard();
            player2.setTotalMana(player2.getTotalMana() + 1);
            player2.setCurrentMana(player2.getTotalMana());
            player2.refreshBoard();
        }else if(player2.isMyTurn()){
            player1.setMyTurn(true);
            player2.setMyTurn(false);
            player1.drawCard();
            player1.setTotalMana(player1.getTotalMana() + 1);
            player1.setCurrentMana(player1.getTotalMana());
            player1.refreshBoard();

        }
    }

}
