package com.pear.pudding.card;

import java.util.Map;

public class StatusEffects {

    public static Map<String,String> effects = Map.ofEntries(
            Map.entry("Default", "Pretty basic but it gets the job done"),
            Map.entry("Rallying Cry", "Effect occurs on summon"),
            Map.entry("Dying Breath", "Effect occurs on discard"),
            Map.entry("Scare", "Remove a minion from the board for two turns"),
            Map.entry("Convert", "Discards in play minion and summon this in it's place, Maintains initiative"),
            Map.entry("Poison", "Effect occurs after n turns"),
            Map.entry("Consume", "Increase your stats by the stats consumed"),
            Map.entry("Vampyric", "Increase your health by damage dealt"),
            Map.entry("Parisitic", "Deals damage when attacked"));
}
