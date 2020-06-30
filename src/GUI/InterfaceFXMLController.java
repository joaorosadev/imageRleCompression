package GUI;

import javafx.event.ActionEvent;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;

public class InterfaceFXMLController {
    public MenuItem openFileButton;
    public TextArea rMatrixTextArea;
    public TextArea gMatrixTextArea;
    public TextArea bMatrixTextArea;
    public TextArea rCompressedTextArea;
    public TextArea gCompressedTextArea;
    public TextArea bCompressedTextArea;
    public TextField rFlag;
    public TextField gFlag;
    public TextField bFlag;
    public ImageView origImage;
    public ImageView finImage;
    public TextArea origImgTextArea;
    public TextArea finalImgTextArea;
    public TextArea racioValueTextArea;
    private CommonFiles cF = new CommonFiles ();

    public void handleOpenFileAction(ActionEvent actionEvent) throws IOException {
        File originalImage = cF.getFile (new JFrame (), "", "Open");

        folders ();

        if (originalImage != null) {
            BufferedImage img;
            try {
                img = ImageIO.read (originalImage);

                int[][] rColorArray = new int[img.getHeight ()][img.getWidth ()];
                int[][] gColorArray = new int[img.getHeight ()][img.getWidth ()];
                int[][] bColorArray = new int[img.getHeight ()][img.getWidth ()];

                //Frequency Arrays
                int[] rArray = new int[256];
                int[] gArray = new int[256];
                int[] bArray = new int[256];

                int x;

                for (x = 0; x < 256; x++) {
                    rArray[x] = 0;
                    gArray[x] = 0;
                    bArray[x] = 0;
                }

                int r, g, b;

                for (int i = 0; i < img.getWidth (); i++) {
                    for (int j = 0; j < img.getHeight (); j++) {
                        //Get RGB Color on each pixel
                        Color c = new Color (img.getRGB (i, j));
                        r = c.getRed ();
                        g = c.getGreen ();
                        b = c.getBlue ();

                        rColorArray[j][i] = r;
                        rArray[r]++;
                        gColorArray[j][i] = g;
                        gArray[r]++;
                        bColorArray[j][i] = b;
                        bArray[r]++;
                    }
                }


                //[0] = flag; [1] = flagSub
                int[] flagInfoR = findFlag (rArray, img);
                int[] flagInfoG = findFlag (gArray, img);
                int[] flagInfoB = findFlag (bArray, img);

                subFlagValues (rColorArray, img, flagInfoR[0], flagInfoR[1]);
                subFlagValues (gColorArray, img, flagInfoG[0], flagInfoG[1]);
                subFlagValues (bColorArray, img, flagInfoB[0], flagInfoB[1]);

                System.out.println ("RED");
                ArrayList<Byte> rMatrixCompressed = colorArrayCompressedFileCreation (img, rColorArray, flagInfoR[0]);
                System.out.println ("GREEN");
                ArrayList<Byte> gMatrixCompressed = colorArrayCompressedFileCreation (img, gColorArray, flagInfoG[0]);
                System.out.println ("BLUEl");
                ArrayList<Byte> bMatrixCompressed = colorArrayCompressedFileCreation (img, bColorArray, flagInfoB[0]);

                ArrayList<Byte> compressedImage = new ArrayList<> ();

                int bytes_in_r_compressed = rMatrixCompressed.size ();
                int bytes_in_g_compressed = gMatrixCompressed.size ();
                int bytes_in_b_compressed = bMatrixCompressed.size ();

                compressedImage.addAll (rMatrixCompressed);
                compressedImage.addAll (gMatrixCompressed);
                compressedImage.addAll (bMatrixCompressed);

                writeToBinFile (compressedImage, "C://RLE//src//data//compressedImage.bin");

                DecompressedImage di = decompress (compressedImage, bytes_in_r_compressed, bytes_in_g_compressed, bytes_in_b_compressed, "C://RLE//src//data//compressedImage.bin");

                int[][] rColorMatrix = rebuildColorMatrix (img, di.getR (), flagInfoR[0]);
                int[][] gColorMatrix = rebuildColorMatrix (img, di.getG (), flagInfoG[0]);
                int[][] bColorMatrix = rebuildColorMatrix (img, di.getB (), flagInfoB[0]);

                File finalImgFile = recreateImage (rColorMatrix, gColorMatrix, bColorMatrix, img);

                File finalBinFile = new File ("C://RLE//src//data//compressedImage.bin");
                double ratio = ratio ((double) (originalImage.length ()), (double) (finalBinFile.length ()));

                System.out.println ("Ratio: " + ratio);

                appendTextAreaMatrix (rMatrixTextArea, rColorArray, img.getWidth (), img.getHeight ());
                appendTextAreaMatrix (gMatrixTextArea, gColorArray, img.getWidth (), img.getHeight ());
                appendTextAreaMatrix (bMatrixTextArea, bColorArray, img.getWidth (), img.getHeight ());

                rFlag.setText ((findMin (rArray, img.getWidth (), img.getHeight ())).toString ());
                gFlag.setText ((findMin (gArray, img.getWidth (), img.getHeight ())).toString ());
                bFlag.setText ((findMin (bArray, img.getWidth (), img.getHeight ())).toString ());

                appendTextAreaCompressedArray (rCompressedTextArea, rMatrixCompressed);
                appendTextAreaCompressedArray (gCompressedTextArea, gMatrixCompressed);
                appendTextAreaCompressedArray (bCompressedTextArea, bMatrixCompressed);

                Image i1 = new Image (originalImage.toURI ().toString ());
                origImage.setImage (i1);
                Image i2 = new Image (finalImgFile.toURI ().toString ());
                finImage.setImage (i2);

                Float faux = (float) originalImage.length ();
                origImgTextArea.setText (faux.toString ());
                faux = (float) finalBinFile.length ();
                finalImgTextArea.setText (faux.toString ());
                faux = (float) ratio;
                racioValueTextArea.setText ("1:" + faux.toString ());


            } catch (IOException e) {
                e.printStackTrace ();
            }
        }
    }

