package com.example.sqliteapp;

public class RowBorder {

    float border; //координаты границы в пикселях
    int coord; //номер столбца или строки, в которой находится ячейка

    public RowBorder(float border, int coord) {
        this.border = border;
        this.coord = coord;
    }

    public RowBorder(){
    }

    public float getBorder() {
        return border;
    }

    public void setBorder(float border) {
        this.border = border;
    }

    public int getCoord() {
        return coord;
    }

    public void setCoord(int coord) {
        this.coord = coord;
    }
}
