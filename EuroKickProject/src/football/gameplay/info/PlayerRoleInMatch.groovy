package football.gameplay.info

/**
 *
 * @author cuong.nguyenmanh2
 */
       public class PlayerRoleInMatch {
        PlayerRole role;
        int rowNum;
        int rowTotal;
        int roleIndex;

        PlayerRoleInMatch(PlayerRole role, int rowNum, int rowTotal, int roleIndex) {
            this.role = role;
            this.rowNum = rowNum;
            this.rowTotal = rowTotal;
            this.roleIndex = roleIndex;
        }
    } 


