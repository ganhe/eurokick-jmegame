/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package football.gameplay.info

/**
 *
 * @author cuong.nguyenmanh2
 */
class PlayerCustomizeSystem {
    static List shirtList=[
"acmilanhome1213fifa07.png",
"ajaxawayy.png",
"ajaxgk.png",
"ajaxhomebyjeanpi.png",
"arsenalhome1213fifa07.png",
"birmin3rd.png",
"blackpool3rd.png",
"bragagk.png",
"bragahome.png",
"brazilgk1214fifa07.png",
"cehiaaway1214fifa07.png",
"cehiagk1214fifa07.png",
"cehiahome1214fifa07.png",
"chelseahome1213v1fifa07.png",
"croatiaaway1214fifa07.png",
"croatiahome1214fifa07.png",
"englandgk1214fifa07.png",
"englandhome1214fifa07.png",
"italiagk1214fifa07v1.png",
"italiagk1214fifa07v2.png",
"muaway1213fifa07.png",
"muhome1213fifa07.png",
"reading3rd.png",
"realmadridaway1213fifa0.png",
"realmadridhome1213fifa0.png",
"romaniagk1214fifa07.png",
"romaniahome1214fifa07.png",
"santos3rd1213.png"
    ]
    static Random RND = new Random();
    static String getRandomShirt(){
        return "/Textures/shirt/"+shirtList[RND.nextInt(shirtList.size())];
    }
}

