package dev.onelsey.voxelparity.collision.paper;

public enum TranslatedCollisionKind {
    BAMBOO("bamboo"),
    POINTED_DRIPSTONE("pointed-dripstone");

    private final String id;

    TranslatedCollisionKind(String id) {
        this.id = id;
    }

    public String id() {
        return id;
    }
}
