package test.gpars.forkjoin
import static groovyx.gpars.GParsPool.runForkJoin
import static groovyx.gpars.GParsPool.withPool

def quicksort(numbers) {
    withPool {
        runForkJoin(0, numbers) {index, list ->
            def groups = list.groupBy {it <=> list[list.size().intdiv(2)]}
            if ((list.size() < 2) || (groups.size() == 1)) {
                return [index: index, list: list.clone()]
            }
            (-1..1).each {
                forkOffChild(it, groups[it] ?: [])
            }
            return [index: index, list: childrenResults.sort {it.index}.sum {it.list}]
        }.list
    }
}
(1..5).each{
    long startTime = System.nanoTime()
    def nums = [5,10,150,12,1,28,9,87,8,21,2,39,4,654,64,4,0,24,5,4,564,7]
    quicksort(nums)
    println(quicksort(nums))
    double takeTime =(System.nanoTime() - startTime) / 10E9
    println(it + "Take "+takeTime)
}