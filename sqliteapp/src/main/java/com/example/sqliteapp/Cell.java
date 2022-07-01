package com.example.sqliteapp;

import android.widget.TextView;

public class Cell {

    int hor; //координаты ячейки по горизонтали
    int ver; //координаты ячейки по вертикали
    TextView cellId; //вью ячейки
    String letter; //буква, которую нужно вставить

    public Cell(int hor, int ver, TextView cellId, String letter) {
        this.hor = hor;
        this.ver = ver;
        this.cellId = cellId;
        this.letter = letter;
    }

    public Cell(){
    }

    // методы

    public int getHor() {
        return hor;
    }

    public void setHor(int hor) {
        this.hor = hor;
    }

    public int getVer() {
        return ver;
    }

    public void setVer(int ver) {
        this.ver = ver;
    }

    public TextView getCellId() {
        return cellId;
    }

    public void setCellId(TextView cellId) {
        this.cellId = cellId;
    }

    public String getLetter() {
        return letter;
    }

    public void setLetter(String letter) {
        this.letter = letter;
    }
}
