package com.pear.pudding.input;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.pear.pudding.player.Player;

public class EndTurnClickListener extends ClickListener {
    private final Player player1;
    private final Player player2;
    private final Runnable onAITurnStart;

    public EndTurnClickListener(Player player1, Player player2, Runnable onAITurnStart) {
        super();
        this.player1 = player1;
        this.player2 = player2;
        this.onAITurnStart = onAITurnStart;
    }

    @Override
    public void clicked(InputEvent event, float x, float y) {
        Gdx.app.log("Button", "Pressed");
        if (player1.isMyTurn()) {
            player1.endTurn();
            player2.startTurn();

            // Trigger AI turn if player2 is AI-controlled
            if (player2.isAI() && onAITurnStart != null) {
                onAITurnStart.run();
            }
        } else if (player2.isMyTurn()) {
            player2.endTurn();
            player1.startTurn();
        }
    }

}
