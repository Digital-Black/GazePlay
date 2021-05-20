package net.gazeplay.games.labyrinth;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum LabyrinthGameVariant {
    LOOK_DESTINATION("Look at the destination box to move"),
    LOOK_LOCAL_ARROWS("Look the movement arrows around the mouse to move"),
    LOOK_GLOBAL_ARROWS("Look the movement arrows around the labyrinth to move"),
    SELECT_THEN_LOOK_DESTINATION("Select the mouse then look at the destination box to move"),
    ANLOOK_DESTINATION("Anime\nLook at the destination box to move"),
    ANLOOK_LOCAL_ARROWS("Anime\nLook the movement arrows around the mouse to move"),
    ANLOOK_GLOBAL_ARROWS("Anime\nLook the movement arrows around the labyrinth to move"),
    ANSELECT_THEN_LOOK_DESTINATION("Anime\nSelect the mouse then look at the destination box to move");

    @Getter
    private final String label;
}
