/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package football.gameplay.info

/**
 *
 * @author cuong.nguyenmanh2
 */
class FootballCommentator {
    String name;
    FootballCommentator(name){
        this.name = name;
    }
    void say(String text){
        println("say: " + text);
    }
}

