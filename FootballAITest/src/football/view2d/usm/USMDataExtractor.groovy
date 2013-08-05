package football.view2d.usm


import java.awt.*
import java.awt.event.*;
import java.awt.BorderLayout as BL
import static java.awt.Color.*
import javax.swing.*
import javax.swing.border.EmptyBorder
import javax.swing.tree.*
import javax.swing.tree.DefaultMutableTreeNode as TreeNode
import javax.swing.event.*
import javax.swing.text.Document

import com.nilo.plaf.nimrod.NimRODLookAndFeel
import com.nilo.plaf.nimrod.NimRODTheme

import groovy.swing.SwingBuilder
import groovy.ui.ConsoleTextEditor
import groovy.swing.j2d.*

import football.view2d.usm.hex.*

// create test byte Arrays
byte[] ar;
ar=new byte[16*16*100];
Arrays.fill(ar,(byte)0);

// Swing
swing = new SwingBuilder();
playerData = [[first:'qwer', last:'asdf'],
    [first:'zxcv', last:'tyui'],
    [first:'ghjk', last:'bnm']]
    
def createNimRODLAF(){
    
    NimRODTheme nt = new NimRODTheme();
    nt.setPrimary1( new Color(255,255,255));
    //nt.setPrimary2( new Color(20,20,20));
    //nt.setPrimary3( new Color(30,30,30));
    nt.setPrimary( new Color(0,150,250))
    nt.setBlack( new Color(255,255,250))
    nt.setWhite( Color.lightGray)
    nt.setSecondary( Color.gray)
 
    NimRODLookAndFeel NimRODLF = new NimRODLookAndFeel();
    NimRODLF.setCurrentTheme( nt);

    //lookAndFeel("com.nilo.plaf.nimrod.NimRODLookAndFeel")
    return NimRODLF;
     
}
 
def createIcon(String path){
    return swing.imageIcon(resource:"../../images/"+path,class:USMDataExtractor.class)
}

swing.build {

    jFrame = frame( title: 'USM Data extrator ', size: [1024,768],
        locationRelativeTo: null, show: true ,defaultCloseOperation: JFrame.EXIT_ON_CLOSE){
        //OyoahaLookAndFeel laf = new OyoahaLookAndFeel();
    
        //lookAndFeel(laf)
        //lookAndFeel(createNimRODLAF())

        //lookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel")
        
        fileOpenDialog  = fileChooser(dialogTitle:"Choose an Dat file")
        openFileAction  = action(name: 'Open file', 
            mnemonic: 'O',
            accelerator:KeyStroke.getKeyStroke(KeyEvent.VK_O, ActionEvent.CTRL_MASK),
            smallIcon:createIcon("icon/mimi/Home24.png"),
            closure:{
                int answer = fileOpenDialog.showOpenDialog(jFrame)
                if( answer== JFileChooser.APPROVE_OPTION) {
                    doOutside {
                        //println(fileOpenDialog.selectedFile.text )
                        openFile(fileOpenDialog.selectedFile)
                    }
                }
            }
        )
        menuBar(){
            menu(text:"File", mnemonic: 'F'){
                menuItem(text:"Open",action: openFileAction)
                separator()
                menuItem(text:"Exit",actionPerformed:{dispose()})
            }
        }
        borderLayout()
        /*
        scrollPane(constraints:BL.WEST,preferredSize:[200,100]){
        //Set the icon for leaf nodes.
        
        ImageIcon leafIcon = imageIcon("/Textures/icons/player-icon-blue.png");
        DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer();
        renderer.setLeafIcon(leafIcon);
        fbPlayerTree=tree(cellRenderer :renderer)
        
        
        }
         */
        panel(constraints:BL.NORTH){
            borderLayout()
            toolBar(constraints:BL.CENTER){
                flowLayout()
                txtAlign = textField("186",preferredSize:[100,25])
                button(text:"Align",actionPerformed:{
                        int aLineLength = txtAlign.text.toInteger();
                        //fetchToLog(aLineLength)
                        fetchToTable(aLineLength)
                    })
                        
                separator()
                panel(preferredSize:[200,20]){
                    label(text:"Progress")
                    loadProgressBar=progressBar()
                }
            }
        }
        tabbedPane{
            hexEditor = panel(title:"Data",new JHexEditor())

        }
    
        tabbedPane(constraints:BL.SOUTH,preferredSize:[440,300]){
            scrollPane(title:"Players"){
                pTable = table {
                    tableModel() {
                        propertyColumn(header:'First Name', propertyName:'name')
                        propertyColumn(header:'Last Name', propertyName:'lastName')
                        propertyColumn(header:'Position', propertyName:'position')
                        propertyColumn(header:'Keeping', propertyName:'keeping')
                        propertyColumn(header:'Tackling', propertyName:'tackling')
                        propertyColumn(header:'Passing', propertyName:'passing')
                        propertyColumn(header:'Shooting', propertyName:'shooting')
                        propertyColumn(header:' Pace', propertyName:'pace')
                        propertyColumn(header:'Heading', propertyName:'heading')
                        propertyColumn(header:' Stamina', propertyName:'stamina')
                        propertyColumn(header:'SetPieces', propertyName:'setPieces')
                        propertyColumn(header:'BallControl', propertyName:'ballControl')
                    }
                }
            }
            scrollPane(title:"Log"){
                log = textArea(text:"",font:new Font("Courier New", 0,12))
            }

        }
        tabbedPane(constraints:BL.EAST,preferredSize:[200,200]){          
            scrollPane(title:"Properties"){
                panel{
                    
                }
            }
        }

    }
}


