package dev.onelsey.voxelparity.movement;

public record ModuleEvaluation(
        String moduleId,
        Outcome outcome,
        String reason,
        boolean suppressVanillaWarning
) {
    public enum Outcome {
        ABSTAIN,
        BLOCKED,
        ALLOW
    }

    public static ModuleEvaluation abstain(String moduleId, String reason) {
        return new ModuleEvaluation(moduleId, Outcome.ABSTAIN, reason, false);
    }

    public static ModuleEvaluation blocked(String moduleId, String reason) {
        return new ModuleEvaluation(moduleId, Outcome.BLOCKED, reason, false);
    }

    public static ModuleEvaluation allow(String moduleId, String reason, boolean suppressVanillaWarning) {
        return new ModuleEvaluation(moduleId, Outcome.ALLOW, reason, suppressVanillaWarning);
    }
}
