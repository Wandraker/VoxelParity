package dev.onelsey.voxelparity.spectator;

import org.bukkit.Input;

public record SpectatorInputState(
        boolean forward,
        boolean backward,
        boolean left,
        boolean right,
        boolean jump,
        boolean sneak,
        boolean sprint
) {
    public static SpectatorInputState from(Input input) {
        return new SpectatorInputState(
                input.isForward(),
                input.isBackward(),
                input.isLeft(),
                input.isRight(),
                input.isJump(),
                input.isSneak(),
                input.isSprint()
        );
    }

    public static SpectatorInputState idle() {
        return new SpectatorInputState(
                false,
                false,
                false,
                false,
                false,
                false,
                false
        );
    }

    public boolean hasMovement() {
        return forward || backward || left || right || jump || sneak;
    }

    public String compact() {
        return "forward=" + forward
                + " backward=" + backward
                + " left=" + left
                + " right=" + right
                + " jump=" + jump
                + " sneak=" + sneak
                + " sprint=" + sprint;
    }
}
