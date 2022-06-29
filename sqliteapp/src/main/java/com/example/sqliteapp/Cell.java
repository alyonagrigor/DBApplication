package com.example.sqliteapp;

public class Cell {

    int hor;
    int ver;
    int direction;
    String cellId;
    char letter;

    public Cell(int hor, int ver, int direction, String cellId, char letter) {
        this.hor = hor;
        this.ver = ver;
        this.direction = direction;
        this.cellId = cellId;
        this.letter = letter;
    }

    public Cell(){
    }

    // методы
    public void putChar () {
        //binding.cellId.setText(letter);
    }

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

    public int getDirection() {
        return direction;
    }

    public void setDirection(int direction) {
        this.direction = direction;
    }

    public String getCellId() {
        return cellId;
    }

    public void setCellId(String cellId) {
        this.cellId = cellId;
    }

    public char getLetter() {
        return letter;
    }

    public void setLetter(char letter) {
        this.letter = letter;
    }
}
