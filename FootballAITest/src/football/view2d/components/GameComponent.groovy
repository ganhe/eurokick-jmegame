package football.view2d.components
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

import groovy.swing.SwingBuilder
import groovy.ui.ConsoleTextEditor
import groovy.swing.j2d.*

public class GameComponent{
    public void build(def swing){
        swing.panel(title:"Game"){
            borderLayout()
            panel(constraints:BL.CENTER){
                borderLayout()
                panel(constraints:BL.NORTH,preferredSize:[800,80]){
                    borderLayout()
                        
                    toolBar(constraints:BL.NORTH,background : Color.black,preferredSize:[800,30]){
                        button(text:"Back")
                        button(text:"Forward")
                        separator()
                        panel(preferredSize:[200,20]){
                            label(text:new Date().getDateString())
                            (1..5).each{
                                label(text:" "+it)
                            }
                        }
                    }
                    panel(constraints:BL.CENTER,preferredSize:[800,50],background : Color.blue){
                        borderLayout()
                        label(constraints:BL.CENTER,text:"Club name",foreground : Color.yellow)
                        label(constraints:BL.EAST,text:"Search",foreground : Color.yellow)
                    }
                }
                tabbedPane(constraints:BL.CENTER){          
                    panel(title:"Overview",tabIcon:createIcon("icon/mimi/Home24.png")){
                        tableLayout {
                            tr {
                                td {
                                    panel(border:lineBorder(color:Color.black),background:new Color(180,50,50),preferredSize:[200,600]){
                                        boxLayout(axis:BoxLayout.Y_AXIS)
                                        label(text:"Club Details",font:blueBigFont,foreground : Color.blue)
                                        label(icon:createIcon("clubs/premier/logos/arsenal-logo.png"),preferredSize:[120,200])
                                               
                                        label(text:"Arsenal", horizontalAlignment : JLabel.CENTER_ALIGNMENT,font:smallFont)
                     
                                        label(text:"England", horizontalAlignment : JLabel.CENTER_ALIGNMENT,font:smallFont)
                      
                                        label(text:"1890", horizontalAlignment : JLabel.CENTER_ALIGNMENT,font:smallFont)
                                                
                                    }
                                }
                                td {
                                    //label(text:"Club name")
                                }
                            }
                                
                        }
                    }
                    panel(title:"Squad",tabIcon:createIcon("icon/mimi/Home24.png")){
                        borderLayout()
                        scrollPane {
                            table (constraints:BL.CENTER){
                                tableModel( list : playerData ) {
                                    propertyColumn(header:'First Name', propertyName:'first')
                                    propertyColumn(header:'last Name', propertyName:'last')
                                }
                            }
                        }
                    }
                    panel(title:"Tattic",tabIcon:createIcon("icon/mimi/Home24.png")){

                    }
                    panel(title:"Contact",tabIcon:createIcon("icon/mimi/Home24.png")){
                        tableLayout {
                            tr {
                                td {
                                    label 'Street:'  // text property is default, so it is implicit.
                                }
                                td {
                                    textField "address.street", id: 'streetField', columns: 20
                                }
                            }
                            tr {
                                td {
                                    label 'Number:'
                                }
                                td {
                                    textField id: 'numberField', columns: 5, text: "address.number"
                                }
                            }
                            tr {
                                td {
                                    label 'City:'
                                }
                                td {
                                    textField id: 'cityField', columns: 20, "address.city"
                                }
                            }
                        }
                    }
                }
                    
            }
            
        }
    }
}
