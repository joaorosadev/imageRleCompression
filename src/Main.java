import GUI.InterfaceGUI;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;

public class Main {

    public static void main(String[] args) {
        InterfaceGUI gui = new InterfaceGUI ();
        gui.main (args);
/*
        //Image Source
        //File originalImage = new File (".//data//images//black_5x5.bmp");
        //File originalImage = new File (".//data//images//FLAG_B24.bmp");
        //File originalImage = new File (".//data//images//colored_image.bmp");
        File originalImage = new File (".//data//images//10_10.bmp");
        //File originalImage = new File (".//data//images//MARBLES.bmp");

        BufferedImage img = null;
        try {

            img = ImageIO.read (originalImage);

            System.out.println (img.getWidth () + " " + img.getHeight ());
            int[][] rcolorArray = new int[img.getHeight ()][img.getWidth ()];
            int[][] gcolorArray = new int[img.getHeight ()][img.getWidth ()];
            int[][] bcolorArray = new int[img.getHeight ()][img.getWidth ()];

            int[] rArray = new int[256];
            int[] gArray = new int[256];
            int[] bArray = new int[256];

            int x;

            for (x = 0; x < 256; x++) {
                rArray[x] = 0;
                gArray[x] = 0;
                bArray[x] = 0;
            }

            int r, g, b, a;

            for (int i = 0; i < img.getWidth (); i++) {
                for (int j = 0; j < img.getHeight (); j++) {
                    //Get RGB Color on each pixel
                    Color c = new Color (img.getRGB (i, j));
                    r = c.getRed ();
                    g = c.getGreen ();
                    b = c.getBlue ();

                    rcolorArray[j][i] = r;
                    rArray[r]++;
                    gcolorArray[j][i] = g;
                    gArray[r]++;
                    bcolorArray[j][i] = b;
                    bArray[r]++;

                }
            }

            System.out.println ("RED MATRIX:\n");
            printMatrix (rcolorArray, img.getWidth (), img.getHeight ());
            System.out.println ("GREEN MATRIX:\n");
            printMatrix (gcolorArray, img.getWidth (), img.getHeight ());
            System.out.println ("BLUE MATRIX:\n");
            printMatrix (bcolorArray, img.getWidth (), img.getHeight ());

            //[0] = flag; [1] = flagSub
            int[] flagInfoR = findFlag (rArray, img);
            int[] flagInfoG = findFlag (gArray, img);
            int[] flagInfoB = findFlag (bArray, img);

            subFlagValues (rcolorArray, img, flagInfoR[0], flagInfoR[1]);
            subFlagValues (gcolorArray, img, flagInfoG[0], flagInfoG[1]);
            subFlagValues (bcolorArray, img, flagInfoB[0], flagInfoB[1]);

            System.out.println ("RED");
            ArrayList<Byte> rMatrixCompressed = colorArrayCompressedFileCreation (img, rcolorArray, flagInfoR[0]);
            System.out.println ("GREEN");
            ArrayList<Byte> gMatrixCompressed = colorArrayCompressedFileCreation (img, gcolorArray, flagInfoG[0]);
            System.out.println ("BLUE");
            ArrayList<Byte> bMatrixCompressed = colorArrayCompressedFileCreation (img, bcolorArray, flagInfoB[0]);

            writeToBinFile (rMatrixCompressed, ".//data//rcompress.bin");
            writeToBinFile (gMatrixCompressed, ".//data//gcompress.bin");
            writeToBinFile (bMatrixCompressed, ".//data//bcompress.bin");
            ArrayList<Byte> rDecompressed = readFromBinFile (".//data//rcompress.bin");
            ArrayList<Byte> gDecompressed = readFromBinFile (".//data//gcompress.bin");
            ArrayList<Byte> bDecompressed = readFromBinFile (".//data//bcompress.bin");


            System.out.println ("\nRED");
            for (Byte r1 : rDecompressed)
                System.out.print ((r1 + 128) + ",");
            System.out.println ();
            for (Byte r1 : rMatrixCompressed)
                System.out.print ((r1 + 128) + ",");

            System.out.println ();
            System.out.println ("\nREBUILDING RED MATRIX");
            int[][] rColorMatrix = rebuildColorMatrix (img, rDecompressed, flagInfoR[0]);
            System.out.println ("REBUILDING GREEN MATRIX");
            int[][] gColorMatrix = rebuildColorMatrix (img, gDecompressed, flagInfoG[0]);
            System.out.println ("REBUILDING BLUE MATRIX");
            int[][] bColorMatrix = rebuildColorMatrix (img, bDecompressed, flagInfoB[0]);


            recreateImage (rColorMatrix, gColorMatrix, bColorMatrix, img);


        } catch (IOException e) {
            e.printStackTrace ();
        }
    }

    public static void printMatrix(int matrix[][], int l, int c) {
        int i, j;
        String s = null;
        for (i = 0; i < c; i++) {
            for (j = 0; j < l; j++) {
                if (j == 0) {
                    s = "|" + matrix[i][j] + "|";
                } else {
                    s = s + "|" + matrix[i][j] + "|";
                }
            }
            System.out.println (s);
        }
        System.out.println ("\n");
    }


    public static int findMin(int[] ar, int l, int c) {
        int minInd = -1, min = l * c + 1;
        for (int w = 0; w < 256; w++) {
            if (ar[w] == 0)
                return w;
            if (ar[w] < min) {
                min = ar[w];
                minInd = w;
            }
        }
        return minInd;
    }

    public static int countRepeatedValues(int valueRead, int l, int c, int[][] colorMatrix, int lines, int cols) {
        int count = 0;

        int i = l;
        for (int j = c; j < cols; j++) {
            if (count == 255) return count;
            if (j == cols - 1) {
                if (i == lines - 1) return count;
                if (colorMatrix[i + 1][0] == valueRead) {
                    i++;
                    j = -1;
                    count++;
                    // System.out.println (count);
                } else return count;
            } else {
                if (colorMatrix[i][j + 1] == valueRead) {
                    // System.out.println (colorMatrix[i][j+1]);
                    count++;
                    // System.out.println (count);
                } else return count;
            }
        }
        return count;
    }

    public static int[] findFlag(int[] countArray, BufferedImage img) {
        int[] returnValues = new int[2];
        System.out.println ("Indice/intensidade (flag) a substituir: " + findMin (countArray, img.getWidth (), img.getHeight ()));
        int flagValue = findMin (countArray, img.getWidth (), img.getHeight ());
        int flagSub = -1;
        //Caso a intensidade com count menor não seja zero. Regra geral: Usa-se o valor anterior
        if (countArray[flagValue] != 0 && flagValue != 0) {
            flagSub = flagValue - 1;
        }//Caso a intensidade com count menor não seja zero e tenha a intensidade 0. Regra especifica: Usa-se o valor seguinte
        else if (countArray[flagValue] != 0 && countArray[flagValue] == 0) {
            flagSub = flagValue + 1;
        }
        returnValues[0] = flagValue;
        returnValues[1] = flagSub;
        return returnValues;
    }

    public static void subFlagValues(int[][] colorMatrix, BufferedImage img, int flag, int flagSub) {
        //Substituição dos valores da flag (caso exista na matriz)
        for (int l = 0; l < img.getHeight (); l++) {
            for (int c = 0; c < img.getWidth (); c++) {
                if (flagSub != -1) { // Se existir na imagem
                    if (colorMatrix[l][c] == flag)
                        colorMatrix[l][c] = flagSub;
                }
            }
        }
    }

    public static ArrayList<Byte> colorArrayCompressedFileCreation(BufferedImage img, int[][] matrixColor, int flag) {
        //Criaçao do Array comprimido para uma dada cor (blue)
        ArrayList<Byte> compressed = new ArrayList<> ();
        for (int i = 0; i < img.getHeight (); i++) {
            for (int j = 0; j < img.getWidth (); j++) {
                int valueRead = matrixColor[i][j];
                int count = countRepeatedValues (valueRead, i, j, matrixColor, img.getHeight (), img.getWidth ());
                if (count > 3) {
                    compressed.add ((byte) (flag - 128));
                    compressed.add ((byte) (count - 128));
                    compressed.add ((byte) (valueRead - 128));
                    //Avançar na matriz count vezes
                    int dif = img.getWidth () - j;
                    if (count < dif) {
                        j += count - 1;
                    } else if (count == dif) {
                        //i++;
                        //j=-1;
                        j = img.getWidth () - 1;
                    } else if (count > dif) {
                        int count_menos_dif = count - dif;
                        int width = img.getWidth ();
                        //double linesToSkip = Math.ceil ((double)count_menos_dif/(double)width);

                        int colsToSkip = count_menos_dif % width;
                        int linesToSkip = ((count_menos_dif) - colsToSkip) / width;
                        i += linesToSkip + 1;
                        j = colsToSkip - 1;
                    }
                } else {
                    compressed.add ((byte) (valueRead - 128));
                }
                if (i >= img.getHeight () || j >= img.getWidth ()) break;
            }
        }
        return compressed;
    }

    public static void writeToBinFile(ArrayList<Byte> colorCompressedArray, String path) {
        //Escrita para o ficheiro comprimido
        File f = null;
        FileOutputStream fos = null;
        DataOutputStream dos = null;

        try {
            f = new File (path);
            fos = new FileOutputStream (f);
            dos = new DataOutputStream (fos);
            dos.writeInt (colorCompressedArray.size ());
            for (int i = 0; i < colorCompressedArray.size (); i++) {
                dos.writeByte (colorCompressedArray.get (i));
            }
        } catch (Exception e) {
            e.printStackTrace ();
        } finally {
            try {
                dos.close ();
                fos.close ();
            } catch (IOException e2) {
            }
        }
    }

    public static ArrayList<Byte> readFromBinFile(String path) {
        ArrayList<Byte> decompress = new ArrayList<> ();
        int compressSize = 0;
        int i;

        //Leitura do ficheiro comprimido
        File f1 = null;
        FileInputStream fis = null;
        DataInputStream dis = null;
        try {
            f1 = new File (path);
            fis = new FileInputStream (f1);
            dis = new DataInputStream (fis);
            compressSize = dis.readInt ();
            for (i = 0; i < compressSize; i++) {
                decompress.add (dis.readByte ());
            }
        } catch (Exception e) {
            e.printStackTrace ();

        } finally {
            try {
                dis.close ();
                fis.close ();
            } catch (IOException e2) {
            }
        }
        return decompress;
    }

    public static int[][] rebuildColorMatrix(BufferedImage img, ArrayList<Byte> decompress, int flagValue) {
        int[][] colorMatrix = new int[img.getHeight ()][img.getWidth ()];
        int count = 0;
        for (int i = 0; i < img.getHeight (); i++) {
            for (int j = 0; j < img.getWidth (); j++) {
                if (count < decompress.size ()) {
                    if (decompress.get (count) == (byte) (flagValue - 128)) {
                        int numOfRepeatedValues = decompress.get (count + 1) + 128;
                        for (int z = 0; z < numOfRepeatedValues; z++) {
                            colorMatrix[i][j] = (int) (decompress.get (count + 2) + 128);
                            if (z != numOfRepeatedValues - 1) {
                                if (j == img.getWidth () - 1) {
                                    i++;
                                    j = 0;
                                    if (i < img.getHeight ())
                                        continue;
                                    else {
                                        i--;
                                        j = img.getWidth () - 1;
                                    }
                                } else {
                                    j++;
                                }
                            }
                        }
                        count += 3;
                    } else {
                        colorMatrix[i][j] = decompress.get (count++) + 128;
                    }
                }

            }
        }
        return colorMatrix;
    }

    public static void recreateImage(int[][] rMatrix, int[][] gMatrix, int[][] bMatrix, BufferedImage img) {
        int r, g, b;
        BufferedImage finalImage = new BufferedImage (
                img.getWidth (), img.getHeight (), BufferedImage.TYPE_INT_RGB);

        BufferedImage redImage = new BufferedImage (
                img.getWidth (), img.getHeight (), BufferedImage.TYPE_INT_RGB);

        BufferedImage greenImage = new BufferedImage (
                img.getWidth (), img.getHeight (), BufferedImage.TYPE_INT_RGB);

        BufferedImage blueImage = new BufferedImage (
                img.getWidth (), img.getHeight (), BufferedImage.TYPE_INT_RGB);

        for (int i = 0; i < img.getHeight (); i++) {
            for (int j = 0; j < img.getWidth (); j++) {
                //Get RGB Color on each pixel
                r = rMatrix[i][j];
                g = gMatrix[i][j];
                b = bMatrix[i][j];

                Color colorR = new Color (r, 0, 0);
                Color colorG = new Color (0, g, 0);
                Color colorB = new Color (0, 0, b);
                redImage.setRGB (j, i, colorR.getRGB ());
                greenImage.setRGB (j, i, colorG.getRGB ());
                blueImage.setRGB (j, i, colorB.getRGB ());

                Color color = new Color (r, g, b);
                finalImage.setRGB (j, i, color.getRGB ());
            }
        }
        try {
            ImageIO.write (finalImage, "bmp", new File (".//data//finalImage.bmp"));
            ImageIO.write (redImage, "bmp", new File (".//data//redImage.bmp"));
            ImageIO.write (greenImage, "bmp", new File (".//data//greenImage.bmp"));
            ImageIO.write (blueImage, "bmp", new File (".//data//blueImage.bmp"));
        } catch (Exception e) {
        }
    }

    //APAGAR
    public static int countColorMatrix(BufferedImage img, ArrayList<Byte> decompress, int flagValue) {
        int[][] colorMatrix = new int[img.getHeight ()][img.getWidth ()];
        int count = 0, totalcount = 0;
        for (int i = 0; i < img.getHeight (); i++) {
            for (int j = 0; j < img.getWidth (); j++) {
                if (count >= decompress.size ()) break;
                if (decompress.get (count) == (byte) (flagValue - 128)) {
                    int numOfRepeatedValues = decompress.get (count + 1) + 128;
                    for (int z = 0; z < numOfRepeatedValues; z++) {
                        totalcount++;
                        if (z != numOfRepeatedValues - 1) {
                            if (j == img.getWidth () - 1) {
                                i++;
                                j = 0;
                            } else {
                                j++;
                            }
                        }
                    }
                    count += 3;
                } else {
                    totalcount++;
                }
            }
        }
        return totalcount;
    */
    }
}
