package football.view2d

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


stad= new Stadium2D();
cachedImages = [:]

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
    return swing.imageIcon(resource:"../images/"+path,class:FootballTestBed.class)
}

swing.build {
    jFrame = frame( title: 'Football Testbed', size: [1024,768],
        locationRelativeTo: null, show: true ,defaultCloseOperation: JFrame.EXIT_ON_CLOSE){
        //OyoahaLookAndFeel laf = new OyoahaLookAndFeel();
    
        //lookAndFeel(laf)
        //lookAndFeel(createNimRODLAF())
        def blueBigFont =  new Font("Times New Roman", Font.BOLD, 28);
        def smallFont =  new Font("SansSerif", Font.PLAIN, 20);
        
        menuBar(){
            menu(text:"File"){
                menuItem(text:"Exit",actionPerformed:{dispose()})
            }
        }
        tabbedPane{

            panel(title:"Editor"){
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
                        button(text:"Find")
                        
                        separator()
                        panel(preferredSize:[200,20]){
                            label(text:"Progress")
                            loadProgressBar=progressBar()
                        }
                    }
                }
                tabbedPane{

                    panel(title:"Stadium"){
                
                    }
                }
    
                tabbedPane(constraints:BL.SOUTH,preferredSize:[440,200]){
                    scrollPane(title:"Players"){
                        playersPanel = panel(){
                            boxLayout(axis:BoxLayout.X_AXIS)
                        }
                    }
                    scrollPane(title:"Log"){
                        log = textArea(text:"Log")
                    }
            
                }
                tabbedPane(constraints:BL.EAST){          
                    scrollPane(title:"Properties"){
                        panel{
                    
                        }
                    }
                }
            }

            panel(title:"Stadium"){
                borderLayout()
                panel(constraints:BL.CENTER){
                    borderLayout()
                    testBedComp=panel(constraints:BL.CENTER, new GraphicsPanel(),preferredSize:[440,300])
                    //testBedComp.addMouseWheelListener(new ZoomOnMouse(this))
                }
            }
        }
    }
}
void buildPlayerTree(){

    
    fbPlayerTree.model.root.removeAllChildren()

    def redTeamNode = new TreeNode("Red Team");
    fbPlayerTree.model.root.add(redTeamNode);
    stad.pitch.redgetTeam().getMembers().each{player->
        redTeamNode.add(new TreeNode("Red"+player.getID()));
    }
    
    def blueTeamNode = new TreeNode("Blue Team");
    fbPlayerTree.model.root.add(blueTeamNode);
    stad.pitch.bluegetTeam().getMembers().each{player->
        blueTeamNode.add(new TreeNode("Blue"+player.getID()));
    }
    
    fbPlayerTree.model.reload(fbPlayerTree.model.root)
    fbPlayerTree.getSelectionModel().addTreeSelectionListener(this as TreeSelectionListener);
    fbPlayerTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
}

public void valueChanged(TreeSelectionEvent event) {
}

class ZoomOnMouse implements MouseWheelListener{
    FootballTestBed main;
    ZoomOnMouse(FootballTestBed main){
        this.main = main;
    }
    public void mouseWheelMoved(MouseWheelEvent e) {
        String message;
        int notches = e.getWheelRotation();
        if (notches < 0) {
            if (e.getScrollType() == MouseWheelEvent.WHEEL_UNIT_SCROLL) {
                main.zoomUp(e.getScrollAmount())
            } else { //scroll type == MouseWheelEvent.WHEEL_BLOCK_SCROLL

            }
        } else {
            if (e.getScrollType() == MouseWheelEvent.WHEEL_UNIT_SCROLL) {
                main.zoomDown(e.getScrollAmount())
            } else { //scroll type == MouseWheelEvent.WHEEL_BLOCK_SCROLL

            }
        }

    }
}

void zoomUp(def amount){
    stad.zoomUp(amount)  
}
void zoomDown(def amount){
    stad.zoomDown(amount)  
}

def loadImage(path){
    //println path
    return swing.imageIcon(file :path)
}

testBedComp.go = stad.getDrawOp(testBedComp)
//buildPlayerTree();
stad.startGame();
