package GUI;
import java.io.File;
import java.util.LinkedList;
import javax.swing.JFileChooser;
import javax.swing.JFrame;

public class CommonFiles {

    public final static String OPEN_MODE = "Open";
    public final static String SAVE_MODE = "Save";
    private static JFileChooser fileChooser = null;

    public static File getFile(JFrame jf, String dirpath, String mode) {
        //String dirpath = null;
        //if (filepath != null && filepath.length() > 0) {
        //    dirpath = filepath.substring(0, filepath.lastIndexOf("/"));
        //    System.out.println("CommonFiles - getFileToSave():  dirpath = " + dirpath);
        //}

        // If *dirpath* is null then opens default directory
        // (which on windows uses to be *Documents*)
        //fileChooser.setCurrentDirectory(new File(dirpath));
        fileChooser = new JFileChooser(dirpath);
        fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);

        // Show Dialog to Open/Save file
        int option = (mode.compareTo(CommonFiles.OPEN_MODE) == 0 ? fileChooser.showOpenDialog(jf) : fileChooser.showSaveDialog(jf));
        if (option == JFileChooser.APPROVE_OPTION) {
            File f = fileChooser.getSelectedFile();
            return f;
        }
        return null;
    }
}

// The javax.swing.filechooser.FileFilter is an abstract class, used to restrict
// the files that are shown in a JFileChooser; We must extend it and define the
// methods accept(f) and getDescription().
class ExtensionsFileFilter extends javax.swing.filechooser.FileFilter {

    private final LinkedList<String> listExtensions = new LinkedList<>();

    public ExtensionsFileFilter(String[] extensions) {
        for (String fext : extensions) {
            addExtension(fext);
        }
    }

    @Override
    public boolean accept(File f) {
        if (f.isDirectory()) {
            return true;
        } else {
            String filename = f.getName();
            String fileext = filename.substring(filename.indexOf('.') + 1);
            for (String fext : listExtensions) {
                if (fext.compareTo(fileext) == 0) {
                    return true;
                }
            }
            return false;
        }
    }

    @Override
    public String getDescription() {
        //System.out.println("List to string: " + this.listExtensions.toString());
        return "" + this.listExtensions.toString();
    }

    private void addExtension(String ext) {
        if (!this.listExtensions.contains(ext)) {
            this.listExtensions.addFirst(ext);
        }
    }
}
