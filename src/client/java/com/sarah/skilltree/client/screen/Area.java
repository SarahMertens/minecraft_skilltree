package com.sarah.skilltree.client.screen;

/** A rectangle on the screen, in GUI pixels. */
record Area(int x, int y, int width, int height) {

    int right() {
        return x + width;
    }

    int bottom() {
        return y + height;
    }

    int centerX() {
        return x + width / 2;
    }

    boolean contains(double pointX, double pointY) {
        return pointX >= x && pointX < right() && pointY >= y && pointY < bottom();
    }
}
