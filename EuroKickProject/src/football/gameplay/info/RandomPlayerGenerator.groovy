package football.gameplay.info

import football.gameplay.info.*;

/**
 *
 * @author cuong.nguyenmanh2
 */
class RandomPlayerGenerator {
    Random RND = new Random();
    def names = 'and marc son bert wick ness ton shire step ley ing sley';
    def namePattern = names.split(' ');
    
    public String getRandomName(){
        String rname = ((0..2).collect { 
                namePattern[RND.nextInt(namePattern.size())] 
            }.join('').capitalize() + ' ' + (0..3).collect { 
                namePattern[RND.nextInt(namePattern.size())] 
            }.join('').capitalize())
        
        return rname
    }
    
    public FootballPlayerInfo getRandomPlayerInfo(FootballPlayerInfo info){
        info.name = getRandomName();
        info.speed = 6+RND.nextInt(3);
        info.role = PlayerRole.Attacker //URandom.random(PlayerRole.class);
        println info.role
        info.skillSpeed =4+RND.nextInt(3);
        info.skillBallControl =6+RND.nextInt(3);
        info.skillBallTake =5+RND.nextInt(3);
        info.skillBallKeep =6+RND.nextInt(3);
        info.skillGoalKeep =4+RND.nextInt(3);
        info.skillPass =3+RND.nextInt(3);
        info.energy =6+RND.nextInt(3);
        info.attitude =6+RND.nextInt(3);
        info.birthDate = new Date();
        return info;
    }
}