void openFile(File f){
    //println(" Open file " + f.name)
    hexEditor.buff = f.readBytes();
    fetchToTable(186)
}

void fetchToLog(int aLineLength){
    int pos = 0;
    int endPos = 0;
    StringBuilder sb = new StringBuilder();
    while (pos < hexEditor.buff.size()){
                            
        endPos = (pos + aLineLength < hexEditor.buff.size())?pos + aLineLength:hexEditor.buff.size();
        //byte[] results = new byte[endPos - pos];
        //String aLine= new StringBuilder();
        for (int index=pos;index <endPos -1;index++){
            //results[index - pos] = hexEditor.buff[index];
            byte b = hexEditor.buff[index];
            String s = "" + new Character((char) b);
            if ((b < 20) || (b > 126)) {
                //s = "" + (char) 16;
                s=" "
            }
            sb.append(s);
        }
        //String aLine = new String(results,"US-ASCII")
        sb.append("\n");
                            
        pos = endPos + 1;
        println pos;
    }
    log.text += sb.toString();    
}

void fetchToTable(int aLineLength){
    int pos = 0;
    int endPos = 0;
    def data=hexEditor.buff;
    while (pos < data.size()){      
        endPos = (pos + aLineLength < hexEditor.buff.size())?pos + aLineLength:hexEditor.buff.size();
        
        // Fetch a row.
        // Player
        // Name
        def name=toStringFromBytes(pos,pos+13,data)
        // Empty name is the trigger to break the loop!
        if (name.toString().trim().isEmpty()){
            break;
        }
        def lastName=toStringFromBytes(pos+14,pos+26,data)
        
        // Create player
        def newPlayer = new FbPlayer();
        newPlayer.name = name;
        newPlayer.lastName =lastName;
        newPlayer.v1 = data[pos+29]
        newPlayer.v2 = data[pos+30]
        newPlayer.position = toAscii(data[pos+31]);
        newPlayer.keeping = data[pos+144]
        newPlayer.tackling = data[pos+145]
        newPlayer.passing = data[pos+146]
        newPlayer.shooting = data[pos+147]
        newPlayer.pace = data[pos+148]
        newPlayer.heading = data[pos+150]
        newPlayer.stamina = data[pos+151]
        newPlayer.setPieces = data[pos+152]
        newPlayer.ballControl = data[pos+153]
        
        // Add to row
        def rows = pTable.getModel().getRowsModel().getValue()
        rows.add(newPlayer)
        def rowsModel = pTable.getModel().getRowsModel()
        //rowsModel.setValue( rows )
        pTable.getModel().fireTableDataChanged()
        
        //--------------------------------------------------
        // NEXT
        pos = endPos + 1;
        //println pos;
        //println "|"+name.toString().trim()+"|";
    }
}
String toStringFromBytes(int from,int toPos,byte[] buff){
    def name=new StringBuilder();
    (from..toPos).each{index->
        byte b = buff[index];
        name.append(toAscii(b));
    }
    return name.toString();
}
String toAscii(byte b){
    String aChar = "" + new Character((char) b);
    if ((b < 20) || (b > 126)) {
        //s = "" + (char) 16;
        aChar=" ";
    }
    return aChar;
}
class FbPlayer{
    String name;
    String lastName;
    String position;
    int v1;
    int v2;
    int playerNumber,
    keeping,
    tackling,
    passing,
    shooting,
    pace,
    heading,
    stamina,
    setPieces,
    ballControl;
    
}