    /**
     * Cria um diretório em C:/RLE/src/data para armazenar o ficheiro comprimido e imagem final
     */
    private void folders() {
        String path = "C://RLE";
        File filePath = new File (path);
        if (!filePath.exists ()) {
            if (filePath.mkdir ()) {
                System.out.println (filePath.getPath () + " created!");
            } else {
                System.out.println (filePath.getPath () + " not created!");
            }
        }else {
            System.out.println (filePath.getPath () + " no need to be created!");
        }
        String[] aux = {"src", "data"};
        for (int i = 0; i < 2; i++) {
            path = path + "//" + aux[i];
            filePath = new File (path);
            if (!filePath.exists ()) {
                if (filePath.mkdir ()) {
                    System.out.println (filePath.getPath () + " created!");
                } else {
                    System.out.println (filePath.getPath () + " not created!");
                }
            }else {
                System.out.println (filePath.getPath () + " no need to be created!");
            }
        }
    }

    /**
     * Permite recolher a informação da matriz de intensidade e apresentar na TextArea da interface em formato texto
     *
     * @param matrixTextArea TextArea da interface grafica que representa o matriz a comprimir
     * @param matrix         matriz de intensidades de um componente RGB extraido da imagem inicial
     * @param l              numero de linhas da matriz
     * @param c              numero de colunas da matriz
     */
    private void appendTextAreaMatrix(TextArea matrixTextArea, int matrix[][], int l, int c) {
        StringBuilder fieldContent = new StringBuilder ("");
        int i, j;
        for (i = 0; i < c; i++) {
            for (j = 0; j < l; j++) {
                if (j == 0) {
                    fieldContent.append ("|" + matrix[i][j] + "|");
                } else {
                    fieldContent.append ("|" + matrix[i][j] + "|");
                }

            }
            fieldContent.append ("\n");
        }
        matrixTextArea.setText (fieldContent.toString ());
    }

    /**
     * Permite recolher a informação arraylist de bytes e apresentar na TextArea da interface em formato texto
     *
     * @param matrixTextArea   TextArea da interface grafica que representa um arraylist de bytes
     * @param matrixCompressed ArrayList de bytes que representa a matriz de intensidades de um componente RGB comprimido
     */
    private void appendTextAreaCompressedArray(TextArea matrixTextArea, ArrayList<Byte> matrixCompressed) {
        StringBuilder fieldContent = new StringBuilder ("");
        int i, value;
        for (i = 0; i < matrixCompressed.size (); i++) {
            value = matrixCompressed.get (i) + 128;
            fieldContent.append ("|" + value + "|");
        }
        matrixTextArea.setText (fieldContent.toString ());
    }

