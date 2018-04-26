package controller;

import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.FileOutputStream;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.WindowConstants;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import views.MyFrame;

public class Controller {

  private final MyFrame frame = new MyFrame();
  
  private boolean fileLoaded = false;
  private boolean saveAsFlag = false;
  private boolean saveFlag = false;
  private String match = "";
  private boolean isModified = false;
  
 
  
  private static JFileChooser getFileChooser() {
      JFileChooser chooser = new JFileChooser();
      
      chooser.setCurrentDirectory(new File(System.getProperty("user.dir")));
      
      chooser.addChoosableFileFilter(
        new FileNameExtensionFilter("Editable Files", "txt"));
      
      chooser.setAcceptAllFileFilterUsed(false);
      
      return chooser;
  }
  
  public Controller() { // START CONSTRUCTOR
    frame.setTitle( "Editor" );
    frame.setLocationRelativeTo(null);
    frame.getEditorArea().setEditable(false); // Not editable until file opened or new file selected
    frame.getModFlag().setText("  ");

    
//--------------------------------------------------------------------------
//  O P E N     F I L E
//--------------------------------------------------------------------------  
    frame.getOpenFile().addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        
          if(discardDialog() == true){
          frame.getEditorArea().setEditable(true);
        
        JFileChooser chooser = Controller.getFileChooser();

        // invoke the chooser dialog for opening a file
        int status = chooser.showOpenDialog(frame);

        // test for approval
        if (status != JFileChooser.APPROVE_OPTION) {
          return;
        }
        File file = chooser.getSelectedFile();
        Path path = file.toPath();

        try {
          System.out.println("\n--------- Load -------------");

          System.out.println("full path: " + path);

          Path working = Paths.get(System.getProperty("user.dir"));

          System.out.println("working path: " + working);

          Path relative = working.relativize(path);

          System.out.println("relative path: " + relative);

          String content = new String(Files.readAllBytes(path));
          frame.getEditorArea().setText(content);
          frame.getEditorArea().setCaretPosition(0);

          fileLoaded = true;
          
          frame.getFileName().setText(relative.toString());
          
          match = content; // Update match  
        }
        catch (IOException ex) {
          ex.printStackTrace(System.err);
          JOptionPane.showMessageDialog(frame, "Cannot open file " + file);
        }   
      }
      }
    });
//--------------------------------------------------------------------------
//  N E W     F I L E
//-------------------------------------------------------------------------- 
    frame.getNewFile().addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            //check to see if okay to discard changes first
            if (discardDialog()) {
                frame.getEditorArea().setEditable(true);
                saveAsFlag = true;
                saveFlag = false;
                fileLoaded = false;
                match = "";
                frame.getEditorArea().setText("");
                frame.getFileName().setText("<FILE NAME>");
            }               
        } // ENDS actionPerformed()
    }); // ENDS frame.getNewFile().addActionsListener()

//--------------------------------------------------------------------------
//  S A V E     F I L E
//-------------------------------------------------------------------------- 
    frame.getSave().addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
           
           File file = new File(frame.getFileName().getText());
           try{
            FileOutputStream istream = new FileOutputStream(file, false);
            String content = frame.getEditorArea().getText();
            istream.write(content.getBytes());
            istream.close(); 
            match = content;
           }
           catch(Exception ex){
            ex.printStackTrace(System.err);
            JOptionPane.showMessageDialog(frame, "Cannot save file " + file);   
           }
         
        }   
    });
//--------------------------------------------------------------------------
//  S A V E     A S    F I L E
//--------------------------------------------------------------------------
    frame.getSaveAs().addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            JFileChooser chooser = Controller.getFileChooser();
            
            int status = chooser.showSaveDialog(frame);
            
            if(status != JFileChooser.APPROVE_OPTION) {
                return;
            }
            
            File file = chooser.getSelectedFile();

            File test = new File(System.getProperty("user.dir"));
             
            
            // Check if it has file extension, if not append .txt
            int lastDot = file.getName().lastIndexOf(".");
  
            String extension = file.getName().substring(lastDot + 1); 
            
            if( !(file.getName().contains(extension)) || lastDot == -1 ){   // working
                StringBuilder fileName = new StringBuilder();                              
                fileName.append(file.getName());                  
                fileName.append(".txt");
                file = new File(System.getProperty("user.dir"), fileName.toString());                
            }
            Path path = file.toPath();
            
            try {
                System.out.println("\n--------- Save to -----------");
                
                System.out.println("full path: " + path);
                
                Path working = Paths.get(System.getProperty("user.dir"));
                
                System.out.println("working path: " + working);
                
                Path relative = working.relativize(path);
                
                System.out.println("relative path: " + relative);
                
                String content = frame.getEditorArea().getText();
                 
                
                File[] files = test.listFiles();
                boolean alreadyExists = false;
                for(File f : files){
                    //System.out.println(f.getName());
                    if(f.getName().equals(relative.toString())){
                       //overwriteDialog(); 
                       alreadyExists = true;
                    } 
                    //System.out.println(f);                   
                } 
                boolean ok = false;
                if(alreadyExists){
                   ok = overwriteDialog();
                }                   
                if(ok == true || alreadyExists==false ){
                    Files.write(path, content.getBytes()); // Write file
                    saveFlag = true; // Enable Save button
                    frame.getModFlag().setText(""); // Reset modified box               
                    match = content; // Update match                
                    frame.getFileName().setText(relative.toString()); // Update file name box
                }
            }
            catch(IOException ex) {
                ex.printStackTrace(System.err);
                JOptionPane.showMessageDialog(frame, "Cannot open file " + file);
            }
        }
    });
    
