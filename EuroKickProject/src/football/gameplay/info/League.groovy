package football.gameplay.info

/**
 * @author cuong.nguyenmanh2
 */
public class League {
    String orgName;
    String orgLogo;
    String leagueName;
    
    List<FootballClub> participants = [];
    List<FootballClub> ranks = [];
    
    int currentRoundNum = 1;
    // Season time
    int seasonNum = 30;
    Date startTime;
    Date endTime; // of the whole season
    List<Round> rounds = [];
    
    League(String leagueName){
        this.leagueName = leagueName;
    }
    public void init(){
        scheduleSeason();
    }
    public class Round{
        int num;
        Date startTime;
        Round(int num){
            this.num = num;
        }
        /** The schedule list. */
        List<FootballMatch> matches = [];
    }
    
    public fillRandomClub(){
        for (int i=0;i<10;i++){
            FootballClub newClub =  new FootballClub("Club " + i);
            newClub.fillPlayerList();
            newClub.hireACoach();
            participants <<newClub;
        }
    }
    
    public void scheduleSeason(){
        this.startTime = new Date()
        createSchedule(participants)
    }
    
    /** Returns the match list (schedule)
     */

    List<FootballMatch> getListOfMatches(FootballClub club){
        def l=[]
        rounds.each{round->
            l<<round.matches.find{match->(match.clubA==club || match.clubB==club)}
        }
        return l;
    }
    List<FootballMatch> getRoundMatch(int roundIndex){
        return rounds[roundIndex].matches
    }
    def bestPlayer(){
        
    }
    
    void refreshRankTable(){
        
    }
    

    /** Creates the entire schedule
     *
     * @param participants List of participants for whom to create the schedule.
     */
    private void createSchedule(List participants) {
        if (participants.size() % 2 == 1) {
            // Number of teams uneven ->  add the bye team.
        }
        for(int i = 1;i<participants.size();i++) {
            createOneRound(i, participants);
        }
    }

    /** Creates one round, i.e. a set of matches where each team plays once.
     *
     * @param round Round number.
     * @param list List of teams
     */
    private void createOneRound(int roundIndex, ArrayList clubs) {
        int size = clubs.size();
        int mid =  size / 2;
        // Split list into two
       
        def l1 = [],l2 = [],l3=[],l4=[],l5=[]
        l4.addAll(clubs.subList(0,mid))
        l4.addAll(clubs.subList(mid,size).reverse())
        //println l4
        l3.add(l4[0])
        l5 = l4.subList(1,size)
        Collections.rotate(l5,roundIndex)
        l3.addAll(l5)
        l1.addAll(l3.subList(0,mid))
        l2.addAll(l3.subList(mid,size))
        
        Round newRound = new Round(roundIndex);
        newRound.startTime = this.startTime + 7 * roundIndex
        (0..mid-1).each{i->
            FootballMatch newMatch = new FootballMatch(l1[i],l2[i]);
            newMatch.startTime = newRound.startTime + i
            newRound.matches << newMatch
            
        }
        
        rounds<<newRound
        /*
        println "Round "+ round + "==============================";
        l1.each{ club ->
            print club.name +" | ";
        }
        println "";
        println "--------------------------------------------";
        l2.each{ club ->
            print club.name +" | ";
        }
        println "";
        println "--------------------------------------------";
        */
    }

    public FootballMatch getNextMatch(FootballClub club){
        List roundMatches = getRoundMatch(currentRoundNum);
        return roundMatches.find{FootballMatch match -> (match.clubA == club || match.clubB == club )};
    }
    public List<FootballMatch> ifJoin(FootballClub club){
        
    }
    
    public registerJoin(FootballClub club){
        
    }

}