    /**
     * Procura o menor número de presente no array de inteiro ou o primeiro 0 presente no array e retorna o indice
     *
     * @param ar array de inteiros que contem informação sobre as repetições de cada intensidade de um componente RGB
     * @param l  numero total de linhas de uma imagem
     * @param c  numero total de colunas de uma imagem
     * @return a intesidade com menor numero de repetiçoes
     */
    public static Integer findMin(int[] ar, int l, int c) {
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

    /**
     * Escolhe a intensidade de um componente RGB para representar a flag
     *
     * @param countArray
     * @param img
     * @return um array de inteiros, onde o indice 0 contém a intensidade para representar a flag e no indice 1 contém a intensidade substituta
     */
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

    /**
     * Percorre a matriz de um componente RGB. Se um valor da matriz for igual ao valor da flag, então este é substituido pelo valor da flagSub.
     *
     * @param colorMatrix matriz de um componente RGB
     * @param img         imagem original
     * @param flag        intensidade que representa a flag
     * @param flagSub     intensidade que vai substituir a intensidade que representa a flag
     */
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

    /**
     * Verifica o numero de repetições de uma intensidade numa matriz de intensidade de um componente RGB, a partir de uma posiçao de uma matriz
     *
     * @param valueRead   intensidade a verificar
     * @param l           posicao inicial em termos de linhas da matriz
     * @param c           posicao inicial em termos de colunas da matriz
     * @param colorMatrix matriz de intensidades de um componente RGB
     * @param lines       numero total de linhas da matriz
     * @param cols        numero total de colunas da matriz
     * @return o número de repetições da intesidade valueRead
     */
    public static int countRepeatedValues(int valueRead, int l, int c, int[][] colorMatrix, int lines, int cols) {
        int count = 1;

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

    /**
     * Cria um arraylist de bytes e verifica o numero de repetiçoes de uma intensidade numa matriz de um componente RGB.
     * Se o numero de repetiçoes for superior a 3, as intensidades repetidas são substituidas pelo valor da flag, numero de repetiçoes e a intensidade repetida.
     * O valor da flag, numero de repetições e a intensidade repetida são adicionados ao arraylist de bytes.
     * Caso contrario, adiciona a intensidade ao arraylist de bytes.
     *
     * @param img         imagem original
     * @param matrixColor matriz de um componente RGB
     * @param flag        intensidade que representa a flag
     * @return arraylist de byte que representa a matriz de um componente RGB comprimida
     */
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

    /**
     * Cria um ficheiro binario com a informaçao obtida de um arraylist de bytes
     *
     * @param colorCompressedArray arraylist de bytes
     * @param path                 diretorio onde o ficheiro vai ser criado
     */
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

    /**
     * Le a informaçao contida no ficheiro binario
     * @param compressed arraylist de bytes
     * @param rSize tamanho do array comprimido do componente red do RGB
     * @param gSize tamanho do array comprimido do componente green do RGB
     * @param bSize tamanho do array comprimido do componente blue do RGB
     * @return
     */
    public DecompressedImage decompress(ArrayList<Byte> compressed, int rSize, int gSize, int bSize, String path) {
        int i;
        DecompressedImage di = new DecompressedImage ();
        ArrayList<Byte> rDec = new ArrayList<> ();
        ArrayList<Byte> gDec = new ArrayList<> ();
        ArrayList<Byte> bDec = new ArrayList<> ();
        byte[] rgbByte=new byte[rSize+gSize+bSize+4];
        File f = null;
        FileInputStream fis = null;
        DataInputStream dis = null;

        try {
            f = new File (path);
            fis = new FileInputStream (f);
            dis = new DataInputStream (fis);
            dis.read (rgbByte);
        } catch (FileNotFoundException e) {
            e.printStackTrace ();
        } catch (IOException e) {
            e.printStackTrace ();
        }

        for (i = 4; i < rSize+4; i++) {
            rDec.add (rgbByte[i]);
        }
        for (i = rSize+4; i < rSize+gSize+4; i++) {
            gDec.add (rgbByte[i]);
        }
        for (i = rSize+gSize+4; i < rSize+gSize+bSize+4; i++) {
            bDec.add (rgbByte[i]);
        }

        di.setR (rDec);
        di.setG (gDec);
        di.setB (bDec);

        return di;
    }

    /**
     * Constoi a matriz para criar a imagem comprimida da informaçao recolhida do ficheiro binario
     * @param img imagem inicial
     * @param decompress arraylist de bytes da informaçao comprimida
     * @param flagValue intensidade/valor da flag
     * @return
     */
    public static int[][] rebuildColorMatrix(BufferedImage img, ArrayList<Byte> decompress, int flagValue) {
        int[][] colorMatrix = new int[img.getHeight ()][img.getWidth ()];
        int count = 0;
        for (int i = 0; i < img.getHeight (); i++) {
            for (int j = 0; j < img.getWidth (); j++) {
                if (count < decompress.size ()) {
                    //System.out.println ("Value: "+(decompress.get(count)+128));
                    if (decompress.get (count) == (byte) (flagValue - 128)) {
                        int numOfRepeatedValues = decompress.get (count + 1) + 128;
                        //System.out.println ("Repetições: "+(decompress.get(count+1)+128)+" Value: "+(decompress.get(count+2)+128));
                        for (int z = 0; z < numOfRepeatedValues; z++) {
                            //System.out.println ("i: " + i + " j: " + j+" value: "+(decompress.get (count + 2) + 128)
                            //        +" Repeticoes: "+(numOfRepeatedValues-z));
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
                        //System.out.println ("i: " + i + " j: " + j);
                        count += 3;
                        //Nao é flag
                    } else {
                        colorMatrix[i][j] = decompress.get (count++) + 128;
                        //System.out.println ("i: " + i + " j: " + (j+1));
                        //System.out.println (decompress.get (count - 1) + 128);
                    }
                }

            }
        }
        return colorMatrix;
    }

    /**
     * Calcula o racio de compressão
     *
     * @param orig       ficheiro a comprimir
     * @param compressed ficheiro comprimido
     * @return retorna o racio de compressão
     */
    public static double ratio(double orig, double compressed) {
        return orig / compressed;
    }

    /**
     * Constroi a imagem final
     * @param rMatrix matriz do componente red do RGB
     * @param gMatrix matriz do componente red do RGB
     * @param bMatrix matriz do componente red do RGB
     * @param img imagem original
     * @return retorna a imagem final
     */
    public static File recreateImage(int[][] rMatrix, int[][] gMatrix, int[][] bMatrix, BufferedImage img) {
        int r, g, b;
        BufferedImage finalImage = new BufferedImage (
                img.getWidth (), img.getHeight (), BufferedImage.TYPE_INT_RGB);

        /*BufferedImage redImage = new BufferedImage(
                img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_RGB);

        BufferedImage greenImage = new BufferedImage(
                img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_RGB);

        BufferedImage blueImage = new BufferedImage(
                img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_RGB);
         */

        for (int i = 0; i < img.getHeight (); i++) {
            for (int j = 0; j < img.getWidth (); j++) {
                //Get RGB Color on each pixel
                r = rMatrix[i][j];
                g = gMatrix[i][j];
                b = bMatrix[i][j];

                Color colorR = new Color (r, 0, 0);
                Color colorG = new Color (0, g, 0);
                Color colorB = new Color (0, 0, b);
                //redImage.setRGB (j,i,colorR.getRGB ());
                //greenImage.setRGB (j,i,colorG.getRGB ());
                //blueImage.setRGB (j,i,colorB.getRGB ());

                Color color = new Color (r, g, b);
                finalImage.setRGB (j, i, color.getRGB ());
            }
        }
        try {
            File f = new File ("C://RLE//src//data//finalImage.bmp");
            ImageIO.write (finalImage, "bmp", f);
            //ImageIO.write (redImage, "bmp", new File ("C://RLE//src//data//redImage.bmp"));
            //ImageIO.write (greenImage, "bmp", new File ("C://RLE//src//data//greenImage.bmp"));
            //ImageIO.write (blueImage, "bmp", new File ("C://RLE//src//data//blueImage.bmp"));
            return f;
        } catch (Exception e) {
        }
        return null;
    }


}
