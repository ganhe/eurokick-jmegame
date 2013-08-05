package test
import football.gameplay.info.*
import static football.gameplay.info.PlayerRole.*

def league = new League()
league.fillRandomClub();
league.participants.each{club->
    println club.name
    club.playersList.each{info->
        println info;
    }
    println "Coach " +club.coach.name
    println "-------------------------------------"
}

league.scheduleSeason()
def club = league.participants[0]
println league.getNextMatch(club)
println "The list of matches for" + club.name
league.getListOfMatches(club).each{match->
    println match
}
/*
def bestGoalKeeper= club.playersList.findAll{player-> player.role == GoalKeeper}.max{player-> player.skillGoalKeep}
println bestGoalKeeper
 */

coach = club.coach
coach.arrangeTeam(null,club.playersList)
