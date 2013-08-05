package football.gameplay.ai.info

import static football.geom.Utils.*
import groovy.transform.*

@CompileStatic
public class Regulator
{
    //the time period between updates 
    private double dUpdatePeriod;

    //the next time the regulator allows code flow
    private long dwNextUpdateTime;
    Random rnd = new Random();
    
    public Regulator(double NumUpdatesPerSecondRqd)
    {
        dwNextUpdateTime = (long)(timeGetTime()+RandFloat()*1000);

        if (NumUpdatesPerSecondRqd > 0)
        {
            dUpdatePeriod = 1000.0 / NumUpdatesPerSecondRqd; 
        }

        else if (isNumberEqual(0.0, NumUpdatesPerSecondRqd))
        {
            dUpdatePeriod = 0.0;
        }

        else if (NumUpdatesPerSecondRqd < 0)
        {
            dUpdatePeriod = -1;
        }
    }


    //returns true if the current time exceeds dwNextUpdateTime
    public boolean isReady()
    {
        //if a regulator is instantiated with a zero freq then it goes into
        //stealth mode (doesn't regulate)
        if (isNumberEqual(0.0, dUpdatePeriod)) return true;

        //if the regulator is instantiated with a negative freq then it will
        //never allow the code to flow
        if (dUpdatePeriod < 0) return false;

        long CurrentTime = timeGetTime();

        //the number of milliseconds the update period can vary per required
        //update-step. This is here to make sure any multiple clients of this class
        //have their updates spread evenly
        double UpdatePeriodVariator = 10.0;

        if (CurrentTime >= dwNextUpdateTime)
        {
            dwNextUpdateTime = (long)(CurrentTime + dUpdatePeriod + RandInRange(-UpdatePeriodVariator, UpdatePeriodVariator));

            return true;
        }

        return false;
    }
    
}