//--------------------------------------------------------------------------
//  E N A B L E / D I S A B L E     M E N U     I T E M S
//--------------------------------------------------------------------------
    frame.getFileMenu().addMenuListener(new MenuListener() {
        @Override
        public void menuSelected(MenuEvent e) {
            // Enable "Save" and "Save As" if file has been loaded
            if(!fileLoaded) {
                frame.getSaveAs().setEnabled(false);
                frame.getSave().setEnabled(false); 
            }
            else {
                frame.getSaveAs().setEnabled(true);
                frame.getSave().setEnabled(true); 
            }
            // Enable "Save As" if file is new
            if(saveAsFlag) {        
                frame.getSaveAs().setEnabled(true);
            }
            // Enable "Save" if new file has been "Saved As"
            if(saveFlag) {                               // if(saveFlag && modFlag) to verify file has been modified
                frame.getSave().setEnabled(true);
            }
        }

        @Override
        public void menuDeselected(MenuEvent e) {
          
        }

        @Override
        public void menuCanceled(MenuEvent e) {
            
        }
    });
    
//--------------------------------------------------------------------------
//  E X I T    A P P L I C A T I O N
//--------------------------------------------------------------------------
    frame.addWindowListener(new WindowAdapter() {
        @Override
        public void windowClosing(WindowEvent evt) {
            if (isModified == true) {
                if(discardDialog() == true)
                    System.exit(0);
                else
                    frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
//                int choice = JOptionPane.showConfirmDialog(
//                        frame, "OK to discard changes?", "Select and Option", JOptionPane.YES_NO_CANCEL_OPTION);
//                if (choice == 0) {
//                    System.exit(0);
//                } else if (choice == 1 || choice == 2) {
//                    frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
//                }
            } else
                System.exit(0);
        }
    });


} // END constructor
//--------------------------------------------------------------------------
//  P R I V A T E    F U N C T I O N S
//--------------------------------------------------------------------------
  private boolean discardDialog() {
        boolean resetOK = true;
        if (isModified == true) {
            int choice = 4; //arbitrary default value
            choice = JOptionPane.showConfirmDialog(
                    frame, "OK to discard changes?", "Select and Option", JOptionPane.YES_NO_CANCEL_OPTION);
            switch (choice) {
                case 0:
                    break;
                case 1:
                    resetOK = false;
                    break;
                case 2:
                    resetOK = false;
                    break;
                default:
                    resetOK = false;
            }
        }
        return resetOK;
    }
  
    private boolean overwriteDialog() {
        boolean resetOK = true;
        int choice = 4; //arbitrary default value
        choice = JOptionPane.showConfirmDialog(
                frame, "OK to overwrite existing file?", "Select and Option", JOptionPane.YES_NO_CANCEL_OPTION);
        switch (choice) {
            case 0:
                break;
            case 1:
                resetOK = false;
                break;
            case 2:
                resetOK = false;
                break;
            default:
                resetOK = false;
        }
        return resetOK;
    }
//--------------------------------------------------------------------------
//  P U B L I C    F U N C T I O N S
//--------------------------------------------------------------------------
  public void test(){
      if(frame.getEditorArea().getText().equals(match)){
          frame.getModFlag().setText("  ");
          isModified = false;
      }
      else {
          frame.getModFlag().setText(" * ");
          isModified = true;
      }
  }

  public static void main(String[] args) {
    Controller app = new Controller();
    app.frame.setVisible(true);
    
    while(true){
        app.test();
    }
  } // END main
} // END Controller.java
