package test.gpars.forkjoin

import static groovyx.gpars.GParsPool.runForkJoin
import static groovyx.gpars.GParsPool.withPool

withPool() { 
    println """Number of files: ${ 
        runForkJoin(new File("./src")) {file -> 
            long count = 0 
            file.eachFile { 
                if (it.isDirectory()) { 
                    println "Forking a child task for $it" 
                    forkOffChild(it) 
                    //fork a child task 
                } else { 
                    count++ 
                } 
            } 
            return count + (childrenResults.sum(0)) 
            //use results of children tasks to calculate and store own result 
        } 
    }""" 
}

