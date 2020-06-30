package GUI;

import java.util.ArrayList;

public class DecompressedImage {
    private ArrayList<Byte> r;
    private ArrayList<Byte> g;
    private ArrayList<Byte> b;

    public DecompressedImage(){
        setR(new ArrayList<>());
        setG(new ArrayList<>());
        setB(new ArrayList<>());
    }

    public ArrayList<Byte> getR() {
        return r;
    }

    public void setR(ArrayList<Byte> r) {
        this.r = r;
    }

    public ArrayList<Byte> getG() {
        return g;
    }

    public void setG(ArrayList<Byte> g) {
        this.g = g;
    }

    public ArrayList<Byte> getB() {
        return b;
    }

    public void setB(ArrayList<Byte> b) {
        this.b = b;
    }
}
