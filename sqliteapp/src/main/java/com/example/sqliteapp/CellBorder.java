package com.example.sqliteapp;

public class CellBorder {
    int coordVer; //координаты ячейки в таблице по горизонтали
    int coordHor; // координаты ячейки в таблице по горизонтали
    float topBorder; //координаты границы в пикселях
    float rightBorder; //координаты границы в пикселях
    float bottomBorder; //координаты границы в пикселях
    float leftBorder; //координаты границы в пикселях
    private String name;

    public CellBorder(int coordVer, int coordHor, float topBorder, float rightBorder,
                      float bottomBorder, float leftBorder) {
        this.coordVer = coordVer;
        this.coordHor = coordHor;
        this.topBorder = topBorder;
        this.rightBorder = rightBorder;
        this.bottomBorder = bottomBorder;
        this.leftBorder = leftBorder;
    }


    public CellBorder(){
    }

/*    @NonNull
    @Override
    public String toString() {
        return getClass().getSimpleName() + "[name=" + name + "]";
    }*/

    public int getCoordVer() {return coordVer; }

    public void setCoordVer(int coordVer) {
        this.coordVer = coordVer;
    }

    public int getCoordHor() { return coordHor; }

    public void setCoordHor(int coordHor) {
        this.coordHor = coordHor;
    }

    public float getTopBorder() {
        return topBorder;
    }

    public void setTopBorder(int topBorder) {
        this.topBorder = topBorder;
    }

    public float getRightBorder() {
        return rightBorder;
    }

    public void setRightBorder(int rightBorder) {
        this.rightBorder = rightBorder;
    }

    public float getBottomBorder() {
        return bottomBorder;
    }

    public void setBottomBorder(int bottomBorder) {
        this.bottomBorder = bottomBorder;
    }

    public float getLeftBorder() {
        return leftBorder;
    }

    public void setLeftBorder(int leftBorder) {
        this.leftBorder = leftBorder;
    }

    }


