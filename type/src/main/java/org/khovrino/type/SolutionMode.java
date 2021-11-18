package org.khovrino.type;

enum SolutionMode {
    IDENTITY, FOR_LEFT_SIDE, FOR_RIGHT_SIDE;

    public SolutionMode invert() {
        return switch (this) {
            case FOR_RIGHT_SIDE -> FOR_LEFT_SIDE;
            case IDENTITY -> IDENTITY;
            case FOR_LEFT_SIDE -> FOR_RIGHT_SIDE;
        };
    }
}