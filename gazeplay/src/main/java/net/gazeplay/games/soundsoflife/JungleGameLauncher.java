package net.gazeplay.games.soundsoflife;

import javafx.scene.Scene;
import net.gazeplay.GameContext;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.GameSpec;
import net.gazeplay.commons.utils.stats.Stats;

public class JungleGameLauncher implements GameSpec.GameLauncher {
    @Override
    public Stats createNewStats(Scene scene) {
        return new SoundsOfLifeStats(scene, "Jungle");
    }

    @Override
    public GameLifeCycle createNewGame(GameContext gameContext, GameSpec.GameVariant gameVariant,
                                       Stats stats) {
        return new SoundsOfLife(gameContext, stats, 1);
    }
}
