package com.example.sqliteapp;

import android.widget.TextView;

public class Cell {

    int ver; //координаты ячейки по вертикали
    int hor; //координаты ячейки по горизонтали
    TextView cellId; //вью ячейки
    String letter; //буква, которую нужно вставить

    public Cell(int ver, int hor, TextView cellId, String letter) {
        this.ver = ver;
        this.hor = hor;
        this.cellId = cellId;
        this.letter = letter;
    }

    public Cell(){
        }

    public int getVer() {
        return ver;
    }

    public void setVer(int ver) {
        this.ver = ver;
    }

    public int getHor() {
        return hor;
    }

    public void setHor(int hor) {
        this.hor = hor;
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
